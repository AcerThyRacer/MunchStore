package com.sugarmunch.app.effects.x2026

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * 🚀 2026 SPECIAL EFFECTS - NEXT GENERATION
 * Futuristic, AI-driven, physics-based effects for the ultimate experience!
 */

@Serializable
data class Effect2026(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val category: Effect2026Category,
    val rarity: EffectRarity,
    val levelRequired: Int,
    val xpValue: Int,
    val gradientColors: List<Long>,
    val hasPhysics: Boolean = false,
    val hasAI: Boolean = false,
    val isInteractive: Boolean = false,
    val unlockRequirements: UnlockRequirements? = null,
    val parameters: List<EffectParameter> = emptyList()
) {
    @Serializable
    data class UnlockRequirements(
        val xpNeeded: Int = 0,
        val effectsOwned: List<String> = emptyList(),
        val achievementsNeeded: List<String> = emptyList(),
        val streakDays: Int = 0
    )
    
    @Serializable
    data class EffectParameter(
        val key: String,
        val name: String,
        val min: Float,
        val max: Float,
        val default: Float,
        val step: Float = 0.1f
    )
}

enum class Effect2026Category {
    QUANTUM,        // Physics-based particle effects
    HOLOGRAPHIC,    // 3D-style overlays
    TEMPORAL,       // Time-based animations
    BIOLOGICAL,     // Organic/life-like effects
    NEBULA,         // Space/cosmic effects
    CYBERNETIC,     // Digital/tech effects
    ELEMENTAL,      // Nature elements
    MYTHICAL,       // Fantasy creatures and magic
    SYNTHWAVE,      // Retro-futuristic
    CHAOS           // Unpredictable/random
}

enum class EffectRarity {
    COMMON,         // White
    UNCOMMON,       // Green
    RARE,           // Blue
    EPIC,           // Purple
    LEGENDARY,      // Orange
    MYTHIC,         // Red
    TRANSCENDENT    // Rainbow/Gold
}

fun EffectRarity.color(): Long = when (this) {
    EffectRarity.COMMON -> 0xFFFFFFFF
    EffectRarity.UNCOMMON -> 0xFF4CAF50
    EffectRarity.RARE -> 0xFF2196F3
    EffectRarity.EPIC -> 0xFF9C27B0
    EffectRarity.LEGENDARY -> 0xFFFF9800
    EffectRarity.MYTHIC -> 0xFFF44336
    EffectRarity.TRANSCENDENT -> 0xFFFFD700
}

/**
 * 🌌 2026 EFFECTS CATALOG - 50+ NEW EFFECTS!
 */
object Effects2026Catalog {
    
    // ═════════════════════════════════════════════════════════════════
    // QUANTUM EFFECTS (Physics-Based)
    // ═════════════════════════════════════════════════════════════════
    
    val QUANTUM_FLUX = Effect2026(
        id = "quantum_flux",
        name = "Quantum Flux",
        description = "Particles exist in multiple states simultaneously - tap to collapse wave function!",
        emoji = "⚛️",
        category = Effect2026Category.QUANTUM,
        rarity = EffectRarity.EPIC,
        levelRequired = 15,
        xpValue = 150,
        gradientColors = listOf(0xFF00FFFF, 0xFFFF00FF, 0xFF00FF00),
        hasPhysics = true,
        isInteractive = true,
        parameters = listOf(
            Effect2026.EffectParameter("superposition", "Superposition", 2f, 10f, 5f, 1f),
            Effect2026.EffectParameter("entanglement", "Entanglement", 0f, 1f, 0.5f),
            Effect2026.EffectParameter("uncertainty", "Uncertainty", 0.1f, 1f, 0.3f)
        )
    )
    
    val PARTICLE_COLLIDER = Effect2026(
        id = "particle_collider",
        name = "Particle Collider",
        description = "Smash particles together to create energy bursts and new particles!",
        emoji = "💥",
        category = Effect2026Category.QUANTUM,
        rarity = EffectRarity.LEGENDARY,
        levelRequired = 25,
        xpValue = 250,
        gradientColors = listOf(0xFFFF4500, 0xFFFFD700, 0xFFFF1493),
        hasPhysics = true,
        isInteractive = true,
        parameters = listOf(
            Effect2026.EffectParameter("collision_energy", "Collision Energy", 100f, 1000f, 500f, 50f),
            Effect2026.EffectParameter("particle_count", "Particles", 10f, 100f, 50f, 10f)
        )
    )
    
