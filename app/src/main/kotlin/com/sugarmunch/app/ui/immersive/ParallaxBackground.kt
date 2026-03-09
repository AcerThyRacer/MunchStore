package com.sugarmunch.app.ui.immersive

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlin.math.cos
import kotlin.math.sin

/**
 * EXTREME Parallax Background System for SugarMunch
 * Multi-layer parallax with gyroscope support and depth effects
 */

/**
 * Parallax layer definition
 */
data class ParallaxLayer(
    val id: String,
    val speed: Float = 0.1f,
    val color: Color,
    val shape: LayerShape = LayerShape.STARS,
    val opacity: Float = 1f,
    val scale: Float = 1f,
    val elements: List<ParallaxElement> = emptyList()
)

data class ParallaxElement(
    val x: Float,
    val y: Float,
    val size: Float,
    val shape: ElementShape = ElementShape.CIRCLE
)

enum class LayerShape {
    STARS,
    BUBBLES,
    PARTICLES,
    WAVES,
    CLOUDS,
    GEOMETRIC
}

enum class ElementShape {
    CIRCLE,
    SQUARE,
    TRIANGLE,
    STAR,
    DIAMOND
}

/**
 * Main parallax background composable
 */
@Composable
fun ParallaxBackground(
    modifier: Modifier = Modifier,
    layers: List<ParallaxLayer> = createDefaultParallaxLayers(),
    enableGyroscope: Boolean = true,
    depthIntensity: Float = 1.0f
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()
    
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    
    // Simulate gyroscope movement
    LaunchedEffect(Unit) {
        while (true) {
            offsetX.animateTo(
                (Math.random().toFloat() - 0.5f) * 50f * depthIntensity,
                animationSpec = tween(2000, easing = LinearEasing)
            )
            offsetY.animateTo(
                (Math.random().toFloat() - 0.5f) * 50f * depthIntensity,
                animationSpec = tween(2000, easing = LinearEasing)
            )
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SugarDimens.Brand.deepPurple)
    ) {
        layers.forEach { layer ->
            ParallaxLayerRenderer(
                layer = layer,
                offsetX = offsetX.value * layer.speed,
                offsetY = offsetY.value * layer.speed,
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )
        }
    }
}

/**
 * Render a single parallax layer
 */
@Composable
fun ParallaxLayerRenderer(
    layer: ParallaxLayer,
    offsetX: Float,
    offsetY: Float,
    screenWidth: Float,
    screenHeight: Float
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = layer.opacity
                scaleX = layer.scale
                scaleY = layer.scale
                translationX = offsetX
                translationY = offsetY
            }
    ) {
        when (layer.shape) {
            LayerShape.STARS -> drawStars(layer.elements, layer.color)
            LayerShape.BUBBLES -> drawBubbles(layer.elements, layer.color)
            LayerShape.PARTICLES -> drawParticles(layer.elements, layer.color)
            LayerShape.WAVES -> drawWaves(offsetX, layer.color)
            LayerShape.CLOUDS -> drawClouds(layer.elements, layer.color)
            LayerShape.GEOMETRIC -> drawGeometricShapes(layer.elements, layer.color)
        }
    }
}

/**
 * Draw stars layer
 */
private fun DrawScope.drawStars(
    elements: List<ParallaxElement>,
    color: Color
) {
    elements.forEach { element ->
        drawCircle(
            color = color,
            radius = element.size,
            center = Offset(element.x, element.y)
        )
        
        // Star sparkle
        if (element.size > 2f) {
            drawStarSparkle(
                center = Offset(element.x, element.y),
                color = color,
                size = element.size * 2
            )
        }
    }
}

/**
 * Draw bubbles layer
 */
private fun DrawScope.drawBubbles(
    elements: List<ParallaxElement>,
    color: Color
) {
    elements.forEach { element ->
        drawCircle(
            color = color.copy(alpha = 0.5f),
            radius = element.size,
            center = Offset(element.x, element.y)
        )
        
        // Bubble highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = element.size * 0.3f,
            center = Offset(element.x - element.size * 0.3f, element.y - element.size * 0.3f)
        )
    }
}

/**
 * Draw particles layer
 */
private fun DrawScope.drawParticles(
    elements: List<ParallaxElement>,
    color: Color
) {
    elements.forEach { element ->
        when (element.shape) {
            ElementShape.CIRCLE -> drawCircle(color, element.size, Offset(element.x, element.y))
            ElementShape.SQUARE -> drawRect(
                color = color,
                size = androidx.compose.ui.geometry.Size(element.size * 2, element.size * 2),
                topLeft = Offset(element.x - element.size, element.y - element.size)
            )
            ElementShape.TRIANGLE -> drawTriangle(
                color = color,
                center = Offset(element.x, element.y),
                size = element.size
            )
            ElementShape.STAR -> drawStarSparkle(
                center = Offset(element.x, element.y),
                color = color,
                size = element.size * 2
            )
            ElementShape.DIAMOND -> drawDiamond(
                color = color,
                center = Offset(element.x, element.y),
                size = element.size
            )
        }
    }
}

/**
 * Draw waves layer
 */
