/************************************************************************************
 *     Copyright 2024 Roland Spatzenegger
 *     This file is part of LSimCore.
 *
 *     LSimCore is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     LSimCore is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with LSimCore.  If not, see <http://www.gnu.org/licenses/>.
 ************************************************************************************/
package net.npg.lsimcore.base

/**
 * Interface defining a logger that can be used to log messages with different levels and optional exceptions.
 */
interface Logger {
    /**
     * Logs a message with a given level and an optional exception.
     *
     * @param level The log level enumeration, indicating the severity of the message.
     * @param message A string representation of the message to be logged.
     * @param exception An optional Exception object that can provide more contextual information in case of errors.
     */
    fun log(level: LogLevel, message: String, exception: Exception? = null)
}

/**
 * Enum class representing different levels of logs based on their severity.
 */
enum class LogLevel {
    /**
     * Debug level logs, typically used for detailed information and debugging purposes.
     */
    DEBUG,

    /**
     * Info level logs, usually displaying informational messages that describe the progress of the application.
     */
    INFO,

    /**
     * Warning level logs, indicating a potentially problematic situation but not an error.
     */
    WARN,

    /**
     * Error level logs, representing significant errors or exceptions that should be addressed as soon as possible.
     */
    ERROR
}

/**
 * A Logger implementation that outputs log messages to the console.
 */
class StdoutLogger : Logger {
    /**
     * Logs a message with the specified level and optional exception.
     *
     * If an exception is provided, the method prints the log level, message, and stack trace to the console.
     * Otherwise, it only prints the log level and message.
     *
     * @param level The log level enumeration for the given message.
     * @param message A string representation of the message to be logged.
     * @param exception An optional Exception object that can provide more contextual information in case of errors.
     */
    override fun log(level: LogLevel, message: String, exception: Exception?) {
        if (exception == null) {
            println("$level: $message")
        } else {
            println("$level: $message ${exception.message}")
            println(exception.stackTraceToString())
        }
    }
}

/**
 * A Logger implementation that does nothing, effectively disabling logging.
 */
class NOPLogger : Logger {
    /**
     * logs nothing
     */
    override fun log(level: LogLevel, message: String, exception: Exception?) {
        // nop
    }
}