/*
 * Jipopro, the Jitsi Post-Processing application for recorded conferences.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package org.jitsi.recording.postprocessing.util;

import org.jitsi.recording.postprocessing.*;
import org.jitsi.recording.postprocessing.exception.*;

import java.io.*;
import java.util.*;

/**
 * A utility class that allows to execute commands and optionally save
 * their <tt>stdout</tt> and <tt>stderr</tt> to a log file.
 *
 * @author Boris Grozev
 */
public class Exec
{
    private static File logFile = null;
    private static FileWriter logWriter = null;

    /**
     * Sets the <tt>File</tt> instance to be used for saving the output of
     * commands which we execute.
     * @param logFile the <tt>File</tt> to use.
     */
    public static void setLogFile(File logFile)
    {
        if (logWriter != null)
        {
            try
            {
                logWriter.close();
            }
            catch (IOException ioe)
            {
                System.err.println("Failed to close log file writer." + ioe);
            }
        }
        if (logFile != null)
        {
            try
            {
                Exec.logFile = logFile;

                //remove the contents of the file...
                new FileWriter(logFile).close();

                Exec.logWriter = new FileWriter(logFile, true);
                System.err.println("Now saving logs to "+logFile.getAbsolutePath());
            }
            catch (IOException ioe)
            {
                System.err.println("Failed to create FileWriter for log file. Going" +
                                           " on without a log file. " + ioe);
            }
        }
        else
        {
            Exec.logFile = null;
            logWriter = null;
        }
    }

    /**
     * Closes the file used for writing logs.
     */
    public static void closeLogFile()
    {
        setLogFile(null);
    }

    /**
     * Executes a command, saving it's <tt>stderr</tt> and <tt>stdout</tt>.
     *
     * The command is specified in the string <tt>command</tt> and it will
     * be split using a space character, ignoring any punctuation. Thus, it is
     * not possible to have arguments which contain space characters, even if
     * quotes are used. For such cases use the variants of {@link #execArray}
     * or {@link #execList}
     */
    public static void exec(String command)
            throws IOException, InterruptedException
    {
        exec(true, command);
    }

    /**
     * Executes a command, optionally saving it's <tt>stderr</tt> and
     * <tt>stdout</tt> according to <tt>saveLog</tt>.
     *
     * The command is specified in the string <tt>command</tt> and it will
     * be split using a space character, ignoring any punctuation. Thus, it is
     * not possible to have arguments which contain space characters, even if
     * quotes are used. For such cases the variants of {@link #execArray}
     * or {@link #execList} can be used.
     */
    public static void exec(boolean saveLog, String command)
            throws IOException, InterruptedException
    {
        execArray(saveLog, command.split(" "));
    }

    /**
     * Executes a command, saving it's <tt>stderr</tt> and <tt>stdout</tt>.
     */
    public static void execList(List<String> list)
            throws IOException, InterruptedException
    {
        execList(true, list);
    }

    /**
     * Executes a command, optionally saving it's <tt>stderr</tt> and
     * <tt>stdout</tt> according to <tt>saveLog</tt>.
     */
    public static void execList(boolean saveLog, List<String> list)
            throws IOException, InterruptedException
    {
        execArray(saveLog, list.toArray(new String[list.size()]));
    }

    /**
     * Executes a command, saving it's <tt>stderr</tt> and <tt>stdout</tt>.
     */
    public static void execArray(String ... command)
            throws IOException, InterruptedException
    {
        execArray(true, command);
    }

    /**
     * Executes a command, optionally saving it's <tt>stderr</tt> and
     * <tt>stdout</tt> according to <tt>saveLog</tt>.
     */
    public static void execArray(boolean saveLog, String ... command)
        throws IOException, InterruptedException
    {
        String commandStr = "";
        for (String s : command)
            commandStr += s + " ";
        System.err.println("[EXEC] " + commandStr);

        ProcessBuilder pb = new ProcessBuilder(command);
        if (!Config.ENV_VARIABLES.isEmpty())
            pb.environment().putAll(Config.ENV_VARIABLES);
        Process p = pb.start();

        if (saveLog && logFile != null && logWriter != null)
        {
            logWriter.write("[EXEC] " + commandStr + '\n');
            new Thread(new Writer(p.getInputStream())).start();
            new Thread(new Writer(p.getErrorStream())).start();
        }

        int exitCode = p.waitFor();

        if (exitCode != 0)
        {
            if (logWriter != null)
                logWriter.flush();
            throw new CommandLineExecutionException(exitCode, commandStr);
        }
    }

    /**
     * Implements a <tt>Runnable</tt> which saves an <tt>InputStream</tt>
     * using {@link Exec#logWriter}.
     */
    private static class Writer
        implements Runnable
    {
        private InputStream stream;

        private Writer(InputStream stream)
        {
            this.stream = stream;

        }

        @Override
        public void run()
        {
            FileWriter logWriter = Exec.logWriter;
            if (stream != null && logWriter != null)
            {
                try
                {
                    BufferedReader br
                        = new BufferedReader(new InputStreamReader(stream));
                    String line = null;
                    while ( (line = br.readLine()) != null)
                    {
                        logWriter.write(line + '\n');
                    }
                }
                catch (IOException ioe)
                {
                    System.err.println("Failed to write log. " + ioe);
                }
            }
        }
    }
}