private fun DrawScope.drawWaves(
    offsetX: Float,
    color: Color
) {
    val waveCount = 5
    repeat(waveCount) { i ->
        val path = androidx.compose.ui.graphics.Path()
        val waveHeight = 30f * (i + 1)
        val yOffset = size.height / waveCount * i
        
        path.moveTo(0f, yOffset)
        
        for (x in 0..size.width.toInt() step 20) {
            val y = yOffset + kotlin.math.sin((x + offsetX) * 0.01f + i) * waveHeight
            path.lineTo(x.toFloat(), y)
        }
        
        path.lineTo(size.width, size.height)
        path.lineTo(0f, size.height)
        path.close()
        
        drawPath(path, color.copy(alpha = 0.3f - i * 0.05f))
    }
}

/**
 * Draw clouds layer
 */
private fun DrawScope.drawClouds(
    elements: List<ParallaxElement>,
    color: Color
) {
    elements.forEach { element ->
        drawCloud(
            center = Offset(element.x, element.y),
            color = color,
            size = element.size
        )
    }
}

/**
 * Draw geometric shapes layer
 */
private fun DrawScope.drawGeometricShapes(
    elements: List<ParallaxElement>,
    color: Color
) {
    elements.forEach { element ->
        when (element.shape) {
            ElementShape.CIRCLE -> drawCircle(color, element.size, Offset(element.x, element.y))
            ElementShape.SQUARE -> drawRect(
                color = color,
                size = androidx.compose.ui.geometry.Size(element.size * 2, element.size * 2),
                topLeft = Offset(element.x - element.size, element.y - element.size),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
            ElementShape.TRIANGLE -> drawTriangle(color, Offset(element.x, element.y), element.size)
            ElementShape.STAR -> drawStarSparkle(Offset(element.x, element.y), color, element.size * 2)
            ElementShape.DIAMOND -> drawDiamond(color, Offset(element.x, element.y), element.size)
        }
    }
}

/**
 * Helper functions for drawing shapes
 */
private fun DrawScope.drawStarSparkle(
    center: Offset,
    color: Color,
    size: Float
) {
    val path = androidx.compose.ui.graphics.Path()
    val points = 8
    val innerRadius = size * 0.5f
    val outerRadius = size
    
    for (i in 0 until points * 2) {
        val angle = (i * 180f / points - 90) * Math.PI / 180
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + kotlin.math.cos(angle) * radius
        val y = center.y + kotlin.math.sin(angle) * radius
        
        if (i == 0) path.moveTo(x, y)
        else path.lineTo(x, y)
    }
    
    path.close()
    drawPath(path, color)
}

private fun DrawScope.drawTriangle(
    color: Color,
    center: Offset,
    size: Float
) {
    val path = androidx.compose.ui.graphics.Path()
    path.moveTo(center.x, center.y - size)
    path.lineTo(center.x + size, center.y + size)
    path.lineTo(center.x - size, center.y + size)
    path.close()
    drawPath(path, color)
}

private fun DrawScope.drawDiamond(
    color: Color,
    center: Offset,
    size: Float
) {
    val path = androidx.compose.ui.graphics.Path()
    path.moveTo(center.x, center.y - size)
    path.lineTo(center.x + size, center.y)
    path.lineTo(center.x, center.y + size)
    path.lineTo(center.x - size, center.y)
    path.close()
    drawPath(path, color)
}

private fun DrawScope.drawCloud(
    center: Offset,
    color: Color,
    size: Float
) {
    drawCircle(color, size * 0.8f, Offset(center.x - size * 0.5f, center.y))
    drawCircle(color, size, center)
    drawCircle(color, size * 0.7f, Offset(center.x + size * 0.5f, center.y))
}

/**
 * Create default parallax layers
 */
fun createDefaultParallaxLayers(): List<ParallaxLayer> {
    return listOf(
        ParallaxLayer(
            id = "background",
            speed = 0.05f,
            color = SugarDimens.Brand.deepPurple.copy(alpha = 0.3f),
            shape = LayerShape.STARS,
            opacity = 0.5f,
            elements = generateRandomElements(50, ElementShape.STAR)
        ),
        ParallaxLayer(
            id = "midground",
            speed = 0.2f,
            color = SugarDimens.Brand.mint.copy(alpha = 0.5f),
            shape = LayerShape.BUBBLES,
            opacity = 0.7f,
            elements = generateRandomElements(30, ElementShape.CIRCLE)
        ),
        ParallaxLayer(
            id = "foreground",
            speed = 0.5f,
            color = SugarDimens.Brand.hotPink.copy(alpha = 0.7f),
            shape = LayerShape.PARTICLES,
            opacity = 1f,
            elements = generateRandomElements(20, ElementShape.DIAMOND)
        )
    )
}

/**
 * Generate random parallax elements
 */
fun generateRandomElements(
    count: Int,
    shape: ElementShape = ElementShape.CIRCLE,
    screenWidth: Float = 1000f,
    screenHeight: Float = 1000f
): List<ParallaxElement> {
    return List(count) {
        ParallaxElement(
            x = (Math.random().toFloat() * screenWidth),
            y = (Math.random().toFloat() * screenHeight),
            size = (Math.random().toFloat() * 10 + 2),
            shape = shape
        )
    }
}
