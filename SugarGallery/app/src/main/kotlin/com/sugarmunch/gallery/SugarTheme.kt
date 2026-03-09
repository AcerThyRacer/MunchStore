package com.sugarmunch.gallery

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

/**
 * Sugar Theme Model for SugarGallery
 * Each theme defines a complete candy-themed visual experience
 */
data class SugarTheme(
    val id: String,
    val name: String,
    val description: String,
    @ColorRes val primaryColor: Int,
    @ColorRes val accentColor: Int,
    @ColorRes val backgroundColor: Int,
    @ColorRes val surfaceColor: Int,
    @DrawableRes val iconRes: Int,
    val category: ThemeCategory,
    val intensityLevel: Float = 1.0f,
    val hasAnimations: Boolean = true,
    val hasParticles: Boolean = true,
    val candyEffects: List<CandyEffect> = emptyList()
) {
    companion object {
        val ALL_THEMES = listOf(
            // ═════════════════════════════════════════════════════════════
            // COTTON CANDY COLLECTION
            // ═════════════════════════════════════════════════════════════
            SugarTheme(
                id = "cotton_candy",
                name = "Cotton Candy",
                description = "Fluffy pink and blue candy floss dreams",
                primaryColor = R.color.cotton_candy_primary,
                accentColor = R.color.cotton_candy_accent,
                backgroundColor = R.color.cotton_candy_background,
                surfaceColor = R.color.cotton_candy_surface,
                iconRes = R.drawable.ic_theme_cotton_candy,
                category = ThemeCategory.COTTON_CANDY,
                intensityLevel = 1.2f,
                candyEffects = listOf(CandyEffect.SPARKLE, CandyEffect.FLOAT)
            ),
            SugarTheme(
                id = "pink_cotton_candy",
                name = "Pink Cotton Candy",
                description = "Extra sweet pink candy floss",
                primaryColor = R.color.pink_cotton_candy_primary,
                accentColor = R.color.pink_cotton_candy_accent,
                backgroundColor = R.color.pink_cotton_candy_background,
                surfaceColor = R.color.pink_cotton_candy_surface,
                iconRes = R.drawable.ic_theme_cotton_candy,
                category = ThemeCategory.COTTON_CANDY,
                intensityLevel = 1.5f,
                candyEffects = listOf(CandyEffect.SPARKLE, CandyEffect.GLOW)
            ),
            SugarTheme(
                id = "blue_cotton_candy",
                name = "Blue Cotton Candy",
                description = "Cool blue candy clouds",
                primaryColor = R.color.blue_cotton_candy_primary,
                accentColor = R.color.blue_cotton_candy_accent,
                backgroundColor = R.color.blue_cotton_candy_background,
                surfaceColor = R.color.blue_cotton_candy_surface,
                iconRes = R.drawable.ic_theme_cotton_candy,
                category = ThemeCategory.COTTON_CANDY,
                intensityLevel = 1.3f,
                candyEffects = listOf(CandyEffect.FLOAT, CandyEffect.SHIMMER)
            ),

            // ═════════════════════════════════════════════════════════════
            // CHOCOLATE COLLECTION
            // ═════════════════════════════════════════════════════════════
            SugarTheme(
                id = "dark_chocolate",
                name = "Dark Chocolate",
                description = "Rich dark chocolate with gold accents",
                primaryColor = R.color.dark_chocolate_primary,
                accentColor = R.color.dark_chocolate_accent,
                backgroundColor = R.color.dark_chocolate_background,
                surfaceColor = R.color.dark_chocolate_surface,
                iconRes = R.drawable.ic_theme_chocolate,
                category = ThemeCategory.CHOCOLATE,
                intensityLevel = 1.0f,
                candyEffects = listOf(CandyEffect.EMBOSS, CandyEffect.GLOW)
            ),
            SugarTheme(
                id = "milk_chocolate",
                name = "Milk Chocolate",
                description = "Creamy milk chocolate sweetness",
                primaryColor = R.color.milk_chocolate_primary,
                accentColor = R.color.milk_chocolate_accent,
                backgroundColor = R.color.milk_chocolate_background,
                surfaceColor = R.color.milk_chocolate_surface,
                iconRes = R.drawable.ic_theme_chocolate,
                category = ThemeCategory.CHOCOLATE,
                intensityLevel = 1.1f,
                candyEffects = listOf(CandyEffect.SMOOTH, CandyEffect.WARM)
            ),
            SugarTheme(
                id = "white_chocolate",
                name = "White Chocolate",
                description = "Ivory white with pink highlights",
                primaryColor = R.color.white_chocolate_primary,
                accentColor = R.color.white_chocolate_accent,
                backgroundColor = R.color.white_chocolate_background,
                surfaceColor = R.color.white_chocolate_surface,
                iconRes = R.drawable.ic_theme_chocolate,
                category = ThemeCategory.CHOCOLATE,
                intensityLevel = 1.2f,
                candyEffects = listOf(CandyEffect.SPARKLE, CandyEffect.SOFT)
            ),
            SugarTheme(
                id = "ruby_chocolate",
                name = "Ruby Chocolate",
                description = "Rare pink-brown ruby chocolate",
                primaryColor = R.color.ruby_chocolate_primary,
                accentColor = R.color.ruby_chocolate_accent,
                backgroundColor = R.color.ruby_chocolate_background,
                surfaceColor = R.color.ruby_chocolate_surface,
                iconRes = R.drawable.ic_theme_chocolate,
                category = ThemeCategory.CHOCOLATE,
                intensityLevel = 1.4f,
                candyEffects = listOf(CandyEffect.RUBY_GLOW, CandyEffect.SPARKLE)
            ),

            // ═════════════════════════════════════════════════════════════
            // GUMMY COLLECTION
            // ═════════════════════════════════════════════════════════════
            SugarTheme(
                id = "gummy_bear",
                name = "Gummy Bear",
                description = "Translucent multicolor gummy bears",
                primaryColor = R.color.gummy_bear_primary,
                accentColor = R.color.gummy_bear_accent,
                backgroundColor = R.color.gummy_bear_background,
                surfaceColor = R.color.gummy_bear_surface,
                iconRes = R.drawable.ic_theme_gummy,
                category = ThemeCategory.GUMMY,
                intensityLevel = 1.6f,
                candyEffects = listOf(CandyEffect.JELLY, CandyEffect.WOBBLE, CandyEffect.TRANSLUCENT)
            ),
            SugarTheme(
                id = "gummy_worm",
                name = "Gummy Worm",
                description = "Rainbow gradient gummy worms",
                primaryColor = R.color.gummy_worm_primary,
                accentColor = R.color.gummy_worm_accent,
                backgroundColor = R.color.gummy_worm_background,
                surfaceColor = R.color.gummy_worm_surface,
                iconRes = R.drawable.ic_theme_gummy,
                category = ThemeCategory.GUMMY,
                intensityLevel = 1.7f,
                candyEffects = listOf(CandyEffect.RAINBOW, CandyEffect.WOBBLE)
            ),
            SugarTheme(
                id = "sour_gummy",
                name = "Sour Gummy",
                description = "Neon sour candy explosion",
                primaryColor = R.color.sour_gummy_primary,
                accentColor = R.color.sour_gummy_accent,
                backgroundColor = R.color.sour_gummy_background,
                surfaceColor = R.color.sour_gummy_surface,
                iconRes = R.drawable.ic_theme_gummy,
                category = ThemeCategory.GUMMY,
                intensityLevel = 2.0f,
                candyEffects = listOf(CandyEffect.NEON, CandyEffect.ELECTRIC, CandyEffect.SPARKLE)
            ),

            // ═════════════════════════════════════════════════════════════
            // LOLLIPOP COLLECTION
            // ═════════════════════════════════════════════════════════════
            SugarTheme(
                id = "lollipop_swirl",
                name = "Lollipop Swirl",
                description = "Spiral rainbow lollipop",
                primaryColor = R.color.lollipop_swirl_primary,
                accentColor = R.color.lollipop_swirl_accent,
                backgroundColor = R.color.lollipop_swirl_background,
                surfaceColor = R.color.lollipop_swirl_surface,
                iconRes = R.drawable.ic_theme_lollipop,
                category = ThemeCategory.LOLLIPOP,
                intensityLevel = 1.5f,
                candyEffects = listOf(CandyEffect.SWIRL, CandyEffect.SPIN)
            ),
            SugarTheme(
                id = "chupa_chups",
                name = "Chupa Chups",
                description = "Classic red-white striped lollipop",
                primaryColor = R.color.chupa_chups_primary,
                accentColor = R.color.chupa_chups_accent,
                backgroundColor = R.color.chupa_chups_background,
                surfaceColor = R.color.chupa_chups_surface,
                iconRes = R.drawable.ic_theme_lollipop,
                category = ThemeCategory.LOLLIPOP,
                intensityLevel = 1.3f,
                candyEffects = listOf(CandyEffect.STRIPES, CandyEffect.CLASSIC)
            ),
            SugarTheme(
                id = "rainbow_pop",
                name = "Rainbow Pop",
                description = "Bright rainbow popsicle",
                primaryColor = R.color.rainbow_pop_primary,
                accentColor = R.color.rainbow_pop_accent,
                backgroundColor = R.color.rainbow_pop_background,
                surfaceColor = R.color.rainbow_pop_surface,
                iconRes = R.drawable.ic_theme_lollipop,
                category = ThemeCategory.LOLLIPOP,
                intensityLevel = 1.8f,
                candyEffects = listOf(CandyEffect.RAINBOW, CandyEffect.POP, CandyEffect.BRIGHT)
            ),

            // ═════════════════════════════════════════════════════════════
            // CARAMEL COLLECTION
            // ═════════════════════════════════════════════════════════════
            SugarTheme(
                id = "salted_caramel",
                name = "Salted Caramel",
                description = "Golden caramel with sea salt sparkle",
                primaryColor = R.color.salted_caramel_primary,
                accentColor = R.color.salted_caramel_accent,
                backgroundColor = R.color.salted_caramel_background,
                surfaceColor = R.color.salted_caramel_surface,
                iconRes = R.drawable.ic_theme_caramel,
                category = ThemeCategory.CARAMEL,
                intensityLevel = 1.2f,
                candyEffects = listOf(CandyEffect.GOLDEN, CandyEffect.SPARKLE)
            ),
            SugarTheme(
                id = "caramel_delight",
                name = "Caramel Delight",
                description = "Swirled caramel gradient paradise",
                primaryColor = R.color.caramel_delight_primary,
                accentColor = R.color.caramel_delight_accent,
                backgroundColor = R.color.caramel_delight_background,
                surfaceColor = R.color.caramel_delight_surface,
                iconRes = R.drawable.ic_theme_caramel,
                category = ThemeCategory.CARAMEL,
                intensityLevel = 1.4f,
                candyEffects = listOf(CandyEffect.SWIRL, CandyEffect.GOLDEN)
            ),
            SugarTheme(
                id = "butterscotch",
                name = "Butterscotch",
                description = "Golden yellow butterscotch warmth",
                primaryColor = R.color.butterscotch_primary,
                accentColor = R.color.butterscotch_accent,
                backgroundColor = R.color.butterscotch_background,
                surfaceColor = R.color.butterscotch_surface,
                iconRes = R.drawable.ic_theme_caramel,
                category = ThemeCategory.CARAMEL,
                intensityLevel = 1.3f,
                candyEffects = listOf(CandyEffect.WARM, CandyEffect.GLOW)
            ),

            // ═════════════════════════════════════════════════════════════
            // EXTREME SUGAR RUSH COLLECTION
            // ═════════════════════════════════════════════════════════════
            SugarTheme(
                id = "nuclear_sugar",
                name = "Nuclear Sugar",
                description = "Neon green-yellow radioactive overload",
                primaryColor = R.color.nuclear_sugar_primary,
                accentColor = R.color.nuclear_sugar_accent,
                backgroundColor = R.color.nuclear_sugar_background,
                surfaceColor = R.color.nuclear_sugar_surface,
                iconRes = R.drawable.ic_theme_extreme,
                category = ThemeCategory.EXTREME,
                intensityLevel = 2.0f,
                candyEffects = listOf(CandyEffect.NEON, CandyEffect.NUCLEAR, CandyEffect.OVERLOAD)
            ),
            SugarTheme(
                id = "candy_overload",
                name = "Candy Overload",
                description = "Maximum saturation candy explosion",
                primaryColor = R.color.candy_overload_primary,
                accentColor = R.color.candy_overload_accent,
                backgroundColor = R.color.candy_overload_background,
                surfaceColor = R.color.candy_overload_surface,
                iconRes = R.drawable.ic_theme_extreme,
                category = ThemeCategory.EXTREME,
                intensityLevel = 2.0f,
                candyEffects = listOf(CandyEffect.OVERLOAD, CandyEffect.EXPLOSION, CandyEffect.RAINBOW)
            ),
            SugarTheme(
                id = "skittles_storm",
                name = "Skittles Storm",
                description = "Rainbow explosion candy storm",
                primaryColor = R.color.skittles_storm_primary,
                accentColor = R.color.skittles_storm_accent,
                backgroundColor = R.color.skittles_storm_background,
                surfaceColor = R.color.skittles_storm_surface,
                iconRes = R.drawable.ic_theme_extreme,
                category = ThemeCategory.EXTREME,
                intensityLevel = 1.9f,
                candyEffects = listOf(CandyEffect.RAINBOW, CandyEffect.STORM, CandyEffect.PARTICLES)
            ),

            // ═════════════════════════════════════════════════════════════
            // SEASONAL SPECIAL COLLECTION
            // ═════════════════════════════════════════════════════════════
            SugarTheme(
                id = "candy_cane",
                name = "Candy Cane",
                description = "Classic red-white striped candy cane",
                primaryColor = R.color.candy_cane_primary,
                accentColor = R.color.candy_cane_accent,
                backgroundColor = R.color.candy_cane_background,
                surfaceColor = R.color.candy_cane_surface,
                iconRes = R.drawable.ic_theme_candy_cane,
                category = ThemeCategory.SEASONAL,
                intensityLevel = 1.4f,
                candyEffects = listOf(CandyEffect.STRIPES, CandyEffect.FESTIVE)
            ),
            SugarTheme(
                id = "peppermint",
                name = "Peppermint",
                description = "Cool minty peppermint freshness",
                primaryColor = R.color.peppermint_primary,
                accentColor = R.color.peppermint_accent,
                backgroundColor = R.color.peppermint_background,
                surfaceColor = R.color.peppermint_surface,
                iconRes = R.drawable.ic_theme_candy_cane,
                category = ThemeCategory.SEASONAL,
                intensityLevel = 1.2f,
                candyEffects = listOf(CandyEffect.COOL, CandyEffect.FRESH, CandyEffect.SPARKLE)
            ),
            SugarTheme(
                id = "gingerbread",
                name = "Gingerbread",
                description = "Warm gingerbread cookie sweetness",
                primaryColor = R.color.gingerbread_primary,
                accentColor = R.color.gingerbread_accent,
                backgroundColor = R.color.gingerbread_background,
                surfaceColor = R.color.gingerbread_surface,
                iconRes = R.drawable.ic_theme_gingerbread,
                category = ThemeCategory.SEASONAL,
                intensityLevel = 1.1f,
                candyEffects = listOf(CandyEffect.WARM, CandyEffect.COZY)
            )
        )

        fun getById(id: String): SugarTheme? = ALL_THEMES.find { it.id == id }

        fun getByCategory(category: ThemeCategory): List<SugarTheme> =
            ALL_THEMES.filter { it.category == category }
    }
}

/**
 * Theme categories for organization
 */
enum class ThemeCategory {
    COTTON_CANDY,
    CHOCOLATE,
    GUMMY,
    LOLLIPOP,
    CARAMEL,
    EXTREME,
    SEASONAL,
    CUSTOM
}

/**
 * Candy effects that can be applied to themes
 */
enum class CandyEffect {
    SPARKLE,
    GLOW,
    FLOAT,
    SHIMMER,
    EMBOSS,
    SMOOTH,
    WARM,
    SOFT,
    RUBY_GLOW,
    JELLY,
    WOBBLE,
    TRANSLUCENT,
    RAINBOW,
    NEON,
    ELECTRIC,
    SWIRL,
    SPIN,
    STRIPES,
    CLASSIC,
    POP,
    BRIGHT,
    GOLDEN,
    NUCLEAR,
    OVERLOAD,
    EXPLOSION,
    STORM,
    PARTICLES,
    FESTIVE,
    COOL,
    FRESH,
    COZY
}
