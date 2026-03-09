package com.sugarmunch.app.theme.profile

import androidx.compose.ui.graphics.Color
import com.sugarmunch.app.theme.builder.CustomTheme
import com.sugarmunch.app.theme.builder.GradientType as BuilderGradientType
import com.sugarmunch.app.theme.builder.ParticleType as BuilderParticleType
import com.sugarmunch.app.theme.layers.LayerConfig
import com.sugarmunch.app.theme.layers.LayerType
import com.sugarmunch.app.theme.layers.LayeredTheme
import com.sugarmunch.app.theme.layers.ThemeLayer
import com.sugarmunch.app.theme.model.AnimationConfig
import com.sugarmunch.app.theme.model.BackgroundStyle
import com.sugarmunch.app.theme.model.BaseColors
import com.sugarmunch.app.theme.model.CandyTheme
import com.sugarmunch.app.theme.model.FloatRange
import com.sugarmunch.app.theme.model.IntensityConfig
import com.sugarmunch.app.theme.model.ParticleConfig
import com.sugarmunch.app.theme.model.ParticleType
import com.sugarmunch.app.theme.model.ThemeCategory
import com.sugarmunch.app.ui.studio.CustomThemeSpec
import com.sugarmunch.app.ui.typography.FontPairing
import com.sugarmunch.app.ui.typography.SugarFontFamily
import java.util.UUID

fun CandyTheme.toThemeProfile(
    builtIn: Boolean = true,
    sourceProfileId: String? = null
): ThemeProfile {
    val palette = ThemePaletteSpec(
        primaryHex = baseColors.primary.toHexString(),
        secondaryHex = baseColors.secondary.toHexString(),
        tertiaryHex = baseColors.tertiary.toHexString(),
        accentHex = baseColors.accent.toHexString(),
        surfaceHex = baseColors.surface.toHexString(),
        surfaceVariantHex = baseColors.surfaceVariant.toHexString(),
        backgroundHex = baseColors.background.toHexString(),
        onPrimaryHex = baseColors.onPrimary.toHexString(),
        onSurfaceHex = baseColors.onSurface.toHexString(),
        onBackgroundHex = baseColors.onBackground.toHexString(),
        errorHex = baseColors.error.toHexString(),
        successHex = baseColors.success.toHexString()
    )
    val backgroundSpec = backgroundStyle.toProfileBackground()
    val meshSpec = (backgroundStyle as? BackgroundStyle.AnimatedMesh)?.toMeshSpec()
    val particleSpec = particleConfig.toProfileParticle()
    val layers = buildList {
        add(backgroundStyle.toThemeLayerSpec())
        if (particleConfig.enabled) {
            add(
                ThemeLayerSpec(
                    id = "particles-$id",
                    name = "Particles",
                    kind = ThemeLayerKind.PARTICLE_SYSTEM,
                    particle = particleSpec,
                    opacity = 1f
                )
            )
        }
    }
    return ThemeProfile(
        id = id,
        name = name,
        description = description,
        category = category,
        isDark = isDark,
        palette = palette,
        background = backgroundSpec,
        mesh = meshSpec,
        particles = particleSpec,
        animation = ThemeAnimationSpec(
            cardPulseEnabled = animationConfig.cardPulseEnabled,
            cardPulseSpeed = animationConfig.cardPulseSpeed,
            backgroundAnimationEnabled = animationConfig.backgroundAnimationEnabled,
            transitionDurationMs = animationConfig.transitionDuration,
            staggerDelayMs = animationConfig.staggerDelay
        ),
        layers = layers,
        intensityDefaults = ThemeIntensityDefaults(
            theme = intensityConfig.defaultValue,
            background = intensityConfig.defaultValue,
            particle = intensityConfig.defaultValue,
            animation = intensityConfig.defaultValue
        ),
        metadata = ThemeProfileMetadata(
            builtIn = builtIn,
            sourceProfileId = sourceProfileId
        )
    )
}

