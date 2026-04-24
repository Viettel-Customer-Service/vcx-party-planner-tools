(function () {
    var modalElement = document.getElementById('messageModal');
    var stateElement = document.getElementById('messageModalState');
    var formElement = document.getElementById('messageForm');
    var modalTitle = document.getElementById('messageModalTitle');
    var submitButton = document.getElementById('messageSubmitBtn');
    var createButton = document.getElementById('openCreateMessageModal');

    if (!modalElement || !stateElement || !formElement) {
        return;
    }

    function ensureBackdrop() {
        var existingBackdrop = document.querySelector('.modal-backdrop');
        if (existingBackdrop) {
            return existingBackdrop;
        }
        var backdrop = document.createElement('div');
        backdrop.className = 'modal-backdrop fade show';
        document.body.appendChild(backdrop);
        return backdrop;
    }

    function removeBackdrop() {
        var backdrop = document.querySelector('.modal-backdrop');
        if (backdrop) {
            backdrop.parentNode.removeChild(backdrop);
        }
    }

    function showModal() {
        modalElement.style.display = 'block';
        modalElement.classList.add('show');
        modalElement.setAttribute('aria-modal', 'true');
        modalElement.removeAttribute('aria-hidden');
        document.body.classList.add('modal-open');
        ensureBackdrop();
    }

    function hideModal() {
        modalElement.style.display = 'none';
        modalElement.classList.remove('show');
        modalElement.setAttribute('aria-hidden', 'true');
        modalElement.removeAttribute('aria-modal');
        document.body.classList.remove('modal-open');
        removeBackdrop();
    }

    function setMode(mode, action) {
        var isUpdate = mode === 'update';
        formElement.setAttribute('action', action || '/messages');
        if (modalTitle) {
            modalTitle.textContent = isUpdate ? 'Cập Nhật Lời Chúc' : 'Thêm Lời Chúc';
        }
        if (submitButton) {
            submitButton.textContent = isUpdate ? 'Cập Nhật' : 'Thêm Mới';
        }
    }

    function resetForm() {
        formElement.reset();
        var typeInput = document.getElementById('type');
        var isActiveInput = document.getElementById('isActive');
        var isDefaultInput = document.getElementById('isDefault');
        if (typeInput) {
            typeInput.value = '';
        }
        if (isActiveInput) {
            isActiveInput.checked = true;
        }
        if (isDefaultInput) {
            isDefaultInput.checked = false;
        }
        var contentInput = document.getElementById('content');
        if (contentInput) {
            contentInput.value = '';
        }
        var nameInput = document.getElementById('name');
        if (nameInput) {
            nameInput.value = '';
        }
    }

    function fillForm(button) {
        document.getElementById('type').value = button.getAttribute('data-message-type') || '';
        document.getElementById('name').value = button.getAttribute('data-message-name') || '';
        document.getElementById('content').value = button.getAttribute('data-message-content') || '';
        document.getElementById('isActive').checked = button.getAttribute('data-message-is-active') === 'true';
        document.getElementById('isDefault').checked = button.getAttribute('data-message-is-default') === 'true';
    }

    if (createButton) {
        createButton.addEventListener('click', function () {
            setMode('create', '/messages');
            resetForm();
            showModal();
        });
    }

    var editButtons = document.querySelectorAll('.js-edit-message');
    editButtons.forEach(function (button) {
        button.addEventListener('click', function (event) {
            event.preventDefault();

            var messageId = button.getAttribute('data-message-id');
            setMode('update', '/messages/' + messageId);
            fillForm(button);
            showModal();
        });
    });

    var dismissButtons = modalElement.querySelectorAll('[data-dismiss="modal"]');
    dismissButtons.forEach(function (button) {
        button.addEventListener('click', function () {
            hideModal();
        });
    });

    modalElement.addEventListener('click', function (event) {
        if (event.target === modalElement) {
            hideModal();
        }
    });

    var shouldOpenModal = stateElement.getAttribute('data-open-modal') === 'true';
    if (shouldOpenModal) {
        setMode(
            stateElement.getAttribute('data-modal-mode') || 'create',
            stateElement.getAttribute('data-form-action') || '/messages'
        );
        showModal();
    }
})();

