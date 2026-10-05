(() => {
    const root = document.getElementById('coffee-carousel');
    if (!root) return;
    const slides = Array.from(root.querySelectorAll('.coffee-slide'));
    if (!slides.length) return;
    const viewport = root.querySelector('.coffee-viewport');
    const track = root.querySelector('.coffee-track');
    const controls = root.querySelector('.coffee-carousel-controls');
    const count = root.querySelector('.coffee-carousel-count');
    const toggle = root.querySelector('[data-carousel-toggle]');
    const reduced = window.matchMedia('(prefers-reduced-motion: reduce)');
    let index = 0, playing = !reduced.matches, hovering = false, focused = false;
    root.classList.add('is-ready');
    controls.hidden = slides.length < 2;
    function render() {
        track.style.transform = 'translateY(-' + index * viewport.clientHeight + 'px)';
        slides.forEach((slide, i) => {
            slide.inert = i !== index;
            slide.setAttribute('aria-hidden', String(i !== index));
        });
        count.textContent = (index + 1) + ' / ' + slides.length;
        toggle.textContent = playing ? 'Pause' : 'Play';
        toggle.setAttribute('aria-label', playing ? 'Pause automatic slideshow' : 'Play automatic slideshow');
    }
    function move(step) { index = (index + step + slides.length) % slides.length; render(); }
    root.querySelector('[data-carousel-prev]').onclick = () => move(-1);
    root.querySelector('[data-carousel-next]').onclick = () => move(1);
    toggle.onclick = () => { playing = !playing; render(); };
    root.addEventListener('mouseenter', () => { hovering = true; });
    root.addEventListener('mouseleave', () => { hovering = false; });
    root.addEventListener('focusin', () => { focused = true; });
    root.addEventListener('focusout', event => { focused = root.contains(event.relatedTarget); });
    root.addEventListener('keydown', event => {
        if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
            event.preventDefault();
            move(event.key === 'ArrowDown' ? 1 : -1);
        }
    });
    reduced.addEventListener('change', () => { if (reduced.matches) playing = false; render(); });
    new ResizeObserver(render).observe(viewport);
    setInterval(() => {
        if (slides.length > 1 && playing && !hovering && !focused && !document.hidden) move(1);
    }, 2000);
    render();
})();
