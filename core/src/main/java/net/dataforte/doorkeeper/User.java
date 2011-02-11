package net.dataforte.doorkeeper;

import java.security.Principal;
import java.util.Set;

public interface User extends Principal {

	public abstract Set<String> getGroups();

	public abstract boolean isUserInRole(String role);

	public abstract String getPropertyValue(String propertyName);

}