/**
 * Project: AndroidRelay
 * Author: Austin Hartline
 * Date: 12/7/14
 */

package edu.msoe.smv.raspirelay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;

/**
 * @author austin
 * @version 2014.12.07
 */
public class Logger {

    public static final String DEFAULT_LOG_DIR = "";

    // the log directory instance
    private File logFile;

    boolean verbose = false;

    private PrintWriter writer;

    /**
     * specify the log directory, specify whether or not to append if file exists
     * @param path log directory
     * @param append appends if true
     * @throws FileNotFoundException
     */
    public Logger(String path, boolean append) throws FileNotFoundException {
        logFile = new File(path);
        if (!append)
            clearLog();
        writer = new PrintWriter(logFile);
    }

    /**
     * specify the log directory, append to the file if exists by default
     * @param path log directory
     * @throws FileNotFoundException
     */
    public Logger(String path) throws FileNotFoundException {
        this(path, true);
    }

    /**
     * default log directory, specify whether or not to append if file exists
     * @param append appends if true
     * @throws FileNotFoundException
     */
    public Logger(boolean append) throws FileNotFoundException {
        this(DEFAULT_LOG_DIR, append);
    }

    /**
     * default log directory, append to the file if exists by default
     * @throws FileNotFoundException
     */
    public Logger() throws FileNotFoundException {
        this(DEFAULT_LOG_DIR, true);
    }

    /**
     * log some data
     * @param event data to log
     * @throws FileNotFoundException
     */
    public void logEvent(String event) throws FileNotFoundException {
        writer.println(getTimeStamp() + event);
    }

    /**
     * log some data if debugging is on
     * @param event data to log
     * @throws FileNotFoundException
     */
    public void logDebug(String event) throws FileNotFoundException {
        if (verbose) {
            writer.println(getTimeStamp() + event);
        }
    }

    public void clearLog() {
        try {
            // hacky magic dependent on printwriter clearing the file before writing, then close because file corruption is bad
            new PrintWriter(logFile).close();
        } catch (FileNotFoundException e) {
            // gulp
        }
    }

    private String getTimeStamp() {
        return new Date().toString();
    }
}
