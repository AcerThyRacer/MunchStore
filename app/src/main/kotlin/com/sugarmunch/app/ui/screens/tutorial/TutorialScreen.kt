package com.sugarmunch.app.ui.screens.tutorial

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Interactive Tutorial System for SugarMunch
 * 
 * Features:
 * - Step-by-step interactive tutorials
 * - Feature highlights
 * - Tips & tricks
 * - Progress tracking
 * - Skip/complete options
 */
@Composable
fun TutorialScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTutorial by remember { mutableStateOf<Tutorial?>(null) }

    if (selectedTutorial != null) {
        TutorialDetailScreen(
            tutorial = selectedTutorial!!,
            onBack = { selectedTutorial = null },
            onComplete = {
                selectedTutorial = null
                onComplete()
            }
        )
    } else {
        TutorialListScreen(
            onTutorialSelected = { selectedTutorial = it },
            modifier = modifier
        )
    }
}

@Composable
private fun TutorialListScreen(
    onTutorialSelected: (Tutorial) -> Unit,
    modifier: Modifier = Modifier
) {
    val tutorials = remember { Tutorial.getAll() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Learn SugarMunch",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Master all features with our interactive tutorials",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(tutorials) { tutorial ->
            TutorialCard(
                tutorial = tutorial,
                onClick = { onTutorialSelected(tutorial) }
            )
        }
    }
}

