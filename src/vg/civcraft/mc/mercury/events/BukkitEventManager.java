package vg.civcraft.mc.mercury.events;

import org.bukkit.Bukkit;

public class BukkitEventManager implements EventManagerBase {
	public BukkitEventManager() {}

	@Override
	public void fireMessage(String originServer, String channel, String message) {
		AsyncPluginBroadcastMessageEvent event = new AsyncPluginBroadcastMessageEvent(originServer, channel, message);
		Bukkit.getPluginManager().callEvent(event);
	}

	@Override
	public void registerListener(EventListener listener) {
		// Bukkit makes its own registry.
		throw new UnsupportedOperationException("Please use the Bukkit event system and AsyncPluginBroadcastMessageEvent");
	}

	@Override
	public void registerListener(EventListener listener, String ... channels) {
		// Bukkit makes its own registry.
		throw new UnsupportedOperationException("Please use the Bukkit event system and AsyncPluginBroadcastMessageEvent");
	}
}
