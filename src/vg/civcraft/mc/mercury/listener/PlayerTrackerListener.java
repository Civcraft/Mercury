package vg.civcraft.mc.mercury.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.MercuryPlugin;

public class PlayerTrackerListener implements Listener{

	private JedisPool pool = MercuryPlugin.pool;
	public PlayerTrackerListener(){
		Bukkit.getScheduler().runTaskTimerAsynchronously(MercuryPlugin.instance, 
				new Runnable(){

					@Override
					public void run() {
						updateLocalCacheofPlayers();
					}
			
		}, 100 // Just so we can get the initial start up of players once server starts.
		, 100); // 100 ticks is plenty of time to keep caching the list of players.
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void playerLoginEvent(PlayerLoginEvent event){
		addPlayer(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void playerQuitEvent(PlayerQuitEvent event){
		removePlayer(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void playerKickEvent(PlayerKickEvent event){
		removePlayer(event.getPlayer());
	}
	
	private void addPlayer(Player p){
		Jedis j = pool.getResource();
		String players = j.get("players");
		if (players == null)
			players = "";
		players += p.getUniqueId().toString() + ";";
		j.set("players", players);
		pool.returnResource(j);
		MercuryAPI.instance.addPlayer(p.getUniqueId());
	}
	
	private void updateLocalCacheofPlayers(){
		Jedis j = pool.getResource();
		String players = j.get("players");
		if (players == null)
			return;
		pool.returnResource(j);
		List<UUID> playersID = new ArrayList<UUID>();
		for (String x: players.split(";"))
			playersID.add(UUID.fromString(x));
		MercuryAPI.instance.setAllPlayers(playersID);
	}
	
	private void removePlayer(Player p){
		Jedis j = pool.getResource();
		String players = j.get("players");
		players += p.getUniqueId().toString() + " ";
		j.set("players", players);
		pool.returnResource(j);
		MercuryAPI.instance.removePlayer(p.getUniqueId());
	}
}
