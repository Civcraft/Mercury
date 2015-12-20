package vg.civcraft.mc.mercury.events;

public interface EventListener {
	void receiveMessage(String originServer, String channel, String message);
}
