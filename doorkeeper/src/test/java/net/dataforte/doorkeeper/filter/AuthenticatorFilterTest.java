package net.dataforte.doorkeeper.filter;

import static org.mockito.Mockito.mock;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.dataforte.doorkeeper.Doorkeeper;

import org.junit.Test;

public class AuthenticatorFilterTest {
	
	@Test
	public void testAuthenticatorFilter() throws Exception {
		Doorkeeper doorkeeper = new Doorkeeper("doorkeeper.properties");
		AuthenticatorFilter filter = new AuthenticatorFilter();
		
		filter.setDoorkeeper(doorkeeper);
		
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);
		
		
		filter.doFilter(request, response, chain);
	}

}
