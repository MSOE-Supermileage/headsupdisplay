/**
 * Project: AndroidRelay
 * Author: Austin Hartline
 * Date: 12/7/14
 */

package edu.msoe.smv.raspirelay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * @author austin
 * @version 2014.12.07
 */
public class Logger {


    // the log directory instance
    private File logFile;

    /**
     * specify the log directory, specify whether or not to append if file exists
     *
     * @param path   log directory
     * @param append appends if true
     * @throws FileNotFoundException
     */
    public Logger(String path, boolean append) throws FileNotFoundException {
        logFile = new File(path);
        if (!append)
            clearLog();
    }

    /**
     * specify the log directory, append to the file if exists by default
     *
     * @param path log directory
     * @throws FileNotFoundException
     */
    public Logger(String path) throws FileNotFoundException {
        this(path, true);
    }

    /**
     * default log directory, specify whether or not to append if file exists
     *
     * @param append appends if true
     * @throws FileNotFoundException
     */
    public Logger(boolean append) throws FileNotFoundException {
        this(getDefaultLogDir(), append);
    }

    /**
     * default log directory, append to the file if exists by default
     *
     * @throws FileNotFoundException
     */
    public Logger() throws FileNotFoundException {
        this(getDefaultLogDir(), true);
    }

    /**
     * @return the default log directory (current directory)
     */
    private static String getDefaultLogDir() {
        // TODO
        return "";
    }

    /**
     * log some data
     *
     * @param event data to log
     * @throws FileNotFoundException
     */
    public void logEvent(String event) throws FileNotFoundException {
        // TODO
    }

    /**
     * log some data if debugging is on
     *
     * @param event data to log
     * @throws FileNotFoundException
     */
    public void logDebug(String event) throws FileNotFoundException {
        // TODO
    }

    /**
     * try to log, return false if can't
     *
     * @param event data to log
     * @return false if can't log
     */
    public boolean tryLogEvent(String event) {
        // TODO
        return false;
    }

    /**
     * try to log if debugging is on, return false if can't
     *
     * @param event data to log
     * @return false if can't log
     */
    public boolean tryLogDebug(String event) {
        // TODO
        return false;
    }

    public void clearLog() {
        try {
            // hacky magic dependent on printwriter clearing the file before writing, then close because file corruption is bad
            new PrintWriter(logFile).close();
        } catch (FileNotFoundException e) {
            // gulp
        }
    }
}