fun ThemeProfile.toCandyTheme(accentOverrideHex: String? = null): CandyTheme {
    val palette = palette
    val meshSpec = mesh ?: layers.firstOrNull { it.kind == ThemeLayerKind.MESH_GRADIENT }?.mesh
    val backgroundColors = when {
        background.colors.isNotEmpty() -> background.colors.map(String::toComposeColor)
        meshSpec?.points?.isNotEmpty() == true -> meshSpec.points.map { it.colorHex.toComposeColor() }
        else -> listOf(palette.backgroundHex.toComposeColor(), palette.surfaceHex.toComposeColor())
    }
    val backgroundStyle = when (background.kind) {
        ThemeBackgroundKind.GRADIENT -> BackgroundStyle.Gradient(
            colors = backgroundColors,
            intenseColors = background.intenseColors.takeIf { it.isNotEmpty() }?.map(String::toComposeColor),
            angleDegrees = background.angleDegrees
        )
        ThemeBackgroundKind.MESH -> BackgroundStyle.AnimatedMesh(
            baseColors = meshSpec?.points?.map { it.colorHex.toComposeColor() } ?: backgroundColors,
            animationSpeed = meshSpec?.animationSpeed ?: 1f,
            complexity = meshSpec?.complexity ?: 3
        )
        ThemeBackgroundKind.SOLID -> BackgroundStyle.Solid(
            color = (background.solidColorHex ?: palette.backgroundHex).toComposeColor()
        )
    }
    return CandyTheme(
        id = id,
        name = name,
        description = description,
        baseColors = BaseColors(
            primary = palette.primaryHex.toComposeColor(),
            secondary = palette.secondaryHex.toComposeColor(),
            tertiary = palette.tertiaryHex.toComposeColor(),
            accent = (accentOverrideHex ?: palette.accentHex).toComposeColor(),
            surface = palette.surfaceHex.toComposeColor(),
            surfaceVariant = palette.surfaceVariantHex.toComposeColor(),
            background = palette.backgroundHex.toComposeColor(),
            onPrimary = palette.onPrimaryHex.toComposeColor(),
            onSurface = palette.onSurfaceHex.toComposeColor(),
            onBackground = palette.onBackgroundHex.toComposeColor(),
            error = palette.errorHex.toComposeColor(),
            success = palette.successHex.toComposeColor()
        ),
        intensityConfig = IntensityConfig(
            defaultValue = intensityDefaults.theme
        ),
        backgroundStyle = backgroundStyle,
        particleConfig = particles.toRuntimeParticle(),
        animationConfig = AnimationConfig(
            cardPulseEnabled = animation.cardPulseEnabled,
            cardPulseSpeed = animation.cardPulseSpeed,
            backgroundAnimationEnabled = animation.backgroundAnimationEnabled,
            transitionDuration = animation.transitionDurationMs,
            staggerDelay = animation.staggerDelayMs
        ),
        isDark = isDark,
        category = category
    )
}

fun LayeredTheme.toThemeProfile(
    baseProfile: ThemeProfile? = null
): ThemeProfile {
    val source = baseProfile ?: ThemeProfile(
        id = id,
        name = name,
        description = description,
        category = ThemeCategory.entries.firstOrNull { it.name == category } ?: ThemeCategory.CUSTOM,
        isDark = isDark,
        palette = ThemePaletteSpec(
            primaryHex = "#FF69B4",
            secondaryHex = "#9370DB",
            tertiaryHex = "#FFB6C1",
            accentHex = "#FF1493",
            surfaceHex = "#FFF8F0",
            surfaceVariantHex = "#FFEDE0",
            backgroundHex = "#FFFBF7",
            onPrimaryHex = "#1A1A1A",
            onSurfaceHex = "#1A1A1A",
            onBackgroundHex = "#1A1A1A"
        )
    )
    return source.copy(
        id = id,
        name = name,
        description = description,
        category = ThemeCategory.entries.firstOrNull { it.name == category } ?: ThemeCategory.CUSTOM,
        isDark = isDark,
        layers = getLayersInOrder().map { it.toSpec() },
        metadata = source.metadata.copy(sourceProfileId = source.id)
    )
}

