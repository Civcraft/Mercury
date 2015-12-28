package vg.civcraft.mc.mercury.rabbitmq;

import java.util.HashMap;
import java.util.Map;

import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.config.MercuryConfigManager;
import vg.civcraft.mc.mercury.events.EventManager;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.AMQP.BasicProperties;

public class RabbitConsumer extends DefaultConsumer {

	private final RabbitHandler handler_;
	private final String queueName_;
	private Map<String, String> channelMap_ = new HashMap<String, String>();

	public RabbitConsumer(RabbitHandler handler, Channel channel, String queueName) {
		super(channel);
		handler_ = handler;
		queueName_ = queueName;
	}

	@Override
	public void handleShutdownSignal(java.lang.String consumerTag, ShutdownSignalException sig) {
		// Potential connection loss
		handler_.rabbitmqShutdownTriggered();
	}

	@Override
	public void handleCancel(java.lang.String consumerTag) {
		// Queue or exchange could be deleted
		handler_.rabbitmqShutdownTriggered();
	}

	@Override
	public void handleDelivery(java.lang.String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) {
			String originServer = null;
			final Object originServerObj = properties.getHeaders().get("ORIGIN_SERVER");
			if (originServerObj != null) {
				originServer = originServerObj.toString();
				if (originServer.equals(handler_.serverName())) {
					// Don't deliver a message to oneself
					if (MercuryConfigManager.getDebug()) {
						MercuryAPI.info("Received message from self %s, dropping message", originServer);
					}
					return;
				}
			}
			String channelName = envelope.getExchange();
			if (channelMap_.containsKey(channelName)) {
				channelName = channelMap_.get(channelName);
			} else {
				if (!channelName.startsWith("mc.")) {
					MercuryAPI.info("Bad chan name %s on %s, dropping message", originServer, channelName);
					return;
				}
				final boolean global = channelName.endsWith(".global");
				String newName;
				if (global) {
					newName = channelName.substring(3, channelName.length() - 7);
				} else {
					newName = channelName.substring(3);
				}
				channelMap_.put(channelName, newName);
				channelName = newName;
			}
			String message;
			try {
				message = new String(body, "UTF-8");
			} catch (java.io.UnsupportedEncodingException e) {
				e.printStackTrace();
				MercuryAPI.info("Exception while UTF-8 decoding message from %s on %s, dropping message", originServer, channelName);
				return;
			}
			if (MercuryConfigManager.getDebug()) {
				MercuryAPI.info("Received from %s on %s: %s", originServer, channelName, message);
			}
			EventManager.fireMessage(originServer, channelName, message);
	}
}
