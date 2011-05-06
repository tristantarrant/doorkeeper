package net.dataforte.doorkeeper.account.provider.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.naming.NamingException;
import javax.sql.DataSource;

import net.dataforte.commons.JNDIUtils;
import net.dataforte.commons.slf4j.LoggerFactory;
import net.dataforte.doorkeeper.User;
import net.dataforte.doorkeeper.account.provider.AbstractAccountProvider;
import net.dataforte.doorkeeper.annotations.Property;
import net.dataforte.doorkeeper.authenticator.AuthenticatorToken;
import net.dataforte.doorkeeper.authenticator.PasswordAuthenticatorToken;

import org.slf4j.Logger;

@Property(name = "name", value = "jdbc")
public class JdbcAccountProvider extends AbstractAccountProvider {
	static final Logger log = LoggerFactory.make();
	private String url;
	private String username;
	private String password;
	private String driverClassName;
	private String jndi;
	private DataSource dataSource;
	private String authenticateSql;
	private String authorizeSql;
	private boolean writable;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getJndi() {
		return jndi;
	}

	public void setJndi(String jndi) {
		this.jndi = jndi;
	}

	public boolean isWritable() {
		return writable;
	}

	public void setWritable(boolean writable) {
		this.writable = writable;

	}

	public String getAuthenticateSql() {
		return authenticateSql;
	}

	public void setAuthenticateSql(String authenticateSql) {
		this.authenticateSql = authenticateSql;
	}

	public String getAuthorizeSql() {
		return authorizeSql;
	}

	public void setAuthorizeSql(String authorizeSql) {
		this.authorizeSql = authorizeSql;
	}

	@Override
	public void flushCaches() {
		// TODO Auto-generated method stub

	}

	@PostConstruct
	public void init() {
		try {
			if (jndi != null) {
				dataSource = JNDIUtils.lookup(jndi, DataSource.class);
			}
		} catch (NamingException e) {
			throw new IllegalStateException("Could not retrieve DataSource from JNDI " + jndi, e);
		}
	}

	@Override
	public User authenticate(AuthenticatorToken token) {
		PasswordAuthenticatorToken passwordToken = (PasswordAuthenticatorToken) token;
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			c = dataSource.getConnection();
			ps = c.prepareStatement(authenticateSql);
			ps.setString(1, passwordToken.getPrincipalName());
			rs = ps.executeQuery();
		} catch (SQLException e) {
			log.error("Could not authenticate a user", e);
		} finally {
			JDBCHelper.close(c);
		}
		// TODO Auto-generated method stub
		return null;
	}

}
