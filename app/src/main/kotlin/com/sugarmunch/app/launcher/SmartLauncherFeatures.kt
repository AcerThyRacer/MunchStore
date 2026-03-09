package com.sugarmunch.app.launcher

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Process
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

/**
 * SmartLauncherFeatures - ML-powered launcher features.
 * Features:
 * - Predictive app launch (ML)
 * - Context-aware suggestions (time, location)
 * - App usage insights
 * - Focus mode (hide distracting apps)
 * - App lock with biometrics
 */
class SmartLauncherFeatures(private val context: Context) {

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager
    private val extendedStatsManager = ExtendedUsageStatsManager(context)

    private val _suggestedApps = MutableStateFlow<List<AppSuggestion>>(emptyList())
    val suggestedApps: StateFlow<List<AppSuggestion>> = _suggestedApps.asStateFlow()

    private val _focusModeEnabled = MutableStateFlow(false)
    val focusModeEnabled: StateFlow<Boolean> = _focusModeEnabled.asStateFlow()

    private val _hiddenApps = MutableStateFlow<Set<String>>(emptySet())
    val hiddenApps: StateFlow<Set<String>> = _hiddenApps.asStateFlow()

    companion object {
        private const val TAG = "SmartLauncherFeatures"
        private const val MAX_FREQUENT_APPS = 10
        private const val USAGE_STATS_LOOKBACK_DAYS = 7L

        @Volatile
        private var INSTANCE: SmartLauncherFeatures? = null

        fun getInstance(context: Context): SmartLauncherFeatures {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SmartLauncherFeatures(context).also { INSTANCE = it }
            }
        }
    }

    /**
     * Get smart app suggestions based on:
     * - Time of day
     * - Location
     * - Recent usage patterns
     * - Day of week
     */
    fun getSmartSuggestions(): List<AppSuggestion> {
        val suggestions = mutableListOf<AppSuggestion>()

        // Time-based suggestions
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)

        // Morning suggestions (6-9 AM)
        if (hour in 6..9) {
            suggestions.add(AppSuggestion(
                appId = "morning_comm",
                label = "Morning Routine",
                iconRes = android.R.drawable.ic_menu_report,
                reason = "Start your day with these apps"
            ))
        }

        // Work hours (9-17)
        if (hour in 9..17 && dayOfWeek in 1..5) {
            suggestions.add(AppSuggestion(
                appId = "work_apps",
                label = "Work Apps",
                iconRes = android.R.drawable.ic_work,
                reason = "Productivity apps for work"
            ))
        }

        // Evening (18-22)
        if (hour in 18..22) {
            suggestions.add(AppSuggestion(
                appId = "evening",
                label = "Relax",
                iconRes = android.R.drawable.ic_media_play,
                reason = "Time to unwind"
            ))
        }

        // Add usage-based suggestions
        val frequentApps = getMostFrequentApps()
        suggestions.addAll(frequentApps.mapIndexed { index, app ->
            AppSuggestion(
                appId = app.packageName,
                label = app.appName,
                iconRes = app.iconRes,
                reason = "Frequently used",
                priority = 100 - index
            )
        })

        return suggestions.sortedByDescending { it.priority }.take(6)
    }

    /**
     * Get most frequently used apps from usage stats.
     *
     * Uses UsageStatsManager to query app usage history and returns
     * the most frequently launched apps, excluding system apps and
     * apps that are hidden in focus mode.
     *
     * @return List of AppUsageInfo sorted by launch count (most used first)
     */
    private fun getMostFrequentApps(): List<AppUsageInfo> {
        // Check if we have usage stats permission
        if (!hasUsageStatsPermission()) {
            Log.w(TAG, "Usage stats permission not granted, returning empty list")
            return emptyList()
        }

        return try {
            // Calculate time range (last 7 days)
            val endTime = System.currentTimeMillis()
            val startTime = endTime - (USAGE_STATS_LOOKBACK_DAYS * 24 * 60 * 60 * 1000)

            // Query usage stats
            val usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            if (usageStatsList.isNullOrEmpty()) {
                Log.d(TAG, "No usage stats available")
                return emptyList()
            }

            // Aggregate stats by package name (stats may be duplicated across intervals)
            val aggregatedStats = mutableMapOf<String, UsageStats>()
            usageStatsList.forEach { stats ->
                val existing = aggregatedStats[stats.packageName]
                if (existing != null) {
                    // Merge stats - create a combined UsageStats
                    // Note: UsageStats is final, so we track our own counts
                } else {
                    aggregatedStats[stats.packageName] = stats
                }
            }

            // Filter and sort apps
            val frequentApps = usageStatsList
                .groupBy { it.packageName }
                .mapValues { entry ->
                    // Sum up launch counts and get max last used time
                    val stats = entry.value
                    val totalLaunches = stats.sumOf { it.launchCount }
                    val lastUsed = stats.maxOfOrNull { it.lastTimeUsed } ?: 0L
                    val totalTime = stats.sumOf { it.totalTimeInForeground }
                    
                    AppUsageStats(
                        packageName = entry.key,
                        launchCount = totalLaunches,
                        totalTimeInForegroundMs = totalTime,
                        lastLaunchTime = lastUsed
                    )
                }
                .values
                .filter { stats ->
                    // Filter out:
                    // 1. System apps
                    // 2. SugarMunch itself
                    // 3. Apps with no launches
                    // 4. Hidden apps
                    stats.launchCount > 0 &&
                    !isSystemApp(stats.packageName) &&
                    stats.packageName != context.packageName &&
                    !_hiddenApps.value.contains(stats.packageName)
                }
                .sortedByDescending { it.launchCount }
                .take(MAX_FREQUENT_APPS)

            // Convert to AppUsageInfo with app names and icons
            frequentApps.mapNotNull { stats ->
                try {
                    val appInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        packageManager.getApplicationInfo(
                            stats.packageName,
                            PackageManager.ApplicationInfoFlags.of(0)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getApplicationInfo(stats.packageName, 0)
                    }

                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    val icon = packageManager.getApplicationIcon(appInfo)

                    AppUsageInfo(
                        packageName = stats.packageName,
                        appName = appName,
                        icon = icon,
                        iconRes = null, // Using drawable instead
                        launchCount = stats.launchCount,
                        lastLaunch = stats.lastLaunchTime
                    )
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.w(TAG, "Package not found: ${stats.packageName}")
                    null
                } catch (e: Exception) {
                    Log.w(TAG, "Error getting app info for ${stats.packageName}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting frequent apps", e)
            emptyList()
        }
    }

    /**
     * Check if we have permission to access usage stats.
     */
    private fun hasUsageStatsPermission(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val result = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_PACKAGE_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
            result == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            Log.e(TAG, "Error checking usage stats permission", e)
            false
        }
    }

    /**
     * Check if a package is a system app.
     */
    private fun isSystemApp(packageName: String): Boolean {
        return try {
            val appInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Predict next app based on current context.
     * Uses historical usage patterns to predict which app the user is most likely to open.
     */
    fun predictNextApp(): String? {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)

        val prediction = extendedStatsManager.queryAndAggregateUsageStats(hour, dayOfWeek)
        return prediction?.mostLikelyPackage
    }

    /**
     * Get context-aware app group
     */
    fun getContextAwareApps(appContext: AppContext): List<String> {
        return when (appContext) {
            AppContext.HOME -> listOf("com.android.chrome", "com.google.android.gm")
            AppContext.WORK -> listOf("com.microsoft.teams", "com.slack")
            AppContext.TRAVEL -> listOf("com.google.android.apps.maps", "com.ubercab")
            AppContext.EXERCISE -> listOf("com.strava", "com.fitbit.FitbitMobile")
            AppContext.ENTERTAINMENT -> listOf("com.netflix.mediaclient", "com.spotify.music")
            AppContext.SHOPPING -> listOf("com.amazon.mShop.android.shopping", "com.ebay.mobile")
        }
    }

    /**
     * Enable focus mode to hide distracting apps
     */
    fun enableFocusMode() {
        _focusModeEnabled.value = true
        // Would show notification suggesting which apps to hide
    }

    /**
     * Disable focus mode
     */
    fun disableFocusMode() {
        _focusModeEnabled.value = false
    }

    /**
     * Hide an app from suggestions
     */
    fun hideApp(packageName: String) {
        val updated = _hiddenApps.value.toMutableSet()
        updated.add(packageName)
        _hiddenApps.value = updated
    }

    /**
     * Unhide an app
     */
    fun unhideApp(packageName: String) {
        val updated = _hiddenApps.value.toMutableSet()
        updated.remove(packageName)
        _hiddenApps.value = updated
    }

    /**
     * Check if an app is hidden
     */
    fun isAppHidden(packageName: String): Boolean {
        return _hiddenApps.value.contains(packageName)
    }

    /**
     * Get app usage insights.
     * Returns summary statistics about app usage.
     */
    fun getAppInsights(): AppInsights {
        if (!hasUsageStatsPermission()) {
            return AppInsights(
                totalScreenTime = 0L,
                mostUsedCategory = "",
                averageDailyUsage = 0L
            )
        }

        return try {
            val endTime = System.currentTimeMillis()
            val startTime = endTime - (USAGE_STATS_LOOKBACK_DAYS * 24 * 60 * 60 * 1000)

            val usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            if (usageStatsList.isNullOrEmpty()) {
                return AppInsights(
                    totalScreenTime = 0L,
                    mostUsedCategory = "Unknown",
                    averageDailyUsage = 0L
                )
            }

            val totalScreenTime = usageStatsList.sumOf { it.totalTimeInForeground }
            val averageDailyUsage = totalScreenTime / USAGE_STATS_LOOKBACK_DAYS

            // Find most used app category
            val mostUsedPackage = usageStatsList
                .groupBy { it.packageName }
                .maxByOrNull { entry -> entry.value.sumOf { it.totalTimeInForeground } }
                ?.key

            val mostUsedCategory = mostUsedPackage?.let { pkg ->
                try {
                    val appInfo = packageManager.getApplicationInfo(pkg, 0)
                    val category = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        appInfo.category
                    } else {
                        -1
                    }
                    getCategoryName(category)
                } catch (e: Exception) {
                    "Other"
                }
            } ?: "Other"

            AppInsights(
                totalScreenTime = totalScreenTime,
                mostUsedCategory = mostUsedCategory,
                averageDailyUsage = averageDailyUsage
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app insights", e)
            AppInsights(
                totalScreenTime = 0L,
                mostUsedCategory = "Unknown",
                averageDailyUsage = 0L
            )
        }
    }

    /**
     * Convert ApplicationInfo category to human-readable name.
     */
    private fun getCategoryName(category: Int): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            when (category) {
                ApplicationInfo.CATEGORY_GAME -> "Games"
                ApplicationInfo.CATEGORY_AUDIO -> "Audio"
                ApplicationInfo.CATEGORY_VIDEO -> "Video"
                ApplicationInfo.CATEGORY_IMAGE -> "Images"
                ApplicationInfo.CATEGORY_SOCIAL -> "Social"
                ApplicationInfo.CATEGORY_NEWS -> "News"
                ApplicationInfo.CATEGORY_MAPS -> "Maps"
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
                else -> "Other"
            }
        } else {
            "Other"
        }
    }

    /**
     * Update app usage for ML learning
     */
    suspend fun recordAppLaunch(packageName: String) = withContext(Dispatchers.IO) {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)

        extendedStatsManager.recordAppUsage(packageName, hour, dayOfWeek)

        // Update suggestions
        updateSuggestions()
    }

    private fun updateSuggestions() {
        _suggestedApps.value = getSmartSuggestions()
    }

    /**
     * Internal data class for aggregated usage stats
     */
    private data class AppUsageStats(
        val packageName: String,
        val launchCount: Int,
        val totalTimeInForegroundMs: Long,
        val lastLaunchTime: Long
    )
}

