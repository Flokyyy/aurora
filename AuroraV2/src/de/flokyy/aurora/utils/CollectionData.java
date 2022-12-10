package de.flokyy.aurora.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import de.flokyy.aurora.Aurora;
import de.flokyy.aurora.mysql.MySQLStatements;
import de.flokyy.aurora.solana.UpdateMetadata;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CollectionData {

	 public static double round(double d, int decimalPlace){
	  BigDecimal bd = new BigDecimal(Double.toString(d));
	  bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
	  return bd.doubleValue();
	}
	 
	public static int percent(double a, double b) {
	   float result = 0.0F;
	   result = (float)((b - a) * 100.0D / a);
	   return (int)result;
	}

	public static String updateMetadata(String token, boolean originalMetadata, String oldURI, int tries) {
		if(tries != 4) {
		UpdateMetadata check = new UpdateMetadata(token); //Updating the metadata back to the original one
		String update = check.run(token, originalMetadata, oldURI);
			
		if(update.equalsIgnoreCase("ERROR")) { //Couldn't update
			updateMetadata(token, originalMetadata, oldURI, tries + 1);
		}
		else {
			return update;
			  }
		}
		return "ERROR";
	}
	
	//Uses the MagicEden API to get the current floor price of a collection
	public static Double getCollectionFP(String collection) {
		try {
		 
			OkHttpClient client = new OkHttpClient().newBuilder().build();
			Request request = new Request.Builder().url("https://api-mainnet.magiceden.dev/v2/collections/" + collection).method("GET", null).build();
			Response response = null;
			
			
			try {
				response = client.newCall(request).execute();
			} catch (IOException e2) {
				return 0.0;
			}
			String s = null;
			try {
				s = response.body().string();
			} catch (IOException e2) {
				return 0.0;
			}
		
			JSONParser parser = new JSONParser();

			JSONObject json = (JSONObject) parser.parse(s);

			JSONObject jsonObject = (JSONObject) json;

	
			
			Object fp = null;
			
			try {
				fp = (Long) jsonObject.get("floorPrice");
			}
			catch(Exception e) {
				return 0.0;
			}
				
				
				
			Long fpLong = (Long) fp;
			long length = (long) (Math.log10(fpLong) + 1);
			double fpREAL = 0;

			// VOLUME
			if (length != 0) {

				if (length >= 9) {
					fpREAL = (fpLong.doubleValue() / 1000000000);

				}

				if (length <= 8) {
					fpREAL = (fpLong.doubleValue() / 10000000000L);

				}
			}
			return fpREAL;
		}
		catch(Exception e) {
			System.out.println("Couldnt fetch FP for: " + collection);
			return 0.0;
		}

	}
	
	public static Double fetchCollectionFloorPrice(String symbol, int tries) {
		if(tries != 3) {
		Double fp =	getCollectionFP(symbol);
			
		if(fp == 0.0 || fp == 0 || fp < 0.0) {
			fetchCollectionFloorPrice(symbol, tries + 1);
		}
		else {
			return fp;
			}
		}
		return 0.0;
	}
	
	public static void getLatestData() {
		try {
			OkHttpClient client = new OkHttpClient().newBuilder()
					 .connectTimeout(3,TimeUnit.MINUTES)
					 .writeTimeout(3,TimeUnit.MINUTES)
					 .readTimeout(3,TimeUnit.MINUTES)
					 .build();
     
			String builder = "https://api.coralcube.cc/0dec5037-f67d-4da8-9eb6-97e2a09ffe9a/inspector/getMintActivities?update_authority=" + Data.update_authority + "&collection_symbol=" + Data.collection +"&limit=" + Data.limit + "";
			
			Request request = new Request.Builder().url(builder).method("GET", null).build();
			Response response = null;
			
			try {
				response = client.newCall(request).execute();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			String s = null;
			try {
				s = response.body().string();
				if(s.contains("Gateway Time-out")) {
					System.out.println("Failed to fetch data from the coralcube API trying again");
					getLatestData();
					
				}
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			
		
			JSONParser parser = new JSONParser();

			Object obj = parser.parse(s);
            JSONArray array = (JSONArray)obj;
    	    
            int paid = 0;
            int notPaid = 0;
            int total = 0;
            
            for(Object ob : array) {
               JSONObject entry = (JSONObject) ob;
               Long royaltyFee = (Long)entry.get("royalty_fee");
               Long price = (Long)entry.get("price");
               
               JSONObject entryObject = (JSONObject) entry.get("metadata");
               String mint = (String)entryObject.get("mint");
               String signature = (String)entry.get("signature");
               String uri = (String)entryObject.get("uri");
               
               String updatedURI = uri.replaceAll("<", "").replaceAll(">", "");
               total++;
               
               
               long length = (long) (Math.log10(royaltyFee) + 1);
       		   double royalty = 0;
       		   
       		   if (length != 0) {

       		   if (length >= 9) {
       			  royalty = (royaltyFee.doubleValue() / 1000000000);
       		   }

       			if (length <= 8) {
       			  royalty = (royaltyFee.doubleValue() / 10000000000L);
       			}
       		   }
       			
       		  long lengthNFTPrice = (long) (Math.log10(price) + 1);
       		  double nftPrice = 0;
      		   
      		   if (lengthNFTPrice != 0) {

      		   if (lengthNFTPrice >= 9) {
      			 nftPrice = (price.doubleValue() / 1000000000);
      		   }

      		   if (lengthNFTPrice <= 8) {
      			nftPrice = (price.doubleValue() / 10000000000L);
      			}
      		 }   
      		
      		Double owedRoyalty = round(nftPrice * Data.creator_royalty, 3); //Normal Royalty calculation using the NFT Sale and the Creator Percentage
      		 
      		if(owedRoyalty <= 0.01) {
      			owedRoyalty = 0.01;
      		}
      		
      		if(royalty > 0) {
      			paid = paid + 1;
      		}
      		
       		if(royalty == 0.0 || royalty == 0) { // Royalty was not paid
       			
       			notPaid = notPaid + 1;
       			if(MySQLStatements.paidTransactionExists(signature)) {
       				 //Already paid the royalty with aurora
       				continue;
				}
	       		try { 
	       			if(!MySQLStatements.cacheTransactionExists(signature)) { // Checking if signature wasn't saved already
	       				if(!updatedURI.equalsIgnoreCase(Data.default_UriLink) && !updatedURI.contains("locked")) {
	       					
	       				
	       				if(Data.dynamicRoyalty) {
	       					
	       					Double fp = CollectionData.fetchCollectionFloorPrice(Data.collection, 1);
	       					if(fp != 0.0) {
	       					Aurora.mysql.update("INSERT INTO auroraCache(TRANSACTION) VALUES ('" + signature + "')"); //Saving data		
	       					int percentage = percent(fp, nftPrice);
	       					
	       					if(percentage > 0) { //Sold above floor
	       						Double dynamicOwedRoyalty = owedRoyalty - (owedRoyalty * Data.creator_royalty * 2);
	       						Aurora.mysql.update("UPDATE auroraCache SET OWED_ROYALTY='" + dynamicOwedRoyalty + "'WHERE TRANSACTION='" + signature + "'");		
	       					}
	       					if(percentage == 0) { //Sold for floor
	       						Aurora.mysql.update("UPDATE auroraCache SET OWED_ROYALTY='" + owedRoyalty + "'WHERE TRANSACTION='" + signature + "'");	
	       					}
	       					if(percentage < 0) { //Paperhanded under floor
	       						Double dynamicOwedRoyalty = owedRoyalty + (owedRoyalty * Data.creator_royalty * 3);
	       						Aurora.mysql.update("UPDATE auroraCache SET OWED_ROYALTY='" + dynamicOwedRoyalty + "'WHERE TRANSACTION='" + signature + "'");		
	       					}
	       					
	       					}
	       					else {
	       						System.out.println("Couldnt fetch the collections floor price trying again later!");
	       						continue;
	       					}
	       				}
	       				if(!Data.dynamicRoyalty) {
	       					Aurora.mysql.update("INSERT INTO auroraCache(TRANSACTION) VALUES ('" + signature + "')"); //Saving data	
	       					Aurora.mysql.update("UPDATE auroraCache SET OWED_ROYALTY='" + owedRoyalty + "'WHERE TRANSACTION='" + signature + "'");	
	       				}
	       				
				       		Aurora.mysql.update("UPDATE auroraCache SET TOKEN='" + mint + "'WHERE TRANSACTION='" + signature + "'");
				       		Aurora.mysql.update("UPDATE auroraCache SET PAID_ROYALTY='" + 0.0 + "'WHERE TRANSACTION='" + signature + "'");
				       		Aurora.mysql.update("UPDATE auroraCache SET SALE_PRICE='" + nftPrice + "'WHERE TRANSACTION='" + signature + "'");
				       		
				       		Aurora.mysql.update("UPDATE auroraCache SET OLD_URI='" + updatedURI + "'WHERE TRANSACTION='" + signature + "'"); // Will be used for later usage
				       		System.out.println("New NFT sale has been fetched and saved.");
	       					
	       				}
	       				else {
	       					System.out.println("Found a transaction but the old URI is equal to the locked URI.");
	       					continue;
	       				}
					}
	       			else {
	       				System.out.println("Transaction already existed.");
       					continue;
	       			}
	       			
	       			if(!MySQLStatements.metadataAlreadyChanged(signature)) {
	       			
	       			if(updatedURI.equalsIgnoreCase(Data.default_UriLink) || updatedURI.contains("locked")) {
	       				if(!MySQLStatements.metadataAlreadyChanged(signature)) {	
						MySQLStatements.metaDataSaved(signature, mint, updatedURI);
						continue;
					}  
	       			}
	       			String update = null;
					try {
						update = updateMetadata(mint, false, "EMTPY", 1);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(update.equalsIgnoreCase("ERROR")) {
						System.out.println("Metadata update for " + mint +  " has failed!");
						continue;
					}
							
					if(!update.equalsIgnoreCase("ERROR") && !(update.isEmpty())) { //Successfully updated the metadata to a locked NFT
						if(!MySQLStatements.metadataAlreadyChanged(signature)) {	
						    MySQLStatements.metaDataSaved(signature, mint, updatedURI);
						}   
				       	System.out.println("Successfully updated metadata for: " + mint + " and saved new entry with transaction: " + signature);
						}
	       			  }
	       			}
	       			catch(Exception e) { 
	       				System.out.println("Error with entry... Error; " + e.getMessage());
	       				continue;
	       			}
       		 }
            }
            System.out.println("Successfully fetched the last: " + total + " sales | Paid Royalty: " + paid + " Not Paid: " + notPaid);
			}
			catch(Exception e) {
				return;
			}
	}	

}
