package nl.melcher.ytdetect.detector;

import com.google.common.collect.*;
import lombok.Getter;
import nl.melcher.ytdetect.fingerprinting.Window;
import nl.melcher.ytdetect.fingerprinting.WindowFactory;
import nl.melcher.ytdetect.fingerprinting.WindowRepository;
import nl.melcher.ytdetect.tui.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides core ADU segment matching facilities. Does the heavy lifting (range searching etc.).
 */
class DetectorBackEnd {
	private static final double TLS_MIN = 1.0023;
	private static final double TLS_MAX = 1.0017;
	private static final int HTTP_HEADER = 525;

	private Multimap<Integer, Window> symbolStateMap = TreeMultimap.create();
	private SortedMultiset<Integer> symbols = TreeMultiset.create();
	private List<Window> currentState = new ArrayList<>();

	@Getter private int generation = 0;

	public DetectorBackEnd(List<Window> windows) {
		// Set initial state
		updateState(windows);
	}

	public DetectorBackEnd() {
		updateState(WindowRepository.getInstance().getWindows());
	}

	public DetectorBackEnd next(int windowSize) {
		// Define interval based on min/max TLS overhead -- removes HTTP header overhead as well
		Double sizeMin = (windowSize / TLS_MIN) - (WindowFactory.WINDOW_SIZE * HTTP_HEADER);
		Double sizeMax = (windowSize / TLS_MAX) - (WindowFactory.WINDOW_SIZE * HTTP_HEADER);

		// Perform range search and retrieve indices
		SortedMultiset<Integer> stateSymbols = symbols.subMultiset(sizeMin.intValue(), BoundType.OPEN, sizeMax.intValue(), BoundType.OPEN);
		Logger.debug("Frame size: " + windowSize + ", Range search: (" + sizeMin.intValue() + ", " + sizeMax.intValue() + ")");

		currentState.clear();
		List<Window> expectedWindows = new ArrayList<>();

		// Find all states associated with the found symbols
		for(Integer symbol : stateSymbols) {
			Logger.debug("Matched key: " + symbol);
			currentState.addAll(symbolStateMap.get(symbol));
			// Find the next expected state
			symbolStateMap.get(symbol).forEach(e -> {
				Window nextWindow = e.getVideoIdentifier().getWindowMap().get(e.getStartIndex() + 1);
				if(nextWindow != null) {
					expectedWindows.add(nextWindow);
				}
			});

		}

		// Set new state
		updateState(expectedWindows);
		generation += 1;
		Logger.debug("========================");
		return this;
	}

	/**
	 * Retrieve a copy of the state.
	 * @return List of windows
	 */
	public List<Window> getCurrentState() {
		return new ArrayList<Window>(currentState);
	}

	/**
	 * Create the symbols index map used for range selection.
	 */
	private void createIndices() {
		symbols.clear();
		symbols.addAll(symbolStateMap.keys());
	}

	private void updateState(List<Window> windows) {
		symbolStateMap.clear();
		windows.stream().forEach(e -> { symbolStateMap.put(e.getSize(), e);});
		createIndices();
	}
}
