package nl.melcher.ytdetect.warp;

import lombok.Getter;
import nl.melcher.ytdetect.Config;
import nl.melcher.ytdetect.VideoIdentifier;
import nl.melcher.ytdetect.adu.AduLine;
import nl.melcher.ytdetect.fingerprinting.Window;
import nl.melcher.ytdetect.tui.utils.Logger;

import java.util.*;

/**
 * Represents a connection. Handles incoming ADUs and controls instances of {@link StateMachine}.
 * The {@link Connection} oversees all reported window matches from NFAs and constructs
 * a list of candidates based on this information.
 */
public class Connection {

	/**
	 * Generation bonus score multiplier.
	 */
	private static final int BONUS = 2;

	/**
	 * List of incoming ADU sizes in bytes including any HTTP and/or TLS overhead still.
	 */
	private final List<Integer> aduBytes = new ArrayList<>();

	/**
	 * Identifier for the connection this warp works on.
	 */
	@Getter private String connectionAddr;

	/**
	 * Timestamp of first received ADU on this connection.
	 */
	@Getter private String firstAduTimestamp;

	/**
	 * Mapping candidate videos to their occurrence.
	 */
	private final Map<VideoIdentifier, Integer> candidateCountMap = new HashMap<>();

	/**
	 * List of all currently active detectors
	 */
	private final List<StateMachine> stateMachines = new ArrayList<>();

	public Connection(String connectionAddr) {
		this.connectionAddr = connectionAddr;
	}

	/**
	 * Push adudump input line and process.
	 * @param line An {@link AduLine} representing an incoming video segment.
	 */
	public void pushSegment(AduLine line) {
		// Process ADU segment
		aduBytes.add(line.getSize());

		if(aduBytes.size() == 1) {
			firstAduTimestamp = line.getTimestamp();
		}

		if(aduBytes.size() >= Config.WINDOW_SIZE) {
			// We have at least one complete window. Calculate total size.
			int startIndex = aduBytes.size() - (Config.WINDOW_SIZE);
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
		// Create new warp in initial state
		StateMachine newMachine = new StateMachine();
		stateMachines.add(newMachine);

		List<StateMachine> invalidMachines = new ArrayList<>();
		for(StateMachine stateMachine : stateMachines) {
			List<Window> curMatches = stateMachine.applySymbol(size).getState();

			// Throw away if no matches
			if(curMatches.size() == 0) {
				invalidMachines.add(stateMachine);
				continue;
			}

			// List of candidate videos processed in this iteration of the warp.
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
					if (stateMachine.getGeneration() > 1) {
						score += BONUS * (stateMachine.getGeneration() -1);
					}
				}
				candidateCountMap.put(videoIdentifier, score);
			}
		}
		// Throw away detectors not producing any results anymore
		invalidMachines.forEach(stateMachines::remove);
	}

	/**
	 * Write the -intermediary- results for this warp connection.
	 */
	public void writeResults() {
		if(aduBytes.size() < Config.WINDOW_SIZE) {
			Logger.write("No results to report for " + connectionAddr);
		} else {
			Logger.write("========================================================================");
			Logger.write("Results for " + connectionAddr);
			Logger.write("First ADU received: " + firstAduTimestamp);

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
		}
	}
}
