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
 * A text overlay. To get configured it needs to be set text, position and font 
 * size.
 * @author Vladimir Marinov
 *
 */
public class TextOverlay extends Overlay {
    /** The text to be shown */
    private String text;
    
    /** The font size to be used */
    private int fontSize;
    
    /**
     * Set the overlay text
     * @param text the overlay text
     */
    public void setText(String text)
    {
        this.text = text;
    }
    
    /**
     * Set the overlay text font size
     * @param fontSize the overlay text font size
     */
    public void setFontSize(int fontSize)
    {
        this.fontSize = fontSize;
    }
    
    @Override
    public String getFFMPEGVideoFilterString() 
    {
        String result = "";
        
        result = "drawtext=fontfile='resources/fonts/Avenir Next Condensed.ttc':"
                + "x=%s: y=%s: text='%s': fontcolor=white: fontsize=%s";
        result = String.format(result, position.x, position.y, text, 
                               fontSize);
        result = "[%s]" + result + "[%s]";
        
        return result;
    }

}
