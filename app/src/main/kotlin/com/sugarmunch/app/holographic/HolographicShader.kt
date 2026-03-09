package com.sugarmunch.app.holographic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*

/**
 * Holographic Shader - Iridescent, light-refracting surface effects
 * 
 * Features:
 * - Rainbow iridescence
 * - Light refraction simulation
 * - Dynamic color shifting
 * - Interference patterns
 * - Specular highlights
 */
class HolographicShader {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Shader state
    private val _shaderState = MutableStateFlow(HolographicShaderState())
    val shaderState: StateFlow<HolographicShaderState> = _shaderState.asStateFlow()

    // Shader configuration
    var shaderConfig = HolographicShaderConfig(
        iridescenceStrength = 0.8f,
        refractionIndex = 1.5f,
        colorShiftSpeed = 0.5f,
        interferencePattern = InterferencePattern.RAINBOW,
        specularIntensity = 0.6f,
        roughness = 0.2f,
        metalness = 0.8f
    )

    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true
        startShaderAnimation()
    }

    fun stop() {
        isRunning = false
    }

    private fun startShaderAnimation() {
        scope.launch {
            var time = 0f

            while (isRunning) {
                time += 0.016f

                _shaderState.value = HolographicShaderState(
                    time = time,
                    colorShift = sin(time * shaderConfig.colorShiftSpeed) * 0.5f + 0.5f,
                    iridescenceOffset = Offset(
                        sin(time * 0.3f) * 10,
                        cos(time * 0.4f) * 10
                    ),
                    specularHighlight = calculateSpecularHighlight()
                )

                delay(16)
            }
        }
    }

    private fun calculateSpecularHighlight(): Float {
        return (sin(_shaderState.value.time * 2) * 0.5f + 0.5f) * shaderConfig.specularIntensity
    }

    // ========== COLOR GENERATION ==========

    /**
     * Generate iridescent color based on angle and position
     */
    fun getIridescentColor(
        position: Offset,
        viewAngle: Offset,
        size: Size
    ): Color {
        val normalizedPos = Offset(
            position.x / size.width,
            position.y / size.height
        )

        val angleFactor = (viewAngle.x + viewAngle.y) / 90f

        return when (shaderConfig.interferencePattern) {
            InterferencePattern.RAINBOW -> generateRainbowIridescence(normalizedPos, angleFactor)
            InterferencePattern.PEARL -> generatePearlIridescence(normalizedPos, angleFactor)
            InterferencePattern.OIL -> generateOilIridescence(normalizedPos, angleFactor)
            InterferencePattern.BUTTERFLY -> generateButterflyIridescence(normalizedPos, angleFactor)
            InterferencePattern.CUSTOM -> generateCustomIridescence(normalizedPos, angleFactor)
        }
    }

    private fun generateRainbowIridescence(pos: Offset, angleFactor: Float): Color {
        val time = _shaderState.value.time
        val colorShift = _shaderState.value.colorShift

        val hue = (pos.x * 360 + pos.y * 180 + time * 50 + angleFactor * 30 + colorShift * 60) % 360
        val saturation = 0.8f + shaderConfig.iridescenceStrength * 0.2f
        val lightness = 0.5f + sin(pos.x * PI * 4 + time) * 0.1f

        return Color.hsl(hue, saturation, lightness, alpha = 0.8f)
    }

    private fun generatePearlIridescence(pos: Offset, angleFactor: Float): Color {
        val time = _shaderState.value.time
        val baseHue = 200f + angleFactor * 20

        val pearlColors = listOf(
            Color.hsl(baseHue, 0.3f, 0.9f),
            Color.hsl(baseHue + 30, 0.4f, 0.85f),
            Color.hsl(baseHue + 60, 0.3f, 0.95f),
            Color.hsl(baseHue + 90, 0.2f, 0.9f)
        )

        val colorIndex = ((pos.x + pos.y + time * 0.2) * pearlColors.size) % pearlColors.size
        return pearlColors[colorIndex.toInt().coerceIn(0, pearlColors.size - 1)]
            .copy(alpha = 0.7f + shaderConfig.iridescenceStrength * 0.3f)
    }

    private fun generateOilIridescence(pos: Offset, angleFactor: Float): Color {
        val time = _shaderState.value.time

        // Oil slick produces concentric rainbow rings
        val distance = sqrt(pos.x * pos.x + pos.y * pos.y)
        val ringPhase = distance * 20 + time

        val hue = (ringPhase * 50) % 360
        val saturation = 0.9f
        val lightness = 0.4f + sin(ringPhase * 2) * 0.1f

        return Color.hsl(hue, saturation, lightness, alpha = 0.6f)
    }

    private fun generateButterflyIridescence(pos: Offset, angleFactor: Float): Color {
        val time = _shaderState.value.time

        // Butterfly wing pattern with structural coloration
        val scale = sin(pos.x * PI * 8) * cos(pos.y * PI * 8)
        val baseHue = 240f + angleFactor * 40 // Blue-purple base

        val hue = baseHue + scale * 60 + time * 20
        val saturation = 0.7f + abs(scale) * 0.3f
        val lightness = 0.5f + scale * 0.2f

        return Color.hsl(hue, saturation, lightness, alpha = 0.8f)
    }

    private fun generateCustomIridescence(pos: Offset, angleFactor: Float): Color {
        val time = _shaderState.value.time
        val iridescenceOffset = _shaderState.value.iridescenceOffset

        // Customizable iridescence formula
        val r = sin(pos.x * 10 + time + iridescenceOffset.x) * 0.5f + 0.5f
        val g = cos(pos.y * 8 + time * 1.5f + iridescenceOffset.y) * 0.5f + 0.5f
        val b = sin((pos.x + pos.y) * 6 + time * 0.8f) * 0.5f + 0.5f

        return Color(
            red = r * shaderConfig.iridescenceStrength,
            green = g * shaderConfig.iridescenceStrength,
            blue = b * shaderConfig.iridescenceStrength,
            alpha = 0.7f
        )
    }

    // ========== SHADER EFFECTS ==========

    /**
     * Apply holographic effect to brush
     */
    fun applyHolographicEffect(
        baseBrush: Brush,
        size: Size,
        viewAngle: Offset
    ): Brush {
        return Brush.radialGradient(
            colors = listOf(
                getIridescentColor(Offset(size.width / 2, size.height / 2), viewAngle, size),
                getIridescentColor(Offset(size.width * 0.3f, size.height * 0.3f), viewAngle, size),
                getIridescentColor(Offset(size.width * 0.7f, size.height * 0.7f), viewAngle, size)
            ),
            center = Offset(size.width / 2 + _shaderState.value.iridescenceOffset.x, size.height / 2 + _shaderState.value.iridescenceOffset.y),
            radius = maxOf(size.width, size.height) * 0.8f
        )
    }

    /**
     * Create holographic linear gradient
     */
    fun createHolographicGradient(
        start: Offset,
        end: Offset,
        size: Size,
        viewAngle: Offset
    ): Brush {
        return Brush.linearGradient(
            colors = listOf(
                getIridescentColor(start, viewAngle, size),
                getIridescentColor(Offset((start.x + end.x) / 2, (start.y + end.y) / 2), viewAngle, size),
                getIridescentColor(end, viewAngle, size)
            ),
            start = start,
            end = end
        )
    }

    /**
     * Create holographic radial gradient
     */
    fun createHolographicRadialGradient(
        center: Offset,
        radius: Float,
        size: Size,
        viewAngle: Offset
    ): Brush {
        val colors = (0..5).map { i ->
            val angle = (i / 5f) * 360
            val pos = Offset(
                center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * radius * 0.5f,
                center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * radius * 0.5f
            )
            getIridescentColor(pos, viewAngle, size)
        }

        return Brush.radialGradient(
            colors = colors,
            center = center,
            radius = radius
        )
    }

    /**
     * Draw holographic overlay
     */
    fun drawHolographicOverlay(
        drawScope: DrawScope,
        size: Size,
        viewAngle: Offset
    ) {
        val state = _shaderState.value

        // Draw interference pattern overlay
        for (i in 0 until 10) {
            val alpha = (0.05f * shaderConfig.iridescenceStrength) * (1 - i / 10f)
            val offset = Offset(
                sin(state.time * 0.5f + i) * 20,
                cos(state.time * 0.6f + i) * 20
            )

            drawScope.drawCircle(
                brush = createHolographicRadialGradient(
                    center = Offset(size.width / 2 + offset.x, size.height / 2 + offset.y),
                    radius = size.width * (0.3f + i * 0.1f),
                    size = size,
                    viewAngle = viewAngle
                ),
                radius = size.width * (0.5f + i * 0.1f),
                alpha = alpha
            )
        }
    }

    // ========== PRESET CONFIGURATIONS ==========

    fun applyPreset(preset: HolographicPreset) {
        shaderConfig = when (preset) {
            HolographicPreset.RAINBOW -> shaderConfig.copy(
                iridescenceStrength = 1.0f,
                interferencePattern = InterferencePattern.RAINBOW,
                colorShiftSpeed = 0.8f
            )
            HolographicPreset.PEARL -> shaderConfig.copy(
                iridescenceStrength = 0.5f,
                interferencePattern = InterferencePattern.PEARL,
                colorShiftSpeed = 0.3f,
                specularIntensity = 0.8f
            )
            HolographicPreset.OIL -> shaderConfig.copy(
                iridescenceStrength = 0.9f,
                interferencePattern = InterferencePattern.OIL,
                colorShiftSpeed = 0.6f,
                roughness = 0.1f
            )
            HolographicPreset.BUTTERFLY -> shaderConfig.copy(
                iridescenceStrength = 0.8f,
                interferencePattern = InterferencePattern.BUTTERFLY,
                colorShiftSpeed = 0.4f,
                metalness = 0.6f
            )
            HolographicPreset.SUBTLE -> shaderConfig.copy(
                iridescenceStrength = 0.3f,
                interferencePattern = InterferencePattern.PEARL,
                colorShiftSpeed = 0.2f,
                specularIntensity = 0.3f
            )
            HolographicPreset.EXTREME -> shaderConfig.copy(
                iridescenceStrength = 1.0f,
                interferencePattern = InterferencePattern.RAINBOW,
                colorShiftSpeed = 1.5f,
                specularIntensity = 1.0f,
                roughness = 0.0f
            )
        }
    }
}

