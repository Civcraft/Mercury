package vg.civcraft.mc.mercury.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
/**
 * This event is called whenever a plugin broadcasts a message across servers.
 * @author rourke750
 *
 */
public class PluginBroadcastMessageEvent extends Event{

	private HandlerList handle = new HandlerList();
	private String channel;
	private String message;
	
	public PluginBroadcastMessageEvent(String channel, String message){
		this.channel = channel;
		this.message = message;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handle;
	}
	
	public String getChannel(){
		return channel;
	}
	
	public String getMessage(){
		return message;
	}

}
