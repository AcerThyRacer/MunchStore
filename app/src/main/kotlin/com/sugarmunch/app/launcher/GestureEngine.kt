package com.sugarmunch.app.launcher

import android.content.Context
import android.view.MotionEvent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

/**
 * Gesture types supported by the launcher
 */
enum class GestureType {
    TAP,
    DOUBLE_TAP,
    LONG_PRESS,
    SWIPE_UP,
    SWIPE_DOWN,
    SWIPE_LEFT,
    SWIPE_RIGHT,
    SWIPE_UP_LEFT,
    SWIPE_UP_RIGHT,
    SWIPE_DOWN_LEFT,
    SWIPE_DOWN_RIGHT,
    PINCH_IN,
    PINCH_OUT,
    TWO_FINGER_SWIPE_UP,
    TWO_FINGER_SWIPE_DOWN,
    TWO_FINGER_TAP,
    CUSTOM_GESTURE
}

/**
 * Actions that can be triggered by gestures
 */
enum class GestureAction {
    NONE,
    OPEN_APP_DRAWER,
    OPEN_NOTIFICATIONS,
    OPEN_QUICK_SETTINGS,
    OPEN_SEARCH,
    OPEN_SETTINGS,
    LOCK_SCREEN,
    TAKE_SCREENSHOT,
    TOGGLE_FLASHLIGHT,
    TOGGLE_WIFI,
    TOGGLE_BLUETOOTH,
    LAUNCH_APP,
    LAUNCH_SHORTCUT,
    SHOW_RECENT_APPS,
    GO_HOME,
    GO_BACK,
    SWITCH_PAGE_LEFT,
    SWITCH_PAGE_RIGHT,
    SHOW_FAVORITES,
    VOICE_SEARCH,
    CAMERA_SHORTCUT,
    WIDGET_PICKER,
    CUSTOM_COMMAND
}

/**
 * Gesture configuration
 */
data class GestureConfig(
    val type: GestureType,
    val action: GestureAction,
    val targetApp: String? = null,
    val customCommand: String? = null,
    val sensitivity: Float = 1.0f,
    val enabled: Boolean = true
)

/**
 * Custom gesture pattern
 */
data class CustomGesturePattern(
    val id: String,
    val name: String,
    val points: List<Offset>,
    val action: GestureAction,
    val tolerance: Float = 50f
)

/**
 * GestureEngine - Advanced gesture detection and handling for the launcher.
 * Features:
 * - Pinch gestures (zoom, expand)
 * - Two-finger swipes
 * - Long-press actions
 * - Shake to trigger effect
 * - Custom gesture drawing
 */
class GestureEngine(private val context: Context) {

    private val _gestureConfigs = MutableStateFlow<Map<GestureType, GestureConfig>>(getDefaultConfigs())
    val gestureConfigs: StateFlow<Map<GestureType, GestureConfig>> = _gestureConfigs.asStateFlow()

    private val _detectedGesture = MutableStateFlow<GestureType?>(null)
    val detectedGesture: StateFlow<GestureType?> = _detectedGesture.asStateFlow()

    private val _customGestures = MutableStateFlow<List<CustomGesturePattern>>(emptyList())
    val customGestures: StateFlow<List<CustomGesturePattern>> = _customGestures.asStateFlow()

    // Gesture detection state
    private var touchStartTime = 0L
    private var touchStartPosition = Offset.Zero
    private var lastTouchPosition = Offset.Zero
    private var touchCount = 0
    private var initialPinchDistance = 0f
    private var currentPinchDistance = 0f

    // Custom gesture recording
    private var isRecordingGesture = false
    private var recordedPoints = mutableListOf<Offset>()

