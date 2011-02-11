package net.dataforte.doorkeeper.authorizer;

import net.dataforte.doorkeeper.User;
import net.dataforte.doorkeeper.authenticator.AuthenticatorException;

public interface Authorizer {
	public static final String ALLOW_ALL = "$ALLOW_ALL";
	public static final String DENY_ALL = "$DENY_ALL";
	public static final String IS_AUTHENTICATED = "$AUTHENTICATED";
	
	boolean authorize(User user, String resourceName) throws AuthenticatorException;
}