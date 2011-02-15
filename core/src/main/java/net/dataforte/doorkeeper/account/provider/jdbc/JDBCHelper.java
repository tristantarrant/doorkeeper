package net.dataforte.doorkeeper.account.provider.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCHelper {
	public static void close(ResultSet... resultSets) {
		for(ResultSet r : resultSets) {
			if(r!=null) {
				try {
					r.close();
				} catch (SQLException e) {
					// Ignore
				}
			}
		}
	}
	
	
	public static void close(Statement... statements) {
		for(Statement s : statements) {
			if(s!=null) {
				try {
					s.close();
				} catch (SQLException e) {
					// Ignore
				}
			}
		}
	}
	
	public static void close(Connection... connections) {
		for(Connection c : connections) {
			if(c!=null) {
				try {
					c.close();
				} catch (SQLException e) {
					// Ignore
				}
			}
		}
	}

}
