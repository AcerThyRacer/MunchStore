package com.sugarmunch.app.ui.shaders

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import kotlin.math.*
import kotlin.random.Random

/**
 * Phase 2: Advanced Shader Effects
 * 
 * 15 new real-time shader effects for maximum visual impact.
 * Each effect supports intensity, speed, and color customization.
 */

// ─────────────────────────────────────────────────────────────────────────────────
// EXTENDED SHADER ENUM
// ─────────────────────────────────────────────────────────────────────────────────

enum class AdvancedShaderEffect {
    // Original effects (from ShaderEffectEngine)
    WATER_RIPPLE,
    CHROMATIC_ABERRATION,
    GLITCH,
    HEAT_HAZE,
    FROSTED_GLASS,
    HOLOGRAPHIC_SWEEP,
    CRT_SCANLINES,
    MATRIX_RAIN,
    LIQUID_CHROME,
    
    // Phase 2: 15 NEW EFFECTS
    BLOOM,
    VIGNETTE,
    PIXELATE,
    KALEIDOSCOPE,
    THERMAL_VISION,
    NIGHT_VISION,
    POSTERIZE,
    SOLARIZE,
    EMBOSS,
    EDGE_DETECT,
    NOISE_GRAIN,
    PRISM,
    AURORA_BOREALIS,
    ELECTRIC_FIELD,
    NEON_GLOW,
    
    // Phase 2 Extended: 20 ADVANCED POST-PROCESSING
    DEPTH_OF_FIELD,
    MOTION_BLUR,
    SSAO,
    COLOR_GRADING,
    FILM_GRAIN,
    VHS_EFFECT,
    DATAMOSH,
    ASCII_ART,
    OIL_PAINTING,
    WATERCOLOR,
    PIXEL_SORT,
    CHROMATIC_SHIFT,
    LIQUIFY,
    KALEIDOSCOPE_MIRROR,
    FRESNEL_GLOW,
    SUBSURFACE_SCATTERING,
    CAUSTICS,
    GOD_RAYS,
    LENS_FLARE,
    TILT_SHIFT
}

data class AdvancedShaderConfig(
    val effect: AdvancedShaderEffect,
    val intensity: Float = 1f,
    val speed: Float = 1f,
    val color: Color? = null,
    val secondaryColor: Color? = null,
    val touchPoint: Offset? = null,
    val audioAmplitude: Float = 0f,
    val audioBass: Float = 0f,
    val audioTreble: Float = 0f
)

