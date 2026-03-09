package com.sugarmunch.app.automation

import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Executes automation actions.
 * Delegates to category-specific extension functions for actual execution logic.
 *
 * @param context Android context for system operations
 */
class ActionExecutor(private val context: Context) {

    private val TAG = "ActionExecutor"

    private val automationEngine: AutomationEngine by lazy {
        AutomationEngine.getInstance(context)
    }

    /**
     * Execute a single action with optional delay
     *
     * @param action The automation action to execute
     * @return The result of action execution
     */
    suspend fun executeAction(action: AutomationAction): ActionResult {
        // Apply action delay if specified
        if (action.delayMs > 0) {
            delay(action.delayMs)
        }

        return try {
            withTimeoutOrNull(30000) { // 30 second timeout
                when (action) {
                    // Effect actions
                    is AutomationAction.EnableEffectAction -> executeEnableEffect(action)
                    is AutomationAction.DisableEffectAction -> executeDisableEffect(action)
                    is AutomationAction.ToggleEffectAction -> executeToggleEffect(action)
                    is AutomationAction.SetEffectIntensityAction -> executeSetEffectIntensity(action)

                    // Theme actions
                    is AutomationAction.ChangeThemeAction -> executeChangeTheme(action)
                    is AutomationAction.RandomThemeAction -> executeRandomTheme(action)
                    is AutomationAction.SetThemeIntensityAction -> executeSetThemeIntensity(action)

                    // App actions
                    is AutomationAction.OpenAppAction -> executeOpenApp(action)
                    is AutomationAction.LaunchSugarMunchScreenAction -> executeLaunchSugarMunchScreen(action)
                    is AutomationAction.ShareAppAction -> executeShareApp(action)

                    // Reward actions
                    is AutomationAction.ClaimRewardAction -> executeClaimReward(action)
                    is AutomationAction.AddSugarPointsAction -> executeAddSugarPoints(action)

                    // System actions
                    is AutomationAction.ShowNotificationAction -> executeShowNotification(action)
                    is AutomationAction.ShowToastAction -> executeShowToast(action)
                    is AutomationAction.VibrateAction -> executeVibrate(action)
                    is AutomationAction.SetBrightnessAction -> executeSetBrightness(action)
                    is AutomationAction.SetVolumeAction -> executeSetVolume(action)
                    is AutomationAction.PlaySoundAction -> executePlaySound(action)
                    is AutomationAction.TurnOffScreenAction -> executeTurnOffScreen(action)

                    // Utility actions
                    is AutomationAction.WaitAction -> executeWait(action)
                    is AutomationAction.ConditionalAction -> executeConditional(action)
                    is AutomationAction.RunTaskAction -> executeRunTask(action)
                }
            } ?: ActionResult.Failure("Action timed out")
        } catch (e: Exception) {
            Log.e(TAG, "Error executing action: ${action.actionId}", e)
            ActionResult.Failure(e.message ?: "Unknown error")
        }
    }

    /**
     * Execute multiple actions in sequence
     *
     * @param actions List of automation actions to execute
     * @return List of results for each action
     */
    suspend fun executeActions(actions: List<AutomationAction>): List<ActionResult> {
        return actions.map { action ->
            executeAction(action)
        }
    }
}
