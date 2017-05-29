package nl.melcher.ytdetect.detector;

import lombok.Getter;
import nl.melcher.ytdetect.VideoIdentifier;
import nl.melcher.ytdetect.fingerprinting.FingerprintFactory;

import java.util.*;

/**
 * Detector front end. Handles incoming segments. Singleton.
 */
public class DetectorFrontEnd {

	private Set<VideoIdentifier> candidates = new HashSet<>();

	private LinkedList<Integer> segmentSizes = new LinkedList<>();

	@Getter
	public static DetectorFrontEnd instance = new DetectorFrontEnd();

	private DetectorFrontEnd() {}


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
