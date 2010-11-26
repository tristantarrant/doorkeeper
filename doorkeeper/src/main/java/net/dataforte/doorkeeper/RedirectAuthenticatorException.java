package net.dataforte.doorkeeper;

public class RedirectAuthenticatorException extends AuthenticatorException {
	String redirectUrl;
	
	public RedirectAuthenticatorException(String redirectUrl) {
		super("Redirecting to "+redirectUrl);
		this.redirectUrl = redirectUrl;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}


}
