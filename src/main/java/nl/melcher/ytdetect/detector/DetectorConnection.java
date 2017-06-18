package nl.melcher.ytdetect.detector;

import nl.melcher.ytdetect.Config;
import nl.melcher.ytdetect.VideoIdentifier;
import nl.melcher.ytdetect.fingerprinting.Window;
import nl.melcher.ytdetect.fingerprinting.WindowFactory;
import nl.melcher.ytdetect.tui.utils.Logger;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.util.*;

/**
 * Represents a connection. Handles incoming ADUs and controls instances of {@link DetectorNFA}.
 * The {@link DetectorConnection} oversees all reported window matches from back ends and constructs
 * a list of candidates based on this information.
 */
public class DetectorConnection {

	private static final int BONUS = 2;

	/**
	 * List of incoming ADU sizes in bytes including any HTTP and/or TLS overhead still.
	 */
	private List<Integer> aduBytes = new ArrayList<>();

	/**
	 * Identifier for the connection this detector works on.
	 */
	private String connectionAddr;

	/**
	 * Mapping candidate videos to their occurrence.
	 */
	private Map<VideoIdentifier, Integer> candidateCountMap = new HashMap<>();

	/**
	 * List of all currently active detectors
	 */
	private List<DetectorNFA> backEndList = new ArrayList<>();

	public DetectorConnection(String connectionAddr) {
		this.connectionAddr = connectionAddr;
	}

	/**
	 * Push adudump input line and process.
	 * @param segmentSize The size of an incoming video segment in bytes.
	 */
	public void pushSegment(int segmentSize) {
		// Process ADU segment
		aduBytes.add(segmentSize);
		if(aduBytes.size() >= Config.WINDOW_SIZE) {
			// We have at least one complete window. Calculate total size.
			int startIndex = aduBytes.size() - (Config.WINDOW_SIZE - 1);
			int endIndex = aduBytes.size();
			int windowSize = aduBytes.subList(startIndex, endIndex).stream().mapToInt(Integer::intValue).sum();
			processWindow(windowSize);
		}
	}

	/**
	 * Process a single window of given size.
	 * @param size The window size in bytes.
	 */
	private void processWindow(int size) {
		// Create new detector in initial state
		DetectorNFA detectorBackEnd = new DetectorNFA();
		backEndList.add(detectorBackEnd);

		List<DetectorNFA> backEndRemoveList = new ArrayList<>();
		for(DetectorNFA backEnd : backEndList) {
			List<Window> curMatches = backEnd.apply(size).getCurrentState();

			if(curMatches.size() == 0) {
				backEndRemoveList.add(backEnd);
				continue;
			}

			// List of candidate videos processed in this iteration of the detector.
			List<VideoIdentifier> curCandidates = new ArrayList<>();
			for(Window match : curMatches) {
				VideoIdentifier videoIdentifier = match.getVideoIdentifier();

				// Do not process multiple matches for same video on single window.
				if(curCandidates.contains(videoIdentifier)) {
					continue;
				}

				curCandidates.add(videoIdentifier);

				// Score this match. Higher generation matches are rated higher since this indicates a streak.
				int score = 1;
				if(candidateCountMap.containsKey(videoIdentifier)) {
					score = candidateCountMap.get(videoIdentifier) +1;
					if (backEnd.getGeneration() > 1) {
						score += BONUS * (backEnd.getGeneration() -1);
					}
				}
				candidateCountMap.put(videoIdentifier, score);
			}
		}
		// Throw away detectors not producing any results anymore
		backEndRemoveList.forEach(backEndList::remove);
	}

	/**
	 * Write the -intermediary- results for this detector connection.
	 */
	public void writeResults() {
		if(aduBytes.size() < Config.WINDOW_SIZE) {
			Logger.write("No results to report for " + connectionAddr);
		} else {
			Logger.write("Results for " + connectionAddr);

			// Remove any candidates ending up with 0 occurrences
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
				Logger.write("===================");
				Logger.write(count + " matches, resulting in a " + percentage + "% match");
				for(VideoIdentifier vId : vids) {
					Logger.write(vId.getTitle());
				}
			}
			//Logger.write("");
			//calcCoefficient();
		}
	}

	private void calcCoefficient() {
		// Prepare
		List<Integer> windowBytes = getWindowsFromAduSegments();
		Map<VideoIdentifier, List<Integer>> vidWindowBytesMap = new HashMap<>();
		for (VideoIdentifier vid : candidateCountMap.keySet()) {
			vidWindowBytesMap.put(vid, getWindowsFromVideo(vid));
		}

		// Calculate pearson's coefficient
		PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();

		for(Map.Entry<VideoIdentifier, List<Integer>> entry : vidWindowBytesMap.entrySet()) {


			List<Integer> vidWindowBytes = entry.getValue();
			if(vidWindowBytes.size() < windowBytes.size()) {
				continue;
			}

			int startIndex = 0;

			while(windowBytes.size() + startIndex < vidWindowBytes.size()) {
				List<Integer> subVidWindowBytes = vidWindowBytes.subList(startIndex, windowBytes.size() + startIndex);

				double corell = pearsonsCorrelation.correlation(windowBytes.stream().mapToDouble(Integer::doubleValue).toArray(),
						subVidWindowBytes.stream().mapToDouble(Integer::doubleValue).toArray());
				Logger.write(entry.getKey() + " Corell: " + corell);
				startIndex += 1;
			}
		}

	}

	/**
	 * Get a list of non-overlapping window sizes from the ADU input on the detector.
	 * @return List of windows.
	 */
	private List<Integer> getWindowsFromAduSegments() {
		List<Integer> result = new ArrayList<>();
		int curWindowBytes = 0;
		int curWindowSize = 0;

		for (Integer adu : aduBytes) {
			curWindowBytes += adu;
			curWindowSize +=1;
			if (curWindowSize == Config.WINDOW_SIZE) {
				result.add(curWindowBytes);
				curWindowBytes = 0;
				curWindowSize = 0;
			}
		}
		return result;
	}

	/**
	 * Get a list of non-overlapping window sizes for a video.
	 * @param videoIdentifier
	 * @return List of windows.
	 */
	private List<Integer> getWindowsFromVideo(VideoIdentifier videoIdentifier) {
		List<Integer> result = new ArrayList<>();
		int startIndex = 0;
		while(videoIdentifier.getWindowMap().containsKey(startIndex)) {
			result.add(videoIdentifier.getWindowMap().get(startIndex).getSize());
			startIndex += Config.WINDOW_SIZE;
		}
		return result;
	}
}
