<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Your Cart — JanuMart"/>
<c:set var="activeNav" value="cart"/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<div class="page">
  <div class="container">
    <div class="page-head">
      <h1 class="page-title">Your Cart</h1>
      <p class="page-sub">Review your picks before checkout.</p>
    </div>

    <div class="cart-layout">
      <div>
        <div class="cart-items" id="cartItems">
          <div class="card text-center">
            <p>Loading your cart…</p>
          </div>
        </div>
      </div>

      <aside class="summary-card" id="cartSummary">
        <h3 style="margin:0 0 10px;font-family:var(--font-heading)">Order Summary</h3>
        <div class="summary-row"><span class="muted"><span id="summaryCount">0</span> items</span><span id="summarySubtotal">₹0.00</span></div>
        <div class="summary-row"><span class="muted">Delivery</span><span class="muted" id="summaryDelivery">—</span></div>
        <div class="summary-row total"><span>Total</span><span id="summaryTotal">₹0.00</span></div>
        <button class="btn btn-primary btn-block mt-18" id="checkoutBtn" type="button">Proceed to Checkout</button>
        <a class="btn btn-ghost btn-block mt-18" href="${ctx}/shop">Continue Shopping</a>
        <div class="note-bar mt-18">Demo checkout — no real payment is taken.</div>
      </aside>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>
<script defer src="${ctx}/js/cart.js"></script>