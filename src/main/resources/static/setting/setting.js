(function () {
    var enabledInput   = document.getElementById('settingEnabled');
    var timeInput      = document.getElementById('settingTime');
    var saveButton     = document.getElementById('saveSettingBtn');
    var toastElement   = document.querySelector('.js-setting-toast');
    var toastTitle     = document.querySelector('.js-setting-toast-title');
    var toastBody      = document.querySelector('.js-setting-toast-body');
    var toastClose     = document.querySelector('.js-setting-toast-close');
    var toggleStatus   = document.getElementById('toggleStatusText');
    var statusBadge    = document.getElementById('statusBadge');
    var statusBadgeText = document.getElementById('statusBadgeText');
    var systemStatusPill = document.getElementById('systemStatusPill');
    var systemStatusText = document.getElementById('systemStatusText');
    var timeStatusPill   = document.getElementById('timeStatusPill');
    var timeStatusText   = document.getElementById('timeStatusText');
    var toastTimer     = null;

    if (!enabledInput || !timeInput || !saveButton || !toastElement) return;

    /* ── Helpers ──────────────────────────────────────── */
    function formatTime(hour, minute) {
        return String(hour).padStart(2, '0') + ':' + String(minute).padStart(2, '0');
    }

    function parseTime(value) {
        var parts = (value || '').split(':');
        if (parts.length !== 2) return null;
        var h = parseInt(parts[0], 10);
        var m = parseInt(parts[1], 10);
        if (Number.isNaN(h) || Number.isNaN(m)) return null;
        return { hour: h, minute: m };
    }

    /* ── Toggle UI sync ───────────────────────────────── */
    function syncToggleUI(checked) {
        if (toggleStatus) {
            toggleStatus.textContent = checked ? 'Bật' : 'Tắt';
        }
    }

    /* ── Status cards sync ────────────────────────────── */
    function syncStatusCards(enabled, time) {
        // Hero badge
        if (statusBadge && statusBadgeText) {
            statusBadgeText.textContent = enabled ? 'Đang hoạt động' : 'Đã tắt';
        }

        // System status pill
        if (systemStatusPill && systemStatusText) {
            systemStatusText.textContent = enabled ? 'Đang hoạt động' : 'Đã tắt';
            if (enabled) {
                systemStatusPill.classList.add('active');
            } else {
                systemStatusPill.classList.remove('active');
            }
        }

        // Time status pill
        if (timeStatusPill && timeStatusText) {
            timeStatusText.textContent = time || '--:--';
            if (time && time !== '--:--') {
                timeStatusPill.classList.add('active');
            } else {
                timeStatusPill.classList.remove('active');
            }
        }
    }

    /* ── Toast ────────────────────────────────────────── */
    function hideToast() {
        toastElement.classList.remove('show');
    }

    function showMessage(message, isError) {
        if (toastTimer) clearTimeout(toastTimer);

        toastElement.classList.remove('toast-success', 'toast-error');
        toastElement.classList.add(isError ? 'toast-error' : 'toast-success');

        if (toastTitle) toastTitle.textContent = isError ? 'Có lỗi xảy ra' : 'Thành công';
        if (toastBody)  toastBody.textContent  = message;

        toastElement.classList.add('show');
        var delay = parseInt(toastElement.getAttribute('data-toast-delay') || '3500', 10);
        toastTimer = setTimeout(hideToast, delay);
    }

    /* ── Button state ─────────────────────────────────── */
    function setLoading(isLoading) {
        var btnText = saveButton.querySelector('.btn-text');
        saveButton.disabled = isLoading;
        if (btnText) {
            btnText.textContent = isLoading ? 'Đang lưu...' : 'Lưu cấu hình';
        }
    }

    /* ── Load config ──────────────────────────────────── */
    function loadConfig() {
        setLoading(true);
        syncStatusCards(false, '--:--');

        fetch('/config/birthday', {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        })
            .then(function (res) {
                if (!res.ok) throw new Error('Không thể tải cấu hình hiện tại');
                return res.json();
            })
            .then(function (data) {
                enabledInput.checked = !!data.enabled;
                timeInput.value = formatTime(data.hour, data.minute);
                syncToggleUI(!!data.enabled);
                syncStatusCards(!!data.enabled, formatTime(data.hour, data.minute));
            })
            .catch(function (err) {
                showMessage(err.message || 'Không thể tải cấu hình', true);
                syncStatusCards(false, '--:--');
            })
            .finally(function () {
                setLoading(false);
            });
    }

    /* ── Save config ──────────────────────────────────── */
    saveButton.addEventListener('click', function () {
        var time = parseTime(timeInput.value);
        if (!time) {
            showMessage('Vui lòng nhập thời gian hợp lệ (HH:MM)', true);
            timeInput.focus();
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
            .then(function (res) {
                if (!res.ok) throw new Error('Lưu cấu hình thất bại');
                return res.json();
            })
            .then(function (data) {
                enabledInput.checked = !!data.enabled;
                timeInput.value = formatTime(data.hour, data.minute);
                syncToggleUI(!!data.enabled);
                syncStatusCards(!!data.enabled, formatTime(data.hour, data.minute));
                showMessage('Cấu hình đã được lưu thành công!', false);
            })
            .catch(function (err) {
                showMessage(err.message || 'Lưu cấu hình thất bại', true);
            })
            .finally(function () {
                setLoading(false);
            });
    });

    /* ── Toggle change event ──────────────────────────── */
    enabledInput.addEventListener('change', function () {
        syncToggleUI(this.checked);
        // Tự động lưu cấu hình khi toggle thay đổi để đáp ứng kỳ vọng của user
        saveButton.click();
    });

    /* ── Close toast button ───────────────────────────── */
    if (toastClose) {
        toastClose.addEventListener('click', hideToast);
    }

    /* ── Initialise ───────────────────────────────────── */
    loadConfig();
})();
