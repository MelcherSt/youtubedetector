package nl.melcher.ytdetect.detector;

import lombok.Getter;
import nl.melcher.ytdetect.VideoIdentifier;
import nl.melcher.ytdetect.adu.AduDumpLine;
import nl.melcher.ytdetect.fingerprinting.Fingerprint;
import nl.melcher.ytdetect.fingerprinting.FingerprintFactory;
import nl.melcher.ytdetect.fingerprinting.FingerprintRepository;
import nl.melcher.ytdetect.tui.utils.Logger;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.io.IOException;
import java.util.*;

/**
 * Detector front end singleton. Handles incoming ADUs and controls instances of {@link DetectorBackEnd}.
 * The front end oversees all incoming match information from back ends and constructs a list of video candidateCountMap
 * based on this information.
 */
public class DetectorFrontEnd {

	private static final int BONUS = 4;

	/**
	 * List of incoming ADU sizes in bytes including any HTTP and/or TLS overhead still.
	 */
	private List<Integer> aduBytes = new ArrayList<>();

	/**
	 * Mapping of found candidate videos to their # of occurrence.
	 */
	private Map<VideoIdentifier, Integer> candidateCountMap = new HashMap<>();

	private Map<VideoIdentifier, Integer> aduOrder = new HashMap<>();

	/**
	 * List containing all candidates that were found in the last segment push.
	 */
	private List<VideoIdentifier> lastCandidates = new ArrayList<>();

	@Getter	public static DetectorFrontEnd instance = new DetectorFrontEnd();

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
	public void pushAduSegment(Integer segmentSize) {
		// Push size
		aduBytes.add(segmentSize);

		if(aduBytes.size() >= FingerprintFactory.WINDOW_SIZE) {
			// We have at least one complete window. Calculate total size.
			int startIndex = aduBytes.size() - (FingerprintFactory.WINDOW_SIZE - 1);
			int endIndex = aduBytes.size();
			int size = aduBytes.subList(startIndex, endIndex).stream().mapToInt(Integer::intValue).sum();

			// Create new back end. Add it to the map and get matches.
			DetectorBackEnd backEnd = new DetectorBackEnd(FingerprintRepository.getFingerprints());
			List<Fingerprint> matches = backEnd.findMatches(size);
			List<VideoIdentifier> newCandidates = new ArrayList<>();

			// Save candidateCountMap
			for(Fingerprint match : matches) {
				VideoIdentifier videoIdentifier = match.getVideoIdentifier();

				if(newCandidates.contains(videoIdentifier)) {
					// Do not process multiple matches for same video on single window
					break;
				}
				newCandidates.add(videoIdentifier);


				if(candidateCountMap.containsKey(videoIdentifier)) {
					candidateCountMap.put(videoIdentifier, candidateCountMap.get(videoIdentifier) +1);
				} else {
					candidateCountMap.put(videoIdentifier, 1);
				}

				if(aduOrder.containsKey(videoIdentifier)) {
					int lastEndindex = aduOrder.get(videoIdentifier);
					if (match.getEndIndex() <= lastEndindex) {
						// This ADU is completely out of order. Discrepancy detected!
						candidateCountMap.put(videoIdentifier, candidateCountMap.get(videoIdentifier) - 2);
					} else if(lastCandidates.contains(videoIdentifier)) {
						if(match.getEndIndex() == lastEndindex + 1) {
							// This is exactly the ADU that is expected. Reward!
							candidateCountMap.put(videoIdentifier, candidateCountMap.get(videoIdentifier) + BONUS);
						}
					}
				} else {
					aduOrder.put(videoIdentifier, match.getEndIndex());
				}
			}

			// Empty and add all candidates found in this step
			lastCandidates.clear();
			lastCandidates.addAll(newCandidates);
		}
	}

	/**
	 * Wrap things up. Report back stats and clear all maps for a fresh start.
	 */
	public void wrap() {
		List<VideoIdentifier> toBeRemoved = new ArrayList<>();
		candidateCountMap.entrySet().stream().forEach(e -> { if( e.getValue() < 0) { toBeRemoved.add(e.getKey());}});
		toBeRemoved.stream().forEach(e -> { candidateCountMap.remove(e);});

		int total = candidateCountMap.values().stream().mapToInt(Integer::intValue).sum();
		Map<Integer, Set<VideoIdentifier>> countMap = new HashMap<>();

		for(Map.Entry<VideoIdentifier, Integer> entry : candidateCountMap.entrySet()) {
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

		if(aduBytes.size() < FingerprintFactory.WINDOW_SIZE) {
			Logger.log("No results to report.");
		}

		calcCoefficient();

		// Clear everything
		candidateCountMap.clear();
		aduBytes.clear();
		aduOrder.clear();
	}

	private void calcCoefficient() {
		// Prepare
		List<Integer> windowBytes = calcWindowsFromInput();
		Map<VideoIdentifier, List<Integer>> vidWindowBytesMap = new HashMap<>();
		for (VideoIdentifier vid : candidateCountMap.keySet()) {
			vidWindowBytesMap.put(vid, calcWindowsFromVid(vid));
		}

		/* Next part is only for debugging purposes */
		StringBuilder sb = new StringBuilder();
		for(Integer i : windowBytes) {
			sb.append(i + ",");
		}
		Logger.log(sb.toString());

		/* end */

		// Calculate pearson's value
		PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();

		for(Map.Entry<VideoIdentifier, List<Integer>> entry : vidWindowBytesMap.entrySet()) {
			sb = new StringBuilder(entry.getKey().toString() + ": ");
			for(Integer i : entry.getValue()) {
				sb.append(i + ",");
			}
			Logger.log(sb.toString());

			List<Integer> vidWindowBytes = entry.getValue();
			if(vidWindowBytes.size() < windowBytes.size()) {
				continue;
			}

			int startIndex = 0;

			while(windowBytes.size() + startIndex < vidWindowBytes.size()) {
				List<Integer> subVidWindowBytes = vidWindowBytes.subList(startIndex, windowBytes.size() + startIndex);

				double corell = pearsonsCorrelation.correlation(windowBytes.stream().mapToDouble(Integer::doubleValue).toArray(),
						subVidWindowBytes.stream().mapToDouble(Integer::doubleValue).toArray());
				Logger.log(entry.getKey() + " Corell: " + corell);
				startIndex += 1;
			}



		}

	}

	/**
	 * Get a list of non-overlapping window sizes from the ADU input.
	 * @return
	 */
	private List<Integer> calcWindowsFromInput() {
		List<Integer> result = new ArrayList<>();
		int curWindowBytes = 0;
		int curWindowSize = 0;

		for (Integer adu : aduBytes) {
			curWindowBytes += adu;
			curWindowSize +=1;
			if (curWindowSize == FingerprintFactory.WINDOW_SIZE) {
				result.add(curWindowBytes);
				curWindowBytes = 0;
				curWindowSize = 0;
			}
		}
		return result;
	}

	private List<Integer> calcWindowsFromVid(VideoIdentifier videoIdentifier) {
		List<Integer> result = new ArrayList<>();
		Fingerprint fp = videoIdentifier.getInitFingerprint();
		result.add(fp.getSize());

		while(fp.hasNext()) {
			Fingerprint nextFp = fp.getNext();
			result.add(nextFp.getSize());
			fp = nextFp;
		}
		return result;
	}
}
