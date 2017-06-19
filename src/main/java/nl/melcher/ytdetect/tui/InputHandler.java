package nl.melcher.ytdetect.tui;

import nl.melcher.ytdetect.tui.handler.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * TUI Input handler.
 */
public class InputHandler {

	private final Map<String, Class<? extends IHandler>> cmdHandlerMap = new HashMap<>();

	{
		// Register all available handlers
		cmdHandlerMap.put("-a", HarAddHandler.class);
		cmdHandlerMap.put("--add", HarAddHandler.class);
		cmdHandlerMap.put("-r", AduDumpInputHandler.class);
		cmdHandlerMap.put("--real", AduDumpInputHandler.class);
		cmdHandlerMap.put("-s", WindowReposSaveHandler.class);
		cmdHandlerMap.put("--save", WindowReposSaveHandler.class);
	}

	public void handle(String[] rawArgs) {
		List<String> args = new LinkedList<>(Arrays.asList(rawArgs));
		if(args.size() < 1) {
			args.add("");
		} else if(args.get(0).equals("q")) {
			return;
		}

		Class<? extends IHandler> clazz = cmdHandlerMap.getOrDefault(args.get(0), UsageHandler.class);
		args.remove(0);

		try {
			IHandler cmdHandler = clazz.newInstance();
			cmdHandler.handle(args);
		} catch (InvalidArgumentsException e) {
			System.out.println("An error occurred while parsing arguments: " + e.getMessage());
		} catch (IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		}

		System.out.println("Finished.");
		BufferedReader f = new BufferedReader(new InputStreamReader(System.in));
		try {
			this.handle(f.readLine().split(" "));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
