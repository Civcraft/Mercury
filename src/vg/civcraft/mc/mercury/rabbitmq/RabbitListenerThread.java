package vg.civcraft.mc.mercury.rabbitmq;

import java.io.IOException;

import vg.civcraft.mc.mercury.events.EventManager;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class RabbitListenerThread extends Thread{

	private final RabbitHandler parent;
	private final Channel chan;
	private final QueueingConsumer consumer;
	private final String queue;
	private boolean running = false;

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
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			String channel = delivery.getEnvelope().getExchange();
			String message = new String(delivery.getBody());
			EventManager.fireMessage(channel, message);
		} catch (ShutdownSignalException | ConsumerCancelledException
				| InterruptedException e) {
			// TODO Auto-generated catch block
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
