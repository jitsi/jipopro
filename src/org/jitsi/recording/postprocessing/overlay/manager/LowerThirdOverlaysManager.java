package org.jitsi.recording.postprocessing.overlay.manager;

import java.io.*;
import java.util.*;
//Disambiguation
import java.util.List;
import java.awt.*;
import java.awt.geom.*;

import org.jitsi.recording.postprocessing.layout.*;
import org.jitsi.recording.postprocessing.overlay.*;
import org.jitsi.recording.postprocessing.participant.*;
import org.jitsi.recording.postprocessing.util.*;

/**
 * An {@link OverlaysManager} implementation that generates FFMPEG video filter
 * graphs for overlaying the participant video tiles according to the lower
 * third designs.
 * @author vmarinov
 *
 */
public class LowerThirdOverlaysManager extends OverlaysManager {
    /**
     * The path to the background image of the active participant lower third
     * overlay
     */
    private final String ACTIVE_BACKGROUND_IMAGE_PATH = 
        "../image/activeUsernameBackground.png";
    /**
     * The path to the background image of the inactive participant lower third
     * overlay
     */
    private final String INACTIVE_BACKGROUND_IMAGE_PATH = 
        "../image/inactiveUsernameBackground.png";
    
    /**
     * The currently speaking participant SSRC
     */
    private int speakerSSRC;
    
    @Override
    protected List<Overlay> getOverlays(ParticipantInfo participant,
            Dimension videoTileDimension) 
    {
        List<Overlay> result = new ArrayList<Overlay>();
        HashMap<String, String> templateReplacements = 
                new HashMap<String, String>();
        templateReplacements.put("name", participant.username);
        templateReplacements.put("description", participant.description);
        if (participant.SSRC == speakerSSRC)
        {
            templateReplacements.put(
                "background", ACTIVE_BACKGROUND_IMAGE_PATH);
        } else
        {
            templateReplacements.put(
                "background", INACTIVE_BACKGROUND_IMAGE_PATH);
        }
        
        int imageHeight = (int) (0.155 * videoTileDimension.height);
        int imageYPosition = videoTileDimension.height - imageHeight;
        
        HTMLTemplateOverlay htmlOverlay = new HTMLTemplateOverlay();
        htmlOverlay.setPosition(
            new Point2D.Double(0, imageYPosition + 2));
        // a width of -1 means "preserve aspect ratio"
        htmlOverlay.setDimension(new Dimension(-1, imageHeight));
        htmlOverlay.setOutputImagePath(
            "temp/lower-third/" + Math.abs(participant.SSRC) + ".png");
        htmlOverlay.setHTMLTemplatePath(
                "resources/html/lower_third_overlay.html");
        htmlOverlay.setTemplateRectangle(new Rectangle(0, 0, 696, 111));
        htmlOverlay.setTemplateReplacements(templateReplacements);
        
        result.add(htmlOverlay);
        
        return result;
    }

    @Override
    protected void prepare(List<ParticipantInfo> participants,
            LayoutStrategy layoutStrategy) 
    {
        for (int i = 0; i < participants.size(); i++)
        {
            if (participants.get(i).isCurrentlySpeaking)
            {
                speakerSSRC = participants.get(i).SSRC;
                return;
            }
        }
    }

    @Override
    public void cleanUp() 
    {
        try
        {
            Exec.exec("rm -f temp/lower-third/*");
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (InterruptedException e) 
        {
            e.printStackTrace();
        }
    }
}
