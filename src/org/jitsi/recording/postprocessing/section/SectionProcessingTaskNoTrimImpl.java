/*
 * Jipopro, the Jitsi Post-Processing application for recorded conferences.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
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
    private String outputDir = "sections/";

    public SectionProcessingTaskNoTrimImpl(SectionDescription sectionDesc)
    {
        this.sectionDesc = sectionDesc;
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

        String activeSeek = Utils.millisToSeconds(startInstant - activeParticipant.currentVideoFileStartInstant);
        String v = "movie="+Utils.trimFileExtension(activeParticipant.fileName)+".mov:seek_point="+activeSeek+", setpts=PTS-STARTPTS";
        if (activeParticipantSmall != null)
          v+=", split [prelarge][presmall"+activeParticipantSmallIndex+"];";
        else v+= " [prelarge];";
        v+="[prelarge] scale="+largeVideoWidth+":"+largeVideoHeight+" [large];";
        if(activeParticipantSmall !=null)
        v+="[presmall"+activeParticipantSmallIndex+"] scale="+(smallVideosDimensions.get(activeParticipantSmallIndex).width+1)+":"+smallVideosDimensions.get(activeParticipantSmallIndex).height+" [small"+activeParticipantSmallIndex+"];";

        v += "movie=logo_200x.bmp [logo];"; //TODO: avoid scale logo
        v += "[large][logo] overlay=30:30 [largeWithLogo];";

        for (int i = 0, j = 0; i < sectionDesc.activeParticipants.size(); i++)
        {
            ParticipantInfo p = sectionDesc.activeParticipants.get(i);
            if (!p.isCurrentlySpeaking)
            {
                if (p != activeParticipantSmall)
                {
                    String seek = Utils.millisToSeconds(startInstant - p.currentVideoFileStartInstant);
                    v += "movie="+Utils.trimFileExtension(p.fileName)+".mov:seek_point="+seek+", setpts=PTS-STARTPTS, scale=";
                    v += (smallVideosDimensions.get(j).width + 1) + ":" +
                        smallVideosDimensions.get(j).height + "[small" + j + "];";
                }
                j++;
            }
        }

        v += "[in][largeWithLogo] overlay=" +
                (Config.OUTPUT_WIDTH - largeVideoWidth) / 2
                + ":0 ";
        v += sectionDesc.activeParticipants.size() > 1 ? "[step0];" : "[out]";

        for (int i = 0; i < sectionDesc.activeParticipants.size() - 1; i++)
        {
            v += "[step" + i + "][small" + i + "] overlay=";
            v += smallVideosPositions.get(i).x + ":" +
                    smallVideosPositions.get(i).y;

            if (i == sectionDesc.activeParticipants.size() - 2)
            {
                v += "[out]";
            } else
            {
                v += "[step" + (i + 1) + "];";
            }
        }

        List<String> exec = new LinkedList<String>();
        exec.add(Config.FFMPEG);
        exec.add("-y");
        exec.add("-loop");
        exec.add("1");
        exec.add("-i");
        exec.add("background.bmp");
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
        exec.add("-vf");
        exec.add(v);
        exec.add(outputDir + "0_" + sectionDesc.sequenceNumber + ".mov");
        Exec.execList(exec);
    }
}

