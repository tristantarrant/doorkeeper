/**
 * Copyright 2010 Tristan Tarrant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dataforte.doorkeeper.account.provider.ldap.helpers;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

/**
 * This class implements some methods for looking up specific ActiveDirectory
 * services (e.g. PDC, Sites, etc) using the DNS Service Locator (SRV records).
 * In particular this class uses DNS names such as
 * _ldap._tcp.dc._msdcs.<DNSDomainName>
 */
public class DNSHelper {

	Hashtable<String, String> env;

	/**
	 * Inizializza il provider
	 */
	public DNSHelper() {
		env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
	}

	/**
	 * Performs a simple address lookup on DNS (using the A record)
	 * 
	 * @param name
	 *            the name to search for
	 * @return an array of strings containing the IP addresses for the looked-up
	 *         name
	 * @throws NamingException
	 */
	public String[] lookup(String name) throws NamingException {
		return lookup(name, "A");
	}

	/**
	 * Generic lookup method
	 * 
	 * @param name
	 *            the name to search for
	 * @param type
	 *            the record type: can be A, SRV, NS, CNAME, SOA
	 * @return an array of strings containing the IP addresses for the looked-up
	 *         name
	 * @throws NamingException
	 */
	public String[] lookup(String name, String type) throws NamingException {
		DirContext ctx = new InitialDirContext(env);
		Attributes attrs = ctx.getAttributes(name, new String[] { type });
		List<String> results = new ArrayList<String>();
		NamingEnumeration<? extends Attribute> en;
		for (en = attrs.getAll(); en.hasMoreElements();) {
			Attribute attr = en.nextElement();
			NamingEnumeration<?> ae;
			for (ae = attr.getAll(); ae.hasMoreElements();) {
				String record = (String) ae.nextElement();
				String records[] = record.split(" ");
				results.add(records[3].substring(0, records[3].length() - 1));
			}
			ae.close();
		}
		en.close();
		ctx.close();
		return (String[]) results.toArray(new String[1]);
	}

	/**
	 * Finds all Active Directory domain controllers for the specified domain
	 * 
	 * @param name
	 *            the domain to lookup
	 * @return an array of strings containing the IP addresses for the looked-up
	 *         name
	 * @throws NamingException
	 */
	public String[] lookupDC(String domain) throws NamingException {
		return lookup("_ldap._tcp.dc._msdcs." + domain, "SRV");
	}

	public void setServers(String serverUrl) {
		env.put(Context.PROVIDER_URL, serverUrl);
	}
}
