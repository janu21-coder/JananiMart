<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:formatNumber value="${stats.revenue}" type="number" minFractionDigits="2" maxFractionDigits="2" var="daRev"/>
<c:set var="pageTitle" value="Admin Dashboard — JanuMart"/>
<c:set var="activeNav" value=""/>
<c:url value="/images/fallback.svg" var="fbUrl"/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<div class="page">
  <div class="container">
    <div class="page-head">
      <h1 class="page-title">Admin Dashboard</h1>
      <p class="page-sub">Platform-wide overview of users, products, orders and revenue.</p>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-icon si-lav">👥</div>
        <div><div class="stat-value">${stats.userCount}</div><div class="stat-label">Total users</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon si-green">🛍️</div>
        <div><div class="stat-value">${stats.buyerCount}</div><div class="stat-label">Buyers</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon si-pink">🏪</div>
        <div><div class="stat-value">${stats.sellerCount}</div><div class="stat-label">Sellers</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon si-amber">📦</div>
        <div><div class="stat-value">${stats.productCount}</div><div class="stat-label">Products</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon si-lav">🧾</div>
        <div><div class="stat-value">${stats.orderCount}</div><div class="stat-label">Orders</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon si-green">💰</div>
        <div><div class="stat-value">₹${daRev}</div><div class="stat-label">Total revenue</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon si-green">✅</div>
        <div><div class="stat-value">${stats.delivered}</div><div class="stat-label">Delivered</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon si-red">❌</div>
        <div><div class="stat-value">${stats.cancelled}</div><div class="stat-label">Cancelled</div></div>
      </div>
    </div>

    <div class="qa-grid mt-24">
      <a class="qa-tile" href="${ctx}/admin/users"><span class="emoji">👥</span> Manage Users</a>
      <a class="qa-tile" href="${ctx}/admin/products"><span class="emoji">📦</span> Manage Products</a>
      <a class="qa-tile" href="${ctx}/admin/orders"><span class="emoji">🧾</span> Manage Orders</a>
      <a class="qa-tile" href="${ctx}/api/v1/admin/stats"><span class="emoji">📊</span> Stats (JSON API)</a>
    </div>

    <div class="dash-grid mt-24">
      <div class="card">
        <div class="card-head">
          <h3>Recent Orders</h3>
          <a class="card-link" href="${ctx}/admin/orders">View all →</a>
        </div>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr><th>Order</th><th>Date</th><th>Buyer</th><th>Total</th><th>Status</th></tr>
            </thead>
            <tbody>
              <c:forEach items="${stats.recentOrders}" var="o">
                <fmt:formatNumber value="${o.totalAmount}" type="number" minFractionDigits="2" maxFractionDigits="2" var="daOt"/>
                <tr>
                  <td>#${o.id}</td>
                  <td>${fn:replace(o.createdAt, 'T', ' ')}</td>
                  <td>${fn:escapeXml(o.buyerName)}</td>
                  <td>₹${daOt}</td>
                  <td><span class="status-pill st-${fn:escapeXml(o.status)}">${fn:escapeXml(o.status)}</span></td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card">
        <div class="card-head">
          <h3>Newest Products</h3>
          <a class="card-link" href="${ctx}/admin/products">View all →</a>
        </div>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr><th>Product</th><th>Seller</th><th>Price</th><th>Stock</th></tr>
            </thead>
            <tbody>
              <c:forEach items="${stats.recentProducts}" var="np">
                <fmt:formatNumber value="${np.price}" type="number" minFractionDigits="2" maxFractionDigits="2" var="daNp"/>
                <tr>
                  <td>
                    <div class="th-product">
                      <img class="thumb" src="${empty np.imageUrl ? fbUrl : fn:escapeXml(np.imageUrl)}" alt="" onerror="this.onerror=null;this.src='${ctx}/images/fallback.svg';">
                      <div><b>${fn:escapeXml(np.name)}</b><span>${fn:escapeXml(np.category)}</span></div>
                    </div>
                  </td>
                  <td>${fn:escapeXml(np.sellerName)}</td>
                  <td>₹${daNp}</td>
                  <td>${np.stockQty}</td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>