package vg.civcraft.mc.mercury;

import java.util.List;
import java.util.UUID;

import org.bukkit.plugin.java.JavaPlugin;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class MercuryAPI{

	private MercuryPlugin plugin = MercuryPlugin.instance;
	public static MercuryAPI instance;
	private List<UUID> allPlayers;
	
	public MercuryAPI(){
		instance = this;
	}
	/**
	 * Allows plugins to register a channel to themselves.
	 * Please register all channels as once, each plugin get's its own listener.
	 * @param plugin- The plugin in question.
	 * @param channel- The channel in question.
	 */
	public void registerPluginMessageChannel(String... channels){
		this.plugin.addChannels(channels);
	}
	/**
	 * Sets all the players on all the servers.
	 * @param players
	 */
	public synchronized void setAllPlayers(List<UUID> players){
		allPlayers = players;
	}
	/**
	 * Adds a player to the list.
	 * @param player
	 */
	public synchronized void addPlayer(UUID player){
		allPlayers.add(player);
	}
	/**
	 * Removes a player from the list.
	 * @param player
	 */
	public synchronized void removePlayer(UUID player){
		allPlayers.remove(player);
	}
	/**
	 * Get all players connected to all servers.
	 * @return
	 */
	public List<UUID> getAllPlayers(){
		return allPlayers;
	}
	
	public void sendMessage(String dest, String message, String... channels){
		plugin.sendMessage(dest, message, channels);
	}
}
