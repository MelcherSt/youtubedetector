package nl.melcher.ytdetect.fingerprinting;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import nl.melcher.ytdetect.VideoIdentifier;

import java.util.ArrayList;
import java.util.List;

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

	public FingerprintFactory(ArrayList<Integer> segmentSizeList, VideoIdentifier videoIdentifier) {
		this.segmentSizeList = segmentSizeList;
	}

	public List<Fingerprint> build() {
		if (segmentSizeList.size() < WINDOW_SIZE) {
			// That's no good
			throw new RuntimeException("There are less (" + segmentSizeList.size() + ") segments than the window size. Cannot create fingerprints.");
		}

		List<Fingerprint> fingerprints = new ArrayList<>();

		int lastIndex = WINDOW_SIZE;
		int firstIndex = 0;

		// Create a mapping for assiging 'next' fp's
		Multimap<Integer, Fingerprint> nextMap = TreeMultimap.create();

		while(lastIndex < (segmentSizeList.size() + 1)) {
			List<Integer> sublist = new ArrayList(segmentSizeList.subList(firstIndex, lastIndex));
			Fingerprint fp = new Fingerprint(sublist, videoIdentifier, firstIndex, lastIndex);
			fingerprints.add(fp);

			// Assign this fp as next to earlier ones
			if(nextMap.containsKey(firstIndex)) {
				nextMap.get(firstIndex).forEach(e -> e.setNext(fp));
			}

			firstIndex += 1;
			lastIndex += 1;

			// Add fp to receive its next
			nextMap.put(lastIndex, fp);
		}

		return fingerprints;
	}



}
