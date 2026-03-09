package com.sugarmunch.app.holographic

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*

/**
 * Parallax Controller - Multi-layer parallax scrolling
 * 
 * Features:
 * - Gyroscope-based parallax
 * - Touch-based parallax
 * - Auto-scrolling parallax
 * - Layered depth system
 */
class ParallaxController(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Parallax layers
    private val _layers = MutableStateFlow<List<ParallaxLayer>>(emptyList())
    val layers: StateFlow<List<ParallaxLayer>> = _layers.asStateFlow()

    // Current parallax offset
    private val _parallaxOffset = MutableStateFlow(Offset.Zero)
    val parallaxOffset: StateFlow<Offset> = _parallaxOffset.asStateFlow()

    // Target offset for smooth animation
    private val _targetOffset = MutableStateFlow(Offset.Zero)
    val targetOffset: StateFlow<Offset> = _targetOffset.asStateFlow()

    // Parallax configuration
    var parallaxConfig = ParallaxConfig(
        sensitivity = 0.5f,
        smoothingFactor = 0.1f,
        maxOffset = Offset(100f, 100f),
        enableGyroscope = true,
        enableTouch = true,
        enableAutoScroll = false,
        autoScrollSpeed = Offset(10f, 5f)
    )

    private var gyroscopeSensor: Sensor? = null
    private var sensorListener: SensorEventListener? = null
    private var isRunning = false

    init {
        initializeSensors()
    }

    private fun initializeSensors() {
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (parallaxConfig.enableGyroscope) {
                    updateFromGyroscope(event.values)
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

        startParallaxLoop()
        startAutoScroll()
    }

    fun stop() {
        isRunning = false
        sensorListener?.let { listener ->
            sensorManager.unregisterListener(listener)
        }
    }

    private fun updateFromGyroscope(values: FloatArray) {
        // Rotation vector to angle conversion
        val pitch = (values[0] * 180 / PI).toFloat().coerceIn(-45f, 45f)
        val roll = (values[1] * 180 / PI).toFloat().coerceIn(-45f, 45f)

        _targetOffset.value = Offset(
            x = -roll * parallaxConfig.sensitivity * 10,
            y = pitch * parallaxConfig.sensitivity * 10
        ).let {
            Offset(
                it.x.coerceIn(-parallaxConfig.maxOffset.x, parallaxConfig.maxOffset.x),
                it.y.coerceIn(-parallaxConfig.maxOffset.y, parallaxConfig.maxOffset.y)
            )
        }
    }

    private fun startParallaxLoop() {
        scope.launch {
            while (isRunning) {
                // Smooth interpolation to target offset
                val current = _parallaxOffset.value
                val target = _targetOffset.value

                val smoothedX = current.x + (target.x - current.x) * parallaxConfig.smoothingFactor
                val smoothedY = current.y + (target.y - current.y) * parallaxConfig.smoothingFactor

                _parallaxOffset.value = Offset(smoothedX, smoothedY)

                // Update layer positions
                updateLayerPositions()

                delay(16)
            }
        }
    }

    private fun startAutoScroll() {
        if (!parallaxConfig.enableAutoScroll) return

        scope.launch {
            var time = 0f

            while (isRunning && parallaxConfig.enableAutoScroll) {
                time += 0.016f

                val autoOffset = Offset(
                    sin(time * parallaxConfig.autoScrollSpeed.x) * 20,
                    cos(time * parallaxConfig.autoScrollSpeed.y) * 20
                )

                _targetOffset.value = _targetOffset.value + autoOffset

                delay(16)
            }
        }
    }

    private fun updateLayerPositions() {
        val offset = _parallaxOffset.value

        _layers.value = _layers.value.map { layer ->
            val depthFactor = layer.depthFactor.coerceIn(0.01f, 1f)
            val layerOffset = Offset(
                offset.x * depthFactor + layer.baseOffset.x,
                offset.y * depthFactor + layer.baseOffset.y
            )

            layer.copy(currentOffset = layerOffset)
        }
    }

    // ========== LAYER MANAGEMENT ==========

    fun addLayer(layer: ParallaxLayer) {
        _layers.value = _layers.value + layer
    }

    fun removeLayer(id: String) {
        _layers.value = _layers.value.filter { it.id != id }
    }

    fun clearLayers() {
        _layers.value = emptyList()
    }

    // ========== TOUCH HANDLING ==========

    fun onTouchEvent(offset: Offset) {
        if (!parallaxConfig.enableTouch) return

        _targetOffset.value = Offset(
            (offset.x - 500) * parallaxConfig.sensitivity,
            (offset.y - 1000) * parallaxConfig.sensitivity
        ).let {
            Offset(
                it.x.coerceIn(-parallaxConfig.maxOffset.x, parallaxConfig.maxOffset.x),
                it.y.coerceIn(-parallaxConfig.maxOffset.y, parallaxConfig.maxOffset.y)
            )
        }
    }

    fun resetParallax() {
        _targetOffset.value = Offset.Zero
    }

    // ========== PRESET LAYER CONFIGURATIONS ==========

    fun createParallaxLayers(): List<ParallaxLayer> {
        return listOf(
            ParallaxLayer(
                id = "background",
                depthFactor = 0.1f,
                scale = 1.2f,
                opacity = 0.8f
            ),
            ParallaxLayer(
                id = "mid_background",
                depthFactor = 0.3f,
                scale = 1.1f,
                opacity = 0.9f
            ),
            ParallaxLayer(
                id = "midground",
                depthFactor = 0.5f,
                scale = 1.0f,
                opacity = 1.0f
            ),
            ParallaxLayer(
                id = "foreground",
                depthFactor = 0.8f,
                scale = 1.0f,
                opacity = 1.0f
            ),
            ParallaxLayer(
                id = "overlay",
                depthFactor = 1.0f,
                scale = 1.0f,
                opacity = 1.0f
            )
        )
    }

    companion object {
        @Volatile
        private var instance: ParallaxController? = null

        fun getInstance(context: Context): ParallaxController {
            return instance ?: synchronized(this) {
                instance ?: ParallaxController(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * Parallax layer configuration
 */
data class ParallaxLayer(
    val id: String,
    val depthFactor: Double = 0.5, // 0 = far, 1 = close
    val scale: Float = 1f,
    val opacity: Float = 1f,
    val baseOffset: Offset = Offset.Zero,
    val currentOffset: Offset = Offset.Zero,
    val rotation: Float = 0f,
    val zIndex: Int = 0,
    val isParallaxEnabled: Boolean = true,
    val content: Any? = null // Placeholder for actual content
)

/**
 * Parallax configuration
 */
data class ParallaxConfig(
    val sensitivity: Float = 0.5f,
    val smoothingFactor: Float = 0.1f,
    val maxOffset: Offset = Offset(100f, 100f),
    val enableGyroscope: Boolean = true,
    val enableTouch: Boolean = true,
    val enableAutoScroll: Boolean = false,
    val autoScrollSpeed: Offset = Offset(10f, 5f),
    val boundaryMode: BoundaryMode = BoundaryMode.CLAMP
)

/**
 * Boundary modes for parallax
 */
enum class BoundaryMode {
    CLAMP,      // Stop at boundaries
    WRAP,       // Wrap around
    BOUNCE,     // Bounce back
    CONTINUE    // Continue scrolling
}

/**
 * Parallax preset configurations
 */
object ParallaxPresets {

    val SUBTLE = ParallaxConfig(
        sensitivity = 0.2f,
        smoothingFactor = 0.05f,
        maxOffset = Offset(30f, 30f),
        enableGyroscope = true,
        enableTouch = true,
        enableAutoScroll = false
    )

    val MODERATE = ParallaxConfig(
        sensitivity = 0.5f,
        smoothingFactor = 0.1f,
        maxOffset = Offset(75f, 75f),
        enableGyroscope = true,
        enableTouch = true,
        enableAutoScroll = false
    )

    val EXTREME = ParallaxConfig(
        sensitivity = 1.0f,
        smoothingFactor = 0.15f,
        maxOffset = Offset(150f, 150f),
        enableGyroscope = true,
        enableTouch = true,
        enableAutoScroll = true,
        autoScrollSpeed = Offset(20f, 15f)
    )

    val DISABLED = ParallaxConfig(
        sensitivity = 0f,
        smoothingFactor = 0f,
        maxOffset = Offset.Zero,
        enableGyroscope = false,
        enableTouch = false,
        enableAutoScroll = false
    )
}
