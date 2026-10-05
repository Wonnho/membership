(() => {
    if (document.getElementById('goodbye-screen')) {
        window.setTimeout(() => window.location.replace('/login'), 3000);
        return;
    }
    const form = document.getElementById('login-form');
    if (!form || form.dataset.clearFields !== 'true') return;
    const clear = () => {
        form.elements.email.value = '';
        form.elements.password.value = '';
        form.elements.password.type = 'password';
    };
    clear();
    window.addEventListener('pageshow', clear);
})();
