<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Manage Orders — JanuMart"/>
<c:set var="activeNav" value=""/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<div class="page">
  <div class="container">
    <div class="page-head flex-between">
      <div>
        <h1 class="page-title">Manage Orders</h1>
        <p class="page-sub">${fn:length(orders)} order(s). Oversee every status.</p>
      </div>
      <form class="search-bar" style="margin:0" method="get" action="${ctx}/admin/orders">
        <select class="form-select" name="status" style="width:auto">
          <option value="">All statuses</option>
          <option value="PENDING" ${param.status eq 'PENDING' ? 'selected' : ''}>Pending</option>
          <option value="CONFIRMED" ${param.status eq 'CONFIRMED' ? 'selected' : ''}>Confirmed</option>
          <option value="SHIPPED" ${param.status eq 'SHIPPED' ? 'selected' : ''}>Shipped</option>
          <option value="DELIVERED" ${param.status eq 'DELIVERED' ? 'selected' : ''}>Delivered</option>
          <option value="CANCELLED" ${param.status eq 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
        </select>
        <button class="btn btn-primary" type="submit">Filter</button>
      </form>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr><th>Order</th><th>Date</th><th>Buyer</th><th>Payment</th><th>Items</th><th>Total</th><th>Status</th></tr>
        </thead>
        <tbody>
          <c:forEach items="${orders}" var="o">
            <fmt:formatNumber value="${o.totalAmount}" type="number" minFractionDigits="2" maxFractionDigits="2" var="aoTotal"/>
            <tr>
              <td><a href="${ctx}/order?id=${o.id}">#${o.id}</a></td>
              <td>${fn:replace(o.createdAt, 'T', ' ')}</td>
              <td>${fn:escapeXml(o.buyerName)}</td>
              <td>${fn:escapeXml(o.paymentMethod)}</td>
              <td>${o.itemCount()}</td>
              <td>₹${aoTotal}</td>
              <td>
                <div class="flex-between" style="gap:8px">
                  <span class="status-pill st-${fn:escapeXml(o.status)}">${fn:escapeXml(o.status)}</span>
                  <c:if test="${o.status ne 'DELIVERED' and o.status ne 'CANCELLED'}">
                    <select class="form-select order-status" data-order="${o.id}" style="width:auto;padding:6px 10px">
                      <option value="PENDING" ${o.status eq 'PENDING' ? 'selected' : ''}>Pending</option>
                      <option value="CONFIRMED" ${o.status eq 'CONFIRMED' ? 'selected' : ''}>Confirmed</option>
                      <option value="SHIPPED" ${o.status eq 'SHIPPED' ? 'selected' : ''}>Shipped</option>
                      <option value="DELIVERED">Delivered</option>
                      <option value="CANCELLED">Cancelled</option>
                    </select>
                  </c:if>
                </div>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>
<script defer src="${ctx}/js/orders.js"></script>