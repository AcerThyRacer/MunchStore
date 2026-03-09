package com.sugarmunch.app.ui.ar.preview

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.sugarmunch.app.theme.model.BaseColors
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * PHASE 15: 2026 CUTTING-EDGE FEATURES
 * AR Preview, 3D Effects, Foldable Support
 */

/**
 * AR Theme Preview
 * Preview themes in augmented reality
 */
@Composable
fun ARThemePreview(
    colors: BaseColors,
    modifier: Modifier = Modifier,
    onThemeApplied: () -> Unit = {}
) {
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        // AR Surface View
        AndroidView(
            factory = { ctx ->
                GLSurfaceView(ctx).apply {
                    setRenderer(ThemeARRenderer(colors))
                    renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay instructions
        Text(
            text = "🍭 Point camera to preview theme in AR",
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            color = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )

        // Apply button
        androidx.compose.material3.Button(
            onClick = onThemeApplied,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text("Apply Theme")
        }
    }
}

/**
 * AR Renderer for theme preview
 */
class ThemeARRenderer(
    private val colors: BaseColors
) : GLSurfaceView.Renderer {

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Initialize AR scene
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // Setup viewport
    }

    override fun onDrawFrame(gl: GL10?) {
        // Render 3D theme preview
        // In production, use ARCore or similar
    }
}

/**
 * 3D Card Effect
 * Cards with 3D perspective and parallax
 */
@Composable
fun Card3DEffect(
    colors: BaseColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationX = rotationX
                rotationY = rotationY
                cameraDistance = 12f
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    rotationY = (dragAmount.x / 10).coerceIn(-30f, 30f)
                    rotationX = (-dragAmount.y / 10).coerceIn(-30f, 30f)
                }
            }
    ) {
        content()
    }
}

/**
 * 3D Icon Animation
 * Icons that rotate in 3D space
 */
@Composable
fun Icon3DAnimation(
    icon: String,
    colors: BaseColors,
    modifier: Modifier = Modifier
) {
    val rotationY = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        rotationY.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Text(
        text = icon,
        style = androidx.compose.ui.text.TextStyle(fontSize = 40.sp),
        modifier = Modifier
            .size(80.dp)
            .graphicsLayer {
                rotationY = rotationY.value
                cameraDistance = 12f
            }
    )
}

/**
 * Foldable Support - Dual Screen Layout
 */
@Composable
fun FoldableDualScreenLayout(
    isFolded: Boolean,
    hingePosition: Float = 0.5f,
    primaryContent: @Composable () -> Unit,
    secondaryContent: @Composable () -> Unit
) {
    if (isFolded) {
        // Dual screen mode
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(hingePosition)
                    .fillMaxHeight()
            ) {
                primaryContent()
            }

            // Hinge divider
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(androidx.compose.ui.graphics.Color.Gray)
            )

            Box(
                modifier = Modifier
                    .weight(1f - hingePosition)
                    .fillMaxHeight()
            ) {
                secondaryContent()
            }
        }
    } else {
        // Single screen mode
        primaryContent()
    }
}

/**
 * Tablet Optimized Layout
 */
@Composable
fun TabletOptimizedLayout(
    isTablet: Boolean,
    landscapeContent: @Composable () -> Unit,
    portraitContent: @Composable () -> Unit
) {
    if (isTablet) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Navigation rail for tablets
            NavigationRail(
                modifier = Modifier.width(80.dp).fillMaxHeight()
            )

            // Main content
            Box(modifier = Modifier.weight(1f)) {
                landscapeContent()
            }
        }
    } else {
        portraitContent()
    }
}

/**
 * Spatial Audio Effect
 * 3D audio that changes based on position
 */
@Composable
fun SpatialAudioEffect(
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    // In production, integrate with spatial audio APIs
    content()
}

/**
 * Holographic Effect
 * Iridescent, holographic visual effect
 */
