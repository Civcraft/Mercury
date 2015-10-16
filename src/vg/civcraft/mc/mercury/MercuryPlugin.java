package vg.civcraft.mc.mercury;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.mercury.config.MercuryConfigManager;

public class MercuryPlugin extends JavaPlugin{

	public static MercuryPlugin instance;
	public static ServiceHandler handler;
	// This is the name of the server.
	public static String name;

	@Override
	public void onEnable(){
		instance = this;
		saveDefaultConfig();
		MercuryConfigManager.initialize();
		if (!handleService()) {
			return;
		}
		new MercuryAPI();
		addServerToServerList();
		Bukkit.getScheduler().runTaskTimer(this, new Runnable(){
			@Override
			public void run() {
				pingService();
			}
		}, 100, 100);

		Bukkit.getPluginManager().registerEvents(new MercuryBukkitListener(), this);
		name = MercuryConfigManager.getServerName();
	}

	public void onDisable(){
		if (handler != null)
			handler.destory();
	}

	public void addChannels(String... channels){
		handler.addChannels(channels);
	}

	public void sendMessage(String dest, String message, String... channels){
		handler.sendMessage(dest, message, channels);
	}

	private void addServerToServerList(){
		handler.addServerToServerList();
	}

	private boolean handleService(){
		handler = ServiceManager.getService();
		boolean enabled = handler != null;
		if (!enabled) {
			getServer().getPluginManager().disablePlugin(this);
		}
		return enabled;
	}

	private void pingService(){
		handler.pingService();
	}
}
