/*
 * Jipopro, the Jitsi Post-Processing application for recorded conferences.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package org.jitsi.recording.postprocessing.video.concat;

import java.io.*;
import java.util.concurrent.*;

import org.jitsi.recording.postprocessing.*;
import org.jitsi.recording.postprocessing.util.*;

/**
 * A {@link ConcatStrategy} that concatenates all files with a single call
 * to ffmpeg.
 *
 * @author Boris Grozev
 */
public class SimpleConcatStrategy
        implements ConcatStrategy
{

    @Override
    public void concatFiles(
        String sourceDir, String outputFilename)
    {
        sourceDir = sourceDir.isEmpty() ? "." : sourceDir;

        int filesNum = (new File(sourceDir)).listFiles().length;

        final String concatFilename = sourceDir + "/" + "concat_list";

        /*
         * ASSUMPTION:
         * The files to be concatenated are named "0_i.mov" with i starting from
         * zero, and *not containing leading zeros*.
         */
        PrintWriter writer;
        try
        {
            writer = new PrintWriter(concatFilename);
            for (int i = 0; i<filesNum-1; i++)
                writer.println("file 0_" + i + ".mov");
            writer.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        try
        {
            // TODO support Config.OUTPUT_FORMAT
            Exec.exec(Config.FFMPEG + " -y -f concat -i "
                  + concatFilename + " -c:v libvpx -cpu-used 16 -threads "
                  + Config.FFMPEG_THREADS+" -b:v 1M " + outputFilename);
        }
        catch (Exception e)
        {
            System.err.println("Failed to concatenate sections: " + e);

        }
    }
}
