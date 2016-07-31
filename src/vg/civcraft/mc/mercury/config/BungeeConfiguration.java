package vg.civcraft.mc.mercury.config;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

// Bungee threading deprecated only to deter its use.
import net.md_5.bungee.api.scheduler.GroupedThreadFactory;

import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.MercuryBungePlugin;


public class BungeeConfiguration implements vg.civcraft.mc.mercury.config.Configuration {
	public BungeeConfiguration() {
		configFile_ = new File(MercuryBungePlugin.plugin.getDataFolder(), "config.yml");
		loadConfig();
	}

	public void setConfigFile(File newConfig) {
		configFile_ = newConfig;
	}

	public void loadConfig() {
		File parentDir = configFile_.getParentFile();
		if (!parentDir.exists()) {
			parentDir.mkdir();
		}
		configManager_ = ConfigurationProvider.getProvider(YamlConfiguration.class);
		try {
			config_ = configManager_.load(configFile_);
		} catch(IOException ex) {
			MercuryAPI.err("Unable to load config: " + configFile_.getName());
			config_ = new net.md_5.bungee.config.Configuration();
		}
	}

	private net.md_5.bungee.config.Configuration config() {
		return config_;
	}

	@Override
	public String getHost(){
		return config().getString("host", "localhost");
	}

	@Override
	public String getPassword(){
		return config().getString("password", "");
	}

	@Override
	public Integer getPort(){
		int val = config().getInt("port", 5672);
		if (val < 0) {
			return 5672;
		}
		return new Integer(val);
	}

	@Override
	public String getUserName() {
		return config().getString("username", "bukkit");
	}

	@Override
	public String getServerName(){
		return config().getString("servername", "");
	}

	@Override
	public Boolean getDebug(){
		return config().getBoolean("debug", false);
	}

	@Override
	public String getServiceHandler(){
		return config().getString("service", "rabbit");
	}
	
	public int getSecondsPing() {
		return config().getInt("proxyPingSeconds", 10);
	}

	// Bungee threading deprecated only to deter its use.
	@SuppressWarnings("deprecation")
	@Override
	public ThreadFactory getThreadFactory() {
		if (threadFactory_ == null) {
			threadFactory_ = (new ThreadFactoryBuilder())
				.setNameFormat("MercuryBungeeThread #%1$d")
				.setThreadFactory(new GroupedThreadFactory(MercuryBungePlugin.plugin, "MercuryBungeeThreadPool"))
				.build();
		}
		return threadFactory_;
	}

	private File configFile_;
	private ConfigurationProvider configManager_;
	private net.md_5.bungee.config.Configuration config_;
	private ThreadFactory threadFactory_;
}
