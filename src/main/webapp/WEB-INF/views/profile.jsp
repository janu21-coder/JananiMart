<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Profile Settings — JanuMart"/>
<c:set var="activeNav" value=""/>
<c:set var="au" value="${sessionScope.authUser}"/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<div class="page">
  <div class="container" style="max-width:760px">
    <div class="page-head">
      <h1 class="page-title">Profile Settings</h1>
      <p class="page-sub">Signed in as <b>${fn:escapeXml(au.email)}</b> · ${fn:escapeXml(au.role)}</p>
    </div>

    <div class="alert alert-success hidden" id="profileOk"></div>
    <p class="field-error hidden" id="profileErr"></p>

    <div class="card mb-16">
      <div class="card-head"><h3>Personal details</h3></div>
      <form class="form" id="profileForm" novalidate>
        <div class="form-group">
          <label class="form-label" for="pfName">Full name</label>
          <input class="form-input" id="pfName" name="name" type="text" maxlength="80" required value="${fn:escapeXml(au.name)}">
        </div>
        <div class="form-group">
          <label class="form-label" for="pfEmail">Email address</label>
          <input class="form-input" id="pfEmail" type="email" value="${fn:escapeXml(au.email)}" disabled>
          <span class="form-hint">Email is used to sign in and cannot be changed here.</span>
        </div>
        <button class="btn btn-primary" type="submit">Save Changes</button>
      </form>
    </div>

    <div class="card">
      <div class="card-head"><h3>Change password</h3></div>
      <form class="form" id="passwordForm" novalidate>
        <div class="form-group">
          <label class="form-label" for="pwCurrent">Current password</label>
          <input class="form-input" id="pwCurrent" name="currentPassword" type="password" required autocomplete="current-password">
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="pwNew">New password</label>
            <input class="form-input" id="pwNew" name="newPassword" type="password" required minlength="8" autocomplete="new-password">
          </div>
          <div class="form-group">
            <label class="form-label" for="pwConfirm">Confirm new password</label>
            <input class="form-input" id="pwConfirm" name="confirmPassword" type="password" required autocomplete="new-password">
          </div>
        </div>
        <button class="btn btn-outline" type="submit">Update Password</button>
      </form>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>
<script defer src="${ctx}/js/profile.js"></script>