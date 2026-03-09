package com.sugarmunch.app.ui.customization

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlinx.serialization.Serializable

/**
 * EXTREME Customization Models for SugarMunch
 * Comprehensive data models for all customization options
 */

// ═══════════════════════════════════════════════════════════════
// BACKGROUND CONFIGURATION
// ═══════════════════════════════════════════════════════════════

@Serializable
data class BackgroundConfig(
    val type: BackgroundType = BackgroundType.STATIC,
    val staticConfig: StaticBackgroundConfig = StaticBackgroundConfig(),
    val animatedConfig: AnimatedBackgroundConfig = AnimatedBackgroundConfig(),
    val reactiveConfig: ReactiveBackgroundConfig = ReactiveBackgroundConfig(),
    val interactiveConfig: InteractiveBackgroundConfig = InteractiveBackgroundConfig()
)

@Serializable
enum class BackgroundType {
    STATIC,
    ANIMATED,
    REACTIVE,
    INTERACTIVE
}

@Serializable
data class StaticBackgroundConfig(
    val solidColor: String = "#FF1A1A2E",
    val gradientType: GradientType = GradientType.VERTICAL,
    val gradientColors: List<String> = listOf("#FF1A1A2E", "#FF2D2D44"),
    val gradientAngle: Float = 0f,
    val gradientSpread: Float = 1f,
    val imageUri: String? = null,
    val imageBlur: Float = 0f,
    val imageOpacity: Float = 1f,
    val imageScale: Float = 1f,
    val patternId: String? = null
)

@Serializable
enum class GradientType {
    VERTICAL,
    HORIZONTAL,
    DIAGONAL,
    RADIAL,
    SWEEP
}

@Serializable
data class AnimatedBackgroundConfig(
    val particleDensity: Int = 50,
    val particleSpeed: Float = 1f,
    val particleSize: Float = 8f,
    val particleType: ParticleType = ParticleType.CIRCLES,
    val animationSpeed: Float = 1f,
    val meshComplexity: MeshComplexity = MeshComplexity.MEDIUM,
    val waveAmplitude: Float = 30f,
    val waveFrequency: Float = 0.01f
)

@Serializable
enum class ParticleType {
    CIRCLES,
    STARS,
    HEARTS,
    DIAMONDS,
    SQUARES,
    TRIANGLES
}

@Serializable
enum class MeshComplexity {
    LOW,
    MEDIUM,
    HIGH,
    ULTRA
}

@Serializable
data class ReactiveBackgroundConfig(
    val weatherSensitivity: Float = 0.5f,
    val musicBeatThreshold: Float = 0.7f,
    val timeTransitionSpeed: Float = 1f,
    val batteryTriggers: List<Int> = listOf(20, 50, 80),
    val chargingStateEffects: Boolean = true,
    val enableWeatherReactive: Boolean = true,
    val enableMusicReactive: Boolean = true,
    val enableTimeReactive: Boolean = true
)

@Serializable
data class InteractiveBackgroundConfig(
    val touchRippleSize: Float = 100f,
    val touchRippleIntensity: Float = 0.5f,
    val gyroscopeSensitivity: Float = 0.5f,
    val tiltResponsiveness: Float = 0.5f,
    val pinchToZoom: Boolean = true,
    val enableTouchRipple: Boolean = true,
    val enableGyroscope: Boolean = true,
    val enableTilt: Boolean = true
)

// ═══════════════════════════════════════════════════════════════
// COLOR PROFILE
// ═══════════════════════════════════════════════════════════════

@Serializable
data class ColorProfile(
    val mode: ColorPickerMode = ColorPickerMode.RGB,
    val primaryColor: String = "#FFFF69B4",
    val secondaryColor: String = "#FF00FFA3",
    val accentColor: String = "#FFFFD700",
    val surfaceColors: SurfaceColors = SurfaceColors(),
    val statusBarColor: String = "#FF1A1A2E",
    val navigationBarColor: String = "#FF0F0F1E",
    val dividerColor: String = "#33FFFFFF",
    val scrollbarColor: String = "#66FFFFFF",
    val selectionHighlightColor: String = "#4000BFFF",
    val focusIndicatorColor: String = "#FFFFD700",
    val colorTemperature: Float = 5000f,
    val saturationCurve: Float = 1f,
    val brightnessCurve: Float = 1f,
    val contrastLevel: ContrastLevel = ContrastLevel.MEDIUM,
    val colorScheme: ColorScheme = ColorScheme.CUSTOM
)