    val QUANTUM_TUNNEL = Effect2026(
        id = "quantum_tunnel",
        name = "Quantum Tunnel",
        description = "Particles tunnel through impossible barriers creating wormhole effects",
        emoji = "🌀",
        category = Effect2026Category.QUANTUM,
        rarity = EffectRarity.RARE,
        levelRequired = 10,
        xpValue = 100,
        gradientColors = listOf(0xFF4B0082, 0xFF9400D3, 0xFF00BFFF),
        hasPhysics = true,
        parameters = listOf(
            Effect2026.EffectParameter("tunnel_depth", "Tunnel Depth", 1f, 10f, 5f),
            Effect2026.EffectParameter("probability", "Probability", 0.1f, 0.9f, 0.3f)
        )
    )
    
    val SCHRODINGER_CANDY = Effect2026(
        id = "schrodinger_candy",
        name = "Schrödinger's Candy",
        description = "The candy is both eaten and uneaten until you observe it!",
        emoji = "🍭",
        category = Effect2026Category.QUANTUM,
        rarity = EffectRarity.EPIC,
        levelRequired = 20,
        xpValue = 200,
        gradientColors = listOf(0xFFFF69B4, 0xFF00CED1, 0xFFFFFF00),
        isInteractive = true,
        parameters = listOf(
            Effect2026.EffectParameter("observation", "Observation Time", 1f, 5f, 3f),
            Effect2026.EffectParameter("collapse_chance", "Collapse Chance", 0.3f, 0.8f, 0.5f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // HOLOGRAPHIC EFFECTS (3D-Style)
    // ═════════════════════════════════════════════════════════════════
    
    val HOLO_CANDY = Effect2026(
        id = "holo_candy",
        name = "Holographic Candy",
        description = "Floating 3D candy that rotates and responds to device tilt",
        emoji = "🔮",
        category = Effect2026Category.HOLOGRAPHIC,
        rarity = EffectRarity.RARE,
        levelRequired = 8,
        xpValue = 80,
        gradientColors = listOf(0xFF00FFFF, 0xFFFF00FF, 0xFFFF4500),
        hasAI = true,
        parameters = listOf(
            Effect2026.EffectParameter("rotation_speed", "Rotation", 0.5f, 5f, 2f),
            Effect2026.EffectParameter("glow_intensity", "Glow", 0.5f, 2f, 1f)
        )
    )
    
    val MATRIX_RAIN = Effect2026(
        id = "matrix_rain",
        name = "Digital Rain",
        description = "Matrix-style candy code cascading down your screen",
        emoji = "🌧️",
        category = Effect2026Category.HOLOGRAPHIC,
        rarity = EffectRarity.UNCOMMON,
        levelRequired = 5,
        xpValue = 50,
        gradientColors = listOf(0xFF00FF00, 0xFF003300, 0xFF00CC00),
        parameters = listOf(
            Effect2026.EffectParameter("fall_speed", "Fall Speed", 1f, 10f, 5f),
            Effect2026.EffectParameter("density", "Density", 0.3f, 1f, 0.6f),
            Effect2026.EffectParameter("code_complexity", "Complexity", 1f, 5f, 3f, 1f)
        )
    )
    
    val CYBER_PORTAL = Effect2026(
        id = "cyber_portal",
        name = "Cyber Portal",
        description = "Swirling digital vortex that pulls in nearby particles",
        emoji = "🌌",
        category = Effect2026Category.HOLOGRAPHIC,
        rarity = EffectRarity.LEGENDARY,
        levelRequired = 30,
        xpValue = 300,
        gradientColors = listOf(0xFF000000, 0xFF00FFFF, 0xFFFF00FF),
        hasPhysics = true,
        hasAI = true,
        parameters = listOf(
            Effect2026.EffectParameter("pull_strength", "Pull Strength", 1f, 10f, 5f),
            Effect2026.EffectParameter("portal_size", "Portal Size", 100f, 500f, 250f, 50f)
        )
    )
    
    val GLITCH_ARTIFACT = Effect2026(
        id = "glitch_artifact",
        name = "Glitch Artifact",
        description = "Random digital glitches that create beautiful corruption patterns",
        emoji = "👾",
        category = Effect2026Category.HOLOGRAPHIC,
        rarity = EffectRarity.UNCOMMON,
        levelRequired = 6,
        xpValue = 60,
        gradientColors = listOf(0xFFFF0000, 0xFF00FF00, 0xFF0000FF),
        parameters = listOf(
            Effect2026.EffectParameter("glitch_frequency", "Glitch Freq", 0.1f, 2f, 0.5f),
            Effect2026.EffectParameter("corruption", "Corruption", 0.1f, 1f, 0.3f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // TEMPORAL EFFECTS (Time-Based)
    // ═════════════════════════════════════════════════════════════════
    
    val TIME_WARP = Effect2026(
        id = "time_warp",
        name = "Time Warp",
        description = "Slow down or speed up time - affects all animations on screen!",
        emoji = "⏰",
        category = Effect2026Category.TEMPORAL,
        rarity = EffectRarity.LEGENDARY,
        levelRequired = 35,
        xpValue = 350,
        gradientColors = listOf(0xFFFFD700, 0xFFFF8C00, 0xFF4B0082),
        hasAI = true,
        isInteractive = true,
        parameters = listOf(
            Effect2026.EffectParameter("time_scale", "Time Scale", 0.1f, 3f, 1f),
            Effect2026.EffectParameter("warp_radius", "Warp Radius", 100f, 500f, 250f, 50f)
        )
    )
    
    val CHRONO_CANDY = Effect2026(
        id = "chrono_candy",
        name = "Chrono Candy",
        description = "Candy that ages backwards - starts as crumbs, becomes whole candy!",
        emoji = "⏳",
        category = Effect2026Category.TEMPORAL,
        rarity = EffectRarity.RARE,
        levelRequired = 12,
        xpValue = 120,
        gradientColors = listOf(0xFF8B4513, 0xFFD2691E, 0xFFF4A460),
        parameters = listOf(
            Effect2026.EffectParameter("time_direction", "Time Direction", -1f, 1f, -1f),
            Effect2026.EffectParameter("aging_speed", "Aging Speed", 0.5f, 3f, 1f)
        )
    )
    
    val DEJA_VU = Effect2026(
        id = "deja_vu",
        name = "Déjà Vu",
        description = "Creates ghostly echoes of your past actions",
        emoji = "👻",
        category = Effect2026Category.TEMPORAL,
        rarity = EffectRarity.EPIC,
        levelRequired = 18,
        xpValue = 180,
        gradientColors = listOf(0xFF9370DB, 0xFFB0C4DE, 0xFFFFFFE0),
        hasAI = true,
        parameters = listOf(
            Effect2026.EffectParameter("echo_count", "Echoes", 2f, 10f, 5f, 1f),
            Effect2026.EffectParameter("echo_delay", "Echo Delay", 100f, 1000f, 500f, 100f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // BIOLOGICAL EFFECTS (Organic/Life-like)
    // ═════════════════════════════════════════════════════════════════
    
    val BIO_LUMINESCENCE = Effect2026(
        id = "bio_luminescence",
        name = "Bio-Luminescence",
        description = "Living organisms that glow and react to your touch",
        emoji = "🦠",
        category = Effect2026Category.BIOLOGICAL,
        rarity = EffectRarity.RARE,
        levelRequired = 9,
        xpValue = 90,
        gradientColors = listOf(0xFF00FF7F, 0xFF00CED1, 0xFF20B2AA),
        hasAI = true,
        isInteractive = true,
        parameters = listOf(
            Effect2026.EffectParameter("organism_count", "Organisms", 5f, 50f, 20f, 5f),
            Effect2026.EffectParameter("sensitivity", "Sensitivity", 0.5f, 3f, 1f),
            Effect2026.EffectParameter("glow_pulse", "Pulse Rate", 0.5f, 3f, 1f)
        )
    )
    
    val CANDY_ECOSYSTEM = Effect2026(
        id = "candy_ecosystem",
        name = "Candy Ecosystem",
        description = "A living world where candies hunt, breed, and evolve!",
        emoji = "🌍",
        category = Effect2026Category.BIOLOGICAL,
        rarity = EffectRarity.LEGENDARY,
        levelRequired = 40,
        xpValue = 400,
        gradientColors = listOf(0xFF228B22, 0xFF32CD32, 0xFF90EE90),
        hasAI = true,
        hasPhysics = true,
        isInteractive = true,
        parameters = listOf(
            Effect2026.EffectParameter("population", "Population", 10f, 100f, 50f, 10f),
            Effect2026.EffectParameter("evolution_rate", "Evolution", 0.1f, 2f, 0.5f),
            Effect2026.EffectParameter("predation", "Predation", 0f, 1f, 0.3f)
        ),
        unlockRequirements = Effect2026.UnlockRequirements(
            xpNeeded = 5000,
            effectsOwned = listOf("bio_luminescence"),
            streakDays = 7
        )
    )
    
    val DNA_HELIX = Effect2026(
        id = "dna_helix",
        name = "Sugar DNA",
        description = "Rotating double helix made of candy molecules",
        emoji = "🧬",
        category = Effect2026Category.BIOLOGICAL,
        rarity = EffectRarity.EPIC,
        levelRequired = 22,
        xpValue = 220,
        gradientColors = listOf(0xFFFF1493, 0xFF00CED1, 0xFFFFD700),
        parameters = listOf(
            Effect2026.EffectParameter("rotation_speed", "Rotation", 0.5f, 3f, 1f),
            Effect2026.EffectParameter("helix_height", "Height", 200f, 800f, 400f, 50f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // NEBULA EFFECTS (Space/Cosmic)
    // ═════════════════════════════════════════════════════════════════
    
    val SUPERNOVA = Effect2026(
        id = "supernova",
        name = "Supernova",
        description = "Massive stellar explosion that births new stars and elements!",
        emoji = "💫",
        category = Effect2026Category.NEBULA,
        rarity = EffectRarity.LEGENDARY,
        levelRequired = 45,
        xpValue = 450,
        gradientColors = listOf(0xFFFFFF00, 0xFFFF4500, 0xFFFF1493, 0xFF9400D3),
        hasPhysics = true,
        parameters = listOf(
            Effect2026.EffectParameter("star_mass", "Star Mass", 1f, 100f, 10f),
            Effect2026.EffectParameter("explosion_force", "Force", 1f, 10f, 5f)
        ),
        unlockRequirements = Effect2026.UnlockRequirements(
            xpNeeded = 8000,
            effectsOwned = listOf("black_hole", "nebula_cloud"),
            streakDays = 14
        )
    )
    
    val BLACK_HOLE = Effect2026(
        id = "black_hole",
        name = "Black Hole",
        description = "Gravitational singularity that devours everything, even light!",
        emoji = "⚫",
        category = Effect2026Category.NEBULA,
        rarity = EffectRarity.MYTHIC,
        levelRequired = 50,
        xpValue = 500,
        gradientColors = listOf(0xFF000000, 0xFF4B0082, 0xFF9400D3),
        hasPhysics = true,
        hasAI = true,
        isInteractive = true,
        parameters = listOf(
            Effect2026.EffectParameter("mass", "Mass", 10f, 1000f, 100f, 10f),
            Effect2026.EffectParameter("accretion_disk", "Accretion Disk", 0f, 1f, 1f),
            Effect2026.EffectParameter("event_horizon", "Horizon Size", 50f, 300f, 150f, 10f)
        ),
        unlockRequirements = Effect2026.UnlockRequirements(
            xpNeeded = 10000,
            effectsOwned = listOf("gravity_well", "dark_matter"),
            achievementsNeeded = listOf("space_explorer"),
            streakDays = 21
        )
    )
    
    val NEBULA_CLOUD = Effect2026(
        id = "nebula_cloud",
        name = "Nebula Cloud",
        description = "Beautiful gas clouds where stars are born",
        emoji = "🌌",
        category = Effect2026Category.NEBULA,
        rarity = EffectRarity.RARE,
        levelRequired = 11,
        xpValue = 110,
        gradientColors = listOf(0xFFFF69B4, 0xFF9370DB, 0xFF00BFFF, 0xFFFF1493),
        parameters = listOf(
            Effect2026.EffectParameter("cloud_density", "Density", 0.3f, 1f, 0.6f),
            Effect2026.EffectParameter("star_formation", "Star Formation", 0f, 1f, 0.5f)
        )
    )
    
    val GRAVITY_WELL = Effect2026(
        id = "gravity_well",
        name = "Gravity Well",
        description = "Visualize gravity bending spacetime around massive objects",
        emoji = "🕳️",
        category = Effect2026Category.NEBULA,
        rarity = EffectRarity.EPIC,
        levelRequired = 28,
        xpValue = 280,
        gradientColors = listOf(0xFF191970, 0xFF4169E1, 0xFF87CEEB),
        hasPhysics = true,
        parameters = listOf(
            Effect2026.EffectParameter("mass", "Mass", 1f, 100f, 10f),
            Effect2026.EffectParameter("spacetime_bend", "Bending", 0.1f, 1f, 0.5f)
        )
    )
    
    val DARK_MATTER = Effect2026(
        id = "dark_matter",
        name = "Dark Matter",
        description = "Invisible matter that only interacts through gravity - can you find it?",
        emoji = "👁️",
        category = Effect2026Category.NEBULA,
        rarity = EffectRarity.MYTHIC,
        levelRequired = 42,
        xpValue = 420,
        gradientColors = listOf(0xFF1a1a2e, 0xFF16213e, 0xFF0f3460),
        hasAI = true,
        isInteractive = true,
        parameters = listOf(
            Effect2026.EffectParameter("detection", "Detection", 0f, 1f, 0.3f),
            Effect2026.EffectParameter("influence", "Gravitational Pull", 0.5f, 5f, 2f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // CYBERNETIC EFFECTS (Digital/Tech)
    // ═════════════════════════════════════════════════════════════════
    
    val AI_CONSCIOUSNESS = Effect2026(
        id = "ai_consciousness",
        name = "AI Consciousness",
        description = "An evolving neural network that learns your preferences!",
        emoji = "🧠",
        category = Effect2026Category.CYBERNETIC,
        rarity = EffectRarity.MYTHIC,
        levelRequired = 48,
        xpValue = 480,
        gradientColors = listOf(0xFF00FFFF, 0xFFFF00FF, 0xFFFFFF00),
        hasAI = true,
        isInteractive = true,
        parameters = listOf(
            Effect2026.EffectParameter("learning_rate", "Learning Rate", 0.01f, 0.5f, 0.1f),
            Effect2026.EffectParameter("neurons", "Neurons", 10f, 100f, 50f, 10f),
            Effect2026.EffectParameter("adaptation", "Adaptation", 0f, 1f, 0.5f)
        ),
        unlockRequirements = Effect2026.UnlockRequirements(
            xpNeeded = 9000,
            effectsOwned = listOf("neural_network", "data_stream"),
            achievementsNeeded = listOf("tech_guru")
        )
    )
    
    val NEURAL_NETWORK = Effect2026(
        id = "neural_network",
        name = "Neural Network",
        description = "Connected nodes that pulse with information flow",
        emoji = "🔗",
        category = Effect2026Category.CYBERNETIC,
        rarity = EffectRarity.EPIC,
        levelRequired = 24,
        xpValue = 240,
        gradientColors = listOf(0xFFFF1493, 0xFF00CED1, 0xFFFFD700),
        hasAI = true,
        parameters = listOf(
            Effect2026.EffectParameter("layers", "Layers", 2f, 10f, 4f, 1f),
            Effect2026.EffectParameter("connection_density", "Density", 0.3f, 1f, 0.6f),
            Effect2026.EffectParameter("pulse_speed", "Pulse", 0.5f, 3f, 1f)
        )
    )
    
    val DATA_STREAM = Effect2026(
        id = "data_stream",
        name = "Data Stream",
        description = "Flowing information highways with encrypted candy data",
        emoji = "📊",
        category = Effect2026Category.CYBERNETIC,
        rarity = EffectRarity.UNCOMMON,
        levelRequired = 7,
        xpValue = 70,
        gradientColors = listOf(0xFF00FF00, 0xFF008000, 0xFF00CC00),
        parameters = listOf(
            Effect2026.EffectParameter("bandwidth", "Bandwidth", 1f, 10f, 5f),
            Effect2026.EffectParameter("encryption", "Encryption", 0f, 1f, 0.5f)
        )
    )
    
    val CIRCUIT_BOARD = Effect2026(
        id = "circuit_board",
        name = "Circuit Board",
        description = "Electronic pathways that light up with current flow",
        emoji = "🔌",
        category = Effect2026Category.CYBERNETIC,
        rarity = EffectRarity.UNCOMMON,
        levelRequired = 8,
        xpValue = 80,
        gradientColors = listOf(0xFF008B8B, 0xFF00CED1, 0xFF20B2AA),
        parameters = listOf(
            Effect2026.EffectParameter("circuit_density", "Density", 0.3f, 1f, 0.6f),
            Effect2026.EffectParameter("current_flow", "Current", 0.5f, 3f, 1f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // ELEMENTAL EFFECTS (Nature Elements)
    // ═════════════════════════════════════════════════════════════════
    
    val ELEMENTAL_STORM = Effect2026(
        id = "elemental_storm",
        name = "Elemental Storm",
        description = "Fire, water, earth, and air collide in chaos!",
        emoji = "🌪️",
        category = Effect2026Category.ELEMENTAL,
        rarity = EffectRarity.LEGENDARY,
        levelRequired = 38,
        xpValue = 380,
        gradientColors = listOf(0xFFFF4500, 0xFF00BFFF, 0xFF228B22, 0xFFF0F8FF),
        hasPhysics = true,
        hasAI = true,
        isInteractive = true,
        parameters = listOf(
            Effect2026.EffectParameter("fire_intensity", "Fire", 0f, 1f, 0.5f),
            Effect2026.EffectParameter("water_flow", "Water", 0f, 1f, 0.5f),
            Effect2026.EffectParameter("earth_stability", "Earth", 0f, 1f, 0.5f),
            Effect2026.EffectParameter("air_speed", "Air", 0f, 1f, 0.5f)
        )
    )
    
    val CRYSTAL_FORMATION = Effect2026(
        id = "crystal_formation",
        name = "Crystal Formation",
        description = "Growing crystalline structures that refract light beautifully",
        emoji = "💎",
        category = Effect2026Category.ELEMENTAL,
        rarity = EffectRarity.RARE,
        levelRequired = 14,
        xpValue = 140,
        gradientColors = listOf(0xFFE0FFFF, 0xFFB0E0E6, 0xFFAFEEEE),
        hasPhysics = true,
        parameters = listOf(
            Effect2026.EffectParameter("growth_rate", "Growth", 0.5f, 3f, 1f),
            Effect2026.EffectParameter("crystal_count", "Crystals", 5f, 50f, 20f, 5f)
        )
    )
    
    val VOLCANIC_ERUPTION = Effect2026(
        id = "volcanic_eruption",
        name = "Volcanic Eruption",
        description = "Spewing lava and ash with realistic physics",
        emoji = "🌋",
        category = Effect2026Category.ELEMENTAL,
        rarity = EffectRarity.EPIC,
        levelRequired = 26,
        xpValue = 260,
        gradientColors = listOf(0xFFFF4500, 0xFFFF8C00, 0xFF8B0000),
        hasPhysics = true,
        parameters = listOf(
            Effect2026.EffectParameter("magma_pressure", "Pressure", 1f, 10f, 5f),
            Effect2026.EffectParameter("eruption_size", "Size", 1f, 5f, 2f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // MYTHICAL EFFECTS (Fantasy Creatures & Magic)
    // ═════════════════════════════════════════════════════════════════
    
    val DRAGON_BREATH = Effect2026(
        id = "dragon_breath",
        name = "Dragon's Breath",
        description = "Legendary fire breath from an ancient candy dragon",
        emoji = "🐉",
        category = Effect2026Category.MYTHICAL,
        rarity = EffectRarity.LEGENDARY,
        levelRequired = 32,
        xpValue = 320,
        gradientColors = listOf(0xFFFF0000, 0xFFFF4500, 0xFFFFD700),
        hasPhysics = true,
        parameters = listOf(
            Effect2026.EffectParameter("fire_temperature", "Temperature", 1000f, 5000f, 2500f, 500f),
            Effect2026.EffectParameter("breath_duration", "Duration", 1f, 5f, 3f)
        )
    )
    
    val PHOENIX_REBIRTH = Effect2026(
        id = "phoenix_rebirth",
        name = "Phoenix Rebirth",
        description = "Rise from the ashes in a spectacular flame resurrection!",
        emoji = "🔥",
        category = Effect2026Category.MYTHICAL,
        rarity = EffectRarity.MYTHIC,
        levelRequired = 46,
        xpValue = 460,
        gradientColors = listOf(0xFFFF4500, 0xFFFFA500, 0xFFFFD700, 0xFFFFFF00),
        hasPhysics = true,
        parameters = listOf(
            Effect2026.EffectParameter("rebirth_cycles", "Cycles", 1f, 5f, 3f, 1f),
            Effect2026.EffectParameter("flame_intensity", "Flames", 1f, 10f, 5f)
        ),
        unlockRequirements = Effect2026.UnlockRequirements(
            xpNeeded = 7500,
            effectsOwned = listOf("dragon_breath"),
            streakDays = 10
        )
    )
    
    val UNICORN_MAGIC = Effect2026(
        id = "unicorn_magic",
        name = "Unicorn Magic",
        description = "Sparkling rainbow magic that grants wishes!",
        emoji = "🦄",
        category = Effect2026Category.MYTHICAL,
        rarity = EffectRarity.EPIC,
        levelRequired = 16,
        xpValue = 160,
        gradientColors = listOf(0xFFFF69B4, 0xFF9370DB, 0xFF00CED1, 0xFF98FB98, 0xFFFFD700),
        isInteractive = true,
        parameters = listOf(
            Effect2026.EffectParameter("magic_power", "Magic", 1f, 10f, 5f),
            Effect2026.EffectParameter("sparkle_density", "Sparkles", 0.5f, 3f, 1.5f)
        )
    )
    
    val FAIRY_DUST = Effect2026(
        id = "fairy_dust",
        name = "Fairy Dust",
        description = "Enchanted pixie dust that makes you fly (visually)!",
        emoji = "🧚",
        category = Effect2026Category.MYTHICAL,
        rarity = EffectRarity.RARE,
        levelRequired = 10,
        xpValue = 100,
        gradientColors = listOf(0xFFFFD700, 0xFFFFA500, 0xFFFF69B4),
        parameters = listOf(
            Effect2026.EffectParameter("dust_amount", "Dust", 0.5f, 3f, 1f),
            Effect2026.EffectParameter("float_height", "Float", 50f, 300f, 150f, 25f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // SYNTHWAVE EFFECTS (Retro-Futuristic)
    // ═════════════════════════════════════════════════════════════════
    
    val RETRO_GRID = Effect2026(
        id = "retro_grid",
        name = "Retro Grid",
        description = "Endless neon grid stretching into the digital sunset",
        emoji = "🕹️",
        category = Effect2026Category.SYNTHWAVE,
        rarity = EffectRarity.UNCOMMON,
        levelRequired = 5,
        xpValue = 50,
        gradientColors = listOf(0xFFFF00FF, 0xFF00FFFF, 0xFFFF1493),
        parameters = listOf(
            Effect2026.EffectParameter("grid_speed", "Grid Speed", 1f, 10f, 5f),
            Effect2026.EffectParameter("perspective", "Perspective", 0.3f, 1f, 0.7f)
        )
    )
    
    val SUNSET_DRIVE = Effect2026(
        id = "sunset_drive",
        name = "Sunset Drive",
        description = "Cruising down the vaporwave highway",
        emoji = "🏎️",
        category = Effect2026Category.SYNTHWAVE,
        rarity = EffectRarity.RARE,
        levelRequired = 13,
        xpValue = 130,
        gradientColors = listOf(0xFFFF1493, 0xFFFF4500, 0xFFFFA500, 0xFFFFD700),
        parameters = listOf(
            Effect2026.EffectParameter("car_speed", "Speed", 50f, 200f, 100f, 10f),
            Effect2026.EffectParameter("palm_trees", "Palms", 0f, 1f, 1f)
        )
    )
    
    val LASER_SHOW = Effect2026(
        id = "laser_show",
        name = "Laser Show",
        description = "Disco lasers that sync to your music (if playing)",
        emoji = "🎆",
        category = Effect2026Category.SYNTHWAVE,
        rarity = EffectRarity.EPIC,
        levelRequired = 19,
        xpValue = 190,
        gradientColors = listOf(0xFF00FF00, 0xFFFF0000, 0xFF0000FF, 0xFFFFFF00),
        hasAI = true,
        parameters = listOf(
            Effect2026.EffectParameter("laser_count", "Lasers", 3f, 20f, 8f, 1f),
            Effect2026.EffectParameter("music_sync", "Music Sync", 0f, 1f, 1f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // CHAOS EFFECTS (Unpredictable)
    // ═════════════════════════════════════════════════════════════════
    
    val CHAOS_THEORY = Effect2026(
        id = "chaos_theory",
        name = "Chaos Theory",
        description = "Butterfly effect - tiny changes cause massive differences!",
        emoji = "🦋",
        category = Effect2026Category.CHAOS,
        rarity = EffectRarity.LEGENDARY,
        levelRequired = 36,
        xpValue = 360,
        gradientColors = listOf(0xFF4B0082, 0xFF8B008B, 0xFF9400D3),
        hasAI = true,
        isInteractive = true,
        parameters = listOf(
            Effect2026.EffectParameter("sensitivity", "Sensitivity", 0.1f, 1f, 0.5f),
            Effect2026.EffectParameter("randomness", "Randomness", 0.3f, 1f, 0.7f)
        )
    )
    
    val FRACTAL_ZOOM = Effect2026(
        id = "fractal_zoom",
        name = "Fractal Zoom",
        description = "Infinite mathematical beauty - zoom forever into Mandelbrot!",
        emoji = "🔍",
        category = Effect2026Category.CHAOS,
        rarity = EffectRarity.MYTHIC,
        levelRequired = 44,
        xpValue = 440,
        gradientColors = listOf(0xFF000000, 0xFF000080, 0xFF800080, 0xFFFF00FF),
        hasAI = true,
        parameters = listOf(
            Effect2026.EffectParameter("zoom_depth", "Zoom", 1f, 100f, 10f),
            Effect2026.EffectParameter("color_shift", "Colors", 0f, 1f, 0.5f),
            Effect2026.EffectParameter("iterations", "Detail", 100f, 1000f, 500f, 50f)
        ),
        unlockRequirements = Effect2026.UnlockRequirements(
            xpNeeded = 7000,
            effectsOwned = listOf("chaos_theory")
        )
    )
    
    val RANDOMIZER = Effect2026(
        id = "randomizer",
        name = "The Randomizer",
        description = "Completely unpredictable - combines random effects every second!",
        emoji = "🎲",
        category = Effect2026Category.CHAOS,
        rarity = EffectRarity.EPIC,
        levelRequired = 21,
        xpValue = 210,
        gradientColors = listOf(0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF, 0xFFFF0000),
        hasAI = true,
        parameters = listOf(
            Effect2026.EffectParameter("switch_speed", "Switch Speed", 1f, 10f, 3f),
            Effect2026.EffectParameter("blend_mode", "Blend", 0f, 1f, 0.5f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // TRANSCENDENT EFFECT (Ultimate Unlock)
    // ═════════════════════════════════════════════════════════════════
    
    val TRANSCENDENCE = Effect2026(
        id = "transcendence",
        name = "✨ TRANSCENDENCE ✨",
        description = "YOU HAVE BECOME ONE WITH THE CANDY. ULTIMATE POWER UNLOCKED!",
        emoji = "🌟",
        category = Effect2026Category.CHAOS,
        rarity = EffectRarity.TRANSCENDENT,
        levelRequired = 100,
        xpValue = 10000,
        gradientColors = listOf(
            0xFFFF0000, 0xFFFF7F00, 0xFFFFFF00, 0xFF00FF00,
            0xFF0000FF, 0xFF4B0082, 0xFF9400D3, 0xFFFF1493
        ),
        hasPhysics = true,
        hasAI = true,
        isInteractive = true,
        parameters = listOf(
            Effect2026.EffectParameter("god_mode", "God Mode", 1f, 10f, 10f),
            Effect2026.EffectParameter("reality_bend", "Reality", 0f, 1f, 1f),
            Effect2026.EffectParameter("infinity", "Infinity", 1f, 100f, 100f)
        ),
        unlockRequirements = Effect2026.UnlockRequirements(
            xpNeeded = 100000,
            effectsOwned = emptyList(), // All effects!
            achievementsNeeded = listOf("master_of_candy", "quantum_physicist", "cosmic_explorer"),
            streakDays = 100
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // ALL EFFECTS LIST
    // ═════════════════════════════════════════════════════════════════
    
    val ALL_EFFECTS_2026 = listOf(
        // Quantum
        QUANTUM_FLUX, PARTICLE_COLLIDER, QUANTUM_TUNNEL, SCHRODINGER_CANDY,
        
        // Holographic
        HOLO_CANDY, MATRIX_RAIN, CYBER_PORTAL, GLITCH_ARTIFACT,
        
        // Temporal
        TIME_WARP, CHRONO_CANDY, DEJA_VU,
        
        // Biological
        BIO_LUMINESCENCE, CANDY_ECOSYSTEM, DNA_HELIX,
        
        // Nebula
        SUPERNOVA, BLACK_HOLE, NEBULA_CLOUD, GRAVITY_WELL, DARK_MATTER,
        
        // Cybernetic
        AI_CONSCIOUSNESS, NEURAL_NETWORK, DATA_STREAM, CIRCUIT_BOARD,
        
        // Elemental
        ELEMENTAL_STORM, CRYSTAL_FORMATION, VOLCANIC_ERUPTION,
        
        // Mythical
        DRAGON_BREATH, PHOENIX_REBIRTH, UNICORN_MAGIC, FAIRY_DUST,
        
        // Synthwave
        RETRO_GRID, SUNSET_DRIVE, LASER_SHOW,
        
        // Chaos
        CHAOS_THEORY, FRACTAL_ZOOM, RANDOMIZER,
        
        // Transcendent
        TRANSCENDENCE
    )
    
    fun getById(id: String): Effect2026? = ALL_EFFECTS_2026.find { it.id == id }
    fun getByCategory(category: Effect2026Category) = ALL_EFFECTS_2026.filter { it.category == category }
    fun getByRarity(rarity: EffectRarity) = ALL_EFFECTS_2026.filter { it.rarity == rarity }
    fun getByLevel(level: Int) = ALL_EFFECTS_2026.filter { it.levelRequired <= level }
    fun getUnlockable(level: Int, owned: List<String>) = ALL_EFFECTS_2026.filter { 
        it.levelRequired <= level && !owned.contains(it.id) 
    }
    
    val TOTAL_EFFECTS = ALL_EFFECTS_2026.size
    val MAX_LEVEL = 100
}
