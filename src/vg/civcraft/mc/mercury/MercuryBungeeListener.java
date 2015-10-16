package vg.civcraft.mc.mercury;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import vg.civcraft.mc.mercury.events.EventListener;

public class MercuryBungeeListener implements EventListener {

	private Set<String> pinged = Collections.synchronizedSet(new TreeSet<String>());
	
	public MercuryBungeeListener() {
		MercuryBungePlugin.plugin.getProxy().getScheduler().schedule(MercuryBungePlugin.plugin, new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			}
			
		}, 10, 5, TimeUnit.SECONDS);
	}
	
	@Override
	public void receiveMessage(String channel, String mes) {
		if (!channel.equalsIgnoreCase("mercury"))
			return;
		String[] message = mes.split(" ");
		String reason = message[0];
		if (reason.equals("login")){
			MercuryAPI.instance.addPlayer(message[2].toLowerCase(), message[1]);
			//MercuryPlugin.instance.getLogger().info("Player "+message[2]+" has logged in on server: "+message[1]);
			return;
		} else if (reason.equals("logoff")){
			MercuryAPI.instance.removePlayer(message[2]);
			//MercuryPlugin.instance.getLogger().info("Player "+message[2]+" has logged off on server: "+message[1]);
			return;
		} else if (reason.equals("sync")){
			String[] players = message[2].split(";");
			String allsynced = "";
			for (String player : players){
				if (!MercuryAPI.instance.getAllPlayers().contains(player))
					MercuryAPI.instance.addPlayer(player.toLowerCase(), message[1]);
				allsynced = allsynced+player+" ,";
			}
			if (allsynced.isEmpty()){return;}
			allsynced = allsynced.substring(0, allsynced.length()-2);
			//MercuryPlugin.instance.getLogger().info("Synced players from '"+message[1]+"': "+allsynced);
			return;
		} else if (reason.equals("ping")){
			String server = message[1];
			MercuryAPI.instance.addConnectedServer(server);
			pinged.add(server);
		}
	}
}
