<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="My Orders — JanuMart"/>
<c:set var="activeNav" value=""/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<div class="page">
  <div class="container">
    <div class="page-head flex-between">
      <div>
        <h1 class="page-title">My Orders</h1>
        <p class="page-sub">Track, review and manage your orders.</p>
      </div>
      <a class="btn btn-primary btn-sm" href="${ctx}/shop">Start Shopping</a>
    </div>

    <c:choose>
      <c:when test="${empty orders}">
        <div class="cart-empty">
          <div class="empty-icon">🛒</div>
          <p class="empty-title">No orders yet</p>
          <p>When you place an order it will show up here with live tracking.</p>
          <a class="btn btn-primary mt-18" href="${ctx}/shop">Browse the Store</a>
        </div>
      </c:when>
      <c:otherwise>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr><th>Order</th><th>Date</th><th>Items</th><th>Total</th><th>Payment</th><th>Status</th><th class="actions">Actions</th></tr>
            </thead>
            <tbody>
              <c:forEach items="${orders}" var="o">
                <fmt:formatNumber value="${o.totalAmount}" type="number" minFractionDigits="2" maxFractionDigits="2" var="orTotal"/>
                <tr>
                  <td><a href="${ctx}/order?id=${o.id}">#${o.id}</a></td>
                  <td>${fn:replace(o.createdAt, 'T', ' ')}</td>
                  <td>${o.itemCount()} item(s)</td>
                  <td>₹${orTotal}</td>
                  <td>${fn:escapeXml(o.paymentMethod)}</td>
                  <td><span class="status-pill st-${fn:escapeXml(o.status)}">${fn:escapeXml(o.status)}</span></td>
                  <td>
                    <div class="actions">
                      <a class="btn btn-sm btn-outline" href="${ctx}/order?id=${o.id}">View</a>
                      <c:if test="${o.status eq 'PENDING' or o.status eq 'CONFIRMED'}">
                        <button type="button" class="btn btn-sm btn-danger order-cancel" data-order="${o.id}">Cancel</button>
                      </c:if>
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
<script defer src="${ctx}/js/orders.js"></script>