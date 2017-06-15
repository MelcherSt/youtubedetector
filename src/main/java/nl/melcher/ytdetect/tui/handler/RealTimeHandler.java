package nl.melcher.ytdetect.tui.handler;

import de.sstoehr.harreader.model.Har;
import nl.melcher.ytdetect.adu.AduLine;
import nl.melcher.ytdetect.adu.AduDumpParser;
import nl.melcher.ytdetect.adu.MalformedAduLineException;
import nl.melcher.ytdetect.detector.DetectorFrontEnd;
import nl.melcher.ytdetect.har.HarFilter;
import nl.melcher.ytdetect.tui.InvalidArgumentsException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses incoming adudump lines in realtime.
 * (when piped to adudump like to: "sudo ./adudump -l 192.168.1./16 if:enps0 | java -jar ytdetector.jar -r")
 */
public class RealTimeHandler implements ICmdHandler {
	@Override
	public void handle(List<String> args) throws InvalidArgumentsException {
		System.out.println("Real-time mode enabled. Awaiting input...");

		// Create input source
		BufferedReader f = new BufferedReader(new InputStreamReader(System.in));

		// Mapping from an address to a detector
		Map<String, DetectorFrontEnd> connectionMap = new HashMap<>();

		// Continue reading input
		while(true) {
			try {
				// Parse lines and send to frontend for further processing
				try {
					AduLine line = AduDumpParser.parseLine(f.readLine());

					DetectorFrontEnd detectorFrontEnd;

					if (connectionMap.containsKey(line.getFromAddress())) {
						detectorFrontEnd = connectionMap.get(line.getFromAddress());
					} else {
						detectorFrontEnd = new DetectorFrontEnd();
						connectionMap.put(line.getFromAddress(), detectorFrontEnd);
					}

					if(line.getType() == AduLine.InferredType.ADU
							&& line.getDirection() == AduLine.Direction.INCOMING
							&& AduLine.isTLS(line.getFromAddress())
							&& line.getSize() > HarFilter.SEGMENT_SIZE_THRESHOLD) {
						detectorFrontEnd.pushAduSegment(line.getSize());
					} else if(line.getType() == AduLine.InferredType.END) {
						detectorFrontEnd.wrap();
					}
				} catch (MalformedAduLineException ex) {
					// No more incoming valid ADU lines.
					for(DetectorFrontEnd detectorFrontEnd : connectionMap.values()) {
						detectorFrontEnd.wrap();
					}
				}
			} catch (IOException e) {
				System.out.println("Expected input, but none was given.");
			}
		}

	}



}
