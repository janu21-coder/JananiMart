<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Orders to Fulfil — JanuMart"/>
<c:set var="activeNav" value=""/>
<c:url value="/images/fallback.svg" var="fbUrl"/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<div class="page">
  <div class="container">
    <div class="page-head">
      <h1 class="page-title">Orders with Your Products</h1>
      <p class="page-sub">Confirm, ship and deliver orders containing your items.</p>
    </div>

    <c:choose>
      <c:when test="${empty orders}">
        <div class="cart-empty">
          <div class="empty-icon">🧾</div>
          <p class="empty-title">No orders yet</p>
          <p>When buyers purchase your products, their orders will appear here.</p>
        </div>
      </c:when>
      <c:otherwise>
        <div style="display:flex;flex-direction:column;gap:18px">
          <c:forEach items="${orders}" var="o">
            <c:set var="sellerShare" value="0"/>
            <div class="card">
              <div class="flex-between">
                <div>
                  <h3 style="margin:0">Order #${o.id}</h3>
                  <p class="muted text-sm" style="margin:4px 0 0">
                    ${fn:replace(o.createdAt, 'T', ' ')} · ${fn:escapeXml(o.buyerName)} · ${fn:escapeXml(o.paymentMethod)}
                  </p>
                </div>
                <div class="flex-between" style="gap:10px">
                  <span class="status-pill st-${fn:escapeXml(o.status)}">${fn:escapeXml(o.status)}</span>
                  <c:if test="${o.status eq 'CONFIRMED' or o.status eq 'SHIPPED' or o.status eq 'PENDING'}">
                    <select class="form-select order-status" data-order="${o.id}" style="width:auto;padding:8px 12px">
                      <option value="CONFIRMED" ${o.status eq 'CONFIRMED' or o.status eq 'PENDING' ? 'selected' : ''}>Confirmed</option>
                      <option value="SHIPPED" ${o.status eq 'SHIPPED' ? 'selected' : ''}>Shipped</option>
                      <option value="DELIVERED" ${o.status eq 'DELIVERED' ? 'selected' : ''}>Delivered</option>
                    </select>
                  </c:if>
                </div>
              </div>

              <div class="table-wrap mt-18">
                <table class="table">
                  <thead>
                    <tr><th>Item</th><th>Qty</th><th>Unit Price</th><th>Total</th></tr>
                  </thead>
                  <tbody>
                    <c:forEach items="${o.items}" var="it">
                      <fmt:formatNumber value="${it.unitPrice}" type="number" minFractionDigits="2" maxFractionDigits="2" var="soUp"/>
                      <fmt:formatNumber value="${it.lineTotal}" type="number" minFractionDigits="2" maxFractionDigits="2" var="soLt"/>
                      <c:set var="sellerShare" value="${sellerShare + it.lineTotal}"/>
                      <tr>
                        <td>
                          <div class="th-product">
                            <img class="thumb" src="${empty it.imageUrl ? fbUrl : fn:escapeXml(it.imageUrl)}" alt="" onerror="this.onerror=null;this.src='${ctx}/images/fallback.svg';">
                            <div><b>${fn:escapeXml(it.productName)}</b><span>#${it.productId}</span></div>
                          </div>
                        </td>
                        <td>${it.quantity}</td>
                        <td>₹${soUp}</td>
                        <td>₹${soLt}</td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </div>

              <div class="flex-between mt-18">
                <span class="muted text-sm">
                  Ship to: ${fn:escapeXml(o.address)}, ${fn:escapeXml(o.city)}, ${fn:escapeXml(o.state)} — ${fn:escapeXml(o.pincode)} · ${fn:escapeXml(o.phone)}
                </span>
                <fmt:formatNumber value="${sellerShare}" type="number" minFractionDigits="2" maxFractionDigits="2" var="soShare"/>
                <span class="summary-row total" style="padding:0">Your share: <b>₹${soShare}</b></span>
              </div>
            </div>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>
<script defer src="${ctx}/js/orders.js"></script>