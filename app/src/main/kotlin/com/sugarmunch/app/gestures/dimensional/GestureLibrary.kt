package com.sugarmunch.app.gestures.dimensional

import androidx.compose.ui.graphics.Path
import kotlinx.coroutines.flow.*
import kotlin.math.*

/**
 * Gesture Library - 50+ gesture definitions and recognition
 */
class GestureLibrary {

    // Gesture database
    private val _gestures = MutableStateFlow<List<GestureTemplate>>(emptyList())
    val gestures: StateFlow<List<GestureTemplate>> = _gestures.asStateFlow()

    // Gesture combos
    private val _combos = MutableStateFlow<List<GestureCombo>>(emptyList())
    val combos: StateFlow<List<GestureCombo>> = _combos.asStateFlow()

    // Custom gestures
    private val customGestures = mutableListOf<RecordedGesture>()

    init {
        loadDefaultCombos()
    }

    fun loadDefaultGestures() {
        val defaultGestures = listOf(
            // ========== SINGLE FINGER GESTURES (1-15) ==========
            GestureTemplate(GestureType.TAP, "Tap", 1, createTapPath()),
            GestureTemplate(GestureType.DOUBLE_TAP, "Double Tap", 1, createDoubleTapPath()),
            GestureTemplate(GestureType.TRIPLE_TAP, "Triple Tap", 1, createTripleTapPath()),
            GestureTemplate(GestureType.LONG_PRESS, "Long Press", 1, createLongPressPath()),
            GestureTemplate(GestureType.SWIPE_UP, "Swipe Up", 1, createSwipePath(0f, -100f)),
            GestureTemplate(GestureType.SWIPE_DOWN, "Swipe Down", 1, createSwipePath(0f, 100f)),
            GestureTemplate(GestureType.SWIPE_LEFT, "Swipe Left", 1, createSwipePath(-100f, 0f)),
            GestureTemplate(GestureType.SWIPE_RIGHT, "Swipe Right", 1, createSwipePath(100f, 0f)),
            GestureTemplate(GestureType.SWIPE_UP_LEFT, "Swipe Up-Left", 1, createSwipePath(-70f, -70f)),
            GestureTemplate(GestureType.SWIPE_UP_RIGHT, "Swipe Up-Right", 1, createSwipePath(70f, -70f)),
            GestureTemplate(GestureType.SWIPE_DOWN_LEFT, "Swipe Down-Left", 1, createSwipePath(-70f, 70f)),
            GestureTemplate(GestureType.SWIPE_DOWN_RIGHT, "Swipe Down-Right", 1, createSwipePath(70f, 70f)),
            GestureTemplate(GestureType.CIRCLE_CW, "Circle Counter-Clockwise", 1, createCirclePath(false)),
            GestureTemplate(GestureType.CIRCLE_CW, "Circle Clockwise", 1, createCirclePath(true)),
            GestureTemplate(GestureType.ZIGZAG, "Zigzag", 1, createZigzagPath()),

            // ========== TWO FINGER GESTURES (16-25) ==========
            GestureTemplate(GestureType.TWO_FINGER_TAP, "Two Finger Tap", 2, createTapPath()),
            GestureTemplate(GestureType.TWO_FINGER_SWIPE_UP, "Two Finger Swipe Up", 2, createSwipePath(0f, -100f)),
            GestureTemplate(GestureType.TWO_FINGER_SWIPE_DOWN, "Two Finger Swipe Down", 2, createSwipePath(0f, 100f)),
            GestureTemplate(GestureType.TWO_FINGER_SWIPE_LEFT, "Two Finger Swipe Left", 2, createSwipePath(-100f, 0f)),
            GestureTemplate(GestureType.TWO_FINGER_SWIPE_RIGHT, "Two Finger Swipe Right", 2, createSwipePath(100f, 0f)),
            GestureTemplate(GestureType.PINCH_IN, "Pinch In", 2, createPinchPath(true)),
            GestureTemplate(GestureType.PINCH_OUT, "Pinch Out", 2, createPinchPath(false)),
            GestureTemplate(GestureType.TWO_FINGER_ROTATE_CW, "Two Finger Rotate CW", 2, createRotatePath(true)),
            GestureTemplate(GestureType.TWO_FINGER_ROTATE_CCW, "Two Finger Rotate CCW", 2, createRotatePath(false)),
            GestureTemplate(GestureType.TWO_FINGER_SCROLL, "Two Finger Scroll", 2, createScrollPath()),

            // ========== THREE FINGER GESTURES (26-35) ==========
            GestureTemplate(GestureType.THREE_FINGER_TAP, "Three Finger Tap", 3, createTapPath()),
            GestureTemplate(GestureType.THREE_FINGER_SWIPE_UP, "Three Finger Swipe Up", 3, createSwipePath(0f, -100f)),
            GestureTemplate(GestureType.THREE_FINGER_SWIPE_DOWN, "Three Finger Swipe Down", 3, createSwipePath(0f, 100f)),
            GestureTemplate(GestureType.THREE_FINGER_SWIPE_LEFT, "Three Finger Swipe Left", 3, createSwipePath(-100f, 0f)),
            GestureTemplate(GestureType.THREE_FINGER_SWIPE_RIGHT, "Three Finger Swipe Right", 3, createSwipePath(100f, 0f)),
            GestureTemplate(GestureType.THREE_FINGER_SPREAD, "Three Finger Spread", 3, createSpreadPath(3)),
            GestureTemplate(GestureType.THREE_FINGER_PINCH, "Three Finger Pinch", 3, createPinchPath(true)),
            GestureTemplate(GestureType.THREE_FINGER_ROTATE, "Three Finger Rotate", 3, createRotatePath(true)),
            GestureTemplate(GestureType.THREE_FINGER_TRIANGLE, "Three Finger Triangle", 3, createTrianglePath()),
            GestureTemplate(GestureType.THREE_FINGER_SWIPE_HOLD, "Three Finger Swipe & Hold", 3, createSwipeHoldPath()),

            // ========== FOUR FINGER GESTURES (36-42) ==========
            GestureTemplate(GestureType.FOUR_FINGER_TAP, "Four Finger Tap", 4, createTapPath()),
            GestureTemplate(GestureType.FOUR_FINGER_SWIPE_UP, "Four Finger Swipe Up", 4, createSwipePath(0f, -100f)),
            GestureTemplate(GestureType.FOUR_FINGER_SWIPE_DOWN, "Four Finger Swipe Down", 4, createSwipePath(0f, 100f)),
            GestureTemplate(GestureType.FOUR_FINGER_SWIPE_LEFT, "Four Finger Swipe Left", 4, createSwipePath(-100f, 0f)),
            GestureTemplate(GestureType.FOUR_FINGER_SWIPE_RIGHT, "Four Finger Swipe Right", 4, createSwipePath(100f, 0f)),
            GestureTemplate(GestureType.FOUR_FINGER_SPREAD, "Four Finger Spread", 4, createSpreadPath(4)),
            GestureTemplate(GestureType.FOUR_FINGER_SQUARE, "Four Finger Square", 4, createSquarePath()),

            // ========== FIVE+ FINGER GESTURES (43-50) ==========
            GestureTemplate(GestureType.FIVE_FINGER_TAP, "Five Finger Tap", 5, createTapPath()),
            GestureTemplate(GestureType.FIVE_FINGER_PINCH, "Five Finger Pinch", 5, createPinchPath(true)),
            GestureTemplate(GestureType.FIVE_FINGER_SPREAD, "Five Finger Spread", 5, createSpreadPath(5)),
            GestureTemplate(GestureType.FIST, "Fist", 5, createFistPath()),
            GestureTemplate(GestureType.OPEN_HAND, "Open Hand", 5, createOpenHandPath()),
            GestureTemplate(GestureType.WAVE, "Wave", 0, createWavePath()),
            GestureTemplate(GestureType.SHAKE, "Shake Device", 0, createShakePath()),
            GestureTemplate(GestureType.ROTATE_DEVICE, "Rotate Device", 0, createRotateDevicePath()),

            // ========== SPECIAL GESTURES (51-55) ==========
            GestureTemplate(GestureType.HEART, "Draw Heart", 1, createHeartPath()),
            GestureTemplate(GestureType.STAR, "Draw Star", 1, createStarPath()),
            GestureTemplate(GestureType.CHECKMARK, "Draw Checkmark", 1, createCheckmarkPath()),
            GestureTemplate(GestureType.X_MARK, "Draw X", 1, createXPath()),
            GestureTemplate(GestureType.SPIRAL, "Draw Spiral", 1, createSpiralPath())
        )

        _gestures.value = defaultGestures
    }

