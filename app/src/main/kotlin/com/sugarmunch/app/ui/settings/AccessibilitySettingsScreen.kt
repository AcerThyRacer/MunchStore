package com.sugarmunch.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.accessibility.AccessibilityManager
import com.sugarmunch.app.accessibility.ColorBlindMode
import com.sugarmunch.app.accessibility.MotionReductionLevel

/**
 * Comprehensive accessibility settings
 */
data class AccessibilitySettingsState(
    // Vision
    val highContrastMode: Boolean = false,
    val colorblindMode: ColorblindModeType? = null,
    val textScaleMultiplier: Float = 1.0f,
    val dyslexiaFontEnabled: Boolean = false,
    val textSpacingLevel: TextSpacingLevel = TextSpacingLevel.MEDIUM,
    
    // Hearing
    val visualSoundIndicators: Boolean = false,
    val hapticFeedbackStrength: Float = 1.0f,
    
    // Motor
    val largeTouchTargets: Boolean = false,
    val touchHoldDuration: Long = 500L,
    val gestureSensitivity: GestureSensitivityType = GestureSensitivityType.NORMAL,
    
    // Cognitive
    val simplifiedUILayout: Boolean = false,
    val reduceAnimations: Boolean = false,
    val focusMode: Boolean = false,
    val epilepsySafeMode: Boolean = false,
    
    // Screen reader
    val screenReaderOptimizations: Boolean = false,
    val detailedDescriptions: Boolean = false
)

enum class ColorblindModeType {
    NONE,
    DEUTERANOPIA,
    PROTANOPIA,
    TRITANOPIA,
    ACHROMATOPSIA,
    BLUE_CONE_MONOCHROMACY,
    ENHANCED_DISTINCTION
}

enum class TextSpacingLevel {
    TIGHT,
    MEDIUM,
    RELAXED,
    WIDE
}

enum class GestureSensitivityType {
    LOW,
    NORMAL,
    HIGH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilitySettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    val accessibilityManager = remember { AccessibilityManager.getInstance(context) }
    
