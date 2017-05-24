package nl.melcher.ytdetect.nfa.event;

import nl.melcher.ytdetect.nfa.NfaState;

import java.util.Set;

/**
 * Created by melcher on 24-5-17.
 */
public class NfaEventHandlerImpl implements NfaEventHandler{
	@Override
	public void stateChanged(Set<NfaState> newState) {
		if(newState.size() == 1) {
			// This NFA terminated!
			System.out.println("Result: " + ((NfaState)(newState.toArray()[0])).getVideoIdentifier().getTitle());
		} else {
			System.out.println(": " + newState);
		}


		System.out.println(newState.size());
	}
}
