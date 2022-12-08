package de.flokyy.aurora.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;

import de.flokyy.aurora.Aurora;

public class MySQLStatements {

	public static boolean cacheTransactionExists(String tx) {
		try {
			ResultSet rs = Aurora.mysql.query("SELECT TRANSACTION FROM auroraCache WHERE TRANSACTION='" + tx + "'");
			if (rs.next()) {
				if (rs.getString("TRANSACTION") != null) {
					return true;
				}
			}
		} catch (SQLException localSQLException) {
		}
		return false;
	}
	
	public static boolean metadataAlreadyChanged(String tx) {
		try {
			ResultSet rs = Aurora.mysql.query("SELECT TRANSACTION FROM auroraMetadata WHERE TRANSACTION='" + tx + "'");
			if (rs.next()) {
				if (rs.getString("TRANSACTION") != null) {
					return true;
				}
			}
		} catch (SQLException localSQLException) {
		}
		return false;
	}
	
	public static boolean paidTransactionExists(String tx) {
		try {
			ResultSet rs = Aurora.mysql.query("SELECT TRANSACTION FROM auroraTransactions WHERE TRANSACTION='" + tx + "'");
			if (rs.next()) {
				if (rs.getString("TRANSACTION") != null) {
					return true;
				}
			}
		} catch (SQLException localSQLException) {
		}
		return false;
	}
	
	public static void savePaidRoyalty(String uuid, String member, Long timestamp, String token, String server, Double amount, String tx) {
		Aurora.mysql.update("INSERT INTO auroraTransactions(UUID) VALUES ('" + uuid + "')");
		Aurora.mysql.update("UPDATE auroraTransactions SET MEMBER='" + member + "'WHERE UUID='" + uuid + "'");
		Aurora.mysql.update("UPDATE auroraTransactions SET TIMESTAMP='" + timestamp + "'WHERE UUID='" + uuid + "'");
		Aurora.mysql.update("UPDATE auroraTransactions SET TOKEN='" + token + "'WHERE UUID='" + uuid + "'");
		Aurora.mysql.update("UPDATE auroraTransactions SET SERVER='" + server + "'WHERE UUID='" + uuid + "'");
		Aurora.mysql.update("UPDATE auroraTransactions SET ROYALTY_AMOUNT='" + amount + "'WHERE UUID='" + uuid + "'");
		Aurora.mysql.update("UPDATE auroraTransactions SET TRANSACTION='" + tx + "'WHERE UUID='" + uuid + "'");
	}
	
	public static void createNewEntry(String transaction, String token, Double royalty, Double selling_price, Double owedRoyalty, String uri) {
		Aurora.mysql.update("INSERT INTO auroraCache(TRANSACTION) VALUES ('" + transaction + "')");
		Aurora.mysql.update("UPDATE auroraCache SET TOKEN='" + token + "'WHERE TRANSACTION='" + transaction + "'");
		Aurora.mysql.update("UPDATE auroraCache SET PAID_ROYALTY='" + royalty + "'WHERE TRANSACTION='" + transaction + "'");
		Aurora.mysql.update("UPDATE auroraCache SET SALE_PRICE='" + selling_price + "'WHERE TRANSACTION='" + transaction + "'");
		Aurora.mysql.update("UPDATE auroraCache SET OWED_ROYALTY='" + owedRoyalty + "'WHERE TRANSACTION='" + transaction + "'");
		Aurora.mysql.update("UPDATE auroraCache SET OLD_URI='" + uri + "'WHERE TRANSACTION='" + transaction + "'");
	}
	
	public static void metaDataSaved(String transaction, String token, String uri) {
		Aurora.mysql.update("INSERT INTO auroraMetadata(TRANSACTION) VALUES ('" + transaction + "')");
		Aurora.mysql.update("UPDATE auroraMetadata SET TOKEN='" + token + "'WHERE TRANSACTION='" + transaction + "'");
		Aurora.mysql.update("UPDATE auroraMetadata SET OLD_URI='" + uri + "'WHERE TRANSACTION='" + transaction + "'");
	}
	
	public static String getTokenFromTransaction(String tx) {
		try {
			ResultSet rs = Aurora.mysql.query("SELECT TOKEN FROM auroraCache WHERE TRANSACTION='" + tx + "'");
			if (rs.next()) {
				String s = rs.getString("TOKEN");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static Double getOwedRoyaltyFromTransaction(String tx) {
		try {
			ResultSet rs = Aurora.mysql.query("SELECT OWED_ROYALTY FROM auroraCache WHERE TRANSACTION='" + tx + "'");
			if (rs.next()) {
				Double s = rs.getDouble("OWED_ROYALTY");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static String getOldURIFromTransaction(String tx) {
		try {
			ResultSet rs = Aurora.mysql.query("SELECT OLD_URI FROM auroraCache WHERE TRANSACTION='" + tx + "'");
			if (rs.next()) {
				String s = rs.getString("OLD_URI");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	
}
