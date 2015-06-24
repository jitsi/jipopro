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
import java.util.regex.*;

import org.jitsi.recording.postprocessing.*;
import org.jitsi.recording.postprocessing.participant.*;
import org.jitsi.recording.postprocessing.util.*;

/**
 * A {@link Runnable} instance capable of processing a single call section 
 * given a {@link SectionDescription} instance
 * @author Vladimir Marinov
 *
 */
public class SectionProcessingTask
        implements Runnable
{
    
    /** The info needed to process the given section */
    private SectionDescription sectionDesc;
    
    /** A directory that this instance will use to save some temp files */
    private String workDir;
    
    /** The directory that the video resulting from processing this section 
     *  will be saved to 
     */
    private String outputDir = "sections/";

    private String resourcesDir;
    
    public SectionProcessingTask(SectionDescription sectionDesc,
                                 String outDir,
                                 String resourcesDir)
    {
        this.sectionDesc = sectionDesc;
        workDir = outDir + "section" + sectionDesc.sequenceNumber + "/";
        outputDir = outDir + "sections/";
        this.resourcesDir = resourcesDir + "/";

    }
    
    @Override
    public void run()
    {
        String s = "Processing section" + sectionDesc.sequenceNumber;
        s += "; duration=" + (sectionDesc.endInstant - sectionDesc.startInstant);
        s += "; largeDimension" + sectionDesc.largeVideoDimension;
        s+= "; smallDimensions:" + sectionDesc.smallVideosDimensions;
        System.err.println(s);

        try
        {
            Exec.exec("mkdir -p " + workDir);
            trimVideos(sectionDesc.startInstant, sectionDesc.endInstant);
            createCurrentSection(
                sectionDesc.startInstant, sectionDesc.endInstant);
            deleteWorkDir();
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
    
    /** Selects the parts of the input files that correspond to the current 
     * section 
     * @param endInstant the end of the section that is to be processes
     * @param startInstant the start of the section that is to be processes
     * @throws InterruptedException 
     * @throws IOException 
    */
    private void trimVideos(int startInstant, int endInstant)
            throws IOException, InterruptedException
    {
        int smallVideos = 0;
        for (int i = 0; i < sectionDesc.activeParticipants.size(); i++)
        {
            String filename;
            String exec = "";
            
            if (sectionDesc.activeParticipants.get(i).isCurrentlySpeaking)
            {
                filename = workDir + "large.mov";
            }
            else
            {
                filename = workDir + "small";
                filename += smallVideos;
                filename += ".mov";
                smallVideos++;
            }
            
            exec += Config.FFMPEG + " -y -i ";
            exec += sectionDesc.activeParticipants.get(i).decodedFilename;
            exec += " -vcodec copy -ss ";
            exec += Utils.millisToSeconds(
                    startInstant -
                            sectionDesc.activeParticipants.
                                    get(i).currentVideoFileStartInstant);
            exec += " -t ";
            exec += Utils.millisToSeconds(endInstant - startInstant);
            exec += " ";
            exec += filename;

            Exec.exec(exec);
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
        int largeVideoWidth, largeVideoHeight;
        List<Dimension> smallVideosDimensions;
        List<Point2D.Double> smallVideosPositions;

        largeVideoWidth = 
            sectionDesc.largeVideoDimension.width;
        largeVideoHeight = 
            sectionDesc.largeVideoDimension.height;
        smallVideosDimensions = 
            sectionDesc.smallVideosDimensions;
        smallVideosPositions = 
            sectionDesc.smallVideosPositions;
        
        String vf = "movie=" + workDir + "large.mov, scale=";
        vf += largeVideoWidth + ":" + largeVideoHeight + " [large];";
        vf += "movie=" + resourcesDir + "logo_200x.bmp [logo];";
        vf += "[large][logo] overlay=30:30 [largeWithLogo];";

        ParticipantInfo currentlySpeaking = null;
        for (ParticipantInfo p : sectionDesc.activeParticipants)
        {
            if (p.isCurrentlySpeaking)
            {
                currentlySpeaking = p;
                break;
            }
        }
        if (currentlySpeaking == null)
        {
            System.err.println("No currently speaking participant, can't "
                               + "process section.");
            return;
        }

        for (int i = 0, j = 0; i < sectionDesc.activeParticipants.size(); i++)
        {
            ParticipantInfo participant = sectionDesc.activeParticipants.get(i);

            if (participant == currentlySpeaking)
                continue;

            Dimension dimension =smallVideosDimensions.get(j);
            boolean useVideo = true;

            if (Config.USE_PARTICIPANT_IMAGES
                    && participant.fileName.equals(currentlySpeaking.fileName))
            {
                // small video for the active speaker
                useVideo = false;
                String imageFilename  = getImage(participant.username,
                                                 dimension.width+1,
                                                 dimension.height);

                if (imageFilename != null)
                    vf += "movie=" + imageFilename +" [small" + j + "];";
                else
                    useVideo = true;
            }

            if (useVideo)
            {
                vf += "movie=" + workDir + "small" + j + ".mov, scale=";
                vf += (dimension.width + 1) + ":" +
                      dimension.height + "[small" + j + "];";
            }

            j++;
        }
        
        vf += "[in][largeWithLogo] overlay=" + 
                (Config.OUTPUT_WIDTH - largeVideoWidth) / 2
                + ":0 ";
        vf += sectionDesc.activeParticipants.size() > 1 ? "[step0];" : "[out]";
        
        for (int i = 0; i < sectionDesc.activeParticipants.size() - 1; i++) 
        {
            vf += "[step" + i + "][small" + i + "] overlay=";
            vf += smallVideosPositions.get(i).x + ":" + 
                  smallVideosPositions.get(i).y;
            
            if (i == sectionDesc.activeParticipants.size() - 2) 
            {
                vf += "[out]";
            } else 
            {
                vf += "[step" + (i + 1) + "];";
            }
        }

        List<String> exec = new LinkedList<String>();
        exec.add(Config.FFMPEG);
        exec.add("-y");
        exec.add("-loop");
        exec.add("1");
        exec.add("-i");
        exec.add(resourcesDir+"background.bmp");
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
        exec.add(vf);
        exec.add(outputDir + "0_" + sectionDesc.sequenceNumber + ".mov");
        Exec.execList(exec);
    }
    
    /**
     * Deletes the working directory where all the temp files are saved
     * @throws InterruptedException 
     * @throws IOException 
     */
    private void deleteWorkDir() throws IOException, InterruptedException
    {
       //Exec.exec("rm -rf " + workDir);
    }

    private String getImage(String displayName,
                            int width,
                            int height)
    {
        String html = "";
        String htmlFilename = workDir + "participant-name.html";

        try
        {
            Scanner scanner
                    = new Scanner(
                    new File(resourcesDir + "participant-name.html"));
            html = scanner.useDelimiter("\\Z").next();
            scanner.close();
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Failed to generate image for participant,"
                                       + " not using an image: " + e);
            return null;
        }

        // Some cheap heuristics about the font size
        int maxWordLength = -1;
        if (displayName.contains(" "))
        {
            for (String word : displayName.split(" "))
            {
                if (word.length() > maxWordLength)
                    maxWordLength = word.length();
            }
        }

        int fontSize = 11;
        if (displayName.length() < 8)
            fontSize = 15;
        else if (displayName.length() < 16
                    && maxWordLength != -1
                    && maxWordLength < 8)
            fontSize = 14;
        else if (displayName.length() < 24
                    && maxWordLength != -1
                    && maxWordLength < 8)
            fontSize = 13;

        html = html.replaceAll(Pattern.quote("${WIDTH}"), ""+width);
        html = html.replaceAll(Pattern.quote("${HEIGHT}"), ""+height);
        html = html.replaceAll(Pattern.quote("${NAME}"), ""+displayName);
        html = html.replaceAll(Pattern.quote("${FONT_SIZE}"), ""+fontSize);

        try
        {
            FileWriter fw = new FileWriter(htmlFilename);
            fw.write(html);
            fw.close();
        }
        catch (IOException ioe)
        {
            System.err.println("Failed to generate image for participant,"
                                       + " not using an image: " + ioe);
            return null;
        }

        try
        {
            List<String> exec = new LinkedList<String>();
            exec.add("phantomjs");
            exec.add(resourcesDir + "js/rasterize.js");
            exec.add(htmlFilename);
            exec.add(workDir + "participant-name.bmp");
            exec.add(width + "px*" + height + "px");
            Exec.execList(exec);
        }
        catch (Exception e)
        {
            System.err.println("Failed to generate image for participant,"
                                       + " not using an image: " + e);
            return null;
        }
        return workDir + "participant-name.bmp";
    }
}
