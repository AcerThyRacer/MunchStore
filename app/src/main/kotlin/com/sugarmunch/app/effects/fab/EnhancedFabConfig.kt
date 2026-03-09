package com.sugarmunch.app.effects.fab

import kotlinx.serialization.Serializable

/**
 * 🍭 ENHANCED FAB CONFIGURATION — Way More Settings & Customization
 *
 * Extends the base FAB system with extensive shape, gesture, particle,
 * badge, glow, sound, auto-hide, blur, progress-ring and menu customization.
 */

// ═════════════════════════════════════════════════════════════════
// SHAPE & SIZE
// ═════════════════════════════════════════════════════════════════

enum class FabShape {
    CIRCLE,      // Classic round FAB
    SQUARE,      // Square with optional corner radius
    PILL,        // Wide capsule / pill shape
    STAR,        // Five-pointed star
    DIAMOND,     // Rotated square
    HEXAGON,     // Six-sided polygon
    CLOUD,       // Fluffy cloud shape
    HEART,       // Heart shape
    BURST,       // Starburst / gear
    BLOB         // Organic morphing blob
}

enum class FabSize {
    TINY,   // 32 dp
    SMALL,  // 44 dp
    NORMAL, // 56 dp  (Material default)
    LARGE,  // 72 dp
    XL,     // 88 dp
    GIANT   // 112 dp
}

/** Corner radius applied when [FabShape] is [FabShape.SQUARE]. 0 = sharp corners. */
@Serializable
data class FabCornerRadius(val dp: Float = 12f)

// ═════════════════════════════════════════════════════════════════
// POSITION
// ═════════════════════════════════════════════════════════════════

enum class FabPositionAnchor {
    BOTTOM_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    TOP_RIGHT,
    TOP_LEFT,
    TOP_CENTER,
    CENTER_RIGHT,
    CENTER_LEFT,
    FLOATING_FREE  // User can drag anywhere
}

@Serializable
data class FabPositionConfig(
    val anchor: FabPositionAnchor = FabPositionAnchor.BOTTOM_RIGHT,
    val offsetXDp: Float = 16f,
    val offsetYDp: Float = 16f,
    val snapToEdge: Boolean = true,
    val snapMarginDp: Float = 8f,
    val respectSystemBars: Boolean = true
)

// ═════════════════════════════════════════════════════════════════
// BORDER / OUTLINE
// ═════════════════════════════════════════════════════════════════

enum class FabBorderStyle {
    NONE,
    SOLID,
    DASHED,
    DOTTED,
    GLOW,          // Soft glow ring
    GRADIENT,      // Animated gradient ring
    ANIMATED_SPIN, // Spinning conic gradient border
    DOUBLE,        // Two concentric rings
    RAINBOW        // Full rainbow animated border
}

@Serializable
data class FabBorderConfig(
    val style: FabBorderStyle = FabBorderStyle.NONE,
    val widthDp: Float = 2f,
    val color: Long = 0xFFFFFFFF,
    val secondaryColor: Long = 0xFFFF69B4,
    val dashLengthDp: Float = 8f,
    val gapLengthDp: Float = 4f,
    val animationSpeedMultiplier: Float = 1f
)

// ═════════════════════════════════════════════════════════════════
// ICON / LABEL
// ═════════════════════════════════════════════════════════════════

enum class FabIconStyle {
    EMOJI,           // UTF-8 emoji character
    TEXT,            // Short text (1-3 chars)
    ANIMATED_EMOJI,  // Emoji that bounces/spins
    MATERIAL_ICON,   // Material Design icon name
    LOTTIE           // Lottie animation reference (asset key)
}

@Serializable
data class FabIconConfig(
    val style: FabIconStyle = FabIconStyle.EMOJI,
    val value: String = "✨",   // Emoji, text, icon name, or lottie key
    val sizeSp: Float = 24f,
    val tintColor: Long? = null, // null = use theme color
    val animateOnIdle: Boolean = true,
    val idleAnimationType: FabIdleAnimation = FabIdleAnimation.GENTLE_PULSE
)

enum class FabIdleAnimation {
    NONE,
    GENTLE_PULSE,
    SLOW_SPIN,
    WIGGLE,
    HEARTBEAT,
    BREATHING,
    BOUNCE,
    SWING,
    TADA,
    RUBBER_BAND
}

// ═════════════════════════════════════════════════════════════════
// SHADOW / ELEVATION
// ═════════════════════════════════════════════════════════════════

