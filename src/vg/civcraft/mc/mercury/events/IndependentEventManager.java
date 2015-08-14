package vg.civcraft.mc.mercury.events;

import java.util.LinkedList;

public class IndependentEventManager implements EventManagerBase {
	public IndependentEventManager() {}

	@Override
	public void fireMessage(String channel, String message) {
		for (EventListener el : listeners_) {
			el.receiveMessage(channel, message);
		}
	}

	@Override
	public void registerListener(EventListener listener) {
		listeners_.add(listener);
	}

	private LinkedList<EventListener> listeners_ = new LinkedList<>();
}
