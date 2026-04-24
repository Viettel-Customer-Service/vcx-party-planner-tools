(function () {
    var enabledInput = document.getElementById('settingEnabled');
    var timeInput = document.getElementById('settingTime');
    var saveButton = document.getElementById('saveSettingBtn');
    var toastElement = document.querySelector('.js-setting-toast');
    var toastTitle = document.querySelector('.js-setting-toast-title');
    var toastBody = document.querySelector('.js-setting-toast-body');
    var toastCloseButton = document.querySelector('.js-setting-toast-close');
    var toastTimer = null;

    if (!enabledInput || !timeInput || !saveButton || !toastElement || !toastBody || !toastTitle) {
        return;
    }

    function hideToast() {
        toastElement.classList.remove('show');
    }

    function showMessage(message, isError) {
        if (toastTimer) {
            window.clearTimeout(toastTimer);
        }

        toastElement.classList.remove('toast-success', 'toast-error');
        toastElement.classList.add(isError ? 'toast-error' : 'toast-success');
        toastTitle.textContent = isError ? 'Có lỗi' : 'Thành công';
        toastBody.textContent = message;
        toastElement.classList.add('show');

        var delay = parseInt(toastElement.getAttribute('data-toast-delay') || '3500', 10);
        toastTimer = window.setTimeout(function () {
            hideToast();
        }, delay);
    }

    function parseTime(value) {
        var parts = (value || '').split(':');
        if (parts.length !== 2) {
            return null;
        }

        var hour = parseInt(parts[0], 10);
        var minute = parseInt(parts[1], 10);
        if (Number.isNaN(hour) || Number.isNaN(minute)) {
            return null;
        }

        return { hour: hour, minute: minute };
    }

    function formatTime(hour, minute) {
        var hh = String(hour).padStart(2, '0');
        var mm = String(minute).padStart(2, '0');
        return hh + ':' + mm;
    }

    function setLoading(isLoading) {
        saveButton.disabled = isLoading;
        saveButton.textContent = isLoading ? 'Đang lưu...' : 'Lưu';
    }

    function loadConfig() {
        setLoading(true);
        fetch('/config/birthday', {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('Không thể tải cấu hình hiện tại');
                }
                return response.json();
            })
            .then(function (data) {
                enabledInput.checked = !!data.enabled;
                timeInput.value = formatTime(data.hour, data.minute);
            })
            .catch(function (error) {
                showMessage(error.message || 'Không thể tải cấu hình', true);
            })
            .finally(function () {
                setLoading(false);
            });
    }

    saveButton.addEventListener('click', function () {
        var time = parseTime(timeInput.value);
        if (!time) {
            showMessage('Vui lòng nhập thời gian hợp lệ', true);
            return;
        }

        setLoading(true);
        fetch('/config/birthday', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                enabled: enabledInput.checked,
                hour: time.hour,
                minute: time.minute
            })
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('Lưu cấu hình thất bại');
                }
                return response.json();
            })
            .then(function (data) {
                enabledInput.checked = !!data.enabled;
                timeInput.value = formatTime(data.hour, data.minute);
                showMessage('Lưu cấu hình thành công', false);
            })
            .catch(function (error) {
                showMessage(error.message || 'Lưu cấu hình thất bại', true);
            })
            .finally(function () {
                setLoading(false);
            });
    });

    if (toastCloseButton) {
        toastCloseButton.addEventListener('click', function () {
            hideToast();
        });
    }

    loadConfig();
})();