enum class FabShadowStyle {
    NONE,
    SOFT,         // Default material shadow
    HARD,         // Crisp offset shadow
    COLORED,      // Shadow tinted by accent color
    NEON,         // Bright neon glow shadow
    LONG,         // Long flat shadow (flat design)
    MULTI_LAYER   // Several stacked shadows for depth
}

@Serializable
data class FabShadowConfig(
    val style: FabShadowStyle = FabShadowStyle.SOFT,
    val elevationDp: Float = 8f,
    val color: Long = 0x44000000,
    val offsetXDp: Float = 0f,
    val offsetYDp: Float = 4f,
    val spreadDp: Float = 0f
)

// ═════════════════════════════════════════════════════════════════
// HAPTIC FEEDBACK
// ═════════════════════════════════════════════════════════════════

enum class FabHapticType {
    NONE,
    LIGHT_CLICK,
    MEDIUM_CLICK,
    HEAVY_CLICK,
    DOUBLE_CLICK,
    TICK,
    SUCCESS,
    WARNING,
    ERROR,
    RHYTHM_PULSE,   // Rhythmic repeating pattern
    CANDY_CRUNCH,   // Custom multi-vibe candy crunch pattern
    JELLY_BOUNCE    // Soft bouncy jelly-like vibration
}

@Serializable
data class FabHapticConfig(
    val onTap: FabHapticType = FabHapticType.MEDIUM_CLICK,
    val onLongPress: FabHapticType = FabHapticType.HEAVY_CLICK,
    val onExpand: FabHapticType = FabHapticType.TICK,
    val onEffectTrigger: FabHapticType = FabHapticType.SUCCESS,
    val intensity: Float = 1f  // 0.0 – 2.0 multiplier
)

// ═════════════════════════════════════════════════════════════════
// BADGE / INDICATOR
// ═════════════════════════════════════════════════════════════════

enum class FabBadgeStyle {
    NONE,
    DOT,           // Small dot indicator
    COUNT,         // Number badge
    PULSE_DOT,     // Pulsing animated dot
    RING,          // Thin ring around FAB
    SPARKLE,       // Sparkle burst indicator
    CUSTOM_TEXT    // Any short string
}

@Serializable
data class FabBadgeConfig(
    val style: FabBadgeStyle = FabBadgeStyle.NONE,
    val count: Int = 0,
    val customText: String = "",
    val color: Long = 0xFFFF1744,
    val textColor: Long = 0xFFFFFFFF,
    val sizeDp: Float = 18f,
    val pulsing: Boolean = false
)

// ═════════════════════════════════════════════════════════════════
// GLOW
// ═════════════════════════════════════════════════════════════════

@Serializable
data class FabGlowConfig(
    val enabled: Boolean = true,
    val color: Long = 0xFFFF69B4,
    val radiusDp: Float = 16f,
    val pulsing: Boolean = true,
    val pulseSpeedMs: Int = 1500,
    val minAlpha: Float = 0.2f,
    val maxAlpha: Float = 0.8f,
    val colorCycling: Boolean = false,
    val colorCycleSpeedMs: Int = 3000
)

// ═════════════════════════════════════════════════════════════════
// SOUND EFFECTS
// ═════════════════════════════════════════════════════════════════

enum class FabSoundEffect {
    NONE,
    CANDY_CRUNCH,   // Satisfying candy crunch
    POP,            // Bubble pop
    CHIME,          // Soft chime bell
    SWOOSH,         // Quick swoosh
    SPARKLE,        // Magical sparkle tinkle
    WHOOSH,         // Windy whoosh
    BOING,          // Cartoon boing
    CLICK,          // Mechanical click
    DING,           // Ding bell
    BUBBLE,         // Underwater bubble
    ZAP,            // Electric zap
    COIN,           // Coin collect
    POWER_UP,       // Video game power-up
    CUSTOM          // Custom asset key
}

@Serializable
data class FabSoundConfig(
    val onTap: FabSoundEffect = FabSoundEffect.CANDY_CRUNCH,
    val onLongPress: FabSoundEffect = FabSoundEffect.CHIME,
    val onExpand: FabSoundEffect = FabSoundEffect.SWOOSH,
    val onCollapse: FabSoundEffect = FabSoundEffect.POP,
    val volume: Float = 0.5f,  // 0.0 – 1.0
    val customAssetKey: String = ""
)

// ═════════════════════════════════════════════════════════════════
// PARTICLE EMISSION ON CLICK
// ═════════════════════════════════════════════════════════════════

