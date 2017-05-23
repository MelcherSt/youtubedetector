package nl.melcher.ytdetect.nfa;

import lombok.Getter;

import java.util.*;

/**
 * Models a simple NFA. Wraps around a set of {@link nl.melcher.ytdetect.nfa.NfaState}.
 */
public class Nfa {

	/**
	 * The NFA start state.
	 */
	@Getter
	private final NfaState startState;

	/**
	 * The NFA state.
	 */
	private Set<NfaState> globalState = new HashSet<>();

	public Nfa(NfaState startState) {
		// Save the start for possible reset
		this.startState = startState;

		// Add start state to current state
		globalState.add(startState);
	}

	/**
	 * Reset global state and start over from start state.
	 */
	public void reset() {
		globalState.clear();
		globalState.add(startState);
	}

	/**
	 * Apply symbol to global NFA state.
	 * @param symbol Applied symbol.
	 * @return this.
	 */
	public Nfa next(Integer symbol) {
		Set<NfaState> currentState = new HashSet<>();
		for(NfaState state : globalState) {
			Set<NfaState> localState = state.next(symbol);
			if(localState != null) {
				currentState.addAll(localState);
			}
		}
		globalState = currentState;
		return this;
	}

	/**
	 * Retrieve an exact copy of the current state.
	 * @return Unmodifiable state set.
	 */
	public Set<NfaState> getGlobalState() {
		return Collections.unmodifiableSet(globalState);
	}

	/**
	 * Retrieve the count of states the NFA is currently in.
	 * @return
	 */
	public Integer getGlobalStateCount() {
		return globalState.size();
	}


}
