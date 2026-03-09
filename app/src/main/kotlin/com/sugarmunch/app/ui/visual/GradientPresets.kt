package com.sugarmunch.app.ui.visual

import androidx.compose.ui.graphics.Color

object GradientPresets {

    // ── Candy Collection ──────────────────────────────────────────────

    val SunsetCandy = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFFFF6B6B), 0f),
            GradientStop(Color(0xFFFFAB76), 0.4f),
            GradientStop(Color(0xFFFFD93D), 1f)
        ),
        angleDegrees = 135f
    )

    val CottonCandyDream = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFFFFB6C1), 0f),
            GradientStop(Color(0xFFE6B3FF), 0.5f),
            GradientStop(Color(0xFFB5DEFF), 1f)
        ),
        angleDegrees = 120f
    )

    val BerryBlast = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFFE91E63), 0f),
            GradientStop(Color(0xFF9C27B0), 0.5f),
            GradientStop(Color(0xFF673AB7), 1f)
        ),
        angleDegrees = 160f
    )

    val CandyCane = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFFFF1744), 0f),
            GradientStop(Color(0xFFFFFFFF), 0.25f),
            GradientStop(Color(0xFFFF1744), 0.5f),
            GradientStop(Color(0xFFFFFFFF), 0.75f)
        ),
        angleDegrees = 45f
    )

    val BubbleGumPop = GradientSpec(
        type = GradientType.RADIAL,
        stops = listOf(
            GradientStop(Color(0xFFFF8EC4), 0f),
            GradientStop(Color(0xFFFF69B4), 0.5f),
            GradientStop(Color(0xFFFF1493), 1f)
        ),
        centerX = 0.5f,
        centerY = 0.5f,
        radiusScale = 1.2f
    )

    val LollipopSwirl = GradientSpec(
        type = GradientType.SWEEP,
        stops = listOf(
            GradientStop(Color(0xFFFF4081), 0f),
            GradientStop(Color(0xFFFFAB40), 0.33f),
            GradientStop(Color(0xFF69F0AE), 0.66f),
            GradientStop(Color(0xFFFF4081), 1f)
        ),
        centerX = 0.5f,
        centerY = 0.5f
    )

    val JellyBeanJoy = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFFFF6F61), 0f),
            GradientStop(Color(0xFFFFD166), 0.33f),
            GradientStop(Color(0xFF06D6A0), 0.66f),
            GradientStop(Color(0xFF118AB2), 1f)
        ),
        angleDegrees = 90f
    )

    val GummyBearGlow = GradientSpec(
        type = GradientType.RADIAL,
        stops = listOf(
            GradientStop(Color(0xFFFFEB3B), 0f),
            GradientStop(Color(0xFFFF9800), 0.4f),
            GradientStop(Color(0xFFFF5722), 1f)
        ),
        centerX = 0.4f,
        centerY = 0.4f,
        radiusScale = 1.3f
    )

    // ── Nature ────────────────────────────────────────────────────────

    val OceanMint = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFF00B4DB), 0f),
            GradientStop(Color(0xFF0083B0), 0.5f),
            GradientStop(Color(0xFF98FF98), 1f)
        ),
        angleDegrees = 180f
    )

    val TwilightCaramel = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFF2C3E50), 0f),
            GradientStop(Color(0xFFDEB887), 0.5f),
            GradientStop(Color(0xFFFD746C), 1f)
        ),
        angleDegrees = 135f
    )

    val ForestDew = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFF134E5E), 0f),
            GradientStop(Color(0xFF71B280), 0.5f),
            GradientStop(Color(0xFFA8E063), 1f)
        ),
        angleDegrees = 160f
    )

    val SunriseBlush = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFFFF758C), 0f),
            GradientStop(Color(0xFFFF7EB3), 0.5f),
            GradientStop(Color(0xFFFFCF91), 1f)
        ),
        angleDegrees = 45f
    )

    val MidnightSky = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFF0F2027), 0f),
            GradientStop(Color(0xFF203A43), 0.5f),
            GradientStop(Color(0xFF2C5364), 1f)
        ),
        angleDegrees = 180f
    )

    val AuroraBorealis = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFF1A2A6C), 0f),
            GradientStop(Color(0xFF00D2FF), 0.3f),
            GradientStop(Color(0xFF7FFF00), 0.6f),
            GradientStop(Color(0xFFB21F1F), 1f)
        ),
        angleDegrees = 170f
    )

    val CoralReef = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFFFF6E7F), 0f),
            GradientStop(Color(0xFFBFE9FF), 1f)
        ),
        angleDegrees = 120f
    )

    val DesertSand = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFFEDC9AF), 0f),
            GradientStop(Color(0xFFF0E6D3), 0.4f),
            GradientStop(Color(0xFFC19A6B), 1f)
        ),
        angleDegrees = 90f
    )

    // ── Neon ──────────────────────────────────────────────────────────

    val NeonRush = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFFFF00FF), 0f),
            GradientStop(Color(0xFF00FFFF), 0.5f),
            GradientStop(Color(0xFFFF00FF), 1f)
        ),
        angleDegrees = 45f
    )

    val ElectricPurple = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFF7F00FF), 0f),
            GradientStop(Color(0xFFE100FF), 1f)
        ),
        angleDegrees = 135f
    )

    val CyberPink = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFFFF0080), 0f),
            GradientStop(Color(0xFFFF8C00), 0.5f),
            GradientStop(Color(0xFFFFE600), 1f)
        ),
        angleDegrees = 90f
    )

    val LaserLime = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFF00FF87), 0f),
            GradientStop(Color(0xFF60EFFF), 1f)
        ),
        angleDegrees = 120f
    )

    val PlasmaBlue = GradientSpec(
        type = GradientType.RADIAL,
        stops = listOf(
            GradientStop(Color(0xFF00D4FF), 0f),
            GradientStop(Color(0xFF0040FF), 0.5f),
            GradientStop(Color(0xFF000080), 1f)
        ),
        centerX = 0.5f,
        centerY = 0.5f,
        radiusScale = 1.2f
    )

    val NeonSunset = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFFFF0099), 0f),
            GradientStop(Color(0xFFFF6600), 0.5f),
            GradientStop(Color(0xFFFFFF00), 1f)
        ),
        angleDegrees = 160f
    )

    // ── Premium ───────────────────────────────────────────────────────

    val RoseGold = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFFB76E79), 0f),
            GradientStop(Color(0xFFE8C8C0), 0.5f),
            GradientStop(Color(0xFFF5D0C5), 1f)
        ),
        angleDegrees = 135f
    )

    val ChromeSilver = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFFC0C0C0), 0f),
            GradientStop(Color(0xFFE8E8E8), 0.3f),
            GradientStop(Color(0xFF808080), 0.6f),
            GradientStop(Color(0xFFE8E8E8), 1f)
        ),
        angleDegrees = 120f
    )

    val MoltenGold = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFFBF953F), 0f),
            GradientStop(Color(0xFFFCF6BA), 0.3f),
            GradientStop(Color(0xFFB38728), 0.6f),
            GradientStop(Color(0xFFFBF5B7), 1f)
        ),
        angleDegrees = 135f
    )

    val DiamondFrost = GradientSpec(
        type = GradientType.RADIAL,
        stops = listOf(
            GradientStop(Color(0xFFFFFFFF), 0f),
            GradientStop(Color(0xFFD6EAF8), 0.4f),
            GradientStop(Color(0xFFAED6F1), 1f)
        ),
        centerX = 0.5f,
        centerY = 0.3f,
        radiusScale = 1.4f
    )

    val VelvetNight = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFF1A0A2E), 0f),
            GradientStop(Color(0xFF3D1663), 0.5f),
            GradientStop(Color(0xFF6B21A8), 1f)
        ),
        angleDegrees = 170f
    )

    val PlatinumMist = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFFD7D2CC), 0f),
            GradientStop(Color(0xFF304352), 1f)
        ),
        angleDegrees = 150f
    )

    val OnyxFlame = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFF1A1A2E), 0f),
            GradientStop(Color(0xFF16213E), 0.3f),
            GradientStop(Color(0xFFE94560), 0.7f),
            GradientStop(Color(0xFFFF6B6B), 1f)
        ),
        angleDegrees = 180f
    )

    val EmeraldDepth = GradientSpec(
        type = GradientType.LINEAR,
        stops = listOf(
            GradientStop(Color(0xFF0D3B2E), 0f),
            GradientStop(Color(0xFF2ECC71), 0.5f),
            GradientStop(Color(0xFFA9DFBF), 1f)
        ),
        angleDegrees = 160f
    )

    // ── All presets ───────────────────────────────────────────────────

    val allPresets: List<Pair<String, GradientSpec>> = listOf(
        // Candy Collection
        "SunsetCandy" to SunsetCandy,
        "CottonCandyDream" to CottonCandyDream,
        "BerryBlast" to BerryBlast,
        "CandyCane" to CandyCane,
        "BubbleGumPop" to BubbleGumPop,
        "LollipopSwirl" to LollipopSwirl,
        "JellyBeanJoy" to JellyBeanJoy,
        "GummyBearGlow" to GummyBearGlow,
        // Nature
        "OceanMint" to OceanMint,
        "TwilightCaramel" to TwilightCaramel,
        "ForestDew" to ForestDew,
        "SunriseBlush" to SunriseBlush,
        "MidnightSky" to MidnightSky,
        "AuroraBorealis" to AuroraBorealis,
        "CoralReef" to CoralReef,
        "DesertSand" to DesertSand,
        // Neon
        "NeonRush" to NeonRush,
        "ElectricPurple" to ElectricPurple,
        "CyberPink" to CyberPink,
        "LaserLime" to LaserLime,
        "PlasmaBlue" to PlasmaBlue,
        "NeonSunset" to NeonSunset,
        // Premium
        "RoseGold" to RoseGold,
        "ChromeSilver" to ChromeSilver,
        "MoltenGold" to MoltenGold,
        "DiamondFrost" to DiamondFrost,
        "VelvetNight" to VelvetNight,
        "PlatinumMist" to PlatinumMist,
        "OnyxFlame" to OnyxFlame,
        "EmeraldDepth" to EmeraldDepth
    )

    fun getPresetsByCategory(): Map<String, List<Pair<String, GradientSpec>>> = mapOf(
        "Candy Collection" to allPresets.subList(0, 8),
        "Nature" to allPresets.subList(8, 16),
        "Neon" to allPresets.subList(16, 22),
        "Premium" to allPresets.subList(22, 30)
    )
}
