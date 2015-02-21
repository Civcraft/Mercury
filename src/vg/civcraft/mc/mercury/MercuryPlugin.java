package vg.civcraft.mc.mercury;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import vg.civcraft.mc.mercury.listener.PlayerTrackerListener;
import vg.civcraft.mc.mercury.listener.PluginChannelAsyncListener;

public class MercuryPlugin extends JavaPlugin{

	public static MercuryPlugin instance;
	public static JedisPool pool;
	
	@Override
	public void onEnable(){
		instance = this;
	    saveDefaultConfig();
	    enableJedis();
	    new MercuryAPI();
	    registerListeners();
	    addServerToServerList();
	    Bukkit.getScheduler().runTaskTimer(this, new Runnable(){

			@Override
			public void run() {
				pingRedis();
			}
	    	
	    }, 100, 1000);
	}
	
	public void onDisable(){
		pool.destroy();
	}
	
	public void enableJedis(){
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(128);
		String host = MercuryConfigManager.getHost();
		int port = MercuryConfigManager.getPort();
		String password = MercuryConfigManager.getPassword();
		pool = new JedisPool(config, host, port, 10, password);
	}
	
	public void addRedisChannels(JavaPlugin plugin, String... channels){
		Jedis j = pool.getResource();
		PluginChannelAsyncListener listen = new PluginChannelAsyncListener(plugin);
		j.subscribe(listen, channels);
		pool.returnResource(j);
	}
	
	public void sendRedisMessage(String message, String... channels){
		Jedis j = pool.getResource();
		for (String channel: channels)
			j.publish(channel, message);
		pool.returnResource(j);
	}
	
	public void registerListeners(){
		getServer().getPluginManager().registerEvents(new PlayerTrackerListener(), this);
	}
	
	private void addServerToServerList(){
		Jedis j = pool.getResource();
		String x = j.get("servers");
		if (x == null)
			x = "";
		x += MercuryConfigManager.getServerName() + ";";
		j.set("servers", x);
		pool.returnResource(j);
	}
	
	private void pingRedis(){
		Jedis j = pool.getResource();
		String x = j.get("servers");
		String[] servers = x.split(";");
		StringBuilder message = new StringBuilder();
		message.append("The servers that are currently connected are ");
		for (String z: servers)
			message.append(z + " ");
		MercuryPlugin.instance.getLogger().log(Level.INFO, message.toString());
	}
}
