package net.dataforte.doorkeeper.provider.ldap;

import org.junit.Assert;
import org.junit.Test;

public class LdapAccountProviderTest {

	@Test
	public void testSplitDN() {
		String dnels[] = "cn=name,ou=here, dc=domain,dc=com".split("\\s*,\\s*");
		Assert.assertEquals(4, dnels.length);
		Assert.assertEquals("cn=name", dnels[0]);
		Assert.assertEquals("dc=domain", dnels[2]);
	}	
}
