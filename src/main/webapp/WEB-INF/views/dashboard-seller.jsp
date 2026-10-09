<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:formatNumber value="${stats.revenue}" type="number" minFractionDigits="2" maxFractionDigits="2" var="dsRev"/>
<c:set var="pageTitle" value="Seller Dashboard — JanuMart"/>
<c:set var="activeNav" value=""/>
<c:url value="/images/fallback.svg" var="fbUrl"/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<div class="page">
  <div class="container">
    <div class="page-head flex-between">
      <div>
        <h1 class="page-title">Seller Dashboard</h1>
        <p class="page-sub">Manage your catalogue, stock and incoming orders.</p>
      </div>
      <a class="btn btn-primary btn-sm" href="${ctx}/seller/product-form">+ Add New Product</a>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-icon si-lav">📦</div>
        <div><div class="stat-value">${stats.productCount}</div><div class="stat-label">Listed products</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon si-green">🟢</div>
        <div><div class="stat-value">${stats.activeProducts}</div><div class="stat-label">Active (in stock)</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon si-pink">🧾</div>
        <div><div class="stat-value">${stats.orderCount}</div><div class="stat-label">Orders received</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon si-amber">💰</div>
        <div><div class="stat-value">₹${dsRev}</div><div class="stat-label">Revenue earned</div></div>
      </div>
    </div>

    <div class="dash-grid mt-24">
      <div class="card">
        <div class="card-head"><h3>Top Selling Products</h3></div>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr><th>Product</th><th>Price</th><th>Stock</th><th>Sold</th></tr>
            </thead>
            <tbody>
              <c:forEach items="${stats.topProducts}" var="tp">
                <fmt:formatNumber value="${tp.price}" type="number" minFractionDigits="2" maxFractionDigits="2" var="dsTp"/>
                <tr>
                  <td>
                    <div class="th-product">
                      <img class="thumb" src="${empty tp.imageUrl ? fbUrl : fn:escapeXml(tp.imageUrl)}" alt="" onerror="this.onerror=null;this.src='${ctx}/images/fallback.svg';">
                      <div><b>${fn:escapeXml(tp.name)}</b><span>${fn:escapeXml(tp.category)}</span></div>
                    </div>
                  </td>
                  <td>₹${dsTp}</td>
                  <td>${tp.stockQty}</td>
                  <td><span class="badge-count">${tp.soldQty} sold</span></td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card">
        <div class="card-head"><h3>Low Stock Alerts</h3></div>
        <c:choose>
          <c:when test="${empty stats.lowStockProducts}">
            <p class="muted">Nothing running low — great job! 🎉</p>
          </c:when>
          <c:otherwise>
            <c:forEach items="${stats.lowStockProducts}" var="lp">
              <div class="cart-item" style="grid-template-columns:72px 1fr auto;padding:12px">
                <img src="${empty lp.imageUrl ? fbUrl : fn:escapeXml(lp.imageUrl)}" alt="" onerror="this.onerror=null;this.src='${ctx}/images/fallback.svg';">
                <div>
                  <a class="ci-name" href="${ctx}/seller/product-form?id=${lp.id}">${fn:escapeXml(lp.name)}</a>
                  <div class="ci-meta">${fn:escapeXml(lp.category)}</div>
                </div>
                <span class="stock-badge low">${lp.stockQty} left</span>
              </div>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </div>
    </div>

    <div class="dash-grid mt-24" style="grid-template-columns:1fr">
      <div class="card">
        <div class="card-head">
          <h3>Recent Orders</h3>
          <a class="card-link" href="${ctx}/seller/orders">Manage all →</a>
        </div>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr><th>Order</th><th>Date</th><th>Buyer</th><th>Items</th><th>Status</th></tr>
            </thead>
            <tbody>
              <c:forEach items="${stats.recentOrders}" var="o">
                <tr>
                  <td><a href="${ctx}/seller/orders">#${o.id}</a></td>
                  <td>${fn:replace(o.createdAt, 'T', ' ')}</td>
                  <td>${fn:escapeXml(o.buyerName)}</td>
                  <td>${o.itemCount()}</td>
                  <td><span class="status-pill st-${fn:escapeXml(o.status)}">${fn:escapeXml(o.status)}</span></td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <c:if test="${not empty stats.newArrivals}">
      <div class="section-head mt-24">
        <div>
          <p class="section-eyebrow">Your fresh picks</p>
          <h2 class="section-title">Newly Added Products</h2>
        </div>
      </div>
      <div class="product-grid">
        <c:forEach items="${stats.newArrivals}" var="p">
          <%@ include file="/WEB-INF/views/fragments/product-card.jspf" %>
        </c:forEach>
      </div>
    </c:if>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>