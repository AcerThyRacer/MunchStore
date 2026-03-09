package com.sugarmunch.wear.presentation.effects

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.SwitchDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.dialog.Alert
import com.google.android.horologist.compose.layout.fadeAway
import com.sugarmunch.wear.data.WearDataLayer
import kotlinx.coroutines.launch

/**
 * Effect Control Screen - Manage all effects from watch
 */
@Composable
fun EffectControlScreen(
    navController: NavHostController,
    wearDataLayer: WearDataLayer
) {
    val effectStates by wearDataLayer.effectStates.collectAsState()
    val listState = rememberScalingLazyListState()
    val scope = rememberCoroutineScope()
    var showMasterOffDialog by remember { mutableStateOf(false) }

    val effectsByCategory = remember {
        mapOf(
            "Visual" to listOf(
                EffectInfo("sugarrush_overlay", "SugarRush", "🌈", "Visual overlay effect"),
                EffectInfo("rainbow_tint", "Rainbow Tint", "🌈", "Colorful screen tint"),
                EffectInfo("mint_wash", "Mint Wash", "🌿", "Fresh mint overlay"),
                EffectInfo("caramel_dim", "Caramel Dim", "🍮", "Warm dimming effect"),
                EffectInfo("chocolate_darkness", "Choco Dark", "🍫", "Dark chocolate overlay"),
                EffectInfo("lollipop_glow", "Lollipop", "🍭", "Sweet glow effect")
            ),
            "Particles" to listOf(
                EffectInfo("candy_confetti", "Confetti", "🎊", "Falling candy pieces"),
                EffectInfo("chocolate_rain", "Choco Rain", "🍫", "Chocolate drops"),
                EffectInfo("gummy_wiggle", "Gummy", "🐻", "Wiggling gummies"),
                EffectInfo("pop_rocks", "Pop Rocks", "💥", "Explosive particles"),
                EffectInfo("candy_fireworks", "Fireworks", "🎆", "Celebration bursts")
            ),
            "Animations" to listOf(
                EffectInfo("unicorn_swirl", "Unicorn", "🦄", "Magical swirls"),
                EffectInfo("ice_crystal", "Ice Crystal", "❄️", "Freezing animation"),
                EffectInfo("cinnamon_fire", "Cinnamon", "🔥", "Spicy fire effect")
            ),
            "Haptic" to listOf(
                EffectInfo("heartbeat_haptic", "Heartbeat", "💓", "Rhythmic pulses"),
                EffectInfo("gummy_bounce", "Bounce", "🏀", "Bouncy feedback"),
                EffectInfo("crunch_haptic", "Crunch", "🍪", "Crunchy vibrations"),
                EffectInfo("fizzy_soda", "Fizzy", "🥤", "Bubbly haptics")
            )
        )
    }

    val activeCount = effectStates.count { it.value.isActive }
    val allEffectsOff = activeCount == 0

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
            verticalArrangement = Arrangement.spacedBy(6.dp),
            anchorType = ScalingLazyListAnchorType.ItemStart
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✨",
                        style = MaterialTheme.typography.display3
                    )
                    Text(
                        text = "Effects",
                        style = MaterialTheme.typography.title3,
                        color = MaterialTheme.colors.primary
                    )
                    Text(
                        text = "$activeCount active",
                        style = MaterialTheme.typography.caption3,
                        color = MaterialTheme.colors.onSurfaceVariant
                    )
                }
            }

            // Master Switch
            item {
                MasterToggleChip(
                    checked = !allEffectsOff,
                    onCheckedChange = { checked ->
                        if (!checked) {
                            showMasterOffDialog = true
                        }
                    },
                    activeCount = activeCount
                )
            }

            // Category sections
            effectsByCategory.forEach { (category, effects) ->
                item {
                    CategoryHeader(category)
                }

                items(effects, key = { it.id }) { effect ->
                    val state = effectStates[effect.id]
                    EffectToggleChip(
                        effect = effect,
                        isActive = state?.isActive ?: false,
                        intensity = state?.intensity ?: 1f,
                        onToggle = { enabled ->
                            scope.launch {
                                wearDataLayer.sendToggleEffect(effect.id, enabled)
                            }
                        }
                    )
                }
            }

            // Bottom spacer
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Master Off Confirmation Dialog
    if (showMasterOffDialog) {
        Alert(
            title = { Text("Turn Off All?") },
            message = { Text("This will disable all active effects.") },
            onDismiss = { showMasterOffDialog = false }
        ) {
            item {
                Button(
                    onClick = {
                        scope.launch {
                            wearDataLayer.sendAllOff()
                        }
                        showMasterOffDialog = false
                    },
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Text("Turn Off")
                }
            }
            item {
                Button(
                    onClick = { showMasterOffDialog = false },
                    colors = ButtonDefaults.primaryButtonColors()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

/**
 * Effect info data class
 */
data class EffectInfo(
    val id: String,
    val name: String,
    val icon: String,
    val description: String
)

/**
 * Category header
 */
@Composable
fun CategoryHeader(category: String) {
    val (icon, color) = when (category) {
        "Visual" -> "👁️" to MaterialTheme.colors.primary
        "Particles" -> "✨" to MaterialTheme.colors.secondary
        "Animations" -> "🎬" to MaterialTheme.colors.tertiary
        "Haptic" -> "📳" to MaterialTheme.colors.error
        else -> "🔹" to MaterialTheme.colors.primary
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.body2
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = category,
            style = MaterialTheme.typography.button,
            color = color
        )
    }
}

/**
 * Master toggle chip for all effects
 */
@Composable
fun MasterToggleChip(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    activeCount: Int
) {
    ToggleChip(
        checked = checked,
        onCheckedChange = onCheckedChange,
        label = {
            Text(
                text = if (checked) "Effects On" else "All Off",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        toggleControl = {
            Switch(
                checked = checked,
                modifier = Modifier.size(24.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colors.primary
                )
            )
        },
        secondaryLabel = {
            Text(
                text = "$activeCount active effects",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        appIcon = {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (checked) "⚡" else "😴",
                    style = MaterialTheme.typography.body1
                )
            }
        },
        colors = ToggleChipDefaults.toggleChipColors(
            checkedStartBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.3f),
            checkedEndBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f)
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Individual effect toggle chip
 */
@Composable
fun EffectToggleChip(
    effect: EffectInfo,
    isActive: Boolean,
    intensity: Float,
    onToggle: (Boolean) -> Unit
) {
    ToggleChip(
        checked = isActive,
        onCheckedChange = onToggle,
        label = {
            Text(
                text = effect.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        toggleControl = {
            Switch(
                checked = isActive,
                modifier = Modifier.size(24.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colors.primary
                )
            )
        },
        secondaryLabel = if (isActive && intensity != 1f) {
            {
                Text(
                    text = "Intensity: ${(intensity * 100).toInt()}%",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else null,
        appIcon = {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = effect.icon)
            }
        },
        colors = if (isActive) {
            ToggleChipDefaults.toggleChipColors(
                checkedStartBackgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.25f),
                checkedEndBackgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.1f)
            )
        } else {
            ToggleChipDefaults.toggleChipColors()
        },
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Effect detail card (for future expansion)
 */
@Composable
fun EffectDetailCard(
    effect: EffectInfo,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        backgroundPainter = if (isActive) {
            androidx.wear.compose.material.CardDefaults.cardBackgroundPainter(
                startBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.2f),
                endBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.05f)
            )
        } else {
            androidx.wear.compose.material.CardDefaults.cardBackgroundPainter()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = effect.icon,
                style = MaterialTheme.typography.title3
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = effect.name,
                    style = MaterialTheme.typography.button
                )
                Text(
                    text = effect.description,
                    style = MaterialTheme.typography.caption3,
                    color = MaterialTheme.colors.onSurfaceVariant
                )
            }
            if (isActive) {
                Text(
                    text = "●",
                    color = MaterialTheme.colors.primary,
                    style = MaterialTheme.typography.title2
                )
            }
        }
    }
}
