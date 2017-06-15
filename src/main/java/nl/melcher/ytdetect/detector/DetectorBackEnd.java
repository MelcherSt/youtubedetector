package nl.melcher.ytdetect.detector;

import com.google.common.collect.*;
import nl.melcher.ytdetect.fingerprinting.Window;
import nl.melcher.ytdetect.fingerprinting.WindowFactory;
import nl.melcher.ytdetect.tui.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides core segment matching facilities. Does the heavy lifting (range searching etc.).
 */
class DetectorBackEnd {

	private static final double TLS_MIN = 1.0023;
	private static final double TLS_MAX = 1.0017;
	private static final int HTTP_HEADER = 525;

	private Multimap<Integer, Window> rangeMap = TreeMultimap.create();
	private SortedMultiset<Integer> keys = TreeMultiset.create();

	public DetectorBackEnd(List<Window> windows) {
		populateFingerprints(windows);
	}

	/**
	 * Find all fingerprints best matching the given window size.
	 * @param frameSize The total size of the frame.
	 * @return A list of matching fingerprints.
	 */
	public List<Window> findMatches(int frameSize) {
		// Define interval based on min/max TLS overhead -- removes HTTP header overhead as well
		Double sizeMin = (frameSize / TLS_MIN) - (WindowFactory.WINDOW_SIZE * HTTP_HEADER);
		Double sizeMax = (frameSize / TLS_MAX) - (WindowFactory.WINDOW_SIZE * HTTP_HEADER);

		SortedMultiset<Integer> range = keys.subMultiset(sizeMin.intValue(), BoundType.OPEN, sizeMax.intValue(), BoundType.OPEN);
		List<Window> resultWindows = new ArrayList<>();

		Logger.log("Frame size: " + frameSize + ", Range search: (" + sizeMin.intValue() + ", " + sizeMax.intValue() + ")");

		for(Integer key : range) {
			Logger.log("Matched key: " + key);

			for(Window fp : rangeMap.get(key)) {
				Logger.log("Index: " + fp.getEndIndex() + " Vid: " + fp.getVideoIdentifier().getTitle());
			}
			resultWindows.addAll(rangeMap.get(key));
		}
		Logger.log("========================");

		return resultWindows;
	}

	/**
	 * Create the keys index map used for range selection.
	 */
	private void createIndexes() {
		keys.clear();
		keys.addAll(rangeMap.keys());
	}

	private void populateFingerprints(List<Window> windows) {
		rangeMap.clear();
		for(Window fp : windows) {
			if(fp != null) {
				rangeMap.put(fp.getSize(), fp);
			}
		}
		createIndexes();
	}
}
