package com.sugarmunch.app.effects.x2026

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Composable for rendering cloth simulation
 */
@Composable
fun ClothSimulationRenderer(
    modifier: Modifier = Modifier,
    simulation: ClothSimulation,
    fabricType: FabricType = FabricType.SILK,
    fabricColor: Color = Color(0xFFFF69B4),
    renderMode: ClothRenderMode = ClothRenderMode.FABRIC,
    onTouch: (Offset) -> Unit = {}
) {
    val alpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            alpha.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(1000))
        }
    }
    
    // Set fabric type
    LaunchedEffect(fabricType) {
        simulation.setFabricType(fabricType)
    }
    
    // Auto-step simulation
    LaunchedEffect(simulation) {
        while (true) {
            simulation.step(0.016f)
            kotlinx.coroutines.delay(16) // ~60fps
        }
    }
    
    androidx.compose.ui.graphics.Canvas(
        modifier = modifier
    ) {
        drawCloth(simulation, fabricColor, renderMode, alpha.value, onTouch)
    }
}

/**
 * Draw cloth simulation
 */
private fun DrawScope.drawCloth(
    simulation: ClothSimulation,
    fabricColor: Color,
    renderMode: ClothRenderMode,
    alpha: Float,
    onTouch: (Offset) -> Unit
) {
    when (renderMode) {
        ClothRenderMode.FABRIC -> drawFabricCloth(simulation, fabricColor, alpha)
        ClothRenderMode.WIREFRAME -> drawWireframeCloth(simulation, fabricColor, alpha)
        ClothRenderMode.PARTICLES -> drawParticleCloth(simulation, fabricColor, alpha)
        ClothRenderMode.RIBBON -> drawRibbonCloth(simulation, fabricColor, alpha)
    }
}

/**
 * Fabric rendering with smooth surface
 */
private fun DrawScope.drawFabricCloth(
    simulation: ClothSimulation,
    fabricColor: Color,
    alpha: Float
) {
    val points = simulation.getAllPoints()
    val width = simulation.width
    val height = simulation.height
    
    // Draw fabric as connected quads
    for (y in 0 until height - 1) {
        for (x in 0 until width - 1) {
            val topLeft = points[simulation.getIndex(x, y)]
            val topRight = points[simulation.getIndex(x + 1, y)]
            val bottomLeft = points[simulation.getIndex(x, y + 1)]
            val bottomRight = points[simulation.getIndex(x + 1, y + 1)]
            
            // Calculate normal for lighting
            val dx1 = topRight.x - topLeft.x
            val dy1 = topRight.y - topLeft.y
            val dx2 = bottomLeft.x - topLeft.x
            val dy2 = bottomLeft.y - topLeft.y
            
            val normalX = -dy1 - dy2
            val normalY = dx1 + dx2
            val normalLength = sqrt(normalX * normalX + normalY * normalY)
            
            val lighting = if (normalLength > 0) {
                0.5f + 0.5f * (normalX / normalLength)
            } else {
                1f
            }
            
            // Create quad path
            val path = Path().apply {
                moveTo(topLeft.x, topLeft.y)
                lineTo(topRight.x, topRight.y)
                lineTo(bottomRight.x, bottomRight.y)
                lineTo(bottomLeft.x, bottomLeft.y)
                close()
            }
            
            // Apply lighting to color
            val litColor = fabricColor.copy(
                red = (fabricColor.red * lighting).coerceIn(0f, 1f),
                green = (fabricColor.green * lighting).coerceIn(0f, 1f),
                blue = (fabricColor.blue * lighting).coerceIn(0f, 1f),
                alpha = alpha * 0.9f
            )
            
            drawPath(path, litColor)
        }
    }
    
    // Draw fabric highlights
    drawFabricHighlights(points, fabricColor, alpha)
}

/**
 * Draw fabric surface highlights
 */
private fun DrawScope.drawFabricHighlights(
    points: List<PointMass>,
    fabricColor: Color,
    alpha: Float
) {
    // Add specular highlights
    points.forEachIndexed { index, point ->
        if (index % 3 == 0 && !point.pinned) {
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.3f),
                radius = 3.dp.toPx(),
                center = Offset(point.x, point.y)
            )
        }
    }
}

/**
 * Wireframe rendering showing constraint structure
 */
