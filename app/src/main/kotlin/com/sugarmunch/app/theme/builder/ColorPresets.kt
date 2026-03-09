package com.sugarmunch.app.theme.builder

import androidx.compose.ui.graphics.Color

/**
 * Color presets for quick selection in the theme builder.
 *
 * Provides curated collections of colors organized by their intended use
 * (primary, secondary, background, surface) to help users quickly create
 * visually harmonious themes.
 */
object ColorPresets {
    val primaryColors = listOf(
        Color(0xFFFF69B4), // Hot Pink
        Color(0xFF00BFFF), // Deep Sky Blue
        Color(0xFF32CD32), // Lime Green
        Color(0xFFFFD700), // Gold
        Color(0xFFFF4500), // Orange Red
        Color(0xFF9370DB), // Medium Purple
        Color(0xFF00CED1), // Dark Turquoise
        Color(0xFFFF1493), // Deep Pink
        Color(0xFF00FF7F), // Spring Green
        Color(0xFFDC143C), // Crimson
        Color(0xFF1E90FF), // Dodger Blue
        Color(0xFFDA70D6), // Orchid
        Color(0xFF20B2AA), // Light Sea Green
        Color(0xFFFF6347), // Tomato
        Color(0xFFBA55D3), // Medium Orchid
        Color(0xFF4169E1), // Royal Blue
        Color(0xFF3CB371), // Medium Sea Green
        Color(0xFFFF8C00), // Dark Orange
        Color(0xFF7B68EE), // Medium Slate Blue
        Color(0xFF00FA9A)  // Medium Spring Green
    ).map { it.value }

    val secondaryColors = listOf(
        Color(0xFF9370DB), // Medium Purple
        Color(0xFF20B2AA), // Light Sea Green
        Color(0xFFFFB6C1), // Light Pink
        Color(0xFF87CEEB), // Sky Blue
        Color(0xFFDDA0DD), // Plum
        Color(0xFF98FB98), // Pale Green
        Color(0xFFFFDAB9), // Peach Puff
        Color(0xFFAFEEEE), // Pale Turquoise
        Color(0xFFE6E6FA), // Lavender
        Color(0xFFFFE4B5), // Moccasin
        Color(0xFFFFFACD), // Lemon Chiffon
        Color(0xFFB0E0E6), // Powder Blue
        Color(0xFF90EE90), // Light Green
        Color(0xFFFFC0CB), // Pink
        Color(0xFF87CEFA), // Light Sky Blue
        Color(0xFFFAFAD2), // Light Goldenrod Yellow
        Color(0xFFD8BFD8), // Thistle
        Color(0xFF7FFFD4), // Aquamarine
        Color(0xFFFFEFD5), // Papaya Whip
        Color(0xFFF0E68C)  // Khaki
    ).map { it.value }

    val backgroundColors = listOf(
        Color(0xFF1A1A2E), // Dark Blue
        Color(0xFF16213E), // Navy
        Color(0xFF0F0F23), // Midnight
        Color(0xFF1C1C3C), // Purple Blue
        Color(0xFF2D1B4E), // Deep Purple
        Color(0xFF1B262C), // Dark Slate
        Color(0xFF0D1B2A), // Dark Navy
        Color(0xFF1B1B2F), // Dark Indigo
        Color(0xFF2C2C54), // Charcoal
        Color(0xFF3D3D3D), // Dark Gray
        Color(0xFF2E2E2E), // Almost Black
        Color(0xFF1E3A5F), // Prussian Blue
        Color(0xFF2F4F4F), // Dark Slate Gray
        Color(0xFF36454F), // Charcoal
        Color(0xFF4B0082), // Indigo
        Color(0xFF2F1B4E), // Dark Violet
        Color(0xFF1C3F1C), // Dark Green
        Color(0xFF3D1C1C), // Dark Red
        Color(0xFF1C3D3D), // Dark Teal
        Color(0xFF3D1C3D)  // Dark Magenta
    ).map { it.value }

    val surfaceColors = listOf(
        Color(0xFF16213E), // Navy
        Color(0xFF1F4068), // Steel Blue
        Color(0xFF2D3561), // Dark Blue Gray
        Color(0xFF3D3D5C), // Blue Gray
        Color(0xFF2E2E4E), // Dark Slate Blue
        Color(0xFF3E3E5E), // Slate Blue
        Color(0xFF4E4E6E), // Light Slate Blue
        Color(0xFF252545), // Dark Purple Blue
        Color(0xFF353555), // Medium Purple Blue
        Color(0xFF454565), // Light Purple Blue
        Color(0xFF1E3A5F), // Prussian Blue
        Color(0xFF2E4A6F), // Steel Blue
        Color(0xFF3E5A7F), // Light Steel Blue
        Color(0xFF1E3A3F), // Dark Teal
        Color(0xFF2E4A4F), // Medium Teal
        Color(0xFF3E5A5F), // Light Teal
        Color(0xFF3A1E3F), // Dark Purple
        Color(0xFF4A2E4F), // Medium Purple
        Color(0xFF5A3E5F), // Light Purple
        Color(0xFF2A2A2A)  // Dark Gray
    ).map { it.value }
}

/**
 * Convert Color to hex string representation.
 */
fun Color.toHex(): String {
    return "#%02X%02X%02X".format(
        (this.red * 255).toInt(),
        (this.green * 255).toInt(),
        (this.blue * 255).toInt()
    )
}

/**
 * Convert hex string to Color.
 *
 * @return The parsed Color, or [Color.White] if parsing fails.
 */
fun String.toColor(): Color {
    return try {
        val hex = this.removePrefix("#")
        Color(
            red = Integer.valueOf(hex.substring(0, 2), 16) / 255f,
            green = Integer.valueOf(hex.substring(2, 4), 16) / 255f,
            blue = Integer.valueOf(hex.substring(4, 6), 16) / 255f
        )
    } catch (e: Exception) {
        Color.White
    }
}

/**
 * Convert a [CustomTheme] to [ThemeBuilderState] for editing.
 */
fun CustomTheme.toBuilderState(): ThemeBuilderState {
    return ThemeBuilderState.fromCustomTheme(this)
}
