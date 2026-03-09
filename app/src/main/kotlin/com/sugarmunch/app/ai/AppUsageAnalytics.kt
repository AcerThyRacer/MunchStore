package com.sugarmunch.app.ai

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process.myUserHandle
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Lightweight analytics compatibility layer.
 *
 * The previous implementation depended on unfinished DAO and ML plumbing that
 * no longer matches the current database schema. For the core app we only need
 * a stable in-process event recorder so recommendation and pruning UI can load.
 */
class AppUsageAnalytics private constructor(
    private val context: Context
) {
    private val _userBehaviorProfile = MutableStateFlow<BehaviorProfile?>(null)
    val userBehaviorProfile: StateFlow<BehaviorProfile?> = _userBehaviorProfile.asStateFlow()

    private val _featureStream = MutableSharedFlow<FeatureVector>(extraBufferCapacity = 32)
    val featureStream: SharedFlow<FeatureVector> = _featureStream.asSharedFlow()

    private val _detectedPatterns = MutableStateFlow<List<BehaviorPattern>>(emptyList())
    val detectedPatterns: StateFlow<List<BehaviorPattern>> = _detectedPatterns.asStateFlow()

    suspend fun recordInteraction(
        type: InteractionType,
        appId: String? = null,
        metadata: Map<String, Any> = emptyMap(),
        duration: Long = 0
    ) = withContext(Dispatchers.Default) {
        val timestamp = System.currentTimeMillis()
        _featureStream.emit(
            FeatureVector(
                values = listOf(
                    when (type) {
                        InteractionType.VIEW -> 0.1f
                        InteractionType.INSTALL -> 1f
                        InteractionType.SHARE -> 0.75f
                        InteractionType.OPEN -> 0.5f
                        InteractionType.SEARCH -> 0.25f
                    },
                    if (duration > 0) (duration / 1000f).coerceAtMost(60f) / 60f else 0f,
                    if (appId != null) 1f else 0f,
                    metadata.size.coerceAtMost(10).toFloat() / 10f
                ),
                timestamp = timestamp
            )
        )
        if (_userBehaviorProfile.value == null) {
            _userBehaviorProfile.value = defaultProfile(timestamp)
        }
    }

    fun recordSessionEvent(event: SessionEvent) {
        val timestamp = when (event) {
            is SessionEvent.Started -> event.timestamp
            is SessionEvent.Ended -> event.timestamp
        }
        if (_userBehaviorProfile.value == null) {
            _userBehaviorProfile.value = defaultProfile(timestamp)
        }
    }

    suspend fun getUserProfile(): BehaviorProfile = withContext(Dispatchers.Default) {
        _userBehaviorProfile.value ?: defaultProfile(System.currentTimeMillis()).also {
            _userBehaviorProfile.value = it
        }
    }

    suspend fun getBehaviorPatterns(): List<BehaviorPattern> = withContext(Dispatchers.Default) {
        _detectedPatterns.value
    }

    /**
     * Get apps that haven't been used in the specified number of months.
     * 
     * Uses UsageStatsManager to query app usage history and identify apps
     * with no launches in the specified time period.
     *
     * NOTE: Requires PACKAGE_USAGE_STATS permission. If not granted, will
     * guide user to settings and return empty list.
     *
     * @param months Number of months to look back (default: 3)
     * @return List of package names that haven't been used
     */
    suspend fun getUnusedAppIds(months: Int = 3): List<String> = withContext(Dispatchers.IO) {
        try {
            // Check if we have usage stats permission
            if (!hasUsageStatsPermission()) {
                Log.w(TAG, "PACKAGE_USAGE_STATS permission not granted, cannot determine unused apps")
                return emptyList()
            }
            
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val packageManager = context.packageManager
            
            // Calculate time range (from X months ago to now)
            val endTime = System.currentTimeMillis()
            val startTime = endTime - (months * 30L * 24 * 60 * 60 * 1000) // Approximate months
            
            // Query usage stats for the time period
            val usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST, // Use best available interval
                startTime,
                endTime
            )
            
            if (usageStatsList.isNullOrEmpty()) {
                Log.d(TAG, "No usage stats available for the time period")
                return emptyList()
            }
            
            // Build a map of package name to last used time
            val lastUsedMap = mutableMapOf<String, Long>()
            usageStatsList.forEach { stats ->
                if (stats.lastTimeUsed > 0) {
                    lastUsedMap[stats.packageName] = maxOf(
                        lastUsedMap[stats.packageName] ?: 0L,
                        stats.lastTimeUsed
                    )
                }
            }
            
            // Get all installed non-system apps
            val installedApps = try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getInstalledPackages(0)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get installed packages", e)
                return emptyList()
            }
            
            // Filter to find apps that:
            // 1. Are not system apps
            // 2. Are not SugarMunch itself
            // 3. Have no usage in the time period OR were never launched
            val unusedPackages = installedApps
                .filter { packageInfo ->
                    val appInfo = packageInfo.applicationInfo
                    val isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                    val isSugarMunch = appInfo.packageName.startsWith("com.sugarmunch")
                    val lastUsed = lastUsedMap[appInfo.packageName] ?: 0L
                    
                    !isSystemApp && !isSugarMunch && (lastUsed < startTime || lastUsed == 0L)
                }
                .map { it.applicationInfo.packageName }
            
            Log.d(TAG, "Found ${unusedPackages.size} unused apps (not launched in $months months)")
            return unusedPackages
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unused app IDs", e)
            return emptyList()
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
                android.os.Process.myUid(),
                context.packageName
            )
            result == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            Log.e(TAG, "Error checking usage stats permission", e)
            false
        }
    }
    
    /**
     * Open system settings to grant usage stats permission.
     */
    fun openUsageStatsSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open usage stats settings", e)
            false
        }
    }
    
    /**
     * Get usage statistics for a specific app.
     *
     * @param packageName The package name to query
     * @param days Number of days to look back
     * @return UsageStats for the app or null if not available
     */
    suspend fun getAppUsageStats(packageName: String, days: Int = 30): UsageStats? = withContext(Dispatchers.IO) {
        if (!hasUsageStatsPermission()) return null
        
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (days * 24L * 60 * 60 * 1000)
        
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        
        return usageStatsList.find { it.packageName == packageName }
    }
    
    /**
     * Get total screen time for an app in the specified period.
     *
     * @param packageName The package name to query
     * @param days Number of days to look back
     * @return Total time in foreground in milliseconds
     */
    suspend fun getAppScreenTime(packageName: String, days: Int = 7): Long = withContext(Dispatchers.IO) {
        if (!hasUsageStatsPermission()) return 0L
        
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (days * 24L * 60 * 60 * 1000)
        
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        
        return usageStatsList
            .filter { it.packageName == packageName }
            .sumOf { it.totalTimeInForeground }
    }

    private fun defaultProfile(timestamp: Long): BehaviorProfile {
        return BehaviorProfile(
            userId = "local-user",
            createdAt = timestamp,
            categoryPreferences = emptyMap(),
            temporalPatterns = TemporalPatterns(
                hourlyDistribution = emptyMap(),
                dailyDistribution = emptyMap(),
                peakActivityHour = null,
                preferredDays = emptyList()
            ),
            engagementMetrics = EngagementMetrics(
                totalSessions = 0,
                avgSessionDuration = 0L,
                avgInteractionsPerSession = 0f,
                returnRate = 0f,
                engagementScore = 0f
            ),
            explorationMetrics = ExplorationMetrics(
                categoriesExplored = 0,
                diversityScore = 0f,
                favoriteCategory = null,
                newAppDiscoveryRate = 0f
            ),
            behaviorPatterns = emptyList(),
            installBehavior = InstallBehavior(
                totalInstalls = 0,
                avgInstallsPerWeek = 0f,
                mostActiveInstallHour = null
            ),
            featureVector = FeatureVector(values = emptyList(), timestamp = timestamp)
        )
    }

    companion object {
        @Volatile
        private var instance: AppUsageAnalytics? = null

        fun getInstance(context: Context): AppUsageAnalytics {
            return instance ?: synchronized(this) {
                instance ?: AppUsageAnalytics(context.applicationContext).also { instance = it }
            }
        }
    }
}

