/* JanuMart — shop page: filters, search, sort & pagination via /api/v1/products. */
(function () {
  'use strict';

  var CTX = window.CTX || '';
  var INIT = new URLSearchParams(window.location.search);

  var state = {
    q: INIT.get('q') || '',
    category: INIT.get('category') || '',
    subcategory: INIT.get('subcategory') || '',
    gender: INIT.get('gender') || '',
    brand: INIT.get('brand') || '',
    color: INIT.get('color') || '',
    size: INIT.get('size') || '',
    minPrice: INIT.get('minPrice') || '',
    maxPrice: INIT.get('maxPrice') || '',
    rating: INIT.get('rating') || '',
    inStock: INIT.get('inStock') === 'true' || INIT.get('inStock') === '1',
    sort: INIT.get('sort') || 'newest',
    page: parseInt(INIT.get('page'), 10) || 1
  };

  var grid = document.getElementById('shopGrid');
  var empty = document.getElementById('shopEmpty');
  var resultCount = document.getElementById('resultCount');
  var pagination = document.getElementById('shopPagination');

  function fallback() {
    return CTX + '/images/fallback.svg';
  }

  function cardHtml(p) {
    var img = p.imageUrl || fallback();
    var avg = p.avgRating;
    var price = money(p.price);
    var name = esc(p.name);
    var brand = esc(p.brand || p.category || '');
    var seller = esc(p.sellerName || 'JanuMart');
    var starsRow = avg ? stars(avg) + ' <span class="rating-text">' + Number(avg).toFixed(1) + '</span><span>(' + (p.ratingCount || 0) + ')</span>'
      : '<span class="muted">New arrival</span>';
    var action = p.stockQty > 0
      ? '<button type="button" class="btn btn-sm btn-primary add-cart" data-id="' + p.id + '">Add to Cart</button>'
      : '<span class="stock-out">Out of Stock</span>';
    return '<div class="product-card">'
      + '<a class="product-img" href="' + CTX + '/product?id=' + p.id + '">'
      + '<img src="' + esc(img) + '" alt="' + name + '" loading="lazy" onerror="this.onerror=null;this.src=\'' + fallback() + '\';">'
      + '</a>'
      + '<div class="product-body">'
      + '<div class="product-brand">' + brand + '</div>'
      + '<a class="product-name" href="' + CTX + '/product?id=' + p.id + '">' + name + '</a>'
      + '<div class="stars-row">' + starsRow + '</div>'
      + '<div class="product-seller">by <b>' + seller + '</b></div>'
      + '<div class="product-foot"><span class="product-price">' + price + '</span>' + action + '</div>'
      + '</div></div>';
  }

  function render(data) {
    var items = data.products || [];
    if (!items.length) {
      grid.innerHTML = '';
      empty.classList.remove('hidden');
      resultCount.innerHTML = 'No products found';
      pagination.innerHTML = '';
      return;
    }
    empty.classList.add('hidden');
    grid.innerHTML = items.map(cardHtml).join('');
    resultCount.innerHTML = 'Showing <b>' + items.length + '</b> of <b>' + data.total + '</b> products';
    renderPagination(data);
  }

  function renderPagination(data) {
    var pages = Math.max(1, data.pages || 1);
    var html = '';
    for (var i = 1; i <= pages; i++) {
      html += '<button type="button" class="page-btn' + (i === data.page ? ' active' : '') + '" data-page="' + i + '">' + i + '</button>';
    }
    pagination.innerHTML = html;
  }

  function fetchProducts() {
    var params = new URLSearchParams();
    Object.keys(state).forEach(function (k) {
      if (String(state[k]) !== '' && state[k] !== false) params.set(k, String(state[k]));
    });
    resultCount.innerHTML = 'Loading products…';
    API.get('/api/v1/products?' + params.toString())
      .then(function (d) {
        render(d.data);
      })
      .catch(function (err) {
        resultCount.innerHTML = err.message;
      });
  }

  function syncControls() {
    var catRadio = document.querySelector('input[name=category][value="' + (state.category.replace(/"/g, '\\"')) + '"]');
    if (!catRadio) catRadio = document.querySelector('input[name=category][value=""]');
    if (catRadio) catRadio.checked = true;

    document.querySelectorAll('input[name=gender]').forEach(function (r) {
      r.checked = r.value === state.gender;
    });

    var q = document.getElementById('qInput');
    if (q) q.value = state.q;
    var min = document.getElementById('fMin');
    var max = document.getElementById('fMax');
    if (min) min.value = state.minPrice;
    if (max) max.value = state.maxPrice;
    var stock = document.getElementById('fInStock');
    if (stock) stock.checked = state.inStock;
    var rating = document.getElementById('fRating');
    if (rating && state.rating) rating.value = state.rating;
    var sort = document.getElementById('sortSelect');
    if (sort) sort.value = state.sort;
  }

  var metaLoaded = null;
  function loadMeta() {
    if (metaLoaded) return metaLoaded;
    metaLoaded = API.get('/api/v1/meta/filters')
      .then(function (d) {
        var m = d.data || {};
        fillSelect('fSubcategory', m.subcategories, state.subcategory);
        fillSelect('fBrand', m.brands, state.brand);
        fillSelect('fColor', m.colors, state.color);
        fillSelect('fSize', m.sizes, state.size);
      })
      .catch(function () { /* non-fatal */ });
    return metaLoaded;
  }

  function fillSelect(id, values, selected) {
    var sel = document.getElementById(id);
    if (!sel || !Array.isArray(values)) return;
    var keys = values.filter(function (v) { return v && String(v).trim() !== ''; });
    sel.innerHTML = '<option value="">All</option>' + keys.map(function (v) {
      return '<option value="' + esc(String(v)) + '"' + (String(v) === String(selected) ? ' selected' : '') + '>' + esc(String(v)) + '</option>';
    }).join('');
  }

  function readFilters() {
    var min = document.getElementById('fMin');
    var max = document.getElementById('fMax');
    var stock = document.getElementById('fInStock');
    var rating = document.getElementById('fRating');
    var sub = document.getElementById('fSubcategory');
    var brand = document.getElementById('fBrand');
    var color = document.getElementById('fColor');
    var size = document.getElementById('fSize');

    state.category = (document.querySelector('input[name=category]:checked') || {}).value || '';
    state.gender = (document.querySelector('input[name=gender]:checked') || {}).value || '';
    state.subcategory = sub ? sub.value : '';
    state.brand = brand ? brand.value : '';
    state.color = color ? color.value : '';
    state.size = size ? size.value : '';
    state.minPrice = min ? min.value.trim() : '';
    state.maxPrice = max ? max.value.trim() : '';
    state.rating = rating ? rating.value : '';
    state.inStock = stock ? stock.checked : false;
    state.page = 1;
  }

  function applyAndFetch() {
    readFilters();
    fetchProducts();
  }

  document.addEventListener('DOMContentLoaded', function () {
    syncControls();
    loadMeta();
    fetchProducts();

    var searchBtn = document.getElementById('searchBtn');
    var qInput = document.getElementById('qInput');
    if (searchBtn) {
      searchBtn.addEventListener('click', function () {
        state.q = qInput ? qInput.value.trim() : '';
        state.page = 1;
        fetchProducts();
      });
    }
    if (qInput) {
      qInput.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
          state.q = qInput.value.trim();
          state.page = 1;
          fetchProducts();
        }
      });
    }

    var applyBtn = document.getElementById('applyFilters');
    if (applyBtn) applyBtn.addEventListener('click', applyAndFetch);

    var clearBtn = document.getElementById('clearFilters');
    if (clearBtn) {
      clearBtn.addEventListener('click', function () {
        state = {
          q: state.q, category: '', subcategory: '', gender: '', brand: '',
          color: '', size: '', minPrice: '', maxPrice: '', rating: '',
          inStock: false, sort: 'newest', page: 1
        };
        syncControls();
        loadMeta();
        fetchProducts();
      });
    }

    var sort = document.getElementById('sortSelect');
    if (sort) {
      sort.addEventListener('change', function () {
        state.sort = sort.value;
        state.page = 1;
        fetchProducts();
      });
    }

    pagination.addEventListener('click', function (e) {
      var btn = e.target.closest('.page-btn');
      if (!btn || btn.classList.contains('active')) return;
      state.page = parseInt(btn.getAttribute('data-page'), 10) || 1;
      fetchProducts();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  });
})();