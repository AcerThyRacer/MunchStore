package com.sugarmunch.app.ui.illustrations

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens

/**
 * EXTREME Illustration Library for SugarMunch
 * Candy-themed illustrations, scene backgrounds, achievement badges
 */

enum class IllustrationType {
    CANDY_JAR,
    MAGIC_WAND,
    GIFT_BOX,
    TROPHY,
    STAR,
    HEART,
    LIGHTNING,
    RAINBOW,
    CLOUD,
    BALLOON
}

@Composable
fun CandyIllustration(
    type: IllustrationType = IllustrationType.CANDY_JAR,
    modifier: Modifier = Modifier,
    size: Float = 100f,
    isAnimated: Boolean = true
) {
    val shimmer = remember { Animatable(0f) }
    val float = remember { Animatable(0f) }
    
    LaunchedEffect(isAnimated) {
        if (isAnimated) {
            shimmer.animateTo(
                1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            float.animateTo(
                1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }
    
    Canvas(
        modifier = modifier.size((size * 1.2).dp)
    ) {
        when (type) {
            IllustrationType.CANDY_JAR -> drawCandyJar(shimmer.value, float.value, size)
            IllustrationType.MAGIC_WAND -> drawMagicWand(shimmer.value, float.value, size)
            IllustrationType.GIFT_BOX -> drawGiftBox(shimmer.value, float.value, size)
            IllustrationType.TROPHY -> drawTrophy(shimmer.value, float.value, size)
            IllustrationType.STAR -> drawStarIllustration(shimmer.value, float.value, size)
            IllustrationType.HEART -> drawHeartIllustration(shimmer.value, float.value, size)
            IllustrationType.LIGHTNING -> drawLightning(shimmer.value, float.value, size)
            IllustrationType.RAINBOW -> drawRainbow(shimmer.value, float.value, size)
            IllustrationType.CLOUD -> drawCloud(shimmer.value, float.value, size)
            IllustrationType.BALLOON -> drawBalloon(shimmer.value, float.value, size)
        }
    }
}

private fun DrawScope.drawCandyJar(shimmer: Float, float: Float, size: Float) {
    val centerX = size * 0.6f
    val centerY = size * 0.6f + float * 10f
    
    // Jar body
    drawCircle(
        color = Color(0xFFE0E0E0).copy(alpha = 0.5f),
        radius = size * 0.4f,
        center = Offset(centerX, centerY)
    )
    
    // Jar lid
    drawRect(
        color = SugarDimens.Brand.hotPink,
        topLeft = Offset(centerX - size * 0.35f, centerY - size * 0.35f),
        size = Size(size * 0.7f, size * 0.15f)
    )
    
    // Candies inside
    repeat(5) { i ->
        val candyX = centerX + (Math.random().toFloat() - 0.5f) * size * 0.5f
        val candyY = centerY + (Math.random().toFloat() - 0.5f) * size * 0.5f
        val candyColor = listOf(
            SugarDimens.Brand.hotPink,
            SugarDimens.Brand.mint,
            SugarDimens.Brand.yellow
        )[i % 3]
        
        drawCircle(
            color = candyColor.copy(alpha = 0.7f + shimmer * 0.3f),
            radius = size * 0.08f,
            center = Offset(candyX, candyY)
        )
    }
}

private fun DrawScope.drawMagicWand(shimmer: Float, float: Float, size: Float) {
    val centerX = size * 0.6f
    val centerY = size * 0.6f + float * 10f
    
    // Wand stick
    drawRect(
        color = SugarDimens.Brand.deepPurple,
        topLeft = Offset(centerX - size * 0.05f, centerY),
        size = Size(size * 0.1f, size * 0.5f)
    )
    
    // Star on top
    drawStar(centerX, centerY - size * 0.25f, size * 0.15f, SugarDimens.Brand.yellow)
    
    // Sparkles
    repeat(4) { i ->
        val angle = (i * 90).toFloat() * Math.PI / 180
        val sparkleX = centerX + kotlin.math.cos(angle).toFloat() * size * 0.25f
        val sparkleY = centerY - size * 0.25f + kotlin.math.sin(angle).toFloat() * size * 0.25f
        
        drawCircle(
            color = SugarDimens.Brand.mint.copy(alpha = 0.5f + shimmer * 0.5f),
            radius = size * 0.05f,
            center = Offset(sparkleX, sparkleY)
        )
    }
}

private fun DrawScope.drawGiftBox(shimmer: Float, float: Float, size: Float) {
    val centerX = size * 0.6f
    val centerY = size * 0.6f + float * 10f
    
    // Box
    drawRect(
        color = SugarDimens.Brand.hotPink,
        topLeft = Offset(centerX - size * 0.3f, centerY - size * 0.25f),
        size = Size(size * 0.6f, size * 0.5f)
    )
    
    // Ribbon vertical
    drawRect(
        color = SugarDimens.Brand.yellow,
        topLeft = Offset(centerX - size * 0.05f, centerY - size * 0.25f),
        size = Size(size * 0.1f, size * 0.5f)
    )
    
    // Ribbon horizontal
    drawRect(
        color = SugarDimens.Brand.yellow,
        topLeft = Offset(centerX - size * 0.3f, centerY - size * 0.1f),
        size = Size(size * 0.6f, size * 0.1f)
    )
    
    // Bow
    drawCircle(
        color = SugarDimens.Brand.yellow,
        radius = size * 0.1f,
        center = Offset(centerX, centerY - size * 0.25f)
    )
}

private fun DrawScope.drawTrophy(shimmer: Float, float: Float, size: Float) {
    val centerX = size * 0.6f
    val centerY = size * 0.6f + float * 10f
    
    // Trophy cup
    drawArc(
        color = SugarDimens.Brand.yellow,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(centerX - size * 0.25f, centerY - size * 0.2f),
        size = Size(size * 0.5f, size * 0.4f)
    )
    
    // Base
    drawRect(
        color = SugarDimens.Brand.candyOrange,
        topLeft = Offset(centerX - size * 0.15f, centerY + size * 0.1f),
        size = Size(size * 0.3f, size * 0.1f)
    )
    
    // Stem
    drawRect(
        color = SugarDimens.Brand.yellow,
        topLeft = Offset(centerX - size * 0.05f, centerY + size * 0.05f),
        size = Size(size * 0.1f, size * 0.1f)
    )
}

private fun DrawScope.drawStarIllustration(shimmer: Float, float: Float, size: Float) {
    drawStar(size * 0.6f, size * 0.6f + float * 10f, size * 0.4f, SugarDimens.Brand.yellow)
}

private fun DrawScope.drawHeartIllustration(shimmer: Float, float: Float, size: Float) {
    val centerX = size * 0.6f
    val centerY = size * 0.6f + float * 10f
    
    drawHeart(centerX, centerY, size * 0.35f, SugarDimens.Brand.hotPink)
}

private fun DrawScope.drawLightning(shimmer: Float, float: Float, size: Float) {
    val centerX = size * 0.6f
    val centerY = size * 0.6f + float * 10f
    
    val path = androidx.compose.ui.graphics.Path()
    path.moveTo(centerX - size * 0.1f, centerY - size * 0.3f)
    path.lineTo(centerX + size * 0.1f, centerY - size * 0.3f)
    path.lineTo(centerX, centerY)
    path.lineTo(centerX + size * 0.15f, centerY)
    path.lineTo(centerX - size * 0.05f, centerY + size * 0.3f)
    path.lineTo(centerX, centerY + size * 0.1f)
    path.lineTo(centerX - size * 0.15f, centerY + size * 0.1f)
    path.close()
    
    drawPath(path, SugarDimens.Brand.yellow.copy(alpha = 0.8f + shimmer * 0.2f))
}

private fun DrawScope.drawRainbow(shimmer: Float, float: Float, size: Float) {
    val centerX = size * 0.6f
    val centerY = size * 0.6f + float * 10f
    
    val colors = listOf(
        Color(0xFFFF0000),
        Color(0xFFFF7F00),
        Color(0xFFFFFF00),
        Color(0xFF00FF00),
        Color(0xFF0000FF),
        Color(0xFF4B0082),
        Color(0xFF9400D3)
    )
    
    colors.forEachIndexed { i, color ->
        drawArc(
            color = color.copy(alpha = 0.7f + shimmer * 0.3f),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(centerX - size * (0.4f + i * 0.05f), centerY - size * (0.3f + i * 0.05f)),
            size = Size(size * (0.8f + i * 0.1f), size * (0.6f + i * 0.1f)),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = size * 0.05f)
        )
    }
}

private fun DrawScope.drawCloud(shimmer: Float, float: Float, size: Float) {
    val centerX = size * 0.6f
    val centerY = size * 0.6f + float * 10f
    
    drawCloud(centerX, centerY, size * 0.3f, Color.White)
}

private fun DrawScope.drawBalloon(shimmer: Float, float: Float, size: Float) {
    val centerX = size * 0.6f
    val centerY = size * 0.5f + float * 10f
    
    // Balloon
    drawCircle(
        color = SugarDimens.Brand.hotPink.copy(alpha = 0.8f + shimmer * 0.2f),
        radius = size * 0.3f,
        center = Offset(centerX, centerY)
    )
    
    // String
    drawLine(
        color = SugarDimens.Brand.hotPink,
        start = Offset(centerX, centerY + size * 0.3f),
        end = Offset(centerX, centerY + size * 0.5f),
        strokeWidth = 2f
    )
}

// Helper functions (reuse from characters)
private fun DrawScope.drawStar(centerX: Float, centerY: Float, size: Float, color: Color) {
    val path = androidx.compose.ui.graphics.Path()
    repeat(10) { i ->
        val angle = (i * 36 - 90).toFloat() * Math.PI / 180
        val radius = if (i % 2 == 0) size else size * 0.5f
        val x = centerX + kotlin.math.cos(angle).toFloat() * radius
        val y = centerY + kotlin.math.sin(angle).toFloat() * radius
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

private fun DrawScope.drawHeart(centerX: Float, centerY: Float, size: Float, color: Color) {
    val path = androidx.compose.ui.graphics.Path()
    path.moveTo(centerX, centerY + size * 0.3f)
    path.cubicTo(
        centerX - size * 0.5f, centerY - size * 0.3f,
        centerX - size * 0.5f, centerY - size * 0.5f,
        centerX, centerY - size * 0.3f
    )
    path.cubicTo(
        centerX + size * 0.5f, centerY - size * 0.5f,
        centerX + size * 0.5f, centerY - size * 0.3f,
        centerX, centerY + size * 0.3f
    )
    drawPath(path, color)
}

private fun DrawScope.drawCloud(centerX: Float, centerY: Float, size: Float, color: Color) {
    drawCircle(color, size * 0.8f, Offset(centerX - size * 0.5f, centerY))
    drawCircle(color, size, Offset(centerX, centerY - size * 0.2f))
    drawCircle(color, size * 0.7f, Offset(centerX + size * 0.5f, centerY))
}
