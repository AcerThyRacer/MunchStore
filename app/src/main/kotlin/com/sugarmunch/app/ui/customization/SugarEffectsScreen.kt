package com.sugarmunch.app.ui.customization

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.effects.SugarEffectConfig
import com.sugarmunch.app.effects.SugarEffectManager
import com.sugarmunch.app.effects.SugarEffectType

/**
 * Sugar Effects Customization Screen
 * Manage and preview all 15 sugar effects
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SugarEffectsScreen(
    onNavigateBack: () -> Unit
) {
    val effectManager = remember { SugarEffectManager.getInstance() }
    var selectedEffect by remember { mutableStateOf<SugarEffectType?>(null) }
    var showPreview by remember { mutableStateOf(false) }
    
    val allEffects = SugarEffectType.values().toList()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sugar Effects") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "15 Sugar-Specific Effects",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Activate and customize extreme candy-themed visual effects",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Effects grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allEffects.size) { index ->
                    val effect = allEffects[index]
                    val isActive = effectManager.isEffectActive(effect)
                    
                    EffectCard(
                        effect = effect,
                        isActive = isActive,
                        onToggle = {
                            if (isActive) {
                                effectManager.removeEffect(effect)
                            } else {
                                effectManager.addEffect(
                                    SugarEffectConfig(
                                        type = effect,
                                        intensity = 1f,
                                        enabled = true
                                    )
                                )
                            }
                        },
                        onPreview = {
                            selectedEffect = effect
                            showPreview = true
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        allEffects.forEach { effect ->
                            effectManager.removeEffect(effect)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Disable All")
                }
                
                Button(
                    onClick = {
                        allEffects.forEach { effect ->
                            if (!effectManager.isEffectActive(effect)) {
                                effectManager.addEffect(
                                    SugarEffectConfig(
                                        type = effect,
                                        intensity = 1f,
                                        enabled = true
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Enable All")
                }
            }
        }
    }
}

@Composable
private fun EffectCard(
    effect: SugarEffectType,
    isActive: Boolean,
    onToggle: () -> Unit,
    onPreview: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Effect icon/emoji
            Text(
                text = getEffectEmoji(effect),
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Effect name
            Text(
                text = effect.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Effect description
            Text(
                text = effect.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Toggle switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.labelSmall
                )
                
                Switch(
                    checked = isActive,
                    onCheckedChange = { onToggle() }
                )
            }
            
            // Preview button
            TextButton(
                onClick = onPreview,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Preview")
            }
        }
    }
}

private fun getEffectEmoji(effect: SugarEffectType): String {
    return when (effect) {
        SugarEffectType.CANDY_RAIN -> "🍬"
        SugarEffectType.CHOCOLATE_FOUNTAIN -> "🍫"
        SugarEffectType.CARAMEL_DRIZZLE -> "🍯"
        SugarEffectType.SPRINKLES_EXPLOSION -> "🎊"
        SugarEffectType.COTTON_CANDY_CLOUD -> "☁️"
        SugarEffectType.LOLLIPOP_SPIN -> "🍭"
        SugarEffectType.GUMMY_WOBBLE -> "🧸"
        SugarEffectType.SUGAR_RUSH_BLUR -> "⚡"
        SugarEffectType.CANDY_TRANSFORM -> "✨"
        SugarEffectType.SWEET_GLOW -> "💫"
        SugarEffectType.BUBBLE_POP -> "🫧"
        SugarEffectType.RAINBOW_WAVE -> "🌈"
        SugarEffectType.FIZZY_BUBBLES -> "🥤"
        SugarEffectType.CANDY_CRUSH -> "🎮"
        SugarEffectType.SUGAR_HIGH -> "🤯"
    }
}
