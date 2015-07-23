package vg.civcraft.mc.mercury.listener;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import redis.clients.jedis.JedisPubSub;
import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;

public class JedisListener extends JedisPubSub{

	private JavaPlugin plugin;
	private BukkitScheduler sched = Bukkit.getScheduler();
	public JedisListener(JavaPlugin plugin){
		this.plugin = plugin;
	}
	
	@Override
	public void onMessage(String channel, String message){
		final AsyncPluginBroadcastMessageEvent event = new AsyncPluginBroadcastMessageEvent(channel, message);
		Bukkit.getPluginManager().callEvent(event);
	}

	@Override
	public void onPMessage(String arg0, String arg1, String arg2) {
		
	}

	@Override
	public void onPSubscribe(String arg0, int arg1) {
		
	}

	@Override
	public void onPUnsubscribe(String arg0, int arg1) {
		
	}

	@Override
	public void onSubscribe(String arg0, int arg1) {
		
	}

	@Override
	public void onUnsubscribe(String arg0, int arg1) {
		
	}
}
