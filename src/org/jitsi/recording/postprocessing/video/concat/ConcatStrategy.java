package org.jitsi.recording.postprocessing.video.concat;

/** 
 * An interface that describes a strategy for concating the video files 
 * contained in a given directory
 * @author vmarinov
 *
 */
public interface ConcatStrategy {
    /**
     * Concatenates the video files stored in sourceDir
     * @param sourceDir path to the directory where the video files are
     * @param outputDir path to the directory which is to store the result video
     * file
     * @param outputFilename the name of the output video file
     */
    public void concatFiles(
        String sourceDir, String outputDir, String outputFilename);
}
