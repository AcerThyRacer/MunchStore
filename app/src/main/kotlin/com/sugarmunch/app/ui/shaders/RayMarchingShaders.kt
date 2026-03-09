package com.sugarmunch.app.ui.shaders

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
 * Raymarching 3D shader engine with Signed Distance Fields (SDF)
 * Renders real-time 3D candy landscapes, floating lollipops, and abstract shapes
 */
@Composable
fun RayMarchingRenderer(
    modifier: Modifier = Modifier,
    preset: RayMarchingPreset = RayMarchingPresets.candyLandscape(),
    onTouch: (Offset) -> Unit = {}
) {
    val time = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        while (true) {
            time.animateTo(time.value + 0.016f, animationSpec = androidx.compose.animation.core.tween(16))
        }
    }
    
    androidx.compose.ui.graphics.Canvas(modifier = modifier) {
        drawRayMarchingScene(time.value, preset)
    }
}

/**
 * Main raymarching draw function
 */
private fun DrawScope.drawRayMarchingScene(
    time: Float,
    preset: RayMarchingPreset
) {
    val width = size.width.toInt()
    val height = size.height.toInt()
    
    // Render at lower resolution for performance
    val scale = 4
    val renderWidth = width / scale
    val renderHeight = height / scale
    
    val imageBitmap = androidx.compose.ui.graphics.ImageBitmap(renderWidth, renderHeight)
    val pixels = IntArray(renderWidth * renderHeight)
    
    // Raymarch each pixel
    for (y in 0 until renderHeight) {
        for (x in 0 until renderWidth) {
            val uv = Offset(
                (2f * x / renderWidth - 1f) * renderWidth.toFloat() / renderHeight.toFloat(),
                2f * y / renderHeight - 1f
            )
            
            val color = raymarch(uv, time, preset)
            val pixelIndex = y * renderWidth + x
            pixels[pixelIndex] = Color(
                red = color.x.coerceIn(0f, 1f),
                green = color.y.coerceIn(0f, 1f),
                blue = color.z.coerceIn(0f, 1f),
                alpha = 1f
            ).value.toInt()
        }
    }
    
    imageBitmap.writePixels(pixels)
    drawImage(imageBitmap)
}

/**
 * Raymarching main loop
 */
private fun raymarch(
    uv: Offset,
    time: Float,
    preset: RayMarchingPreset
): Offset {
    // Camera setup
    val cameraPos = Offset3D(0f, 1.5f, -3f)
    val cameraTarget = Offset3D(0f, 0f, 0f)
    
    // Ray direction
    val rayDir = calculateRayDirection(uv, cameraPos, cameraTarget)
    
    // March the ray
    var totalDistance = 0f
    var color = Offset(0f, 0f, 0f)
    var distance = 0f
    
    for (i in 0 until 64) {
        val position = Offset3D(
            cameraPos.x + rayDir.x * totalDistance,
            cameraPos.y + rayDir.y * totalDistance,
            cameraPos.z + rayDir.z * totalDistance
        )
        
        distance = sceneSDF(position, time, preset)
        
        if (distance < 0.001f) {
            // Hit surface - calculate lighting
            color = calculateLighting(position, rayDir, time, preset)
            break
        }
        
        if (totalDistance > 100f) {
            // Background color
            color = Offset(0.1f, 0.1f, 0.2f)
            break
        }
        
        totalDistance += distance
    }
    
    return color
}

/**
 * Calculate ray direction from camera
 */
private fun calculateRayDirection(
    uv: Offset,
    cameraPos: Offset3D,
    cameraTarget: Offset3D
): Offset3D {
    val forward = normalize(subtract(cameraTarget, cameraPos))
    val right = normalize(cross(forward, Offset3D(0f, 1f, 0f)))
    val up = cross(right, forward)
    
    return normalize(
        Offset3D(
            forward.x + uv.x * right.x + uv.y * up.x,
            forward.y + uv.x * right.y + uv.y * up.y,
            forward.z + uv.x * right.z + uv.y * up.z
        )
    )
}

/**
 * Scene SDF - combines all 3D objects
 */
