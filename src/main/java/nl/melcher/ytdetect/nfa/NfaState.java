package nl.melcher.ytdetect.nfa;

import lombok.Getter;
import nl.melcher.ytdetect.VideoIdentifier;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A single state in the Nfa including transitions to other states for a given symbol.
 * Also includes a dedicated {@link nl.melcher.ytdetect.VideoIdentifier} to identify the video this state belongs to.
 */
public class NfaState implements Serializable {

	private final Map<Integer, Set<NfaState>> symbolStateMap = new HashMap<>();

	/**
	 * Retrieve the video associated with this state. May be null.
	 */
	@Getter
	private VideoIdentifier videoIdentifier;

	public NfaState(VideoIdentifier videoIdentifier) {
		this.videoIdentifier = videoIdentifier;
	}

	/**
	 * Apply symbol to this state and retrieve the resulting state(s).
	 * @param symbol Applied symbol.
	 * @return Set of resulting states.
	 */
	public Set<NfaState> next(Integer symbol) {
		return symbolStateMap.get(symbol);
	}

	/**
	 * Add a state transition.
	 * @param symbol Symbol at which the transition will happen.
	 * @param state State to which the transition goes.
	 */
	public void addTransition(Integer symbol, NfaState state) {
		Set<NfaState> stateSet = symbolStateMap.get(symbol);
		if(stateSet == null) {
			stateSet = new HashSet<>();
			symbolStateMap.putIfAbsent(symbol, stateSet);
		}
		stateSet.add(state);
	}

	/**
	 * Serialize this state and all of its child states.
	 * @param fileName
	 * @throws IOException
	 */
	public void serialize(String fileName) throws IOException {
		try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
			outputStream.writeObject(this);
		}
	}

	/**
	 * Deserialize a state and its child states from file.
	 * @param fileName
	 * @return Nfastate
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static NfaState deserialize(String fileName) throws IOException, ClassNotFoundException {
		try(ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName))) {
			return (NfaState)inputStream.readObject();
		}
	}
}