@Serializable
enum class ColorPickerMode {
    RGB,
    HSV,
    HSL,
    HEX
}

@Serializable
data class SurfaceColors(
    val card: String = "#FF2D2D44",
    val dialog: String = "#FF3D3D5C",
    val bottomSheet: String = "#FF2D2D44",
    val surface: String = "#FF1A1A2E",
    val background: String = "#FF0F0F1E"
)

@Serializable
enum class ContrastLevel {
    LOW,
    MEDIUM_LOW,
    MEDIUM,
    MEDIUM_HIGH,
    HIGH
}

@Serializable
enum class ColorScheme {
    CUSTOM,
    COMPLEMENTARY,
    ANALOGOUS,
    TRIADIC,
    TETRADIC,
    SPLIT_COMPLEMENTARY,
    MONOCHROMATIC
}

// ═══════════════════════════════════════════════════════════════
// ANIMATION PROFILE
// ═══════════════════════════════════════════════════════════════

@Serializable
data class AnimationProfile(
    val masterEnabled: Boolean = true,
    val reduceMotion: Boolean = false,
    val powerSaveMode: Boolean = false,
    val componentAnimations: Map<String, ComponentAnimationConfig> = emptyMap(),
    val animationCurve: AnimationCurve = AnimationCurve.FAST_OUT_SLOW_IN,
    val customBezier: CustomBezier = CustomBezier(),
    val staggerPattern: StaggerPattern = StaggerPattern.FORWARD,
    val qualityPreset: QualityPreset = QualityPreset.HIGH,
    val frameRateCap: FrameRateCap = FrameRateCap.FPS_60
)

@Serializable
data class ComponentAnimationConfig(
    val enabled: Boolean = true,
    val type: AnimationType = AnimationType.FADE,
    val duration: Int = 300,
    val direction: AnimationDirection = AnimationDirection.CENTER,
    val scaleOrigin: ScaleOrigin = ScaleOrigin.CENTER,
    val delay: Int = 0
)

@Serializable
enum class AnimationType {
    NONE,
    FADE,
    SLIDE,
    SCALE,
    ROTATE,
    BOUNCE,
    ELASTIC,
    FLIP,
    TYPEWRITER,
    WAVE
}

@Serializable
enum class AnimationDirection {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT,
    CENTER,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}

@Serializable
enum class ScaleOrigin {
    CENTER,
    TOP,
    BOTTOM,
    LEFT,
    RIGHT,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}

@Serializable
enum class AnimationCurve {
    LINEAR,
    EASE_IN,
    EASE_OUT,
    EASE_IN_OUT,
    BOUNCE,
    ELASTIC,
    SPRING,
    CUSTOM
}

@Serializable
data class CustomBezier(
    val x1: Float = 0.25f,
    val y1: Float = 0.1f,
    val x2: Float = 0.25f,
    val y2: Float = 1f
)

@Serializable
enum class StaggerPattern {
    FORWARD,
    BACKWARD,
    CENTER_OUT,
    RANDOM,
    DOMINO,
    WAVE,
    SPIRAL
}

@Serializable
enum class QualityPreset {
    ULTRA,
    HIGH,
    MEDIUM,
    LOW,
    POWER_SAVER,
    ACCESSIBILITY
}

@Serializable
enum class FrameRateCap {
    FPS_30,
    FPS_60,
    FPS_90,
    FPS_120,
    UNLIMITED
}

// ═══════════════════════════════════════════════════════════════
// GESTURE MAPPING
// ═══════════════════════════════════════════════════════════════

@Serializable
data class GestureMapping(
    val singleTap: GestureAction = GestureAction.NONE,
    val doubleTap: GestureAction = GestureAction.NONE,
    val longPressDuration: Int = 1000,
    val longPress: GestureAction = GestureAction.NONE,
    val swipeUp: GestureAction = GestureAction.NONE,
    val swipeDown: GestureAction = GestureAction.NONE,
    val swipeLeft: GestureAction = GestureAction.NONE,
    val swipeRight: GestureAction = GestureAction.NONE,
    val pinchIn: GestureAction = GestureAction.NONE,
    val pinchOut: GestureAction = GestureAction.NONE,
    val twoFingerSwipe: GestureAction = GestureAction.NONE,
    val circleGesture: GestureAction = GestureAction.NONE,
    val figure8Gesture: GestureAction = GestureAction.NONE,
    val swipeDistanceThreshold: Float = 50f,
    val velocityThreshold: VelocityThreshold = VelocityThreshold.MEDIUM,
    val angleTolerance: AngleTolerance = AngleTolerance.DEGREES_30,
    val multiTouchTimeout: Int = 200
)

