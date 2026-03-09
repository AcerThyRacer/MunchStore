package com.sugarmunch.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.sugarmunch.app.R
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.AnimationSettingsManager
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.model.*
import com.sugarmunch.app.theme.presets.Candy2026Themes
import com.sugarmunch.app.theme.presets.ThemePresets
import com.sugarmunch.app.ui.components.Haptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * 🍭 2026 CANDY STYLE ONBOARDING
 * Revolutionary multi-step onboarding with live theme previews,
 * animation demos, and interactive candy aesthetics
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen2026(
    onComplete: () -> Unit,
    onRequestOverlayPermission: () -> Unit = {}
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val animManager = remember { AnimationSettingsManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    val pagerState = rememberPagerState(pageCount = { 6 })
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Dynamic theme background
        AnimatedThemeBackground()
        
        // Main content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true
        ) { page ->
            when (page) {
                0 -> WelcomePage(colors, currentTheme.name)
                1 -> ThemeSelectorPage(themeManager, colors)
                2 -> AnimationPreviewPage(animManager, colors)
                3 -> FeaturesPage(colors)
                4 -> PermissionPage(colors) { showPermissionDialog = true }
                5 -> GetStartedPage(colors, onComplete)
            }
        }
        
        // Bottom indicators and controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            // Page indicators
            PageIndicators(
                pageCount = 6,
                currentPage = pagerState.currentPage,
                colors = colors
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip button (hidden on last page)
                if (pagerState.currentPage < 5) {
                    TextButton(
                        onClick = { 
                            scope.launch {
                                pagerState.animateScrollToPage(5)
                            }
                            Haptics.performTick(context)
                        }
                    ) {
                        Text(
                            "Skip",
                            color = colors.onSurface.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(64.dp))
                }
                
                // Next/Complete button
                FilledIconButton(
                    onClick = {
                        if (pagerState.currentPage < 5) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                            Haptics.performTick(context)
                        }
                    },
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = colors.primary
                    )
                ) {
                    Icon(
                        if (pagerState.currentPage < 5) 
                            Icons.Default.ArrowForward 
                        else 
                            Icons.Default.Check,
                        contentDescription = if (pagerState.currentPage < 5) "Next" else "Complete",
                        tint = colors.onPrimary
                    )
                }
            }
        }
        
        // Permission dialog
        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                icon = { Icon(Icons.Default.DisplaySettings, null) },
                title = { Text("Enable Overlay Permission") },
                text = { Text("SugarMunch needs permission to display the candy FAB over other apps. This is required for the full experience.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPermissionDialog = false
                            onRequestOverlayPermission()
                        }
                    ) {
                        Text("Open Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text("Skip for Now")
                    }
                }
            )
        }
    }
}

@Composable
private fun WelcomePage(colors: AdjustedColors, themeName: String) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated logo container
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors.primary.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing rings
            PulsingRings(colors)
            
            // Lottie animation
            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.candy_splash)
            )
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                speed = 0.8f
            )
            
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(150.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Title with gradient
        Text(
            text = "SugarMunch",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                brush = Brush.linearGradient(
                    colors = listOf(colors.primary, colors.secondary, colors.tertiary)
                )
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Tagline
        Text(
            text = "Live, Life, Love ❤",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onSurface.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Description
        Text(
            text = "Your sweet gateway to modded apps with a candy-coated experience. Swipe to customize your journey!",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Current theme badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surfaceVariant.copy(alpha = 0.8f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Palette,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Theme: $themeName",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurface
                )
            }
        }
    }
}

