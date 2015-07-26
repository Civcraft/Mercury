package vg.civcraft.mc.mercury.venus;

import org.bukkit.Bukkit;

import vg.civcraft.mc.mercury.MercuryPlugin;
import vg.civcraft.mc.mercury.ServiceHandler;

public class VenusHandler implements ServiceHandler{

	private VenusService service;
	private MercuryPlugin plugin = MercuryPlugin.instance;
	
	public VenusHandler(){
		service = new VenusService();
		if (service.connected)
			Bukkit.getScheduler().runTaskAsynchronously(plugin, service);
		else
			service = null;
	}
	
	// Venus doesn't need to be pinged.
	@Override
	public void pingService() {
		
	}

	@Override
	public void addServerToServerList() {
		
	}

	@Override
	public void sendMessage(String destination, String message, String... channels) {
		for (String channel: channels)
			if (service != null)
				service.sendMessage(destination, message, channel);
	}

	// Venus doesn't need channels registered.
	@Override
	public void addChannels(String... channels) {
		
	}

	@Override
	public void destory() {
		if (service != null)
			service.destroy();
	}
	
	@Override
	public boolean isEnabled(){
		if (service != null)
			return true;
		else
			return false;
	}


}
