/*
 * Jipopro, the Jitsi Post-Processing application for recorded conferences.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package org.jitsi.recording.postprocessing.video.concat;

/** 
 * An interface that describes a strategy for concating the video files 
 * contained in a given directory
 * @author Vladimir Marinov
 *
 */
public interface ConcatStrategy {
    /**
     * Concatenates the video files stored in sourceDir
     * @param sourceDir path to the directory where the video files are
     * @param outputFilename the name of the output video file
     */
    public void concatFiles(
        String sourceDir, String outputFilename);
}
