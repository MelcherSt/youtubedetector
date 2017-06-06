package nl.melcher.ytdetect.detector;

import com.google.common.collect.*;
import nl.melcher.ytdetect.fingerprinting.Fingerprint;
import nl.melcher.ytdetect.fingerprinting.FingerprintFactory;
import nl.melcher.ytdetect.tui.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by melcher on 29-5-17.
 */
public class DetectorBackEnd {

	private static final double TLS_MIN = 1.0023;
	private static final double TLS_MAX = 1.0018;
	private static final int HTTP_HEADER = 525;

	private Multimap<Integer, Fingerprint> rangeMap = TreeMultimap.create();
	private SortedMultiset<Integer> keys = TreeMultiset.create();
	private int generation = 0;

	public DetectorBackEnd(List<Fingerprint> fingerprints) {
		populateFingerprints(fingerprints);
	}

	/**
	 * Find all fingerprints best matching the given window size.
	 * @param frameSize
	 * @return
	 */
	public List<Fingerprint> findMatches(int frameSize) {
		// Define interval based on min/max TLS overhead -- removes HTTP header overhead as well
		Double sizeMin = (frameSize / TLS_MIN) - (FingerprintFactory.WINDOW_SIZE * HTTP_HEADER);
		Double sizeMax = (frameSize / TLS_MAX) - (FingerprintFactory.WINDOW_SIZE * HTTP_HEADER);

		Logger.log("========================");
		Logger.log("Gen:" + generation + ", fpDB: " + keys.size());
		Logger.log("Frame size: " + frameSize + ",Range search: (" + sizeMin.intValue() + ", " + sizeMax.intValue() + ")");

		SortedMultiset<Integer> range = keys.subMultiset(sizeMin.intValue(), BoundType.OPEN, sizeMax.intValue(), BoundType.OPEN);
		List<Fingerprint> resultFingerprints = new ArrayList<>();

		for(Integer key : range) {
			Logger.log("Matched key: " + key);

			for(Fingerprint fp : rangeMap.get(key)) {
				Logger.log("Index: " + fp.getEndIndex());
				if (fp.getVideoIdentifier() != null) {
					Logger.log(fp.getVideoIdentifier().toString());
				} else {
					Logger.log("Reference to video is null!");
				}
			}
			resultFingerprints.addAll(rangeMap.get(key));
		}

		Logger.log("========================");

		// Set-up this backend for the next generation

		if(resultFingerprints.size() != 0) {
			List<Fingerprint> nextFingerprints = resultFingerprints.stream()
					.map(e -> { return e.getNext();}).collect(Collectors.toList());
			populateFingerprints(nextFingerprints);
			generation += 1;
		} else {
			generation = -1;
		}

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