// ─────────────────────────────────────────────────────────────────────────────────
// MAIN COMPOSABLE
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
fun AdvancedShaderOverlay(
    config: AdvancedShaderConfig,
    modifier: Modifier = Modifier
) {
    var timeSeconds by remember { mutableFloatStateOf(0f) }
    var lastFrameNanos by remember { mutableLongStateOf(0L) }

    LaunchedEffect(config.speed) {
        while (true) {
            withFrameNanos { nanos ->
                if (lastFrameNanos != 0L) {
                    val deltaSec = (nanos - lastFrameNanos) / 1_000_000_000f
                    timeSeconds += deltaSec * config.speed
                }
                lastFrameNanos = nanos
            }
        }
    }

    when (config.effect) {
        // Original effects - delegate to existing implementation
        AdvancedShaderEffect.WATER_RIPPLE,
        AdvancedShaderEffect.CHROMATIC_ABERRATION,
        AdvancedShaderEffect.GLITCH,
        AdvancedShaderEffect.HEAT_HAZE,
        AdvancedShaderEffect.FROSTED_GLASS,
        AdvancedShaderEffect.HOLOGRAPHIC_SWEEP,
        AdvancedShaderEffect.CRT_SCANLINES,
        AdvancedShaderEffect.MATRIX_RAIN,
        AdvancedShaderEffect.LIQUID_CHROME -> {
            // Map to original shader config
            ShaderEffectOverlay(
                config = ShaderConfig(
                    effect = ShaderEffect.valueOf(config.effect.name),
                    intensity = config.intensity,
                    speed = config.speed,
                    color = config.color,
                    touchPoint = config.touchPoint
                ),
                modifier = modifier
            )
        }
        
        // Phase 2: NEW EFFECTS
        AdvancedShaderEffect.BLOOM -> BloomOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.VIGNETTE -> VignetteOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.PIXELATE -> PixelateOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.KALEIDOSCOPE -> KaleidoscopeOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.THERMAL_VISION -> ThermalVisionOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.NIGHT_VISION -> NightVisionOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.POSTERIZE -> PosterizeOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.SOLARIZE -> SolarizeOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.EMBOSS -> EmbossOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.EDGE_DETECT -> EdgeDetectOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.NOISE_GRAIN -> NoiseGrainOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.PRISM -> PrismOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.AURORA_BOREALIS -> AuroraBorealisOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.ELECTRIC_FIELD -> ElectricFieldOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.NEON_GLOW -> NeonGlowOverlay(config, timeSeconds, modifier)
        
        // Phase 2 Extended: 20 ADVANCED POST-PROCESSING
        AdvancedShaderEffect.DEPTH_OF_FIELD -> DepthOfFieldOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.MOTION_BLUR -> MotionBlurOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.SSAO -> SSAOOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.COLOR_GRADING -> ColorGradingOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.FILM_GRAIN -> FilmGrainOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.VHS_EFFECT -> VHSEffectOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.DATAMOSH -> DataMoshOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.ASCII_ART -> AsciiArtOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.OIL_PAINTING -> OilPaintingOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.WATERCOLOR -> WatercolorOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.PIXEL_SORT -> PixelSortOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.CHROMATIC_SHIFT -> ChromaticShiftOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.LIQUIFY -> LiquifyOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.KALEIDOSCOPE_MIRROR -> KaleidoscopeMirrorOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.FRESNEL_GLOW -> FresnelGlowOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.SUBSURFACE_SCATTERING -> SubsurfaceScatteringOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.CAUSTICS -> CausticsOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.GOD_RAYS -> GodRaysOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.LENS_FLARE -> LensFlareOverlay(config, timeSeconds, modifier)
        AdvancedShaderEffect.TILT_SHIFT -> TiltShiftOverlay(config, timeSeconds, modifier)
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// 1. BLOOM EFFECT
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun BloomOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val tint = config.color ?: Color(0xFFFFB6C1)
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val maxRadius = max(size.width, size.height) * 0.8f
        
        // Pulsating bloom
        val pulse = 1f + sin(time * 2f * PI.toFloat()) * 0.2f * config.intensity
        val audioBoost = 1f + config.audioAmplitude * 0.5f
        
        // Multiple bloom layers
        for (layer in 0 until 5) {
            val layerRadius = maxRadius * (0.3f + layer * 0.15f) * pulse * audioBoost
            val layerAlpha = (0.15f - layer * 0.025f) * config.intensity
            
            val gradient = Brush.radialGradient(
                colors = listOf(
                    tint.copy(alpha = layerAlpha),
                    tint.copy(alpha = layerAlpha * 0.5f),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = layerRadius
            )
            
            drawCircle(
                brush = gradient,
                radius = layerRadius,
                center = Offset(centerX, centerY),
                blendMode = BlendMode.Screen
            )
        }
        
        // Central bright spot
        val coreRadius = maxRadius * 0.1f * pulse
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.3f * config.intensity),
                    tint.copy(alpha = 0.2f * config.intensity),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = coreRadius
            ),
            radius = coreRadius,
            center = Offset(centerX, centerY),
            blendMode = BlendMode.Screen
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// 2. VIGNETTE EFFECT
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun VignetteOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val tint = config.color ?: Color.Black
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val maxRadius = sqrt(centerX * centerX + centerY * centerY)
        
        // Animated vignette intensity
        val pulse = 0.7f + sin(time * PI.toFloat()) * 0.15f * config.intensity
        val vignetteRadius = maxRadius * pulse
        
        // Radial vignette
        val gradient = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color.Transparent,
                tint.copy(alpha = 0.3f * config.intensity),
                tint.copy(alpha = 0.7f * config.intensity),
                tint.copy(alpha = config.intensity)
            ),
            center = Offset(centerX, centerY),
            radius = vignetteRadius
        )
        
        drawRect(brush = gradient, size = size)
        
        // Optional: Animated corner emphasis
        val cornerSize = min(size.width, size.height) * 0.3f
        val cornerAlpha = 0.2f * config.intensity * (0.5f + 0.5f * sin(time * 2f))
        
        // Top-left corner
        drawArc(
            color = tint.copy(alpha = cornerAlpha),
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = Offset(-cornerSize / 2, -cornerSize / 2),
            size = Size(cornerSize, cornerSize)
        )
        
        // Top-right corner
        drawArc(
            color = tint.copy(alpha = cornerAlpha),
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = Offset(size.width - cornerSize / 2, -cornerSize / 2),
            size = Size(cornerSize, cornerSize)
        )
        
        // Bottom-left corner
        drawArc(
            color = tint.copy(alpha = cornerAlpha),
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = Offset(-cornerSize / 2, size.height - cornerSize / 2),
            size = Size(cornerSize, cornerSize)
        )
        
        // Bottom-right corner
        drawArc(
            color = tint.copy(alpha = cornerAlpha),
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = Offset(size.width - cornerSize / 2, size.height - cornerSize / 2),
            size = Size(cornerSize, cornerSize)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// 3. PIXELATE EFFECT
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun PixelateOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val tint = config.color ?: Color(0xFF4FC3F7)
    
    // Animated pixel size
    val basePixelSize = (8 + 16 * config.intensity).toInt()
    val pixelSize = basePixelSize + (sin(time * 3f) * 4f).toInt()
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val cols = (size.width / pixelSize).toInt() + 1
        val rows = (size.height / pixelSize).toInt() + 1
        
        // Draw pixel grid
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val x = col * pixelSize.toFloat()
                val y = row * pixelSize.toFloat()
                
                // Create pattern based on position and time
                val pattern = sin((col + row + time * 5f) * 0.5f) * 0.5f + 0.5f
                val alpha = pattern * 0.15f * config.intensity
                
                drawRect(
                    color = tint.copy(alpha = alpha),
                    topLeft = Offset(x, y),
                    size = Size(pixelSize.toFloat(), pixelSize.toFloat())
                )
            }
        }
        
        // Pixel grid lines
        val gridAlpha = 0.05f * config.intensity
        for (col in 0..cols) {
            drawLine(
                color = tint.copy(alpha = gridAlpha),
                start = Offset(col * pixelSize.toFloat(), 0f),
                end = Offset(col * pixelSize.toFloat(), size.height),
                strokeWidth = 1f
            )
        }
        for (row in 0..rows) {
            drawLine(
                color = tint.copy(alpha = gridAlpha),
                start = Offset(0f, row * pixelSize.toFloat()),
                end = Offset(size.width, row * pixelSize.toFloat()),
                strokeWidth = 1f
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// 4. KALEIDOSCOPE EFFECT
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun KaleidoscopeOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val segments = (6 + (4 * config.intensity).toInt()).coerceIn(6, 16)
    val tint = config.color ?: Color(0xFFFFB6C1)
    val secondaryTint = config.secondaryColor ?: Color(0xFF98FF98)
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = max(size.width, size.height) * 0.6f
        
        val angleStep = 360f / segments
        
        for (i in 0 until segments) {
            val angle = i * angleStep + time * 20f
            val rad = Math.toRadians(angle.toDouble())
            
            // Primary segment
            val gradient = Brush.sweepGradient(
                colors = listOf(
                    tint.copy(alpha = 0.2f * config.intensity),
                    secondaryTint.copy(alpha = 0.15f * config.intensity),
                    Color.Transparent,
                    tint.copy(alpha = 0.1f * config.intensity)
                ),
                center = Offset(centerX, centerY),
                angleOffset = angle
            )
            
            // Draw wedge
            val path = Path().apply {
                moveTo(centerX, centerY)
                arcTo(
                    rect = Rect(
                        left = centerX - radius,
                        top = centerY - radius,
                        right = centerX + radius,
                        bottom = centerY + radius
                    ),
                    startAngleDegrees = angle,
                    sweepAngleDegrees = angleStep * 0.8f,
                    forceMoveTo = false
                )
                close()
            }
            
            drawPath(
                path = path,
                brush = gradient,
                blendMode = BlendMode.Screen
            )
        }
        
        // Center decoration
        val centerPulse = 1f + sin(time * 4f) * 0.2f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.3f * config.intensity),
                    tint.copy(alpha = 0.2f * config.intensity),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = radius * 0.2f * centerPulse
            ),
            radius = radius * 0.2f * centerPulse,
            center = Offset(centerX, centerY)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// 5. THERMAL VISION EFFECT
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun ThermalVisionOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // Thermal color gradient: blue -> cyan -> green -> yellow -> orange -> red
        val thermalColors = listOf(
            Color(0xFF0000FF), // Cold - Blue
            Color(0xFF00FFFF), // Cyan
            Color(0xFF00FF00), // Green
            Color(0xFFFFFF00), // Yellow
            Color(0xFFFF8000), // Orange
            Color(0xFFFF0000)  // Hot - Red
        )
        
        val cellSize = 20f
        val cols = (size.width / cellSize).toInt() + 1
        val rows = (size.height / cellSize).toInt() + 1
        
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val x = col * cellSize
                val y = row * cellSize
                
                // Generate heat value based on position and time
                val noise = sin((col * 0.3f + time * 2f)) * 
                           cos((row * 0.3f + time * 1.5f)) * 
                           sin((col + row) * 0.1f + time)
                val heat = (noise * 0.5f + 0.5f) * config.intensity
                
                // Map heat to color
                val colorIndex = (heat * (thermalColors.size - 1)).toInt()
                    .coerceIn(0, thermalColors.size - 2)
                val colorFraction = (heat * (thermalColors.size - 1)) - colorIndex
                
                val c1 = thermalColors[colorIndex]
                val c2 = thermalColors[colorIndex + 1]
                val color = Color(
                    c1.red * (1 - colorFraction) + c2.red * colorFraction,
                    c1.green * (1 - colorFraction) + c2.green * colorFraction,
                    c1.blue * (1 - colorFraction) + c2.blue * colorFraction,
                    0.4f * config.intensity
                )
                
                drawRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(cellSize, cellSize)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// 6. NIGHT VISION EFFECT
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun NightVisionOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val tint = config.color ?: Color(0xFF00FF00) // Classic green night vision
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // Green overlay
        drawRect(
            color = tint.copy(alpha = 0.15f * config.intensity),
            size = size,
            blendMode = BlendMode.Screen
        )
        
        // Scanlines
        val scanlineSpacing = 3f
        val scanlineCount = (size.height / scanlineSpacing).toInt()
        val scanlineAlpha = 0.08f * config.intensity
        
        for (i in 0 until scanlineCount) {
            val y = i * scanlineSpacing
            drawLine(
                color = Color.Black.copy(alpha = scanlineAlpha),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }
        
        // Vignette
        val vignetteRadius = max(size.width, size.height) * 0.7f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Transparent,
                    tint.copy(alpha = 0.3f * config.intensity)
                ),
                center = Offset(size.width / 2f, size.height / 2f),
                radius = vignetteRadius
            ),
            radius = vignetteRadius,
            center = Offset(size.width / 2f, size.height / 2f)
        )
        
        // Noise grain
        val noiseDensity = 0.02f * config.intensity
        val random = Random((time * 1000).toInt())
        for (i in 0 until (size.width * size.height * noiseDensity).toInt()) {
            val x = random.nextFloat() * size.width
            val y = random.nextFloat() * size.height
            val alpha = random.nextFloat() * 0.3f
            drawCircle(
                color = tint.copy(alpha = alpha),
                radius = 1f,
                center = Offset(x, y)
            )
        }
        
        // Moving scan line
        val scanY = ((time * 100f) % (size.height + 50)) - 25
        drawRect(
            color = tint.copy(alpha = 0.1f * config.intensity),
            topLeft = Offset(0f, scanY),
            size = Size(size.width, 50f),
            blendMode = BlendMode.Screen
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// 7. POSTERIZE EFFECT
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun PosterizeOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val levels = (2 + (4 * config.intensity).toInt()).coerceIn(2, 8)
    val tint = config.color ?: Color(0xFFFFB6C1)
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val cellSize = 30f
        val cols = (size.width / cellSize).toInt() + 1
        val rows = (size.height / cellSize).toInt() + 1
        
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val x = col * cellSize
                val y = row * cellSize
                
                // Generate posterized value
                val noise = sin((col + row + time * 3f) * 0.5f) * 0.5f + 0.5f
                val posterized = (noise * levels).toInt() / levels.toFloat()
                
                // Color based on posterized level
                val hue = posterized * 360f
                val color = hsvToRgb(hue, 0.7f, 0.8f, 0.3f * config.intensity)
                
                drawRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(cellSize, cellSize)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// 8. SOLARIZE EFFECT
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun SolarizeOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val tint = config.color ?: Color(0xFFFFB6C1)
    val threshold = 0.5f + sin(time) * 0.2f
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val cellSize = 15f
        val cols = (size.width / cellSize).toInt() + 1
        val rows = (size.height / cellSize).toInt() + 1
        
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val x = col * cellSize
                val y = row * cellSize
                
                // Generate value
                val value = sin((col * 0.2f + time * 2f)) * 
                           cos((row * 0.2f + time * 1.5f)) * 0.5f + 0.5f
                
                // Solarize: invert above threshold
                val solarized = if (value > threshold) {
                    1f - value
                } else {
                    value
                }
                
                val color = Color(
                    solarized * tint.red,
                    solarized * tint.green,
                    solarized * tint.blue,
                    0.4f * config.intensity
                )
                
                drawRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(cellSize, cellSize)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// 9. EMBOSS EFFECT
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmbossOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val tint = config.color ?: Color(0xFF808080)
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val cellSize = 10f
        val cols = (size.width / cellSize).toInt() + 1
        val rows = (size.height / cellSize).toInt() + 1
        
        // Generate height map
        val heightMap = Array(rows) { row ->
            FloatArray(cols) { col ->
                sin((col * 0.3f + time * 2f)) * cos((row * 0.3f + time * 1.5f)) * 0.5f + 0.5f
            }
        }
        
        // Apply emboss kernel
        for (row in 1 until rows - 1) {
            for (col in 1 until cols - 1) {
                val x = col * cellSize
                val y = row * cellSize
                
                // Emboss kernel: top-left highlight, bottom-right shadow
                val emboss = -heightMap[row - 1][col - 1] +
                             heightMap[row + 1][col + 1] +
                             heightMap[row - 1][col] * 0.5f -
                             heightMap[row + 1][col] * 0.5f
                
                val value = (emboss * 0.5f + 0.5f).coerceIn(0f, 1f)
                
                val color = Color(
                    value * tint.red,
                    value * tint.green,
                    value * tint.blue,
                    0.5f * config.intensity
                )
                
                drawRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(cellSize, cellSize)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// 10. EDGE DETECT EFFECT
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun EdgeDetectOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val tint = config.color ?: Color(0xFF00FF00)
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val cellSize = 8f
        val cols = (size.width / cellSize).toInt() + 1
        val rows = (size.height / cellSize).toInt() + 1
        
        // Generate pattern
        val pattern = Array(rows) { row ->
            FloatArray(cols) { col ->
                sin((col * 0.5f + time * 3f)) * cos((row * 0.5f + time * 2f)) * 0.5f + 0.5f
            }
        }
        
        // Sobel edge detection
        for (row in 1 until rows - 1) {
            for (col in 1 until cols - 1) {
                val x = col * cellSize
                val y = row * cellSize
                
                // Sobel X
                val gx = -pattern[row - 1][col - 1] + pattern[row - 1][col + 1] +
                         -2 * pattern[row][col - 1] + 2 * pattern[row][col + 1] +
                         -pattern[row + 1][col - 1] + pattern[row + 1][col + 1]
                
                // Sobel Y
                val gy = -pattern[row - 1][col - 1] - 2 * pattern[row - 1][col] - pattern[row - 1][col + 1] +
                         pattern[row + 1][col - 1] + 2 * pattern[row + 1][col] + pattern[row + 1][col + 1]
                
                val edge = sqrt(gx * gx + gy * gy).coerceIn(0f, 1f)
                
                drawRect(
                    color = tint.copy(alpha = edge * config.intensity),
                    topLeft = Offset(x, y),
                    size = Size(cellSize, cellSize)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// 11. NOISE GRAIN EFFECT
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun NoiseGrainOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val tint = config.color ?: Color.White
    val seed = (time * 10f).toInt()
    val random = Random(seed)
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val grainCount = (size.width * size.height * 0.1f * config.intensity).toInt()
        
        // Film grain particles
        for (i in 0 until grainCount) {
            val x = random.nextFloat() * size.width
            val y = random.nextFloat() * size.height
            val alpha = random.nextFloat() * 0.3f * config.intensity
            val size_grain = 1f + random.nextFloat() * 2f
            
            drawCircle(
                color = tint.copy(alpha = alpha),
                radius = size_grain,
                center = Offset(x, y)
            )
        }
        
        // Occasional bright spots (dust)
        for (i in 0 until (grainCount / 20)) {
            val x = random.nextFloat() * size.width
            val y = random.nextFloat() * size.height
            val alpha = random.nextFloat() * 0.5f * config.intensity
            
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = 2f + random.nextFloat() * 3f,
                center = Offset(x, y)
            )
        }
        
        // Scratches
        for (i in 0 until 3) {
            if (random.nextFloat() > 0.7f) {
                val startX = random.nextFloat() * size.width
                val startY = random.nextFloat() * size.height
                val endX = startX + (random.nextFloat() - 0.5f) * 100f
                val endY = startY + random.nextFloat() * 200f
                
                drawLine(
                    color = tint.copy(alpha = 0.1f * config.intensity),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1f
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// 12. PRISM EFFECT
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun PrismOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        
        // Rainbow prism colors
        val prismColors = listOf(
            Color(0x30FF0000),
            Color(0x30FF8800),
            Color(0x30FFFF00),
            Color(0x3000FF00),
            Color(0x3000FFFF),
            Color(0x300000FF),
            Color(0x30FF00FF)
        )
        
        // Animated prism angle
        val prismAngle = time * 30f
        
        // Multiple prism layers
        for (layer in 0 until 3) {
            val layerOffset = layer * 120f
            val layerAlpha = (1f - layer * 0.3f) * config.intensity
            
            for ((index, color) in prismColors.withIndex()) {
                val angle = prismAngle + layerOffset + index * (360f / prismColors.size)
                val rad = Math.toRadians(angle.toDouble())
                
                val offsetX = cos(rad).toFloat() * 50f * config.intensity
                val offsetY = sin(rad).toFloat() * 50f * config.intensity
                
                val gradient = Brush.linearGradient(
                    colors = listOf(
                        color.copy(alpha = color.alpha * layerAlpha),
                        Color.Transparent
                    ),
                    start = Offset(centerX + offsetX, centerY + offsetY),
                    end = Offset(centerX - offsetX, centerY - offsetY)
                )
                
                drawRect(
                    brush = gradient,
                    size = size,
                    blendMode = BlendMode.Screen
                )
            }
        }
        
        // Central prism highlight
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.2f * config.intensity),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = min(size.width, size.height) * 0.3f
            ),
            radius = min(size.width, size.height) * 0.3f,
            center = Offset(centerX, centerY),
            blendMode = BlendMode.Screen
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// 13. AURORA BOREALIS EFFECT
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun AuroraBorealisOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val auroraColors = listOf(
        Color(0xFF00FF88), // Green
        Color(0xFF00FFCC), // Cyan-green
        Color(0xFF00CCFF), // Cyan
        Color(0xFF8800FF), // Purple
        Color(0xFFFF00CC)  // Magenta
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // Multiple aurora curtains
        for (curtain in 0 until 5) {
            val curtainY = size.height * (0.2f + curtain * 0.15f)
            val curtainHeight = size.height * (0.1f + curtain * 0.05f)
            
            val path = Path().apply {
                moveTo(0f, curtainY)
                
                // Wavy curtain shape
                for (x in 0..size.width.toInt() step 20) {
                    val waveY = curtainY + 
                        sin((x * 0.01f + time * 2f + curtain)) * 30f * config.intensity +
                        sin((x * 0.02f + time * 1.5f + curtain * 0.5f)) * 20f * config.intensity
                    
                    lineTo(x.toFloat(), waveY)
                }
                
                lineTo(size.width, curtainY + curtainHeight)
                
                // Bottom wave
                for (x in size.width.toInt() downTo 0 step 20) {
                    val waveY = curtainY + curtainHeight +
                        sin((x * 0.015f + time * 1.8f + curtain)) * 20f * config.intensity
                    
                    lineTo(x.toFloat(), waveY)
                }
                
                close()
            }
            
            val colorIndex = curtain % auroraColors.size
            val nextColorIndex = (curtain + 1) % auroraColors.size
            
            val gradient = Brush.linearGradient(
                colors = listOf(
                    auroraColors[colorIndex].copy(alpha = 0.1f * config.intensity),
                    auroraColors[nextColorIndex].copy(alpha = 0.15f * config.intensity),
                    auroraColors[colorIndex].copy(alpha = 0.05f * config.intensity)
                ),
                start = Offset(0f, curtainY),
                end = Offset(0f, curtainY + curtainHeight)
            )
            
            drawPath(
                path = path,
                brush = gradient,
                blendMode = BlendMode.Screen
            )
        }
        
        // Stars
        val starRandom = Random(42)
        for (i in 0 until 50) {
            val starX = starRandom.nextFloat() * size.width
            val starY = starRandom.nextFloat() * size.height * 0.5f
            val starAlpha = (0.3f + sin(time * 3f + i) * 0.2f) * config.intensity
            
            drawCircle(
                color = Color.White.copy(alpha = starAlpha),
                radius = 1f + starRandom.nextFloat(),
                center = Offset(starX, starY)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// 14. ELECTRIC FIELD EFFECT
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun ElectricFieldOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val tint = config.color ?: Color(0xFF00FFFF)
    val random = Random((time * 100).toInt())
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // Electric arcs
        for (arc in 0 until (5 * config.intensity).toInt()) {
            val startX = random.nextFloat() * size.width
            val startY = random.nextFloat() * size.height
            var currentX = startX
            var currentY = startY
            
            val path = Path().apply {
                moveTo(currentX, currentY)
                
                // Create jagged lightning path
                for (segment in 0 until 10) {
                    val angle = random.nextFloat() * 2f * PI.toFloat()
                    val length = 20f + random.nextFloat() * 40f
                    currentX += cos(angle) * length
                    currentY += sin(angle) * length
                    
                    lineTo(currentX, currentY)
                }
            }
            
            // Glow effect
            drawPath(
                path = path,
                color = tint.copy(alpha = 0.1f * config.intensity),
                style = Stroke(width = 8f, cap = StrokeCap.Round),
                blendMode = BlendMode.Screen
            )
            
            // Core
            drawPath(
                path = path,
                color = tint.copy(alpha = 0.4f * config.intensity),
                style = Stroke(width = 2f, cap = StrokeCap.Round),
                blendMode = BlendMode.Screen
            )
            
            // Bright center
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.6f * config.intensity),
                style = Stroke(width = 1f, cap = StrokeCap.Round),
                blendMode = BlendMode.Screen
            )
        }
        
        // Electric nodes
        for (node in 0 until (8 * config.intensity).toInt()) {
            val nodeX = random.nextFloat() * size.width
            val nodeY = random.nextFloat() * size.height
            val nodeRadius = 5f + random.nextFloat() * 10f
            val pulse = 1f + sin(time * 10f + node) * 0.3f
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.5f * config.intensity),
                        tint.copy(alpha = 0.3f * config.intensity),
                        Color.Transparent
                    ),
                    center = Offset(nodeX, nodeY),
                    radius = nodeRadius * pulse
                ),
                radius = nodeRadius * pulse,
                center = Offset(nodeX, nodeY),
                blendMode = BlendMode.Screen
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// 15. NEON GLOW EFFECT
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun NeonGlowOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val neonColors = listOf(
        Color(0xFFFF00FF), // Magenta
        Color(0xFF00FFFF), // Cyan
        Color(0xFFFF0088), // Pink
        Color(0xFF00FF88), // Green
        Color(0xFF8800FF), // Purple
        Color(0xFFFFFF00)  // Yellow
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // Neon grid
        val gridSpacing = 60f
        val gridAlpha = 0.1f * config.intensity
        
        // Vertical lines
        for (x in 0..size.width.toInt() step gridSpacing.toInt()) {
            val colorIndex = (x / gridSpacing.toInt()) % neonColors.size
            val pulse = 0.7f + sin(time * 3f + x * 0.1f) * 0.3f
            
            // Glow
            drawLine(
                color = neonColors[colorIndex].copy(alpha = gridAlpha * pulse),
                start = Offset(x.toFloat(), 0f),
                end = Offset(x.toFloat(), size.height),
                strokeWidth = 8f,
                blendMode = BlendMode.Screen
            )
            
            // Core
            drawLine(
                color = neonColors[colorIndex].copy(alpha = gridAlpha * 2f * pulse),
                start = Offset(x.toFloat(), 0f),
                end = Offset(x.toFloat(), size.height),
                strokeWidth = 2f,
                blendMode = BlendMode.Screen
            )
        }
        
        // Horizontal lines
        for (y in 0..size.height.toInt() step gridSpacing.toInt()) {
            val colorIndex = (y / gridSpacing.toInt()) % neonColors.size
            val pulse = 0.7f + sin(time * 3f + y * 0.1f) * 0.3f
            
            drawLine(
                color = neonColors[colorIndex].copy(alpha = gridAlpha * pulse),
                start = Offset(0f, y.toFloat()),
                end = Offset(size.width, y.toFloat()),
                strokeWidth = 8f,
                blendMode = BlendMode.Screen
            )
            
            drawLine(
                color = neonColors[colorIndex].copy(alpha = gridAlpha * 2f * pulse),
                start = Offset(0f, y.toFloat()),
                end = Offset(size.width, y.toFloat()),
                strokeWidth = 2f,
                blendMode = BlendMode.Screen
            )
        }
        
        // Neon shapes
        val shapeCount = (3 * config.intensity).toInt()
        for (i in 0 until shapeCount) {
            val shapeX = (size.width * (0.2f + i * 0.2f))
            val shapeY = size.height * 0.5f + sin(time * 2f + i) * 100f
            val shapeSize = 40f + sin(time * 3f + i * 2f) * 20f
            val color = neonColors[i % neonColors.size]
            
            // Glow
            drawCircle(
                color = color.copy(alpha = 0.2f * config.intensity),
                radius = shapeSize * 1.5f,
                center = Offset(shapeX, shapeY),
                blendMode = BlendMode.Screen
            )
            
            // Core
            drawCircle(
                color = color.copy(alpha = 0.5f * config.intensity),
                radius = shapeSize,
                center = Offset(shapeX, shapeY),
                style = Stroke(width = 3f),
                blendMode = BlendMode.Screen
            )
            
            // Inner glow
            drawCircle(
                color = Color.White.copy(alpha = 0.3f * config.intensity),
                radius = shapeSize * 0.8f,
                center = Offset(shapeX, shapeY),
                style = Stroke(width = 1f),
                blendMode = BlendMode.Screen
            )
        }
        
        // Horizon line
        val horizonY = size.height * 0.7f
        drawLine(
            color = neonColors[0].copy(alpha = 0.3f * config.intensity),
            start = Offset(0f, horizonY),
            end = Offset(size.width, horizonY),
            strokeWidth = 4f,
            blendMode = BlendMode.Screen
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// PHASE 2 EXTENDED: 20 ADVANCED POST-PROCESSING SHADERS
// ─────────────────────────────────────────────────────────────────────────────────

// 1. DEPTH OF FIELD - Cinematic bokeh effect
@Composable
private fun DepthOfFieldOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val focusPoint = config.touchPoint ?: Offset(size.width / 2f, size.height / 2f)
        val focusRadius = 100f * config.intensity
        
        // Draw blurred background
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    config.color?.copy(alpha = 0.3f) ?: Color.Blue.copy(alpha = 0.3f)
                ),
                center = focusPoint,
                radius = focusRadius * 2f
            ),
            blendMode = BlendMode.SoftLight
        )
        
        // Draw focus point indicator
        drawCircle(
            color = Color.White.copy(alpha = 0.2f * config.intensity),
            radius = focusRadius,
            center = focusPoint,
            style = Stroke(width = 2f)
        )
    }
}

// 2. MOTION BLUR - Speed trails
@Composable
private fun MotionBlurOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val blurDirection = Offset(
            cos(time * 2f) * 50f * config.intensity,
            sin(time * 2f) * 50f * config.intensity
        )
        
        // Draw motion streaks
        repeat(5) { i ->
            val alpha = (1f - i / 5f) * 0.3f * config.intensity
            drawRect(
                color = (config.color ?: Color.Cyan).copy(alpha = alpha),
                topLeft = Offset(i * blurDirection.x / 5f, i * blurDirection.y / 5f),
                size = Size(size.width, size.height),
                blendMode = BlendMode.Screen
            )
        }
    }
}

