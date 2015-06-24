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

/**
 * A simple class that contains basic aspect ratio information.
 * @author Vladimir Marinov
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
