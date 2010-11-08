package net.dataforte.doorkeeper.account.provider.properties;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import net.dataforte.commons.resources.ResourceFinder;
import net.dataforte.doorkeeper.AuthenticatorException;
import net.dataforte.doorkeeper.AuthenticatorUser;
import net.dataforte.doorkeeper.account.provider.AccountProvider;
import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;
import net.dataforte.doorkeeper.authenticator.PasswordAuthenticatorToken;

@Property(name = "name", value = "properties")
public class PropertiesAccountProvider implements AccountProvider {

	String userProperties;
	String groupProperties;
	private Properties users;
	private Properties groups;

	public String getUserProperties() {
		return userProperties;
	}

	public void setUserProperties(String userProperties) {
		this.userProperties = userProperties;
	}

	public String getGroupProperties() {
		return groupProperties;
	}

	public void setGroupProperties(String groupProperties) {
		this.groupProperties = groupProperties;
	}

	@PostConstruct
	public void init() {
		if(this.userProperties==null) {
			throw new IllegalStateException("userProperties not specified");
		}
		if(this.groupProperties==null) {
			throw new IllegalStateException("groupProperties not specified");
		}
		try {
			users = new Properties();
			users.load(ResourceFinder.getResource(this.userProperties));
			groups = new Properties();
			groups.load(ResourceFinder.getResource(this.groupProperties));
		} catch (IOException e) {
			throw new IllegalStateException("Cannot initialize", e);
		}
	}

	@Override
	public AuthenticatorUser authenticate(AuthenticatorToken token) throws AuthenticatorException {
		PasswordAuthenticatorToken passwordToken = (PasswordAuthenticatorToken) token;
		String userPassword = users.getProperty(passwordToken.getPrincipalName());
		if(userPassword!=null && userPassword.equals(passwordToken.getPassword())) {
			return load(token);
		} else {
			throw new AuthenticatorException("Could not authenticate "+token.getPrincipalName());
		}
	}

	@Override
	public AuthenticatorUser load(AuthenticatorToken token) {
		AuthenticatorUser authenticatorUser = new AuthenticatorUser(token.getPrincipalName());
		String gs = groups.getProperty(token.getPrincipalName());
		if(gs!=null) {
			for(String g : gs.split(",")) {
				authenticatorUser.getGroups().add(g);
			}
		}
		return authenticatorUser;
	}

	@Override
	public List<AuthenticatorUser> getUsersInGroup(String group) {
		// TODO Auto-generated method stub
		return null;
	}
}