enum class FabClickParticleType {
    NONE,
    SPARKLES,
    CONFETTI,
    HEARTS,
    STARS,
    BUBBLES,
    CANDY_PIECES,
    FLAMES,
    PETALS,
    PIXELS,
    COINS,
    LIGHTNING_BOLTS
}

@Serializable
data class FabClickParticleConfig(
    val type: FabClickParticleType = FabClickParticleType.SPARKLES,
    val count: Int = 12,
    val spreadAngle: Float = 360f,
    val minSpeed: Float = 2f,
    val maxSpeed: Float = 8f,
    val lifetimeMs: Int = 800,
    val minSize: Float = 4f,
    val maxSize: Float = 16f,
    val useThemeColors: Boolean = true,
    val customColors: List<Long> = emptyList()
)

// ═════════════════════════════════════════════════════════════════
// EXPANDED MENU STYLE
// ═════════════════════════════════════════════════════════════════

enum class FabMenuStyle {
    RADIAL,       // Items fan out in a radial arc
    LINEAR_UP,    // Items stack vertically upward
    LINEAR_DOWN,  // Items stack vertically downward
    LINEAR_LEFT,  // Items stack horizontally to the left
    LINEAR_RIGHT, // Items stack horizontally to the right
    ARC_UP_LEFT,  // Quarter-circle arc toward upper-left
    ARC_UP_RIGHT, // Quarter-circle arc toward upper-right
    GRID,         // Grid popup
    CIRCULAR,     // Full circle surrounding FAB
    STACKED_CARDS,// Cards stacked like a fan of cards
    BOTTOM_SHEET  // Bottom sheet drawer
}

enum class FabMenuItemAnimation {
    SLIDE_FADE,
    BOUNCE_IN,
    SCALE_IN,
    ROTATE_IN,
    SPRING,
    CATAPULT,
    CASCADE,
    INSTANT
}

@Serializable
data class FabMenuConfig(
    val style: FabMenuStyle = FabMenuStyle.RADIAL,
    val itemAnimation: FabMenuItemAnimation = FabMenuItemAnimation.SPRING,
    val openAnimationMs: Int = 300,
    val itemSpacingDp: Float = 12f,
    val arcAngleRange: Float = 180f,  // for RADIAL and ARC modes
    val arcStartAngle: Float = 180f,  // degrees, 0 = right
    val showLabels: Boolean = true,
    val labelPosition: FabLabelPosition = FabLabelPosition.BESIDE,
    val backdropBlur: Boolean = false,
    val backdropDimAlpha: Float = 0f,
    val closeOnSelect: Boolean = true
)

enum class FabLabelPosition {
    BESIDE,   // Label next to the item button
    BELOW,    // Label under the item button
    ABOVE,    // Label above the item button
    TOOLTIP   // Shows as tooltip on long-press
}

// ═════════════════════════════════════════════════════════════════
// TRANSPARENCY
// ═════════════════════════════════════════════════════════════════

@Serializable
data class FabTransparencyConfig(
    val idleAlpha: Float = 1f,       // Alpha when not interacting
    val pressedAlpha: Float = 0.85f, // Alpha when pressed
    val expandedAlpha: Float = 1f,   // Alpha when menu is open
    val hiddenAlpha: Float = 0f,     // Alpha when auto-hidden
    val transitionMs: Int = 200      // Transition duration
)

// ═════════════════════════════════════════════════════════════════
// AUTO-HIDE
// ═════════════════════════════════════════════════════════════════

enum class FabAutoHideTrigger {
    NEVER,
    INACTIVITY,      // Hide after inactivity timeout
    SCROLL_DOWN,     // Hide when user scrolls down
    KEYBOARD_OPEN,   // Hide when soft keyboard appears
    VIDEO_PLAYING,   // Hide during full-screen video
    CUSTOM_ROUTE     // Hidden on specific navigation routes
}

@Serializable
data class FabAutoHideConfig(
    val trigger: FabAutoHideTrigger = FabAutoHideTrigger.NEVER,
    val inactivityTimeoutMs: Long = 5000,
    val fadeOutMs: Int = 300,
    val fadeInMs: Int = 200,
    val miniModeEnabled: Boolean = false,  // Shrink instead of hide
    val miniSizeRatio: Float = 0.5f        // Size when in mini mode
)

// ═════════════════════════════════════════════════════════════════
// PROGRESS RING
// ═════════════════════════════════════════════════════════════════