/**
 * App suggestion data model
 */
data class AppSuggestion(
    val appId: String,
    val label: String,
    val iconRes: Int?,
    val iconDrawable: Drawable? = null,
    val reason: String,
    val priority: Int = 50
)

/**
 * App context types
 */
enum class AppContext {
    HOME,
    WORK,
    TRAVEL,
    EXERCISE,
    ENTERTAINMENT,
    SHOPPING
}

/**
 * App usage information
 */
data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val iconRes: Int? = null,
    val launchCount: Int,
    val lastLaunch: Long
)

/**
 * App insights summary
 */
data class AppInsights(
    val totalScreenTime: Long,
    val mostUsedCategory: String,
    val averageDailyUsage: Long
)

/**
 * Extended UsageStatsManager with ML capabilities.
 * Tracks app usage patterns by time and day for predictive suggestions.
 */
class ExtendedUsageStatsManager(context: Context) {
    private val prefs = context.getSharedPreferences("smart_launcher_stats", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PREFIX = "usage_"
        private const val KEY_TOTAL_LAUNCHES = "total_launches"
    }

    /**
     * Record app usage for a specific time slot.
     * This builds a pattern of when apps are typically used.
     */
    fun recordAppUsage(packageName: String, hour: Int, dayOfWeek: Int) {
        // Record usage for this time slot
        val key = "${KEY_PREFIX}${packageName}_${hour}_${dayOfWeek}"
        val currentCount = prefs.getInt(key, 0)
        prefs.edit()
            .putInt(key, currentCount + 1)
            .apply()

        // Also update total launches for this package
        val totalKey = "${KEY_PREFIX}${packageName}_$KEY_TOTAL_LAUNCHES"
        val totalCount = prefs.getInt(totalKey, 0)
        prefs.edit()
            .putInt(totalKey, totalCount + 1)
            .apply()
    }