// 3. SSAO - Screen-space ambient occlusion
@Composable
private fun SSAOOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // Simulate AO in corners and edges
        val cornerSize = 200f * config.intensity
        
        // Top-left
        drawArc(
            color = Color.Black.copy(alpha = 0.3f * config.intensity),
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = Offset(0f, 0f),
            size = Size(cornerSize, cornerSize)
        )
        
        // Top-right
        drawArc(
            color = Color.Black.copy(alpha = 0.3f * config.intensity),
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = Offset(size.width - cornerSize, 0f),
            size = Size(cornerSize, cornerSize)
        )
        
        // Bottom-right
        drawArc(
            color = Color.Black.copy(alpha = 0.3f * config.intensity),
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = Offset(size.width - cornerSize, size.height - cornerSize),
            size = Size(cornerSize, cornerSize)
        )
        
        // Bottom-left
        drawArc(
            color = Color.Black.copy(alpha = 0.3f * config.intensity),
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = Offset(0f, size.height - cornerSize),
            size = Size(cornerSize, cornerSize)
        )
    }
}

// 4. COLOR GRADING - LUT-based color correction
@Composable
private fun ColorGradingOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // Apply color tint based on time
        val tint = when {
            time % 10f < 3f -> Color(0xFFFFD700) // Warm
            time % 10f < 6f -> Color(0xFF00BFFF) // Cool
            else -> Color(0xFF9370DB) // Neutral
        }
        
        drawRect(
            color = tint.copy(alpha = 0.2f * config.intensity),
            blendMode = BlendMode.Color
        )
    }
}

