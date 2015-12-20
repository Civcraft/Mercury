package vg.civcraft.mc.mercury.events;

import java.util.HashMap;
import java.util.LinkedList;

public class IndependentEventManager implements EventManagerBase {
	public IndependentEventManager() {}

	@Override
	public void fireMessage(String originServer, String channel, String message) {
		for (EventListener el : listeners_) {
			el.receiveMessage(originServer, channel, message);
		}
		LinkedList<EventListener> listeners = directListeners_.get(channel);
		if (listeners != null) {
			for (EventListener el : listeners) {
				el.receiveMessage(originServer, channel, message);
			}
		}
	}

	@Override
	public void registerListener(EventListener listener) {
		listeners_.add(listener);
	}

	@Override
	public void registerListener(EventListener listener, String ... channels) {
		for (String channel : channels) {
			LinkedList<EventListener> listeners = directListeners_.get(channel);
			if (listeners == null) {
				listeners = new LinkedList<EventListener>();
				directListeners_.put(channel, listeners);
			}
			listeners.add(listener);
		}
	}

	private LinkedList<EventListener> listeners_ = new LinkedList<>();
	// Channel name, Set of listener objects
	private HashMap<String, LinkedList<EventListener>> directListeners_ = new HashMap<>();
}
