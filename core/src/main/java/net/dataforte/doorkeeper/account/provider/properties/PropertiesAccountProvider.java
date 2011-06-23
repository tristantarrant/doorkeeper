package net.dataforte.doorkeeper.account.provider.properties;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import net.dataforte.commons.resources.ResourceFinder;
import net.dataforte.doorkeeper.User;
import net.dataforte.doorkeeper.account.provider.AbstractAccountProvider;
import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.AuthenticatorException;
import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;
import net.dataforte.doorkeeper.authenticator.PasswordAuthenticatorToken;
import net.dataforte.doorkeeper.utils.JSONUtils;

import org.json.JSONException;

@Property(name = "name", value = "properties")
public class PropertiesAccountProvider extends AbstractAccountProvider {

	public class PropertiesUser implements User {
		String name;
		String password;

		Set<String> groups = new HashSet<String>();
		Map<String, String[]> properties = new HashMap<String, String[]>();

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Set<String> getGroups() {
			return groups;
		}

		@Override
		public boolean isUserInRole(String role) {
			return groups.contains(role);
		}

		@Override
		public String getPropertyValue(String propertyName) {
			String[] v = properties.get(propertyName);
			return v.length == 0 ? null : v[0];
		}

		@Override
		public String[] getPropertyValues(String propertyName) {
			return properties.get(propertyName);
		}

	}

	private static final String PASSWORD_FIELD = "password";
	private boolean writable;
	private String userProperties;
	private String groupProperties;
	private Map<String, PropertiesUser> users;
	private Set<String> groups;

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

	public void setWritable(boolean writable) {
		this.writable = writable;
	}

	@Override
	public boolean isWritable() {
		return writable;
	}

	@Override
	public void flushCaches() {
		
	}

	@PostConstruct
	public void init() {
		if (this.userProperties == null) {
			throw new IllegalStateException("userProperties not specified");
		}
		if (this.groupProperties == null) {
			throw new IllegalStateException("groupProperties not specified");
		}
		try {
			Properties up = new Properties();
			up.load(ResourceFinder.getResource(this.userProperties));

			Properties gp = new Properties();
			gp.load(ResourceFinder.getResource(this.groupProperties));

			parseUsers(up, gp);

		} catch (IOException e) {
			throw new IllegalStateException("Cannot initialize", e);
		}
	}

	private void parseUsers(Properties up, Properties gp) {
		users = new HashMap<String, PropertiesUser>();
		groups = new TreeSet<String>();
		
		for (String username : up.stringPropertyNames()) {
			PropertiesUser user = new PropertiesUser();
			user.name = username;

			String value = up.getProperty(username);

			try {
				Map<String, ?> map = JSONUtils.json2map(value);
				for (Entry<String, ?> entry : map.entrySet()) {
					if (PASSWORD_FIELD.equals(entry.getKey())) {
						user.password = entry.getValue().toString();
					} else {
						Object v = entry.getValue();
						if (v instanceof String) {
							user.properties.put(entry.getKey(), new String[] { (String) v });
						} else if (v instanceof List) {
							user.properties.put(entry.getKey(), ((List<String>) v).toArray(new String[0]));
						}
					}
				}
				if (user.password == null) {
					throw new IllegalStateException("User '" + username + "' in property file '" + userProperties + "' does not specify a password field");
				}
			} catch (JSONException e) {
				// Not a JSON object, use it as a simple password
				user.password = value;
			}
			// Add all the groups
			String gs = gp.getProperty(username);
			if (gs != null) {
				for (String g : gs.split(",")) {
					user.groups.add(g);
					// Keep track of all groups
					groups.add(g);
				}
			}
			// Store the user keyed by the username
			users.put(username, user);
		}
	}

	@Override
	public User authenticate(AuthenticatorToken token) throws AuthenticatorException {
		PasswordAuthenticatorToken passwordToken = (PasswordAuthenticatorToken) token;
		PropertiesUser user = users.get(passwordToken.getPrincipalName());
		if(user==null) {
			throw new AuthenticatorException("Could not authenticate " + token.getPrincipalName());
		}
		String userPassword = user.password;
		if (userPassword != null && userPassword.equals(passwordToken.getPassword())) {
			return load(token);
		} else {
			throw new AuthenticatorException("Could not authenticate " + token.getPrincipalName());
		}
	}

	@Override
	public User load(AuthenticatorToken token) {
		return users.get(token.getPrincipalName());
	}

}
