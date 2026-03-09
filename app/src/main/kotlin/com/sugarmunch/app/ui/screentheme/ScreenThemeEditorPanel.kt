package com.sugarmunch.app.ui.screentheme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.ui.design.SugarDimens
import com.sugarmunch.app.ui.visual.GradientSpec
import com.sugarmunch.app.ui.visual.GradientStop
import com.sugarmunch.app.ui.visual.GradientType
import kotlinx.coroutines.launch

// ── Preset colors for quick selection ──────────────────────────────────

private val PRESET_COLORS = listOf(
    Color(0xFFE91E63), Color(0xFFF44336), Color(0xFFFF9800), Color(0xFFFFC107),
    Color(0xFF4CAF50), Color(0xFF009688), Color(0xFF03A9F4), Color(0xFF2196F3),
    Color(0xFF673AB7), Color(0xFF9C27B0), Color(0xFF795548), Color(0xFF607D8B),
    Color(0xFFFFFFFF), Color(0xFF212121), Color(0xFFFF6EC7), Color(0xFF7FFF00)
)

private val GRADIENT_PRESETS = listOf(
    GradientSpec(GradientType.LINEAR, listOf(GradientStop(Color(0xFFFF6B6B), 0f), GradientStop(Color(0xFFFFC371), 1f)), angleDegrees = 135f),
    GradientSpec(GradientType.LINEAR, listOf(GradientStop(Color(0xFF667EEA), 0f), GradientStop(Color(0xFF764BA2), 1f)), angleDegrees = 135f),
    GradientSpec(GradientType.LINEAR, listOf(GradientStop(Color(0xFF11998E), 0f), GradientStop(Color(0xFF38EF7D), 1f)), angleDegrees = 90f),
    GradientSpec(GradientType.RADIAL, listOf(GradientStop(Color(0xFFFC5C7D), 0f), GradientStop(Color(0xFF6A82FB), 1f))),
    GradientSpec(GradientType.LINEAR, listOf(GradientStop(Color(0xFFF093FB), 0f), GradientStop(Color(0xFFF5576C), 1f)), angleDegrees = 45f),
    GradientSpec(GradientType.SWEEP, listOf(GradientStop(Color(0xFFFFD700), 0f), GradientStop(Color(0xFFFF6347), 0.5f), GradientStop(Color(0xFFFFD700), 1f))),
)

// ── App screens ────────────────────────────────────────────────────────

private data class ScreenInfo(val route: String, val displayName: String)

private val APP_SCREENS = listOf(
    ScreenInfo("catalog", "Catalog"),
    ScreenInfo("effects", "Effects"),
    ScreenInfo("theme", "Theme"),
    ScreenInfo("settings", "Settings"),
    ScreenInfo("shop", "Shop"),
    ScreenInfo("rewards", "Rewards"),
    ScreenInfo("detail", "Detail"),
    ScreenInfo("events", "Events"),
)

