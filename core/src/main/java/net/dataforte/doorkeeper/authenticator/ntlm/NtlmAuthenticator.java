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
package net.dataforte.doorkeeper.authenticator.ntlm;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jcifs.Config;
import jcifs.UniAddress;
import jcifs.http.NtlmSsp;
import jcifs.smb.NtStatus;
import jcifs.smb.NtlmChallenge;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbSession;
import jcifs.util.Hexdump;
import jcifs.util.LogStream;

import net.dataforte.commons.slf4j.LoggerFactory;
import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.Authenticator;
import net.dataforte.doorkeeper.authenticator.AuthenticatorState;
import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;

import org.slf4j.Logger;

/**
 * NtlmAuthenticator is an authenticator which understands Microsoft's NTLM
 * authentication tokens sent automatically to trusted/intranet sites by IE
 * (and optionally other browser such as Firefox) when the browser is running
 * within the account of a user authenticated to a Windows domain.
 * 
 * @author Tristan Tarrant
 *
 */
@Property(name="name", value="ntlm")
public class NtlmAuthenticator implements Authenticator {
	private static final String NTLM_HTTP_AUTH = "NtlmHttpAuth";

	private static final String NTLM_HTTP_CHAL = "NtlmHttpChal";

	private static final Logger log = LoggerFactory.make();

	String defaultDomain;

	String domainController;

	String loadBalance;

	String winsAddress;

	boolean balancing;

	public boolean init() {
		Config.setProperty("jcifs.smb.client.soTimeout", "300000");
		Config.setProperty("jcifs.netbios.cachePolicy", "1200");
		Config.setProperty("jcifs.smb.client.domain", defaultDomain);
		Config.setProperty("jcifs.http.domainController", domainController);
		Config.setProperty("jcifs.util.loglevel", "10");
		LogStream.setLevel(10);

		if (winsAddress != null) {
			Config.setProperty("jcifs.netbios.wins", winsAddress);
		}

		if (domainController == null) {
			domainController = defaultDomain;
			balancing = Boolean.parseBoolean(loadBalance);
		}

		return true;
	}

	/**
	 * Negotiate password hashes with MSIE clients using NTLM SSP
	 * 
	 * @param req
	 *            The servlet request
	 * @param resp
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
	public AuthenticatorToken negotiate(HttpServletRequest req, HttpServletResponse resp) {
		NtlmPasswordAuthentication ntlm = null;
		try {
			String msg = req.getHeader("Authorization");
			// The browser has sent us an Authorization header with NTLM,
			// process it
			if (msg != null && msg.startsWith("NTLM ")) {
				HttpSession session = req.getSession();
				byte[] challenge;
				UniAddress dc;

				if (balancing) {
					if (log.isDebugEnabled()) {
						log.debug("Performing balancing");
					}
					NtlmChallenge chal = (NtlmChallenge) session.getAttribute(NTLM_HTTP_CHAL);
					if (chal == null) {
						chal = SmbSession.getChallengeForDomain();
						session.setAttribute(NTLM_HTTP_CHAL, chal);
					}
					dc = chal.dc;
					challenge = chal.challenge;
				} else {
					dc = UniAddress.getByName(domainController, true);

					challenge = SmbSession.getChallenge(dc);
				}
				if (log.isDebugEnabled()) {
					log.debug("Retrieved address for domain controller " + dc + ". Authenticating...");
				}
				if ((ntlm = NtlmSsp.authenticate(req, resp, challenge)) == null) {
					// handle the first part of the negotiation
					return new AuthenticatorToken(AuthenticatorState.NEGOTIATING);
				}
				// negotiation complete, remove the challenge object
				session.removeAttribute(NTLM_HTTP_CHAL);

				try {
					SmbSession.logon(dc, ntlm);
					if (log.isDebugEnabled()) {
						log.debug("NtlmHttpFilter: " + ntlm + " successfully authenticated against " + dc);
					}
				} catch (SmbAuthException sae) {
					if (log.isDebugEnabled()) {
						log.debug("NtlmHttpFilter: " + ntlm.getName() + ": 0x" + Hexdump.toHexString(sae.getNtStatus(), 8) + ": ", sae);
					}
					if (sae.getNtStatus() == NtStatus.NT_STATUS_ACCESS_VIOLATION) {
						/*
						 * Server challenge no longer valid for externally
						 * supplied password hashes.
						 */
						if (session != null) {
							session.removeAttribute(NTLM_HTTP_AUTH);
						}
					}
					resp.setHeader("WWW-Authenticate", "NTLM");

					resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					resp.flushBuffer();
					return new AuthenticatorToken(AuthenticatorState.REJECTED);
				}
				session.setAttribute(NTLM_HTTP_AUTH, ntlm);
			} else {
				// We need to tell the browser we want authentication
				// credentials
				HttpSession session = req.getSession(false);
				// No session available or already authenticated
				if (session == null || (ntlm = (NtlmPasswordAuthentication) session.getAttribute(NTLM_HTTP_AUTH)) == null) {
					if (log.isDebugEnabled()) {
						log.debug("Initiating NTLM Authentication");
					}
					resp.setHeader("WWW-Authenticate", "NTLM");
					resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					resp.flushBuffer();
					return new AuthenticatorToken(AuthenticatorState.NEGOTIATING);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return new AuthenticatorToken(ntlm.getUsername());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.colibriframework.sso.authenticator.Authenticator#restart(javax.servlet
	 * .http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public AuthenticatorToken restart(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.setHeader("WWW-Authenticate", "NTLM");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.flushBuffer();
		} catch (IOException e) {
			// Ignore
		}
		return new AuthenticatorToken(AuthenticatorState.NEGOTIATING);
	}
	
	@Override
	public AuthenticatorToken complete(HttpServletRequest req, HttpServletResponse res) throws IOException {
		return new AuthenticatorToken(AuthenticatorState.NONE);
	}

	public String getDefaultDomain() {
		return defaultDomain;
	}

	public String getDomainController() {
		return domainController;
	}

	public void setDefaultDomain(String defaultDomain) {
		this.defaultDomain = defaultDomain;
	}

	public void setDomainController(String domainController) {
		this.domainController = domainController;
	}

	public String getLoadBalance() {
		return loadBalance;
	}

	public void setLoadBalance(String loadBalance) {
		this.loadBalance = loadBalance;
	}

	public String getWinsAddress() {
		return winsAddress;
	}

	public void setWinsAddress(String winsAddress) {
		this.winsAddress = winsAddress;
	}

	public String getName() {
		return "NTLM";
	}

}
