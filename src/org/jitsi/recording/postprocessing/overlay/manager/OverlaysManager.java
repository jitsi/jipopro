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

package org.jitsi.recording.postprocessing.overlay.manager;

import java.awt.*;
import java.util.*;
//Disambiguation
import java.util.List;

import org.jitsi.recording.postprocessing.layout.*;
import org.jitsi.recording.postprocessing.overlay.*;
import org.jitsi.recording.postprocessing.participant.*;

public abstract class OverlaysManager {
    
    /**
     * Gets the {@link Overlay} objects that will be used by the
     * getOverlayVideoFilterStrings method in order to combine the FFMPEG video
     * filter Strings and produce a filter chain with all the single overlay
     * video filter Strings
     * @param participant the participant which video tile should be overlayed
     * @return {@link List} of {@link Overlay} objects
     */
    protected abstract List<Overlay> getOverlays(ParticipantInfo participant,
            Dimension videoTileDimension);
    
    /**
     * This method is called before any work is done in order to get some 
     * information that will be used during the process (e.g. find the index
     * of the active speaker etc.)
     * @param participants the participants
     * @param layoutStrategy the layout strategy
     */
    protected abstract void prepare(
        List<ParticipantInfo> participants, LayoutStrategy layoutStrategy);
    
    /**
     * Performs clean up operations if necessary (e.g. delete temporary files)
     */
    public abstract void cleanUp();
    
    /**
     * Gets the FFMPEG video filter Strings that will be used in order to 
     * overlay the participants video tiles given the participants and a layout 
     * strategy. The layout strategy is used in order to determine the
     * dimensions of the video tiles.
     * @param participants participants whose video tiles will be overlayed
     * @param layoutStrategy the layout strategy that will be used in order to
     * determine the video tile dimensions
     * @return a list of Strings. Each of these Strings contains a video filter
     * graph that 
     */
    public List<String> getOverlayVideoFilterString(
        List<ParticipantInfo> participants, LayoutStrategy layoutStrategy)
    {
        String currentNode = "in", nextNode = "";
        String currentFilter = "", participantFilter = "";
        
        List<Overlay> overlays;
        List<String> result = new ArrayList<String>();
        
        prepare(participants, layoutStrategy);
        
        layoutStrategy.calculateDimensions(participants);
        List<Dimension> smallVideosDimensions = 
                layoutStrategy.getSmallVideosDimensions();
        Dimension largeVideoDimenson = layoutStrategy.getLargeVideoDimensions();
        Dimension currentTileDimension = null;
        int smallVideos = 0;
        
        for(int i = 0; i < participants.size(); i++)
        {
            currentFilter = "";
            
            if (participants.get(i).isCurrentlySpeaking)
            {
                currentTileDimension = largeVideoDimenson;
            } else 
            {
                currentTileDimension = smallVideosDimensions.get(smallVideos);
                smallVideos++;
            }
            
            int videoTileWidth = currentTileDimension.width % 2 == 0?
                    currentTileDimension.width :
                    currentTileDimension.width + 1;
            int videoTileHeight = currentTileDimension.height % 2 == 0?
                    currentTileDimension.height :
                    currentTileDimension.height + 1;
            participantFilter = "[in]scale=%s:%s[step0];";
            participantFilter = String.format(participantFilter,
                    videoTileWidth, videoTileHeight);
            currentNode = "step0";
            
            overlays = getOverlays(participants.get(i), currentTileDimension);
            for (int j = 0; j < overlays.size(); j++)
            {
                if (j == overlays.size() - 1)
                {
                    nextNode = "out";
                } else 
                {
                    nextNode = "step" + (j + 1);
                }
                
                currentFilter = overlays.get(j).getFFMPEGVideoFilterString();
                currentFilter = String.format(
                    currentFilter, currentNode, nextNode);
                
                currentNode = nextNode;
                participantFilter += currentFilter;
                participantFilter += j == overlays.size() - 1 ? "" : ";";
            }
            
            result.add(participantFilter);
        }
        
        return result;
    }
}