// ── Editor Panel ───────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScreenThemeEditorPanel(
    screenRoute: String,
    screenName: String,
    currentOverride: ScreenThemeOverride?,
    onSave: (ScreenThemeOverride) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val themeColors = currentTheme.getColorsForIntensity(themeIntensity)

    var primaryColor by remember { mutableStateOf(currentOverride?.primaryColor) }
    var secondaryColor by remember { mutableStateOf(currentOverride?.secondaryColor) }
    var tertiaryColor by remember { mutableStateOf(currentOverride?.tertiaryColor) }
    var surfaceColor by remember { mutableStateOf(currentOverride?.surfaceColor) }
    var backgroundColor by remember { mutableStateOf(currentOverride?.backgroundColor) }
    var gradient by remember { mutableStateOf(currentOverride?.gradient) }
    var gradientEnabled by remember { mutableStateOf(currentOverride?.gradient != null) }
    var intensity by remember { mutableFloatStateOf(currentOverride?.intensity ?: 1.0f) }
    var activeColorSlot by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = SugarDimens.radiusLg, topEnd = SugarDimens.radiusLg),
        tonalElevation = SugarDimens.elevationHigh,
        shadowElevation = SugarDimens.elevationHigh
    ) {
        Column(modifier = Modifier.padding(SugarDimens.md)) {
            // ── Header ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customize: $screenName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = onReset) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            Spacer(Modifier.height(SugarDimens.sm))

            // ── Color overrides ────────────────────────────────────
            Text("Colors", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(SugarDimens.xs))

            val colorSlots = listOf(
                "Primary" to (primaryColor to themeColors.primary),
                "Secondary" to (secondaryColor to themeColors.secondary),
                "Tertiary" to (tertiaryColor to themeColors.tertiary),
                "Surface" to (surfaceColor to themeColors.surface),
                "Background" to (backgroundColor to themeColors.background),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                colorSlots.forEach { (label, pair) ->
                    val (overrideColor, defaultColor) = pair
                    val isActive = activeColorSlot == label
                    ColorCircle(
                        color = overrideColor,
                        defaultColor = defaultColor,
                        label = label,
                        selected = isActive,
                        onClick = { activeColorSlot = if (isActive) null else label }
                    )
                }
            }

            // ── Inline color picker ────────────────────────────────
            AnimatedVisibility(
                visible = activeColorSlot != null,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                activeColorSlot?.let { slot ->
                    CompactColorPicker(
                        selectedColor = when (slot) {
                            "Primary" -> primaryColor
                            "Secondary" -> secondaryColor
                            "Tertiary" -> tertiaryColor
                            "Surface" -> surfaceColor
                            "Background" -> backgroundColor
                            else -> null
                        },
                        onColorSelected = { color ->
                            when (slot) {
                                "Primary" -> primaryColor = color
                                "Secondary" -> secondaryColor = color
                                "Tertiary" -> tertiaryColor = color
                                "Surface" -> surfaceColor = color
                                "Background" -> backgroundColor = color
                            }
                        },
                        onClear = {
                            when (slot) {
                                "Primary" -> primaryColor = null
                                "Secondary" -> secondaryColor = null
                                "Tertiary" -> tertiaryColor = null
                                "Surface" -> surfaceColor = null
                                "Background" -> backgroundColor = null
                            }
                        },
                        modifier = Modifier.padding(vertical = SugarDimens.sm)
                    )
                }
            }

            Spacer(Modifier.height(SugarDimens.md))

            // ── Gradient section ───────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Custom Gradient", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = gradientEnabled,
                    onCheckedChange = { enabled ->
                        gradientEnabled = enabled
                        if (!enabled) gradient = null
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                )
            }

            AnimatedVisibility(
                visible = gradientEnabled,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SugarDimens.xs),
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.xs),
                    verticalArrangement = Arrangement.spacedBy(SugarDimens.xs)
                ) {
                    GRADIENT_PRESETS.forEach { preset ->
                        GradientPresetChip(
                            gradientSpec = preset,
                            selected = gradient == preset,
                            onClick = { gradient = preset }
                        )
                    }
                }
            }

            Spacer(Modifier.height(SugarDimens.md))

            // ── Intensity slider ───────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Intensity", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    text = String.format("%.1f", intensity),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = intensity,
                onValueChange = { intensity = it },
                valueRange = 0.3f..2.0f,
                steps = 16,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(SugarDimens.md))

            // ── Apply button ───────────────────────────────────────
            Button(
                onClick = {
                    onSave(
                        ScreenThemeOverride(
                            screenRoute = screenRoute,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            tertiaryColor = tertiaryColor,
                            surfaceColor = surfaceColor,
                            backgroundColor = backgroundColor,
                            gradient = if (gradientEnabled) gradient else null,
                            pattern = currentOverride?.pattern,
                            intensity = intensity
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(SugarDimens.radiusMd),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Apply", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Color circle (shows override or dashed default) ────────────────────

@Composable
private fun ColorCircle(
    color: Color?,
    defaultColor: Color,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val displayColor by animateColorAsState(
        targetValue = color ?: defaultColor,
        animationSpec = tween(250),
        label = "circle_$label"
    )
    val isOverridden = color != null
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .then(
                    if (isOverridden) {
                        Modifier
                            .background(displayColor, CircleShape)
                            .border(BorderStroke(2.dp, borderColor), CircleShape)
                    } else {
                        Modifier
                            .background(displayColor.copy(alpha = 0.4f), CircleShape)
                            .drawBehind {
                                drawRoundRect(
                                    color = defaultColor.copy(alpha = 0.6f),
                                    cornerRadius = CornerRadius(size.minDimension / 2),
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(6.dp.toPx(), 4.dp.toPx())
                                        )
                                    )
                                )
                            }
                    }
                )
                .clickable(onClick = onClick)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
    }
}

// ── Compact color picker ───────────────────────────────────────────────

@Composable
private fun CompactColorPicker(
    selectedColor: Color?,
    onColorSelected: (Color) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var hexInput by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = SugarDimens.xs),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.xs)
        ) {
            items(PRESET_COLORS) { color ->
                val isSelected = selectedColor == color
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color, CircleShape)
                        .border(
                            BorderStroke(
                                if (isSelected) 2.dp else 0.dp,
                                if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent
                            ),
                            CircleShape
                        )
                        .clickable { onColorSelected(color) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            modifier = Modifier.size(16.dp),
                            tint = if (color.luminance() > 0.5f) Color.Black else Color.White
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(SugarDimens.xs))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.xs)
        ) {
            OutlinedTextField(
                value = hexInput,
                onValueChange = { input ->
                    hexInput = input.filter { it.isLetterOrDigit() || it == '#' }.take(9)
                },
                label = { Text("Hex") },
                placeholder = { Text("#FF5722") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    parseHexColor(hexInput)?.let(onColorSelected)
                    focusManager.clearFocus()
                })
            )
            Button(
                onClick = {
                    parseHexColor(hexInput)?.let(onColorSelected)
                    focusManager.clearFocus()
                },
                contentPadding = PaddingValues(horizontal = SugarDimens.sm),
                shape = RoundedCornerShape(SugarDimens.radiusSm)
            ) {
                Text("Set")
            }
            Button(
                onClick = {
                    onClear()
                    hexInput = ""
                },
                contentPadding = PaddingValues(horizontal = SugarDimens.sm),
                shape = RoundedCornerShape(SugarDimens.radiusSm),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Clear")
            }
        }
    }
}

