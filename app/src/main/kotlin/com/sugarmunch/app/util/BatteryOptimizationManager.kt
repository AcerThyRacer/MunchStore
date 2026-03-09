package com.sugarmunch.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.sugarmunch.app.service.OverlayService

/**
 * Battery Optimization Manager
 * Helps manage battery optimization settings for background services
 *
 * ## Hilt Migration Note
 * This class has been migrated from a singleton pattern to Hilt dependency injection.
 * Inject this class using:
 * ```
 * @Inject lateinit var batteryOptimizationManager: BatteryOptimizationManager
 * ```
 * In a Hilt module, provide it with [@ApplicationContext](file:///home/ace/Downloads/SugarMunch/app/src/main/kotlin/com/sugarmunch/app/ui/theme/Color.kt#L5-L5):
 * ```
 * @Provides
 * @Singleton
 * fun provideBatteryOptimizationManager(@ApplicationContext context: Context): BatteryOptimizationManager {
 *     return BatteryOptimizationManager(context)
 * }
 * ```
 */
class BatteryOptimizationManager constructor(private val context: Context) {

    private val powerManager = ContextCompat.getSystemService(context, PowerManager::class.java)

    /**
     * Check if battery optimization is enabled for this app
     */
    fun isBatteryOptimizationEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) == false
        } else {
            false
        }
    }

    /**
     * Check if app can draw over other apps (required for overlay service)
     */
    fun canDrawOverlays(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    /**
     * Get intent to request battery optimization exemption
     */
    fun getBatteryOptimizationRequestIntent(): Intent {
        return Intent().apply {
            action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            data = Uri.parse("package:${context.packageName}")
        }
    }

    /**
     * Get intent to open battery optimization settings
     */
    fun getBatteryOptimizationSettingsIntent(): Intent {
        return Intent().apply {
            action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        }
    }

    /**
     * Get intent to request overlay permission
     */
    fun getOverlayPermissionIntent(): Intent {
        return Intent().apply {
            action = Settings.ACTION_MANAGE_OVERLAY_PERMISSION
            data = Uri.parse("package:${context.packageName}")
        }
    }

    /**
     * Check if overlay service can run
     */
    fun canRunOverlayService(): Boolean {
        return canDrawOverlays()
    }

    /**
     * Get battery optimization status
     */
    fun getOptimizationStatus(): OptimizationStatus {
        val ignoresOptimization = !isBatteryOptimizationEnabled()
        val hasOverlayPermission = canDrawOverlays()

        return when {
            ignoresOptimization && hasOverlayPermission -> OptimizationStatus.OPTIMAL
            ignoresOptimization && !hasOverlayPermission -> OptimizationStatus.NEEDS_OVERLAY_PERMISSION
            !ignoresOptimization && hasOverlayPermission -> OptimizationStatus.NEEDS_BATTERY_OPTIMIZATION
            else -> OptimizationStatus.NEEDS_BOTH
        }
    }

    /**
     * Get recommendations for improving battery usage
     */
    fun getRecommendations(): List<BatteryRecommendation> {
        val recommendations = mutableListOf<BatteryRecommendation>()
        val status = getOptimizationStatus()

        when (status) {
            OptimizationStatus.OPTIMAL -> {
                // No recommendations needed
            }
            OptimizationStatus.NEEDS_OVERLAY_PERMISSION -> {
                recommendations.add(
                    BatteryRecommendation(
                        title = "Enable Overlay Permission",
                        description = "Required for the floating action button to work",
                        priority = Priority.HIGH,
                        action = BatteryRecommendationAction(
                            label = "Grant Permission",
                            intent = getOverlayPermissionIntent()
                        )
                    )
                )
            }
            OptimizationStatus.NEEDS_BATTERY_OPTIMIZATION -> {
                recommendations.add(
                    BatteryRecommendation(
                        title = "Disable Battery Optimization",
                        description = "Allows effects to run smoothly in background",
                        priority = Priority.MEDIUM,
                        action = BatteryRecommendationAction(
                            label = "Disable Optimization",
                            intent = getBatteryOptimizationRequestIntent()
                        )
                    )
                )
            }
            OptimizationStatus.NEEDS_BOTH -> {
                recommendations.add(
                    BatteryRecommendation(
                        title = "Configure App Permissions",
                        description = "Enable overlay and battery optimization for best experience",
                        priority = Priority.HIGH,
                        action = BatteryRecommendationAction(
                            label = "Configure Now",
                            intent = getBatteryOptimizationSettingsIntent()
                        )
                    )
                )
            }
        }

        // Additional recommendations based on usage patterns
        recommendations.add(
            BatteryRecommendation(
                title = "Use Effect Scheduling",
                description = "Schedule effects to turn off automatically to save battery",
                priority = Priority.LOW,
                action = null
            )
        )

        return recommendations
    }

    /**
     * Track battery usage for analytics
     */
    fun getBatteryUsageStats(): BatteryUsageStats {
        // This would integrate with BatteryManager for real stats
        return BatteryUsageStats(
            isOptimized = isBatteryOptimizationEnabled(),
            hasOverlayPermission = canDrawOverlays(),
            backgroundRestrictionLevel = getBackgroundRestrictionLevel()
        )
    }

    private fun getBackgroundRestrictionLevel(): BackgroundRestrictionLevel {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Would check actual restriction level
            BackgroundRestrictionLevel.UNRESTRICTED
        } else {
            BackgroundRestrictionLevel.UNRESTRICTED
        }
    }
}

/**
 * Optimization status enum
 */
enum class OptimizationStatus {
    OPTIMAL,
    NEEDS_OVERLAY_PERMISSION,
    NEEDS_BATTERY_OPTIMIZATION,
    NEEDS_BOTH
}

/**
 * Background restriction level
 */
enum class BackgroundRestrictionLevel {
    UNRESTRICTED,
    RESTRICTED,
    RESTRICTED_STANDBY
}

/**
 * Battery usage statistics
 */
data class BatteryUsageStats(
    val isOptimized: Boolean,
    val hasOverlayPermission: Boolean,
    val backgroundRestrictionLevel: BackgroundRestrictionLevel
)

/**
 * Battery recommendation data class
 */
data class BatteryRecommendation(
    val title: String,
    val description: String,
    val priority: Priority,
    val action: BatteryRecommendationAction?
)

/**
 * Priority levels
 */
enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Action for recommendations
 */
data class BatteryRecommendationAction(
    val label: String,
    val intent: Intent
)
