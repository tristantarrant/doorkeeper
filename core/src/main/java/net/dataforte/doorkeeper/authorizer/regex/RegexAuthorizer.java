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
import net.dataforte.commons.slf4j.LoggerFactory;
import net.dataforte.doorkeeper.User;
import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.AccessDeniedException;
import net.dataforte.doorkeeper.authenticator.AuthenticatorException;
import net.dataforte.doorkeeper.authorizer.Authorizer;
import net.dataforte.doorkeeper.authorizer.BooleanAuthorizerOperator;

import org.slf4j.Logger;

/**
 * RegexAuthorizer is an authorizer which matches URIs against a set of regular expressions and,
 * if a match is found, the user's group membership is matched against a set of required acls.
 * The match can be evaluated using boolean operators: 
 * <ul>
 * <li>OR means that the user must belong to at least one of the specified groups</li>
 * <li>AND means that the user must belong to all of the specified groups</li>
 * <li>XOR means that the user must belong to only one of the specified groups</li>
 * </ul>
 * 
 * <p>The special group names $AUTHENTICATED, $ALLOW_ALL and $DENY_ALL can be used</p>
 * 
 * <p>The acl is specified using JSON notation as follows:</p>
 * 
 * <pre>authorizer.regex.aclMap={ "^/index.jsp":["$ALLOW_ALL"], "^/[css|js|img]/.*":["$ALLOW_ALL"], "^/auth/.*":["$AUTHENTICATED"],"^/admin/.*":["administrator"] }
 * authorizer.regex.redirectUrl=${baseUrl}/index.jsp</pre>
 * 
 * <p>If the acl is not satisfied the authorizer will throw an {@link AccessDeniedException}</p>  
 * 
 * @author Tristan Tarrant
 *
 */
@Property(name = "name", value = "regex")
public class RegexAuthorizer implements Authorizer {
	private static final Logger log = LoggerFactory.make();

	Map<Pattern, Set<String>> aclMap;
	BooleanAuthorizerOperator operator = BooleanAuthorizerOperator.OR;	

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

	@Override
	public boolean authorize(User user, String resourceName) throws AuthenticatorException {
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
					return coincidences > 0;
				case AND:
					return coincidences == acl.getValue().size();
				case XOR:
					return coincidences == 1;
				}

			}
		}
		return true;
	}

}
