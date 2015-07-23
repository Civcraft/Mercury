package vg.civcraft.mc.mercury.rabbitmq;

import java.io.IOException;

import org.bukkit.Bukkit;

import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class RabbitListenerThread implements Runnable{

	private final Channel chan;
	private final QueueingConsumer consumer;
	private final String queue;
	
	public RabbitListenerThread(Channel chan, String queue){
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
	
	@Override
	public void run() {
		while(true){
			try {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				String channel = delivery.getEnvelope().getExchange();
				String message = new String(delivery.getBody());
				AsyncPluginBroadcastMessageEvent event = new AsyncPluginBroadcastMessageEvent(channel, message);
				Bukkit.getPluginManager().callEvent(event);
			} catch (ShutdownSignalException | ConsumerCancelledException
					| InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
