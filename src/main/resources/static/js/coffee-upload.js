(() => {
    const dialog = document.getElementById('upload-dialog');
    const value = document.getElementById('image-value');
    const preview = document.getElementById('image-preview');
    const status = document.getElementById('image-status');
    const error = document.getElementById('upload-error');
    const fileInput = document.getElementById('upload-file');
    const upload = document.getElementById('upload-button');
    const form = value.form;
    let busy = false;
    function showPreview() {
        preview.hidden = !value.value;
        if (value.value) preview.src = value.value;
        else preview.removeAttribute('src');
        status.textContent = value.value ? 'Image selected.' : 'Upload a JPG or PNG image.';
    }
    showPreview();
    document.getElementById('open-upload').onclick = () => {
        error.textContent = '';
        dialog.showModal();
    };
    document.getElementById('close-upload').onclick = () => dialog.close();
    upload.onclick = async () => {
        const file = fileInput.files[0];
        if (!file || file.size > 5 * 1024 * 1024 || !['image/jpeg', 'image/png'].includes(file.type)) {
            error.textContent = 'Choose a JPG or PNG up to 5 MB.';
            return;
        }
        busy = true;
        upload.disabled = true;
        error.textContent = '';
        try {
            const data = new FormData();
            data.append('file', file);
            const response = await fetch('/coffeeboard/upload', {
                method: 'POST',
                headers: {'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content},
                body: data
            });
            if (!response.ok) throw new Error('Upload failed. Check the image format and size, then try again.');
            const result = await response.json();
            value.value = result.image;
            showPreview();
            dialog.close();
        } catch (err) {
            error.textContent = err.message;
        } finally {
            busy = false;
            upload.disabled = false;
        }
    };
    form.addEventListener('submit', event => {
        if (busy || !value.value) {
            event.preventDefault();
            status.textContent = busy ? 'Please wait for the upload.' : 'Please upload an image first.';
        }
    });
    form.addEventListener('reset', () => setTimeout(() => {
        fileInput.value = '';
        showPreview();
    }, 0));
})();