    companion object {
        private const val TAP_TIMEOUT = 200L
        private const val LONG_PRESS_TIMEOUT = 500L
        private const val SWIPE_THRESHOLD = 100f
        private const val PINCH_THRESHOLD = 50f
        private const val SHAKE_THRESHOLD = 12f

        @Volatile
        private var INSTANCE: GestureEngine? = null

        fun getInstance(context: Context): GestureEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GestureEngine(context).also { INSTANCE = it }
            }
        }
    }

    /**
     * Process touch event and detect gesture
     */
    fun processTouchEvent(event: MotionEvent): GestureType? {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchStartTime = System.currentTimeMillis()
                touchStartPosition = Offset(event.x, event.y)
                lastTouchPosition = touchStartPosition
                touchCount = 1

                if (isRecordingGesture) {
                    recordedPoints.clear()
                    recordedPoints.add(touchStartPosition)
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                touchCount = event.pointerCount
                if (touchCount == 2) {
                    initialPinchDistance = getPinchDistance(event)
                    currentPinchDistance = initialPinchDistance
                }
            }

            MotionEvent.ACTION_MOVE -> {
                lastTouchPosition = Offset(event.x, event.y)

                if (isRecordingGesture) {
                    recordedPoints.add(lastTouchPosition)
                }

                // Check pinch
                if (touchCount == 2) {
                    currentPinchDistance = getPinchDistance(event)
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                touchCount = event.pointerCount - 1
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val gesture = detectGesture(event)
                _detectedGesture.value = gesture
                return gesture
            }
        }

        return null
    }

    private fun detectGesture(event: MotionEvent): GestureType? {
        val duration = System.currentTimeMillis() - touchStartTime
        val dx = lastTouchPosition.x - touchStartPosition.x
        val dy = lastTouchPosition.y - touchStartPosition.y
        val distance = sqrt(dx * dx + dy * dy)

        // Check custom gestures first
        if (isRecordingGesture && recordedPoints.size > 5) {
            return GestureType.CUSTOM_GESTURE
        }

        // Check custom gesture patterns
        checkCustomGesture()?.let { return it }

        // Two-finger gestures
        if (touchCount >= 2) {
            return detectTwoFingerGesture(dx, dy, distance)
        }

        // Tap gestures
        if (distance < 30f) {
            return when {
                duration < TAP_TIMEOUT -> GestureType.TAP
                duration > LONG_PRESS_TIMEOUT -> GestureType.LONG_PRESS
                else -> null
            }
        }

        // Swipe gestures
        return detectSwipeGesture(dx, dy, distance)
    }

    private fun detectSwipeGesture(dx: Float, dy: Float, distance: Float): GestureType? {
        if (distance < SWIPE_THRESHOLD) return null

        val angle = atan2(dy, dx) * 180 / PI

        return when {
            angle >= -22.5 && angle < 22.5 -> GestureType.SWIPE_RIGHT
            angle >= 22.5 && angle < 67.5 -> GestureType.SWIPE_DOWN_RIGHT
            angle >= 67.5 && angle < 112.5 -> GestureType.SWIPE_DOWN
            angle >= 112.5 && angle < 157.5 -> GestureType.SWIPE_DOWN_LEFT
            angle >= 157.5 || angle < -157.5 -> GestureType.SWIPE_LEFT
            angle >= -157.5 && angle < -112.5 -> GestureType.SWIPE_UP_LEFT
            angle >= -112.5 && angle < -67.5 -> GestureType.SWIPE_UP
            angle >= -67.5 && angle < -22.5 -> GestureType.SWIPE_UP_RIGHT
            else -> null
        }
    }

    private fun detectTwoFingerGesture(dx: Float, dy: Float, distance: Float): GestureType? {
        // Check pinch
        val pinchDelta = currentPinchDistance - initialPinchDistance
        if (abs(pinchDelta) > PINCH_THRESHOLD) {
            return if (pinchDelta > 0) GestureType.PINCH_OUT else GestureType.PINCH_IN
        }

        // Two-finger swipe
        if (distance > SWIPE_THRESHOLD) {
            return if (abs(dy) > abs(dx)) {
                if (dy > 0) GestureType.TWO_FINGER_SWIPE_DOWN
                else GestureType.TWO_FINGER_SWIPE_UP
            } else null
        }

        // Two-finger tap
        if (distance < 30f) {
            return GestureType.TWO_FINGER_TAP
        }

        return null
    }

    private fun checkCustomGesture(): GestureType? {
        if (recordedPoints.isEmpty()) return null

        for (pattern in _customGestures.value) {
            if (matchGesturePattern(recordedPoints, pattern)) {
                return GestureType.CUSTOM_GESTURE
            }
        }

        return null
    }

    private fun matchGesturePattern(points: List<Offset>, pattern: CustomGesturePattern): Boolean {
        if (points.size < pattern.points.size * 0.5f ||
            points.size > pattern.points.size * 2f) return false

        // Simplified pattern matching
        // Would use proper gesture recognition algorithm in production
        return false
    }

    private fun getPinchDistance(event: MotionEvent): Float {
        if (event.pointerCount < 2) return 0f

        val dx = event.getX(0) - event.getX(1)
        val dy = event.getY(0) - event.getY(1)
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Configure a gesture action
     */
    fun configureGesture(type: GestureType, config: GestureConfig) {
        val updated = _gestureConfigs.value.toMutableMap()
        updated[type] = config
        _gestureConfigs.value = updated
    }

    /**
     * Get action for a detected gesture
     */
    fun getActionForGesture(type: GestureType): GestureAction {
        return _gestureConfigs.value[type]?.action ?: GestureAction.NONE
    }

    /**
     * Start recording a custom gesture
     */
    fun startRecordingGesture() {
        isRecordingGesture = true
        recordedPoints.clear()
    }

    /**
     * Stop recording and save custom gesture
     */
    fun stopRecordingGesture(name: String, action: GestureAction): CustomGesturePattern? {
        isRecordingGesture = false

        if (recordedPoints.size < 5) {
            recordedPoints.clear()
            return null
        }

        val pattern = CustomGesturePattern(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            points = recordedPoints.toList(),
            action = action
        )

        val updated = _customGestures.value + pattern
        _customGestures.value = updated

        return pattern
    }

    /**
     * Delete a custom gesture
     */
    fun deleteCustomGesture(id: String) {
        _customGestures.value = _customGestures.value.filter { it.id != id }
    }

    /**
     * Detect shake gesture (would be called from sensor listener)
     */
    fun detectShake(acceleration: Float): Boolean {
        return acceleration > SHAKE_THRESHOLD
    }

    private fun getDefaultConfigs(): Map<GestureType, GestureConfig> {
        return mapOf(
            GestureType.SWIPE_UP to GestureConfig(
                type = GestureType.SWIPE_UP,
                action = GestureAction.OPEN_APP_DRAWER
            ),
            GestureType.SWIPE_DOWN to GestureConfig(
                type = GestureType.SWIPE_DOWN,
                action = GestureAction.OPEN_NOTIFICATIONS
            ),
            GestureType.DOUBLE_TAP to GestureConfig(
                type = GestureType.DOUBLE_TAP,
                action = GestureAction.LOCK_SCREEN
            ),
            GestureType.LONG_PRESS to GestureConfig(
                type = GestureType.LONG_PRESS,
                action = GestureAction.SHOW_FAVORITES
            ),
            GestureType.PINCH_IN to GestureConfig(
                type = GestureType.PINCH_IN,
                action = GestureAction.WIDGET_PICKER
            ),
            GestureType.TWO_FINGER_SWIPE_DOWN to GestureConfig(
                type = GestureType.TWO_FINGER_SWIPE_DOWN,
                action = GestureAction.OPEN_QUICK_SETTINGS
            )
        )
    }
}

