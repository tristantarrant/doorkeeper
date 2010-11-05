package net.dataforte.doorkeeper.authorizer.regex;

import net.dataforte.doorkeeper.AuthenticatorUser;

import org.junit.Assert;
import org.junit.Test;

public class RegexAuthorizerTest {

	@Test
	public void testRegexAuthorizer() throws Exception {
		RegexAuthorizer regexAuth = new RegexAuthorizer();
		//regexAuth.setAclMap("{\"^/auth/.*\":[\"authenticated\"],\"^/admin/.*\":[\"administrator\"]}");
		AuthenticatorUser user = new AuthenticatorUser("tom");
		user.getGroups().add("administrator");
		Assert.assertTrue(regexAuth.authorize(user, "/admin/page.html"));
	}
}
