package nl.melcher.ytdetect.detector;

import lombok.Getter;
import nl.melcher.ytdetect.VideoIdentifier;
import nl.melcher.ytdetect.fingerprinting.Fingerprint;
import nl.melcher.ytdetect.fingerprinting.FingerprintFactory;
import nl.melcher.ytdetect.fingerprinting.FingerprintRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Detector front end singleton. Handles incoming segments and controls instances of {@link DetectorBackEnd}.
 * The front end oversees all incoming match information from back ends and constructs a list of video candidates
 * based on this information.
 */
public class DetectorFrontEnd {

	private Map<VideoIdentifier, Integer> candidates = new HashMap<>();
	private LinkedList<Integer> segmentSizes = new LinkedList<>();
	private Map<Integer, DetectorBackEnd> nextBackEndMap = new HashMap<>();

	private Map<VideoIdentifier, Integer> segmentOrder = new HashMap<>();

	@Getter
	public static DetectorFrontEnd instance = new DetectorFrontEnd();

	private DetectorFrontEnd() {
		try {
			FingerprintRepository.deserialize(FingerprintRepository.FILE_NAME);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Push an incoming segment size for detection.
	 * @param segmentSize The size of a segment.
	 */
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

			// Save candidates
			for(Fingerprint match : matches) {
				VideoIdentifier videoIdentifier = match.getVideoIdentifier();

				if(candidates.containsKey(videoIdentifier)) {
					candidates.put(videoIdentifier, candidates.get(videoIdentifier) +1);
				} else {
					candidates.put(videoIdentifier, 1);
				}


				if(segmentOrder.containsKey(videoIdentifier)) {
					int lastEndindex = segmentOrder.get(videoIdentifier);
					if (match.getEndIndex() <= lastEndindex) {
						// Discrepancy! Set this video back 1 place.
						candidates.put(videoIdentifier, candidates.get(videoIdentifier) -1);
					}
				} else {
					segmentOrder.put(videoIdentifier, match.getEndIndex());
				}
			}


			// 'Next' fingerprint mechanism
		/*	if(matches.size() > 0) {
				nextBackEndMap.put(endIndex + FingerprintFactory.WINDOW_SIZE, backEnd);
			}

			// Look for ordered next matches
			if(nextBackEndMap.containsKey(startIndex)) {
				Logger.log("!NEXT!");
				DetectorBackEnd nextBackEnd = nextBackEndMap.get(startIndex);
				List<Fingerprint> nextMatches = backEnd.findMatches(size);
			}*/
		}
	}

	/**
	 * Wrap things up. Report back stats and clear all maps for a fresh start.
	 */
	public void wrap() {
		int total = candidates.values().stream().mapToInt(Integer::intValue).sum();

		System.out.println("Total entries : " + total);
		for(Map.Entry<VideoIdentifier, Integer> entry : candidates.entrySet()) {
			double percentage = Double.valueOf(entry.getValue()) / total * 100;

			System.out.println(entry.getKey().getTitle() + " : " + entry.getValue() + " (" + percentage + "%)");
		}

		// Clear everything
		candidates.clear();
		segmentSizes.clear();
		segmentOrder.clear();
		nextBackEndMap.clear();
	}
}