enum class FabProgressRingStyle {
    NONE,
    COUNTDOWN,     // Sweeps clockwise to indicate a cooldown
    ACTIVE_EFFECT, // Shows duration of a running effect
    INDETERMINATE, // Spinning indefinite progress
    BATTERY,       // Shows device battery level
    VOLUME         // Shows current media volume
}

@Serializable
data class FabProgressRingConfig(
    val style: FabProgressRingStyle = FabProgressRingStyle.NONE,
    val color: Long = 0xFF00CED1,
    val strokeWidthDp: Float = 3f,
    val insetDp: Float = 2f,
    val showTrack: Boolean = true,
    val trackColor: Long = 0x33FFFFFF,
    val animatedColor: Boolean = true
)

// ═════════════════════════════════════════════════════════════════
// NOTIFICATION BADGE
// ═════════════════════════════════════════════════════════════════

@Serializable
data class FabNotificationBadgeConfig(
    val enabled: Boolean = false,
    val packageName: String = "",   // Monitor this app's notifications
    val maxCount: Int = 99,
    val position: FabBadgeCorner = FabBadgeCorner.TOP_RIGHT,
    val color: Long = 0xFFFF1744,
    val pulseOnNew: Boolean = true
)

enum class FabBadgeCorner {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}

// ═════════════════════════════════════════════════════════════════
// BACKDROP / SCRIM BLUR
// ═════════════════════════════════════════════════════════════════

@Serializable
data class FabBackdropBlurConfig(
    val enabled: Boolean = false,
    val blurRadiusDp: Float = 16f,
    val tintColor: Long = 0x22000000,
    val dismissOnTapOutside: Boolean = true,
    val animationMs: Int = 250
)

// ═════════════════════════════════════════════════════════════════
// GESTURE CONFIGURATION
// ═════════════════════════════════════════════════════════════════

enum class FabGestureAction {
    NONE,
    TOGGLE_MENU,
    TRIGGER_PRIMARY_EFFECT,
    TRIGGER_RANDOM_EFFECT,
    CYCLE_EFFECTS,
    OPEN_EFFECTS_SCREEN,
    TOGGLE_ALL_EFFECTS,
    SHOW_QUICK_SETTINGS,
    DISMISS,
    CUSTOM_CALLBACK
}

@Serializable
data class FabGestureConfig(
    val singleTapAction: FabGestureAction = FabGestureAction.TRIGGER_PRIMARY_EFFECT,
    val doubleTapAction: FabGestureAction = FabGestureAction.TOGGLE_MENU,
    val longPressAction: FabGestureAction = FabGestureAction.OPEN_EFFECTS_SCREEN,
    val swipeUpAction: FabGestureAction = FabGestureAction.CYCLE_EFFECTS,
    val swipeDownAction: FabGestureAction = FabGestureAction.TOGGLE_ALL_EFFECTS,
    val swipeLeftAction: FabGestureAction = FabGestureAction.NONE,
    val swipeRightAction: FabGestureAction = FabGestureAction.NONE,
    val swipeThresholdDp: Float = 40f,
    val doubleTapWindowMs: Int = 300,
    val longPressWindowMs: Int = 500,
    val isDraggable: Boolean = true
)

// ═════════════════════════════════════════════════════════════════
// GRADIENT BACKGROUND
// ═════════════════════════════════════════════════════════════════

enum class FabGradientType {
    SOLID,
    LINEAR,
    RADIAL,
    SWEEP,
    ANIMATED_LINEAR,
    ANIMATED_SWEEP,
    HOLOGRAPHIC,  // Shifting rainbow holographic
    CANDY_STRIPE  // Diagonal candy stripes
}

@Serializable
data class FabGradientConfig(
    val type: FabGradientType = FabGradientType.RADIAL,
    val colors: List<Long> = listOf(0xFFFF69B4, 0xFFFF1493, 0xFFC71585),
    val angleDegrees: Float = 45f,
    val animationSpeedMultiplier: Float = 1f,
    val stripeWidthDp: Float = 8f  // for CANDY_STRIPE
)

// ═════════════════════════════════════════════════════════════════
// RIPPLE / PRESS EFFECT
// ═════════════════════════════════════════════════════════════════

enum class FabPressEffect {
    DEFAULT_RIPPLE,
    SCALE_DOWN,
    BOUNCE,
    JELLY,
    RUBBER_BAND,
    EXPLODE,      // Brief expand then snap back
    PRESS_IN,     // Depth press-in illusion
    GLOW_BURST,   // Glow flare on press
    MORPH_SHAPE   // Shape morphs to different FabShape on press
}

