package de.flokyy.aurora;

import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;

import de.flokyy.aurora.listener.CommandListener;
import de.flokyy.aurora.mysql.MySQL;
import de.flokyy.aurora.utils.CollectionData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Aurora {

	public static boolean startup;
	public static JDABuilder builder;
	public static JDA jda;
	
	public static MySQL mysql;

	public MySQL getMySQL() {
	  return mysql;
	}

	public static void connectMySQL() {
	   mysql = new MySQL(MySQL.HOST, MySQL.DATABASE, MySQL.USER, MySQL.PASSWORD);
	   mysql.update("CREATE TABLE IF NOT EXISTS auroraCache(TRANSACTION text, TOKEN text, PAID_ROYALTY double, SALE_PRICE double, OWED_ROYALTY double, OLD_URI text)");
	   mysql.update("CREATE TABLE IF NOT EXISTS auroraMetadata(TRANSACTION text, TOKEN text, OLD_URI text)");
	   mysql.update("CREATE TABLE IF NOT EXISTS auroraTransactions(UUID text, MEMBER text, TIMESTAMP long, TOKEN text, SERVER text, ROYALTY_AMOUNT double, TRANSACTION text)");
	}
	
	
	public static void main(String[] args) {
		try {
			Aurora.connectMySQL(); // Connect to database
		} catch (Exception e) {
			System.out.println("" + e.getMessage());
			System.exit(1);
		}
		
		builder = JDABuilder.createDefault(""); // Your Discord Bot Secret Key
		builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_INVITES);
		builder.setStatus(OnlineStatus.ONLINE);
		
		builder.addEventListeners(new CommandListener());

		try {
			jda = builder.build(); // Start jda
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				try {
					
					 CollectionData.getLatestData(); 
					}
					catch (Exception e2) {
						System.out.println("Error when trying to fetch data from the coralcube API.");
					}
			}
		};
	  timer.schedule(timerTask, 0l, 120000); // Update every 2 minutes		

	
	}
}
