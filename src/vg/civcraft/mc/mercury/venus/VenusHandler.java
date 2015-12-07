package vg.civcraft.mc.mercury.venus;

import vg.civcraft.mc.mercury.MercuryPlugin;
import vg.civcraft.mc.mercury.ServiceHandler;

public class VenusHandler implements ServiceHandler {

	private VenusService service;
	private MercuryPlugin plugin = MercuryPlugin.instance;

	public VenusHandler(){
		service = new VenusService();
		if (service.connected) {
			service.start();
		} else {
			service = null;
		}
	}

	// Venus doesn't need to be pinged.
	@Override
	public void pingService() {}

	@Override
	public void addServerToServerList() {}

	@Override
	public void sendMessage(String server, String message, String... pluginChannels) {
		if (service == null) {
			return;
		}
		for (String channel : pluginChannels) {
			service.sendMessage(server, message, channel);
		}
	}

	@Override
	public void sendGlobalMessage(String message, String... pluginChannels) {
		throw new UnsupportedOperationException();
	}

	// Venus doesn't need channels registered.
	@Override
	public void addChannels(String... channels) {}

	@Override
	public void addGlobalChannels(String... pluginChannels) {
		addChannels(pluginChannels);
	}

	@Override
	public void destory() {
		if (service != null)
			service.teardown();
	}

	@Override
	public boolean isEnabled(){
		return service != null;
	}
}
