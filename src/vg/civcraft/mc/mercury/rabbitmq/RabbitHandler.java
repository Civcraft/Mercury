package vg.civcraft.mc.mercury.rabbitmq;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
	private Set<String> exchanges_ = new HashSet<>();
	private Map<String, RabbitConsumer> consumers_ = new HashMap<>();
	private String serverName_;

	public RabbitHandler() {
		serverName_ = MercuryAPI.serverName;
		enableRabbit();
		addServerToServerList();
	}

	public String serverName() {
		return serverName_;
	}

	@Override
	public void destory() {}

	@Override
	public boolean isEnabled() {
		return con_ != null && con_.isOpen() && chan_ != null && chan_.isOpen();
	}

	@Override
	public void pingService() {
		sendMessage(serverName_, "ping", "mercury.ping");
	}

	@Override
	public void addServerToServerList() {
		sendGlobalMessage(serverName_, "mercury.newserver");
	}

	@Override
	public void sendMessage(String server, String message, String... pluginChannels) {
		for (final String pluginChannelName : pluginChannels) {
			final String realChanName = String.format("mc.%s", pluginChannelName);
			try {
				if (!chan_.isOpen()) // Incase we somehow loose connection.
					enableRabbit();
				Map<String, Object> headers = new HashMap(1);
				headers.put("ORIGIN_SERVER", serverName_);
				chan_.basicPublish(
						realChanName,                // exchange
						server,                      // routingKey
						(new AMQP.BasicProperties.Builder())  // props
								.headers(headers)
								.build(),
						message.getBytes("UTF-8"));  // body
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
				Map<String, Object> headers = new HashMap(1);
				headers.put("ORIGIN_SERVER", serverName_);
				chan_.basicPublish(
						realChanName,                // exchange
						""    ,                      // routingKey
						(new AMQP.BasicProperties.Builder())  // props
								.headers(headers)
								.build(),
						message.getBytes("UTF-8"));  // body
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void registerExchangesFor(boolean broadcastOnly, String pluginChannelName) throws IOException {
		// Global message exchange
		String exchangeName = "mc." + pluginChannelName;
		String globalExchangeName = exchangeName + ".global";
		chan_.exchangeDeclare(
				globalExchangeName,  // Exchange name
				"fanout",            // type
				false,               // durable
				true,                // auto-delete
				false,               // internal
				null);               // arguments
		exchanges_.add(globalExchangeName);

		if (!broadcastOnly) {
			chan_.queueBind(queueName_, globalExchangeName, "");

			// Direct message exchange
			chan_.exchangeDeclare(
					exchangeName,  // Exchange name
					"direct",      // type
					false,         // durable
					true,          // auto-delete
					false,         // internal
					null);         // arguments
			chan_.queueBind(queueName_, exchangeName, serverName_);
			exchanges_.add(exchangeName);
		}
	}

	@Override
	public void addChannels(String... pluginChannels) {
		for (final String pluginChannelName : pluginChannels)
			try {
				if (!chan_.isOpen()) // Incase we somehow loose connection.
					enableRabbit();
				registerExchangesFor(false, pluginChannelName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public void addBroadcastOnlyChannels(String... pluginChannels) {
		for (final String pluginChannelName : pluginChannels)
			try {
				if (!chan_.isOpen()) // Incase we somehow loose connection.
					enableRabbit();
				registerExchangesFor(true, pluginChannelName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private void enableRabbit() {
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
			RabbitConsumer consumer = new RabbitConsumer(this, chan_, queueName_);
			chan_.basicConsume(queueName_, true, consumer);
			consumers_.put(queueName_, consumer);
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
