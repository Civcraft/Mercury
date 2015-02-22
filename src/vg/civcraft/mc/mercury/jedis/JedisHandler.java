package vg.civcraft.mc.mercury.jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.MercuryConfigManager;
import vg.civcraft.mc.mercury.MercuryPlugin;
import vg.civcraft.mc.mercury.ServiceHandler;
import vg.civcraft.mc.mercury.listener.PluginChannelAsyncListener;

public class JedisHandler implements ServiceHandler{

	public static JedisPool pool;
	
	public JedisHandler(){
		addServerToServerList();
		enableJedis();
	}
	
	@Override
	public void pingService() {
		Jedis j = pool.getResource();
		String x = j.get("servers");
		String[] servers = x.split(";");
		StringBuilder message = new StringBuilder();
		message.append("The servers that are currently connected are ");
		for (String z: servers)
			message.append(z + " ");
		MercuryPlugin.instance.getLogger().log(Level.INFO, message.toString());
	}
	
	@Override
	public void addServerToServerList(){
		Jedis j = pool.getResource();
		String x = j.get("servers");
		if (x == null)
			x = "";
		x += MercuryConfigManager.getServerName() + ";";
		j.set("servers", x);
		pool.returnResource(j);
	}
	
	@Override
	public void sendMessage(String message, String... channels){
		Jedis j = pool.getResource();
		for (String channel: channels)
			j.publish(channel, message);
		pool.returnResource(j);
	}
	
	@Override
	public void addChannels(JavaPlugin plugin, String... channels){
		Jedis j = pool.getResource();
		PluginChannelAsyncListener listen = new PluginChannelAsyncListener(plugin);
		j.subscribe(listen, channels);
		pool.returnResource(j);
	}
	
	private void enableJedis(){
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(128);
		String host = MercuryConfigManager.getHost();
		int port = MercuryConfigManager.getPort();
		String password = MercuryConfigManager.getPassword();
		pool = new JedisPool(config, host, port, 10, password);
	}

	@Override
	public void destory() {
		Jedis j = pool.getResource();
		String x = j.get("servers");
		x.replaceFirst(MercuryConfigManager.getServerName() +";", "");
		j.set("servers", x);
		pool.returnResource(j);
		pool.destroy();
	}
	
	public void addPlayer(Player p){
		Jedis j = pool.getResource();
		String players = j.get("players");
		if (players == null)
			players = "";
		players += p.getUniqueId().toString() + ";";
		j.set("players", players);
		pool.returnResource(j);
		MercuryAPI.instance.addPlayer(p.getUniqueId());
	}
	
	@Override
	public void updateLocalCacheofPlayers(){
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

	@Override
	public void removePlayer(Player p) {
		Jedis j = pool.getResource();
		String players = j.get("players");
		players += p.getUniqueId().toString() + " ";
		j.set("players", players);
		pool.returnResource(j);
		MercuryAPI.instance.removePlayer(p.getUniqueId());
	}
}
