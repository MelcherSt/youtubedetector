package nl.melcher.ytdetect.fingerprinting;

import java.io.*;
import java.util.*;

/**
 * Manages {@link Window} and {@link nl.melcher.ytdetect.VideoIdentifier} instances for a session.
 */
public class WindowRepository {

	private static final List<Window> WINDOWS = new ArrayList<>();
	public static final String FILE_NAME = "fingerprints.bin";

	public static void addWindow(Window window) {
		WINDOWS.add(window);
	}

	public static void addWindows(Collection<Window> windows) {
		WINDOWS.addAll(windows);
	}

	 /* Clear current windows and add all deserialized windows from file.
	 * @param fileName
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void deserialize(String fileName) throws IOException {
		try(ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName))) {
			WINDOWS.clear();
			WINDOWS.addAll((List<Window>)inputStream.readObject());
		} catch(ClassNotFoundException ex) {
			// Ignore
		}
	}

	/**
	 * Serialize windows
	 * @param fileName
	 * @throws IOException
	 */
	public static void serialize(String fileName) throws IOException {
		try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
			outputStream.writeObject(WINDOWS);
		}
	}

	/**
	 * Get a list of the currently loaded windows.
	 * @return The list of windows.
	 */
	public static List<Window> getWindows() {
		return WINDOWS;
	}
}
