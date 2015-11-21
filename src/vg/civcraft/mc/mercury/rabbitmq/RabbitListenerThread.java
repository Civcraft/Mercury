package vg.civcraft.mc.mercury.rabbitmq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import vg.civcraft.mc.mercury.events.EventManager;

import com.rabbitmq.client.Channel;
// import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class RabbitListenerThread extends Thread {

	private final RabbitHandler parent;
	private final Channel chan;
	private final QueueingConsumer consumer;
	private final String queue;
	private boolean running = false;
	private Map<String, String> channelMap_ = new HashMap<String, String>();

	public RabbitListenerThread(RabbitHandler parent, Channel chan, String queue){
		this.parent = parent;
		this.chan = chan;
		this.queue = queue;
		consumer = new QueueingConsumer(chan);
		try {
			chan.basicConsume(queue, true, consumer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void disable() {
		running = false;
	}

	private void processNextDelivery() {
		try {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery(100);
			if (delivery == null) {
				return;
			}
			String channelName = delivery.getEnvelope().getExchange();
			if (channelMap_.containsKey(channelName)) {
				channelName = channelMap_.get(channelName);
			} else {
				if (!channelName.startsWith("mc.")) {
					return;
				}
				boolean global = channelName.endsWith(".global");
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
				message = new String(delivery.getBody(), "UTF-8");
			} catch (java.io.UnsupportedEncodingException e) {
				e.printStackTrace();
				return;
			}
			EventManager.fireMessage(channelName, message);
		} catch (InterruptedException e) {
			// NOP
		} catch (ShutdownSignalException e) {
			running = false;
		} catch (ConsumerCancelledException e) {
			e.printStackTrace();
			running = false;
		}
	}

	@Override
	public void run() {
		running = true;
		try {
			while(running){
				processNextDelivery();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			if (!parent.notifyThreadException(ex)) {
				throw new RuntimeException("RabbitListenerThread unhandled exception", ex);
			}
		} finally {
			running = false;
		}
	}
}
