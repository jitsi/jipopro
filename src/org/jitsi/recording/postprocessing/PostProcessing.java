package org.jitsi.recording.postprocessing;
import java.io.*;
import java.util.*;
import java.util.List; //Disambiguation
import java.util.concurrent.*;

import org.jitsi.service.neomedia.*;
import org.jitsi.service.neomedia.recording.*;
import org.jitsi.service.neomedia.recording.RecorderEvent.*;
import org.jitsi.recording.postprocessing.section.*;
import org.jitsi.recording.postprocessing.util.*;
import org.jitsi.recording.postprocessing.video.concat.*;
import org.jitsi.recording.postprocessing.layout.*;
import org.jitsi.recording.postprocessing.participant.*;
import org.json.simple.*;


/**
 * A unit that processes videos recorded by the video recorder. It reads a
 * metadata file, and the audio and video files described in it, and produces
 * a single combined file.
 *
 * @author Vladimir Marinov
 * @author Boris Grozev
 */
public class PostProcessing
{
    /**
     * Duration of a single video frame.
     */
    private static final int SINGLE_FRAME_DURATION = 1000 / Config.OUTPUT_FPS;

    /**
     * The minimum duration that a section can have (the difference between
     * two consecutive event instants) in order for it to get processed.
     */
    private static final int MINIMUM_SECTION_DURATION = 100;

    /**
     * The length of video trimming result is usually bigger than the section
     * we wanted to get. This leads to error accumulation that we store in
     * this variable in order to later compensate it
     */
    private static int videoDurationError = 0;
    
    /**
     * We need to fade in only videos of participants that join the call
     * after the recording has started.
     */
    private static boolean hasProcessedEvents = false;
    
    /**
     * A list of participants that are currently shown.
     * */
    private static List<ParticipantInfo> activeParticipants;
    
    /**
     * An instance that determines how the active participants should be
     * placed in the output video.
     */
    private static final LayoutStrategy layoutStrategy
            = new SmallVideosOverLargeVideoLayoutStrategy();
    
    /**
     * An instance that takes care of concatenating the video files that are
     * stored in a specific directory.
     */
    private static final ConcatStrategy concatStrategy
            = new ParallelConcatStrategy();
    
    /**
     * An instance that determines which participants are currently active
     * and what is their order in the list of small videos
     */
    private static ActiveParticipantsManager activeParticipantsManager =
        new WithSpeakerInVideosListParticipantsManager();

    /**
     * The instant of the event that was last read from the metadata file
     */
    private static int eventInstant = 0;
    /**
     * The instant of the event that was previously read from the metadata file
     */
    private static int lastEventInstant = 0;
    /**
     * The instant the first video started
     */
    private static int firstVideoStartInstant = -1;
    
    /**
     * A task queue responsible for decoding the input video files into MJPEG
     * files.
     */
    private static ExecutorService decodingTaskQueue =
        Executors.newFixedThreadPool(Config.JIPOPRO_THREADS);
    
    /**
     * A task queue responsible for processing the separate call sections.
     */
    private static ExecutorService sectionProcessingTaskQueue =
            Executors.newFixedThreadPool(Config.JIPOPRO_THREADS);

    private static long processingStarted;
    