// 5. FILM GRAIN - Analog film texture
@Composable
private fun FilmGrainOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val grainCount = (1000 * config.intensity).toInt()
        
        repeat(grainCount) {
            val x = Random.nextFloat() * size.width
            val y = Random.nextFloat() * size.height
            val grainSize = Random.nextFloat() * 2f + 1f
            val brightness = Random.nextFloat() * 0.3f
            
            drawCircle(
                color = Color(brightness, brightness, brightness, 0.5f),
                radius = grainSize,
                center = Offset(x, y)
            )
        }
    }
}

// 6. VHS EFFECT - Retro tape distortion
@Composable
private fun VHSEffectOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // Horizontal scanlines
        val scanlineSpacing = 4f
        for (y in 0f until size.height step scanlineSpacing) {
            drawLine(
                color = Color.Black.copy(alpha = 0.1f * config.intensity),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }
        
        // Color bleeding
        val bleedOffset = sin(time * 5f) * 3f * config.intensity
        drawRect(
            color = Color.Red.copy(alpha = 0.1f),
            topLeft = Offset(bleedOffset, 0f),
            size = Size(size.width, size.height),
            blendMode = BlendMode.Screen
        )
        drawRect(
            color = Color.Blue.copy(alpha = 0.1f),
            topLeft = Offset(-bleedOffset, 0f),
            size = Size(size.width, size.height),
            blendMode = BlendMode.Screen
        )
        
        // Static noise
        if (Random.nextFloat() < 0.1f * config.intensity) {
            drawRect(
                color = Color.White.copy(alpha = 0.2f),
                size = Size(size.width, Random.nextFloat() * 20f + 5f),
                topLeft = Offset(0f, Random.nextFloat() * size.height)
            )
        }
    }
}

