package de.flokyy.aurora.solana;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import de.flokyy.aurora.utils.Data;

public class UpdateMetadata extends Thread {
	
	  public UpdateMetadata(String string) {
		// TODO Auto-generated constructor stub
	}

	
	public String run(String token, boolean originalMetadata, String originalURI) {
		String s = "";
		Process p;
		String tx = "";
		
		ArrayList list = new ArrayList<>();
		
		if(!originalMetadata) {
		try {
		p = Runtime.getRuntime().exec("metaboss update uri --keypair key.json --account " + token + " --new-uri " + Data.default_UriLink);
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
	    p.waitFor(1, TimeUnit.MINUTES);
	    while ((s = br.readLine()) != null) {
            list.add(s);
          
	    }
	    
	    for (int i = 0; i < list.size(); i++) {
		      String sig = (String) list.get(i);
		      if(sig.contains("Tx sig:")) {
		    	  tx = sig; 
		    	 
		      }
	    	}
         
	    	p.destroyForcibly();
	    	stop();
            String[] parts = tx.split(":");
            String part1 = parts[1].replaceAll("\\s+","");
            return part1;
          
            
    } catch (Exception e) {
	return "ERROR";
    }
	
    }
		else {
			try {
				
				p = Runtime.getRuntime().exec("metaboss update uri --keypair key.json --account " + token + " --new-uri " + originalURI);
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			    p.waitFor(1, TimeUnit.MINUTES);
			    while ((s = br.readLine()) != null) {
		            list.add(s);
		          
			    }
			    
			    for (int i = 0; i < list.size(); i++) {
				      String sig = (String) list.get(i);
				      if(sig.contains("Tx sig:")) {
				    	  tx = sig; 
				    	 
				      }
			    	}
		         
			    	p.destroyForcibly();
			    	stop();
		            String[] parts = tx.split(":");
		            String part1 = parts[1].replaceAll("\\s+","");
		            return part1;
		          
		            
		    } catch (Exception e) {
			return "ERROR";
		    }
		}
		
	}

}
