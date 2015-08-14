package vg.civcraft.mc.mercury;

public interface ServiceHandler {
	public boolean isEnabled();
	public void pingService();
	public void addServerToServerList();
	public void sendMessage(String server, String message, String... channels);
	public void addChannels(String... channels);
	public void destory();
}
