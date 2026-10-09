/* JanuMart — profile settings: update name & change password. */
(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', function () {
    var ok = document.getElementById('profileOk');

    function flash(text) {
      if (ok) {
        ok.textContent = text;
        ok.classList.remove('hidden');
        clearTimeout(ok._tm);
        ok._tm = setTimeout(function () { ok.classList.add('hidden'); }, 4000);
      }
    }

    var profileForm = document.getElementById('profileForm');
    if (profileForm) {
      profileForm.addEventListener('submit', function (e) {
        e.preventDefault();
        var btn = profileForm.querySelector('button[type=submit]');
        btn.disabled = true;
        API.put('/api/v1/profile', { name: profileForm.name.value.trim() })
          .then(function () { flash('Profile updated successfully.'); })
          .catch(function (err) { toast(err.message, 'error'); })
          .finally(function () { btn.disabled = false; });
      });
    }

    var pwForm = document.getElementById('passwordForm');
    if (pwForm) {
      pwForm.addEventListener('submit', function (e) {
        e.preventDefault();
        if (pwForm.newPassword.value !== pwForm.confirmPassword.value) {
          toast('New passwords do not match.', 'error');
          return;
        }
        var btn = pwForm.querySelector('button[type=submit]');
        btn.disabled = true;
        API.put('/api/v1/profile/password', {
          currentPassword: pwForm.currentPassword.value,
          newPassword: pwForm.newPassword.value,
          confirmPassword: pwForm.confirmPassword.value
        }).then(function () {
          flash('Password updated successfully.');
          pwForm.reset();
        }).catch(function (err) {
          toast(err.message, 'error');
        }).finally(function () {
          btn.disabled = false;
        });
      });
    }
  });
})();