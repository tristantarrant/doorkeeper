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
package net.dataforte.doorkeeper.authenticator.rest;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.Authenticator;
import net.dataforte.doorkeeper.authenticator.AuthenticatorState;
import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;
import net.dataforte.doorkeeper.authenticator.PasswordAuthenticatorToken;

@Property(name = "name", value = "rest")
public class RestAuthenticator implements Authenticator {
	private static final String DEFAULT_SECURITY_CHECK_PATH = "/j_doorkeeper_security_check";
	private static final String DEFAULT_USERNAME_PARAMETER = "j_username";
	private static final String DEFAULT_PASSWORD_PARAMETER = "j_password";

	private String securityCheckPath = DEFAULT_SECURITY_CHECK_PATH;
	private String usernameParameter = DEFAULT_USERNAME_PARAMETER;
	private String passwordParameter = DEFAULT_PASSWORD_PARAMETER;

	private String loginSuccessUrl;
	private String loginFailUrl;

	@PostConstruct
	public void init() {
		// Do nothing
	}

	@Override
	public String getName() {
		return "REST";
	}

	@Override
	public AuthenticatorToken negotiate(HttpServletRequest request, HttpServletResponse response) {
		if (request.getRequestURI().endsWith(securityCheckPath)) {
			if("POST".equalsIgnoreCase(request.getMethod())) {
				String username = request.getParameter(usernameParameter);
				String password = request.getParameter(passwordParameter);
				if (username != null && password != null) {
					return new PasswordAuthenticatorToken(username, password);
				}
			} else {
				// TODO: decide how to handle error
			}
		}
		return new AuthenticatorToken(AuthenticatorState.NONE);
	}

	@Override
	public AuthenticatorToken restart(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//TODO: send fail JSON in response
		return new AuthenticatorToken(AuthenticatorState.NONE);
	}
	
	@Override
	public AuthenticatorToken complete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//TODO: send success JSON in response
		return new AuthenticatorToken(AuthenticatorState.NONE);
	}

	public String getSecurityCheckPath() {
		return securityCheckPath;
	}

	public void setSecurityCheckPath(String securityCheckPath) {
		this.securityCheckPath = securityCheckPath;
	}

	public String getUsernameParameter() {
		return usernameParameter;
	}

	public void setUsernameParameter(String usernameParameter) {
		this.usernameParameter = usernameParameter;
	}

	public String getPasswordParameter() {
		return passwordParameter;
	}

	public void setPasswordParameter(String passwordParameter) {
		this.passwordParameter = passwordParameter;
	}

	public String getLoginSuccessUrl() {
		return loginSuccessUrl;
	}

	public void setLoginSuccessUrl(String loginSuccessUrl) {
		this.loginSuccessUrl = loginSuccessUrl;
	}

	public String getLoginFailUrl() {
		return loginFailUrl;
	}

	public void setLoginFailUrl(String loginFailUrl) {
		this.loginFailUrl = loginFailUrl;
	}
}