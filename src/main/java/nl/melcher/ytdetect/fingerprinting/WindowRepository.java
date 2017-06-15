package nl.melcher.ytdetect.fingerprinting;

import lombok.Getter;
import nl.melcher.ytdetect.tui.utils.Logger;

import java.io.*;
import java.util.*;

/**
 * Manages {@link Window} and {@link nl.melcher.ytdetect.VideoIdentifier} instances for a session.
 */
public class WindowRepository {

	private static final List<Window> WINDOWS = new ArrayList<>();
	public static final String FILE_NAME = "ytdetectrepos.bin";

	public void addWindow(Window window) {
		WINDOWS.add(window);
	}

	public void addWindows(Collection<Window> windows) {
		WINDOWS.addAll(windows);
	}

	@Getter	public static WindowRepository instance = new WindowRepository();

	private WindowRepository() {
		try {
			deserialize(WindowRepository.FILE_NAME);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	 /* Clear current windows and add all deserialized windows from file.
	 * @param fileName
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void deserialize(String fileName) throws IOException {
		try(ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName))) {
			WINDOWS.clear();
			WINDOWS.addAll((List<Window>)inputStream.readObject());
		} catch(ClassNotFoundException | InvalidClassException ex) {
			Logger.write("Could not deserialize windows from file '" + fileName + "'");
		} catch(FileNotFoundException ex) {
			Logger.write("File '" + fileName + "' was not found. Please create this file first using the '-a' and '-s' switches.");
		}
	}

	/**
	 * Serialize windows
	 * @param fileName
	 * @throws IOException
	 */
	public void serialize(String fileName) throws IOException {
		try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
			outputStream.writeObject(WINDOWS);
		}
	}

	/**
	 * Get a list of the currently loaded windows.
	 * @return The list of windows.
	 */
	public List<Window> getWindows() {
		return WINDOWS;
	}
}