private fun sceneSDF(
    position: Offset3D,
    time: Float,
    preset: RayMarchingPreset
): Float {
    var distance = Float.MAX_VALUE
    
    // Ground plane
    val ground = position.y + 1f
    distance = minOf(distance, ground)
    
    // Add preset-specific objects
    when (preset) {
        is RayMarchingPreset.CandyLandscape -> {
            // Candy cane pillars
            for (i in -2..2) {
                val pillarPos = Offset3D(i * 2f, 0f, sin(time + i) * 2f)
                val pillarDist = sdCylinder(position, pillarPos, 0.3f, 3f)
                distance = minOf(distance, pillarDist)
            }
            
            // Floating lollipops
            for (i in -1..1) {
                val lollipopPos = Offset3D(
                    i * 1.5f,
                    2f + sin(time * 2f + i) * 0.5f,
                    cos(time + i) * 2f
                )
                val lollipopDist = sdSphere(position, lollipopPos, 0.4f)
                distance = minOf(distance, lollipopDist)
            }
        }
        is RayMarchingPreset.AbstractShapes -> {
            // Rotating torus
            val torusPos = Offset3D(0f, 1f, 0f)
            val torusDist = sdTorus(position, torusPos, 0.8f, 0.3f, time)
            distance = minOf(distance, torusDist)
            
            // Pulsing sphere
            val sphereRadius = 0.5f + sin(time * 3f) * 0.2f
            val sphereDist = sdSphere(position, Offset3D(0f, 0.5f, 0f), sphereRadius)
            distance = minOf(distance, sphereDist)
        }
        is RayMarchingPreset.FractalMountains -> {
            // Fractal terrain using noise
            val terrainHeight = fractalNoise(position.x, position.z, time) * 2f - 1f
            val terrain = position.y - terrainHeight
            distance = minOf(distance, terrain)
        }
    }
    
    return distance
}

/**
 * Calculate lighting on surface
 */
private fun calculateLighting(
    position: Offset3D,
    rayDir: Offset3D,
    time: Float,
    preset: RayMarchingPreset
): Offset {
    // Calculate normal
    val epsilon = 0.001f
    val normal = normalize(Offset3D(
        sceneSDF(Offset3D(position.x + epsilon, position.y, position.z), time, preset) -
        sceneSDF(Offset3D(position.x - epsilon, position.y, position.z), time, preset),
        sceneSDF(Offset3D(position.x, position.y + epsilon, position.z), time, preset) -
        sceneSDF(Offset3D(position.x, position.y - epsilon, position.z), time, preset),
        sceneSDF(Offset3D(position.x, position.y, position.z + epsilon), time, preset) -
        sceneSDF(Offset3D(position.x, position.y, position.z - epsilon), time, preset)
    ))
    
    // Light direction
    val lightDir = normalize(Offset3D(1f, 2f, -1f))
    
    // Diffuse lighting
    val diffuse = maxOf(0f, dot(normal, lightDir))
    
    // Specular lighting
    val reflectDir = reflect(-lightDir, normal)
    val specular = pow(maxOf(0f, -dot(reflectDir, rayDir)), 32f)
    
    // Ambient
    val ambient = 0.3f
    
    // Get base color from preset
    val baseColor = preset.getBaseColor(position, time)
    
    // Combine lighting
    return Offset(
        baseColor.x * (ambient + diffuse * 0.7f + specular * 0.3f),
        baseColor.y * (ambient + diffuse * 0.7f + specular * 0.3f),
        baseColor.z * (ambient + diffuse * 0.7f + specular * 0.3f)
    )
}

// SDF Primitives
private fun sdSphere(position: Offset3D, center: Offset3D, radius: Float): Float {
    return length(subtract(position, center)) - radius
}

private fun sdCylinder(position: Offset3D, center: Offset3D, radius: Float, height: Float): Float {
    val d = Offset3D(
        sqrt(position.x * position.x + position.z * position.z) - radius,
        abs(position.y - center.y) - height,
        0f
    )
    return maxOf(d.x, d.y, 0f) + minOf(maxOf(d.x, d.y), 0f)
}

