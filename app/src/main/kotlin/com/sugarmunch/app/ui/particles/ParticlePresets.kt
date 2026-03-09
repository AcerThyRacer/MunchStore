package com.sugarmunch.app.ui.particles

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

/**
 * Collection of 20+ ready-to-use particle presets for SugarMunch.
 * Each preset is a carefully tuned [ParticleSystemConfig] that produces
 * a distinct visual experience using the candy color palette.
 */
object ParticlePresets {

    /** Candy particles falling gently from the top with a light sway. */
    val CandyRain = ParticleSystemConfig(
        emitterShape = EmitterShape.SCREEN_TOP,
        particleTypes = setOf(ParticleType.CANDY),
        colors = listOf(
            Color(0xFFFF6B9D), Color(0xFFC084FC), Color(0xFF60D394),
            Color(0xFFFFC75F), Color(0xFFFF8FA3)
        ),
        spawnRate = 8f,
        maxParticles = 120,
        lifetime = 4f..6f,
        initialSpeed = 30f..70f,
        initialAngle = 80f..100f,
        size = 8f..14f,
        gravity = 40f,
        wind = 15f,
        turbulence = 20f,
        fadeIn = 0.05f,
        fadeOut = 0.2f,
        rotationSpeed = -90f..90f
    )

    /** Stars exploding outward from the screen center. */
    val StarBurst = ParticleSystemConfig(
        emitterShape = EmitterShape.POINT,
        particleTypes = setOf(ParticleType.STAR),
        colors = listOf(
            Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFEC8B),
            Color(0xFFFFF8DC)
        ),
        spawnRate = 40f,
        maxParticles = 250,
        lifetime = 1.5f..3f,
        initialSpeed = 120f..300f,
        initialAngle = 0f..360f,
        size = 5f..12f,
        gravity = 30f,
        turbulence = 10f,
        fadeIn = 0.02f,
        fadeOut = 0.4f,
        sizeOverLife = listOf(0.2f, 1f, 0.8f, 0f),
        rotationSpeed = -360f..360f
    )

    /** Bubbles rising gently from the bottom of the screen. */
    val BubbleFloat = ParticleSystemConfig(
        emitterShape = EmitterShape.SCREEN_BOTTOM,
        particleTypes = setOf(ParticleType.BUBBLE),
        colors = listOf(
            Color(0xFFB5DEFF), Color(0xFFCAF0F8), Color(0xFFE0F7FF),
            Color(0xFFADE8F4)
        ),
        spawnRate = 6f,
        maxParticles = 80,
        lifetime = 5f..8f,
        initialSpeed = 30f..60f,
        initialAngle = 250f..290f,
        size = 8f..20f,
        gravity = -25f,
        wind = 0f,
        turbulence = 30f,
        fadeIn = 0.1f,
        fadeOut = 0.15f,
        sizeOverLife = listOf(0.5f, 1f, 1.1f, 0.9f),
        rotationSpeed = -20f..20f
    )

    /** Bright neon sparks raining down fast. */
    val NeonSparks = ParticleSystemConfig(
        emitterShape = EmitterShape.SCREEN_TOP,
        particleTypes = setOf(ParticleType.SPARKLE),
        colors = listOf(
            Color(0xFFFF00FF), Color(0xFF00FFFF), Color(0xFFFF3366),
            Color(0xFF39FF14), Color(0xFFFFFF00)
        ),
        spawnRate = 25f,
        maxParticles = 200,
        lifetime = 1f..2.5f,
        initialSpeed = 150f..350f,
        initialAngle = 70f..110f,
        size = 3f..8f,
        gravity = 200f,
        turbulence = 40f,
        fadeIn = 0.01f,
        fadeOut = 0.5f,
        sizeOverLife = listOf(1f, 1.2f, 0.3f),
        rotationSpeed = -500f..500f
    )

    /** Gentle snowflakes drifting down with soft sway. */
    val SugarSnow = ParticleSystemConfig(
        emitterShape = EmitterShape.SCREEN_TOP,
        particleTypes = setOf(ParticleType.SNOWFLAKE),
        colors = listOf(
            Color(0xFFFFFFFF), Color(0xFFE8E8FF), Color(0xFFF0F8FF),
            Color(0xFFB0E0E6)
        ),
        spawnRate = 5f,
        maxParticles = 100,
        lifetime = 6f..10f,
        initialSpeed = 15f..35f,
        initialAngle = 80f..100f,
        size = 6f..14f,
        gravity = 12f,
        wind = 8f,
        turbulence = 25f,
        fadeIn = 0.15f,
        fadeOut = 0.25f,
        rotationSpeed = -60f..60f
    )

    /** Dim sparkles drifting slowly like fireflies at night. */
    val Fireflies = ParticleSystemConfig(
        emitterShape = EmitterShape.SCREEN_EDGES,
        particleTypes = setOf(ParticleType.SPARKLE, ParticleType.CIRCLE),
        colors = listOf(
            Color(0xFFFFFF66), Color(0xFFCCFF66), Color(0xFFFFE066),
            Color(0xFF99FF99)
        ),
        spawnRate = 4f,
        maxParticles = 60,
        lifetime = 4f..8f,
        initialSpeed = 10f..30f,
        initialAngle = 0f..360f,
        size = 3f..6f,
        gravity = -5f,
        turbulence = 40f,
        fadeIn = 0.3f,
        fadeOut = 0.4f,
        sizeOverLife = listOf(0.3f, 1f, 0.6f, 1f, 0.2f),
        rotationSpeed = -30f..30f
    )

    /** Slow horizontal colored waves drifting across the screen. */
    val AuroraWaves = ParticleSystemConfig(
        emitterShape = EmitterShape.SCREEN_EDGES,
        particleTypes = setOf(ParticleType.CIRCLE, ParticleType.CONFETTI_CIRCLE),
        colors = listOf(
            Color(0xFF00CED1), Color(0xFF7B68EE), Color(0xFF48D1CC),
            Color(0xFFDA70D6), Color(0xFF98FB98)
        ),
        spawnRate = 12f,
        maxParticles = 150,
        lifetime = 6f..10f,
        initialSpeed = 20f..50f,
        initialAngle = 170f..190f,
        size = 10f..25f,
        gravity = 0f,
        wind = 30f,
        turbulence = 15f,
        fadeIn = 0.2f,
        fadeOut = 0.3f,
        sizeOverLife = listOf(0.5f, 1f, 1f, 0.5f),
        rotationSpeed = -10f..10f
    )

    /** Large slow bubbles rising like a lava lamp. */
    val LavaLamp = ParticleSystemConfig(
        emitterShape = EmitterShape.SCREEN_BOTTOM,
        particleTypes = setOf(ParticleType.BUBBLE, ParticleType.CIRCLE),
        colors = listOf(
            Color(0xFFFF4500), Color(0xFFFF6347), Color(0xFFFF7F50),
            Color(0xFFFFD700), Color(0xFFFF1493)
        ),
        spawnRate = 2f,
        maxParticles = 30,
        lifetime = 8f..14f,
        initialSpeed = 10f..25f,
        initialAngle = 250f..290f,
        size = 18f..40f,
        gravity = -15f,
        wind = 0f,
        turbulence = 20f,
        fadeIn = 0.2f,
        fadeOut = 0.3f,
        sizeOverLife = listOf(0.3f, 0.8f, 1f, 1f, 0.6f),
        rotationSpeed = -5f..5f
    )

    /** Tiny sparkles in high count drifting slowly—a galaxy of dust. */
    val GalaxyDust = ParticleSystemConfig(
        emitterShape = EmitterShape.SCREEN_EDGES,
        particleTypes = setOf(ParticleType.SPARKLE, ParticleType.CIRCLE),
        colors = listOf(
            Color(0xFFE6E6FA), Color(0xFFFFF0F5), Color(0xFFB0C4DE),
            Color(0xFFDDA0DD), Color(0xFF87CEFA)
        ),
        spawnRate = 20f,
        maxParticles = 300,
        lifetime = 5f..12f,
        initialSpeed = 5f..20f,
        initialAngle = 0f..360f,
        size = 1f..4f,
        gravity = 0f,
        turbulence = 10f,
        fadeIn = 0.3f,
        fadeOut = 0.3f,
        sizeOverLife = listOf(0.5f, 1f, 0.8f, 1f, 0.3f),
        rotationSpeed = -120f..120f
    )

    /** Pink circles gently falling with sway, like cherry blossoms. */
    val CherryBlossoms = ParticleSystemConfig(
        emitterShape = EmitterShape.SCREEN_TOP,
        particleTypes = setOf(ParticleType.CIRCLE, ParticleType.CONFETTI_CIRCLE),
        colors = listOf(
            Color(0xFFFFB7C5), Color(0xFFFF69B4), Color(0xFFFFC0CB),
            Color(0xFFFFDAB9), Color(0xFFFFF0F5)
        ),
        spawnRate = 7f,
        maxParticles = 100,
        lifetime = 5f..9f,
        initialSpeed = 20f..45f,
        initialAngle = 75f..105f,
        size = 5f..10f,
        gravity = 18f,
        wind = 20f,
        turbulence = 35f,
        fadeIn = 0.1f,
        fadeOut = 0.25f,
        sizeOverLife = listOf(0.6f, 1f, 1f, 0.7f),
        rotationSpeed = -200f..200f
    )

    /** Confetti rectangles and circles exploding from center—a celebration! */
    val Celebration = ParticleSystemConfig(
        emitterShape = EmitterShape.POINT,
        particleTypes = setOf(ParticleType.CONFETTI_RECT, ParticleType.CONFETTI_CIRCLE),
        colors = listOf(
            Color(0xFFFF0000), Color(0xFF00FF00), Color(0xFF0000FF),
            Color(0xFFFFFF00), Color(0xFFFF00FF), Color(0xFF00FFFF),
            Color(0xFFFF8C00), Color(0xFFFF1493)
        ),
        spawnRate = 60f,
        maxParticles = 300,
        lifetime = 2f..5f,
        initialSpeed = 100f..280f,
        initialAngle = 0f..360f,
        size = 4f..10f,
        gravity = 80f,
        turbulence = 20f,
        fadeIn = 0.01f,
        fadeOut = 0.35f,
        sizeOverLife = listOf(0.5f, 1f, 1f, 0.6f),
        rotationSpeed = -400f..400f
    )

    /** Gummy bears bouncing around with collision enabled. */
    val GummyBounce = ParticleSystemConfig(
        emitterShape = EmitterShape.SCREEN_TOP,
        particleTypes = setOf(ParticleType.GUMMY_BEAR),
        colors = listOf(
            Color(0xFFFF4040), Color(0xFF40FF40), Color(0xFFFFBF00),
            Color(0xFFFF8C00), Color(0xFFFF69B4), Color(0xFF00CED1)
        ),
        spawnRate = 4f,
        maxParticles = 40,
        lifetime = 8f..14f,
        initialSpeed = 40f..100f,
        initialAngle = 60f..120f,
        size = 12f..20f,
        gravity = 150f,
        wind = 0f,
        turbulence = 10f,
        fadeIn = 0.05f,
        fadeOut = 0.15f,
        collision = true,
        bounciness = 0.7f,
        rotationSpeed = -45f..45f
    )

    /** Lollipops raining down from the top of the screen. */
    val LollipopShower = ParticleSystemConfig(
        emitterShape = EmitterShape.SCREEN_TOP,
        particleTypes = setOf(ParticleType.LOLLIPOP),
        colors = listOf(
            Color(0xFFFF6B9D), Color(0xFFC084FC), Color(0xFF60D394),
            Color(0xFF74C0FC), Color(0xFFFFC75F)
        ),
        spawnRate = 6f,
        maxParticles = 80,
        lifetime = 4f..7f,
        initialSpeed = 40f..80f,
        initialAngle = 75f..105f,
        size = 10f..18f,
        gravity = 55f,
        wind = 10f,
        turbulence = 15f,
        fadeIn = 0.05f,
        fadeOut = 0.2f,
        rotationSpeed = -120f..120f
    )

    /** Lightning bolts—fast, bright, and brief. */
    val Lightning = ParticleSystemConfig(
        emitterShape = EmitterShape.SCREEN_TOP,
        particleTypes = setOf(ParticleType.LIGHTNING),
        colors = listOf(
            Color(0xFFFFFFE0), Color(0xFFFFFF00), Color(0xFFADD8E6),
            Color(0xFFF0FFFF)
        ),
        spawnRate = 15f,
        maxParticles = 80,
        lifetime = 0.3f..0.8f,
        initialSpeed = 300f..500f,
        initialAngle = 75f..105f,
        size = 10f..20f,
        gravity = 400f,
        turbulence = 60f,
        fadeIn = 0.01f,
        fadeOut = 0.6f,
        sizeOverLife = listOf(1f, 0.8f, 0f),
        rotationSpeed = -60f..60f
    )

    /** Hearts rising gently from the bottom. */
    val HeartFloat = ParticleSystemConfig(
        emitterShape = EmitterShape.SCREEN_BOTTOM,
        particleTypes = setOf(ParticleType.HEART),
        colors = listOf(
            Color(0xFFFF6B9D), Color(0xFFFF1493), Color(0xFFFF69B4),
            Color(0xFFFFB6C1), Color(0xFFC71585)
        ),
        spawnRate = 5f,
        maxParticles = 60,
        lifetime = 4f..7f,
        initialSpeed = 30f..65f,
        initialAngle = 250f..290f,
        size = 8f..16f,
        gravity = -20f,
        turbulence = 25f,
        fadeIn = 0.1f,
        fadeOut = 0.3f,
        sizeOverLife = listOf(0.4f, 1f, 1f, 0.7f),
        rotationSpeed = -40f..40f
    )

    /** Slow diamond-shaped sparkles drifting across the screen. */
    val DiamondSparkle = ParticleSystemConfig(
        emitterShape = EmitterShape.SCREEN_EDGES,
        particleTypes = setOf(ParticleType.SPARKLE),
        colors = listOf(
            Color(0xFFB9F2FF), Color(0xFFE0FFFF), Color(0xFFAFEEEE),
            Color(0xFFFFFFFF), Color(0xFFF0F8FF)
        ),
        spawnRate = 4f,
        maxParticles = 50,
        lifetime = 5f..9f,
        initialSpeed = 10f..25f,
        initialAngle = 0f..360f,
        size = 6f..14f,
        gravity = 0f,
        turbulence = 15f,
        fadeIn = 0.25f,
        fadeOut = 0.35f,
        sizeOverLife = listOf(0.3f, 1f, 0.5f, 1f, 0.2f),
        rotationSpeed = -20f..20f
    )

    /** Thin fast circles falling like drizzling rain. */
    val RainDrizzle = ParticleSystemConfig(
        emitterShape = EmitterShape.SCREEN_TOP,
        particleTypes = setOf(ParticleType.CIRCLE),
        colors = listOf(
            Color(0xFF87CEEB), Color(0xFF4682B4), Color(0xFFB0C4DE),
            Color(0xFF6495ED)
        ),
        spawnRate = 30f,
        maxParticles = 250,
        lifetime = 1f..2f,
        initialSpeed = 250f..400f,
        initialAngle = 85f..95f,
        size = 1.5f..3f,
        gravity = 300f,
        wind = 15f,
        turbulence = 5f,
        fadeIn = 0.01f,
        fadeOut = 0.1f,
        rotationSpeed = 0f..0f
    )

    /** Tiny multi-color particles drifting slowly—cosmic dust. */
    val CosmicDust = ParticleSystemConfig(
        emitterShape = EmitterShape.SCREEN_EDGES,
        particleTypes = setOf(ParticleType.CIRCLE, ParticleType.SPARKLE),
        colors = listOf(
            Color(0xFF9B59B6), Color(0xFF3498DB), Color(0xFF1ABC9C),
            Color(0xFFE74C3C), Color(0xFFF39C12), Color(0xFFE91E63)
        ),
        spawnRate = 15f,
        maxParticles = 200,
        lifetime = 6f..12f,
        initialSpeed = 5f..15f,
        initialAngle = 0f..360f,
        size = 1f..3.5f,
        gravity = 0f,
        turbulence = 8f,
        fadeIn = 0.25f,
        fadeOut = 0.25f,
        sizeOverLife = listOf(0.5f, 1f, 1f, 0.5f),
        rotationSpeed = -60f..60f
    )

    /** Fast tiny circles shooting in all directions from the bottom—pop rocks! */
    val PopRocks = ParticleSystemConfig(
        emitterShape = EmitterShape.SCREEN_BOTTOM,
        particleTypes = setOf(ParticleType.CIRCLE, ParticleType.CONFETTI_CIRCLE),
        colors = listOf(
            Color(0xFFFF4040), Color(0xFFFF8C00), Color(0xFFFFD700),
            Color(0xFF7FFF00), Color(0xFF00BFFF), Color(0xFFBA55D3)
        ),
        spawnRate = 35f,
        maxParticles = 250,
        lifetime = 0.8f..2f,
        initialSpeed = 200f..400f,
        initialAngle = 220f..320f,
        size = 2f..5f,
        gravity = 180f,
        turbulence = 50f,
        fadeIn = 0.01f,
        fadeOut = 0.4f,
        sizeOverLife = listOf(1f, 0.8f, 0f),
        rotationSpeed = -300f..300f
    )

    /** All candy-themed particle types exploding outward from center. */
    val CandyExplosion = ParticleSystemConfig(
        emitterShape = EmitterShape.POINT,
        particleTypes = setOf(
            ParticleType.CANDY, ParticleType.LOLLIPOP, ParticleType.GUMMY_BEAR,
            ParticleType.STAR, ParticleType.HEART, ParticleType.CONFETTI_RECT,
            ParticleType.CONFETTI_CIRCLE
        ),
        colors = listOf(
            Color(0xFFFF6B9D), Color(0xFFC084FC), Color(0xFF60D394),
            Color(0xFFFFC75F), Color(0xFF74C0FC), Color(0xFFFF8FA3),
            Color(0xFFFFD700), Color(0xFFFF4500)
        ),
        spawnRate = 50f,
        maxParticles = 300,
        lifetime = 2f..5f,
        initialSpeed = 80f..250f,
        initialAngle = 0f..360f,
        size = 6f..16f,
        gravity = 60f,
        turbulence = 25f,
        fadeIn = 0.02f,
        fadeOut = 0.3f,
        sizeOverLife = listOf(0.3f, 1f, 1f, 0.5f),
        rotationSpeed = -300f..300f
    )

    /** All available presets as name-config pairs. */
    val allPresets: List<Pair<String, ParticleSystemConfig>> = listOf(
        "Candy Rain" to CandyRain,
        "Star Burst" to StarBurst,
        "Bubble Float" to BubbleFloat,
        "Neon Sparks" to NeonSparks,
        "Sugar Snow" to SugarSnow,
        "Fireflies" to Fireflies,
        "Aurora Waves" to AuroraWaves,
        "Lava Lamp" to LavaLamp,
        "Galaxy Dust" to GalaxyDust,
        "Cherry Blossoms" to CherryBlossoms,
        "Celebration" to Celebration,
        "Gummy Bounce" to GummyBounce,
        "Lollipop Shower" to LollipopShower,
        "Lightning" to Lightning,
        "Heart Float" to HeartFloat,
        "Diamond Sparkle" to DiamondSparkle,
        "Rain Drizzle" to RainDrizzle,
        "Cosmic Dust" to CosmicDust,
        "Pop Rocks" to PopRocks,
        "Candy Explosion" to CandyExplosion
    )

    /**
     * Groups presets by mood for easy selection in UI.
     * Moods: "Calm", "Energetic", "Festive", "Nature", "Cosmic"
     */
    fun getPresetsByMood(): Map<String, List<Pair<String, ParticleSystemConfig>>> = mapOf(
        "Calm" to listOf(
            "Bubble Float" to BubbleFloat,
            "Sugar Snow" to SugarSnow,
            "Fireflies" to Fireflies,
            "Cherry Blossoms" to CherryBlossoms,
            "Diamond Sparkle" to DiamondSparkle,
            "Heart Float" to HeartFloat
        ),
        "Energetic" to listOf(
            "Neon Sparks" to NeonSparks,
            "Pop Rocks" to PopRocks,
            "Lightning" to Lightning,
            "Star Burst" to StarBurst,
            "Gummy Bounce" to GummyBounce
        ),
        "Festive" to listOf(
            "Celebration" to Celebration,
            "Candy Explosion" to CandyExplosion,
            "Candy Rain" to CandyRain,
            "Lollipop Shower" to LollipopShower,
            "Confetti Burst" to Celebration
        ),
        "Nature" to listOf(
            "Cherry Blossoms" to CherryBlossoms,
            "Rain Drizzle" to RainDrizzle,
            "Bubble Float" to BubbleFloat,
            "Sugar Snow" to SugarSnow,
            "Lava Lamp" to LavaLamp
        ),
        "Cosmic" to listOf(
            "Galaxy Dust" to GalaxyDust,
            "Cosmic Dust" to CosmicDust,
            "Aurora Waves" to AuroraWaves,
            "Star Burst" to StarBurst,
            "Diamond Sparkle" to DiamondSparkle
        )
    )
}
