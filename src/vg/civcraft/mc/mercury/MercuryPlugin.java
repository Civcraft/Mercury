package vg.civcraft.mc.mercury;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import vg.civcraft.mc.mercury.jedis.JedisHandler;
import vg.civcraft.mc.mercury.listener.PlayerTrackerListener;
import vg.civcraft.mc.mercury.listener.PluginChannelAsyncListener;
import vg.civcraft.mc.mercury.rabbitmq.RabbitHandler;

public class MercuryPlugin extends JavaPlugin{

	public static MercuryPlugin instance;
	public static ServiceHandler handler;
	
	@Override
	public void onEnable(){
		instance = this;
	    saveDefaultConfig();
	    handleService();
	    new MercuryAPI();
	    registerListeners();
	    addServerToServerList();
	    Bukkit.getScheduler().runTaskTimer(this, new Runnable(){

			@Override
			public void run() {
				pingService();
			}
	    	
	    }, 100, 1000);
	}
	
	public void onDisable(){
		handler.destory();
	}
	
	public void addChannels(JavaPlugin plugin, String... channels){
		handler.addChannels(plugin, channels);
	}
	
	public void sendMessage(String message, String... channels){
		handler.sendMessage(message, channels);
	}
	
	public void registerListeners(){
		getServer().getPluginManager().registerEvents(new PlayerTrackerListener(), this);
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
	}
	
	private void pingService(){
		handler.pingService();
	}
}
