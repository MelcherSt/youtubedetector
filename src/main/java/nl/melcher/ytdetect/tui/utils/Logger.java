package nl.melcher.ytdetect.tui.utils;

/**
 * Created by melcher on 31-5-17.
 */
public class Logger {

	private static final boolean VERBOSE = true;

	public static void log(String message) {
		if (VERBOSE) {
			System.out.println(message);
		}
	}
}