fun ThemeProfile.toLayeredTheme(): LayeredTheme {
    val layerSpecs = if (layers.isNotEmpty()) layers else toCandyTheme().toThemeProfile().layers
    val runtimeLayers = layerSpecs.map { it.toRuntimeLayer() }
    return LayeredTheme(
        id = id,
        name = name,
        description = description,
        layers = runtimeLayers,
        layerOrder = runtimeLayers.map { it.id },
        isDark = isDark,
        category = category.name
    )
}

fun CustomThemeSpec.toThemeProfile(id: String = UUID.randomUUID().toString()): ThemeProfile {
    val fontPairing = fontPairing
    return ThemeProfile(
        id = id,
        name = name,
        description = "Theme Studio custom theme",
        isDark = isDark,
        category = ThemeCategory.CUSTOM,
        palette = ThemePaletteSpec(
            primaryHex = primaryColor.toHexString(),
            secondaryHex = secondaryColor.toHexString(),
            tertiaryHex = tertiaryColor.toHexString(),
            accentHex = accentColor.toHexString(),
            surfaceHex = surfaceColor.toHexString(),
            surfaceVariantHex = surfaceColor.copy(alpha = 0.88f).toHexString(),
            backgroundHex = backgroundColor.toHexString(),
            onPrimaryHex = if (isDark) "#FFFFFF" else "#1A1A1A",
            onSurfaceHex = if (isDark) "#FFFFFF" else "#1A1A1A",
            onBackgroundHex = if (isDark) "#FFFFFF" else "#1A1A1A"
        ),
        background = ThemeBackgroundSpec(
            kind = if (gradient != null) ThemeBackgroundKind.GRADIENT else ThemeBackgroundKind.SOLID,
            colors = gradient?.stops?.map { it.color.toHexString() } ?: emptyList(),
            angleDegrees = gradient?.angleDegrees ?: 90f,
            solidColorHex = backgroundColor.toHexString()
        ),
        typography = ThemeTypographySpec(
            headingFont = fontPairing?.headingFont?.toThemeFontRef()
                ?: SugarFontFamily.SYSTEM_DEFAULT.toThemeFontRef(),
            bodyFont = fontPairing?.bodyFont?.toThemeFontRef()
                ?: SugarFontFamily.SYSTEM_DEFAULT.toThemeFontRef(),
            captionFont = fontPairing?.captionFont?.toThemeFontRef()
                ?: SugarFontFamily.SYSTEM_DEFAULT.toThemeFontRef()
        ),
        layers = buildList {
            add(
                ThemeLayerSpec(
                    id = "$id-background",
                    name = "Background",
                    kind = if (gradient != null) ThemeLayerKind.BACKGROUND_GRADIENT else ThemeLayerKind.COLOR_OVERLAY,
                    gradientStops = gradient?.stops?.map {
                        ThemeGradientStopSpec(it.color.toHexString(), it.position)
                    } ?: emptyList(),
                    angleDegrees = gradient?.angleDegrees ?: 90f,
                    overlayHex = backgroundColor.toHexString()
                )
            )
            pattern?.let {
                add(
                    ThemeLayerSpec(
                        id = "$id-pattern",
                        name = "Pattern Overlay",
                        kind = ThemeLayerKind.TEXTURE,
                        overlayHex = it.secondaryColor.toHexString(),
                        opacity = it.opacity
                    )
                )
            }
        }
    )
}