    public static void main(String[] args)
        throws IOException,
               InterruptedException
    {
        processingStarted = System.currentTimeMillis();
        initLogFile();

        if (!sanityCheck())
            return;

        layoutStrategy.initialize(Config.OUTPUT_WIDTH,
                                  Config.OUTPUT_HEIGHT);

        int sectionNumber = 0;


        // Read the metadata file.
        JSONObject metadataJSONObject = null;
        Scanner scanner = new Scanner(new File(Config.METADATA_FILENAME));
        String metadataString = scanner.useDelimiter("\\Z").next();
        scanner.close();

        metadataJSONObject = (JSONObject) JSONValue.parse(metadataString);
        if (metadataJSONObject == null)
        {
            log("Failed to parse metadata from "
                                 + Config.METADATA_FILENAME + ". Broken json?");
            return;
        }

        List<RecorderEvent> videoEvents = extractEvents(metadataJSONObject,
                                                        MediaType.VIDEO);
        if (videoEvents == null)
        {
            return; //error already logged
        }
        time("Extracted video events (calculated durations)");

        // Decode videos
        decodeParticipantVideos(videoEvents);
        time("Finished decoding videos");


        // And now the magic begins :)
        long firstVideoStartInstantLong = -1;
        for (RecorderEvent event : videoEvents)
        {
            int instant = (int) event.getInstant();
            //XXX Boris this is bound to lead to problems (or at least make
            // debugging harder). Please keep SSRCs in long-s.
            int ssrc = (int) event.getSsrc();

            if (event.getType() != RecorderEvent.Type.RECORDING_STARTED &&
                firstVideoStartInstant == -1)
            {
                continue;
            }
            
            if (firstVideoStartInstant == -1) 
            {
                firstVideoStartInstant = instant;
                firstVideoStartInstantLong = event.getInstant();
            }
            eventInstant = instant - firstVideoStartInstant;
            
            //Once we read an event from the metadata file we process the videos 
            // files from the previous event instant to the current event
            // instant
            if (eventInstant != 0 &&
                eventInstant - lastEventInstant
                        >= MINIMUM_SECTION_DURATION)
            {
                System.err.println("Processing event: " + event);
                //processLastEvent(eventInstant, lastEventInstant);
                SectionDescription sectionDesc = new SectionDescription();
                sectionDesc.activeParticipants = activeParticipants;
                layoutStrategy.calculateDimensions(activeParticipants);
                sectionDesc.largeVideoDimension = 
                    layoutStrategy.getLargeVideoDimensions();
                sectionDesc.smallVideosDimensions = 
                    layoutStrategy.getSmallVideosDimensions();
                sectionDesc.smallVideosPositions = 
                    layoutStrategy.getSmallVideosPositions();
                sectionDesc.sequenceNumber = sectionNumber;
                sectionDesc.startInstant = lastEventInstant;
                videoDurationError += 
                (eventInstant - lastEventInstant) % SINGLE_FRAME_DURATION;
                int sectionDurationCorrection = 0;
                if (videoDurationError > SINGLE_FRAME_DURATION)
                {
                    sectionDurationCorrection = -SINGLE_FRAME_DURATION;
                    videoDurationError -= SINGLE_FRAME_DURATION;
                }
                sectionDesc.endInstant = 
                    eventInstant + sectionDurationCorrection;
                
                sectionProcessingTaskQueue.execute(
                    new SectionProcessingTask(sectionDesc));
                
                sectionNumber++;
                hasProcessedEvents = true;
                lastEventInstant = eventInstant;
            }
            else if (eventInstant - lastEventInstant >= MINIMUM_SECTION_DURATION)
            {
                System.err.println("Ignoring an event because it's too close"
                                   + "to the previous: " + event.getType()
                                   + ", " + event.getSsrc() + " " + event);
            }

            switch (event.getType())
            {
                case RECORDING_STARTED:
                    int newParticipantSSRC = ssrc;
                    ParticipantInfo participant = 
                            new ParticipantInfo(newParticipantSSRC);
                    participant.currentVideoFileStartInstant = eventInstant;
                    participant.lastActiveInstant = eventInstant;
                    //Needs refactoring
                    participant.aspectRatio = 
                        event.getAspectRatio() == AspectRatio.ASPECT_RATIO_16_9 ?
                        AspectRatioUtil.ASPECT_RATIO_16_9 :
                        AspectRatioUtil.ASPECT_RATIO_4_3;
                    participant.fileName = event.getFilename();
                    participant.username = event.getParticipantName();

                    //XXX Boris: if an event doesn't have a participantName
                    //it now returns null instead of "". This should probably be
                    //fixed somewhere else (participant.setUsername()?)
                    if (participant.username == null)
                        participant.username = "";
                    participant.description = event.getParticipantDescription();
                    if (participant.description == null)
                        participant.description = "";
                    participant.disableOtherVideosOnTop = 
                        event.getDisableOtherVideosOnTop();
                    activeParticipantsManager.addParticipant(participant);
                    break;
                case RECORDING_ENDED:
                    int participantToRemove = ssrc;
                    activeParticipantsManager.
                        removeParticipant(participantToRemove);
                    break;
                case SPEAKER_CHANGED:
                    int speakerSSRC = ssrc;
                    activeParticipantsManager.
                        speakerChange(speakerSSRC, eventInstant);
                    break;
                case OTHER:
                    return;
            }
            
            activeParticipants = 
                activeParticipantsManager.getActiveParticipantsList();
            removeSmallVideosIfDisabled();
            
            if (activeParticipants == null || activeParticipants.size() == 0) 
            {
                break; //video all done.
            }
        }

        sectionProcessingTaskQueue.shutdown();
        try
        {
            sectionProcessingTaskQueue.awaitTermination(
                    Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            time("Sections processed");
        }
        catch (InterruptedException e)
        {
            log("Failed to process sections: "+e);
            e.printStackTrace();
            //should we continue?
        }

        String videoFilename = "output.mov";
        concatStrategy.concatFiles("sections", "", videoFilename);
        time("Sections concatenated");
        Exec.exec("rm -rf sections");

        // Handle audio
        List<RecorderEvent> audioEvents
                = extractEvents(metadataJSONObject, MediaType.AUDIO);
        if (audioEvents == null)
            return; //error already logged

        String audioMix = "resultAudio.wav";
        long firstAudioInstant = mixAudio(audioEvents, audioMix);
        time("Audio mixed");


        long audioOffset = 0, videoOffset = 0;
        long diff = firstAudioInstant - firstVideoStartInstantLong;
        if (diff > 0)
            audioOffset = diff;
        else if (diff < 0)
            videoOffset = -diff;

        merge(audioMix, audioOffset, videoFilename, videoOffset, videoFilename);
        time("Audio and video merged");

        String finalResult = encodeResultVideo(videoFilename);
        time("Result encoded");
        log("All done, result saved in " + finalResult
                + ". And it took only " +
                millisToSeconds(System.currentTimeMillis()-processingStarted)
                + " seconds.");

        Exec.closeLogFile();
    }

    private static void time(String s)
    {
        log("[TIME] " + s + ": "
                    + millisToSeconds(
                        System.currentTimeMillis() - processingStarted));

    }

    /**
     * Merges and audio and video file, using the given offsets.
     * @param audioFilename the name of the audio file.
     * @param audioStartOffset the offset at which audio should start in the
     * merged file.
     * @param videoFilename the name of the video file.
     * @param videoStartOffset the offset at which video should start in the
     * merged file.
     * @param outputFilename the name of the file where the result should be
     * stored.
     * @throws InterruptedException
     * @throws IOException
     */
    private static void merge(String audioFilename,
                                 long audioStartOffset,
                                 String videoFilename,
                                 long videoStartOffset,
                                 String outputFilename)
            throws InterruptedException,
                   IOException
    {
        Exec.exec(Config.FFMPEG + " -y"
                          + " -itsoffset " + millisToSeconds(videoStartOffset)
                          + " -i " + videoFilename
                          + " -itsoffset " + millisToSeconds(audioStartOffset)
                          + " -i " + audioFilename
                          + " -codec copy temp.mov");
        Exec.exec("mv " + videoFilename + " output-no-sound.mov"); //keep for debugging

        // use temp.mov to allow videoFilename == outputFilename
        Exec.exec("mv temp.mov " + outputFilename);
    }

    /**
     * Mixes the audio according the the events in <tt>audioEvents</tt>
     * @param audioEvents the list of audio events.
     * @param outputFilename the name of the file where to store the mix.
     * @return the instant of the first event which is included in the mix.
     * @throws InterruptedException
     * @throws IOException
     */
    private static long mixAudio(List<RecorderEvent> audioEvents,
                                   String outputFilename)
            throws InterruptedException,
                   IOException
    {
        long nextAudioFileInstant;
        long firstAudioFileInstant = 0;

        String[] filenames = new String[audioEvents.size()];
        long[] padding = new long[audioEvents.size()];
        int i = 0;
        for (RecorderEvent event : audioEvents)
        {
            nextAudioFileInstant = event.getInstant();

            if (event.getType() == Type.RECORDING_STARTED)
            {
                // workaround a current problem with the recorder which leaves
                // empty files. also, sox chokes on small files
                int minAudioFileSize = 4000;
                File file = new File(event.getFilename());
                if (!file.exists() || file.length() < minAudioFileSize)
                    continue;

                if (i == 0)
                {
                    firstAudioFileInstant = nextAudioFileInstant;
                }
                else
                {
                    padding[i] = nextAudioFileInstant - firstAudioFileInstant;
                }

                filenames[i] = event.getFilename();

                i++;
            }
        }

        Exec.exec("mkdir -p audio_tmp");

        // the first file is just converted to wav
        Exec.exec("sox " + filenames[0] + " audio_tmp/padded0.wav");

        // the rest need padding
        for (int j = 1; j < i; j++)
            Exec.exec("sox " + filenames[j] + " audio_tmp/padded" + j
                      + ".wav pad " + millisToSeconds(padding[j]));

        String exec = "sox --combine mix-power ";
        for (int j = 0; j < i; j++)
            exec += "audio_tmp/padded" + j + ".wav ";
        exec += outputFilename;
        Exec.exec(exec);

        Exec.exec("rm -rf audio_tmp");

        return firstAudioFileInstant;
    }

    /**
     * Extracts a list of <tt>RecorderEvent</tt> with a specific media type
     * from JSON format.
     * @param json the JSON object containing all events in the recorder
     * metadata format.
     * @param mediaType the media type specifying which events to extract.
     * @return A list of <tt>RecorderEvent</tt>, ordered by "instant".
     */
    private static List<RecorderEvent> extractEvents(JSONObject json,
                                                     MediaType mediaType)
    {
        Object array = json.get(mediaType.toString());
        if (array == null || !(array instanceof JSONArray))
        {
            log("Failed to extract events from metadata, mediaType="
                        + mediaType + "; json:\n" +json.toJSONString());
            return null;
        }

        List<RecorderEvent> eventList = new LinkedList<RecorderEvent>();
        for (Object o : (JSONArray) array)
        {
            RecorderEvent event = new RecorderEvent((JSONObject) o);
            MediaType eventMediaType = event.getMediaType();
            Type eventType = event.getType();

            // For video, we generate RECORDING_ENDED events on our own, based
            // on the actual length of the video files.
            if (MediaType.VIDEO.equals(eventMediaType))
            {
                if (Type.RECORDING_ENDED.equals(eventType))
                    continue;
                else if (Type.RECORDING_STARTED.equals(eventType))
                {
                    //Insert a RECORDING_ENDED event for this file
                    try
                    {
                        long duration = getVideoDurationMillis(event.getFilename());
                        RecorderEvent newEndedEvent = new RecorderEvent();
                        newEndedEvent.setType(Type.RECORDING_ENDED);
                        newEndedEvent.setInstant(event.getInstant() + duration);
                        newEndedEvent.setMediaType(MediaType.VIDEO);
                        newEndedEvent.setSsrc(event.getSsrc());
                        newEndedEvent.setFilename(event.getFilename());
                        eventList.add(newEndedEvent);
                    }
                    catch (Exception e)
                    {
                        log("Failed to insert RECORDING_ENDED event: "
                            + e);
                        return null; // is it safe to continue here?
                    }

                }
            }

            Collections.sort(eventList, new Comparator<RecorderEvent>() {
                @Override
                public int compare(RecorderEvent o1, RecorderEvent o2)
                {
                   return (int) (o1.getInstant() - o2.getInstant());
                }
            });

            eventList.add(event);
        }

        return eventList;
    }

    /**
     * Logs a message.
     * @param s the message to log.
     */
    private static void log(String s)
    {
        System.err.println(s);
    }
    
    /**
     * Extracts duration of a video file using ffprobe
     * @return the duration of the webm file <tt>filename</tt> in milliseconds.
     * @param filename the video file which duration is about to be extracted
     * @throws InterruptedException 
     * @throws IOException 
     */
    private static long getVideoDurationMillis(String filename)
            throws IOException, InterruptedException {
        String videoInfoFilename = "video_info.txt";
        //note: this is slow
        String exec = "ffprobe -v quiet -print_format json=c=1 -show_frames " +
                filename + " | tail -n 3 | head -n 1 | tee " + videoInfoFilename;
        long videoDuration = 0;
        
        Exec.execArray(new String[] { "bash", "-c", exec });
        
        try 
        {
            JSONObject videoInfo = (JSONObject)
                JSONValue.parse(new FileReader(videoInfoFilename));
            videoDuration = (Long) videoInfo.get("pkt_pts");
        } catch (FileNotFoundException e) 
        {
            e.printStackTrace();
        }

        return videoDuration;
    }

    /**
     * Prevent small videos from showing
     */
    private static void removeSmallVideosIfDisabled() {
        for (ParticipantInfo participant : activeParticipants)
        {
            if (participant.isCurrentlySpeaking && 
                participant.disableOtherVideosOnTop)
            {
                activeParticipants = new ArrayList<ParticipantInfo>();
                activeParticipants.add(participant);
                break;
            }
        }
    }

    /** Encodes the result video in the chosen file format 
     * @throws InterruptedException 
     * @throws IOException */
    private static String encodeResultVideo(String inputFilename)
        throws IOException, InterruptedException 
    {
        String outputFilename
                = trimFileExtension(inputFilename);

        if (Config.OUTPUT_FORMAT == Config.WEBM_OUTPUT_FORMAT)
        {
            outputFilename += ".webm";
            Exec.exec(Config.FFMPEG + " -y -i " + inputFilename + " -c:v libvpx "
                    + "-cpu-used " + Config.FFMPEG_CPU_USED +  " -threads " + 
                    Config.FFMPEG_THREADS + " -b:v 1M " + outputFilename);

            return outputFilename;
        }
        else if (Config.OUTPUT_FORMAT == Config.MP4_OUTPUT_FORMAT)
        {
            outputFilename += ".mp4";
            Exec.exec(Config.FFMPEG + " -y -i " + inputFilename + " -vcodec "
                              + "libx264 " + "-acodec copy " + outputFilename);

            return outputFilename;
        }

        return null;
    }

    /** Decodes an input video file and encodes it using MJPEG
     */
    private static void decodeParticipantVideoFile(String participantFileName) 
        throws IOException, InterruptedException 
    {
        String fadeFilter = "";
        if (hasProcessedEvents) 
        {
            fadeFilter = "-vf fade=in:st=0:d=1:color=black ";
        }
        
        Exec.exec(
            Config.FFMPEG + " -y -vcodec libvpx -i " + participantFileName +
            " -vcodec mjpeg -cpu-used " + Config.FFMPEG_CPU_USED +  
            " -threads " + Config.FFMPEG_THREADS + " -an -q:v " + Config.QUALITY_LEVEL + " " +
            "-r " + Config.OUTPUT_FPS + " " +
            fadeFilter + trimFileExtension(participantFileName) + ".mov");
    }

    private static void decodeParticipantVideos(List<RecorderEvent> videoEvents)
    {
        for (RecorderEvent event : videoEvents)
        {
            if (event.getType() == Type.RECORDING_STARTED)
            {
                final String filename = event.getFilename();
                decodingTaskQueue.execute(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            decodeParticipantVideoFile(filename);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        decodingTaskQueue.shutdown();
        try
        {
            decodingTaskQueue.awaitTermination(Long.MAX_VALUE,
                                               TimeUnit.NANOSECONDS);
        }
        catch (InterruptedException e)
        {
            log("Faied to decode participant videos: " + e);
            e.printStackTrace();
        }

    }

    /** Converts time in milliseconds to a String representing the time in 
     * seconds in the format XX.XXX
     * @param millis the time in milliseconds that we want to convert
     * @return a String representing the time in seconds in the format XX.XXX
     */
    private static String millisToSeconds(long millis)
    {
        String result = new String();
        String complement = "";
        long fraction = Math.abs(millis) % 1000;
        
        if (fraction < 10)
        {
            complement = "00";
        } else if (fraction < 100)
        {
            complement = "0";
        }
        
        result += millis / 1000;
        result += ".";
        result += complement + fraction;
        
        return result;
    
    }

    /** Removes the file extension from a file name
     * @param fileName the file name which extension we want to trim
     * @return the trimmed file name
     */
    private static String trimFileExtension(String fileName) 
    {
        int index = fileName.lastIndexOf('.');
        return fileName.substring(0, index);
    }

    /** Perform some initial tests and fail early if they fail. */
    private static boolean sanityCheck()
    {
        Runtime runtime = Runtime.getRuntime();
        Process p;
        int ret;

        try
        {
            p = runtime.exec("which phantomjs");
            ret = p.waitFor();
            if (ret != 0)
            {
                System.err.println("Cannot find 'phantomjs' executable.");
                return false;
            }

            p = runtime.exec("which " + Config.FFMPEG);
            ret = p.waitFor();
            if (ret != 0)
            {
                System.err.println("Cannot find 'ffmpeg' executable.");
                return false;
            }

            BufferedReader reader
                = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String ffmpeg = reader.readLine();
            System.err.println("Using ffmpeg: " + ffmpeg);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        if (!new File(Config.METADATA_FILENAME).exists())
        {
            System.err.println("Metadata file " + Config.METADATA_FILENAME
                + " does not exist.");
            return false;
        }

        return true;
    }

    private static void initLogFile()
    {
        File logFile = new File(Config.LOG_FILENAME);
        boolean fail = false;
        Exception e = null;

        try
        {
            if (!logFile.exists())
                logFile.createNewFile();
        }
        catch (IOException ioe)
        {
            fail = true;
            e = ioe;
        }

        if (!logFile.canWrite())
            fail = true;

        if (fail)
        {
            System.err.println("Could not open log file for writing."
                    + " Continuing without a log file.\n"
                    + (e == null ? "" : e));
        }
        else
        {
            Exec.setLogFile(logFile);
        }
    }
}
