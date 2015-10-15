package vg.civcraft.mc.mercury;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.plugin.java.JavaPlugin;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import vg.civcraft.mc.mercury.config.MercuryConfigManager;

public class MercuryAPI{

	public static MercuryAPI instance;
	private static HashMap<String, String> onlineAllServers; // Players, Server
	private Set<String> connectedServers;
	public static String serverName;
	private ServiceHandler service;
	
	public MercuryAPI(){
		try {
			Class.forName("org.bukkit.Bukkit");
			service = MercuryPlugin.handler;
		} catch (ClassNotFoundException e) {
			MercuryConfigManager.initialize();
			MercuryBungePlugin.plugin.getProxy().getScheduler().runAsync(MercuryBungePlugin.plugin, new Runnable(){

				@Override
				public void run() {
					service = ServiceManager.getService();
				}
				
			});
		}
		instance = this;
		onlineAllServers = new HashMap<String, String>();
		connectedServers = new TreeSet<String>();
		serverName = MercuryConfigManager.getServerName();
	}
	/**
	 * Allows plugins to register a channel to themselves.
	 * Please register all channels as once, each plugin get's its own listener.
	 * @param plugin- The plugin in question.
	 * @param channel- The channel in question.
	 */
	public void registerPluginMessageChannel(String... channels){
		this.service.addChannels(channels);
	}
	/**
	 * Sets all the players on a server.
	 * @param players
	 */
	public synchronized void setAllPlayers(String server, List<String> players){
		for (String player: players)
			onlineAllServers.put(player, server);
	}
	/**
	 * Returns the server that a player is on.
	 */
	public synchronized String getServerforPlayer(String player) {
		return onlineAllServers.get(player);
	}
	/**
	 * Adds a player to the list.
	 * @param player
	 */
	public synchronized void addPlayer(String server, String player){
		onlineAllServers.put(player, server);
	}
	/**
	 * Removes a player from the list.
	 * @param player
	 */
	public synchronized void removePlayer(String player){
		onlineAllServers.remove(player);
	}
	/**
	 * Get all players connected to all servers.
	 * @return
	 */
	public Set<String> getAllPlayers(){
		return onlineAllServers.keySet();
	}
	
	public void sendMessage(String dest, String message, String... channels){
		service.sendMessage(dest, message, channels);
	}
	
	/**
	 * Gets all connected servers.
	 * @return Returns a list of servers that are connected.
	 */
	public synchronized Set<String> getAllConnectedServers() {
		return connectedServers;
	}
	
	protected synchronized void addConnectedServer(String server) {
		connectedServers.add(server);
	}
	
	protected synchronized void removeConnectedServer(String server) {
		connectedServers.remove(server);
	}
}
