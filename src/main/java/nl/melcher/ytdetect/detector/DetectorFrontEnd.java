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
 * Detector front end singleton. Handles incoming segments and controls instances of {@link DetectorBackEnd}.
 * The front end oversees all incoming match information from back ends and constructs a list of video candidates
 * based on this information.
 */
public class DetectorFrontEnd {

	private static final int BONUS = 3;

	private Map<VideoIdentifier, Integer> candidates = new HashMap<>();
	private LinkedList<Integer> segmentSizes = new LinkedList<>();
	private Map<Integer, DetectorBackEnd> nextBackEndMap = new HashMap<>();

	private Map<VideoIdentifier, Integer> segmentOrder = new HashMap<>();
	private List<VideoIdentifier> lastCandidates = new ArrayList<>();

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
			int startIndex = segmentSizes.size() - FingerprintFactory.WINDOW_SIZE + 1;
			int endIndex = segmentSizes.size();
			int size = segmentSizes.subList(startIndex, endIndex).stream().mapToInt(Integer::intValue).sum();

			// Create new back end. Add it to the map and get matches.
			DetectorBackEnd backEnd = new DetectorBackEnd(fingerprints);
			List<Fingerprint> matches = backEnd.findMatches(size);
			List<VideoIdentifier> newCandidates = new ArrayList<>();

			// Save candidates
			for(Fingerprint match : matches) {
				VideoIdentifier videoIdentifier = match.getVideoIdentifier();
				newCandidates.add(videoIdentifier);

				if(candidates.containsKey(videoIdentifier)) {
					candidates.put(videoIdentifier, candidates.get(videoIdentifier) +1);
				} else {
					candidates.put(videoIdentifier, 1);
				}


				if(segmentOrder.containsKey(videoIdentifier)) {
					int lastEndindex = segmentOrder.get(videoIdentifier);
					if (match.getEndIndex() <= lastEndindex) {
						// Discrepancy! Set this video back 1 place.
						candidates.put(videoIdentifier, candidates.get(videoIdentifier) - 1);
					} else if(lastCandidates.contains(videoIdentifier)) {
						if(match.getEndIndex() == lastEndindex + 1) {
							// This is exactly the next expected segment. Bonus!
							candidates.put(videoIdentifier, candidates.get(videoIdentifier) + BONUS);
						}
					}
				} else {
					segmentOrder.put(videoIdentifier, match.getEndIndex());
				}
			}

			lastCandidates.clear();
			lastCandidates.addAll(newCandidates);

			// 'Next' fingerprint mechanism
			if(matches.size() > 0) {
				nextBackEndMap.put(endIndex + (FingerprintFactory.WINDOW_SIZE) , backEnd);
			}

			// Look for ordered next matches
			if(nextBackEndMap.containsKey(startIndex)) {
				Logger.log("!NEXT!");
				DetectorBackEnd nextBackEnd = nextBackEndMap.get(startIndex);
				List<Fingerprint> nextMatches = backEnd.findMatches(size);
			}
		}
	}

	/**
	 * Wrap things up. Report back stats and clear all maps for a fresh start.
	 */
	public void wrap() {
		int total = candidates.values().stream().mapToInt(Integer::intValue).sum();
		Map<Integer, Set<VideoIdentifier>> countMap = new HashMap<>();

		for(Map.Entry<VideoIdentifier, Integer> entry : candidates.entrySet()) {
			int count = entry.getValue();
			Set<VideoIdentifier> vids = countMap.containsKey(count) ? countMap.get(count) : new HashSet<>();
			vids.add(entry.getKey());
			countMap.put(count, vids);
		}

		SortedSet<Integer> sorted = new TreeSet<>(countMap.keySet()).descendingSet();
		for(Integer count: sorted) {
			double percentage = Double.valueOf(count) / total * 100;
			Set<VideoIdentifier> vids = countMap.get(count);
			Logger.log("===================");
			Logger.log(count + " matches, resulting in a " + percentage + "% match");
			for(VideoIdentifier vId : vids) {
				Logger.log(vId.toString());
			}
		}

		// Clear everything
		candidates.clear();
		segmentSizes.clear();
		segmentOrder.clear();
		nextBackEndMap.clear();
	}

	private void calcCoefficient() {

	}
}
