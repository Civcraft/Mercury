package vg.civcraft.mc.mercury.events;

import vg.civcraft.mc.mercury.config.MercuryConfigManager;

public class EventManager {
	static {
		if (MercuryConfigManager.inBukkit()) {
			eventMgr_ = new BukkitEventManager();
		} else {
			eventMgr_ = new IndependentEventManager();
		}
	}

	public static void fireMessage(String originServer, String channel, String message) {
		eventMgr_.fireMessage(originServer, channel, message);
	}

	public static void registerListener(EventListener listener) {
		eventMgr_.registerListener(listener);
	}

	public static void registerListener(EventListener listener, String ... channels) {
		eventMgr_.registerListener(listener, channels);
	}

	private EventManager() {}

	private static final EventManagerBase eventMgr_;
}
