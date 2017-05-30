package nl.melcher.ytdetect.tui.handler;

import nl.melcher.ytdetect.adu.AduDumpParser;
import nl.melcher.ytdetect.tui.InvalidArgumentsException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Parses incoming adudump lines in realtime
 * (when piped to adudump like to: "sudo ./adudump -l 192.168.1./16 if:enps0 | java -jar this.jar -r")
 */
public class RealTimeHandler implements ICmdHandler {
	@Override
	public void handle(List<String> args) throws InvalidArgumentsException {
		// Create input source
		BufferedReader f = new BufferedReader(new InputStreamReader(System.in));

		// Keep reading lines
		while(true) {
			try {
				String input = f.readLine();
				// Output parsed adudump line
				System.out.println(AduDumpParser.parseLine(input));
			} catch (IOException e) {
				System.out.println("Expected input, but there was none!");
			}
		}

	}
}
