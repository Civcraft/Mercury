package vg.civcraft.mc.mercury;

import net.md_5.bungee.api.plugin.Plugin;

public class MercuryBungePlugin extends Plugin{

	public void onEnable(){ 
		new MercuryAPI();
	}
}
