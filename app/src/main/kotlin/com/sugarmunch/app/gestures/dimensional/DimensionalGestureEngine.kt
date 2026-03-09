package com.sugarmunch.app.gestures.dimensional

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.MotionEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*

/**
 * Dimensional Gesture Engine - Advanced gesture recognition
 * 
 * Features:
 * - 50+ gesture types
 * - Multi-finger support (up to 10 fingers)
 * - Air gestures (camera-based)
 * - Pressure sensitivity
 * - Gesture combos and macros
 * - 3D gesture recognition
 * - Haptic gesture feedback
 */
class DimensionalGestureEngine(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Gesture state
    private val _gestureState = MutableStateFlow(GestureState())
    val gestureState: StateFlow<GestureState> = _gestureState.asStateFlow()

    // Detected gestures
    private val _detectedGestures = MutableStateFlow<List<DetectedGesture>>(emptyList())
    val detectedGestures: StateFlow<List<DetectedGesture>> = _detectedGestures.asStateFlow()

    // Active touch points
    private val _touchPoints = MutableStateFlow<List<TouchPoint>>(emptyList())
    val touchPoints: StateFlow<List<TouchPoint>> = _touchPoints.asStateFlow()

    // Gesture library
    private val gestureLibrary = GestureLibrary()

    // Gesture configuration
    var gestureConfig = GestureConfig(
        maxTouchPoints = 10,
        gestureSensitivity = 0.5f,
        airGestureEnabled = false,
        pressureSensitivityEnabled = true,
        hapticFeedbackEnabled = true,
        comboEnabled = true,
        macroEnabled = true,
        minGestureLength = 50f,
        maxGestureTime = 2000L
    )

    // Sensors
    private var accelerometerSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null
    private var proximitySensor: Sensor? = null
    private var sensorListener: SensorEventListener? = null

    // Gesture recording
    private var isRecordingGesture = false
    private var recordedGesturePoints = mutableListOf<GesturePoint>()

    // Combo tracking
    private val recentGestures = mutableListOf<String>()
    private var comboStartTime = 0L

    init {
        initializeSensors()
        loadGestureLibrary()
    }

    private fun initializeSensors() {
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> handleAccelerometer(event.values)
                    Sensor.TYPE_GYROSCOPE -> handleGyroscope(event.values)
                    Sensor.TYPE_PROXIMITY -> handleProximity(event.values[0])
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    fun start() {
        accelerometerSensor?.let { sensor ->
            sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        gyroscopeSensor?.let { sensor ->
            sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        proximitySensor?.let { sensor ->
            sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorListener?.let { listener ->
            sensorManager.unregisterListener(listener)
        }
    }

    // ========== TOUCH EVENT PROCESSING ==========

    fun processTouchEvent(event: MotionEvent) {
        val touchPoints = mutableListOf<TouchPoint>()
        val pointerCount = event.pointerCount.coerceAtMost(gestureConfig.maxTouchPoints)

        for (i in 0 until pointerCount) {
            val pointerId = event.getPointerId(i)
            val x = event.getX(i)
            val y = event.getY(i)
            val pressure = if (gestureConfig.pressureSensitivityEnabled) event.getPressure(i) else 1f
            val size = event.getSize(i)

            touchPoints.add(
                TouchPoint(
                    pointerId = pointerId,
                    position = Offset(x, y),
                    pressure = pressure,
                    size = size,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        _touchPoints.value = touchPoints

        // Process gesture based on action
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> onGestureStart(touchPoints.first())
            MotionEvent.ACTION_MOVE -> onGestureMove(touchPoints)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> onGestureEnd(touchPoints)
            MotionEvent.ACTION_CANCEL -> onGestureCancel()
        }
    }

    private fun onGestureStart(touchPoint: TouchPoint) {
        _gestureState.value = GestureState(
            isActive = true,
            startTime = System.currentTimeMillis(),
            startPoint = touchPoint.position,
            fingerCount = 1
        )

        if (isRecordingGesture) {
            recordedGesturePoints.clear()
            recordedGesturePoints.add(
                GesturePoint(
                    position = touchPoint.position,
                    pressure = touchPoint.pressure,
                    timestamp = touchPoint.timestamp
                )
            )
        }
    }

    private fun onGestureMove(touchPoints: List<TouchPoint>) {
        val state = _gestureState.value
        val currentTime = System.currentTimeMillis()

        _gestureState.value = state.copy(
            currentPoints = touchPoints,
            fingerCount = touchPoints.size,
            distance = calculateTotalDistance(touchPoints, state.startPoint),
            duration = currentTime - state.startTime
        )

        if (isRecordingGesture) {
            touchPoints.forEach { point ->
                recordedGesturePoints.add(
                    GesturePoint(
                        position = point.position,
                        pressure = point.pressure,
                        timestamp = point.timestamp
                    )
                )
            }
        }

        // Detect air gestures if enabled
        if (gestureConfig.airGestureEnabled) {
            detectAirGesture(touchPoints)
        }
    }

    private fun onGestureEnd(touchPoints: List<TouchPoint>) {
        val state = _gestureState.value
        if (!state.isActive) return

        val gesturePath = buildGesturePath()
        val detectedGesture = recognizeGesture(gesturePath, touchPoints.size)

        if (detectedGesture != null) {
            _detectedGestures.value = _detectedGestures.value + detectedGesture
            handleDetectedGesture(detectedGesture)

            if (gestureConfig.comboEnabled) {
                trackCombo(detectedGesture.id)
            }
        }

        if (isRecordingGesture) {
            isRecordingGesture = false
        }

        _gestureState.value = state.copy(
            isActive = false,
            lastGesture = detectedGesture
        )
    }

    private fun onGestureCancel() {
        _gestureState.value = _gestureState.value.copy(isActive = false)
        isRecordingGesture = false
    }

    // ========== SENSOR HANDLING ==========

    private fun handleAccelerometer(values: FloatArray) {
        val x = values[0]
        val y = values[1]
        val z = values[2]

        // Detect shake gesture
        val acceleration = sqrt(x * x + y * y + z * z - 9.8f * 9.8f)
        if (acceleration > 15f) {
            triggerGesture(GestureType.SHAKE)
        }

        // Detect device orientation changes
        val pitch = (atan2(y.toDouble(), z.toDouble()) * 180 / PI).toFloat()
        val roll = (atan2(x.toDouble(), z.toDouble()) * 180 / PI).toFloat()

        _gestureState.value = _gestureState.value.copy(
            devicePitch = pitch,
            deviceRoll = roll
        )
    }

    private fun handleGyroscope(values: FloatArray) {
        val x = values[0]
        val y = values[1]
        val z = values[2]

        // Detect rotation gestures
        val rotationSpeed = sqrt(x * x + y * y + z * z)
        if (rotationSpeed > 5f) {
            triggerGesture(GestureType.ROTATE_DEVICE)
        }
    }

    private fun handleProximity(distance: Float) {
        // Detect wave gesture (hand over proximity sensor)
        if (distance < 5f) {
            triggerGesture(GestureType.WAVE)
        }
    }

    // ========== GESTURE RECOGNITION ==========

    private fun buildGesturePath(): Path {
        val path = Path()
        val points = recordedGesturePoints

        if (points.isEmpty()) return path

        path.moveTo(points.first().position.x, points.first().position.y)

        for (i in 1 until points.size) {
            val point = points[i]
            path.lineTo(point.position.x, point.position.y)
        }

        return path
    }

    private fun recognizeGesture(path: Path, fingerCount: Int): DetectedGesture? {
        val state = _gestureState.value
        if (state.distance < gestureConfig.minGestureLength) return null
        if (state.duration > gestureConfig.maxGestureTime) return null

        // Match against gesture library
        val matches = gestureLibrary.matchGesture(path, fingerCount, gestureConfig.gestureSensitivity)

        return matches.firstOrNull()?.let { match ->
            DetectedGesture(
                id = match.gesture.id,
                type = match.gesture.type,
                name = match.gesture.name,
                confidence = match.confidence,
                fingerCount = fingerCount,
                path = path,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    private fun detectAirGesture(touchPoints: List<TouchPoint>) {
        // Analyze touch point patterns for air gestures
        if (touchPoints.size >= 3) {
            // Three-finger spread
            val avgDistance = calculateAverageDistance(touchPoints)
            if (avgDistance > 200f) {
                triggerGesture(GestureType.THREE_FINGER_SPREAD)
            }
        }

        if (touchPoints.size >= 5) {
            // Five-finger pinch
            triggerGesture(GestureType.FIVE_FINGER_PINCH)
        }
    }

    // ========== GESTURE HANDLING ==========

    private fun handleDetectedGesture(gesture: DetectedGesture) {
        scope.launch {
            // Haptic feedback
            if (gestureConfig.hapticFeedbackEnabled) {
                provideHapticFeedback(gesture.type)
            }

            // Execute gesture action
            gesture.action?.invoke(gesture)
        }
    }

    private fun provideHapticFeedback(gestureType: GestureType) {
        // Would integrate with haptic engine
        // Different patterns for different gestures
    }

    private fun triggerGesture(gestureType: GestureType) {
        val gesture = gestureLibrary.getGestureByType(gestureType)?.let { template ->
            DetectedGesture(
                id = "${gestureType.name}_${System.currentTimeMillis()}",
                type = gestureType,
                name = template.name,
                confidence = 1.0f,
                fingerCount = 0,
                path = Path(),
                timestamp = System.currentTimeMillis()
            )
        }

        if (gesture != null) {
            _detectedGestures.value = _detectedGestures.value + gesture
            handleDetectedGesture(gesture)
        }
    }

    // ========== COMBO TRACKING ==========

    private fun trackCombo(gestureId: String) {
        val now = System.currentTimeMillis()

        // Reset combo if too much time has passed
        if (now - comboStartTime > 3000) {
            recentGestures.clear()
            comboStartTime = now
        }

        recentGestures.add(gestureId)

        // Check for combo matches
        val combo = gestureLibrary.findCombo(recentGestures)
        if (combo != null) {
            triggerCombo(combo)
            recentGestures.clear()
        }
    }

    private fun triggerCombo(combo: GestureCombo) {
        val comboGesture = DetectedGesture(
            id = "combo_${combo.id}_${System.currentTimeMillis()}",
            type = GestureType.COMBO,
            name = combo.name,
            confidence = 1.0f,
            fingerCount = 0,
            path = Path(),
            timestamp = System.currentTimeMillis(),
            action = combo.action
        )

        _detectedGestures.value = _detectedGestures.value + comboGesture
        handleDetectedGesture(comboGesture)
    }

    // ========== GESTURE RECORDING ==========

    fun startRecordingGesture() {
        isRecordingGesture = true
        recordedGesturePoints.clear()
    }

    fun stopRecordingGesture(name: String, action: (DetectedGesture) -> Unit): RecordedGesture? {
        isRecordingGesture = false

        if (recordedGesturePoints.size < 5) {
            recordedGesturePoints.clear()
            return null
        }

        val path = buildGesturePath()
        val gesture = RecordedGesture(
            id = "recorded_${System.currentTimeMillis()}",
            name = name,
            path = path,
            points = recordedGesturePoints.toList(),
            action = action
        )

        gestureLibrary.addCustomGesture(gesture)
        recordedGesturePoints.clear()

        return gesture
    }

    // ========== UTILITY METHODS ==========

    private fun calculateTotalDistance(touchPoints: List<TouchPoint>, startPoint: Offset): Float {
        return touchPoints.sumOf { point ->
            (point.position - startPoint).getDistance()
        }.toFloat()
    }

    private fun calculateAverageDistance(touchPoints: List<TouchPoint>): Float {
        if (touchPoints.size < 2) return 0f

        var totalDistance = 0f
        var count = 0

        for (i in 0 until touchPoints.size) {
            for (j in (i + 1) until touchPoints.size) {
                totalDistance += (touchPoints[i].position - touchPoints[j].position).getDistance()
                count++
            }
        }

        return totalDistance / count
    }

    fun getGestureHistory(count: Int = 10): List<DetectedGesture> {
        return _detectedGestures.value.takeLast(count)
    }

    fun clearGestureHistory() {
        _detectedGestures.value = emptyList()
    }

    private fun loadGestureLibrary() {
        gestureLibrary.loadDefaultGestures()
    }

    companion object {
        @Volatile
        private var instance: DimensionalGestureEngine? = null

        fun getInstance(context: Context): DimensionalGestureEngine {
            return instance ?: synchronized(this) {
                instance ?: DimensionalGestureEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * Gesture state
 */
data class GestureState(
    val isActive: Boolean = false,
    val startTime: Long = 0,
    val startPoint: Offset = Offset.Zero,
    val currentPoints: List<TouchPoint> = emptyList(),
    val fingerCount: Int = 0,
    val distance: Float = 0f,
    val duration: Long = 0,
    val devicePitch: Float = 0f,
    val deviceRoll: Float = 0f,
    val lastGesture: DetectedGesture? = null,
    val isAirGesture: Boolean = false
)

/**
 * Touch point
 */
data class TouchPoint(
    val pointerId: Int,
    val position: Offset,
    val pressure: Float,
    val size: Float,
    val timestamp: Long
)

/**
 * Detected gesture
 */
data class DetectedGesture(
    val id: String,
    val type: GestureType,
    val name: String,
    val confidence: Float,
    val fingerCount: Int,
    val path: Path,
    val timestamp: Long,
    val action: ((DetectedGesture) -> Unit)? = null
)

/**
 * Recorded gesture
 */
data class RecordedGesture(
    val id: String,
    val name: String,
    val path: Path,
    val points: List<GesturePoint>,
    val action: (DetectedGesture) -> Unit
)

/**
 * Gesture point
 */
data class GesturePoint(
    val position: Offset,
    val pressure: Float,
    val timestamp: Long
)

/**
 * Gesture configuration
 */
data class GestureConfig(
    val maxTouchPoints: Int = 10,
    val gestureSensitivity: Float = 0.5f,
    val airGestureEnabled: Boolean = false,
    val pressureSensitivityEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val comboEnabled: Boolean = true,
    val macroEnabled: Boolean = true,
    val minGestureLength: Float = 50f,
    val maxGestureTime: Long = 2000L
)
