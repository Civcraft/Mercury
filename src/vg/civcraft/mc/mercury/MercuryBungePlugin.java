package vg.civcraft.mc.mercury;

import vg.civcraft.mc.mercury.events.EventManager;
import net.md_5.bungee.api.plugin.Plugin;

public class MercuryBungePlugin extends Plugin{

	public static MercuryBungePlugin plugin;
	public void onEnable() {
		plugin = this;
		new MercuryAPI();
		EventManager.registerListener(new MercuryBungeeListener());
	}
}