@Composable
private fun ThemeSelectorPage(themeManager: ThemeManager, colors: AdjustedColors) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val allThemes = remember { 
        ThemePresets.ALL_THEMES + Candy2026Themes.ALL_2026_THEMES 
    }
    
    var selectedCategory by remember { mutableStateOf<Any?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp, bottom = 120.dp, start = 24.dp, end = 24.dp)
    ) {
        Text(
            text = "Pick Your Flavor",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        
        Text(
            text = "Choose a theme that matches your vibe",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        
        // Quick category chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All") }
                )
            }
            
            // Legacy categories
            items(ThemeCategory.entries) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category.name.replace("_", " ")) }
                )
            }
            
            // 2026 categories
            items(ThemeCategory2026.entries) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category.name.replace("_", " ")) }
                )
            }
        }
        
        // Theme grid (simplified to avoid LazyVerticalGrid issues)
        val filteredThemes = when (selectedCategory) {
            is ThemeCategory -> allThemes.filter { it.category == selectedCategory }
            is ThemeCategory2026 -> Candy2026Themes.getByCategory(selectedCategory)
            else -> allThemes
        }.take(8)
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            filteredThemes.chunked(2).forEach { rowThemes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowThemes.forEach { theme ->
                        ThemePreviewCard(
                            theme = theme,
                            onClick = {
                                themeManager.setTheme(theme)
                                Haptics.performSuccess(context)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowThemes.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        
        // Random theme button
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                val randomTheme = allThemes.random()
                themeManager.setTheme(randomTheme)
                Haptics.performSuccess(context)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.primary
            )
        ) {
            Icon(Icons.Default.Shuffle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Surprise Me! 🎲")
        }
    }
}

@Composable
private fun AnimationPreviewPage(animManager: AnimationSettingsManager, colors: AdjustedColors) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp, bottom = 120.dp, start = 24.dp, end = 24.dp)
    ) {
        Text(
            text = "Feel the Motion",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        
        Text(
            text = "Choose how things move and feel",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        
        // Animation preset cards
        val presets = listOf(
            Triple("Smooth", Icons.Default.Waves, AnimationPresets.SMOOTH),
            Triple("Energetic", Icons.Default.Bolt, AnimationPresets.ENERGETIC),
            Triple("Chill", Icons.Default.AcUnit, AnimationPresets.CHILL),
            Triple("Gamer", Icons.Default.SportsEsports, AnimationPresets.GAMER),
            Triple("Minimal", Icons.Default.Minimize, AnimationPresets.MINIMAL),
            Triple("Cinematic", Icons.Default.Movie, AnimationPresets.CINEMATIC)
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            presets.chunked(2).forEach { rowPresets ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowPresets.forEach { (name, icon, preset) ->
                        AnimationPresetCard(
                            name = name,
                            icon = icon,
                            onClick = {
                                animManager.applyPreset(preset)
                                Haptics.performSuccess(context)
                            },
                            colors = colors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowPresets.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Live demo
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Live Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Demo button
                AnimatedDemoButton(colors)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Demo cards
                AnimatedDemoCards(colors)
            }
        }
    }
}

@Composable
private fun FeaturesPage(colors: AdjustedColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp, bottom = 120.dp, start = 24.dp, end = 24.dp)
    ) {
        Text(
            text = "Sweet Features",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        
        Text(
            text = "Everything you need for a candy-coated experience",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        
        // Feature cards
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCard(
                icon = Icons.Default.Apps,
                title = "App Catalog",
                description = "Browse and install modded APKs",
                color = colors.primary,
                colors = colors
            )
            
            FeatureCard(
                icon = Icons.Default.RadioButtonChecked,
                title = "Candy FAB",
                description = "Floating action button for quick access",
                color = colors.secondary,
                colors = colors
            )
            
            FeatureCard(
                icon = Icons.Default.AutoAwesome,
                title = "SugarRush Effects",
                description = "Visual overlays and haptic feedback",
                color = colors.tertiary,
                colors = colors
            )
            
            FeatureCard(
                icon = Icons.Default.Palette,
                title = "Theme Studio",
                description = "50+ themes with live customization",
                color = colors.accent,
                colors = colors
            )
        }
    }
}

@Composable
private fun PermissionPage(colors: AdjustedColors, onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp, bottom = 120.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Almost There!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Permission illustration
        Box(
            modifier = Modifier
                .size(180.dp)
                .background(
                    colors.primary.copy(alpha = 0.15f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.DisplaySettings,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = colors.primary
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "Display Over Other Apps",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "SugarMunch needs permission to show the candy FAB on top of other apps. This lets you access your favorite modded apps from anywhere!",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary
            )
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Grant Permission",
                style = MaterialTheme.typography.labelLarge
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(
            onClick = { }
        ) {
            Text(
                "I'll do this later",
                color = colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun GetStartedPage(colors: AdjustedColors, onComplete: () -> Unit) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp, bottom = 120.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ready to Munch?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Celebration animation
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors.primary.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Confetti particles
            ConfettiExplosion(colors)
            
            Icon(
                Icons.Default.Celebration,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = colors.primary
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "Your candy-coated journey begins now!",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = {
                Haptics.performSuccess(context)
                onComplete()
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(64.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            Text(
                "Start Munching! 🍬",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Live, Life, Love ❤",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface.copy(alpha = 0.5f)
        )
    }
}

// ═════════════════════════════════════════════════════════════════
// COMPONENT COMPONENTS
// ═════════════════════════════════════════════════════════════════

@Composable
private fun PulsingRings(colors: AdjustedColors) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    
    repeat(3) { index ->
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(offsetMillis = index * 600)
            ),
            label = "ring_$index"
        )
        
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(offsetMillis = index * 600)
            ),
            label = "ring_alpha_$index"
        )
        
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(scale)
                .alpha(alpha)
                .border(
                    width = 2.dp,
                    color = colors.primary,
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun PageIndicators(
    pageCount: Int,
    currentPage: Int,
    colors: AdjustedColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                animationSpec = spring(),
                label = "indicator_width"
            )
            
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(width)
                    .height(8.dp)
                    .background(
                        color = if (isSelected) colors.primary else colors.onSurface.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun ThemePreviewCard(
    theme: CandyTheme, 
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = theme.getColorsForIntensity(1f)
    
    Card(
        onClick = onClick,
        modifier = modifier
            .height(100.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = themeColors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Color dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ColorDot(themeColors.primary)
                ColorDot(themeColors.secondary)
                ColorDot(themeColors.tertiary)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = theme.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = themeColors.onSurface,
                maxLines = 1
            )
            
            Text(
                text = theme.description,
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.onSurface.copy(alpha = 0.6f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .background(color, CircleShape)
    )
}

@Composable
private fun AnimationPresetCard(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    colors: AdjustedColors,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurface
            )
        }
    }
}

@Composable
private fun AnimatedDemoButton(colors: AdjustedColors) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(),
        label = "demo_button_scale"
    )
    
    Button(
        onClick = { 
            isPressed = true
            kotlinx.coroutines.GlobalScope.launch {
                delay(150)
                isPressed = false
            }
        },
        modifier = Modifier.scale(scale),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.secondary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("Tap Me!")
    }
}

@Composable
private fun AnimatedDemoCards(colors: AdjustedColors) {
    val items = remember { List(3) { it } }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { index, _ ->
            var visible by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                delay(index * 100L)
                visible = true
            }
            
            val offsetX by animateFloatAsState(
                targetValue = if (visible) 0f else 50f,
                animationSpec = spring(),
                label = "card_offset"
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .graphicsLayer { translationX = offsetX },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.tertiary.copy(alpha = 0.2f)
                )
            ) {}
        }
    }
}

@Composable
private fun FeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    color: Color,
    colors: AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ConfettiExplosion(colors: AdjustedColors) {
    val confettiColors = listOf(
        colors.primary,
        colors.secondary,
        colors.tertiary,
        colors.accent
    )
    
    repeat(20) { index ->
        val offsetX = remember { (Math.random() * 200 - 100).toFloat() }
        val offsetY = remember { (Math.random() * 200 - 100).toFloat() }
        val color = confettiColors[index % confettiColors.size]
        val size = remember { (4..12).random().dp }
        
        Box(
            modifier = Modifier
                .offset(offsetX.dp, offsetY.dp)
                .size(size)
                .background(color, CircleShape)
        )
    }
}

// Helper extension
private fun Modifier.graphicsLayer(block: androidx.compose.ui.graphics.GraphicsLayerScope.() -> Unit): Modifier {
    return this.then(Modifier.drawBehind { })
}

// GridCells sealed class for LazyVerticalGrid
private sealed class GridCells {
    class Fixed(val count: Int) : GridCells()
}