private fun DrawScope.drawWireframeCloth(
    simulation: ClothSimulation,
    fabricColor: Color,
    alpha: Float
) {
    val points = simulation.getAllPoints()
    val constraints = simulation.getConstraints()
    
    // Draw constraints
    constraints.forEach { constraint ->
        val point1 = points[constraint.index1]
        val point2 = points[constraint.index2]
        
        // Color based on stretch
        val dx = point2.x - point1.x
        val dy = point2.y - point1.y
        val dist = sqrt(dx * dx + dy * dy)
        val stretch = (dist - constraint.restLength) / constraint.restLength
        
        val constraintColor = when {
            stretch > 0.1f -> Color.Red.copy(alpha = alpha)
            stretch < -0.1f -> Color.Blue.copy(alpha = alpha)
            else -> fabricColor.copy(alpha = alpha * 0.5f)
        }
        
        drawLine(
            color = constraintColor,
            start = Offset(point1.x, point1.y),
            end = Offset(point2.x, point2.y),
            strokeWidth = 1.dp.toPx()
        )
    }
    
    // Draw points
    points.forEach { point ->
        drawCircle(
            color = if (point.pinned) Color.Yellow else fabricColor,
            radius = 3.dp.toPx(),
            center = Offset(point.x, point.y)
        )
    }
}

/**
 * Particle-based cloth rendering
 */
private fun DrawScope.drawParticleCloth(
    simulation: ClothSimulation,
    fabricColor: Color,
    alpha: Float
) {
    val points = simulation.getAllPoints()
    
    points.forEach { point ->
        // Calculate velocity for color variation
        val vx = point.x - point.prevX
        val vy = point.y - point.prevY
        val speed = sqrt(vx * vx + vy * vy)
        
        val particleColor = fabricColor.copy(
            alpha = (alpha * (0.5f + speed * 10f)).coerceIn(0f, 1f)
        )
        
        drawCircle(
            color = particleColor,
            radius = (4.dp.toPx() + speed * 20f).coerceIn(2.dp.toPx(), 8.dp.toPx()),
            center = Offset(point.x, point.y)
        )
    }
}

/**
 * Ribbon-style rendering with flowing lines
 */
private fun DrawScope.drawRibbonCloth(
    simulation: ClothSimulation,
    fabricColor: Color,
    alpha: Float
) {
    val points = simulation.getAllPoints()
    val width = simulation.width
    val height = simulation.height
    
    // Draw horizontal ribbons
    for (y in 0 until height) {
        val path = Path()
        
        for (x in 0 until width) {
            val point = points[simulation.getIndex(x, y)]
            
            if (x == 0) {
                path.moveTo(point.x, point.y)
            } else {
                // Smooth curve through points
                val prevPoint = points[simulation.getIndex(x - 1, y)]
                val midX = (prevPoint.x + point.x) / 2f
                val midY = (prevPoint.y + point.y) / 2f
                path.lineTo(midX, midY)
            }
        }
        
        // Vary ribbon color by row
        val rowColor = fabricColor.copy(
            alpha = alpha * (0.3f + 0.7f * (y.toFloat() / height)),
            red = (fabricColor.red * (0.8f + 0.2f * y / height.toFloat())).coerceIn(0f, 1f),
            green = (fabricColor.green * (0.8f + 0.2f * y / height.toFloat())).coerceIn(0f, 1f),
            blue = (fabricColor.blue * (0.8f + 0.2f * y / height.toFloat())).coerceIn(0f, 1f)
        )
        
        drawPath(
            path = path,
            color = rowColor,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

/**
 * Cloth rendering modes
 */
enum class ClothRenderMode {
    FABRIC,     // Smooth fabric surface
    WIREFRAME,  // Show constraint structure
    PARTICLES,  // Point-based rendering
    RIBBON      // Flowing ribbon lines
}

/**
 * Get composable cloth effect overlay
 */
@Composable
fun ClothEffectOverlay(
    modifier: Modifier = Modifier,
    fabricType: FabricType = FabricType.SILK,
    fabricColor: Color = Color(0xFFFF69B4),
    renderMode: ClothRenderMode = ClothRenderMode.FABRIC,
    intensity: Float = 1.0f
) {
    val simulation = remember {
        ClothSimulation(
            width = 40,
            height = 30,
            spacing = 8f * intensity
        )
    }
    
    ClothSimulationRenderer(
        modifier = modifier,
        simulation = simulation,
        fabricType = fabricType,
        fabricColor = fabricColor,
        renderMode = renderMode
    ) { offset ->
        simulation.setTouchPoint(offset.x, offset.y)
    }
}
