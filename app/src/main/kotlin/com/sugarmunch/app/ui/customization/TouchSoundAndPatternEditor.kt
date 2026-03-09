package com.sugarmunch.app.ui.customization

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.ui.feedback.HapticPreset
import com.sugarmunch.app.ui.feedback.HapticSegment
import com.sugarmunch.app.ui.feedback.ThemeAwareHapticEngine
import kotlinx.coroutines.launch

/**
 * Sound context for touch feedback
 */
enum class SoundContext {
    CLICK,
    SUCCESS,
    ERROR,
    SWIPE,
    SCROLL,
    NOTIFICATION,
    SYSTEM_EVENT
}

/**
 * Sound configuration for touch feedback
 */
data class SoundConfig(
    val enabled: Boolean = true,
    val volume: Float = 0.5f,
    val pitch: Float = 1.0f,
    val selectedSounds: Map<SoundContext, SoundType> = mapOf(
        SoundContext.CLICK to SoundType.DEFAULT_CLICK,
        SoundContext.SUCCESS to SoundType.CHIME,
        SoundContext.ERROR to SoundType.BUZZ,
        SoundContext.SWIPE to SoundType.SWOOSH,
        SoundContext.SCROLL to SoundType.NONE,
        SoundContext.NOTIFICATION to SoundType.POP,
        SoundContext.SYSTEM_EVENT to SoundType.NONE
    )
)

/**
 * Touch feedback configuration
 */
data class TouchFeedbackConfig(
    val visualIndicator: TouchIndicatorType = TouchIndicatorType.RIPPLE,
    val indicatorSize: Float = 50f,
    val indicatorDuration: Int = 300,
    val indicatorColor: Color = Color.Blue,
    val showForAllTouches: Boolean = false,
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val soundConfig: SoundConfig = SoundConfig()
)

/**
 * Touch indicator types
 */
enum class TouchIndicatorType {
    NONE,
    RIPPLE,
    CIRCLE,
    DOT,
    SQUARE,
    STAR
}

/**
 * Haptic pattern preset
 */
enum class HapticPresetType {
    LIGHT_TAP,
    MEDIUM_TAP,
    HEAVY_TAP,
    DOUBLE_TAP,
    TRIPLE_TAP,
    SUCCESS,
    ERROR,
    WARNING,
    HEARTBEAT,
    CELEBRATION,
    CUSTOM
}

/**
 * Sound types for touch feedback
 */
enum class SoundType {
    NONE,
    DEFAULT_CLICK,
    POP,
    CHIME,
    BUZZ,
    SWOOSH,
    LEVEL_UP,
    ACHIEVEMENT,
    CUSTOM
}

