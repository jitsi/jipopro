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

package org.jitsi.recording.postprocessing.participant;

/**
 * Holds information about a participant in the video call being recorded
 * @author Vladimir Marinov
 *
 */
public class ParticipantInfo implements Cloneable {
    public int SSRC;
    /**
     * The name of the participant's video file
     */
    public String fileName;

    /**
     * The name of the "decoded" (e.g. MJPEG mov) file used for the participant.
     */
    public String decodedFilename;

    /**
     * The aspect ratio of the participant's video file
     */
    public int aspectRatio;
    /**
     * Time of the last action of the participant
     */
    public int lastActiveInstant = 0;
    /**
     * The instant the current participant video file started (with respect to
     * the global recording timeline)
     */
    public int currentVideoFileStartInstant;
    /**
     * Is the participant the active speaker in the current moment
     */
    public boolean isCurrentlySpeaking = false;
    
    /** The username of the current participant */
    public String username = "";
    
    /** The description text written beneath the current participant username */
    public String description = "";
    
    /** When set to true this participant's video will be the only video shown
     * on the screen whenever the participant is the currently active speaker 
     */
    public boolean disableOtherVideosOnTop = false;
    
    public ParticipantInfo(int SSRC)
    {
        this.SSRC = SSRC;
    }
    
    public Object clone() throws CloneNotSupportedException 
    {
        return super.clone();
    }
}
