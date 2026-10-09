/* JanuMart — login & register forms (JSON API). */
(function () {
  'use strict';

  var CTX = window.CTX || '';

  function showError(msg) {
    var el = document.getElementById('authError');
    if (el) {
      el.textContent = msg || '';
      el.classList.remove('hidden');
    }
  }

  function hideError() {
    var el = document.getElementById('authError');
    if (el) el.classList.add('hidden');
  }

  var login = document.getElementById('loginForm');
  if (login) {
    login.addEventListener('submit', function (e) {
      e.preventDefault();
      hideError();
      var btn = login.querySelector('button[type=submit]');
      btn.disabled = true;
      API.post('/api/v1/auth/login', {
        email: login.email.value.trim(),
        password: login.password.value
      }).then(function () {
        window.location.href = CTX + '/dashboard';
      }).catch(function (err) {
        showError(err.message);
      }).finally(function () {
        btn.disabled = false;
      });
    });
  }

  var reg = document.getElementById('registerForm');
  if (reg) {
    reg.addEventListener('submit', function (e) {
      e.preventDefault();
      hideError();
      var btn = reg.querySelector('button[type=submit]');
      btn.disabled = true;
      var roleEl = reg.querySelector('input[name=role]:checked');
      API.post('/api/v1/auth/register', {
        name: reg.name.value.trim(),
        email: reg.email.value.trim(),
        password: reg.password.value,
        confirmPassword: reg.confirm.value,
        role: roleEl ? roleEl.value : 'BUYER'
      }).then(function () {
        window.location.href = CTX + '/dashboard';
      }).catch(function (err) {
        showError(err.message);
      }).finally(function () {
        btn.disabled = false;
      });
    });
  }
})();