package com.sugarmunch.app.ui.studio

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.ui.design.CardStyle
import com.sugarmunch.app.ui.design.SugarCard
import com.sugarmunch.app.ui.design.SugarDimens
import com.sugarmunch.app.ui.typography.FontPairings
import com.sugarmunch.app.ui.typography.SugarFontFamily
import com.sugarmunch.app.ui.visual.GradientPresets
import com.sugarmunch.app.ui.visual.GradientSpec
import com.sugarmunch.app.ui.visual.GradientStop
import com.sugarmunch.app.ui.visual.GradientType
import com.sugarmunch.app.ui.visual.PatternSpec
import com.sugarmunch.app.ui.visual.PatternType

// ── Theme spec model ──────────────────────────────────────────────────────────

data class CustomThemeSpec(
    val name: String = "My Theme",
    val primaryColor: Color = Color(0xFFFF69B4),
    val secondaryColor: Color = Color(0xFF9370DB),
    val tertiaryColor: Color = Color(0xFFFFB6C1),
    val surfaceColor: Color = Color(0xFFFFF8F0),
    val backgroundColor: Color = Color(0xFFFFFBF7),
    val accentColor: Color = Color(0xFFFF1493),
    val gradient: GradientSpec? = null,
    val pattern: PatternSpec? = null,
    val fontPairing: com.sugarmunch.app.ui.typography.FontPairing? = null,
    val isDark: Boolean = false
)

private val TAB_TITLES = listOf("Colors", "Gradients", "Patterns", "Typography", "Preview")
private val PALETTE_LABELS = listOf("Primary", "Secondary", "Tertiary", "Surface", "Background", "Accent")

// ── Main screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeStudioScreen(
    onBack: () -> Unit,
    onSaveTheme: (CustomThemeSpec) -> Unit,
    editingTheme: CustomThemeSpec? = null,
    modifier: Modifier = Modifier
) {
    var spec by remember { mutableStateOf(editingTheme ?: CustomThemeSpec()) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Theme Studio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onSaveTheme(spec) }) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = spec.primaryColor.copy(alpha = 0.12f)
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Theme name
            OutlinedTextField(
                value = spec.name,
                onValueChange = { spec = spec.copy(name = it) },
                label = { Text("Theme name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Dark mode toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark mode", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                Switch(checked = spec.isDark, onCheckedChange = { spec = spec.copy(isDark = it) })
            }

            // Tabs
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 8.dp) {
                TAB_TITLES.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Tab content
            when (selectedTab) {
                0 -> ColorsTab(spec) { spec = it }
                1 -> GradientsTab(spec) { spec = it }
                2 -> PatternsTab(spec) { spec = it }
                3 -> TypographyTab(spec) { spec = it }
                4 -> PreviewTab(spec)
            }
        }
    }
}

// ── Colors tab ────────────────────────────────────────────────────────────────

@Composable
private fun ColorsTab(spec: CustomThemeSpec, onSpecChange: (CustomThemeSpec) -> Unit) {
    var editingIndex by remember { mutableIntStateOf(-1) }

    val paletteColors = listOf(
        spec.primaryColor, spec.secondaryColor, spec.tertiaryColor,
        spec.surfaceColor, spec.backgroundColor, spec.accentColor
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Seed Color", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            ColorWheelPicker(
                selectedColor = if (editingIndex >= 0) paletteColors[editingIndex] else spec.primaryColor,
                onColorSelected = { color ->
                    if (editingIndex < 0) {
                        // Auto-generate full palette from seed
                        val palette = generateHarmonyPalette(color)
                        onSpecChange(
                            spec.copy(
                                primaryColor = palette[0],
                                secondaryColor = palette[1],
                                tertiaryColor = palette[2],
                                surfaceColor = palette[3],
                                backgroundColor = palette[4],
                                accentColor = palette[5]
                            )
                        )
                    } else {
                        onSpecChange(
                            when (editingIndex) {
                                0 -> spec.copy(primaryColor = color)
                                1 -> spec.copy(secondaryColor = color)
                                2 -> spec.copy(tertiaryColor = color)
                                3 -> spec.copy(surfaceColor = color)
                                4 -> spec.copy(backgroundColor = color)
                                5 -> spec.copy(accentColor = color)
                                else -> spec
                            }
                        )
                    }
                }
            )
        }

        item {
            Text("Palette", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (editingIndex < 0) "Tap a swatch to override individually"
                else "Editing: ${PALETTE_LABELS[editingIndex]} — tap again to deselect",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            ColorPaletteRow(
                colors = paletteColors,
                selectedIndex = editingIndex,
                onColorTapped = { idx ->
                    editingIndex = if (editingIndex == idx) -1 else idx
                }
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PALETTE_LABELS.forEach { label ->
                    Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                }
            }
        }
    }
}

