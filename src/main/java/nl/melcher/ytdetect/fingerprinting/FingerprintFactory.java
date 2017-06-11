package nl.melcher.ytdetect.fingerprinting;

import lombok.AllArgsConstructor;
import nl.melcher.ytdetect.VideoIdentifier;
import nl.melcher.ytdetect.tui.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Build fingerprints of size {@value WINDOW_SIZE} from a list of segments.
 */
@AllArgsConstructor
public class FingerprintFactory {

	private List<Integer> aduBytes;
	private VideoIdentifier videoIdentifier;

	/**
	 * Size of sliding window frames.
	 */
	public static final int WINDOW_SIZE = 7;

	public List<Fingerprint> build() {
		if (aduBytes.size() < WINDOW_SIZE) {
			throw new RuntimeException("Cannot create fingerprints: there not enough ADUs to fill a single window. Received: "
					+ aduBytes.size() + " Expected: " + WINDOW_SIZE);
		}

		List<Fingerprint> fingerprints = new ArrayList<>();
		int lastIndex = WINDOW_SIZE -1;
		int firstIndex = 0;

		// Create a mapping for assigning 'next' fp's
		Map<Integer, Fingerprint> nextMap = new HashMap<>();

		while(lastIndex < (aduBytes.size() + 1)) {
			List<Integer> sublist = new ArrayList(aduBytes.subList(firstIndex, lastIndex));
			Fingerprint fp = new Fingerprint(sublist, videoIdentifier, firstIndex, lastIndex);
			fingerprints.add(fp);

			// Assign this fp as next to earlier ones
			if(nextMap.containsKey(firstIndex)) {
				nextMap.get(firstIndex).setNext(fp);
			}

			if(nextMap.containsKey(firstIndex - 1 - WINDOW_SIZE)) {
				fp.setPrevious(nextMap.get(firstIndex - 1 - WINDOW_SIZE));
			}

			// Add fp to receive its next
			nextMap.put(lastIndex, fp);

			firstIndex += 1;
			lastIndex += 1;
		}
		return fingerprints;
	}
}
