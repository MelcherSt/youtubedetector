package nl.melcher.ytdetect.fingerprinting;

import lombok.AllArgsConstructor;
import nl.melcher.ytdetect.Config;
import nl.melcher.ytdetect.VideoIdentifier;

import java.util.*;

/**
 * Build windows of size WINDOW_SIZE from a list of segments.
 */
@AllArgsConstructor
public class WindowFactory {
	private List<Integer> segmentBytes;
	private VideoIdentifier videoIdentifier;

	public List<Window> build() {
		if (segmentBytes.size() < Config.WINDOW_SIZE) {
			throw new RuntimeException("Cannot create windows: there not enough segments to fill a single window. Received: "
					+ segmentBytes.size() + " Expected: " + Config.WINDOW_SIZE);
		}

		List<Window> result = new ArrayList<>();
		int firstIndex = 0;
		int lastIndex = Config.WINDOW_SIZE -1;

		// Slide the window along the array of segment sizes
		while(lastIndex < (segmentBytes.size() + 1)) {
			ArrayList sublist = new ArrayList(segmentBytes.subList(firstIndex, lastIndex));
			Window window = new Window(sublist, videoIdentifier, firstIndex, lastIndex);
			result.add(window);
			videoIdentifier.getWindowMap().put(firstIndex, window);
			firstIndex += 1;
			lastIndex += 1;
		}
		return result;
	}
}
