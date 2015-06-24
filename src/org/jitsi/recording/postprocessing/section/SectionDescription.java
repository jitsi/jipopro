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

package org.jitsi.recording.postprocessing.section;

import java.awt.*;
import java.awt.geom.*;
//Disambiguation
import java.util.List;

import org.jitsi.recording.postprocessing.layout.*;
import org.jitsi.recording.postprocessing.participant.*;

/**
 * Contains the info that is needed in order to process a single section
 * @author Vladimir Marinov
 *
 */
public class SectionDescription {
    /** The list of participants that are active during this section */
    public List<ParticipantInfo> activeParticipants;
    
    /** The dimensions of the large video */
    public Dimension largeVideoDimension;
    
    /** The dimensions of the small videos */
    public List<Dimension> smallVideosDimensions;
    
    /** The positions of the small videos */
    public List<Point2D.Double> smallVideosPositions;
    
    /** The section starting instant */
    public int startInstant;
    
    /** The section ending instant */
    public int endInstant;
    
    /** The sequence number of this section (starting with 0) */
    public int sequenceNumber;
}
