package org.jitsi.recording.postprocessing.overlay;

import org.jitsi.recording.postprocessing.*;
import org.jitsi.recording.postprocessing.util.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;

/**
 * An overlay that takes a HTML template and produces and image overlay by 
 * taking a snapshot of the HTML. It takes a {@link Map} in order
 * to replace the template points in the HTML template with real data. For 
 * example: in &lt;p&gt;Hello, @name&lt;/p&gt; replace @name with Jivko. It 
 * also needs to be told where the snapshot should be placed. This allows the
 * caller to take control over the file and eventually delete it later on. This
 * overlay also needs a {@link Rectangle} instance which tells what region of
 * the HTML to be snapshot.
 * @author vmarinov
 *
 */
public class HTMLTemplateOverlay extends Overlay {
    /** Path to the HTML template file */
    private String htmlTemplatePath;
    
    /** Path to a file where the output snapshot will be placed */
    private String outputImagePath;
    
    /** What part of the HTML template will be taken snapshot of */
    private Rectangle templateRectangle;
    
    /** The map that will be used in order to replace the template points 
     * in the HTML template with real data */
    private Map<String, String> templateReplacements;
    
    /** The image overlay that will generate the FFMPEG video filter 
     * {@link String} once we've taken snapshot of the HTML
     */
    private ImageOverlay imageOverlay = new ImageOverlay();
    
    /**
     * Set the path to the HTML template file
     * @param htmlTemplatePath the path to the HTML template file
     */
    public void setHTMLTemplatePath(String htmlTemplatePath)
    {
        this.htmlTemplatePath = htmlTemplatePath;
    }
    
    /**
     * Set the path to a file where the output snapshot will be placed
     * @param outputImagePath the path to a file where the output snapshot
     * will be placed
     */
    public void setOutputImagePath(String outputImagePath)
    {
        this.outputImagePath = outputImagePath;
    }
    
    /**
     * Set what part of the HTML template will be taken snapshot of
     * @param templateRectangle what part of the HTML template will be 
     * taken snapshot of
     */
    public void setTemplateRectangle(Rectangle templateRectangle)
    {
        this.templateRectangle = templateRectangle;
    }
    
    /**
     * Set the map that will be used in order to replace the template 
     * points in the HTML template with real data
     * @param templateReplacements the {@link Map} that will be used in order to 
     * replace the template points in the HTML template with real data
     */
    public void setTemplateReplacements(
        Map<String, String> templateReplacements)
    {
        this.templateReplacements = templateReplacements;
    }

    @Override
    public String getFFMPEGVideoFilterString() 
    {
        String htmlTemplateString = "", mergedHTMLString = "";
        
        try {
            Scanner scanner = new Scanner(new File(htmlTemplatePath));
            htmlTemplateString = scanner.useDelimiter("\\Z").next();
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        mergedHTMLString = htmlTemplateString;
        for (String key : templateReplacements.keySet())
        {
            mergedHTMLString = 
                mergedHTMLString.replaceAll(Pattern.quote("$" + key), 
                templateReplacements.get(key));
        }
        
        try 
        {
            FileWriter fw = new FileWriter(getMergedHTMLFilename());
            fw.write(mergedHTMLString);
            fw.close();
            
            //String exec = "phantomjs resources/js/rasterize.js %s %s";
            List<String> exec = new LinkedList<String>();
            exec.add("phantomjs");
            exec.add("resources/js/rasterize.js");
            exec.add(getMergedHTMLFilename());
            exec.add(outputImagePath);

            Exec.execList(exec);

            exec = new LinkedList<String>();
            exec.add(Config.FFMPEG);
            exec.add("-y");
            exec.add("-i");
            exec.add(outputImagePath);
            exec.add("-vf");
            exec.add(String.format("crop=%s:%s:%s:%s",
                                   templateRectangle.width,
                                   templateRectangle.height,
                                   templateRectangle.x,
                                   templateRectangle.y));
            exec.add(outputImagePath);

            Exec.execList(exec);

            imageOverlay.setDimension(dimension);
            imageOverlay.setPosition(position);
            imageOverlay.setPath(outputImagePath);
            
            Exec.exec("rm " + getMergedHTMLFilename());
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (InterruptedException e) 
        {
            e.printStackTrace();
        }
        
        return imageOverlay.getFFMPEGVideoFilterString();
    }
    
    /**
     * Returns the filename of the HTML file produced by replacing the real 
     * data in the HTML tepmlate file
     * @return the filename of the HTML file produced by replacing the real 
     * data in the HTML tepmlate file
     */
    private String getMergedHTMLFilename()
    {
        return htmlTemplatePath + hashCode() + ".html";
    }
}