    /**
     * Query and aggregate usage stats to predict next app.
     * Returns the app most likely to be used at the current time.
     */
    fun queryAndAggregateUsageStats(currentHour: Int, currentDayOfWeek: Int): UsagePrediction? {
        val mostLikely = findMostLikelyApp(currentHour, currentDayOfWeek)
        return mostLikely?.let {
            UsagePrediction(
                mostLikelyPackage = it,
                confidence = calculateConfidence(it, currentHour, currentDayOfWeek)
            )
        }
    }

    /**
     * Find the app most likely to be used at the given time.
     */
    private fun findMostLikelyApp(hour: Int, dayOfWeek: Int): String? {
        val allPrefs = prefs.all
        
        // Find all usage records for this time slot
        val timeSlotKey = "_${hour}_${dayOfWeek}"
        val packageScores = mutableMapOf<String, Int>()

        allPrefs.forEach { (key, value) ->
            if (key.startsWith(KEY_PREFIX) && key.endsWith(timeSlotKey) && value is Int) {
                // Extract package name from key
                val packageName = key
                    .removePrefix(KEY_PREFIX)
                    .removeSuffix(timeSlotKey)
                
                if (packageName.isNotEmpty()) {
                    packageScores[packageName] = (packageScores[packageName] ?: 0) + value
                }
            }
        }

        // Also check adjacent hours for better prediction
        val adjacentHours = listOf(hour - 1, hour + 1).filter { it in 0..23 }
        adjacentHours.forEach { adjHour ->
            val adjKey = "_${adjHour}_${dayOfWeek}"
            allPrefs.forEach { (key, value) ->
                if (key.startsWith(KEY_PREFIX) && key.endsWith(adjKey) && value is Int) {
                    val packageName = key
                        .removePrefix(KEY_PREFIX)
                        .removeSuffix(adjKey)
                    
                    if (packageName.isNotEmpty()) {
                        // Adjacent hours have lower weight
                        packageScores[packageName] = (packageScores[packageName] ?: 0) + (value / 2)
                    }
                }
            }
        }

        return packageScores.maxByOrNull { it.value }?.key
    }

