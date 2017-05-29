package nl.melcher.ytdetect.fingerprinting;

import nl.melcher.ytdetect.VideoIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by melcher on 29-5-17.
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

		while(lastIndex < (segmentSizeList.size() + 1)) {
			Fingerprint fp = new Fingerprint(segmentSizeList.subList(firstIndex, lastIndex), videoIdentifier, firstIndex, lastIndex);
			fingerprints.add(fp);
			firstIndex += 1;
			lastIndex += 1;
		}

		return fingerprints;
	}



}
