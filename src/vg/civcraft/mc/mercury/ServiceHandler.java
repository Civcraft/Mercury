package vg.civcraft.mc.mercury;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public interface ServiceHandler {

	public void pingService();
	public void addServerToServerList();
	public void sendMessage(String message, String... channels);
	public void addChannels(JavaPlugin plugin, String... channels);
	public void destory();
	public void addPlayer(Player p);
	public void removePlayer(Player p);
	public void updateLocalCacheofPlayers();
}
