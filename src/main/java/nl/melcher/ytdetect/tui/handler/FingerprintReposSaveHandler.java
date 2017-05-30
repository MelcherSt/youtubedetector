package nl.melcher.ytdetect.tui.handler;

import nl.melcher.ytdetect.fingerprinting.FingerprintRepository;
import nl.melcher.ytdetect.tui.InvalidArgumentsException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by melcher on 30-5-17.
 */
public class FingerprintReposSaveHandler implements ICmdHandler {
	@Override
	public void handle(List<String> args) throws InvalidArgumentsException {
		/**
		 * Acceptable formats:
		 * -s
		 * -s file.bin
		 */
		String fileName = FingerprintRepository.FILE_NAME;
		if(args.size() > 0) {
			fileName = args.stream().collect(Collectors.joining(""));
		}

		try {
			FingerprintRepository.serialize(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