@Serializable
enum class GestureAction {
    NONE,
    BACK,
    HOME,
    RECENTS,
    SEARCH,
    SETTINGS,
    NOTIFICATIONS,
    QUICK_SETTINGS,
    APP_DRAWER,
    CUSTOM_APP,
    THEME_SWITCH,
    ANIMATION_TOGGLE,
    EFFECT_TOGGLE,
    SCREENSHOT,
    RECORD_SCREEN,
    FLASHLIGHT,
    VOLUME_PANEL,
    POWER_MENU,
    LOCK_SCREEN,
    SPLIT_SCREEN
}

@Serializable
enum class VelocityThreshold {
    SLOW,
    MEDIUM,
    FAST
}

@Serializable
enum class AngleTolerance {
    DEGREES_15,
    DEGREES_30,
    DEGREES_45
}

// ═══════════════════════════════════════════════════════════════
// HAPTIC PATTERN
// ═══════════════════════════════════════════════════════════════

@Serializable
data class HapticPattern(
    val buttonIntensity: Float = 0.5f,
    val sliderIntensity: Float = 0.5f,
    val scrollingIntensity: Float = 0.3f,
    val gestureIntensity: Float = 0.5f,
    val notificationIntensity: Float = 0.7f,
    val systemEventIntensity: Float = 0.5f,
    val clickPattern: HapticPreset = HapticPreset.MEDIUM,
    val successPattern: HapticPreset = HapticPreset.SUCCESS,
    val errorPattern: HapticPreset = HapticPreset.ERROR,
    val warningPattern: HapticPreset = HapticPreset.WARNING,
    val customPattern: List<HapticSegment> = emptyList(),
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00",
    val meetingMode: Boolean = false,
    val nightMode: Boolean = false
)

@Serializable
enum class HapticPreset {
    NONE,
    LIGHT,
    MEDIUM,
    HEAVY,
    SUCCESS,
    ERROR,
    WARNING,
    CUSTOM
}

@Serializable
data class HapticSegment(
    val duration: Int = 50,
    val amplitude: Int = 128,
    val delay: Int = 0
)

@Serializable
data class TouchFeedbackConfig(
    val visualIndicator: TouchIndicatorType = TouchIndicatorType.RIPPLE,
    val indicatorSize: Float = 50f,
    val indicatorColor: String = "#40FFFFFF",
    val indicatorDuration: Int = 300,
    val showForAllTouches: Boolean = false
)

@Serializable
enum class TouchIndicatorType {
    NONE,
    CIRCLE,
    RIPPLE,
    GLOW,
    SPARKLE
}

// ═══════════════════════════════════════════════════════════════
// LAYOUT CONFIG
// ═══════════════════════════════════════════════════════════════

@Serializable
data class LayoutConfig(
    val columns: Int = 4,
    val rows: Int = 6,
    val gridType: GridType = GridType.SQUARE,
    val itemAspectRatio: AspectRatio = AspectRatio.RATIO_1_1,
    val gridGaps: Dp = 8.dp,
    val sectionMargins: Dp = 16.dp,
    val interItemSpacingHorizontal: Dp = 8.dp,
    val interItemSpacingVertical: Dp = 8.dp,
    val sectionSpacing: Dp = 24.dp,
    val contentPaddingLeft: Dp = 16.dp,
    val contentPaddingRight: Dp = 16.dp,
    val contentPaddingTop: Dp = 16.dp,
    val contentPaddingBottom: Dp = 16.dp,
    val horizontalAlignment: HorizontalAlignment = HorizontalAlignment.CENTER,
    val verticalAlignment: VerticalAlignment = VerticalAlignment.CENTER,
    val contentDistribution: ContentDistribution = ContentDistribution.START,
    val itemOrdering: ItemOrdering = ItemOrdering.NORMAL
)

@Serializable
enum class GridType {
    SQUARE,
    RECTANGULAR,
    MASONRY,
    WATERFALL
}

