package net.dataforte.doorkeeper;

import net.dataforte.doorkeeper.account.provider.jdbc.JdbcAccountProvider;
import net.dataforte.doorkeeper.account.provider.ldap.LdapAccountProvider;
import net.dataforte.doorkeeper.authenticator.basic.BasicAuthenticator;

import org.junit.Test;
import static org.junit.Assert.*;

public class DoorkeeperTest {
	
	@Test
	public void testDoorkeeper() {
		Doorkeeper doorkeeper = new Doorkeeper();
		
		assertEquals(10, doorkeeper.getAuthenticators().size());
		
		assertEquals(3, doorkeeper.getAccountProviders().size());
		
		assertEquals(2, doorkeeper.getAuthenticatorChain().size());
		
		BasicAuthenticator authenticator = (BasicAuthenticator) doorkeeper.getAuthenticatorChain().get(0);
		
		assertEquals("Doorkeeper", authenticator.getRealm());
		
		assertEquals(3, doorkeeper.getAccountProviderChain().size());
		
		LdapAccountProvider ldapProvider = (LdapAccountProvider) doorkeeper.getAccountProviderChain().get(0);
		
		assertEquals("ldap://hostname:389", ldapProvider.getUrl());
		
		JdbcAccountProvider jdbcProvider1 = (JdbcAccountProvider) doorkeeper.getAccountProviderChain().get(1);
		
		assertEquals("jdbc:derby:memory:db1", jdbcProvider1.getUrl());
		
		JdbcAccountProvider jdbcProvider2 = (JdbcAccountProvider) doorkeeper.getAccountProviderChain().get(2);
		
		assertEquals("jdbc:derby:memory:db2", jdbcProvider2.getUrl());
		
	}

}
