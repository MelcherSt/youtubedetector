package nl.melcher.ytdetect.fingerprinting;

import lombok.AllArgsConstructor;
import nl.melcher.ytdetect.VideoIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Build windows of size {@value WINDOW_SIZE} from a list of segments.
 */
@AllArgsConstructor
public class WindowFactory {

	private List<Integer> segmentBytes;
	private VideoIdentifier videoIdentifier;

	/**
	 * Size of sliding window frames.
	 */
	public static final int WINDOW_SIZE = 7;

	public List<Window> build() {
		if (segmentBytes.size() < WINDOW_SIZE) {
			throw new RuntimeException("Cannot create windows: there not enough segments to fill a single window. Received: "
					+ segmentBytes.size() + " Expected: " + WINDOW_SIZE);
		}

		List<Window> windows = new ArrayList<>();
		int lastIndex = WINDOW_SIZE -1;
		int firstIndex = 0;

		// Create a mapping for assigning 'next' windows
		Map<Integer, Window> nextMap = new HashMap<>();

		while(lastIndex < (segmentBytes.size() + 1)) {
			List<Integer> sublist = new ArrayList(segmentBytes.subList(firstIndex, lastIndex));
			Window fp = new Window(sublist, videoIdentifier, firstIndex, lastIndex);
			windows.add(fp);

			// Assign this window as next to earlier ones
			if(nextMap.containsKey(firstIndex)) {
				nextMap.get(firstIndex).setNext(fp);
			}

			if(nextMap.containsKey(firstIndex - 1 - WINDOW_SIZE)) {
				fp.setPrevious(nextMap.get(firstIndex - 1 - WINDOW_SIZE));
			}

			// Add window to receive its next
			nextMap.put(lastIndex, fp);

			firstIndex += 1;
			lastIndex += 1;
		}
		return windows;
	}
}
