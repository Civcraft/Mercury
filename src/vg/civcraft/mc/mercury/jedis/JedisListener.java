package vg.civcraft.mc.mercury.jedis;

import redis.clients.jedis.JedisPubSub;

import vg.civcraft.mc.mercury.events.EventManager;

public class JedisListener extends JedisPubSub{

	public JedisListener(){}

	@Override
	public void onMessage(String channel, String message){
		EventManager.fireMessage(null, channel, message);
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
