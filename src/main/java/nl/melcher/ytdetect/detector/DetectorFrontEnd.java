package nl.melcher.ytdetect.detector;

import lombok.Getter;
import nl.melcher.ytdetect.VideoIdentifier;
import nl.melcher.ytdetect.fingerprinting.Window;
import nl.melcher.ytdetect.fingerprinting.WindowFactory;
import nl.melcher.ytdetect.fingerprinting.WindowRepository;
import nl.melcher.ytdetect.tui.utils.Logger;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.io.IOException;
import java.util.*;

/**
 * Detector front end for a connection. Handles incoming ADUs and controls instances of {@link DetectorBackEnd}.
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

	private Map<VideoIdentifier, Integer> aduSegmentOrder = new HashMap<>();

	/**
	 * List containing all candidates that were found in the last segment push.
	 */
	private List<VideoIdentifier> lastCandidates = new ArrayList<>();

	/**
	 * Push an incoming segment size for detection.
	 * @param segmentSize The size of a segment.
	 */
	public void pushAduSegment(Integer segmentSize) {
		// Push size
		aduBytes.add(segmentSize);

		if(aduBytes.size() >= WindowFactory.WINDOW_SIZE) {
			// We have at least one complete window. Calculate total size.
			int startIndex = aduBytes.size() - (WindowFactory.WINDOW_SIZE - 1);
			int endIndex = aduBytes.size();
			int size = aduBytes.subList(startIndex, endIndex).stream().mapToInt(Integer::intValue).sum();

			// Create new back end. Add it to the map and get matches.
			DetectorBackEnd backEnd = new DetectorBackEnd(WindowRepository.getInstance().getWindows());
			List<Window> matches = backEnd.findMatches(size);
			List<VideoIdentifier> newCandidates = new ArrayList<>();

			// Handle all matches
			for(Window match : matches) {
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

				if(aduSegmentOrder.containsKey(videoIdentifier)) {
					int lastEndindex = aduSegmentOrder.get(videoIdentifier);
					if (match.getEndIndex() <= lastEndindex) {
						// ADU segment completely out of order.
						candidateCountMap.put(videoIdentifier, candidateCountMap.get(videoIdentifier) - 2);
					} else if(lastCandidates.contains(videoIdentifier) && match.getEndIndex() == lastEndindex + 1) {
						// Exact match of expected ADU segment. Reward.
						candidateCountMap.put(videoIdentifier, candidateCountMap.get(videoIdentifier) + BONUS);
					}
				}

				// Update last seen index for this video
				aduSegmentOrder.put(videoIdentifier, match.getEndIndex());
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
		candidateCountMap.entrySet().forEach(e -> { if( e.getValue() < 0) { toBeRemoved.add(e.getKey());}});
		toBeRemoved.forEach(candidateCountMap::remove);

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

		if(aduBytes.size() < WindowFactory.WINDOW_SIZE) {
			Logger.log("No results to report.");
		} else {
			for(Integer aduSegment : aduBytes) {
				System.out.print(aduSegment + ", ");
			}
		}

		calcCoefficient();

		// Clear everything
		candidateCountMap.clear();
		aduBytes.clear();
		aduSegmentOrder.clear();
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
	 * Get a list of non-overlapping window sizes from the ADU input on the detector.
	 * @return
	 */
	private List<Integer> calcWindowsFromInput() {
		List<Integer> result = new ArrayList<>();
		int curWindowBytes = 0;
		int curWindowSize = 0;

		for (Integer adu : aduBytes) {
			curWindowBytes += adu;
			curWindowSize +=1;
			if (curWindowSize == WindowFactory.WINDOW_SIZE) {
				result.add(curWindowBytes);
				curWindowBytes = 0;
				curWindowSize = 0;
			}
		}
		return result;
	}

	/**
	 * Get a list of non-overlapping windows sizes for a video.
	 * @param videoIdentifier
	 * @return
	 */
	private List<Integer> calcWindowsFromVid(VideoIdentifier videoIdentifier) {
		List<Integer> result = new ArrayList<>();
		Window fp = videoIdentifier.getInitWindow();
		result.add(fp.getSize());

		while(fp.hasNext()) {
			Window nextFp = fp.getNext();
			result.add(nextFp.getSize());
			fp = nextFp;
		}
		return result;
	}
}
