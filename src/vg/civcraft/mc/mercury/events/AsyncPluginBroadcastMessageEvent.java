package vg.civcraft.mc.mercury.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
/**
 * This event is called whenever a plugin broadcasts a message across servers.
 * @author rourke750
 *
 */
public class AsyncPluginBroadcastMessageEvent extends Event{

	private static final HandlerList handle = new HandlerList();
  private String originServer;
	private String channel;
	private String message;
	
	public AsyncPluginBroadcastMessageEvent(String originServer, String channel, String message){
		this.originServer = originServer;
		this.channel = channel;
		this.message = message;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handle;
	}
	
	public static HandlerList getHandlerList() {
		return handle;
	}

	public String getOriginServer() {
		return originServer;
	}
	
	public String getChannel(){
		return channel;
	}
	
	public String getMessage(){
		return message;
	}

}
