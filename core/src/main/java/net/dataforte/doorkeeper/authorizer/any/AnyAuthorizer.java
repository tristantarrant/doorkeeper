package net.dataforte.doorkeeper.authorizer.any;

import net.dataforte.doorkeeper.User;
import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.AuthenticatorException;
import net.dataforte.doorkeeper.authorizer.Authorizer;

@Property(name = "name", value = "any")
public class AnyAuthorizer implements Authorizer {

	@Override
	public boolean authorize(User user, String resourceName) throws AuthenticatorException {
		return true;
	}

}
