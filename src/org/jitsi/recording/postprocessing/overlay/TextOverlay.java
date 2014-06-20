/*
 * Jipopro, the Jitsi Post-Processing application for recorded conferences.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package org.jitsi.recording.postprocessing.overlay;

/**
 * A text overlay. To get configured it needs to be set text, position and font 
 * size.
 * @author Vladimir Marinov
 *
 */
public class TextOverlay extends Overlay {
    /** The text to be shown */
    private String text;
    
    /** The font size to be used */
    private int fontSize;
    
    /**
     * Set the overlay text
     * @param text the overlay text
     */
    public void setText(String text)
    {
        this.text = text;
    }
    
    /**
     * Set the overlay text font size
     * @param fontSize the overlay text font size
     */
    public void setFontSize(int fontSize)
    {
        this.fontSize = fontSize;
    }
    
    @Override
    public String getFFMPEGVideoFilterString() 
    {
        String result = "";
        
        result = "drawtext=fontfile='resources/fonts/Avenir Next Condensed.ttc':"
                + "x=%s: y=%s: text='%s': fontcolor=white: fontsize=%s";
        result = String.format(result, position.x, position.y, text, 
                               fontSize);
        result = "[%s]" + result + "[%s]";
        
        return result;
    }

}
