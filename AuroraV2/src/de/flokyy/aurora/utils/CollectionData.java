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

	public static void getLatestData() {
		try {
			System.out.println("Fetching data from coralcube...");
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
    	    
            for(Object ob : array) {
               JSONObject entry = (JSONObject) ob;
               Long royaltyFee = (Long)entry.get("royalty_fee");
               Long price = (Long)entry.get("price");
               
               JSONObject entryObject = (JSONObject) entry.get("metadata");
               String mint = (String)entryObject.get("mint");
               String signature = (String)entry.get("signature");
               String uri = (String)entryObject.get("uri");
             
               String updatedURI = uri.replaceAll("<", "").replaceAll(">", "");
              
               
               
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
      		
      		Double owedRoyalty = round(nftPrice * Data.creator_royalty, 3);
      		 
      		if(owedRoyalty <= 0.01) {
      			owedRoyalty = 0.01;
      		}
      		
       		if(royalty == 0.0 || royalty == 0) { // Royalty was not paid
       		 if(!MySQLStatements.cacheTransactionExists(signature)) { //If transaction wasn't saved yet in the database
       			 
       			System.out.println("New transaction found. Try to save..."); 
       			
       			if(updatedURI.equalsIgnoreCase(Data.default_UriLink)) {
					System.out.println("Found an entry, but old uri is equal to the default seems like it already has been locked. Skipping..");
					continue;
				}
	       		try { 
	       			UpdateMetadata check = new UpdateMetadata(mint); //Updating the metadata

					String update = check.run(mint, false, "EMPTY");
					
					if(update.equalsIgnoreCase("ERROR")) { //Couldn't update
						System.out.println("Error when trying to lock metadata from token: " + mint);
	       				continue;
					}
					else { //Successfully updated the metadata to a locked NFT
					Aurora.mysql.update("INSERT INTO auroraCache(TRANSACTION) VALUES ('" + signature + "')");
		       		Aurora.mysql.update("UPDATE auroraCache SET TOKEN='" + mint + "'WHERE TRANSACTION='" + signature + "'");
		       		Aurora.mysql.update("UPDATE auroraCache SET PAID_ROYALTY='" + 0.0 + "'WHERE TRANSACTION='" + signature + "'");
		       		Aurora.mysql.update("UPDATE auroraCache SET SALE_PRICE='" + nftPrice + "'WHERE TRANSACTION='" + signature + "'");
		       		Aurora.mysql.update("UPDATE auroraCache SET OWED_ROYALTY='" + owedRoyalty + "'WHERE TRANSACTION='" + signature + "'");
		       		Aurora.mysql.update("UPDATE auroraCache SET OLD_URI='" + updatedURI + "'WHERE TRANSACTION='" + signature + "'");
		       			
		       		System.out.println("Successfully updated metadata for: " + mint);
		       		System.out.println("Saved new entry with transaction: " + signature);
					continue;
					}
	       			}
	       			catch(Exception e) { //Couldn't update
	       				System.out.println("Error when trying to lock metadata from token: " + mint);
	       				continue;
	       			}

       			}
       		}
       		
       		
          
            }
           
  	        
		}
		catch(Exception e) {
			return;
		}
		

	}	

}