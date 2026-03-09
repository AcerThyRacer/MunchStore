package com.sugarmunch.app.ui.studio

import androidx.compose.ui.graphics.Color
import com.google.gson.Gson
import com.sugarmunch.app.theme.profile.ThemeTransportEnvelope
import com.sugarmunch.app.theme.profile.parseThemeImportCandidate
import com.sugarmunch.app.theme.profile.toHexString
import com.sugarmunch.app.theme.profile.toJson
import com.sugarmunch.app.theme.profile.toThemeProfile
import com.sugarmunch.app.ui.typography.FontPairings
import com.sugarmunch.app.ui.visual.GradientSpec
import com.sugarmunch.app.ui.visual.GradientStop
import com.sugarmunch.app.ui.visual.GradientType
import com.sugarmunch.app.ui.visual.PatternSpec
import com.sugarmunch.app.ui.visual.PatternType
import kotlinx.serialization.Serializable

// ── Exportable data models ────────────────────────────────────────────────────

@Serializable
data class ExportableTheme(
    val name: String,
    val version: Int = 1,
    val colors: ExportableColors,
    val gradient: ExportableGradient? = null,
    val pattern: ExportablePattern? = null,
    val fontPairing: String? = null,
    val isDark: Boolean = false
)

@Serializable
data class ExportableColors(
    val primary: String,
    val secondary: String,
    val tertiary: String,
    val surface: String,
    val background: String,
    val accent: String
)

@Serializable
data class ExportableGradient(
    val type: String,
    val stops: List<ExportableGradientStop>,
    val angleDegrees: Float
)

@Serializable
data class ExportableGradientStop(
    val color: String,
    val position: Float
)

@Serializable
data class ExportablePattern(
    val type: String,
    val primaryColor: String,
    val secondaryColor: String,
    val scale: Float,
    val opacity: Float,
    val rotation: Float
)

// ── CustomThemeSpec ↔ ExportableTheme ─────────────────────────────────────────

fun CustomThemeSpec.toExportable(): ExportableTheme = ExportableTheme(
    name = name,
    colors = ExportableColors(
        primary = primaryColor.toHexString(),
        secondary = secondaryColor.toHexString(),
        tertiary = tertiaryColor.toHexString(),
        surface = surfaceColor.toHexString(),
        background = backgroundColor.toHexString(),
        accent = accentColor.toHexString()
    ),
    gradient = gradient?.let { g ->
        ExportableGradient(
            type = g.type.name,
            stops = g.stops.map { ExportableGradientStop(it.color.toHexString(), it.position) },
            angleDegrees = g.angleDegrees
        )
    },
    pattern = pattern?.let { p ->
        ExportablePattern(
            type = p.type.name,
            primaryColor = p.primaryColor.toHexString(),
            secondaryColor = p.secondaryColor.toHexString(),
            scale = p.scale,
            opacity = p.opacity,
            rotation = p.rotation
        )
    },
    fontPairing = fontPairing?.name,
    isDark = isDark
)

fun ExportableTheme.toCustomThemeSpec(): CustomThemeSpec = CustomThemeSpec(
    name = name,
    primaryColor = colors.primary.toComposeColor(),
    secondaryColor = colors.secondary.toComposeColor(),
    tertiaryColor = colors.tertiary.toComposeColor(),
    surfaceColor = colors.surface.toComposeColor(),
    backgroundColor = colors.background.toComposeColor(),
    accentColor = colors.accent.toComposeColor(),
    gradient = gradient?.let { g ->
        GradientSpec(
            type = GradientType.valueOf(g.type),
            stops = g.stops.map { GradientStop(it.color.toComposeColor(), it.position) },
            angleDegrees = g.angleDegrees
        )
    },
    pattern = pattern?.let { p ->
        PatternSpec(
            type = PatternType.valueOf(p.type),
            primaryColor = p.primaryColor.toComposeColor(),
            secondaryColor = p.secondaryColor.toComposeColor(),
            scale = p.scale,
            opacity = p.opacity,
            rotation = p.rotation
        )
    },
    fontPairing = fontPairing?.let { name ->
        FontPairings.all.firstOrNull { it.name == name }
    },
    isDark = isDark
)

// ── JSON serialization ────────────────────────────────────────────────────────

fun ExportableTheme.toJson(): String = ThemeTransportEnvelope(
    profile = toCustomThemeSpec().toThemeProfile()
).toJson(Gson())

fun parseThemeJson(json: String): ExportableTheme? =
    parseThemeImportCandidate(json, Gson())?.profile?.toCustomThemeSpec()?.toExportable()

// ── Color ↔ Hex helpers ───────────────────────────────────────────────────────

fun Color.toHexString(): String = com.sugarmunch.app.theme.profile.toHexString()

fun String.toComposeColor(): Color {
    val hex = removePrefix("#")
    require(hex.length == 6 || hex.length == 8) { "Invalid hex color: $this" }
    val colorLong = hex.toLong(16)
    return if (hex.length == 8) {
        Color(colorLong.toInt())
    } else {
        Color(
            red = ((colorLong shr 16) and 0xFF).toInt(),
            green = ((colorLong shr 8) and 0xFF).toInt(),
            blue = (colorLong and 0xFF).toInt()
        )
    }
}
