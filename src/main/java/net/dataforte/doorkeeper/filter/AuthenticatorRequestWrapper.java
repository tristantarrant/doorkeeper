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
package net.dataforte.doorkeeper.filter;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import net.dataforte.doorkeeper.AuthenticatorUser;

public class AuthenticatorRequestWrapper extends HttpServletRequestWrapper {
	
	AuthenticatorUser user;
	Principal prePrincipal;
	String preRemoteUser;
	
	public AuthenticatorRequestWrapper(HttpServletRequest req, AuthenticatorUser user) {
		super(req);
		this.user = user;
		this.prePrincipal = req.getUserPrincipal();
		this.preRemoteUser = req.getRemoteUser();
	}
	
	public String getRemoteUser() {
		if (user != null) {
			return user.getName();
		} else {
			return preRemoteUser;
		}
	}

	public AuthenticatorUser getUser() {
		return user;
	}

	public Principal getUserPrincipal() {
		if(user!=null) {
			return user;
		} else {
			return prePrincipal;
		}
	}

	public String getAuthType() {
		return "CW2P";
	}

	public boolean isUserInRole(String role) {
		return user.isUserInRole(role);
	}

	public boolean isPreauthenticated() {
		return this.prePrincipal != null || this.preRemoteUser != null;
	}

}
