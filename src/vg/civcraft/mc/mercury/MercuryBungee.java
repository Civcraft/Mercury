package vg.civcraft.mc.mercury;

public class MercuryBungee {

	public static void enableService(final MercuryAPI api) {
		MercuryBungePlugin.plugin.getProxy().getScheduler().runAsync(MercuryBungePlugin.plugin, new Runnable(){

			@Override
			public void run() {
				api.setServiceHandler(ServiceManager.getService());
			}
			
		});
	}
}
