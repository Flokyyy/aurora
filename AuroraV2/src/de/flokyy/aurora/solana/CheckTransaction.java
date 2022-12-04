package de.flokyy.aurora.solana;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class CheckTransaction extends Thread {
	
	  public CheckTransaction(String part1) {
		// TODO Auto-generated constructor stub
	}

	public static String run(String transaction) {
		String s = "";
        Process p;
        String balance = "";
        try {
            p = Runtime.getRuntime().exec("solana confirm " + transaction);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            p.waitFor(1, TimeUnit.MINUTES);
            
                while ((s = br.readLine()) != null) {
                balance = s;
                }
                p.destroyForcibly();
                return balance;
        } catch (Exception e) {
        	return "ERROR";
        }
	}

}

