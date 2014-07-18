/*
 * Jipopro, the Jitsi Post-Processing application for recorded conferences.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package org.jitsi.recording.postprocessing.participant;

import java.util.*;
//Disambiguation
import java.util.List;

/**
 * An implementation of {@link ActiveParticipantsManager} that shows
 * the active speaker in the active participants list
 * @author Vladimir Marinov
 */
public class WithSpeakerInVideosListParticipantsManager 
    implements ActiveParticipantsManager 
{
    /**
     * The maximum number of small videos that can be shown
     */
    private final int MAX_ACTIVE_PARTICIPANTS_NUMBER = 7;
    
    /**
     * A list containing all the participants
     */
    private List<ParticipantInfo> participants = 
        new ArrayList<ParticipantInfo>();
    
    /**
     * A list containing only active participants
     */
    private List<ParticipantInfo> activeParticipants = 
            new ArrayList<ParticipantInfo>();
    
    /**
     * Add participant to the conversation
     */
    public void addParticipant(ParticipantInfo participant)
    {
        if (activeParticipants.size() == 0) 
        {
            participant.isCurrentlySpeaking = true;
        }
        
        participants.add(participant);
        
        if (activeParticipants.size() < MAX_ACTIVE_PARTICIPANTS_NUMBER)
        {
            activeParticipants.add(participant);
        } else 
        {
            int indexToReplace = findOldestActiveParticipantIndex();
            activeParticipants.set(indexToReplace, participant);
        }
    }
    
    /**
     * Remove participant from the conversation
     */
    public void removeParticipant(int participantSSRC)
    {
        if (activeParticipants.size() == 1) 
        {
            activeParticipants.remove(0);
            return;
        }
        
        boolean isActiveSpeaker = false;
        int activeParticipantsIndex = -1, participantsIndex = -1;
        
        for (int i = 0; i < activeParticipants.size(); i++)
        {
            if (activeParticipants.get(i).SSRC == participantSSRC) 
            {
                activeParticipantsIndex = i;
            }
        }
        participantsIndex = findParticipantIndex(participantSSRC);
        
        if (activeParticipantsIndex > -1 &&
            activeParticipants.get(activeParticipantsIndex).isCurrentlySpeaking)
        {
            isActiveSpeaker = true;
        }
        
        int toBecomeActiveIndex = -1, toBecomeActiveInstant = 0;
        for (int i = 0; i < participants.size(); i++) 
        {
            if (
                participants.get(i).lastActiveInstant > toBecomeActiveInstant &&
                getActiveParticipantsIndex(participants.get(i).SSRC) == -1) 
            {
                toBecomeActiveInstant = participants.get(i).lastActiveInstant;
                toBecomeActiveIndex = i;
            }
        }
        
        if (isActiveSpeaker) 
        {
            int latestActiveNonSpeakerIndex = 
                findLatestActiveNonSpeakerIndex();
            
            if (participants.size() > MAX_ACTIVE_PARTICIPANTS_NUMBER) 
            {
                
                activeParticipants.set(activeParticipantsIndex, 
                    participants.get(toBecomeActiveIndex));
                activeParticipants.get(latestActiveNonSpeakerIndex).
                    isCurrentlySpeaking = true;
                
                participants.remove(participantsIndex);
            } else 
            {
                activeParticipants.get(latestActiveNonSpeakerIndex).
                    isCurrentlySpeaking = true;
                activeParticipants.remove(activeParticipantsIndex);
                participants.remove(participantsIndex);
                
            }
        } else 
        {
            if (activeParticipantsIndex != -1) 
            {
                if (participants.size() > MAX_ACTIVE_PARTICIPANTS_NUMBER)
                {
                    activeParticipants.set(activeParticipantsIndex,
                        participants.get(toBecomeActiveIndex));
                    participants.remove(participantsIndex);
                } else {
                    activeParticipants.remove(activeParticipantsIndex);
                    participants.remove(participantsIndex);
                }
            } else 
            {
                if (participants.size() > MAX_ACTIVE_PARTICIPANTS_NUMBER)
                {
                    participants.remove(participantsIndex);
                } else {
                    //Not possible
                }
            }
        }
    }
    
    /**
     * Change the active speaker
     */
    public void speakerChange(int newSpeakerSSRC, int eventInstant)
    {
        int activeParticipantsIndex = -1, participantsIndex = -1;
        
        for (int i = 0; i < activeParticipants.size(); i++)
        {
            if (activeParticipants.get(i).SSRC == newSpeakerSSRC) 
            {
                activeParticipantsIndex = i;
            }
        }
        
        participantsIndex = findParticipantIndex(newSpeakerSSRC);
        
        if (participantsIndex == -1)
        {
            System.err.println("Not changing active speaker, because"
                + " participant not found (ssrc=" + newSpeakerSSRC + ")");
            return;
        }
        
        if (activeParticipantsIndex == -1)
        {
            int oldestActiveIndex = findOldestActiveParticipantIndex();
            activeParticipants.set(oldestActiveIndex, 
                participants.get(participantsIndex));
        }
        
        setCurrentSpeaker(newSpeakerSSRC, eventInstant);
    }
    
    /**
     * Returns a list containing the active participants
     */
    public List<ParticipantInfo> getActiveParticipantsList()
    {
        List<ParticipantInfo> result = new ArrayList<ParticipantInfo>();
        
        for (ParticipantInfo participant : activeParticipants) 
        {
            try 
            {
                ParticipantInfo clone = (ParticipantInfo) participant.clone();
                if (participant.isCurrentlySpeaking) 
                {
                    ParticipantInfo largeVideoClone = 
                        (ParticipantInfo) participant.clone();
                    result.add(largeVideoClone);
                }
                
                if (activeParticipants.size() > 1) 
                {
                    clone.isCurrentlySpeaking = false;
                    result.add(clone);
                }
            } catch (CloneNotSupportedException e) 
            {
                e.printStackTrace();
            }
        }
        
        return result;
    }
    
    /**
     * Find the active participant that has been inactive (i.e. non-speaking)
     * for the longest time (among all active participants)
     * @return the index of the active participant that has been inactive 
     * (i.e. non-speaking) for the longest time (among all active participants)
     */
    private int findOldestActiveParticipantIndex()
    {
        int resultIndex = -1, oldestInstant;
        
        oldestInstant = activeParticipants.size() == 0 ?
                        -1 : Integer.MAX_VALUE;
        
        for (int i = 0; i < activeParticipants.size(); i++) 
        {
            if (activeParticipants.get(i).lastActiveInstant < oldestInstant && 
                !activeParticipants.get(i).isCurrentlySpeaking) 
            {
                oldestInstant = activeParticipants.get(i).lastActiveInstant;
                resultIndex = i;
            }
        }
        
        return resultIndex;
    }
    
    /**
     * Find the active participant that isn't currently speaking and has been 
     * inactive (i.e. non-speaking) for the shortest time 
     * (among all active participants)
     * @return the index of the active participant that isn't currently speaking
     *  and has been inactive (i.e. non-speaking) for the shortest time (among 
     *  all active participants)
     */
    private int findLatestActiveNonSpeakerIndex()
    {
        int resultIndex = -1, latestInstant = -1;
        
        for (int i = 0; i < activeParticipants.size(); i++) 
        {
            if (activeParticipants.get(i).lastActiveInstant > latestInstant && 
                !activeParticipants.get(i).isCurrentlySpeaking) 
            {
                latestInstant = activeParticipants.get(i).lastActiveInstant;
                resultIndex = i;
            }
        }
        
        return resultIndex;
    }
    
    /**
     * Sets a participant as currently speaking
     * @param newSpeakerSSRC The SSRC of the participant that is to be set as
     * currently speaking
     * @param eventInstant The instant of the SPEAKER_CHANGED event that led
     * to this method invocation
     */
    private void setCurrentSpeaker(int newSpeakerSSRC, int eventInstant) 
    {
        for (ParticipantInfo participantInfo : participants) 
        {
            if (participantInfo.SSRC == newSpeakerSSRC) 
            {
                participantInfo.isCurrentlySpeaking = true;
            } else 
            {
                if (participantInfo.isCurrentlySpeaking)
                {
                    participantInfo.lastActiveInstant = eventInstant;
                }
                
                participantInfo.isCurrentlySpeaking = false;
            }
        }
    }
    
    /**
     * Finds the index of a participant in the active participants collection
     * @param participantSSRC the participant whose index we are looking for
     * @return the index of a participant in the active participants collection
     */
    private int getActiveParticipantsIndex(int participantSSRC) 
    {
        for (int i = 0; i < activeParticipants.size(); i++)
        {
            if (activeParticipants.get(i).SSRC == participantSSRC) 
            {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Finds the index of a participant in the participants collection
     * @param participantSSRC the participant whose index we are looking for
     * @return the index of a participant in the participants collection
     */
    private int findParticipantIndex(int participantSSRC)
    {
        for (int i = 0; i < participants.size(); i++)
        {
            if (participants.get(i).SSRC == participantSSRC) 
            {
                return i;
            }
        }
        
        return -1;
    }
}
