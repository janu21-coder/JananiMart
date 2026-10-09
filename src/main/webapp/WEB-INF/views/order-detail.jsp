<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:formatNumber value="${order.totalAmount}" type="number" minFractionDigits="2" maxFractionDigits="2" var="odTotal"/>
<c:set var="pageTitle" value="Order #${order.id} — JanuMart"/>
<c:url value="/images/fallback.svg" var="fbUrl"/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<c:choose>
  <c:when test="${order.status eq 'PENDING'}"><c:set var="curStep" value="1"/></c:when>
  <c:when test="${order.status eq 'CONFIRMED'}"><c:set var="curStep" value="2"/></c:when>
  <c:when test="${order.status eq 'SHIPPED'}"><c:set var="curStep" value="3"/></c:when>
  <c:when test="${order.status eq 'DELIVERED'}"><c:set var="curStep" value="4"/></c:when>
  <c:otherwise><c:set var="curStep" value="0"/></c:otherwise>
</c:choose>

<div class="page">
  <div class="container">
    <div class="breadcrumb"><a href="${ctx}/">Home</a> / <a href="${ctx}/orders">My Orders</a> / #${order.id}</div>

    <div class="page-head flex-between">
      <div>
        <h1 class="page-title">Order #${order.id}</h1>
        <p class="page-sub">Placed on ${fn:replace(order.createdAt, 'T', ' ')} · ${fn:escapeXml(order.paymentMethod)}</p>
      </div>
      <c:if test="${order.status eq 'PENDING' or order.status eq 'CONFIRMED'}">
        <button type="button" class="btn btn-danger order-cancel" data-order="${order.id}">Cancel Order</button>
      </c:if>
    </div>

    <div class="card">
      <div class="card-head">
        <h3>Order Status</h3>
        <span class="status-pill st-${fn:escapeXml(order.status)}">${fn:escapeXml(order.status)}</span>
      </div>

      <c:choose>
        <c:when test="${order.status eq 'CANCELLED'}">
          <div class="alert alert-error">This order was cancelled. If you think this is a mistake, contact the seller.</div>
        </c:when>
        <c:otherwise>
          <div class="steps">
            <div class="step ${curStep ge 1 ? 'done' : ''} ${curStep eq 1 ? 'active' : ''}"><div class="step-dot">1</div><span class="step-label">Placed</span></div>
            <div class="step ${curStep ge 2 ? 'done' : ''} ${curStep eq 2 ? 'active' : ''}"><div class="step-dot">2</div><span class="step-label">Confirmed</span></div>
            <div class="step ${curStep ge 3 ? 'done' : ''} ${curStep eq 3 ? 'active' : ''}"><div class="step-dot">3</div><span class="step-label">Shipped</span></div>
            <div class="step ${curStep ge 4 ? 'done' : ''} ${curStep eq 4 ? 'active' : ''}"><div class="step-dot">4</div><span class="step-label">Delivered</span></div>
          </div>
          <div class="mt-24 muted">Your order is currently <b>${fn:escapeXml(order.status)}</b>.</div>
        </c:otherwise>
      </c:choose>
    </div>

    <div class="cart-layout mt-24" style="grid-template-columns:1fr 340px">
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr><th>Item</th><th>Qty</th><th>Unit Price</th><th>Total</th></tr>
          </thead>
          <tbody>
            <c:forEach items="${order.items}" var="it">
              <fmt:formatNumber value="${it.unitPrice}" type="number" minFractionDigits="2" maxFractionDigits="2" var="odUp"/>
              <fmt:formatNumber value="${it.lineTotal}" type="number" minFractionDigits="2" maxFractionDigits="2" var="odLt"/>
              <tr>
                <td>
                  <div class="th-product">
                    <img class="thumb" src="${empty it.imageUrl ? fbUrl : fn:escapeXml(it.imageUrl)}" alt="" onerror="this.onerror=null;this.src='${ctx}/images/fallback.svg';">
                    <div><b>${fn:escapeXml(it.productName)}</b><span>#${it.productId}</span></div>
                  </div>
                </td>
                <td>${it.quantity}</td>
                <td>₹${odUp}</td>
                <td>₹${odLt}</td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>

      <aside>
        <div class="summary-card">
          <h3 style="margin:0 0 10px;font-family:var(--font-heading)">Summary</h3>
          <div class="summary-row"><span class="muted">Items</span><span>${order.itemCount()}</span></div>
          <div class="summary-row"><span class="muted">Delivery</span><span>${order.customerName}</span></div>
          <div class="summary-row total"><span>Total</span><span>₹${odTotal}</span></div>
          <div class="note-bar mt-18">
            <b>Deliver to</b><br>
            ${fn:escapeXml(order.address)}, ${fn:escapeXml(order.city)}, ${fn:escapeXml(order.state)} — ${fn:escapeXml(order.pincode)}<br>
            Phone: ${fn:escapeXml(order.phone)}
          </div>
        </div>
      </aside>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>
<script defer src="${ctx}/js/orders.js"></script>