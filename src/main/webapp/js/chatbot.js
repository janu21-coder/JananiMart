/* ==========================================================================
   Janu AI Shopping Assistant — frontend widget (vanilla JS, no frameworks).
   Loaded on customer-facing pages (see footer.jsp). Talks to
   POST /api/v1/chatbot/message and renders real catalog product cards.
   ========================================================================== */
(function () {
  'use strict';

  var CTX = window.CTX || '';
  var MAX_LEN = 500;
  var STORAGE_KEY = 'janumart.chatbot.history';

  var ICON_CHAT = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M21 11.5a8.5 8.5 0 0 1-8.5 8.5c-1.5 0-3-.4-4.2-1L3 20l1.3-4.1A8.5 8.5 0 1 1 21 11.5z"/><path d="M8.5 11.5h.01M12 11.5h.01M15.5 11.5h.01"/></svg>';
  var ICON_CLOSE = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18"/></svg>';
  var ICON_TRASH = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 7h16M9 7V5a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2m3 0-.8 13a2 2 0 0 1-2 1.9H7.8a2 2 0 0 1-2-1.9L5 7"/><path d="M10 11v6m4-6v6"/></svg>';
  var ICON_SEND = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M22 2 11 13M22 2 15 22l-4-9-9-4 20-7z"/></svg>';
  var FB_IMG = CTX + '/images/fallback.svg';

  var state = { open: false, busy: false, history: loadHistory(), lastSent: 0 };
  var els = {};

  function loadHistory() {
    try {
      var raw = sessionStorage.getItem(STORAGE_KEY);
      if (!raw) { return []; }
      var arr = JSON.parse(raw);
      if (!Array.isArray(arr)) { return []; }
      return arr
        .filter(function (m) { return m && (m.role === 'user' || m.role === 'assistant') && typeof m.content === 'string'; })
        .slice(-40);
    } catch (e) {
      return [];
    }
  }

  function saveHistory() {
    try {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state.history.slice(-40)));
    } catch (e) { /* storage unavailable — history lives in memory only */ }
  }

  function clearHistory() {
    state.history = [];
    saveHistory();
  }

  /* ------------------------- DOM build ------------------------- */

  function build() {
    if (document.getElementById('janu-chat-fab') || document.getElementById('janu-chat')) {
      return;
    }

    var fab = document.createElement('button');
    fab.id = 'janu-chat-fab';
    fab.className = 'janu-chat-fab';
    fab.setAttribute('type', 'button');
    fab.setAttribute('aria-label', 'Chat with Janu AI');
    fab.innerHTML = ICON_CHAT;

    var win = document.createElement('section');
    win.id = 'janu-chat';
    win.className = 'janu-chat';
    win.setAttribute('role', 'dialog');
    win.setAttribute('aria-label', 'Janu AI Shopping Assistant chat window');

    win.innerHTML =
      '<div class="janu-chat-head">' +
        '<span class="janu-avatar" aria-hidden="true">Janu</span>' +
        '<div class="janu-chat-title">' +
          '<strong>Janu AI Shopping Assistant</strong>' +
          '<span>JANANIMART · Accessories for Human.</span>' +
        '</div>' +
        '<button type="button" class="janu-head-btn" id="januClear" title="Clear conversation" aria-label="Clear conversation">' + ICON_TRASH + '</button>' +
        '<button type="button" class="janu-head-btn" id="januClose" title="Minimize chat" aria-label="Minimize chat">' + ICON_CLOSE + '</button>' +
      '</div>' +
      '<div class="janu-chat-body" id="januBody" aria-live="polite"></div>' +
      '<div class="janu-chips" id="januChips"></div>' +
      '<div class="janu-chat-input">' +
        '<textarea id="januInput" rows="1" maxlength="' + MAX_LEN + '" placeholder="Ask me about accessories…" aria-label="Type a message for Janu AI"></textarea>' +
        '<button type="button" class="janu-send" id="januSend" aria-label="Send message">' + ICON_SEND + '</button>' +
      '</div>' +
      '<div class="janu-charlimit" id="januCount"></div>';

    document.body.appendChild(fab);
    document.body.appendChild(win);

    els = {
      fab: fab, win: win,
      body: document.getElementById('januBody'),
      chips: document.getElementById('januChips'),
      input: document.getElementById('januInput'),
      send: document.getElementById('januSend'),
      clear: document.getElementById('januClear'),
      close: document.getElementById('januClose'),
      count: document.getElementById('januCount')
    };

    fab.addEventListener('click', toggle);
    els.close.addEventListener('click', close);
    els.clear.addEventListener('click', function () {
      clearHistory();
      els.body.innerHTML = '';
      els.chips.innerHTML = '';
      welcome();
    });
    els.send.addEventListener('click', send);
    els.input.addEventListener('keydown', function (e) {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        send();
      }
    });
    els.input.addEventListener('input', function () {
      autoSize();
      updateCount();
    });
    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape') { close(); }
    });
  }

  function toggle() {
    if (state.open) { close(); } else { open(); }
  }

  function open() {
    els.win.classList.add('open');
    state.open = true;
    els.fab.setAttribute('aria-expanded', 'true');
    if (els.body.children.length === 0 && state.history.length === 0) {
      welcome();
    } else if (els.body.children.length === 0) {
      renderHistory();
    }
    els.input.focus();
  }

  function close() {
    els.win.classList.remove('open');
    state.open = false;
    els.fab.setAttribute('aria-expanded', 'false');
  }

  var SUGGESTIONS = [
    'Suggest a gift under ₹1000',
    'Affordable watches',
    'What categories do you have?',
    'How do I track my order?'
  ];

  function welcome() {
    addMsg('assistant', 'Hi! 👋 Welcome to JANANIMART! I\'m Janu, your AI shopping assistant. I can help you discover accessories, explore categories, find products, understand offers, and get guidance with your shopping. What are you looking for today?');
    SUGGESTIONS.forEach(function (s) {
      var chip = document.createElement('button');
      chip.type = 'button';
      chip.className = 'janu-chip';
      chip.textContent = s;
      chip.addEventListener('click', function () { els.input.value = s; send(); });
      els.chips.appendChild(chip);
    });
  }

  function renderHistory() {
    state.history.forEach(function (m) {
      addMsg(m.role, m.content);
    });
  }

  /* ------------------------- rendering ------------------------- */

  function bubble(role, content) {
    var div = document.createElement('div');
    div.className = 'janu-msg ' + role;
    var p = document.createElement('p');
    p.textContent = content;
    div.appendChild(p);
    return div;
  }

  function addMsg(role, content) {
    els.body.appendChild(bubble(role, content));
    scrollDown();
  }

  function typingBubble() {
    var div = document.createElement('div');
    div.className = 'janu-msg assistant janu-typing';
    div.id = 'januTyping';
    div.innerHTML = '<i></i><i></i><i></i>';
    els.body.appendChild(div);
    scrollDown();
    return div;
  }

  function removeTyping() {
    var t = document.getElementById('januTyping');
    if (t && t.parentNode) { t.parentNode.removeChild(t); }
  }

  function scrollDown() {
    els.body.scrollTop = els.body.scrollHeight;
  }

  function inr(n) {
    var v = Number(n);
    if (!isFinite(v)) { v = 0; }
    return '₹' + v.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  function productCard(p) {
    var img = window.esc(p.imageUrl ? p.imageUrl : FB_IMG);
    return '' +
      '<a class="janu-card" href="' + CTX + '/product?id=' + encodeURIComponent(p.id) + '">' +
        '<img src="' + img + '" alt="' + window.esc(p.name) + '" loading="lazy" onerror="this.onerror=null;this.src=\'' + FB_IMG + '\';">' +
        '<div class="janu-card-body">' +
          '<div class="janu-card-cat">' + window.esc(p.category || '') + '</div>' +
          '<div class="janu-card-name">' + window.esc(p.name) + '</div>' +
          '<div class="janu-card-foot">' +
            '<span class="janu-card-price">' + inr(p.price) + '</span>' +
            '<span class="janu-card-stock">' + (p.stockQty > 0 ? 'In stock' : 'Out of stock') + '</span>' +
          '</div>' +
        '</div>' +
      '</a>';
  }

  function renderReply(data) {
    addMsg('assistant', data.reply);

    var products = data.products;
    if (products && products.length) {
      var tag = document.createElement('div');
      tag.className = 'janu-mode-tag';
      tag.textContent = data.mode === 'ai' ? 'Recommendations · real catalog · AI' : 'Recommendations · JanuMart catalog';
      els.body.appendChild(tag);

      var row = document.createElement('div');
      row.className = 'janu-products';
      products.forEach(function (p) { row.insertAdjacentHTML('beforeend', productCard(p)); });
      els.body.appendChild(row);
      scrollDown();
    }
  }

  /* ------------------------- sending ------------------------- */

  function send() {
    if (state.busy) { return; }
    var text = els.input.value.trim();
    if (!text) {
      els.input.focus();
      return;
    }
    if (text.length > MAX_LEN) {
      addMsg('assistant', 'That message is a bit long — please keep it under ' + MAX_LEN + ' characters.');
      return;
    }
    // Small debounce against accidental double submit.
    var now = Date.now();
    if (now - state.lastSent < 500) { return; }
    state.lastSent = now;

    els.input.value = '';
    autoSize();
    updateCount();
    state.busy = true;
    els.send.disabled = true;

    addMsg('user', text);
    state.history.push({ role: 'user', content: text });
    saveHistory();

    var typing = typingBubble();

    var payload = {
      message: text,
      conversationHistory: state.history
        .filter(function (m) { return m.role !== 'user' || m.content !== text; })
        .slice(-8)
    };

    var controller = typeof AbortController !== 'undefined' ? new AbortController() : null;
    var timer = controller ? setTimeout(function () { controller.abort(); }, 30000) : null;

    fetch(CTX + '/api/v1/chatbot/message', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'same-origin',
      body: JSON.stringify(payload),
      signal: controller ? controller.signal : undefined
    }).then(function (res) {
      return res.json().then(function (data) {
        return { status: res.status, body: data };
      }).catch(function () {
        return { status: res.status, body: null };
      });
    }).then(function (r) {
      removeTyping();
      if (r.body && r.body.success && r.body.data) {
        renderReply(r.body.data);
        state.history.push({ role: 'assistant', content: r.body.data.reply });
        saveHistory();
      } else {
        var msg = (r.body && r.body.message) ? r.body.message : 'Something went wrong. Please try again.';
        els.body.appendChild(bubble('assistant error', msg));
        scrollDown();
      }
    }).catch(function () {
      removeTyping();
      els.body.appendChild(bubble('assistant error', 'I\'m having trouble connecting right now. Please try again in a moment.'));
      scrollDown();
    }).finally(function () {
      if (timer) { clearTimeout(timer); }
      state.busy = false;
      els.send.disabled = false;
      els.input.focus();
    });
  }

  /* ------------------------- input helpers ------------------------- */

  function autoSize() {
    els.input.style.height = 'auto';
    els.input.style.height = Math.min(84, els.input.scrollHeight) + 'px';
  }

  function updateCount() {
    var n = els.input.value.length;
    els.count.textContent = n > 0 ? (n + ' / ' + MAX_LEN) : '';
    els.count.classList.toggle('warn', n > MAX_LEN * 0.9);
  }

  /* ------------------------- init ------------------------- */

  function init() {
    build();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();