package net.dataforte.doorkeeper.provider.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.dataforte.doorkeeper.User;
import net.dataforte.doorkeeper.account.provider.ldap.LdapAccountProvider;
import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;
import net.dataforte.doorkeeper.authenticator.PasswordAuthenticatorToken;

import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.core.annotations.ApplyLdifFiles;
import org.apache.directory.server.core.annotations.CreateDS;
import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
import org.apache.directory.server.core.integ.FrameworkRunner;
import org.apache.directory.shared.ldap.name.DN;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(FrameworkRunner.class)
@CreateLdapServer(transports = { @CreateTransport(protocol = "LDAP") })
@CreateDS(allowAnonAccess = true, name = "LdapAccountProviderTest") 
@ApplyLdifFiles("test.ldif")
public class LdapAccountProviderTest extends AbstractLdapTestUnit {
	
	
	@Test
	public void testProvider() throws Exception {
		
		assertNotNull(ldapServer);
		// Verify that the server is correctly initialized
		assertTrue(service.getAdminSession().exists(new DN("cn=testPerson1,ou=system")));
		
		// Perform the actual test
		LdapAccountProvider provider = new LdapAccountProvider();
		provider.setUrl("ldap://localhost:"+ldapServer.getPort());
		provider.setSearchBase("ou=system");
		provider.setUserBase("ou=system");
		provider.setGroupBase("ou=system");
		provider.setUidAttribute("cn");
		provider.init();
		AuthenticatorToken token = new AuthenticatorToken("testPerson1");
		User user = provider.load(token);
		assertNotNull(user);
		assertEquals("testPerson1", user.getName());
		assertEquals(3, user.getGroups().size());
		
		AuthenticatorToken passwordToken = new PasswordAuthenticatorToken("testPerson1", "secret");
		User user2 = provider.authenticate(passwordToken);
		assertNotNull(user2);
		assertEquals("testPerson1", user2.getName());
		assertEquals(3, user2.getGroups().size());
	}
}
