package com.sugarmunch.app.ui.widgetcustom

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlin.math.roundToInt

// ── Candy-themed preset palette ──────────────────────────────────────────

private val CandyPresetColors = listOf(
    Color(0xFFFF6B6B), // Strawberry
    Color(0xFFFF9F43), // Tangerine
    Color(0xFFFFD93D), // Lemon Drop
    Color(0xFF6BCB77), // Lime
    Color(0xFF4D96FF), // Blueberry
    Color(0xFF9B59B6), // Grape
    Color(0xFFFF6B9D), // Bubblegum
    Color(0xFF00D2D3), // Mint
    Color(0xFFF8B500), // Butterscotch
    Color(0xFFFF4757), // Cherry
)

// ═══════════════════════════════════════════════════════════════════════════
//  Main screen
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetCustomizerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val engine = remember { WidgetThemeEngine.getInstance(context) }

    val dailyAppearance by engine.dailyRewardAppearance.collectAsState()
    val quickAppearance by engine.quickInstallAppearance.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Daily Reward", "Quick Install")

    val currentAppearance = if (selectedTab == 0) dailyAppearance else quickAppearance

    var editedAppearance by remember(selectedTab, currentAppearance) {
        mutableStateOf(currentAppearance)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Widget Appearance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(SugarDimens.Spacing.md)
            ) {
                // ── Live preview ─────────────────────────────────────
                WidgetPreview(
                    widgetType = if (selectedTab == 0) "daily_reward" else "quick_install",
                    appearance = editedAppearance,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SugarDimens.Spacing.sm)
                )

                Spacer(modifier = Modifier.height(SugarDimens.Spacing.lg))

                // ── 1. Style selector ────────────────────────────────
                SectionLabel("Style")
                StyleSelector(
                    selected = editedAppearance.style,
                    onSelected = { editedAppearance = editedAppearance.copy(style = it) }
                )

                Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

                // ── 2. Corner radius ─────────────────────────────────
                SectionLabel("Corner Radius")
                CornerRadiusSlider(
                    selected = editedAppearance.cornerRadius,
                    onSelected = { editedAppearance = editedAppearance.copy(cornerRadius = it) }
                )

                Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

                // ── 3. Size ──────────────────────────────────────────
                SectionLabel("Size")
                SizeSelector(
                    selected = editedAppearance.size,
                    onSelected = { editedAppearance = editedAppearance.copy(size = it) }
                )

                Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

                // ── 4. Colour pickers (conditional) ──────────────────
                val showColorSection = editedAppearance.style in listOf(
                    WidgetStyle.SOLID_COLOR, WidgetStyle.GRADIENT, WidgetStyle.OUTLINED
                )
                if (showColorSection) {
                    SectionLabel("Colors")

                    WidgetColorPicker(
                        label = "Background",
                        selectedColor = editedAppearance.backgroundColor ?: CandyPresetColors[0],
                        onColorSelected = {
                            editedAppearance = editedAppearance.copy(backgroundColor = it)
                        }
                    )

                    if (editedAppearance.style == WidgetStyle.GRADIENT) {
                        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
                        WidgetColorPicker(
                            label = "Gradient End",
                            selectedColor = editedAppearance.gradientColors?.getOrNull(1)
                                ?: CandyPresetColors[4],
                            onColorSelected = { endColor ->
                                val start = editedAppearance.backgroundColor ?: CandyPresetColors[0]
                                editedAppearance = editedAppearance.copy(
                                    gradientColors = listOf(start, endColor)
                                )
                            }
                        )
                    }

                    if (editedAppearance.style == WidgetStyle.OUTLINED) {
                        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
                        WidgetColorPicker(
                            label = "Border",
                            selectedColor = editedAppearance.borderColor ?: CandyPresetColors[6],
                            onColorSelected = {
                                editedAppearance = editedAppearance.copy(borderColor = it)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))
                }

                // ── 5. Opacity ───────────────────────────────────────
                SectionLabel("Opacity — ${(editedAppearance.opacity * 100).roundToInt()}%")
                Slider(
                    value = editedAppearance.opacity,
                    onValueChange = { editedAppearance = editedAppearance.copy(opacity = it) },
                    valueRange = 0.5f..1f,
                    steps = 9,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))

                // ── 6. Text colour ───────────────────────────────────
                SectionLabel("Text Color")
                var useCustomText by remember { mutableStateOf(editedAppearance.textColor != null) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Custom", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(SugarDimens.Spacing.xs))
                    Switch(
                        checked = useCustomText,
                        onCheckedChange = { on ->
                            useCustomText = on
                            editedAppearance = if (on) {
                                editedAppearance.copy(textColor = Color.White)
                            } else {
                                editedAppearance.copy(textColor = null)
                            }
                        }
                    )
                }
                if (useCustomText) {
                    WidgetColorPicker(
                        label = "Text",
                        selectedColor = editedAppearance.textColor ?: Color.White,
                        onColorSelected = {
                            editedAppearance = editedAppearance.copy(textColor = it)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))

                // ── 7. Shadow toggle ─────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Show Shadow",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = editedAppearance.showShadow,
                        onCheckedChange = {
                            editedAppearance = editedAppearance.copy(showShadow = it)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(SugarDimens.Spacing.xl))

                // ── 8. Apply button ──────────────────────────────────
                Button(
                    onClick = {
                        if (selectedTab == 0) {
                            engine.updateDailyRewardWidget(editedAppearance)
                        } else {
                            engine.updateQuickInstallWidget(editedAppearance)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SugarDimens.Height.button),
                    shape = RoundedCornerShape(SugarDimens.Radius.md)
                ) {
                    Text("Apply to Widget", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxl))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  Widget preview
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun WidgetPreview(
    widgetType: String,
    appearance: WidgetAppearance,
    modifier: Modifier = Modifier
) {
    val animatedOpacity by animateFloatAsState(
        targetValue = appearance.opacity,
        animationSpec = tween(300),
        label = "preview_opacity"
    )
    val animatedRadius by animateDpAsState(
        targetValue = appearance.cornerRadius.radiusDp.dp,
        animationSpec = tween(300),
        label = "preview_radius"
    )
    val shadowElevation = if (appearance.showShadow) SugarDimens.Elevation.medium else 0.dp

    val bgBrush = resolvePreviewBrush(appearance)
    val textColor = appearance.textColor ?: Color.White

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .shadow(shadowElevation, RoundedCornerShape(animatedRadius))
            .clip(RoundedCornerShape(animatedRadius))
            .then(
                if (appearance.style == WidgetStyle.OUTLINED) {
                    Modifier.border(
                        width = appearance.borderWidth.dp,
                        color = appearance.borderColor ?: MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(animatedRadius)
                    )
                } else Modifier
            )
            .background(bgBrush, alpha = animatedOpacity)
            .padding(SugarDimens.Spacing.md)
    ) {
        val heightMod = when (appearance.size) {
            WidgetSize.COMPACT -> Modifier.height(100.dp)
            WidgetSize.STANDARD -> Modifier.height(140.dp)
            WidgetSize.EXPANDED -> Modifier.height(180.dp)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = heightMod.fillMaxWidth()
        ) {
            if (widgetType == "daily_reward") {
                DailyRewardPreviewContent(textColor)
            } else {
                QuickInstallPreviewContent(textColor)
            }
        }
    }
}

@Composable
private fun DailyRewardPreviewContent(textColor: Color) {
    val animColor by animateColorAsState(textColor, tween(300), label = "text_anim")

    Text("🎁", fontSize = 28.sp)
    Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))
    Text(
        "Daily Reward",
        color = animColor,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))
    Text("🔥 7-day streak", color = animColor.copy(alpha = 0.8f), fontSize = 13.sp)
    Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))

    // Placeholder claim button
    Canvas(modifier = Modifier.size(width = 100.dp, height = 28.dp)) {
        drawRoundRect(
            color = animColor.copy(alpha = 0.25f),
            cornerRadius = CornerRadius(14.dp.toPx())
        )
    }
}

