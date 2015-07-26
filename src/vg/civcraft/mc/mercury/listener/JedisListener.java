package vg.civcraft.mc.mercury.listener;

import org.bukkit.Bukkit;

import redis.clients.jedis.JedisPubSub;
import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;

public class JedisListener extends JedisPubSub{

	public JedisListener(){}
	
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
