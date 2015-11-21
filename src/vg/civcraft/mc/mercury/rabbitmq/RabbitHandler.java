package vg.civcraft.mc.mercury.rabbitmq;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.ServiceHandler;
import vg.civcraft.mc.mercury.config.MercuryConfigManager;

public class RabbitHandler implements ServiceHandler {

	private Connection con_;
	private Channel chan_;
	private String queueName_;
	private Set<String> exchanges_ = new HashSet<String>();
	private String serverName_;
	private RabbitListenerThread thread_ = null;

	public RabbitHandler() {
		serverName_ = MercuryAPI.serverName;
		addServerToServerList();
		enableRabbit();
	}

	@Override
	public boolean isEnabled() {
		return con_ != null && con_.isOpen() && chan_ != null && chan_.isOpen();
	}

	@Override
	public void pingService() {
		MercuryAPI.instance.sendMessage(serverName_, "ping", "mercury.ping");
	}

	@Override
	public void addServerToServerList() {
		MercuryAPI.instance.sendGlobalMessage(serverName_, "mercury.newserver");
	}

	@Override
	public void sendMessage(String server, String message, String... pluginChannels) {
		for (final String pluginChannelName : pluginChannels) {
			final String realChanName = String.format("mc.%s", pluginChannelName);
			try {
				if (!chan_.isOpen()) // Incase we somehow loose connection.
					enableRabbit();
				chan_.basicPublish(realChanName, server, null, message.getBytes("UTF-8"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void sendGlobalMessage(String message, String... pluginChannels) {
		for (final String pluginChannelName : pluginChannels) {
			final String realChanName = String.format("mc.%s.global", pluginChannelName);
			try {
				if (!chan_.isOpen()) // Incase we somehow loose connection.
					enableRabbit();
				chan_.basicPublish(realChanName, "", null, message.getBytes("UTF-8"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void registerExchangesFor(String pluginChannelName) throws IOException {
		// Direct message exchange
		String exchangeName = "mc." + pluginChannelName;
		chan_.exchangeDeclare(
				exchangeName,  // Exchange name
				"direct",      // type
				false,         // durable
				true,          // auto-delete
				false,         // internal
				null);         // arguments
		chan_.queueBind(queueName_, exchangeName, serverName_);
		exchanges_.add(exchangeName);

		// Global message exchange
		exchangeName = pluginChannelName + ".global";
		chan_.exchangeDeclare(
				exchangeName,  // Exchange name
				"fanout",      // type
				false,         // durable
				true,          // auto-delete
				false,         // internal
				null);         // arguments
		chan_.queueBind(queueName_, exchangeName, "");
		exchanges_.add(exchangeName);
	}

	@Override
	public void addChannels(String... pluginChannels) {
		for (final String pluginChannelName : pluginChannels)
			try {
				if (!chan_.isOpen()) // Incase we somehow loose connection.
					enableRabbit();
				registerExchangesFor(pluginChannelName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public void destory() {
		thread_.disable();
		try {
			thread_.join();
		} catch (InterruptedException e) {}
		try {
			con_.close();
			chan_.close();
		} catch (IOException e) {
			e.printStackTrace();
			con_ = null;
			chan_ = null;
		}
	}

	private void enableRabbit(){
		if (!exchanges_.isEmpty()) {
			exchanges_ = new HashSet<String>();
		}
		ConnectionFactory factory = new ConnectionFactory();
		factory.setAutomaticRecoveryEnabled(true);
		factory.setNetworkRecoveryInterval(1000);  // 1sec
		factory.setHost(MercuryConfigManager.getHost());
		factory.setUsername(MercuryConfigManager.getUserName());
		factory.setPassword(MercuryConfigManager.getPassword());
		factory.setPort(MercuryConfigManager.getPort());
		try {
			con_ = factory.newConnection();
			chan_ = con_.createChannel();
			queueName_ = String.format("mc.shard.%s", serverName_);
			chan_.queueDeclare(
				queueName_,
				false,  // durable
				true,   // exclusive
				true,   // auto-delete
				null    // queue properties
				);
			if (thread_ != null) {
				thread_.disable();
				try {
					thread_.join();
				} catch (InterruptedException e) {}
			}
			thread_ = new RabbitListenerThread(this, chan_, queueName_);
			thread_.start();
		} catch (IOException e) {
			e.printStackTrace();
			con_ = null;
			chan_ = null;
		}
		addChannels("mercury.ping", "mercury.newserver");
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