@Composable
private fun QuickInstallPreviewContent(textColor: Color) {
    val animColor by animateColorAsState(textColor, tween(300), label = "text_anim")

    Text("⚡", fontSize = 28.sp)
    Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))
    Text(
        "Quick Install",
        color = animColor,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))

    Row(horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs)) {
        repeat(3) {
            Canvas(modifier = Modifier.size(36.dp)) {
                drawRoundRect(
                    color = animColor.copy(alpha = 0.2f),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
                drawCircle(color = animColor.copy(alpha = 0.4f), radius = size.minDimension / 4)
            }
        }
    }
}

private fun resolvePreviewBrush(appearance: WidgetAppearance): Brush {
    return when (appearance.style) {
        WidgetStyle.MATCH_APP -> Brush.verticalGradient(
            listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
        )
        WidgetStyle.TRANSPARENT -> Brush.verticalGradient(
            listOf(Color.Gray.copy(alpha = 0.15f), Color.Gray.copy(alpha = 0.08f))
        )
        WidgetStyle.FROSTED_GLASS -> Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.15f))
        )
        WidgetStyle.SOLID_COLOR -> Brush.linearGradient(
            listOf(
                appearance.backgroundColor ?: Color(0xFF1A1A2E),
                appearance.backgroundColor ?: Color(0xFF1A1A2E)
            )
        )
        WidgetStyle.GRADIENT -> {
            val colors = appearance.gradientColors
                ?: listOf(Color(0xFFFF6B6B), Color(0xFF4D96FF))
            Brush.linearGradient(colors)
        }
        WidgetStyle.OUTLINED -> Brush.verticalGradient(
            listOf(Color.Transparent, Color.Transparent)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  Style selector
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun StyleSelector(
    selected: WidgetStyle,
    onSelected: (WidgetStyle) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        WidgetStyle.entries.forEach { style ->
            val isSelected = style == selected
            val borderColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                tween(200),
                label = "style_border"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(76.dp)
                    .clip(RoundedCornerShape(SugarDimens.Radius.sm))
                    .border(2.dp, borderColor, RoundedCornerShape(SugarDimens.Radius.sm))
                    .clickable { onSelected(style) }
                    .padding(SugarDimens.Spacing.xs)
            ) {
                StyleMiniPreview(style)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    style.displayName,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
private fun StyleMiniPreview(style: WidgetStyle) {
    Canvas(modifier = Modifier.size(48.dp, 32.dp)) {
        val cr = CornerRadius(6.dp.toPx())
        when (style) {
            WidgetStyle.MATCH_APP -> drawRoundRect(Color(0xFF1A1A2E), cornerRadius = cr)
            WidgetStyle.TRANSPARENT -> {
                drawRoundRect(Color.Gray.copy(alpha = 0.15f), cornerRadius = cr)
                drawRoundRect(Color.Gray.copy(alpha = 0.3f), cornerRadius = cr, style = Stroke(1.dp.toPx()))
            }
            WidgetStyle.FROSTED_GLASS -> drawRoundRect(Color.White.copy(alpha = 0.4f), cornerRadius = cr)
            WidgetStyle.SOLID_COLOR -> drawRoundRect(Color(0xFFFF6B6B), cornerRadius = cr)
            WidgetStyle.GRADIENT -> drawRoundRect(
                Brush.linearGradient(listOf(Color(0xFFFF6B6B), Color(0xFF4D96FF))),
                cornerRadius = cr
            )
            WidgetStyle.OUTLINED -> drawRoundRect(
                Color.Gray, cornerRadius = cr, style = Stroke(1.5f.dp.toPx())
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  Corner radius slider
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun CornerRadiusSlider(
    selected: WidgetCornerRadius,
    onSelected: (WidgetCornerRadius) -> Unit
) {
    val values = WidgetCornerRadius.entries
    val index = values.indexOf(selected).coerceAtLeast(0)

    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            values.forEach { radius ->
                Text(
                    radius.displayName,
                    fontSize = 10.sp,
                    color = if (radius == selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (radius == selected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Slider(
            value = index.toFloat(),
            onValueChange = { onSelected(values[it.roundToInt().coerceIn(0, values.lastIndex)]) },
            valueRange = 0f..values.lastIndex.toFloat(),
            steps = values.size - 2,
            modifier = Modifier.fillMaxWidth()
        )

        // Live visual indicator
        val animRadius by animateDpAsState(selected.radiusDp.dp, tween(250), label = "rad")
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(60.dp, 36.dp)
                .clip(RoundedCornerShape(animRadius))
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  Size selector (segmented button row)
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun SizeSelector(
    selected: WidgetSize,
    onSelected: (WidgetSize) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs),
        modifier = Modifier.fillMaxWidth()
    ) {
        WidgetSize.entries.forEach { size ->
            val isSelected = size == selected
            val bgColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                tween(200),
                label = "seg_bg"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(SugarDimens.Height.buttonSmall)
                    .clip(RoundedCornerShape(SugarDimens.Radius.sm))
                    .background(bgColor)
                    .clickable { onSelected(size) }
            ) {
                Text(
                    size.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  Colour picker
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun WidgetColorPicker(
    label: String,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var hexInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xxs),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            // Current colour indicator (tappable to toggle hex input)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(selectedColor)
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable { expanded = !expanded }
            )

            Spacer(modifier = Modifier.width(SugarDimens.Spacing.xxs))

            // Preset swatches
            CandyPresetColors.forEach { color ->
                val borderColor by animateColorAsState(
                    if (color == selectedColor) MaterialTheme.colorScheme.primary
                    else Color.Transparent,
                    tween(150),
                    label = "swatch_border"
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(2.dp, borderColor, CircleShape)
                        .clickable { onColorSelected(color) }
                )
            }
        }

        // Hex input (expanded)
        if (expanded) {
            Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs)
            ) {
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { hexInput = it.take(7) },
                    label = { Text("Hex (#RRGGBB)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            parseHexColor(hexInput)?.let(onColorSelected)
                            focusManager.clearFocus()
                        }
                    ),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        parseHexColor(hexInput)?.let(onColorSelected)
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.height(SugarDimens.Height.buttonSmall),
                    contentPadding = ButtonDefaults.TextButtonContentPadding
                ) {
                    Text("Set")
                }
            }
        }
    }
}

private fun parseHexColor(hex: String): Color? {
    val cleaned = hex.removePrefix("#").trim()
    if (cleaned.length != 6) return null
    return try {
        Color(("FF$cleaned").toLong(16).toInt())
    } catch (_: NumberFormatException) {
        null
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  Helpers
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = SugarDimens.Spacing.xs)
    )
}
