package com.sugarmunch.app.effects.x2026

import androidx.compose.ui.graphics.Color

/**
 * Collection of cloth simulation presets for SugarMunch
 */
object ClothPresetCollection {
    
    val allPresets = listOf(
        Preset(
            id = "silk_flag",
            name = "Silk Flag",
            description = "Flowing silk flag in the wind",
            fabricType = FabricType.SILK,
            color = Color(0xFFFF69B4),
            renderMode = ClothRenderMode.FABRIC,
            windStrength = 0.8f,
            mood = "Energetic"
        ),
        
        Preset(
            id = "velvet_curtain",
            name = "Velvet Curtain",
            description = "Heavy velvet theater curtain",
            fabricType = FabricType.VELVET,
            color = Color(0xFF8B0000),
            renderMode = ClothRenderMode.FABRIC,
            windStrength = 0.2f,
            mood = "Dramatic"
        ),
        
        Preset(
            id = "cotton_sheet",
            name = "Cotton Sheet",
            description = "White cotton sheet drying",
            fabricType = FabricType.COTTON,
            color = Color(0xFFF8F8FF),
            renderMode = ClothRenderMode.FABRIC,
            windStrength = 0.5f,
            mood = "Calm"
        ),
        
        Preset(
            id = "metallic_cape",
            name = "Metallic Cape",
            description = "Shiny metallic superhero cape",
            fabricType = FabricType.METALLIC,
            color = Color(0xFFC0C0C0),
            renderMode = ClothRenderMode.FABRIC,
            windStrength = 0.3f,
            mood = "Futuristic"
        ),
        
        Preset(
            id = "rainbow_ribbon",
            name = "Rainbow Ribbon",
            description = "Colorful rhythmic gymnastics ribbon",
            fabricType = FabricType.SILK,
            color = Color(0xFFFF0000),
            renderMode = ClothRenderMode.RIBBON,
            windStrength = 0.6f,
            mood = "Fun"
        ),
        
        Preset(
            id = "neon_mesh",
            name = "Neon Mesh",
            description = "Glowing neon wireframe cloth",
            fabricType = FabricType.METALLIC,
            color = Color(0xFF00FFFF),
            renderMode = ClothRenderMode.WIREFRAME,
            windStrength = 0.7f,
            mood = "Party"
        ),
        
        Preset(
            id = "particle_dust",
            name = "Particle Dust",
            description = "Floating dust particles on cloth",
            fabricType = FabricType.COTTON,
            color = Color(0xFFD2691E),
            renderMode = ClothRenderMode.PARTICLES,
            windStrength = 0.4f,
            mood = "Trippy"
        ),
        
        Preset(
            id = "ocean_wave",
            name = "Ocean Wave",
            description = "Blue fabric flowing like water",
            fabricType = FabricType.SILK,
            color = Color(0xFF006994),
            renderMode = ClothRenderMode.FABRIC,
            windStrength = 0.9f,
            mood = "Calm"
        ),
        
        Preset(
            id = "fire_banner",
            name = "Fire Banner",
            description = "Burning orange and red banner",
            fabricType = FabricType.SILK,
            color = Color(0xFFFF4500),
            renderMode = ClothRenderMode.FABRIC,
            windStrength = 1.0f,
            mood = "Extreme"
        ),
        
        Preset(
            id = "ghost_sheet",
            name = "Ghost Sheet",
            description = "Spooky floating white sheet",
            fabricType = FabricType.COTTON,
            color = Color(0xFFF5F5F5),
            renderMode = ClothRenderMode.FABRIC,
            windStrength = 0.3f,
            mood = "Spooky"
        ),
        
        Preset(
            id = "candy_stripe",
            name = "Candy Stripe",
            description = "Striped candy wrapper fabric",
            fabricType = FabricType.METALLIC,
            color = Color(0xFFFF69B4),
            renderMode = ClothRenderMode.FABRIC,
            windStrength = 0.5f,
            mood = "Sweet"
        ),
        
        Preset(
            id = "aurora_curtain",
            name = "Aurora Curtain",
            description = "Northern lights fabric display",
            fabricType = FabricType.SILK,
            color = Color(0xFF00FF00),
            renderMode = ClothRenderMode.RIBBON,
            windStrength = 0.6f,
            mood = "Magical"
        )
    )
    
    fun getPresetById(id: String): Preset? {
        return allPresets.find { it.id == id }
    }
    
    fun getPresetsByMood(mood: String): List<Preset> {
        return allPresets.filter { it.mood.equals(mood, ignoreCase = true) }
    }
    
    fun getPresetsByFabricType(type: FabricType): List<Preset> {
        return allPresets.filter { it.fabricType == type }
    }
    
    fun getPresetsByRenderMode(mode: ClothRenderMode): List<Preset> {
        return allPresets.filter { it.renderMode == mode }
    }
}

data class Preset(
    val id: String,
    val name: String,
    val description: String,
    val fabricType: FabricType,
    val color: Color,
    val renderMode: ClothRenderMode,
    val windStrength: Float,
    val mood: String
)
