package com.sugarmunch.app.physics.quantum

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*

/**
 * Motion Trails - Visual trails following touch and movement
 * 
 * Features:
 * - Smooth trail rendering
 * - Color gradients
 * - Width variation
 * - Fade effects
 */
class MotionTrails {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Trail points
    private val _trails = MutableStateFlow<List<Trail>>(emptyList())
    val trails: StateFlow<List<Trail>> = _trails.asStateFlow()

    // Trail configuration
    var trailConfig = TrailConfig(
        maxPoints = 100,
        minDistance = 5f,
        baseWidth = 10f,
        widthVariation = 0.5f,
        fadeDuration = 1f,
        colorGradient = true,
        smoothness = 0.8f
    )

    // Active touch points
    private val activeTouchPoints = mutableMapOf<Int, MutableList<TrailPoint>>()

    private var isRunning = false

    /**
     * Start motion trail tracking
     */
    fun start() {
        if (isRunning) return
        isRunning = true
        startTrailUpdateLoop()
    }

    /**
     * Stop motion trail tracking
     */
    fun stop() {
        isRunning = false
    }

    /**
     * Update trail fading
     */
    private fun startTrailUpdateLoop() {
        scope.launch {
            while (isRunning) {
                updateTrails()
                delay(16)
            }
        }
    }

    /**
     * Update trails (fade, remove old points)
     */
    private fun updateTrails() {
        val currentTime = System.currentTimeMillis() / 1000f

        val updatedTrails = _trails.value.map { trail ->
            val updatedPoints = trail.points.filter { point ->
                val age = currentTime - point.timestamp
                age < trailConfig.fadeDuration
            }.map { point ->
                val age = currentTime - point.timestamp
                val normalizedAge = age / trailConfig.fadeDuration
                point.copy(alpha = 1f - normalizedAge)
            }

            trail.copy(points = updatedPoints)
        }.filter { it.points.isNotEmpty() }

        _trails.value = updatedTrails
    }

    /**
     * Add touch point
     */
    fun addTouchPoint(pointerId: Int, position: Offset, pressure: Float = 1f) {
        val points = activeTouchPoints.getOrPut(pointerId) { mutableListOf() }

        // Check minimum distance
        if (points.isNotEmpty()) {
            val lastPoint = points.last()
            val distance = (position - lastPoint.position).getDistance()
            if (distance < trailConfig.minDistance) return
        }

        val newPoint = TrailPoint(
            position = position,
            timestamp = System.currentTimeMillis() / 1000f,
            pressure = pressure,
            alpha = 1f
        )

        points.add(newPoint)

        // Limit points
        if (points.size > trailConfig.maxPoints) {
            points.removeAt(0)
        }

        // Create trail from points
        if (points.size >= 2) {
            val trail = createTrailFromPoints(points, pointerId)
            _trails.value = _trails.value.filter { it.pointerId != pointerId } + trail
        }
    }

    /**
     * Remove touch point
     */
    fun removeTouchPoint(pointerId: Int) {
        activeTouchPoints.remove(pointerId)
    }

    /**
     * Clear all trails
     */
    fun clear() {
        _trails.value = emptyList()
        activeTouchPoints.clear()
    }

    /**
     * Create trail from points
     */
    private fun createTrailFromPoints(points: List<TrailPoint>, pointerId: Int): Trail {
        val smoothedPoints = smoothPoints(points)

        return Trail(
            id = "trail_$pointerId_${System.currentTimeMillis()}",
            pointerId = pointerId,
            points = smoothedPoints,
            color = Color.Blue,
            width = trailConfig.baseWidth
        )
    }

    /**
     * Smooth trail points using Catmull-Rom spline
     */
    private fun smoothPoints(points: List<TrailPoint>): List<TrailPoint> {
        if (points.size < 3) return points

        val smoothed = mutableListOf<TrailPoint>()
        val smoothness = trailConfig.smoothness

        for (i in points.indices) {
            if (i == 0 || i == points.size - 1) {
                smoothed.add(points[i])
            } else {
                val p0 = points[i - 1]
                val p1 = points[i]
                val p2 = points[i + 1]

                // Catmull-Rom interpolation
                val smoothedPosition = Offset(
                    x = catmullRom(p0.position.x, p1.position.x, p2.position.x, smoothness),
                    y = catmullRom(p0.position.y, p1.position.y, p2.position.y, smoothness)
                )

                smoothed.add(p1.copy(position = smoothedPosition))
            }
        }

        return smoothed
    }

