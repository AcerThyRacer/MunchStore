package com.sugarmunch.app.ui.customization

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens

/**
 * EXTREME Color System Customization Screen
 * Advanced color picker and per-element color control
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSystemScreen(
    onNavigateBack: () -> Unit,
    profile: ColorProfile,
    onProfileChange: (ColorProfile) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Color System") },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Mode Selector
            ModeSelector(
                selectedMode = profile.mode,
                onModeSelected = { onProfileChange(profile.copy(mode = it)) }
            )
            
            HorizontalDivider()
            
            // Color Configuration
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(SugarDimens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xl)
            ) {
                // Primary Colors
                PrimaryColorsSection(
                    profile = profile,
                    onProfileChange = onProfileChange
                )
                
                // Surface Colors
                SurfaceColorsSection(
                    surfaceColors = profile.surfaceColors,
                    onSurfaceColorsChange = { 
                        onProfileChange(profile.copy(surfaceColors = it))
                    }
                )
                
                // UI Element Colors
                UIColorsSection(
                    profile = profile,
                    onProfileChange = onProfileChange
                )
                
                // Color Profile Settings
                ColorProfileSettingsSection(
                    profile = profile,
                    onProfileChange = onProfileChange
                )
                
                // Color Harmonies
                ColorHarmoniesSection(
                    primaryColor = profile.primaryColor,
                    colorScheme = profile.colorScheme,
                    onColorSchemeChange = { onProfileChange(profile.copy(colorScheme = it)) }
                )
            }
        }
    }
}

@Composable
private fun ModeSelector(
    selectedMode: ColorPickerMode,
    onModeSelected: (ColorPickerMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SugarDimens.Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
    ) {
        ColorPickerMode.entries.forEach { mode ->
            FilterChip(
                selected = mode == selectedMode,
                onClick = { onModeSelected(mode) },
                label = { Text(mode.name) }
            )
        }
    }
}

@Composable
private fun PrimaryColorsSection(
    profile: ColorProfile,
    onProfileChange: (ColorProfile) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Primary Colors",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.hotPink
            )
            
            ColorSelector(
                label = "Primary Color",
                colorHex = profile.primaryColor,
                onColorSelected = { onProfileChange(profile.copy(primaryColor = it)) }
            )
            
            ColorSelector(
                label = "Secondary Color",
                colorHex = profile.secondaryColor,
                onColorSelected = { onProfileChange(profile.copy(secondaryColor = it)) }
            )
            
            ColorSelector(
                label = "Accent Color",
                colorHex = profile.accentColor,
                onColorSelected = { onProfileChange(profile.copy(accentColor = it)) }
            )
        }
    }
}

@Composable
private fun SurfaceColorsSection(
    surfaceColors: SurfaceColors,
    onSurfaceColorsChange: (SurfaceColors) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Surface Colors",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.mint
            )
            
            ColorSelector(
                label = "Card Surface",
                colorHex = surfaceColors.card,
                onColorSelected = { onSurfaceColorsChange(surfaceColors.copy(card = it)) }
            )
            
            ColorSelector(
                label = "Dialog Surface",
                colorHex = surfaceColors.dialog,
                onColorSelected = { onSurfaceColorsChange(surfaceColors.copy(dialog = it)) }
            )
            
            ColorSelector(
                label = "Bottom Sheet",
                colorHex = surfaceColors.bottomSheet,
                onColorSelected = { onSurfaceColorsChange(surfaceColors.copy(bottomSheet = it)) }
            )
            
            ColorSelector(
                label = "Main Surface",
                colorHex = surfaceColors.surface,
                onColorSelected = { onSurfaceColorsChange(surfaceColors.copy(surface = it)) }
            )
            
            ColorSelector(
                label = "Background",
                colorHex = surfaceColors.background,
                onColorSelected = { onSurfaceColorsChange(surfaceColors.copy(background = it)) }
            )
        }
    }
}

@Composable
private fun UIColorsSection(
    profile: ColorProfile,
    onProfileChange: (ColorProfile) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "UI Element Colors",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.yellow
            )
            
            ColorSelector(
                label = "Status Bar",
                colorHex = profile.statusBarColor,
                onColorSelected = { onProfileChange(profile.copy(statusBarColor = it)) }
            )
            
            ColorSelector(
                label = "Navigation Bar",
                colorHex = profile.navigationBarColor,
                onColorSelected = { onProfileChange(profile.copy(navigationBarColor = it)) }
            )
            
            ColorSelector(
                label = "Divider",
                colorHex = profile.dividerColor,
                onColorSelected = { onProfileChange(profile.copy(dividerColor = it)) }
            )
            
            ColorSelector(
                label = "Scrollbar",
                colorHex = profile.scrollbarColor,
                onColorSelected = { onProfileChange(profile.copy(scrollbarColor = it)) }
            )
            
            ColorSelector(
                label = "Selection Highlight",
                colorHex = profile.selectionHighlightColor,
                onColorSelected = { onProfileChange(profile.copy(selectionHighlightColor = it)) }
            )
            
            ColorSelector(
                label = "Focus Indicator",
                colorHex = profile.focusIndicatorColor,
                onColorSelected = { onProfileChange(profile.copy(focusIndicatorColor = it)) }
            )
        }
    }
}

@Composable
private fun ColorProfileSettingsSection(
    profile: ColorProfile,
    onProfileChange: (ColorProfile) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Color Profile",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.candyOrange
            )
            
            SliderWithLabel(
                label = "Color Temperature (${profile.colorTemperature.toInt()}K)",
                value = profile.colorTemperature,
                onValueChange = { onProfileChange(profile.copy(colorTemperature = it)) },
                valueRange = 2700f..6500f,
                steps = 37
            )
            
            SliderWithLabel(
                label = "Saturation (${profile.saturationCurve}x)",
                value = profile.saturationCurve,
                onValueChange = { onProfileChange(profile.copy(saturationCurve = it)) },
                valueRange = 0f..2f,
                steps = 19
            )
            
            SliderWithLabel(
                label = "Brightness (${profile.brightnessCurve}x)",
                value = profile.brightnessCurve,
                onValueChange = { onProfileChange(profile.copy(brightnessCurve = it)) },
                valueRange = 0f..2f,
                steps = 19
            )
            
            // Contrast Level
            Text("Contrast Level", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                ContrastLevel.entries.forEach { level ->
                    FilterChip(
                        selected = profile.contrastLevel == level,
                        onClick = { onProfileChange(profile.copy(contrastLevel = level)) },
                        label = { Text(level.name.replace("_", " ")) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorHarmoniesSection(
    primaryColor: String,
    colorScheme: ColorScheme,
    onColorSchemeChange: (ColorScheme) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Color Harmonies",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.bubblegumBlue
            )
            
            Text(
                text = "Based on primary color: $primaryColor",
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Color Scheme Selector
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                items(ColorScheme.entries.size) { index ->
                    val scheme = ColorScheme.entries[index]
                    val isSelected = scheme == colorScheme
                    
                    ColorSchemeCard(
                        scheme = scheme,
                        primaryColor = primaryColor,
                        isSelected = isSelected,
                        onClick = { onColorSchemeChange(scheme) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSelector(
    label: String,
    colorHex: String,
    onColorSelected: (String) -> Unit
) {
    val color = remember(colorHex) { 
        try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color.White
        }
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, RoundedCornerShape(8.dp))
                .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = colorHex,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        TextButton(onClick = { /* Open color picker */ }) {
            Text("Change")
        }
    }
}

