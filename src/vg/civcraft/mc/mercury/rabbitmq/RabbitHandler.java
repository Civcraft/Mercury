package vg.civcraft.mc.mercury.rabbitmq;

import java.io.IOException;
import java.util.Arrays;
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
	public static final String EXCHANGE_DEBUG = "mc.debug";

	// Can also lock boundShardChannels_ and boundGlobalChannels_. These can be locked inside of a rabbitLock_ but not outside.
	private static final Object rabbitLock_ = new Object();

	private ConnectionFactory factory_;
	private Connection con_;
	private Channel chan_;
	private String queueName_;
	private Set<String> exchanges_ = new HashSet<>();
	private Set<String> boundGlobalChannels_ = new HashSet<>();
	private Set<String> boundShardChannels_ = new HashSet<>();
	private Map<String, RabbitConsumer> consumers_ = new HashMap<>();
	private String serverName_;
	private ThreadFactory threadFactory_ = null;
	private boolean reconnecting_ = false;

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
		MercuryAPI.info("RabbitMQ handler loaded");
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

	private void publishMessage(String serverName, String channelName, AMQP.BasicProperties msgProps, byte[] message) {
		int retry = 3;
		while (retry > 0) {
			try {
				synchronized (RabbitHandler.rabbitLock_) {
					chan_.basicPublish(
							channelName,               // exchange
							serverName,                // routingKey
							msgProps,
							message);                  // body
				}
				return;
			} catch (IOException e) {
				// Assume the connection died and reconnect. This is the only way to know if the client has lost connection.
				enableRabbit();
				retry--;
			}
		}
		MercuryAPI.err("publishMessage ran out of retries, message lost, unable to re-establish connection to RabbitMQ!");
	}

	@Override
	public void sendMessage(String server, String message, String... pluginChannels) {
		final Map<String, Object> headers = new HashMap<>(1);
		headers.put("ORIGIN_SERVER", serverName_);
		final AMQP.BasicProperties msgProps = (new AMQP.BasicProperties.Builder()).headers(headers).build();
		byte[] byteMsg = null;
		try {
			byteMsg = message.getBytes("UTF-8");
		} catch (java.io.UnsupportedEncodingException ex) {
			MercuryAPI.warn("Message contains unencodable bytes, not sent: %s", message);
			return;
		}

		for (final String pluginChannelName : pluginChannels) {
			final String realChanName = String.format("mc.%s", pluginChannelName);
			publishMessage(server, realChanName, msgProps, byteMsg);
		}
	}

	@Override
	public void sendGlobalMessage(String message, String... pluginChannels) {
		final Map<String, Object> headers = new HashMap<>(1);
		headers.put("ORIGIN_SERVER", serverName_);
		final AMQP.BasicProperties msgProps = (new AMQP.BasicProperties.Builder()).headers(headers).build();
		byte[] byteMsg = null;
		try {
			byteMsg = message.getBytes("UTF-8");
		} catch (java.io.UnsupportedEncodingException ex) {
			MercuryAPI.warn("Message contains unencodable bytes, not sent: %s", message);
			return;
		}

		for (final String pluginChannelName : pluginChannels) {
			final String realChanName = String.format("mc.%s.global", pluginChannelName);
			// Blank server for global message (aka. message routing key)
			publishMessage("", realChanName, msgProps, byteMsg);
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

		// Bind to debug exchange
		chan_.exchangeBind(
				EXCHANGE_DEBUG,      // Dest
				globalExchangeName,  // Src
				"");                 // routingKey

		if (!isGlobalExchange) {
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

			// Bind to debug exchange
			chan_.exchangeBind(
					EXCHANGE_DEBUG,      // Dest
					exchangeName,        // Src
					"");                 // routingKey

			synchronized(boundShardChannels_) {
				boundShardChannels_.add(pluginChannelName);
			}
		} else {
			synchronized(boundGlobalChannels_) {
				boundGlobalChannels_.add(pluginChannelName);
			}
		}
	}

	private void addRMQChannels(boolean isGlobal, String... pluginChannels) {
		boolean connectionLost = false;
		synchronized (RabbitHandler.rabbitLock_) {
			for (final String pluginChannelName : pluginChannels) {
				try {
					registerExchangesFor(isGlobal, pluginChannelName);
				} catch (IOException e) {
					// Must assume the connection was lost and retry. In this case the bound*Channels lists can be leveraged
					//  to register these channels upon reconnection
					connectionLost = true;
					break;
				}
			}
		}
		if (connectionLost) {
			// Load all of these channels into the appropriate bound*Channels and reconnect
			if (isGlobal) {
				synchronized(boundGlobalChannels_) {
					boundGlobalChannels_.addAll(Arrays.asList(pluginChannels));
				}
			} else {
				synchronized(boundShardChannels_) {
					boundShardChannels_.addAll(Arrays.asList(pluginChannels));
				}
			}
			enableRabbit();
		}
	}

	@Override
	public void addChannels(String... pluginChannels) {
		addRMQChannels(false, pluginChannels);
	}

	@Override
	public void addGlobalChannels(String... pluginChannels) {
		addRMQChannels(true, pluginChannels);
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

	/* internal */ void rabbitmqShutdownTriggered() {
		if (reconnecting_) {
			// Consumers would be notified when the connection is closed in enableRabbit()
			return;
		}
		try {
			chan_.close();
		} catch (Exception ex) {}
		enableRabbit();
	}

	private void enableRabbit() {
		boolean alreadyReconnecting = reconnecting_;
		synchronized (RabbitHandler.rabbitLock_) {
			if (alreadyReconnecting) {
				// Assume the pending reconnection this witnessed before waiting was successful
				return;
			}
			reconnecting_ = true;
			if (!exchanges_.isEmpty()) {
				exchanges_ = new HashSet<String>();
			}
			int retry = 5;
			while (retry > 0) {
				if (con_ != null) {
					try {
						// Just try to cleanup the old connection
						con_.close();
						con_ = null;
					} catch (Exception ex) {}
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

					chan_.exchangeDeclare(
							EXCHANGE_DEBUG,      // Exchange name
							"fanout",            // type
							false,               // durable
							true,                // auto-delete
							false,               // internal
							null);               // arguments
					exchanges_.add(EXCHANGE_DEBUG);
					// Re-bind plugin channels
					synchronized(boundGlobalChannels_) {
						for (String chan : boundGlobalChannels_) {
							try {
								registerExchangesFor(true, chan);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					synchronized(boundShardChannels_) {
						for (String chan : boundShardChannels_) {
							try {
								registerExchangesFor(false, chan);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					break;
				} catch (IOException | TimeoutException e) {
					e.printStackTrace();
					con_ = null;
					chan_ = null;
					retry--;
					// This will result in a retry
				}
			}
			if (retry <= 0) {
				MercuryAPI.err("Failed to reconnect to RabbitMQ!");
			}
			reconnecting_ = false;
		}
	}
}
