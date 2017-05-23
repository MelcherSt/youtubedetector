package nl.melcher.ytdetect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Identifies a YouTube video
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class VideoIdentifier {

	@NonNull
	private String title;
	private Integer videoQuality;
	private Integer audioQuality = -1;
	private String url = "";
}
