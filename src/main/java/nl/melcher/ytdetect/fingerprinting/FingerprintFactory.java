package nl.melcher.ytdetect.fingerprinting;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import lombok.AllArgsConstructor;
import nl.melcher.ytdetect.VideoIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Build fingerprints of size {@value WINDOW_SIZE} from a list of segments.
 */
@AllArgsConstructor
public class FingerprintFactory {

	private List<Integer> segmentSizeList;
	private VideoIdentifier videoIdentifier;

	/**
	 * Size of sliding window frames.
	 */
	public static final int WINDOW_SIZE = 7;
	public static final int NEXT_WINDOW_OVERLAP_FACTOR = WINDOW_SIZE / 2;

	public List<Fingerprint> build() {
		if (segmentSizeList.size() < WINDOW_SIZE) {
			throw new RuntimeException("There are less segments than the window size: "
					+ segmentSizeList.size() + "/" + WINDOW_SIZE + ". Cannot create fingerprints.");
		}

		List<Fingerprint> fingerprints = new ArrayList<>();
		int lastIndex = WINDOW_SIZE;
		int firstIndex = 0;

		// Create a mapping for assigning 'next' fp's
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
			nextMap.put(lastIndex + NEXT_WINDOW_OVERLAP_FACTOR, fp);
		}
		return fingerprints;
	}
}
