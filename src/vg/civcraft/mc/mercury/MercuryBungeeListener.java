package vg.civcraft.mc.mercury;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ProxyServer;
import vg.civcraft.mc.mercury.config.MercuryConfigManager;
import vg.civcraft.mc.mercury.events.EventListener;

public class MercuryBungeeListener implements EventListener {

	private Set<String> pinged = Collections.synchronizedSet(new TreeSet<String>());
	public static boolean initialized = false; // I have no fucking idea why this is required but dont remove it.
	// For some reason this class is somehow instigating two of itself.
	
	public MercuryBungeeListener() {
		if (initialized) {
			return;
		}
		initialized = true;
		ProxyServer.getInstance().getScheduler().schedule(MercuryBungePlugin.plugin, new Runnable() {

			@Override
			public void run() {
				for (String server : MercuryAPI.getAllConnectedServers()) {
					if (pinged.contains(server)) { // Then they havent removed it.
						MercuryAPI.instance.removeConnectedServer(server); // So remove them.
					}
					else {
						pinged.add(server); // Add them to be checked.
					}
				}
			}
			
		}, 1, MercuryConfigManager.getSecondsPing(), TimeUnit.SECONDS);
	}
	
	@Override
	public void receiveMessage(String originServer, String channel, String msg) {
		if (!channel.equalsIgnoreCase("mercury")) {
			return;
		}
		String[] message = msg.split("\\|", 3);
		if (message.length < 2) {
			// Malformed
			MercuryAPI.warn("Malformed message: %s", msg);
			return;
		}
		final String thisServer = MercuryAPI.serverName();
		final String reason = message[0];
		final String remoteServer = message[1];
		final String remainder = message.length >= 3 ? message[2] : null;
		pinged.remove(remoteServer);
		if (reason.equals("ping")){
			MercuryAPI.instance.addConnectedServer(remoteServer);
			return;
		}
		if (reason.equals("sync")){
			// Data format: sync|serverName|jsonPlayerDetails
			final List<PlayerDetails> playerList = PlayerDetails.deserializeList(remainder);
			if (playerList == null) {
				MercuryAPI.warn("Malformed message: %s", msg);
				return;
			}
			for (PlayerDetails details : playerList) {
				MercuryAPI.addPlayer(details);
			}
			MercuryAPI.info("Synced %d players from %s", playerList.size(), remoteServer);
			return;
		}

		if (remainder != null) {
			message = remainder.split("\\|");
		} else {
			message = null;
		}

		if (reason.equals("login")){
			// Data format: login|serverName|playerUUID|playerName
			if (message == null || message.length < 2) {
				MercuryAPI.warn("Malformed message: %s", msg);
				return;
			}
			final String playerUUID = message[0];
			final String playerName = message[1];
			try {
				UUID accountId = UUID.fromString(playerUUID);
			  MercuryAPI.addPlayer(accountId, playerName, remoteServer);
			} catch(Exception ex) {}
			return;
		}
		if (reason.equals("logoff")){
			// Data format: logoff|serverName|playerUUID|playerName
			if (message == null || message.length < 2) {
				MercuryAPI.warn("Malformed message: %s", msg);
				return;
			}
			final String playerUUID = message[0];
			final String playerName = message[1];
			try {
				UUID accountId = UUID.fromString(playerUUID);
				MercuryAPI.removeAccount(accountId, playerName);
			} catch(Exception ex) {}
			return;
		}
	}
}
