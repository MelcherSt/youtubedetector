package nl.melcher.ytdetect.tui.utils;

/**
 * Logger class writes to console.
 */
public class Logger {
	/** Control whether debug output is visible **/
	private static final boolean VERBOSE = true;

	public static void write(String message) {
		System.out.println(message);
	}

	public static void debug(String message) {
		if (VERBOSE) {
			System.out.print(message);
		}
	}

	public static void debugln(String message) {
		if (VERBOSE) {
			System.out.println("[DEBUG] " + message);
		}
	}
}
