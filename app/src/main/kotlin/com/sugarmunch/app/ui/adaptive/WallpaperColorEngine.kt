/**
 * WallpaperColorEngine.kt — Phase 5.5
 *
 * Dynamic wallpaper color extraction and theme generation for SugarMunch.
 * Provides color palette extraction from bitmaps, tonal palette generation
 * via HSL manipulation, and full light/dark color-scheme derivation.
 * All rendering uses Canvas drawing primitives (no AGSL/RuntimeShader).
 */
package com.sugarmunch.app.ui.adaptive

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────────────────────
// Data Models
// ─────────────────────────────────────────────────────────────────────────────

data class WallpaperPalette(
    val dominant: Color,
    val vibrant: Color,
    val muted: Color,
    val darkVibrant: Color,
    val lightVibrant: Color,
    val darkMuted: Color,
    val lightMuted: Color
)

data class TonalPalette(
    val tone0: Color,
    val tone10: Color,
    val tone20: Color,
    val tone30: Color,
    val tone40: Color,
    val tone50: Color,
    val tone60: Color,
    val tone70: Color,
    val tone80: Color,
    val tone90: Color,
    val tone95: Color,
    val tone99: Color,
    val tone100: Color
) {
    fun asList(): List<Color> = listOf(
        tone0, tone10, tone20, tone30, tone40, tone50, tone60,
        tone70, tone80, tone90, tone95, tone99, tone100
    )
}

data class WallpaperColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color
)

// ─────────────────────────────────────────────────────────────────────────────
// HSL helpers
// ─────────────────────────────────────────────────────────────────────────────

private data class HSL(val h: Float, val s: Float, val l: Float)

private fun Color.toHSL(): HSL {
    val r = red
    val g = green
    val b = blue
    val cMax = max(r, max(g, b))
    val cMin = min(r, min(g, b))
    val delta = cMax - cMin

    val l = (cMax + cMin) / 2f

    val s = if (delta == 0f) 0f
    else delta / (1f - abs(2f * l - 1f))

    val h = when {
        delta == 0f -> 0f
        cMax == r -> 60f * (((g - b) / delta) % 6f)
        cMax == g -> 60f * (((b - r) / delta) + 2f)
        else -> 60f * (((r - g) / delta) + 4f)
    }

    return HSL(
        h = if (h < 0f) h + 360f else h,
        s = s.coerceIn(0f, 1f),
        l = l.coerceIn(0f, 1f)
    )
}

private fun HSL.toColor(alpha: Float = 1f): Color {
    val c = (1f - abs(2f * l - 1f)) * s
    val x = c * (1f - abs((h / 60f) % 2f - 1f))
    val m = l - c / 2f

    val (r1, g1, b1) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = (r1 + m).coerceIn(0f, 1f),
        green = (g1 + m).coerceIn(0f, 1f),
        blue = (b1 + m).coerceIn(0f, 1f),
        alpha = alpha
    )
}

