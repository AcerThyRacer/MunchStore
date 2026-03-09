package com.sugarmunch.app.physics.quantum

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.*
import kotlin.math.*

/**
 * Physics-Based Transitions - Page and content transitions using physics
 */
class PhysicsBasedTransitions {

    companion object {
        /**
         * Spring-based slide transition
         */
        fun slideTransition(
            direction: SlideDirection = SlideDirection.LEFT,
            springConfig: SpringConfig = SpringPresets.SNAPPY
        ): ContentTransform {
            val slideSpec = spring(
                stiffness = springConfig.tension,
                dampingRatio = dampingRatioFromConfig(springConfig)
            )

            val slideOffset = when (direction) {
                SlideDirection.LEFT -> Offset(-1f, 0f)
                SlideDirection.RIGHT -> Offset(1f, 0f)
                SlideDirection.UP -> Offset(0f, -1f)
                SlideDirection.DOWN -> Offset(0f, 1f)
            }

            return slideTransition(
                slideOffset = slideOffset,
                animationSpec = slideSpec
            )
        }

        /**
         * Spring-based scale transition
         */
        fun scaleTransition(
            initialScale: Float = 0.8f,
            springConfig: SpringConfig = SpringPresets.BOUNCY
        ): ContentTransform {
            val scaleSpec = spring(
                stiffness = springConfig.tension,
                dampingRatio = dampingRatioFromConfig(springConfig)
            )

            return scaleTransition(
                initialScale = initialScale,
                animationSpec = scaleSpec
            )
        }

        /**
         * Spring-based fade transition
         */
        fun fadeTransition(
            springConfig: SpringConfig = SpringPresets.GENTLE
        ): ContentTransform {
            val fadeSpec = spring(
                stiffness = springConfig.tension,
                dampingRatio = dampingRatioFromConfig(springConfig)
            )

            return fadeIn(animationSpec = fadeSpec) + fadeOut(animationSpec = fadeSpec)
        }

        /**
         * Combined slide and scale transition
         */
        fun slideAndScaleTransition(
            direction: SlideDirection = SlideDirection.LEFT,
            initialScale: Float = 0.9f,
            springConfig: SpringConfig = SpringPresets.SNAPPY
        ): ContentTransform {
            val slideSpec = spring(
                stiffness = springConfig.tension,
                dampingRatio = dampingRatioFromConfig(springConfig)
            )

            val slideOffset = when (direction) {
                SlideDirection.LEFT -> Offset(-0.1f, 0f)
                SlideDirection.RIGHT -> Offset(0.1f, 0f)
                SlideDirection.UP -> Offset(0f, -0.1f)
                SlideDirection.DOWN -> Offset(0f, 0.1f)
            }

            return slideTransition(slideOffset, slideSpec) + scaleTransition(initialScale, slideSpec)
        }

        /**
         * Rotation transition
         */
        fun rotateTransition(
            initialRotation: Float = -10f,
            springConfig: SpringConfig = SpringPresets.WOBBLY
        ): ContentTransform {
            val rotateSpec = spring(
                stiffness = springConfig.tension,
                dampingRatio = dampingRatioFromConfig(springConfig)
            )

            return animateEnterExit(
                enter = fadeIn() + scaleIn(initialScale = 0.9f, animationSpec = rotateSpec),
                exit = fadeOut() + scaleOut(targetScale = 1.1f, animationSpec = rotateSpec)
            )
        }

        /**
         * Elastic bounce transition
         */
        fun bounceTransition(
            springConfig: SpringConfig = SpringPresets.JELLY
        ): ContentTransform {
            val bounceSpec = spring(
                stiffness = springConfig.tension,
                dampingRatio = dampingRatioFromConfig(springConfig)
            )

            return animateEnterExit(
                enter = scaleIn(initialScale = 0.5f, animationSpec = bounceSpec) +
                    fadeIn(animationSpec = bounceSpec),
                exit = scaleOut(targetScale = 1.5f, animationSpec = bounceSpec) +
                    fadeOut(animationSpec = bounceSpec)
            )
        }

        /**
         * Page flip transition
         */
        fun pageFlipTransition(
            direction: FlipDirection = FlipDirection.LEFT
        ): ContentTransform {
            return animateEnterExit(
                enter = fadeIn(animationSpec = tween(300)) +
                    transform { progress ->
                        val rotation = when (direction) {
                            FlipDirection.LEFT -> -90f + progress * 90f
                            FlipDirection.RIGHT -> 90f - progress * 90f
                        }
                        this.rotationY = rotation
                        this.scaleX = abs(cos(rotation * PI / 180))
                    },
                exit = fadeOut(animationSpec = tween(300)) +
                    transform { progress ->
                        val rotation = when (direction) {
                            FlipDirection.LEFT -> -progress * 90f
                            FlipDirection.RIGHT -> progress * 90f
                        }
                        this.rotationY = rotation
                        this.scaleX = abs(cos(rotation * PI / 180))
                    }
            )
        }

        /**
         * Shared axis transition
         */
        fun sharedAxisTransition(
            axis: Axis = Axis.X,
            forward: Boolean = true
        ): ContentTransform {
            val offsetSpec = spring(
                stiffness = SpringPresets.SNAPPY.tension,
                dampingRatio = 0.8f
            )

            val offset = when (axis) {
                Axis.X -> if (forward) Offset(-0.1f, 0f) else Offset(0.1f, 0f)
                Axis.Y -> if (forward) Offset(0f, -0.1f) else Offset(0f, 0.1f)
                Axis.Z -> Offset.Zero
            }

            return slideTransition(offset, offsetSpec) + fadeTransition(SpringPresets.GENTLE)
        }

        /**
         * Morph transition (shape changing)
         */
        fun morphTransition(
            duration: Int = 500
        ): ContentTransform {
            return animateEnterExit(
                enter = fadeIn(animationSpec = tween(duration)) +
                    scaleIn(initialScale = 0.8f, animationSpec = tween(duration)),
                exit = fadeOut(animationSpec = tween(duration)) +
                    scaleOut(targetScale = 1.2f, animationSpec = tween(duration))
            )
        }

        private fun dampingRatioFromConfig(config: SpringConfig): Float {
            val criticalDamping = 2 * sqrt(config.tension * config.mass)
            return config.damping / criticalDamping
        }
    }
}

