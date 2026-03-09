package com.sugarmunch.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens

/**
 * Sugar - The SugarMunch mascot
 * A cute candy character for onboarding, tooltips, and brand identity
 */
@Composable
fun SugarMascot(
    modifier: Modifier = Modifier,
    size: Float = 1.0f,
    isAnimated: Boolean = true,
    mood: MascotMood = MascotMood.Happy
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_animation")
    
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_bounce"
    )

    val wiggle by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_wiggle"
    )

    val scale = if (isAnimated) 1f + (bounce / 200f) else 1f
    val rotation = if (isAnimated) wiggle else 0f

    Box(
        modifier = modifier
            .size(120.dp * size)
            .scale(scale)
            .rotate(rotation),
        contentAlignment = Alignment.Center
    ) {
        // Body - candy shape
        CandyBody(mood = mood)
        
        // Face
        CandyFace(mood = mood)
    }
}

@Composable
private fun CandyBody(mood: MascotMood) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF69B4), // Hot pink center
                        Color(0xFFFF1493), // Deep pink edge
                        Color(0xFFC71585)  // Medium violet red outer
                    )
                )
            )
    )
}

@Composable
private fun CandyFace(mood: MascotMood) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Eyes
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Eye(isLeft = true, mood = mood)
        }
        
        // Mouth
        Mouth(mood = mood)
        
        // Eyes
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Eye(isLeft = false, mood = mood)
        }
    }
}

@Composable
private fun Eye(
    isLeft: Boolean,
    mood: MascotMood
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eye_blink")
    val blink by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000),
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )

    val eyeHeight = if (blink > 0.9f) 2.dp else 12.dp
    val eyeWidth = if (mood == MascotMood.Excited) 14.dp else 12.dp

    Box(
        modifier = Modifier
            .size(width = eyeWidth, height = eyeHeight)
            .clip(CircleShape)
            .background(Color.White)
    ) {
        // Pupil
        Box(
            modifier = Modifier
                .size(6.dp)
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(Color.Black)
        )
        
        // Highlight
        Box(
            modifier = Modifier
                .size(2.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.8f))
        )
    }
}

@Composable
private fun Mouth(mood: MascotMood) {
    when (mood) {
        MascotMood.Happy -> HappyMouth()
        MascotMood.Excited -> ExcitedMouth()
        MascotMood.Surprised -> SurprisedMouth()
        MascotMood.Thinking -> ThinkingMouth()
    }
}

@Composable
private fun HappyMouth() {
    Box(
        modifier = Modifier
            .size(20.dp, 10.dp)
            .clip(
                RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 10.dp,
                    bottomEnd = 10.dp
                )
            )
            .background(Color.White)
    )
}

@Composable
private fun ExcitedMouth() {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(Color.White)
    )
}

@Composable
private fun SurprisedMouth() {
    Box(
        modifier = Modifier
            .size(12.dp, 16.dp)
            .clip(CircleShape)
            .background(Color.White)
    )
}

@Composable
private fun ThinkingMouth() {
    Box(
        modifier = Modifier
            .size(16.dp, 8.dp)
            .clip(
                RoundedCornerShape(
                    topStart = 8.dp,
                    topEnd = 8.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                )
            )
            .background(Color.White)
    )
}

enum class MascotMood {
    Happy,
    Excited,
    Surprised,
    Thinking
}

/**
 * Animated SugarMunch Logo with morphing effect
 */
@Composable
fun AnimatedSugarMunchLogo(
    modifier: Modifier = Modifier,
    size: Float = 1.0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_animation")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "logo_rotation"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    Box(
        modifier = modifier
            .size(80.dp * size)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            SugarDimens.Brand.hotPink,
                            SugarDimens.Brand.mint,
                            SugarDimens.Brand.yellow,
                            SugarDimens.Brand.hotPink
                        )
                    )
                )
                .rotate(rotation)
        )
        
        // Inner circle
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(SugarDimens.Brand.deepPurple)
        ) {
            Text(
                text = "SM",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            SugarDimens.Brand.hotPink,
                            SugarDimens.Brand.mint
                        )
                    )
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

/**
 * Brand gradient background for screens
 */
@Composable
fun SugarMunchBrandBackground(
    modifier: Modifier = Modifier,
    isAnimated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "brand_bg")
    
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "brand_offset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        SugarDimens.Brand.deepPurple,
                        SugarDimens.Brand.hotPink.copy(alpha = 0.3f),
                        SugarDimens.Brand.mint.copy(alpha = 0.2f)
                    ),
                    start = Offset(offset, 0f),
                    end = Offset(offset + 500f, 1000f)
                )
            )
    )
}
