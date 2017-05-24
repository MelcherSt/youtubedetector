package nl.melcher.ytdetect.tui;

import nl.melcher.ytdetect.har.HarAddCmdHandler;

import java.util.*;

/**
 * Created by melcher on 24-5-17.
 */
public class InputHandler {

	private final Map<String, Class<? extends ICmdHandler>> cmdHandlerMap = new HashMap<>();

	{
		// Register all available handlers
		cmdHandlerMap.put("-h", HarAddCmdHandler.class);
		cmdHandlerMap.put("--har", HarAddCmdHandler.class);
	}

	public void handle(String[] rawArgs) {
		List<String> args = new LinkedList<String>(Arrays.asList(rawArgs));
		Class<? extends ICmdHandler> clazz = cmdHandlerMap.getOrDefault(args.get(0), UsageCmdHandler.class);
		args.remove(0);


		try {
			ICmdHandler cmdHandler = clazz.newInstance();
			cmdHandler.handle(args);
		} catch (InvalidArgumentsException e) {
			System.out.println("An error occurred while parsing arguments: " + e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
	}

}
