package vg.civcraft.mc.mercury.rabbitmq;

import java.io.IOException;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import vg.civcraft.mc.mercury.MercuryConfigManager;
import vg.civcraft.mc.mercury.ServiceHandler;

public class RabbitHandler implements ServiceHandler{

	private Connection con;
	private Channel chan;
	
	public RabbitHandler(){
		addServerToServerList();
		enableRabbit();
	}
	
	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void pingService() {
		
	}

	@Override
	public void addServerToServerList() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessage(String dest, String message, String... channels) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addChannels(JavaPlugin plugin, String... channels) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destory() {
		try {
			con.close();
			chan.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void enableRabbit(){
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost(MercuryConfigManager.getHost());
	    factory.setPassword(MercuryConfigManager.getPassword());
	    factory.setPort(MercuryConfigManager.getPort());
	    try {
			con = factory.newConnection();
			chan = con.createChannel();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
