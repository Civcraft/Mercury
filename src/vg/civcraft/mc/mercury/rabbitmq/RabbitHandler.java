package vg.civcraft.mc.mercury.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.MercuryPlugin;
import vg.civcraft.mc.mercury.ServiceHandler;
import vg.civcraft.mc.mercury.config.MercuryConfigManager;

public class RabbitHandler implements ServiceHandler{

	private Connection con;
	private Channel chan;
	private String queue;
	private RabbitListenerThread thread = null;

	public RabbitHandler(){
		addServerToServerList();
		enableRabbit();
	}

	@Override
	public boolean isEnabled() {
		return con != null && con.isOpen() && chan != null && chan.isOpen();
	}

	@Override
	public void pingService() {
		MercuryAPI.instance.sendMessage("all", "ping " + MercuryAPI.serverName, "mercury");
	}

	@Override
	public void addServerToServerList() {
		
	}

	@Override
	public void sendMessage(String dest, String message, String... channels) {
		for (String channel: channels)
			try {
				if (!chan.isOpen()) // Incase we somehow loose connection.
					enableRabbit();
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
				if (!chan.isOpen()) // Incase we somehow loose connection.
					enableRabbit();
				chan.exchangeDeclare(channel, "fanout", true);
				chan.queueBind(queue, channel, "");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public void destory() {
		thread.disable();
		try {
			con.close();
			chan.close();
		} catch (IOException e) {
			e.printStackTrace();
			con = null;
			chan = null;
		}
	}

	private void enableRabbit(){
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(MercuryConfigManager.getHost());
		factory.setUsername(MercuryConfigManager.getUserName());
		factory.setPassword(MercuryConfigManager.getPassword());
		factory.setPort(MercuryConfigManager.getPort());
		try {
			con = factory.newConnection();
			chan = con.createChannel();
			queue = chan.queueDeclare().getQueue();
			thread = new RabbitListenerThread(this, chan, queue);
			thread.start();
		} catch (IOException e) {
			e.printStackTrace();
			con = null;
			chan = null;
		}
	}

	// Not for outside usage. Scoped for package level visiblity only for
	//   RabbitListenerThread's use.
	protected boolean notifyThreadException(Exception ex) {
		// TODO: Catastrophic failure condition. Rabbit should be reinitialized or
		//   plugin should be disabled.

		// This is called in the thread. For now let's just return that we
		//   didn't handle it so it rethrows.
		return false;
	}
}
