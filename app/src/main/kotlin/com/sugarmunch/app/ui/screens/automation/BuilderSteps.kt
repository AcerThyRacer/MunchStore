package com.sugarmunch.app.ui.screens.automation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import com.sugarmunch.app.automation.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Represents the different steps in the task builder wizard.
 *
 * The task builder follows a sequential flow:
 * 1. [TRIGGER] - Select what starts the automation
 * 2. [CONDITIONS] - Configure when the automation should run
 * 3. [ACTIONS] - Define what happens when triggered
 * 4. [REVIEW] - Review and save the automation
 */
enum class BuilderStep {
    TRIGGER,
    CONDITIONS,
    ACTIONS,
    REVIEW
}

/**
 * State holder for the visual task builder.
 *
 * Manages the state of the task building process including:
 * - Current step in the wizard
 * - Selected trigger
 * - Selected conditions
 * - Selected actions
 * - Task name and description
 *
 * @see AutomationTask
 * @see AutomationTrigger
 * @see AutomationCondition
 * @see AutomationAction
 */
class VisualTaskBuilderState {

    private val _currentStep = MutableStateFlow(BuilderStep.TRIGGER)
    val currentStep: StateFlow<BuilderStep> = _currentStep.asStateFlow()

    private val _selectedTrigger = MutableStateFlow<AutomationTrigger?>(null)
    val selectedTrigger: StateFlow<AutomationTrigger?> = _selectedTrigger.asStateFlow()

    private val _selectedConditions = MutableStateFlow<List<AutomationCondition>>(emptyList())
    val selectedConditions: StateFlow<List<AutomationCondition>> = _selectedConditions.asStateFlow()

    private val _selectedActions = MutableStateFlow<List<AutomationAction>>(emptyList())
    val selectedActions: StateFlow<List<AutomationAction>> = _selectedActions.asStateFlow()

    private val _taskName = MutableStateFlow("")
    val taskName: StateFlow<String> = _taskName.asStateFlow()

    private val _taskDescription = MutableStateFlow("")
    val taskDescription: StateFlow<String> = _taskDescription.asStateFlow()

    /**
     * Advances to the next step in the builder wizard.
     */
    fun nextStep() {
        val current = _currentStep.value
        if (current.ordinal < BuilderStep.values().size - 1) {
            _currentStep.value = BuilderStep.values()[current.ordinal + 1]
        }
    }

    /**
     * Goes back to the previous step in the builder wizard.
     */
    fun previousStep() {
        val current = _currentStep.value
        if (current.ordinal > 0) {
            _currentStep.value = BuilderStep.values()[current.ordinal - 1]
        }
    }

    /**
     * Sets the selected trigger for the automation.
     *
     * @param trigger The trigger to use for this automation
     */
    fun setTrigger(trigger: AutomationTrigger) {
        _selectedTrigger.value = trigger
    }

    /**
     * Adds a condition to the automation.
     *
     * @param condition The condition to add
     */
    fun addCondition(condition: AutomationCondition) {
        _selectedConditions.value = _selectedConditions.value + condition
    }

    /**
     * Removes a condition from the automation.
     *
     * @param condition The condition to remove
     */
    fun removeCondition(condition: AutomationCondition) {
        _selectedConditions.value = _selectedConditions.value - condition
    }

    /**
     * Adds an action to the automation.
     *
     * @param action The action to add
     */
    fun addAction(action: AutomationAction) {
        _selectedActions.value = _selectedActions.value + action
    }

    /**
     * Removes an action from the automation.
     *
     * @param action The action to remove
     */
    fun removeAction(action: AutomationAction) {
        _selectedActions.value = _selectedActions.value - action
    }

    /**
     * Moves an action up in the execution order.
     *
     * @param currentIndex The current index of the action
     * @param newIndex The new index for the action
     */
    fun moveAction(currentIndex: Int, newIndex: Int) {
        val actions = _selectedActions.value.toMutableList()
        if (newIndex in actions.indices) {
            val action = actions.removeAt(currentIndex)
            actions.add(newIndex, action)
            _selectedActions.value = actions
        }
    }

    /**
     * Sets the name for the automation task.
     *
     * @param name The task name
     */
    fun setTaskName(name: String) {
        _taskName.value = name
    }

    /**
     * Sets the description for the automation task.
     *
     * @param description The task description
     */
    fun setTaskDescription(description: String) {
        _taskDescription.value = description
    }

    /**
     * Loads an existing task into the builder for editing.
     *
     * @param task The task to load
     */
    fun loadTask(task: AutomationTask) {
        _taskName.value = task.name
        _taskDescription.value = task.description ?: ""
        _selectedTrigger.value = task.trigger
        _selectedConditions.value = task.conditions
        _selectedActions.value = task.actions
        _currentStep.value = BuilderStep.TRIGGER
    }

    /**
     * Builds an [AutomationTask] from the current builder state.
     *
     * @return The constructed automation task, or null if no trigger is selected
     */
    fun buildTask(): AutomationTask? {
        val trigger = _selectedTrigger.value ?: return null
        return AutomationTask(
            id = UUID.randomUUID().toString(),
            name = _taskName.value.ifBlank { "Unnamed Automation" },
            description = _taskDescription.value.ifBlank { null },
            trigger = trigger,
            conditions = _selectedConditions.value,
            actions = _selectedActions.value,
            isEnabled = true
        )
    }
}

/**
 * Represents a trigger item in the trigger selection UI.
 *
 * @property name The display name of the trigger
 * @property description A brief description of what the trigger does
 * @property createTrigger A factory function to create the actual trigger instance
 */
data class TriggerItem(
    val name: String,
    val description: String,
    val createTrigger: () -> AutomationTrigger
)

