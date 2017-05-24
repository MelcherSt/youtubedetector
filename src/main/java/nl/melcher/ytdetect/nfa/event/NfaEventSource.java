package nl.melcher.ytdetect.nfa.event;

import nl.melcher.ytdetect.nfa.NfaState;

import java.util.*;

/**
 * Created by melcher on 24-5-17.
 */
public class NfaEventSource {

	private final List<NfaEventHandler> eventHandlers = new ArrayList<>();

	protected NfaEventSource() {}

	public void addHandler(NfaEventHandler eventHandler) {
		eventHandlers.add(eventHandler);
	}

	public void removeHandler(NfaEventHandler eventHandler) {
		eventHandlers.remove(eventHandler);
	}

	protected void onStateChanged(Set<NfaState> newState) {
		for(NfaEventHandler handler : eventHandlers) {
			handler.stateChanged(newState);
		}
	}

}
