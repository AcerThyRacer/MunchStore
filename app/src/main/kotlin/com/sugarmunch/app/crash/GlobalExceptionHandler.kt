package com.sugarmunch.app.crash

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.sugarmunch.app.util.SecureLogger
import com.sugarmunch.app.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

/**
 * Global Exception Handler - Catches uncaught exceptions
 * Integrates with Crashlytics and provides graceful error handling
 */
class GlobalExceptionHandler private constructor(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    private val crashReporter = CrashReportingManager.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private var currentActivity: Activity? = null
    private var activityStack = mutableListOf<String>()

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        scope.launch {
            try {
                // Log the exception (SecureLogger; Crashlytics gets full details)
                SecureLogger.create(TAG).e("Uncaught exception in thread ${thread.name}", throwable)

                // Record to Crashlytics
                FirebaseCrashlytics.getInstance().recordException(throwable)

                // Add context
                FirebaseCrashlytics.getInstance().setCustomKey("thread", thread.name)
                FirebaseCrashlytics.getInstance().setCustomKey("activity_stack", activityStack.joinToString(" > "))
                currentActivity?.let {
                    FirebaseCrashlytics.getInstance().setCustomKey("current_activity", it.localClassName)
                }

                // Add device info
                addDeviceInfo()

                // Save crash state
                saveCrashState(throwable)

            } catch (e: Exception) {
                SecureLogger.create(TAG).e("Error recording crash", e)
            } finally {
                // Pass to default handler (shows system crash dialog)
                defaultHandler?.uncaughtException(thread, throwable)
                
                // If in debug mode, don't kill the process immediately
                if (!BuildConfig.DEBUG) {
                    exitProcess(1)
                }
            }
        }

        // Give time for crash reporting
        Thread.sleep(2000)
    }

    /**
     * Track activity lifecycle for better crash context
     */
    fun registerActivityLifecycleCallbacks(application: Application) {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activityStack.add(activity.localClassName)
                currentActivity = activity
                crashReporter.setScreenName(activity.localClassName)
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
                crashReporter.setScreenName(activity.localClassName)
            }

            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {
                activityStack.remove(activity.localClassName)
                if (currentActivity == activity) {
                    currentActivity = activityStack.lastOrNull()?.let {
                        // Find the activity instance - simplified approach
                        null
                    }
                }
            }
        })
    }

    /**
     * Handle exception in coroutine context
     */
    fun handleCoroutineException(cause: Throwable, context: kotlin.coroutines.CoroutineContext) {
        scope.launch {
            SecureLogger.create(TAG).e("Coroutine exception", cause)
            
            crashReporter.recordException(
                throwable = cause,
                severity = Severity.ERROR,
                context = ErrorContext(
                    screen = currentActivity?.localClassName,
                    additionalData = mapOf(
                        "coroutine_context" to context.toString()
                    )
                )
            )
        }
    }

    /**
     * Report a caught exception (non-fatal)
     */
    fun reportCaughtException(
        throwable: Throwable,
        severity: Severity = Severity.ERROR,
        screen: String? = null,
        action: String? = null
    ) {
        scope.launch {
            crashReporter.recordException(
                throwable = throwable,
                severity = severity,
                context = ErrorContext(
                    screen = screen ?: currentActivity?.localClassName,
                    action = action
                )
            )
        }
    }

    /**
     * Report a handled error with custom message
     */
    fun reportError(
        message: String,
        severity: Severity = Severity.ERROR,
        screen: String? = null
    ) {
        scope.launch {
            crashReporter.recordException(
                message = message,
                severity = severity,
                context = ErrorContext(screen = screen)
            )
        }
    }

    private fun addDeviceInfo() {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCustomKey("device_model", android.os.Build.MODEL)
        crashlytics.setCustomKey("device_manufacturer", android.os.Build.MANUFACTURER)
        crashlytics.setCustomKey("android_version", android.os.Build.VERSION.RELEASE)
        crashlytics.setCustomKey("sdk_int", android.os.Build.VERSION.SDK_INT)
    }

    private fun saveCrashState(throwable: Throwable) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putLong(KEY_LAST_CRASH_TIME, System.currentTimeMillis())
            putString(KEY_LAST_CRASH_TYPE, throwable::class.java.name)
            putString(KEY_LAST_CRASH_MESSAGE, throwable.message)
            putInt(KEY_CRASH_COUNT, prefs.getInt(KEY_CRASH_COUNT, 0) + 1)
            apply()
        }
    }

    /**
     * Get crash statistics
     */
    fun getCrashStats(): CrashStats {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return CrashStats(
            lastCrashTime = prefs.getLong(KEY_LAST_CRASH_TIME, 0),
            lastCrashType = prefs.getString(KEY_LAST_CRASH_TYPE, "Unknown").orEmpty(),
            lastCrashMessage = prefs.getString(KEY_LAST_CRASH_MESSAGE, "").orEmpty(),
            totalCrashCount = prefs.getInt(KEY_CRASH_COUNT, 0)
        )
    }

    /**
     * Clear crash statistics
     */
    fun clearCrashStats() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    companion object {
        private const val TAG = "GlobalExceptionHandler"
        private const val PREFS_NAME = "sugarmunch_crash_stats"
        private const val KEY_LAST_CRASH_TIME = "last_crash_time"
        private const val KEY_LAST_CRASH_TYPE = "last_crash_type"
        private const val KEY_LAST_CRASH_MESSAGE = "last_crash_message"
        private const val KEY_CRASH_COUNT = "crash_count"

        @Volatile
        private var instance: GlobalExceptionHandler? = null

        fun initialize(context: Context): GlobalExceptionHandler {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            return instance ?: synchronized(this) {
                instance ?: GlobalExceptionHandler(context.applicationContext, defaultHandler).also {
                    instance = it
                }
            }
        }

        fun getInstance(): GlobalExceptionHandler {
            return instance ?: throw IllegalStateException("GlobalExceptionHandler not initialized")
        }
    }
}

/**
 * Crash statistics data class
 */
data class CrashStats(
    val lastCrashTime: Long,
    val lastCrashType: String,
    val lastCrashMessage: String,
    val totalCrashCount: Int
) {
    val lastCrashFormatted: String
        get() = if (lastCrashTime > 0) {
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date(lastCrashTime))
        } else "Never"
}
