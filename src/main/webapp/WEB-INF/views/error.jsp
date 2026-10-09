<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="errCode" value="${empty errorStatus ? requestScope['javax.servlet.error.status_code'] : errorStatus}"/>
<c:set var="errMsg" value="${empty errorMessage ? requestScope['javax.servlet.error.message'] : errorMessage}"/>
<c:set var="code" value="${empty errCode ? 500 : errCode}"/>
<c:set var="msg" value="${empty errMsg ? 'Something went wrong. Please try again.' : errMsg}"/>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>${fn:escapeXml(code)} — JanuMart</title>
<link rel="icon" href="${ctx}/images/favicon.svg" type="image/svg+xml">
<link rel="stylesheet" href="${ctx}/css/style.css">
</head>
<body>
<div class="auth-wrap">
  <div class="auth-card text-center" style="max-width:480px">
    <div class="empty-icon">${code eq 404 ? '🧭' : (code eq 403 ? '🔒' : '😅')}</div>
    <h1 class="page-title" style="font-size:64px">${code}</h1>
    <c:choose>
      <c:when test="${code eq 404}"><h2 class="empty-title">Page not found</h2></c:when>
      <c:when test="${code eq 403}"><h2 class="empty-title">Access denied</h2></c:when>
      <c:otherwise><h2 class="empty-title">Something went wrong</h2></c:otherwise>
    </c:choose>
    <p class="muted" style="margin:8px 0 22px">${fn:escapeXml(msg)}</p>
    <div class="hero-buttons" style="justify-content:center">
      <a class="btn btn-primary" href="${ctx}/">Back to Home</a>
      <a class="btn btn-ghost" href="${ctx}/shop">Browse Shop</a>
    </div>
  </div>
</div>
</body>
</html>