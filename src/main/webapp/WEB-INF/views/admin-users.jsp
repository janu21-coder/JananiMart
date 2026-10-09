<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Manage Users — JanuMart"/>
<c:set var="activeNav" value=""/>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>

<div class="page">
  <div class="container">
    <div class="page-head flex-between">
      <div>
        <h1 class="page-title">Manage Users</h1>
        <p class="page-sub">${fn:length(users)} user(s) match your filters.</p>
      </div>
      <form class="search-bar" style="margin:0;min-width:340px" method="get" action="${ctx}/admin/users">
        <input class="form-input" type="search" name="q" placeholder="Search by name or email…" value="${fn:escapeXml(param.q)}">
        <select class="form-select" name="role" style="width:auto">
          <option value="">All roles</option>
          <option value="BUYER" ${param.role eq 'BUYER' ? 'selected' : ''}>Buyer</option>
          <option value="SELLER" ${param.role eq 'SELLER' ? 'selected' : ''}>Seller</option>
          <option value="ADMIN" ${param.role eq 'ADMIN' ? 'selected' : ''}>Admin</option>
        </select>
        <button class="btn btn-primary" type="submit">Filter</button>
      </form>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Joined</th><th class="actions">Actions</th></tr>
        </thead>
        <tbody>
          <c:forEach items="${users}" var="usr">
            <tr>
              <td>${usr.id}</td>
              <td><b>${fn:escapeXml(usr.name)}</b></td>
              <td>${fn:escapeXml(usr.email)}</td>
              <td><span class="status-pill ${usr.role eq 'ADMIN' ? 'st-CONFIRMED' : (usr.role eq 'SELLER' ? 'st-SHIPPED' : 'st-DELIVERED')}">${fn:escapeXml(usr.role)}</span></td>
              <td>${fn:replace(usr.createdAt, 'T', ' ')}</td>
              <td>
                <c:if test="${usr.role ne 'ADMIN' and usr.id ne sessionScope.authUser.id}">
                  <button type="button" class="btn btn-sm btn-danger del-user" data-id="${usr.id}">Delete</button>
                </c:if>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>
<script defer src="${ctx}/js/admin.js"></script>