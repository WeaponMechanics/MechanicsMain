package me.deecaad.core.utils;

import java.util.logging.Level;

public enum LogLevel {
    
    INFO(1, Level.INFO),
    ERROR(1, Level.SEVERE),
    WARN(2, Level.WARNING),
    DEBUG(3, Level.INFO);
    
    private final int level;
    private final Level parallel;
    
    /**
     * Constructs a log level
     *
     * @param level The integer level. Greater means finer detail
     * @param parallel The logging level for loggers
     */
    LogLevel(int level, Level parallel) {
        this.level = level;
        this.parallel = parallel;
    }
    
    public boolean shouldPrint(int level) {
        return level >= this.level;
    }
    
    public Level getParallel() {
        return parallel;
    }
}
