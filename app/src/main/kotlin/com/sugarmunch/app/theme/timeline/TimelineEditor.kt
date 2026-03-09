package com.sugarmunch.app.theme.timeline

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Theme Animation Timeline Editor
 *
 * Visual timeline editor for creating complex theme animations:
 * - Keyframe-based animation editing
 * - Multiple animation tracks
 * - Interpolation controls
 * - Preview and playback
 * - Export to code/JSON
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineEditor(
    onNavigateBack: () -> Unit,
    onSaveAnimation: (TimelineAnimation) -> Unit
) {
    val scope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(0f) }
    var selectedTrack by remember { mutableStateOf<AnimationTrack?>(null) }
    var selectedKeyframe by remember { mutableStateOf<Keyframe?>(null) }
    var zoomLevel by remember { mutableStateOf(1f) }

    val tracks = remember { mutableStateListOf<AnimationTrack>() }
    val totalDuration = 10000f // 10 seconds default

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Animation Timeline")
                        Text(
                            text = "${tracks.size} tracks • ${formatTime(currentTime)} / ${formatTime(totalDuration)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Play/Pause */ isPlaying = !isPlaying }) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play"
                        )
                    }
                    IconButton(onClick = { /* Stop */ isPlaying = false; currentTime = 0f }) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop")
                    }
                    IconButton(onClick = { onSaveAnimation(TimelineAnimation(tracks, totalDuration)) }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
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
            // Timeline Controls
            TimelineControls(
                isPlaying = isPlaying,
                currentTime = currentTime,
                totalDuration = totalDuration,
                onTimeChanged = { currentTime = it },
                zoomLevel = zoomLevel,
                onZoomChanged = { zoomLevel = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Timeline Tracks
            TimelineTracks(
                tracks = tracks,
                selectedTrack = selectedTrack,
                selectedKeyframe = selectedKeyframe,
                currentTime = currentTime,
                totalDuration = totalDuration,
                zoomLevel = zoomLevel,
                onTrackSelected = { selectedTrack = it },
                onKeyframeSelected = { selectedKeyframe = it },
                onAddTrack = { tracks.add(it) },
                onRemoveTrack = { tracks.remove(it) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            // Keyframe Properties Panel
            if (selectedKeyframe != null) {
                KeyframePropertiesPanel(
                    keyframe = selectedKeyframe!!,
                    onKeyframeUpdated = { /* Update keyframe */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }

    // Playback simulation
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (currentTime < totalDuration && isPlaying) {
                currentTime += 16.67f // ~60fps
                kotlinx.coroutines.delay(16)
            }
            if (currentTime >= totalDuration) {
                isPlaying = false
                currentTime = 0f
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// TIMELINE CONTROLS
// ═════════════════════════════════════════════════════════════

@Composable
private fun TimelineControls(
    isPlaying: Boolean,
    currentTime: Float,
    totalDuration: Float,
    onTimeChanged: (Float) -> Unit,
    zoomLevel: Float,
    onZoomChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Time Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onTimeChanged((currentTime - 1000).coerceAtLeast(0f)) }) {
                    Icon(Icons.Default.Replay10, contentDescription = "Rewind 10s")
                }

                Slider(
                    value = currentTime,
                    onValueChange = onTimeChanged,
                    valueRange = 0f..totalDuration,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { onTimeChanged((currentTime + 1000).coerceAtMost(totalDuration)) }) {
                    Icon(Icons.Default.Forward10, contentDescription = "Forward 10s")
                }

                // Time Display
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = formatTime(currentTime),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Zoom Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Zoom",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { onZoomChanged((zoomLevel - 0.25f).coerceAtLeast(0.25f)) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                    }

                    Text(
                        text = "${(zoomLevel * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(50.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(onClick = { onZoomChanged((zoomLevel + 0.25f).coerceAtMost(4f)) }) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In")
                    }
                }

                // Add Track Button
                Button(onClick = { /* Show add track dialog */ }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Track")
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// TIMELINE TRACKS
// ═════════════════════════════════════════════════════════════

@Composable
private fun TimelineTracks(
    tracks: List<AnimationTrack>,
    selectedTrack: AnimationTrack?,
    selectedKeyframe: Keyframe?,
    currentTime: Float,
    totalDuration: Float,
    zoomLevel: Float,
    onTrackSelected: (AnimationTrack) -> Unit,
    onKeyframeSelected: (Keyframe) -> Unit,
    onAddTrack: (AnimationTrack) -> Unit,
    onRemoveTrack: (AnimationTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tracks, key = { it.id }) { track ->
            TimelineTrackRow(
                track = track,
                isSelected = track == selectedTrack,
                selectedKeyframe = selectedKeyframe,
                currentTime = currentTime,
                totalDuration = totalDuration,
                zoomLevel = zoomLevel,
                onSelect = { onTrackSelected(track) },
                onKeyframeSelected = onKeyframeSelected,
                onRemove = { onRemoveTrack(track) }
            )
        }

        item {
            // Empty state or add track prompt
            if (tracks.isEmpty()) {
                EmptyTimelineState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            }
        }
    }
}

@Composable
private fun TimelineTrackRow(
    track: AnimationTrack,
    isSelected: Boolean,
    selectedKeyframe: Keyframe?,
    currentTime: Float,
    totalDuration: Float,
    zoomLevel: Float,
    onSelect: () -> Unit,
    onKeyframeSelected: (Keyframe) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Track Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getTrackIcon(track.type),
                        contentDescription = null,
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = track.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = { }, Modifier.size(28.dp)) {
                        Icon(Icons.Default.Visibility, contentDescription = "Toggle Visibility")
                    }
                    IconButton(onClick = onRemove, Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Track Timeline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        } else {
                            Color.Transparent
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                // Playhead
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .align(Alignment.CenterStart)
                        .offset {
                            val x = ((currentTime / totalDuration) * (this@Box.maxWidth * zoomLevel)).toInt()
                            androidx.compose.ui.unit.IntOffset(x, 0)
                        }
                        .background(MaterialTheme.colorScheme.error)
                )

                // Keyframes
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(track.keyframes, key = { it.id }) { keyframe ->
                        KeyframeMarker(
                            keyframe = keyframe,
                            isSelected = keyframe == selectedKeyframe,
                            zoomLevel = zoomLevel,
                            totalDuration = totalDuration,
                            onClick = { onKeyframeSelected(keyframe) }
                        )
                    }

                    // Add keyframe button at end
                    item {
                        IconButton(
                            onClick = { /* Add keyframe at current time */ },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Keyframe")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyframeMarker(
    keyframe: Keyframe,
    isSelected: Boolean,
    zoomLevel: Float,
    totalDuration: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    getKeyframeColor(keyframe.type)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getKeyframeIcon(keyframe.type),
            contentDescription = keyframe.type.name,
            tint = Color.White,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun KeyframePropertiesPanel(
    keyframe: Keyframe,
    onKeyframeUpdated: (Keyframe) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KEYFRAME PROPERTIES",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(onClick = { /* Delete keyframe */ }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }

            // Time
            OutlinedTextField(
                value = formatTime(keyframe.time),
                onValueChange = { /* Parse and update time */ },
                label = { Text("Time") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            // Interpolation Selector
            Text(
                text = "Interpolation",
                style = MaterialTheme.typography.bodyMedium
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(InterpolationType.entries) { type ->
                    FilterChip(
                        selected = keyframe.interpolation == type,
                        onClick = { /* Update interpolation */ },
                        label = { Text(type.name) }
                    )
                }
            }

            // Value Sliders based on keyframe type
            when (keyframe.type) {
                KeyframeType.OPACITY -> {
                    SliderWithLabel(
                        label = "Opacity",
                        value = keyframe.value,
                        onValueChange = { /* Update value */ },
                        valueRange = 0f..1f
                    )
                }
                KeyframeType.SCALE -> {
                    SliderWithLabel(
                        label = "Scale",
                        value = keyframe.value,
                        onValueChange = { /* Update value */ },
                        valueRange = 0f..2f
                    )
                }
                KeyframeType.ROTATION -> {
                    SliderWithLabel(
                        label = "Rotation",
                        value = keyframe.value,
                        onValueChange = { /* Update value */ },
                        valueRange = 0f..360f,
                        valueSuffix = "°"
                    )
                }
                else -> {
                    Text(
                        text = "Value: ${keyframe.value}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// HELPER COMPOSABLES
// ═════════════════════════════════════════════════════════════

@Composable
private fun EmptyTimelineState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Animation,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Animation Tracks",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Click 'Add Track' to create your first animation",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SliderWithLabel(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    valueSuffix: String = ""
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "${String.format("%.2f", value)}$valueSuffix",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

private fun formatTime(ms: Float): String {
    val seconds = (ms / 1000).toInt()
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}

private fun getTrackIcon(type: TrackType): ImageVector {
    return when (type) {
        TrackType.OPACITY -> Icons.Default.BlurOn
        TrackType.SCALE -> Icons.Default.AspectRatio
        TrackType.ROTATION -> Icons.Default.RotateRight
        TrackType.POSITION -> Icons.Default.DragIndicator
        TrackType.COLOR -> Icons.Default.Colorize
        TrackType.CUSTOM -> Icons.Default.Tune
    }
}

private fun getKeyframeIcon(type: KeyframeType): ImageVector {
    return when (type) {
        KeyframeType.OPACITY -> Icons.Default.BlurOn
        KeyframeType.SCALE -> Icons.Default.AspectRatio
        KeyframeType.ROTATION -> Icons.Default.RotateRight
        KeyframeType.POSITION -> Icons.Default.DragIndicator
        KeyframeType.COLOR -> Icons.Default.Colorize
        KeyframeType.CUSTOM -> Icons.Default.Star
    }
}

private fun getKeyframeColor(type: KeyframeType): Color {
    return when (type) {
        KeyframeType.OPACITY -> Color(0xFF2196F3)
        KeyframeType.SCALE -> Color(0xFF4CAF50)
        KeyframeType.ROTATION -> Color(0xFFFF9800)
        KeyframeType.POSITION -> Color(0xFF9C27B0)
        KeyframeType.COLOR -> Color(0xFFF44336)
        KeyframeType.CUSTOM -> Color(0xFFFFEB3B)
    }
}

// ═════════════════════════════════════════════════════════════
// DATA CLASSES
// ═════════════════════════════════════════════════════════════

/**
 * Complete timeline animation definition
 */
data class TimelineAnimation(
    val tracks: List<AnimationTrack>,
    val totalDuration: Float,
    val name: String = "Custom Animation",
    val isLooping: Boolean = true
)

/**
 * Animation track containing keyframes
 */
data class AnimationTrack(
    val id: String = "track_${System.currentTimeMillis()}",
    val name: String,
    val type: TrackType,
    val targetProperty: String,
    val keyframes: List<Keyframe>,
    val isEnabled: Boolean = true
)

enum class TrackType {
    OPACITY, SCALE, ROTATION, POSITION, COLOR, CUSTOM
}

/**
 * Keyframe definition
 */
data class Keyframe(
    val id: String = "keyframe_${System.currentTimeMillis()}",
    val time: Float,
    val type: KeyframeType,
    val value: Float,
    val interpolation: InterpolationType = InterpolationType.EASE_IN_OUT,
    val easeCurve: BezierCurve? = null
)

enum class KeyframeType {
    OPACITY, SCALE, ROTATION, POSITION, COLOR, CUSTOM
}

enum class InterpolationType {
    LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT, STEP, CUSTOM
}

data class BezierCurve(
    val controlPoint1X: Float,
    val controlPoint1Y: Float,
    val controlPoint2X: Float,
    val controlPoint2Y: Float
)