@Composable
private fun TutorialCard(
    tutorial: Tutorial,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tutorial icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(tutorial.colorStart),
                                Color(tutorial.colorEnd)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tutorial.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Tutorial info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tutorial.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tutorial.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${tutorial.durationMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${tutorial.steps.size} steps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Arrow
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Go",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TutorialDetailScreen(
    tutorial: Tutorial,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    val progress = (currentStep + 1).toFloat() / tutorial.steps.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tutorial.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onComplete) {
                        Text("Skip")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tutorial header
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = tutorial.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                InfoChip(
                                    icon = Icons.Default.AccessTime,
                                    text = "${tutorial.durationMinutes} min"
                                )
                                InfoChip(
                                    icon = Icons.Default.Book,
                                    text = "${tutorial.steps.size} steps"
                                )
                                InfoChip(
                                    icon = Icons.Default.Star,
                                    text = "${tutorial.difficulty}/5 difficulty"
                                )
                            }
                        }
                    }
                }

                // Steps
                items(tutorial.steps.size) { index ->
                    TutorialStep(
                        step = tutorial.steps[index],
                        stepNumber = index + 1,
                        isActive = index == currentStep,
                        isCompleted = index < currentStep
                    )
                }

                // Navigation buttons
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { if (currentStep > 0) currentStep-- },
                            enabled = currentStep > 0
                        ) {
                            Text("Previous")
                        }

                        if (currentStep < tutorial.steps.size - 1) {
                            Button(
                                onClick = { currentStep++ }
                            ) {
                                Text("Next")
                            }
                        } else {
                            Button(
                                onClick = onComplete
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Complete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialStep(
    step: TutorialStep,
    stepNumber: Int,
    isActive: Boolean,
    isCompleted: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isActive) Modifier.scale(scale) else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                isActive -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isActive) {
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Step number
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isCompleted) {
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
                            )
                        } else if (isActive) {
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "$stepNumber",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Step content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isActive) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isActive) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                // Step image/illustration placeholder
                if (step.imageResId != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tip box
                if (step.tip != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = step.tip!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

// ═════════════════════════════════════════════════════════════
// TUTORIAL DATA
// ═════════════════════════════════════════════════════════════

data class Tutorial(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val colorStart: Long,
    val colorEnd: Long,
    val durationMinutes: Int,
    val difficulty: Int,
    val steps: List<TutorialStep>
) {
    companion object {
        fun getAll(): List<Tutorial> = listOf(
            Tutorial(
                id = "getting_started",
                title = "Getting Started",
                description = "Learn the basics of SugarMunch and how to use it",
                icon = Icons.Default.School,
                colorStart = 0xFFFF69B4,
                colorEnd = 0xFF9370DB,
                durationMinutes = 5,
                difficulty = 1,
                steps = listOf(
                    TutorialStep(
                        title = "Welcome to SugarMunch",
                        description = "SugarMunch is your candy-themed app store with amazing visual effects.",
                        tip = "Take your time to explore all features!"
                    ),
                    TutorialStep(
                        title = "Browse Apps",
                        description = "Discover modded APKs in our catalog. Use search and filters to find what you need.",
                        tip = "Featured apps are highlighted at the top!"
                    ),
                    TutorialStep(
                        title = "Download & Install",
                        description = "Tap the download button to get an app. Once downloaded, you can install it directly.",
                        imageResId = "download_illustration"
                    ),
                    TutorialStep(
                        title = "Enable Effects",
                        description = "Go to the Effects tab to enable visual overlays and transformations.",
                        tip = "Try SugarRush first - it's our signature effect!"
                    ),
                    TutorialStep(
                        title = "Earn Rewards",
                        description = "Claim daily rewards and earn Sugar Points by completing achievements.",
                        tip = "Check back daily for increasing rewards!"
                    )
                )
            ),
            Tutorial(
                id = "effects_guide",
                title = "Effects Guide",
                description = "Master all 26+ visual effects available in SugarMunch",
                icon = Icons.Default.BlurOn,
                colorStart = 0xFF9370DB,
                colorEnd = 0xFF20B2AA,
                durationMinutes = 10,
                difficulty = 2,
                steps = listOf(
                    TutorialStep(
                        title = "Effect Categories",
                        description = "Effects are organized into: Visual Overlays, Particles, Animations, and Haptic."
                    ),
                    TutorialStep(
                        title = "Intensity Control",
                        description = "Each effect has adjustable intensity from 0% to 200%. Find your perfect setting!"
                    ),
                    TutorialStep(
                        title = "Effect Presets",
                        description = "Use curated presets like 'Party Time', 'Focus Mode', or 'Maximum Overdrive'."
                    )
                )
            ),
            Tutorial(
                id = "themes_guide",
                title = "Themes Guide",
                description = "Customize your experience with beautiful themes",
                icon = Icons.Default.Palette,
                colorStart = 0xFF20B2AA,
                colorEnd = 0xFFFF69B4,
                durationMinutes = 8,
                difficulty = 2,
                steps = listOf(
                    TutorialStep(
                        title = "Browse Themes",
                        description = "Explore 26+ pre-made themes in different categories."
                    ),
                    TutorialStep(
                        title = "Apply Themes",
                        description = "Tap on a theme to preview it, then tap Apply to use it."
                    ),
                    TutorialStep(
                        title = "Create Custom Theme",
                        description = "Use the Theme Builder to create your own unique theme."
                    )
                )
            ),
            Tutorial(
                id = "rewards_guide",
                title = "Rewards Guide",
                description = "Maximize your Sugar Points and unlock exclusive content",
                icon = Icons.Default.CardGiftcard,
                colorStart = 0xFFFFD700,
                colorEnd = 0xFFFF69B4,
                durationMinutes = 6,
                difficulty = 1,
                steps = listOf(
                    TutorialStep(
                        title = "Daily Rewards",
                        description = "Claim rewards daily for increasing bonuses. Day 7, 14, and 30 give legendary rewards!"
                    ),
                    TutorialStep(
                        title = "Achievements",
                        description = "Complete achievements to earn Sugar Points and badges."
                    ),
                    TutorialStep(
                        title = "Sugar Shop",
                        description = "Spend your points on exclusive themes, effects, and more."
                    )
                )
            )
        )
    }
}

data class TutorialStep(
    val title: String,
    val description: String,
    val imageResId: String? = null,
    val tip: String? = null
)