private fun lerpColor(a: Color, b: Color, t: Float): Color {
    val f = t.coerceIn(0f, 1f)
    return Color(
        red = a.red + (b.red - a.red) * f,
        green = a.green + (b.green - a.green) * f,
        blue = a.blue + (b.blue - a.blue) * f,
        alpha = a.alpha + (b.alpha - a.alpha) * f
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// WallpaperColorEngine
// ─────────────────────────────────────────────────────────────────────────────

object WallpaperColorEngine {

    /**
     * Extracts a [WallpaperPalette] from a [Bitmap] using AndroidX Palette.
     */
    fun extractFromBitmap(bitmap: Bitmap): WallpaperPalette {
        val palette = Palette.from(bitmap).maximumColorCount(16).generate()
        val fallback = 0xFF6750A4.toInt() // Material default purple

        fun Int.toComposeColor(): Color = Color(this)

        return WallpaperPalette(
            dominant = (palette.getDominantColor(fallback)).toComposeColor(),
            vibrant = (palette.getVibrantColor(fallback)).toComposeColor(),
            muted = (palette.getMutedColor(fallback)).toComposeColor(),
            darkVibrant = (palette.getDarkVibrantColor(fallback)).toComposeColor(),
            lightVibrant = (palette.getLightVibrantColor(fallback)).toComposeColor(),
            darkMuted = (palette.getDarkMutedColor(fallback)).toComposeColor(),
            lightMuted = (palette.getLightMutedColor(fallback)).toComposeColor()
        )
    }

    /**
     * Generates a Material You-style tonal palette from a seed [Color].
     * Shifts lightness across 13 canonical tonal stops while preserving hue.
     */
    fun generateTonalPalette(seedColor: Color): TonalPalette {
        val hsl = seedColor.toHSL()
        // Slight desaturation at extreme lightness to mimic Material tonal palettes
        fun toneAt(lightness: Float): Color {
            val satAdjust = when {
                lightness < 0.15f -> hsl.s * 0.7f
                lightness > 0.85f -> hsl.s * 0.5f
                else -> hsl.s * (0.85f + 0.15f * (1f - abs(lightness - 0.5f) * 2f))
            }
            return HSL(hsl.h, satAdjust.coerceIn(0f, 1f), lightness).toColor()
        }

        return TonalPalette(
            tone0 = toneAt(0.00f),
            tone10 = toneAt(0.10f),
            tone20 = toneAt(0.20f),
            tone30 = toneAt(0.30f),
            tone40 = toneAt(0.40f),
            tone50 = toneAt(0.50f),
            tone60 = toneAt(0.60f),
            tone70 = toneAt(0.70f),
            tone80 = toneAt(0.80f),
            tone90 = toneAt(0.90f),
            tone95 = toneAt(0.95f),
            tone99 = toneAt(0.99f),
            tone100 = toneAt(1.00f)
        )
    }

    /**
     * Generates a full light/dark [WallpaperColorScheme] from extracted palette.
     */
    fun generateColorScheme(palette: WallpaperPalette, isDark: Boolean): WallpaperColorScheme {
        val primaryTonal = generateTonalPalette(palette.vibrant)
        val secondaryTonal = generateTonalPalette(palette.muted)
        val tertiaryTonal = generateTonalPalette(
            lerpColor(palette.vibrant, palette.lightVibrant, 0.5f)
        )

        return if (isDark) {
            WallpaperColorScheme(
                primary = primaryTonal.tone80,
                onPrimary = primaryTonal.tone20,
                primaryContainer = primaryTonal.tone30,
                onPrimaryContainer = primaryTonal.tone90,
                secondary = secondaryTonal.tone80,
                onSecondary = secondaryTonal.tone20,
                secondaryContainer = secondaryTonal.tone30,
                onSecondaryContainer = secondaryTonal.tone90,
                tertiary = tertiaryTonal.tone80,
                background = primaryTonal.tone10,
                surface = primaryTonal.tone10,
                surfaceVariant = secondaryTonal.tone30
            )
        } else {
            WallpaperColorScheme(
                primary = primaryTonal.tone40,
                onPrimary = primaryTonal.tone100,
                primaryContainer = primaryTonal.tone90,
                onPrimaryContainer = primaryTonal.tone10,
                secondary = secondaryTonal.tone40,
                onSecondary = secondaryTonal.tone100,
                secondaryContainer = secondaryTonal.tone90,
                onSecondaryContainer = secondaryTonal.tone10,
                tertiary = tertiaryTonal.tone40,
                background = primaryTonal.tone99,
                surface = primaryTonal.tone99,
                surfaceVariant = secondaryTonal.tone90
            )
        }
    }

    /**
     * Blends wallpaper-derived colors into the current Material 3 [ColorScheme].
     * [intensity] ranges from 0 (no wallpaper influence) to 1 (full wallpaper colors).
     */
    fun blendWithCurrentTheme(
        wallpaperColors: WallpaperColorScheme,
        currentTheme: ColorScheme,
        intensity: Float
    ): ColorScheme {
        val t = intensity.coerceIn(0f, 1f)
        return currentTheme.copy(
            primary = lerpColor(currentTheme.primary, wallpaperColors.primary, t),
            onPrimary = lerpColor(currentTheme.onPrimary, wallpaperColors.onPrimary, t),
            primaryContainer = lerpColor(
                currentTheme.primaryContainer, wallpaperColors.primaryContainer, t
            ),
            onPrimaryContainer = lerpColor(
                currentTheme.onPrimaryContainer, wallpaperColors.onPrimaryContainer, t
            ),
            secondary = lerpColor(currentTheme.secondary, wallpaperColors.secondary, t),
            onSecondary = lerpColor(currentTheme.onSecondary, wallpaperColors.onSecondary, t),
            secondaryContainer = lerpColor(
                currentTheme.secondaryContainer, wallpaperColors.secondaryContainer, t
            ),
            onSecondaryContainer = lerpColor(
                currentTheme.onSecondaryContainer, wallpaperColors.onSecondaryContainer, t
            ),
            tertiary = lerpColor(currentTheme.tertiary, wallpaperColors.tertiary, t),
            background = lerpColor(currentTheme.background, wallpaperColors.background, t),
            surface = lerpColor(currentTheme.surface, wallpaperColors.surface, t),
            surfaceVariant = lerpColor(
                currentTheme.surfaceVariant, wallpaperColors.surfaceVariant, t
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sample wallpaper presets (for demo/preview when real wallpaper unavailable)
// ─────────────────────────────────────────────────────────────────────────────

private data class WallpaperPreset(
    val name: String,
    val colors: List<Color>,
    val palette: WallpaperPalette
)

private val SampleWallpapers = listOf(
    WallpaperPreset(
        name = "Beach",
        colors = listOf(Color(0xFF87CEEB), Color(0xFFFFD700), Color(0xFFF5DEB3)),
        palette = WallpaperPalette(
            dominant = Color(0xFF87CEEB),
            vibrant = Color(0xFF00BFFF),
            muted = Color(0xFFF5DEB3),
            darkVibrant = Color(0xFF006994),
            lightVibrant = Color(0xFF87CEEB),
            darkMuted = Color(0xFF8B7D6B),
            lightMuted = Color(0xFFFFF8DC)
        )
    ),
    WallpaperPreset(
        name = "Forest",
        colors = listOf(Color(0xFF228B22), Color(0xFF2E8B57), Color(0xFF6B8E23)),
        palette = WallpaperPalette(
            dominant = Color(0xFF2E8B57),
            vibrant = Color(0xFF32CD32),
            muted = Color(0xFF6B8E23),
            darkVibrant = Color(0xFF006400),
            lightVibrant = Color(0xFF90EE90),
            darkMuted = Color(0xFF556B2F),
            lightMuted = Color(0xFFA9C89A)
        )
    ),
    WallpaperPreset(
        name = "City Night",
        colors = listOf(Color(0xFF191970), Color(0xFF4169E1), Color(0xFFFFD700)),
        palette = WallpaperPalette(
            dominant = Color(0xFF191970),
            vibrant = Color(0xFF4169E1),
            muted = Color(0xFF2F4F4F),
            darkVibrant = Color(0xFF0D0D5E),
            lightVibrant = Color(0xFF6495ED),
            darkMuted = Color(0xFF1C1C3D),
            lightMuted = Color(0xFF778899)
        )
    ),
    WallpaperPreset(
        name = "Sunset",
        colors = listOf(Color(0xFFFF4500), Color(0xFFFF8C00), Color(0xFFFFD700)),
        palette = WallpaperPalette(
            dominant = Color(0xFFFF8C00),
            vibrant = Color(0xFFFF4500),
            muted = Color(0xFFCD853F),
            darkVibrant = Color(0xFFCC3700),
            lightVibrant = Color(0xFFFF6347),
            darkMuted = Color(0xFF8B4513),
            lightMuted = Color(0xFFFFDAB9)
        )
    ),
    WallpaperPreset(
        name = "Candy Shop",
        colors = listOf(Color(0xFFFF69B4), Color(0xFFFF1493), Color(0xFFFFB6C1)),
        palette = WallpaperPalette(
            dominant = Color(0xFFFF69B4),
            vibrant = Color(0xFFFF1493),
            muted = Color(0xFFDDA0DD),
            darkVibrant = Color(0xFFC71585),
            lightVibrant = Color(0xFFFFB6C1),
            darkMuted = Color(0xFF8B5A8B),
            lightMuted = Color(0xFFFFE4E1)
        )
    ),
    WallpaperPreset(
        name = "Galaxy",
        colors = listOf(Color(0xFF4B0082), Color(0xFF8A2BE2), Color(0xFF0D0D2B)),
        palette = WallpaperPalette(
            dominant = Color(0xFF4B0082),
            vibrant = Color(0xFF8A2BE2),
            muted = Color(0xFF483D8B),
            darkVibrant = Color(0xFF2E0854),
            lightVibrant = Color(0xFF9370DB),
            darkMuted = Color(0xFF1C1040),
            lightMuted = Color(0xFFB0A4D4)
        )
    )
)

// ─────────────────────────────────────────────────────────────────────────────
// WallpaperPreviewPanel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun WallpaperPreviewPanel(
    palette: WallpaperPalette?,
    intensity: Float,
    onIntensityChanged: (Float) -> Unit,
    showBefore: Boolean,
    onToggleBefore: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            "Wallpaper Colors",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        // Wallpaper thumbnail / placeholder gradient
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            if (palette != null) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val brush = Brush.horizontalGradient(
                        colors = listOf(
                            palette.dominant,
                            palette.vibrant,
                            palette.lightVibrant,
                            palette.muted
                        )
                    )
                    drawRect(brush = brush)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    MaterialTheme.colorScheme.tertiaryContainer
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No wallpaper selected", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Extracted color circles
        if (palette != null) {
            Text("Extracted Colors", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            val colorEntries = listOf(
                "Dominant" to palette.dominant,
                "Vibrant" to palette.vibrant,
                "Muted" to palette.muted,
                "Dark Vib." to palette.darkVibrant,
                "Light Vib." to palette.lightVibrant,
                "Dark Mut." to palette.darkMuted,
                "Light Mut." to palette.lightMuted
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                colorEntries.forEach { (label, color) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Tonal palette gradient bar
            Text("Tonal Palette", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            val tonal = remember(palette) {
                WallpaperColorEngine.generateTonalPalette(palette.vibrant)
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                val tones = tonal.asList()
                val sliceWidth = size.width / tones.size
                tones.forEachIndexed { idx, color ->
                    drawRect(
                        color = color,
                        topLeft = Offset(idx * sliceWidth, 0f),
                        size = Size(sliceWidth + 1f, size.height)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Before / After toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Show original theme", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.width(8.dp))
            Switch(checked = showBefore, onCheckedChange = onToggleBefore)
        }

        Spacer(Modifier.height(12.dp))

        // Intensity slider
        Text("Wallpaper Influence", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("0%", style = MaterialTheme.typography.labelSmall)
            Slider(
                value = intensity,
                onValueChange = onIntensityChanged,
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f)
            )
            Text("100%", style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WallpaperSettingsPanel
// ─────────────────────────────────────────────────────────────────────────────

enum class WallpaperSource(val label: String) {
    HOME_SCREEN("Home Screen"),
    LOCK_SCREEN("Lock Screen"),
    CUSTOM_IMAGE("Custom Image")
}

@Composable
fun WallpaperSettingsPanel(
    enabled: Boolean,
    onEnabledChanged: (Boolean) -> Unit,
    intensity: Float,
    onIntensityChanged: (Float) -> Unit,
    source: WallpaperSource,
    onSourceChanged: (WallpaperSource) -> Unit,
    autoUpdate: Boolean,
    onAutoUpdateChanged: (Boolean) -> Unit,
    excludedColors: Set<String>,
    onToggleExcludedColor: (String) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Wallpaper Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        // Enable / disable
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Wallpaper Integration",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "Derive theme colors from wallpaper",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = enabled, onCheckedChange = onEnabledChanged)
        }

        Spacer(Modifier.height(16.dp))

        // Intensity
        Text("Intensity", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Low", style = MaterialTheme.typography.labelSmall)
            Slider(
                value = intensity,
                onValueChange = onIntensityChanged,
                valueRange = 0f..1f,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
            Text("High", style = MaterialTheme.typography.labelSmall)
        }
        Text(
            "${(intensity * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        // Source picker
        Text("Source", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WallpaperSource.entries.forEach { src ->
                FilterChip(
                    selected = src == source,
                    onClick = { onSourceChanged(src) },
                    label = { Text(src.label, style = MaterialTheme.typography.labelSmall) },
                    enabled = enabled
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Auto-update
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Auto-Update", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Detect wallpaper changes in real-time",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = autoUpdate,
                onCheckedChange = onAutoUpdateChanged,
                enabled = enabled
            )
        }

        Spacer(Modifier.height(16.dp))

        // Color override chips
        Text("Exclude Colors", style = MaterialTheme.typography.titleSmall)
        Text(
            "Tap to exclude extracted colors from theme generation",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        val colorLabels = listOf(
            "Dominant", "Vibrant", "Muted",
            "Dark Vibrant", "Light Vibrant", "Dark Muted", "Light Muted"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            colorLabels.forEach { label ->
                val isExcluded = excludedColors.contains(label)
                FilterChip(
                    selected = isExcluded,
                    onClick = { onToggleExcludedColor(label) },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    enabled = enabled
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset")
            }
            Button(
                onClick = onApply,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            ) {
                Text("Apply")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WallpaperColorDemo
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun WallpaperColorDemo(modifier: Modifier = Modifier) {
    var selectedPreset by remember { mutableStateOf<WallpaperPreset?>(null) }
    var isDark by remember { mutableStateOf(false) }
    var intensity by remember { mutableFloatStateOf(0.75f) }

    val wallpaperScheme = remember(selectedPreset, isDark) {
        selectedPreset?.let {
            WallpaperColorEngine.generateColorScheme(it.palette, isDark)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Wallpaper Color Demo",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Tap a wallpaper to see generated theme colors",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        // 6 sample wallpapers — 2x3 grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                SampleWallpapers.filterIndexed { i, _ -> i % 2 == 0 }.forEach { preset ->
                    WallpaperCard(
                        preset = preset,
                        isSelected = preset == selectedPreset,
                        onClick = { selectedPreset = preset }
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                SampleWallpapers.filterIndexed { i, _ -> i % 2 == 1 }.forEach { preset ->
                    WallpaperCard(
                        preset = preset,
                        isSelected = preset == selectedPreset,
                        onClick = { selectedPreset = preset }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Dark / light toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Dark Mode", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(8.dp))
            Switch(checked = isDark, onCheckedChange = { isDark = it })
        }

        // Intensity
        Text("Intensity", style = MaterialTheme.typography.titleSmall)
        Slider(
            value = intensity,
            onValueChange = { intensity = it },
            valueRange = 0f..1f
        )

        Spacer(Modifier.height(16.dp))

        // Sample UI elements with generated colors
        if (wallpaperScheme != null) {
            val scheme = wallpaperScheme!!
            val animPrimary by animateColorAsState(scheme.primary, tween(400), label = "p")
            val animOnPrimary by animateColorAsState(scheme.onPrimary, tween(400), label = "op")
            val animContainer by animateColorAsState(
                scheme.primaryContainer, tween(400), label = "pc"
            )
            val animOnContainer by animateColorAsState(
                scheme.onPrimaryContainer, tween(400), label = "opc"
            )
            val animSecondary by animateColorAsState(scheme.secondary, tween(400), label = "s")
            val animSurface by animateColorAsState(scheme.surface, tween(400), label = "sf")
            val animSurfaceVar by animateColorAsState(
                scheme.surfaceVariant, tween(400), label = "sv"
            )

            Text(
                "Generated Theme Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            // Color scheme swatches
            Text("Color Scheme", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                val colors = listOf(
                    animPrimary, animOnPrimary, animContainer, animOnContainer,
                    animSecondary, animSurface, animSurfaceVar
                )
                val w = size.width / colors.size
                colors.forEachIndexed { idx, c ->
                    drawRect(
                        color = c,
                        topLeft = Offset(idx * w, 0f),
                        size = Size(w + 1f, size.height)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Sample Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = animContainer,
                    contentColor = animOnContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Sample Card",
                        style = MaterialTheme.typography.titleMedium,
                        color = animOnContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "This card uses the generated primary container color from the ${selectedPreset?.name ?: "selected"} wallpaper.",
                        style = MaterialTheme.typography.bodySmall,
                        color = animOnContainer.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {}) {
                            Text("Primary")
                        }
                        OutlinedButton(onClick = {}) {
                            Text("Outlined")
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Sample Surface
            Surface(
                color = animSurfaceVar,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Surface Variant",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Demonstrates the surface variant color from wallpaper extraction.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Sample FAB
            ExtendedFloatingActionButton(
                onClick = {},
                containerColor = animPrimary,
                contentColor = animOnPrimary
            ) {
                Text("Sample FAB")
            }

            Spacer(Modifier.height(12.dp))

            // Tonal palette strip
            val tonalPalette = remember(selectedPreset) {
                selectedPreset?.let {
                    WallpaperColorEngine.generateTonalPalette(it.palette.vibrant)
                }
            }
            if (tonalPalette != null) {
                Text("Tonal Palette", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    val tones = tonalPalette!!.asList()
                    val w = size.width / tones.size
                    tones.forEachIndexed { idx, c ->
                        drawRect(
                            color = c,
                            topLeft = Offset(idx * w, 0f),
                            size = Size(w + 1f, size.height)
                        )
                    }
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "Select a wallpaper above to preview generated theme",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WallpaperCard helper composable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WallpaperCard(
    preset: WallpaperPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(250),
        label = "wBorder"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val brush = Brush.horizontalGradient(preset.colors)
                drawRoundRect(
                    brush = brush,
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    size = size,
                    style = Fill
                )
            }
            // Label overlay
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = preset.name,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
