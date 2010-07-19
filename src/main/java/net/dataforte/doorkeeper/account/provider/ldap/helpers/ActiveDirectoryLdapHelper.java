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

import java.io.UnsupportedEncodingException;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import net.dataforte.doorkeeper.account.provider.ldap.LdapEntry;
import net.dataforte.doorkeeper.account.provider.ldap.LdapHelper;
import net.dataforte.doorkeeper.account.provider.ldap.LdapModifiers;

public class ActiveDirectoryLdapHelper implements LdapHelper {
	public static final int UF_ACCOUNTDISABLE = 0x0002;
	public static final int UF_PASSWD_NOTREQD = 0x0020;
	public static final int UF_PASSWD_CANT_CHANGE = 0x0040;
	public static final int UF_NORMAL_ACCOUNT = 0x0200;
	public static final int UF_DONT_EXPIRE_PASSWD = 0x10000;
	public static final int UF_PASSWORD_EXPIRED = 0x800000;
	
	public static byte[] encodePassword(String password) throws UnsupportedEncodingException {
		String quotedPassword = "\"" + password + "\"";
		return quotedPassword.getBytes("UTF-16LE"); 
	}

	@Override
	public LdapModifiers getChangePasswordModifiers(String oldPassword, String newPassword) {
		try {
			LdapModifiers lm = new LdapModifiers();
			lm.modificationItems = new ModificationItem[] {
					new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("unicodePwd", encodePassword(oldPassword))),
					new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("unicodePwd", encodePassword(newPassword)))
			};
			return lm;
		} catch (UnsupportedEncodingException e) {
			// Will never happen
			return null;
		}
	}

	@Override
	public LdapModifiers getCreateUserModifiers() {
		LdapModifiers lm = new LdapModifiers();
		lm.attributes = new BasicAttribute[] { new BasicAttribute("userAccountControl", Integer.toString(UF_NORMAL_ACCOUNT + UF_PASSWD_NOTREQD
				+ UF_PASSWORD_EXPIRED + UF_ACCOUNTDISABLE)) };
		return lm;
	}

	@Override
	public boolean isUserEnabled(LdapEntry entry) {
		return true;
	}

	@Override
	public LdapModifiers getUserEnableModifiers(boolean enable) {
		// TODO Auto-generated method stub
		return null;
	}

}
