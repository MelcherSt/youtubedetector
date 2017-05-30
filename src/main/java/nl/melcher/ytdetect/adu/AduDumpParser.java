package nl.melcher.ytdetect.adu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
	public static List<AduDumpLine> parseFile(String fileName) throws IOException {
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
			return stream.map(AduDumpParser::parseLine).collect(Collectors.toList());
		}
	}

	/**
	 * Parses a single line.
	 * @param aduDumpLine
	 * @return
	 */
	public static AduDumpLine parseLine(String aduDumpLine) {
		String[] parts = aduDumpLine.split(" ");
		if (parts.length < 5) {
			throw new RuntimeException("Malformed adudump line! A line should at least have 5 parts.");
		}

		// Get all the data
		AduDumpLine.InferredType type;
		String timestamp = parts[1];
		String addrOne = parts[2];
		AduDumpLine.Direction direction = AduDumpLine.Direction.fromString(parts[3]);
		String addrTwo = parts[4];
		int size = 0;

		// Get type
		try {
			type = AduDumpLine.InferredType.valueOf(parts[0].substring(0, 3));
		} catch (IllegalArgumentException ex) {
			type = AduDumpLine.InferredType.UNKNOWN;
		}

		// Set size when necessary
		if(type == AduDumpLine.InferredType.ADU) {
			size = Integer.valueOf(parts[5]);
		}

		return new AduDumpLine(type, timestamp, addrOne, addrTwo, size, direction);
	}
}