@Composable
private fun ColorSchemeCard(
    scheme: ColorScheme,
    primaryColor: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = remember(scheme, primaryColor) {
        generateColorHarmony(scheme, primaryColor)
    }
    
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(SugarDimens.Radius.md),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                SugarDimens.Brand.hotPink.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SugarDimens.Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = scheme.name.replace("_", " "),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                colors.take(5).forEach { colorHex ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .background(
                                Color(android.graphics.Color.parseColor(colorHex)),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = SugarDimens.Brand.hotPink
                )
            }
        }
    }
}

/**
 * Generate color harmony based on scheme type
 */
private fun generateColorHarmony(scheme: ColorScheme, primaryColor: String): List<String> {
    // Simplified implementation - in real app would use proper color theory
    return when (scheme) {
        ColorScheme.COMPLEMENTARY -> listOf(primaryColor, "#FF00FFA3", "#FFFFD700", "#FFFF69B4", "#FF00BFFF")
        ColorScheme.ANALOGOUS -> listOf(primaryColor, "#FFFF8C8C", "#FFFFB88C", "#FFFFE88C", "#FFC8FF8C")
        ColorScheme.TRIADIC -> listOf(primaryColor, "#FF00FFA3", "#FFFFD700", "#FFFF69B4", "#FF00BFFF")
        ColorScheme.TETRADIC -> listOf(primaryColor, "#FF00FFA3", "#FFFFD700", "#FFFF69B4", "#FF8C00FF")
        ColorScheme.SPLIT_COMPLEMENTARY -> listOf(primaryColor, "#FF00FF8C", "#FF008CFF", "#FFFF8CFF", "#FFFFFF8C")
        ColorScheme.MONOCHROMATIC -> listOf(primaryColor, "#FF3D3D5C", "#FF4D4D6C", "#FF5D5D7C", "#FF6D6D8C")
        ColorScheme.CUSTOM -> listOf(primaryColor, "#FFFFFFFF", "#FFFFFFFF", "#FFFFFFFF", "#FFFFFFFF")
    }
}
