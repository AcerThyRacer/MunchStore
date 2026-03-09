package com.sugarmunch.app.ai.sentient

import android.content.Context
import android.hardware.Camera
import android.hardware.camera2.*
import android.util.Log
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Attention Tracker - Eye tracking and attention detection
 * 
 * Features:
 * - Eye gaze tracking
 * - Attention detection
 * - Focus measurement
 * - Hands-free navigation
 */
class AttentionTracker(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Attention state
    private val _attentionState = MutableStateFlow(AttentionState())
    val attentionState: StateFlow<AttentionState> = _attentionState.asStateFlow()

    // Gaze history
    private val gazeHistory = mutableListOf<GazePoint>()

    // Attention configuration
    var attentionConfig = AttentionConfig(
        enabled = false,
        sensitivity = 0.5f,
        gazeTrackingEnabled = false,
        attentionTimeout = 5000L,
        handsFreeEnabled = false
    )

    private var cameraManager: CameraManager? = null
    private var isTracking = false

    init {
        initializeCamera()
    }

    private fun initializeCamera() {
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    }

    fun startTracking() {
        if (!attentionConfig.enabled || isTracking) return

        isTracking = true
        startGazeTracking()
        startAttentionLoop()
    }

    fun stopTracking() {
        isTracking = false
        stopGazeTracking()
    }

    private fun startGazeTracking() {
        if (!attentionConfig.gazeTrackingEnabled) return

        scope.launch {
            while (isTracking) {
                // Simulated gaze tracking (would use actual camera/ML)
                val gazePoint = detectGazePoint()
                gazeHistory.add(gazePoint)

                // Trim history
                while (gazeHistory.size > 100) {
                    gazeHistory.removeAt(0)
                }

                _attentionState.value = _attentionState.value.copy(
                    currentGazePoint = gazePoint,
                    isLookingAtScreen = gazePoint.isOnScreen
                )

                delay(100) // 10 FPS gaze tracking
            }
        }
    }

    private fun stopGazeTracking() {
        // Cleanup camera resources
    }

    private fun startAttentionLoop() {
        scope.launch {
            var lastAttentionChange = System.currentTimeMillis()

            while (isTracking) {
                val attention = calculateAttentionLevel()
                val focusArea = calculateFocusArea()

                val now = System.currentTimeMillis()
                val isAttentive = attention > 0.5f

                if (isAttentive != _attentionState.value.isAttentive) {
                    lastAttentionChange = now
                }

                _attentionState.value = _attentionState.value.copy(
                    attentionLevel = attention,
                    focusArea = focusArea,
                    isAttentive = isAttentive,
                    attentionDuration = now - lastAttentionChange
                )

                delay(500)
            }
        }
    }

    private fun detectGazePoint(): GazePoint {
        // In production, this would use:
        // - Front camera for eye tracking
        // - ML model for gaze estimation
        // - Screen position mapping

        // Simulated gaze point
        return GazePoint(
            x = (0.5 + (Math.random() - 0.5) * 0.3).toFloat(),
            y = (0.5 + (Math.random() - 0.5) * 0.3).toFloat(),
            confidence = 0.8f,
            timestamp = System.currentTimeMillis(),
            isOnScreen = true
        )
    }

    private fun calculateAttentionLevel(): Float {
        if (gazeHistory.isEmpty()) return 0f

        // Calculate based on gaze stability
        val recentGaze = gazeHistory.takeLast(20)
        if (recentGaze.size < 5) return 0.5f

        val xVariance = calculateVariance(recentGaze.map { it.x })
        val yVariance = calculateVariance(recentGaze.map { it.y })

        val stability = 1f - ((xVariance + yVariance) / 2).coerceIn(0f, 1f)

        // Calculate based on screen presence
        val onScreenRatio = recentGaze.count { it.isOnScreen }.toFloat() / recentGaze.size

        // Combined attention score
        return (stability * 0.6f + onScreenRatio * 0.4f).coerceIn(0f, 1f)
    }

    private fun calculateFocusArea(): FocusArea {
        if (gazeHistory.isEmpty()) return FocusArea.CENTER

        val recentGaze = gazeHistory.takeLast(30)
        val avgX = recentGaze.map { it.x }.average().toFloat()
        val avgY = recentGaze.map { it.y }.average().toFloat()

        return when {
            avgY < 0.33 -> FocusArea.TOP
            avgY > 0.66 -> FocusArea.BOTTOM
            avgX < 0.33 -> FocusArea.LEFT
            avgX > 0.66 -> FocusArea.RIGHT
            else -> FocusArea.CENTER
        }
    }

    private fun calculateVariance(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        val mean = values.average()
        return values.map { (it - mean) * (it - mean) }.average().toFloat()
    }

    // ========== HANDS-FREE NAVIGATION ==========

    fun handleGazeInteraction(position: Offset): GazeInteraction {
        if (!attentionConfig.handsFreeEnabled) return GazeInteraction.NONE

        val gazePoint = _attentionState.value.currentGazePoint ?: return GazeInteraction.NONE

        // Check if gaze is sustained on position
        val dwellTime = calculateDwellTime(position)

        return when {
            dwellTime > 2000 -> GazeInteraction.LONG_PRESS
            dwellTime > 1000 -> GazeInteraction.TAP
            else -> GazeInteraction.NONE
        }
    }

    private fun calculateDwellTime(position: Offset): Long {
        val recentGaze = gazeHistory.takeLast(30)
        var dwellCount = 0

        recentGaze.forEach { gaze ->
            val gazeOffset = Offset(gaze.x, gaze.y)
            val distance = (gazeOffset - position).getDistance()

            if (distance < 0.1f) { // Within 10% of screen
                dwellCount++
            }
        }

        return (dwellCount * 100).toLong() // Each gaze point is ~100ms apart
    }

    // ========== ATTENTION-BASED FEATURES ==========

    fun getAttentionBasedSuggestions(): List<Suggestion> {
        val suggestions = mutableListOf<Suggestion>()
        val state = _attentionState.value

        // Low attention - suggest break
        if (state.attentionLevel < 0.3f && state.attentionDuration > 60000) {
            suggestions.add(
                Suggestion(
                    id = "attention_break",
                    type = SuggestionType.WELLNESS,
                    title = "Time for a Break?",
                    description = "You seem distracted. Take a short break.",
                    priority = Priority.LOW
                )
            )
        }

        // High attention - enable focus mode
        if (state.attentionLevel > 0.8f && state.attentionDuration > 300000) {
            suggestions.add(
                Suggestion(
                    id = "focus_mode",
                    type = SuggestionType.PRODUCTIVITY,
                    title = "Focus Mode",
                    description = "You're in the zone. Enable focus mode?",
                    priority = Priority.LOW
                )
            )
        }

        return suggestions
    }

    fun shouldDimScreen(): Boolean {
        val state = _attentionState.value
        return !state.isLookingAtScreen && state.attentionDuration > 10000
    }

    fun shouldPauseContent(): Boolean {
        val state = _attentionState.value
        return !state.isAttentive && state.attentionDuration > 30000
    }

    // ========== CALIBRATION ==========

    fun startCalibration(): Flow<CalibrationProgress> = flow {
        emit(CalibrationProgress(0, "Starting calibration..."))

        // 9-point calibration
        val points = listOf(
            Offset(0.1f, 0.1f), Offset(0.5f, 0.1f), Offset(0.9f, 0.1f),
            Offset(0.1f, 0.5f), Offset(0.5f, 0.5f), Offset(0.9f, 0.5f),
            Offset(0.1f, 0.9f), Offset(0.5f, 0.9f), Offset(0.9f, 0.9f)
        )

        var completed = 0

        points.forEach { point ->
            emit(CalibrationProgress(
                progress = (completed.toFloat() / points.size) * 100,
                message = "Look at the point",
                targetPosition = point
            ))

            // Wait for user to look at point
            delay(2000)
            completed++
        }

        emit(CalibrationProgress(100, "Calibration complete!"))

        _attentionState.value = _attentionState.value.copy(
            isCalibrated = true,
            calibrationTimestamp = System.currentTimeMillis()
        )
    }

    companion object {
        @Volatile
        private var instance: AttentionTracker? = null

        fun getInstance(context: Context): AttentionTracker {
            return instance ?: synchronized(this) {
                instance ?: AttentionTracker(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * Attention state
 */
data class AttentionState(
    val attentionLevel: Float = 0.5f,
    val focusArea: FocusArea = FocusArea.CENTER,
    val isAttentive: Boolean = true,
    val isLookingAtScreen: Boolean = true,
    val attentionDuration: Long = 0,
    val currentGazePoint: GazePoint? = null,
    val isCalibrated: Boolean = false,
    val calibrationTimestamp: Long = 0
)

/**
 * Gaze point
 */
data class GazePoint(
    val x: Float,
    val y: Float,
    val confidence: Float,
    val timestamp: Long,
    val isOnScreen: Boolean
)

/**
 * Focus areas
 */
enum class FocusArea {
    TOP_LEFT, TOP, TOP_RIGHT,
    LEFT, CENTER, RIGHT,
    BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT
}

/**
 * Gaze interactions
 */
enum class GazeInteraction {
    NONE,
    TAP,
    LONG_PRESS,
    SCROLL,
    HOVER
}

/**
 * Attention configuration
 */
data class AttentionConfig(
    val enabled: Boolean = false,
    val sensitivity: Float = 0.5f,
    val gazeTrackingEnabled: Boolean = false,
    val attentionTimeout: Long = 5000L,
    val handsFreeEnabled: Boolean = false
)

/**
 * Calibration progress
 */
data class CalibrationProgress(
    val progress: Float,
    val message: String,
    val targetPosition: Offset? = null
)

/**
 * Suggestion types for attention
 */
enum class AttentionSuggestionType {
    WELLNESS,
    PRODUCTIVITY,
    ACCESSIBILITY
}
