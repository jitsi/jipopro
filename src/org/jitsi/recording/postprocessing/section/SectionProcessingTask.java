package org.jitsi.recording.postprocessing.section;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
//Disambiguation
import java.util.List;

import org.jitsi.recording.postprocessing.*;
import org.jitsi.recording.postprocessing.util.*;

/**
 * A {@link Runnable} instance capable of processing a single call section 
 * given a {@link SectionDescription} instance
 * @author vmarinov
 *
 */
public class SectionProcessingTask implements Runnable {
    
    /** The info needed to process the given section */
    private SectionDescription sectionDesc;
    
    /** A directory that this instance will use to save some temp files */
    private String workDir = "";
    
    /** The directory that the video resulting from processing this section 
     *  will be saved to 
     */
    private String outputDir = "sections/";
    
    public SectionProcessingTask(SectionDescription sectionDesc) {
        this.sectionDesc = sectionDesc;
        workDir = "section" + sectionDesc.sequenceNumber + "/";
    }
    
    @Override
    public void run() {
            try {
                Exec.exec("mkdir -p " + workDir);
                trimVideos(sectionDesc.endInstant, sectionDesc.startInstant);
                createCurrentSection(
                    sectionDesc.endInstant, sectionDesc.startInstant);
                deleteWorkDir();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
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
    private void trimVideos(int endInstant, int startInstant) 
            throws IOException, InterruptedException
    {
        int smallVideos = 0;
        for (int i = 0; i < sectionDesc.activeParticipants.size(); i++)
        {
            String filename = new String(), exec = new String();
            
            if (sectionDesc.activeParticipants.get(i).isCurrentlySpeaking)
            {
                filename = workDir + "large.mov";
            } else {
                filename = workDir + "small";
                filename += smallVideos;
                filename += ".mov";
                smallVideos++;
            }
            
            exec += Config.FFMPEG + " -y -i ";
            exec += trimFileExtension(
                        sectionDesc.activeParticipants.get(i).fileName)
                + ".mov";
            exec += " -vcodec copy -ss ";
            exec += millisToSeconds(
                startInstant - 
                sectionDesc.activeParticipants.
                    get(i).currentVideoFileStartInstant);
            exec += " -t ";
            exec += millisToSeconds(endInstant - startInstant);
            exec += " ";
            exec += filename;
          //ffmpeg -i big-buck-bunny_trailer.webm -vcodec libvpx -acodec copy -ss 4.376 -t 16.233 video_trim_test.webm
            
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
    private void createCurrentSection(int endInstant, int startInstant) 
        throws IOException, InterruptedException
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
        vf += "movie=logo.bmp, scale=200:-1 [logo];";
        vf += "[large][logo] overlay=30:30 [largeWithLogo];";
        
        for (int i = 0, j = 0; i < sectionDesc.activeParticipants.size(); i++)
        {
            if (!sectionDesc.activeParticipants.get(i).isCurrentlySpeaking)
            {
                vf += "movie=" + workDir + "small" + j + ".mov, scale=";
                vf += (smallVideosDimensions.get(j).width + 1) + ":" + 
                    smallVideosDimensions.get(j).height + "[small" + j + "];";
                j++;
            }
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
        exec.add("background.bmp");
        exec.add("-r");
        exec.add(Integer.toString(Config.OUTPUT_FPS));
        exec.add("-ss");
        //XXX Boris: what is 0.06? Can we make that a constant?
        exec.add("0.06");
        exec.add("-t");
        exec.add(millisToSeconds(endInstant - startInstant));
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
        Exec.exec("rm -rf " + workDir);
    }
    
    /** Removes the file extension from a file name
     * @param fileName the file name which extension we want to trim
     * @return the trimmed file name
     */
    private String trimFileExtension(String fileName) 
    {
        int index = fileName.lastIndexOf('.');
        return fileName.substring(0, index);
    }
    
    /** Converts time in milliseconds to a String representing the time in 
     * seconds in the format XX.XXX
     * @param millis the time in milliseconds that we want to convert
     * @return a String representing the time in seconds in the format XX.XXX
     */
    private String millisToSeconds(int millis) 
    {
        String result = new String();
        String complement = "";
        int fraction = Math.abs(millis) % 1000;
        
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
