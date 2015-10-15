package vg.civcraft.mc.mercury;

import net.md_5.bungee.api.plugin.Plugin;

public class MercuryBungePlugin extends Plugin{

	public static MercuryBungePlugin plugin;
	public void onEnable() {
		plugin = this;
		new MercuryAPI();
	}
	
	public static ServiceHandler enableService() {
		return ServiceManager.getService();
	}
}
