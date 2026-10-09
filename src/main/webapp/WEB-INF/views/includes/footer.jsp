<footer class="footer">
  <div class="footer-grid">
    <div>
      <span class="brand">JanuMart</span>
      <span class="brand-tag">Accessories for Human.</span>
      <p class="about">A friendly multi-seller marketplace for the accessories that make everyday life better — watches, bags, jewellery, travel companions and little gifts that say a lot.</p>
    </div>
    <div>
      <h4>Top Categories</h4>
      <c:set var="topCats" value="${['Watches','Bags','Jewellery','Sunglasses','Wallets','Travel Accessories']}"/>
      <c:forEach items="${topCats}" var="tcat">
        <a href="${ctx}/shop?category=${fn:escapeXml(tcat)}">${fn:escapeXml(tcat)}</a>
      </c:forEach>
    </div>
    <div>
      <h4>My Account</h4>
      <c:choose>
        <c:when test="${not empty sessionScope.authUser}">
          <a href="${ctx}/dashboard">Dashboard</a>
          <c:if test="${sessionScope.authUser.role eq 'BUYER'}">
            <a href="${ctx}/orders">My Orders</a>
            <a href="${ctx}/cart">My Cart</a>
          </c:if>
          <a href="${ctx}/profile">Profile Settings</a>
        </c:when>
        <c:otherwise>
          <a href="${ctx}/login">Login</a>
          <a href="${ctx}/register">Create Account</a>
        </c:otherwise>
      </c:choose>
    </div>
    <div>
      <h4>JanuMart</h4>
      <a href="${ctx}/shop">Browse Store</a>
      <a href="${ctx}/register">Become a Seller</a>
      <a href="${ctx}/shop">New Arrivals</a>
      <a href="${ctx}/shop">Best Sellers</a>
    </div>
  </div>
  <div class="footer-bottom">
    <span>© 2026 JanuMart · Capstone project built with Java Servlets, JSP &amp; H2.</span>
    <span>Accessories for Human.</span>
  </div>
</footer>
<script src="${ctx}/js/api.js"></script>
<script src="${ctx}/js/header.js"></script>
<script src="${ctx}/js/main.js"></script>
<c:if test="${showChatbot}">
<script src="${ctx}/js/chatbot.js"></script>
</c:if>
</body>
</html>