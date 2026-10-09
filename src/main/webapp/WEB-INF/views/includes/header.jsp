<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- Janu AI chat is a customer-facing widget: shown for anonymous visitors and
     buyers, hidden on administrator screens (admin pages are only reachable
     by the ADMIN role). --%>
<c:set var="showChatbot" value="${empty sessionScope.authUser or sessionScope.authUser.role ne 'ADMIN'}"/>
<script>window.CTX = '${ctx}';</script>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>${fn:escapeXml(empty pageTitle ? 'JanuMart — Accessories for Human' : pageTitle)}</title>
<link rel="icon" href="${ctx}/images/favicon.svg" type="image/svg+xml">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Poppins:wght@500;600;700;800&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${ctx}/css/style.css">
<c:if test="${showChatbot}">
<link rel="stylesheet" href="${ctx}/css/chatbot.css">
</c:if>
</head>
<body>
<c:set var="u" value="${sessionScope.authUser}"/>
<header class="navbar">
  <div class="nav-inner">
    <button class="hamburger" id="openMenu" aria-label="Open menu">☰</button>
    <a class="logo" href="${ctx}/">JanuMart<span class="logo-dot"></span><span class="logo-tag">Accessories for Human.</span></a>
    <nav class="nav-links">
      <a class="nav-link ${activeNav eq 'home' ? 'active' : ''}" href="${ctx}/">Home</a>
      <a class="nav-link ${activeNav eq 'shop' ? 'active' : ''}" href="${ctx}/shop">Shop</a>
      <div class="dropdown">
        <a class="nav-link" href="${ctx}/shop">Categories<span class="dropdown-caret">▼</span></a>
        <div class="dropdown-menu">
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:2px;min-width:300px">
            <c:forEach items="${applicationScope.APP_CATEGORIES}" var="cat">
              <a href="${ctx}/shop?category=${fn:escapeXml(cat.name)}">${fn:escapeXml(cat.name)}</a>
            </c:forEach>
          </div>
        </div>
      </div>
      <a class="nav-link" href="${ctx}/shop?category=Watches">Watches</a>
      <a class="nav-link" href="${ctx}/shop?category=Bags">Bags</a>
      <a class="nav-link" href="${ctx}/shop?category=Jewellery">Jewellery</a>
    </nav>
    <div class="nav-actions">
      <c:choose>
        <c:when test="${not empty u}">
          <c:if test="${u.role eq 'BUYER'}">
            <a class="icon-btn" href="${ctx}/cart" aria-label="Cart">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="9" cy="21" r="1.6"/><circle cx="19" cy="21" r="1.6"/>
                <path d="M2.5 3h2l2.4 12.2a2 2 0 0 0 2 1.6h8.9a2 2 0 0 0 2-1.6L22 7H6"/>
              </svg>
              <span class="cart-badge hidden" id="cartCount">0</span>
            </a>
          </c:if>
          <div class="dropdown">
            <a class="avatar-chip" href="${ctx}/dashboard">
              <span class="avatar-dot">${fn:substring(u.name, 0, 1)}</span>${fn:escapeXml(u.name)}<span class="dropdown-caret">▼</span>
            </a>
            <div class="dropdown-menu right">
              <a href="${ctx}/dashboard">My Dashboard</a>
              <c:choose>
                <c:when test="${u.role eq 'BUYER'}">
                  <a href="${ctx}/orders">My Orders</a>
                  <a href="${ctx}/cart">My Cart</a>
                  <a href="${ctx}/profile">Profile Settings</a>
                </c:when>
                <c:when test="${u.role eq 'SELLER'}">
                  <a href="${ctx}/seller/dashboard">Seller Overview</a>
                  <a href="${ctx}/seller/products">My Products</a>
                  <a href="${ctx}/seller/product-form">Add Product</a>
                  <a href="${ctx}/seller/orders">Orders I Received</a>
                  <a href="${ctx}/profile">Profile Settings</a>
                </c:when>
                <c:otherwise>
                  <a href="${ctx}/admin/dashboard">Admin Overview</a>
                  <a href="${ctx}/admin/users">Manage Users</a>
                  <a href="${ctx}/admin/products">Manage Products</a>
                  <a href="${ctx}/admin/orders">Manage Orders</a>
                </c:otherwise>
              </c:choose>
              <a href="#" id="logoutBtn">Logout</a>
            </div>
          </div>
        </c:when>
        <c:otherwise>
          <a class="btn btn-sm btn-outline" href="${ctx}/login">Login</a>
          <a class="btn btn-sm btn-primary" href="${ctx}/register">Register</a>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
  <div class="mobile-menu" id="mobileMenu">
    <a href="${ctx}/">Home</a>
    <a href="${ctx}/shop">Shop All</a>
    <div class="m-label">Categories</div>
    <c:forEach items="${applicationScope.APP_CATEGORIES}" var="cat">
      <a href="${ctx}/shop?category=${fn:escapeXml(cat.name)}">${fn:escapeXml(cat.name)}</a>
    </c:forEach>
    <c:choose>
      <c:when test="${not empty u}">
        <div class="m-label">Account</div>
        <a href="${ctx}/dashboard">My Dashboard</a>
        <c:choose>
          <c:when test="${u.role eq 'BUYER'}">
            <a href="${ctx}/orders">My Orders</a>
            <a href="${ctx}/cart">My Cart</a>
            <a href="${ctx}/profile">Profile Settings</a>
          </c:when>
          <c:when test="${u.role eq 'SELLER'}">
            <a href="${ctx}/seller/products">My Products</a>
            <a href="${ctx}/seller/orders">Orders I Received</a>
            <a href="${ctx}/profile">Profile Settings</a>
          </c:when>
          <c:otherwise>
            <a href="${ctx}/admin/users">Manage Users</a>
            <a href="${ctx}/admin/products">Manage Products</a>
            <a href="${ctx}/admin/orders">Manage Orders</a>
          </c:otherwise>
        </c:choose>
        <a href="#" data-logout>Logout</a>
      </c:when>
      <c:otherwise>
        <div class="m-label">Account</div>
        <a href="${ctx}/login">Login</a>
        <a href="${ctx}/register">Register</a>
      </c:otherwise>
    </c:choose>
  </div>
</header>