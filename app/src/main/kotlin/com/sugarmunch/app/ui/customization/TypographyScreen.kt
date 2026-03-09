package com.sugarmunch.app.ui.customization

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens

/**
 * EXTREME Typography Control Screen
 * Font management, text scaling, and readability controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypographyScreen(
    onNavigateBack: () -> Unit,
    typographyConfig: TypographyConfig,
    onTypographyConfigChange: (TypographyConfig) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Typography") },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.lg)
        ) {
            // Font Management
            item {
                FontManagementSection(
                    typographyConfig = typographyConfig,
                    onTypographyConfigChange = onTypographyConfigChange
                )
            }

            // Text Scaling
            item {
                TextScalingSection(
                    typographyConfig = typographyConfig,
                    onTypographyConfigChange = onTypographyConfigChange
                )
            }

            // Text Effects
            item {
                TextEffectsSection(
                    typographyConfig = typographyConfig,
                    onTypographyConfigChange = onTypographyConfigChange
                )
            }

            // Readability Features
            item {
                ReadabilityFeaturesSection(
                    typographyConfig = typographyConfig,
                    onTypographyConfigChange = onTypographyConfigChange
                )
            }
        }
    }
}

@Composable
private fun FontManagementSection(
    typographyConfig: TypographyConfig,
    onTypographyConfigChange: (TypographyConfig) -> Unit
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
                text = "Font Management",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.hotPink
            )

            // Heading Font
            FontSelectorRow(
                label = "Heading Font",
                selectedFont = typographyConfig.headingFont,
                onFontSelected = { onTypographyConfigChange(typographyConfig.copy(headingFont = it)) }
            )

            // Body Font
            FontSelectorRow(
                label = "Body Font",
                selectedFont = typographyConfig.bodyFont,
                onFontSelected = { onTypographyConfigChange(typographyConfig.copy(bodyFont = it)) }
            )

            // Caption Font
            FontSelectorRow(
                label = "Caption Font",
                selectedFont = typographyConfig.captionFont,
                onFontSelected = { onTypographyConfigChange(typographyConfig.copy(captionFont = it)) }
            )

            // Button Font
            FontSelectorRow(
                label = "Button Font",
                selectedFont = typographyConfig.buttonFont,
                onFontSelected = { onTypographyConfigChange(typographyConfig.copy(buttonFont = it)) }
            )

            // Default Font Weight
            SliderWithLabel(
                label = "Default Font Weight (${typographyConfig.defaultFontWeight})",
                value = typographyConfig.defaultFontWeight.toFloat(),
                onValueChange = {
                    onTypographyConfigChange(typographyConfig.copy(defaultFontWeight = it.toInt()))
                },
                valueRange = 100f..900f,
                steps = 15
            )

            // Default Font Style
            Text("Default Font Style", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                FontStyle.entries.forEach { style ->
                    FilterChip(
                        selected = typographyConfig.defaultFontStyle == style,
                        onClick = {
                            onTypographyConfigChange(typographyConfig.copy(defaultFontStyle = style))
                        },
                        label = { Text(style.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FontSelectorRow(
    label: String,
    selectedFont: String,
    onFontSelected: (String) -> Unit
) {
    val fonts = listOf("default", "nunito", "comfortaa", "space_grotesk", "roboto", "opensans")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
        ) {
            fonts.forEach { font ->
                FilterChip(
                    selected = selectedFont == font,
                    onClick = { onFontSelected(font) },
                    label = { Text(font.replace("_", " ").capitalize()) }
                )
            }
        }
    }
}

@Composable
private fun TextScalingSection(
    typographyConfig: TypographyConfig,
    onTypographyConfigChange: (TypographyConfig) -> Unit
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
                text = "Text Scaling",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.mint
            )

            // Global Scale
            SliderWithLabel(
                label = "Global Scale (${(typographyConfig.globalScale * 100).toInt()}%)",
                value = typographyConfig.globalScale,
                onValueChange = { onTypographyConfigChange(typographyConfig.copy(globalScale = it)) },
                valueRange = 0.5f..2f,
                steps = 29
            )

            // Title Scale
            SliderWithLabel(
                label = "Title Scale (${(typographyConfig.titleScale * 100).toInt()}%)",
                value = typographyConfig.titleScale,
                onValueChange = { onTypographyConfigChange(typographyConfig.copy(titleScale = it)) },
                valueRange = 0.5f..2f,
                steps = 29
            )

            // Body Scale
            SliderWithLabel(
                label = "Body Scale (${(typographyConfig.bodyScale * 100).toInt()}%)",
                value = typographyConfig.bodyScale,
                onValueChange = { onTypographyConfigChange(typographyConfig.copy(bodyScale = it)) },
                valueRange = 0.5f..2f,
                steps = 29
            )

            // Caption Scale
            SliderWithLabel(
                label = "Caption Scale (${(typographyConfig.captionScale * 100).toInt()}%)",
                value = typographyConfig.captionScale,
                onValueChange = { onTypographyConfigChange(typographyConfig.copy(captionScale = it)) },
                valueRange = 0.5f..2f,
                steps = 29
            )

            // Button Scale
            SliderWithLabel(
                label = "Button Scale (${(typographyConfig.buttonScale * 100).toInt()}%)",
                value = typographyConfig.buttonScale,
                onValueChange = { onTypographyConfigChange(typographyConfig.copy(buttonScale = it)) },
                valueRange = 0.5f..2f,
                steps = 29
            )

            // Line Height Multiplier
            SliderWithLabel(
                label = "Line Height (${typographyConfig.lineHeightMultiplier}x)",
                value = typographyConfig.lineHeightMultiplier,
                onValueChange = { onTypographyConfigChange(typographyConfig.copy(lineHeightMultiplier = it)) },
                valueRange = 0.8f..2f,
                steps = 23
            )

            // Letter Spacing
            SliderWithLabel(
                label = "Letter Spacing (${typographyConfig.letterSpacing}px)",
                value = typographyConfig.letterSpacing,
                onValueChange = { onTypographyConfigChange(typographyConfig.copy(letterSpacing = it)) },
                valueRange = -2f..10f,
                steps = 23
            )

            // Word Spacing
            SliderWithLabel(
                label = "Word Spacing (${typographyConfig.wordSpacing}px)",
                value = typographyConfig.wordSpacing,
                onValueChange = { onTypographyConfigChange(typographyConfig.copy(wordSpacing = it)) },
                valueRange = -2f..10f,
                steps = 23
            )
        }
    }
}

@Composable
private fun TextEffectsSection(
    typographyConfig: TypographyConfig,
    onTypographyConfigChange: (TypographyConfig) -> Unit
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
                text = "Text Effects",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.candyOrange
            )

            // All Caps Headings
            SwitchWithLabel(
                label = "All Caps Headings",
                checked = typographyConfig.allCapsHeadings,
                onCheckedChange = {
                    onTypographyConfigChange(typographyConfig.copy(allCapsHeadings = it))
                }
            )

            // Small Caps
            SwitchWithLabel(
                label = "Small Caps",
                checked = typographyConfig.smallCaps,
                onCheckedChange = {
                    onTypographyConfigChange(typographyConfig.copy(smallCaps = it))
                }
            )

            // Underline Style
            Text("Underline Style", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                UnderlineStyle.entries.forEach { style ->
                    FilterChip(
                        selected = typographyConfig.underlineStyle == style,
                        onClick = {
                            onTypographyConfigChange(typographyConfig.copy(underlineStyle = style))
                        },
                        label = { Text(style.name) }
                    )
                }
            }

            // Strikethrough Style
            Text("Strikethrough Style", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                StrikethroughStyle.entries.forEach { style ->
                    FilterChip(
                        selected = typographyConfig.strikethroughStyle == style,
                        onClick = {
                            onTypographyConfigChange(typographyConfig.copy(strikethroughStyle = style))
                        },
                        label = { Text(style.name) }
                    )
                }
            }

            // Animated Text
            Text("Animated Text", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                TextAnimation.entries.forEach { animation ->
                    FilterChip(
                        selected = typographyConfig.animatedText == animation,
                        onClick = {
                            onTypographyConfigChange(typographyConfig.copy(animatedText = animation))
                        },
                        label = { Text(animation.name.replace("_", " ")) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadabilityFeaturesSection(
    typographyConfig: TypographyConfig,
    onTypographyConfigChange: (TypographyConfig) -> Unit
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
                text = "Readability Features",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.bubblegumBlue
            )

            // Dyslexia-Friendly Font
            SwitchWithLabel(
                label = "Dyslexia-Friendly Font",
                checked = typographyConfig.dyslexiaFriendlyFont,
                onCheckedChange = {
                    onTypographyConfigChange(typographyConfig.copy(dyslexiaFriendlyFont = it))
                }
            )

            // Increased Letter Spacing
            SwitchWithLabel(
                label = "Increased Letter Spacing",
                checked = typographyConfig.increasedLetterSpacing,
                onCheckedChange = {
                    onTypographyConfigChange(typographyConfig.copy(increasedLetterSpacing = it))
                }
            )

            // Increased Word Spacing
            SwitchWithLabel(
                label = "Increased Word Spacing",
                checked = typographyConfig.increasedWordSpacing,
                onCheckedChange = {
                    onTypographyConfigChange(typographyConfig.copy(increasedWordSpacing = it))
                }
            )

            // Hyphenation
            SwitchWithLabel(
                label = "Hyphenation",
                checked = typographyConfig.hyphenation,
                onCheckedChange = {
                    onTypographyConfigChange(typographyConfig.copy(hyphenation = it))
                }
            )

            // Text Justification
            Text("Text Justification", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                TextJustification.entries.forEach { justification ->
                    FilterChip(
                        selected = typographyConfig.textJustification == justification,
                        onClick = {
                            onTypographyConfigChange(typographyConfig.copy(textJustification = justification))
                        },
                        label = { Text(justification.name) }
                    )
                }
            }

            // Paragraph Spacing
            SliderWithLabel(
                label = "Paragraph Spacing (${typographyConfig.paragraphSpacing.value.toInt()}dp)",
                value = typographyConfig.paragraphSpacing.value,
                onValueChange = {
                    onTypographyConfigChange(typographyConfig.copy(paragraphSpacing = it.dp))
                },
                valueRange = 8f..48f,
                steps = 39
            )
        }
    }
}

@Composable
private fun SwitchWithLabel(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SliderWithLabel(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}
