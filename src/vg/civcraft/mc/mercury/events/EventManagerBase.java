package vg.civcraft.mc.mercury.events;

public interface EventManagerBase {
	void fireMessage(String channel, String message);
	void registerListener(EventListener listener);
}
