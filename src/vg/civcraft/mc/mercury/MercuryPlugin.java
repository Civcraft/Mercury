package vg.civcraft.mc.mercury;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.mercury.config.MercuryConfigManager;

public class MercuryPlugin extends JavaPlugin{

	public static MercuryPlugin instance;
	// This is the name of the server.
	public static String name;

	public static Logger log() {
		return MercuryPlugin.instance.getLogger();
	}

	@Override
	public void onEnable(){
		instance = this;
		saveDefaultConfig();
		MercuryAPI.initialize();
		if (!handleService()) {
			return;
		}
		addServerToServerList();
		Bukkit.getScheduler().runTaskTimer(this, new Runnable(){
			private long lastTime = System.currentTimeMillis();
			@Override
			public void run() {
				if (lastTime + 2500 <= System.currentTimeMillis()) {
					lastTime = System.currentTimeMillis();
					pingService();
				}
			}
		}, 0, MercuryConfigManager.getPingTicks());

		Bukkit.getPluginManager().registerEvents(new MercuryBukkitListener(), this);
		name = MercuryConfigManager.getServerName();
	}

	public void onDisable(){
		MercuryAPI.shutdown();
	}

	public void addChannels(String... channels){
		MercuryAPI.addChannels(channels);
	}

	public void sendMessage(String dest, String message, String... channels){
		MercuryAPI.sendMessage(dest, message, channels);
	}

	private void addServerToServerList(){
		MercuryAPI.addServerToServerList();
	}

	private boolean handleService(){
		ServiceHandler handler = ServiceManager.getService();
		boolean enabled = handler != null;
		if (!enabled) {
			getServer().getPluginManager().disablePlugin(this);
		}
		return enabled;
	}

	private void pingService(){
		MercuryAPI.pingService();
	}
}