@Serializable
enum class AspectRatio {
    RATIO_1_1,
    RATIO_4_3,
    RATIO_16_9,
    CUSTOM
}

@Serializable
enum class HorizontalAlignment {
    LEFT,
    CENTER,
    RIGHT,
    JUSTIFY
}

@Serializable
enum class VerticalAlignment {
    TOP,
    CENTER,
    BOTTOM,
    BASELINE
}

@Serializable
enum class ContentDistribution {
    START,
    CENTER,
    END,
    SPACE_BETWEEN,
    SPACE_AROUND,
    SPACE_EVENLY
}

@Serializable
enum class ItemOrdering {
    NORMAL,
    REVERSE,
    RANDOM,
    CUSTOM
}

// ═══════════════════════════════════════════════════════════════
// NAVIGATION CONFIG
// ═══════════════════════════════════════════════════════════════

@Serializable
data class NavigationConfig(
    val style: NavigationStyle = NavigationStyle.BOTTOM_BAR,
    val height: Dp = 64.dp,
    val width: Dp = 64.dp,
    val backgroundType: NavigationBackground = NavigationBackground.SOLID,
    val backgroundColor: String = "#FF1A1A2E",
    val gradientColors: List<String> = listOf("#FF1A1A2E", "#FF2D2D44"),
    val elevation: Dp = 8.dp,
    val cornerRadius: Dp = 0.dp,
    val iconSize: Dp = 24.dp,
    val labelPosition: LabelPosition = LabelPosition.BOTTOM,
    val labelSize: Float = 12f,
    val activeIndicator: ActiveIndicatorType = ActiveIndicatorType.PILL,
    val autoHideOnScroll: Boolean = false,
    val shrinkOnScroll: Boolean = false,
    val transformToFAB: Boolean = false,
    val badgeStyle: BadgeStyle = BadgeStyle.DOT,
    val badgePosition: BadgePosition = BadgePosition.TOP_RIGHT,
    val transitionAnimation: NavigationTransition = NavigationTransition.FADE
)

@Serializable
enum class NavigationStyle {
    BOTTOM_BAR,
    RAIL_LEFT,
    RAIL_RIGHT,
    DRAWER_LEFT,
    DRAWER_RIGHT,
    TOP_TABS,
    GESTURE_ONLY,
    FAB,
    PIE_MENU
}

@Serializable
enum class NavigationBackground {
    SOLID,
    GRADIENT,
    GLASS,
    BLUR,
    TRANSPARENT
}

@Serializable
enum class LabelPosition {
    BOTTOM,
    SIDE,
    INLINE,
    NONE
}

@Serializable
enum class ActiveIndicatorType {
    NONE,
    PILL,
    CIRCLE,
    UNDERLINE,
    GLOW,
    SCALE
}

@Serializable
enum class BadgeStyle {
    NONE,
    DOT,
    NUMBER,
    CUSTOM
}

@Serializable
enum class BadgePosition {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_RIGHT,
    BOTTOM_LEFT,
    CENTER
}

@Serializable
enum class NavigationTransition {
    NONE,
    FADE,
    SLIDE,
    SCALE,
    SLIDE_FADE,
    EXPLODE
}

// ═══════════════════════════════════════════════════════════════
// CARD STYLE CONFIG
// ═══════════════════════════════════════════════════════════════

@Serializable
data class CardStyleConfig(
    val shape: CardShape = CardShape.ROUNDED,
    val customShapePath: String? = null,
    val borderType: BorderType = BorderType.NONE,
    val borderWidth: Dp = 0.dp,
    val borderColor: String = "#00FFFFFF",
    val borderGradientColors: List<String> = emptyList(),
    val shadowType: ShadowType = ShadowType.SUBTLE,
    val shadowColor: String = "#20000000",
    val shadowBlur: Dp = 8.dp,
    val shadowOffsetX: Dp = 0.dp,
    val shadowOffsetY: Dp = 4.dp,
    val elevation: Dp = 4.dp,
    val surfaceType: CardSurface = CardSurface.SOLID,
    val surfaceColor: String = "#FF2D2D44",
    val surfaceGradientColors: List<String> = emptyList(),
    val imagePosition: ContentPosition = ContentPosition.TOP,
    val titlePosition: ContentPosition = ContentPosition.TOP,
    val subtitlePosition: ContentPosition = ContentPosition.TOP,
    val actionButtonsPosition: ActionPosition = ActionPosition.BOTTOM,
    val metadataDisplay: MetadataDisplay = MetadataDisplay.ALWAYS,
    val contentPadding: ContentPadding = ContentPadding.COMFORTABLE,
    val interactiveStates: InteractiveStatesConfig = InteractiveStatesConfig()
)

