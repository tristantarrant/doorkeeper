package net.dataforte.doorkeeper.authorizer.regex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import net.dataforte.doorkeeper.AuthenticatorUser;
import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authorizer.Authorizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Property(name = "name", value = "regex")
public class RegexAuthorizer implements Authorizer {
	final Logger log = LoggerFactory.getLogger(RegexAuthorizer.class);

	Map<Pattern, Set<String>> aclMap;

	public RegexAuthorizer() {
		aclMap = new LinkedHashMap<Pattern, Set<String>>();
	}

	public Map<Pattern, Set<String>> getAclMap() {
		return aclMap;
	}

	public void setAclMap(String aclMap) {
		try {
			this.aclMap.clear();
			JSONObject json = new JSONObject(aclMap);
			for(Iterator<String> it = json.keys(); it.hasNext(); ) {
				String key = it.next();
				Set<String> set = new HashSet<String>();
				this.aclMap.put(Pattern.compile(key), set);
				JSONArray jsonArray = json.getJSONArray(key);
				for(int i=0; i<jsonArray.length(); i++) {
					set.add(jsonArray.getString(i));
				}
			}
			
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean authorize(AuthenticatorUser user, String resourceName) {
		for (Entry<Pattern, Set<String>> acl : aclMap.entrySet()) {
			if (acl.getKey().matcher(resourceName).matches()) {
				if(log.isDebugEnabled()) {
					log.debug("Found pattern for {0}", resourceName);
				}
				HashSet<String> set = new HashSet<String>(acl.getValue());
				set.retainAll(user.getGroups());
				return set.size() > 0;
			}
		}
		return false;
	}

}
