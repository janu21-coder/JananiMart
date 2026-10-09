<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="JanuMart — Accessories for Human"/>
<c:set var="activeNav" value="home"/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<section class="hero">
  <div class="hero-inner">
    <div>
      <span class="hero-kicker">✦ A marketplace made for you</span>
      <h1 class="hero-title">Accessories<span class="hero-accent"> for Human.</span></h1>
      <p class="hero-text">Watches, bags, jewellery, travel companions and thoughtful gifts — handpicked by ${sellerCount} trusted sellers. One cart, one checkout, delivered to your door.</p>
      <div class="hero-buttons">
        <a class="btn btn-primary btn-lg" href="${ctx}/shop">Explore the Store</a>
        <a class="btn btn-outline btn-lg" href="${ctx}/register">Become a Seller</a>
      </div>
      <div class="hero-stats">
        <div class="hero-stat"><b>${productCount}+</b><span>Accessories</span></div>
        <div class="hero-stat"><b>${sellerCount}</b><span>Sellers</span></div>
        <div class="hero-stat"><b>15</b><span>Categories</span></div>
        <div class="hero-stat"><b>100%</b><span>Loved</span></div>
      </div>
    </div>
    <div class="hero-art">
      <div class="hero-blob">👜</div>
    </div>
  </div>
</section>

<div class="section">
  <div class="container">
    <div class="section-head">
      <div>
        <p class="section-eyebrow">Shop by category</p>
        <h2 class="section-title">Find your favourite</h2>
      </div>
      <a class="section-link" href="${ctx}/shop">Browse all →</a>
    </div>
    <div class="category-grid">
      <c:forEach items="${categories}" var="cat">
        <a class="category-card" href="${ctx}/shop?category=${fn:escapeXml(cat.name)}">
          <span class="category-icon">${cat.svg}</span>
          <span class="category-name">${fn:escapeXml(cat.name)}</span>
        </a>
      </c:forEach>
    </div>
  </div>
</div>

<div class="section section-alt">
  <div class="container">
    <div class="section-head">
      <div>
        <p class="section-eyebrow">Trending now</p>
        <h2 class="section-title">What everyone is adding to cart</h2>
      </div>
      <a class="section-link" href="${ctx}/shop?sort=rating">Top rated →</a>
    </div>
    <div class="product-grid">
      <c:forEach items="${trending}" var="p">
        <%@ include file="/WEB-INF/views/fragments/product-card.jspf" %>
      </c:forEach>
    </div>
  </div>
</div>

<div class="section">
  <div class="container">
    <div class="section-head">
      <div>
        <p class="section-eyebrow">Why JanuMart</p>
        <h2 class="section-title">Shopping should feel this good</h2>
      </div>
    </div>
    <div class="features-grid">
      <div class="feature">
        <div class="feature-icon">🛍️</div>
        <h4>Multi-seller marketplace</h4>
        <p>8 curated sellers — from StyleHub to Gift Corner — each with their own collection and personality.</p>
      </div>
      <div class="feature">
        <div class="feature-icon">🚚</div>
        <h4>Fast local shipping</h4>
        <p>Orders are confirmed instantly and shipped in real-world demo time. Track every step of the way.</p>
      </div>
      <div class="feature">
        <div class="feature-icon">🔒</div>
        <h4>Secure checkout</h4>
        <p>UPI, card or cash on delivery — with a transactional checkout that guards every order and unit of stock.</p>
      </div>
      <div class="feature">
        <div class="feature-icon">💬</div>
        <h4>Honest reviews</h4>
        <p>Only verified buyers of delivered orders can review. What you see is what real humans thought.</p>
      </div>
    </div>
  </div>
</div>

<c:forEach items="${categoryRows}" var="row">
  <div class="section ${row.key eq 'Gifts' ? 'section-alt' : ''}">
    <div class="container">
      <div class="section-head">
        <div>
          <p class="section-eyebrow">Curated for you</p>
          <h2 class="section-title">${fn:escapeXml(row.key)}</h2>
        </div>
        <a class="section-link" href="${ctx}/shop?category=${fn:escapeXml(row.key)}">See all →</a>
      </div>
      <div class="product-grid">
        <c:forEach items="${row.value}" var="p">
          <%@ include file="/WEB-INF/views/fragments/product-card.jspf" %>
        </c:forEach>
      </div>
    </div>
  </div>
</c:forEach>

<div class="section section-alt">
  <div class="container">
    <div class="section-head">
      <div>
        <p class="section-eyebrow">Fresh arrivals</p>
        <h2 class="section-title">New in store</h2>
      </div>
      <a class="section-link" href="${ctx}/shop?sort=newest">Newest →</a>
    </div>
    <div class="product-grid">
      <c:forEach items="${newArrivals}" var="p">
        <%@ include file="/WEB-INF/views/fragments/product-card.jspf" %>
      </c:forEach>
    </div>
  </div>
</div>

<div class="section">
  <div class="container">
    <div class="section-head">
      <div>
        <p class="section-eyebrow">Best sellers</p>
        <h2 class="section-title">Loved by buyers</h2>
      </div>
      <a class="section-link" href="${ctx}/shop?sort=rating">More →</a>
    </div>
    <div class="product-grid">
      <c:forEach items="${bestSellers}" var="p">
        <%@ include file="/WEB-INF/views/fragments/product-card.jspf" %>
      </c:forEach>
    </div>
  </div>
</div>

<div class="section section-alt">
  <div class="container">
    <div class="section-head">
      <div>
        <p class="section-eyebrow">Our sellers</p>
        <h2 class="section-title">The humans of JanuMart</h2>
      </div>
    </div>
    <div class="sellers-grid">
      <c:forEach items="${sellers}" var="s">
        <a class="seller-card" href="${ctx}/shop">
          <span class="seller-avatar">${fn:substring(s.name, 0, 1)}</span>
          <div>
            <h5>${fn:escapeXml(s.name)}</h5>
            <span>${s.count} products</span>
          </div>
        </a>
      </c:forEach>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>