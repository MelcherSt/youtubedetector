package nl.melcher.ytdetect.nfa.event;

import nl.melcher.ytdetect.nfa.NfaState;

import java.util.Set;

/**
 * Represents an NFA state event handler
 */
public interface NfaEventHandler {
	void stateChanged(Set<NfaState> newState);
}
