package de.flokyy.aurora.solana;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class NewWallet extends Thread {
	
	  public NewWallet(String string) {
		// TODO Auto-generated constructor stub
	}

	public String run(String userID) {
		String s = "";
		Process p;
		String publicKey = "";
		ArrayList list = new ArrayList<>();
		
		 try {
		p = Runtime.getRuntime().exec("solana-keygen new --no-passphrase --outfile /home/Aurora/wallets/" + userID + ".json");
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
	    p.waitFor(1, TimeUnit.MINUTES);
	    while ((s = br.readLine()) != null) {
            list.add(s);
          
	    }
	    
	    for (int i = 0; i < list.size(); i++) {
		      String pubKey = (String) list.get(i);
		      if(pubKey.contains("pubkey:")) {
		    	  publicKey = pubKey; 
		      }
	    	}
         
	    	p.destroyForcibly();
	    	stop();
            String[] parts = publicKey.split(":");
            String part1 = parts[1].replaceAll("\\s+","");
            return part1;
          
            
    } catch (Exception e) {
	return "ERROR";
    }
	
    }

}
