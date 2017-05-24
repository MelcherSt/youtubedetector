package nl.melcher.ytdetect.har;

import de.sstoehr.harreader.HarReaderException;
import nl.melcher.ytdetect.VideoIdentifier;
import nl.melcher.ytdetect.tui.ICmdHandler;
import nl.melcher.ytdetect.tui.InvalidArgumentsException;

import java.util.List;

/**
 * Add a video using HAR and video information
 */
public class HarAddCmdHandler implements ICmdHandler {
	@Override
	public void handle(List<String> args) throws InvalidArgumentsException {
		/*
			Acceptable formats:
			-h file.har -t "Video Name" -q 248 -aq 238 -u http://youtube.com/bla
			--har file.har --title VideoTitle
		 */

		// Validate arguments
		if(args.size() < 2 || args.size() > 9) {
			throw new InvalidArgumentsException("Invalid amount of arguments");
		}

		String harFile = args.get(0);
		String vidTitle = null;
		Integer vidQuality = 0;
		Integer audQuality = 0;
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

		if((index = args.indexOf("-aq")) != -1) {
			try {
				audQuality = Integer.valueOf(args.get(index + 1));
			} catch(NumberFormatException ex) {
				throw new InvalidArgumentsException("Audio quality should be an integer");
			}
		}

		// Create video id
		VideoIdentifier vId = new VideoIdentifier(vidTitle, vidQuality, audQuality, vidUrl);

		// Parse the HAR file and filter relevant information
		HarFilter harFilter = new HarFilter(harFile);
		try {
			List<Integer> segmentSizes = harFilter.filter();
			for(Integer size : segmentSizes) {
				System.out.print(size + ", ");
			}
		} catch (HarReaderException e) {
			e.printStackTrace();
		}

		System.out.println(vId);
	}
}
