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
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapContext;

import net.dataforte.doorkeeper.account.provider.ldap.LdapEntry;
import net.dataforte.doorkeeper.account.provider.ldap.LdapHelper;
import net.dataforte.doorkeeper.account.provider.ldap.LdapModifiers;

public class OpenLdapHelper implements LdapHelper {

	@Override
	public LdapModifiers getChangePasswordModifiers(String oldPassword, String newPassword) {
		LdapModifiers lm = new LdapModifiers();
		lm.modificationItems = new ModificationItem[] { new ModificationItem(LdapContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", newPassword)) };
		return lm;
	}

	@Override
	public LdapModifiers getCreateUserModifiers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LdapModifiers getUserEnableModifiers(boolean enable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUserEnabled(LdapEntry entry) {
		// TODO Auto-generated method stub
		return false;
	}

}
