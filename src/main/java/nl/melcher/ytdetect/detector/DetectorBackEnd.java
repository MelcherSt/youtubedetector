package nl.melcher.ytdetect.detector;

import com.google.common.collect.*;
import nl.melcher.ytdetect.fingerprinting.Fingerprint;
import nl.melcher.ytdetect.fingerprinting.FingerprintFactory;
import nl.melcher.ytdetect.tui.utils.Logger;
import sun.rmi.runtime.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides core segment matching facilities. Does the heavy lifting (range searching etc.).
 */
public class DetectorBackEnd {

	private static final double TLS_MIN = 1.0019;
	private static final double TLS_MAX = 1.0017;
	private static final int HTTP_HEADER = 525;

	private Multimap<Integer, Fingerprint> rangeMap = TreeMultimap.create();
	private SortedMultiset<Integer> keys = TreeMultiset.create();

	public DetectorBackEnd(List<Fingerprint> fingerprints) {
		populateFingerprints(fingerprints);
	}

	/**
	 * Find all fingerprints best matching the given window size.
	 * @param frameSize The total size of the frame.
	 * @return A list of matching fingerprints.
	 */
	public List<Fingerprint> findMatches(int frameSize) {
		// Define interval based on min/max TLS overhead -- removes HTTP header overhead as well
		Double sizeMin = (frameSize / TLS_MIN) - (FingerprintFactory.WINDOW_SIZE * HTTP_HEADER);
		Double sizeMax = (frameSize / TLS_MAX) - (FingerprintFactory.WINDOW_SIZE * HTTP_HEADER);

		SortedMultiset<Integer> range = keys.subMultiset(sizeMin.intValue(), BoundType.OPEN, sizeMax.intValue(), BoundType.OPEN);
		List<Fingerprint> resultFingerprints = new ArrayList<>();

		Logger.log("Frame size: " + frameSize + ", Range search: (" + sizeMin.intValue() + ", " + sizeMax.intValue() + ")");

		for(Integer key : range) {
			Logger.log("Matched key: " + key);

			for(Fingerprint fp : rangeMap.get(key)) {
				Logger.log("Index: " + fp.getEndIndex() + " Vid: " + fp.getVideoIdentifier().getTitle());
			}
			resultFingerprints.addAll(rangeMap.get(key));
		}
		Logger.log("========================");

		return resultFingerprints;
	}

	/**
	 * Create the keys index map used for range selection.
	 */
	private void createIndexes() {
		keys.clear();
		keys.addAll(rangeMap.keys());
	}

	private void populateFingerprints(List<Fingerprint> fingerprints) {
		rangeMap.clear();
		for(Fingerprint fp : fingerprints) {
			if(fp != null) {
				rangeMap.put(fp.getSize(), fp);
			}
		}
		createIndexes();
	}
}
