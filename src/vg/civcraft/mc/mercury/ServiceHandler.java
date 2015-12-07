package vg.civcraft.mc.mercury;

public interface ServiceHandler {
	public boolean isEnabled();
	public void pingService();
	public void addServerToServerList();
	public void sendMessage(String server, String message, String... pluginChannels);
	public void sendGlobalMessage(String message, String... pluginChannels);
	public void addChannels(String... pluginChannels);
	public void addGlobalChannels(String... pluginChannels);
	public void destory();
}
