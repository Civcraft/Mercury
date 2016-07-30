package vg.civcraft.mc.mercury;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import vg.civcraft.mc.mercury.config.MercuryConfigManager;
import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;

public class MercuryBukkitListener implements Listener {
	
	private Set<String> pinged = Collections.synchronizedSet(new TreeSet<String>()); // If the server is in this list then they 
	// are not active.
	
	public MercuryBukkitListener() {
		MercuryAPI.registerPluginMessageChannel("mercury");
		MercuryAPI.sendGlobalMessage(
				String.format(
						"whoonline|%s",
						MercuryAPI.serverName()), "mercury");

		Bukkit.getScheduler().scheduleSyncRepeatingTask(MercuryPlugin.instance, new Runnable() {
			@Override
			public void run() {
				sendSyncResponse(MercuryAPI.serverName(), null);
			}
		}, 10, 200);

		Bukkit.getScheduler().scheduleSyncRepeatingTask(MercuryPlugin.instance, new Runnable() {
			@Override
			public void run() {
				for (String server : MercuryAPI.getAllConnectedServers()) {
					if (pinged.contains(server)) { // Then they havent removed it.
						MercuryAPI.instance.removeConnectedServer(server); // So remove them.
					}
					else
						pinged.add(server); // Add them to be checked.
				}
			}
		}, 10, 160);
	}

	public void sendSyncResponse(String thisServer, String remoteServer) {
		final Collection<? extends Player> playerList = Bukkit.getOnlinePlayers();
		final List<PlayerDetails> details = new ArrayList<>(playerList.size());
		for (Player p : playerList) {
			details.add(new PlayerDetails(p.getUniqueId(), p.getDisplayName(), thisServer));
		}
		if (details.isEmpty()) {
			return;
		}
		final String listJson = PlayerDetails.serializeList(details);
		if (listJson == null) {
			MercuryAPI.err("Unable to generate player list for whoonline reponse");
			return;
		}
		final String syncMessage = String.format(
				"sync|%s|%s",
				thisServer,
				listJson);
		if (remoteServer == null) {
			MercuryAPI.sendGlobalMessage(syncMessage, "mercury");
			if (MercuryConfigManager.getDebug()) {
				MercuryAPI.info("Broadcasted sync request");
			}
		} else {
			MercuryAPI.sendMessage(remoteServer, syncMessage, "mercury");
			if (MercuryConfigManager.getDebug()) {
				MercuryAPI.info("Responded to server %s sync request", remoteServer);
			}
		}
	}
	
	@EventHandler()
	public void onMercuryMessage(AsyncPluginBroadcastMessageEvent event) {
		if (!event.getChannel().equalsIgnoreCase("mercury")) {
			return;
		}
		final String msg = event.getMessage();
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
		if (reason.equals("whoonline")){
			sendSyncResponse(thisServer, remoteServer);
			return;
		}
		if (reason.equals("sync")){
			// Data format: sync|serverName|jsonPlayerDetails
			final List<PlayerDetails> playerList = PlayerDetails.deserializeList(remainder);
			if (playerList == null) {
				MercuryAPI.warn("Malformed message: %s", msg);
				return;
			}
			List<String> allSynced = new LinkedList<>();
			for (PlayerDetails details : playerList) {
				MercuryAPI.addPlayer(details);
				allSynced.add(details.getPlayerName());
			}
			if (allSynced.isEmpty()) {
				return;
			}
			if (MercuryConfigManager.getDebug()) {
				MercuryAPI.info("Synced players from %s: %s", remoteServer, MercuryAPI.joinComma.join(allSynced));
			}
			return;
		}
		if (reason.equals("ping")){
			// Data format: ping|serverName
			MercuryAPI.instance.addConnectedServer(remoteServer);
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
			} catch (Exception ex) {}
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
				if (Bukkit.getPlayer(accountId) != null) {
					// Don't remove the player if they are logged into this server.
					// In fact, re-broadcast that they exist on this server.
					sendLoginMessage(accountId, playerName);
					return;
				}
				MercuryAPI.removeAccount(accountId, playerName);
			} catch (Exception ex) {}
			return;
		}
	}

	public void sendLoginMessage(UUID accountId, String playerName) {
		MercuryAPI.addPlayer(accountId, playerName, MercuryAPI.serverName());
		MercuryAPI.sendGlobalMessage(
				String.format(
						"login|%s|%s|%s",
						MercuryAPI.serverName(),
						accountId.toString(),
						playerName),
				"mercury");
	}

	public void sendLogoffMessage(UUID accountId, String playerName) {
		MercuryAPI.removeAccount(accountId, playerName);
		MercuryAPI.sendGlobalMessage(
				String.format(
						"logoff|%s|%s|%s",
						MercuryAPI.serverName(),
						accountId.toString(),
						playerName),
				"mercury");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin(PlayerJoinEvent event){
		sendLoginMessage(event.getPlayer().getUniqueId(), event.getPlayer().getDisplayName());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onKick(PlayerKickEvent event){
		sendLogoffMessage(event.getPlayer().getUniqueId(), event.getPlayer().getDisplayName());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogoff(PlayerQuitEvent event){
		sendLogoffMessage(event.getPlayer().getUniqueId(), event.getPlayer().getDisplayName());
	}
}
