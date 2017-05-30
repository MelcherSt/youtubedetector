# Youtube Detector
YouTube detector (ytdetector) is part of the research project "I know what you watched: fingerprinting YouTube video streams".
It aims to detect YouTube videos playing over HTTPS-protected video streams using fingerprinting techniques. 
Melcher © 2017

## Background 
 
 ### adudump
 YouTube Detector is meant to be used together with the *adudump* tool created by Dr. Jeff Terrell. Adudump is able to infer
 Application Data Unit (ADU) sizes from TCP segments. Ytdetector has a real-time mode in which adudump output can be 
 piped through to the detector for immediate detection.
 
 ### HTTP Archive (HAR)
 HAR files are used as input to determine the video segments. 
 An HAR can easily be created using any modern browsers' developer tools suite. No HAR files are included in this repository.
 
 ### Video definition
 `videos.txt` contains a definition of the 20 most viewed YouTube videos. These videos are used as part of the research project. 
 Part of the definition includes the video's HAR file.
 
## Implementation
### Fingerprints
A fingerprint is a collection of video segments. The identifier for a fingerprint is the sum of its segments. 
The number of segments in a fingerprint is defined by the window size. Each fingerprint has an expected *next* fingerprint.

### Detector algorithm
The algorithm uses HTTP request sizes and their respective order to detect which video is playing. 
It is loosely based on the idea of state machines - NFAs to be precisely. The detector algorithm is 
initialized with the complete set of fingerprints generated from HAR file corresponding to a video. 
After initialization, dectector is ready for ADU input. When the window size is first reached, processing 
will start.


 