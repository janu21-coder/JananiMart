/* JanuMart — seller pages: product create/update/delete. */
(function () {
  'use strict';

  var CTX = window.CTX || '';

  function readForm(form) {
    var data = {
      name: form.elements['name'].value.trim(),
      description: form.elements['description'].value.trim(),
      price: parseFloat(form.elements['price'].value),
      stockQty: parseInt(form.elements['stockQty'].value, 10) || 0,
      category: form.elements['category'].value,
      subcategory: form.elements['subcategory'].value.trim(),
      gender: form.elements['gender'].value,
      brand: form.elements['brand'].value.trim(),
      color: form.elements['color'].value.trim(),
      size: form.elements['size'].value.trim(),
      material: form.elements['material'].value.trim(),
      imageUrl: form.elements['imageUrl'].value.trim()
    };
    return data;
  }

  document.addEventListener('DOMContentLoaded', function () {
    var form = document.getElementById('productForm');
    if (form) {
      form.addEventListener('submit', function (e) {
        e.preventDefault();
        var err = document.getElementById('pfErr');
        var btn = form.querySelector('button[type=submit]');
        var editing = form.elements['productId']; // hidden field present when editing
        var id = editing ? editing.value : null;
        var payload = readForm(form);

        btn.disabled = true;
        var req = id
          ? API.put('/api/v1/products/' + id, payload)
          : API.post('/api/v1/products', payload);
        req.then(function () {
          toast(id ? 'Product updated.' : 'Product listed!');
          window.location.href = CTX + '/seller/products';
        }).catch(function (ex) {
          if (err) { err.textContent = ex.message; err.classList.remove('hidden'); }
          else { toast(ex.message, 'error'); }
        }).finally(function () {
          btn.disabled = false;
        });
      });
    }

    document.addEventListener('click', function (e) {
      var btn = e.target.closest('.del-product');
      if (!btn) return;
      e.preventDefault();
      var id = btn.getAttribute('data-id');
      if (!window.confirm('Delete this product permanently?')) return;
      btn.disabled = true;
      API.del('/api/v1/products/' + id)
        .then(function () {
          toast('Product deleted.');
          window.location.reload();
        })
        .catch(function (err) {
          toast(err.message, 'error');
          btn.disabled = false;
        });
    });
  });
})();