package vg.civcraft.mc.mercury;

import vg.civcraft.mc.mercury.config.MercuryConfigManager;

import vg.civcraft.mc.mercury.jedis.JedisHandler;
import vg.civcraft.mc.mercury.rabbitmq.RabbitHandler;
import vg.civcraft.mc.mercury.venus.VenusHandler;

class ServiceManager {
	public final static ServiceHandler getService() {
		return ServiceManager.getService(MercuryConfigManager.getServiceHandler());
	}

	public final static ServiceHandler getService(String name) {
		ServiceHandler handler;
		if (name.equalsIgnoreCase("redis")) {
			handler = new JedisHandler();
		} else if (name.equalsIgnoreCase("rabbit")) {
			handler = new RabbitHandler();
		} else if (name.equalsIgnoreCase("venus")) {
			handler = new VenusHandler();
		} else {
			return null;
		}

		if (handler.isEnabled() == false){
			return null;
		}
		return handler;
	}
}
