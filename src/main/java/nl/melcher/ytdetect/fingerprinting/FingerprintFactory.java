package nl.melcher.ytdetect.fingerprinting;

import nl.melcher.ytdetect.VideoIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Build fingerprints of size {@value WINDOW_SIZE} from a list of segments.
 */
public class FingerprintFactory {

	private List<Integer> segmentSizeList;
	private VideoIdentifier videoIdentifier;

	/**
	 * Size of sliding window frames.
	 */
	public static final int WINDOW_SIZE = 5;

	public FingerprintFactory(List<Integer> segmentSizeList, VideoIdentifier videoIdentifier) {
		this.segmentSizeList = segmentSizeList;
	}

	public List<Fingerprint> build() {
		if (segmentSizeList.size() < WINDOW_SIZE) {
			// That's no good
			throw new RuntimeException("There are less segments than the window size. Cannot create fingerprints.");
		}

		List<Fingerprint> fingerprints = new ArrayList<>();

		int lastIndex = WINDOW_SIZE;
		int firstIndex = 0;

		// Create a mapping for assiging 'next' fp's
		Map<Integer, Fingerprint> nextMap = new HashMap<>();

		while(lastIndex < (segmentSizeList.size() + 1)) {
			Fingerprint fp = new Fingerprint(segmentSizeList.subList(firstIndex, lastIndex), videoIdentifier, firstIndex, lastIndex);
			fingerprints.add(fp);

			if(nextMap.containsKey(firstIndex)) {
				nextMap.get(firstIndex).setNext(fp);
			}

			firstIndex += 1;
			lastIndex += 1;

			nextMap.put(lastIndex, fp);
		}

		return fingerprints;
	}



}
