package nl.melcher.ytdetect.detector;

import com.google.common.collect.*;
import nl.melcher.ytdetect.fingerprinting.Fingerprint;
import nl.melcher.ytdetect.fingerprinting.FingerprintFactory;
import nl.melcher.ytdetect.tui.utils.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by melcher on 29-5-17.
 */
public class DetectorBackEnd {

	private static final double TLS_MIN = 1.0025;
	private static final double TLS_MAX = 1.0017;
	private static final int HTTP_HEADER = 525;

	private Multimap<Integer, Fingerprint> rangeMap = TreeMultimap.create();
	private SortedMultiset<Integer> keys = TreeMultiset.create();

	public DetectorBackEnd(List<Fingerprint> fingerprints) {
		for(Fingerprint fp : fingerprints) {
			rangeMap.put(fp.getSize(), fp);
		}
		createIndex();
	}

	/**
	 * Find all fingerprints best matching the given window size.
	 * @param windowSize
	 * @return
	 */
	public List<Fingerprint> findMatches(int windowSize) {
		// Define interval based on min/max TLS overhead -- removes HTTP header overhead as well
		Double sizeMin = (windowSize / TLS_MIN) - (FingerprintFactory.WINDOW_SIZE * HTTP_HEADER);
		Double sizeMax = (windowSize / TLS_MAX) - (FingerprintFactory.WINDOW_SIZE * HTTP_HEADER);

		Logger.log("========================");
		Logger.log("Range search: [" + sizeMin + ", " + sizeMax + "]");

		SortedMultiset<Integer> range = keys.subMultiset(sizeMin.intValue(), BoundType.OPEN, sizeMax.intValue(), BoundType.OPEN);
		List<Fingerprint> resultFingerprints = new ArrayList<>();

		for(Integer key : range) {
			Logger.log("Matched key: " + key);

			for(Fingerprint fp : rangeMap.get(key)) {
				if (fp.getVideoIdentifier() != null) {
					Logger.log(fp.getVideoIdentifier().toString());
				} else {
					Logger.log("Attached video is null");
				}
			}
			resultFingerprints.addAll(rangeMap.get(key));
		}
		Logger.log("========================");
		return resultFingerprints;
	}

	/**
	 * Create the keys index map used for range selection.
	 */
	private void createIndex() {
		keys.addAll(rangeMap.keys());
	}

}
