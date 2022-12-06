## 💎 Aurora Royalt Enforcement
- Aurora is a fully automated royalty enforcement tool with On-Chain NFT-Locking and Discord integration for users to pay the projects creator royalty thereby unlocking their NFT.

# 📖 Description 
- Aurora automatically deactivates any NFT in your collection that interacts with an optional royalty marketplace by swapping the image with a warning and replacing the NFT name. Also users are onced their NFT is locked not able to verify for a Holder role of a project. 
- With the help of our tool, projects can again demand flexible royalties from their users in the long term without giving anyone access to the update-authority, as Aurora is self-hosted and update-authorities therefore stay in the hands of each project.

# 💡 Demo
- Aurora can be tested inside our developer discord: https://discord.gg/q7pJcPgvKz or the NexiLabs [Discord](https://discord.gg/UeHDvRkv).
- For the Demo Royalties are paid for: [NexiLabs](https://magiceden.io/marketplace/nexilabs)
- If you want to live test the tool head over to MagicEden and buy a Nexi Orb without any royalties. The Orb will be locked afterwards. 
- Join the NexiLabs Discord and click on the "Pay-Royalty" channel. 
- Follow the process described in [Royalty Payment Process](https://github.com/Flokyyy/aurora#-royalty-payment-process).
- Once the Royalty is paid the Orb will be unlocked.

# 🎉 1 Minute video overview
- https://www.youtube.com/watch?v=IiQR_q8O7RY

# ⚙️ Aurora Features
🔋 | Fully Automatic 
- Aurora works fully automatic and therefore once installed there's no further work your you.

🛡️ | Simple Royalty Payment
- You don't want to connect your wallet to any site? No worries we got you covered. 
- With Aurora you can pay the Creator Royalty inside the project's discord by sending a simple transaction to a given wallet. More information is provided in the [Royalty Payment Process](https://github.com/Flokyyy/aurora#-royalty-payment-process).

📙 | Global Support 
- Aurora can be used with all Solana collections.

🔑 | Automatic Metadata Update
- Aurora updates the metadata (URI) so users are forced to pay royalties.

🧊 | Automatic Freeze
 - Aurora automatically freezes NFTs if the royalty was not paid within a sale on a marketplace. 
 - Aurora uses the [CoralCubeAPI](https://optemization.notion.site/optemization/Coral-Cube-Royalty-API-Documentation-4c37410d75ed40fe84ec212c82e33ac2) to detect if royalties were paid or not.

🔓 | Automatic Unlock
- Automatic unlocks the NFT once the Creator Royalty is paid by the user and updates the metadata to the original URI.

🌐 | Database Support
- Aurora saves all transaction inside a MySQL database for later usage.

# 💻 Installation
1. Install [Solana-CLI](https://docs.solana.com/cli/install-solana-cli-tools)
2. Install [Metaboss](https://github.com/samuelvanderwaal/metaboss)
3. Change all variables inside the [Data.java](/AuroraV2/src/de/flokyy/aurora/utils/Data.java) file
   - update_authority: Your collections's update authority (e.g ``4CjQRsuLEZBRQngXDAv9CqYtop94SBui3mboA5Ya7y9H``)
   - collection: Your collection symbol (e.g ``nexilabs``)
   - creator_royalty: Charged Royalty Amount (``0.01 = 1%, 0.05 = 5%, 0.1 = 10%``)
   - vault_wallet: Vault Wallet where all royalty funds are being sent to.
   - default_UriLink: The default template of the frozen metadata. See a example in #Default URI Template
   - Update the database details in [MySQL.java](/AuroraV2/src/de/flokyy/aurora/mysql/MySQL.java)
4. Store the update-authority key inside the same directly where the .jar file is running from (Key must be called ``key.json``) 
   - Example-Key:                 ```[4,182,130,247,119,117,227,207,112,73,170,126,222,197,244,99,215,107,255,202,33,43,36,17,104,111,157,246,196,192,174,95,240,23,238,206,118,215,154,238,229,96,11,37,156,123,51,223,5,231,17,117,86,136,103,14,75,95,175,132,148,54,1,46]```
5. Update the Discord Secret Key in [Aurora.java](/AuroraV2/src/de/flokyy/aurora/Aurora.java)
6. Export the code as a runnable jar file.
7. Invite the Bot to your discord (https://discord.com/oauth2/authorize?permissions=8&scope=bot+applications.commands&client_id=YOURCLIENTIDEHERE)
- Permissions for Aurora are automatically assigned through the invite (Administrator), otherwise manually add them. 

8. Create a new channel (e.g "royalty-payment") and type "+setuproyalty". 
![Screenshot (1238)](https://user-images.githubusercontent.com/68162827/205489491-f8c7da00-1ecf-46cb-924c-503d1afd1fab.png)
- Aurora will automatically post the payment message.

9. Your finished! Please do a demo payment in order to test you setup your vault-wallet correctly. 

# 💸 Royalty Payment Process
- Once you click on "Pay Royalty" Aurora will automatically create a new channel for the royalty payment process. 
![Screenshot (1240)](https://user-images.githubusercontent.com/68162827/205489625-360b5daf-130b-48ac-8f63-5893891e920d.png)

- To continue click on "Provide a transaction" 
![Screenshot (1237)](https://user-images.githubusercontent.com/68162827/205489635-3f80d994-8c9a-4ee8-9440-0b36f34a5320.png)
- You will need to enter the transaction of your nft sale.

- Once you provided the transaction Aurora will send you the main details for the payment processs.
![Screenshot (1261)](https://user-images.githubusercontent.com/68162827/205521135-1f19dc16-f524-4f3d-8810-c8f02b68bd91.png)

# Default URI Template
- This is the default URI template which is used for the metadata change. 
- This json file must be uploaded and needs to be accessable all time. We therefore suggest uploading this to Arweave etc.
- Make sure to change all required fields before using this, otherwise your metadata can get screwed up.
```
{"name":"Locked NFT","image":"https://media.discordapp.net/attachments/1041799650623103007/1048662599832719360/royaltyprotection.png","symbol":"SYMBOL","attributes":[{"value":"Not Paid","trait_type":"Royalty"}],"properties":{"files":[{"uri":"https://media.discordapp.net/attachments/1041799650623103007/1048662599832719360/royaltyprotection.png","type":"image"}],"category":"image","creators":[{"share":100,"address":"YOURCREATORADDRESS"}]},"description":"This NFT is locked due to not paying creator royalties.","seller_fee_basis_points":1000}
```

# 💰 Paid Royalty:
- If you paid the Royalty Aurora will automatically detect this and will sent the royalty to the projects vault.
- Aurora will provide the transaction for the funds transaction the the projects vault wallet.
- Your NFT gets automatically unlocked and is ready to use again.
![Screenshot (1239)](https://user-images.githubusercontent.com/68162827/205492897-2db8a411-07b0-4b12-b048-09a3268076f5.png)

# ⛔ Royalty not Paid:
- If you didn't pay the Royalty we implemented a timeout with takes place after 7 minutes.
![Screenshot (1191)](https://user-images.githubusercontent.com/68162827/204086620-258bb674-8908-4151-9bc1-072da0498ef3.png)

# FAQ
- Is this kind of payment secure? Yes, this method is even much more secure than current models, since the user's wallet does not come into direct contact with any of our smart contracts, since no login or signing is required. Users only need to send a transaction to a given address.

# Solana transactions for Java
- Because there is no direct API for Java we used the Solana Cli or Metaboss in order to perform transactions on the Solana Blockchain. So make sure to have those tools installed if you selfhost Aurora. 

# Contact
- Twitter: https://twitter.com/SolFloky
