package com.sugarmunch.app.ui.marketplace

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// region Data Model

data class SharedEffect(
    val id: String,
    val name: String,
    val author: String,
    val description: String,
    val previewColors: List<Color>,
    val particleTypes: List<String>,
    val downloadCount: Int = 0,
    val rating: Float = 0f,
    val isInstalled: Boolean = false,
    val originalEffectId: String? = null
)

// endregion

// region Effect Gallery Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectGalleryScreen(
    onBack: () -> Unit,
    onInstallEffect: (SharedEffect) -> Unit,
    onRemixEffect: (SharedEffect) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }

    val filters = remember { listOf("All", "Particles", "Visual", "Haptic", "Remixes") }

    val filteredEffects = remember(searchQuery, selectedFilter) {
        SampleEffectData.sampleEffects.filter { effect ->
            val matchesQuery = searchQuery.isBlank() ||
                effect.name.contains(searchQuery, ignoreCase = true) ||
                effect.author.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "All" -> true
                "Particles" -> effect.particleTypes.any {
                    it in listOf("Confetti", "Sparkle", "Snow", "Bubbles", "Firework")
                }
                "Visual" -> effect.particleTypes.any {
                    it in listOf("Glow", "Trail", "Wave", "Pulse")
                }
                "Haptic" -> effect.particleTypes.any {
                    it in listOf("Haptic", "Vibration")
                }
                "Remixes" -> effect.originalEffectId != null
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Effect Gallery") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                        Icon(
                            if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(SugarDimens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
        ) {
            // Search bar
            if (isSearchExpanded) {
                item {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search effects…") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(SugarDimens.Radius.pill),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }

            // Filter chips
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs)
                ) {
                    filters.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) }
                        )
                    }
                }
            }

            // Effect cards
            items(filteredEffects, key = { it.id }) { effect ->
                SharedEffectCard(
                    effect = effect,
                    onInstall = { onInstallEffect(effect) },
                    onRemix = { onRemixEffect(effect) }
                )
            }
        }
    }
}

// endregion

// region Effect Card

