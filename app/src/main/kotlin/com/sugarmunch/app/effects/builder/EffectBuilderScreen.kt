package com.sugarmunch.app.effects.builder

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.effects.v2.model.EffectV2

/**
 * Custom Effect Builder - Combine and customize effects
 */
@Composable
fun EffectBuilderScreen(
    onEffectCreated: (CustomEffectPreset) -> Unit,
    onNavigateBack: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    val effectState = remember { mutableStateOf(EffectBuilderState()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Custom Effect") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            LinearProgressIndicator(
                progress = (currentStep + 1).toFloat() / 3,
                modifier = Modifier.fillMaxWidth()
            )
            
            when (currentStep) {
                0 -> EffectSelectionStep(
                    state = effectState.value,
                    onStateChange = { effectState.value = it }
                )
                1 -> IntensityEditorStep(
                    state = effectState.value,
                    onStateChange = { effectState.value = it }
                )
                2 -> PreviewAndSaveStep(
                    state = effectState.value,
                    onSave = {
                        onEffectCreated(it.toCustomEffectPreset())
                        onNavigateBack()
                    }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { if (currentStep > 0) currentStep-- },
                    enabled = currentStep > 0
                ) {
                    Text("Back")
                }
                
                if (currentStep < 2) {
                    Button(onClick = { currentStep++ }) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                } else {
                    Button(onClick = { /* Save */ }) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Preset")
                    }
                }
            }
        }
    }
}

@Composable
private fun EffectSelectionStep(
    state: EffectBuilderState,
    onStateChange: (EffectBuilderState) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Select Effects to Combine",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        
        items(EffectCategory.values()) { category ->
            EffectCategorySection(
                category = category,
                selectedEffects = state.selectedEffects,
                onEffectToggle = { effectId ->
                    val newSelection = if (state.selectedEffects.contains(effectId)) {
                        state.selectedEffects - effectId
                    } else {
                        state.selectedEffects + effectId
                    }
                    onStateChange(state.copy(selectedEffects = newSelection))
                }
            )
        }
    }
}

@Composable
private fun EffectCategorySection(
    category: EffectCategory,
    selectedEffects: Set<String>,
    onEffectToggle: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(category.effects) { effect ->
                    EffectChip(
                        effect = effect,
                        isSelected = selectedEffects.contains(effect.id),
                        onToggle = { onEffectToggle(effect.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EffectChip(
    effect: EffectInfo,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onToggle,
        label = { Text(effect.name) },
        leadingIcon = if (isSelected) {
            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
        } else null
    )
}

@Composable
private fun IntensityEditorStep(
    state: EffectBuilderState,
    onStateChange: (EffectBuilderState) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Adjust Effect Intensities",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        
        items(state.selectedEffects.toList()) { effectId ->
            EffectIntensitySlider(
                effectId = effectId,
                intensity = state.effectIntensities[effectId] ?: 1f,
                onIntensityChange = {
                    onStateChange(
                        state.copy(
                            effectIntensities = state.effectIntensities + (effectId to it)
                        )
                    )
                }
            )
        }
        
        item {
            MasterIntensitySlider(
                intensity = state.masterIntensity,
                onIntensityChange = {
                    onStateChange(state.copy(masterIntensity = it))
                }
            )
        }
    }
}

@Composable
private fun EffectIntensitySlider(
    effectId: String,
    intensity: Float,
    onIntensityChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = effectId,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "%.0f%%".format(intensity * 100),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = intensity,
                onValueChange = onIntensityChange,
                valueRange = 0f..2f,
                steps = 39
            )
        }
    }
}

@Composable
private fun MasterIntensitySlider(
    intensity: Float,
    onIntensityChange: (Float) -> Unit
) {
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
                text = "Master Intensity",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Overall multiplier",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "%.1fx".format(intensity),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = intensity,
                onValueChange = onIntensityChange,
                valueRange = 0.5f..3f,
                steps = 24
            )
        }
    }
}

@Composable
private fun PreviewAndSaveStep(
    state: EffectBuilderState,
    onSave: () -> Unit
) {
    var presetName by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Preview & Save",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Selected Effects",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    state.selectedEffects.forEach { effectId ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = effectId,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "%.0f%%".format((state.effectIntensities[effectId] ?: 1f) * 100),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        
        item {
            OutlinedTextField(
                value = presetName,
                onValueChange = { presetName = it },
                label = { Text("Preset Name") },
                placeholder = { Text("My Custom Preset") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        
        item {
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = presetName.isNotBlank() && state.selectedEffects.isNotEmpty()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Preset")
            }
        }
    }
}

// Data classes
data class EffectBuilderState(
    val selectedEffects: Set<String> = emptySet(),
    val effectIntensities: Map<String, Float> = emptyMap(),
    val masterIntensity: Float = 1f
)

data class CustomEffectPreset(
    val name: String,
    val effects: Map<String, Float>,
    val masterIntensity: Float,
    val createdAt: Long = System.currentTimeMillis()
)

enum class EffectCategory(val displayName: String, val effects: List<EffectInfo>) {
    VISUAL("Visual Overlays", listOf(
        EffectInfo("sugarrush_overlay", "SugarRush"),
        EffectInfo("rainbow_tint", "Rainbow Tint"),
        EffectInfo("mint_wash", "Mint Wash"),
        EffectInfo("caramel_dim", "Caramel Dim")
    )),
    PARTICLES("Particles", listOf(
        EffectInfo("candy_confetti", "Candy Confetti"),
        EffectInfo("pop_rocks", "Pop Rocks")
    )),
    ANIMATIONS("Animations", listOf(
        EffectInfo("unicorn_swirl", "Unicorn Swirl"),
        EffectInfo("ice_crystal", "Ice Crystal")
    )),
    HAPTIC("Haptic", listOf(
        EffectInfo("heartbeat", "Heartbeat"),
        EffectInfo("gummy_bounce", "Gummy Bounce")
    ))
}

data class EffectInfo(val id: String, val name: String)
