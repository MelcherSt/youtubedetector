package nl.melcher.ytdetect.nfa;

import nl.melcher.ytdetect.nfa.event.NfaEventSource;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * NFA Extraordinaire. Wraps around a set of {@link Nfa}.
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
		Iterator<Nfa> i = nfaSet.iterator();
		while(i.hasNext()) {
			Nfa nfa = i.next();
			nfa.next(symbol);

			if(nfa.getGlobalStateCount() == 1) {
				// This NFA terminated!
				System.out.println("Result: " + ((NfaState)(nfa.getGlobalState().toArray()[0])).getVideoIdentifier().getTitle());
			} else {
				System.out.println(": " + nfa.getGlobalState());
			}

			if(nfa.getGlobalStateCount() == 0) {
				// This NFA has no state so remove it
				i.remove();
			}
		}
		System.out.println("=====================");
		return this;
	}
}
