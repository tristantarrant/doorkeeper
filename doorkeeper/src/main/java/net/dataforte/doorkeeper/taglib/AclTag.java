package net.dataforte.doorkeeper.taglib;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import net.dataforte.doorkeeper.AuthenticatorUser;

public class AclTag extends TagSupport {
	String groups;
	String redirect;

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

	@Override
	public int doEndTag() throws JspException {
		HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();

		Set<String> set = new HashSet<String>(Arrays.asList(groups.split(",")));
		Set<String> userSet = null;
		AuthenticatorUser user = (AuthenticatorUser) req.getUserPrincipal();
		if (user == null) {
			userSet = Collections.emptySet();
		} else {
			userSet = user.getGroups();
		}
		set.retainAll(userSet);
		if (set.size() > 0) {
			return EVAL_PAGE;
		} else {
			return SKIP_PAGE;
		}
	}

}
