<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Login — JanuMart"/>
<c:set var="activeNav" value=""/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<div class="auth-wrap">
  <div class="auth-card">
    <h1 class="auth-title">Welcome back 👋</h1>
    <p class="auth-sub">Log in to continue shopping, selling or managing JanuMart.</p>

    <p class="field-error hidden" id="authError"></p>

    <form class="form" id="loginForm" novalidate>
      <div class="form-group">
        <label class="form-label" for="email">Email address</label>
        <input class="form-input" id="email" name="email" type="email" required autocomplete="email" placeholder="you@example.com">
      </div>
      <div class="form-group">
        <label class="form-label" for="password">Password</label>
        <input class="form-input" id="password" name="password" type="password" required autocomplete="current-password" placeholder="••••••••">
      </div>
      <button class="btn btn-primary btn-block" type="submit">Login</button>
    </form>

    <div class="note-bar">
      <b>Demo accounts</b><br>
      Buyer: demo@janumart.com / Buyer@123<br>
      Seller: stylehub@janumart.com / Seller@123<br>
      Admin: admin@janumart.com / Admin@123
    </div>

    <p class="login-switch">New to JanuMart? <a href="${ctx}/register">Create an account</a></p>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>
<script defer src="${ctx}/js/auth.js"></script>