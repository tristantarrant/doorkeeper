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
package net.dataforte.doorkeeper.authenticator.digest;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.Authenticator;
import net.dataforte.doorkeeper.authenticator.AuthenticatorState;
import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;
import net.dataforte.doorkeeper.authenticator.PasswordAuthenticatorToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Property(name="name", value="digest")
public class DigestAuthenticator implements Authenticator {
	static final Logger log = LoggerFactory.getLogger(DigestAuthenticator.class);
	String realm;

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public boolean init() {
		if (realm == null) {
			realm = "DIGEST";
		}
		return true;
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
			if (msg != null && msg.startsWith("Digest ")) {
				if (log.isDebugEnabled()) {
					log.debug("Finalizing Digest Authentication");
				}
				Map<String, String> tokens = HeaderTokenizer.tokenize(msg.substring(7));
				String nonce = tokens.get("cnonce");
				String username = tokens.get("username");
				String nc = tokens.get("nc");
				String qop = tokens.get("qop");
				String password = ""; //FIXME
				return new PasswordAuthenticatorToken(username, password);
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Initiating Digest Authentication");
				}
				String nonce = "";
				response.setHeader("WWW-Authenticate", "Digest realm=\"" + realm + "\", nonce=\"" + nonce + "\"");

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
		return "Digest";
	}

	public AuthenticatorToken restart(HttpServletRequest request, HttpServletResponse response) {
		try {
			String nonce = "";
			response.setHeader("WWW-Authenticate", "Digest realm=\"" + realm + "\", nonce=\"" + nonce + "\"");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.flushBuffer();
		} catch (IOException e) {
			// Ignore
		}
		return new AuthenticatorToken(AuthenticatorState.NEGOTIATING);
	}

}
