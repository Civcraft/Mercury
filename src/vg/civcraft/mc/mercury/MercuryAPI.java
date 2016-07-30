package vg.civcraft.mc.mercury;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.common.base.Joiner;

import vg.civcraft.mc.mercury.config.MercuryConfigManager;
import vg.civcraft.mc.mercury.events.EventListener;
import vg.civcraft.mc.mercury.events.EventManager;

public class MercuryAPI {
	public static final Joiner joinPipe = Joiner.on("|");
	public static final Joiner joinComma = Joiner.on(", ");

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
		synchronized (MercuryAPI.instance.playerListLock_) {
			for (PlayerDetails player: players) {
				MercuryAPI.instance.playersByUUID_.put(player.getAccountId(), player);
				MercuryAPI.instance.playersByName_.put(player.getPlayerName(), player);
				if (!MercuryAPI.instance.playersByServer_.containsKey(player.getServerName()))
					MercuryAPI.instance.playersByServer_.put(player.getServerName(), new ArrayList<PlayerDetails>());
				MercuryAPI.instance.playersByServer_.get(player.getServerName()).add(player);
			}
		}
	}

	/**
	 * Returns the server that a player is on.
	 */
	public static PlayerDetails getServerforAccount(UUID accountId) {
		synchronized (MercuryAPI.instance.playerListLock_) {
			return MercuryAPI.instance.playersByUUID_.get(accountId);
		}
	}

	/**
	 * Case-insensitive retrieval of player details
	 */
	public static PlayerDetails getServerforPlayer(String playerName) {
		synchronized (MercuryAPI.instance.playerListLock_) {
			return MercuryAPI.instance.playersByName_.get(playerName);
		}
	}

	/**
	 * Adds a player to the list.
	 * @param player The player's name.
	 * @param server The server that the player is on.
	 */
	public static boolean addPlayer(UUID accountId, String player, String server) {
		return addPlayer(new PlayerDetails(accountId, player, server));
	}

	public static boolean addPlayer(PlayerDetails details) {
		boolean result = true;
		synchronized (MercuryAPI.instance.playerListLock_) {
			PlayerDetails oldDetails = MercuryAPI.instance.playersByUUID_.get(details.getAccountId());
			if (oldDetails != null) {
				if (oldDetails.getServerName().equalsIgnoreCase(details.getServerName())) {
					// The player is already on that server so technically there shouldn't be an update.
					// In the case of a NameLayer player rename though, this player's name could have
					// changed so continue to update the maps.
					result = false;
				}
				// Here we want to remove any previous record if it existed.
				if (!MercuryAPI.instance.playersByServer_.containsKey(oldDetails.getServerName()))
					MercuryAPI.instance.playersByServer_.put(oldDetails.getServerName(), new ArrayList<PlayerDetails>());
				MercuryAPI.instance.playersByServer_.get(oldDetails.getServerName()).remove(oldDetails);
			}
			MercuryAPI.instance.playersByUUID_.put(details.getAccountId(), details);
			MercuryAPI.instance.playersByName_.put(details.getPlayerName(), details);
			if (!MercuryAPI.instance.playersByServer_.containsKey(details.getServerName()))
				MercuryAPI.instance.playersByServer_.put(details.getServerName(), new ArrayList<PlayerDetails>());
			MercuryAPI.instance.playersByServer_.get(details.getServerName()).add(details);
		}
		return result;
	}

	/**
	 * Removes a player from the list.
	 * @param player
	 */
	public static void removeAccount(UUID accountId, String accountName){
		synchronized (MercuryAPI.instance.playerListLock_) {
			PlayerDetails details = MercuryAPI.instance.playersByName_.get(accountName);
			if (details == null) {
				return;
			}
			if (!MercuryAPI.instance.playersByServer_.containsKey(details.getServerName()))
				MercuryAPI.instance.playersByServer_.put(details.getServerName(), new ArrayList<PlayerDetails>());
			MercuryAPI.instance.playersByServer_.get(details.getServerName()).remove(details);
			MercuryAPI.instance.playersByUUID_.remove(accountId);
			MercuryAPI.instance.playersByName_.remove(accountName);
		}
	}

	/**
	 * Get all players connected to all servers.
	 * @return
	 */
	public static Set<UUID> getAllAccounts(){
		synchronized (MercuryAPI.instance.playerListLock_) {
			return MercuryAPI.instance.playersByUUID_.keySet();
		}
	}

	public static Set<String> getAllPlayers(){
		synchronized (MercuryAPI.instance.playerListLock_) {
			return MercuryAPI.instance.playersByName_.keySet();
		}
	}
	
	public static ArrayList<PlayerDetails> getAllAccountsByServer(String server) {
		synchronized(MercuryAPI.instance.playerListLock_) {
			if (!MercuryAPI.instance.playersByServer_.containsKey(server))
				MercuryAPI.instance.playersByServer_.put(server, new ArrayList<PlayerDetails>());
			return MercuryAPI.instance.playersByServer_.get(server);
		}
	}

	public static boolean isKnownAccount(UUID accountId) {
		synchronized (MercuryAPI.instance.playerListLock_) {
			return MercuryAPI.instance.playersByUUID_.containsKey(accountId);
		}
	}

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
			return new HashSet<String>(MercuryAPI.instance.connectedServers_);
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
		playersByName_ = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		playersByServer_ = new HashMap<>();
		connectedServers_ = new TreeSet<>();
	}

	protected void setServiceHandler(ServiceHandler service) {
		service_ = service;
	}

	protected void addConnectedServer(String server) {
		boolean result = false;
		synchronized (MercuryAPI.instance.connectedServers_) {
			if (MercuryAPI.serverName().equalsIgnoreCase(server)) {
				MercuryAPI.err("DUPLICATE SERVER NAME REGISTERED: %s", server);
			}
			result = connectedServers_.add(server);
		}
		if (result) {
			//MercuryAPI.info("Server connected: %s", server);
		}
	}

	protected void removeConnectedServer(String server) {
		boolean result = false;
		synchronized (MercuryAPI.instance.connectedServers_) {
			result = connectedServers_.remove(server);
		}
		if (result) {
			MercuryAPI.info("Server disconnected: %s", server);
		}
	}

	private ServiceHandler service_;
	private String serverName_;
	private Logger log_;
	private Object playerListLock_ = new Object();
	private HashMap<UUID, PlayerDetails> playersByUUID_;
	private TreeMap<String, PlayerDetails> playersByName_;
	private HashMap<String, ArrayList<PlayerDetails>> playersByServer_;
	private Set<String> connectedServers_;
}
