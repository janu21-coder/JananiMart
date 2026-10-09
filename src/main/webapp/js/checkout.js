/* JanuMart — checkout: summary + placing the order (mock payment). */
(function () {
  'use strict';

  var CTX = window.CTX || '';

  var form = document.getElementById('checkoutForm');
  var itemsBox = document.getElementById('checkoutItems');
  var totalEl = document.getElementById('checkoutTotal');

  function renderSummary(data) {
    var items = data.items || [];
    if (!items.length) {
      itemsBox.innerHTML = '<div class="muted">Your cart is empty.</div>';
      totalEl.textContent = money(0);
      window.location.href = CTX + '/cart';
      return;
    }
    itemsBox.innerHTML = items.map(function (it) {
      return '<div class="summary-row"><span>' + esc(it.productName) + ' × ' + it.quantity + '</span><span class="muted">' + money(it.price * it.quantity) + '</span></div>';
    }).join('');
    totalEl.textContent = money(data.subtotal);
  }

  function load() {
    API.get('/api/v1/cart')
      .then(function (d) { renderSummary(d.data); })
      .catch(function () { /* api.js will bounce to login */ });
  }

  document.addEventListener('DOMContentLoaded', function () {
    load();

    form.addEventListener('submit', function (e) {
      e.preventDefault();
      var err = document.getElementById('checkoutError');
      var btn = document.getElementById('placeOrderBtn');

      var payment = form.querySelector('input[name=paymentMethod]:checked');
      if (!payment) {
        if (err) { err.textContent = 'Please choose a payment method.'; err.classList.remove('hidden'); }
        return;
      }

      btn.disabled = true;
      var payload = {
        customerName: form.customerName.value.trim(),
        address: form.address.value.trim(),
        city: form.city.value.trim(),
        state: form.state.value.trim(),
        pincode: form.pincode.value.trim(),
        phone: form.phone.value.trim(),
        paymentMethod: payment.value
      };

      API.post('/api/v1/orders', payload)
        .then(function (d) {
          var order = d && d.data;
          toast('Order placed! Redirecting…');
          window.location.href = CTX + '/order-success?id=' + order.id;
        })
        .catch(function (ex) {
          if (err) { err.textContent = ex.message; err.classList.remove('hidden'); }
          else { toast(ex.message, 'error'); }
        })
        .finally(function () { btn.disabled = false; });
    });
  });
})();