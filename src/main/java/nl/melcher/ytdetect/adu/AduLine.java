package nl.melcher.ytdetect.adu;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Partly representation of a single line of `adudump` output.
 */
@AllArgsConstructor
public class AduLine {

	@Getter private InferredType type;
	@Getter private String timestamp;
	private String addrOne;
	private String addrTwo;
	@Getter private int size = 0;
	@Getter private Direction direction;

	/**
	 * Get the address from which data was sent.
	 * @return
	 */
	public String getFromAddress() {
		if(direction == Direction.INCOMING) {
			return addrTwo;
		} else if(direction == Direction.OUTGOING) {
			return addrOne;
		}
		return "";
	}

	/**
	 * Get the address to which data was sent.
	 * @return
	 */
	public String getToAddress() {
		if(direction == Direction.INCOMING) {
			return addrOne;
		} else if(direction == Direction.OUTGOING) {
			return addrTwo;
		}
		return "";
	}

	@Override
	public String toString() {
		return "AduLine[Type=" + type + ",Timestamp=" + timestamp + ",AddrOne="
				+ addrOne + ",AddrTwo=" + addrTwo + ",Direction=" + direction + ",Size=" + size + "]";
	}

	/**
	 * Determine if the given address contains port 443.
	 * @param addr
	 * @return
	 */
	public static boolean isTLS(String addr) {
		String[] segm = addr.split("\\.");
		return segm.length != 0 && (segm[segm.length - 1]).equals("443");
	}

	public enum InferredType {
		SYN, RTT, SEQ, ADU, CNC, INC, UNKNOWN, END
	}

	public enum Direction {
		INCOMING, OUTGOING, UNKNOWN;

		public static Direction fromString(String sign) {
			switch (sign) {
				case "<1":
					return Direction.INCOMING;
				case ">1":
					return Direction.OUTGOING;
				default:
					return Direction.UNKNOWN;
			}
		}
	}
}
