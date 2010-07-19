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
package net.dataforte.doorkeeper.authenticator.basic;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.Authenticator;
import net.dataforte.doorkeeper.authenticator.AuthenticatorState;
import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;
import net.dataforte.doorkeeper.authenticator.PasswordAuthenticatorToken;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Property(name="name", value="basic")
public class BasicAuthenticator implements Authenticator {
	static final Logger log = LoggerFactory.getLogger(BasicAuthenticator.class);
	String realm;

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	@PostConstruct
	public void init() {
		if (realm == null) {
			realm = "BASIC";
		}
	}

	/**
	 * Basic Authentication
	 * 
	 * @param request
	 *            The servlet request
	 * @param response
	 *            The servlet response
	 * @param skipAuthentication
	 *            If true the negotiation is only done if it is initiated by the
	 *            client (MSIE post requests after successful NTLM SSP
	 *            authentication). If false and the user has not been
	 *            authenticated yet the client will be forced to send an
	 *            authentication (server sends
	 *            HttpServletResponse.SC_UNAUTHORIZED).
	 * @return True if the negotiation is complete, otherwise false
	 */
	public AuthenticatorToken negotiate(HttpServletRequest request, HttpServletResponse response) {
		String msg = request.getHeader("Authorization");
		try {
			if (msg != null && msg.startsWith("Basic ")) {
				if (log.isDebugEnabled()) {
					log.debug("Finalizing Basic Authentication");
				}
				Base64 b64 = new Base64();
				String auth = new String(b64.decode(msg.substring(6)), "US-ASCII");
				int index = auth.indexOf(':');
				String username = (index != -1) ? auth.substring(0, index) : auth;
				String password = (index != -1) ? auth.substring(index + 1) : "";
				index = username.indexOf('\\');
				if (index == -1)
					index = username.indexOf('/');

				username = (index != -1) ? username.substring(index + 1) : username;
				if (log.isDebugEnabled()) {
					log.debug("Username: " + username + " Password: " + password);
				}
				return new PasswordAuthenticatorToken(username, password);
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Initiating Basic Authentication");
				}
				response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");

				// response.setContentLength(0);
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.flushBuffer();
				return new AuthenticatorToken(AuthenticatorState.NEGOTIATING);
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return new AuthenticatorToken(AuthenticatorState.NONE);
	}

	public String getName() {
		return "Basic";
	}

	public AuthenticatorToken restart(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.flushBuffer();
		} catch (IOException e) {
			// Ignore
		}
		return new AuthenticatorToken(AuthenticatorState.NEGOTIATING);
	}

}
