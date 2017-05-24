package nl.melcher.ytdetect.tui;

import java.util.List;

/**
 * Handler to simply print usage details
 */
public class UsageCmdHandler implements ICmdHandler {
	@Override
	public void handle(List<String> args) {
		System.out.println("Usage details");
		System.out.println("===============");
		System.out.println("Not available.");
	}
}
