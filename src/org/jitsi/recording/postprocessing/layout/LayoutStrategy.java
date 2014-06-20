/*
 * Jipopro, the Jitsi Post-Processing application for recorded conferences.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package org.jitsi.recording.postprocessing.layout;

import java.awt.*;
import java.awt.geom.*;
//Disambiguation
import java.util.List;

import org.jitsi.recording.postprocessing.participant.ParticipantInfo;

/** An interface that determines how active participants should be
 * placed in the output video
*/
public interface LayoutStrategy {
    /**
     * Initializes this instance
     * @param outputVideoWidth width of the output video
     * @param outputVideoHeight height of the output video
     */
    public void initialize(int outputVideoWidth, int outputVideoHeight);
    
    /** Calculates the dimensions of the large video and the small videos in
     * the output video
     * * @param participants the list of active participants whose videos
     * are to be shown
    */
    public void calculateDimensions(List<ParticipantInfo> participants);
    
    /**
     * Returns a list containing the dimensions of the small videos
     * @return a list containing the dimensions of the small videos
     */
    public List<Dimension> getSmallVideosDimensions();
    
    /**
     * Returns the dimensions of the large video
     * @return the dimensions of the large video
     */
    public Dimension getLargeVideoDimensions();
    
    /**
     * Returns a list containing the positions of the small videos
     * @return a list containing the positions of the small videos
     */
    public List<Point2D.Double> getSmallVideosPositions();
}
