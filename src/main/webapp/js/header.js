/* JanuMart — navbar interactions, logout, cart badge. */
(function () {
  'use strict';

  var CTX = window.CTX || '';

  function toggleMobile(show) {
    var mm = document.getElementById('mobileMenu');
    if (mm) {
      mm.classList.toggle('open', show);
    }
  }

  function bindLogout() {
    document.addEventListener('click', function (e) {
      var t = e.target.closest('#logoutBtn, [data-logout]');
      if (!t) return;
      e.preventDefault();
      API.post('/api/v1/auth/logout').catch(function () { /* keep going */ })
        .finally(function () {
          window.location.href = CTX + '/';
        });
    });
  }

  function renderBadge(n) {
    var b = document.getElementById('cartCount');
    if (!b) return;
    if (n > 0) {
      b.textContent = n > 99 ? '99+' : String(n);
      b.classList.remove('hidden');
    } else {
      b.classList.add('hidden');
    }
  }

  window.updateCartBadge = function () {
    API.get('/api/v1/auth/me').then(function (d) {
      if (d && d.data && d.data.role === 'BUYER') {
        return API.get('/api/v1/cart').then(function (c) {
          renderBadge(c && c.data ? c.data.count : 0);
        });
      }
      renderBadge(0);
      return undefined;
    }).catch(function () {
      renderBadge(0);
    });
  };

  document.addEventListener('DOMContentLoaded', function () {
    var burger = document.getElementById('openMenu');
    if (burger) {
      burger.addEventListener('click', function () {
        var mm = document.getElementById('mobileMenu');
        toggleMobile(!(mm && mm.classList.contains('open')));
      });
    }
    bindLogout();
    updateCartBadge();
  });
})();