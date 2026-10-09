<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:formatNumber value="${order.totalAmount}" type="number" minFractionDigits="2" maxFractionDigits="2" var="osTotal"/>
<c:set var="pageTitle" value="Order Placed — JanuMart"/>
<c:set var="activeNav" value=""/>
<c:url value="/images/fallback.svg" var="fbUrl"/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<div class="page">
  <div class="container" style="max-width:820px">
    <div class="alert alert-success">🎉 Order placed successfully! Your demo payment was accepted and your order is confirmed.</div>

    <div class="card">
      <div class="flex-between">
        <div>
          <h2 class="page-title" style="font-size:24px">Thank you, ${fn:escapeXml(order.customerName)}!</h2>
          <p class="page-sub">Order #${order.id} · placed on ${fn:replace(order.createdAt, 'T', ' ')}</p>
        </div>
        <a class="btn btn-outline btn-sm" href="${ctx}/order?id=${order.id}">Track Order</a>
      </div>

      <div class="summary-row total"><span>Amount paid (${fn:escapeXml(order.paymentMethod)})</span><span>₹${osTotal}</span></div>

      <div class="table-wrap mt-24">
        <table class="table">
          <thead>
            <tr><th>Item</th><th>Qty</th><th>Unit Price</th><th>Total</th></tr>
          </thead>
          <tbody>
            <c:forEach items="${order.items}" var="it">
              <fmt:formatNumber value="${it.unitPrice}" type="number" minFractionDigits="2" maxFractionDigits="2" var="osUp"/>
              <fmt:formatNumber value="${it.lineTotal}" type="number" minFractionDigits="2" maxFractionDigits="2" var="osLt"/>
              <tr>
                <td>
                  <div class="th-product">
                    <img class="thumb" src="${empty it.imageUrl ? fbUrl : fn:escapeXml(it.imageUrl)}" alt="" onerror="this.onerror=null;this.src='${ctx}/images/fallback.svg';">
                    <div><b>${fn:escapeXml(it.productName)}</b><span>#${it.productId}</span></div>
                  </div>
                </td>
                <td>${it.quantity}</td>
                <td>₹${osUp}</td>
                <td>₹${osLt}</td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>

      <div class="flex-between mt-24">
        <a class="btn btn-primary" href="${ctx}/shop">Continue Shopping</a>
        <a class="btn btn-ghost" href="${ctx}/orders">View All Orders</a>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>