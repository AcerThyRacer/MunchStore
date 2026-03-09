package com.sugarmunch.app.holographic

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*

/**
 * Depth Engine - 3D depth perception and Z-axis layering
 * 
 * Features:
 * - 10+ depth layers
 * - Gyroscope-based parallax
 * - Touch-based depth interaction
 * - Depth-of-field blur effects
 * - Z-index animation
 */
class DepthEngine(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Depth layers (0 = furthest, 9 = closest)
    private val _depthLayers = MutableStateFlow<List<DepthLayer>>(emptyList())
    val depthLayers: StateFlow<List<DepthLayer>> = _depthLayers.asStateFlow()

    // Current device orientation for parallax
    private val _deviceOrientation = MutableStateFlow(DeviceOrientation())
    val deviceOrientation: StateFlow<DeviceOrientation> = _deviceOrientation.asStateFlow()

    // Depth camera position
    private val _cameraPosition = MutableStateFlow(CameraPosition())
    val cameraPosition: StateFlow<CameraPosition> = _cameraPosition.asStateFlow()

    // Depth configuration
    var depthConfig = DepthConfig(
        maxDepth = 10,
        parallaxStrength = 0.5f,
        depthOfFieldEnabled = true,
        focalDistance = 5,
        blurStrength = 0.3f
    )

    private var gyroscopeSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var sensorListener: SensorEventListener? = null
    private var isRunning = false

    init {
        initializeSensors()
    }

    private fun initializeSensors() {
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_GYROSCOPE -> {
                        updateOrientation(
                            pitch = event.values[0],
                            roll = event.values[1],
                            yaw = event.values[2]
                        )
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        updateAcceleration(
                            x = event.values[0],
                            y = event.values[1],
                            z = event.values[2]
                        )
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    fun start() {
        if (isRunning) return
        isRunning = true

        gyroscopeSensor?.let { sensor ->
            sensorManager.registerListener(
                sensorListener,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        accelerometerSensor?.let { sensor ->
            sensorManager.registerListener(
                sensorListener,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        startDepthAnimationLoop()
    }

    fun stop() {
        isRunning = false
        sensorListener?.let { listener ->
            sensorManager.unregisterListener(listener)
        }
    }

    private fun updateOrientation(pitch: Float, roll: Float, yaw: Float) {
        _deviceOrientation.value = DeviceOrientation(
            pitch = pitch.coerceIn(-45f, 45f),
            roll = roll.coerceIn(-45f, 45f),
            yaw = yaw.coerceIn(-180f, 180f)
        )

        // Update camera position based on orientation
        _cameraPosition.value = CameraPosition(
            x = -roll * depthConfig.parallaxStrength,
            y = pitch * depthConfig.parallaxStrength,
            z = 0f
        )
    }

    private fun updateAcceleration(x: Float, y: Float, z: Float) {
        // Use acceleration for dynamic depth effects
        val intensity = sqrt(x * x + y * y + z * z - 9.8f * 9.8f)

        if (intensity > 5f) {
            // Apply depth shake effect
            _depthLayers.value = _depthLayers.value.map { layer ->
                layer.copy(
                    offsetX = layer.offsetX + (Math.random().toFloat() - 0.5f) * intensity * 0.1f,
                    offsetY = layer.offsetY + (Math.random().toFloat() - 0.5f) * intensity * 0.1f
                )
            }
        }
    }

    private fun startDepthAnimationLoop() {
        scope.launch {
            while (isRunning) {
                // Animate depth layers
                val time = System.currentTimeMillis() / 1000f

                _depthLayers.value = _depthLayers.value.map { layer ->
                    if (layer.isAnimated) {
                        layer.copy(
                            offsetX = layer.baseOffsetX + sin(time * layer.animationSpeed + layer.phase) * layer.animationAmplitude,
                            offsetY = layer.baseOffsetY + cos(time * layer.animationSpeed + layer.phase) * layer.animationAmplitude
                        )
                    } else {
                        layer
                    }
                }

                delay(16)
            }
        }
    }

    // ========== LAYER MANAGEMENT ==========

    fun addLayer(layer: DepthLayer) {
        _depthLayers.value = _depthLayers.value + layer
    }

    fun removeLayer(id: String) {
        _depthLayers.value = _depthLayers.value.filter { it.id != id }
    }

    fun updateLayer(id: String, update: DepthLayer.() -> DepthLayer) {
        _depthLayers.value = _depthLayers.value.map { layer ->
            if (layer.id == id) layer.update() else layer
        }
    }

    fun clearLayers() {
        _depthLayers.value = emptyList()
    }

    // ========== PRESET LAYER CONFIGURATIONS ==========

    fun createBackgroundLayer(): DepthLayer {
        return DepthLayer(
            id = "background_${System.currentTimeMillis()}",
            depth = 0,
            scale = 1.1f,
            opacity = 0.8f,
            blurRadius = 10f,
            isAnimated = true,
            animationSpeed = 0.5f,
            animationAmplitude = 5f
        )
    }

    fun createMidgroundLayer(): DepthLayer {
        return DepthLayer(
            id = "midground_${System.currentTimeMillis()}",
            depth = 5,
            scale = 1.0f,
            opacity = 1.0f,
            blurRadius = 0f,
            isAnimated = false
        )
    }

    fun createForegroundLayer(): DepthLayer {
        return DepthLayer(
            id = "foreground_${System.currentTimeMillis()}",
            depth = 9,
            scale = 1.05f,
            opacity = 1.0f,
            blurRadius = 0f,
            isAnimated = true,
            animationSpeed = 1.0f,
            animationAmplitude = 3f,
            castShadow = true,
            shadowBlur = 20f,
            shadowOpacity = 0.3f
        )
    }

    fun createFloatingLayer(): DepthLayer {
        return DepthLayer(
            id = "floating_${System.currentTimeMillis()}",
            depth = 7,
            scale = 1.0f,
            opacity = 0.9f,
            blurRadius = 2f,
            isAnimated = true,
            animationSpeed = 0.3f,
            animationAmplitude = 10f,
            phase = Math.random().toFloat() * 2 * PI
        )
    }

    // ========== DEPTH CALCULATIONS ==========

    fun calculateParallaxOffset(depth: Int): Offset {
        val orientation = _deviceOrientation.value
        val normalizedDepth = depth.toFloat() / depthConfig.maxDepth

        return Offset(
            x = -orientation.roll * depthConfig.parallaxStrength * normalizedDepth,
            y = orientation.pitch * depthConfig.parallaxStrength * normalizedDepth
        )
    }

    fun calculateDepthBlur(depth: Int): Float {
        if (!depthConfig.depthOfFieldEnabled) return 0f

        val depthDifference = abs(depth - depthConfig.focalDistance)
        return depthDifference * depthConfig.blurStrength
    }

    fun calculateDepthScale(depth: Int): Float {
        val normalizedDepth = depth.toFloat() / depthConfig.maxDepth
        return 1f + (normalizedDepth - 0.5f) * 0.1f
    }

    fun calculateDepthOpacity(depth: Int): Float {
        val normalizedDepth = depth.toFloat() / depthConfig.maxDepth
        return 0.5f + normalizedDepth * 0.5f
    }

    companion object {
        @Volatile
        private var instance: DepthEngine? = null

        fun getInstance(context: Context): DepthEngine {
            return instance ?: synchronized(this) {
                instance ?: DepthEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * Depth layer configuration
 */
data class DepthLayer(
    val id: String,
    val depth: Int, // 0-9
    val scale: Float = 1f,
    val opacity: Float = 1f,
    val blurRadius: Float = 0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val baseOffsetX: Float = 0f,
    val baseOffsetY: Float = 0f,
    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
    val rotationZ: Float = 0f,
    val isAnimated: Boolean = false,
    val animationSpeed: Float = 1f,
    val animationAmplitude: Float = 5f,
    val phase: Float = 0f,
    val castShadow: Boolean = false,
    val shadowBlur: Float = 10f,
    val shadowOpacity: Float = 0.2f,
    val receivesShadow: Boolean = true,
    val reflectivity: Float = 0f,
    val normalMap: String? = null
)

/**
 * Device orientation state
 */
data class DeviceOrientation(
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val yaw: Float = 0f
)

/**
 * Camera position for 3D rendering
 */
data class CameraPosition(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val focalLength: Float = 500f
)

/**
 * Depth configuration
 */
data class DepthConfig(
    val maxDepth: Int = 10,
    val parallaxStrength: Float = 0.5f,
    val depthOfFieldEnabled: Boolean = true,
    val focalDistance: Int = 5,
    val blurStrength: Float = 0.3f,
    val ambientOcclusion: Boolean = true,
    val shadowQuality: ShadowQuality = ShadowQuality.HIGH
)

/**
 * Shadow quality levels
 */
enum class ShadowQuality {
    LOW, MEDIUM, HIGH, ULTRA
}

/**
 * Depth preset configurations
 */
object DepthPresets {

    val SUBTLE = DepthConfig(
        maxDepth = 5,
        parallaxStrength = 0.2f,
        depthOfFieldEnabled = true,
        focalDistance = 3,
        blurStrength = 0.1f
    )

    val BALANCED = DepthConfig(
        maxDepth = 10,
        parallaxStrength = 0.5f,
        depthOfFieldEnabled = true,
        focalDistance = 5,
        blurStrength = 0.3f
    )

    val EXTREME = DepthConfig(
        maxDepth = 20,
        parallaxStrength = 1.0f,
        depthOfFieldEnabled = true,
        focalDistance = 10,
        blurStrength = 0.5f
    )

    val FLAT = DepthConfig(
        maxDepth = 1,
        parallaxStrength = 0f,
        depthOfFieldEnabled = false,
        focalDistance = 0,
        blurStrength = 0f
    )
}
