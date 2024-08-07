package me.deecaad.core.utils;

import com.cjcrafter.scheduler.FoliaCompatibility;
import com.cjcrafter.scheduler.ServerImplementation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * This class outlines an easier to use version of a {@link Logger}. Debuggers have the advantage of
 * only displaying specific messages based on which {@link LogLevel} is currently being used. This
 * means that debug messages are only shown when the user wants to see debug messages, and errors
 * are shown if users want errors to be shown.
 *
 * @see LogLevel
 */
public class Debugger {

    // Which logger to use to log information,
    // no logical reason to be able to change this
    private final Logger logger;
    private int level;
    private final boolean isPrintTraces;
    private int errors;

    public String msg = "MechanicsPlugin had %s error(s) in console.";
    public String permission = "mechanicscore.errorlog";
    public long updateTime = 300L;
    private boolean hasStarted;

    public Debugger(Logger logger, int level) {
        this(logger, level, false);
    }

    public Debugger(Logger logger, int level, boolean isPrintTraces) {
        this.logger = logger;
        this.level = level;
        this.isPrintTraces = isPrintTraces;
    }

    /**
     * Returns the backing {@link Logger} that belongs to the plugin that instantiated this debugger.
     *
     * @return The non-null {@link Logger}
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Gets the current numeric logging level that was input by the user.
     *
     * @return The non-negative numeric logging level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets the numeric logging that. This level should be set by the user.
     *
     * @param level The non-negative numeric logging level.
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Returns <code>true</code> if the given {@link LogLevel} can be logged based on the currently set
     * numeric logging level.
     *
     * @param level The non-null {@link LogLevel} to test.
     * @return <code>true</code> if the <code>level</code> can be logged.
     */
    public boolean canLog(LogLevel level) {
        return level.shouldPrint(this.level);
    }

    /**
     * Shorthand for using {@link #log(LogLevel, String...)} at debugging level.
     *
     * @param msg The non-null messages to log.
     * @see LogLevel#DEBUG
     */
    public void debug(String... msg) {
        if (canLog(LogLevel.DEBUG))
            log(LogLevel.DEBUG, msg);
    }

    /**
     * Shorthand for using {@link #log(LogLevel, String...)} at information level.
     *
     * @param msg The non-null messages to log.
     * @see LogLevel#INFO
     */
    public void info(String... msg) {
        if (canLog(LogLevel.INFO))
            log(LogLevel.INFO, msg);
    }

    /**
     * Shorthand for using {@link #log(LogLevel, String...)} at warning level.
     *
     * @param msg The non-null messages to log.
     * @see LogLevel#WARN
     */
    public void warn(String... msg) {
        if (canLog(LogLevel.WARN))
            log(LogLevel.WARN, msg);
    }

    /**
     * Shorthand for using {@link #log(LogLevel, String...)} at error level.
     *
     * @param msg The non-null messages to log.
     * @see LogLevel#ERROR
     */
    public void error(String... msg) {
        if (canLog(LogLevel.ERROR))
            log(LogLevel.ERROR, msg);
    }

    /**
     * Logs the given messages to console at the given {@link LogLevel}.
     *
     * @param level The non-null level to log the messages.
     * @param msg The non-null messages to log.
     */
    public void log(LogLevel level, String... msg) {
        if (!canLog(level))
            return;

        for (String str : msg) {
            logger.log(level.getParallel(), str);
        }

        // Used if we want to find the origin of an error
        if (level == LogLevel.ERROR || level == LogLevel.WARN) {
            if (isPrintTraces) {
                log(level, new Throwable());
            }

            // Only alert users about actual errors
            if (level == LogLevel.ERROR)
                errors++;
        }
    }

    /**
     * Logs the given exception to console at the given {@link LogLevel}.
     *
     * @param level The non-null level to log the messages.
     * @param error The non-null exception to log.
     */
    public void log(LogLevel level, Throwable error) {
        if (!canLog(level))
            return;

        logger.log(level.getParallel(), "", error);
    }

    /**
     * Logs the given message and the given exception at the given {@link LogLevel}. If the exception
     * does not have a message, the <code>msg</code> is used as the message. Otherwise, they are logged
     * separately.
     *
     * @param level The non-null level to log the messages.
     * @param msg The message to log.
     * @param error The exception to log.
     */
    public void log(LogLevel level, String msg, Throwable error) {
        if (!canLog(level))
            return;

        logger.log(level.getParallel(), msg, error);
    }

    /**
     * Logs the given messages as a {@link LogLevel#ERROR} if the given <code>bool</code> is
     * <code>false</code>.
     *
     * @param bool The condition to check for. If this is <code>true</code>, the messages are not
     *        logged.
     * @param messages The messages to log as an error.
     */
    public void validate(boolean bool, String... messages) {
        if (!bool) {
            log(LogLevel.ERROR, messages);
        }
    }

    /**
     * Logs the given messages at the given {@link LogLevel} if the given <code>bool</code> is
     * <code>false</code>.
     *
     * @param level The logging level to log the messages.
     * @param bool The condition to check for. If this is <code>true</code>, the messages are not
     *        logged.
     * @param messages The messages to log.
     */
    public void validate(LogLevel level, boolean bool, String... messages) {
        if (!bool) {
            log(level, messages);
        }
    }

    /**
     * Starts the warning runnable which warns opped users if an error occurs in console.
     *
     * @param plugin The plugin to schedule the task.
     */
    @Deprecated
    public synchronized void start(Plugin plugin) {
        if (hasStarted)
            return;

        ServerImplementation impl = new FoliaCompatibility(plugin).getServerImplementation();
        start(impl);
    }

    public synchronized void start(ServerImplementation impl) {
        if (hasStarted)
            return;

        impl.async().runAtFixedRate(task -> {
            if (errors > 0) {
                boolean alertedPlayer = false;

                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (!player.hasPermission(permission)) {
                        continue;
                    }

                    alertedPlayer = true;
                    player.sendMessage(ChatColor.RED + String.format(msg, errors));
                }

                if (alertedPlayer) {
                    errors = 0;
                }
            }
        }, 10L, updateTime);

        hasStarted = true;
    }
}
