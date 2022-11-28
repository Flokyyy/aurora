package de.flokyy.aurora.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.jdbc.ResultSetMetaData;

import de.flokyy.aurora.Aurora;
import de.flokyy.aurora.Aurora;

public class MySQLStatements {

	public static boolean royaltyServerExists(String server) {
		try {
			ResultSet rs = Aurora.mysql.query("SELECT SERVER FROM auroraRoyalty WHERE SERVER='" + server + "'");
			if (rs.next()) {
				if (rs.getString("SERVER") != null) {
					return true;
				}
			}
		} catch (SQLException localSQLException) {
		}
		return false;
	}
	
	public static void createNewRoyaltyEntry(String server, String collection, Double royalty, Long role, Double percentage, String vault) {
		Aurora.mysql.update("INSERT INTO auroraRoyalty(SERVER) VALUES ('" + server + "')");
		Aurora.mysql.update("UPDATE auroraRoyalty SET COLLECTION='" + collection + "'WHERE SERVER='" + server + "'");
		Aurora.mysql.update("UPDATE auroraRoyalty SET ROYALTY_AMOUNT='" + royalty + "'WHERE SERVER='" + server + "'");
		Aurora.mysql.update("UPDATE auroraRoyalty SET ROYALTY_PERCENTAGE='" + percentage + "'WHERE SERVER='" + server + "'");
		Aurora.mysql.update("UPDATE auroraRoyalty SET ROLE='" + role + "'WHERE SERVER='" + server + "'");
		Aurora.mysql.update("UPDATE auroraRoyalty SET VAULT='" + vault + "'WHERE SERVER='" + server + "'");
	}
	
	public static void createNewEntry(String uuid, String member, Long timestamp, String token, String server, String nft) {
		Aurora.mysql.update("INSERT INTO auroraTransaction(UUID) VALUES ('" + uuid + "')");
		Aurora.mysql.update("UPDATE auroraTransaction SET MEMBER='" + member + "'WHERE UUID='" + uuid + "'");
		Aurora.mysql.update("UPDATE auroraTransaction SET TIMESTAMP='" + timestamp + "'WHERE UUID='" + uuid + "'");
		Aurora.mysql.update("UPDATE auroraTransaction SET TOKEN='" + token + "'WHERE UUID='" + uuid + "'");
		Aurora.mysql.update("UPDATE auroraTransaction SET SERVER='" + server + "'WHERE UUID='" + uuid + "'");
		Aurora.mysql.update("UPDATE auroraTransaction SET NFT='" + nft + "'WHERE UUID='" + uuid + "'");
	}
	
	public static String getServerVaultWallet(String server) {
		try {
			ResultSet rs = Aurora.mysql.query("SELECT VAULT FROM auroraRoyalty WHERE SERVER='" + server + "'");
			if (rs.next()) {
				String s = rs.getString("VAULT");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static String getServerCollectionName(String server) {
		try {
			ResultSet rs = Aurora.mysql.query("SELECT COLLECTION FROM auroraRoyalty WHERE SERVER='" + server + "'");
			if (rs.next()) {
				String s = rs.getString("COLLECTION");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static Double getServerRoyaltyPercentage(String server) {
		try {
			ResultSet rs = Aurora.mysql.query("SELECT ROYALTY_PERCENTAGE FROM auroraRoyalty WHERE SERVER='" + server + "'");
			if (rs.next()) {
				Double s = rs.getDouble("ROYALTY_PERCENTAGE");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static void updateCollectionRoyalty(String server, Double newRoyalty) {
		Aurora.mysql.update("UPDATE auroraRoyalty SET ROYALTY_AMOUNT='" + newRoyalty + "'WHERE SERVER='" + server + "'");
	}
	
	public static ArrayList getAllServersFromDatabase() {
		try {
			ArrayList<String> list = new ArrayList<String>();
			ResultSet rs = Aurora.mysql.query("SELECT SERVER FROM auroraRoyalty");
			while (rs.next()) {
				String s = rs.getString("SERVER");
				list.add(s);
			}
			return list;
		} catch (SQLException localSQLException) {
		}
		return null;
	}
	
	
	public static Double getServerRoyaltyAmount(String server) {
		try {
			ResultSet rs = Aurora.mysql.query("SELECT ROYALTY_AMOUNT FROM auroraRoyalty WHERE SERVER='" + server + "'");
			if (rs.next()) {
				Double s = rs.getDouble("ROYALTY_AMOUNT");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static Long getServerAssignRole(String server) {
		try {
			ResultSet rs = Aurora.mysql.query("SELECT ROLE FROM auroraRoyalty WHERE SERVER='" + server + "'");
			if (rs.next()) {
				Long s = rs.getLong("ROLE");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
}
