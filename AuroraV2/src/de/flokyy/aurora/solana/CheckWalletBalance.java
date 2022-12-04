package de.flokyy.aurora.solana;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class CheckWalletBalance extends Thread {
	
	public CheckWalletBalance(String wallet) {
		// TODO Auto-generated constructor stub
	}

	public String run(String publicKey) {
		String s = "";
        Process p;
        String balance = "";
        try {
            p = Runtime.getRuntime().exec("solana balance " + publicKey +  " --url https://api.mainnet-beta.solana.com");
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            
                p.waitFor(1, TimeUnit.MINUTES);
                while ((s = br.readLine()) != null) {
                balance = s;
                }
                p.destroyForcibly();
                stop();
                return balance;
        } catch (Exception e) {
		return "ERROR";
        }
	}

}