// ── Gradients tab ─────────────────────────────────────────────────────────────

@Composable
private fun GradientsTab(spec: CustomThemeSpec, onSpecChange: (CustomThemeSpec) -> Unit) {
    var customAngle by remember { mutableFloatStateOf(spec.gradient?.angleDegrees ?: 135f) }
    val customStops = remember {
        mutableStateListOf<GradientStop>().apply {
            spec.gradient?.stops?.let { addAll(it) }
            if (isEmpty()) {
                add(GradientStop(spec.primaryColor, 0f))
                add(GradientStop(spec.secondaryColor, 1f))
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Presets grid
        item {
            Text("Presets", style = MaterialTheme.typography.titleMedium)
        }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(280.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(GradientPresets.allPresets) { preset ->
                    GradientSwatch(
                        gradient = preset,
                        isSelected = spec.gradient == preset,
                        onClick = { onSpecChange(spec.copy(gradient = preset)) }
                    )
                }
            }
        }

        // Custom builder
        item {
            Text("Custom Gradient", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            // Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colorStops = customStops
                                .map { it.position to it.color }
                                .toTypedArray(),
                            start = angleToOffset(customAngle, 0f),
                            end = angleToOffset(customAngle, 1f)
                        )
                    )
            )
        }

        item {
            Text("Angle: ${customAngle.toInt()}°", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = customAngle,
                onValueChange = { customAngle = it },
                valueRange = 0f..360f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Color Stops", style = MaterialTheme.typography.labelMedium)
                if (customStops.size < 5) {
                    IconButton(onClick = {
                        customStops.add(GradientStop(Color.White, 0.5f))
                    }) {
                        Icon(Icons.Filled.Add, "Add stop")
                    }
                }
            }
        }

        items(customStops.size) { idx ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(customStops[idx].color)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text("Pos:", style = MaterialTheme.typography.labelSmall)
                Slider(
                    value = customStops[idx].position,
                    onValueChange = { pos ->
                        customStops[idx] = customStops[idx].copy(position = pos)
                    },
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f)
                )
                if (customStops.size > 2) {
                    IconButton(onClick = { customStops.removeAt(idx) }) {
                        Icon(Icons.Filled.Close, "Remove stop", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    val gradient = GradientSpec(
                        type = GradientType.LINEAR,
                        stops = customStops.toList(),
                        angleDegrees = customAngle
                    )
                    onSpecChange(spec.copy(gradient = gradient))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Custom Gradient")
            }
        }
    }
}