@Serializable
data class FabPressEffectConfig(
    val type: FabPressEffect = FabPressEffect.JELLY,
    val scaleMin: Float = 0.90f,
    val scaleMax: Float = 1.10f,
    val durationMs: Int = 150,
    val springStiffness: Float = 400f,
    val springDamping: Float = 0.7f
)

// ═════════════════════════════════════════════════════════════════
// ENTRANCE / EXIT ANIMATION
// ═════════════════════════════════════════════════════════════════

enum class FabEntranceAnimation {
    NONE,
    SCALE_FADE,
    SLIDE_UP,
    SLIDE_DOWN,
    SLIDE_LEFT,
    SLIDE_RIGHT,
    BOUNCE_IN,
    ROTATE_ZOOM,
    SPRING_UP,
    CANDY_DROP  // Drops from top and bounces
}

enum class FabExitAnimation {
    NONE,
    SCALE_FADE,
    SLIDE_DOWN,
    SHRINK_FADE,
    ROTATE_OUT,
    BURST_DISSOLVE
}

@Serializable
data class FabEntranceExitConfig(
    val entrance: FabEntranceAnimation = FabEntranceAnimation.SPRING_UP,
    val exit: FabExitAnimation = FabExitAnimation.SHRINK_FADE,
    val entranceDurationMs: Int = 400,
    val exitDurationMs: Int = 250,
    val entranceDelayMs: Int = 0
)

// ═════════════════════════════════════════════════════════════════
// THEME SYNC
// ═════════════════════════════════════════════════════════════════

@Serializable
data class FabThemeSyncConfig(
    val syncWithAppTheme: Boolean = true,
    val syncGlowColor: Boolean = true,
    val syncGradientColors: Boolean = false,
    val syncBorderColor: Boolean = false,
    val reactiveToBrightness: Boolean = false, // Adjust opacity based on wallpaper brightness
    val reactiveToTime: Boolean = false        // Shift colors based on time of day
)

// ═════════════════════════════════════════════════════════════════
// MASTER ENHANCED FAB CONFIGURATION
// ═════════════════════════════════════════════════════════════════

@Serializable
data class EnhancedFabConfig(
    // Shape & Size
    val shape: FabShape = FabShape.CIRCLE,
    val size: FabSize = FabSize.NORMAL,
    val cornerRadius: FabCornerRadius = FabCornerRadius(),

    // Position
    val position: FabPositionConfig = FabPositionConfig(),

    // Visual appearance
    val gradient: FabGradientConfig = FabGradientConfig(),
    val border: FabBorderConfig = FabBorderConfig(),
    val shadow: FabShadowConfig = FabShadowConfig(),
    val glow: FabGlowConfig = FabGlowConfig(),

    // Icon
    val icon: FabIconConfig = FabIconConfig(),

    // Transparency
    val transparency: FabTransparencyConfig = FabTransparencyConfig(),

    // Press feel
    val pressEffect: FabPressEffectConfig = FabPressEffectConfig(),

    // Entrance / exit
    val entranceExit: FabEntranceExitConfig = FabEntranceExitConfig(),

    // Gestures
    val gestures: FabGestureConfig = FabGestureConfig(),

    // Haptics
    val haptics: FabHapticConfig = FabHapticConfig(),

    // Sound
    val sound: FabSoundConfig = FabSoundConfig(),

    // Particles on tap
    val tapParticles: FabClickParticleConfig = FabClickParticleConfig(),

    // Expanded menu
    val menu: FabMenuConfig = FabMenuConfig(),

    // Badge
    val badge: FabBadgeConfig = FabBadgeConfig(),

    // Notification badge
    val notificationBadge: FabNotificationBadgeConfig = FabNotificationBadgeConfig(),

    // Progress ring
    val progressRing: FabProgressRingConfig = FabProgressRingConfig(),

    // Auto-hide
    val autoHide: FabAutoHideConfig = FabAutoHideConfig(),

    // Backdrop blur when menu open
    val backdropBlur: FabBackdropBlurConfig = FabBackdropBlurConfig(),

    // Theme sync
    val themeSync: FabThemeSyncConfig = FabThemeSyncConfig(),

    // Legacy base config (kept for compatibility)
    val baseConfig: FabConfiguration = FabConfiguration()
) {
    companion object {
        val DEFAULT = EnhancedFabConfig()
    }
}

// ═════════════════════════════════════════════════════════════════
// ENHANCED PRESET CONFIGURATIONS
// ═════════════════════════════════════════════════════════════════

object EnhancedFabPresets {

