package com.sugarmunch.app.automation

import com.sugarmunch.app.theme.engine.ThemeManager

/**
 * Theme-related automation actions execution logic.
 * Handles theme changes, random theme selection, and theme intensity adjustments.
 */

/**
 * Execute ChangeTheme action
 * Changes the current theme to the specified theme ID
 */
suspend fun ActionExecutor.executeChangeTheme(action: AutomationAction.ChangeThemeAction): ActionResult {
    return try {
        val themeManager = ThemeManager.getInstance(context)
        themeManager.setThemeById(action.themeId)

        action.intensity?.let {
            themeManager.setMasterIntensity(it)
        }

        AutomationEventBus.emitThemeChanged(action.themeId)

        ActionResult.Success("Theme changed to: ${action.themeId}")
    } catch (e: Exception) {
        ActionResult.Failure(e.message ?: "Failed to change theme")
    }
}

/**
 * Execute RandomTheme action
 * Applies a random theme, optionally filtered by category
 */
suspend fun ActionExecutor.executeRandomTheme(action: AutomationAction.RandomThemeAction): ActionResult {
    return try {
        val themeManager = ThemeManager.getInstance(context)
        val themes = if (action.category != null) {
            themeManager.getThemesByCategory(action.category)
        } else {
            themeManager.getAllThemes()
        }

        if (themes.isEmpty()) {
            return ActionResult.Failure("No themes available")
        }

        val randomTheme = themes.random()
        themeManager.setTheme(randomTheme)
        AutomationEventBus.emitThemeChanged(randomTheme.id)

        ActionResult.Success("Random theme applied: ${randomTheme.name}")
    } catch (e: Exception) {
        ActionResult.Failure(e.message ?: "Failed to apply random theme")
    }
}

/**
 * Execute SetThemeIntensity action
 * Sets the intensity for specific theme components
 */
suspend fun ActionExecutor.executeSetThemeIntensity(action: AutomationAction.SetThemeIntensityAction): ActionResult {
    return try {
        val themeManager = ThemeManager.getInstance(context)
        when (action.component) {
            AutomationAction.SetThemeIntensityAction.ThemeComponent.ALL ->
                themeManager.setMasterIntensity(action.intensity)
            AutomationAction.SetThemeIntensityAction.ThemeComponent.COLORS ->
                themeManager.setThemeIntensity(action.intensity)
            AutomationAction.SetThemeIntensityAction.ThemeComponent.BACKGROUND ->
                themeManager.setBackgroundIntensity(action.intensity)
            AutomationAction.SetThemeIntensityAction.ThemeComponent.PARTICLES ->
                themeManager.setParticleIntensity(action.intensity)
            AutomationAction.SetThemeIntensityAction.ThemeComponent.ANIMATIONS ->
                themeManager.setAnimationIntensity(action.intensity)
        }

        ActionResult.Success("Theme intensity set: ${action.component} = ${action.intensity}")
    } catch (e: Exception) {
        ActionResult.Failure(e.message ?: "Failed to set theme intensity")
    }
}
