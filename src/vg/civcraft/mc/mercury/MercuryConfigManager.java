package vg.civcraft.mc.mercury;

import org.bukkit.configuration.file.FileConfiguration;

public class MercuryConfigManager {

	private static FileConfiguration config = MercuryPlugin.instance.getConfig();
	
	public static String getHost(){
		return config.getString("host");
	}
	
	public static String getPassword(){
		return config.getString("password");
	}
	
	public static int getPort(){
		return config.getInt("port");
	}
	
	public static String getServerName(){
		return config.getString("server-name");
	}
	
	public static String getServiceHandler(){
		return config.getString("service");
	}
}
