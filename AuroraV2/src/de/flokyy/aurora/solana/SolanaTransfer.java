package de.flokyy.aurora.solana;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class SolanaTransfer extends Thread {

	 public SolanaTransfer(String sender, String receipt, Double amount) {
			// TODO Auto-generated constructor stub
		}

	  
	  public static String run(String sender, String receipt, Double amount) {
			String s = "";
	        Process p;
	        String balance = "";
	        
	        ArrayList list = new ArrayList<>();
	        try {
	        	
	            p = Runtime.getRuntime().exec("solana transfer --from /home/Aurora/wallets/" + sender + ".json " + receipt + " " + amount + " --allow-unfunded-recipient --url https://api.mainnet-beta.solana.com --fee-payer /home/Aurora/wallets/" + sender + ".json --no-wait");
	            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
	            p.waitFor(1, TimeUnit.MINUTES);
	    	    while ((s = br.readLine()) != null) {
	                list.add(s);
	              
	    	    }
	    	    
	    	    for (int i = 0; i < list.size(); i++) {
	    		      String pubKey = (String) list.get(i);
	    		      if(pubKey.contains("Signature:")) {
	    		    	  balance = pubKey; 
	    		      }
	    	    	}
	             
	                p.destroyForcibly();
	                return balance;
	        } catch (Exception e) {
			return "ERROR";
	        }
		}
		
		
	
		 
}