    /** Neon cyberpunk look */
    val CYBERPUNK = EnhancedFabConfig(
        shape = FabShape.HEXAGON,
        size = FabSize.LARGE,
        gradient = FabGradientConfig(
            type = FabGradientType.ANIMATED_SWEEP,
            colors = listOf(0xFF00FFFF, 0xFFFF00FF, 0xFF00FF00)
        ),
        border = FabBorderConfig(
            style = FabBorderStyle.ANIMATED_SPIN,
            widthDp = 2f,
            color = 0xFF00FFFF,
            secondaryColor = 0xFFFF00FF
        ),
        shadow = FabShadowConfig(style = FabShadowStyle.NEON, color = 0x8800FFFF),
        glow = FabGlowConfig(
            color = 0xFF00FFFF,
            radiusDp = 24f,
            pulsing = true,
            colorCycling = true,
            colorCycleSpeedMs = 2000
        ),
        icon = FabIconConfig(
            style = FabIconStyle.ANIMATED_EMOJI,
            value = "⚡",
            animateOnIdle = true,
            idleAnimationType = FabIdleAnimation.SLOW_SPIN
        ),
        tapParticles = FabClickParticleConfig(
            type = FabClickParticleType.LIGHTNING_BOLTS,
            count = 8,
            spreadAngle = 360f
        ),
        sound = FabSoundConfig(onTap = FabSoundEffect.ZAP),
        haptics = FabHapticConfig(onTap = FabHapticType.HEAVY_CLICK),
        pressEffect = FabPressEffectConfig(type = FabPressEffect.GLOW_BURST),
        menu = FabMenuConfig(
            style = FabMenuStyle.RADIAL,
            itemAnimation = FabMenuItemAnimation.CATAPULT,
            backdropBlur = true,
            backdropDimAlpha = 0.4f
        ),
        baseConfig = FabConfigurationPresets.GAMING
    )

    /** Candy-sweet pink theme */
    val SUGAR_QUEEN = EnhancedFabConfig(
        shape = FabShape.BLOB,
        size = FabSize.LARGE,
        gradient = FabGradientConfig(
            type = FabGradientType.ANIMATED_LINEAR,
            colors = listOf(0xFFFF69B4, 0xFFFF1493, 0xFFFF85C2, 0xFFFFC0CB),
            angleDegrees = 135f
        ),
        border = FabBorderConfig(
            style = FabBorderStyle.RAINBOW,
            widthDp = 3f
        ),
        glow = FabGlowConfig(
            color = 0xFFFF69B4,
            radiusDp = 20f,
            pulsing = true,
            colorCycling = false
        ),
        icon = FabIconConfig(
            style = FabIconStyle.ANIMATED_EMOJI,
            value = "🍭",
            idleAnimationType = FabIdleAnimation.HEARTBEAT
        ),
        tapParticles = FabClickParticleConfig(
            type = FabClickParticleType.CANDY_PIECES,
            count = 15,
            spreadAngle = 360f,
            useThemeColors = true
        ),
        sound = FabSoundConfig(onTap = FabSoundEffect.CANDY_CRUNCH),
        haptics = FabHapticConfig(onTap = FabHapticType.CANDY_CRUNCH),
        pressEffect = FabPressEffectConfig(type = FabPressEffect.JELLY),
        menu = FabMenuConfig(
            style = FabMenuStyle.ARC_UP_LEFT,
            itemAnimation = FabMenuItemAnimation.SPRING
        ),
        badge = FabBadgeConfig(style = FabBadgeStyle.SPARKLE),
        baseConfig = FabConfigurationPresets.SUGAR_RUSH
    )

    /** Minimal professional look */
    val CLEAN_MINIMAL = EnhancedFabConfig(
        shape = FabShape.CIRCLE,
        size = FabSize.NORMAL,
        gradient = FabGradientConfig(
            type = FabGradientType.SOLID,
            colors = listOf(0xFF6750A4) // Material You primary
        ),
        border = FabBorderConfig(style = FabBorderStyle.NONE),
        glow = FabGlowConfig(enabled = false),
        icon = FabIconConfig(
            style = FabIconStyle.MATERIAL_ICON,
            value = "add",
            animateOnIdle = false
        ),
        tapParticles = FabClickParticleConfig(type = FabClickParticleType.NONE),
        sound = FabSoundConfig(onTap = FabSoundEffect.CLICK),
        haptics = FabHapticConfig(onTap = FabHapticType.LIGHT_CLICK),
        pressEffect = FabPressEffectConfig(type = FabPressEffect.SCALE_DOWN),
        menu = FabMenuConfig(
            style = FabMenuStyle.LINEAR_UP,
            showLabels = true,
            itemAnimation = FabMenuItemAnimation.SLIDE_FADE
        ),
        autoHide = FabAutoHideConfig(
            trigger = FabAutoHideTrigger.SCROLL_DOWN,
            inactivityTimeoutMs = 4000
        ),
        baseConfig = FabConfigurationPresets.MINIMAL
    )

