package com.sugarmunch.app.theme.engine

import com.sugarmunch.app.theme.model.AdjustedColors
import com.sugarmunch.app.theme.model.CandyTheme
import com.sugarmunch.app.theme.profile.ThemeProfile
import com.sugarmunch.app.ui.typography.DynamicTypographyConfig

data class ThemeRuntimeSnapshot(
    val profile: ThemeProfile,
    val theme: CandyTheme,
    val colors: AdjustedColors,
    val typography: DynamicTypographyConfig,
    val themeIntensity: Float,
    val backgroundIntensity: Float,
    val particleIntensity: Float,
    val animationIntensity: Float,
    val isOverrideActive: Boolean = false
)
