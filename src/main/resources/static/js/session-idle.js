(() => {
    const timeout = 30 * 60 * 1000;
    let lastActivity = Date.now(), lastPing = 0, lastBroadcast = 0, leaving = false;
    const channel = typeof BroadcastChannel !== 'undefined' ? new BroadcastChannel('coffee-activity') : null;
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const goToLogin = () => window.location.replace('/login?timeout');
    const expire = () => {
        if (leaving) return;
        leaving = true;
        // The server also expires the session independently of this timer.
        window.setTimeout(goToLogin, 2000);
        fetch('/logout', {method: 'POST', headers: {'X-CSRF-TOKEN': token || ''}})
            .finally(goToLogin);
    };
    const check = () => { if (Date.now() - lastActivity >= timeout) expire(); };
    const activity = event => {
        if (!event.isTrusted || leaving) return;
        check();
        if (leaving) return;
        const now = Date.now();
        lastActivity = now;
        if (now - lastBroadcast >= 1000) {
            channel?.postMessage(now);
            lastBroadcast = now;
        }
        // Only real interaction sends this request, never an automatic keep-alive timer.
        if (now - lastPing >= 60000) {
            lastPing = now;
            fetch('/session/activity', {method: 'POST', headers: {'X-CSRF-TOKEN': token || ''}})
                .then(response => { if (response.redirected || response.status === 401 || response.status === 403) {
                    leaving = true; goToLogin();
                }}).catch(() => {});
        }
    };
    if (channel) channel.onmessage = event => {
        if (typeof event.data === 'number' && event.data <= Date.now()) lastActivity = Math.max(lastActivity, event.data);
    };
    ['pointerdown', 'pointermove', 'keydown', 'wheel', 'touchstart'].forEach(name =>
        document.addEventListener(name, activity, {passive: true}));
    document.addEventListener('visibilitychange', check);
    window.addEventListener('pageshow', check);
    window.setInterval(check, 1000);
})();
