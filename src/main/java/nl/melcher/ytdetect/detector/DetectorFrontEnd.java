package nl.melcher.ytdetect.detector;

import nl.melcher.ytdetect.VideoIdentifier;
import nl.melcher.ytdetect.fingerprinting.FingerprintFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Detector front end. Handles incoming segment sizes.
 */
public class DetectorFrontEnd {

	private List<VideoIdentifier> candidates = new ArrayList<>();

	private LinkedList<Integer> segmentSizes = new LinkedList<>();

	public void pushSegment(Integer segmentSize) {
		// Push size
		segmentSizes.push(segmentSize);

		if(segmentSizes.size() >= FingerprintFactory.WINDOW_SIZE) {
			// We have at least one complete window. Calculate total size.
			int size = segmentSizes
					.subList(segmentSizes.size() - FingerprintFactory.WINDOW_SIZE, segmentSizes.size())
					.stream().mapToInt(Integer::intValue).sum();

			// Send size to back end
			//TODO: do back end things here...
		}
	}
}
