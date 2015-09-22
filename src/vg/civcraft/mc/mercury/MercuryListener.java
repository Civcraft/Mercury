package vg.civcraft.mc.mercury;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;

public class MercuryListener implements Listener {
	
	public MercuryListener() {
		MercuryAPI.instance.registerPluginMessageChannel("mercury");
		MercuryAPI.instance.sendMessage("all", "whoonline "+MercuryPlugin.name, "mercury");
		Bukkit.getScheduler().scheduleSyncRepeatingTask(MercuryPlugin.instance, new Runnable() {

			@Override
			public void run() {
				StringBuilder message = new StringBuilder();
				message.append("sync " + MercuryPlugin.name + " ");
				for (Player p: Bukkit.getOnlinePlayers())
					message.append(p.getName() + ";");
				
				if (message.toString().split(" ").length != 3)
					return;
				MercuryAPI.instance.sendMessage("all", message.toString(), "mercury");
			}
			
		}, 10, 1200);
	}
	
	public void onMerucyrMessage(AsyncPluginBroadcastMessageEvent event) {
		if (!event.getChannel().equalsIgnoreCase("mercury"))
			return;
		String[] message = event.getMessage().split(" ");
		String reason = message[0];
		if (reason.equals("whoonline")){
			String playerlist = "";
			for(Player p : Bukkit.getOnlinePlayers()){
				playerlist = playerlist+p.getDisplayName()+";";
			}
			if (playerlist.isEmpty()){return;}
			playerlist = playerlist.substring(0, playerlist.length()-1);
			MercuryAPI.instance.sendMessage(message[1], "sync "+MercuryPlugin.name+" "+playerlist, "namelayer");
			MercuryPlugin.instance.getLogger().info("Responded to server '"+message[1]+"' sync request");
			return;
		} else if (reason.equals("login")){
			MercuryAPI.instance.addPlayer(message[2].toLowerCase(), message[1]);
			MercuryPlugin.instance.getLogger().info("Player "+message[2]+" has logged in on server: "+message[1]);
			return;
		} else if (reason.equals("logoff")){
			MercuryAPI.instance.removePlayer(message[2]);
			MercuryPlugin.instance.getLogger().info("Player "+message[2]+" has logged off on server: "+message[1]);
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
			MercuryPlugin.instance.getLogger().info("Synced players from '"+message[1]+"': "+allsynced);
			return;
		}
		
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin(PlayerJoinEvent event){
		MercuryPlugin.handler.sendMessage("all", "login "+MercuryPlugin.name+" "+event.getPlayer().getDisplayName(), "mercury");
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogoff(PlayerQuitEvent event){
		MercuryPlugin.handler.sendMessage("all", "logoff "+MercuryPlugin.name+" "+event.getPlayer().getDisplayName(), "mercury");
	}
}
