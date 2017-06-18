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

	// Set of symbols
	private SortedMultiset<Integer> symbols = TreeMultiset.create();

	// Map of symbols to state
	private Multimap<Integer, Window> transitions = TreeMultimap.create();

	// Representation of current state
	private List<Window> currentState = new ArrayList<>();

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
	 * @return This NFA instance.
	 */
	public StateMachine apply(int windowSize) {
		// Empty current state
		currentState.clear();

		// Define interval based on min/max TLS overhead -- removes HTTP header overhead as well
		Double sizeMin = (windowSize / Config.TLS_MIN) - (Config.WINDOW_SIZE * Config.HTTP_HEADER_SIZE);
		Double sizeMax = (windowSize / Config.TLS_MAX) - (Config.WINDOW_SIZE * Config.HTTP_HEADER_SIZE);

		// Perform range search and retrieve indices
		SortedMultiset<Integer> stateSymbols = symbols.subMultiset(sizeMin.intValue(), BoundType.OPEN, sizeMax.intValue(), BoundType.OPEN);
		List<Window> expectedWindows = new ArrayList<>();

		Logger.debug("Gen: " + generation + ", Frame size: " + windowSize + ", Range search: (" + sizeMin.intValue() + ", " + sizeMax.intValue() + ")");

		// Find all states associated with the found symbols
		for(Integer symbol : stateSymbols) {
			Logger.debug("Matched key: " + symbol);
			currentState.addAll(transitions.get(symbol));

			// TODO: remove block of debug output
			for(Window wind : transitions.get(symbol)) {
				Logger.debug("Index: " + wind.getStartIndex());
				Logger.debug(wind.getVideoIdentifier().getTitle());
			}

			// Find the next expected state
			transitions.get(symbol).forEach(e -> {
				Window nextWindow = e.getVideoIdentifier().getWindowMap().get(e.getStartIndex() + 1);
				if(nextWindow != null) {
					expectedWindows.add(nextWindow);
				}
			});
		}

		Logger.debug("========================");

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
		return new ArrayList<Window>(currentState);
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
		windows.stream().forEach(e -> { transitions.put(e.getSize(), e);});
		updateSymbols();
	}
}