/**
 * Touch Sound Customization Screen
 * Allows users to customize touch sounds for different contexts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouchSoundCustomizationScreen(
    onNavigateBack: () -> Unit,
    soundConfig: SoundConfig,
    onSoundConfigChange: (SoundConfig) -> Unit
) {
    val context = LocalContext.current
    val soundPool = remember { createSoundPool(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Touch Sounds") },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Toggle
            item {
                SoundMasterToggle(
                    enabled = soundConfig.enabled,
                    onEnabledChange = { onSoundConfigChange(soundConfig.copy(enabled = it)) }
                )
            }

            if (soundConfig.enabled) {
                // Volume Control
                item {
                    SoundVolumeControl(
                        volume = soundConfig.volume,
                        onVolumeChange = { onSoundConfigChange(soundConfig.copy(volume = it)) }
                    )
                }

                // Pitch Control
                item {
                    SoundPitchControl(
                        pitch = soundConfig.pitch,
                        onPitchChange = { onSoundConfigChange(soundConfig.copy(pitch = it)) }
                    )
                }

                // Sound Selection by Context
                item {
                    Text(
                        text = "Sound by Context",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Click Sound
                item {
                    SoundSelector(
                        context = SoundContext.CLICK,
                        currentSound = soundConfig.selectedSounds[SoundContext.CLICK] ?: SoundType.NONE,
                        onSoundSelected = { 
                            onSoundConfigChange(
                                soundConfig.copy(
                                    selectedSounds = soundConfig.selectedSounds + (SoundContext.CLICK to it)
                                )
                            )
                        }
                    )
                }

                // Success Sound
                item {
                    SoundSelector(
                        context = SoundContext.SUCCESS,
                        currentSound = soundConfig.selectedSounds[SoundContext.SUCCESS] ?: SoundType.NONE,
                        onSoundSelected = {
                            onSoundConfigChange(
                                soundConfig.copy(
                                    selectedSounds = soundConfig.selectedSounds + (SoundContext.SUCCESS to it)
                                )
                            )
                        }
                    )
                }

                // Error Sound
                item {
                    SoundSelector(
                        context = SoundContext.ERROR,
                        currentSound = soundConfig.selectedSounds[SoundContext.ERROR] ?: SoundType.NONE,
                        onSoundSelected = {
                            onSoundConfigChange(
                                soundConfig.copy(
                                    selectedSounds = soundConfig.selectedSounds + (SoundContext.ERROR to it)
                                )
                            )
                        }
                    )
                }

                // Swipe Sound
                item {
                    SoundSelector(
                        context = SoundContext.SWIPE,
                        currentSound = soundConfig.selectedSounds[SoundContext.SWIPE] ?: SoundType.NONE,
                        onSoundSelected = {
                            onSoundConfigChange(
                                soundConfig.copy(
                                    selectedSounds = soundConfig.selectedSounds + (SoundContext.SWIPE to it)
                                )
                            )
                        }
                    )
                }

                // Scroll Sound
                item {
                    SoundSelector(
                        context = SoundContext.SCROLL,
                        currentSound = soundConfig.selectedSounds[SoundContext.SCROLL] ?: SoundType.NONE,
                        onSoundSelected = {
                            onSoundConfigChange(
                                soundConfig.copy(
                                    selectedSounds = soundConfig.selectedSounds + (SoundContext.SCROLL to it)
                                )
                            )
                        }
                    )
                }

                // Test Sounds Section
                item {
                    SoundTestSection(
                        soundConfig = soundConfig,
                        soundPool = soundPool
                    )
                }
            }
        }
    }
}

/**
 * Haptic Pattern Editor Screen
 * Allows users to create custom haptic patterns
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HapticPatternEditorScreen(
    onNavigateBack: () -> Unit,
    customPattern: List<HapticSegment>,
    onPatternChange: (List<HapticSegment>) -> Unit
) {
    val context = LocalContext.current
    val hapticEngine = remember { ThemeAwareHapticEngine.getInstance(context) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Haptic Pattern Editor") },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pattern Timeline
            item {
                HapticTimeline(
                    segments = customPattern,
                    onSegmentAdd = { onPatternChange(customPattern + HapticSegment()) },
                    onSegmentRemove = { index ->
                        onPatternChange(customPattern.filterIndexed { i, _ -> i != index })
                    },
                    onSegmentChange = { index, segment ->
                        val newPattern = customPattern.toMutableList()
                        newPattern[index] = segment
                        onPatternChange(newPattern)
                    }
                )
            }

            // Preset Patterns
            item {
                PresetPatternsSection(
                    onPresetSelected = { preset ->
                        onPatternChange(preset.toSegments())
                    }
                )
            }

            // Test Pattern Button
            item {
                Button(
                    onClick = {
                        scope.launch {
                            hapticEngine.performCustomPattern(customPattern, 1.0f)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Pattern")
                }
            }

            // Pattern Info
            item {
                PatternInfo(pattern = customPattern)
            }
        }
    }
}

/**
 * Sound Master Toggle Card
 */
@Composable
private fun SoundMasterToggle(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Touch Sounds",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Play sounds on touch interactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange
            )
        }
    }
}

/**
 * Sound Volume Control
 */
@Composable
private fun SoundVolumeControl(
    volume: Float,
    onVolumeChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Volume",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "${(volume * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                steps = 19
            )
        }
    }
}

/**
 * Sound Pitch Control
 */
@Composable
private fun SoundPitchControl(
    pitch: Float,
    onPitchChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pitch",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "${pitch}x",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = pitch,
                onValueChange = onPitchChange,
                valueRange = 0.5f..2.0f,
                steps = 14
            )
        }
    }
}

/**
 * Sound Selector for a context
 */
@Composable
private fun SoundSelector(
    context: SoundContext,
    currentSound: SoundType,
    onSoundSelected: (SoundType) -> Unit
) {
    var showSelector by remember { mutableStateOf(false) }

    Card(
        onClick = { showSelector = true },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = context.name.replace("_", " "),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = currentSound.name.replace("_", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Select"
            )
        }
    }

    if (showSelector) {
        SoundTypeSelectorDialog(
            currentSound = currentSound,
            onSoundSelected = {
                onSoundSelected(it)
                showSelector = false
            },
            onDismiss = { showSelector = false }
        )
    }
}

/**
 * Sound Type Selector Dialog
 */
