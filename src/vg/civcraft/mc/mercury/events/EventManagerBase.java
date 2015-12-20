package vg.civcraft.mc.mercury.events;

public interface EventManagerBase {
	void fireMessage(String originServer, String channel, String message);
	void registerListener(EventListener listener);
	void registerListener(EventListener listener, String ... channels);
}
