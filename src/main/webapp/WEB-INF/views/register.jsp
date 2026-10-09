<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Create Account — JanuMart"/>
<c:set var="activeNav" value=""/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<div class="auth-wrap">
  <div class="auth-card">
    <h1 class="auth-title">Join JanuMart</h1>
    <p class="auth-sub">Accessories for Human. Shop, sell and explore.</p>

    <p class="field-error hidden" id="authError"></p>

    <form class="form" id="registerForm" novalidate>
      <div class="form-group">
        <label class="form-label" for="name">Full name</label>
        <input class="form-input" id="name" name="name" type="text" maxlength="80" required autocomplete="name" placeholder="e.g. Sara Iyer">
      </div>
      <div class="form-group">
        <label class="form-label" for="email">Email address</label>
        <input class="form-input" id="email" name="email" type="email" required autocomplete="email" placeholder="you@example.com">
      </div>
      <div class="form-row">
        <div class="form-group">
          <label class="form-label" for="password">Password</label>
          <input class="form-input" id="password" name="password" type="password" required minlength="8" autocomplete="new-password" placeholder="Min 8 characters">
        </div>
        <div class="form-group">
          <label class="form-label" for="confirm">Confirm password</label>
          <input class="form-input" id="confirm" name="confirm" type="password" required autocomplete="new-password" placeholder="Repeat password">
        </div>
      </div>
      <div class="form-group">
        <span class="form-label">I want to…</span>
        <label class="form-check">
          <input type="radio" name="role" value="BUYER" checked>
          <span><b>Shop on JanuMart</b> <span class="muted">— buy accessories as a buyer</span></span>
        </label>
        <label class="form-check">
          <input type="radio" name="role" value="SELLER">
          <span><b>Sell on JanuMart</b> <span class="muted">— list products as a seller</span></span>
        </label>
      </div>
      <button class="btn btn-primary btn-block" type="submit">Create Account</button>
    </form>

    <p class="login-switch">Already have an account? <a href="${ctx}/login">Login</a></p>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>
<script defer src="${ctx}/js/auth.js"></script>