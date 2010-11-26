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
package net.dataforte.doorkeeper.authenticator.container;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.Authenticator;
import net.dataforte.doorkeeper.authenticator.AuthenticatorState;
import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;

/**
 * ContainerAuthenticator is an authenticator which trusts the principal
 * passed on by the container (e.g. JAAS Security, Tomcat security Valves, etc)
 * 
 * @author Tristan Tarrant
 *
 */
@Property(name="name", value="container")
public class ContainerAuthenticator implements Authenticator {

	@PostConstruct
	public void init() {
		// Do nothing
	}

	@Override
	public String getName() {
		return "Container";
	}

	@Override
	public AuthenticatorToken negotiate(HttpServletRequest request, HttpServletResponse response) {
		if(request.getRemoteUser()!=null) {
			return new AuthenticatorToken(request.getRemoteUser());
		}
		return new AuthenticatorToken(AuthenticatorState.NONE);
	}

	@Override
	public AuthenticatorToken restart(HttpServletRequest request, HttpServletResponse response) {
		return new AuthenticatorToken(AuthenticatorState.NONE);
	}

	@Override
	public AuthenticatorToken complete(HttpServletRequest req, HttpServletResponse res) throws IOException {
		return new AuthenticatorToken(AuthenticatorState.NONE);
	}
}
