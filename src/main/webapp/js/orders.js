/* JanuMart — order actions: buyer cancel + seller/admin status updates. */
(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', function () {
    document.addEventListener('click', function (e) {
      var btn = e.target.closest('.order-cancel');
      if (!btn) return;
      e.preventDefault();
      var id = btn.getAttribute('data-order');
      if (!id) return;
      if (!window.confirm('Cancel this order? This cannot be undone.')) return;
      btn.disabled = true;
      API.put('/api/v1/orders/' + id + '/status', { status: 'CANCELLED' })
        .then(function () {
          toast('Order cancelled.');
          window.location.reload();
        })
        .catch(function (err) {
          toast(err.message, 'error');
          btn.disabled = false;
        });
    });

    document.addEventListener('change', function (e) {
      var sel = e.target.closest('.order-status');
      if (!sel || !sel.value) return;
      var id = sel.getAttribute('data-order');
      var next = sel.value;
      sel.disabled = true;
      API.put('/api/v1/orders/' + id + '/status', { status: next })
        .then(function () {
          toast('Order marked as ' + next + '.');
          window.location.reload();
        })
        .catch(function (err) {
          toast(err.message, 'error');
          sel.disabled = false;
        });
    });
  });
})();