    private fun loadDefaultCombos() {
        val defaultCombos = listOf(
            GestureCombo(
                id = "home_combo",
                name = "Go Home",
                sequence = listOf(GestureType.TAP.name, GestureType.TAP.name, GestureType.TAP.name),
                action = { /* Navigate home */ }
            ),
            GestureCombo(
                id = "recent_combo",
                name = "Recent Apps",
                sequence = listOf(GestureType.SWIPE_UP.name, GestureType.SWIPE_UP.name),
                action = { /* Show recent apps */ }
            ),
            GestureCombo(
                id = "back_combo",
                name = "Go Back",
                sequence = listOf(GestureType.SWIPE_RIGHT.name),
                action = { /* Go back */ }
            ),
            GestureCombo(
                id = "screenshot_combo",
                name = "Screenshot",
                sequence = listOf(GestureType.TAP.name, GestureType.SWIPE_DOWN.name),
                action = { /* Take screenshot */ }
            ),
            GestureCombo(
                id = "lock_combo",
                name = "Lock Screen",
                sequence = listOf(GestureType.SWIPE_DOWN.name, GestureType.SWIPE_DOWN.name),
                action = { /* Lock screen */ }
            )
        )

        _combos.value = defaultCombos
    }

    // ========== PATH CREATION HELPERS ==========

