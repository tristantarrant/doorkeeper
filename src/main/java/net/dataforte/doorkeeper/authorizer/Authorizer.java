package net.dataforte.doorkeeper.authorizer;

import net.dataforte.doorkeeper.AuthenticatorUser;

public interface Authorizer {
	boolean authorize(AuthenticatorUser user, String resourceName);
}
