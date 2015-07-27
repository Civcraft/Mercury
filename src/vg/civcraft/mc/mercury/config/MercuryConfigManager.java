package vg.civcraft.mc.mercury.config;

public class MercuryConfigManager {
	public static void initialize() {
		if (MercuryConfigManager.inBukkit()) {
			config_ = new BukkitConfiguration();
		} else {
			config_ = new JsonConfiguration();
		}
	}

	public static String getHost(){
		return config_.getHost();
	}

	public static String getPassword(){
		return config_.getPassword();
	}

	public static Integer getPort(){
		return config_.getPort();
	}

	public static String getServerName(){
		return config_.getServerName();
	}

	public static String getServiceHandler(){
		return config_.getServiceHandler();
	}

	public static boolean inBukkit() {
		if (MercuryConfigManager.inBukkit_ != null) {
			return inBukkit_.booleanValue();
		}
		try {
			Class.forName("org.bukkit.Bukkit");
			inBukkit_ = new Boolean(true);
			return true;
		} catch(ClassNotFoundException ex) {
			inBukkit_ = new Boolean(false);
			return false;
		}
	}

	private static Configuration config_ = null;
	private static Boolean inBukkit_ = null;
}
