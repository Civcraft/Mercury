package vg.civcraft.mc.mercury;

import vg.civcraft.mc.mercury.config.MercuryConfigManager;

import vg.civcraft.mc.mercury.jedis.JedisHandler;
import vg.civcraft.mc.mercury.rabbitmq.RabbitHandler;
import vg.civcraft.mc.mercury.venus.VenusHandler;

public class ServiceManager {
	private static ServiceHandler handler_;

	public final static ServiceHandler getService() {
		return ServiceManager.getService(MercuryConfigManager.getServiceHandler());
	}

	public final static ServiceHandler getService(String name) {
		if (ServiceManager.handler_ != null) {
			return ServiceManager.handler_;
		}
		if (name.equalsIgnoreCase("redis")) {
			ServiceManager.handler_ = new JedisHandler();
		} else if (name.equalsIgnoreCase("rabbit")) {
			if (MercuryConfigManager.inBungee()) {
				ServiceManager.handler_ = new RabbitHandler(MercuryConfigManager.getThreadFactory());
			} else {
				ServiceManager.handler_ = new RabbitHandler();
			}
		} else if (name.equalsIgnoreCase("venus")) {
			ServiceManager.handler_ = new VenusHandler();
		} else {
			return null;
		}

		if (ServiceManager.handler_.isEnabled() == false){
			ServiceManager.handler_ = null;
		}
		return ServiceManager.handler_;
	}
}
