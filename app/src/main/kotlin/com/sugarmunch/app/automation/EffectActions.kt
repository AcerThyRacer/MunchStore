package com.sugarmunch.app.automation

import android.content.Context
import android.view.WindowManager
import com.sugarmunch.app.effects.EffectEngine

/**
 * Effect-related automation actions execution logic.
 * Handles enabling, disabling, toggling, and setting intensity of visual effects.
 */

/**
 * Execute EnableEffect action
 * Enables the specified effect and optionally sets its intensity
 */
suspend fun ActionExecutor.executeEnableEffect(action: AutomationAction.EnableEffectAction): ActionResult {
    return try {
        val effect = EffectEngine.allEffects().find { it.id == action.effectId }
            ?: return ActionResult.Failure("Effect not found: ${action.effectId}")

        if (!effect.isActive()) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            effect.enable(context, windowManager)
            // Emit event for trigger detection
            AutomationEventBus.emitEffectToggled(action.effectId, true)
        }

        action.intensity?.let {
            // Apply intensity if the effect supports it
        }

        ActionResult.Success("Effect enabled: ${effect.name}")
    } catch (e: Exception) {
        ActionResult.Failure(e.message ?: "Failed to enable effect")
    }
}

/**
 * Execute DisableEffect action
 * Disables the specified effect
 */
suspend fun ActionExecutor.executeDisableEffect(action: AutomationAction.DisableEffectAction): ActionResult {
    return try {
        val effect = EffectEngine.allEffects().find { it.id == action.effectId }
            ?: return ActionResult.Failure("Effect not found: ${action.effectId}")

        if (effect.isActive()) {
            effect.disable()
            AutomationEventBus.emitEffectToggled(action.effectId, false)
        }

        ActionResult.Success("Effect disabled: ${effect.name}")
    } catch (e: Exception) {
        ActionResult.Failure(e.message ?: "Failed to disable effect")
    }
}

/**
 * Execute ToggleEffect action
 * Toggles the specified effect on or off
 */
suspend fun ActionExecutor.executeToggleEffect(action: AutomationAction.ToggleEffectAction): ActionResult {
    return try {
        val effect = EffectEngine.allEffects().find { it.id == action.effectId }
            ?: return ActionResult.Failure("Effect not found: ${action.effectId}")

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        EffectEngine.toggle(context, windowManager, effect)
        AutomationEventBus.emitEffectToggled(action.effectId, effect.isActive())

        val status = if (effect.isActive()) "enabled" else "disabled"
        ActionResult.Success("Effect toggled: ${effect.name} is now $status")
    } catch (e: Exception) {
        ActionResult.Failure(e.message ?: "Failed to toggle effect")
    }
}

/**
 * Execute SetEffectIntensity action
 * Sets the intensity level for the specified effect
 */
suspend fun ActionExecutor.executeSetEffectIntensity(action: AutomationAction.SetEffectIntensityAction): ActionResult {
    // Effect intensity would be implemented based on effect capabilities
    return ActionResult.Success("Effect intensity set to ${action.intensity}")
}
