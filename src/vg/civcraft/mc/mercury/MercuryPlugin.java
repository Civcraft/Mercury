package vg.civcraft.mc.mercury;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public class MercuryPlugin extends JavaPlugin{

	public static MercuryPlugin instance;
	public static MercuryAPI api;
	private static MercuryService service;
	
	@Override
	public void onEnable(){
		instance = this;
		saveDefaultConfig();
		if (service == null){
			service = new MercuryService(this);
		} else {
			service.enable();
		}
	}
	
	public void onDisable(){
		service.destroy();
	}
	
	
}
