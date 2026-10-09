/* JanuMart — admin pages: delete users & products. */
(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', function () {
    document.addEventListener('click', function (e) {
      var btn = e.target.closest('.del-user, .del-product');
      if (!btn) return;
      e.preventDefault();
      var id = btn.getAttribute('data-id');
      var isUser = btn.classList.contains('del-user');
      if (!window.confirm(isUser ? 'Delete this user account permanently?' : 'Delete this product permanently?')) return;
      btn.disabled = true;
      var url = isUser ? '/api/v1/admin/users/' + id : '/api/v1/admin/products/' + id;
      API.del(url)
        .then(function () {
          toast(isUser ? 'User deleted.' : 'Product deleted.');
          window.location.reload();
        })
        .catch(function (err) {
          toast(err.message, 'error');
          btn.disabled = false;
        });
    });
  });
})();