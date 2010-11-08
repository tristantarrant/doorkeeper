package net.dataforte.doorkeeper.provider.properties;

import net.dataforte.doorkeeper.AuthenticatorUser;
import net.dataforte.doorkeeper.account.provider.properties.PropertiesAccountProvider;
import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;

import org.junit.Assert;
import org.junit.Test;

public class PropertiesAccountProviderTest {
	
	@Test
	public void testPropertiesAccountProvider() {
		PropertiesAccountProvider provider = new PropertiesAccountProvider();
		provider.setUserProperties("users.properties");
		provider.setGroupProperties("groups.properties");
		provider.init();
		AuthenticatorUser user = provider.load(new AuthenticatorToken("admin"));
		Assert.assertEquals("admin", user.getName());
		Assert.assertTrue(user.isUserInRole("administrator"));	
	}

}
