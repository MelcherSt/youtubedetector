package nl.melcher.ytdetect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * Identifies a YouTube video
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class VideoIdentifier implements Serializable {

	@NonNull
	private String title;
	private Integer videoQuality;
	private Integer audioQuality = -1;
	private String url = "";

	@Override
	public String toString() {
		return "Title=" + title + ",VideoQuality=" + videoQuality + ",AudioQuality=" + audioQuality + ",Url=" + url;
	}
}
