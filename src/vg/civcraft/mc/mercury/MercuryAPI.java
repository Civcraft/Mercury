package vg.civcraft.mc.mercury;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.common.base.Joiner;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import vg.civcraft.mc.mercury.config.MercuryConfigManager;
import vg.civcraft.mc.mercury.events.EventListener;
import vg.civcraft.mc.mercury.events.EventManager;

public class MercuryAPI {

	public static MercuryAPI instance;

	public static void initialize() {
		if (MercuryAPI.instance != null) {
			return;
		}
		MercuryAPI.instance = new MercuryAPI();
		MercuryAPI.instance.internalInitialize();
		MercuryAPI.info("Mercury initialized");
	}

	public static void shutdown() {
		MercuryAPI.instance.service_.destory();
		MercuryAPI.info("Mercury shutdown");
	}

	public static void info(String msg) {
		MercuryAPI.instance.log_.info(msg);
	}

	public static void info(String msg, Object ... params) {
		MercuryAPI.instance.log_.info(String.format(msg, params));
	}

	public static void warn(String msg) {
		MercuryAPI.instance.log_.warning(msg);
	}

	public static void warn(String msg, Object ... params) {
		MercuryAPI.instance.log_.warning(String.format(msg, params));
	}

	public static void err(String msg) {
		MercuryAPI.instance.log_.severe(msg);
	}

	public static void err(String msg, Object ... params) {
		MercuryAPI.instance.log_.severe(String.format(msg, params));
	}

	public static String serverName() {
		return MercuryAPI.instance.serverName_;
	}

	/**
	 * Allows plugins to register a channel to themselves.
	 * Please register all channels as once, each plugin get's its own listener.
	 * @param plugin- The plugin in question.
	 * @param channel- The channel in question.
	 */
	public static void registerPluginMessageChannel(String... channels){
		MercuryAPI.instance.service_.addChannels(channels);
	}

	/**
	 * Sets all the players on a server.
	 * @param players
	 */
	public static void setAllPlayers(List<PlayerDetails> players){
		synchronized (MercuryAPI.instance.playersByUUID_) {
			for (PlayerDetails player: players) {
				MercuryAPI.instance.playersByUUID_.put(player.getAccountId(), player);
				MercuryAPI.instance.playersByName_.put(player.getPlayerName(), player);
			}
		}
	}

	/**
	 * Returns the server that a player is on.
	 */
	public static PlayerDetails getServerforAccount(UUID accountId) {
		synchronized (MercuryAPI.instance.playersByUUID_) {
			return MercuryAPI.instance.playersByUUID_.get(accountId);
		}
	}

	public static PlayerDetails getServerforPlayer(String playerName) {
		synchronized (MercuryAPI.instance.playersByUUID_) {
			return MercuryAPI.instance.playersByName_.get(playerName);
		}
	}

	/**
	 * Adds a player to the list.
	 * @param player The player's name.
	 * @param server The server that the player is on.
	 */
	public static void addPlayer(UUID accountId, String player, String server) {
		addPlayer(new PlayerDetails(accountId, player, server));
	}

	public static void addPlayer(PlayerDetails details) {
		synchronized (MercuryAPI.instance.playersByUUID_) {
			MercuryAPI.instance.playersByUUID_.put(details.getAccountId(), details);
		}
	}

	/**
	 * Removes a player from the list.
	 * @param player
	 */
	public static void removeAccount(UUID accountId){
		synchronized (MercuryAPI.instance.playersByUUID_) {
			MercuryAPI.instance.playersByUUID_.remove(accountId);
		}
	}

	/**
	 * Get all players connected to all servers.
	 * @return
	 */
	public static Set<UUID> getAllAccounts(){
		synchronized (MercuryAPI.instance.playersByUUID_) {
			return MercuryAPI.instance.playersByUUID_.keySet();
		}
	}

	public static Set<String> getAllPlayers(){
		synchronized (MercuryAPI.instance.playersByName_) {
			return MercuryAPI.instance.playersByName_.keySet();
		}
	}

	public static boolean isKnownAccount(UUID accountId) {
		synchronized (MercuryAPI.instance.playersByUUID_) {
			return MercuryAPI.instance.playersByUUID_.containsKey(accountId);
		}
	}

	private static final Joiner joinPipe = Joiner.on("|");

	public static void traceSendMessage(String dest, String message, String... channels) {
		if (!MercuryConfigManager.getDebug()) {
			return;
		}
		if (dest != null) {
			MercuryAPI.info("Sending to %s on %s: %s", dest, joinPipe.join(channels), message);
		} else {
			MercuryAPI.info("Sending on %s: %s", joinPipe.join(channels), message);
		}
	}

	public static void sendMessage(String dest, String message, String... channels){
		traceSendMessage(dest, message, channels);
		MercuryAPI.instance.service_.sendMessage(dest, message, channels);
	}

	public static void addServerToServerList() {
		MercuryAPI.instance.service_.addServerToServerList();
	}

	public static void pingService() {
		MercuryAPI.instance.service_.pingService();
	}

	public static void sendGlobalMessage(String message, String... channels){
		traceSendMessage(null, message, channels);
		MercuryAPI.instance.service_.sendGlobalMessage(message, channels);
	}

	public static void addChannels(String... pluginChannels) {
		MercuryAPI.instance.service_.addChannels(pluginChannels);
	}

	public static void addGlobalChannels(String... pluginChannels) {
		MercuryAPI.instance.service_.addGlobalChannels(pluginChannels);
	}

	public static void registerListener(EventListener listener) {
		EventManager.registerListener(listener);
	}

	public static void registerListener(EventListener listener, String ... channels) {
		EventManager.registerListener(listener, channels);
	}

	/**
	 * Gets all connected servers.
	 * @return Returns a list of servers that are connected.
	 */
	public static Set<String> getAllConnectedServers() {
		synchronized (MercuryAPI.instance.connectedServers_) {
			return MercuryAPI.instance.connectedServers_;
		}
	}

	protected MercuryAPI() {}

	protected void internalInitialize() {
		if (MercuryConfigManager.inBukkit()) {
			log_ = MercuryPlugin.log();
		} else if (MercuryConfigManager.inBungee()) {
			log_ = MercuryBungee.log();
		} else {
			log_ = Logger.getLogger("MercuryAPI");
		}
		MercuryConfigManager.initialize();

		serverName_ = MercuryConfigManager.getServerName();
		service_ = ServiceManager.getService();
		playersByUUID_ = new HashMap<>();
		playersByName_ = new HashMap<>();
		connectedServers_ = new TreeSet<>();
	}

	protected void setServiceHandler(ServiceHandler service) {
		service_ = service;
	}

	protected void addConnectedServer(String server) {
		MercuryAPI.info("Server connected: %s", server);
		synchronized (MercuryAPI.instance.connectedServers_) {
			if (MercuryAPI.serverName().equalsIgnoreCase(server)) {
				MercuryAPI.err("DUPLICATE SERVER NAME REGISTERED: %s", server);
			}
			connectedServers_.add(server);
		}
	}

	protected void removeConnectedServer(String server) {
		MercuryAPI.info("Server disconnected: %s", server);
		synchronized (MercuryAPI.instance.connectedServers_) {
			connectedServers_.remove(server);
		}
	}

	private ServiceHandler service_;
	private String serverName_;
	private Logger log_;
	private HashMap<UUID, PlayerDetails> playersByUUID_;
	private HashMap<String, PlayerDetails> playersByName_;
	private Set<String> connectedServers_;
}