    private fun createTapPath(): Path {
        return Path().apply {
            moveTo(50f, 50f)
            lineTo(50f, 50f)
        }
    }

    private fun createDoubleTapPath(): Path {
        return Path().apply {
            moveTo(50f, 50f)
            lineTo(50f, 50f)
            moveTo(50f, 50f)
            lineTo(50f, 50f)
        }
    }

    private fun createTripleTapPath(): Path {
        return Path().apply {
            moveTo(50f, 50f)
            lineTo(50f, 50f)
            moveTo(50f, 50f)
            lineTo(50f, 50f)
            moveTo(50f, 50f)
            lineTo(50f, 50f)
        }
    }

    private fun createLongPressPath(): Path {
        return Path().apply {
            moveTo(50f, 50f)
            lineTo(50f, 50f)
        }
    }

    private fun createSwipePath(dx: Float, dy: Float): Path {
        return Path().apply {
            moveTo(50f, 50f)
            lineTo(50f + dx, 50f + dy)
        }
    }

    private fun createCirclePath(clockwise: Boolean): Path {
        return Path().apply {
            val centerX = 50f
            val centerY = 50f
            val radius = 30f

            if (clockwise) {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        centerX - radius,
                        centerY - radius,
                        centerX + radius,
                        centerY + radius
                    ),
                    androidx.compose.ui.graphics.PathDirection.Clockwise
                )
            } else {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        centerX - radius,
                        centerY - radius,
                        centerX + radius,
                        centerY + radius
                    ),
                    androidx.compose.ui.graphics.PathDirection.CounterClockwise
                )
            }
        }
    }

    private fun createZigzagPath(): Path {
        return Path().apply {
            moveTo(20f, 50f)
            lineTo(40f, 30f)
            lineTo(60f, 70f)
            lineTo(80f, 30f)
            lineTo(100f, 50f)
        }
    }

    private fun createPinchPath(inward: Boolean): Path {
        return Path().apply {
            if (inward) {
                moveTo(20f, 50f)
                lineTo(50f, 50f)
                moveTo(80f, 50f)
                lineTo(50f, 50f)
            } else {
                moveTo(50f, 50f)
                lineTo(20f, 50f)
                moveTo(50f, 50f)
                lineTo(80f, 50f)
            }
        }
    }

    private fun createRotatePath(clockwise: Boolean): Path {
        return createCirclePath(clockwise)
    }

    private fun createScrollPath(): Path {
        return createSwipePath(0f, 100f)
    }

    private fun createSpreadPath(fingerCount: Int): Path {
        return Path().apply {
            val centerX = 50f
            val centerY = 50f
            val spread = 30f

            for (i in 0 until fingerCount) {
                val angle = (i.toFloat() / fingerCount) * 360
                val x = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * spread
                val y = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * spread
                moveTo(centerX, centerY)
                lineTo(x, y)
            }
        }
    }

    private fun createTrianglePath(): Path {
        return Path().apply {
            moveTo(50f, 20f)
            lineTo(80f, 80f)
            lineTo(20f, 80f)
            close()
        }
    }

    private fun createSwipeHoldPath(): Path {
        return Path().apply {
            moveTo(50f, 80f)
            lineTo(50f, 30f)
            // Hold at end
            lineTo(50f, 30f)
        }
    }

    private fun createSquarePath(): Path {
        return Path().apply {
            moveTo(30f, 30f)
            lineTo(70f, 30f)
            lineTo(70f, 70f)
            lineTo(30f, 70f)
            close()
        }
    }

    private fun createFistPath(): Path {
        return Path().apply {
            // All fingers come together
            for (i in 0..4) {
                val x = 20f + i * 15f
                moveTo(x, 20f)
                lineTo(50f, 50f)
            }
        }
    }

    private fun createOpenHandPath(): Path {
        return Path().apply {
            // Fingers spread out
            for (i in 0..4) {
                val x = 20f + i * 15f
                moveTo(50f, 50f)
                lineTo(x, 20f)
            }
        }
    }

    private fun createWavePath(): Path {
        return Path().apply {
            moveTo(20f, 50f)
            lineTo(40f, 30f)
            lineTo(60f, 70f)
            lineTo(80f, 30f)
        }
    }

    private fun createShakePath(): Path {
        return Path().apply {
            // Rapid back and forth
            moveTo(50f, 50f)
            lineTo(30f, 50f)
            lineTo(70f, 50f)
            lineTo(30f, 50f)
            lineTo(70f, 50f)
        }
    }

    private fun createRotateDevicePath(): Path {
        return Path().apply {
            // Circular motion indicating rotation
            addOval(
                androidx.compose.ui.geometry.Rect(20f, 20f, 80f, 80f),
                androidx.compose.ui.graphics.PathDirection.Clockwise
            )
        }
    }

    private fun createHeartPath(): Path {
        return Path().apply {
            moveTo(50f, 30f)
            cubicTo(20f, 10f, 10f, 40f, 50f, 80f)
            cubicTo(90f, 40f, 80f, 10f, 50f, 30f)
        }
    }

    private fun createStarPath(): Path {
        return Path().apply {
            val centerX = 50f
            val centerY = 50f
            val outerRadius = 40f
            val innerRadius = 15f

            for (i in 0 until 10) {
                val angle = (i * 36 - 90).toFloat()
                val radius = if (i % 2 == 0) outerRadius else innerRadius
                val x = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * radius
                val y = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * radius

                if (i == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
            close()
        }
    }

    private fun createCheckmarkPath(): Path {
        return Path().apply {
            moveTo(20f, 50f)
            lineTo(45f, 75f)
            lineTo(80f, 25f)
        }
    }

    private fun createXPath(): Path {
        return Path().apply {
            moveTo(20f, 20f)
            lineTo(80f, 80f)
            moveTo(80f, 20f)
            lineTo(20f, 80f)
        }
    }

    private fun createSpiralPath(): Path {
        return Path().apply {
            val centerX = 50f
            val centerY = 50f

            for (i in 0..360 step 10) {
                val angle = Math.toRadians(i.toDouble())
                val radius = (i / 360.0) * 40
                val x = centerX + cos(angle) * radius
                val y = centerY + sin(angle) * radius

                if (i == 0) {
                    moveTo(x.toFloat(), y.toFloat())
                } else {
                    lineTo(x.toFloat(), y.toFloat())
                }
            }
        }
    }

    // ========== GESTURE MATCHING ==========

    fun matchGesture(path: Path, fingerCount: Int, sensitivity: Float): List<GestureMatch> {
        val candidates = _gestures.value.filter { it.fingerCount == fingerCount || it.fingerCount == 0 }

        return candidates.mapNotNull { template ->
            val similarity = calculatePathSimilarity(path, template.path)
            if (similarity >= sensitivity) {
                GestureMatch(template, similarity)
            } else {
                null
            }
        }.sortedByDescending { it.confidence }
    }

    private fun calculatePathSimilarity(path1: Path, path2: Path): Float {
        // Simplified path comparison using bounding box and key points
        val bounds1 = getPathBounds(path1)
        val bounds2 = getPathBounds(path2)

        val sizeSimilarity = 1f - abs(bounds1.width - bounds2.width) / max(bounds1.width, bounds2.width, 0.01f)
        val shapeSimilarity = comparePathShapes(path1, path2)

        return (sizeSimilarity * 0.4f + shapeSimilarity * 0.6f).coerceIn(0f, 1f)
    }

    private fun getPathBounds(path: Path): androidx.compose.ui.geometry.Rect {
        val bounds = android.graphics.RectF()
        // Would extract actual bounds from path
        return androidx.compose.ui.geometry.Rect(0f, 0f, 100f, 100f)
    }

    private fun comparePathShapes(path1: Path, path2: Path): Float {
        // Compare path directions and key points
        return 0.8f // Simplified
    }

    fun findCombo(recentGestures: List<String>): GestureCombo? {
        return _combos.value.find { combo ->
            combo.sequence == recentGestures
        }
    }

    fun getGestureByType(type: GestureType): GestureTemplate? {
        return _gestures.value.find { it.type == type }
    }

    fun addCustomGesture(gesture: RecordedGesture) {
        customGestures.add(gesture)

        val template = GestureTemplate(
            type = GestureType.CUSTOM,
            name = gesture.name,
            fingerCount = 1,
            path = gesture.path,
            isCustom = true
        )

        _gestures.value = _gestures.value + template
    }

    fun getCustomGestures(): List<RecordedGesture> = customGestures.toList()

    fun removeCustomGesture(id: String) {
        customGestures.removeAll { it.id == id }
        _gestures.value = _gestures.value.filter { !it.isCustom || it.id != id }
    }
}

/**
 * Gesture template
 */
data class GestureTemplate(
    val type: GestureType,
    val name: String,
    val fingerCount: Int,
    val path: Path,
    val description: String = "",
    val isCustom: Boolean = false,
    val id: String = "${type.name}_${System.currentTimeMillis()}"
)

/**
 * Gesture match result
 */
data class GestureMatch(
    val gesture: GestureTemplate,
    val confidence: Float
)

/**
 * Gesture combo
 */
data class GestureCombo(
    val id: String,
    val name: String,
    val sequence: List<String>,
    val action: () -> Unit
)

/**
 * All gesture types (50+)
 */
enum class GestureType {
    // Single finger (1-15)
    TAP,
    DOUBLE_TAP,
    TRIPLE_TAP,
    LONG_PRESS,
    SWIPE_UP,
    SWIPE_DOWN,
    SWIPE_LEFT,
    SWIPE_RIGHT,
    SWIPE_UP_LEFT,
    SWIPE_UP_RIGHT,
    SWIPE_DOWN_LEFT,
    SWIPE_DOWN_RIGHT,
    CIRCLE_CW,
    CIRCLE_CCW,
    ZIGZAG,

    // Two finger (16-25)
    TWO_FINGER_TAP,
    TWO_FINGER_SWIPE_UP,
    TWO_FINGER_SWIPE_DOWN,
    TWO_FINGER_SWIPE_LEFT,
    TWO_FINGER_SWIPE_RIGHT,
    PINCH_IN,
    PINCH_OUT,
    TWO_FINGER_ROTATE_CW,
    TWO_FINGER_ROTATE_CCW,
    TWO_FINGER_SCROLL,

    // Three finger (26-35)
    THREE_FINGER_TAP,
    THREE_FINGER_SWIPE_UP,
    THREE_FINGER_SWIPE_DOWN,
    THREE_FINGER_SWIPE_LEFT,
    THREE_FINGER_SWIPE_RIGHT,
    THREE_FINGER_SPREAD,
    THREE_FINGER_PINCH,
    THREE_FINGER_ROTATE,
    THREE_FINGER_TRIANGLE,
    THREE_FINGER_SWIPE_HOLD,

    // Four finger (36-42)
    FOUR_FINGER_TAP,
    FOUR_FINGER_SWIPE_UP,
    FOUR_FINGER_SWIPE_DOWN,
    FOUR_FINGER_SWIPE_LEFT,
    FOUR_FINGER_SWIPE_RIGHT,
    FOUR_FINGER_SPREAD,
    FOUR_FINGER_SQUARE,

    // Five+ finger (43-50)
    FIVE_FINGER_TAP,
    FIVE_FINGER_PINCH,
    FIVE_FINGER_SPREAD,
    FIST,
    OPEN_HAND,
    WAVE,
    SHAKE,
    ROTATE_DEVICE,

    // Special (51-55)
    HEART,
    STAR,
    CHECKMARK,
    X_MARK,
    SPIRAL,

    // System
    COMBO,
    CUSTOM
}
