package nl.melcher.ytdetect.nfa;

import lombok.NonNull;
import nl.melcher.ytdetect.VideoIdentifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by melcher on 23-5-17.
 */
public class NfaFactory {

	private final Map<VideoIdentifier, List<Integer>> videoMap = new HashMap<>();

	public void addVideo(@NonNull VideoIdentifier video, @NonNull List<Integer> segmentList) {
		videoMap.put(video, segmentList);
	}

	public void addAllVideo(Map<VideoIdentifier, List<Integer>> videoIdentifierListMap) {
		videoMap.putAll(videoIdentifierListMap);
	}

	public Nfa build() {
		// Create a start state
		NfaState startState = new NfaState(null);

		// Loop over all videos
		for(Map.Entry<VideoIdentifier, List<Integer>> entry : videoMap.entrySet()) {
			VideoIdentifier vId = entry.getKey();
			NfaState prevState = null;

			// Loop over segment sizes
			for(Integer size : entry.getValue()) {
				NfaState curState = new NfaState(vId);

				// Add transitions to previous and start states
				if(prevState != null) {
					prevState.addTransition(size, curState);
				}
				startState.addTransition(size, curState);

				// Update previous state before continuing
				prevState = curState;
			}
		}
		return new Nfa(startState);
	}
}