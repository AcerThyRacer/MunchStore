package com.sugarmunch.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay

enum class SweetDialogType {
    SUCCESS,
    ERROR,
    LOADING
}

@Composable
fun SweetSuccessDialog(
    showDialog: Boolean,
    type: SweetDialogType = SweetDialogType.SUCCESS,
    title: String = "Sweet Success!",
    message: String = "Your candy is ready \uD83C\uDF6C",
    buttonText: String = "Yay!",
    onDismiss: () -> Unit,
    onButtonClick: () -> Unit = onDismiss
) {
    var showConfetti by remember { mutableStateOf(false) }
    
    if (showDialog && type == SweetDialogType.SUCCESS) {
        showConfetti = true
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = type != SweetDialogType.LOADING,
                dismissOnClickOutside = type != SweetDialogType.LOADING
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Animated icon
                    SweetDialogIcon(type = type)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    if (type != SweetDialogType.LOADING) {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = onButtonClick,
                            modifier = Modifier.fillMaxWidth(0.7f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (type) {
                                    SweetDialogType.SUCCESS -> com.sugarmunch.app.ui.theme.CandyPink
                                    SweetDialogType.ERROR -> Color(0xFFFF6B6B)
                                    SweetDialogType.LOADING -> MaterialTheme.colorScheme.primary
                                }
                            )
                        ) {
                            Text(
                                text = buttonText,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SweetDialogIcon(type: SweetDialogType) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            when (type) {
                SweetDialogType.SUCCESS -> com.sugarmunch.app.R.raw.candy_splash
                SweetDialogType.ERROR -> com.sugarmunch.app.R.raw.candy_splash
                SweetDialogType.LOADING -> com.sugarmunch.app.R.raw.candy_splash
            }
        )
    )
    
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = if (type == SweetDialogType.LOADING) Int.MAX_VALUE else 1,
        speed = if (type == SweetDialogType.LOADING) 1.5f else 1f
    )

    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = when (type) {
                        SweetDialogType.SUCCESS -> listOf(
                            com.sugarmunch.app.ui.theme.CandyPink.copy(alpha = 0.2f),
                            com.sugarmunch.app.ui.theme.CandyPink.copy(alpha = 0.1f)
                        )
                        SweetDialogType.ERROR -> listOf(
                            Color(0xFFFF6B6B).copy(alpha = 0.2f),
                            Color(0xFFFF6B6B).copy(alpha = 0.1f)
                        )
                        SweetDialogType.LOADING -> listOf(
                            com.sugarmunch.app.ui.theme.CottonCandyBlue.copy(alpha = 0.2f),
                            com.sugarmunch.app.ui.theme.CottonCandyBlue.copy(alpha = 0.1f)
                        )
                    }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(80.dp)
        )
    }
}

@Composable
fun CandyConfettiOverlay(
    show: Boolean,
    onComplete: () -> Unit = {},
    colors: List<androidx.compose.ui.graphics.Color>? = null
) {
    val confettiColors = colors?.map { it.toArgb() } ?: listOf(
        0xFFFFB6C1.toInt(),
        0xFFB5DEFF.toInt(),
        0xFF98FF98.toInt(),
        0xFFE6B3FF.toInt(),
        0xFFFFFACD.toInt()
    )
    AnimatedVisibility(
        visible = show,
        enter = fadeIn()
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            nl.dionsegijn.konfetti.compose.KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(
                    nl.dionsegijn.konfetti.core.Party(
                        speed = 10f,
                        maxSpeed = 30f,
                        damping = 0.9f,
                        spread = nl.dionsegijn.konfetti.core.Spread.SMALL,
                        colors = confettiColors,
                        position = nl.dionsegijn.konfetti.core.Position.Relative(0.5, 0.3),
                        angle = nl.dionsegijn.konfetti.core.Angle.TOP,
                        emitter = nl.dionsegijn.konfetti.core.emitter.Emitter(
                            duration = 2,
                            java.util.concurrent.TimeUnit.SECONDS
                        ).perSecond(100)
                    )
                ),
                updateListener = object : nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener {
                    override fun onParticleSystemEnded(
                        system: nl.dionsegijn.konfetti.core.PartySystem,
                        activeSystems: Int
                    ) {
                        if (activeSystems == 0) {
                            onComplete()
                        }
                    }
                    override fun onParticleSystemStarted(
                        system: nl.dionsegijn.konfetti.core.PartySystem,
                        activeSystems: Int
                    ) {}
                }
            )
        }
    }
}