/**
 * Represents a category of triggers for organization in the UI.
 *
 * @property name The category name
 * @property triggers The list of trigger items in this category
 */
data class TriggerCategory(
    val name: String,
    val triggers: List<TriggerItem>
)

/**
 * Object containing all trigger categories and their triggers.
 */
object TriggerCategories {
    val categories = listOf(
        TriggerCategory(
            name = "Time-Based",
            triggers = listOf(
                TriggerItem(
                    name = "Specific Time",
                    description = "Run at a specific time of day",
                    createTrigger = { AutomationTrigger.TimeTrigger(9, 0) }
                ),
                TriggerItem(
                    name = "Sunrise",
                    description = "Run at sunrise",
                    createTrigger = { AutomationTrigger.SunriseTrigger() }
                ),
                TriggerItem(
                    name = "Sunset",
                    description = "Run at sunset",
                    createTrigger = { AutomationTrigger.SunsetTrigger() }
                )
            )
        ),
        TriggerCategory(
            name = "App Events",
            triggers = listOf(
                TriggerItem(
                    name = "App Launch",
                    description = "Run when SugarMunch is opened",
                    createTrigger = { AutomationTrigger.AppLaunchTrigger() }
                ),
                TriggerItem(
                    name = "Daily Reward",
                    description = "Run when daily reward is claimed",
                    createTrigger = { AutomationTrigger.DailyRewardTrigger() }
                ),
                TriggerItem(
                    name = "Effect Activated",
                    description = "Run when an effect is activated",
                    createTrigger = { AutomationTrigger.EffectActivatedTrigger() }
                )
            )
        ),
        TriggerCategory(
            name = "System Events",
            triggers = listOf(
                TriggerItem(
                    name = "Charging Started",
                    description = "Run when device starts charging",
                    createTrigger = { AutomationTrigger.ChargingTrigger(isCharging = true) }
                ),
                TriggerItem(
                    name = "Low Battery",
                    description = "Run when battery is low",
                    createTrigger = { AutomationTrigger.BatteryTrigger(20) }
                ),
                TriggerItem(
                    name = "WiFi Connected",
                    description = "Run when connected to WiFi",
                    createTrigger = { AutomationTrigger.WifiTrigger(connected = true) }
                )
            )
        )
    )
}

/**
 * Represents an action item in the action picker UI.
 *
 * @property name The display name of the action
 * @property description A brief description of what the action does
 * @property createAction A factory function to create the actual action instance
 */
data class ActionItem(
    val name: String,
    val description: String,
    val createAction: () -> AutomationAction
)

/**
 * Represents a category of actions for organization in the UI.
 *
 * @property name The category name
 * @property actions The list of action items in this category
 */
data class ActionCategory(
    val name: String,
    val actions: List<ActionItem>
)

/**
 * Object containing all action categories and their actions.
 */
object ActionCategories {
    val categories = listOf(
        ActionCategory(
            name = "Theme & Effects",
            actions = listOf(
                ActionItem(
                    name = "Enable Effect",
                    description = "Turn on a specific effect",
                    createAction = { AutomationAction.EnableEffectAction("effect_id") }
                ),
                ActionItem(
                    name = "Disable Effect",
                    description = "Turn off a specific effect",
                    createAction = { AutomationAction.DisableEffectAction("effect_id") }
                ),
                ActionItem(
                    name = "Change Theme",
                    description = "Switch to a different theme",
                    createAction = { AutomationAction.ChangeThemeAction("theme_id") }
                ),
                ActionItem(
                    name = "Random Theme",
                    description = "Apply a random theme",
                    createAction = { AutomationAction.RandomThemeAction(null) }
                ),
                ActionItem(
                    name = "Set Intensity",
                    description = "Change theme intensity",
                    createAction = { AutomationAction.SetThemeIntensityAction(0.5f) }
                )
            )
        ),
        ActionCategory(
            name = "Navigation",
            actions = listOf(
                ActionItem(
                    name = "Open App",
                    description = "Launch another app",
                    createAction = { AutomationAction.OpenAppAction("com.example.app") }
                ),
                ActionItem(
                    name = "Open Screen",
                    description = "Navigate to a SugarMunch screen",
                    createAction = { AutomationAction.LaunchSugarMunchScreenAction(SugarMunchScreen.HOME) }
                )
            )
        ),
        ActionCategory(
            name = "Rewards",
            actions = listOf(
                ActionItem(
                    name = "Claim Reward",
                    description = "Claim daily reward automatically",
                    createAction = { AutomationAction.ClaimRewardAction() }
                ),
                ActionItem(
                    name = "Add Points",
                    description = "Add sugar points",
                    createAction = { AutomationAction.AddSugarPointsAction(10) }
                )
            )
        ),
        ActionCategory(
            name = "Feedback",
            actions = listOf(
                ActionItem(
                    name = "Show Notification",
                    description = "Display a notification",
                    createAction = { AutomationAction.ShowNotificationAction("Title", "Message") }
                ),
                ActionItem(
                    name = "Show Toast",
                    description = "Show a toast message",
                    createAction = { AutomationAction.ShowToastAction("Message") }
                ),
                ActionItem(
                    name = "Vibrate",
                    description = "Vibrate the device",
                    createAction = { AutomationAction.VibrateAction(VibrationPattern.SHORT) }
                )
            )
        ),
        ActionCategory(
            name = "System",
            actions = listOf(
                ActionItem(
                    name = "Set Brightness",
                    description = "Adjust screen brightness",
                    createAction = { AutomationAction.SetBrightnessAction(0.5f) }
                ),
                ActionItem(
                    name = "Wait",
                    description = "Delay before next action",
                    createAction = { AutomationAction.WaitAction(1000) }
                )
            )
        )
    )
}
