package vg.civcraft.mc.mercury.listener;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import redis.clients.jedis.JedisPubSub;
import vg.civcraft.mc.mercury.events.PluginBroadcastMessageEvent;

public class PluginChannelAsyncListener extends JedisPubSub{

	private JavaPlugin plugin;
	private BukkitScheduler sched = Bukkit.getScheduler();
	public PluginChannelAsyncListener(JavaPlugin plugin){
		this.plugin = plugin;
	}
	
	@Override
	public void onMessage(String channel, String message){
		final PluginBroadcastMessageEvent event = new PluginBroadcastMessageEvent(channel, message);
		sched.scheduleSyncDelayedTask(plugin, new Runnable(){

			@Override
			public void run() {
				// just to make sure everything is thread safe we are calling
				// this from a synced thread.
				Bukkit.getPluginManager().callEvent(event);
			}
			
		});
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
