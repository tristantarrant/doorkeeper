package net.dataforte.doorkeeper.authorizer.regex;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import net.dataforte.doorkeeper.AuthenticatorUser;
import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authorizer.Authorizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Property(name = "name", value = "regex")
public class RegexAuthorizer implements Authorizer {
	final Logger log = LoggerFactory.getLogger(RegexAuthorizer.class);

	Map<Pattern, List<String>> aclMap;

	public RegexAuthorizer() {
		aclMap = new LinkedHashMap<Pattern, List<String>>();
	}

	public Map<Pattern, List<String>> getAclMap() {
		return aclMap;
	}

	public void setAclMap(Map<String, List<String>> aclMap) {
		
		this.aclMap.clear();
		for(Entry<String, List<String>> acl : aclMap.entrySet()) {
			this.aclMap.put(Pattern.compile(acl.getKey()), acl.getValue());
		}
	}

	@Override
	public boolean authorize(AuthenticatorUser user, String resourceName) {
		for (Entry<Pattern, List<String>> acl : aclMap.entrySet()) {
			if (acl.getKey().matcher(resourceName).matches()) {
				if(log.isDebugEnabled()) {
					log.debug("Found pattern for {}", resourceName);
				}
				Set<String> set = new HashSet<String>(acl.getValue());				
				Set<String> userSet = null;
				if(user==null) {
					userSet = Collections.emptySet();
				} else {
					userSet = user.getGroups();					
				}
				set.retainAll(userSet);
				return set.size() > 0;
			}
		}
		return true;
	}

}
