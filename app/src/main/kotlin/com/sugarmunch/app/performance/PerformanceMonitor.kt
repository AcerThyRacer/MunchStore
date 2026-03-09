package com.sugarmunch.app.performance

import android.util.Log
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.roundToInt

/**
 * Real-time performance monitoring for SugarMunch
 * Tracks FPS, memory usage, and effect performance
 *
 * ## Hilt Migration Note
 * This class has been migrated from a singleton pattern to Hilt dependency injection.
 * Inject this class using:
 * ```
 * @Inject lateinit var performanceMonitor: PerformanceMonitor
 * ```
 * In a Hilt module, provide it:
 * ```
 * @Provides
 * @Singleton
 * fun providePerformanceMonitor(): PerformanceMonitor {
 *     return PerformanceMonitor()
 * }
 * ```
 */
class PerformanceMonitor {
    private val mutex = Mutex()
    private val _metrics = MutableStateFlow(PerformanceMetrics())
    val metrics: StateFlow<PerformanceMetrics> = _metrics.asStateFlow()

    private var frameCount = 0
    private var lastFpsUpdate = System.currentTimeMillis()
    private var frameStartNanos = 0L

    /**
     * Start monitoring a frame
     */
    fun startFrame() {
        frameStartNanos = System.nanoTime()
    }

    /**
     * End monitoring a frame and calculate FPS
     */
    fun endFrame() {
        val frameEndNanos = System.nanoTime()
        val frameTimeNanos = frameEndNanos - frameStartNanos
        val frameTimeMillis = frameTimeNanos / 1_000_000f

        frameCount++
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastFpsUpdate

        if (elapsed >= 1000) { // Update every second
            val fps = (frameCount * 1000f / elapsed).roundToInt()
            frameCount = 0
            lastFpsUpdate = currentTime

            _metrics.value = _metrics.value.copy(
                fps = fps,
                frameTimeMs = frameTimeMillis,
                isDroppingFrames = fps < 30
            )
        }
    }

    /**
     * Update memory metrics
     */
    fun updateMemoryMetrics(
        usedMemoryMb: Long,
        totalMemoryMb: Long,
        nativeHeapUsedMb: Long? = null
    ) = mutex.withLock {
        val memoryUsage = (usedMemoryMb.toFloat() / totalMemoryMb * 100).roundToInt()
        _metrics.value = _metrics.value.copy(
            usedMemoryMb = usedMemoryMb,
            totalMemoryMb = totalMemoryMb,
            memoryUsagePercent = memoryUsage,
            nativeHeapUsedMb = nativeHeapUsedMb
        )
    }

    /**
     * Track effect performance
     */
    fun trackEffectPerformance(
        effectId: String,
        renderTimeMs: Float,
        particleCount: Int = 0
    ) = mutex.withLock {
        val currentEffects = _metrics.value.activeEffects.toMutableMap()
        currentEffects[effectId] = EffectPerformance(
            renderTimeMs = renderTimeMs,
            particleCount = particleCount,
            isEnabled = true
        )
        _metrics.value = _metrics.value.copy(activeEffects = currentEffects)
    }

    /**
     * Remove tracked effect
     */
    fun removeEffect(effectId: String) = mutex.withLock {
        val currentEffects = _metrics.value.activeEffects.toMutableMap()
        currentEffects.remove(effectId)
        _metrics.value = _metrics.value.copy(activeEffects = currentEffects)
    }

    /**
     * Reset all metrics
     */
    fun reset() = mutex.withLock {
        frameCount = 0
        lastFpsUpdate = System.currentTimeMillis()
        _metrics.value = PerformanceMetrics()
    }

    /**
     * Get performance recommendations
     */
    fun getRecommendations(): List<PerformanceRecommendation> {
        val currentMetrics = _metrics.value
        val recommendations = mutableListOf<PerformanceRecommendation>()

        if (currentMetrics.fps < 30) {
            recommendations.add(PerformanceRecommendation.CRITICAL_LOW_FPS)
        } else if (currentMetrics.fps < 50) {
            recommendations.add(PerformanceRecommendation.LOW_FPS)
        }

        if (currentMetrics.memoryUsagePercent > 80) {
            recommendations.add(PerformanceRecommendation.HIGH_MEMORY)
        }

        if (currentMetrics.activeEffects.size > 5) {
            recommendations.add(PerformanceRecommendation.TOO_MANY_EFFECTS)
        }

        currentMetrics.activeEffects.values.forEach { effect ->
            if (effect.renderTimeMs > 16f) { // More than one frame at 60fps
                recommendations.add(PerformanceRecommendation.SLOW_EFFECT)
            }
        }

        return recommendations.distinct()
    }
}

data class PerformanceMetrics(
    val fps: Int = 60,
    val frameTimeMs: Float = 16.67f,
    val isDroppingFrames: Boolean = false,
    val usedMemoryMb: Long = 0,
    val totalMemoryMb: Long = 0,
    val memoryUsagePercent: Int = 0,
    val nativeHeapUsedMb: Long? = null,
    val activeEffects: Map<String, EffectPerformance> = emptyMap()
)

data class EffectPerformance(
    val renderTimeMs: Float,
    val particleCount: Int,
    val isEnabled: Boolean
)

enum class PerformanceRecommendation {
    CRITICAL_LOW_FPS,
    LOW_FPS,
    HIGH_MEMORY,
    TOO_MANY_EFFECTS,
    SLOW_EFFECT
}

/**
 * Composable to display FPS overlay
 */
@Composable
fun FpsOverlay(
    modifier: Modifier = Modifier,
    monitor: PerformanceMonitor
) {
    val metrics by monitor.metrics.collectAsState()

    // Implementation would use Canvas to draw FPS counter
    // For now, just track the metrics
    LaunchedEffect(metrics.fps) {
        if (metrics.fps < 30) {
            Log.w("PerformanceMonitor", "Low FPS detected: ${metrics.fps}")
        }
    }
}
