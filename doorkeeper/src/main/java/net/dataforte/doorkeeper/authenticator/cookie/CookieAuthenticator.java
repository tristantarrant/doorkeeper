package net.dataforte.doorkeeper.authenticator.cookie;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.Authenticator;
import net.dataforte.doorkeeper.authenticator.AuthenticatorState;
import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;

@Property(name="name", value="cookie")
public class CookieAuthenticator implements Authenticator {
	String cookieName;

	protected String getCookieName() {
		return cookieName;
	}

	protected void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}

	@PostConstruct
	public void init() {
		// Do nothing
	}

	@Override
	public String getName() {
		return "Cookie";
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

	@Override
	public AuthenticatorToken complete(HttpServletRequest req, HttpServletResponse res) throws IOException {
		return new AuthenticatorToken(AuthenticatorState.NONE);
	}
}
