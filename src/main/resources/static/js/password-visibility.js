(() => {
    document.querySelectorAll('input[type="password"]').forEach((input, index) => {
        if (input.dataset.visibilityReady) return;
        input.dataset.visibilityReady = 'true';
        if (!input.id) input.id = 'password-field-' + index;
        const wrapper = document.createElement('div');
        wrapper.className = 'password-field';
        ['mb-3', 'mb-4'].forEach(name => {
            if (input.classList.contains(name)) {
                input.classList.remove(name);
                wrapper.classList.add(name);
            }
        });
        input.before(wrapper);
        wrapper.append(input);
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'password-toggle';
        button.setAttribute('aria-controls', input.id);
        button.innerHTML = '<svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="1.7"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/><path class="eye-slash" d="m3 3 18 18"/></svg>';
        const render = () => {
            const shown = input.type === 'text';
            button.setAttribute('aria-label', shown ? 'Hide password' : 'Show password');
            button.setAttribute('aria-pressed', String(shown));
            button.title = shown ? 'Hide password' : 'Show password';
            button.querySelector('.eye-slash').style.display = shown ? '' : 'none';
        };
        button.addEventListener('click', () => {
            input.type = input.type === 'password' ? 'text' : 'password';
            render();
        });
        const hide = () => { input.type = 'password'; render(); };
        input.form?.addEventListener('reset', hide);
        window.addEventListener('pageshow', hide);
        window.addEventListener('pagehide', hide);
        wrapper.append(button);
        render();
    });
})();