// 7. DATAMOSH - Digital artifact glitch
@Composable
private fun DataMoshOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val blockSize = 20f * config.intensity
        val glitchChance = 0.3f * config.intensity
        
        for (x in 0f until size.width step blockSize) {
            for (y in 0f until size.height step blockSize) {
                if (Random.nextFloat() < glitchChance) {
                    val offsetX = (Random.nextFloat() - 0.5f) * blockSize * 2f
                    val offsetY = (Random.nextFloat() - 0.5f) * blockSize * 2f
                    
                    drawRect(
                        color = Color(
                            Random.nextFloat(),
                            Random.nextFloat(),
                            Random.nextFloat(),
                            0.5f
                        ),
                        topLeft = Offset(x + offsetX, y + offsetY),
                        size = Size(blockSize, blockSize)
                    )
                }
            }
        }
    }
}

// 8. ASCII ART - Text-based rendering
@Composable
private fun AsciiArtOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val chars = "@%#*+=-:. "
        val fontSize = 12f * config.intensity
        val cols = (size.width / fontSize).toInt()
        val rows = (size.height / fontSize).toInt()
        
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val x = col * fontSize
                val y = row * fontSize
                val charIndex = ((sin(time + col * 0.1f) + 1) / 2 * (chars.length - 1)).toInt()
                val char = chars.getOrElse(charIndex) { ' ' }
                
                // Draw character (simplified - would need proper text rendering)
                drawCircle(
                    color = Color.Green.copy(alpha = 0.5f * config.intensity),
                    radius = fontSize / 4f,
                    center = Offset(x + fontSize / 2f, y + fontSize / 2f)
                )
            }
        }
    }
}

