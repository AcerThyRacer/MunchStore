package com.sugarmunch.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.data.LocalPreferencesRepository
import com.sugarmunch.app.phaseone.PhaseOneUtilities
import com.sugarmunch.app.phaseone.PhaseOneUtilitySpec
import com.sugarmunch.app.phaseone.UtilityCustomization
import com.sugarmunch.app.phaseone.UtilityFeatureHighlight
import com.sugarmunch.app.phaseone.UtilityModePreset
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.components.ThemeAwareCard
import com.sugarmunch.app.theme.engine.ThemeRuntimeSnapshot
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.presets.ThemePresets
import com.sugarmunch.app.theme.profile.toThemeProfile
import com.sugarmunch.app.ui.typography.toDynamicTypographyConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilityStudioScreen(
    appId: String,
    onBack: () -> Unit,
    onPreviewClick: () -> Unit,
    onThemeStudioClick: () -> Unit,
    onNearbyShareClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = LocalPreferencesRepository.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val fallbackTheme = remember { ThemePresets.getDefault() }
    val runtime by themeManager.observeThemeRuntime(appId).collectAsState(
        initial = ThemeRuntimeSnapshot(
            profile = fallbackTheme.toThemeProfile(),
            theme = fallbackTheme,
            colors = fallbackTheme.getColorsForIntensity(1f),
            typography = fallbackTheme.toThemeProfile().typography.toDynamicTypographyConfig(context, emptyList()),
            themeIntensity = 1f,
            backgroundIntensity = 1f,
            particleIntensity = 1f,
            animationIntensity = 1f
        )
    )
    val colors = runtime.colors
    val spec = remember(appId) { PhaseOneUtilities.specFor(appId) }

    if (spec == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Utility Studio", color = colors.onSurface) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onSurface)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                AnimatedThemeBackground(appId = appId)
                Text(
                    text = "No utility studio is available for this app yet.",
                    color = colors.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
        return
    }

    val defaultCustomization = remember(spec) { spec.defaultCustomization() }
    val customization by prefs.getUtilityCustomization(appId, defaultCustomization)
        .collectAsState(initial = defaultCustomization)
    var draftIntensity by remember(customization.visualIntensity) {
        mutableStateOf(customization.visualIntensity.toFloat())
    }
    val selectedMode = remember(spec, customization) { spec.currentMode(customization) }

    fun saveCustomization(update: UtilityCustomization) {
        scope.launch {
            prefs.setUtilityCustomization(appId, update)
        }
    }

    fun applyMode(mode: UtilityModePreset) {
        val updated = customization.copy(selectedModeId = mode.id)
        saveCustomization(updated)
        if (updated.autoThemeSync) {
            themeManager.setThemeById(mode.recommendedThemeId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(spec.studioTitle, color = colors.onSurface)
                        Text(
                            spec.fallbackApp.name,
                            color = colors.primary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedThemeBackground(appId = appId)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                colors.background.copy(alpha = 0.95f),
                                colors.background.copy(alpha = 0.86f),
                                colors.background.copy(alpha = 0.78f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                UtilityHeroCard(
                    spec = spec,
                    colors = colors,
                    animIntensity = runtime.animationIntensity,
                    selectedMode = selectedMode,
                    customization = customization,
                    visualIntensity = draftIntensity
                )

                StudioQuickActionsCard(
                    spec = spec,
                    colors = colors,
                    onPreviewClick = onPreviewClick,
                    onThemeStudioClick = onThemeStudioClick,
                    onNearbyShareClick = onNearbyShareClick
                )

                UtilityModesCard(
                    spec = spec,
                    colors = colors,
                    selectedMode = selectedMode,
                    onModeSelected = ::applyMode
                )

                VisualIntensityCard(
                    colors = colors,
                    value = draftIntensity,
                    onValueChange = { draftIntensity = it },
                    onValueChangeFinished = {
                        saveCustomization(customization.copy(visualIntensity = draftIntensity.toInt()))
                    }
                )

                UtilitySwitchesCard(
                    colors = colors,
                    customization = customization,
                    onRibbonsChanged = { saveCustomization(customization.copy(ribbonsEnabled = it)) },
                    onTrailBadgesChanged = { saveCustomization(customization.copy(trailBadgesEnabled = it)) },
                    onAutoThemeSyncChanged = {
                        val updated = customization.copy(autoThemeSync = it)
                        saveCustomization(updated)
                        if (it) {
                            themeManager.setThemeById(selectedMode.recommendedThemeId)
                        }
                    }
                )

                UtilityFeatureHighlightsCard(spec = spec, colors = colors)

                RecommendedThemesCard(
                    spec = spec,
                    activeThemeId = runtime.theme.id,
                    colors = colors,
                    onThemeSelected = { themeId -> themeManager.setThemeById(themeId) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UtilityHeroCard(
    spec: PhaseOneUtilitySpec,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    animIntensity: Float,
    selectedMode: UtilityModePreset,
    customization: UtilityCustomization,
    visualIntensity: Float
) {
    val pulse = rememberInfiniteTransition(label = "utility_studio_pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_scale"
    )
    val intensityFactor = (visualIntensity / 100f).coerceIn(0.45f, 1.2f)

    ThemeAwareCard(intensity = animIntensity, pulseEnabled = true) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                spec.heroColors.map { it.copy(alpha = 0.3f + (0.4f * intensityFactor)) }
                            )
                        )
                        .padding(18.dp)
                ) {
                    if (customization.ribbonsEnabled) {
                        FlowRow(
                            modifier = Modifier.align(Alignment.TopStart),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("MAX visuals", "Candy sync", "Utility rush").forEach { label ->
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = colors.surface.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        color = colors.onSurface,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(110.dp)
                            .scale(pulse.value)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    spec.heroColors + listOf(spec.heroColors.first())
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = spec.emojiIcon,
                            style = MaterialTheme.typography.displayLarge
                        )
                    }

                    if (customization.trailBadgesEnabled) {
                        Surface(
                            modifier = Modifier.align(Alignment.BottomEnd),
                            shape = RoundedCornerShape(16.dp),
                            color = colors.surface.copy(alpha = 0.75f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = spec.heroColors.first(),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Trail-ready",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = spec.fallbackApp.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = spec.shortTagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = spec.heroColors.first()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = selectedMode.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = selectedMode.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.72f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (spec.appId == PhaseOneUtilities.SUGAR_FILES.appId) {
                    SugarFilesPreview(
                        spec = spec,
                        colors = colors,
                        intensityFactor = intensityFactor,
                        showTrailBadges = customization.trailBadgesEnabled
                    )
                } else {
                    TaffySendPreview(
                        spec = spec,
                        colors = colors,
                        intensityFactor = intensityFactor,
                        showRibbon = customization.ribbonsEnabled,
                        showTrailBadges = customization.trailBadgesEnabled
                    )
                }
            }
        }
    }
}

@Composable
private fun SugarFilesPreview(
    spec: PhaseOneUtilitySpec,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    intensityFactor: Float,
    showTrailBadges: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf("Vault", "Screens", "Drops").forEachIndexed { index, label ->
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = spec.heroColors[index].copy(alpha = 0.18f + (0.12f * intensityFactor))
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = spec.heroColors[index]
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (showTrailBadges) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.surface.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = if (index == 0) "Pinned" else "Sweet",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaffySendPreview(
    spec: PhaseOneUtilitySpec,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    intensityFactor: Float,
    showRibbon: Boolean,
    showTrailBadges: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DevicePreviewCard(
                title = "Laptop",
                subtitle = "Ready to receive",
                accent = spec.heroColors.first(),
                colors = colors
            )
            DevicePreviewCard(
                title = "Phone",
                subtitle = "Burst relay",
                accent = spec.heroColors[1],
                colors = colors
            )
        }
        LinearProgressIndicator(
            progress = { intensityFactor.coerceIn(0.2f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50)),
            color = spec.heroColors[1],
            trackColor = spec.heroColors.last().copy(alpha = 0.2f)
        )
        if (showRibbon || showTrailBadges) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (showRibbon) {
                    UtilityBadge(label = "Candy beam", accent = spec.heroColors.first(), colors = colors)
                }
                if (showTrailBadges) {
                    UtilityBadge(label = "QR burst", accent = spec.heroColors[1], colors = colors)
                }
            }
        }
    }
}

@Composable
private fun DevicePreviewCard(
    title: String,
    subtitle: String,
    accent: Color,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.18f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
private fun UtilityBadge(
    label: String,
    accent: Color,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.16f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = colors.onSurface,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StudioQuickActionsCard(
    spec: PhaseOneUtilitySpec,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onPreviewClick: () -> Unit,
    onThemeStudioClick: () -> Unit,
    onNearbyShareClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Quick actions",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Jump into previews, theme changes, and utility-specific shortcuts.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.72f)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onPreviewClick, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Preview")
                }
                OutlinedButton(onClick = onThemeStudioClick) {
                    Icon(Icons.Default.Palette, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Themes")
                }
                if (spec.supportsNearbyShare) {
                    OutlinedButton(onClick = onNearbyShareClick) {
                        Icon(Icons.Default.Wifi, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Lab")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UtilityModesCard(
    spec: PhaseOneUtilitySpec,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    selectedMode: UtilityModePreset,
    onModeSelected: (UtilityModePreset) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Mode presets",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                spec.customizationModes.forEach { mode ->
                    FilterChip(
                        selected = selectedMode.id == mode.id,
                        onClick = { onModeSelected(mode) },
                        label = { Text(mode.title) }
                    )
                }
            }
            Text(
                text = selectedMode.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = spec.heroColors.first()
            )
            Text(
                text = selectedMode.description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
private fun VisualIntensityCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Visual intensity",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${value.toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.primary
                )
            }
            Text(
                text = "Scale hero glow, badge depth, and preview punch for this app only.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.72f)
            )
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 40f..100f,
                onValueChangeFinished = onValueChangeFinished
            )
        }
    }
}

@Composable
private fun UtilitySwitchesCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    customization: UtilityCustomization,
    onRibbonsChanged: (Boolean) -> Unit,
    onTrailBadgesChanged: (Boolean) -> Unit,
    onAutoThemeSyncChanged: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Live toggles",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            StudioToggleRow(
                title = "Candy ribbons",
                subtitle = "Add animated overlays and transfer beams.",
                checked = customization.ribbonsEnabled,
                onCheckedChange = onRibbonsChanged
            )
            StudioToggleRow(
                title = "Trail badges",
                subtitle = "Show stash chips, quick labels, and launch markers.",
                checked = customization.trailBadgesEnabled,
                onCheckedChange = onTrailBadgesChanged
            )
            StudioToggleRow(
                title = "Auto theme sync",
                subtitle = "Apply the mode's matching theme when you switch presets.",
                checked = customization.autoThemeSync,
                onCheckedChange = onAutoThemeSyncChanged
            )
        }
    }
}

