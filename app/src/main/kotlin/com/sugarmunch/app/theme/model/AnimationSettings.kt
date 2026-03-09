package com.sugarmunch.app.theme.model

import androidx.datastore.preferences.core.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 🎬 ANIMATION SETTINGS SYSTEM
 * Complete customization for all app animations with live previews
 */

@Serializable
data class AnimationSettings(
    // ═════════════════════════════════════════════════════════════════
    // GLOBAL ANIMATION TOGGLES
    // ═════════════════════════════════════════════════════════════════
    val animationsEnabled: Boolean = true,
    val reduceMotion: Boolean = false,
    val powerSaveMode: Boolean = false,
    
    // ═════════════════════════════════════════════════════════════════
    // TRANSITION ANIMATIONS
    // ═════════════════════════════════════════════════════════════════
    val transitionType: TransitionType = TransitionType.SMOOTH,
    val transitionDuration: Float = 300f, // ms
    val screenTransitionEnabled: Boolean = true,
    val sharedElementTransitions: Boolean = true,
    
    // ═════════════════════════════════════════════════════════════════
    // UI MICRO-INTERACTIONS
    // ═════════════════════════════════════════════════════════════════
    val buttonPressScale: Float = 0.95f,
    val buttonPressAnimationDuration: Float = 150f,
    val cardHoverScale: Float = 1.02f,
    val rippleEnabled: Boolean = true,
    val rippleColorStyle: RippleColorStyle = RippleColorStyle.THEME_ACCENT,
    
    // ═════════════════════════════════════════════════════════════════
    // LIST & GRID ANIMATIONS
    // ═════════════════════════════════════════════════════════════════
    val listItemEntrance: ListAnimationType = ListAnimationType.FADE_SLIDE,
    val listStaggerDelay: Float = 50f,
    val gridItemEntrance: ListAnimationType = ListAnimationType.SCALE_FADE,
    val pullToRefreshAnimation: Boolean = true,
    val infiniteScrollAnimation: Boolean = true,
    
    // ═════════════════════════════════════════════════════════════════
    // SCROLL ANIMATIONS
    // ═════════════════════════════════════════════════════════════════
    val parallaxScrolling: Boolean = true,
    val scrollHeaderFade: Boolean = true,
    val scrollBounceEffect: Boolean = true,
    val fastScrollingAnimation: Boolean = true,
    
    // ═════════════════════════════════════════════════════════════════
    // LOADING & SKELETON
    // ═════════════════════════════════════════════════════════════════
    val shimmerEnabled: Boolean = true,
    val shimmerSpeed: Float = 1f,
    val skeletonPulse: Boolean = true,
    val loadingSpinnerStyle: SpinnerStyle = SpinnerStyle.CANDY,
    
    // ═════════════════════════════════════════════════════════════════
    // SUCCESS & ERROR ANIMATIONS
    // ═════════════════════════════════════════════════════════════════
    val successAnimationEnabled: Boolean = true,
    val errorShakeEnabled: Boolean = true,
    val confettiOnSuccess: Boolean = true,
    val hapticFeedback: Boolean = true,
    
    // ═════════════════════════════════════════════════════════════════
    // FAB & OVERLAY ANIMATIONS
    // ═════════════════════════════════════════════════════════════════
    val fabEntranceAnimation: FabAnimationType = FabAnimationType.BOUNCE,
    val fabDragPhysics: Boolean = true,
    val fabSnapAnimation: Boolean = true,
    val overlayPanelAnimation: Boolean = true,
    
    // ═════════════════════════════════════════════════════════════════
    // PARTICLE EFFECTS
    // ═════════════════════════════════════════════════════════════════
    val particlesEnabled: Boolean = true,
    val particleDensity: ParticleDensity = ParticleDensity.MEDIUM,
    val particleSpeed: Float = 1f,
    val particleInteractionEnabled: Boolean = true,
    
    // ═════════════════════════════════════════════════════════════════
    // THEME BACKGROUND ANIMATIONS
    // ═════════════════════════════════════════════════════════════════
    val backgroundAnimationEnabled: Boolean = true,
    val meshAnimationSpeed: Float = 1f,
    val gradientAnimationSpeed: Float = 1f,
    val backgroundParallax: Boolean = true,
    
    // ═════════════════════════════════════════════════════════════════
    // ADVANCED PHYSICS
    // ═════════════════════════════════════════════════════════════════
    val springStiffness: Float = 300f,
    val springDamping: Float = 0.7f,
    val flingFriction: Float = 1f,
    val overscrollStretch: Boolean = true
) {
    companion object {
        val DEFAULT = AnimationSettings()
        
        val CHILL = AnimationSettings(
            animationsEnabled = true,
            transitionDuration = 400f,
            listStaggerDelay = 80f,
            shimmerSpeed = 0.6f,
            particleDensity = ParticleDensity.LOW,
            particleSpeed = 0.5f,
            springStiffness = 200f,
            springDamping = 0.9f
        )
        
        val ENERGETIC = AnimationSettings(
            animationsEnabled = true,
            transitionDuration = 200f,
            listStaggerDelay = 30f,
            shimmerSpeed = 1.5f,
            particleDensity = ParticleDensity.HIGH,
            particleSpeed = 1.5f,
            springStiffness = 500f,
            springDamping = 0.5f,
            confettiOnSuccess = true
        )
        
        val OFF = AnimationSettings(
            animationsEnabled = false,
            reduceMotion = true,
            particlesEnabled = false,
            shimmerEnabled = false,
            backgroundAnimationEnabled = false
        )
    }
}