// 9. OIL PAINTING - Paint stroke simulation
@Composable
private fun OilPaintingOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val brushSize = 10f * config.intensity
        val strokeCount = 200
        
        repeat(strokeCount) {
            val x = Random.nextFloat() * size.width
            val y = Random.nextFloat() * size.height
            val angle = Random.nextFloat() * 360f
            val length = brushSize * Random.nextFloat() * 3f
            
            val colors = listOf(Color.Red, Color.Blue, Color.Yellow, Color.Green)
            val color = colors.random()
            
            drawLine(
                color = color.copy(alpha = 0.3f * config.intensity),
                start = Offset(x, y),
                end = Offset(
                    x + cos(Math.toRadians(angle.toDouble()).toFloat()) * length,
                    y + sin(Math.toRadians(angle.toDouble()).toFloat()) * length
                ),
                strokeWidth = brushSize * Random.nextFloat(),
                blendMode = BlendMode.Multiply
            )
        }
    }
}

// 10. WATERCOLOR - Wet paint bleed
@Composable
private fun WatercolorOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val blobCount = (50 * config.intensity).toInt()
        
        repeat(blobCount) {
            val x = Random.nextFloat() * size.width
            val y = Random.nextFloat() * size.height
            val radius = Random.nextFloat() * 50f + 20f
            
            val colors = listOf(
                Color(0xFFFF69B4),
                Color(0xFF87CEEB),
                Color(0xFF98FB98),
                Color(0xFFFFD700)
            )
            val color = colors.random()
            
            // Draw soft blob
            drawCircle(
                color = color.copy(alpha = 0.2f * config.intensity),
                radius = radius,
                center = Offset(x, y),
                blendMode = BlendMode.SoftLight
            )
            
            // Draw bleed effect
            drawCircle(
                color = color.copy(alpha = 0.1f * config.intensity),
                radius = radius * 1.5f,
                center = Offset(x + Random.nextFloat() * 10f, y + Random.nextFloat() * 10f),
                blendMode = BlendMode.SoftLight
            )
        }
    }
}