    /** Cosy nature / outdoor theme */
    val NATURE_BLISS = EnhancedFabConfig(
        shape = FabShape.CLOUD,
        size = FabSize.LARGE,
        gradient = FabGradientConfig(
            type = FabGradientType.LINEAR,
            colors = listOf(0xFF56AB2F, 0xFFA8E063),
            angleDegrees = 45f
        ),
        border = FabBorderConfig(style = FabBorderStyle.GLOW, color = 0xFF56AB2F, widthDp = 2f),
        glow = FabGlowConfig(color = 0xFF56AB2F, radiusDp = 18f, pulsing = true),
        icon = FabIconConfig(
            style = FabIconStyle.ANIMATED_EMOJI,
            value = "🌿",
            idleAnimationType = FabIdleAnimation.GENTLE_PULSE
        ),
        tapParticles = FabClickParticleConfig(
            type = FabClickParticleType.PETALS,
            count = 10,
            spreadAngle = 180f
        ),
        sound = FabSoundConfig(onTap = FabSoundEffect.CHIME),
        haptics = FabHapticConfig(onTap = FabHapticType.LIGHT_CLICK),
        pressEffect = FabPressEffectConfig(type = FabPressEffect.BOUNCE),
        menu = FabMenuConfig(
            style = FabMenuStyle.CIRCULAR,
            itemAnimation = FabMenuItemAnimation.BOUNCE_IN
        ),
        baseConfig = FabConfigurationPresets.CHILL_MODE
    )

    /** Wild party mode */
    val PARTY_ANIMAL = EnhancedFabConfig(
        shape = FabShape.BURST,
        size = FabSize.XL,
        gradient = FabGradientConfig(
            type = FabGradientType.HOLOGRAPHIC,
            colors = listOf(0xFFFF00FF, 0xFF00FFFF, 0xFFFFFF00, 0xFF00FF00)
        ),
        border = FabBorderConfig(
            style = FabBorderStyle.ANIMATED_SPIN,
            widthDp = 4f,
            color = 0xFFFFFF00,
            secondaryColor = 0xFFFF00FF,
            animationSpeedMultiplier = 2f
        ),
        glow = FabGlowConfig(
            color = 0xFFFF00FF,
            radiusDp = 32f,
            pulsing = true,
            colorCycling = true,
            colorCycleSpeedMs = 500
        ),
        icon = FabIconConfig(
            style = FabIconStyle.ANIMATED_EMOJI,
            value = "🎉",
            idleAnimationType = FabIdleAnimation.TADA
        ),
        tapParticles = FabClickParticleConfig(
            type = FabClickParticleType.CONFETTI,
            count = 30,
            spreadAngle = 360f,
            minSpeed = 5f,
            maxSpeed = 15f
        ),
        sound = FabSoundConfig(onTap = FabSoundEffect.POWER_UP),
        haptics = FabHapticConfig(onTap = FabHapticType.RHYTHM_PULSE),
        pressEffect = FabPressEffectConfig(type = FabPressEffect.EXPLODE),
        menu = FabMenuConfig(
            style = FabMenuStyle.CIRCULAR,
            itemAnimation = FabMenuItemAnimation.CASCADE,
            backdropBlur = true,
            backdropDimAlpha = 0.3f,
            openAnimationMs = 200
        ),
        badge = FabBadgeConfig(
            style = FabBadgeStyle.PULSE_DOT,
            color = 0xFFFFFF00,
            pulsing = true
        ),
        progressRing = FabProgressRingConfig(
            style = FabProgressRingStyle.INDETERMINATE,
            color = 0xFFFFFF00,
            animatedColor = true
        ),
        baseConfig = FabConfigurationPresets.PARTY_MODE
    )

