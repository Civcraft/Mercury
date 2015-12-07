package vg.civcraft.mc.mercury;

import java.util.logging.Logger;

import net.md_5.bungee.api.plugin.Plugin;

import vg.civcraft.mc.mercury.MercuryAPI;

public class MercuryBungePlugin extends Plugin{

	public static MercuryBungePlugin plugin;

	public static Logger log() {
		return MercuryBungePlugin.plugin.getLogger();
	}

	public void onEnable() {
		plugin = this;
		MercuryAPI.initialize();
		MercuryAPI.addChannels("mercury");
		MercuryAPI.registerListener(new MercuryBungeeListener(), "mercury");
	}
}