    private fun catmullRom(p0: Float, p1: Float, p2: Float, t: Float): Float {
        return p1 + t * (p2 - p0) / 2
    }

    /**
     * Set trail color
     */
    fun setTrailColor(pointerId: Int, color: Color) {
        _trails.value = _trails.value.map { trail ->
            if (trail.pointerId == pointerId) trail.copy(color = color) else trail
        }
    }

    /**
     * Set trail width
     */
    fun setTrailWidth(pointerId: Int, width: Float) {
        _trails.value = _trails.value.map { trail ->
            if (trail.pointerId == pointerId) trail.copy(width = width) else trail
        }
    }

    /**
     * Create burst effect at position
     */
    fun createBurst(position: Offset, color: Color = Color.White, count: Int = 8) {
        val burstPoints = mutableListOf<TrailPoint>()
        val timestamp = System.currentTimeMillis() / 1000f

        for (i in 0 until count) {
            val angle = (i.toFloat() / count) * 2 * PI
            val distance = 20f

            burstPoints.add(
                TrailPoint(
                    position = position + Offset(cos(angle) * distance, sin(angle) * distance),
                    timestamp = timestamp,
                    pressure = 1f,
                    alpha = 1f
                )
            )
        }

        val burstTrail = Trail(
            id = "burst_${System.currentTimeMillis()}",
            pointerId = -1,
            points = burstPoints,
            color = color,
            width = trailConfig.baseWidth * 0.5f
        )

        _trails.value = _trails.value + burstTrail
    }

    /**
     * Create ripple effect at position
     */
    fun createRipple(position: Offset, color: Color = Color.Blue, rings: Int = 3) {
        for (i in 0 until rings) {
            val ringPoints = mutableListOf<TrailPoint>()
            val timestamp = System.currentTimeMillis() / 1000f + i * 0.1f
            val radius = 30f + i * 20f

            for (j in 0 until 32) {
                val angle = (j.toFloat() / 32) * 2 * PI
                ringPoints.add(
                    TrailPoint(
                        position = position + Offset(cos(angle) * radius, sin(angle) * radius),
                        timestamp = timestamp,
                        pressure = 1f,
                        alpha = 1f - (i.toFloat() / rings)
                    )
                )
            }

            val rippleTrail = Trail(
                id = "ripple_${i}_${System.currentTimeMillis()}",
                pointerId = -2,
                points = ringPoints,
                color = color,
                width = trailConfig.baseWidth * (1 - i.toFloat() / rings) * 0.5f
            )

            _trails.value = _trails.value + rippleTrail
        }
    }
}

/**
 * Trail configuration
 */
data class TrailConfig(
    val maxPoints: Int = 100,
    val minDistance: Float = 5f,
    val baseWidth: Float = 10f,
    val widthVariation: Float = 0.5f,
    val fadeDuration: Float = 1f,
    val colorGradient: Boolean = true,
    val smoothness: Float = 0.8f
)

/**
 * Trail data
 */
data class Trail(
    val id: String,
    val pointerId: Int,
    val points: List<TrailPoint>,
    val color: Color,
    val width: Float
)

/**
 * Trail point
 */
data class TrailPoint(
    val position: Offset,
    val timestamp: Float,
    val pressure: Float,
    val alpha: Float
)

/**
 * Trail style presets
 */
object TrailPresets {

    val SMOOTH = TrailConfig(
        maxPoints = 100,
        minDistance = 3f,
        baseWidth = 8f,
        widthVariation = 0.3f,
        fadeDuration = 1.5f,
        colorGradient = true,
        smoothness = 0.9f
    )

    val JITTERY = TrailConfig(
        maxPoints = 50,
        minDistance = 2f,
        baseWidth = 4f,
        widthVariation = 0.8f,
        fadeDuration = 0.5f,
        colorGradient = false,
        smoothness = 0.2f
    )

    val BOLD = TrailConfig(
        maxPoints = 80,
        minDistance = 5f,
        baseWidth = 15f,
        widthVariation = 0.2f,
        fadeDuration = 2f,
        colorGradient = true,
        smoothness = 0.7f
    )

    val FADE = TrailConfig(
        maxPoints = 60,
        minDistance = 4f,
        baseWidth = 6f,
        widthVariation = 0.4f,
        fadeDuration = 0.8f,
        colorGradient = true,
        smoothness = 0.6f
    )

    val RAINBOW = TrailConfig(
        maxPoints = 100,
        minDistance = 3f,
        baseWidth = 10f,
        widthVariation = 0.5f,
        fadeDuration = 1.2f,
        colorGradient = true,
        smoothness = 0.8f
    )
}
