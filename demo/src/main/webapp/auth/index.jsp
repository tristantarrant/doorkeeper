<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<%@taglib prefix="dk" uri="/doorkeeper" %>
	<head>
		<title>Doorkeeper: Private Area</title>	
	</head>
	<body>
		<p>Welcome <%=request.getRemoteUser() %></p>
		<dk:acl groups="administrator">You are an admin</dk:acl>
		<p>Principal = <%=request.getUserPrincipal() %></p>
	</body>
</html>

