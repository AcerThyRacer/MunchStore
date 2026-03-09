package com.sugarmunch.app.ui.characters

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import com.sugarmunch.app.ui.feedback.CelebrationType

/**
 * Character animation system
 */

@Composable
fun rememberCharacterAnimation(
    emotion: CharacterEmotion,
    triggered: Boolean
): State<Float> {
    val animation = remember { Animatable(0f) }
    
    LaunchedEffect(triggered) {
        if (triggered) {
            animation.snapTo(0f)
            animation.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }
    
    return animation.asState()
}

@Composable
fun CharacterCelebration(
    characterType: CharacterType,
    triggered: Boolean,
    celebrationType: CelebrationType = CelebrationType.CONFETTI_CANNON
) {
    val animationProgress = rememberCharacterAnimation(CharacterEmotion.EXCITED, triggered)
    
    // Character would animate based on celebration
    // Integration with CelebrationManager
}
