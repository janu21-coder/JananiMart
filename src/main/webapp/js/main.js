/* JanuMart — global delegated handlers (add-to-cart buttons everywhere). */
(function () {
  'use strict';

  document.addEventListener('click', function (e) {
    var btn = e.target.closest('.add-cart');
    if (!btn) return;
    e.preventDefault();

    var id = btn.getAttribute('data-id');
    if (!id) return;

    var qty = 1;
    var target = btn.getAttribute('data-qty-target');
    if (target) {
      var input = document.getElementById(target);
      if (input && input.value) {
        qty = parseInt(input.value, 10) || 1;
      }
    } else if (btn.getAttribute('data-qty')) {
      qty = parseInt(btn.getAttribute('data-qty'), 10) || 1;
    }

    btn.disabled = true;
    API.post('/api/v1/cart', { productId: parseInt(id, 10), quantity: qty })
      .then(function () {
        toast('Added to cart!');
        if (window.updateCartBadge) {
          window.updateCartBadge();
        }
      })
      .catch(function (err) {
        toast(err.message, 'error');
      })
      .finally(function () {
        btn.disabled = false;
      });
  });
})();