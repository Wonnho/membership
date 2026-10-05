const test = require('node:test');
const assert = require('node:assert/strict');
const vm = require('node:vm');
const fs = require('node:fs');
const path = require('node:path');
const source = fs.readFileSync(path.resolve(__dirname, '../../main/resources/static/js/session-idle.js'), 'utf8');
function browser() {
    let now = 10000000, tick, destination;
    const events = {}, requests = [];
    const context = {
        Date: {now: () => now},
        document: {
            querySelector: () => ({content:'test-csrf'}),
            addEventListener: (name, fn) => events[name] = fn
        },
        window: {
            addEventListener: (name, fn) => events[name] = fn,
            setInterval: fn => tick = fn,
            setTimeout: () => {},
            location: {replace: url => destination = url}
        },
        fetch: (url, options) => {
            requests.push({url, options});
            return Promise.resolve({redirected:false, status:204});
        }
    };
    vm.runInNewContext(source, context);
    return {advance: ms => now += ms, tick: () => tick(), events, requests, destination: () => destination};
}
test('30 idle minutes logs out without background keep-alive', async () => {
    const b = browser();
    b.advance(30*60*1000-1); b.tick();
    assert.equal(b.requests.length, 0);
    b.advance(1); b.tick();
    assert.equal(b.requests[0].url, '/logout');
    assert.equal(b.requests[0].options.method, 'POST');
    await new Promise(resolve => setImmediate(resolve));
    assert.equal(b.destination(), '/login?timeout');
});
test('real input extends the deadline and sends activity with CSRF', () => {
    const b = browser();
    b.advance(29*60*1000);
    b.events.keydown({isTrusted:true});
    assert.equal(b.requests[0].url, '/session/activity');
    assert.equal(b.requests[0].options.headers['X-CSRF-TOKEN'], 'test-csrf');
    b.advance(2*60*1000); b.tick();
    assert.equal(b.requests.length, 1);
    b.advance(28*60*1000); b.tick();
    assert.equal(b.requests[1].url, '/logout');
});
test('synthetic input cannot keep an idle session alive', () => {
    const b = browser();
    b.advance(29*60*1000);
    b.events.pointermove({isTrusted:false});
    b.advance(60*1000); b.tick();
    assert.equal(b.requests.length, 1);
    assert.equal(b.requests[0].url, '/logout');
});
test('input after deadline logs out rather than reviving the session', () => {
    const b = browser();
    b.advance(31*60*1000);
    b.events.pointerdown({isTrusted:true});
    assert.equal(b.requests.length, 1);
    assert.equal(b.requests[0].url, '/logout');
});
