package net.dataforte.doorkeeper.provider.properties;

import net.dataforte.doorkeeper.User;
import net.dataforte.doorkeeper.account.provider.properties.PropertiesAccountProvider;
import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;
import net.dataforte.doorkeeper.authenticator.PasswordAuthenticatorToken;

import org.junit.Assert;
import org.junit.Test;

public class PropertiesAccountProviderTest {
	
	@Test
	public void testPropertiesAccountProvider() throws Exception {
		PropertiesAccountProvider provider = new PropertiesAccountProvider();
		provider.setUserProperties("users.properties");
		provider.setGroupProperties("groups.properties");
		provider.init();
		User user = provider.load(new AuthenticatorToken("admin"));
		Assert.assertEquals("admin", user.getName());
		Assert.assertTrue(user.isUserInRole("administrator"));
		Assert.assertEquals("Joe", user.getPropertyValue("firstName"));
		provider.authenticate(new PasswordAuthenticatorToken("admin", "admin"));
		
		provider.authenticate(new PasswordAuthenticatorToken("guest", "guest"));
	}
}
