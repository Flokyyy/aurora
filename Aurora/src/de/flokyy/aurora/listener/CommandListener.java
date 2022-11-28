package de.flokyy.aurora.listener;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.flokyy.aurora.mysql.MySQLStatements;
import de.flokyy.aurora.solana.CheckTransaction;
import de.flokyy.aurora.solana.CheckWalletBalance;
import de.flokyy.aurora.solana.NewWallet;
import de.flokyy.aurora.solana.SolanaTransfer;
import de.flokyy.aurora.utils.CollectionData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CommandListener extends ListenerAdapter {

	
	public static HashMap map = new HashMap();
	public static ArrayList blocked = new ArrayList();
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		try {
			if (event.isAcknowledged()) {
				return;
			}

			if (event.getName().equalsIgnoreCase("setup")) {
				event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
				InteractionHook hook = event.getHook();
				hook.setEphemeral(true);

				OptionMapping roleObject = event.getOption("role");
				if (roleObject == null) {
					hook.sendMessage("Role not provided for some reason").queue();
					return;
				}

				OptionMapping collectionObject = event.getOption("collection");
				if (collectionObject == null) {
					hook.sendMessage("Collection not provided for some reason").queue();
					return;
				}

				OptionMapping percentageObject = event.getOption("percentage");
				if (percentageObject == null) {
					hook.sendMessage("Percentage not provided for some reason").queue();
					return;
				}

				OptionMapping walletObject = event.getOption("vault-wallet");
				if (walletObject == null) {
					hook.sendMessage("Wallet not provided for some reason").queue();
					return;
				}

				if (MySQLStatements.royaltyServerExists(event.getGuild().getId())) { // Check if server has already been
																						// setup
					hook.sendMessage("This server has already been setup!").queue();
					return;
				}

				Role role = roleObject.getAsRole();
				String collection = collectionObject.getAsString().toLowerCase();
				String percentage = percentageObject.getAsString();
				String vaultwallet = walletObject.getAsString();

				Double percentageDouble = 0.0;
				try {
					percentageDouble = Double.parseDouble(percentage);
				} catch (Exception e) {
					hook.sendMessage("Your royalty percentage is invaild. Try again. Use ``.`` instead of ``,``")
							.queue();
					return;
				}

				Double fp = CollectionData.getCollectionFP(collection);

				if (fp == 0.0) {
					hook.sendMessage("Couldn't fetch a vaild floorprice for this collection. Please try again!")
							.queue();
					return;
				}

				if (fp == 5.0E-4) {
					hook.sendMessage("Couldn't fetch a vaild floorprice for this collection. Please try again!")
							.queue();
					return;
				}

				if (fp <= 0.01) {
					fp = 0.01; // Minimum fp
				}

				if (fp > 0.0) {

					if (percentageDouble == 0.0) {
						hook.sendMessage("The Royalty percentage can't be 0%").queue();
						return;
					} else {

						Double royaltyPercentageFee = (percentageDouble / 100.0);
					
						Double royaltyAmount = (fp * royaltyPercentageFee);
						
						Double royalyRound = round(royaltyAmount, 3);

						if (royalyRound < 0.01) {
							royalyRound = 0.01;
						}
						MySQLStatements.createNewRoyaltyEntry(event.getGuild().getId(), collection, royalyRound, role.getIdLong(), percentageDouble, vaultwallet);
						hook.sendMessage("You have successfully setup the Aurora Royalty System. Royalty Percentage: ``"
										+ percentageDouble + "``% | Royalty-Amount: ``" + royalyRound + "`` SOL | Vault: ``" + vaultwallet + "``")
								.queue();
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Aurora Setup Error: " + e.getMessage());
		}
	}
	
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
		    
		    if(event.getModalId().equalsIgnoreCase("PayWithTokenAddress")) {
		    	
		    	ModalMapping input = event.getValue("token");
		    	String token = input.getAsString();
				
		    	OkHttpClient client = new OkHttpClient().newBuilder()
		                    .connectTimeout(1, TimeUnit.MINUTES)
		                    .writeTimeout(1, TimeUnit.MINUTES)
		                    .readTimeout(1, TimeUnit.MINUTES)

		                    .build();
		        Request request = new Request.Builder()
		                    .url("https://api-mainnet.magiceden.io/rpc/getNFTByMintAddress/"+token)
		                    .header("User-Agent",
		                            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
		                    .build();

		        Response response = null;

		        try {
		              response = client.newCall(request).execute();
		          } catch (IOException var11) {
		            hook.sendMessage("There was an error. Please try again!").queue();
					return;
		        }

		        String s = null;

		        try {
					s = response.body().string();
				} catch (IOException e1) {
					hook.sendMessage("There was an error. Please try again!").queue();
					return;
				}
		             
		           
		            
		    	JSONParser parser = new JSONParser();

		    	// Read JSON file
		    	Object obj = null;
		    	try {
		    		obj = parser.parse(s);
		    	} catch (ParseException e) {
		    		hook.sendMessage("There was an error. Please try again!").queue();
					return;
		    	}
		    		
				EmbedBuilder help3 = new EmbedBuilder();

		    	JSONObject jsonObject = (JSONObject) obj;

		    	JSONObject metadata = (JSONObject) jsonObject.get("results");

		    	String collectionName = (String) metadata.get("collectionName");
		    	String img = (String) metadata.get("img");
		    	String title = (String) metadata.get("title");

		    	if(collectionName == null) {
		    		hook.sendMessage("This token-address doesn't apply to the collection. Please choose another one!").queue();
		    		return;
		    	}
			
		    	String tokenCollection = collectionName; // Getting the symbol for the provided address
		    	String royaltyCollection = MySQLStatements.getServerCollectionName(event.getGuild().getId()); // Getting the project's (guild uuid) collection symbol
		    	
		    	if(!tokenCollection.equalsIgnoreCase(royaltyCollection)) { // If they are not the same
					hook.sendMessage("This token-address doesn't apply to the collection. Please choose another one!").queue();
					return;
		    	}
		    	try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		    	hook.sendMessage("Thanks for providing a vaild token-address. We will mark this token-address as paid in our system once the process is finished!").queue();
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
						+ "``\n\nMore information is displayed down below :point_down: \nMake sure the amount you send is **equal** or **above** to the provided total amount. \n\nAurora __automatically__ detects if funds have been deposited and will post further information if done.");

				String collection = MySQLStatements.getServerCollectionName(event.getGuild().getId()).replaceAll("_", " ");

				builder.addField("Collection", "" + collection.toUpperCase() + "", false);
			
				TextChannel tc = event.getTextChannel();
				if(!map.containsKey(event.getMember())) {
					tc.sendMessage("Please start over. There was an error").queue();
				}
				
				int nfts = (int) map.get(event.getMember());
				Double amount = MySQLStatements.getServerRoyaltyAmount(event.getGuild().getId()) * nfts;
				builder.addField("NFT", "" + title, false);
				builder.addField("Total Amount", "" + amount + " SOL", false);
				builder.addField("Creator Royalty", "" + amount / nfts + " SOL / NFT", false);
				builder.addField("Token-Address", "" + token, false);
				Double percentageRate = (MySQLStatements.getServerRoyaltyPercentage(event.getGuild().getId()));
				if(img != null && !img.contains(".webp")) {
					builder.setThumbnail(img);
				}
				builder.addField("Creator Royalty Fee", "" + percentageRate + "%", false);

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

								if (walletBalance >= amount) { // If wallet balance >= the set royalty from the database
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
											MySQLStatements.createNewEntry(uuid, event.getMember().getId(), unixTime, token, event.getGuild().getId(), title); //Saving the paid royalty in the database
										}
										catch(Exception e) {
											String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
											long unixTime = Instant.now().getEpochSecond();
											MySQLStatements.createNewEntry(uuid, event.getMember().getId(), unixTime, token, event.getGuild().getId(), title); //Trying again
										}
										
										Double amount = walletBalance - 0.001; // Amount on the wallet - network fee
																				// otherwise the transaction will fail

										Long roleID = MySQLStatements.getServerAssignRole(event.getGuild().getId());
										Role role = event.getGuild().getRoleById(roleID);
										if (role != null) {
											if (!event.getMember().getRoles().contains(role)) {
												event.getGuild().addRoleToMember(event.getMember(), role).queue();
											}
										}

										String vaultWallet = MySQLStatements.getServerVaultWallet(event.getGuild().getId()); // get
																																// vault
																																// from
																																// the
																																// given
																																// project
																																// from
																																// the
																																// database
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
											event.getChannel().delete().queueAfter(3, TimeUnit.MINUTES); // Delete channel	
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
		    
			if(event.getModalId().equalsIgnoreCase("payment")) {
				ModalMapping numberInput = event.getValue("number");
				String number = numberInput.getAsString();
			
				int nfts = 0;
				try {
					nfts = Integer.parseInt(number);
				}
				catch(Exception e) {
					hook.sendMessage("The number you entered is invaild.").queue();
					return;
				}
				if(nfts == 0 || nfts < 0) {
					hook.sendMessage("The number you entered is invaild.").queue();
					return;
				}

				if (!MySQLStatements.royaltyServerExists(event.getGuild().getId())) { // Check if server has already been setup
					hook.sendMessage(
							"This server hasnt been setup for Aurora yet! Please tell the owner to use the /setup command of Aurora to continue.")
							.queue();
					return;
				}
				
				if(!map.containsKey(event.getMember())) {
				map.put(event.getMember(), nfts);
				}
				else {
					map.remove(event.getMember());
					map.put(event.getMember(), nfts);
				}
				
				hook.sendMessage("Great the NFT count was saved! Please follow the instructions now.").queue();

				List<Button> buttons = new ArrayList<Button>();
				buttons.add(Button.danger("Cancel", "Cancel the payment"));
				buttons.add(Button.secondary("PayWithTokenAddress", "Provide a token-address"));
			
				 
				EmbedBuilder builder = new EmbedBuilder();
				builder.setTitle("TOKEN ADDRESS");
				builder.setDescription("Enter the token-address of your NFT.");

				builder.setColor(Color.yellow);
				builder.setFooter("Powered by Aurora",
						"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");
				builder.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
				 
				TextChannel tc = event.getTextChannel();
				tc.sendMessageEmbeds(builder.build()).setActionRow(buttons).queue();
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
		
		
		if (event.getComponentId().equals("PayWithTokenAddress")) {
			
			if(!blocked.contains(event.getMember())) {
			 TextInput name = TextInput.create("token", "Tokenaddress", TextInputStyle.SHORT)
			          .setPlaceholder("Enter the token-address")
			          .setMinLength(1)
			          .setRequired(true)
			          .build();
			 
			
			Modal modal = Modal.create("PayWithTokenAddress", "Aurora Royalty Payment").addActionRow(name).build();
			event.replyModal(modal).queue();
			}
			else {
				event.reply("You already canceled this payment process!").queue();
				return;
			}
		}

		if (event.getComponentId().equals("Start")) {

			if(!blocked.contains(event.getMember())) {
			 TextInput name = TextInput.create("number", "Number of NFTs", TextInputStyle.SHORT)
			          .setPlaceholder("Enter for how many nfts you want to pay the royalty")
			          .setMinLength(1)
			          .setRequired(true)
			          .build();
			 
			
			Modal modal = Modal.create("payment", "Aurora Royalty Payment").addActionRow(name).build();
			event.replyModal(modal).queue();
			}
			else {
				event.reply("You already canceled this payment process!").queue();
				return;
			}
		}

		if (event.getComponentId().equals("Pay")) {
			event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
			InteractionHook hook = event.getHook();
			hook.setEphemeral(true);

			if (!MySQLStatements.royaltyServerExists(event.getGuild().getId())) { // Check if server has already been setup
				hook.sendMessage(
						"This server hasnt been setup for Aurora yet! Please tell the owner to use the /setup command of Aurora to continue.")
						.queue();
				return;
			}

			for (TextChannel tc : event.getGuild().getTextChannels()) {
				if (tc.getTopic() != null) {
					if (tc.getTopic().equalsIgnoreCase(event.getMember().getId())) { // Checking for existing tickets
																						// with the topic.
						hook.sendMessage(
								"You already got a royalty payment request! Please __close__ or __finish__ the other one before.")
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
								+ "\nThank you for choosing to pay our Creator Royalty. \nIn our next step we will start with the payment process. \n\nTo continue please click ``Start`` and fill in the amount of NFTs for which you want to pay the royalty.");
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

		// Just for testing simulating matrica, metahelix role
		if (event.getComponentId().equals("Test")) {
			Role role = event.getGuild().getRoleById(1040296023509172234L);

			if (!event.getMember().getRoles().contains(role)) {
				event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(1040296023509172234L))
						.queueAfter(1L, TimeUnit.SECONDS);

				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.setTitle("→ **ROLE GIVEN**");
				embedBuilder.setDescription(":white_check_mark: You got the verify role!");
				embedBuilder.setThumbnail(event.getMember().getAvatarUrl());
				embedBuilder.setColor(new Color(144, 238, 144));

				embedBuilder.setFooter("Powered by Aurora",
						"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");

				embedBuilder.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
				event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
				return;
			} else {
				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.setTitle("→ **ERROR**");
				embedBuilder.setDescription(":x: You already got this role!");
				embedBuilder.setThumbnail(event.getMember().getAvatarUrl());
				embedBuilder.setColor(Color.red);

				embedBuilder.setFooter("Powered by Aurora",
						"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");

				embedBuilder.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
				event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
				return;
			}
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
							+ "``. \nThis community requests payments from their holders in order to receive a Creator Royalty. \nIf you want find out more about this process please check our [FAQ](https://github.com/Flokyyy/Aurora)");
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
