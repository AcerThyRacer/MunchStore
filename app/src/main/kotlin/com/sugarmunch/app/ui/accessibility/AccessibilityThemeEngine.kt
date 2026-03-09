package com.sugarmunch.app.ui.accessibility

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

// ═══════════════════════════════════════════════════════════════════
// Phase 5.4 — Accessibility Theme Engine
// Comprehensive accessibility visual modes for SugarMunch
// ═══════════════════════════════════════════════════════════════════

/**
 * Enumerates all supported accessibility display modes.
 */
enum class AccessibilityMode(val label: String, val description: String) {
    NONE("None", "Default display with no accessibility adjustments"),
    HIGH_CONTRAST("High Contrast", "Maximizes contrast between foreground and background elements"),
    DEUTERANOPIA("Deuteranopia", "Red-green colorblind simulation (green-weak)"),
    PROTANOPIA("Protanopia", "Red-green colorblind simulation (red-weak)"),
    TRITANOPIA("Tritanopia", "Blue-yellow colorblind simulation"),
    DYSLEXIA_FRIENDLY("Dyslexia Friendly", "Increased spacing and larger fonts for readability"),
    REDUCED_MOTION("Reduced Motion", "Minimizes animations and motion effects"),
    LARGE_TARGETS("Large Targets", "Increases touch target sizes for easier interaction")
}

/**
 * Colorblind vision types with associated descriptions.
 */
enum class ColorblindMode(val label: String, val description: String) {
    DEUTERANOPIA("Deuteranopia", "Most common form — difficulty distinguishing red and green hues"),
    PROTANOPIA("Protanopia", "Reduced sensitivity to red light, reds appear darker"),
    TRITANOPIA("Tritanopia", "Rare form — difficulty distinguishing blue and yellow hues")
}

/**
 * Full accessibility configuration state.
 */
data class AccessibilityConfig(
    val mode: AccessibilityMode = AccessibilityMode.NONE,
    val highContrastEnabled: Boolean = false,
    val colorblindMode: ColorblindMode? = null,
    val dyslexiaFontEnabled: Boolean = false,
    val reducedMotion: Boolean = false,
    val largeTouchTargets: Boolean = false,
    val minimumContrastRatio: Float = 4.5f
)

// ═══════════════════════════════════════════════════════════════════
// Core Engine — Color transformations and WCAG utilities
// ═══════════════════════════════════════════════════════════════════

/**
 * Stateless engine providing color remapping, contrast enforcement,
 * and full color-scheme transformations for accessibility.
 *
 * Colorblind simulation uses Brettel/Viénot-style 3×3 matrix
 * multiplication on linearized RGB values.
 */
object AccessibilityThemeEngine {

    // ----- Brettel / Viénot approximation matrices -----
    // Each matrix transforms linear-RGB into the simulated vision space.

    private val deuteranopiaMatrix = floatArrayOf(
        0.625f, 0.375f, 0.000f,
        0.700f, 0.300f, 0.000f,
        0.000f, 0.300f, 0.700f
    )

    private val protanopiaMatrix = floatArrayOf(
        0.567f, 0.433f, 0.000f,
        0.558f, 0.442f, 0.000f,
        0.000f, 0.242f, 0.758f
    )

    private val tritanopiaMatrix = floatArrayOf(
        0.950f, 0.050f, 0.000f,
        0.000f, 0.433f, 0.567f,
        0.000f, 0.475f, 0.525f
    )

    /**
     * Remap [color] to simulate how it appears under the given [mode].
     * Uses Brettel/Viénot-style 3×3 matrix applied to linearized RGB.
     */
    fun remapColorForColorblind(color: Color, mode: ColorblindMode): Color {
        val matrix = when (mode) {
            ColorblindMode.DEUTERANOPIA -> deuteranopiaMatrix
            ColorblindMode.PROTANOPIA -> protanopiaMatrix
            ColorblindMode.TRITANOPIA -> tritanopiaMatrix
        }

        val linR = srgbToLinear(color.red)
        val linG = srgbToLinear(color.green)
        val linB = srgbToLinear(color.blue)

        val outR = matrix[0] * linR + matrix[1] * linG + matrix[2] * linB
        val outG = matrix[3] * linR + matrix[4] * linG + matrix[5] * linB
        val outB = matrix[6] * linR + matrix[7] * linG + matrix[8] * linB

        return Color(
            red = linearToSrgb(outR.coerceIn(0f, 1f)),
            green = linearToSrgb(outG.coerceIn(0f, 1f)),
            blue = linearToSrgb(outB.coerceIn(0f, 1f)),
            alpha = color.alpha
        )
    }

