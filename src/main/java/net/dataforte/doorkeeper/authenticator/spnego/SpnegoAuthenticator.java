package net.dataforte.doorkeeper.authenticator.spnego;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.Authenticator;
import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;

@Property(name="name", value="spnego")
public class SpnegoAuthenticator implements Authenticator {

	@PostConstruct
	public void init() {
	}

	@Override
	public String getName() {
		return "SPNEGO";
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
