(() => {
    const input = document.getElementById('reset-link');
    if (input) input.value = new URL(input.value, window.location.origin).href;
})();
