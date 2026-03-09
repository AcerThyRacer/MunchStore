package com.sugarmunch.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.R
import com.airbnb.lottie.compose.*
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.presets.ThemePresets
import com.sugarmunch.app.ui.components.Haptics
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
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
        iterations = Int.MAX_VALUE,
        speed = 0.8f + (themeIntensity * 0.4f)
    )
    
    var contentVisible by remember { mutableStateOf(false) }
    var buttonVisible by remember { mutableStateOf(false) }
    var showThemeHint by remember { mutableStateOf(false) }
    var step by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        delay(500)
        contentVisible = true
        delay(800)
        buttonVisible = true
        delay(2000)
        showThemeHint = true
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Dynamic theme background
        val backgroundBrush = currentTheme.getBackgroundGradient(bgIntensity)
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        )
        
        // Animated particles
        AnimatedThemeBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (step == 0) {
            // Lottie animation
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(180.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title with animation
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn() + slideInVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SugarMunch",
                        style = MaterialTheme.typography.displaySmall,
                        color = colors.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Live, Life, Love \u2764",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Description with animation
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(animationSpec = tween(500))
            ) {
                Text(
                    text = "Discover and install sweet modded apps.\nGrant overlay permission for the candy FAB.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = colors.onSurface.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Current theme indicator
            AnimatedVisibility(
                visible = showThemeHint,
                enter = fadeIn()
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Current theme: ${currentTheme.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Button with animation
            AnimatedVisibility(
                visible = buttonVisible,
                enter = fadeIn() + slideInVertically()
            ) {
                Button(
                    onClick = { step = 1 },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary
                    )
                ) {
                    Text(
                        "Get Started",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick theme switch hint
            AnimatedVisibility(
                visible = buttonVisible,
                enter = fadeIn()
            ) {
                TextButton(
                    onClick = {
                        val themes = listOf(
                            ThemePresets.CLASSIC_CANDY,
                            ThemePresets.COTTON_CANDY,
                            ThemePresets.CHILL_MINT,
                            ThemePresets.SUGARRUSH_CLASSIC
                        )
                        val currentIndex = themes.indexOfFirst { it.id == currentTheme.id }
                        val nextTheme = themes[(currentIndex + 1) % themes.size]
                        themeManager.setTheme(nextTheme)
                        Haptics.performTick(context)
                    }
                ) {
                    Text(
                        "Try a different theme \uD83C\uDFA8",
                        color = colors.primary
                    )
                }
            }
            } else {
                // Step 1: FAB hint
                Icon(
                    painter = painterResource(R.drawable.ic_fab_candy),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = colors.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.onboarding_fab_hint_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.onboarding_fab_hint_message),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = colors.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        themeManager.boostIntensity(2000L, 0.5f)
                        Haptics.performSuccess(context)
                        onComplete()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text(
                        stringResource(R.string.onboarding_done),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onPrimary
                    )
                }
            }
        }
    }
}
