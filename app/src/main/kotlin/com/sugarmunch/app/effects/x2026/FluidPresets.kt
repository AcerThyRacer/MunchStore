package com.sugarmunch.app.effects.x2026

import androidx.compose.ui.graphics.Color

/**
 * Collection of fluid simulation presets for SugarMunch
 */
object FluidPresetCollection {
    
    val allPresets = listOf(
        Preset(
            id = "liquid_candy",
            name = "Liquid Candy",
            description = "Sweet flowing candy syrup",
            config = FluidSimulationConfig(
                viscosity = 0.15f,
                diffusion = 0.0001f,
                surfaceTension = 0.5f,
                gravity = 9.8f,
                fluidColor = Color(0xFFFF69B4),
                secondaryColor = Color(0xFF00FFA3),
                name = "Liquid Candy"
            ),
            renderMode = FluidRenderMode.SURFACE,
            mood = "Energetic"
        ),
        
        Preset(
            id = "honey_flow",
            name = "Honey Flow",
            description = "Thick golden honey dripping",
            config = FluidSimulationConfig(
                viscosity = 0.5f,
                diffusion = 0.00001f,
                surfaceTension = 0.7f,
                gravity = 9.8f,
                fluidColor = Color(0xFFFFD700),
                secondaryColor = Color(0xFFFFA500),
                name = "Honey"
            ),
            renderMode = FluidRenderMode.SURFACE,
            mood = "Calm"
        ),
        
        Preset(
            id = "rainbow_slime",
            name = "Rainbow Slime",
            description = "Stretchy colorful slime",
            config = FluidSimulationConfig(
                viscosity = 2.0f,
                diffusion = 0.0001f,
                surfaceTension = 0.9f,
                gravity = 4.9f,
                fluidColor = Color(0xFF00FF00),
                secondaryColor = Color(0xFFFF00FF),
                name = "Slime"
            ),
            renderMode = FluidRenderMode.PARTICLES,
            mood = "Trippy"
        ),
        
        Preset(
            id = "lava_lamp",
            name = "Lava Lamp",
            description = "Groovy lava lamp bubbles",
            config = FluidSimulationConfig(
                viscosity = 0.1f,
                diffusion = 0.001f,
                surfaceTension = 0.5f,
                gravity = 9.8f,
                fluidColor = Color(0xFFFF4500),
                secondaryColor = Color(0xFFFFD700),
                name = "Lava"
            ),
            renderMode = FluidRenderMode.SURFACE,
            mood = "Retro"
        ),
        
        Preset(
            id = "water_ripple",
            name = "Water Ripple",
            description = "Clear water with ripples",
            config = FluidSimulationConfig(
                viscosity = 0.0001f,
                diffusion = 0.00001f,
                surfaceTension = 0.3f,
                gravity = 9.8f,
                fluidColor = Color(0xFF00BFFF),
                secondaryColor = Color(0xFF87CEEB),
                name = "Water"
            ),
            renderMode = FluidRenderMode.SURFACE,
            mood = "Calm"
        ),
        
        Preset(
            id = "neon_fluid",
            name = "Neon Fluid",
            description = "Glowing neon liquid",
            config = FluidSimulationConfig(
                viscosity = 0.05f,
                diffusion = 0.0001f,
                surfaceTension = 0.4f,
                gravity = 9.8f,
                fluidColor = Color(0xFF00FFFF),
                secondaryColor = Color(0xFFFF00FF),
                name = "Neon"
            ),
            renderMode = FluidRenderMode.HEATMAP,
            mood = "Party"
        ),
        
        Preset(
            id = "chocolate_syrup",
            name = "Chocolate Syrup",
            description = "Rich chocolate flowing",
            config = FluidSimulationConfig(
                viscosity = 0.3f,
                diffusion = 0.00005f,
                surfaceTension = 0.6f,
                gravity = 9.8f,
                fluidColor = Color(0xFF8B4513),
                secondaryColor = Color(0xFFD2691E),
                name = "Chocolate"
            ),
            renderMode = FluidRenderMode.SURFACE,
            mood = "Calm"
        ),
        
        Preset(
            id = "mercury",
            name = "Liquid Metal",
            description = "Shiny metallic fluid",
            config = FluidSimulationConfig(
                viscosity = 0.00015f,
                diffusion = 0.00001f,
                surfaceTension = 0.95f,
                gravity = 9.8f,
                fluidColor = Color(0xFFC0C0C0),
                secondaryColor = Color(0xFFE8E8E8),
                name = "Mercury"
            ),
            renderMode = FluidRenderMode.SURFACE,
            mood = "Futuristic"
        ),
        
        Preset(
            id = "bubble_gum",
            name = "Bubble Gum",
            description = "Sticky pink bubble gum",
            config = FluidSimulationConfig(
                viscosity = 0.4f,
                diffusion = 0.0001f,
                surfaceTension = 0.8f,
                gravity = 9.8f,
                fluidColor = Color(0xFFFF69B4),
                secondaryColor = Color(0xFFFFB6C1),
                name = "Bubble Gum"
            ),
            renderMode = FluidRenderMode.PARTICLES,
            mood = "Fun"
        ),
        
        Preset(
            id = "plasma",
            name = "Plasma",
            description = "Hot plasma energy",
            config = FluidSimulationConfig(
                viscosity = 0.01f,
                diffusion = 0.01f,
                surfaceTension = 0.2f,
                gravity = 0f,
                fluidColor = Color(0xFFFF0000),
                secondaryColor = Color(0xFFFFFF00),
                name = "Plasma"
            ),
            renderMode = FluidRenderMode.HEATMAP,
            mood = "Extreme"
        )
    )
    
    fun getPresetById(id: String): Preset? {
        return allPresets.find { it.id == id }
    }
    
    fun getPresetsByMood(mood: String): List<Preset> {
        return allPresets.filter { it.mood.equals(mood, ignoreCase = true) }
    }
    
    fun getPresetsByRenderMode(mode: FluidRenderMode): List<Preset> {
        return allPresets.filter { it.renderMode == mode }
    }
}

data class Preset(
    val id: String,
    val name: String,
    val description: String,
    val config: FluidSimulationConfig,
    val renderMode: FluidRenderMode,
    val mood: String
)
