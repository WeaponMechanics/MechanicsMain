package me.deecaad.core

import me.deecaad.core.file.SerializeData
import me.deecaad.core.file.Serializer
import me.deecaad.core.file.SerializerException
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEventSource
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.jvm.Throws

/**
 * Wraps a [Logger] and provides ways to:
 * - Configure which logging levels to print
 * - Alert operators (/op) when errors occur
 * -
 */
class MechanicsLogger(
    val plugin: MechanicsPlugin,
    var config: LoggerConfig,
) {
    private val playerAlerts = mutableMapOf<Player, Int>()
    private val alerts = mutableListOf<Alert>()

    init {
        logger.level = Level.ALL
    }

    fun start() {
        val listener = object : Listener {
            @EventHandler
            fun onPlayerJoin(event: org.bukkit.event.player.PlayerJoinEvent) {
                if (event.player.isOp)
                    playerAlerts[event.player] = 0
            }
        }
    }

    /**
     * Returns true if the specified level is allowed to be logged.
     */
    fun canLog(level: Level): Boolean {
        return config.printLevel.intValue() <= level.intValue()
    }

    fun handleAlerts() {
        for ((player, alertIndex) in playerAlerts) {
            if (alerts.size <= alertIndex)
                continue
            if (!player.isOnline)
                continue

            val newAlerts = alerts.subList(alertIndex, alerts.size)
            val clampedList = newAlerts.takeLast(config.maxAlertsToShow)

            val builder = Component.text()
            builder.append(Component.text(plugin.name).style(plugin.primaryColor).decorate(TextDecoration.BOLD))
            builder.append(Component.text(" has had ${newAlerts.size} new alerts:").style(plugin.secondaryColor))

            for (alert in clampedList) {
                val fullComponent = Component.text()
                for ((index, message) in alert.messages.withIndex()) {
                    if (index > 0)
                        fullComponent.appendNewline()
                    fullComponent.append(Component.text(message).style(plugin.secondaryColor))
                }

                builder.appendNewline()
                builder.append(Component.text("${alert.index}. ").style(plugin.primaryColor))
                builder.append(Component.text(alert.summary()).style(plugin.secondaryColor).hoverEvent(fullComponent.asComponent()))
            }

            val audience =
        }
    }

    /**
     * Logs a message with the specified level.
     *
     * @param level The logging level to log the message with
     * @param messages The messages to log
     * @param throwable An optional throwable to log
     */
    fun log(level: Level, vararg messages: String, throwable: Throwable? = null) {
        // Log the main message
        if (config.printLevel.intValue() <= level.intValue()) {
            for (message in messages) {
                logger.log(level, message)
            }

            if (throwable != null) {
                logger.log(level, "Stack trace: ", throwable)
            }
        }

        // This is typically used for debugging where a log call originates
        if (config.stackTracesLevel.intValue() <= level.intValue()) {
            val arbitraryException = Throwable()
            logger.log(level, "Stack trace: ", arbitraryException)
        }

        // Show alerts to operators
        if (config.alertLevel.intValue() <= level.intValue()) {
            val alert = Alert(level, alerts.size, messages.toList())
            alerts.add(alert)
        }
    }

    @JvmOverloads
    fun severe(message: String, throwable: Throwable? = null) {
        return log(Level.SEVERE, message, throwable = throwable)
    }

    fun severe(vararg messages: String, throwable: Throwable? = null) {
        return log(Level.SEVERE, *messages, throwable = throwable)
    }

    @JvmOverloads
    fun warning(message: String, throwable: Throwable? = null) {
        return log(Level.WARNING, message, throwable = throwable)
    }

    fun warning(vararg messages: String, throwable: Throwable? = null) {
        return log(Level.WARNING, *messages, throwable = throwable)
    }

    @JvmOverloads
    fun info(message: String, throwable: Throwable? = null) {
        return log(Level.INFO, message, throwable = throwable)
    }

    fun info(vararg messages: String, throwable: Throwable? = null) {
        return log(Level.INFO, *messages, throwable = throwable)
    }

    @JvmOverloads
    fun config(message: String, throwable: Throwable? = null) {
        return log(Level.CONFIG, message, throwable = throwable)
    }

    fun config(vararg messages: String, throwable: Throwable? = null) {
        return log(Level.CONFIG, *messages, throwable = throwable)
    }

    @JvmOverloads
    fun fine(message: String, throwable: Throwable? = null) {
        return log(Level.FINE, message, throwable = throwable)
    }

    fun fine(vararg messages: String, throwable: Throwable? = null) {
        return log(Level.FINE, *messages, throwable = throwable)
    }

    @JvmOverloads
    fun finer(message: String, throwable: Throwable? = null) {
        return log(Level.FINER, message, throwable = throwable)
    }

    fun finer(vararg messages: String, throwable: Throwable? = null) {
        return log(Level.FINER, *messages, throwable = throwable)
    }

    @JvmOverloads
    fun finest(message: String, throwable: Throwable? = null) {
        return log(Level.FINEST, message, throwable = throwable)
    }

    fun finest(vararg messages: String, throwable: Throwable? = null) {
        return log(Level.FINEST, *messages, throwable = throwable)
    }


    /**
     * The config options available to the logger.
     */
    class LoggerConfig : Serializer<LoggerConfig> {
        lateinit var printLevel: Level
        lateinit var stackTracesLevel: Level
        lateinit var alertLevel: Level
        var maxAlertsToShow: Int = 5
        var alertInterval: Int = 20 * 15

        /**
         * Default constructor for serializer
         */
        constructor()

        constructor(
            printLevel: Level = Level.CONFIG,
            stackTracesLevel: Level = Level.OFF,
            alertLevel: Level = Level.WARNING,
        ) {
            this.printLevel = printLevel
            this.stackTracesLevel = stackTracesLevel
            this.alertLevel = alertLevel
        }

        override fun getKeyword(): String {
            return "Logger_Config"
        }

        @Throws(SerializerException::class)
        private fun parseLevel(data: SerializeData, relative: String): Level {
            try {
                val level = data.of(relative).get(String::class.java).get()
                return Level.parse(level)
            } catch (e: Exception) {
                throw data.exception(relative, "Could not parse '$relative' to any valid logging level.",
                    "Expected a value like 'SEVERE', 'WARNING', 'INFO', 'CONFIG', 'FINE', 'FINER' FINEST'",
                )
            }
        }

        @Throws(SerializerException::class)
        override fun serialize(data: SerializeData): LoggerConfig {
            val printLevel = parseLevel(data, "Print_Level")
            val stackTracesLevel = parseLevel(data, "Stack_Traces_Level")
            val alertLevel = parseLevel(data, "Alert_Level")

            return LoggerConfig(printLevel, stackTracesLevel, alertLevel)
        }
    }

    /**
     * An alert message that can be sent to players.
     */
    data class Alert(val level: Level, val index: Int, val messages: List<String>) {
        /**
         * Returns a shortened version of the alert message, suitable for chat.
         */
        fun summary(): String {
            val estimatedChatWidth = 42
            val message = "Alert: ${level.name} - ${messages.joinToString(", ")}"
            val shortened = message.take(estimatedChatWidth)
            return if (shortened.length < message.length) {
                "$shortened..."
            } else {
                shortened
            }
        }
    }
}