@Serializable
enum class CardShape {
    RECTANGLE,
    ROUNDED,
    CIRCLE,
    SQUIRCLE,
    HEXAGON,
    OCTAGON,
    CUSTOM
}

@Serializable
enum class BorderType {
    NONE,
    SOLID,
    DASHED,
    DOTTED,
    GRADIENT
}

@Serializable
enum class ShadowType {
    NONE,
    SUBTLE,
    MEDIUM,
    HEAVY,
    CUSTOM
}

@Serializable
enum class CardSurface {
    SOLID,
    GRADIENT,
    GLASS,
    FROSTED,
    IMAGE,
    VIDEO
}

@Serializable
enum class ContentPosition {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT,
    BACKGROUND,
    OVERLAY
}

@Serializable
enum class ActionPosition {
    TOP,
    BOTTOM,
    SIDE,
    OVERLAY
}

@Serializable
enum class MetadataDisplay {
    ALWAYS,
    ON_HOVER,
    ON_PRESS,
    NEVER
}

@Serializable
enum class ContentPadding {
    COMPACT,
    COMFORTABLE,
    SPACIOUS
}

@Serializable
data class InteractiveStatesConfig(
    val defaultConfig: StateConfig = StateConfig(),
    val hoverConfig: StateConfig = StateConfig(),
    val pressedConfig: StateConfig = StateConfig(),
    val selectedConfig: StateConfig = StateConfig(),
    val disabledConfig: StateConfig = StateConfig(),
    val focusedConfig: StateConfig = StateConfig()
)

@Serializable
data class StateConfig(
    val backgroundColor: String? = null,
    val borderColor: String? = null,
    val shadowColor: String? = null,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val alpha: Float = 1f
)

// ═══════════════════════════════════════════════════════════════
// TYPOGRAPHY CONFIG
// ═══════════════════════════════════════════════════════════════

@Serializable
data class TypographyConfig(
    val headingFont: String = "default",
    val bodyFont: String = "default",
    val captionFont: String = "default",
    val buttonFont: String = "default",
    val defaultFontWeight: Int = 400,
    val defaultFontStyle: FontStyle = FontStyle.NORMAL,
    val globalScale: Float = 1f,
    val titleScale: Float = 1f,
    val bodyScale: Float = 1f,
    val captionScale: Float = 1f,
    val buttonScale: Float = 1f,
    val lineHeightMultiplier: Float = 1.2f,
    val letterSpacing: Float = 0f,
    val wordSpacing: Float = 0f,
    val allCapsHeadings: Boolean = false,
    val smallCaps: Boolean = false,
    val underlineStyle: UnderlineStyle = UnderlineStyle.NONE,
    val strikethroughStyle: StrikethroughStyle = StrikethroughStyle.NONE,
    val textShadow: TextShadowConfig? = null,
    val textStroke: TextStrokeConfig? = null,
    val gradientText: Boolean = false,
    val gradientTextColors: List<String> = emptyList(),
    val animatedText: TextAnimation = TextAnimation.NONE,
    val dyslexiaFriendlyFont: Boolean = false,
    val increasedLetterSpacing: Boolean = false,
    val increasedWordSpacing: Boolean = false,
    val paragraphSpacing: Dp = 16.dp,
    val hyphenation: Boolean = false,
    val textJustification: TextJustification = TextJustification.LEFT
)

@Serializable
enum class FontStyle {
    NORMAL,
    ITALIC,
    OBLIQUE
}

@Serializable
enum class UnderlineStyle {
    NONE,
    SOLID,
    DASHED,
    DOTTED,
    WAVY,
    DOUBLE
}

@Serializable
enum class StrikethroughStyle {
    NONE,
    SOLID,
    DASHED,
    DOTTED,
    DOUBLE
}

@Serializable
data class TextShadowConfig(
    val color: String = "#40000000",
    val blur: Dp = 4.dp,
    val offsetX: Dp = 2.dp,
    val offsetY: Dp = 2.dp
)

@Serializable
data class TextStrokeConfig(
    val color: String = "#FF000000",
    val width: Dp = 1.dp
)

