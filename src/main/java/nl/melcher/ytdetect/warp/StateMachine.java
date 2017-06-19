package nl.melcher.ytdetect.warp;

import com.google.common.collect.*;
import lombok.Getter;
import nl.melcher.ytdetect.Config;
import nl.melcher.ytdetect.fingerprinting.Window;
import nl.melcher.ytdetect.fingerprinting.WindowRepository;
import nl.melcher.ytdetect.tui.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * WARP NFA. Provides core window matching facilities.
 */
class StateMachine {

	// Set of accepted symbols
	private final SortedMultiset<Integer> symbols = TreeMultiset.create();

	// Map of symbols to state
	private final Multimap<Integer, Window> transitions = TreeMultimap.create();

	// Representation of current state
	private final List<Window> currentState = new ArrayList<>();

	@Getter private int generation = 0;

	/**
	 * Create NFA and initialize with windows from repository.
	 */
	public StateMachine() {
		updateTransitions(WindowRepository.getInstance().getWindows());
	}

	/**
	 * Apply symbol to update state.
	 * @param windowSize Symbol.
	 * @return This state machine with symbol applied.
	 */
	public StateMachine applySymbol(int windowSize) {
		// Empty current state
		currentState.clear();

		// Define interval based on min/max TLS overhead -- removes HTTP header overhead as well
		Double sizeMin = (windowSize / Config.TLS_MIN) - (Config.WINDOW_SIZE * Config.HTTP_HEADER_SIZE_MAX);
		Double sizeMax = (windowSize / Config.TLS_MAX) - (Config.WINDOW_SIZE * Config.HTTP_HEADER_SIZE_MIN);

		// Perform range search and retrieve indices
		SortedMultiset<Integer> stateSymbols = symbols.subMultiset(sizeMin.intValue(), BoundType.OPEN, sizeMax.intValue(), BoundType.OPEN);
		List<Window> expectedWindows = new ArrayList<>();

		Logger.debugln("Gen: " + generation + ", Frame size: " + windowSize + ", Range search: (" + sizeMin.intValue() + ", " + sizeMax.intValue() + ")");

		// Find all states associated with the found symbols
		for(Integer symbol : stateSymbols) {
			Logger.debugln("Matched key: " + symbol);
			currentState.addAll(transitions.get(symbol));

			for(Window wind : transitions.get(symbol)) {
				Logger.debugln("Index: " + wind.getStartIndex());
				Logger.debugln(wind.getVideoIdentifier().getTitle());

				// Find the next expected state
				Window nextWindow = wind.getVideoIdentifier().getWindowMap().get(wind.getStartIndex() + 1);
				if(nextWindow != null) {
					expectedWindows.add(nextWindow);
				}
			}
		}

		Logger.debugln("========================");

		// Set new state
		updateTransitions(expectedWindows);
		generation += 1;

		return this;
	}

	/**
	 * Retrieve a copy of the current state.
	 * @return List of windows.
	 */
	public List<Window> getState() {
		return new ArrayList<>(currentState);
	}

	/**
	 * Create the symbols list used for range selection.
	 */
	private void updateSymbols() {
		symbols.clear();
		symbols.addAll(transitions.keys());
	}

	/**
	 * Clear out the current transitions and set transitions according to given list of windows.
	 * @param windows List of windows to set as transitions.
	 */
	private void updateTransitions(List<Window> windows) {
		transitions.clear();
		windows.forEach(e -> transitions.put(e.getSize(), e));
		updateSymbols();
	}
}
