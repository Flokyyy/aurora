package de.flokyy.aurora.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL {
	
	public static String HOST = "localhost";
	public static String DATABASE = ""; //your database table
	public static String USER = ""; //your database user
	public static String PASSWORD = "";	//your database password

	public static Connection con;

	public MySQL(String host, String database, String user, String password) {
		HOST = host;
		DATABASE = database;
		USER = user;
		PASSWORD = password;
		connect();
	}

	public void connect() {
		try {
			con = DriverManager.getConnection("jdbc:mysql://" + HOST + ":3306/" + DATABASE + "?autoReconnect=true",
					USER, PASSWORD);

			System.out.println("MySQL connection was successful.");
		} catch (SQLException e) {	
			System.out.println("Error when trying to connect to MySQL " + e.getMessage());
		}
	}

	public void close() {
		try {
			if (con != null) {
				System.out.println("MySQL connection was closed!");
				con.close();
			}
		} catch (SQLException localSQLException) {
		}
	}

	public void update(String qry) {
		try {
			Statement st = con.createStatement();
			st.executeUpdate(qry);
			st.close();
		} catch (SQLException e) {
			connect();
			System.err.println(e);
		}
	}

	public ResultSet query(String qry) {
		ResultSet rs = null;
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery(qry);
		} catch (SQLException e) {
			connect();
			System.err.println(e);
		}
		return rs;
	}
}
