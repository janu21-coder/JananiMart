<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Shop Accessories — JanuMart"/>
<c:set var="activeNav" value="shop"/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<div class="page">
  <div class="container">
    <div class="page-head">
      <h1 class="page-title">Shop Accessories</h1>
      <p class="page-sub">Search, filter and sort across ${applicationScope.APP_CATEGORIES.size()} categories and 8 trusted sellers.</p>
    </div>

    <div class="search-bar">
      <input class="form-input" id="qInput" type="search" placeholder="Search watches, bags, sunglasses, wallets, jewellery…" value="${fn:escapeXml(param.q)}">
      <button class="btn btn-primary" id="searchBtn">Search</button>
    </div>

    <div class="layout-shop">
      <aside class="filters">
        <h4 class="filter-title">Categories</h4>
        <div class="filter-group">
          <label class="filter-option"><input type="radio" name="category" value="" ${empty param.category ? 'checked' : ''}> All categories</label>
          <c:forEach items="${applicationScope.APP_CATEGORIES}" var="cat">
            <label class="filter-option"><input type="radio" name="category" value="${fn:escapeXml(cat.name)}" ${param.category eq cat.name ? 'checked' : ''}> ${fn:escapeXml(cat.name)}</label>
          </c:forEach>
        </div>

        <h4 class="filter-title">Subcategory</h4>
        <select class="form-select" id="fSubcategory"><option value="">All</option></select>

        <h4 class="filter-title">Brand</h4>
        <select class="form-select" id="fBrand"><option value="">All</option></select>

        <h4 class="filter-title">Colour</h4>
        <select class="form-select" id="fColor"><option value="">All</option></select>

        <h4 class="filter-title">Size</h4>
        <select class="form-select" id="fSize"><option value="">All</option></select>

        <h4 class="filter-title">Gender</h4>
        <div class="filter-group">
          <label class="filter-option"><input type="radio" name="gender" value="" ${empty param.gender ? 'checked' : ''}> All</label>
          <label class="filter-option"><input type="radio" name="gender" value="Men" ${param.gender eq 'Men' ? 'checked' : ''}> Men</label>
          <label class="filter-option"><input type="radio" name="gender" value="Women" ${param.gender eq 'Women' ? 'checked' : ''}> Women</label>
          <label class="filter-option"><input type="radio" name="gender" value="Unisex" ${param.gender eq 'Unisex' ? 'checked' : ''}> Unisex</label>
        </div>

        <h4 class="filter-title">Price (₹)</h4>
        <div class="price-range">
          <input class="form-input" id="fMin" type="number" min="0" placeholder="Min" value="${fn:escapeXml(param.minPrice)}">
          <input class="form-input" id="fMax" type="number" min="0" placeholder="Max" value="${fn:escapeXml(param.maxPrice)}">
        </div>

        <div class="filter-group mt-18">
          <label class="filter-option"><input type="checkbox" id="fInStock"> In stock only</label>
        </div>

        <h4 class="filter-title">Minimum rating</h4>
        <select class="form-select" id="fRating">
          <option value="">Any rating</option>
          <option value="4">4★ &amp; up</option>
          <option value="3">3★ &amp; up</option>
          <option value="2">2★ &amp; up</option>
        </select>

        <button class="btn btn-primary mt-18" id="applyFilters">Apply Filters</button>
        <button class="btn btn-ghost mt-18" id="clearFilters">Clear All</button>
      </aside>

      <main>
        <div class="shop-toolbar">
          <span class="result-count" id="resultCount">Loading products…</span>
          <div class="sort-bar">
            <label for="sortSelect">Sort by</label>
            <select class="form-select" id="sortSelect" style="width:auto">
              <option value="newest" ${empty param.sort or param.sort eq 'newest' ? 'selected' : ''}>Newest</option>
              <option value="price_asc" ${param.sort eq 'price_asc' ? 'selected' : ''}>Price: Low to High</option>
              <option value="price_desc" ${param.sort eq 'price_desc' ? 'selected' : ''}>Price: High to Low</option>
              <option value="rating" ${param.sort eq 'rating' ? 'selected' : ''}>Top Rated</option>
              <option value="name_asc" ${param.sort eq 'name_asc' ? 'selected' : ''}>Name A–Z</option>
            </select>
          </div>
        </div>
        <div class="product-grid" id="shopGrid"></div>
        <div class="empty hidden" id="shopEmpty">
          <div class="empty-icon">🔍</div>
          <p class="empty-title">No products found</p>
          <p>Try adjusting your search or clearing some filters.</p>
        </div>
        <div class="pagination" id="shopPagination"></div>
      </main>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>
<script defer src="${ctx}/js/shop.js"></script>