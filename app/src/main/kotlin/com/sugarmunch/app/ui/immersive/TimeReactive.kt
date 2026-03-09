package com.sugarmunch.app.ui.immersive

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.sugarmunch.app.ui.design.SugarDimens
import java.util.*

/**
 * EXTREME Time-Reactive Background
 * Circadian rhythm, sunrise/sunset, star field at night
 */

enum class TimeOfDay {
    DAWN,
    MORNING,
    NOON,
    AFTERNOON,
    SUNSET,
    EVENING,
    NIGHT,
    MIDNIGHT
}

@Composable
fun TimeReactiveBackground(
    modifier: Modifier = Modifier,
    timeOfDay: TimeOfDay = getCurrentTimeOfDay(),
    enableStars: Boolean = true,
    enableSun: Boolean = true
) {
    val sunPosition = remember { Animatable(0f) }
    val starAlpha = remember { Animatable(0f) }
    
    LaunchedEffect(timeOfDay) {
        val targetSunPos = when (timeOfDay) {
            TimeOfDay.DAWN -> 0.2f
            TimeOfDay.MORNING -> 0.4f
            TimeOfDay.NOON -> 0.5f
            TimeOfDay.AFTERNOON -> 0.6f
            TimeOfDay.SUNSET -> 0.8f
            TimeOfDay.EVENING -> 0.9f
            TimeOfDay.NIGHT -> 1.0f
            TimeOfDay.MIDNIGHT -> 1.0f
        }
        
        sunPosition.animateTo(targetSunPos, animationSpec = tween(2000))
        
        val targetStarAlpha = if (timeOfDay == TimeOfDay.NIGHT || timeOfDay == TimeOfDay.MIDNIGHT) 1f else 0f
        starAlpha.animateTo(targetStarAlpha, animationSpec = tween(2000))
    }
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        // Sky gradient based on time
        val skyColors = getSkyGradientColors(timeOfDay)
        drawRect(brush = Brush.verticalGradient(skyColors))
        
        // Draw sun/moon
        if (enableSun) {
            drawCelestialBody(timeOfDay, sunPosition.value)
        }
        
        // Draw stars
        if (enableStars && starAlpha.value > 0) {
            drawStarField(starAlpha.value)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCelestialBody(
    timeOfDay: TimeOfDay,
    sunPosition: Float
) {
    val isNight = timeOfDay == TimeOfDay.NIGHT || timeOfDay == TimeOfDay.MIDNIGHT
    val x = size.width * sunPosition
    val y = if (isNight) size.height * 0.2f else size.height * sunPosition * 0.5f
    
    if (isNight) {
        // Draw moon
        drawCircle(
            color = Color(0xFFF4F6F0),
            radius = size.minDimension / 10,
            center = Offset(x, y)
        )
        
        // Moon craters
        drawCircle(
            color = Color(0xFFDDDDDD).copy(alpha = 0.5f),
            radius = size.minDimension / 30,
            center = Offset(x - size.minDimension / 20, y - size.minDimension / 30)
        )
    } else {
        // Draw sun
        drawCircle(
            color = Color(0xFFFFD700),
            radius = size.minDimension / 8,
            center = Offset(x, y)
        )
        
        // Sun rays
        repeat(12) { i ->
            val angle = (i * 30).toFloat() * Math.PI / 180
            val rayStart = Offset(
                x + kotlin.math.cos(angle) * size.minDimension / 8,
                y + kotlin.math.sin(angle) * size.minDimension / 8
            )
            val rayEnd = Offset(
                x + kotlin.math.cos(angle) * size.minDimension / 5,
                y + kotlin.math.sin(angle) * size.minDimension / 5
            )
            drawLine(
                color = Color(0xFFFFD700).copy(alpha = 0.5f),
                start = rayStart,
                end = rayEnd,
                strokeWidth = 3f
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStarField(alpha: Float) {
    repeat(100) { i ->
        val x = (i * 137.5f) % size.width
        val y = (i * 97.3f) % size.height
        val brightness = 0.5f + Math.random().toFloat() * 0.5f
        
        drawCircle(
            color = Color.White.copy(alpha = brightness * alpha),
            radius = (Math.random().toFloat() * 2 + 1) * alpha,
            center = Offset(x, y)
        )
    }
}

private fun getSkyGradientColors(timeOfDay: TimeOfDay): List<Color> {
    return when (timeOfDay) {
        TimeOfDay.DAWN -> listOf(
            Color(0xFFFF7F50),
            Color(0xFFFF6347),
            SugarDimens.Brand.bubblegumBlue
        )
        TimeOfDay.MORNING -> listOf(
            Color(0xFF87CEEB),
            Color(0xFFB0E0E6),
            SugarDimens.Brand.mint.copy(alpha = 0.5f)
        )
        TimeOfDay.NOON -> listOf(
            Color(0xFF00BFFF),
            Color(0xFF87CEEB),
            Color.White
        )
        TimeOfDay.AFTERNOON -> listOf(
            Color(0xFF87CEEB),
            Color(0xFFB0E0E6),
            SugarDimens.Brand.yellow.copy(alpha = 0.3f)
        )
        TimeOfDay.SUNSET -> listOf(
            SugarDimens.Brand.hotPink,
            SugarDimens.Brand.candyOrange,
            Color(0xFFFFD700)
        )
        TimeOfDay.EVENING -> listOf(
            SugarDimens.Brand.deepPurple,
            Color(0xFF4B0082),
            SugarDimens.Brand.bubblegumBlue.copy(alpha = 0.5f)
        )
        TimeOfDay.NIGHT -> listOf(
            SugarDimens.Brand.deepPurple,
            Color(0xFF0F0F2D),
            Color.Black
        )
        TimeOfDay.MIDNIGHT -> listOf(
            Color(0xFF0F0F2D),
            Color.Black,
            Color(0xFF000010)
        )
    }
}

fun getCurrentTimeOfDay(): TimeOfDay {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..7 -> TimeOfDay.DAWN
        in 8..10 -> TimeOfDay.MORNING
        in 11..13 -> TimeOfDay.NOON
        in 14..16 -> TimeOfDay.AFTERNOON
        in 17..19 -> TimeOfDay.SUNSET
        in 20..21 -> TimeOfDay.EVENING
        in 22..23 -> TimeOfDay.NIGHT
        else -> TimeOfDay.MIDNIGHT
    }
}
