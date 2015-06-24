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

package org.jitsi.recording.postprocessing.section;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
//Disambiguation
import java.util.List;

import org.jitsi.recording.postprocessing.*;
import org.jitsi.recording.postprocessing.participant.*;
import org.jitsi.recording.postprocessing.util.*;

/**
 * A {@link Runnable} instance capable of processing a single call section
 * given a {@link SectionDescription} instance.
 * @author Boris Grozev
 *
 */
public class SectionProcessingTaskNoTrimImpl
        implements Runnable
{

    /** The info needed to process the given section */
    private SectionDescription sectionDesc;

    /** The directory that the video resulting from processing this section
     *  will be saved to
     */
    private String outputDir;
    private String resourcesDir;

    public SectionProcessingTaskNoTrimImpl(SectionDescription sectionDesc,
                                           String outputDir,
                                           String resourcesDir)
    {
        this.sectionDesc = sectionDesc;
        this.outputDir = outputDir + "sections/";
        this.resourcesDir = resourcesDir;
    }

    @Override
    public void run()
    {
        try
        {
            createCurrentSection(
                    sectionDesc.startInstant, sectionDesc.endInstant);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /** Overlays the parts of the input files that correspond to the current
     * event
     * @param endInstant the end of the section that is to be processes
     * @param startInstant the start of the section that is to be processes
     * @throws InterruptedException
     * @throws IOException
     */
    private void createCurrentSection(int startInstant, int endInstant)
            throws IOException,
                   InterruptedException
    {
        int largeVideoWidth =
                sectionDesc.largeVideoDimension.width;
        int largeVideoHeight =
                sectionDesc.largeVideoDimension.height;
        List<Dimension> smallVideosDimensions =
                sectionDesc.smallVideosDimensions;
        List<Point2D.Double> smallVideosPositions =
                sectionDesc.smallVideosPositions;

        ParticipantInfo activeParticipant = null;
        for (ParticipantInfo p : sectionDesc.activeParticipants)
        {
            if (p.isCurrentlySpeaking)
            {
                activeParticipant = p;
                break;
            }
        }

        ParticipantInfo activeParticipantSmall = null;
        int activeParticipantSmallIndex = -1;
        int jj = 0;
        for (int i = 0; i<sectionDesc.activeParticipants.size(); i++)
        {
            ParticipantInfo p = sectionDesc.activeParticipants.get(i);
            if (p.isCurrentlySpeaking)
                continue;

            if (p.fileName.equals(activeParticipant.fileName))
            {
                activeParticipantSmall = p;
                activeParticipantSmallIndex = jj;
                break;
            }

            jj++;
        }

        String activeSeek
                = Utils.millisToSeconds(
                startInstant - activeParticipant.currentVideoFileStartInstant);

        List<String> exec = new LinkedList<String>();
        exec.add(Config.FFMPEG);
        exec.add("-y");
        exec.add("-loop");
        exec.add("1");

        exec.add("-i");
        exec.add(resourcesDir + "background.bmp"); //[0:v]

        exec.add("-i");
        exec.add(resourcesDir + "logo_200x.bmp"); //[1:v]

        exec.add("-itsoffset");
        exec.add(activeSeek);
        exec.add("-i");
        exec.add(activeParticipant.decodedFilename); //[2:v]

        Map<ParticipantInfo, Integer> pariticipantInputIndexes
                = new HashMap<ParticipantInfo, Integer>();
        // j is the index for the ffmpeg inputs used in the filter expression
        // starts from 3, because 0, 1 and 2 are taken by background, logo and
        // active participant -- see above.
        for (int i = 0, j = 3; i < sectionDesc.activeParticipants.size(); i++)
        {
            ParticipantInfo p = sectionDesc.activeParticipants.get(i);
            if (p == activeParticipant)
                continue;

            String seek
                    = Utils.millisToSeconds(
                    startInstant - p.currentVideoFileStartInstant);

            //TODO: check for active's small video here, and replace it with an image
            exec.add("-itsoffset");
            exec.add(seek);
            exec.add("-i");
            exec.add(p.decodedFilename);

            pariticipantInputIndexes.put(p, j++);
        }

        exec.add("-r");
        exec.add(Integer.toString(Config.OUTPUT_FPS));
        exec.add("-ss");
        //XXX Boris: what is 0.06? Can we make that a constant?
        exec.add("0.06");
        exec.add("-t");
        exec.add(Utils.millisToSeconds(endInstant - startInstant));
        exec.add("-vcodec");
        exec.add("mjpeg");
        exec.add("-cpu-used");
        exec.add(String.valueOf(Config.FFMPEG_CPU_USED));
        exec.add("-threads");
        exec.add(1 + "");
        exec.add("-q:v");
        exec.add(Integer.toString(Config.QUALITY_LEVEL));


        // The filter string

        String filter = "[2:v] scale=" + largeVideoWidth + ":" + largeVideoHeight + " [large];";
        filter += "[large][1:v] overlay=30:30 [largeWithLogo];";

        for (int i = 0, j = 0; i < sectionDesc.activeParticipants.size(); i++)
        {
            ParticipantInfo p = sectionDesc.activeParticipants.get(i);
            if (p == activeParticipant)
                continue;

            filter += "[" + pariticipantInputIndexes.get(p) + ":v] scale=";
            filter += (smallVideosDimensions.get(j).width + 1) + ":" +
                    smallVideosDimensions.get(j).height + "[small" + j + "];";

            j++;
        }

        filter += "[0:v][largeWithLogo] overlay=" +
                (Config.OUTPUT_WIDTH - largeVideoWidth) / 2
                + ":0 ";
        filter += sectionDesc.activeParticipants.size() > 1 ? "[step0];" : "[out]";

        for (int i = 0; i < sectionDesc.activeParticipants.size() - 1; i++)
        {
            filter += "[step" + i + "][small" + i + "] overlay=";
            filter += smallVideosPositions.get(i).x + ":" +
                    smallVideosPositions.get(i).y;

            if (i == sectionDesc.activeParticipants.size() - 2)
            {
                filter += "[out]";
            } else
            {
                filter += "[step" + (i + 1) + "];";
            }
        }


        exec.add("-filter_complex");
        exec.add(filter);
        exec.add("-map");
        exec.add("[out]");
        exec.add(outputDir + "0_" + sectionDesc.sequenceNumber + ".mov");

        Exec.execList(exec);
    }
}