@Composable
private fun StudioToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UtilityFeatureHighlightsCard(
    spec: PhaseOneUtilitySpec,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Phase 1 features",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                spec.featureHighlights.forEachIndexed { index, feature ->
                    FeatureCard(
                        feature = feature,
                        accent = spec.heroColors[index % spec.heroColors.size],
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    feature: UtilityFeatureHighlight,
    accent: Color,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = featureIcon(feature.iconName),
                    contentDescription = null,
                    tint = accent
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.72f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecommendedThemesCard(
    spec: PhaseOneUtilitySpec,
    activeThemeId: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onThemeSelected: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Recommended themes",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap any theme to skin the studio instantly.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.72f)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                spec.recommendedThemeIds.forEach { themeId ->
                    FilterChip(
                        selected = activeThemeId == themeId,
                        onClick = { onThemeSelected(themeId) },
                        label = {
                            Text(ThemePresets.getById(themeId)?.name ?: themeId)
                        }
                    )
                }
            }
        }
    }
}

private fun featureIcon(iconName: String): ImageVector = when (iconName) {
    "folder" -> Icons.Default.FolderOpen
    "palette" -> Icons.Default.Palette
    "star" -> Icons.Default.Star
    "qr" -> Icons.Default.QrCode2
    "wifi" -> Icons.Default.Wifi
    "auto_awesome" -> Icons.Default.AutoAwesome
    else -> Icons.Default.RocketLaunch
}
