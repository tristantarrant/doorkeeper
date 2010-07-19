package net.dataforte.doorkeeper.authenticator.openid;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.Authenticator;
import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;

@Property(name="name", value="openid")
public class OpenIDAuthenticator implements Authenticator {

	@PostConstruct
	public void init() {
	}

	@Override
	public String getName() {
		return "OpenID";
	}

	@Override
	public AuthenticatorToken negotiate(HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AuthenticatorToken restart(HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

}
