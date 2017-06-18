package nl.melcher.ytdetect.warp;

import nl.melcher.ytdetect.Config;
import nl.melcher.ytdetect.adu.AduLine;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by melcher on 15-6-17.
 */
public class ConnectionManager {

	/**
	 * Map connection address to warp
	 */
	Map<String, Connection> connectionMap = new HashMap<>();

	/**
	 * Push an ADU segment to its connection.
	 * @param line
	 */
	public void addAduLine(AduLine line) {
		String fromAddress = line.getFromAddress();
		Connection connection;

		// Only process TLS connections
		if (AduLine.isTLS(fromAddress)) {
			// Find or create connection
			if (connectionMap.containsKey(fromAddress)) {
				connection = connectionMap.get(fromAddress);
			} else {
				connection = new Connection(fromAddress);
				connectionMap.put(fromAddress, connection);
			}

			if (line.getType() == AduLine.InferredType.ADU
					&& line.getDirection() == AduLine.Direction.INCOMING
					&& line.getSize() > Config.SEGMENT_SIZE_THRESHOLD) {
				// Process ADU segment
				connection.pushSegment(line.getSize());
			} else if (line.getType() == AduLine.InferredType.END) {
				// Connection ends here. Close down warp.
				connection.writeResults();
				connectionMap.remove(connection);
			}
		}
	}

	/**
	 * Write the results for each connection and remove them all.
	 */
	public void finish() {
		for (Connection connection : connectionMap.values()) {
			connection.writeResults();
			connectionMap.remove(connection);
		}
	}
}
