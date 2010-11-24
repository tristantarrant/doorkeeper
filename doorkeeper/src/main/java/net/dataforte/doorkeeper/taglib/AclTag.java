package net.dataforte.doorkeeper.taglib;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import net.dataforte.commons.CollectionUtils;
import net.dataforte.doorkeeper.AuthenticatorUser;
import net.dataforte.doorkeeper.authorizer.BooleanAuthorizerOperator;

public class AclTag extends TagSupport {
	String groups;
	String redirect;
	BooleanAuthorizerOperator operator = BooleanAuthorizerOperator.OR;

	public String getGroups() {
		return groups;
	}

	public void setGroups(String groups) {
		this.groups = groups;
	}

	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	public String getOperator() {
		return operator.toString();
	}

	public void setOperator(String operator) {
		this.operator = BooleanAuthorizerOperator.valueOf(operator);
	}

	@Override
	public int doStartTag() throws JspException {
		HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();

		Set<String> set = new HashSet<String>(Arrays.asList(groups.split(",")));
		Set<String> userSet = null;
		AuthenticatorUser user = (AuthenticatorUser) req.getUserPrincipal();
		if (user == null) {
			userSet = Collections.emptySet();
		} else {
			userSet = user.getGroups();
		}
		int coincidences = CollectionUtils.collectionCompare(set, userSet);
		switch(operator) {
		case OR:
			return coincidences > 0 ? EVAL_BODY_INCLUDE : SKIP_BODY;
		case AND:
			return coincidences == set.size() ? EVAL_BODY_INCLUDE : SKIP_BODY;
		case XOR:
			return coincidences == 1 ? EVAL_BODY_INCLUDE : SKIP_BODY;
		}
		return SKIP_BODY;
	}

	@Override
	public void release() {
		this.groups = null;
		this.redirect = null;
		this.operator = BooleanAuthorizerOperator.OR;
	}

}
