package vg.civcraft.mc.mercury.config;

import java.util.concurrent.ThreadFactory;

import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.MercuryPlugin;
import org.bukkit.configuration.ConfigurationSection;

public class BukkitConfiguration implements Configuration {
	public BukkitConfiguration(){}

	@Override
	public String getHost(){
		return config().getString("host", null);
	}

	@Override
	public String getPassword(){
		return config().getString("password", null);
	}

	@Override
	public Integer getPort(){
		int val = config().getInt("port", -1);
		if (val < 0) {
			return null;
		}
		return new Integer(val);
	}
	
	@Override
	public String getUserName() {
		return config().getString("username", "");
	}

	@Override
	public String getServerName(){
		return config().getString("servername", null);
	}

	@Override
	public String getServiceHandler(){
		return config().getString("service", null);
	}

	@Override
	public Boolean getDebug(){
		return config().getBoolean("debug", false);
	}

	@Override
	public ThreadFactory getThreadFactory() {
		return null;
	}

	private ConfigurationSection config(){
		return MercuryPlugin.instance.getConfig();
	}
	
	public int getPingTicks() {
		return config().getInt("proxyticks", 20);
	}
}
