/*
/*
 * Jipopro, the Jitsi Post-Processing application for recorded conferences.
 *
 *
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