private fun parseHexColor(input: String): Color? {
    val hex = input.removePrefix("#").trim()
    return try {
        when (hex.length) {
            6 -> Color(android.graphics.Color.parseColor("#$hex"))
            8 -> Color(android.graphics.Color.parseColor("#$hex"))
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

private fun Color.luminance(): Float = 0.299f * red + 0.587f * green + 0.114f * blue

// ── Gradient preset chip ───────────────────────────────────────────────

@Composable
private fun GradientPresetChip(
    gradientSpec: GradientSpec,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = gradientSpec.stops.map { it.color }
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent

    Box(
        modifier = Modifier
            .size(width = 56.dp, height = 36.dp)
            .clip(RoundedCornerShape(SugarDimens.radiusSm))
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(colors),
                shape = RoundedCornerShape(SugarDimens.radiusSm)
            )
            .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(SugarDimens.radiusSm))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                modifier = Modifier.size(16.dp),
                tint = Color.White
            )
        }
    }
}

// ── Screen Theme Settings (full screen list) ───────────────────────────

@Composable
fun ScreenThemeSettings(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val screenThemeManager = remember { ScreenThemeManager.getInstance(context) }
    val overrides by screenThemeManager.overrides.collectAsState()
    var editingScreen by remember { mutableStateOf<ScreenInfo?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        // ── Header ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SugarDimens.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Screen Themes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        // ── Screen list ────────────────────────────────────────
        LazyColumn(
            contentPadding = PaddingValues(horizontal = SugarDimens.md),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.xs)
        ) {
            items(APP_SCREENS, key = { it.route }) { screen ->
                val override = overrides[screen.route]
                ScreenThemeRow(
                    screenName = screen.displayName,
                    override = override,
                    onClick = { editingScreen = screen }
                )
            }
        }

        // ── Inline editor ──────────────────────────────────────
        editingScreen?.let { screen ->
            Spacer(Modifier.height(SugarDimens.sm))
            ScreenThemeEditorPanel(
                screenRoute = screen.route,
                screenName = screen.displayName,
                currentOverride = overrides[screen.route],
                onSave = { override ->
                    screenThemeManager.setOverride(override)
                    editingScreen = null
                },
                onReset = {
                    screenThemeManager.removeOverride(screen.route)
                    editingScreen = null
                },
                onDismiss = { editingScreen = null }
            )
        }
    }
}

@Composable
private fun ScreenThemeRow(
    screenName: String,
    override: ScreenThemeOverride?,
    onClick: () -> Unit
) {
    val hasOverride = override != null

    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(SugarDimens.radiusMd),
        tonalElevation = if (hasOverride) SugarDimens.elevationMedium else SugarDimens.elevationSubtle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SugarDimens.md, vertical = SugarDimens.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = screenName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (hasOverride) FontWeight.SemiBold else FontWeight.Normal
                )
                if (hasOverride) {
                    Spacer(Modifier.width(SugarDimens.sm))
                    ColorPreviewDots(override)
                }
            }
            if (hasOverride) {
                Surface(
                    shape = RoundedCornerShape(SugarDimens.radiusSm),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Customized",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = SugarDimens.xs, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorPreviewDots(override: ScreenThemeOverride?) {
    if (override == null) return

    val dots = listOfNotNull(
        override.primaryColor,
        override.secondaryColor,
        override.tertiaryColor,
        override.surfaceColor,
        override.backgroundColor
    )
    if (dots.isEmpty()) return

    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        dots.forEach { color ->
            val animatedColor by animateColorAsState(
                targetValue = color,
                animationSpec = tween(300),
                label = "dot"
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(animatedColor, CircleShape)
                    .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.2f)), CircleShape)
            )
        }
    }
}
