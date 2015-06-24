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

import java.util.*;

/**
 * An interface that manages the list of active participants. It takes care of
 * which participants should be show in the videos list and what their order
 * should be
 * @author Vladimir Marinov
 *
 */
public interface ActiveParticipantsManager {
    /** Updates the active participants list upon a participant 
     * joining the call 
     * @param participant the participant to be added
     */
    public void addParticipant(ParticipantInfo participant);
    
    /** Updates the active participants list upon a participant 
     * leaving the call 
     * @param participantSSRC the SSRC of the participant that is to be removed
     */
    public void removeParticipant(int participantSSRC);
    
    /** Updates the active participants list upon a participant 
     * becoming the active speaker
     * @param newSpeakerSSRC the SSRC of the participant that is to become an
     * active speaker
     * @param eventInstant the instant the SPEAKER_CHANGED event occurred
     */
    public void speakerChange(int newSpeakerSSRC, int eventInstant);
    
    /** 
     * Returns the active participants list 
     * @return a list of active participants 
     */
    public List<ParticipantInfo> getActiveParticipantsList();
}
