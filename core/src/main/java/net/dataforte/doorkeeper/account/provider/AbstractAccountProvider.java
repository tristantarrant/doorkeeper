package net.dataforte.doorkeeper.account.provider;

import java.util.Collection;
import java.util.List;

import net.dataforte.doorkeeper.User;
import net.dataforte.doorkeeper.authenticator.AuthenticatorException;
import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;

public abstract class AbstractAccountProvider implements AccountProvider {

	@Override
	public User authenticate(AuthenticatorToken token) throws AuthenticatorException {
		throw new UnsupportedOperationException("Not available");
	}

	@Override
	public User load(AuthenticatorToken token) throws AuthenticatorException {
		throw new UnsupportedOperationException("Not available");
	}

	@Override
	public List<User> getUsersInGroup(String group) {
		throw new UnsupportedOperationException("Not available");
	}

	@Override
	public Collection<String> getGroups() {
		throw new UnsupportedOperationException("Not available");
	}

	@Override
	public boolean isWritable() {
		return false;
	}

	@Override
	public void store(User user) throws AuthenticatorException {
		throw new UnsupportedOperationException("Not available");
	}

}