/**
 * Slide directions
 */
enum class SlideDirection {
    LEFT, RIGHT, UP, DOWN
}

/**
 * Flip directions
 */
enum class FlipDirection {
    LEFT, RIGHT
}

/**
 * Transition axes
 */
enum class Axis {
    X, Y, Z
}

/**
 * Physics-based modifier extensions
 */
fun Modifier.physicsEnter(
    animationSpec: SpringSpec<Float> = spring(stiffness = 100f, dampingRatio = 0.8f)
) = this.then(
    Modifier.animateEnterExit(
        enter = fadeIn(animationSpec = animationSpec) + scaleIn(animationSpec = animationSpec)
    )
)

fun Modifier.physicsExit(
    animationSpec: SpringSpec<Float> = spring(stiffness = 100f, dampingRatio = 0.8f)
) = this.then(
    Modifier.animateEnterExit(
        exit = fadeOut(animationSpec = animationSpec) + scaleOut(animationSpec = animationSpec)
    )
)

/**
 * Wobble effect modifier
 */
fun Modifier.wobble(
    enabled: Boolean = true,
    wobbleAmount: Float = 5f,
    frequency: Float = 10f
) = if (enabled) {
    this.drawWithContent {
        val wobble = sin(System.currentTimeMillis() / 1000f * frequency * 2 * PI).toFloat() * wobbleAmount
        drawContent()
    }
} else {
    this
}

/**
 * Jiggle effect modifier
 */
fun Modifier.jiggle(
    enabled: Boolean = true,
    jiggleAmount: Float = 3f
) = if (enabled) {
    this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            val jiggleX = (Math.random().toFloat() - 0.5f) * jiggleAmount * 2
            val jiggleY = (Math.random().toFloat() - 0.5f) * jiggleAmount * 2
            placeable.placeRelative(jiggleX, jiggleY)
        }
    }
} else {
    this
}

/**
 * Ripple background effect
 */
fun Modifier.rippleBackground(
    color: Color = Color.White.copy(alpha = 0.3f),
    rippleCount: Int = 3
) = this.drawWithContent {
    drawContent()

    val centerX = size.width / 2
    val centerY = size.height / 2
    val maxRadius = maxOf(size.width, size.height)

    for (i in 0 until rippleCount) {
        val progress = ((System.currentTimeMillis() / 1000f + i * 0.3f) % 1f)
        val radius = progress * maxRadius
        val alpha = (1 - progress) * color.alpha

        drawCircle(
            color = color.copy(alpha = alpha),
            radius = radius,
            center = Offset(centerX, centerY)
        )
    }
}
