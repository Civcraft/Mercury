package vg.civcraft.mc.mercury;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.mercury.jedis.JedisHandler;
import vg.civcraft.mc.mercury.rabbitmq.RabbitHandler;
import vg.civcraft.mc.mercury.venus.VenusHandler;

public class MercuryPlugin extends JavaPlugin{

	public static MercuryPlugin instance;
	public static ServiceHandler handler;
	public static String name;
	
	@Override
	public void onEnable(){
		instance = this;
	    saveDefaultConfig();
	    handleService();
	    if (handler == null)
	    	return;
	    new MercuryAPI();
	    addServerToServerList();
	    Bukkit.getScheduler().runTaskTimer(this, new Runnable(){

			@Override
			public void run() {
				pingService();
			}
	    	
	    }, 100, 1000);
	    
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
	
	private void handleService(){
		String service = MercuryConfigManager.getServiceHandler();
		if (service.equalsIgnoreCase("redis"))
			handler = new JedisHandler();
		else if (service.equalsIgnoreCase("rabbit"))
			handler = new RabbitHandler();
		else if (service.equalsIgnoreCase("venus"))
			handler = new VenusHandler();
			
		if (handler.isEnabled() == false){
			handler = null;
			getServer().getPluginManager().disablePlugin(this);
		}
	}
	
	private void pingService(){
		handler.pingService();
	}
}
