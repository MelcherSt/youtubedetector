package nl.melcher.ytdetect.adu;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Partly representation of a single line of `adudump` output.
 */
@AllArgsConstructor
public class AduDumpLine {

	@Getter private InferredType type;
	@Getter private String timestamp;
	private String addrOne;
	private String addrTwo;
	@Getter private int size = 0;
	@Getter private Direction direction;

	public String getFromAddress() {
		if(direction == Direction.INCOMING) {
			return addrTwo;
		} else if(direction == Direction.OUTGOING) {
			return addrOne;
		}
		return "";
	}

	public String getToAddress() {
		if(direction == Direction.INCOMING) {
			return addrOne;
		} else if(direction == Direction.OUTGOING) {
			return addrTwo;
		}
		return "";
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

	@Override
	public String toString() {
		return "AduDumpLine[Type=" + type + ",Timestamp=" + timestamp + ",AddrOne="
				+ addrOne + ",AddrTwo=" + addrTwo + ",Direction=" + direction + ",Size=" + size + "]";
	}
}