// 11. PIXEL SORT - Glitch art
@Composable
private fun PixelSortOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val sortLines = (20 * config.intensity).toInt()
        
        repeat(sortLines) {
            val x = Random.nextFloat() * size.width
            val width = Random.nextFloat() * 50f + 10f
            val startY = Random.nextFloat() * size.height
            val height = Random.nextFloat() * 100f + 50f
            
            val colors = listOf(Color.Red, Color.Green, Color.Blue)
            val color = colors.random()
            
            // Draw sorted pixel column
            drawRect(
                color = color.copy(alpha = 0.4f * config.intensity),
                topLeft = Offset(x, startY),
                size = Size(width, height)
            )
        }
    }
}

// 12. CHROMATIC SHIFT - RGB channel separation
@Composable
private fun ChromaticShiftOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val shiftAmount = 5f * config.intensity * sin(time * 3f)
        
        // Red channel shift
        drawRect(
            color = Color.Red.copy(alpha = 0.2f * config.intensity),
            topLeft = Offset(shiftAmount, 0f),
            size = Size(size.width, size.height),
            blendMode = BlendMode.Screen
        )
        
        // Blue channel shift
        drawRect(
            color = Color.Blue.copy(alpha = 0.2f * config.intensity),
            topLeft = Offset(-shiftAmount, 0f),
            size = Size(size.width, size.height),
            blendMode = BlendMode.Screen
        )
    }
}

// 13. LIQUIFY - Fluid distortion
@Composable
private fun LiquifyOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val waveCount = 5
        val waveAmplitude = 20f * config.intensity
        
        for (i in 0 until waveCount) {
            val y = size.height / waveCount * i
            val phase = time * 2f + i
            
            drawLine(
                color = (config.color ?: Color.Cyan).copy(alpha = 0.3f * config.intensity),
                start = Offset(0f, y + sin(phase) * waveAmplitude),
                end = Offset(size.width, y + sin(phase + size.width * 0.01f) * waveAmplitude),
                strokeWidth = 3f,
                blendMode = BlendMode.SoftLight
            )
        }
    }
}