@Composable
private fun GradientSwatch(
    gradient: GradientSpec,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    colors = gradient.stops.map { it.color },
                    start = angleToOffset(gradient.angleDegrees, 0f),
                    end = angleToOffset(gradient.angleDegrees, 1f)
                )
            )
            .then(
                if (isSelected) Modifier.border(3.dp, Color.White, RoundedCornerShape(8.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
    )
}

private fun angleToOffset(angleDeg: Float, t: Float): Offset {
    val rad = Math.toRadians(angleDeg.toDouble())
    return if (t == 0f) Offset.Zero
    else Offset(
        (kotlin.math.cos(rad) * 1000f).toFloat(),
        (kotlin.math.sin(rad) * 1000f).toFloat()
    )
}

// ── Patterns tab ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PatternsTab(spec: CustomThemeSpec, onSpecChange: (CustomThemeSpec) -> Unit) {
    var selectedType by remember { mutableStateOf(spec.pattern?.type ?: PatternType.CANDY_STRIPES) }
    var opacity by remember { mutableFloatStateOf(spec.pattern?.opacity ?: 0.15f) }
    var scale by remember { mutableFloatStateOf(spec.pattern?.scale ?: 1f) }
    var rotation by remember { mutableFloatStateOf(spec.pattern?.rotation ?: 0f) }

    fun updatePattern() {
        onSpecChange(
            spec.copy(
                pattern = PatternSpec(
                    type = selectedType,
                    primaryColor = spec.primaryColor,
                    secondaryColor = spec.secondaryColor.copy(alpha = opacity),
                    scale = scale,
                    opacity = opacity,
                    rotation = rotation
                )
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Pattern Type", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PatternType.entries.forEach { type ->
                    val isSelected = type == selectedType
                    FilledTonalButton(
                        onClick = { selectedType = type; updatePattern() },
                        colors = if (isSelected) {
                            ButtonDefaults.filledTonalButtonColors(
                                containerColor = spec.primaryColor.copy(alpha = 0.3f)
                            )
                        } else {
                            ButtonDefaults.filledTonalButtonColors()
                        }
                    ) {
                        Text(
                            type.name.replace('_', ' ').lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        item {
            Text("Opacity: ${"%.2f".format(opacity)}", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = opacity,
                onValueChange = { opacity = it; updatePattern() },
                valueRange = 0f..0.5f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text("Scale: ${"%.1f".format(scale)}x", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = scale,
                onValueChange = { scale = it; updatePattern() },
                valueRange = 0.5f..2f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text("Rotation: ${rotation.toInt()}°", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = rotation,
                onValueChange = { rotation = it; updatePattern() },
                valueRange = 0f..360f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Pattern preview
        item {
            Text("Preview", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(spec.backgroundColor)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawPatternPreview(selectedType, spec.primaryColor, opacity, scale, rotation)
                }
            }
        }

        // Clear button
        item {
            if (spec.pattern != null) {
                FilledTonalButton(
                    onClick = { onSpecChange(spec.copy(pattern = null)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Pattern")
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPatternPreview(
    type: PatternType,
    color: Color,
    opacity: Float,
    scale: Float,
    rotation: Float
) {
    val patternColor = color.copy(alpha = opacity)
    val spacing = 24.dp.toPx() * scale

    when (type) {
        PatternType.POLKA_DOTS -> {
            var x = 0f
            while (x < size.width + spacing) {
                var y = 0f
                while (y < size.height + spacing) {
                    drawCircle(patternColor, radius = 4.dp.toPx() * scale, center = Offset(x, y))
                    y += spacing
                }
                x += spacing
            }
        }
        PatternType.CANDY_STRIPES, PatternType.DIAGONAL_LINES -> {
            var x = -size.height
            while (x < size.width + size.height) {
                drawLine(
                    patternColor, Offset(x, 0f), Offset(x + size.height, size.height),
                    strokeWidth = 3.dp.toPx() * scale
                )
                x += spacing
            }
        }
        PatternType.CROSSHATCH -> {
            var x = 0f
            while (x < size.width + spacing) {
                drawLine(patternColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
                x += spacing
            }
            var y = 0f
            while (y < size.height + spacing) {
                drawLine(patternColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                y += spacing
            }
        }
        else -> {
            // Simplified preview for other pattern types — show as dots
            var x = spacing / 2
            while (x < size.width) {
                var y = spacing / 2
                while (y < size.height) {
                    drawCircle(patternColor, radius = 3.dp.toPx() * scale, center = Offset(x, y))
                    y += spacing
                }
                x += spacing
            }
        }
    }
}

// ── Typography tab ────────────────────────────────────────────────────────────

@Composable
private fun TypographyTab(spec: CustomThemeSpec, onSpecChange: (CustomThemeSpec) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Font Pairings", style = MaterialTheme.typography.titleMedium)
        }

        items(FontPairings.all) { pairing ->
            val isSelected = spec.fontPairing?.name == pairing.name
            SugarCard(
                style = if (isSelected) CardStyle.OUTLINED else CardStyle.ELEVATED,
                onClick = { onSpecChange(spec.copy(fontPairing = pairing)) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            pairing.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Heading: ${pairing.headingFont.displayName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Body: ${pairing.bodyFont.displayName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Caption: ${pairing.captionFont.displayName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                        // Sample text
                        Text(
                            "The quick brown fox",
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "jumps over the lazy dog while eating candy",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (isSelected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = spec.primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Individual font overrides
        item {
            Spacer(Modifier.height(8.dp))
            Text("Individual Overrides", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Select a pairing above first, then override individual slots:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            FontDropdown("Heading Font", spec.fontPairing?.headingFont) { font ->
                val current = spec.fontPairing ?: FontPairings.all.first()
                onSpecChange(spec.copy(fontPairing = current.copy(headingFont = font)))
            }
        }
        item {
            FontDropdown("Body Font", spec.fontPairing?.bodyFont) { font ->
                val current = spec.fontPairing ?: FontPairings.all.first()
                onSpecChange(spec.copy(fontPairing = current.copy(bodyFont = font)))
            }
        }
        item {
            FontDropdown("Caption Font", spec.fontPairing?.captionFont) { font ->
                val current = spec.fontPairing ?: FontPairings.all.first()
                onSpecChange(spec.copy(fontPairing = current.copy(captionFont = font)))
            }
        }
    }
}

@Composable
private fun FontDropdown(
    label: String,
    selected: SugarFontFamily?,
    onSelect: (SugarFontFamily) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded }
                .padding(12.dp)
        ) {
            Text(
                selected?.displayName ?: "Default",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(4.dp)
            ) {
                SugarFontFamily.entries.forEach { font ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(font); expanded = false }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = font == selected,
                            onClick = { onSelect(font); expanded = false }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(font.displayName, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                font.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Preview tab ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreviewTab(spec: CustomThemeSpec) {
    val bgModifier = if (spec.gradient != null) {
        Modifier.background(
            Brush.linearGradient(
                colors = spec.gradient!!.stops.map { it.color },
                start = angleToOffset(spec.gradient!!.angleDegrees, 0f),
                end = angleToOffset(spec.gradient!!.angleDegrees, 1f)
            )
        )
    } else {
        Modifier.background(spec.backgroundColor)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .then(bgModifier),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Fake top bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(spec.primaryColor),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    spec.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Cards
        item {
            SugarCard(style = CardStyle.ELEVATED) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Sample Card",
                        style = MaterialTheme.typography.titleSmall,
                        color = spec.primaryColor
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "This is how a card looks with your theme. " +
                            "The colors, typography, and styling all come together here.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            SugarCard(style = CardStyle.OUTLINED) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(spec.accentColor)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Outlined Card", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Secondary style card preview",
                            style = MaterialTheme.typography.bodySmall,
                            color = spec.secondaryColor
                        )
                    }
                }
            }
        }

        // Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = spec.primaryColor),
                    modifier = Modifier.weight(1f)
                ) { Text("Primary") }
                FilledTonalButton(
                    onClick = {},
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = spec.secondaryColor.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.weight(1f)
                ) { Text("Secondary") }
            }
        }

        // Color swatches
        item {
            Text("Color Palette", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            ColorPaletteRow(
                colors = listOf(
                    spec.primaryColor, spec.secondaryColor, spec.tertiaryColor,
                    spec.surfaceColor, spec.accentColor
                ),
                selectedIndex = -1,
                onColorTapped = {}
            )
        }

        // Typography samples
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(spec.surfaceColor)
                    .padding(16.dp)
            ) {
                Text(
                    "Heading Text",
                    style = MaterialTheme.typography.headlineSmall,
                    color = spec.primaryColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Body text shows how readable your theme is for longer passages of content.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Caption · ${spec.fontPairing?.name ?: "Default"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = spec.secondaryColor
                )
            }
        }

        // FAB preview
        item {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                FloatingActionButton(
                    onClick = {},
                    containerColor = spec.accentColor
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}
