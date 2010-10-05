package net.dataforte.doorkeeper.account.provider.properties;

import java.util.List;

import javax.annotation.PostConstruct;

import net.dataforte.doorkeeper.AuthenticatorUser;
import net.dataforte.doorkeeper.account.provider.AccountProvider;
import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;

@Property(name="name", value="properties")
public class PropertiesAccountProvider implements AccountProvider {

	@PostConstruct
	public void init() {

	}

	@Override
	public AuthenticatorUser authenticate(AuthenticatorToken token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AuthenticatorUser load(AuthenticatorToken token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AuthenticatorUser> getUsersInGroup(String group) {
		// TODO Auto-generated method stub
		return null;
	}
}
