package de.flokyy.aurora.listener;

import java.awt.Color;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import de.flokyy.aurora.mysql.MySQLStatements;
import de.flokyy.aurora.solana.CheckTransaction;
import de.flokyy.aurora.solana.CheckWalletBalance;
import de.flokyy.aurora.solana.NewWallet;
import de.flokyy.aurora.solana.SolanaTransfer;
import de.flokyy.aurora.solana.UpdateMetadata;
import de.flokyy.aurora.utils.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

public class CommandListener extends ListenerAdapter {

	public static ArrayList blocked = new ArrayList();
	
	public static double round(double d, int decimalPlace){
		BigDecimal bd = new BigDecimal(Double.toString(d));
		bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}
	 
	@Override
	public void onModalInteraction(ModalInteractionEvent event) {
		event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
		InteractionHook hook = event.getHook(); // This is a special webhook that allows you to send messages without having permissions in the channel and also allows ephemeral messages
	    hook.setEphemeral(true);
	    
	    if(event.getModalId().equalsIgnoreCase("PayWithTransaction")) {
	    	ModalMapping input = event.getValue("transaction");
	    	String transaction = input.getAsString();

	    	if(!MySQLStatements.cacheTransactionExists(transaction)) { //Transaction is invaild or was not fetched yet.
	    		hook.sendMessage("There is no owed royalty for this NFT yet. Please try a another transaction or at a different time again!").queue();
	    		return;
	    	}
	    	
	    	if(MySQLStatements.paidTransactionExists(transaction)) { //Royalty was already paid for this exact transaction
	    		hook.sendMessage("Someone already paid the Creator Royalty for this Sale. Please try a another transaction!").queue();
	    		return;
	    	}
	    	
	    	if(MySQLStatements.cacheTransactionExists(transaction)) { // Transaction wasn't paid yet
	    		
	    	hook.sendMessage("Thanks for providing a vaild transaction. Aurora will automatically unlock your NFT once the royalty is paid!").queue();
			String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
			NewWallet myThread = new NewWallet(uuid);

			String wallet = myThread.run(uuid);
			myThread.stop();

			if (wallet.equalsIgnoreCase("ERROR")) { // If wallet creation failed

				EmbedBuilder builder = new EmbedBuilder();
				builder.setTitle("ERROR");

				builder.setDescription(":exclamation: I'm sorry " + event.getMember().getAsMention()
						+ ", but an error occured. Please try again!");
				builder.setColor(Color.red);

				builder.setFooter("Powered by Aurora",
						"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");
				builder.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
				TextChannel tc = event.getTextChannel();
				tc.sendMessageEmbeds(builder.build()).queue();
				return;
			}

			EmbedBuilder builder = new EmbedBuilder();
			builder.setTitle("ROYALTY PAYMENT");

			builder.setDescription("Please send the royalty payment to: ``" + wallet
					+ "``\n\nMore information is displayed down below :point_down: \nMake sure the amount you send is **equal** or **above** to the provided owed royalty amount. \n\nAurora __automatically__ detects if funds have been deposited and will post further information if done.");

			

			builder.addField("Collection", "" + Data.collection.toUpperCase() + "", false);
		
			TextChannel tc = event.getTextChannel();
			
			
		
			Double royaltyAmount = MySQLStatements.getOwedRoyaltyFromTransaction(transaction);
		
			builder.addField("Total Owed Royalty", "" + royaltyAmount + " SOL", false);
			builder.addField("Creator Royalty Fee", "" + round((Data.creator_royalty) * 100.0, 3) + "%", false);
			
			String token = MySQLStatements.getTokenFromTransaction(transaction);
			builder.addField("Token-Address", "" + token, false);
			
		

			builder.setColor(new Color(144, 238, 144));
			builder.setFooter("Powered by Aurora",
					"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");
			builder.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
			
			tc.sendMessageEmbeds(builder.build())
					.setActionRow(Button.danger("Cancel", "Cancel Request")).queue();

			Timer timer1112 = new Timer();
			TimerTask hourlyTask1112 = new TimerTask() {

				int tries = 20; // Max tries

				@Override
				public void run() {
					try {
						tries--;

						if (event.getChannel() == null) { // In case the channel is deleted
							cancel();
							return;
						}
						
						if (tries <= 0) {
							EmbedBuilder builder = new EmbedBuilder();
							builder.setTitle("PAYMENT TIMEOUT");
							builder.setDescription(
									"The Royalty Payment was not sent within ``7 minutes`` leading to a cancellation. Please try again.");

							builder.setColor(Color.red);
							builder.setFooter("Powered by Aurora",
									"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");
							builder.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));

							if (event.getChannel() != null) {
								TextChannel tc = event.getTextChannel();
								tc.sendMessageEmbeds(builder.build()).queue();
							}
							cancel(); // Cancel Timer
							if (event.getChannel() != null) {
								
								try {
								event.getChannel().delete().queueAfter(40, TimeUnit.SECONDS); // Delete channel after 3 minutes
								}
								catch(Exception e) {
									
								}
							}
							return;

						} else {
							CheckWalletBalance myThread = new CheckWalletBalance(wallet);

							String balance = myThread.run(wallet).replace("SOL", "").replaceAll("\\s+", "");
							myThread.stop();

							Double walletBalance = Double.parseDouble(balance);

							if (walletBalance >= royaltyAmount) { // If wallet balance is greater than previous and greater than or equal the set owed royalty
								try {
									EmbedBuilder builder = new EmbedBuilder();
									builder.setTitle("ROYALTY PAID");
									builder.setDescription("We successfully received your payment "
											+ event.getMember().getAsMention()
											+ ". \nThis channel will get deleted automatically in ``3 minutes`` \n\nThank you for using Aurora <3");

									builder.setColor(Color.green);
									builder.setFooter("Powered by Aurora",
											"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");
									builder.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
									TextChannel tc = event.getTextChannel();
									tc.sendMessageEmbeds(builder.build()).queue();

									cancel();

									try {
										String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
										long unixTime = Instant.now().getEpochSecond();
										MySQLStatements.savePaidRoyalty(uuid, event.getMember().getId(), unixTime, token, event.getGuild().getId(), royaltyAmount, transaction); //Saving the paid royalty in the database
									}
									catch(Exception e) {
										String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
										long unixTime = Instant.now().getEpochSecond();
										MySQLStatements.savePaidRoyalty(uuid, event.getMember().getId(), unixTime, token, event.getGuild().getId(), royaltyAmount, transaction);  //Trying again
									}
								
									boolean shouldCloseChannel = false;
									
									
									try {
									String oldURI = MySQLStatements.getOldURIFromTransaction(transaction);
									UpdateMetadata check = new UpdateMetadata(token); //Updating the metadata back to the original one
									String update = check.run(token, true, oldURI);
									
									if(update.equalsIgnoreCase("ERROR")) { //Couldn't update
										EmbedBuilder builder1 = new EmbedBuilder();
										builder1.setTitle("ERROR");
										builder1.setDescription(
												"Aurora couldn't unlock your NFT. Please contact the support from the project and ask for a manual unlock.");

										builder1.setColor(Color.red);
										builder1.setFooter("Powered by Aurora",
												"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");
										builder1.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
										
										tc.sendMessageEmbeds(builder1.build()).queue();
					       				
									}
									else { //Successfully updated the metadata to a locked NFT
										
										EmbedBuilder builder1 = new EmbedBuilder();
										builder1.setTitle("NFT UNLOCKED");
										builder1.setDescription(
												"We successfully unlocked your NFT and updated the metadata.");

										builder1.setColor(Color.green);
										builder1.setFooter("Powered by Aurora",
												"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");
										builder1.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
										
										tc.sendMessageEmbeds(builder1.build()).setActionRow(
												Button.link("https://solscan.io/token/" + token, "See your NFT"))
												.queue();
										
										shouldCloseChannel = true;
						       	
						       	
								
									}
									
					       			}
					       			catch(Exception e) { //Couldn't update
					       				EmbedBuilder builder1 = new EmbedBuilder();
										builder1.setTitle("ERROR");
										builder1.setDescription(
												"We couldnt unlock your NFT. Please contact the support and ask for a manual unlock.");

										builder1.setColor(Color.red);
										builder1.setFooter("Powered by Aurora",
												"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");
										builder1.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
										
										tc.sendMessageEmbeds(builder1.build()).queue();
					       				
					       			}
								
									Double amount = walletBalance - 0.001; // Amount on the wallet - network fee
																			// otherwise the transaction will fail

									

									String vaultWallet = Data.vault_wallet; // Getting the vault wallet from the data class
									
									SolanaTransfer transfer = new SolanaTransfer(uuid, vaultWallet, amount); // Transfer
																												// the
																												// SOL
																												// to
																												// the
																												// vault

									String transaction = transfer.run(uuid, vaultWallet, amount);
									transfer.stop();

									String[] parts = transaction.split(":");
									String part1 = parts[1].replaceAll("\\s+", "");

									if (!part1.equalsIgnoreCase("ERROR")) {

										CheckTransaction check = new CheckTransaction(part1);

										String confirmation = check.run(part1);
										check.stop();

										
										
										EmbedBuilder builder1 = new EmbedBuilder();
										builder1.setTitle("ROYALTY DISTRIBUTED");
										builder1.setDescription(
												"We successfully sent the royalty to the vault of the project. Save this transaction for potential later usage.");

										builder1.setColor(Color.green);
										builder1.setFooter("Powered by Aurora",
												"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");
										builder1.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
										
										tc.sendMessageEmbeds(builder1.build()).setActionRow(
												Button.link("https://solana.fm/tx/" + part1, "SolanaFM Transaction"))
												.queue();

									} else {
										System.out.println("Error when trying to send the vault transaction for: " + vaultWallet + " User: " + event.getMember().getId());
										cancel();
									}

									if (event.getChannel() != null) {
										if(shouldCloseChannel == true) {
										event.getChannel().delete().queueAfter(3, TimeUnit.MINUTES); // Delete channel	
										}
									}
									return;

								} catch (Exception e) {
									cancel(); // Cancel Timer
								}
							}
						}

					} catch (Exception e) {
						System.out.println("[LOG] | Error " + e.getMessage());
						return;
					}
				}
			};
			timer1112.schedule(hourlyTask1112, 0l, 20000);
	    	}
	    }
	    
		
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		if (event.getComponentId().equals("Cancel")) {

			event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
			InteractionHook hook = event.getHook(); // This is a special webhook that allows you to send messages
													// without having permissions in the channel and also allows
													// ephemeral messages
			hook.setEphemeral(true);

			
			if(blocked.contains(event.getMember())) {
				hook.sendMessage("You already canceled this request.").queue();
				return;
			}
			
			if(!blocked.contains(event.getMember())) {
			   blocked.add(event.getMember()); // Adding user to the blocked arraylist for later usage
			   hook.sendMessage("Request has been cancelled. This channel will get removed in some seconds.").queue();
			}
			
			event.getChannel().delete().queueAfter(4, TimeUnit.SECONDS);
		}
		
		
		if (event.getComponentId().equals("PayWithTransaction")) {
			
			if(!blocked.contains(event.getMember())) {
			 TextInput name = TextInput.create("transaction", "Transaction", TextInputStyle.SHORT)
			          .setPlaceholder("Enter the full transaction here")
			          .setMinLength(1)
			          .setRequired(true)
			          .build();
			 
			
			Modal modal = Modal.create("PayWithTransaction", "Aurora Royalty Payment").addActionRow(name).build();
			event.replyModal(modal).queue();
			}
			else {
				event.reply("You already canceled this payment process!").queue();
				return;
			}
		}

		if (event.getComponentId().equals("Start")) {
			event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
			InteractionHook hook = event.getHook(); // This is a special webhook that allows you to send messages
													// without having permissions in the channel and also allows
													// ephemeral messages
			hook.setEphemeral(true);
			
			if(!blocked.contains(event.getMember())) {
				
				hook.sendMessage("Great! Please now provide a transaction.").queue();
				List<Button> buttons = new ArrayList<Button>();
				buttons.add(Button.danger("Cancel", "Cancel the payment"));
				buttons.add(Button.secondary("PayWithTransaction", "Provide a transaction"));
			
				 
				EmbedBuilder builder = new EmbedBuilder();
				builder.setTitle("TRANSACTION");
				builder.setDescription("Enter the transaction of your NFT sale.");

				builder.setColor(Color.yellow);
				builder.setFooter("Powered by Aurora",
						"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");
				builder.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
				 
				TextChannel tc = event.getTextChannel();
				tc.sendMessageEmbeds(builder.build()).setActionRow(buttons).queue();
				
			}
			else {
				hook.sendMessage("You already canceled this payment process!").queue();
				return;
			}
		}

		
		if (event.getComponentId().equals("Pay")) {
			event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
			InteractionHook hook = event.getHook();
			hook.setEphemeral(true);

			if (!Data.update_authority.isEmpty() && !Data.vault_wallet.isEmpty() && !Data.collection.isEmpty() && !(Data.creator_royalty != 0.0)) { // Check if server has already been setup
				hook.sendMessage(
						"This server hasnt been setup for Aurora yet!")
						.queue();
				return;
			}

			for (TextChannel tc : event.getGuild().getTextChannels()) {
				if (tc.getTopic() != null) {
					if (tc.getTopic().equalsIgnoreCase(event.getMember().getId())) { // Checking for existing tickets
																						// with the topic.
						hook.sendMessage(
								"You already got a royalty payment request! Please **close** or **finish** the other one before.")
								.setEphemeral(true).queue();
						return;
					}
				}
			}

			if(blocked.contains(event.getMember())) {
				blocked.remove(event.getMember());
			}
			
			event.getGuild().createTextChannel("royalty-payment").setTopic(event.getMember().getId()) // Sets channel
																										// topic to
																										// users id to
																										// check later
																										// that they
																										// don't open
																										// multiple
			
			.addPermissionOverride(event.getMember(), EnumSet.of(Permission.VIEW_CHANNEL), null)
			.addPermissionOverride(event.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
					
					.queue(channel -> {
						TextChannel txtChannel = event.getJDA().getTextChannelById(channel.getIdLong());

						EmbedBuilder builder = new EmbedBuilder();
						builder.setTitle("INFORMATION");

						builder.setDescription("Hey there, " + event.getMember().getAsMention()
								+ "\nThank you for choosing to pay our Creator Royalty to unlock your NFT. \nIn our next step we will start with the payment process. \n\nTo continue please click ``Start``.");
						builder.setColor(new Color(144, 238, 144));
						builder.setThumbnail(event.getUser().getAvatarUrl());
						builder.setFooter("Powered by Aurora",
								"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");
						builder.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
						txtChannel.sendMessageEmbeds(builder.build()).setActionRow(Button.primary("Start", "Start"))
								.queue();

						builder.clear();

						hook.sendMessage("Request sent. Please check: " + txtChannel.getAsMention()).setEphemeral(true)
								.queue();

					});
			}
	}

	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		try {
			String msg = event.getMessage().getContentDisplay();
			if (msg.equalsIgnoreCase("+setuproyalty")) {
				event.getMessage().delete().queue();
				if (event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
					EmbedBuilder builder = new EmbedBuilder();
					builder.setTitle("ROYALTY PAYMENT");

					builder.setDescription("Please click on the button to pay the creator royalty for ``"
							+ event.getGuild().getName()
							+ "``. \nOnce your Royalty is paid your NFTs are getting unlocked automatically.");
					builder.setColor(new Color(144, 238, 144));
					builder.setFooter("Powered by Aurora",
							"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");

					event.getChannel().sendMessageEmbeds(builder.build())
							.setActionRow(Button.primary("Pay", "Pay Royalty")).queue();

				}
			}

		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
}