@Composable
private fun SoundTypeSelectorDialog(
    currentSound: SoundType,
    onSoundSelected: (SoundType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Sound") },
        text = {
            LazyColumn {
                items(SoundType.entries) { soundType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSoundSelected(soundType) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = soundType.name.replace("_", " "),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (soundType == currentSound) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Sound Test Section
 */
@Composable
private fun SoundTestSection(
    soundConfig: SoundConfig,
    soundPool: SoundPool
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Test Sounds",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SoundType.entries.filter { it != SoundType.NONE && it != SoundType.CUSTOM }.forEach { soundType ->
                    FilterChip(
                        selected = false,
                        onClick = { /* Play sound */ },
                        label = { Text(soundType.name.replace("_", " ")) }
                    )
                }
            }
        }
    }
}

/**
 * Haptic Timeline Editor
 */
@Composable
private fun HapticTimeline(
    segments: List<HapticSegment>,
    onSegmentAdd: () -> Unit,
    onSegmentRemove: (Int) -> Unit,
    onSegmentChange: (Int, HapticSegment) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pattern Timeline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onSegmentAdd) {
                    Icon(Icons.Default.Add, contentDescription = "Add Segment")
                }
            }

            // Timeline visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    segments.forEach { segment ->
                        val heightFraction = segment.amplitude / 255f
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .fillMaxHeight(heightFraction)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }
            }

            // Segment editors
            if (segments.isEmpty()) {
                Text(
                    text = "Tap + to add segments",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                itemsIndexed(segments) { index, segment ->
                    HapticSegmentEditor(
                        segment = segment,
                        index = index,
                        onDurationChange = {
                            onSegmentChange(index, segment.copy(duration = it))
                        },
                        onAmplitudeChange = {
                            onSegmentChange(index, segment.copy(amplitude = it))
                        },
                        onRemove = { onSegmentRemove(index) }
                    )
                }
            }
        }
    }
}

/**
 * Haptic Segment Editor
 */
@Composable
private fun HapticSegmentEditor(
    segment: HapticSegment,
    index: Int,
    onDurationChange: (Long) -> Unit,
    onAmplitudeChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Segment ${index + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Duration slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Duration",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "${segment.duration}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = segment.duration.toFloat(),
                onValueChange = { onDurationChange(it.toLong()) },
                valueRange = 10f..500f,
                steps = 48,
                modifier = Modifier.fillMaxWidth()
            )

            // Amplitude slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Intensity",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "${(segment.amplitude / 255f * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = segment.amplitude.toFloat(),
                onValueChange = { onAmplitudeChange(it.toInt()) },
                valueRange = 0f..255f,
                steps = 25,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Preset Patterns Section
 */
@Composable
private fun PresetPatternsSection(
    onPresetSelected: (HapticPresetType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Preset Patterns",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HapticPresetType.entries) { preset ->
                    FilterChip(
                        selected = false,
                        onClick = { onPresetSelected(preset) },
                        label = { Text(preset.name.replace("_", " ")) }
                    )
                }
            }
        }
    }
}

/**
 * Pattern Info Card
 */
@Composable
private fun PatternInfo(pattern: List<HapticSegment>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Pattern Info",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Segments: ${pattern.size}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Total Duration: ${pattern.sumOf { it.duration + it.delay }}ms",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Avg Intensity: ${if (pattern.isNotEmpty()) (pattern.average { it.amplitude } / 255f * 100).toInt() else 0}%",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Create SoundPool for playing sounds
 */
private fun createSoundPool(context: Context): SoundPool {
    val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    return SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(audioAttributes)
        .build()
}

/**
 * Convert HapticPresetType to segments
 */
private fun HapticPresetType.toSegments(): List<HapticSegment> {
    return when (this) {
        HapticPresetType.LIGHT_TAP -> listOf(HapticSegment(duration = 20, amplitude = 60))
        HapticPresetType.MEDIUM_TAP -> listOf(HapticSegment(duration = 30, amplitude = 120))
        HapticPresetType.HEAVY_TAP -> listOf(HapticSegment(duration = 40, amplitude = 200))
        HapticPresetType.DOUBLE_TAP -> listOf(
            HapticSegment(duration = 20, amplitude = 120, delay = 0),
            HapticSegment(duration = 20, amplitude = 120, delay = 50)
        )
        HapticPresetType.TRIPLE_TAP -> listOf(
            HapticSegment(duration = 20, amplitude = 120, delay = 0),
            HapticSegment(duration = 20, amplitude = 120, delay = 50),
            HapticSegment(duration = 20, amplitude = 120, delay = 50)
        )
        HapticPresetType.SUCCESS -> listOf(
            HapticSegment(duration = 30, amplitude = 150, delay = 0),
            HapticSegment(duration = 30, amplitude = 200, delay = 80)
        )
        HapticPresetType.ERROR -> listOf(
            HapticSegment(duration = 25, amplitude = 200, delay = 0),
            HapticSegment(duration = 25, amplitude = 200, delay = 50),
            HapticSegment(duration = 25, amplitude = 200, delay = 50)
        )
        HapticPresetType.WARNING -> listOf(HapticSegment(duration = 100, amplitude = 180))
        HapticPresetType.HEARTBEAT -> listOf(
            HapticSegment(duration = 40, amplitude = 200, delay = 0),
            HapticSegment(duration = 40, amplitude = 160, delay = 100)
        )
        HapticPresetType.CELEBRATION -> listOf(
            HapticSegment(duration = 20, amplitude = 80, delay = 0),
            HapticSegment(duration = 30, amplitude = 150, delay = 40),
            HapticSegment(duration = 40, amplitude = 220, delay = 60),
            HapticSegment(duration = 50, amplitude = 255, delay = 80)
        )
        HapticPresetType.CUSTOM -> listOf(HapticSegment())
    }
}
