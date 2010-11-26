<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Doorkeeper: Login</title>
		<link rel="stylesheet" type="text/css" media="screen" href="<%=request.getContextPath() %>/css/style.css" />
	</head>
	<body>
		
		<form action="auth/j_doorkeeper_security_check" method="post">
			<img src="img/doorkeeper.png" />
			<table>
				<tr>
					<td><label for="j_username">Username</label></td>
					<td><input name="j_username" id="j_username" type="text"/></td>
				</tr>
				<tr>
					<td><label for="j_password">Password</label></td>
					<td><input name="j_password" id="j_password" type="password"/></td>
				</tr>
				<tr>
					<td colspan="2" class="center"><input type="submit" value="Login"/></td>
				</tr>
			</table>
		</form>
		<div class="help center">
			You can login with either admin/admin or guest/guest
		</div>
	</body>
</html>

