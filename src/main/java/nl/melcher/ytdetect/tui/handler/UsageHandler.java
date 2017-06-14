package nl.melcher.ytdetect.tui.handler;

import java.util.List;

/**
 * Handler to simply print usage details
 */
public class UsageHandler implements ICmdHandler {
	@Override
	public void handle(List<String> args) {
		System.out.println("Usage details");
		System.out.println("===============");
		System.out.println("-a / --add	:	Parse HAR file to fingerprints - stored this session");
		System.out.println("-r / --real	:	Parse adudump output in realtime");
		System.out.println("-s / --save : 	Save all windows created from HAR this session");
	}
}
