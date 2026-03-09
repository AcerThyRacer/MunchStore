package com.sugarmunch.app.ui.typography

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import com.sugarmunch.app.theme.profile.ImportedFontAsset
import com.sugarmunch.app.theme.profile.ThemeFontAxisValue
import com.sugarmunch.app.theme.profile.ThemeFontRef
import com.sugarmunch.app.theme.profile.ThemeFontSource
import com.sugarmunch.app.theme.profile.ThemeTypographySpec
import com.sugarmunch.app.theme.profile.toSugarFontFamily

fun ThemeTypographySpec.toDynamicTypographyConfig(
    context: Context,
    importedFonts: List<ImportedFontAsset>
): DynamicTypographyConfig {
    return DynamicTypographyConfig(
        headingFont = headingFont.toSugarFontFamily(),
        bodyFont = bodyFont.toSugarFontFamily(),
        captionFont = captionFont.toSugarFontFamily(),
        headingAxes = headingFont.axes,
        bodyAxes = bodyFont.axes,
        captionAxes = captionFont.axes,
        headingTypeface = headingFont.toImportedTypeface(context, importedFonts),
        bodyTypeface = bodyFont.toImportedTypeface(context, importedFonts),
        captionTypeface = captionFont.toImportedTypeface(context, importedFonts),
        scale = typeScale,
        letterSpacingMultiplier = letterSpacingMultiplier,
        lineHeightMultiplier = lineHeightMultiplier,
        fontWeightBoost = fontWeightBoost
    )
}

private fun ThemeFontRef.toImportedTypeface(
    context: Context,
    importedFonts: List<ImportedFontAsset>
): Typeface? {
    if (source != ThemeFontSource.IMPORTED) return null
    val uriString = uri ?: importedFonts.firstOrNull { it.id == id }?.uri ?: return null
    return runCatching {
        context.contentResolver.openFileDescriptor(Uri.parse(uriString), "r")?.use { descriptor ->
            Typeface.Builder(descriptor.fileDescriptor)
                .setFontVariationSettings(buildVariationSettingsString(axes))
                .build()
        }
    }.getOrNull()
}

private fun buildVariationSettingsString(axes: List<ThemeFontAxisValue>): String {
    return axes.joinToString(",") { axis ->
        "'${axis.tag.take(4)}' ${axis.value}"
    }
}
