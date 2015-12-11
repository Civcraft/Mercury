package vg.civcraft.mc.mercury.rabbitmq;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
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

	private ConnectionFactory factory_;
	private Connection con_;
	private Channel chan_;
	private String queueName_;
	private Set<String> exchanges_ = new HashSet<>();
	private Set<String> boundGlobalExchanges_ = new HashSet<>();
	private Set<String> boundShardExchanges_ = new HashSet<>();
	private Map<String, RabbitConsumer> consumers_ = new HashMap<>();
	private String serverName_;
	private ThreadFactory threadFactory_ = null;

	public RabbitHandler() {
		this(null);
	}

	public RabbitHandler(ThreadFactory threadFactory) {
		threadFactory_ = threadFactory;
		serverName_ = MercuryAPI.serverName();
		createFactory();
		enableRabbit();
		addGlobalChannels("mercury");
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
		sendGlobalMessage(String.format("ping|%s", serverName_), "mercury");
	}

	@Override
	public void addServerToServerList() {}

	@Override
	public void sendMessage(String server, String message, String... pluginChannels) {
		for (final String pluginChannelName : pluginChannels) {
			final String realChanName = String.format("mc.%s", pluginChannelName);
			try {
				if (!chan_.isOpen()) // Incase we somehow loose connection.
					enableRabbit();
				Map<String, Object> headers = new HashMap<>(1);
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
				Map<String, Object> headers = new HashMap<>(1);
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

	private void registerExchangesFor(boolean isGlobalExchange, String pluginChannelName) throws IOException {
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

		if (!isGlobalExchange) {
			chan_.queueBind(queueName_, globalExchangeName, "");
			boundGlobalExchanges_.add(globalExchangeName);

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
			boundShardExchanges_.add(exchangeName);
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
	public void addGlobalChannels(String... pluginChannels) {
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

	private void createFactory() {
		factory_ = new ConnectionFactory();
		factory_.setAutomaticRecoveryEnabled(true);
		factory_.setNetworkRecoveryInterval(1000);  // 1sec
		factory_.setHost(MercuryConfigManager.getHost());
		factory_.setUsername(MercuryConfigManager.getUserName());
		factory_.setPassword(MercuryConfigManager.getPassword());
		factory_.setPort(MercuryConfigManager.getPort());
		if (threadFactory_ != null) {
			factory_.setThreadFactory(threadFactory_);
		}
	}

	private void enableRabbit() {
		if (!exchanges_.isEmpty()) {
			exchanges_ = new HashSet<String>();
		}
		try {
			con_ = factory_.newConnection();
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
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
			con_ = null;
			chan_ = null;
		}
		// Re-bind exchanges
		for (String exch : boundGlobalExchanges_) {
			try {
				registerExchangesFor(true, exch);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (String exch : boundShardExchanges_) {
			try {
				registerExchangesFor(false, exch);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
