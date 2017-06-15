package nl.melcher.ytdetect.fingerprinting;

import lombok.Getter;
import lombok.Setter;
import nl.melcher.ytdetect.VideoIdentifier;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a section of segments of a video.
 */
public class Window implements Serializable, Comparable {

	/**
	 * List of segment sizes in bytes.
	 */
	private List<Integer> segmentBytes;

	/**
	 * Total summed size in bytes of all segments contained.
	 */
	@Getter	private int size = 0;

	/**
	 * Start segment no.
	 */
	@Getter	private int startIndex;

	/**
	 * End segment no.
	 */
	@Getter	private int endIndex;

	/**
	 * The actual video this fingerprint belongs to
	 */
	@Getter	private VideoIdentifier videoIdentifier;

	public Window(List<Integer> segmentSizeList, VideoIdentifier videoIdentifier, int startIndex, int endIndex) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.segmentBytes = segmentSizeList;
		this.videoIdentifier = videoIdentifier;
		size = segmentSizeList.stream().mapToInt(Integer::intValue).sum();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Fingerprint[StartIndex=" + startIndex + ",EndIndex=" + endIndex + ",Size=" + size + ",Segments=");

		for(Integer segmentSize : segmentBytes) {
			sb.append(segmentSize + ",");
		}
		return sb.toString();
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof Window) {
			int otherSize = ((Window) o).getSize();
			if (otherSize < size) {
				return -1;
			} else if( otherSize == size) {
				return 0;
			} else {
				return 1;
			}
		}
		return 0;
	}
}
