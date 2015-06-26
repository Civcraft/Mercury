package vg.civcraft.mc.mercury;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public interface ServiceHandler {
	public boolean isEnabled();
	public void pingService();
	public void addServerToServerList();
	public void sendMessage(String server, String message, String... channels);
	public void addChannels(JavaPlugin plugin, String... channels);
	public void destory();
}
