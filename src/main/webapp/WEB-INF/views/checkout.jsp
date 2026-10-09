<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Checkout — JanuMart"/>
<c:set var="activeNav" value="cart"/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<div class="page">
  <div class="container">
    <div class="page-head">
      <h1 class="page-title">Checkout</h1>
      <p class="page-sub">Almost yours! Enter your delivery details and pick a demo payment method.</p>
    </div>

    <form class="cart-layout" id="checkoutForm" novalidate>
      <div style="display:flex;flex-direction:column;gap:18px">
        <div class="card">
          <div class="card-head"><h3>Delivery Details</h3></div>
          <div class="form">
            <div class="form-group">
              <label class="form-label" for="coName">Full name</label>
              <input class="form-input" id="coName" name="customerName" type="text" maxlength="100" required placeholder="e.g. Aarav Sharma">
            </div>
            <div class="form-group">
              <label class="form-label" for="coAddress">Address</label>
              <textarea class="form-input" id="coAddress" name="address" rows="2" maxlength="300" required placeholder="House / street / area"></textarea>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label" for="coCity">City</label>
                <input class="form-input" id="coCity" name="city" type="text" maxlength="80" required>
              </div>
              <div class="form-group">
                <label class="form-label" for="coState">State</label>
                <input class="form-input" id="coState" name="state" type="text" maxlength="80" required>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label" for="coPincode">PIN code</label>
                <input class="form-input" id="coPincode" name="pincode" type="text" maxlength="10" required placeholder="6 digits">
              </div>
              <div class="form-group">
                <label class="form-label" for="coPhone">Phone</label>
                <input class="form-input" id="coPhone" name="phone" type="text" maxlength="15" required placeholder="10 digits">
              </div>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-head"><h3>Payment Method</h3></div>
          <div class="form">
            <label class="form-check">
              <input type="radio" name="paymentMethod" value="UPI" checked>
              <span><b>UPI</b> <span class="muted">— Pay via any UPI app (demo)</span></span>
            </label>
            <label class="form-check">
              <input type="radio" name="paymentMethod" value="CARD">
              <span><b>Credit / Debit Card</b> <span class="muted">— Visa, Mastercard, RuPay (demo)</span></span>
            </label>
            <label class="form-check">
              <input type="radio" name="paymentMethod" value="COD">
              <span><b>Cash on Delivery</b> <span class="muted">— Pay when your order arrives</span></span>
            </label>
          </div>
          <p class="field-error hidden" id="checkoutError"></p>
        </div>
      </div>

      <aside class="summary-card" id="checkoutSummary">
        <h3 style="margin:0 0 10px;font-family:var(--font-heading)">Your Order</h3>
        <div id="checkoutItems" class="form-hint">Loading your cart…</div>
        <div class="summary-row total"><span>To Pay</span><span id="checkoutTotal">₹0.00</span></div>
        <button class="btn btn-primary btn-block mt-18" id="placeOrderBtn" type="submit">Place Order</button>
        <a class="btn btn-ghost btn-block mt-18" href="${ctx}/cart">Back to Cart</a>
      </aside>
    </form>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>
<script defer src="${ctx}/js/checkout.js"></script>