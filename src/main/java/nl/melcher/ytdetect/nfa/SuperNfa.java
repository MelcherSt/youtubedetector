package nl.melcher.ytdetect.nfa;

import java.util.HashSet;
import java.util.Set;

/**
 * NFA Extraordinaire. Wraps around a set of {@link Nfa}
 */
public class SuperNfa {

	private final NfaState startState;

	private final Set<Nfa> nfaSet = new HashSet<>();

	public SuperNfa(NfaState startState) {
		this.startState = startState;
	}

	public SuperNfa(Nfa nfa) {
		this.startState = nfa.getStartState();
	}

	/**
	 * Apply symbol to current NFA state and initiate NFA starting from this symbol.
	 * @param symbol Applied symbol.
	 * @return this.
	 */
	public SuperNfa next(Integer symbol) {
		// Create a new NFA starting at symbol
		nfaSet.add(new Nfa(startState));

		// Iterate and apply symbol transition
		for(Nfa nfa : nfaSet) {
			nfa.next(symbol);

			if(nfa.getGlobalStateCount() == 1) {
				// This NFA terminated!
				System.out.println(nfa.hashCode() + "Result: " + ((NfaState)(nfa.getGlobalState().toArray()[0])).getVideoIdentifier().getTitle());
			} else {
				System.out.println(nfa.hashCode() + ": " + nfa.getGlobalState());
			}

			if(nfa.getGlobalStateCount() == 0) {
				// This NFA has no state
				nfaSet.remove(nfa);
			}
		}
		return this;
	}
}