// 14. KALEIDOSCOPE MIRROR - Symmetric patterns
@Composable
private fun KaleidoscopeMirrorOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val segments = 8
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        
        for (i in 0 until segments) {
            val angle = (360f / segments) * i + time * 10f
            val color = Color.HSV(angle % 360f, 0.7f, 0.5f).copy(alpha = 0.3f * config.intensity)
            
            drawLine(
                color = color,
                start = Offset(centerX, centerY),
                end = Offset(
                    centerX + cos(Math.toRadians(angle.toDouble()).toFloat()) * size.width,
                    centerY + sin(Math.toRadians(angle.toDouble()).toFloat()) * size.height
                ),
                strokeWidth = 2f,
                blendMode = BlendMode.Screen
            )
        }
    }
}

// 15. FRESNEL GLOW - Edge-based glow
@Composable
private fun FresnelGlowOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val glowColor = config.color ?: Color.Cyan
        
        // Draw edge glow
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    glowColor.copy(alpha = 0.5f * config.intensity),
                    Color.Transparent,
                    Color.Transparent,
                    glowColor.copy(alpha = 0.5f * config.intensity)
                )
            ),
            blendMode = BlendMode.Screen
        )
    }
}

// 16. SUBSURFACE SCATTERING - Translucent materials
@Composable
private fun SubsurfaceScatteringOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val scatterPoint = config.touchPoint ?: Offset(size.width / 2f, size.height / 2f)
        val scatterRadius = 150f * config.intensity
        
        // Draw subsurface glow
        drawCircle(
            color = (config.color ?: Color.Red).copy(alpha = 0.3f * config.intensity),
            radius = scatterRadius,
            center = scatterPoint,
            blendMode = BlendMode.Screen
        )
        
        // Draw inner scatter
        drawCircle(
            color = Color.White.copy(alpha = 0.2f * config.intensity),
            radius = scatterRadius * 0.5f,
            center = scatterPoint,
            blendMode = BlendMode.Screen
        )
    }
}

// 17. CAUSTICS - Light refraction patterns
@Composable
private fun CausticsOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val causticCount = (30 * config.intensity).toInt()
        
        repeat(causticCount) {
            val x = size.width * (0.5f + 0.5f * sin(time * 2f + it * 0.2f))
            val y = size.height * (0.5f + 0.5f * cos(time * 1.5f + it * 0.3f))
            
            // Draw caustic line
            drawLine(
                color = Color.Cyan.copy(alpha = 0.3f * config.intensity),
                start = Offset(x, y),
                end = Offset(
                    x + sin(time + it) * 50f,
                    y + cos(time + it) * 50f
                ),
                strokeWidth = 2f,
                blendMode = BlendMode.Screen
            )
        }
    }
}

// 18. GOD RAYS - Crepuscular rays
@Composable
private fun GodRaysOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val rayCount = 12
        val centerX = size.width * 0.7f
        val centerY = size.height * 0.3f
        
        for (i in 0 until rayCount) {
            val angle = (360f / rayCount) * i + time * 5f
            val rayLength = size.width * (0.5f + 0.5f * sin(time * 2f + i))
            
            drawLine(
                color = Color(1f, 0.9f, 0.7f).copy(alpha = 0.3f * config.intensity),
                start = Offset(centerX, centerY),
                end = Offset(
                    centerX + cos(Math.toRadians(angle.toDouble()).toFloat()) * rayLength,
                    centerY + sin(Math.toRadians(angle.toDouble()).toFloat()) * rayLength
                ),
                strokeWidth = 10f * config.intensity,
                blendMode = BlendMode.Screen
            )
        }
    }
}

// 19. LENS FLARE - Optical flare artifacts
@Composable
private fun LensFlareOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val lightPos = config.touchPoint ?: Offset(size.width * 0.8f, size.height * 0.2f)
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        
        // Draw flare circles
        val flareCount = 5
        for (i in 0 until flareCount) {
            val t = i / flareCount.toFloat()
            val x = centerX + (lightPos.x - centerX) * t
            val y = centerY + (lightPos.y - centerY) * t
            val size = 20f * (1f - t) * config.intensity
            val alpha = 0.3f * (1f - t) * config.intensity
            
            drawCircle(
                color = Color(1f, 0.9f, 0.8f).copy(alpha = alpha),
                radius = size,
                center = Offset(x, y),
                blendMode = BlendMode.Screen
            )
        }
        
        // Draw main light
        drawCircle(
            color = Color.White.copy(alpha = 0.8f * config.intensity),
            radius = 30f * config.intensity,
            center = lightPos,
            blendMode = BlendMode.Screen
        )
    }
}

// 20. TILT SHIFT - Miniature effect
@Composable
private fun TiltShiftOverlay(
    config: AdvancedShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val focusY = config.touchPoint?.y ?: size.height / 2f
        val focusHeight = 100f * config.intensity
        
        // Draw blur gradient above and below focus
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.3f * config.intensity),
                    Color.Transparent,
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.3f * config.intensity)
                ),
                startY = 0f,
                endY = size.height,
                colorStops = arrayOf(
                    0f to 0f,
                    (focusY - focusHeight) / size.height to 0f,
                    focusY / size.height to 1f,
                    (focusY + focusHeight) / size.height to 0f,
                    1f to 0f
                )
            ),
            blendMode = BlendMode.DstOut
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// UTILITY FUNCTIONS
// ─────────────────────────────────────────────────────────────────────────────────

private fun hsvToRgb(h: Float, s: Float, v: Float, a: Float = 1f): Color {
    val hNorm = (h % 360f) / 60f
    val i = hNorm.toInt()
    val f = hNorm - i
    val p = v * (1 - s)
    val q = v * (1 - s * f)
    val t = v * (1 - s * (1 - f))
    
    return when (i) {
        0 -> Color(v, t, p, a)
        1 -> Color(q, v, p, a)
        2 -> Color(p, v, t, a)
        3 -> Color(p, q, v, a)
        4 -> Color(t, p, v, a)
        else -> Color(v, p, q, a)
    }
}