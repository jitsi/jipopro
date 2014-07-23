/*
 * Jipopro, the Jitsi Post-Processing application for recorded conferences.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package org.jitsi.recording.postprocessing;

import java.util.*;

/**
 * Contains configuration variables for the post processing application.
 *
 * @author Boris Grozev
 * @author Vladimir Marinov
 */
public class Config
{
    /** The command to use to execute ffmpeg. */
    public static String FFMPEG = "ffmpeg";

    /**
     * Additional environment variables to set when executing external
     * commands.
     */
    public static Map<String,String> ENV_VARIABLES = new HashMap<String,String>();
    static
    {
        //ENV_VARIABLES.put("LD_LIBRARY_PATH", "value");
    }

    /** Constant for the WEBM format of the output file. */
    static final int WEBM_OUTPUT_FORMAT = 0;

    /** Constant for the MP4 format of the output file. */
    static final int MP4_OUTPUT_FORMAT = 1;

    /** The quality level that FFMPEG will use when processing the videos. */
    public static final int QUALITY_LEVEL = 8;

    /** The name of the file containing the metadata. */
    static String METADATA_FILENAME = "metadata.json";

    /**
     * The name of the file containing information about endpoints.
     */
    static String ENDPOINTS_FILENAME = "endpoints.json";

    /** The name of the file where logs will be stored. */
    static String LOG_FILENAME = "jipopro.log";

    /** Height of the the output video. */
    public static int OUTPUT_HEIGHT = 720;

    /** Width of the output video. */
    public static int OUTPUT_WIDTH = (int) (OUTPUT_HEIGHT * 16 / 9);

    /** Format of the output video. */
    static int OUTPUT_FORMAT = MP4_OUTPUT_FORMAT;

    /** Frame rate of the output video */
    public static int OUTPUT_FPS = 25;
    
    /** The amount of CPU that FFMPEG will use */
    public static int FFMPEG_CPU_USED = 16;
    
    /** The number of threads that FFMPEG will use */
    public static int FFMPEG_THREADS = 3;
    
    /**
     * Number of threads Jipopro will use in order to process simultaneously
     * sections and video files decoding 
     */
    public static int JIPOPRO_THREADS = 3;

    /**
     * Whether or not to use MKVINFO to calculate webm file duration.
     */
    public static boolean USE_MKVINFO = true;

    static final String IN_ARG_NAME = "--in=";
    static final String OUT_ARG_NAME = "--out=";
    static final String RESOURCES_ARG_NAME = "--resources=";

    public static final boolean USE_PARTICIPANT_IMAGES = true;
}
