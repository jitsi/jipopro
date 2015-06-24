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
    public void concatFiles(String sourceDir, String outputFilename)
        throws IOException, InterruptedException

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
            for (int i = 0; i<filesNum; i++)
                writer.println("file 0_" + i + ".mov");
            writer.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        // TODO support Config.OUTPUT_FORMAT
        Exec.exec(Config.FFMPEG + " -y -f concat -i "
              + concatFilename + " -c:v libvpx -cpu-used 16 -threads "
              + Config.FFMPEG_THREADS+" -b:v 1M " + outputFilename);
    }
}
