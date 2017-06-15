package nl.melcher.ytdetect.adu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides functionality for parsing `adudump` generated entries.
 */
public class AduDumpParser {

	/**
	 * Parses a complete file.
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static List<AduLine> parseFile(String fileName) throws IOException {
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
			return stream.map(AduDumpParser::parseLine).collect(Collectors.toList());
		}
	}

	/**
	 * Parses a single line.
	 * @param aduDumpLine
	 * @return
	 */
	public static AduLine parseLine(String aduDumpLine) throws MalformedAduLineException {
		String[] parts = aduDumpLine.split(" ");
		if (parts.length < 5) {
			throw new MalformedAduLineException("A line should contain at least 5 parts of information.");
		}

		// Get all the data
		AduLine.InferredType type;
		String timestamp = parts[1];
		String addrOne = parts[2];
		AduLine.Direction direction = AduLine.Direction.fromString(parts[3]);
		String addrTwo = parts[4];
		int size = 0;

		// Get type
		try {
			type = AduLine.InferredType.valueOf(parts[0].substring(0, 3));
		} catch (IllegalArgumentException ex) {
			type = AduLine.InferredType.UNKNOWN;
		}

		// Set size when necessary
		if(type == AduLine.InferredType.ADU) {
			size = Integer.valueOf(parts[5]);
		}

		return new AduLine(type, timestamp, addrOne, addrTwo, size, direction);
	}
}
