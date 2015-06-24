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
