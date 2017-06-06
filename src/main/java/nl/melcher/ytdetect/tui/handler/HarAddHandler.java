package nl.melcher.ytdetect.tui.handler;

import de.sstoehr.harreader.HarReaderException;
import nl.melcher.ytdetect.VideoIdentifier;
import nl.melcher.ytdetect.fingerprinting.Fingerprint;
import nl.melcher.ytdetect.fingerprinting.FingerprintFactory;
import nl.melcher.ytdetect.fingerprinting.FingerprintRepository;
import nl.melcher.ytdetect.har.HarFilter;
import nl.melcher.ytdetect.tui.InvalidArgumentsException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Add a video using HAR and video information.
 */
public class HarAddHandler implements ICmdHandler {

	private boolean verbose = true;

	@Override
	public void handle(List<String> args) throws InvalidArgumentsException {
		/*
			Acceptable formats:
			-h file.har -t "Video Name" -q 248 -l 238 -u http://youtube.com/bla
			--har videos.txt
		 */

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

		// Parse the HAR file and filter relevant information
		ArrayList<Integer> segmentSizes = new ArrayList<>();
		HarFilter harFilter = new HarFilter(harFile);
		try {
			segmentSizes.addAll(harFilter.filter());
		} catch (HarReaderException e) {
			throw new InvalidArgumentsException("An error occurred while reading HAR file: " + e.getMessage());
		}

		// Create video identifier
		VideoIdentifier videoIdentifier = new VideoIdentifier(vidTitle, vidQuality, vidUrl, vidLen);

		if(verbose) {
			System.out.println("Created vId: " + videoIdentifier.getTitle());
		}

		// Build fingerprints for video
		try {
			FingerprintFactory fingerprintFactory = new FingerprintFactory(segmentSizes, videoIdentifier);
			List<Fingerprint> fingerprints = fingerprintFactory.build();
			videoIdentifier.setSegmentCount(fingerprints.size());

			// Add to repository
			FingerprintRepository.addFingerprints(fingerprints);

			System.out.println("Created # of fingerprints: " + fingerprints.size());
		} catch (RuntimeException e) {
			System.out.println(e.getMessage());
		}


	}
}
