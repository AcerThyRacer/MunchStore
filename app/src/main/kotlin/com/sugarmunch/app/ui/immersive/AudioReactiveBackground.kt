package com.sugarmunch.app.ui.immersive

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.sugarmunch.app.ui.design.SugarDimens
import kotlin.math.cos
import kotlin.math.sin

/**
 * EXTREME Audio-Reactive Background
 * Beat-synchronized pulses and frequency-based visualizations
 */

@Composable
fun AudioReactiveBackground(
    modifier: Modifier = Modifier,
    audioData: FloatArray = floatArrayOf(),
    sensitivity: Float = 1.0f,
    colors: List<Color> = listOf(
        SugarDimens.Brand.hotPink,
        SugarDimens.Brand.mint,
        SugarDimens.Brand.yellow,
        SugarDimens.Brand.candyOrange
    )
) {
    val beatPulse = remember { Animatable(0f) }
    val frequencyBands = remember { mutableStateOf(FloatArray(8)) }
    
    // Simulate audio analysis
    LaunchedEffect(Unit) {
        while (true) {
            // Simulate beat detection
            val isBeat = Math.random() > 0.7
            if (isBeat) {
                beatPulse.snapTo(0f)
                beatPulse.animateTo(1f, animationSpec = tween(100))
                beatPulse.animateTo(0.3f, animationSpec = tween(200))
            }
            
            // Simulate frequency bands
            frequencyBands.value = FloatArray(8) { Math.random().toFloat() }
            
            androidx.coroutines.delay(100)
        }
    }
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        
        // Draw background pulse
        val pulseRadius = size.maxDimension / 2 * (0.5f + beatPulse.value * 0.5f * sensitivity)
        drawCircle(
            color = colors[0].copy(alpha = 0.3f * beatPulse.value),
            radius = pulseRadius,
            center = center
        )
        
        // Draw frequency visualizer rings
        frequencyBands.value.forEachIndexed { index, amplitude ->
            val ringRadius = size.minDimension / 4 * (index + 1) / 8f
            val ringAmplitude = amplitude * sensitivity * 50f
            
            drawFrequencyRing(
                center = center,
                baseRadius = ringRadius,
                amplitude = ringAmplitude,
                bandIndex = index,
                color = colors[index % colors.size],
                alpha = 0.5f + amplitude * 0.5f
            )
        }
        
        // Draw particle effects on beat
        if (beatPulse.value > 0.5f) {
            drawBeatParticles(center, beatPulse.value, colors)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFrequencyRing(
    center: Offset,
    baseRadius: Float,
    amplitude: Float,
    bandIndex: Int,
    color: Color,
    alpha: Float
) {
    val points = 36
    val path = androidx.compose.ui.graphics.Path()
    
    for (i in 0 until points) {
        val angle = (i * 360f / points) * Math.PI / 180
        val waveOffset = kotlin.math.sin(angle * 3 + bandIndex) * amplitude
        val radius = baseRadius + waveOffset
        val x = center.x + cos(angle).toFloat() * radius
        val y = center.y + sin(angle).toFloat() * radius
        
        if (i == 0) path.moveTo(x, y)
        else path.lineTo(x, y)
    }
    
    path.close()
    drawPath(path, color.copy(alpha = alpha), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBeatParticles(
    center: Offset,
    beatStrength: Float,
    colors: List<Color>
) {
    repeat(20) { i ->
        val angle = (i * 18f) * Math.PI / 180
        val distance = 100f * beatStrength
        val x = center.x + cos(angle).toFloat() * distance
        val y = center.y + sin(angle).toFloat() * distance
        
        drawCircle(
            color = colors[i % colors.size].copy(alpha = beatStrength * 0.5f),
            radius = 5f * beatStrength,
            center = Offset(x, y)
        )
    }
}
