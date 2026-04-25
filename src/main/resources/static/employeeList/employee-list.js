(function () {
    var modalElement = document.getElementById('employeeModal');
    var importModalElement = document.getElementById('employeeImportModal');
    var stateElement = document.getElementById('employeeModalState');
    var formElement = document.getElementById('employeeForm');
    var importFormElement = document.getElementById('employeeImportForm');
    var modalTitle = document.getElementById('employeeModalTitle');
    var submitButton = document.getElementById('employeeSubmitBtn');
    var createButton = document.getElementById('openCreateEmployeeModal');
    var importButton = document.getElementById('openImportEmployeeModal');
    var importFileInput = document.getElementById('importFile');
    var importFileError = document.getElementById('importFileError');
    var importSubmitButton = document.getElementById('employeeImportSubmitBtn');
    var pageToast = document.querySelector('.js-page-toast');
    var upcomingBirthdayBlock = document.getElementById('upcomingBirthdayBlock');
    var upcomingBirthdayTicker = document.getElementById('upcomingBirthdayTicker');

    if (!modalElement || !stateElement || !formElement) {
        return;
    }

    function formatBirthDate(dateText) {
        if (!dateText) {
            return '';
        }
        var parts = String(dateText).split('-');
        if (parts.length !== 3) {
            return dateText;
        }
        return parts[2] + '/' + parts[1];
    }

    function escapeHtml(value) {
        return String(value == null ? '' : value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function joinTickerSegments(segments) {
        return segments.filter(function (segment) {
            return segment && segment.length > 0;
        }).join('<span class="text-muted">•</span>');
    }

    function renderUpcomingBirthdays(items) {
        if (!upcomingBirthdayBlock || !upcomingBirthdayTicker) {
            return;
        }

        if (!items || items.length === 0) {
            upcomingBirthdayTicker.innerHTML = '';
            upcomingBirthdayBlock.classList.add('d-none');
            return;
        }

        var tickerItemsHtml = items.map(function (item) {
            var status = item.status || '';
            var isToday = status === 'Hôm nay';
            var statusHtml = isToday
                ? '<span class="badge badge-danger badge-pill px-2 py-1"><i class="bi bi-cake2-fill mr-1" aria-hidden="true"></i>' + escapeHtml(status || 'Hôm nay') + '</span>'
                : '<span class="badge badge-light border text-dark badge-pill px-2 py-1">' + escapeHtml(status) + '</span>';
            var detailHtml = joinTickerSegments([
                item.fullName ? '<strong class="ticker-name">' + escapeHtml(item.fullName) + '</strong>' : '',
                item.title ? '<span class="ticker-role">' + escapeHtml(item.title) + '</span>' : '',
                item.dateOfBirth ? '<span class="text-muted">Sinh nhật ' + escapeHtml(formatBirthDate(item.dateOfBirth)) + '</span>' : ''
            ]);
            return '<span class="ticker-item">' +
                statusHtml +
                detailHtml +
                '</span>';
        }).join('');

        upcomingBirthdayTicker.innerHTML = '<div class="ticker-group">' + tickerItemsHtml + '</div>' +
            '<div class="ticker-group" aria-hidden="true">' + tickerItemsHtml + '</div>';
        upcomingBirthdayBlock.classList.remove('d-none');
    }

    function loadUpcomingBirthdays() {
        if (!upcomingBirthdayBlock || !upcomingBirthdayTicker || !window.fetch) {
            return;
        }

        window.fetch('/employees/upcoming-birthdays', {
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('Cannot load upcoming birthdays');
                }
                return response.json();
            })
            .then(function (data) {
                renderUpcomingBirthdays(Array.isArray(data) ? data : []);
            })
            .catch(function () {
                upcomingBirthdayBlock.classList.add('d-none');
            });
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

    function showImportModal() {
        if (!importModalElement) {
            return;
        }
        importModalElement.style.display = 'block';
        importModalElement.classList.add('show');
        importModalElement.setAttribute('aria-modal', 'true');
        importModalElement.removeAttribute('aria-hidden');
        document.body.classList.add('modal-open');
        ensureBackdrop();
    }

    function hideImportModal() {
        if (!importModalElement) {
            return;
        }
        importModalElement.style.display = 'none';
        importModalElement.classList.remove('show');
        importModalElement.setAttribute('aria-hidden', 'true');
        importModalElement.removeAttribute('aria-modal');
        document.body.classList.remove('modal-open');
        removeBackdrop();
        resetImportForm();
    }

    function setMode(mode, action) {
        var isUpdate = mode === 'update';
        formElement.setAttribute('action', action || '/employees');
        if (modalTitle) {
            modalTitle.textContent = isUpdate ? 'Cập Nhật Nhân Viên' : 'Thêm Nhân Viên';
        }
        if (submitButton) {
            submitButton.textContent = isUpdate ? 'Cập Nhật' : 'Thêm Mới';
        }
    }

    function resetForm() {
        formElement.reset();
        var departmentInput = document.getElementById('departmentId');
        if (departmentInput) {
            departmentInput.value = '';
        }
    }

    function setImportFileError(message) {
        if (!importFileError) {
            return;
        }
        importFileError.textContent = message || '';
        importFileError.style.display = message ? 'block' : 'none';
        importFileError.classList.toggle('d-none', !message);
    }

    function resetImportForm() {
        if (importFormElement) {
            importFormElement.reset();
        }
        if (importFileInput) {
            importFileInput.value = '';
        }
        setImportFileError('');
        if (importSubmitButton) {
            importSubmitButton.disabled = false;
        }
    }

    function validateImportFile(file) {
        if (!file) {
            setImportFileError('Vui lòng chọn file Excel .xlsx.');
            return false;
        }

        if (!/\.xlsx$/i.test(file.name || '')) {
            setImportFileError('Chỉ hỗ trợ file .xlsx.');
            return false;
        }

        if (file.size > 10 * 1024 * 1024) {
            setImportFileError('File vượt quá dung lượng cho phép 10MB.');
            return false;
        }

        setImportFileError('');
        return true;
    }

    if (createButton) {
        createButton.addEventListener('click', function () {
            setMode('create', '/employees');
            resetForm();
            showModal();
        });
    }

    if (importButton) {
        importButton.addEventListener('click', function () {
            resetImportForm();
            showImportModal();
        });
    }

    var editButtons = document.querySelectorAll('.js-edit-employee');
    editButtons.forEach(function (button) {
        button.addEventListener('click', function (event) {
            event.preventDefault();

            var employeeId = button.getAttribute('data-employee-id');
            setMode('update', '/employees/' + employeeId);

            document.getElementById('departmentId').value = button.getAttribute('data-department-id') || '';
            document.getElementById('jobTitle').value = button.getAttribute('data-job-title') || '';
            document.getElementById('employeeCode').value = button.getAttribute('data-employee-code') || '';
            document.getElementById('fullName').value = button.getAttribute('data-full-name') || '';
            document.getElementById('dateOfBirth').value = button.getAttribute('data-date-of-birth') || '';
            document.getElementById('phoneNumber').value = button.getAttribute('data-phone-number') || '';
            document.getElementById('email').value = button.getAttribute('data-email') || '';

            showModal();
        });
    });

    var deleteButtons = document.querySelectorAll('.js-delete-employee');
    deleteButtons.forEach(function (button) {
        button.addEventListener('click', function (event) {
            event.preventDefault();

            var employeeName = button.getAttribute('data-employee-name') || 'nhân viên này';
            var confirmed = window.confirm('Bạn có chắc chắn muốn xóa ' + employeeName + '?');
            if (!confirmed) {
                return;
            }

            button.disabled = true;
            var form = button.closest('form');
            if (form) {
                form.submit();
            }
        });
    });

    var dismissButtons = modalElement.querySelectorAll('[data-dismiss="modal"]');
    dismissButtons.forEach(function (button) {
        button.addEventListener('click', function () {
            hideModal();
        });
    });

    if (importModalElement) {
        var importDismissButtons = importModalElement.querySelectorAll('[data-dismiss="modal"]');
        importDismissButtons.forEach(function (button) {
            button.addEventListener('click', function () {
                hideImportModal();
            });
        });

        importModalElement.addEventListener('click', function (event) {
            if (event.target === importModalElement) {
                hideImportModal();
            }
        });
    }

    modalElement.addEventListener('click', function (event) {
        if (event.target === modalElement) {
            hideModal();
        }
    });

    var shouldOpenModal = stateElement.getAttribute('data-open-modal') === 'true';
    if (shouldOpenModal) {
        setMode(
            stateElement.getAttribute('data-modal-mode') || 'create',
            stateElement.getAttribute('data-form-action') || '/employees'
        );
        showModal();
    }

    var shouldOpenImportModal = stateElement.getAttribute('data-open-import-modal') === 'true';
    if (shouldOpenImportModal) {
        showImportModal();
    }

    if (importFileInput) {
        importFileInput.addEventListener('change', function () {
            var file = importFileInput.files && importFileInput.files.length > 0 ? importFileInput.files[0] : null;
            validateImportFile(file);
        });
    }

    if (importFormElement) {
        importFormElement.addEventListener('submit', function (event) {
            var file = importFileInput && importFileInput.files && importFileInput.files.length > 0
                ? importFileInput.files[0]
                : null;

            if (!validateImportFile(file)) {
                event.preventDefault();
                return;
            }

            if (importSubmitButton) {
                importSubmitButton.disabled = true;
            }
        });
    }

    function showToast(toastElement) {
        if (!toastElement) {
            return;
        }

        toastElement.classList.add('show');

        var delay = parseInt(toastElement.getAttribute('data-toast-delay') || '3500', 10);
        window.setTimeout(function () {
            toastElement.classList.remove('show');
        }, delay);
    }

    if (pageToast) {
        showToast(pageToast);
        var closeButton = pageToast.querySelector('.js-toast-close');
        if (closeButton) {
            closeButton.addEventListener('click', function () {
                pageToast.classList.remove('show');
            });
        }
    }

    var searchInput = document.querySelector('.employee-search-form .search-input');
    var searchForm = document.querySelector('.employee-search-form');
    
    if (searchInput && searchForm) {
        if (sessionStorage.getItem('employeeSearchFocused') === 'true') {
            searchInput.focus();
            var val = searchInput.value;
            searchInput.value = '';
            searchInput.value = val;
            sessionStorage.removeItem('employeeSearchFocused');
        }

        var debounceTimer;
        searchInput.addEventListener('input', function () {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(function () {
                sessionStorage.setItem('employeeSearchFocused', 'true');
                searchForm.submit();
            }, 1000);
        });
    }

    loadUpcomingBirthdays();
})();