fun ThemeProfile.toCustomThemeSpec(): CustomThemeSpec {
    val typography = typography
    return CustomThemeSpec(
        name = name,
        primaryColor = palette.primaryHex.toComposeColor(),
        secondaryColor = palette.secondaryHex.toComposeColor(),
        tertiaryColor = palette.tertiaryHex.toComposeColor(),
        surfaceColor = palette.surfaceHex.toComposeColor(),
        backgroundColor = palette.backgroundHex.toComposeColor(),
        accentColor = palette.accentHex.toComposeColor(),
        gradient = background.colors.takeIf { it.isNotEmpty() }?.mapIndexed { index, colorHex ->
            com.sugarmunch.app.ui.visual.GradientStop(
                color = colorHex.toComposeColor(),
                position = index.toFloat() / (background.colors.lastIndex.coerceAtLeast(1))
            )
        }?.let { stops ->
            com.sugarmunch.app.ui.visual.GradientSpec(
                type = com.sugarmunch.app.ui.visual.GradientType.LINEAR,
                stops = stops,
                angleDegrees = background.angleDegrees
            )
        },
        fontPairing = FontPairing(
            name = "${typography.headingFont.displayName} / ${typography.bodyFont.displayName}",
            headingFont = typography.headingFont.toSugarFontFamily(),
            bodyFont = typography.bodyFont.toSugarFontFamily(),
            captionFont = typography.captionFont.toSugarFontFamily()
        ),
        isDark = isDark
    )
}

fun CustomTheme.toThemeProfile(idOverride: String? = null): ThemeProfile {
    val particlesEnabled = enableParticles
    return ThemeProfile(
        id = idOverride ?: UUID.randomUUID().toString(),
        name = name,
        description = "Legacy builder theme",
        category = ThemeCategory.CUSTOM,
        isDark = Color(backgroundColor).luminance() < 0.5f,
        palette = ThemePaletteSpec(
            primaryHex = Color(primaryColor).toHexString(),
            secondaryHex = Color(secondaryColor).toHexString(),
            tertiaryHex = Color(secondaryColor).copy(alpha = 0.8f).toHexString(),
            accentHex = Color(primaryColor).copy(alpha = 0.9f).toHexString(),
            surfaceHex = Color(surfaceColor).toHexString(),
            surfaceVariantHex = Color(surfaceColor).copy(alpha = 0.88f).toHexString(),
            backgroundHex = Color(backgroundColor).toHexString(),
            onPrimaryHex = "#FFFFFF",
            onSurfaceHex = "#FFFFFF",
            onBackgroundHex = "#FFFFFF"
        ),
        background = ThemeBackgroundSpec(
            kind = if (gradientColors.isNotEmpty()) ThemeBackgroundKind.GRADIENT else ThemeBackgroundKind.SOLID,
            colors = gradientColors.map { Color(it).toHexString() },
            angleDegrees = gradientAngle,
            solidColorHex = Color(backgroundColor).toHexString()
        ),
        particles = ThemeParticleSpec(
            enabled = particlesEnabled,
            colors = gradientColors.takeIf { it.isNotEmpty() }?.map { Color(it).toHexString() }
                ?: listOf(Color(primaryColor).toHexString(), Color(secondaryColor).toHexString()),
            countMin = particleDensity.coerceAtLeast(1),
            countMax = particleDensity.coerceAtLeast(1) * 2,
            speedMin = particleSpeed,
            speedMax = particleSpeed * 1.5f,
            sizeMin = 2f,
            sizeMax = 10f,
            type = particleType.toRuntimeParticleType()
        )
    )
}

