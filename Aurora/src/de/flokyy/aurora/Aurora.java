package de.flokyy.aurora;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;

import de.flokyy.aurora.listener.CommandListener;
import de.flokyy.aurora.mysql.MySQL;
import de.flokyy.aurora.mysql.MySQLStatements;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
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
	   mysql.update("CREATE TABLE IF NOT EXISTS auroraRoyalty(SERVER text, COLLECTION text, ROYALTY_AMOUNT double, ROLE long, ROYALTY_PERCENTAGE double, VAULT text)");
	}
	
	public static void main(String[] args) {
		try {
			Aurora.connectMySQL(); // Connect to database
		} catch (Exception e) {
			System.out.println("" + e.getMessage());
			System.exit(1);
		}
		
		builder = JDABuilder.createDefault("HIDDEN"); // Discord Bot Secret Key
		builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_INVITES);
		builder.setStatus(OnlineStatus.ONLINE);
		
		builder.addEventListeners(new CommandListener());

		OptionData option1 = new OptionData(OptionType.ROLE, "role", "choose the role that the user gets assigned once the royalty is paid", true);
		OptionData option2 = new OptionData(OptionType.STRING, "collection", "set your collection (ME symbol)", true);
		OptionData option3 = new OptionData(OptionType.STRING, "percentage", "set the % as royalty you want from the current floor price of your collection", true);
		OptionData option4 = new OptionData(OptionType.STRING, "vault-wallet", "set vault wallet where all royalties are being sent to", true);
		
		List<CommandData> commandData = new ArrayList<>();
	    commandData.add(Commands.slash("setup", "Set up the Aurora royalty system on your discord").addOptions(option1, option2, option3, option4));

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
		

        jda.updateCommands().addCommands(commandData).queue();
        
		 Timer timer = new Timer();
		 TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {
					try {
						 MySQLStatements.refreshDatabase();
						}
						catch (Exception e2) {
							System.out.println("Error when trying to update the database connection.");
						}
				}
			};
		  timer.schedule(timerTask, 0l, 900000); // Update every 5 minutes
	
	}
}