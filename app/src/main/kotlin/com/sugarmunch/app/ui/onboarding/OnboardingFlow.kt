package com.sugarmunch.app.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.model.CandyTheme
import com.sugarmunch.app.theme.presets.ThemePresets

/**
 * Onboarding Flow for New Users
 * 
 * Steps:
 * 1. Welcome to SugarMunch
 * 2. Theme Selection (preview 3 themes)
 * 3. Haptic Feedback Test
 * 4. Gesture Tutorial
 * 5. Folder Creation Demo
 * 6. App Store Introduction
 * 7. Done - Show Home Screen
 */
@Composable
fun OnboardingFlow(
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)

    val pagerState = rememberPagerState(pageCount = { 7 })
    var selectedTheme by remember { mutableStateOf<CandyTheme>(ThemePresets.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Animated background
        AnimatedThemeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onSkip) {
                    Text("Skip")
                }
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage(colors = colors)
                    1 -> ThemeSelectionPage(
                        selectedTheme = selectedTheme,
                        onThemeSelected = { selectedTheme = it },
                        colors = colors
                    )
                    2 -> HapticFeedbackPage(
                        onTestHaptic = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        colors = colors
                    )
                    3 -> GestureTutorialPage(colors = colors)
                    4 -> FolderCreationDemoPage(colors = colors)
                    5 -> AppStoreIntroPage(colors = colors)
                    6 -> DonePage(colors = colors)
                }
            }

            // Page indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(7) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) colors.primary else colors.onSurface.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back button
                OutlinedButton(
                    onClick = {
                        if (pagerState.currentPage > 0) {
                            haptic.performHapticFeedback(HapticFeedbackType.GestureEnd)
                        }
                    },
                    enabled = pagerState.currentPage > 0,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Back")
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Next/Complete button
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.GestureEnd)
                        if (pagerState.currentPage < 6) {
                            // Navigate to next page
                        } else {
                            // Complete onboarding
                            themeManager.setTheme(selectedTheme)
                            onComplete()
                        }
                    },
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (pagerState.currentPage < 6) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    } else {
                        Text("Get Started")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            }
        }
    }
}

/**
 * Welcome Page
 */
@Composable
private fun WelcomePage(colors: com.sugarmunch.app.theme.model.AdjustedColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated icon
        var scale by remember { mutableStateOf(1f) }
        LaunchedEffect(Unit) {
            while (true) {
                scale = 1.1f
                kotlinx.coroutines.delay(500)
                scale = 1f
                kotlinx.coroutines.delay(500)
            }
        }

        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .background(
                    Brush.radialGradient(
                        listOf(colors.primary, colors.secondary)
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Candy,
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to SugarMunch",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your personalized app launcher with beautiful themes, haptic feedback, and powerful organization features.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Theme Selection Page
 */
@Composable
private fun ThemeSelectionPage(
    selectedTheme: CandyTheme,
    onThemeSelected: (CandyTheme) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Choose Your Theme",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )

        Text(
            text = "Pick a theme that matches your style",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Theme preview cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(ThemePresets.ALL_THEMES_2026.take(5)) { theme ->
                ThemePreviewCard(
                    theme = theme,
                    isSelected = selectedTheme.id == theme.id,
                    onSelected = { onThemeSelected(theme) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Selected theme preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = selectedTheme.getColorsForIntensity(1f).surface
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            selectedTheme.getBackgroundGradient(1f).colors
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = selectedTheme.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = selectedTheme.getColorsForIntensity(1f).onSurface
                    )
                    Text(
                        text = selectedTheme.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = selectedTheme.getColorsForIntensity(1f).onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        }
    }
}

/**
 * Theme Preview Card
 */
@Composable
private fun ThemePreviewCard(
    theme: CandyTheme,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val colors = theme.getColorsForIntensity(1f)

    Card(
        onClick = onSelected,
        modifier = Modifier
            .width(100.dp)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                width = 3.dp,
                brush = Brush.linearGradient(listOf(colors.primary, colors.secondary))
            )
        } else {
            null
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(colors.themeGradient.colors)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = theme.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

/**
 * Haptic Feedback Page
 */
@Composable
private fun HapticFeedbackPage(
    onTestHaptic: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Vibration,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = colors.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Feel the Feedback",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SugarMunch provides rich haptic feedback for all your interactions. Tap the button below to feel it!",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onTestHaptic,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.TouchApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Test Haptic")
        }
    }
}

/**
 * Gesture Tutorial Page
 */
@Composable
private fun GestureTutorialPage(colors: com.sugarmunch.app.theme.model.AdjustedColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Master the Gestures",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Gesture examples
        listOf(
            "Swipe Up" to "Open App Drawer",
            "Swipe Down" to "Quick Settings",
            "Double Tap" to "Lock Screen",
            "Long Press" to "Custom Action",
            "Pinch" to "Widget Picker"
        ).forEach { (gesture, action) ->
            GestureRow(gesture, action, colors)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "You can customize all gestures in Settings",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun GestureRow(gesture: String, action: String, colors: com.sugarmunch.app.theme.model.AdjustedColors) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = gesture,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = colors.primary
            )
            Text(
                text = action,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface
            )
        }
    }
}

/**
 * Folder Creation Demo Page
 */
@Composable
private fun FolderCreationDemoPage(colors: com.sugarmunch.app.theme.model.AdjustedColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = colors.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Organize with Folders",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Drag apps onto each other to create folders. Organize by category, usage, or your own system.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Visual demo
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // App 1
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(colors.primary, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Apps,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Plus sign
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.onSurface
            )

            // App 2
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(colors.secondary, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = colors.onSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Arrow
            Text(
                text = "→",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.onSurface
            )

            // Folder
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(colors.tertiary, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    tint = colors.onTertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * App Store Intro Page
 */
@Composable
private fun AppStoreIntroPage(colors: com.sugarmunch.app.theme.model.AdjustedColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = colors.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Plugin Store",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Discover and install plugins to extend SugarMunch with new themes, effects, and features.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Feature list
        listOf(
            "Custom themes and effects",
            "Utility plugins",
            "App integrations",
            "Community creations"
        ).forEach { feature ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface
                )
            }
        }
    }
}

/**
 * Done Page
 */
@Composable
private fun DonePage(colors: com.sugarmunch.app.theme.model.AdjustedColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Confetti-like celebration
        var scale by remember { mutableStateOf(0f) }
        LaunchedEffect(Unit) {
            scale = 1f
        }

        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(scale)
                .background(
                    Brush.radialGradient(
                        listOf(colors.primary, colors.secondary, colors.tertiary)
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Celebration,
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "You're All Set!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to SugarMunch! Let's start customizing your perfect launcher experience.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "You can always change settings later",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface.copy(alpha = 0.5f)
        )
    }
}
