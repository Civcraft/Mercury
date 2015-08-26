package vg.civcraft.mc.mercury.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonConfiguration implements Configuration {
	public JsonConfiguration(){
		fileName_ = "mercury_cfg.json";
		parse();
	}

	public JsonConfiguration(String fileName){
		fileName_ = fileName;
		parse();
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

	private void parse() {
		InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(
					new FileInputStream(new File(fileName_)), "UTF-8");
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

	private String getString(JSONObject json, String key) {
		try {
			return json.getString(key);
		} catch (JSONException ex) {
			return null;
		}
	}

	private Integer getInt(JSONObject json, String key) {
		try {
			return new Integer(json.getInt(key));
		} catch (JSONException ex) {
			return null;
		}
	}

	private String fileName_ = null;
	private String host_ = null;
	private String password_ = null;
	private Integer port_ = null;
	private String serverName_ = null;
	private String serviceHandler_ = null;
	private String userName = null;
}
