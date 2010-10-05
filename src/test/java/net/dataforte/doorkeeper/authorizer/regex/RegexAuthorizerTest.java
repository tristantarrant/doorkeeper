package net.dataforte.doorkeeper.authorizer.regex;

import org.junit.Assert;
import org.junit.Test;

import net.dataforte.doorkeeper.AuthenticatorUser;

public class RegexAuthorizerTest {

	@Test
	public void testRegexAuthorizer() {
		RegexAuthorizer regexAuth = new RegexAuthorizer();
		regexAuth.setAclMap("{\"^/auth/.*\":[\"authenticated\"],\"^/admin/.*\":[\"administrator\"]}");
		AuthenticatorUser user = new AuthenticatorUser("tom");
		user.getGroups().add("administrator");
		Assert.assertTrue(regexAuth.authorize(user, "/admin/page.html"));
	}
}
