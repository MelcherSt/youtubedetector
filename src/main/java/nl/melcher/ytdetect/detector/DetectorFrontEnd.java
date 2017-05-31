package nl.melcher.ytdetect.detector;

import lombok.Getter;
import nl.melcher.ytdetect.VideoIdentifier;
import nl.melcher.ytdetect.fingerprinting.Fingerprint;
import nl.melcher.ytdetect.fingerprinting.FingerprintFactory;
import nl.melcher.ytdetect.fingerprinting.FingerprintRepository;
import nl.melcher.ytdetect.tui.utils.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Detector front end. Handles incoming segments. Singleton.
 */
public class DetectorFrontEnd {

	private Set<VideoIdentifier> candidates = new HashSet<>();

	private LinkedList<Integer> segmentSizes = new LinkedList<>();

	private Map<Integer, DetectorBackEnd> nextBackEndMap = new HashMap<>();

	@Getter
	public static DetectorFrontEnd instance = new DetectorFrontEnd();

	private DetectorFrontEnd() {
		try {
			FingerprintRepository.deserialize(FingerprintRepository.FILE_NAME);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void pushSegment(Integer segmentSize) {
		List<Fingerprint> fingerprints = FingerprintRepository.getFingerprints();

		// Push size
		segmentSizes.add(segmentSize);

		if(segmentSizes.size() >= FingerprintFactory.WINDOW_SIZE) {
			// We have at least one complete window. Calculate total size.
			int startIndex = segmentSizes.size() - FingerprintFactory.WINDOW_SIZE;
			int endIndex = segmentSizes.size();
			int size = segmentSizes.subList(startIndex, endIndex).stream().mapToInt(Integer::intValue).sum();

			// Create new back end. Add it to the map and get matches.
			DetectorBackEnd backEnd = new DetectorBackEnd(fingerprints);
			List<Fingerprint> matches = backEnd.findMatches(size);

			if(matches.size() > 0) {
				nextBackEndMap.put(endIndex + FingerprintFactory.WINDOW_SIZE, backEnd);
			}

			// Look for ordered next matches
			if(nextBackEndMap.containsKey(startIndex)) {
				Logger.log("!NEXT!");
				DetectorBackEnd nextBackEnd = nextBackEndMap.get(startIndex);
				List<Fingerprint> nextMatches = backEnd.findMatches(size);
			}
		}
	}
}
