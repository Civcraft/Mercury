package vg.civcraft.mc.mercury;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import vg.civcraft.mc.mercury.events.EventListener;

public class MercuryBungeeListener implements EventListener {

	@Override
	public void receiveMessage(String channel, String msg) {
		if (!channel.equalsIgnoreCase("mercury")) {
			return;
		}
		String[] message = msg.split("|", 3);
		if (message.length < 2) {
			// Malformed
			MercuryAPI.warn("Malformed message: %s", msg);
			return;
		}
		final String thisServer = MercuryAPI.serverName();
		final String reason = message[0];
		final String remoteServer = message[1];
		final String remainder = message.length >= 3 ? message[2] : null;
		if (reason.equals("ping")){
			String server = message[1];
			MercuryAPI.instance.addConnectedServer(server);
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
			message = remainder.split("|");
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
			  MercuryAPI.instance.addPlayer(accountId, playerName, remoteServer);
			  MercuryAPI.info("Player %s has logged in on server: %s", playerName, remoteServer);
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
				MercuryAPI.removeAccount(accountId);
			  MercuryAPI.info("Player %s has logged off on server: %s", playerName, remoteServer);
			} catch(Exception ex) {}
			return;
		}
	}
}
