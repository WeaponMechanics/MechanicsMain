package me.deecaad.core.utils;

import java.util.logging.Level;

/**
 * This enum outlines the different levels that messages can be logged at. Less important messages
 * have larger numeric levels, and more important messages have smaller numeric levels.
 */
public enum LogLevel {

    INFO(1, Level.INFO),
    ERROR(1, Level.SEVERE),
    WARN(2, Level.WARNING),
    DEBUG(3, Level.INFO);

    private final int level;
    private final Level parallel;

    LogLevel(int level, Level parallel) {
        this.level = level;
        this.parallel = parallel;
    }

    /**
     * Returns <code>true</code> if this level can be logged to console, given the number
     * <code>level</code> from user input.
     *
     * @param level Non-negative numeric level. Should be defined by the user.
     * @return <code>true</code> if this level can be logged.
     */
    public boolean shouldPrint(int level) {
        return level >= this.level;
    }

    /**
     * Returns the java logging level that corresponds to this logging level.
     *
     * @return The non-null corresponding java logging level.
     */
    public Level getParallel() {
        return parallel;
    }
}
