package nl.melcher.ytdetect.har;

import de.sstoehr.harreader.HarReaderException;
import nl.melcher.ytdetect.VideoIdentifier;
import nl.melcher.ytdetect.fingerprinting.Fingerprint;
import nl.melcher.ytdetect.fingerprinting.FingerprintFactory;
import nl.melcher.ytdetect.tui.ICmdHandler;
import nl.melcher.ytdetect.tui.InvalidArgumentsException;

import java.util.ArrayList;
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
			--har file.har --title VideoTitle (TBD)
		 */

		// Validate arguments
		if(args.size() < 2 || args.size() > 9) {
			throw new InvalidArgumentsException("Invalid amount of arguments");
		}

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

		// Parse the HAR file and filter relevant information
		List<Integer> segmentSizes = new ArrayList<>();
		HarFilter harFilter = new HarFilter(harFile);
		try {
			segmentSizes.addAll(harFilter.filter());
		} catch (HarReaderException e) {
			e.printStackTrace();
		}

		// Create video identifier
		VideoIdentifier videoIdentifier = new VideoIdentifier(vidTitle, vidQuality, vidUrl, vidLen);

		// Build fingerprints for video
		FingerprintFactory fingerprintFactory = new FingerprintFactory(segmentSizes, videoIdentifier);
		List<Fingerprint> fingerprints = fingerprintFactory.build();
		for (Fingerprint fp : fingerprints) {
			System.out.println(fp.toString());
			System.out.println("Next expected fp index=" + fp.getNext().getStartIndex());
		}

		videoIdentifier.setSegmentCount(fingerprints.size());
	}
}