@Serializable
enum class TextAnimation {
    NONE,
    TYPEWRITER,
    WAVE,
    FADE_IN,
    SLIDE_IN,
    SCALE_IN,
    ROTATE_IN
}

@Serializable
enum class TextJustification {
    LEFT,
    RIGHT,
    CENTER,
    JUSTIFIED
}

// ═══════════════════════════════════════════════════════════════
// EFFECT CONFIG
// ═══════════════════════════════════════════════════════════════

@Serializable
data class EffectConfig(
    val enabled: Boolean = true,
    val intensity: Float = 1f,
    val duration: Float = 1f,
    val size: Float = 1f,
    val speed: Float = 1f,
    val density: Float = 1f,
    val colorOverride: String? = null,
    val opacity: Float = 1f,
    val blendMode: BlendMode = BlendMode.NORMAL
)

@Serializable
enum class BlendMode {
    NORMAL,
    MULTIPLY,
    SCREEN,
    OVERLAY,
    DARKEN,
    LIGHTEN,
    COLOR_DODGE,
    COLOR_BURN,
    HARD_LIGHT,
    SOFT_LIGHT,
    DIFFERENCE,
    EXCLUSION,
    HUE,
    SATURATION,
    COLOR,
    LUMINOSITY
}

// ═══════════════════════════════════════════════════════════════
// PARTICLE CONFIG
// ═══════════════════════════════════════════════════════════════

@Serializable
data class ParticleConfig(
    val gravity: Float = 1f,
    val gravityDirection: Float = 90f,
    val wind: Float = 0f,
    val windDirection: Float = 0f,
    val friction: Float = 0.1f,
    val bounce: Float = 0.5f,
    val collisionDetection: Boolean = false,
    val particleLifetime: Float = 10f,
    val spawnRate: Int = 50,
    val shapeLibrary: List<String> = emptyList(),
    val customShapeUri: String? = null,
    val sizeMin: Float = 4f,
    val sizeMax: Float = 12f,
    val colorStart: String = "#FFFF69B4",
    val colorEnd: String = "#FF00FFA3",
    val opacityOverLifetime: List<Float> = listOf(1f, 0f),
    val sizeOverLifetime: List<Float> = listOf(1f, 1f),
    val rotationOverLifetime: List<Float> = listOf(0f, 360f),
    val spinSpeedMin: Float = -10f,
    val spinSpeedMax: Float = 10f,
    val spawnPosition: SpawnPosition = SpawnPosition.RANDOM,
    val initialVelocity: Float = 50f,
    val spawnAngle: Float = 0f,
    val spawnSpread: Float = 360f,
    val acceleration: Float = 0f,
    val followTouch: Boolean = false,
    val followTouchStrength: Float = 0.5f,
    val followMusic: Boolean = false,
    val followMusicSensitivity: Float = 0.5f,
    val avoidObstacles: Boolean = false,
    val attractToPoints: Boolean = false,
    val attractStrength: Float = 0.5f,
    val attractPoints: List<AttractPoint> = emptyList()
)

@Serializable
enum class SpawnPosition {
    EDGES,
    CENTER,
    RANDOM,
    CUSTOM,
    TOP,
    BOTTOM,
    LEFT,
    RIGHT
}

@Serializable
data class AttractPoint(
    val x: Float,
    val y: Float,
    val strength: Float,
    val radius: Float
)

// ═══════════════════════════════════════════════════════════════
// USER PROFILE
// ═══════════════════════════════════════════════════════════════

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val category: ProfileCategory = ProfileCategory.CUSTOM,
    val iconId: String = "default",
    val iconColor: String = "#FFFF69B4",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis(),
    val usageCount: Int = 0,
    val totalUsageTime: Long = 0,
    val backgroundConfig: BackgroundConfig = BackgroundConfig(),
    val colorProfile: ColorProfile = ColorProfile(),
    val animationProfile: AnimationProfile = AnimationProfile(),
    val gestureMapping: GestureMapping = GestureMapping(),
    val hapticPattern: HapticPattern = HapticPattern(),
    val layoutConfig: LayoutConfig = LayoutConfig(),
    val navigationConfig: NavigationConfig = NavigationConfig(),
    val cardStyleConfig: CardStyleConfig = CardStyleConfig(),
    val typographyConfig: TypographyConfig = TypographyConfig(),
    val effectConfigs: Map<String, EffectConfig> = emptyMap(),
    val particleConfig: ParticleConfig = ParticleConfig(),
    val isSystemProfile: Boolean = false,
    val isFavorite: Boolean = false
)

