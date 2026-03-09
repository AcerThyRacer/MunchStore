package com.sugarmunch.app.effects.x2026

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Composable for rendering destruction effects
 */
@Composable
fun DestructionRenderer(
    modifier: Modifier = Modifier,
    simulation: DestructionPhysics,
    onTouch: (Offset) -> Unit = {}
) {
    val alpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            alpha.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(1000))
        }
    }
    
    // Auto-step simulation
    LaunchedEffect(simulation) {
        while (true) {
            simulation.step(0.016f)
            kotlinx.coroutines.delay(16)
        }
    }
    
    androidx.compose.ui.graphics.Canvas(modifier = modifier) {
        drawDestruction(simulation, alpha.value)
    }
}

/**
 * Draw destruction effects
 */
private fun DrawScope.drawDestruction(
    simulation: DestructionPhysics,
    alpha: Float
) {
    // Draw debris first (background)
    drawDebris(simulation.getDebris(), alpha)
    
    // Draw shards on top
    drawShards(simulation.getShards(), alpha)
}

/**
 * Draw shards with rotation and physics
 */
private fun DrawScope.drawShards(
    shards: List<Shard>,
    alpha: Float
) {
    shards.forEach { shard ->
        if (shard.vertices.size < 3) return@forEach
        
        val path = Path().apply {
            // Transform vertices by rotation and position
            val transformedVertices = shard.vertices.map { vertex ->
                val angle = Math.toRadians(shard.rotation.toDouble()).toFloat()
                val cos = cos(angle)
                val sin = sin(angle)
                
                val dx = vertex.x - shard.x
                val dy = vertex.y - shard.y
                
                val rotatedX = dx * cos - dy * sin + shard.x
                val rotatedY = dx * sin + dy * cos + shard.y
                
                Offset(rotatedX, rotatedY)
            }
            
            // Create polygon path
            moveTo(transformedVertices[0].x, transformedVertices[0].y)
            for (i in 1 until transformedVertices.size) {
                lineTo(transformedVertices[i].x, transformedVertices[i].y)
            }
            close()
        }
        
        // Draw shard with gradient
        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(
                    shard.color.copy(alpha = alpha * shard.alpha),
                    shard.color.copy(alpha = alpha * shard.alpha * 0.7f)
                )
            )
        )
        
        // Draw outline
        drawPath(
            path = path,
            color = Color.White.copy(alpha = alpha * shard.alpha * 0.3f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
        )
        
        // Add shine effect for glass/ice
        if (shard.materialType == MaterialType.GLASS || shard.materialType == MaterialType.ICE) {
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = alpha * shard.alpha * 0.4f),
                        Color.Transparent
                    )
                ),
                blendMode = BlendMode.Screen
            )
        }
    }
}

/**
 * Draw debris particles
 */
private fun DrawScope.drawDebris(
    debris: List<Debris>,
    alpha: Float
) {
    debris.forEach { d ->
        drawCircle(
            color = d.color.copy(alpha = alpha * d.alpha),
            radius = d.size.dp.toPx(),
            center = Offset(d.x, d.y)
        )
    }
}

/**
 * Destruction effect overlay
 */
@Composable
fun DestructionEffectOverlay(
    modifier: Modifier = Modifier,
    materialType: MaterialType = MaterialType.GLASS,
    intensity: Float = 1.0f
) {
    val simulation = remember {
        DestructionPhysics(
            gravity = 9.8f,
            friction = 0.8f,
            restitution = 0.6f
        ).apply {
            maxShards = (100 * intensity).toInt()
            maxDebris = (200 * intensity).toInt()
        }
    }
    
    DestructionRenderer(
        modifier = modifier,
        simulation = simulation
    ) { offset ->
        simulation.shatter(
            centerX = offset.x,
            centerY = offset.y,
            width = 200f * intensity,
            height = 200f * intensity,
            impactX = offset.x,
            impactY = offset.y,
            force = 20f * intensity,
            materialType = materialType
        )
    }
}