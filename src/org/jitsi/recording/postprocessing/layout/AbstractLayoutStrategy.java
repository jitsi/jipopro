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

/** An abstract implementation of {@link LayoutStrategy} */
public abstract class AbstractLayoutStrategy implements LayoutStrategy {
    /**
     * Width of the output video
     */
    protected int outputVideoWidth;
    
    /**
     * Height of the output video
     */
    protected int outputVideoHeight;
    
    /**
     * List of active participants
     */
    protected List<ParticipantInfo> participants;
    
    /**
     * The sizes of the small videos calculated by this instance
     */
    protected List<Dimension> smallVideosDimensions;
    
    /**
     * The large video dimension calculated by this instance
     */
    protected Dimension largeVideoDimension;
    
    /**
     * The positions of the small videos calculated by this instance
     */
    protected List<Point2D.Double> smallVideosPositions;
    
    public void initialize(int outputVideoWidth, int outputVideoHeight)
    {
        this.outputVideoWidth = outputVideoWidth;
        this.outputVideoHeight = outputVideoHeight;
    }
    
    public void calculateDimensions(List<ParticipantInfo> participants)
    {
        this.participants = participants;
        doCalculateDimensions();
        doCalculatePositions();
    }
    
    public List<Dimension> getSmallVideosDimensions()
    {
        return smallVideosDimensions;
    }
    
    public Dimension getLargeVideoDimensions()
    {
        return largeVideoDimension;
    }
    
    public List<Point2D.Double> getSmallVideosPositions()
    {
        return smallVideosPositions;
    }
    
    /** A method that performs the actual calculation of dimensions */
    protected abstract void doCalculateDimensions();
    
    /** A method that performs the actual calculation of positions */
    protected abstract void doCalculatePositions();
}