@Serializable
enum class ProfileCategory {
    WORK,
    HOME,
    GAMING,
    READING,
    NIGHT,
    PRODUCTIVITY,
    ENTERTAINMENT,
    CUSTOM
}

// ═══════════════════════════════════════════════════════════════
// PRESET CONFIG
// ═══════════════════════════════════════════════════════════════

@Serializable
data class PresetConfig(
    val id: String,
    val name: String,
    val description: String = "",
    val category: PresetCategory = PresetCategory.CUSTOM,
    val contextTriggers: List<ContextTrigger> = emptyList(),
    val backgroundConfig: BackgroundConfig? = null,
    val colorProfile: ColorProfile? = null,
    val animationProfile: AnimationProfile? = null,
    val effectConfigs: Map<String, EffectConfig>? = null,
    val particleConfig: ParticleConfig? = null,
    val isAutoGenerated: Boolean = false,
    val usageCount: Int = 0,
    val lastUsedAt: Long? = null,
    val rating: Float = 0f,
    val creatorId: String? = null
)

@Serializable
enum class PresetCategory {
    MORNING,
    EVENING,
    NIGHT,
    READING,
    GAMING,
    BATTERY_SAVER,
    PRODUCTIVITY,
    CUSTOM
}

@Serializable
data class ContextTrigger(
    val type: ContextType,
    val value: String,
    val action: ContextAction
)

@Serializable
enum class ContextType {
    TIME,
    LOCATION,
    BATTERY,
    APP,
    WIFI,
    BLUETOOTH,
    CHARGING,
    DO_NOT_DISTURB
}

@Serializable
enum class ContextAction {
    ACTIVATE,
    DEACTIVATE,
    TOGGLE
}

// ═══════════════════════════════════════════════════════════════
// BACKUP DATA
// ═══════════════════════════════════════════════════════════════

@Serializable
data class BackupData(
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val backupType: BackupType = BackupType.FULL,
    val includedCategories: List<BackupCategory> = BackupCategory.entries,
    val backgroundConfig: BackgroundConfig? = null,
    val colorProfile: ColorProfile? = null,
    val animationProfile: AnimationProfile? = null,
    val gestureMapping: GestureMapping? = null,
    val hapticPattern: HapticPattern? = null,
    val layoutConfig: LayoutConfig? = null,
    val navigationConfig: NavigationConfig? = null,
    val cardStyleConfig: CardStyleConfig? = null,
    val typographyConfig: TypographyConfig? = null,
    val effectConfigs: Map<String, EffectConfig>? = null,
    val particleConfig: ParticleConfig? = null,
    val profiles: List<UserProfile> = emptyList(),
    val presets: List<PresetConfig> = emptyList(),
    val checksum: String = ""
)

@Serializable
enum class BackupType {
    FULL,
    PARTIAL,
    INCREMENTAL
}

@Serializable
enum class BackupCategory {
    BACKGROUNDS,
    COLORS,
    ANIMATIONS,
    GESTURES,
    HAPTICS,
    LAYOUTS,
    NAVIGATION,
    CARDS,
    TYPOGRAPHY,
    EFFECTS,
    PARTICLES,
    PROFILES,
    PRESETS
}

// ═══════════════════════════════════════════════════════════════
// EXPERIMENTAL FLAGS
// ═══════════════════════════════════════════════════════════════

@Serializable
data class ExperimentalFlags(
    val nextGenParticleEngine: Boolean = false,
    val aiPoweredThemeGenerator: Boolean = false,
    val voiceControlledCustomization: Boolean = false,
    val arPreviewMode: Boolean = false,
    val gestureLearning: Boolean = false,
    val predictiveAnimations: Boolean = false,
    val advancedHaptics: Boolean = false,
    val neuralThemeAdaptation: Boolean = false,
    val fpsCounterOverlay: Boolean = false,
    val gpuUsageOverlay: Boolean = false,
    val memoryUsageOverlay: Boolean = false,
    val animationTimelineViewer: Boolean = false,
    val touchEventVisualizer: Boolean = false,
    val networkActivityMonitor: Boolean = false,
    val batteryDrainAnalyzer: Boolean = false
)
