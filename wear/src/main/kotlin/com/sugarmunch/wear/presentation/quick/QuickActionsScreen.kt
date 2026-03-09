package com.sugarmunch.wear.presentation.quick

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.InlineSlider
import androidx.wear.compose.material.InlineSliderDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.dialog.Alert
import com.google.android.horologist.compose.layout.fadeAway
import com.sugarmunch.wear.data.WearDataLayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Quick Actions Screen - Presets and boost controls
 */
@Composable
fun QuickActionsScreen(
    navController: NavHostController,
    wearDataLayer: WearDataLayer
) {
    val boostMode by wearDataLayer.boostMode.collectAsState()
    val activeEffectCount by wearDataLayer.activeEffectCount.collectAsState()
    val listState = rememberScalingLazyListState()
    val scope = rememberCoroutineScope()
    var showPresetDialog by remember { mutableStateOf<String?>(null) }
    var isApplying by remember { mutableStateOf(false) }

    Scaffold(
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        timeText = { 
            TimeText(
                modifier = Modifier.fadeAway { listState }
            ) 
        }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            anchorType = ScalingLazyListAnchorType.ItemStart
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚡",
                        style = MaterialTheme.typography.display3
                    )
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.title3,
                        color = MaterialTheme.colors.primary
                    )
                    if (activeEffectCount > 0) {
                        Text(
                            text = "$activeEffectCount effects active",
                            style = MaterialTheme.typography.caption3,
                            color = MaterialTheme.colors.secondary
                        )
                    }
                }
            }

            // Presets Section
            item {
                SectionHeader("Presets", "🎯")
            }

            item {
                PresetButton(
                    name = "Chill",
                    icon = "😌",
                    description = "Relaxed & calm",
                    colors = listOf(Color(0xFF98D8C8), Color(0xFFB8E0D2)),
                    onClick = { showPresetDialog = "chill" }
                )
            }

            item {
                PresetButton(
                    name = "Focus",
                    icon = "🎯",
                    description = "Minimal distractions",
                    colors = listOf(Color(0xFF4169E1), Color(0xFF87CEEB)),
                    onClick = { showPresetDialog = "focus" }
                )
            }

            item {
                PresetButton(
                    name = "Party",
                    icon = "🎉",
                    description = "Maximum fun!",
                    colors = listOf(Color(0xFFFF1493), Color(0xFFFFD700), Color(0xFF00CED1)),
                    onClick = { showPresetDialog = "party" }
                )
            }

            item {
                PresetButton(
                    name = "Gaming",
                    icon = "🎮",
                    description = "High energy",
                    colors = listOf(Color(0xFF39FF14), Color(0xFF00FFFF), Color(0xFFFF00FF)),
                    onClick = { showPresetDialog = "gaming" }
                )
            }

            // Boost Mode Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("Boost", "🚀")
            }

            item {
                BoostModeCard(
                    enabled = boostMode,
                    onToggle = { enabled ->
                        scope.launch {
                            wearDataLayer.sendSetBoostMode(enabled)
                        }
                    }
                )
            }

            // Quick Controls
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("Controls", "🎛️")
            }

            item {
                QuickActionButton(
                    text = if (isApplying) "Applying..." else "Install Favorites",
                    icon = "⭐",
                    onClick = {
                        scope.launch {
                            isApplying = true
                            wearDataLayer.sendApplyPreset("favorites")
                            delay(1000)
                            isApplying = false
                        }
                    },
                    enabled = !isApplying
                )
            }

            item {
                QuickActionButton(
                    text = "Random Theme",
                    icon = "🎲",
                    onClick = {
                        scope.launch {
                            wearDataLayer.sendApplyPreset("random_theme")
                        }
                    }
                )
            }

            item {
                QuickActionButton(
                    text = "Reset All",
                    icon = "🔄",
                    onClick = {
                        scope.launch {
                            wearDataLayer.sendAllOff()
                        }
                    },
                    colors = ButtonDefaults.secondaryButtonColors()
                )
            }

            // Bottom spacer
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Preset confirmation dialog
    showPresetDialog?.let { preset ->
        val (title, description, icon) = when (preset) {
            "chill" -> Triple("Chill Mode", "Relaxed effects for calm moments", "😌")
            "focus" -> Triple("Focus Mode", "Minimal effects for productivity", "🎯")
            "party" -> Triple("Party Mode", "Maximum effects for fun!", "🎉")
            "gaming" -> Triple("Gaming Mode", "High energy effects", "🎮")
            else -> Triple("Preset", "Apply preset?", "🎯")
        }

        Alert(
            title = { Text(icon + " " + title) },
            message = { Text(description) },
            onDismiss = { showPresetDialog = null }
        ) {
            item {
                Button(
                    onClick = {
                        scope.launch {
                            wearDataLayer.sendApplyPreset(preset)
                        }
                        showPresetDialog = null
                    },
                    colors = ButtonDefaults.primaryButtonColors()
                ) {
                    Text("Apply")
                }
            }
            item {
                Button(
                    onClick = { showPresetDialog = null },
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

/**
 * Section header
 */
@Composable
fun SectionHeader(title: String, icon: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.body2
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.button,
            color = MaterialTheme.colors.primary
        )
    }
}

/**
 * Preset button with gradient
 */
@Composable
fun PresetButton(
    name: String,
    icon: String,
    description: String,
    colors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        backgroundPainter = androidx.wear.compose.material.CardDefaults.cardBackgroundPainter(
            startBackgroundColor = colors.first().copy(alpha = 0.3f),
            endBackgroundColor = colors.last().copy(alpha = 0.15f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(colors)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.title3
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.button
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.caption3,
                    color = MaterialTheme.colors.onSurfaceVariant
                )
            }
            Text(
                text = "▶",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.primary
            )
        }
    }
}

