/* JanuMart — cart page: list, quantity and removal via /api/v1/cart. */
(function () {
  'use strict';

  var CTX = window.CTX || '';
  var fallback = CTX + '/images/fallback.svg';

  var itemsBox = document.getElementById('cartItems');

  function itemHtml(it) {
    var img = it.imageUrl || fallback();
    var canUp = it.quantity < it.stock;
    var canDown = it.quantity > 1;
    return '<div class="cart-item">'
      + '<img src="' + esc(img) + '" alt="' + esc(it.productName) + '" onerror="this.onerror=null;this.src=\'' + fallback + '\';">'
      + '<div>'
      + '<a class="ci-name" href="' + CTX + '/product?id=' + it.productId + '">' + esc(it.productName) + '</a>'
      + '<div class="ci-meta">by ' + esc(it.sellerName || 'JanuMart') + ' · ' + money(it.price) + ' each</div>'
      + '<div class="qty mt-18" style="margin-top:10px">'
      + '<button type="button" class="qty-btn qty-minus" data-id="' + it.productId + '"' + (canDown ? '' : ' disabled') + '>−</button>'
      + '<span class="qty-input">' + it.quantity + '</span>'
      + '<button type="button" class="qty-btn qty-plus" data-id="' + it.productId + '"' + (canUp ? '' : ' disabled') + '>+</button>'
      + '</div>'
      + '</div>'
      + '<div class="ci-right">'
      + '<span class="ci-price">' + money(it.price * it.quantity) + '</span>'
      + '<button type="button" class="ci-remove" data-id="' + it.productId + '">Remove</button>'
      + '</div>'
      + '</div>';
  }

  function emptyHtml() {
    return '<div class="cart-empty">'
      + '<div class="empty-icon">🛒</div>'
      + '<p class="empty-title">Your cart is empty</p>'
      + '<p>Add some accessories you love and come back here to check out.</p>'
      + '<a class="btn btn-primary mt-18" href="' + CTX + '/shop">Browse the Store</a>'
      + '</div>';
  }

  function render(data) {
    var items = data.items || [];
    var subtotal = data.subtotal || 0;
    var count = data.count || 0;

    itemsBox.innerHTML = items.length ? items.map(itemHtml).join('') : emptyHtml();

    document.getElementById('summaryCount').textContent = String(count);
    document.getElementById('summarySubtotal').textContent = money(subtotal);
    document.getElementById('summaryDelivery').textContent = items.length ? 'FREE' : '—';
    document.getElementById('summaryTotal').textContent = money(subtotal);
    if (window.updateCartBadge) window.updateCartBadge();
  }

  function load() {
    itemsBox.innerHTML = '<div class="card text-center"><p>Loading your cart…</p></div>';
    API.get('/api/v1/cart')
      .then(function (d) { render(d.data); })
      .catch(function (err) {
        itemsBox.innerHTML = '<div class="card text-center"><p class="field-error">' + esc(err.message) + '</p></div>';
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    load();

    var checkoutBtn = document.getElementById('checkoutBtn');
    if (checkoutBtn) {
      checkoutBtn.addEventListener('click', function () {
        window.location.href = CTX + '/checkout';
      });
    }

    itemsBox.addEventListener('click', function (e) {
      var minus = e.target.closest('.qty-minus');
      var plus = e.target.closest('.qty-plus');
      var remove = e.target.closest('.ci-remove');
      if (minus || plus) {
        var dir = minus ? -1 : 1;
        var id = (minus || plus).getAttribute('data-id');
        API.get('/api/v1/cart')
          .then(function (d) {
            var item = (d.data.items || []).find(function (i) { return String(i.productId) === id; });
            if (!item) return;
            return API.put('/api/v1/cart/' + id, { quantity: item.quantity + dir });
          })
          .then(function (d) {
            if (d) { render(d.data); toast('Cart updated'); }
          })
          .catch(function (err) { toast(err.message, 'error'); });
      } else if (remove) {
        var rid = remove.getAttribute('data-id');
        API.del('/api/v1/cart/' + rid)
          .then(function (d) { render(d.data); toast('Item removed from cart'); })
          .catch(function (err) { toast(err.message, 'error'); });
      }
    });
  });
})();