fun ThemeProfile.toCustomTheme(): CustomTheme {
    return CustomTheme(
        name = name,
        primaryColor = palette.primaryHex.toComposeColor().value.toLong(),
        secondaryColor = palette.secondaryHex.toComposeColor().value.toLong(),
        backgroundColor = palette.backgroundHex.toComposeColor().value.toLong(),
        surfaceColor = palette.surfaceHex.toComposeColor().value.toLong(),
        gradientType = BuilderGradientType.LINEAR,
        gradientAngle = background.angleDegrees,
        gradientColors = background.colors.map { it.toComposeColor().value.toLong() },
        enableParticles = particles.enabled,
        particleType = particles.type.toBuilderParticleType(),
        particleDensity = particles.countMin,
        particleSpeed = particles.speedMin,
        enableAnimation = animation.backgroundAnimationEnabled,
        enableBlur = layers.any { it.kind == ThemeLayerKind.LIGHT_EFFECTS }
    )
}

fun SugarFontFamily.toThemeFontRef(): ThemeFontRef = ThemeFontRef(
    source = when (this) {
        SugarFontFamily.NUNITO_VARIABLE,
        SugarFontFamily.COMFORTAA_VARIABLE,
        SugarFontFamily.SPACE_GROTESK_VARIABLE -> ThemeFontSource.BUNDLED
        else -> ThemeFontSource.SYSTEM
    },
    id = name,
    displayName = displayName
)

fun ThemeFontRef.toSugarFontFamily(): SugarFontFamily =
    SugarFontFamily.entries.firstOrNull { it.name == id } ?: SugarFontFamily.SYSTEM_DEFAULT

private fun BackgroundStyle.toProfileBackground(): ThemeBackgroundSpec = when (this) {
    is BackgroundStyle.Gradient -> ThemeBackgroundSpec(
        kind = ThemeBackgroundKind.GRADIENT,
        colors = colors.map(Color::toHexString),
        intenseColors = intenseColors?.map(Color::toHexString).orEmpty(),
        angleDegrees = angleDegrees
    )
    is BackgroundStyle.AnimatedMesh -> ThemeBackgroundSpec(
        kind = ThemeBackgroundKind.MESH,
        colors = baseColors.map(Color::toHexString)
    )
    is BackgroundStyle.Solid -> ThemeBackgroundSpec(
        kind = ThemeBackgroundKind.SOLID,
        solidColorHex = color.toHexString()
    )
}

private fun BackgroundStyle.AnimatedMesh.toMeshSpec(): ThemeMeshSpec {
    val basePoints = baseColors.mapIndexed { index, color ->
        ThemeMeshPointSpec(
            id = "mesh-$index",
            xFraction = when (index % 3) {
                0 -> 0.2f
                1 -> 0.5f
                else -> 0.8f
            },
            yFraction = when (index / 3) {
                0 -> 0.2f
                1 -> 0.5f
                else -> 0.8f
            },
            colorHex = color.toHexString(),
            influence = 1f,
            driftX = 0.04f,
            driftY = 0.04f
        )
    }
    return ThemeMeshSpec(
        points = basePoints,
        animationSpeed = animationSpeed,
        complexity = complexity,
        amplitude = 0.12f,
        seed = 0L
    )
}

private fun ParticleConfig.toProfileParticle(): ThemeParticleSpec = ThemeParticleSpec(
    enabled = enabled,
    colors = colors.map(Color::toHexString),
    countMin = count.first,
    countMax = count.last,
    speedMin = speed.min,
    speedMax = speed.max,
    sizeMin = size.min,
    sizeMax = size.max,
    type = type,
    intensityMultiplier = intensityMultiplier
)

private fun ThemeParticleSpec.toRuntimeParticle(): ParticleConfig = ParticleConfig(
    enabled = enabled,
    colors = colors.map(String::toComposeColor),
    count = countMin..countMax,
    speed = FloatRange(speedMin, speedMax),
    size = FloatRange(sizeMin, sizeMax),
    type = type,
    intensityMultiplier = intensityMultiplier
)

