package net.dataforte.doorkeeper.authenticator.digest;

import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Test;

public class DigestTokenizerTest {

	private static final String AUTHORIZATION_HEADER = "Digest username=\"Mufasa\", " +
			"realm=\"testrealm@host.com\", " +
			"nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
			"uri=\"/dir/index.html\", " +
			"qop=auth, " +
			"nc=00000001, " +
			"cnonce=\"0a4f113b\", " +
			"response=\"6629fae49393a05397450978507c4ef1\", " +
			"opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";

	@Test
	public void testDigestTokenizer() {
		Map<String, String> tokens = HeaderTokenizer.tokenize(AUTHORIZATION_HEADER.substring(7));
		
		assertEquals(9, tokens.size());
		assertEquals("/dir/index.html", tokens.get("uri"));
		assertEquals("auth", tokens.get("qop"));
	}
}
