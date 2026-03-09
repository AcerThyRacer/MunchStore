package com.sugarmunch.app

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.sugarmunch.app.crash.CrashReportingManager
import com.sugarmunch.app.crash.GlobalExceptionHandler
import com.sugarmunch.app.util.StrictModeManager
import com.sugarmunch.app.ai.SmartCacheManager
import com.sugarmunch.app.work.AnalyticsSyncWorker
import com.sugarmunch.app.work.BackgroundSyncWorker
import com.sugarmunch.app.work.CacheWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * SugarMunchApplication - Main application class
 *
 * Initializes:
 * - Crash reporting (Firebase Crashlytics)
 * - Global exception handling
 * - StrictMode (debug builds)
 * - WorkManager for background tasks
 * - SmartCacheManager for AI-powered caching
 * - Background sync schedules
 */
@HiltAndroidApp
class SugarMunchApplication : Application(), Configuration.Provider {

    // Application-wide coroutine scope
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize crash reporting and error handling
        initializeCrashReporting()
        
        // Initialize StrictMode (debug only)
        StrictModeManager.enable()

        // Initialize background workers
        initializeWorkManager()

        // Log app start
        FirebaseCrashlytics.getInstance().log("Application started")
    }

    /**
     * Initialize crash reporting and global exception handling
     */
    private fun initializeCrashReporting() {
        // Initialize global exception handler
        val exceptionHandler = GlobalExceptionHandler.initialize(this)
        exceptionHandler.registerActivityLifecycleCallbacks(this)

        // Initialize crash reporting manager
        CrashReportingManager.getInstance(this)

        // Set up coroutine exception handler
        setupCoroutineExceptionHandler()
    }

    /**
     * Set up global coroutine exception handler
     */
    private fun setupCoroutineExceptionHandler() {
        val handler = GlobalExceptionHandler.getInstance()
        
        // Set as default coroutine exception handler
        kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
            handler.handleCoroutineException(throwable, kotlin.coroutines.EmptyCoroutineContext)
        }
    }

    /**
     * Initialize WorkManager and schedule background tasks
     */
    private fun initializeWorkManager() {
        // Schedule manifest sync
        BackgroundSyncWorker.schedule(this)

        // Schedule cache maintenance
        CacheWorker.scheduleMaintenance(this)

        // Schedule periodic cache warming (runs when charging + WiFi)
        CacheWorker.schedulePeriodicWarming(this)

        // Schedule analytics sync
        AnalyticsSyncWorker.schedule(this)
    }

    /**
     * Provide custom WorkManager configuration
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // Log memory warning to Crashlytics
        FirebaseCrashlytics.getInstance().log("Low memory warning")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Log memory trim level to Crashlytics
        FirebaseCrashlytics.getInstance().setCustomKey("memory_trim_level", level)
    }

    companion object {
        /**
         * Initialize SmartCacheManager with available apps
         * Call this after catalog is loaded
         */
        fun initializePredictiveCache(context: Context, availableApps: List<com.sugarmunch.app.data.AppEntry>) {
            val cacheManager = SmartCacheManager.getInstance(context)

            // Launch in background using application scope
            (context.applicationContext as? SugarMunchApplication)?.applicationScope?.launch {
                // Generate initial predictions
                cacheManager.predictNextDownloads(availableApps)

                // Try to warm cache if conditions are good
                cacheManager.performSmartCacheWarming()
            }
        }

        @Volatile
        private var instance: SugarMunchApplication? = null

        /**
         * Get application instance. Must be called after [onCreate]; otherwise throws.
         */
        fun getInstance(): SugarMunchApplication {
            return instance ?: throw IllegalStateException("SugarMunchApplication not initialized yet")
        }
    }
}
