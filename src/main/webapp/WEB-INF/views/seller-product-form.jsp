<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="editing" value="${not empty product}"/>
<fmt:formatNumber value="${editing ? product.price : 0}" type="number" minFractionDigits="2" maxFractionDigits="2" var="pfPrice"/>
<c:set var="pageTitle" value="${editing ? 'Edit Product' : 'Add Product'} — JanuMart"/>
<c:set var="activeNav" value=""/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<div class="page">
  <div class="container" style="max-width:840px">
    <div class="page-head">
      <h1 class="page-title">${editing ? 'Edit Product' : 'Add New Product'}</h1>
      <p class="page-sub"><c:choose><c:when test="${editing}">Update #${product.id} on your JanuMart storefront.</c:when><c:otherwise>List a new accessory on your JanuMart storefront.</c:otherwise></c:choose></p>
    </div>

    <p class="field-error hidden" id="pfErr"></p>

    <form class="card" id="productForm" novalidate>
      <c:if test="${editing}"><input type="hidden" name="productId" id="pfProductId" value="${product.id}"></c:if>

      <div class="form" style="display:flex;flex-direction:column;gap:16px">
        <div class="form-group">
          <label class="form-label" for="pfName">Product name *</label>
          <input class="form-input" id="pfName" name="name" type="text" maxlength="200" required value="${editing ? fn:escapeXml(product.name) : ''}" placeholder="e.g. Analog Leather Strap Watch">
        </div>

        <div class="form-group">
          <label class="form-label" for="pfDesc">Description</label>
          <textarea class="form-input" id="pfDesc" name="description" rows="4" maxlength="2000" placeholder="Materials, fit, what makes it special…">${editing ? fn:escapeXml(product.description) : ''}</textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="pfPrice">Price (₹) *</label>
            <input class="form-input" id="pfPrice" name="price" type="number" step="0.01" min="0.01" required value="${editing ? pfPrice : ''}" placeholder="e.g. 499.00">
          </div>
          <div class="form-group">
            <label class="form-label" for="pfStock">Stock quantity *</label>
            <input class="form-input" id="pfStock" name="stockQty" type="number" min="0" required value="${editing ? product.stockQty : ''}" placeholder="e.g. 25">
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="pfCategory">Category *</label>
            <select class="form-select" id="pfCategory" name="category" required>
              <option value="">Select category…</option>
              <c:forEach items="${applicationScope.APP_CATEGORIES}" var="cat">
                <option value="${fn:escapeXml(cat.name)}" ${editing and product.category eq cat.name ? 'selected' : ''}>${fn:escapeXml(cat.name)}</option>
              </c:forEach>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label" for="pfSub">Subcategory</label>
            <input class="form-input" id="pfSub" name="subcategory" type="text" maxlength="60" value="${editing ? fn:escapeXml(product.subcategory) : ''}" placeholder="e.g. Analogue Watches">
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="pfGender">Gender</label>
            <select class="form-select" id="pfGender" name="gender">
              <option value="" ${editing and empty product.gender ? 'selected' : ''}>Unspecified</option>
              <option value="Men" ${editing and product.gender eq 'Men' ? 'selected' : ''}>Men</option>
              <option value="Women" ${editing and product.gender eq 'Women' ? 'selected' : ''}>Women</option>
              <option value="Unisex" ${editing and product.gender eq 'Unisex' ? 'selected' : ''}>Unisex</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label" for="pfBrand">Brand</label>
            <input class="form-input" id="pfBrand" name="brand" type="text" maxlength="80" value="${editing ? fn:escapeXml(product.brand) : ''}" placeholder="e.g. Titan">
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="pfColor">Colour</label>
            <input class="form-input" id="pfColor" name="color" type="text" maxlength="40" value="${editing ? fn:escapeXml(product.color) : ''}" placeholder="e.g. Black">
          </div>
          <div class="form-group">
            <label class="form-label" for="pfSize">Size</label>
            <input class="form-input" id="pfSize" name="size" type="text" maxlength="40" value="${editing ? fn:escapeXml(product.size) : ''}" placeholder="e.g. 42mm / Free Size">
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="pfMaterial">Material</label>
            <input class="form-input" id="pfMaterial" name="material" type="text" maxlength="80" value="${editing ? fn:escapeXml(product.material) : ''}" placeholder="e.g. Stainless Steel">
          </div>
          <div class="form-group">
            <label class="form-label" for="pfImage">Image URL</label>
            <input class="form-input" id="pfImage" name="imageUrl" type="url" maxlength="500" value="${editing ? fn:escapeXml(product.imageUrl) : ''}" placeholder="https://… (starts with http)">
          </div>
        </div>

        <div class="flex-between">
          <a class="btn btn-ghost" href="${ctx}/seller/products">Cancel</a>
          <button class="btn btn-primary" type="submit">${editing ? 'Save Changes' : 'List Product'}</button>
        </div>
      </div>
    </form>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>
<script defer src="${ctx}/js/seller.js"></script>