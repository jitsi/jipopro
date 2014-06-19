package org.jitsi.recording.postprocessing.layout;

/**
 * A simple class that contains basic aspect ratio information.
 * @author vmarinov
 *
 */
public class AspectRatioUtil {
    public static final int ASPECT_RATIO_16_9 = 0;
    public static final int ASPECT_RATIO_4_3 = 1;
    
    /**
     * An array that contains the width/height ratio for every aspect ratio
     * that we support
     */
    public static final double[] scaleFactors = new double[2];
    
    static {
        scaleFactors[ASPECT_RATIO_16_9] = 16.0 / 9.0;
        scaleFactors[ASPECT_RATIO_4_3] = 4.0 / 3.0;
    }
}
