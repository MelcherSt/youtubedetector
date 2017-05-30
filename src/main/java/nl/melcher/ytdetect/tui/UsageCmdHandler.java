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
		System.out.println("-h / --har	:	Parse HAR file to fingerprints - stored this session");
		System.out.println("-r / --real	:	Parse adudump output in realtime");
	}
}
