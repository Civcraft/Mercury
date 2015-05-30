package vg.civcraft.mc.mercury;

import java.util.ArrayList;
import java.util.HashMap;

public class MercuryAPI {
	private static MercuryPlugin plugin = MercuryPlugin.instance;
	private static MercuryService service;
	private HashMap<String, ArrayList<String>> plugins = new HashMap<String, ArrayList<String>>();
	private boolean disabled = true;
	
	
	public MercuryAPI(MercuryService service) {
		MercuryAPI.service = service;
	}
	
	public synchronized void queueMessage(String plugin, String message){
		if (plugins.containsKey(plugin)){
			plugins.get(plugins).add(message);
		}
	}

	
	// Sends a message. If destination is empty or self, it will simulate sending to a local plugin.
	// Returns true on success; false if otherwise: empty message; no existing plugin on local message; no connection to server
	public synchronized boolean sendMessage(String destination, String plugin, String message){
		if (disabled){return false;}
		if (message.isEmpty()){return false;}
		if (destination.isEmpty()){destination = service.servername;}
		if (plugin.isEmpty()){plugin = " ";}
		
		if (destination.equals(service.servername)){ //sent to self
			if (plugins.containsKey(plugin)){
				plugins.get(plugin).add(message);
				return true;
			} else {
				return false;
			}
		}

		if (service.connected){
			service.sendMessage(destination,plugin,message);
			return true;
		} else {
			return false;
		}
	}
	
	// Adds a plugins name to plugin mailbox queue
	// returns true on success; false if name taken
	public synchronized boolean registerPlugin(String name){
		if (disabled){return false;}
		if (plugins.containsKey(name)){
			return false;
		} else {
			plugins.put(name, new ArrayList<String>());
			return true;
		}
	}
	
	// Checks a plugins message box for messages
	// returns true if box has messages; false if box is empty or plugin not registered
	public synchronized boolean hasMessages(String name){
		if (plugins.containsKey(name)){
			return !plugins.get(name).isEmpty();
		} else{
			return false;
		}
	}
	
	// Takes the current message box and returns to sender, and replaces it with an empty one
	// returns arraylist<string> with messages; null if box is empty
	public synchronized ArrayList<String> getMessages(String name){
		if (plugins.containsKey(name)){
			final ArrayList<String> oldbox = plugins.get(name);
			plugins.put(name, new ArrayList<String>());
			return oldbox;
		} else {
			return null;
		}
	}


	public synchronized void disable() {
		disabled = true;
		plugins.clear();
	}

	public synchronized void enable() {
		disabled = false;		
	}


}
