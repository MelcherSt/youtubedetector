package nl.melcher.ytdetect.tui.handler;

import nl.melcher.ytdetect.adu.AduDumpLine;
import nl.melcher.ytdetect.adu.AduDumpParser;
import nl.melcher.ytdetect.detector.DetectorFrontEnd;
import nl.melcher.ytdetect.har.HarFilter;
import nl.melcher.ytdetect.tui.InvalidArgumentsException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

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
		DetectorFrontEnd frontEnd = DetectorFrontEnd.getInstance();

		// Continue reading input
		while(true) {
			try {
				// Parse lines and send to frontend for further processing
				AduDumpLine line = AduDumpParser.parseLine(f.readLine());
				if(line.getType() == AduDumpLine.InferredType.ADU &&
						line.getDirection() == AduDumpLine.Direction.INCOMING) {

					if(line.getSize() > HarFilter.SEGMENT_SIZE_THRESHOLD) {
						frontEnd.pushSegment(line.getSize());
					}
				}
			} catch (IOException e) {
				System.out.println("Expected input, but there was none!");
			}
		}

	}
}
