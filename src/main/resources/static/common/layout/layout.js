(function () {
    var toggleBtn = document.getElementById('sidebarToggle');
    var appShell = document.getElementById('appShell');

    if (!toggleBtn || !appShell) {
        return;
    }

    toggleBtn.addEventListener('click', function () {
        appShell.classList.toggle('sidebar-collapsed');
    });
})();

(function () {
    var userMenu = document.querySelector('.user-menu');
    if (!userMenu) {
        return;
    }

    var trigger = userMenu.querySelector('.user-menu-btn');
    var menu = userMenu.querySelector('.user-menu-dropdown');
    if (!trigger || !menu) {
        return;
    }

    function closeMenu() {
        userMenu.classList.remove('show');
        menu.classList.remove('show');
        trigger.setAttribute('aria-expanded', 'false');
    }

    function openMenu() {
        userMenu.classList.add('show');
        menu.classList.add('show');
        trigger.setAttribute('aria-expanded', 'true');
    }

    trigger.addEventListener('click', function (event) {
        event.preventDefault();
        event.stopPropagation();

        if (menu.classList.contains('show')) {
            closeMenu();
            return;
        }

        openMenu();
    });

    document.addEventListener('click', function (event) {
        if (!userMenu.contains(event.target)) {
            closeMenu();
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            closeMenu();
        }
    });
})();

