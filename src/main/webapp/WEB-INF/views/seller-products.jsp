<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="My Products — JanuMart"/>
<c:set var="activeNav" value=""/>
<c:url value="/images/fallback.svg" var="fbUrl"/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<div class="page">
  <div class="container">
    <div class="page-head flex-between">
      <div>
        <h1 class="page-title">My Products</h1>
        <p class="page-sub">${fn:length(products)} products listed in your catalogue.</p>
      </div>
      <a class="btn btn-primary btn-sm" href="${ctx}/seller/product-form">+ Add New Product</a>
    </div>

    <c:choose>
      <c:when test="${empty products}">
        <div class="cart-empty">
          <div class="empty-icon">📦</div>
          <p class="empty-title">No products yet</p>
          <p>List your first accessory and start selling on JanuMart.</p>
          <a class="btn btn-primary mt-18" href="${ctx}/seller/product-form">Add a Product</a>
        </div>
      </c:when>
      <c:otherwise>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr><th>Product</th><th>Category</th><th>Price</th><th>Stock</th><th>Status</th><th class="actions">Actions</th></tr>
            </thead>
            <tbody>
              <c:forEach items="${products}" var="pr">
                <fmt:formatNumber value="${pr.price}" type="number" minFractionDigits="2" maxFractionDigits="2" var="spPrice"/>
                <tr>
                  <td>
                    <div class="th-product">
                      <img class="thumb" src="${empty pr.imageUrl ? fbUrl : fn:escapeXml(pr.imageUrl)}" alt="" onerror="this.onerror=null;this.src='${ctx}/images/fallback.svg';">
                      <div><b>${fn:escapeXml(pr.name)}</b><span>#${pr.id}</span></div>
                    </div>
                  </td>
                  <td>${fn:escapeXml(pr.category)}<br><span class="muted text-sm">${fn:escapeXml(empty pr.subcategory ? '—' : pr.subcategory)}</span></td>
                  <td>₹${spPrice}</td>
                  <td>
                    <c:choose>
                      <c:when test="${pr.stockQty == 0}"><span class="stock-out">Out of stock</span></c:when>
                      <c:when test="${pr.stockQty <= 5}"><span class="stock-badge low">${pr.stockQty} left</span></c:when>
                      <c:otherwise><span class="stock-badge">${pr.stockQty} in stock</span></c:otherwise>
                    </c:choose>
                  </td>
                  <td>
                    <c:choose>
                      <c:when test="${pr.inStock}"><span class="badge-count">Active</span></c:when>
                      <c:otherwise><span class="status-pill st-CANCELLED">Draft</span></c:otherwise>
                    </c:choose>
                  </td>
                  <td>
                    <div class="actions">
                      <a class="btn btn-sm btn-outline" href="${ctx}/seller/product-form?id=${pr.id}">Edit</a>
                      <a class="btn btn-sm btn-ghost" href="${ctx}/product?id=${pr.id}">View</a>
                      <button type="button" class="btn btn-sm btn-danger del-product" data-id="${pr.id}">Delete</button>
                    </div>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>
<script defer src="${ctx}/js/seller.js"></script>