package net.dataforte.doorkeeper.account.provider.properties;

import static net.dataforte.doorkeeper.Doorkeeper.json2map;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.annotation.PostConstruct;

import net.dataforte.commons.resources.ResourceFinder;
import net.dataforte.doorkeeper.account.provider.AccountProvider;
import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.AuthenticatorException;
import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;
import net.dataforte.doorkeeper.authenticator.AuthenticatorUser;
import net.dataforte.doorkeeper.authenticator.PasswordAuthenticatorToken;

import org.json.JSONException;

@Property(name = "name", value = "properties")
public class PropertiesAccountProvider implements AccountProvider {

	private static final String PASSWORD_FIELD = "password";
	String userProperties;
	String groupProperties;
	private Map<String, Map<String,String>> users;
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
			Properties p = new Properties();
			p.load(ResourceFinder.getResource(this.userProperties));
			parseUsers(p);
			
			groups = new Properties();
			groups.load(ResourceFinder.getResource(this.groupProperties));
		} catch (IOException e) {
			throw new IllegalStateException("Cannot initialize", e);
		}
	}
	
	private void parseUsers(Properties props) {
		users = new HashMap<String, Map<String,String>>();
		for(String username : props.stringPropertyNames()) {
			Map<String, String> userData = new HashMap<String, String>();
			String value = props.getProperty(username);
			
			try {
				Map<String, ?> map = json2map(value);
				for(Entry<String, ?> entry : map.entrySet()) {
					userData.put(entry.getKey(), entry.getValue().toString());
				}
				if(!userData.containsKey(PASSWORD_FIELD)) {
					throw new IllegalStateException("User '"+username+"' in property file '"+userProperties+"' does not specify a password field");
				}
			} catch (JSONException e) {
				// Not a JSON object, use it as a simple password
				userData.put(PASSWORD_FIELD, value);
			}
			users.put(username, userData);
		}
	}

	@Override
	public AuthenticatorUser authenticate(AuthenticatorToken token) throws AuthenticatorException {
		PasswordAuthenticatorToken passwordToken = (PasswordAuthenticatorToken) token;
		String userPassword = users.get(passwordToken.getPrincipalName()).get(PASSWORD_FIELD);
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
