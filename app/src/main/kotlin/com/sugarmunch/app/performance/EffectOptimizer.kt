package com.sugarmunch.app.performance

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Automatic effect optimizer that adjusts quality based on device performance
 *
 * ## Hilt Migration Note
 * This class has been migrated from a singleton pattern to Hilt dependency injection.
 * Inject this class using:
 * ```
 * @Inject lateinit var effectOptimizer: EffectOptimizer
 * ```
 * In a Hilt module, provide it with [@ApplicationContext](file:///home/ace/Downloads/SugarMunch/app/src/main/kotlin/com/sugarmunch/app/ui/theme/Color.kt#L5-L5) and [PerformanceMonitor]:
 * ```
 * @Provides
 * @Singleton
 * fun provideEffectOptimizer(
 *     @ApplicationContext context: Context,
 *     performanceMonitor: PerformanceMonitor
 * ): EffectOptimizer {
 *     return EffectOptimizer(context, performanceMonitor)
 * }
 * ```
 */
class EffectOptimizer(
    private val context: Context,
    private val performanceMonitor: PerformanceMonitor
) {
    private val mutex = Mutex()
    private val _optimizationState = MutableStateFlow(OptimizationState())
    val optimizationState: StateFlow<OptimizationState> = _optimizationState.asStateFlow()

    private val deviceTier by lazy { determineDeviceTier() }
    private var batterySaverMode = false

    init {
        monitorBatteryState()
        monitorPerformance()
    }

    /**
     * Determine device performance tier
     */
    private fun determineDeviceTier(): DeviceTier {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / (1024 * 1024) // MB
        val processorCount = runtime.availableProcessors()

        return when {
            maxMemory >= 4096 && processorCount >= 8 -> DeviceTier.HIGH_END
            maxMemory >= 2048 && processorCount >= 6 -> DeviceTier.MID_RANGE
            else -> DeviceTier.LOW_END
        }
    }

    /**
     * Monitor battery state for power saving mode
     */
    private fun monitorBatteryState() {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        batterySaverMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryManager.isBatteryLow || 
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && batteryManager.isPowerSaveMode)
        } else {
            false
        }
    }

    /**
     * Monitor performance and auto-adjust
     */
    private fun monitorPerformance() {
        kotlinx.coroutines.GlobalScope.launch {
            performanceMonitor.metrics.collectLatest { metrics ->
                mutex.withLock {
                    val currentState = _optimizationState.value
                    
                    // Auto-adjust based on FPS
                    val newQualityLevel = when {
                        metrics.fps < 30 -> QualityLevel.LOW
                        metrics.fps < 50 -> QualityLevel.MEDIUM
                        else -> QualityLevel.HIGH
                    }

                    // Adjust if needed
                    if (currentState.currentQualityLevel != newQualityLevel) {
                        _optimizationState.value = currentState.copy(
                            currentQualityLevel = newQualityLevel,
                            lastAdjustmentTime = System.currentTimeMillis(),
                            adjustmentReason = "Auto-adjust: FPS=${metrics.fps}"
                        )
                    }

                    // Battery saver mode
                    if (batterySaverMode && currentState.currentQualityLevel != QualityLevel.LOW) {
                        _optimizationState.value = currentState.copy(
                            currentQualityLevel = QualityLevel.LOW,
                            lastAdjustmentTime = System.currentTimeMillis(),
                            adjustmentReason = "Battery saver mode"
                        )
                    }
                }
            }
        }
    }

    /**
     * Get recommended particle count for current device/quality
     */
    fun getRecommendedParticleCount(baseCount: Int = 100): Int {
        val state = _optimizationState.value
        return when (state.currentQualityLevel) {
            QualityLevel.LOW -> (baseCount * 0.3).toInt()
            QualityLevel.MEDIUM -> (baseCount * 0.6).toInt()
            QualityLevel.HIGH -> baseCount
            QualityLevel.MAX -> (baseCount * 1.5).toInt()
        }
    }

    /**
     * Get recommended animation speed multiplier
     */
    fun getAnimationSpeedMultiplier(): Float {
        val state = _optimizationState.value
        return when (state.currentQualityLevel) {
            QualityLevel.LOW -> 0.5f
            QualityLevel.MEDIUM -> 0.75f
            QualityLevel.HIGH -> 1.0f
            QualityLevel.MAX -> 1.25f
        }
    }

    /**
     * Check if an effect should be rendered
     */
    fun shouldRenderEffect(effectComplexity: EffectComplexity): Boolean {
        val state = _optimizationState.value
        
        return when (state.currentQualityLevel) {
            QualityLevel.LOW -> effectComplexity == EffectComplexity.LOW
            QualityLevel.MEDIUM -> effectComplexity != EffectComplexity.EXTREME
            QualityLevel.HIGH -> true
            QualityLevel.MAX -> true
        }
    }

    /**
     * Manually set quality level
     */
    fun setQualityLevel(level: QualityLevel, reason: String = "Manual override") {
        _optimizationState.value = _optimizationState.value.copy(
            currentQualityLevel = level,
            lastAdjustmentTime = System.currentTimeMillis(),
            adjustmentReason = reason
        )
    }

    /**
     * Reset to automatic optimization
     */
    fun resetToAutomatic() {
        _optimizationState.value = _optimizationState.value.copy(
            currentQualityLevel = when (deviceTier) {
                DeviceTier.HIGH_END -> QualityLevel.HIGH
                DeviceTier.MID_RANGE -> QualityLevel.MEDIUM
                DeviceTier.LOW_END -> QualityLevel.LOW
            },
            adjustmentReason = "Reset to auto"
        )
    }
}

data class OptimizationState(
    val currentQualityLevel: QualityLevel = QualityLevel.MEDIUM,
    val lastAdjustmentTime: Long = 0,
    val adjustmentReason: String = "",
    val isBatterySaverMode: Boolean = false
)

enum class QualityLevel {
    LOW,
    MEDIUM,
    HIGH,
    MAX
}

enum class DeviceTier {
    LOW_END,
    MID_RANGE,
    HIGH_END
}

enum class EffectComplexity {
    LOW,
    MEDIUM,
    HIGH,
    EXTREME
}

/**
 * Composable to get current optimization state
 * Note: When using Hilt, inject EffectOptimizer instead of using this composable
 */
@Composable
fun rememberEffectOptimizer(
    context: Context = LocalContext.current,
    performanceMonitor: PerformanceMonitor = PerformanceMonitor()
): EffectOptimizer {
    return remember { EffectOptimizer(context, performanceMonitor) }
}

/**
 * Composable to automatically optimize effect parameters
 */
@Composable
fun AutoOptimizedEffect(
    baseParticleCount: Int = 100,
    baseAnimationSpeed: Float = 1.0f,
    content: @Composable (optimizedParticleCount: Int, optimizedSpeed: Float) -> Unit
) {
    val optimizer = rememberEffectOptimizer()
    val state by optimizer.optimizationState.collectAsState()

    val optimizedParticleCount = remember(baseParticleCount, state.currentQualityLevel) {
        optimizer.getRecommendedParticleCount(baseParticleCount)
    }
    
    val optimizedSpeed = remember(baseAnimationSpeed, state.currentQualityLevel) {
        baseAnimationSpeed * optimizer.getAnimationSpeedMultiplier()
    }

    content(optimizedParticleCount, optimizedSpeed)
}
