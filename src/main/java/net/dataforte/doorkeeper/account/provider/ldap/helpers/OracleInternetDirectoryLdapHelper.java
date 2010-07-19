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

import javax.naming.directory.BasicAttribute;

import net.dataforte.doorkeeper.account.provider.ldap.LdapEntry;
import net.dataforte.doorkeeper.account.provider.ldap.LdapHelper;
import net.dataforte.doorkeeper.account.provider.ldap.LdapModifiers;

public class OracleInternetDirectoryLdapHelper implements LdapHelper {

	@Override
	public LdapModifiers getChangePasswordModifiers(String oldPassword, String newPassword) {
		return null;
	}

	@Override
	public LdapModifiers getCreateUserModifiers() {
		return null;
	}

	@Override
	public boolean isUserEnabled(LdapEntry entry) {
		String orclisenabled = entry.attributes.get("orclisenabled");
		return !("DISABLED".equals(orclisenabled));
	}

	@Override
	public LdapModifiers getUserEnableModifiers(boolean enable) {
		LdapModifiers lm = new LdapModifiers();
		lm.attributes = new BasicAttribute[] { new BasicAttribute("orclisenabled", enable ? "" : "DISABLED") };

		return lm;
	}

}
