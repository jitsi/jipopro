/*
 * Jipopro, the Jitsi Post-Processing application for recorded conferences.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
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
