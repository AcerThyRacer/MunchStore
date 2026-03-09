package com.sugarmunch.app.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Interactive Onboarding Flow for SugarMunch
 * 
 * Features:
 * - Multi-page carousel with smooth animations
 * - Interactive feature previews
 * - Permission explanations
 * - Theme customization preview
 * - Quick start option
 */
@Composable
fun OnboardingFlow(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { OnboardingPage.values().size })
    val scope = rememberCoroutineScope()
    val showSkip = remember { mutableStateOf(true) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Skip button
            AnimatedVisibility(
                visible = showSkip.value && pagerState.currentPage < OnboardingPage.values().size - 1,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.End)
            ) {
                TextButton(onClick = onComplete) {
                    Text("Skip")
                }
            }

            // Page content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(
                    page = OnboardingPage.values()[page],
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Page indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(OnboardingPage.values().size) { index ->
                    PageIndicator(
                        isSelected = pagerState.currentPage == index,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                if (pagerState.currentPage > 0) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Next/Done button
                Button(
                    onClick = {
                        if (pagerState.currentPage < OnboardingPage.values().size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onComplete()
                        }
                    },
                    modifier = Modifier.defaultMinSize(minWidth = 120.dp)
                ) {
                    Text(
                        if (pagerState.currentPage < OnboardingPage.values().size - 1) 
                            "Next" else "Get Started"
                    )
                    if (pagerState.currentPage < OnboardingPage.values().size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// ONBOARDING PAGES
// ═════════════════════════════════════════════════════════════

enum class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val backgroundColor: @Composable () -> Long
) {
    WELCOME(
        icon = Icons.Default.Candy,
        title = "Welcome to SugarMunch",
        description = "Your candy-themed app store with amazing visual effects",
        backgroundColor = { 0xFFFF69B4 } // Hot pink
    ),
    EFFECTS(
        icon = Icons.Default.BlurOn,
        title = "Stunning Visual Effects",
        description = "Transform your screen with SugarRush, Rainbow Tint, and 20+ more effects",
        backgroundColor = { 0xFF9370DB } // Medium purple
    ),
    THEMES(
        icon = Icons.Default.Palette,
        title = "Beautiful Themes",
        description = "Customize with 26+ themes or create your own unique style",
        backgroundColor = { 0xFF20B2AA } // Light sea green
    ),
    REWARDS(
        icon = Icons.Default.CardGiftcard,
        title = "Earn Rewards",
        description = "Get Sugar Points for daily logins, achievements, and more",
        backgroundColor = { 0xFFFFD700 } // Gold
    ),
    PERMISSIONS(
        icon = Icons.Default.Security,
        title = "Permissions",
        description = "We only request permissions needed for core features",
        backgroundColor = { 0xFF87CEEB } // Sky blue
    )
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated icon
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(
                    color = androidx.compose.ui.graphics.Color(page.backgroundColor())
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .offset(y = floatY.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        // Page-specific interactive content
        Spacer(modifier = Modifier.height(32.dp))
        
        when (page) {
            OnboardingPage.WELCOME -> WelcomePreview()
            OnboardingPage.EFFECTS -> EffectsPreview()
            OnboardingPage.THEMES -> ThemesPreview()
            OnboardingPage.REWARDS -> RewardsPreview()
            OnboardingPage.PERMISSIONS -> PermissionsPreview()
        }
    }
}

// ═════════════════════════════════════════════════════════════
// INTERACTIVE PREVIEWS
// ═════════════════════════════════════════════════════════════

@Composable
private fun WelcomePreview() {
    var showEffect by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tap to see the magic! ✨",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { showEffect = !showEffect },
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(if (showEffect) "Hide Effect" else "Show Effect")
            }

            AnimatedVisibility(
                visible = showEffect,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "🍬 Sugar Rush Active! 🍬",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EffectsPreview() {
    val effects = listOf(
        "SugarRush" to Icons.Default.BlurOn,
        "Rainbow Tint" to Icons.Default.ColorLens,
        "Candy Confetti" to Icons.Default.Celebration
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Available Effects",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            effects.forEach { (name, icon) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemesPreview() {
    val themes = listOf(
        "Dark Candy" to 0xFF2D2D2D,
        "Cotton Candy" to 0xFFFFB6C1,
        "Lemon Drop" to 0xFFFFFACD
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        themes.forEach { (name, color) ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Color(color))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun RewardsPreview() {
    var points by remember { mutableStateOf(0) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Daily Reward Preview",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+$points Points",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(onClick = { points += 50 }) {
                Text("Claim Reward")
            }
        }
    }
}

@Composable
private fun PermissionsPreview() {
    val permissions = listOf(
        "Display over other apps" to "Required for effects overlay",
        "Install unknown apps" to "Needed to install downloaded APKs",
        "Notifications" to "Show download progress"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            permissions.forEach { (permission, reason) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = permission,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// PAGE INDICATOR
// ═════════════════════════════════════════════════════════════

@Composable
private fun PageIndicator(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedSize by animateDpAsState(
        targetValue = if (isSelected) 28.dp else 8.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(
        modifier = modifier
            .size(width = animatedSize, height = 8.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                }
            )
    )
}