    var settings by remember { mutableStateOf(AccessibilitySettingsState()) }
    var showTextScaleDialog by remember { mutableStateOf(false) }
    var showTouchHoldDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Accessibility",
                        color = colors.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colors.onSurface
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colors.background.copy(alpha = 0.95f),
                                colors.background.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Vision Section
                AccessibilitySectionCard(
                    title = "Vision",
                    icon = Icons.Default.Visibility,
                    colors = colors
                ) {
                    // High Contrast Mode
                    AccessibilityToggleRow(
                        icon = Icons.Default.Contrast,
                        label = "High Contrast Mode",
                        description = "Maximize contrast for better visibility",
                        checked = settings.highContrastMode,
                        onCheckedChange = {
                            settings = settings.copy(highContrastMode = it)
                            accessibilityManager.setHighContrastEnabled(it)
                        }
                    )

                    // Colorblind Modes
                    Text(
                        text = "Colorblind Mode",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ColorblindModeType.entries) { mode ->
                            FilterChip(
                                selected = settings.colorblindMode == mode,
                                onClick = {
                                    settings = settings.copy(colorblindMode = mode)
                                    val managerMode = when (mode) {
                                        ColorblindModeType.DEUTERANOPIA -> ColorBlindMode.DEUTERANOPIA
                                        ColorblindModeType.PROTANOPIA -> ColorBlindMode.PROTANOPIA
                                        ColorblindModeType.TRITANOPIA -> ColorBlindMode.TRITANOPIA
                                        else -> null
                                    }
                                    managerMode?.let { accessibilityManager.setColorBlindMode(it) }
                                },
                                label = { 
                                    Text(
                                        mode.name.replaceFirstChar { it.lowercase() }
                                            .replace("_", " "),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primary
                                )
                            )
                        }
                    }

                    // Text Scale
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.TextFields,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Text Scale",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.onSurface
                            )
                            Text(
                                text = "${(settings.textScaleMultiplier * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Button(
                            onClick = { showTextScaleDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary
                            )
                        ) {
                            Text("Adjust")
                        }
                    }

                    // Dyslexia Font
                    Spacer(modifier = Modifier.height(12.dp))

                    AccessibilityToggleRow(
                        icon = Icons.Default.Title,
                        label = "Dyslexia-Friendly Font",
                        description = "Use fonts designed for dyslexic readers",
                        checked = settings.dyslexiaFontEnabled,
                        onCheckedChange = {
                            settings = settings.copy(dyslexiaFontEnabled = it)
                        }
                    )

                    // Text Spacing
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Text Spacing",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextSpacingLevel.entries.forEach { level ->
                            FilterChip(
                                selected = settings.textSpacingLevel == level,
                                onClick = {
                                    settings = settings.copy(textSpacingLevel = level)
                                },
                                label = { Text(level.name.replaceFirstChar { it.lowercase() }) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hearing Section
                AccessibilitySectionCard(
                    title = "Hearing",
                    icon = Icons.Default.Hearing,
                    colors = colors
                ) {
                    AccessibilityToggleRow(
                        icon = Icons.Default.VolumeUp,
                        label = "Visual Sound Indicators",
                        description = "Show visual cues for audio notifications",
                        checked = settings.visualSoundIndicators,
                        onCheckedChange = {
                            settings = settings.copy(visualSoundIndicators = it)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Vibration,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Haptic Feedback Strength",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.onSurface
                            )
                            Text(
                                text = "${(settings.hapticFeedbackStrength * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Slider(
                            value = settings.hapticFeedbackStrength,
                            onValueChange = {
                                settings = settings.copy(hapticFeedbackStrength = it)
                            },
                            valueRange = 0f..1f,
                            steps = 9,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Motor Section
                AccessibilitySectionCard(
                    title = "Motor",
                    icon = Icons.Default.TouchApp,
                    colors = colors
                ) {
                    AccessibilityToggleRow(
                        icon = Icons.Default.PanTool,
                        label = "Large Touch Targets",
                        description = "Increase button and control sizes",
                        checked = settings.largeTouchTargets,
                        onCheckedChange = {
                            settings = settings.copy(largeTouchTargets = it)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Touch Hold Duration",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.onSurface
                            )
                            Text(
                                text = "${settings.touchHoldDuration}ms",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Button(
                            onClick = { showTouchHoldDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary
                            )
                        ) {
                            Text("Adjust")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Gesture Sensitivity",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GestureSensitivityType.entries.forEach { sensitivity ->
                            FilterChip(
                                selected = settings.gestureSensitivity == sensitivity,
                                onClick = {
                                    settings = settings.copy(gestureSensitivity = sensitivity)
                                },
                                label = { Text(sensitivity.name.replaceFirstChar { it.lowercase() }) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cognitive Section
                AccessibilitySectionCard(
                    title = "Cognitive",
                    icon = Icons.Default.Psychology,
                    colors = colors
                ) {
                    AccessibilityToggleRow(
                        icon = Icons.Default.ViewSimple,
                        label = "Simplified UI Layout",
                        description = "Reduce visual complexity",
                        checked = settings.simplifiedUILayout,
                        onCheckedChange = {
                            settings = settings.copy(simplifiedUILayout = it)
                        }
                    )

                    AccessibilityToggleRow(
                        icon = Icons.Default.SlowMotionVideo,
                        label = "Reduce Animations",
                        description = "Minimize motion and transitions",
                        checked = settings.reduceAnimations,
                        onCheckedChange = {
                            settings = settings.copy(reduceAnimations = it)
                            accessibilityManager.setMotionReduction(
                                if (it) MotionReductionLevel.REDUCED else MotionReductionLevel.NONE
                            )
                        }
                    )

                    AccessibilityToggleRow(
                        icon = Icons.Default.CenterFocusStrong,
                        label = "Focus Mode",
                        description = "Highlight focused elements",
                        checked = settings.focusMode,
                        onCheckedChange = {
                            settings = settings.copy(focusMode = it)
                        }
                    )

                    AccessibilityToggleRow(
                        icon = Icons.Default.Warning,
                        label = "Epilepsy-Safe Mode",
                        description = "Prevent flashing and rapid transitions",
                        checked = settings.epilepsySafeMode,
                        onCheckedChange = {
                            settings = settings.copy(epilepsySafeMode = it)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Screen Reader Section
                AccessibilitySectionCard(
                    title = "Screen Reader",
                    icon = Icons.Default.RecordVoiceOver,
                    colors = colors
                ) {
                    AccessibilityToggleRow(
                        icon = Icons.Default.Accessibility,
                        label = "Screen Reader Optimizations",
                        description = "Enhanced TalkBack/VoiceOver support",
                        checked = settings.screenReaderOptimizations,
                        onCheckedChange = {
                            settings = settings.copy(screenReaderOptimizations = it)
                        }
                    )

                    AccessibilityToggleRow(
                        icon = Icons.Default.Description,
                        label = "Detailed Descriptions",
                        description = "More verbose element descriptions",
                        checked = settings.detailedDescriptions,
                        onCheckedChange = {
                            settings = settings.copy(detailedDescriptions = it)
                        }
                    )
                }

                // Text Scale Dialog
                if (showTextScaleDialog) {
                    TextScaleDialog(
                        currentScale = settings.textScaleMultiplier,
                        onDismiss = { showTextScaleDialog = false },
                        onScaleChange = { scale ->
                            settings = settings.copy(textScaleMultiplier = scale)
                        }
                    )
                }

                // Touch Hold Dialog
                if (showTouchHoldDialog) {
                    TouchHoldDialog(
                        currentDuration = settings.touchHoldDuration,
                        onDismiss = { showTouchHoldDialog = false },
                        onDurationChange = { duration ->
                            settings = settings.copy(touchHoldDuration = duration)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AccessibilitySectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    content: @Composable ColumnScope.() -> Unit
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
private fun AccessibilityToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun TextScaleDialog(
    currentScale: Float,
    onDismiss: () -> Unit,
    onScaleChange: (Float) -> Unit
) {
    var scale by remember { mutableFloatStateOf(currentScale) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.TextFields, contentDescription = null)
        },
        title = { Text("Text Scale") },
        text = {
            Column {
                Text(
                    text = "${(scale * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = scale,
                    onValueChange = { scale = it },
                    valueRange = 0.8f..1.5f,
                    steps = 13
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Preview
                Text(
                    text = "Preview Text",
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize * scale,
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onScaleChange(scale)
                onDismiss()
            }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TouchHoldDialog(
    currentDuration: Long,
    onDismiss: () -> Unit,
    onDurationChange: (Long) -> Unit
) {
    var duration by remember { mutableLongStateOf(currentDuration) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.TouchApp, contentDescription = null)
        },
        title = { Text("Touch Hold Duration") },
        text = {
            Column {
                Text(
                    text = "${duration}ms",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = duration.toFloat(),
                    onValueChange = { duration = it.toLong() },
                    valueRange = 300f..2000f,
                    steps = 17
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Longer duration helps prevent accidental long presses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onDurationChange(duration)
                onDismiss()
            }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
