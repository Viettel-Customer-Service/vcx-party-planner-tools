(function () {
    /* ── Sidebar collapse toggle ──────────────────────── */
    var toggleBtn = document.getElementById('sidebarToggle');
    var appShell  = document.getElementById('appShell');
    var STORAGE_KEY = 'vcx_sidebar_collapsed';

    if (toggleBtn && appShell) {
        // Restore saved state
        if (localStorage.getItem(STORAGE_KEY) === '1') {
            appShell.classList.add('sidebar-collapsed');
        }

        toggleBtn.addEventListener('click', function () {
            appShell.classList.toggle('sidebar-collapsed');
            var isCollapsed = appShell.classList.contains('sidebar-collapsed');
            localStorage.setItem(STORAGE_KEY, isCollapsed ? '1' : '0');
            toggleBtn.setAttribute('aria-label', isCollapsed ? 'Mở rộng menu' : 'Thu gọn menu');
            toggleBtn.setAttribute('data-tooltip', isCollapsed ? 'Mở rộng menu' : 'Thu gọn menu');
        });
    }
})();

(function () {
    /* ── User menu dropdown ───────────────────────────── */
    var userMenu = document.querySelector('.user-menu');
    if (!userMenu) return;

    var trigger  = userMenu.querySelector('.user-menu-btn');
    var menu     = userMenu.querySelector('.user-menu-dropdown');
    if (!trigger || !menu) return;

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

    trigger.addEventListener('click', function (e) {
        e.preventDefault();
        e.stopPropagation();
        menu.classList.contains('show') ? closeMenu() : openMenu();
    });

    document.addEventListener('click', function (e) {
        if (!userMenu.contains(e.target)) closeMenu();
    });

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') closeMenu();
    });
})();

(function () {
    /* ── Custom Tooltip for Sidebar (Vanilla JS) ────────── */
    var tooltipEl = document.createElement('div');
    tooltipEl.className = 'custom-tooltip';
    document.body.appendChild(tooltipEl);

    var currentTarget = null;
    var tooltipTimeout = null;

    function showTooltip(e) {
        var appShell = document.getElementById('appShell');
        // Only show custom tooltip if sidebar is collapsed
        if (!appShell || !appShell.classList.contains('sidebar-collapsed')) return;

        var target = e.currentTarget;
        var text = target.getAttribute('data-tooltip');
        if (!text) return;

        currentTarget = target;
        tooltipEl.textContent = text;
        
        var rect = target.getBoundingClientRect();
        var top = rect.top + (rect.height / 2);
        var left = rect.right + 12;
        
        tooltipEl.style.top = top + 'px';
        tooltipEl.style.left = left + 'px';

        clearTimeout(tooltipTimeout);
        tooltipEl.classList.add('show');
    }

    function hideTooltip() {
        if (!currentTarget) return;
        currentTarget = null;
        tooltipEl.classList.remove('show');
    }

    var triggers = document.querySelectorAll('[data-tooltip]');
    triggers.forEach(function (trigger) {
        trigger.addEventListener('mouseenter', showTooltip);
        trigger.addEventListener('mouseleave', hideTooltip);
        trigger.addEventListener('click', hideTooltip);
    });
})();
