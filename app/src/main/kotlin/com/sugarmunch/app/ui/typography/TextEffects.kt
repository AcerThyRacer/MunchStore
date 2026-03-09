package com.sugarmunch.app.ui.typography

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.sugarmunch.app.ui.design.SugarDimens

/**
 * EXTREME Typography & Text Effects for SugarMunch
 * Gradient text, animated reveals, interactive text
 */

/**
 * Gradient flow text effect
 */
@Composable
fun GradientText(
    text: String,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        SugarDimens.Brand.hotPink,
        SugarDimens.Brand.mint,
        SugarDimens.Brand.yellow,
        SugarDimens.Brand.candyOrange
    ),
    style: TextStyle = MaterialTheme.typography.headlineLarge
) {
    val gradientOffset = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        gradientOffset.animateTo(
            1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }
    
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(
            brush = Brush.linearGradient(
                colors = colors,
                start = Offset(gradientOffset.value, 0f),
                end = Offset(gradientOffset.value + 500f, 0f)
            )
        )
    )
}

/**
 * Neon glow text
 */
@Composable
fun NeonText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = SugarDimens.Brand.hotPink,
    style: TextStyle = MaterialTheme.typography.headlineLarge
) {
    val flicker = remember { Animatable(1f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            flicker.snapTo(0.7f + Math.random().toFloat() * 0.3f)
            delay(50)
        }
    }
    
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(
            color = color.copy(alpha = flicker.value),
            shadow = Shadow(
                color = color.copy(alpha = 0.8f * flicker.value),
                blurRadius = 20f
            )
        )
    )
}

/**
 * Wave reveal text animation
 */
@Composable
fun WaveRevealText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    delayPerChar: Int = 50
) {
    val charAnimations = remember(text.length) {
        List(text.length) { Animatable(0f) }
    }
    
    LaunchedEffect(text) {
        charAnimations.forEachIndexed { index, anim ->
            delay((index * delayPerChar).toLong())
            anim.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }
    
    Row(modifier = modifier) {
        text.forEachIndexed { index, char ->
            BasicText(
                text = char.toString(),
                modifier = Modifier
                    .graphicsLayer {
                        alpha = charAnimations[index].value
                        translationY = (1 - charAnimations[index].value) * 50f
                    },
                style = style
            )
        }
    }
}

/**
 * Typewriter effect
 */
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    charDelay: Int = 100
) {
    val visibleChars = remember { mutableStateOf(0) }
    
    LaunchedEffect(text) {
        visibleChars.value = 0
        for (i in text.indices) {
            visibleChars.value = i + 1
            delay(charDelay.toLong())
        }
    }
    
    BasicText(
        text = text.take(visibleChars.value),
        modifier = modifier,
        style = style
    )
}

/**
 * Liquid wobbly text
 */
@Composable
fun LiquidText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = SugarDimens.Brand.bubblegumBlue,
    style: TextStyle = MaterialTheme.typography.headlineMedium
) {
    val waveOffset = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        waveOffset.animateTo(
            360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }
    
    // Simplified liquid effect
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(
            color = color
        )
    )
}

/**
 * Holographic shimmer text
 */
@Composable
fun HolographicText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineLarge
) {
    val shimmer = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        shimmer.animateTo(
            1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFF0080),
                    Color(0xFF00FFFF),
                    Color(0xFF80FF00),
                    Color(0xFFFF0080)
                ),
                start = Offset(shimmer.value * 1000f, 0f),
                end = Offset(shimmer.value * 1000f + 500f, 0f)
            )
        )
    )
}

/**
 * Bounce in text
 */
@Composable
fun BounceInText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge
) {
    val scale = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    
    BasicText(
        text = text,
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
        style = style
    )
}

/**
 * Interactive text with hover glow
 */
@Composable
fun InteractiveText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    onHover: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val isHovered = remember { mutableStateOf(false) }
    val glowAlpha = remember { Animatable(0f) }
    
    LaunchedEffect(isHovered.value) {
        glowAlpha.animateTo(
            if (isHovered.value) 1f else 0f,
            animationSpec = tween(200)
        )
    }
    
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(
            shadow = Shadow(
                color = SugarDimens.Brand.hotPink.copy(alpha = 0.5f * glowAlpha.value),
                blurRadius = 10f * glowAlpha.value
            )
        )
    )
}
