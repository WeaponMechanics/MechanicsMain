package me.deecaad.core.utils;

import java.util.Arrays;
import java.util.logging.Logger;

public class DebugUtil {
    
    // public in case anybody wants to mess with the logger
    public static Logger logger = null;
    
    /**
     * Don't let anyone instantiate this class
     */
    private DebugUtil() { }
    
    /**
     * Logs all given messaged at the given level
     * Each msg goes on a new line
     *
     * @param level The level to log at
     * @param msg The messages
     */
    public static void log(LogLevel level, String...msg) {
        Arrays.stream(msg).forEach(string -> logger.log(level.getParallel(), string));
    }
    
    /**
     * Logs the given error and gives the user the
     * given message.
     *
     * @param level Level to log at
     * @param msg Message to send
     * @param error Error
     */
    public static void log(LogLevel level, String msg, Throwable error) {
        logger.log(level.getParallel(), msg, error);
    }
    
    /**
     * Shorthand for asserting true with an <code>ERROR</code>
     * <code>LoggingLevel</code>.
     *
     * @param bool What to assert
     * @param messages Messages to log if assertion failed
     */
    public static void assertTrue(boolean bool, String...messages) {
        assertTrue(LogLevel.ERROR, bool, messages);
    }
    
    /**
     * Easy way to assert variables cleanly. If <code>bool</code>
     * is true, than there is no error, the method exits. If it
     * is false, than there is an error, and the given messages
     * should be logged at the given level so the user can be
     * aware of possible errors.
     *
     * @param level Level to log at
     * @param bool What to assert
     * @param messages Messages to log if assertion failed
     */
    public static void assertTrue(LogLevel level, boolean bool, String...messages) {
        if (!bool) {
            log(level, messages);
        }
    }
}
