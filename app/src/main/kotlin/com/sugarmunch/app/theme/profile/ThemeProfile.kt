package com.sugarmunch.app.theme.profile

import com.sugarmunch.app.theme.layers.BlendMode
import com.sugarmunch.app.theme.model.ParticleType
import com.sugarmunch.app.theme.model.ThemeCategory
import com.sugarmunch.app.ui.typography.TypeScale

enum class ThemeSchemaVersion(val value: Int) {
    V1(1)
}

enum class ThemeBackgroundKind {
    GRADIENT,
    MESH,
    SOLID
}

enum class ThemeFontSource {
    SYSTEM,
    BUNDLED,
    IMPORTED
}

enum class ThemeLayerKind {
    BACKGROUND_GRADIENT,
    MESH_GRADIENT,
    PARTICLE_SYSTEM,
    COLOR_OVERLAY,
    TEXTURE,
    LIGHT_EFFECTS,
    UI_ELEMENTS
}

data class ThemePaletteSpec(
    val primaryHex: String,
    val secondaryHex: String,
    val tertiaryHex: String,
    val accentHex: String,
    val surfaceHex: String,
    val surfaceVariantHex: String,
    val backgroundHex: String,
    val onPrimaryHex: String,
    val onSurfaceHex: String,
    val onBackgroundHex: String,
    val errorHex: String = "#FF6B6B",
    val successHex: String = "#51CF66"
)

data class ThemeGradientStopSpec(
    val colorHex: String,
    val position: Float
)

data class ThemeBackgroundSpec(
    val kind: ThemeBackgroundKind = ThemeBackgroundKind.GRADIENT,
    val colors: List<String> = emptyList(),
    val intenseColors: List<String> = emptyList(),
    val angleDegrees: Float = 90f,
    val solidColorHex: String? = null
)

data class ThemeMeshPointSpec(
    val id: String,
    val xFraction: Float,
    val yFraction: Float,
    val colorHex: String,
    val influence: Float = 1f,
    val driftX: Float = 0f,
    val driftY: Float = 0f
)

data class ThemeMeshSpec(
    val points: List<ThemeMeshPointSpec> = emptyList(),
    val animationSpeed: Float = 1f,
    val complexity: Int = 3,
    val amplitude: Float = 0.12f,
    val seed: Long = 0L
)

data class ThemeParticleSpec(
    val enabled: Boolean = true,
    val colors: List<String> = emptyList(),
    val countMin: Int = 20,
    val countMax: Int = 60,
    val speedMin: Float = 0.5f,
    val speedMax: Float = 2f,
    val sizeMin: Float = 2f,
    val sizeMax: Float = 8f,
    val type: ParticleType = ParticleType.FLOATING,
    val intensityMultiplier: Float = 1f
)

data class ThemeAnimationSpec(
    val cardPulseEnabled: Boolean = true,
    val cardPulseSpeed: Float = 1f,
    val backgroundAnimationEnabled: Boolean = true,
    val transitionDurationMs: Int = 300,
    val staggerDelayMs: Int = 50
)

data class ThemeIntensityDefaults(
    val theme: Float = 1f,
    val background: Float = 1f,
    val particle: Float = 1f,
    val animation: Float = 1f
)

data class ThemeFontAxisValue(
    val tag: String,
    val value: Float
)

data class ThemeFontRef(
    val source: ThemeFontSource = ThemeFontSource.SYSTEM,
    val id: String,
    val displayName: String,
    val uri: String? = null,
    val axes: List<ThemeFontAxisValue> = emptyList()
)

data class ThemeTypographySpec(
    val headingFont: ThemeFontRef = ThemeFontRef(
        source = ThemeFontSource.SYSTEM,
        id = "SYSTEM_DEFAULT",
        displayName = "System Default"
    ),
    val bodyFont: ThemeFontRef = ThemeFontRef(
        source = ThemeFontSource.SYSTEM,
        id = "SYSTEM_DEFAULT",
        displayName = "System Default"
    ),
    val captionFont: ThemeFontRef = ThemeFontRef(
        source = ThemeFontSource.SYSTEM,
        id = "SYSTEM_DEFAULT",
        displayName = "System Default"
    ),
    val typeScale: TypeScale = TypeScale.DEFAULT,
    val letterSpacingMultiplier: Float = 1f,
    val lineHeightMultiplier: Float = 1f,
    val fontWeightBoost: Boolean = false
)

data class ThemeLayerSpec(
    val id: String,
    val name: String,
    val kind: ThemeLayerKind,
    val enabled: Boolean = true,
    val opacity: Float = 1f,
    val blendMode: BlendMode = BlendMode.NORMAL,
    val gradientStops: List<ThemeGradientStopSpec> = emptyList(),
    val angleDegrees: Float = 90f,
    val mesh: ThemeMeshSpec? = null,
    val particle: ThemeParticleSpec? = null,
    val overlayHex: String? = null,
    val accentHex: String? = null,
    val glowHex: String? = null,
    val textureOpacity: Float = 0.4f,
    val glowIntensity: Float = 1f,
    val cornerRadius: Float = 16f,
    val elevation: Float = 4f
)

data class ThemeProfileMetadata(
    val schemaVersion: ThemeSchemaVersion = ThemeSchemaVersion.V1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val builtIn: Boolean = false,
    val importedFrom: String? = null,
    val sourceProfileId: String? = null
)

data class ThemeProfile(
    val id: String,
    val name: String,
    val description: String,
    val category: ThemeCategory = ThemeCategory.CUSTOM,
    val isDark: Boolean = false,
    val palette: ThemePaletteSpec,
    val background: ThemeBackgroundSpec = ThemeBackgroundSpec(),
    val mesh: ThemeMeshSpec? = null,
    val particles: ThemeParticleSpec = ThemeParticleSpec(),
    val animation: ThemeAnimationSpec = ThemeAnimationSpec(),
    val typography: ThemeTypographySpec = ThemeTypographySpec(),
    val layers: List<ThemeLayerSpec> = emptyList(),
    val intensityDefaults: ThemeIntensityDefaults = ThemeIntensityDefaults(),
    val metadata: ThemeProfileMetadata = ThemeProfileMetadata()
) {
    fun withUpdatedTimestamp(): ThemeProfile = copy(
        metadata = metadata.copy(updatedAt = System.currentTimeMillis())
    )
}

data class ThemeTransportEnvelope(
    val schemaVersion: Int = ThemeSchemaVersion.V1.value,
    val exportedAt: Long = System.currentTimeMillis(),
    val source: String = "SugarMunch",
    val profile: ThemeProfile
)

data class ImportedFontAsset(
    val id: String,
    val displayName: String,
    val uri: String,
    val isVariable: Boolean = true,
    val importedAt: Long = System.currentTimeMillis()
)

data class AppThemeOverride(
    val enabled: Boolean = true,
    val themeProfileId: String? = null,
    val themeIntensity: Float? = null,
    val backgroundIntensity: Float? = null,
    val particleIntensity: Float? = null,
    val animationIntensity: Float? = null,
    val accentHex: String? = null,
    val typographyOverrideEnabled: Boolean = false
)
