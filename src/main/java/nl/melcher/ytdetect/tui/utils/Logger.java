package nl.melcher.ytdetect.tui.utils;

/**
 * Created by melcher on 31-5-17.
 */
public class Logger {
	/** Control whether debug output is visible **/
	private static final boolean VERBOSE = true;

	public static void write(String message) {
		System.out.println(message);
	}

	public static void debug(String message) {
		if (VERBOSE) {
			System.out.println("[DEBUG] " + message);
		}
	}
}
