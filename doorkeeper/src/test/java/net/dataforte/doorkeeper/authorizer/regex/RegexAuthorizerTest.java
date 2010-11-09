package net.dataforte.doorkeeper.authorizer.regex;

import java.util.List;
import java.util.Map;

import net.dataforte.doorkeeper.AuthenticatorUser;
import net.dataforte.doorkeeper.Doorkeeper;

import org.junit.Assert;
import org.junit.Test;

public class RegexAuthorizerTest {

	@Test
	public void testRegexAuthorizer() throws Exception {
		RegexAuthorizer regexAuth = new RegexAuthorizer();
		Map<String, List<String>> authMap = (Map<String, List<String>>) Doorkeeper.json2map("{\"^/perm1/.*\":[\"group1\"],\"^/perm2/.*\":[\"group2\"]}");
		regexAuth.setAclMap(authMap);
		AuthenticatorUser user = new AuthenticatorUser("tom");
		user.getGroups().add("group1");
		Assert.assertTrue(regexAuth.authorize(user, "/perm1/page.html"));
		Assert.assertFalse(regexAuth.authorize(user, "/perm2/page.html"));
	}
}