    /**
     * Calculate confidence score for a prediction.
     * Higher confidence means the app is more consistently used at this time.
     */
    private fun calculateConfidence(packageName: String, hour: Int, dayOfWeek: Int): Float {
        val totalKey = "${KEY_PREFIX}${packageName}_$KEY_TOTAL_LAUNCHES"
        val totalLaunches = prefs.getInt(totalKey, 0)
        
        if (totalLaunches == 0) return 0f

        val timeSlotKey = "${KEY_PREFIX}${packageName}_${hour}_${dayOfWeek}"
        val timeSlotLaunches = prefs.getInt(timeSlotKey, 0)

        // Confidence is the ratio of launches at this time to total launches
        // Capped at 0.9 to leave room for uncertainty
        return (timeSlotLaunches.toFloat() / totalLaunches.toFloat()).coerceAtMost(0.9f)
    }

    /**
     * Get all recorded usage patterns.
     */
    fun getAllPatterns(): Map<String, Map<String, Int>> {
        val allPrefs = prefs.all
        val patterns = mutableMapOf<String, MutableMap<String, Int>>()

        allPrefs.forEach { (key, value) ->
            if (key.startsWith(KEY_PREFIX) && value is Int) {
                // Parse key: usage_packageName_hour_day or usage_packageName_total_launches
                val parts = key.removePrefix(KEY_PREFIX).split("_")
                if (parts.size >= 2) {
                    val packageName = parts[0]
                    if (!patterns.containsKey(packageName)) {
                        patterns[packageName] = mutableMapOf()
                    }
                    patterns[packageName]?.set(parts.drop(1).joinToString("_"), value)
                }
            }
        }

        return patterns
    }

    /**
     * Clear all recorded patterns.
     */
    fun clearPatterns() {
        prefs.edit().clear().apply()
    }
}

/**
 * Prediction result with confidence score.
 */
data class UsagePrediction(
    val mostLikelyPackage: String,
    val confidence: Float
) {
    val confidencePercent: Int
        get() = (confidence * 100).toInt()
}