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
import java.util.*;
//Disambiguation
import java.util.List;

/**
 * An implementation of {@link LayoutStrategy} that preserves the aspect ratios 
 * of the videos. If the small videos exceed band in the lower side of
 * the output video their sizes are reduced so that they fit the band exactly.
 * @author Vladimir Marinov
 *
 */
public class NaiveLayoutStrategy extends AbstractLayoutStrategy {
    /**
     * The maximum height of small videos and respectively of the small videos
     * band
     */
    private final int MAX_SMALL_VIDEO_HEIGHT = 97;

    @Override
    protected void doCalculateDimensions() 
    {
        double widthToHeightRatio = 0.0;
        int smallVideosHeight = 0, largeVideoHeight;
        Dimension largeVideoDimensions = new Dimension();
        List<Dimension> smallVideoDimensions = new ArrayList<Dimension>();
        
        for (int i = 0; i < participants.size(); i++) 
        {
            if (!participants.get(i).isCurrentlySpeaking) 
            {
                widthToHeightRatio += 
                    AspectRatioUtil.scaleFactors
                        [participants.get(i).aspectRatio];
            }
        }
        
        if (widthToHeightRatio != 0) 
        {
            smallVideosHeight = 
                outputVideoWidth / widthToHeightRatio < MAX_SMALL_VIDEO_HEIGHT ?
                (int) (outputVideoWidth / widthToHeightRatio) : 
                MAX_SMALL_VIDEO_HEIGHT;
        }
        
        largeVideoHeight = outputVideoHeight - smallVideosHeight;
        for (int i = 0; i < participants.size(); i++) 
        {
            if (!participants.get(i).isCurrentlySpeaking) 
            {
                smallVideoDimensions.add(new Dimension(
                (int) (smallVideosHeight * 
                AspectRatioUtil.scaleFactors[participants.get(i).aspectRatio]),
                smallVideosHeight));
            } else 
            {
                largeVideoDimensions = new Dimension(
                (int) (largeVideoHeight * 
                AspectRatioUtil.scaleFactors[participants.get(i).aspectRatio]),
                largeVideoHeight);
            }
        }
        
        this.smallVideosDimensions = smallVideoDimensions;
        this.largeVideoDimension = largeVideoDimensions;
    }

    @Override
    protected void doCalculatePositions() {
        List<Point2D.Double> smallVideosPositions = 
            new ArrayList<Point2D.Double>();
        double firstSmallVideoXOffset, currentSmallVideoXOffset;
        
        firstSmallVideoXOffset = outputVideoWidth / 2.0;
        for (int i = 0, j = 0; i < participants.size(); i++) 
        {
            if (!participants.get(i).isCurrentlySpeaking) 
            {
                firstSmallVideoXOffset -= 
                    smallVideosDimensions.get(j).width / 2.0;
                j++;
            }
        }
        currentSmallVideoXOffset = firstSmallVideoXOffset;
        
        for (int i = 0; i < smallVideosDimensions.size(); i++)
        {
            smallVideosPositions.add(
                new Point2D.Double(
                currentSmallVideoXOffset, 
                outputVideoHeight * 1.0 - smallVideosDimensions.get(i).height));
            currentSmallVideoXOffset += smallVideosDimensions.get(i).width;
        }
        
        this.smallVideosPositions = smallVideosPositions;
    }
}
