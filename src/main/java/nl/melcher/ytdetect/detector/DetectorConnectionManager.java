package nl.melcher.ytdetect.detector;

import nl.melcher.ytdetect.adu.AduLine;
import nl.melcher.ytdetect.adu.MalformedAduLineException;
import nl.melcher.ytdetect.fingerprinting.WindowFactory;
import nl.melcher.ytdetect.har.HarFilter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by melcher on 15-6-17.
 */
public class DetectorConnectionManager {

	// Map connection to detector
	Map<String, DetectorConnection> connectionMap = new HashMap<>();

	public void pushAduLine(AduLine line) {
		String fromAddress = line.getFromAddress();
		DetectorConnection detectorConnection;

		// Only process TLS connections
		if (AduLine.isTLS(fromAddress)) {
			// Find or create detector for this connection
			if (connectionMap.containsKey(fromAddress)) {
				detectorConnection = connectionMap.get(fromAddress);
			} else {
				detectorConnection = new DetectorConnection(fromAddress);
				connectionMap.put(fromAddress, detectorConnection);
			}

			if (line.getType() == AduLine.InferredType.ADU
					&& line.getDirection() == AduLine.Direction.INCOMING
					&& line.getSize() > HarFilter.SEGMENT_SIZE_THRESHOLD) {
				// Process ADU segment
				detectorConnection.pushSegment(line.getSize());
			} else if (line.getType() == AduLine.InferredType.END) {
				// Connection ends here. Close down detector.
				detectorConnection.writeResults();
			}
		}
	}

	public void wrap() {
		for (DetectorConnection detectorConnection : connectionMap.values()) {
			detectorConnection.writeResults();
			connectionMap.remove(detectorConnection);
		}
	}
}
