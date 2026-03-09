package com.sugarmunch.app.ui.immersive

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens

/**
 * EXTREME Weather-Reactive Background
 * Rain, snow, lightning, fog effects based on weather
 */

enum class WeatherType {
    SUNNY,
    CLOUDY,
    RAINY,
    SNOWY,
    STORMY,
    FOGGY
}

@Composable
fun WeatherReactiveBackground(
    weatherType: WeatherType = WeatherType.SUNNY,
    modifier: Modifier = Modifier,
    intensity: Float = 1.0f
) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(weatherType) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(1000))
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Background gradient based on weather
        WeatherBackgroundGradient(weatherType, animationProgress.value)
        
        // Weather effects
        when (weatherType) {
            WeatherType.RAINY -> RainEffect(animationProgress.value, intensity)
            WeatherType.SNOWY -> SnowEffect(animationProgress.value, intensity)
            WeatherType.STORMY -> StormEffect(animationProgress.value, intensity)
            WeatherType.FOGGY -> FogEffect(animationProgress.value, intensity)
            else -> {}
        }
    }
}

@Composable
private fun WeatherBackgroundGradient(
    weatherType: WeatherType,
    alpha: Float
) {
    val colors = when (weatherType) {
        WeatherType.SUNNY -> listOf(
            Color(0xFF87CEEB),
            Color(0xFFB0E0E6),
            SugarDimens.Brand.bubblegumBlue
        )
        WeatherType.CLOUDY -> listOf(
            Color(0xFFB0B0B0),
            Color(0xFFD3D3D3),
            Color(0xFFE0E0E0)
        )
        WeatherType.RAINY -> listOf(
            Color(0xFF4A4A4A),
            Color(0xFF696969),
            SugarDimens.Brand.bubblegumBlue.copy(alpha = 0.5f)
        )
        WeatherType.SNOWY -> listOf(
            Color(0xFFE6F3FF),
            Color(0xFFB0E0E6),
            SugarDimens.Brand.mint.copy(alpha = 0.3f)
        )
        WeatherType.STORMY -> listOf(
            Color(0xFF2C2C2C),
            Color(0xFF4A4A4A),
            SugarDimens.Brand.deepPurple
        )
        WeatherType.FOGGY -> listOf(
            Color(0xFFB0B0B0),
            Color(0xFFC0C0C0),
            Color(0xFFD3D3D3)
        )
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(colors),
            alpha = alpha
        )
    }
}

@Composable
private fun RainEffect(progress: Float, intensity: Float) {
    val raindrops = remember { (50 * intensity).toInt() }
    val positions = remember { List(raindrops) { Pair(Math.random().toFloat(), Math.random().toFloat()) } }
    val fallOffset = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        fallOffset.animateTo(
            1000f,
            animationSpec = infiniteRepeatable(
                animation = tween((2000 / intensity).toInt(), easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        positions.forEach { (x, y) ->
            val dropX = x * size.width
            val dropY = (y * size.height + fallOffset.value) % size.height
            drawLine(
                color = SugarDimens.Brand.bubblegumBlue.copy(alpha = 0.5f * progress),
                start = Offset(dropX, dropY),
                end = Offset(dropX, dropY + 20f),
                strokeWidth = 2f * intensity
            )
        }
    }
}

@Composable
private fun SnowEffect(progress: Float, intensity: Float) {
    val snowflakes = remember { (30 * intensity).toInt() }
    val positions = remember { List(snowflakes) { Triple(Math.random().toFloat(), Math.random().toFloat(), Math.random().toFloat() * 5 + 2) } }
    val fallOffset = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        fallOffset.animateTo(
            1000f,
            animationSpec = infiniteRepeatable(
                animation = tween((3000 / intensity).toInt(), easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        positions.forEach { (x, y, size) ->
            val flakeX = x * size.width + kotlin.math.sin(fallOffset.value * 0.01f + x) * 30f
            val flakeY = (y * size.height + fallOffset.value) % size.height
            drawCircle(
                color = Color.White.copy(alpha = 0.8f * progress),
                radius = size * intensity,
                center = Offset(flakeX, flakeY)
            )
        }
    }
}

@Composable
private fun StormEffect(progress: Float, intensity: Float) {
    val lightningAlpha = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            lightningAlpha.snapTo(0f)
            androidx.coroutines.delay((Math.random() * 5000).toLong())
            lightningAlpha.snapTo(0.8f * progress)
            androidx.coroutines.delay(100)
            lightningAlpha.snapTo(0f)
        }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (lightningAlpha.value > 0) {
            drawRect(color = Color.White.copy(alpha = lightningAlpha.value))
            
            // Draw lightning bolt
            val startX = size.width * 0.5f
            var currentY = 0f
            var currentX = startX
            
            repeat(10) {
                val nextX = currentX + (Math.random().toFloat() - 0.5f) * 100f
                val nextY = currentY + size.height / 10f
                drawLine(
                    color = Color(0xFFFFD700).copy(alpha = lightningAlpha.value),
                    start = Offset(currentX, currentY),
                    end = Offset(nextX, nextY),
                    strokeWidth = 3f * intensity
                )
                currentX = nextX
                currentY = nextY
            }
        }
    }
    
    RainEffect(progress, intensity * 1.5f)
}

@Composable
private fun FogEffect(progress: Float, intensity: Float) {
    val fogOffset = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        fogOffset.animateTo(
            1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        repeat(5) { i ->
            val fogY = size.height - (i + 1) * size.height / 6
            val fogAlpha = (0.1f + i * 0.05f) * progress
            drawRect(
                color = Color.Gray.copy(alpha = fogAlpha),
                topLeft = Offset(0f, fogY + kotlin.math.sin(fogOffset.value * 0.001f + i) * 20f),
                size = androidx.compose.ui.geometry.Size(size.width, size.height / 4f)
            )
        }
    }
}