private fun sdTorus(position: Offset3D, center: Offset3D, majorRadius: Float, minorRadius: Float, rotation: Float): Float {
    val rotatedX = position.x * cos(rotation) - position.z * sin(rotation)
    val rotatedZ = position.x * sin(rotation) + position.z * cos(rotation)
    
    val q = Offset3D(
        length(Offset3D(rotatedX - center.x, 0f, rotatedZ - center.z)) - majorRadius,
        position.y - center.y,
        0f
    )
    return length(q) - minorRadius
}

// Noise functions for terrain
private fun fractalNoise(x: Float, z: Float, time: Float): Float {
    var value = 0f
    var amplitude = 0.5f
    var frequency = 1f
    
    for (i in 0 until 4) {
        value += amplitude * sin(x * frequency + time) * sin(z * frequency + time)
        amplitude *= 0.5f
        frequency *= 2f
    }
    
    return value
}

// Vector math helpers
private fun length(v: Offset3D): Float = sqrt(v.x * v.x + v.y * v.y + v.z * v.z)
private fun normalize(v: Offset3D): Offset3D {
    val len = length(v)
    return if (len > 0) Offset3D(v.x / len, v.y / len, v.z / len) else v
}
private fun subtract(a: Offset3D, b: Offset3D): Offset3D = Offset3D(a.x - b.x, a.y - b.y, a.z - b.z)
private fun dot(a: Offset3D, b: Offset3D): Float = a.x * b.x + a.y * b.y + a.z * b.z
private fun cross(a: Offset3D, b: Offset3D): Offset3D = Offset3D(
    a.y * b.z - a.z * b.y,
    a.z * b.x - a.x * b.z,
    a.x * b.y - a.y * b.x
)
private fun reflect(i: Offset3D, n: Offset3D): Offset3D = subtract(i, Offset3D(n.x * 2f * dot(i, n), n.y * 2f * dot(i, n), n.z * 2f * dot(i, n)))
private fun pow(x: Float, n: Float): Float = x.pow(n)
private fun abs(x: Float): Float = kotlin.math.abs(x)
private fun sin(x: Float): Float = kotlin.math.sin(x.toDouble()).toFloat()
private fun cos(x: Float): Float = kotlin.math.cos(x.toDouble()).toFloat()

data class Offset3D(val x: Float, val y: Float, val z: Float)

/**
 * Raymarching presets
 */
sealed class RayMarchingPreset {
    abstract fun getBaseColor(position: Offset3D, time: Float): Offset
    
    data class CandyLandscape(
        val candyColors: List<Offset> = listOf(
            Offset(1f, 0.4f, 0.7f), // Pink
            Offset(0f, 1f, 0.6f),   // Mint
            Offset(1f, 0.8f, 0f)    // Yellow
        )
    ) : RayMarchingPreset() {
        override fun getBaseColor(position: Offset3D, time: Float): Offset {
            val index = (abs(position.x) + abs(position.y) + abs(position.z)).toInt() % candyColors.size
            return candyColors[index]
        }
    }
    
    data class AbstractShapes(
        val baseColor: Offset = Offset(0.2f, 0.8f, 1f)
    ) : RayMarchingPreset() {
        override fun getBaseColor(position: Offset3D, time: Float): Offset {
            return Offset(
                baseColor.x + sin(time) * 0.3f,
                baseColor.y + cos(time) * 0.3f,
                baseColor.z
            )
        }
    }
    
    data class FractalMountains(
        val mountainColor: Offset = Offset(0.6f, 0.4f, 0.8f)
    ) : RayMarchingPreset() {
        override fun getBaseColor(position: Offset3D, time: Float): Offset {
            val height = position.y
            return Offset(
                mountainColor.x * (1f - height * 0.2f),
                mountainColor.y * (1f - height * 0.2f),
                mountainColor.z * (1f - height * 0.2f)
            )
        }
    }
}

/**
 * Preset factory
 */
object RayMarchingPresets {
    fun candyLandscape() = RayMarchingPreset.CandyLandscape()
    fun abstractShapes() = RayMarchingPreset.AbstractShapes()
    fun fractalMountains() = RayMarchingPreset.FractalMountains()
}
