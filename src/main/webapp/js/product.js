/* JanuMart — product detail: qty stepper, reviews, review form. */
(function () {
  'use strict';

  var CTX = window.CTX || '';
  var params = new URLSearchParams(window.location.search);
  var productId = params.get('id');
  var fallback = CTX + '/images/fallback.svg';

  var qtyInput = document.getElementById('qtyInput');
  var qtyMinus = document.getElementById('qtyMinus');
  var qtyPlus = document.getElementById('qtyPlus');

  if (qtyInput && qtyMinus && qtyPlus) {
    var maxQty = parseInt(qtyInput.getAttribute('max'), 10) || 1;
    qtyMinus.addEventListener('click', function () {
      var v = parseInt(qtyInput.value, 10) || 1;
      qtyInput.value = String(Math.max(1, v - 1));
    });
    qtyPlus.addEventListener('click', function () {
      var v = parseInt(qtyInput.value, 10) || 1;
      qtyInput.value = String(Math.min(maxQty, v + 1));
    });
  }

  function avatar(name) {
    return (name || '?').trim().charAt(0).toUpperCase();
  }

  function renderReviews(list) {
    var box = document.getElementById('reviewsList');
    if (!box) return;
    if (!list || !list.length) {
      box.innerHTML = '<div class="card text-center muted">No reviews yet — be the first to review this accessory!</div>';
      return;
    }
    box.innerHTML = list.map(function (r) {
      return '<div class="review-card">'
        + '<div class="review-head">'
        + '<span class="review-avatar">' + esc(avatar(r.userName || r.userName)) + '</span>'
        + '<div><div class="review-name">' + esc(r.userName) + '</div>'
        + '<div class="review-date">' + esc(r.createdAt) + '</div></div>'
        + '<div class="stars-row" style="margin-left:auto">' + stars(r.rating) + '</div>'
        + '</div>'
        + '<p class="review-text">' + esc(r.comment || 'No comment.') + '</p>'
        + '</div>';
    }).join('');
  }

  function loadReviews() {
    if (!productId) return;
    API.get('/api/v1/products/' + productId + '/reviews')
      .then(function (d) { renderReviews(d.data); })
      .catch(function (err) {
        var box = document.getElementById('reviewsList');
        if (box) box.innerHTML = '<div class="card muted">' + esc(err.message) + '</div>';
      });
  }

  function initReviewForm() {
    var form = document.getElementById('reviewForm');
    if (!form || !productId) return;

    API.get('/api/v1/auth/me').then(function (d) {
      if (d && d.data && d.data.role === 'BUYER') {
        form.classList.remove('hidden');
      }
    }).catch(function () { /* not signed in */ });

    var input = document.getElementById('ratingInput');
    var hidden = document.getElementById('ratingValue');
    if (input) {
      input.addEventListener('click', function (e) {
        var star = e.target.closest('span[data-val]');
        if (!star) return;
        var val = parseInt(star.getAttribute('data-val'), 10);
        hidden.value = String(val);
        input.querySelectorAll('span').forEach(function (s) {
          s.classList.toggle('on', parseInt(s.getAttribute('data-val'), 10) <= val);
        });
      });
    }

    form.addEventListener('submit', function (e) {
      e.preventDefault();
      var err = document.getElementById('reviewError');
      var rating = parseInt(hidden.value, 10) || 0;
      var comment = document.getElementById('reviewComment').value.trim();
      if (!rating) {
        if (err) { err.textContent = 'Please pick a star rating.'; err.classList.remove('hidden'); }
        return;
      }
      if (err) err.classList.add('hidden');
      var btn = form.querySelector('button[type=submit]');
      btn.disabled = true;
      API.post('/api/v1/reviews', { productId: parseInt(productId, 10), rating: rating, comment: comment })
        .then(function () {
          toast('Thanks! Your review was posted.');
          form.reset();
          hidden.value = '0';
          if (input) input.querySelectorAll('span').forEach(function (s) { s.classList.remove('on'); });
          form.classList.add('hidden');
          loadReviews();
        })
        .catch(function (ex) {
          if (err) { err.textContent = ex.message; err.classList.remove('hidden'); }
          else { toast(ex.message, 'error'); }
        })
        .finally(function () { btn.disabled = false; });
    });
  }

  document.addEventListener('DOMContentLoaded', function () {
    loadReviews();
    initReviewForm();
  });
})();