/**
 * Boost mode toggle card
 */
@Composable
fun BoostModeCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (enabled) 1.05f else 1f,
        label = "boost_scale"
    )

    Card(
        onClick = { onToggle(!enabled) },
        backgroundPainter = if (enabled) {
            androidx.wear.compose.material.CardDefaults.cardBackgroundPainter(
                startBackgroundColor = Color(0xFFFF1493).copy(alpha = 0.4f),
                endBackgroundColor = Color(0xFFFFD700).copy(alpha = 0.2f)
            )
        } else {
            androidx.wear.compose.material.CardDefaults.cardBackgroundPainter()
        },
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (enabled) {
                            Brush.linearGradient(
                                listOf(Color(0xFFFF1493), Color(0xFFFFD700))
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colors.onSurfaceVariant.copy(alpha = 0.3f),
                                    MaterialTheme.colors.onSurfaceVariant.copy(alpha = 0.1f)
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (enabled) "🚀" else "🐢",
                    style = MaterialTheme.typography.title3
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Boost Mode",
                    style = MaterialTheme.typography.button
                )
                Text(
                    text = if (enabled) "MAXIMUM POWER!" else "Normal intensity",
                    style = MaterialTheme.typography.caption3,
                    color = if (enabled) Color(0xFFFF1493) else MaterialTheme.colors.onSurfaceVariant
                )
            }
            SwitchIndicator(enabled)
        }
    }
}

/**
 * Simple switch indicator
 */
@Composable
fun SwitchIndicator(enabled: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (enabled) MaterialTheme.colors.primary
                else MaterialTheme.colors.onSurfaceVariant.copy(alpha = 0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) Color.White
                    else MaterialTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                )
        )
    }
}

/**
 * Quick action button
 */
@Composable
fun QuickActionButton(
    text: String,
    icon: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    colors: androidx.wear.compose.material.ButtonColors = ButtonDefaults.primaryButtonColors()
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.body1
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text)
        }
    }
}

/**
 * Intensity slider (for future use)
 */
@Composable
fun IntensitySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Intensity: ${(value * 100).toInt()}%",
            style = MaterialTheme.typography.caption3,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        InlineSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..2f,
            steps = 19,
            modifier = Modifier.fillMaxWidth(),
            decreaseIcon = { Text("-") },
            increaseIcon = { Text("+") },
            colors = InlineSliderDefaults.colors(
                selectedBarColor = MaterialTheme.colors.primary,
                unselectedBarColor = MaterialTheme.colors.onSurfaceVariant.copy(alpha = 0.3f)
            )
        )
    }
}

/**
 * Quick preset chips row (for compact layouts)
 */
@Composable
fun QuickPresetRow(
    onPresetSelected: (String) -> Unit
) {
    val presets = listOf(
        "chill" to "😌",
        "focus" to "🎯",
        "party" to "🎉",
        "gaming" to "🎮"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        presets.forEach { (preset, icon) ->
            Button(
                onClick = { onPresetSelected(preset) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.secondaryButtonColors()
            ) {
                Text(icon)
            }
        }
    }
}
