package com.sugarmunch.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val bgIntensity by themeManager.backgroundIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(com.sugarmunch.app.R.raw.candy_splash)
    )
    
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = 1f + (themeIntensity * 0.5f) // Speed up animation with intensity
    )
    
    var textVisible by remember { mutableStateOf(false) }
    val textAlpha by animateFloatAsState(
        targetValue = if (textVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "textAlpha"
    )
    
    LaunchedEffect(progress) {
        if (progress >= 0.3f) {
            textVisible = true
        }
        if (progress >= 1f) {
            delay(300)
            onSplashComplete()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Dynamic theme background with high intensity for splash
        val backgroundBrush = currentTheme.getBackgroundGradient(bgIntensity * 1.5f)
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        )
        
        // Additional particle effects based on theme
        if (currentTheme.particleConfig.enabled) {
            // Particles are rendered by the theme system
        }
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(200.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Column(
                    modifier = Modifier.alpha(textAlpha),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SugarMunch",
                        style = MaterialTheme.typography.displaySmall,
                        color = colors.onSurface
                    )
                    Text(
                        text = "Live, Life, Love \u2764",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Show current theme name
                    Text(
                        text = currentTheme.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
