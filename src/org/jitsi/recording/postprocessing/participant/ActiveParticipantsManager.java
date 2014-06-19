package org.jitsi.recording.postprocessing.participant;

import java.util.*;

/**
 * An interface that manages the list of active participants. It takes care of
 * which participants should be show in the videos list and what their order
 * should be
 * @author vmarinov
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
