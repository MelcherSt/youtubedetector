package nl.melcher.ytdetect;

import lombok.*;
import nl.melcher.ytdetect.fingerprinting.Window;
import nl.melcher.ytdetect.fingerprinting.Window;

import java.io.Serializable;
import java.util.List;

/**
 * Identifies a YouTube video
 */
public class VideoIdentifier implements Serializable {

	@Getter	private String title;

	/**
	 * Video itag quality id.
	 */
	@Getter	private int videoQuality;

	@Getter	private String url;

	/**
	 * Video length in seconds.
	 */
	@Getter private int length = 0;

	@Setter @Getter private int aduCount = 0;

	@Setter @Getter private Window initWindow = null;

	public VideoIdentifier(String title, int videoQuality, String url, int length) {
		this.title = title;
		this.videoQuality = videoQuality;
		this.url = url;
		this.length = length;
	}

	@Override
	public String toString() {
		return "VideoIdentifier[Title=" + title + ",VideoQuality=" + videoQuality + ",Length=" + length + ",Url=" + url + "]";
	}
}