@Composable
fun HolographicEffect(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val hueRotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        hueRotation.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Box(
        modifier = modifier.drawWithContent {
            // Draw content
            drawContent()

            // Draw holographic overlay
            drawRect(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Hsv(hueRotation.value % 360, 0.5f, 0.3f),
                        Color.Hsv((hueRotation.value + 60) % 360, 0.5f, 0.3f),
                        Color.Hsv((hueRotation.value + 120) % 360, 0.5f, 0.3f),
                        Color.Hsv((hueRotation.value + 180) % 360, 0.5f, 0.3f),
                        Color.Hsv((hueRotation.value + 240) % 360, 0.5f, 0.3f),
                        Color.Hsv((hueRotation.value + 300) % 360, 0.5f, 0.3f),
                        Color.Hsv(hueRotation.value % 360, 0.5f, 0.3f)
                    )
                ),
                alpha = 0.2f
            )
        }
    ) {
        content()
    }
}

/**
 * Liquid Animation Effect
 * Fluid, liquid-like animation
 */
@Composable
fun LiquidAnimationEffect(
    colors: BaseColors,
    modifier: Modifier = Modifier
) {
    val offset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        offset.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        repeat(5) { i ->
            drawCircle(
                color = colors.primary.copy(alpha = 0.3f),
                radius = size.minDimension * 0.3f,
                center = Offset(
                    size.width / 2 + kotlin.math.cos(Math.toRadians((offset.value + i * 72).toDouble())).toFloat() * 100,
                    size.height / 2 + kotlin.math.sin(Math.toRadians((offset.value + i * 72).toDouble())).toFloat() * 100
                )
            )
        }
    }
}

/**
 * Particle Explosion Effect
 * Burst of particles on interaction
 */
@Composable
fun ParticleExplosionEffect(
    trigger: Boolean,
    colors: BaseColors,
    modifier: Modifier = Modifier,
    particleCount: Int = 50
) {
    var particles by remember { mutableStateOf(listOf<ParticleState>()) }

    LaunchedEffect(trigger) {
        if (trigger) {
            particles = List(particleCount) { i ->
                ParticleState(
                    angle = (i * 360f / particleCount),
                    speed = 5f + (i % 10),
                    color = listOf(colors.primary, colors.secondary, colors.tertiary)[i % 3],
                    size = 5f + (i % 10)
                )
            }

            kotlinx.coroutines.delay(1000)
            particles = emptyList()
        }
    }

    Box(modifier = modifier) {
        particles.forEach { particle ->
            AnimatedParticle(particle = particle)
        }
    }
}

data class ParticleState(
    val angle: Float,
    val speed: Float,
    val color: Int,
    val size: Float
)

@Composable
fun AnimatedParticle(particle: ParticleState) {
    val distance = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        distance.animateTo(
            targetValue = 200f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
    }

    val x = kotlin.math.cos(Math.toRadians(particle.angle.toDouble())).toFloat() * distance.value
    val y = kotlin.math.sin(Math.toRadians(particle.angle.toDouble())).toFloat() * distance.value

    Box(
        modifier = Modifier
            .offset { androidx.compose.ui.unit.IntOffset(x.toInt(), y.toInt()) }
            .size(particle.size.dp)
            .background(
                androidx.compose.ui.graphics.Color(particle.color),
                androidx.compose.foundation.shape.CircleShape
            )
    )
}

/**
 * Glassmorphism Effect
 * Frosted glass appearance
 */
@Composable
fun GlassmorphismEffect(
    modifier: Modifier = Modifier,
    blurRadius: Float = 10f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f),
                androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            .drawWithContent {
                drawContent()
                drawRect(
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.05f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
            }
    ) {
        content()
    }
}

/**
 * Neon Glow Effect
 * Bright neon-style glow
 */
@Composable
fun NeonGlowEffect(
    text: String,
    colors: BaseColors,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = androidx.compose.ui.text.TextStyle(
            fontSize = 24.sp,
            drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.dp.toPx(),
                miter = 4f
            )
        ),
        color = androidx.compose.ui.graphics.Color(colors.primary),
        modifier = modifier.drawWithContent {
            // Draw glow
            drawContent()
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        androidx.compose.ui.graphics.Color(colors.primary).copy(alpha = 0.5f),
                        androidx.compose.ui.graphics.Color.Transparent
                    )
                ),
                alpha = 0.5f
            )
        }
    )
}
