package com.sugarmunch.app.effects.v2.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.effects.v2.engine.EffectEngineV2
import com.sugarmunch.app.effects.v2.model.*
import com.sugarmunch.app.effects.v2.presets.EffectPresets
import com.sugarmunch.app.effects.v2.presets.EffectRegistry
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectsScreenV2(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val effectEngine = remember { EffectEngineV2.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    val allEffects by effectEngine.allEffects.collectAsState()
    val activeEffects by effectEngine.activeEffects.collectAsState()
    val masterIntensity by effectEngine.masterIntensity.collectAsState()
    val currentPreset by effectEngine.currentPreset.collectAsState()
    val hapticsEnabled by effectEngine.hapticsEnabled.collectAsState()
    
    var selectedCategory by remember { mutableStateOf<EffectCategory?>(null) }
    var showIntensityPanel by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Effects Studio")
                        Text(
                            "${activeEffects.size} active • Master: ${(masterIntensity * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showIntensityPanel = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Adjust Intensity")
                    }
                    IconButton(onClick = { effectEngine.clearAllEffects() }) {
                        Icon(Icons.Default.ClearAll, contentDescription = "Clear All")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedThemeBackground()
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick Presets
                item {
                    QuickPresetsSection(
                        currentPreset = currentPreset,
                        colors = colors,
                        onPresetSelected = { preset ->
                            effectEngine.applyPreset(preset)
                        }
                    )
                }
                
                // Active Effects Card
                if (activeEffects.isNotEmpty()) {
                    item {
                        ActiveEffectsCard(
                            activeEffects = activeEffects,
                            colors = colors,
                            onEffectIntensityChange = { effectId, intensity ->
                                effectEngine.setEffectIntensity(effectId, intensity)
                            },
                            onDisableEffect = { effectId ->
                                effectEngine.disableEffect(effectId)
                            }
                        )
                    }
                }
                
                // Category Filter
                item {
                    CategoryFilterChips(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        colors = colors
                    )
                }
                
                // Effects Grid
                val displayEffects = if (selectedCategory != null) {
                    allEffects.filter { it.category == selectedCategory }
                } else {
                    allEffects
                }
                
                items(displayEffects.chunked(2)) { rowEffects ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowEffects.forEach { effect ->
                            EffectCard(
                                effect = effect,
                                isActive = activeEffects.containsKey(effect.id),
                                intensity = activeEffects[effect.id]?.currentIntensity ?: effect.defaultIntensity,
                                onToggle = {
                                    effectEngine.toggleEffect(effect.id, effect.defaultIntensity)
                                },
                                onIntensityChange = { intensity ->
                                    if (activeEffects.containsKey(effect.id)) {
                                        effectEngine.setEffectIntensity(effect.id, intensity)
                                    }
                                },
                                colors = colors,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowEffects.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                
                // Bottom spacer
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            
            // Intensity Control Panel
            AnimatedVisibility(
                visible = showIntensityPanel,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                MasterIntensityPanel(
                    masterIntensity = masterIntensity,
                    hapticsEnabled = hapticsEnabled,
                    onMasterIntensityChange = { effectEngine.setMasterIntensity(it) },
                    onHapticsToggle = { effectEngine.setHapticsEnabled(it) },
                    onBoost = { effectEngine.boostAll(durationMs = 3000, boostAmount = 0.5f) },
                    onDismiss = { showIntensityPanel = false },
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun QuickPresetsSection(
    currentPreset: EffectPreset?,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onPresetSelected: (EffectPreset) -> Unit
) {
    Column {
        Text(
            text = "Quick Presets",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(EffectPresets.getQuickPresets()) { preset ->
                PresetChip(
                    preset = preset,
                    isSelected = currentPreset?.id == preset.id,
                    onClick = { onPresetSelected(preset) },
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun PresetChip(
    preset: EffectPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val backgroundColor = if (isSelected) {
        colors.primary
    } else {
        colors.surface.copy(alpha = 0.8f)
    }
    
    val contentColor = if (isSelected) {
        colors.onPrimary
    } else {
        colors.onSurface
    }
    
    Surface(
        onClick = onClick,
        modifier = Modifier.height(64.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = preset.icon,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = preset.name,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun ActiveEffectsCard(
    activeEffects: Map<String, EffectState>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onEffectIntensityChange: (String, Float) -> Unit,
    onDisableEffect: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Active Effects",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            activeEffects.values.forEach { state ->
                ActiveEffectRow(
                    state = state,
                    colors = colors,
                    onIntensityChange = { onEffectIntensityChange(state.effect.id, it) },
                    onDisable = { onDisableEffect(state.effect.id) }
                )
                
                if (state.effect.id != activeEffects.keys.lastOrNull()) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = colors.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveEffectRow(
    state: EffectState,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onIntensityChange: (Float) -> Unit,
    onDisable: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = state.effect.icon,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(end = 8.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.effect.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface
            )
            
            Slider(
                value = state.currentIntensity,
                onValueChange = onIntensityChange,
                valueRange = state.effect.minIntensity..state.effect.maxIntensity,
                steps = ((state.effect.maxIntensity - state.effect.minIntensity) * 10).toInt() - 1,
                colors = SliderDefaults.colors(
                    thumbColor = colors.primary,
                    activeTrackColor = colors.primary,
                    inactiveTrackColor = colors.surfaceVariant
                ),
                modifier = Modifier.height(32.dp)
            )
        }
        
        Text(
            text = "${(state.currentIntensity * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = colors.primary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        IconButton(onClick = onDisable) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Disable",
                tint = colors.error
            )
        }
    }
}

@Composable
private fun CategoryFilterChips(
    selectedCategory: EffectCategory?,
    onCategorySelected: (EffectCategory?) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All") },
                leadingIcon = if (selectedCategory == null) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null
            )
            
            EffectCategory.values().forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.name.replace("_", " ")) },
                    leadingIcon = if (selectedCategory == category) {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun EffectCard(
    effect: EffectV2,
    isActive: Boolean,
    intensity: Float,
    onToggle: () -> Unit,
    onIntensityChange: (Float) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Card(
        onClick = onToggle,
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                colors.primary.copy(alpha = 0.2f)
            } else {
                colors.surface.copy(alpha = 0.95f)
            }
        ),
        border = if (isActive) {
            androidx.compose.foundation.BorderStroke(2.dp, colors.primary)
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Preview
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                effect.Preview(intensity = if (isActive) intensity else effect.defaultIntensity)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = effect.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            
            Text(
                text = effect.icon,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.6f)
            )
            
            if (isActive) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = intensity / effect.maxIntensity,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = colors.primary,
                    trackColor = colors.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MasterIntensityPanel(
    masterIntensity: Float,
    hapticsEnabled: Boolean,
    onMasterIntensityChange: (Float) -> Unit,
    onHapticsToggle: (Boolean) -> Unit,
    onBoost: () -> Unit,
    onDismiss: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.98f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Handle bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            colors.onSurface.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp)
                        )
                        .clickable { onDismiss() }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Master Controls",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Master Intensity Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Master Intensity",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${(masterIntensity * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Slider(
                    value = masterIntensity,
                    onValueChange = onMasterIntensityChange,
                    valueRange = 0.2f..2f,
                    steps = 17,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.primary,
                        activeTrackColor = colors.primary,
                        inactiveTrackColor = colors.surfaceVariant
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🧊", style = MaterialTheme.typography.bodySmall)
                    Text("🍬", style = MaterialTheme.typography.bodySmall)
                    Text("🚀", style = MaterialTheme.typography.bodySmall)
                    Text("🔥", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Haptics Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Vibration,
                    contentDescription = null,
                    tint = colors.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Haptic Feedback",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Enable vibration effects",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = hapticsEnabled,
                    onCheckedChange = onHapticsToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.primary,
                        checkedTrackColor = colors.primary.copy(alpha = 0.5f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Boost Button
            Button(
                onClick = {
                    onBoost()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("BOOST ALL (3s)")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = { onMasterIntensityChange(2f) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SET TO MAXIMUM 🔥")
            }
        }
    }
}
