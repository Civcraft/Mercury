package vg.civcraft.mc.mercury.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonConfiguration implements Configuration {

	public JsonConfiguration(String fileName){
		fileName_ = fileName;
		//parse();
	}

	@Override
	public String getHost(){
		return host_;
	}

	@Override
	public String getPassword(){
		return password_;
	}

	@Override
	public Integer getPort(){
		return port_;
	}

	@Override
	public String getServerName(){
		return serverName_;
	}
	
	@Override
	public String getUserName() {
		return userName;
	}

	@Override
	public String getServiceHandler(){
		return serviceHandler_;
	}

	/*
	private void parse() {
		InputStreamReader isr = null;
		try {
			File file = new File(fileName_);
			if (!file.exists())
				file.createNewFile();
			isr = new InputStreamReader(
					new FileInputStream(file), "UTF-8");
			JSONObject json = new JSONObject(new JSONTokener(isr));

			host_ = getString(json, "host");
			password_ = getString(json, "password");
			port_ = getInt(json, "port");
			serverName_ = getString(json, "server-name");
			serviceHandler_ = getString(json, "service");
			userName = getString(json, "username");
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException ex) {
				}
			}
		}
	}
	*/
	
	public static JsonConfiguration load(File file) {
		Gson gson = new Gson();
		try {
			return (JsonConfiguration) gson.fromJson(new FileReader(file), JsonConfiguration.class);
		} catch (Exception e) {
			return new JsonConfiguration(file.getAbsolutePath());
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

	private String fileName_ = null;
	private String host_ = "localhost";
	private String password_ = "";
	private Integer port_ = 0;
	private String serverName_ = "";
	private String serviceHandler_ = "rabbitmq";
	private String userName = "bukkit";
}
