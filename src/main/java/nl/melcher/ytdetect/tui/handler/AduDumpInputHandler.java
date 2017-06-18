package nl.melcher.ytdetect.tui.handler;

import nl.melcher.ytdetect.adu.AduDumpParser;
import nl.melcher.ytdetect.adu.MalformedAduLineException;
import nl.melcher.ytdetect.warp.ConnectionManager;
import nl.melcher.ytdetect.tui.InvalidArgumentsException;
import nl.melcher.ytdetect.tui.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Parses incoming adudump lines and delegates ADU segments to detectors.
 */
public class AduDumpInputHandler implements IHandler {
	@Override
	public void handle(List<String> args) throws InvalidArgumentsException {
		Logger.write("Awaiting input...");

		// Create input source
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		ConnectionManager man = new ConnectionManager();

		while(true) {
			try {
				man.addAduLine(AduDumpParser.parseLine(input.readLine()));
			} catch (IOException e) {
				Logger.write("Expected input, but none was given.");
			} catch (MalformedAduLineException ex) {
			// No more incoming valid ADU lines. Close down all connections.
			man.finish();
		}
		}
	}
}
