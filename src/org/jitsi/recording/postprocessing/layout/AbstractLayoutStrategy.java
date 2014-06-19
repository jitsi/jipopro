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