private fun BackgroundStyle.toThemeLayerSpec(): ThemeLayerSpec = when (this) {
    is BackgroundStyle.Gradient -> ThemeLayerSpec(
        id = "background-gradient",
        name = "Background Gradient",
        kind = ThemeLayerKind.BACKGROUND_GRADIENT,
        gradientStops = colors.mapIndexed { index, color ->
            ThemeGradientStopSpec(
                colorHex = color.toHexString(),
                position = index.toFloat() / colors.lastIndex.coerceAtLeast(1)
            )
        },
        angleDegrees = angleDegrees
    )
    is BackgroundStyle.AnimatedMesh -> ThemeLayerSpec(
        id = "mesh-gradient",
        name = "Mesh Gradient",
        kind = ThemeLayerKind.MESH_GRADIENT,
        mesh = toMeshSpec()
    )
    is BackgroundStyle.Solid -> ThemeLayerSpec(
        id = "solid-overlay",
        name = "Solid Background",
        kind = ThemeLayerKind.COLOR_OVERLAY,
        overlayHex = color.toHexString()
    )
}

private fun ThemeLayer.toSpec(): ThemeLayerSpec {
    val kind = when (layerType) {
        LayerType.BACKGROUND_GRADIENT -> ThemeLayerKind.BACKGROUND_GRADIENT
        LayerType.MESH_GRADIENT -> ThemeLayerKind.MESH_GRADIENT
        LayerType.PARTICLE_SYSTEM -> ThemeLayerKind.PARTICLE_SYSTEM
        LayerType.COLOR_OVERLAY -> ThemeLayerKind.COLOR_OVERLAY
        LayerType.TEXTURE -> ThemeLayerKind.TEXTURE
        LayerType.LIGHT_EFFECTS -> ThemeLayerKind.LIGHT_EFFECTS
        LayerType.UI_ELEMENTS -> ThemeLayerKind.UI_ELEMENTS
    }
    return ThemeLayerSpec(
        id = id,
        name = name,
        kind = kind,
        enabled = isEnabled,
        opacity = opacity,
        blendMode = blendMode,
        gradientStops = config.gradientColors.mapIndexed { index, color ->
            ThemeGradientStopSpec(
                colorHex = color.toHexString(),
                position = index.toFloat() / config.gradientColors.lastIndex.coerceAtLeast(1)
            )
        },
        angleDegrees = config.gradientAngle,
        mesh = config.meshColors.takeIf { it.isNotEmpty() }?.mapIndexed { index, color ->
            ThemeMeshPointSpec(
                id = "$id-mesh-$index",
                xFraction = (index + 1f) / (config.meshColors.size + 1f),
                yFraction = (index + 1f) / (config.meshColors.size + 1f),
                colorHex = color.toHexString()
            )
        }?.let {
            ThemeMeshSpec(
                points = it,
                animationSpeed = config.animationSpeed,
                complexity = config.meshComplexity,
                seed = config.meshSeed
            )
        },
        particle = config.particleColors.takeIf { it.isNotEmpty() }?.let {
            ThemeParticleSpec(
                enabled = true,
                colors = it.map(Color::toHexString),
                countMin = config.particleCount,
                countMax = (config.particleCount * 1.5f).toInt(),
                speedMin = config.particleSpeed,
                speedMax = config.particleSpeed * 1.5f,
                sizeMin = config.particleSize,
                sizeMax = config.particleSize * 1.5f
            )
        },
        overlayHex = config.overlayColor.takeIf { it != Color.Transparent }?.toHexString(),
        accentHex = config.accentColor.takeIf { it != Color.Unspecified }?.toHexString(),
        glowHex = config.glowColor.toHexString(),
        textureOpacity = config.textureOpacity,
        glowIntensity = config.glowIntensity,
        cornerRadius = config.cornerRadius,
        elevation = config.elevation
    )
}

