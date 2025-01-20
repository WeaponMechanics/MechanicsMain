package me.deecaad.core

import me.deecaad.core.file.SerializeData
import me.deecaad.core.file.Serializer
import java.util.logging.Level
import java.util.logging.Logger

class MechanicsLogger(
    val logger: Logger,
    val config: LoggerConfig,
) {

    var errorCounter

    fun severe(vararg messages: String, throwable: Throwable? = null) {
        if (config.printLevel.intValue() <= Level.SEVERE.intValue()) {
            for (message in messages) {
                logger.log(Level.SEVERE, message)
            }

            if (throwable != null) {
                logger.log(Level.SEVERE, "Stack trace: ", throwable)
            }
        }

        // This is typically used for debugging where a log call originates
        if (config.stackTracesLevel.intValue() <= Level.SEVERE.intValue()) {
            val arbitraryException = Throwable()
            logger.log(Level.SEVERE, "Stack trace: ", arbitraryException)
        }

    }

    fun warning(vararg messages: String, throwable: Throwable? = null) {
        if (config.printLevel.intValue() <= Level.WARNING.intValue()) {
            for (message in messages) {
                logger.log(Level.WARNING, message)
            }
        }
    }

    fun config(vararg messages: String) {
        if (config.printLevel.intValue() <= Level.CONFIG.intValue()) {
            for (message in messages) {
                logger.log(Level.CONFIG, message)
            }
        }
    }


    class LoggerConfig : Serializer<LoggerConfig> {
        lateinit var printLevel: Level
        lateinit var stackTracesLevel: Level

        /**
         * Default constructor for serializer
         */
        constructor()

        constructor(
            printLevel: Level = Level.CONFIG,
            stackTracesLevel: Level = Level.CONFIG,
        ) {
            this.printLevel = printLevel
            this.stackTracesLevel = stackTracesLevel
        }

        override fun getKeyword(): String {
            return "Logger_Config"
        }

        override fun serialize(data: SerializeData): LoggerConfig {
            val level: Level
            try {
                level = Level.parse(data.of("Print_Level").assertExists().get(String::class.java).get())
            } catch (e: Exception) {
                throw data.exception("Print_Level", "Could not parse 'Print_Level' to any valid logging level.",
                    "Expected a value like 'INFO', 'WARNING', 'SEVERE', etc.",
                    "We suggest using 'CONFIG' here")
            }
            val stackTracesLevel: Level
            try {
                stackTracesLevel = Level.parse(data.of("Stack_Traces_Level").assertExists().get(String::class.java).get())
            } catch (e: Exception) {
                throw data.exception("Stack_Traces_Level", "Could not parse 'Print_Level' to any valid logging level.",
                    "Expected a value like 'INFO', 'WARNING', 'SEVERE', etc.",
                    "We suggest using 'CONFIG' here")
            }
        }
    }
}