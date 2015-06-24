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

package org.jitsi.recording.postprocessing.exception;

/**
 * An exception that indicates that an error occurred while running a command
 * line procedure.
 * @author Vladimir Marinov
 *
 */
public class CommandLineExecutionException extends RuntimeException {
    /** The (non-zero) exit code that was returned by the command line 
     * procedure 
     */
    private int exitCode;
    
    /**
     * The command line procedure that failed
     */
    private String command;
    
    public CommandLineExecutionException(int exitCode, String command) 
    {
        super("Command line execution exited with non-zero code \n"
            + "Exit code: " + exitCode + "\n"
            + "Command: " + command);
    }
}