// ═════════════════════════════════════════════════════════════════
// ENUMERATIONS
// ═════════════════════════════════════════════════════════════════

enum class TransitionType {
    INSTANT,        // No animation
    FADE,           // Simple fade
    SLIDE,          // Slide transition
    SMOOTH,         // Smooth fade + slide
    SHARED_ELEMENT, // Material shared element
    CUBE,           // 3D cube rotation
    FLIP,           // Card flip
    ZOOM,           // Zoom in/out
    EXPLODE,        // Material explode
    CANDY_BOUNCE    // Custom bouncy transition
}

enum class RippleColorStyle {
    THEME_PRIMARY,
    THEME_ACCENT,
    THEME_SECONDARY,
    ADAPTIVE_SURFACE,
    CUSTOM
}

enum class ListAnimationType {
    NONE,
    FADE,
    SLIDE_UP,
    SLIDE_SIDE,
    SCALE_FADE,
    FADE_SLIDE,
    BOUNCE,
    ELASTIC,
    FLIP,
    CASCADING
}

enum class SpinnerStyle {
    DEFAULT,
    CANDY,
    SUGAR_CUBES,
    LOLLIPOP,
    COTTON_CANDY
}

enum class FabAnimationType {
    NONE,
    FADE,
    SLIDE,
    SCALE,
    BOUNCE,
    ELASTIC,
    SPIN
}

enum class ParticleDensity {
    OFF,
    MINIMAL,    // 10-20 particles
    LOW,        // 20-40 particles
    MEDIUM,     // 40-80 particles
    HIGH,       // 80-150 particles
    EXTREME     // 150+ particles
}

// ═════════════════════════════════════════════════════════════════
// PRESET PACKAGES
// ═════════════════════════════════════════════════════════════════

object AnimationPresets {
    
    val SMOOTH = AnimationSettings.DEFAULT
    
    val CHILL = AnimationSettings.CHILL
    
    val ENERGETIC = AnimationSettings.ENERGETIC
    
    val OFF = AnimationSettings.OFF
    
    val GAMER = AnimationSettings(
        animationsEnabled = true,
        transitionType = TransitionType.CANDY_BOUNCE,
        transitionDuration = 150f,
        buttonPressScale = 0.92f,
        listItemEntrance = ListAnimationType.BOUNCE,
        listStaggerDelay = 25f,
        particleDensity = ParticleDensity.HIGH,
        particleSpeed = 1.8f,
        fabEntranceAnimation = FabAnimationType.ELASTIC,
        springStiffness = 600f,
        springDamping = 0.4f,
        confettiOnSuccess = true,
        hapticFeedback = true
    )
    
    val MINIMAL = AnimationSettings(
        animationsEnabled = true,
        transitionType = TransitionType.FADE,
        transitionDuration = 200f,
        rippleEnabled = false,
        listItemEntrance = ListAnimationType.FADE,
        listStaggerDelay = 0f,
        shimmerEnabled = false,
        particleDensity = ParticleDensity.OFF,
        backgroundAnimationEnabled = false,
        parallaxScrolling = false
    )
    
    val CINEMATIC = AnimationSettings(
        animationsEnabled = true,
        transitionType = TransitionType.SHARED_ELEMENT,
        transitionDuration = 500f,
        screenTransitionEnabled = true,
        sharedElementTransitions = true,
        listItemEntrance = ListAnimationType.CASCADING,
        listStaggerDelay = 100f,
        scrollHeaderFade = true,
        backgroundParallax = true,
        particleDensity = ParticleDensity.LOW,
        particleSpeed = 0.7f
    )
    
    val ALL_PRESETS = listOf(
        "smooth" to SMOOTH,
        "chill" to CHILL,
        "energetic" to ENERGETIC,
        "gamer" to GAMER,
        "minimal" to MINIMAL,
        "cinematic" to CINEMATIC,
        "off" to OFF
    )
}

// ═════════════════════════════════════════════════════════════════
// SERIALIZATION HELPERS
// ═════════════════════════════════════════════════════════════════

fun AnimationSettings.toJson(): String = Json.encodeToString(this)

fun String.toAnimationSettings(): AnimationSettings = 
    try {
        Json.decodeFromString(this)
    } catch (e: Exception) {
        AnimationSettings.DEFAULT
    }

// DataStore keys
object AnimationSettingsKeys {
    val SETTINGS_JSON = stringPreferencesKey("animation_settings_json")
    val LAST_PRESET = stringPreferencesKey("animation_last_preset")
}
