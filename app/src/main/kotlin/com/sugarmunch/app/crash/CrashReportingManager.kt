package com.sugarmunch.app.crash

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.sugarmunch.app.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Crash Reporting Manager - Firebase Crashlytics integration
 * Provides centralized crash reporting and error tracking
 */
class CrashReportingManager private constructor(private val context: Context) {

    private val crashlytics = FirebaseCrashlytics.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val errorHistory = mutableListOf<ErrorEvent>()
    private val maxHistorySize = 100

    init {
        setupCrashlytics()
    }

    private fun setupCrashlytics() {
        // Set custom keys for better debugging
        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("version_code", BuildConfig.VERSION_CODE)
        crashlytics.setCustomKey("build_type", BuildConfig.BUILD_TYPE)
        crashlytics.setCustomKey("is_debug", BuildConfig.DEBUG)
    }

    /**
     * Record a non-fatal exception
     */
    fun recordException(
        throwable: Throwable,
        severity: Severity = Severity.ERROR,
        context: ErrorContext? = null
    ) {
        // Set severity
        crashlytics.recordException(throwable)
        
        // Add custom context
        context?.let {
            it.userId?.let { userId -> crashlytics.setUserId(userId) }
            it.screen?.let { screen -> crashlytics.setCustomKey("screen", screen) }
            it.action?.let { action -> crashlytics.setCustomKey("action", action) }
            it.additionalData.forEach { (key, value) ->
                crashlytics.setCustomKey(key, value.toString())
            }
        }

        // Track in history
        scope.launch {
            addToHistory(
                ErrorEvent(
                    type = throwable::class.java.simpleName,
                    message = throwable.message ?: "Unknown error",
                    severity = severity,
                    timestamp = System.currentTimeMillis(),
                    screen = context?.screen,
                    action = context?.action
                )
            )
        }
    }

    /**
     * Record a non-fatal exception with message
     */
    fun recordException(
        message: String,
        severity: Severity = Severity.ERROR,
        context: ErrorContext? = null
    ) {
        crashlytics.log("${severity.name}: $message")
        
        context?.let {
            it.screen?.let { screen -> crashlytics.setCustomKey("screen", screen) }
            it.action?.let { action -> crashlytics.setCustomKey("action", action) }
        }

        scope.launch {
            addToHistory(
                ErrorEvent(
                    type = "Manual",
                    message = message,
                    severity = severity,
                    timestamp = System.currentTimeMillis(),
                    screen = context?.screen,
                    action = context?.action
                )
            )
        }
    }

    /**
     * Log a message for debugging
     */
    fun log(message: String) {
        crashlytics.log(message)
    }

    /**
     * Set user ID for crash reports
     */
    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    /**
     * Set custom key-value pair
     */
    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * Set custom key-value pair (Int)
     */
    fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * Set custom key-value pair (Float)
     */
    fun setCustomKey(key: String, value: Float) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * Set custom key-value pair (Boolean)
     */
    fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * Set current screen name
     */
    fun setScreenName(screenName: String) {
        crashlytics.setCustomKey("current_screen", screenName)
    }

    /**
     * Enable/disable crash collection
     */
    fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }

    /**
     * Check if app crashed in previous session
     */
    fun didCrashOnPreviousExecution(): Boolean {
        return crashlytics.didCrashOnPreviousExecution()
    }

    /**
     * Get recent error history
     */
    fun getErrorHistory(): List<ErrorEvent> {
        return errorHistory.toList()
    }

    /**
     * Clear error history
     */
    fun clearErrorHistory() {
        errorHistory.clear()
    }

    /**
     * Get error summary for analytics
     */
    fun getErrorSummary(): ErrorSummary {
        val now = System.currentTimeMillis()
        val last24Hours = now - (24 * 60 * 60 * 1000)
        val last7Days = now - (7 * 24 * 60 * 60 * 1000)

        val errorsLast24Hours = errorHistory.count { it.timestamp >= last24Hours }
        val errorsLast7Days = errorHistory.count { it.timestamp >= last7Days }
        val criticalErrors = errorHistory.count { it.severity == Severity.CRITICAL }

        return ErrorSummary(
            totalErrors = errorHistory.size,
            errorsLast24Hours = errorsLast24Hours,
            errorsLast7Days = errorsLast7Days,
            criticalErrors = criticalErrors,
            mostCommonError = errorHistory
                .groupingBy { it.type }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key ?: "N/A"
        )
    }

    private fun addToHistory(event: ErrorEvent) {
        errorHistory.add(event)
        if (errorHistory.size > maxHistorySize) {
            errorHistory.removeAt(0)
        }
    }

    companion object {
        @Volatile
        private var instance: CrashReportingManager? = null

        fun getInstance(context: Context): CrashReportingManager {
            return instance ?: synchronized(this) {
                instance ?: CrashReportingManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * Error severity levels
 */
enum class Severity {
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}

/**
 * Context information for errors
 */
data class ErrorContext(
    val userId: String? = null,
    val screen: String? = null,
    val action: String? = null,
    val additionalData: Map<String, Any> = emptyMap()
)

/**
 * Error event for history tracking
 */
@Serializable
data class ErrorEvent(
    val type: String,
    val message: String,
    val severity: Severity,
    val timestamp: Long,
    val screen: String? = null,
    val action: String? = null
) {
    val formattedTime: String
        get() = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
}

/**
 * Error summary for analytics
 */
data class ErrorSummary(
    val totalErrors: Int,
    val errorsLast24Hours: Int,
    val errorsLast7Days: Int,
    val criticalErrors: Int,
    val mostCommonError: String
)