private fun ThemeLayerSpec.toRuntimeLayer(): ThemeLayer {
    val layerType = when (kind) {
        ThemeLayerKind.BACKGROUND_GRADIENT -> LayerType.BACKGROUND_GRADIENT
        ThemeLayerKind.MESH_GRADIENT -> LayerType.MESH_GRADIENT
        ThemeLayerKind.PARTICLE_SYSTEM -> LayerType.PARTICLE_SYSTEM
        ThemeLayerKind.COLOR_OVERLAY -> LayerType.COLOR_OVERLAY
        ThemeLayerKind.TEXTURE -> LayerType.TEXTURE
        ThemeLayerKind.LIGHT_EFFECTS -> LayerType.LIGHT_EFFECTS
        ThemeLayerKind.UI_ELEMENTS -> LayerType.UI_ELEMENTS
    }
    return ThemeLayer(
        id = id,
        name = name,
        layerType = layerType,
        isEnabled = enabled,
        opacity = opacity,
        blendMode = blendMode,
        config = LayerConfig(
            gradientColors = gradientStops.map { it.colorHex.toComposeColor() },
            gradientAngle = angleDegrees,
            meshColors = mesh?.points?.map { it.colorHex.toComposeColor() }.orEmpty(),
            meshComplexity = mesh?.complexity ?: 3,
            animationSpeed = mesh?.animationSpeed ?: 1f,
            meshSeed = mesh?.seed ?: 0L,
            particleColors = particle?.colors?.map(String::toComposeColor).orEmpty(),
            particleCount = particle?.countMin ?: 24,
            particleSize = particle?.sizeMin ?: 1f,
            particleSpeed = particle?.speedMin ?: 1f,
            overlayColor = overlayHex?.toComposeColor() ?: Color.Transparent,
            textureOpacity = textureOpacity,
            glowColor = glowHex?.toComposeColor() ?: Color.White,
            glowIntensity = glowIntensity,
            accentColor = accentHex?.toComposeColor() ?: Color.Unspecified,
            cornerRadius = cornerRadius,
            elevation = elevation
        )
    )
}

fun String.toComposeColor(): Color {
    val cleaned = removePrefix("#")
    val value = cleaned.toLong(16)
    return when (cleaned.length) {
        6 -> Color(
            red = ((value shr 16) and 0xFF).toInt(),
            green = ((value shr 8) and 0xFF).toInt(),
            blue = (value and 0xFF).toInt()
        )
        8 -> Color(value.toInt())
        else -> Color.White
    }
}

fun Color.toHexString(): String {
    val alpha = (alpha * 255).toInt().coerceIn(0, 255)
    val red = (red * 255).toInt().coerceIn(0, 255)
    val green = (green * 255).toInt().coerceIn(0, 255)
    val blue = (blue * 255).toInt().coerceIn(0, 255)
    return if (alpha == 255) {
        "#%02X%02X%02X".format(red, green, blue)
    } else {
        "#%02X%02X%02X%02X".format(alpha, red, green, blue)
    }
}

private fun BuilderParticleType.toRuntimeParticleType(): ParticleType = when (this) {
    BuilderParticleType.CIRCLES -> ParticleType.FLOATING
    BuilderParticleType.SQUARES -> ParticleType.CHAOTIC
    BuilderParticleType.STARS -> ParticleType.SPARKLE
    BuilderParticleType.HEARTS -> ParticleType.RISING
    BuilderParticleType.DIAMONDS -> ParticleType.SWIRLING
}

private fun ParticleType.toBuilderParticleType(): BuilderParticleType = when (this) {
    ParticleType.FLOATING -> BuilderParticleType.CIRCLES
    ParticleType.RAINING -> BuilderParticleType.SQUARES
    ParticleType.RISING -> BuilderParticleType.HEARTS
    ParticleType.EXPLODING -> BuilderParticleType.STARS
    ParticleType.SWIRLING -> BuilderParticleType.DIAMONDS
    ParticleType.CHAOTIC -> BuilderParticleType.SQUARES
    ParticleType.BUBBLES -> BuilderParticleType.CIRCLES
    ParticleType.SPARKLE -> BuilderParticleType.STARS
}
