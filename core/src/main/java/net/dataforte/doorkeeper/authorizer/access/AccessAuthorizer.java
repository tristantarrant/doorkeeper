package net.dataforte.doorkeeper.authorizer.access;

import net.dataforte.doorkeeper.AuthenticatorUser;
import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authorizer.Authorizer;

@Property(name = "name", value = "access")
public class AccessAuthorizer implements Authorizer {

	@Override
	public boolean authorize(AuthenticatorUser user, String resourceName) {
		// TODO Auto-generated method stub
		return false;
	}

}
