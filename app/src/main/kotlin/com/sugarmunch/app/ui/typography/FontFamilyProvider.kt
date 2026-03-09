package com.sugarmunch.app.ui.typography

import androidx.compose.ui.text.font.Font
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.R
import com.sugarmunch.app.theme.profile.ThemeFontAxisValue

@OptIn(ExperimentalStdlibApi::class)
enum class SugarFontFamily(val displayName: String, val description: String) {
    SYSTEM_DEFAULT("System Default", "Device default font"),
    SANS_SERIF("Sans Serif", "Clean and modern"),
    SERIF("Serif", "Classic and elegant"),
    MONOSPACE("Monospace", "Technical and precise"),
    CURSIVE("Cursive", "Playful and decorative"),
    NUNITO_VARIABLE("Nunito Variable", "Rounded variable font with soft candy energy"),
    COMFORTAA_VARIABLE("Comfortaa Variable", "Playful geometric variable font"),
    SPACE_GROTESK_VARIABLE("Space Grotesk Variable", "Modern display variable font"),
    SANS_SERIF_MEDIUM("Sans Serif Medium", "Medium weight sans serif"),
    SANS_SERIF_LIGHT("Sans Serif Light", "Light and airy"),
    SANS_SERIF_CONDENSED("Sans Serif Condensed", "Compact and efficient")
}

fun SugarFontFamily.toComposeFontFamily(
    axes: List<ThemeFontAxisValue> = emptyList()
): FontFamily = when (this) {
    SugarFontFamily.SYSTEM_DEFAULT -> FontFamily.Default
    SugarFontFamily.SANS_SERIF -> FontFamily.SansSerif
    SugarFontFamily.SERIF -> FontFamily.Serif
    SugarFontFamily.MONOSPACE -> FontFamily.Monospace
    SugarFontFamily.CURSIVE -> FontFamily.Cursive
    SugarFontFamily.NUNITO_VARIABLE -> variableFontFamily(R.font.nunito_variable, axes)
    SugarFontFamily.COMFORTAA_VARIABLE -> variableFontFamily(R.font.comfortaa_variable, axes)
    SugarFontFamily.SPACE_GROTESK_VARIABLE -> variableFontFamily(R.font.space_grotesk_variable, axes)
    SugarFontFamily.SANS_SERIF_MEDIUM -> FontFamily.SansSerif
    SugarFontFamily.SANS_SERIF_LIGHT -> FontFamily.SansSerif
    SugarFontFamily.SANS_SERIF_CONDENSED -> FontFamily.SansSerif
}

data class FontPairing(
    val name: String,
    val headingFont: SugarFontFamily,
    val bodyFont: SugarFontFamily,
    val captionFont: SugarFontFamily
)

object FontPairings {
    val Classic = FontPairing("Classic", SugarFontFamily.SERIF, SugarFontFamily.SANS_SERIF, SugarFontFamily.SANS_SERIF)
    val Modern = FontPairing("Modern", SugarFontFamily.SANS_SERIF, SugarFontFamily.SANS_SERIF_LIGHT, SugarFontFamily.SANS_SERIF)
    val Technical = FontPairing("Technical", SugarFontFamily.MONOSPACE, SugarFontFamily.SANS_SERIF, SugarFontFamily.MONOSPACE)
    val Playful = FontPairing("Playful", SugarFontFamily.CURSIVE, SugarFontFamily.SANS_SERIF, SugarFontFamily.SANS_SERIF)
    val Elegant = FontPairing("Elegant", SugarFontFamily.SERIF, SugarFontFamily.SERIF, SugarFontFamily.SANS_SERIF_LIGHT)
    val Compact = FontPairing("Compact", SugarFontFamily.SANS_SERIF_CONDENSED, SugarFontFamily.SANS_SERIF_CONDENSED, SugarFontFamily.SANS_SERIF_CONDENSED)
    val Bold = FontPairing("Bold", SugarFontFamily.SANS_SERIF_MEDIUM, SugarFontFamily.SANS_SERIF, SugarFontFamily.SANS_SERIF)
    val Candy = FontPairing("Candy", SugarFontFamily.CURSIVE, SugarFontFamily.SANS_SERIF, SugarFontFamily.CURSIVE)

    val all = listOf(Classic, Modern, Technical, Playful, Elegant, Compact, Bold, Candy)
}

fun FontPairing.toTypography(scale: TypeScale = TypeScale.DEFAULT): Typography {
    val config = scale.toConfig()
    val headingFamily = headingFont.toComposeFontFamily()
    val bodyFamily = bodyFont.toComposeFontFamily()
    val captionFamily = captionFont.toComposeFontFamily()

    return Typography(
        displayLarge = config.displayLarge.copy(fontFamily = headingFamily),
        displayMedium = config.displayMedium.copy(fontFamily = headingFamily),
        displaySmall = config.displaySmall.copy(fontFamily = headingFamily),
        headlineLarge = config.headlineLarge.copy(fontFamily = headingFamily),
        headlineMedium = config.headlineMedium.copy(fontFamily = headingFamily),
        headlineSmall = config.headlineSmall.copy(fontFamily = headingFamily),
        titleLarge = config.titleLarge.copy(fontFamily = headingFamily),
        titleMedium = config.titleMedium.copy(fontFamily = bodyFamily),
        titleSmall = config.titleSmall.copy(fontFamily = bodyFamily),
        bodyLarge = config.bodyLarge.copy(fontFamily = bodyFamily),
        bodyMedium = config.bodyMedium.copy(fontFamily = bodyFamily),
        bodySmall = config.bodySmall.copy(fontFamily = captionFamily),
        labelLarge = config.labelLarge.copy(fontFamily = captionFamily),
        labelMedium = config.labelMedium.copy(fontFamily = captionFamily),
        labelSmall = config.labelSmall.copy(fontFamily = captionFamily)
    )
}

@OptIn(ExperimentalStdlibApi::class)
private fun variableFontFamily(
    resId: Int,
    axes: List<ThemeFontAxisValue>
): FontFamily {
    val settings = buildVariationSettings(axes)
    return FontFamily(
        Font(
            resId = resId,
            weight = FontWeight.Normal,
            style = FontStyle.Normal,
            variationSettings = settings
        )
    )
}

private fun buildVariationSettings(axes: List<ThemeFontAxisValue>): FontVariation.Settings {
    val settings = axes.map { axis ->
        when (axis.tag.lowercase()) {
            "wght" -> FontVariation.weight(axis.value.toInt())
            "wdth" -> FontVariation.width(axis.value)
            "ital" -> FontVariation.italic(axis.value)
            "slnt" -> FontVariation.slant(axis.value)
            "opsz" -> FontVariation.opticalSizing(axis.value.sp)
            else -> FontVariation.Setting(axis.tag.take(4), axis.value)
        }
    }
    return FontVariation.Settings(
        weight = FontWeight.Normal,
        style = FontStyle.Normal,
        *settings.toTypedArray()
    )
}