    /** Romantic pink heart theme */
    val ROMANTIC_HEARTS = EnhancedFabConfig(
        shape = FabShape.HEART,
        size = FabSize.LARGE,
        gradient = FabGradientConfig(
            type = FabGradientType.RADIAL,
            colors = listOf(0xFFFF69B4, 0xFFFF1493, 0xFFAD1457)
        ),
        border = FabBorderConfig(
            style = FabBorderStyle.GLOW,
            color = 0xFFFF69B4,
            widthDp = 2f
        ),
        glow = FabGlowConfig(
            color = 0xFFFF69B4,
            radiusDp = 22f,
            pulsing = true,
            pulseSpeedMs = 800
        ),
        icon = FabIconConfig(
            style = FabIconStyle.ANIMATED_EMOJI,
            value = "💝",
            idleAnimationType = FabIdleAnimation.HEARTBEAT
        ),
        tapParticles = FabClickParticleConfig(
            type = FabClickParticleType.HEARTS,
            count = 12,
            spreadAngle = 360f,
            maxSpeed = 6f
        ),
        sound = FabSoundConfig(onTap = FabSoundEffect.BUBBLE),
        haptics = FabHapticConfig(
            onTap = FabHapticType.JELLY_BOUNCE,
            intensity = 0.7f
        ),
        pressEffect = FabPressEffectConfig(
            type = FabPressEffect.JELLY,
            scaleMax = 1.15f,
            durationMs = 200
        ),
        menu = FabMenuConfig(
            style = FabMenuStyle.ARC_UP_RIGHT,
            itemAnimation = FabMenuItemAnimation.SPRING
        ),
        entranceExit = FabEntranceExitConfig(
            entrance = FabEntranceAnimation.CANDY_DROP,
            exit = FabExitAnimation.BURST_DISSOLVE
        ),
        baseConfig = FabConfigurationPresets.ROMANTIC
    )

    /** Space / galaxy explorer */
    val GALAXY_EXPLORER = EnhancedFabConfig(
        shape = FabShape.CIRCLE,
        size = FabSize.LARGE,
        gradient = FabGradientConfig(
            type = FabGradientType.ANIMATED_SWEEP,
            colors = listOf(0xFF0D0D2B, 0xFF2E004F, 0xFF6A0DAD, 0xFF1B2A6B)
        ),
        border = FabBorderConfig(
            style = FabBorderStyle.ANIMATED_SPIN,
            color = 0xFF6A0DAD,
            secondaryColor = 0xFFE8C5F5,
            widthDp = 2f,
            animationSpeedMultiplier = 0.5f
        ),
        glow = FabGlowConfig(
            color = 0xFF6A0DAD,
            radiusDp = 28f,
            pulsing = true,
            pulseSpeedMs = 2000,
            colorCycling = false
        ),
        shadow = FabShadowConfig(
            style = FabShadowStyle.COLORED,
            color = 0x886A0DAD,
            elevationDp = 12f
        ),
        icon = FabIconConfig(
            style = FabIconStyle.ANIMATED_EMOJI,
            value = "🚀",
            idleAnimationType = FabIdleAnimation.SWING
        ),
        tapParticles = FabClickParticleConfig(
            type = FabClickParticleType.STARS,
            count = 20,
            spreadAngle = 360f,
            useThemeColors = false,
            customColors = listOf(0xFFFFFFFF, 0xFFF5D78E, 0xFFE8C5F5)
        ),
        sound = FabSoundConfig(onTap = FabSoundEffect.WHOOSH),
        haptics = FabHapticConfig(onTap = FabHapticType.MEDIUM_CLICK),
        pressEffect = FabPressEffectConfig(type = FabPressEffect.RUBBER_BAND),
        menu = FabMenuConfig(
            style = FabMenuStyle.RADIAL,
            arcAngleRange = 150f,
            arcStartAngle = 225f,
            itemAnimation = FabMenuItemAnimation.ROTATE_IN,
            backdropBlur = true,
            backdropDimAlpha = 0.5f
        ),
        progressRing = FabProgressRingConfig(
            style = FabProgressRingStyle.INDETERMINATE,
            color = 0xFFE8C5F5,
            strokeWidthDp = 2f
        ),
        themeSync = FabThemeSyncConfig(
            syncWithAppTheme = false,
            reactiveToTime = true
        )
    )

    val ALL_PRESETS = listOf(
        "cyberpunk" to CYBERPUNK,
        "sugar_queen" to SUGAR_QUEEN,
        "clean_minimal" to CLEAN_MINIMAL,
        "nature_bliss" to NATURE_BLISS,
        "party_animal" to PARTY_ANIMAL,
        "romantic_hearts" to ROMANTIC_HEARTS,
        "galaxy_explorer" to GALAXY_EXPLORER
    )

    fun getById(id: String): EnhancedFabConfig? =
        ALL_PRESETS.find { it.first == id }?.second
}
