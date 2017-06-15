package nl.melcher.ytdetect.tui.handler;

import nl.melcher.ytdetect.adu.AduLine;
import nl.melcher.ytdetect.adu.AduDumpParser;
import nl.melcher.ytdetect.adu.MalformedAduLineException;
import nl.melcher.ytdetect.detector.DetectorConnection;
import nl.melcher.ytdetect.tui.InvalidArgumentsException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses incoming adudump lines and delegates ADU segments to detectors.
 * (when piped to adudump like so: "sudo ./adudump -l 192.168.1./16 if:enps0 | java -jar ytdetector.jar -r")
 */
public class RealTimeHandler implements IHandler {
	@Override
	public void handle(List<String> args) throws InvalidArgumentsException {
		System.out.println("Real-time mode. Awaiting input...");

		// Create input source
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

		// Map connection to detector
		Map<String, DetectorConnection> connectionMap = new HashMap<>();

		while(true) {
			try {
				// Parse ADU line
				AduLine line = AduDumpParser.parseLine(input.readLine());
				String fromAddress = line.getFromAddress();
				DetectorConnection detectorConnection;

				// Only process TLS connections
				if(AduLine.isTLS(fromAddress)) {
					// Find or create detector for this connection
					if (connectionMap.containsKey(fromAddress)) {
						detectorConnection = connectionMap.get(fromAddress);
					} else {
						detectorConnection = new DetectorConnection(fromAddress);
						connectionMap.put(fromAddress, detectorConnection);
					}
					detectorConnection.pushAdu(line);
				}

			} catch (IOException e) {
				System.out.println("Expected input, but none was given.");
			} catch (MalformedAduLineException ex) {
				// No more incoming valid ADU lines.
				for(DetectorConnection detectorConnection : connectionMap.values()) {
					detectorConnection.writeResults();
					connectionMap.remove(detectorConnection);
				}
			}
		}
	}
}
