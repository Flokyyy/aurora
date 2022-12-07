package de.flokyy.aurora.utils;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import de.flokyy.aurora.Aurora;
import de.flokyy.aurora.mysql.MySQLStatements;
import de.flokyy.aurora.solana.UpdateMetadata;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CollectionData {

	 public static double round(double d, int decimalPlace){
		   BigDecimal bd = new BigDecimal(Double.toString(d));
		   bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
		   return bd.doubleValue();
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
       		 //If transaction wasn't saved yet in the database
       			
       			if(MySQLStatements.metadataAlreadyChanged(signature)) {	
       			    //Already locked and metadata was updated
					continue;
       			}
       			if(MySQLStatements.paidTransactionExists(signature)) {
       				 //Already paid the royalty with aurora
       				continue;
       			
				}
	       		try { 
	       			
	       			if(!MySQLStatements.cacheTransactionExists(signature)) { // Checking if signature wasn't saved already
					Aurora.mysql.update("INSERT INTO auroraCache(TRANSACTION) VALUES ('" + signature + "')"); //Saving data
			       		Aurora.mysql.update("UPDATE auroraCache SET TOKEN='" + mint + "'WHERE TRANSACTION='" + signature + "'");
			       		Aurora.mysql.update("UPDATE auroraCache SET PAID_ROYALTY='" + 0.0 + "'WHERE TRANSACTION='" + signature + "'");
			       		Aurora.mysql.update("UPDATE auroraCache SET SALE_PRICE='" + nftPrice + "'WHERE TRANSACTION='" + signature + "'");
			       		Aurora.mysql.update("UPDATE auroraCache SET OWED_ROYALTY='" + owedRoyalty + "'WHERE TRANSACTION='" + signature + "'");
			       		Aurora.mysql.update("UPDATE auroraCache SET OLD_URI='" + updatedURI + "'WHERE TRANSACTION='" + signature + "'"); // Will be used for later usage
					}
	       			
	       			String update = null;
					try {
						update = updateMetadata(mint, false, "EMTPY", 1);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(!update.equalsIgnoreCase("ERROR") && !(update != null)) { //Successfully updated the metadata to a locked NFT
						
						if(!MySQLStatements.metadataAlreadyChanged(signature)) {	
							   MySQLStatements.metaDataSaved(signature, mint, updatedURI);
							}
				       		System.out.println("Successfully updated metadata for: " + mint);
				       		System.out.println("Saved new entry with transaction: " + signature);
						}
					
					
					else { 
						System.out.println("Error when trying to lock metadata from token: " + mint + "...");
					}
	       			}
	       			catch(Exception e) { 
	       				continue;
	       			}
       		 }
            }
           
  	        
		}
		catch(Exception e) {
			return;
		}
		

	}	

}
