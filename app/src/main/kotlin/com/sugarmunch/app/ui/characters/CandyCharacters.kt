package com.sugarmunch.app.ui.characters

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlin.math.cos
import kotlin.math.sin

/**
 * EXTREME Character & Mascot System for SugarMunch
 * Candy the Gummy Bear, Professor Lollipop, Sugar Rush
 */

enum class CharacterType {
    CANDY_GUMMY_BEAR,
    PROFESSOR_LOLLIPOP,
    SUGAR_RUSH,
    MINTY_FRESH,
    CARAMEL_DIM
}

enum class CharacterEmotion {
    HAPPY,
    EXCITED,
    CONFUSED,
    SURPRISED,
    SLEEPY,
    LOVE
}

@Composable
fun CandyCharacter(
    characterType: CharacterType = CharacterType.CANDY_GUMMY_BEAR,
    emotion: CharacterEmotion = CharacterEmotion.HAPPY,
    modifier: Modifier = Modifier,
    size: Float = 100f,
    isAnimated: Boolean = true
) {
    val breathAnimation = remember { Animatable(0f) }
    val bounceAnimation = remember { Animatable(0f) }
    
    LaunchedEffect(isAnimated) {
        if (isAnimated) {
            // Breathing animation
            launch {
                breathAnimation.animateTo(
                    1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
            
            // Bounce animation
            launch {
                bounceAnimation.animateTo(
                    1f,
                    animationSpec = infiniteRepeatable(
                        animation = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
        }
    }
    
    Canvas(
        modifier = modifier.size((size * 1.5).dp)
    ) {
        when (characterType) {
            CharacterType.CANDY_GUMMY_BEAR -> drawCandyGummyBear(emotion, breathAnimation.value, bounceAnimation.value, size)
            CharacterType.PROFESSOR_LOLLIPOP -> drawProfessorLollipop(emotion, breathAnimation.value, size)
            CharacterType.SUGAR_RUSH -> drawSugarRush(emotion, breathAnimation.value, size)
            else -> drawCandyGummyBear(emotion, breathAnimation.value, bounceAnimation.value, size)
        }
    }
}

private fun DrawScope.drawCandyGummyBear(
    emotion: CharacterEmotion,
    breath: Float,
    bounce: Float,
    size: Float
) {
    val centerX = size * 0.75f
    val centerY = size * 0.75f + bounce * 10f
    val scale = 1f + breath * 0.05f
    
    // Body (gummy bear shape)
    drawCircle(
        color = SugarDimens.Brand.hotPink.copy(alpha = 0.8f),
        radius = size * 0.4f * scale,
        center = Offset(centerX, centerY)
    )
    
    // Head
    drawCircle(
        color = SugarDimens.Brand.hotPink,
        radius = size * 0.3f * scale,
        center = Offset(centerX, centerY - size * 0.5f * scale)
    )
    
    // Ears
    drawCircle(
        color = SugarDimens.Brand.hotPink,
        radius = size * 0.1f * scale,
        center = Offset(centerX - size * 0.25f * scale, centerY - size * 0.7f * scale)
    )
    drawCircle(
        color = SugarDimens.Brand.hotPink,
        radius = size * 0.1f * scale,
        center = Offset(centerX + size * 0.25f * scale, centerY - size * 0.7f * scale)
    )
    
    // Eyes based on emotion
    drawCharacterEyes(emotion, centerX, centerY - size * 0.55f * scale, size * scale)
    
    // Smile
    drawCharacterSmile(emotion, centerX, centerY - size * 0.4f * scale, size * scale)
}

private fun DrawScope.drawProfessorLollipop(
    emotion: CharacterEmotion,
    breath: Float,
    size: Float
) {
    val centerX = size * 0.75f
    val centerY = size * 0.75f
    
    // Lollipop stick
    drawRect(
        color = Color.White,
        topLeft = Offset(centerX - size * 0.05f, centerY),
        size = androidx.compose.ui.geometry.Size(size * 0.1f, size * 0.5f)
    )
    
    // Lollipop head (swirl)
    drawCircle(
        color = SugarDimens.Brand.mint,
        radius = size * 0.35f,
        center = Offset(centerX, centerY - size * 0.2f)
    )
    
    // Swirl pattern
    repeat(3) { i ->
        drawCircle(
            color = SugarDimens.Brand.yellow,
            radius = size * (0.25f - i * 0.08f),
            center = Offset(centerX, centerY - size * 0.2f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
        )
    }
    
    // Professor glasses
    drawCharacterGlasses(centerX, centerY - size * 0.25f, size)
}

private fun DrawScope.drawSugarRush(
    emotion: CharacterEmotion,
    breath: Float,
    size: Float
) {
    val centerX = size * 0.75f
    val centerY = size * 0.75f
    
    // Lightning bolt body
    val path = androidx.compose.ui.graphics.Path()
    path.moveTo(centerX - size * 0.1f, centerY - size * 0.3f)
    path.lineTo(centerX + size * 0.1f, centerY - size * 0.3f)
    path.lineTo(centerX, centerY)
    path.lineTo(centerX + size * 0.15f, centerY)
    path.lineTo(centerX - size * 0.05f, centerY + size * 0.3f)
    path.lineTo(centerX, centerY + size * 0.1f)
    path.lineTo(centerX - size * 0.15f, centerY + size * 0.1f)
    path.close()
    
    drawPath(path, SugarDimens.Brand.yellow)
    
    // Electric aura
    repeat(8) { i ->
        val angle = (i * 45).toFloat() * Math.PI / 180
        val sparkX = centerX + cos(angle).toFloat() * size * 0.5f
        val sparkY = centerY + sin(angle).toFloat() * size * 0.5f
        
        drawCircle(
            color = SugarDimens.Brand.candyOrange.copy(alpha = 0.5f + breath * 0.3f),
            radius = size * 0.05f,
            center = Offset(sparkX, sparkY)
        )
    }
}

private fun DrawScope.drawCharacterEyes(
    emotion: CharacterEmotion,
    centerX: Float,
    centerY: Float,
    size: Float
) {
    when (emotion) {
        CharacterEmotion.HAPPY -> {
            // Happy curved eyes
            drawArc(
                color = Color.Black,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(centerX - size * 0.15f, centerY - size * 0.05f),
                size = androidx.compose.ui.geometry.Size(size * 0.1f, size * 0.1f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
            drawArc(
                color = Color.Black,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(centerX + size * 0.05f, centerY - size * 0.05f),
                size = androidx.compose.ui.geometry.Size(size * 0.1f, size * 0.1f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
        }
        CharacterEmotion.EXCITED -> {
            // Star eyes
            drawStar(centerX - size * 0.1f, centerY, size * 0.08f)
            drawStar(centerX + size * 0.1f, centerY, size * 0.08f)
        }
        CharacterEmotion.LOVE -> {
            // Heart eyes
            drawHeart(centerX - size * 0.1f, centerY, size * 0.08f, Color.Red)
            drawHeart(centerX + size * 0.1f, centerY, size * 0.08f, Color.Red)
        }
        else -> {
            // Normal eyes
            drawCircle(Color.Black, size * 0.05f, Offset(centerX - size * 0.1f, centerY))
            drawCircle(Color.Black, size * 0.05f, Offset(centerX + size * 0.1f, centerY))
        }
    }
}

private fun DrawScope.drawCharacterSmile(
    emotion: CharacterEmotion,
    centerX: Float,
    centerY: Float,
    size: Float
) {
    when (emotion) {
        CharacterEmotion.HAPPY, CharacterEmotion.EXCITED, CharacterEmotion.LOVE -> {
            drawArc(
                color = Color.Black,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(centerX - size * 0.15f, centerY),
                size = androidx.compose.ui.geometry.Size(size * 0.3f, size * 0.2f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
        }
        CharacterEmotion.CONFUSED -> {
            drawArc(
                color = Color.Black,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(centerX - size * 0.15f, centerY),
                size = androidx.compose.ui.geometry.Size(size * 0.3f, size * 0.2f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
        }
        else -> {}
    }
}

private fun DrawScope.drawStar(centerX: Float, centerY: Float, size: Float) {
    val path = androidx.compose.ui.graphics.Path()
    repeat(10) { i ->
        val angle = (i * 36 - 90).toFloat() * Math.PI / 180
        val radius = if (i % 2 == 0) size else size * 0.5f
        val x = centerX + cos(angle).toFloat() * radius
        val y = centerY + sin(angle).toFloat() * radius
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, SugarDimens.Brand.yellow)
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

private fun DrawScope.drawCharacterGlasses(centerX: Float, centerY: Float, size: Float) {
    drawCircle(
        color = Color.Black,
        radius = size * 0.12f,
        center = Offset(centerX - size * 0.15f, centerY),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
    )
    drawCircle(
        color = Color.Black,
        radius = size * 0.12f,
        center = Offset(centerX + size * 0.15f, centerY),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
    )
    drawLine(
        color = Color.Black,
        start = Offset(centerX - size * 0.03f, centerY),
        end = Offset(centerX + size * 0.03f, centerY),
        strokeWidth = 3f
    )
}
