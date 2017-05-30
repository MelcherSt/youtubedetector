package nl.melcher.ytdetect.fingerprinting;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages {@link Fingerprint} and {@link nl.melcher.ytdetect.VideoIdentifier} instances for a session.
 */
public class FingerprintRepository {

	private static final Set<Fingerprint> FINGERPRINTS = new HashSet<>();
	public static final String FILE_NAME = "fingerprints.bin";

	public static void addFingerprint(Fingerprint fingerprint) {
		FINGERPRINTS.add(fingerprint);
	}

	public static void addFingerprints(Collection<Fingerprint> fingerprints) {
		FINGERPRINTS.addAll(fingerprints);
	}

	 /* Clear current fingerprints and add all deserialized fingerprints from file.
	 * @param fileName
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void deserialize(String fileName) throws IOException, ClassNotFoundException {
		try(ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName))) {
			FINGERPRINTS.clear();
			FINGERPRINTS.addAll((Set<Fingerprint>)inputStream.readObject());
		}
	}

	/**
	 * Serialize fingerprints
	 * @param fileName
	 * @throws IOException
	 */
	public static void serialize(String fileName) throws IOException {
		try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
			outputStream.writeObject(FINGERPRINTS);
		}
	}

	public static Set<Fingerprint> getFingerprints() {
		return FINGERPRINTS;
	}
}
