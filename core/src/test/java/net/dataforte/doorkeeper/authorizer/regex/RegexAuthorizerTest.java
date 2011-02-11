package net.dataforte.doorkeeper.authorizer.regex;

import java.util.Collection;
import java.util.Map;

import net.dataforte.doorkeeper.Doorkeeper;
import net.dataforte.doorkeeper.User;
import net.dataforte.doorkeeper.authenticator.AuthenticatorUser;
import net.dataforte.doorkeeper.authorizer.BooleanAuthorizerOperator;

import org.junit.Assert;
import org.junit.Test;

public class RegexAuthorizerTest {

	@Test
	public void testRegexAuthorizerOr() throws Exception {
		RegexAuthorizer regexAuth = new RegexAuthorizer();
		Map<String, Collection<String>> authMap = (Map<String, Collection<String>>) Doorkeeper.json2map("{\"^/perm1/.*\":[\"group1\"],\"^/perm2/.*\":[\"group2\"]}");
		regexAuth.setAclMap(authMap);
		regexAuth.setOperator(BooleanAuthorizerOperator.OR.toString());
		User user = new AuthenticatorUser("tom");
		user.getGroups().add("group1");
		Assert.assertTrue(regexAuth.authorize(user, "/perm1/page.html"));
		Assert.assertFalse(regexAuth.authorize(user, "/perm2/page.html"));
	}
	
	@Test
	public void testRegexAuthorizerAnd() throws Exception {
		RegexAuthorizer regexAuth = new RegexAuthorizer();
		Map<String, Collection<String>> authMap = (Map<String, Collection<String>>) Doorkeeper.json2map("{\"^/perm1/.*\":[\"group1\",\"group2\"],\"^/perm2/.*\":[\"group1\"]}");
		regexAuth.setAclMap(authMap);
		regexAuth.setOperator(BooleanAuthorizerOperator.AND.toString());
		User user = new AuthenticatorUser("tom");
		user.getGroups().add("group1");
		Assert.assertFalse(regexAuth.authorize(user, "/perm1/page.html"));
		Assert.assertTrue(regexAuth.authorize(user, "/perm2/page.html"));
		user.getGroups().add("group2");
		Assert.assertTrue(regexAuth.authorize(user, "/perm1/page.html"));
	}
}
