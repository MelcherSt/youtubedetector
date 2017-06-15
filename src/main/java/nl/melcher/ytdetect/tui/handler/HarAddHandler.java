package nl.melcher.ytdetect.tui.handler;

import de.sstoehr.harreader.HarReaderException;
import nl.melcher.ytdetect.VideoIdentifier;
import nl.melcher.ytdetect.fingerprinting.Window;
import nl.melcher.ytdetect.fingerprinting.WindowFactory;
import nl.melcher.ytdetect.fingerprinting.WindowRepository;
import nl.melcher.ytdetect.har.HarFilter;
import nl.melcher.ytdetect.tui.InvalidArgumentsException;
import nl.melcher.ytdetect.tui.utils.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Add a video using HAR and video information.
 */
public class HarAddHandler implements ICmdHandler {

	@Override
	public void handle(List<String> args) throws InvalidArgumentsException {
		/* Acceptable formats:
		-a file.har -t "Video Name" -q 248 -l 238 -u http://youtube.com/bla
		--add videos.txt */

		// Validate arguments
		if(args.size() < 1 || args.size() > 9) {
			throw new InvalidArgumentsException("Invalid amount of arguments");
		}

		if(args.size() == 1) {
			try (BufferedReader reader = new BufferedReader(new FileReader(new File(args.get(0))))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if(line.charAt(0) != '#') {
						handleLine(Arrays.asList(line.split("[ ]+(?=([^\"]*\"[^\"]*\")*[^\"]*$)")));
					}
				}
			} catch (FileNotFoundException e) {
				throw new InvalidArgumentsException("File '" + args.get(0) + "' does not exist!");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			handleLine(args);
		}
	}

	private void handleLine(List<String> args)  throws InvalidArgumentsException {
		String harFile = args.get(0);
		String vidTitle = null;
		Integer vidQuality = -1;
		Integer vidLen = -1;
		String vidUrl = "";

		int index = -1;
		if((index = args.indexOf("-t")) != -1 || (index = args.indexOf("--title")) != -1 ) {
			StringBuilder strb = new StringBuilder();
			vidTitle = args.get(index + 1);
		} else {
			throw new InvalidArgumentsException("Should at least define a title for video");
		}

		if((index = args.indexOf("-u")) != -1) {
			vidUrl = args.get(index + 1);
		}

		if((index = args.indexOf("-q")) != -1) {
			try {
				vidQuality = Integer.valueOf(args.get(index + 1));
			} catch(NumberFormatException ex) {
				throw new InvalidArgumentsException("Video quality should be an integer");
			}
		}

		if((index = args.indexOf("-l")) != -1) {
			try {
				vidLen = Integer.valueOf(args.get(index + 1));
			} catch(NumberFormatException ex) {
				throw new InvalidArgumentsException("Length should be an integer");
			}
		}

		// Parse the HAR file and retrieve video fingerprint
		List<Integer> fingerprint = new ArrayList<>();
		HarFilter harFilter = new HarFilter(harFile);
		try {
			fingerprint.addAll(harFilter.filter());

		} catch (HarReaderException e) {
			throw new InvalidArgumentsException("An error occurred while reading HAR file: " + e.getMessage());
		}

		// Create video identifier
		VideoIdentifier videoIdentifier = new VideoIdentifier(vidTitle, vidQuality, vidUrl, vidLen);

		// Build windows for video
		try {
			WindowFactory windowFactory = new WindowFactory(fingerprint, videoIdentifier);
			List<Window> windows = windowFactory.build();
			videoIdentifier.setAduCount(windows.size());

			// Add to repository
			WindowRepository.getInstance().addWindows(windows);

			Logger.log("");
			Logger.log("Created " + windows.size() + " windows from " +  fingerprint.size() + " segments for video " + vidTitle + "/" + vidLen + "/" + vidQuality);
			Logger.log("------");
		} catch (RuntimeException e) {
			System.out.println(e.getMessage());
		}


	}
}
