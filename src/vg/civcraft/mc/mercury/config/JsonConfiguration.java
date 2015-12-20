package vg.civcraft.mc.mercury.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.concurrent.ThreadFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonConfiguration implements Configuration {

	@Override
	public String getHost(){
		return host;
	}

	@Override
	public String getPassword(){
		return password;
	}

	@Override
	public Integer getPort(){
		return port;
	}

	@Override
	public String getServerName(){
		return servername;
	}
	
	@Override
	public String getUserName() {
		return username;
	}

	@Override
	public String getServiceHandler(){
		return service;
	}

	@Override
	public Boolean getDebug(){
		return "true".equalsIgnoreCase(debug) || "1".equals(debug)
			|| "yes".equalsIgnoreCase(debug) || "enable".equalsIgnoreCase(debug);
	}

	@Override
	public ThreadFactory getThreadFactory() {
		return null;
	}

	public static JsonConfiguration load(File file) {
		Gson gson = new Gson();
		try {
			return (JsonConfiguration) gson.fromJson(new FileReader(file), JsonConfiguration.class);
		} catch (Exception e) {
			return new JsonConfiguration();
		}
	}
	
	public void save(File file) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			String json = gson.toJson(this);
			
			FileWriter fw = new FileWriter(file);
			fw.write(json);
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String service = "rabbit";
	private String servername = "";
	private String host = "localhost";
	private Integer port = 5672;
	private String username = "bukkit";
	private String password = "";
	private String debug = "";
}
