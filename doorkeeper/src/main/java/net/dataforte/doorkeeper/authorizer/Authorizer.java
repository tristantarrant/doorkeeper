package net.dataforte.doorkeeper.authorizer;

import net.dataforte.doorkeeper.AuthenticatorUser;

public interface Authorizer {
	public static final String IS_AUTHENTICATED = "$AUTHENTICATED";
	
	boolean authorize(AuthenticatorUser user, String resourceName);
}
