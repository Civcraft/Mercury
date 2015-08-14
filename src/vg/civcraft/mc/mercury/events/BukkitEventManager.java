package vg.civcraft.mc.mercury.events;

import org.bukkit.Bukkit;

public class BukkitEventManager implements EventManagerBase {
	public BukkitEventManager() {}

	@Override
	public void fireMessage(String channel, String message) {
		AsyncPluginBroadcastMessageEvent event = new AsyncPluginBroadcastMessageEvent(channel, message);
		Bukkit.getPluginManager().callEvent(event);
	}

	@Override
	public void registerListener(EventListener listener) {
		// Bukkit handles it's own event registration
	}
}