/**
 * Composable modifier for gesture detection
 */
fun Modifier.gestureHandler(
    gestureEngine: GestureEngine,
    onGesture: (GestureType) -> Unit
): Modifier {
    return pointerInput(Unit) {
        detectTapGestures(
            onDoubleTap = {
                val config = gestureEngine.gestureConfigs.value[GestureType.DOUBLE_TAP]
                if (config?.enabled == true) {
                    onGesture(GestureType.DOUBLE_TAP)
                }
            },
            onLongPress = {
                val config = gestureEngine.gestureConfigs.value[GestureType.LONG_PRESS]
                if (config?.enabled == true) {
                    onGesture(GestureType.LONG_PRESS)
                }
            }
        )
    }
}

/**
 * Shake detector using accelerometer
 */
class ShakeDetector(
    private val onShake: () -> Unit
) {
    private var lastAcceleration = 0f
    private var shakeCount = 0
    private var lastShakeTime = 0L

    fun onSensorChanged(values: FloatArray) {
        val x = values[0]
        val y = values[1]
        val z = values[2]

        val acceleration = sqrt(x * x + y * y + z * z) - 9.8f

        if (acceleration > SHAKE_THRESHOLD) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime < 500) {
                shakeCount++
                if (shakeCount >= 2) {
                    onShake()
                    shakeCount = 0
                }
            } else {
                shakeCount = 1
            }
            lastShakeTime = now
        }
    }

    companion object {
        private const val SHAKE_THRESHOLD = 12f
    }
}
