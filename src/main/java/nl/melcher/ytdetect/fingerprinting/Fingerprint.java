package nl.melcher.ytdetect.fingerprinting;

import lombok.Getter;
import lombok.Setter;
import nl.melcher.ytdetect.VideoIdentifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents a section of segments of a video and as such fingerprints part of a video.
 */
public class Fingerprint implements Serializable, Comparable {

	private List<Integer> segmentSizeList;

	/**
	 * Total size of the segments contained in fingerprint.
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

	/**
	 * Expected next fingerprint without any overlap on this fingerprint. May be null.
	 */
	@Getter @Setter private Fingerprint next;

	public Fingerprint(List<Integer> segmentSizeList, VideoIdentifier videoIdentifier, int startIndex, int endIndex) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.segmentSizeList = segmentSizeList;
		this.videoIdentifier = videoIdentifier;
		size = segmentSizeList.stream().mapToInt(Integer::intValue).sum();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Fingerprint[StartIndex=" + startIndex + ",EndIndex=" + endIndex + ",Size=" + size + ",Segments=");

		for(Integer segmentSize : segmentSizeList) {
			sb.append(segmentSize + ",");
		}

		sb.append("]");
		return sb.toString();
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof Fingerprint) {
			int otherSize = ((Fingerprint) o).getSize();
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
