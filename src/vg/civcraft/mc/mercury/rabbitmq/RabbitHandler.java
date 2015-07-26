package vg.civcraft.mc.mercury.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import vg.civcraft.mc.mercury.MercuryConfigManager;
import vg.civcraft.mc.mercury.MercuryPlugin;
import vg.civcraft.mc.mercury.ServiceHandler;

public class RabbitHandler implements ServiceHandler{

	private Connection con;
	private Channel chan;
	private String queue;
	
	public RabbitHandler(){
		addServerToServerList();
		enableRabbit();
	}
	
	@Override
	public boolean isEnabled() {
		return con != null && con.isOpen() && chan.isOpen();
	}

	@Override
	public void pingService() {
		
	}

	@Override
	public void addServerToServerList() {
		
	}

	@Override
	public void sendMessage(String dest, String message, String... channels) {
		for (String channel: channels)
			try {
				chan.basicPublish(channel, "", null, message.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public void addChannels(String... channels) {
		for (String channel: channels)
			try {
				chan.exchangeDeclare(channel, "fanout");
				chan.queueBind(queue, channel, "");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public void destory() {
		try {
			con.close();
			chan.close();
		} catch (IOException e) {
			e.printStackTrace();
			con = null;
			chan = null;
		} catch (TimeoutException te) {
			te.printStackTrace();
			con = null;
			chan = null;
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
			queue = chan.queueDeclare().getQueue();
			RabbitListenerThread thread = new RabbitListenerThread(chan, queue);
			Bukkit.getScheduler().runTaskAsynchronously(MercuryPlugin.instance, thread);
		} catch (IOException e) {
			e.printStackTrace();
			con = null;
			chan = null;
		} catch (TimeoutException te) {
			te.printStackTrace();
			con = null;
			chan = null;
		}
	}

}
