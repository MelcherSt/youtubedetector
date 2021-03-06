package nl.melcher.ytdetect.har;

import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarEntry;
import lombok.AllArgsConstructor;
import nl.melcher.ytdetect.Config;
import nl.melcher.ytdetect.tui.utils.Logger;

import java.io.File;
import java.util.*;

/**
 * HAR filter.
 */
@AllArgsConstructor
public class HarFilter {
	private final String harFileName;

	/**
	 * Filter HAR file.
	 * @return All responses with size above SEGMENT_SIZE_THRESHOLD bytes.
	 * @throws HarReaderException
	 */
	public List<Integer> filter() throws HarReaderException {
		HarReader harReader = new HarReader();
		List<Integer> segmentSizeList = new ArrayList<>();

		Har har = harReader.readFromFile(new File(harFileName));
		List<HarEntry> harEntries = har.getLog().getEntries();

		for(HarEntry entry : harEntries) {
			Long segmentSize = entry.getResponse().getBodySize();
			if(segmentSize > Config.SEGMENT_SIZE_THRESHOLD) {
				/* Generally, we aren't dealing with values
				 exceeding INT.MAX_VALUE so it's save to cast */
				segmentSizeList.add(segmentSize.intValue());

				// Print the individual ADU segment sizes
				Logger.debug(segmentSize.intValue() + ", ");
			}
		}
		return segmentSizeList;
	}
}
