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
package net.dataforte.doorkeeper.authenticator.fake;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.Authenticator;
import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Property(name="name", value="fake")
public class FakeAuthenticator implements Authenticator {
	static final Logger log = LoggerFactory.getLogger(FakeAuthenticator.class);
	String username;

	/**
	 * Just return the configured username
	 * 
	 * @param request
	 *            The servlet request
	 * @param response
	 *            The servlet response
	 * @return the username
	 */
	public AuthenticatorToken negotiate(HttpServletRequest request, HttpServletResponse response) {
		return new AuthenticatorToken(username);
	}

	public String getName() {
		return "FAKE";
	}

	public String getUsername() {
		return username;
	}

	public boolean init() {
		return true;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public AuthenticatorToken restart(HttpServletRequest request, HttpServletResponse response) {
		throw new UnsupportedOperationException();
	}

}