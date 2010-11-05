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
package net.dataforte.doorkeeper;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Tristan Tarrant
 */
public class AuthenticatorUser implements Principal, Serializable {
	String name;
	Set<String> groups;
	Map<String, String[]> properties;
	
	public AuthenticatorUser(String name) {
		this.name = name;
		this.groups = new HashSet<String>();
		this.properties = new HashMap<String, String[]>();
	}

	@Override
	public String getName() {
		return name;
	}

	public Set<String> getGroups() {
		return groups;
	}

	@Override
	public String toString() {
		return "AuthenticatorUser [name=" + name + "]";
	}

	public boolean isUserInRole(String role) {
		return groups.contains(role);
	}
	
	public String getPropertyValue(String propertyName) {
		String[] v = properties.get(propertyName);
		return v.length==0?null:v[0];
	}
	
	
}
