package nl.melcher.ytdetect.detector;

import com.google.common.collect.*;
import nl.melcher.ytdetect.fingerprinting.Fingerprint;
import nl.melcher.ytdetect.fingerprinting.FingerprintFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by melcher on 29-5-17.
 */
public class DetectorBackEnd {

	private static final double TLS_MIN = 1.0019;
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
	public Set<Fingerprint> findBestMatches(int windowSize) {
		// Define interval based on min/max TLS overhead -- removes HTTP header overhead as well
		Double sizeMin = (windowSize / TLS_MIN) - (FingerprintFactory.WINDOW_SIZE * HTTP_HEADER);
		Double sizeMax = (windowSize / TLS_MAX) - (FingerprintFactory.WINDOW_SIZE * HTTP_HEADER);

		SortedMultiset<Integer> range = keys.subMultiset(sizeMin.intValue(), BoundType.CLOSED, sizeMax.intValue(), BoundType.CLOSED);
		Set<Fingerprint> resultFingerprints = new HashSet<>();

		for(Integer key : range) {
			resultFingerprints.addAll(rangeMap.get(key));
		}
		return resultFingerprints;
	}

	/**
	 * Create the keys index map used for range selection.
	 */
	private void createIndex() {
		keys.addAll(rangeMap.keys());
	}

}