    /**
     * Adjust [foreground] and [background] so their contrast ratio
     * meets or exceeds [minRatio] (default WCAG AA 4.5:1).
     *
     * Strategy: lighten the lighter color and darken the darker color
     * iteratively until the ratio is satisfied.
     */
    fun enforceHighContrast(
        foreground: Color,
        background: Color,
        minRatio: Float = 4.5f
    ): Pair<Color, Color> {
        var fg = foreground
        var bg = background
        var currentRatio = calculateContrastRatio(fg, bg)

        if (currentRatio >= minRatio) return fg to bg

        val fgLum = relativeLuminance(fg)
        val bgLum = relativeLuminance(bg)
        val fgIsLighter = fgLum > bgLum

        var iterations = 0
        val maxIterations = 30
        val step = 0.05f

        while (currentRatio < minRatio && iterations < maxIterations) {
            if (fgIsLighter) {
                fg = lightenColor(fg, step)
                bg = darkenColor(bg, step)
            } else {
                fg = darkenColor(fg, step)
                bg = lightenColor(bg, step)
            }
            currentRatio = calculateContrastRatio(fg, bg)
            iterations++
        }

        return fg to bg
    }

    /**
     * WCAG 2.1 contrast ratio between two colours.
     * Returns a value in the range [1, 21].
     */
    fun calculateContrastRatio(color1: Color, color2: Color): Float {
        val lum1 = relativeLuminance(color1)
        val lum2 = relativeLuminance(color2)
        val lighter = max(lum1, lum2)
        val darker = min(lum1, lum2)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    /**
     * Transform an entire Material 3 [ColorScheme] according to
     * the given [AccessibilityConfig].
     */
    fun applyAccessibilityToColorScheme(
        scheme: ColorScheme,
        config: AccessibilityConfig
    ): ColorScheme {
        var result = scheme

        // Colorblind remapping
        config.colorblindMode?.let { mode ->
            result = result.copy(
                primary = remapColorForColorblind(result.primary, mode),
                onPrimary = remapColorForColorblind(result.onPrimary, mode),
                primaryContainer = remapColorForColorblind(result.primaryContainer, mode),
                onPrimaryContainer = remapColorForColorblind(result.onPrimaryContainer, mode),
                secondary = remapColorForColorblind(result.secondary, mode),
                onSecondary = remapColorForColorblind(result.onSecondary, mode),
                secondaryContainer = remapColorForColorblind(result.secondaryContainer, mode),
                onSecondaryContainer = remapColorForColorblind(result.onSecondaryContainer, mode),
                tertiary = remapColorForColorblind(result.tertiary, mode),
                onTertiary = remapColorForColorblind(result.onTertiary, mode),
                tertiaryContainer = remapColorForColorblind(result.tertiaryContainer, mode),
                onTertiaryContainer = remapColorForColorblind(result.onTertiaryContainer, mode),
                error = remapColorForColorblind(result.error, mode),
                onError = remapColorForColorblind(result.onError, mode),
                errorContainer = remapColorForColorblind(result.errorContainer, mode),
                onErrorContainer = remapColorForColorblind(result.onErrorContainer, mode),
                background = remapColorForColorblind(result.background, mode),
                onBackground = remapColorForColorblind(result.onBackground, mode),
                surface = remapColorForColorblind(result.surface, mode),
                onSurface = remapColorForColorblind(result.onSurface, mode),
                surfaceVariant = remapColorForColorblind(result.surfaceVariant, mode),
                onSurfaceVariant = remapColorForColorblind(result.onSurfaceVariant, mode),
                outline = remapColorForColorblind(result.outline, mode),
                outlineVariant = remapColorForColorblind(result.outlineVariant, mode)
            )
        }

        // High contrast enforcement
        if (config.highContrastEnabled) {
            val minRatio = config.minimumContrastRatio
            val (adjPrimary, adjOnPrimary) = enforceHighContrast(
                result.onPrimary, result.primary, minRatio
            )
            val (adjOnBg, adjBg) = enforceHighContrast(
                result.onBackground, result.background, minRatio
            )
            val (adjOnSurface, adjSurface) = enforceHighContrast(
                result.onSurface, result.surface, minRatio
            )
            val (adjSecondary, adjOnSecondary) = enforceHighContrast(
                result.onSecondary, result.secondary, minRatio
            )
            val (adjError, adjOnError) = enforceHighContrast(
                result.onError, result.error, minRatio
            )
            result = result.copy(
                primary = adjOnPrimary,
                onPrimary = adjPrimary,
                secondary = adjOnSecondary,
                onSecondary = adjSecondary,
                error = adjOnError,
                onError = adjError,
                background = adjBg,
                onBackground = adjOnBg,
                surface = adjSurface,
                onSurface = adjOnSurface
            )
        }

        return result
    }

    /**
     * Returns a [TextStyle] optimized for readers with dyslexia.
     * Increases letter spacing, word spacing, line height, and minimum size.
     */
    fun getDyslexiaTextStyle(baseStyle: TextStyle): TextStyle {
        val minSize = if ((baseStyle.fontSize.value) < 16f) 16.sp else baseStyle.fontSize
        return baseStyle.copy(
            fontSize = minSize,
            letterSpacing = 1.2.sp,
            lineHeight = (minSize.value * 1.8f).sp,
            fontWeight = baseStyle.fontWeight ?: FontWeight.Normal
        )
    }

    /**
     * Returns the recommended minimum touch target dimension.
     * 48 dp for large-target mode (Material guidelines), 40 dp otherwise.
     */
    fun getMinTouchTarget(config: AccessibilityConfig): Dp {
        return if (config.largeTouchTargets) 48.dp else 40.dp
    }

    // ── Private helpers ──────────────────────────────────────────

    /** Relative luminance per WCAG 2.1 (sRGB). */
    private fun relativeLuminance(color: Color): Float {
        val r = srgbToLinear(color.red)
        val g = srgbToLinear(color.green)
        val b = srgbToLinear(color.blue)
        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }

    /** sRGB → linear channel conversion. */
    private fun srgbToLinear(channel: Float): Float {
        return if (channel <= 0.04045f) {
            channel / 12.92f
        } else {
            ((channel + 0.055f) / 1.055f).pow(2.4f)
        }
    }

    /** Linear → sRGB channel conversion. */
    private fun linearToSrgb(channel: Float): Float {
        return if (channel <= 0.0031308f) {
            channel * 12.92f
        } else {
            1.055f * channel.pow(1f / 2.4f) - 0.055f
        }
    }

    private fun lightenColor(color: Color, amount: Float): Color {
        return Color(
            red = (color.red + amount).coerceIn(0f, 1f),
            green = (color.green + amount).coerceIn(0f, 1f),
            blue = (color.blue + amount).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    }

    private fun darkenColor(color: Color, amount: Float): Color {
        return Color(
            red = (color.red - amount).coerceIn(0f, 1f),
            green = (color.green - amount).coerceIn(0f, 1f),
            blue = (color.blue - amount).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// Composables — Contrast Checker
// ═══════════════════════════════════════════════════════════════════

/**
 * Visual tool displaying the WCAG contrast ratio between two colors
 * with AA / AAA pass/fail badges and a live text preview.
 * Suggests improved colors when the pair fails a given level.
 */
@Composable
fun ContrastChecker(
    foreground: Color,
    background: Color,
    modifier: Modifier = Modifier
) {
    val ratio = remember(foreground, background) {
        AccessibilityThemeEngine.calculateContrastRatio(foreground, background)
    }

    val passAA = ratio >= 4.5f
    val passAALarge = ratio >= 3.0f
    val passAAA = ratio >= 7.0f
    val passAAALarge = ratio >= 4.5f

    val improvedPair = remember(foreground, background) {
        if (!passAA) {
            AccessibilityThemeEngine.enforceHighContrast(foreground, background, 4.5f)
        } else null
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Contrast Checker",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Color swatches
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ColorSwatch(color = foreground, label = "FG")
                ColorSwatch(color = background, label = "BG")
                Text(
                    text = "Ratio  %.2f : 1".format(ratio),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(background, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Normal text preview — The quick brown fox",
                        color = foreground,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Large text preview",
                        color = foreground,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // WCAG badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                WcagBadge("AA", passAA, Modifier.weight(1f))
                WcagBadge("AA Large", passAALarge, Modifier.weight(1f))
                WcagBadge("AAA", passAAA, Modifier.weight(1f))
                WcagBadge("AAA Large", passAAALarge, Modifier.weight(1f))
            }

            // Suggested improvement
            if (improvedPair != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Suggested improvement:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(improvedPair.second, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Improved preview — %.2f : 1".format(
                            AccessibilityThemeEngine.calculateContrastRatio(
                                improvedPair.first,
                                improvedPair.second
                            )
                        ),
                        color = improvedPair.first,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WcagBadge(label: String, pass: Boolean, modifier: Modifier = Modifier) {
    val badgeColor by animateColorAsState(
        targetValue = if (pass) Color(0xFF2E7D32) else Color(0xFFC62828),
        animationSpec = tween(300),
        label = "badge_$label"
    )
    Row(
        modifier = modifier
            .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (pass) Icons.Default.Check else Icons.Default.Close,
            contentDescription = if (pass) "Pass" else "Fail",
            tint = badgeColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = badgeColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// Composables — Settings Panel
// ═══════════════════════════════════════════════════════════════════

/**
 * Full accessibility settings screen with live previews.
 */
@Composable
fun AccessibilitySettingsPanel(
    config: AccessibilityConfig,
    onConfigChange: (AccessibilityConfig) -> Unit,
    onPreviewAllModes: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPreviewGrid by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Accessibility,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Accessibility Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider()

        // ── High Contrast ──
        SettingsToggleRow(
            title = "High Contrast",
            subtitle = "Enforce WCAG-compliant contrast ratios",
            checked = config.highContrastEnabled,
            onCheckedChange = {
                onConfigChange(config.copy(highContrastEnabled = it))
            }
        )

        if (config.highContrastEnabled) {
            HighContrastPreview(config)
        }

        HorizontalDivider()

        // ── Colorblind Mode ──
        Text(
            text = "Colorblind Mode",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        val colorblindOptions: List<ColorblindMode?> = listOf(null) + ColorblindMode.entries
        colorblindOptions.forEach { mode ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onConfigChange(config.copy(colorblindMode = mode)) }
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = config.colorblindMode == mode,
                    onClick = { onConfigChange(config.copy(colorblindMode = mode)) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mode?.label ?: "None",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (mode != null) {
                        Text(
                            text = mode.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Color preview circles
                ColorblindPreviewDots(mode)
            }
        }

        HorizontalDivider()

        // ── Dyslexia-Friendly ──
        SettingsToggleRow(
            title = "Dyslexia-Friendly Text",
            subtitle = "Wider letter/line spacing and larger minimum font",
            checked = config.dyslexiaFontEnabled,
            onCheckedChange = {
                onConfigChange(config.copy(dyslexiaFontEnabled = it))
            }
        )

        if (config.dyslexiaFontEnabled) {
            DyslexiaTextPreview()
        }

        HorizontalDivider()

        // ── Reduced Motion ──
        SettingsToggleRow(
            title = "Reduced Motion",
            subtitle = "Minimize animations (also respects system setting)",
            checked = config.reducedMotion,
            onCheckedChange = {
                onConfigChange(config.copy(reducedMotion = it))
            }
        )

        HorizontalDivider()

        // ── Large Touch Targets ──
        SettingsToggleRow(
            title = "Large Touch Targets",
            subtitle = "Increase interactive element sizes to 48 dp minimum",
            checked = config.largeTouchTargets,
            onCheckedChange = {
                onConfigChange(config.copy(largeTouchTargets = it))
            }
        )

        HorizontalDivider()

        // ── Minimum Contrast Ratio Slider ──
        Text(
            text = "Minimum Contrast Ratio: %.1f : 1".format(config.minimumContrastRatio),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "WCAG AA = 4.5, AAA = 7.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = config.minimumContrastRatio,
            onValueChange = {
                onConfigChange(config.copy(minimumContrastRatio = it))
            },
            valueRange = 3.0f..7.0f,
            steps = 7
        )

        HorizontalDivider()

        // Preview All Modes button
        Button(
            onClick = {
                showPreviewGrid = !showPreviewGrid
                onPreviewAllModes()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Visibility, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Preview All Modes")
        }

        if (showPreviewGrid) {
            Spacer(modifier = Modifier.height(8.dp))
            AccessibilityPreviewGrid()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun HighContrastPreview(config: AccessibilityConfig) {
    val normalPrimary = MaterialTheme.colorScheme.primary
    val normalOnPrimary = MaterialTheme.colorScheme.onPrimary
    val (adjustedFg, adjustedBg) = remember(normalPrimary, normalOnPrimary, config.minimumContrastRatio) {
        AccessibilityThemeEngine.enforceHighContrast(
            normalOnPrimary, normalPrimary, config.minimumContrastRatio
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Before
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = normalPrimary)
        ) {
            Text(
                text = "Before",
                color = normalOnPrimary,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        // After
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = adjustedBg)
        ) {
            Text(
                text = "After",
                color = adjustedFg,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ColorblindPreviewDots(mode: ColorblindMode?) {
    val sampleColors = listOf(
        Color(0xFFE53935), // Red
        Color(0xFF43A047), // Green
        Color(0xFF1E88E5), // Blue
        Color(0xFFFDD835)  // Yellow
    )

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        sampleColors.forEach { base ->
            val displayColor = if (mode != null) {
                AccessibilityThemeEngine.remapColorForColorblind(base, mode)
            } else base
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(displayColor)
            )
        }
    }
}

@Composable
private fun DyslexiaTextPreview() {
    val baseStyle = MaterialTheme.typography.bodyMedium
    val dyslexiaStyle = remember(baseStyle) {
        AccessibilityThemeEngine.getDyslexiaTextStyle(baseStyle)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Dyslexia-Friendly Preview", style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The quick brown fox jumps over the lazy dog. " +
                    "SugarMunch makes candy collecting fun and accessible!",
                style = dyslexiaStyle
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Composables — Preview Grid
// ═══════════════════════════════════════════════════════════════════

/**
 * 2×4 grid where each card simulates a sample UI element under
 * one of the eight [AccessibilityMode] values.
 */
@Composable
fun AccessibilityPreviewGrid(modifier: Modifier = Modifier) {
    val modes = AccessibilityMode.entries

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Mode Previews",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // 2-column grid
        for (rowIndex in modes.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (colOffset in 0..1) {
                    val modeIndex = rowIndex + colOffset
                    if (modeIndex < modes.size) {
                        ModePreviewCard(
                            mode = modes[modeIndex],
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ModePreviewCard(
    mode: AccessibilityMode,
    modifier: Modifier = Modifier
) {
    val sampleBg: Color
    val sampleFg: Color
    val sampleAccent: Color

    when (mode) {
        AccessibilityMode.NONE -> {
            sampleBg = Color(0xFFFFF8E1)
            sampleFg = Color(0xFF212121)
            sampleAccent = Color(0xFFE91E63)
        }
        AccessibilityMode.HIGH_CONTRAST -> {
            sampleBg = Color.Black
            sampleFg = Color.White
            sampleAccent = Color.Yellow
        }
        AccessibilityMode.DEUTERANOPIA -> {
            val bg = Color(0xFFFFF8E1)
            val fg = Color(0xFF212121)
            val accent = Color(0xFFE91E63)
            sampleBg = AccessibilityThemeEngine.remapColorForColorblind(bg, ColorblindMode.DEUTERANOPIA)
            sampleFg = AccessibilityThemeEngine.remapColorForColorblind(fg, ColorblindMode.DEUTERANOPIA)
            sampleAccent = AccessibilityThemeEngine.remapColorForColorblind(accent, ColorblindMode.DEUTERANOPIA)
        }
        AccessibilityMode.PROTANOPIA -> {
            val bg = Color(0xFFFFF8E1)
            val fg = Color(0xFF212121)
            val accent = Color(0xFFE91E63)
            sampleBg = AccessibilityThemeEngine.remapColorForColorblind(bg, ColorblindMode.PROTANOPIA)
            sampleFg = AccessibilityThemeEngine.remapColorForColorblind(fg, ColorblindMode.PROTANOPIA)
            sampleAccent = AccessibilityThemeEngine.remapColorForColorblind(accent, ColorblindMode.PROTANOPIA)
        }
        AccessibilityMode.TRITANOPIA -> {
            val bg = Color(0xFFFFF8E1)
            val fg = Color(0xFF212121)
            val accent = Color(0xFFE91E63)
            sampleBg = AccessibilityThemeEngine.remapColorForColorblind(bg, ColorblindMode.TRITANOPIA)
            sampleFg = AccessibilityThemeEngine.remapColorForColorblind(fg, ColorblindMode.TRITANOPIA)
            sampleAccent = AccessibilityThemeEngine.remapColorForColorblind(accent, ColorblindMode.TRITANOPIA)
        }
        AccessibilityMode.DYSLEXIA_FRIENDLY -> {
            sampleBg = Color(0xFFFFF8E1)
            sampleFg = Color(0xFF212121)
            sampleAccent = Color(0xFFE91E63)
        }
        AccessibilityMode.REDUCED_MOTION -> {
            sampleBg = Color(0xFFFFF8E1)
            sampleFg = Color(0xFF212121)
            sampleAccent = Color(0xFFE91E63)
        }
        AccessibilityMode.LARGE_TARGETS -> {
            sampleBg = Color(0xFFFFF8E1)
            sampleFg = Color(0xFF212121)
            sampleAccent = Color(0xFFE91E63)
        }
    }

    Card(
        modifier = modifier.height(130.dp),
        colors = CardDefaults.cardColors(containerColor = sampleBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = mode.label,
                color = sampleFg,
                style = if (mode == AccessibilityMode.DYSLEXIA_FRIENDLY) {
                    AccessibilityThemeEngine.getDyslexiaTextStyle(
                        MaterialTheme.typography.labelSmall
                    )
                } else {
                    MaterialTheme.typography.labelSmall
                },
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Mini sample button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (mode == AccessibilityMode.LARGE_TARGETS) 48.dp else 32.dp)
                    .background(sampleAccent, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Button",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Mini sample text
            Text(
                text = "Sample candy text",
                color = sampleFg.copy(alpha = 0.7f),
                style = if (mode == AccessibilityMode.DYSLEXIA_FRIENDLY) {
                    AccessibilityThemeEngine.getDyslexiaTextStyle(
                        MaterialTheme.typography.bodySmall
                    )
                } else {
                    MaterialTheme.typography.bodySmall
                },
                maxLines = 1
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Composables — Colorblind Simulator
// ═══════════════════════════════════════════════════════════════════

/**
 * Displays a list of [colors] in four rows — Normal plus each
 * [ColorblindMode] — so users can compare how colors shift.
 */
@Composable
fun ColorblindSimulator(
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    data class SimRow(val label: String, val mode: ColorblindMode?)

    val rows = listOf(
        SimRow("Normal", null),
        SimRow(ColorblindMode.DEUTERANOPIA.label, ColorblindMode.DEUTERANOPIA),
        SimRow(ColorblindMode.PROTANOPIA.label, ColorblindMode.PROTANOPIA),
        SimRow(ColorblindMode.TRITANOPIA.label, ColorblindMode.TRITANOPIA)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Colorblind Simulator",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            rows.forEach { (label, mode) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(100.dp),
                        fontWeight = FontWeight.Medium
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(colors) { color ->
                            val displayColor = if (mode != null) {
                                AccessibilityThemeEngine.remapColorForColorblind(color, mode)
                            } else color

                            Canvas(modifier = Modifier.size(32.dp)) {
                                drawRoundRect(
                                    color = displayColor,
                                    cornerRadius = CornerRadius(6.dp.toPx()),
                                    size = size
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
