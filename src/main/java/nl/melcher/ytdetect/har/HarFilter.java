package nl.melcher.ytdetect.har;

import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarLog;
import de.sstoehr.harreader.model.HarResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Filter HAR file contents to only include relevant video data
 */
public class HarFilter {

	/**
	 * Filter threshold for segments in bytes.
	 */
	private static final long SEGMENT_SIZE_THRESHOLD = 0;

	private final String harFileName;

	public HarFilter(String fileName) {
		this.harFileName = fileName;
	}

	public List<Integer> filter() throws HarReaderException {
		HarReader harReader = new HarReader();
		List<Integer> segmentSizeList = new ArrayList<>();

		Har har = harReader.readFromFile(new File(harFileName));
		List<HarEntry> harEntries = har.getLog().getEntries();

		for(HarEntry entry : harEntries) {
			Long segmentSize = entry.getResponse().getBodySize();
			if(segmentSize > SEGMENT_SIZE_THRESHOLD) {
				/* Generally, we aren't dealing with values
				 great than INT.MAX_VALUE so it's save to cast */
				segmentSizeList.add(segmentSize.intValue());
			}
		}
		return segmentSizeList;
	}
}
