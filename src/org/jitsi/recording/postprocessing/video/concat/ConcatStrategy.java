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
    public void concatFiles(String sourceDir, String outputFilename)
        throws IOException, InterruptedException;
}
