package nl.melcher.ytdetect.adu;

/**
 * Typed exception.
 */
public class MalformedAduLineException extends RuntimeException {

	public MalformedAduLineException() {
		super();
	}

	public MalformedAduLineException(String message) {
		super(message);
	}
}
