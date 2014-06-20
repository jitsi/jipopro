/*
 * Jipopro, the Jitsi Post-Processing application for recorded conferences.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package org.jitsi.recording.postprocessing.overlay;

/**
 * An image overlay. It needs path to the image to be set as well as image
 * dimensions and position.
 * @author Vladimir Marinov
 *
 */
public class ImageOverlay extends Overlay {
    /**
     * Path on the file system to the image to be overlayed
     */
    private String path;
    
    /**
     * Set the path to the image
     * @param path the path to the image
     */
    public void setPath(String path)
    {
        this.path = path;
    }
    
    @Override
    public String getFFMPEGVideoFilterString() 
    {
        String result = "", movieFilter = "";
        
        result = "[%s]overlay=%s:%s";
        result = String.format(result, "img" + hashCode(), position.x,
                position.y);
        movieFilter = "movie='%s', scale=%s:%s[%s];";
        movieFilter = String.format(movieFilter, path, dimension.width, 
                dimension.height, "img" + hashCode(), "%s");
        result = movieFilter + "[%s]" + result + "[%s]";
        
        return result;
    }

}
