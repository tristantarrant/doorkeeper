package net.dataforte.doorkeeper.authorizer.regex;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import net.dataforte.commons.CollectionUtils;
import net.dataforte.doorkeeper.AuthenticatorException;
import net.dataforte.doorkeeper.AuthenticatorUser;
import net.dataforte.doorkeeper.RedirectAuthenticatorException;
import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authorizer.Authorizer;
import net.dataforte.doorkeeper.authorizer.BooleanAuthorizerOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Property(name = "name", value = "regex")
public class RegexAuthorizer implements Authorizer {
	final Logger log = LoggerFactory.getLogger(RegexAuthorizer.class);

	Map<Pattern, Set<String>> aclMap;
	BooleanAuthorizerOperator operator = BooleanAuthorizerOperator.OR;
	String redirectUrl;

	public RegexAuthorizer() {
		aclMap = new LinkedHashMap<Pattern, Set<String>>();
	}

	public Map<Pattern, Set<String>> getAclMap() {
		return aclMap;
	}

	public void setAclMap(Map<String, Collection<String>> aclMap) {

		this.aclMap.clear();
		for (Entry<String, Collection<String>> acl : aclMap.entrySet()) {
			this.aclMap.put(Pattern.compile(acl.getKey()), new HashSet<String>(acl.getValue()));
		}
	}

	public BooleanAuthorizerOperator getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = BooleanAuthorizerOperator.valueOf(operator);
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
	
	private boolean returnOrRedirect(boolean b) throws RedirectAuthenticatorException {
		if(b)
			return true;
		else if(redirectUrl!=null) {
			throw new RedirectAuthenticatorException(redirectUrl);
		} else {
			return false;
		}
	}

	@Override
	public boolean authorize(AuthenticatorUser user, String resourceName) throws AuthenticatorException {
		for (Entry<Pattern, Set<String>> acl : aclMap.entrySet()) {
			if (acl.getKey().matcher(resourceName).matches()) {
				Set<String> set = acl.getValue();
				// If the pattern allows all access, return immediately
				if (set.contains(Authorizer.ALLOW_ALL)) {
					return true;
				}
				Set<String> userSet = null;
				if (user == null) {
					userSet = Collections.emptySet();
				} else {
					userSet = user.getGroups();
				}
				int coincidences = CollectionUtils.collectionCompare(set, userSet);
				if (log.isDebugEnabled()) {
					log.debug("User=" + user + " accessing " + resourceName + " matches rule " + acl.getKey().pattern() + ", coincidences = " + coincidences);
				}
				switch (operator) {
				case OR:
					return returnOrRedirect(coincidences > 0);
				case AND:
					return returnOrRedirect(coincidences == acl.getValue().size());
				case XOR:
					return returnOrRedirect(coincidences == 1);
				}

			}
		}
		return true;
	}

}