data class UserInteraction(
    val id: String,
    val type: InteractionType,
    val appId: String?,
    val timestamp: Long,
    val hourOfDay: Int,
    val dayOfWeek: Int,
    val sessionId: String,
    val duration: Long,
    val metadata: Map<String, Any> = emptyMap()
)

enum class InteractionType {
    VIEW,
    INSTALL,
    SHARE,
    OPEN,
    SEARCH
}

sealed class SessionEvent {
    data class Started(
        val sessionId: String,
        val timestamp: Long = System.currentTimeMillis(),
        val entryPoint: String = "app"
    ) : SessionEvent()

    data class Ended(
        val sessionId: String,
        val timestamp: Long = System.currentTimeMillis(),
        val interactionCount: Int = 0,
        val duration: Long = 0L
    ) : SessionEvent()
}

data class BehaviorProfile(
    val userId: String,
    val createdAt: Long,
    val categoryPreferences: Map<String, Float>,
    val temporalPatterns: TemporalPatterns,
    val engagementMetrics: EngagementMetrics,
    val explorationMetrics: ExplorationMetrics,
    val behaviorPatterns: List<BehaviorPattern>,
    val installBehavior: InstallBehavior,
    val featureVector: FeatureVector
)

data class TemporalPatterns(
    val hourlyDistribution: Map<Int, Float>,
    val dailyDistribution: Map<Int, Float>,
    val peakActivityHour: Int?,
    val preferredDays: List<Int>
)

data class EngagementMetrics(
    val totalSessions: Int,
    val avgSessionDuration: Long,
    val avgInteractionsPerSession: Float,
    val returnRate: Float,
    val engagementScore: Float
)

data class ExplorationMetrics(
    val categoriesExplored: Int,
    val diversityScore: Float,
    val favoriteCategory: String?,
    val newAppDiscoveryRate: Float
)

data class InstallBehavior(
    val totalInstalls: Int,
    val avgInstallsPerWeek: Float,
    val mostActiveInstallHour: Int?
)

sealed class BehaviorPattern {
    data class CategoryAffinity(val category: String, val score: Float) : BehaviorPattern()
    data class TimeWindow(val hour: Int, val score: Float) : BehaviorPattern()
}

data class FeatureVector(
    val values: List<Float>,
    val timestamp: Long
)

data class TrainingBatch(
    val features: List<FeatureVector>,
    val labels: List<Float>
)

data class SessionStats(
    val sessionCount: Int,
    val avgDuration: Long,
    val avgInteractions: Float
)

fun Collection<Float>.stdDev(): Float {
    if (isEmpty()) return 0f
    val mean = average().toFloat()
    val variance = map { value -> (value - mean) * (value - mean) }.average().toFloat()
    return kotlin.math.sqrt(variance)
}
