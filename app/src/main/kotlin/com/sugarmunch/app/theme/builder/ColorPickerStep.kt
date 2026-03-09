package com.sugarmunch.app.theme.builder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Step 1: Color Picker - Allows users to select and customize theme colors.
 *
 * Provides sections for primary, secondary, background, and surface colors,
 * plus a custom color picker for additional colors.
 *
 * @param state Current theme builder state
 * @param onStateChange Callback to update the state
 */
@Composable
fun ColorPickerStep(
    state: ThemeBuilderState,
    onStateChange: (ThemeBuilderState) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Primary color picker
        item {
            ColorPickerSection(
                title = "Primary Color",
                selectedColor = state.primaryColorAsColor(),
                onColorSelected = { onStateChange(state.copy(primaryColor = it.value)) },
                presetColors = ColorPresets.primaryColors.map { Color(it) }
            )
        }

        // Secondary color picker
        item {
            ColorPickerSection(
                title = "Secondary Color",
                selectedColor = state.secondaryColorAsColor(),
                onColorSelected = { onStateChange(state.copy(secondaryColor = it.value)) },
                presetColors = ColorPresets.secondaryColors.map { Color(it) }
            )
        }

        // Background color picker
        item {
            ColorPickerSection(
                title = "Background Color",
                selectedColor = state.backgroundColorAsColor(),
                onColorSelected = { onStateChange(state.copy(backgroundColor = it.value)) },
                presetColors = ColorPresets.backgroundColors.map { Color(it) }
            )
        }

        // Surface color picker
        item {
            ColorPickerSection(
                title = "Surface Color",
                selectedColor = state.surfaceColorAsColor(),
                onColorSelected = { onStateChange(state.copy(surfaceColor = it.value)) },
                presetColors = ColorPresets.surfaceColors.map { Color(it) }
            )
        }

        // Custom color picker
        item {
            CustomColorPicker(
                title = "Custom Colors",
                customColors = state.customColorsAsColors(),
                onAddColor = { onStateChange(state.copy(customColors = state.customColors + it.value)) },
                onRemoveColor = { index ->
                    onStateChange(state.copy(customColors = state.customColors.filterIndexed { i, _ -> i != index }))
                }
            )
        }
    }
}

/**
 * A section containing a color picker with preset colors.
 *
 * @param title The title for this section
 * @param selectedColor Currently selected color
 * @param onColorSelected Callback when a color is selected
 * @param presetColors List of preset colors to display
 */
@Composable
private fun ColorPickerSection(
    title: String,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    presetColors: List<Color>
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
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Selected color preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selected:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(selectedColor)
                        .border(2.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Color palette grid
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presetColors) { color ->
                    ColorSwatch(
                        color = color,
                        isSelected = selectedColor == color,
                        onClick = { onColorSelected(color) }
                    )
                }
            }
        }
    }
}

/**
 * A single color swatch button.
 *
 * @param color The color to display
 * @param isSelected Whether this color is currently selected
 * @param onClick Callback when clicked
 */
@Composable
private fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier.border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                }
            )
            .clickable {
                scope.launch {
                    onClick()
                }
            }
    )
}

/**
 * Custom color picker that allows users to add custom colors via RGB sliders.
 *
 * @param title The title for this picker
 * @param customColors List of custom colors already added
 * @param onAddColor Callback when a new color is added
 * @param onRemoveColor Callback when a color is removed (long press)
 */
@Composable
private fun CustomColorPicker(
    title: String,
    customColors: List<Color>,
    onAddColor: (Color) -> Unit,
    onRemoveColor: (Int) -> Unit
) {
    var showColorPicker by remember { mutableStateOf(false) }

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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { showColorPicker = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add color")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (customColors.isEmpty()) {
                Text(
                    text = "Tap + to add custom colors",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyHorizontalGrid(
                    rows = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(100.dp)
                ) {
                    items(customColors.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(customColors[index])
                                .combinedClickable(
                                    onClick = { },
                                    onLongClick = { onRemoveColor(index) }
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Long press to remove",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showColorPicker) {
        var selectedColor by remember { mutableStateOf(Color.Red) }
        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            title = { Text("Choose Custom Color") },
            text = {
                Column {
                    // Simple RGB sliders
                    var red by remember { mutableStateOf(255f) }
                    var green by remember { mutableStateOf(128f) }
                    var blue by remember { mutableStateOf(128f) }

                    selectedColor = Color(red / 255f, green / 255f, blue / 255f)

                    Slider(
                        value = red,
                        onValueChange = { red = it },
                        valueRange = 0f..255f,
                        steps = 255
                    )
                    Slider(
                        value = green,
                        onValueChange = { green = it },
                        valueRange = 0f..255f,
                        steps = 255
                    )
                    Slider(
                        value = blue,
                        onValueChange = { blue = it },
                        valueRange = 0f..255f,
                        steps = 255
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(selectedColor)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onAddColor(selectedColor)
                    showColorPicker = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showColorPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
