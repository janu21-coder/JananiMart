/* JanuMart — shared API helper + tiny UI utilities (vanilla JS, no frameworks). */
(function () {
  'use strict';

  var CTX = window.CTX || '';

  async function request(method, url, body) {
    var opts = { method: method, headers: {}, credentials: 'same-origin' };
    if (body !== undefined) {
      opts.headers['Content-Type'] = 'application/json';
      opts.body = JSON.stringify(body);
    }
    var res;
    try {
      res = await fetch(CTX + url, opts);
    } catch (e) {
      throw new Error('Network error. Please try again.');
    }

    var data = null;
    try {
      data = await res.json();
    } catch (e) {
      data = null;
    }

    if (!res.ok) {
      var msg = data && data.message ? data.message : 'Something went wrong. Please try again.';
      var isAuth = url.indexOf('/api/v1/auth/login') === 0
        || url.indexOf('/api/v1/auth/register') === 0
        || url.indexOf('/api/v1/auth/me') === 0;
      if (res.status === 401 && !isAuth && window.location.pathname !== CTX + '/login') {
        window.location.href = CTX + '/login';
        throw new Error(msg);
      }
      if (res.status === 403 && msg) {
        throw new Error(msg);
      }
      throw new Error(msg);
    }
    return data;
  }

  window.API = {
    get: function (u) { return request('GET', u); },
    post: function (u, b) { return request('POST', u, b); },
    put: function (u, b) { return request('PUT', u, b); },
    del: function (u) { return request('DELETE', u); }
  };

  window.esc = function (s) {
    return String(s == null ? '' : s).replace(/[&<>"']/g, function (m) {
      return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m];
    });
  };

  window.money = function (n) {
    var v = Number(n || 0);
    return '\u20B9' + v.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  };

  window.stars = function (avg) {
    var pct = Math.max(0, Math.min(100, Math.round(((avg == null ? 0 : avg) / 5) * 100)));
    return '<span class="stars">\u2605\u2605\u2605\u2605\u2605<span class="stars-fill" style="width:' + pct + '%">\u2605\u2605\u2605\u2605\u2605</span></span>';
  };

  window.toast = function (msg, type) {
    var t = document.getElementById('toast');
    if (!t) {
      t = document.createElement('div');
      t.id = 'toast';
      document.body.appendChild(t);
    }
    t.textContent = msg;
    t.className = 'toast' + (type === 'error' ? ' error' : '') + ' show';
    clearTimeout(t._tm);
    t._tm = setTimeout(function () {
      t.className = 'toast';
    }, 2800);
  };
})();