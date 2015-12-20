package vg.civcraft.mc.mercury.config;

import java.util.concurrent.ThreadFactory;

public interface Configuration {
	String getHost();
	String getPassword();
	Integer getPort();
	String getServerName();
	String getServiceHandler();
	String getUserName();
	Boolean getDebug();
	ThreadFactory getThreadFactory();
}
