/*
 * Jipopro, the Jitsi Post-Processing application for recorded conferences.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package org.jitsi.recording.postprocessing.util;

/**
 * Static utility methods
 */
public class Utils
{
    /** Removes the file extension from a file name
     * @param fileName the file name which extension we want to trim
     * @return the trimmed file name
     */
    public static String trimFileExtension(String fileName)
    {
        int index = fileName.lastIndexOf('.');
        return fileName.substring(0, index);
    }

    /** Converts time in milliseconds to a String representing the time in
     * seconds in the format XX.XXX
     * @param millis the time in milliseconds that we want to convert
     * @return a String representing the time in seconds in the format XX.XXX
     */
    public static String millisToSeconds(long millis)
    {
        String result = "";
        String complement = "";
        long fraction = Math.abs(millis) % 1000;

        if (fraction < 10)
        {
            complement = "00";
        } else if (fraction < 100)
        {
            complement = "0";
        }

        result += millis / 1000;
        result += ".";
        result += complement + fraction;

        return result;
    }
}
