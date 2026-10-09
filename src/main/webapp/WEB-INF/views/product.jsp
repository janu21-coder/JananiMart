<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:formatNumber value="${product.price}" type="number" minFractionDigits="2" maxFractionDigits="2" var="pdPrice"/>
<c:set var="pageTitle" value="${product.name} — JanuMart"/>
<c:set var="activeNav" value="shop"/>
<c:url value="/images/fallback.svg" var="fbUrl"/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<div class="page">
  <div class="container">
    <div class="breadcrumb">
      <a href="${ctx}/">Home</a> / <a href="${ctx}/shop">Shop</a>
      / <a href="${ctx}/shop?category=${fn:escapeXml(product.category)}">${fn:escapeXml(product.category)}</a>
      / ${fn:escapeXml(product.name)}
    </div>

    <div class="detail-layout">
      <div class="detail-gallery">
        <img src="${empty product.imageUrl ? fbUrl : fn:escapeXml(product.imageUrl)}"
             alt="${fn:escapeXml(product.name)}"
             onerror="this.onerror=null;this.src='${ctx}/images/fallback.svg';">
      </div>

      <div>
        <p class="detail-brand">${fn:escapeXml(empty product.brand ? 'JanuMart' : product.brand)} · Sold by ${fn:escapeXml(product.sellerName)}</p>
        <h1 class="detail-title">${fn:escapeXml(product.name)}</h1>

        <div class="stars-row">
          <c:choose>
            <c:when test="${not empty product.avgRating}">
              <fmt:formatNumber value="${product.avgRating}" maxFractionDigits="1" var="pdRating"/>
              <span class="stars">★★★★★<span class="stars-fill" style="width:${product.avgRating * 20}%">★★★★★</span></span>
              <span class="rating-text">${pdRating}</span><span>(${product.ratingCount} reviews)</span>
            </c:when>
            <c:otherwise>
              <span class="muted">New arrival · no reviews yet</span>
            </c:otherwise>
          </c:choose>
        </div>

        <div class="detail-price">₹${pdPrice}</div>

        <p class="detail-desc">${fn:escapeXml(empty product.description ? 'Handpicked by our sellers — quality you can feel. Stock may vary between sellers.' : product.description)}</p>

        <div class="detail-meta">
          <div><b>Category</b><span>${fn:escapeXml(product.category)}</span></div>
          <div><b>Subcategory</b><span>${fn:escapeXml(empty product.subcategory ? '—' : product.subcategory)}</span></div>
          <div><b>Brand</b><span>${fn:escapeXml(empty product.brand ? '—' : product.brand)}</span></div>
          <div><b>Gender</b><span>${fn:escapeXml(empty product.gender ? '—' : product.gender)}</span></div>
          <div><b>Colour</b><span>${fn:escapeXml(empty product.color ? '—' : product.color)}</span></div>
          <div><b>Size</b><span>${fn:escapeXml(empty product.size ? '—' : product.size)}</span></div>
          <div><b>Material</b><span>${fn:escapeXml(empty product.material ? '—' : product.material)}</span></div>
          <div><b>Seller</b><span>${fn:escapeXml(product.sellerName)}</span></div>
        </div>

        <p class="stock-line">
          <c:choose>
            <c:when test="${product.inStock}">
              <span class="stock-badge">✓ In stock · ${product.stockQty} units</span>
            </c:when>
            <c:otherwise>
              <span class="stock-out">Currently out of stock</span>
            </c:otherwise>
          </c:choose>
        </p>

        <div class="buy-row">
          <div class="qty">
            <button type="button" class="qty-btn" id="qtyMinus" aria-label="Decrease quantity">−</button>
            <input class="qty-input" id="qtyInput" type="number" min="1" max="${product.stockQty}" value="1" readonly>
            <button type="button" class="qty-btn" id="qtyPlus" aria-label="Increase quantity">+</button>
          </div>
          <c:choose>
            <c:when test="${product.inStock}">
              <button type="button" class="btn btn-primary add-cart" data-id="${product.id}" data-qty-target="qtyInput">Add to Cart</button>
            </c:when>
            <c:otherwise>
              <button type="button" class="btn btn-ghost" disabled>Out of Stock</button>
            </c:otherwise>
          </c:choose>
        </div>

        <div class="note-bar mt-24">
          🛒 Demo: Add to cart, then check out with mock UPI / card / cash-on-delivery. No real payment is charged.
        </div>
      </div>
    </div>

    <div class="reviews">
      <div class="section-head">
        <div>
          <p class="section-eyebrow">Verified reviews</p>
          <h2 class="section-title">What buyers think</h2>
        </div>
      </div>

      <form class="card hidden" id="reviewForm" style="max-width:640px">
        <div class="card-head"><h3>Write a review</h3></div>
        <div class="form-group">
          <span class="form-label">Your rating</span>
          <div class="rating-input" id="ratingInput">
            <span data-val="1">★</span><span data-val="2">★</span><span data-val="3">★</span><span data-val="4">★</span><span data-val="5">★</span>
          </div>
          <input type="hidden" id="ratingValue" name="rating" value="0">
        </div>
        <div class="form-group">
          <label class="form-label" for="reviewComment">Your review</label>
          <textarea class="form-input" id="reviewComment" name="comment" rows="3" maxlength="600" placeholder="What did you like (or not) about this accessory?"></textarea>
        </div>
        <button type="submit" class="btn btn-primary">Post Review</button>
        <p class="field-error hidden" id="reviewError"></p>
      </form>

      <div id="reviewsList">
        <div class="muted">Loading reviews…</div>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>
<script defer src="${ctx}/js/product.js"></script>