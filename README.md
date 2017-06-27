# WARP Detector
WARP detector is an implementation of the Wedge Arrangement Rewarding Pipeline or **WARP** algorithm. The WARP algorithm
is part of the research project "I know what you watched: fingerprinting YouTube video streams".
It aims to detect YouTube videos playing over HTTPS-protected video streams using video fingerprinting.

## Background 
 
 ### adudump
 The application is meant to be used together with the *adudump* tool created by Dr. Jeff Terrell. Adudump is able to infer
 Application Data Unit (ADU) sizes from TCP segments. The application only accepts adudump output. 
 
 ### HTTP Archive (HAR)
 HAR files are used as input to determine the video segments. 
 An HAR can easily be created using any modern browsers' developer tools suite. No HAR files are included in this repository.
 
 ### Video definition
 `videos.txt` contains a definition of the 20 most viewed YouTube videos. These videos are used as part of the research project. 
 Part of the definition includes the video's HAR file. The HAR files themselves have not been added to the repository and need to
 be captured by hand.
 
 ## Implementation
 The current implementation supports various window sizes. The default window size has been set to 1 (corresponds to a segment 1 to 1) 
 as this has given the best results.
 
 ## Usage
 Capture HAR files for some YouTube videos. Process the HAR files by used in the `-a` switch in combination with some video information
 or use the `-a` switch in combination with a file like `videos.txt`. After adding save all the fingerprints by typing `-s`.
 Now one is able to process adudump output by typing `-r` and copy pasting the output into the program.



 