package vg.civcraft.mc.mercury;

import org.bukkit.scheduler.BukkitRunnable;

public class MercuryService extends BukkitRunnable {
	private static MercuryPlugin plugin = MercuryPlugin.instance;
	public String servername;
	public boolean connected = false;
	
	
	public MercuryService(MercuryPlugin plugin){
		enable();
	}
	
	public void destroy(){
		if (connected){
			// lock message queue
			this.shutdown();
		}
	}
	
	// message structure when receiving is: msg,plugin,message
	private void receiveMessage(String message){
		if (message.isEmpty()){return;}
		
		if (message.startsWith("msg,")){
			message = message.substring("msg,".length()-1,message.length());
			final String plugin = message.substring(0, message.indexOf(","));
			final String msg = message.substring(message.indexOf(","), message.length());
			this.plugin.api.queueMessage(plugin, msg);
			
		}		
	}
	
	public void enable(){
		// setup connection & read config from yml
		servername = ""; //load servername from yml
		//setup connection
		
		plugin.api.enable();
	}

	private void shutdown() {
		// close connection
		
		plugin.api.disable();
	}

	@Override
	public void run() {
		
		
	}

	public synchronized void sendMessage(String destination, String plugin, String message) {
		// queue up message to be sent.
		// message structure when sending is: msg,server,plugin,message
		
	}
}
