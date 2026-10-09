<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Manage Products — JanuMart"/>
<c:set var="activeNav" value=""/>
<c:set var="apQ" value="${fn:escapeXml(param.q)}"/>
<c:url value="/images/fallback.svg" var="fbUrl"/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<div class="page">
  <div class="container">
    <div class="page-head flex-between">
      <div>
        <h1 class="page-title">Manage Products</h1>
        <p class="page-sub">${productData.total} product(s) across all sellers.</p>
      </div>
      <form class="search-bar" style="margin:0;min-width:300px" method="get" action="${ctx}/admin/products">
        <input class="form-input" type="search" name="q" placeholder="Search products…" value="${apQ}">
        <button class="btn btn-primary" type="submit">Search</button>
      </form>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr><th>Product</th><th>Seller</th><th>Category</th><th>Price</th><th>Stock</th><th class="actions">Actions</th></tr>
        </thead>
        <tbody>
          <c:forEach items="${productData.products}" var="pr">
            <fmt:formatNumber value="${pr.price}" type="number" minFractionDigits="2" maxFractionDigits="2" var="apPrice"/>
            <tr>
              <td>
                <div class="th-product">
                  <img class="thumb" src="${empty pr.imageUrl ? fbUrl : fn:escapeXml(pr.imageUrl)}" alt="" onerror="this.onerror=null;this.src='${ctx}/images/fallback.svg';">
                  <div><b>${fn:escapeXml(pr.name)}</b><span>#${pr.id}</span></div>
                </div>
              </td>
              <td>${fn:escapeXml(pr.sellerName)}</td>
              <td>${fn:escapeXml(pr.category)}</td>
              <td>₹${apPrice}</td>
              <td>
                <c:choose>
                  <c:when test="${pr.stockQty == 0}"><span class="stock-out">Out of stock</span></c:when>
                  <c:when test="${pr.stockQty <= 5}"><span class="stock-badge low">${pr.stockQty} left</span></c:when>
                  <c:otherwise><span class="stock-badge">${pr.stockQty}</span></c:otherwise>
                </c:choose>
              </td>
              <td>
                <div class="actions">
                  <a class="btn btn-sm btn-ghost" href="${ctx}/product?id=${pr.id}">View</a>
                  <button type="button" class="btn btn-sm btn-danger del-product" data-id="${pr.id}">Delete</button>
                </div>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>

    <c:if test="${productData.pages gt 1}">
      <div class="pagination">
        <c:forEach var="n" begin="1" end="${productData.pages}">
          <a class="page-btn ${n eq productData.page ? 'active' : ''}" href="${ctx}/admin/products?page=${n}&q=${apQ}">${n}</a>
        </c:forEach>
      </div>
    </c:if>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>
<script defer src="${ctx}/js/admin.js"></script>