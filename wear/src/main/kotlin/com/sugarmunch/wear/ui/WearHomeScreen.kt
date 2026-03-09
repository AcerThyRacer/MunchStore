package com.sugarmunch.wear.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import com.sugarmunch.wear.data.WearStats

/**
 * Wear OS Home Screen
 */
@Composable
fun WearHomeScreen(
    onNavigateToEffects: () -> Unit,
    onNavigateToThemes: () -> Unit,
    onNavigateToStats: () -> Unit,
    viewModel: WearViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            item {
                Text(
                    text = "🍭 SugarMunch",
                    style = MaterialTheme.typography.title3,
                    textAlign = TextAlign.Center
                )
            }

            // Quick Stats
            item {
                WearStatsCard(stats = uiState.stats)
            }

            // Quick Actions
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.caption2,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            item {
                WearActionChip(
                    icon = "⚡",
                    label = "Effects",
                    onClick = onNavigateToEffects
                )
            }

            item {
                WearActionChip(
                    icon = "🎨",
                    label = "Themes",
                    onClick = onNavigateToThemes
                )
            }

            item {
                WearActionChip(
                    icon = "📊",
                    label = "Stats",
                    onClick = onNavigateToStats
                )
            }

            // Daily Reward Quick Claim
            if (uiState.canClaimReward) {
                item {
                    Chip(
                        onClick = { viewModel.claimDailyReward() },
                        label = { Text("🎁 Claim Reward!") },
                        secondaryLabel = { Text("${uiState.rewardAmount} points") },
                        colors = ChipDefaults.secondaryChipColors()
                    )
                }
            }

            // Active Effect
            if (uiState.activeEffect.isNotEmpty()) {
                item {
                    Text(
                        text = "Active Effect",
                        style = MaterialTheme.typography.caption2,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                item {
                    Chip(
                        onClick = { viewModel.toggleActiveEffect() },
                        label = { Text(uiState.activeEffect) },
                        secondaryLabel = { Text("Tap to toggle") },
                        colors = ChipDefaults.primaryChipColors()
                    )
                }
            }
        }
    }
}

@Composable
fun WearStatsCard(stats: WearStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Today's Progress",
                style = MaterialTheme.typography.caption2
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Installs", stats.installs.toString())
                StatItem("Effects", stats.effects.toString())
                StatItem("Points", stats.points.toString())
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.title3,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.caption3,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun WearActionChip(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Chip(
        onClick = onClick,
        label = { Text(label) },
        icon = {
            Text(
                text = icon,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape),
                textAlign = TextAlign.Center
            )
        },
        colors = ChipDefaults.primaryChipColors()
    )
}

@Composable
fun WearEffectsScreen(
    onBack: () -> Unit
) {
    val effects = listOf(
        "SugarRush" to "🚀",
        "Rainbow Tint" to "🌈",
        "Candy Confetti" to "🎊",
        "Heartbeat" to "💓",
        "Dragon Breath" to "🐉",
        "Stardust Trail" to "✨"
    )

    Scaffold(
        title = { Text("Effects") },
        timeText = { TimeText() }
    ) {
        ScalingLazyColumn {
            items(effects) { (name, icon) ->
                EffectToggleItem(name = name, icon = icon)
            }
            item {
                Chip(
                    onClick = onBack,
                    label = { Text("← Back") }
                )
            }
        }
    }
}

@Composable
fun EffectToggleItem(name: String, icon: String) {
    var enabled by remember { mutableStateOf(false) }
    
    Chip(
        onClick = { enabled = !enabled },
        label = { Text(name) },
        icon = { Text(icon) },
        colors = if (enabled) {
            ChipDefaults.primaryChipColors()
        } else {
            ChipDefaults.secondaryChipColors()
        }
    )
}

@Composable
fun WearThemesScreen(
    onBack: () -> Unit
) {
    val themes = listOf(
        "Classic Candy" to "🍬",
        "SugarRush" to "🚀",
        "Trippy Rainbow" to "🌈",
        "Dark Chocolate" to "🍫",
        "Golden Candy" to "🌟",
        "Cyber Punk" to "🌆"
    )

    Scaffold(
        title = { Text("Themes") },
        timeText = { TimeText() }
    ) {
        ScalingLazyColumn {
            items(themes) { (name, icon) ->
                ThemeSelectItem(name = name, icon = icon)
            }
            item {
                Chip(
                    onClick = onBack,
                    label = { Text("← Back") }
                )
            }
        }
    }
}

@Composable
fun ThemeSelectItem(name: String, icon: String) {
    Chip(
        onClick = { /* Apply theme */ },
        label = { Text(name) },
        icon = { Text(icon) },
        colors = ChipDefaults.secondaryChipColors()
    )
}

@Composable
fun WearStatsScreen(
    onBack: () -> Unit
) {
    Scaffold(
        title = { Text("Statistics") },
        timeText = { TimeText() }
    ) {
        ScalingLazyColumn {
            item {
                StatRow("Total Installs", "47")
            }
            item {
                StatRow("Effects Used", "23")
            }
            item {
                StatRow("Themes Tried", "12")
            }
            item {
                StatRow("Sugar Points", "2,450")
            }
            item {
                StatRow("Current Streak", "7 days 🔥")
            }
            item {
                StatRow("Level", "15")
            }
            item {
                StatRow("Achievements", "28/50")
            }
            item {
                Chip(
                    onClick = onBack,
                    label = { Text("← Back") }
                )
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.caption2
            )
            Text(
                text = value,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
