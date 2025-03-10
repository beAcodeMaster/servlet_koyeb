<html>
<body>
<h2>Hello World!</h2>

<!-- 서블릿 호출하는 링크 -->
<a href="<%= request.getContextPath() %>/hello-servlet">Go to HelloServlet</a>

<!-- 버튼 클릭 시 서블릿 호출 -->
<form action="<%= request.getContextPath() %>/hello-servlet" method="get">

</form>
</body>
</html>
