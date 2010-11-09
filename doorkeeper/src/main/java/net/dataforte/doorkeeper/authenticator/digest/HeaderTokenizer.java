package net.dataforte.doorkeeper.authenticator.digest;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeaderTokenizer {
	static private Pattern pattern = Pattern.compile("(\\w+)[:=] ?\"?([^\",]+)\"?,? ?");

	public static Map<String, String> tokenize(String s) {
		Map<String,String> tokens = new HashMap<String, String>();
		Matcher matcher = pattern.matcher(s);
		while(matcher.find()) {
			tokens.put(matcher.group(1), matcher.group(2));
		}
		return tokens;
	}

}