/**
 * Holographic shader state
 */
data class HolographicShaderState(
    val time: Float = 0f,
    val colorShift: Float = 0.5f,
    val iridescenceOffset: Offset = Offset.Zero,
    val specularHighlight: Float = 0.5f
)

/**
 * Holographic shader configuration
 */
data class HolographicShaderConfig(
    val iridescenceStrength: Float = 0.8f,
    val refractionIndex: Float = 1.5f,
    val colorShiftSpeed: Float = 0.5f,
    val interferencePattern: InterferencePattern = InterferencePattern.RAINBOW,
    val specularIntensity: Float = 0.6f,
    val roughness: Float = 0.2f,
    val metalness: Float = 0.8f
)

/**
 * Interference pattern types
 */
enum class InterferencePattern {
    RAINBOW,    // Full spectrum rainbow
    PEARL,      // Soft pearl-like iridescence
    OIL,        // Oil slick pattern
    BUTTERFLY,  // Butterfly wing structural color
    CUSTOM      // User-defined pattern
}

/**
 * Holographic presets
 */
enum class HolographicPreset {
    RAINBOW,
    PEARL,
    OIL,
    BUTTERFLY,
    SUBTLE,
    EXTREME
}

/**
 * Holographic brush factory
 */
object HolographicBrushes {

    fun createHolographicCardBrush(
        size: Size,
        viewAngle: Offset,
        shader: HolographicShader
    ): Brush {
        return shader.createHolographicGradient(
            start = Offset(0f, 0f),
            end = size,
            size = size,
            viewAngle = viewAngle
        )
    }

    fun createHolographicIconBrush(
        size: Size,
        viewAngle: Offset,
        shader: HolographicShader
    ): Brush {
        return shader.createHolographicRadialGradient(
            center = Offset(size.width / 2, size.height / 2),
            radius = maxOf(size.width, size.height) * 0.7f,
            size = size,
            viewAngle = viewAngle
        )
    }
}
