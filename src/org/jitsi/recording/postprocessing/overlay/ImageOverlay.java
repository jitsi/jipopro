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

package org.jitsi.recording.postprocessing.overlay;

/**
 * An image overlay. It needs path to the image to be set as well as image
 * dimensions and position.
 * @author Vladimir Marinov
 *
 */
public class ImageOverlay extends Overlay {
    /**
     * Path on the file system to the image to be overlayed
     */
    private String path;
    
    /**
     * Set the path to the image
     * @param path the path to the image
     */
    public void setPath(String path)
    {
        this.path = path;
    }
    
    @Override
    public String getFFMPEGVideoFilterString() 
    {
        String result = "", movieFilter = "";
        
        result = "[%s]overlay=%s:%s";
        result = String.format(result, "img" + hashCode(), position.x,
                position.y);
        movieFilter = "movie='%s', scale=%s:%s[%s];";
        movieFilter = String.format(movieFilter, path, dimension.width, 
                dimension.height, "img" + hashCode(), "%s");
        result = movieFilter + "[%s]" + result + "[%s]";
        
        return result;
    }

}