@Composable
fun SharedEffectCard(
    effect: SharedEffect,
    onInstall: () -> Unit,
    onRemix: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SugarDimens.Radius.md))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(SugarDimens.Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
    ) {
        // Animated preview area
        MiniParticlePreview(
            colors = effect.previewColors,
            modifier = Modifier.size(60.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                effect.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "by ${effect.author}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                effect.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Particle type chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xxxs),
                modifier = Modifier.padding(top = SugarDimens.Spacing.xxs)
            ) {
                effect.particleTypes.take(3).forEach { type ->
                    Text(
                        type,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(SugarDimens.Radius.xs))
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                            .padding(
                                horizontal = SugarDimens.Spacing.xxs,
                                vertical = SugarDimens.Spacing.xxxs
                            )
                    )
                }
            }

            // Remix badge
            if (effect.originalEffectId != null) {
                val originalName = SampleEffectData.sampleEffects
                    .find { it.id == effect.originalEffectId }?.name ?: "Unknown"
                Text(
                    "Remixed from: $originalName",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(top = SugarDimens.Spacing.xxxs)
                        .clip(RoundedCornerShape(SugarDimens.Radius.xs))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                        .padding(
                            horizontal = SugarDimens.Spacing.xxs,
                            vertical = SugarDimens.Spacing.xxxs
                        )
                )
            }

            Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))

            // Bottom row: rating, downloads, buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    String.format("%.1f", effect.rating),
                    style = MaterialTheme.typography.labelSmall
                )

                Spacer(modifier = Modifier.width(SugarDimens.Spacing.xs))

                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    formatEffectCount(effect.downloadCount),
                    style = MaterialTheme.typography.labelSmall
                )

                Spacer(modifier = Modifier.weight(1f))

                FilledTonalButton(
                    onClick = onRemix,
                    contentPadding = PaddingValues(horizontal = SugarDimens.Spacing.xs),
                    modifier = Modifier.height(SugarDimens.Height.buttonSmall)
                ) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(SugarDimens.Spacing.xxxs))
                    Text("Remix", style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.width(SugarDimens.Spacing.xxs))

                if (effect.isInstalled) {
                    OutlinedButton(
                        onClick = {},
                        contentPadding = PaddingValues(horizontal = SugarDimens.Spacing.xs),
                        modifier = Modifier.height(SugarDimens.Height.buttonSmall)
                    ) {
                        Text("Installed", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    Button(
                        onClick = onInstall,
                        contentPadding = PaddingValues(horizontal = SugarDimens.Spacing.xs),
                        modifier = Modifier.height(SugarDimens.Height.buttonSmall)
                    ) {
                        Text("Install", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

// endregion

// region Mini Particle Preview

@Composable
private fun MiniParticlePreview(
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val particleColors = colors.ifEmpty { listOf(Color.White) }
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_time"
    )

    val particles = remember {
        List(12) {
            ParticleState(
                angle = Random.nextFloat() * 360f,
                radius = Random.nextFloat() * 0.3f + 0.1f,
                speed = Random.nextFloat() * 0.5f + 0.5f,
                size = Random.nextFloat() * 3f + 2f,
                colorIndex = it % particleColors.size
            )
        }
    }

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(SugarDimens.Radius.sm))
            .background(Color.Black.copy(alpha = 0.85f))
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxRadius = minOf(cx, cy) * 0.85f

        particles.forEach { p ->
            val angle = Math.toRadians(((p.angle + time * p.speed) % 360f).toDouble())
            val r = maxRadius * p.radius
            val x = cx + (r * cos(angle)).toFloat()
            val y = cy + (r * sin(angle)).toFloat()

            drawCircle(
                color = particleColors[p.colorIndex % particleColors.size],
                radius = p.size,
                center = Offset(x, y),
                alpha = 0.8f
            )
        }
    }
}

private data class ParticleState(
    val angle: Float,
    val radius: Float,
    val speed: Float,
    val size: Float,
    val colorIndex: Int
)

// endregion

// region Effect Remixer Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectRemixerScreen(
    originalEffect: SharedEffect,
    onSave: (SharedEffect) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val editableColors = remember { mutableStateListOf(*originalEffect.previewColors.toTypedArray()) }
    val editableParticleTypes = remember {
        mutableStateListOf(*originalEffect.particleTypes.toTypedArray())
    }
    var speed by remember { mutableFloatStateOf(0.5f) }
    var density by remember { mutableFloatStateOf(0.5f) }
    var gravity by remember { mutableFloatStateOf(0.3f) }

    val allParticleTypes = remember {
        listOf("Confetti", "Sparkle", "Snow", "Bubbles", "Firework", "Glow", "Trail", "Wave")
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Remix Effect") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = SugarDimens.Spacing.md)
        ) {
            // Live preview
            Text(
                "Live Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
            MiniParticlePreview(
                colors = editableColors.toList(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

            // Color palette editor
            Text(
                "Color Palette",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
            Row(
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                editableColors.forEachIndexed { index, color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                    ) {
                        if (editableColors.size > 1) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove color",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .padding(2.dp)
                            )
                        }
                    }
                }

                // Add color button
                if (editableColors.size < 6) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

            // Particle type toggles
            Text(
                "Particle Types",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
            Column(verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xxs)) {
                allParticleTypes.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
                    ) {
                        row.forEach { type ->
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Switch(
                                    checked = type in editableParticleTypes,
                                    onCheckedChange = { checked ->
                                        if (checked) editableParticleTypes.add(type)
                                        else editableParticleTypes.remove(type)
                                    }
                                )
                                Spacer(modifier = Modifier.width(SugarDimens.Spacing.xxs))
                                Text(type, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

            // Sliders
            SliderControl("Speed", speed) { speed = it }
            SliderControl("Density", density) { density = it }
            SliderControl("Gravity", gravity) { gravity = it }

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = {
                    val remix = SharedEffect(
                        id = "remix_${originalEffect.id}_${System.currentTimeMillis()}",
                        name = "${originalEffect.name} Remix",
                        author = "You",
                        description = "A remix of ${originalEffect.name}",
                        previewColors = editableColors.toList(),
                        particleTypes = editableParticleTypes.toList(),
                        originalEffectId = originalEffect.id
                    )
                    onSave(remix)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SugarDimens.Height.buttonLarge)
                    .padding(bottom = SugarDimens.Spacing.md),
                shape = RoundedCornerShape(SugarDimens.Radius.md)
            ) {
                Icon(
                    Icons.Default.Shuffle,
                    contentDescription = null,
                    modifier = Modifier.size(SugarDimens.IconSize.sm)
                )
                Spacer(modifier = Modifier.width(SugarDimens.Spacing.xs))
                Text("Save as Remix", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SliderControl(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(60.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f)
        )
        Text(
            "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(40.dp)
        )
    }
}

// endregion

// region Effect Showcase Recorder

@Composable
fun EffectShowcaseRecorder(
    effectId: String,
    modifier: Modifier = Modifier
) {
    val effect = remember(effectId) {
        SampleEffectData.sampleEffects.find { it.id == effectId }
    }
    var isRecording by remember { mutableStateOf(false) }
    var recordingComplete by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(5) }
    val recordingProgress by animateFloatAsState(
        targetValue = if (isRecording) 1f else 0f,
        animationSpec = tween(durationMillis = 5000),
        label = "recording_progress"
    )

    LaunchedEffect(isRecording) {
        if (isRecording) {
            countdown = 5
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            isRecording = false
            recordingComplete = true
        }
    }

    Column(
        modifier = modifier.padding(SugarDimens.Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Effect Showcase",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

        // Preview canvas with recording ring
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            MiniParticlePreview(
                colors = effect?.previewColors ?: listOf(Color.White),
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
            )

            if (isRecording) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 4.dp.toPx()
                    drawArc(
                        color = Color.Red,
                        startAngle = -90f,
                        sweepAngle = recordingProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(
                            size.width - strokeWidth,
                            size.height - strokeWidth
                        )
                    )
                }

                Text(
                    "$countdown",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

        if (recordingComplete) {
            Button(
                onClick = { recordingComplete = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(SugarDimens.Radius.md)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(SugarDimens.IconSize.sm)
                )
                Spacer(modifier = Modifier.width(SugarDimens.Spacing.xs))
                Text("Share")
            }
        } else if (isRecording) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.FiberManualRecord,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(SugarDimens.Spacing.xxs))
                Text(
                    "Recording… ${countdown}s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red
                )
            }
        } else {
            Button(
                onClick = { isRecording = true },
                shape = RoundedCornerShape(SugarDimens.Radius.md)
            ) {
                Icon(
                    Icons.Default.FiberManualRecord,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(SugarDimens.IconSize.sm)
                )
                Spacer(modifier = Modifier.width(SugarDimens.Spacing.xs))
                Text("Record 5s Preview")
            }
        }
    }
}

// endregion

// region Utilities

private fun formatEffectCount(count: Int): String = when {
    count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
    count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
    else -> count.toString()
}

// endregion

// region Sample Data

object SampleEffectData {
    val sampleEffects: List<SharedEffect> = listOf(
        SharedEffect(
            id = "effect_candy_confetti",
            name = "Candy Confetti",
            author = "SugarTeam",
            description = "Colorful confetti particles that burst and float down with a gentle sway.",
            previewColors = listOf(Color(0xFFFF6B6B), Color(0xFFFFD93D), Color(0xFF6BCB77)),
            particleTypes = listOf("Confetti", "Sparkle"),
            downloadCount = 8400,
            rating = 4.7f
        ),
        SharedEffect(
            id = "effect_magic_sparkle",
            name = "Magic Sparkle",
            author = "EffectMaster",
            description = "Twinkling star-shaped sparkles that follow touch and fade with a golden glow.",
            previewColors = listOf(Color(0xFFFFD700), Color(0xFFFFF8DC), Color(0xFFDAA520)),
            particleTypes = listOf("Sparkle", "Glow", "Trail"),
            downloadCount = 6200,
            rating = 4.5f
        ),
        SharedEffect(
            id = "effect_sugar_snow",
            name = "Sugar Snow",
            author = "WinterCandy",
            description = "Gentle snowflake particles that drift down with realistic physics and melt on surfaces.",
            previewColors = listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2), Color(0xFFFFFFFF)),
            particleTypes = listOf("Snow", "Sparkle"),
            downloadCount = 5100,
            rating = 4.3f,
            isInstalled = true
        ),
        SharedEffect(
            id = "effect_bubble_pop",
            name = "Bubble Pop",
            author = "FizzyFX",
            description = "Iridescent bubbles that float up and pop with a satisfying haptic response.",
            previewColors = listOf(Color(0xFF89CFF0), Color(0xFFDDA0DD), Color(0xFF98FB98)),
            particleTypes = listOf("Bubbles", "Haptic"),
            downloadCount = 7300,
            rating = 4.6f
        ),
        SharedEffect(
            id = "effect_neon_pulse",
            name = "Neon Pulse",
            author = "GlowArtist",
            description = "Pulsating neon rings that expand outward with electric colors and glow effects.",
            previewColors = listOf(Color(0xFFFF00FF), Color(0xFF00FFFF), Color(0xFFFF6600)),
            particleTypes = listOf("Pulse", "Glow", "Wave"),
            downloadCount = 4800,
            rating = 4.4f
        ),
        SharedEffect(
            id = "effect_firework_blast",
            name = "Firework Blast",
            author = "PyroCandy",
            description = "Explosive firework bursts with trails, sparkles, and vibrant color cascades.",
            previewColors = listOf(Color(0xFFFF0000), Color(0xFFFFD700), Color(0xFF00FF00)),
            particleTypes = listOf("Firework", "Trail", "Sparkle"),
            downloadCount = 9100,
            rating = 4.8f
        ),
        SharedEffect(
            id = "effect_candy_remix_1",
            name = "Candy Confetti Deluxe",
            author = "RemixKing",
            description = "An enhanced remix of Candy Confetti with extra sparkle and pastel colors.",
            previewColors = listOf(Color(0xFFFFB6C1), Color(0xFFE6B3FF), Color(0xFFB5DEFF)),
            particleTypes = listOf("Confetti", "Sparkle", "Glow"),
            downloadCount = 2300,
            rating = 4.2f,
            originalEffectId = "effect_candy_confetti"
        ),
        SharedEffect(
            id = "effect_rainbow_wave",
            name = "Rainbow Wave",
            author = "SpectrumFX",
            description = "Smooth rainbow waves that ripple across the screen with a soothing motion.",
            previewColors = listOf(
                Color(0xFFFF0000), Color(0xFFFF8C00), Color(0xFFFFD700),
                Color(0xFF00FF00), Color(0xFF0000FF), Color(0xFF8B00FF)
            ),
            particleTypes = listOf("Wave", "Trail"),
            downloadCount = 3600,
            rating = 4.1f
        )
    )
}

// endregion
