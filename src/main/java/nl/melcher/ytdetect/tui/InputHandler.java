package nl.melcher.ytdetect.tui;

import nl.melcher.ytdetect.adu.AduDumpParser;
import nl.melcher.ytdetect.tui.handler.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by melcher on 24-5-17.
 */
public class InputHandler {

	private final Map<String, Class<? extends ICmdHandler>> cmdHandlerMap = new HashMap<>();

	{
		// Register all available handlers
		cmdHandlerMap.put("-a", HarAddHandler.class);
		cmdHandlerMap.put("--add", HarAddHandler.class);
		cmdHandlerMap.put("-r", RealTimeHandler.class);
		cmdHandlerMap.put("--real", RealTimeHandler.class);
		cmdHandlerMap.put("-s", FingerprintReposSaveHandler.class);
		cmdHandlerMap.put("--save", FingerprintReposSaveHandler.class);
	}

	public void handle(String[] rawArgs) {
		List<String> args = new LinkedList<String>(Arrays.asList(rawArgs));
		if(args.size() < 1) {
			args.add("");
		} else if(args.get(0) == "q") {
			return;
		}

		Class<? extends ICmdHandler> clazz = cmdHandlerMap.getOrDefault(args.get(0), UsageHandler.class);
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

		System.out.println("Finished.");
		BufferedReader f = new BufferedReader(new InputStreamReader(System.in));
		try {
			this.handle(f.readLine().split(" "));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
