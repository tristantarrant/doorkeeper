package net.dataforte.doorkeeper.authenticator;


public class AccessDeniedException extends AuthenticatorException {

	public AccessDeniedException(String message) {
		super(message);		
	}

}
