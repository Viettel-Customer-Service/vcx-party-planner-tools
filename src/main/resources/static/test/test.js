(function () {
    var sendButtons = document.querySelectorAll('.js-send-birthday');
    sendButtons.forEach(function (button) {
        button.addEventListener('click', function (event) {
            event.preventDefault();
            var employeeName = button.getAttribute('data-employee-name') || 'nhan vien nay';
            var confirmed = window.confirm('Ban co chac chan gui loi chuc cho ' + employeeName + '?');
            if (!confirmed) {
                return;
            }

            button.disabled = true;
            button.textContent = 'Sending...';
            var form = button.closest('form');
            if (form) {
                form.submit();
            }
        });
    });

    var pageToast = document.querySelector('.js-page-toast');
    if (!pageToast) {
        return;
    }

    pageToast.classList.add('show');

    var delay = parseInt(pageToast.getAttribute('data-toast-delay') || '3500', 10);
    window.setTimeout(function () {
        pageToast.classList.remove('show');
    }, delay);

    var closeButton = pageToast.querySelector('.js-toast-close');
    if (closeButton) {
        closeButton.addEventListener('click', function () {
            pageToast.classList.remove('show');
        });
    }
})();

