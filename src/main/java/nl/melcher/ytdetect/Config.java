package nl.melcher.ytdetect;

/**
 * Central configuration storing constant values used throughout the application.
 */
public class Config {
	public static final double TLS_MIN = 1.0023;
	public static final double TLS_MAX = 1.0017;

	/**
	 * Assumed size of HTTP header.
	 */
	public static final int HTTP_HEADER_SIZE = 525;

	/**
	 * Size of sliding window frames.
	 */
	public static final int WINDOW_SIZE = 7;

	/**
	 * Filter threshold for segments in bytes.
	 */
	public static final long SEGMENT_SIZE_THRESHOLD = 400000;
}
