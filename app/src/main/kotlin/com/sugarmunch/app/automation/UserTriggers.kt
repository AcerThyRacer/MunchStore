package com.sugarmunch.app.automation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * User interaction trigger evaluation functions.
 * Handles EffectToggled, ThemeChanged, RewardClaimed, and Manual triggers.
 */
internal class UserTriggerEvaluator {

    /**
     * Evaluates effect toggled triggers that fire when effects are enabled/disabled.
     */
    fun evaluateEffectToggledTrigger(trigger: AutomationTrigger.EffectToggledTrigger): Flow<TriggerEvent> =
        AutomationEventBus.effectToggledEvents
            .filter { event ->
                (trigger.effectId == null || event.effectId == trigger.effectId) &&
                (trigger.state == AutomationTrigger.EffectToggledTrigger.EffectState.EITHER ||
                 (trigger.state == AutomationTrigger.EffectToggledTrigger.EffectState.ENABLED && event.enabled) ||
                 (trigger.state == AutomationTrigger.EffectToggledTrigger.EffectState.DISABLED && !event.enabled))
            }
            .map { TriggerEvent.EffectToggledEvent(it.effectId, it.enabled) }

    /**
     * Evaluates theme changed triggers that fire when themes are switched.
     */
    fun evaluateThemeChangedTrigger(trigger: AutomationTrigger.ThemeChangedTrigger): Flow<TriggerEvent> =
        AutomationEventBus.themeChangedEvents
            .filter { event ->
                trigger.themeId == null || event.themeId == trigger.themeId
            }
            .map { TriggerEvent.ThemeChangedEvent(it.themeId) }

    /**
     * Evaluates reward claimed triggers that fire when rewards are claimed.
     */
    fun evaluateRewardClaimedTrigger(trigger: AutomationTrigger.RewardClaimedTrigger): Flow<TriggerEvent> =
        AutomationEventBus.rewardClaimedEvents
            .filter { event ->
                trigger.rewardType == null || event.rewardType == trigger.rewardType
            }
            .map { TriggerEvent.RewardClaimedEvent(it.rewardType) }

    /**
     * Evaluates manual triggers that fire when user manually activates them.
     */
    fun evaluateManualTrigger(trigger: AutomationTrigger.ManualTrigger): Flow<TriggerEvent> =
        AutomationEventBus.manualTriggerEvents
            .filter { it.triggerId == trigger.triggerId }
            .map { TriggerEvent.ManualEvent(trigger.triggerId, trigger.shortcutName) }
}
