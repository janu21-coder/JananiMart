<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:formatNumber value="${stats.totalSpent}" type="number" minFractionDigits="2" maxFractionDigits="2" var="dbSpent"/>
<c:set var="pageTitle" value="My Dashboard — JanuMart"/>
<c:set var="activeNav" value=""/>
<c:url value="/images/fallback.svg" var="fbUrl"/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<div class="page">
  <div class="container">
    <div class="page-head">
      <h1 class="page-title">Hello, ${fn:escapeXml(sessionScope.authUser.name)} 👋</h1>
      <p class="page-sub">Your shopping world at a glance.</p>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-icon si-lav">🛍️</div>
        <div><div class="stat-value">${stats.orderCount}</div><div class="stat-label">Orders placed</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon si-green">💰</div>
        <div><div class="stat-value">₹${dbSpent}</div><div class="stat-label">Total spent</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon si-pink">🛒</div>
        <div><div class="stat-value">${stats.cartCount}</div><div class="stat-label">Items in cart</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon si-amber">⭐</div>
        <div><div class="stat-value">${fn:length(recentlyViewed)}</div><div class="stat-label">Recently viewed</div></div>
      </div>
    </div>

    <div class="qa-grid mt-24">
      <a class="qa-tile" href="${ctx}/shop"><span class="emoji">🛍️</span> Explore the Store</a>
      <a class="qa-tile" href="${ctx}/cart"><span class="emoji">🛒</span> View My Cart</a>
      <a class="qa-tile" href="${ctx}/orders"><span class="emoji">📦</span> Track My Orders</a>
      <a class="qa-tile" href="${ctx}/profile"><span class="emoji">⚙️</span> Profile Settings</a>
    </div>

    <div class="dash-grid mt-24">
      <div class="card">
        <div class="card-head">
          <h3>Recent Orders</h3>
          <a class="card-link" href="${ctx}/orders">View all →</a>
        </div>
        <c:choose>
          <c:when test="${empty stats.recentOrders}">
            <p class="muted">You haven't placed any orders yet.</p>
          </c:when>
          <c:otherwise>
            <div class="table-wrap">
              <table class="table">
                <thead>
                  <tr><th>Order</th><th>Date</th><th>Items</th><th>Total</th><th>Status</th></tr>
                </thead>
                <tbody>
                  <c:forEach items="${stats.recentOrders}" var="o">
                    <fmt:formatNumber value="${o.totalAmount}" type="number" minFractionDigits="2" maxFractionDigits="2" var="dbOt"/>
                    <tr>
                      <td><a href="${ctx}/order?id=${o.id}">#${o.id}</a></td>
                      <td>${fn:replace(o.createdAt, 'T', ' ')}</td>
                      <td>${o.itemCount()}</td>
                      <td>₹${dbOt}</td>
                      <td><span class="status-pill st-${fn:escapeXml(o.status)}">${fn:escapeXml(o.status)}</span></td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
          </c:otherwise>
        </c:choose>
      </div>

      <div class="card">
        <div class="card-head"><h3>Recently Viewed</h3></div>
        <c:choose>
          <c:when test="${empty recentlyViewed}">
            <p class="muted">Products you open will appear here for quick access.</p>
            <a class="btn btn-primary btn-sm mt-18" href="${ctx}/shop">Start browsing</a>
          </c:when>
          <c:otherwise>
            <c:forEach items="${recentlyViewed}" var="p">
              <div class="cart-item" style="grid-template-columns:72px 1fr auto;padding:12px">
                <img src="${empty p.imageUrl ? fbUrl : fn:escapeXml(p.imageUrl)}" alt="" onerror="this.onerror=null;this.src='${ctx}/images/fallback.svg';">
                <div>
                  <a class="ci-name" href="${ctx}/product?id=${p.id}">${fn:escapeXml(p.name)}</a>
                  <div class="ci-meta">${fn:escapeXml(p.category)}</div>
                </div>
                <span class="ci-price"><fmt:formatNumber value="${p.price}" type="number" minFractionDigits="2" maxFractionDigits="2" var="dbPv"/>₹${dbPv}</span>
              </div>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>