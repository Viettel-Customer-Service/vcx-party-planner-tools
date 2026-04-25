(function () {
    var modalElement = document.getElementById('logDetailModal');
    if (!modalElement) {
        return;
    }

    var detailButtons = document.querySelectorAll('.js-open-log-detail');
    var employeeNameElement = document.getElementById('detailEmployeeName');
    var channelElement = document.getElementById('detailChannel');
    var statusElement = document.getElementById('detailStatus');
    var sendTimeElement = document.getElementById('detailSendTime');
    var messageElement = document.getElementById('detailMessage');
    var errorElement = document.getElementById('detailError');
    var errorGroup = document.getElementById('detailErrorGroup');

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

    function fillDetail(button) {
        var status = button.getAttribute('data-log-status') || '';
        employeeNameElement.textContent = button.getAttribute('data-log-employee-name') || '';
        
        var channel = button.getAttribute('data-log-channel') || '';
        channelElement.innerHTML = '<span class="channel-badge channel-' + channel.toLowerCase() + '">' + channel + '</span>';
        
        statusElement.className = 'status-badge ' + (status === 'SUCCESS' ? 'status-success' : 'status-failed');
        statusElement.innerHTML = (status === 'SUCCESS' ? '<i class="bi bi-check-circle-fill"></i> Thành công' : '<i class="bi bi-x-circle-fill"></i> Thất bại');
        
        sendTimeElement.textContent = button.getAttribute('data-log-send-time') || '—';
        messageElement.value = button.getAttribute('data-log-message') || '';
        
        var errorMsg = button.getAttribute('data-log-error') || '';
        if (status === 'FAILED' || errorMsg) {
            errorElement.value = errorMsg;
            errorGroup.style.display = 'block';
        } else {
            errorElement.value = '';
            errorGroup.style.display = 'none';
        }
    }

    detailButtons.forEach(function (button) {
        button.addEventListener('click', function (event) {
            event.preventDefault();
            fillDetail(button);
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
})();
