package nl.melcher.ytdetect.tui.handler;

import nl.melcher.ytdetect.fingerprinting.WindowRepository;
import nl.melcher.ytdetect.tui.InvalidArgumentsException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Save all windows currently in {@link WindowRepository}.
 */
public class WindowReposSaveHandler implements ICmdHandler {
	@Override
	public void handle(List<String> args) throws InvalidArgumentsException {
		/**
		 * Acceptable formats:
		 * -s
		 * -s file.bin
		 */
		String fileName = WindowRepository.FILE_NAME;
		if(args.size() > 0) {
			fileName = args.stream().collect(Collectors.joining(""));
		}

		try {
			WindowRepository.getInstance().serialize(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
