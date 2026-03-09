package com.sugarmunch.app.util

import android.util.Log
import com.sugarmunch.app.security.SecurityConfig

/**
 * Secure logging utility for SugarMunch.
 *
 * This utility provides centralized logging with automatic respect for security
 * configuration. In production builds, verbose and debug logs are automatically
 * suppressed to prevent information leakage.
 *
 * Usage:
 * ```
 * private val logger = SecureLogger.create("MyTag")
 *
 * logger.d("Debug message")  // Only shown in debug builds
 * logger.i("Info message")   // Always shown
 * logger.w("Warning message") // Always shown
 * logger.e("Error message", exception) // Always shown with stack trace
 * ```
 *
 * For sensitive data:
 * ```
 * logger.s("Sensitive operation completed")  // Only in debug with explicit config
 * logger.s("User token: $token")  // Automatically suppressed in production
 * ```
 */
object SecureLogger {

    /**
     * Create a logger with the specified tag.
     * @param tag The tag to use for log messages (max 23 characters)
     */
    fun create(tag: String): Logger {
        // Android log tags are limited to 23 characters
        val truncatedTag = tag.take(23)
        return Logger(truncatedTag)
    }

    /**
     * Logger class for a specific tag.
     */
    class Logger(private val tag: String) {

        /**
         * Log a verbose message.
         * Only shown when verbose logging is enabled.
         */
        fun v(message: String, throwable: Throwable? = null) {
            if (SecurityConfig.ENABLE_VERBOSE_LOGGING) {
                Log.v(tag, message, throwable)
            }
        }

        /**
         * Log a debug message.
         * Only shown in debug builds.
         */
        fun d(message: String, throwable: Throwable? = null) {
            if (SecurityConfig.ENABLE_VERBOSE_LOGGING) {
                Log.d(tag, message, throwable)
            }
        }

        /**
         * Log an info message.
         * Always shown.
         */
        fun i(message: String, throwable: Throwable? = null) {
            Log.i(tag, message, throwable)
        }

        /**
         * Log a warning message.
         * Always shown.
         */
        fun w(message: String, throwable: Throwable? = null) {
            Log.w(tag, message, throwable)
        }

        /**
         * Log an error message.
         * Always shown with full stack trace.
         */
        fun e(message: String, throwable: Throwable? = null) {
            Log.e(tag, message, throwable)
        }

        /**
         * Log a sensitive message.
         * Only shown when explicitly configured for sensitive logging.
         * Never shown in production.
         */
        fun s(message: String, throwable: Throwable? = null) {
            if (SecurityConfig.ENABLE_VERBOSE_LOGGING && SecurityConfig.LOG_SENSITIVE_OPERATIONS) {
                Log.d("$tag-SENSITIVE", maskSensitiveData(message), throwable)
            }
        }

        /**
         * Log a security event.
         * Respects security logging configuration.
         */
        fun security(message: String, throwable: Throwable? = null) {
            SecurityConfig.logSecurityEvent(tag, message, sensitive = false)
        }

        /**
         * Log a sensitive security event.
         * Only logged when sensitive operation logging is enabled.
         */
        fun securitySensitive(message: String, throwable: Throwable? = null) {
            SecurityConfig.logSecurityEvent(tag, message, sensitive = true)
        }

        /**
         * Mask sensitive data in log messages.
         * Replaces common patterns with redacted versions.
         */
        private fun maskSensitiveData(message: String): String {
            return message
                // Mask email addresses
                .replace(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"), "[EMAIL_REDACTED]")
                // Mask phone numbers (basic pattern)
                .replace(Regex("\\+?[0-9]{1,3}?[-.\\s]?\\(?[0-9]{1,4}?\\)?[-.\\s]?[0-9]{1,4}[-.\\s]?[0-9]{1,9}"), "[PHONE_REDACTED]")
                // Mask potential API keys/tokens (long alphanumeric strings)
                .replace(Regex("\\b[A-Za-z0-9]{32,}\\b"), "[TOKEN_REDACTED]")
                // Mask potential passwords in key=value format
                .replace(Regex("(?i)(password|passwd|pwd|secret|token|api_key|apikey)\\s*=\\s*[^\\s]+"), "$1=[REDACTED]")
        }
    }

    /**
     * Extension function to log with reified type for generic logging.
     */
    inline fun <reified T> T.log(): Logger {
        val className = T::class.java.simpleName
        return create(className.ifEmpty { "SugarMunch" })
    }

    /**
     * Log a one-time message without creating a logger instance.
     * Useful for quick debugging.
     */
    fun log(tag: String, message: String, level: LogLevel = LogLevel.DEBUG) {
        when (level) {
            LogLevel.VERBOSE -> create(tag).v(message)
            LogLevel.DEBUG -> create(tag).d(message)
            LogLevel.INFO -> create(tag).i(message)
            LogLevel.WARNING -> create(tag).w(message)
            LogLevel.ERROR -> create(tag).e(message)
        }
    }
}

/**
 * Log levels for SecureLogger.
 */
enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARNING,
    ERROR
}

/**
 * Extension functions for common logging patterns.
 */

/**
 * Log a function call with timing.
 */
inline fun <T> SecureLogger.Logger.measureOperation(
    operationName: String,
    block: () -> T
): T {
    val startTime = System.currentTimeMillis()
    try {
        d("Starting: $operationName")
        return block()
    } catch (e: Exception) {
        e("Failed: $operationName (${System.currentTimeMillis() - startTime}ms)", e)
        throw e
    } finally {
        val duration = System.currentTimeMillis() - startTime
        d("Completed: $operationName (${duration}ms)")
    }
}

/**
 * Log entry and exit of a function.
 */
inline fun <T> SecureLogger.Logger.traceFunction(
    functionName: String,
    block: () -> T
): T {
    d("→ Enter: $functionName")
    try {
        val result = block()
        d("← Exit: $functionName")
        return result
    } catch (e: Exception) {
        e("✗ Exception: $functionName", e)
        throw e
    }
}

/**
 * Log a block execution with error handling.
 * Returns null if an exception occurs.
 */
inline fun <T> SecureLogger.Logger.safeExecute(
    operationName: String,
    block: () -> T
): T? {
    return try {
        d("Executing: $operationName")
        block()
    } catch (e: Exception) {
        e("Failed: $operationName", e)
        null
    }
}
