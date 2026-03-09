package com.sugarmunch.app.automation

import android.content.Context
import com.sugarmunch.app.effects.EffectEngine
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.presets.ThemePresets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * TaskBuilder - Visual task builder backend
 * Provides a fluent API for building automation tasks
 */
class TaskBuilder(private val context: Context) {
    
    private var taskId: String = UUID.randomUUID().toString()
    private var name: String = ""
    private var description: String = ""
    private var trigger: AutomationTrigger? = null
    private val conditions = mutableListOf<AutomationCondition>()
    private val actions = mutableListOf<AutomationAction>()
    private var templateId: String? = null
    
    private val _builderState = MutableStateFlow<BuilderState>(BuilderState.Empty)
    val builderState: StateFlow<BuilderState> = _builderState.asStateFlow()
    
    // ═════════════════════════════════════════════════════════════
    // FLUENT API
    // ═════════════════════════════════════════════════════════════
    
    fun id(id: String) = apply { this.taskId = id }
    fun name(name: String) = apply { this.name = name }
    fun description(description: String) = apply { this.description = description }
    fun fromTemplate(templateId: String) = apply { this.templateId = templateId }
    
    // Trigger builders
    fun atTime(hour: Int, minute: Int, days: List<Int> = emptyList()) = apply {
        trigger = AutomationTrigger.TimeTrigger(
            hour = hour,
            minute = minute,
            repeatDays = days
        )
    }
    
    fun everyInterval(minutes: Long) = apply {
        trigger = AutomationTrigger.IntervalTrigger(intervalMinutes = minutes)
    }
    
    fun atSunrise(offsetMinutes: Int = 0) = apply {
        trigger = AutomationTrigger.SunriseSunsetTrigger(
            event = AutomationTrigger.SunriseSunsetTrigger.SunEvent.SUNRISE,
            offsetMinutes = offsetMinutes
        )
    }
    
    fun atSunset(offsetMinutes: Int = 0) = apply {
        trigger = AutomationTrigger.SunriseSunsetTrigger(
            event = AutomationTrigger.SunriseSunsetTrigger.SunEvent.SUNSET,
            offsetMinutes = offsetMinutes
        )
    }
    
    fun whenAppOpened(packageNames: List<String>) = apply {
        trigger = AutomationTrigger.AppOpenedTrigger(packageNames = packageNames)
    }
    
    fun whenAppClosed(packageNames: List<String>) = apply {
        trigger = AutomationTrigger.AppClosedTrigger(packageNames = packageNames)
    }
    
    fun onEffectToggled(effectId: String? = null) = apply {
        trigger = AutomationTrigger.EffectToggledTrigger(effectId = effectId)
    }
    
    fun onThemeChanged(themeId: String? = null) = apply {
        trigger = AutomationTrigger.ThemeChangedTrigger(themeId = themeId)
    }
    
    fun whenBatteryLevel(level: Int, operator: AutomationTrigger.BatteryLevelTrigger.ComparisonOperator = 
        AutomationTrigger.BatteryLevelTrigger.ComparisonOperator.LESS_THAN_OR_EQUAL) = apply {
        trigger = AutomationTrigger.BatteryLevelTrigger(level = level, operator = operator)
    }
    
    fun whenCharging(state: AutomationTrigger.ChargingTrigger.ChargingState) = apply {
        trigger = AutomationTrigger.ChargingTrigger(state = state)
    }
    
    fun whenWifiConnected(ssid: String? = null) = apply {
        trigger = AutomationTrigger.WifiConnectedTrigger(
            ssid = ssid,
            anyWifi = ssid == null
        )
    }
    
    fun whenScreen(state: AutomationTrigger.ScreenStateTrigger.ScreenState) = apply {
        trigger = AutomationTrigger.ScreenStateTrigger(state = state)
    }
    
    fun atLocation(latitude: Double, longitude: Double, radiusMeters: Float = 100f,
                   transition: AutomationTrigger.GeofenceTrigger.GeofenceTransition = 
                   AutomationTrigger.GeofenceTrigger.GeofenceTransition.ENTER) = apply {
        trigger = AutomationTrigger.GeofenceTrigger(
            latitude = latitude,
            longitude = longitude,
            radiusMeters = radiusMeters,
            transition = transition
        )
    }
    
    fun onShake(sensitivity: AutomationTrigger.ShakeTrigger.ShakeSensitivity = 
                AutomationTrigger.ShakeTrigger.ShakeSensitivity.MEDIUM) = apply {
        trigger = AutomationTrigger.ShakeTrigger(sensitivity = sensitivity)
    }
    
    fun manual() = apply {
        trigger = AutomationTrigger.ManualTrigger()
    }
    
    // Condition builders
    fun onlyDuring(hourStart: Int, minuteStart: Int, hourEnd: Int, minuteEnd: Int) = apply {
        conditions.add(AutomationCondition.TimeCondition(
            startHour = hourStart,
            startMinute = minuteStart,
            endHour = hourEnd,
            endMinute = minuteEnd
        ))
    }
    
    fun onlyOnWeekdays() = apply {
        conditions.add(AutomationCondition.TimeCondition(
            startHour = 0,
            startMinute = 0,
            endHour = 23,
            endMinute = 59,
            daysOfWeek = listOf(1, 2, 3, 4, 5) // Monday to Friday
        ))
    }
    
    fun onlyOnWeekends() = apply {
        conditions.add(AutomationCondition.TimeCondition(
            startHour = 0,
            startMinute = 0,
            endHour = 23,
            endMinute = 59,
            daysOfWeek = listOf(0, 6) // Sunday and Saturday
        ))
    }
    
    fun onlyWhenBatteryAbove(level: Int) = apply {
        conditions.add(AutomationCondition.BatteryCondition(minLevel = level))
    }
    
    fun onlyWhenBatteryBelow(level: Int) = apply {
        conditions.add(AutomationCondition.BatteryCondition(maxLevel = level))
    }
    
    fun onlyWhenCharging() = apply {
        conditions.add(AutomationCondition.BatteryCondition(mustBeCharging = true))
    }
    
    fun onlyWhenWifiConnected(ssid: String? = null) = apply {
        conditions.add(AutomationCondition.WifiCondition(connected = true, ssid = ssid))
    }
    
    fun onlyWhenEffectActive(effectId: String) = apply {
        conditions.add(AutomationCondition.EffectActiveCondition(effectId = effectId, mustBeActive = true))
    }
    
    // Action builders - Effects
    fun enableEffect(effectId: String, intensity: Float? = null) = apply {
        actions.add(AutomationAction.EnableEffectAction(
            effectId = effectId,
            intensity = intensity
        ))
    }
    
    fun disableEffect(effectId: String) = apply {
        actions.add(AutomationAction.DisableEffectAction(effectId = effectId))
    }
    
    fun toggleEffect(effectId: String) = apply {
        actions.add(AutomationAction.ToggleEffectAction(effectId = effectId))
    }
    
    // Action builders - Themes
    fun changeTheme(themeId: String, intensity: Float? = null) = apply {
        actions.add(AutomationAction.ChangeThemeAction(
            themeId = themeId,
            intensity = intensity
        ))
    }
    
    fun randomTheme(category: String? = null) = apply {
        actions.add(AutomationAction.RandomThemeAction(category = category))
    }
    
    fun setThemeIntensity(intensity: Float) = apply {
        actions.add(AutomationAction.SetThemeIntensityAction(intensity = intensity))
    }
    
    // Action builders - Apps
    fun openApp(packageName: String) = apply {
        actions.add(AutomationAction.OpenAppAction(packageName = packageName))
    }
    
    fun openSugarMunchScreen(screen: AutomationAction.LaunchSugarMunchScreenAction.SugarMunchScreen) = apply {
        actions.add(AutomationAction.LaunchSugarMunchScreenAction(screen = screen))
    }
    
    // Action builders - SugarMunch
    fun claimDailyReward() = apply {
        actions.add(AutomationAction.ClaimRewardAction())
    }
    
    fun addSugarPoints(points: Int, reason: String) = apply {
        actions.add(AutomationAction.AddSugarPointsAction(points = points, reason = reason))
    }
    
    // Action builders - System
    fun showNotification(title: String, message: String, priority: AutomationAction.ShowNotificationAction.NotificationPriority = 
                         AutomationAction.ShowNotificationAction.NotificationPriority.DEFAULT) = apply {
        actions.add(AutomationAction.ShowNotificationAction(
            title = title,
            message = message,
            priority = priority
        ))
    }
    
    fun showToast(message: String, duration: AutomationAction.ShowToastAction.ToastDuration = 
                  AutomationAction.ShowToastAction.ToastDuration.SHORT) = apply {
        actions.add(AutomationAction.ShowToastAction(message = message, duration = duration))
    }
    
    fun vibrate(pattern: AutomationAction.VibrateAction.VibrationPattern = 
                AutomationAction.VibrateAction.VibrationPattern.SHORT) = apply {
        actions.add(AutomationAction.VibrateAction(pattern = pattern))
    }
    
    fun setBrightness(level: Float) = apply {
        actions.add(AutomationAction.SetBrightnessAction(level = level))
    }
    
    fun wait(durationMs: Long) = apply {
        actions.add(AutomationAction.WaitAction(durationMs = durationMs))
    }
    
    // Control flow
    fun ifThen(condition: AutomationCondition, thenActions: List<AutomationAction>, 
               elseActions: List<AutomationAction> = emptyList()) = apply {
        actions.add(AutomationAction.ConditionalAction(
            condition = condition,
            thenActions = thenActions,
            elseActions = elseActions
        ))
    }
    
    fun runTask(taskId: String) = apply {
        actions.add(AutomationAction.RunTaskAction(taskId = taskId))
    }
    
    // ═════════════════════════════════════════════════════════════
    // BUILD METHODS
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Build the task without saving
     */
    fun build(): AutomationTask {
        val trigger = trigger ?: throw IllegalStateException("Trigger is required")
        
        if (actions.isEmpty()) {
            throw IllegalStateException("At least one action is required")
        }
        
        return AutomationTask(
            id = taskId,
            name = name,
            description = description,
            trigger = trigger,
            conditions = conditions.toList(),
            actions = actions.toList(),
            templateId = templateId
        )
    }
    
    /**
     * Build and save the task
     */
    suspend fun buildAndSave(): AutomationTask {
        val task = build()
        AutomationRepository.getInstance(context).saveTask(task)
        _builderState.value = BuilderState.Saved(task)
        return task
    }
    
    /**
     * Build and execute immediately
     */
    suspend fun buildAndRun(): AutomationTask {
        val task = build()
        AutomationEngine.getInstance(context).runTask(task.id, "builder")
        _builderState.value = BuilderState.Executed(task)
        return task
    }
    
    /**
     * Validate the current task configuration
     */
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (name.isBlank()) {
            errors.add("Name is required")
        }
        
        if (trigger == null) {
            errors.add("Trigger is required")
        }
        
        if (actions.isEmpty()) {
            errors.add("At least one action is required")
        }
        
        // Validate specific triggers
        trigger?.let { t ->
            when (t) {
                is AutomationTrigger.GeofenceTrigger -> {
                    if (t.radiusMeters < 10) {
                        errors.add("Geofence radius must be at least 10 meters")
                    }
                }
                is AutomationTrigger.TimeTrigger -> {
                    if (t.hour !in 0..23 || t.minute !in 0..59) {
                        errors.add("Invalid time specified")
                    }
                }
                else -> {}
            }
        }
        
        // Validate actions
        actions.forEachIndexed { index, action ->
            when (action) {
                is AutomationAction.ChangeThemeAction -> {
                    if (action.themeId.isBlank()) {
                        errors.add("Action ${index + 1}: Theme ID is required")
                    }
                }
                is AutomationAction.EnableEffectAction, 
                is AutomationAction.DisableEffectAction,
                is AutomationAction.ToggleEffectAction -> {
                    // Could validate effect exists
                }
                else -> {}
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Reset the builder to initial state
     */
    fun reset() {
        taskId = UUID.randomUUID().toString()
        name = ""
        description = ""
        trigger = null
        conditions.clear()
        actions.clear()
        templateId = null
        _builderState.value = BuilderState.Empty
    }
    
    /**
     * Load an existing task for editing
     */
    fun loadTask(task: AutomationTask) {
        taskId = task.id
        name = task.name
        description = task.description
        trigger = task.trigger
        conditions.clear()
        conditions.addAll(task.conditions)
        actions.clear()
        actions.addAll(task.actions)
        templateId = task.templateId
        _builderState.value = BuilderState.Loaded(task)
    }
    
    // ═════════════════════════════════════════════════════════════
    // TEMPLATE METHODS
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Save current configuration as a template
     */
    suspend fun saveAsTemplate(templateName: String, description: String = ""): AutomationTask {
        val task = build().copy(
            id = UUID.randomUUID().toString(),
            name = templateName,
            description = description,
            isTemplate = true
        )
        AutomationRepository.getInstance(context).saveTask(task)
        return task
    }
    
    companion object {
        /**
         * Create a builder from a template
         */
        fun fromTemplate(context: Context, template: AutomationTask): TaskBuilder {
            return TaskBuilder(context).apply {
                loadTask(template)
                this.templateId = template.id
                // Generate new ID so it's a new task based on template
                taskId = UUID.randomUUID().toString()
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// BUILDER STATES
// ═════════════════════════════════════════════════════════════

sealed class BuilderState {
    object Empty : BuilderState()
    data class Loaded(val task: AutomationTask) : BuilderState()
    data class Saved(val task: AutomationTask) : BuilderState()
    data class Executed(val task: AutomationTask) : BuilderState()
    data class Error(val message: String) : BuilderState()
}

// ═════════════════════════════════════════════════════════════
// VALIDATION RESULT
// ═════════════════════════════════════════════════════════════

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult()
    
    val isValid: Boolean get() = this is Valid
}

// ═════════════════════════════════════════════════════════════
// VISUAL TASK BUILDER - UI State Management
// ═════════════════════════════════════════════════════════════

/**
 * Manages the visual task builder UI state
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
    
    fun setStep(step: BuilderStep) {
        _currentStep.value = step
    }
    
    fun nextStep() {
        val current = _currentStep.value
        val next = BuilderStep.values().getOrNull(current.ordinal + 1)
        if (next != null) {
            _currentStep.value = next
        }
    }
    
    fun previousStep() {
        val current = _currentStep.value
        val prev = BuilderStep.values().getOrNull(current.ordinal - 1)
        if (prev != null) {
            _currentStep.value = prev
        }
    }
    
    fun setTrigger(trigger: AutomationTrigger) {
        _selectedTrigger.value = trigger
    }
    
    fun addCondition(condition: AutomationCondition) {
        _selectedConditions.value = _selectedConditions.value + condition
    }
    
    fun removeCondition(condition: AutomationCondition) {
        _selectedConditions.value = _selectedConditions.value - condition
    }
    
    fun addAction(action: AutomationAction) {
        _selectedActions.value = _selectedActions.value + action
    }
    
    fun removeAction(action: AutomationAction) {
        _selectedActions.value = _selectedActions.value - action
    }
    
    fun moveAction(fromIndex: Int, toIndex: Int) {
        val current = _selectedActions.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _selectedActions.value = current
        }
    }
    
    fun setTaskName(name: String) {
        _taskName.value = name
    }
    
    fun setTaskDescription(description: String) {
        _taskDescription.value = description
    }
    
    fun reset() {
        _currentStep.value = BuilderStep.TRIGGER
        _selectedTrigger.value = null
        _selectedConditions.value = emptyList()
        _selectedActions.value = emptyList()
        _taskName.value = ""
        _taskDescription.value = ""
    }
    
    fun loadTask(task: AutomationTask) {
        _taskName.value = task.name
        _taskDescription.value = task.description
        _selectedTrigger.value = task.trigger
        _selectedConditions.value = task.conditions
        _selectedActions.value = task.actions
    }
    
    fun isValid(): Boolean {
        return _taskName.value.isNotBlank() &&
               _selectedTrigger.value != null &&
               _selectedActions.value.isNotEmpty()
    }
    
    fun buildTask(): AutomationTask? {
        if (!isValid()) return null
        
        return AutomationTask(
            id = UUID.randomUUID().toString(),
            name = _taskName.value,
            description = _taskDescription.value,
            trigger = _selectedTrigger.value!!,
            conditions = _selectedConditions.value,
            actions = _selectedActions.value
        )
    }
}

enum class BuilderStep {
    TRIGGER,
    CONDITIONS,
    ACTIONS,
    REVIEW
}

// ═════════════════════════════════════════════════════════════
// ACTION CATEGORIES FOR UI
// ═════════════════════════════════════════════════════════════

object ActionCategories {
    
    data class Category(
        val id: String,
        val name: String,
        val icon: String, // Would be actual icon resource in real implementation
        val description: String,
        val actions: List<ActionItem>
    )
    
    data class ActionItem(
        val id: String,
        val name: String,
        val description: String,
        val createAction: () -> AutomationAction
    )
    
    val categories = listOf(
        Category(
            id = "effects",
            name = "Effects",
            icon = "sparkles",
            description = "Enable, disable or modify visual effects",
            actions = listOf(
                ActionItem("enable_effect", "Enable Effect", "Turn on a visual effect") {
                    AutomationAction.EnableEffectAction(effectId = "")
                },
                ActionItem("disable_effect", "Disable Effect", "Turn off a visual effect") {
                    AutomationAction.DisableEffectAction(effectId = "")
                },
                ActionItem("toggle_effect", "Toggle Effect", "Switch an effect on/off") {
                    AutomationAction.ToggleEffectAction(effectId = "")
                }
            )
        ),
        Category(
            id = "themes",
            name = "Themes",
            icon = "palette",
            description = "Change themes and adjust intensity",
            actions = listOf(
                ActionItem("change_theme", "Change Theme", "Switch to a specific theme") {
                    AutomationAction.ChangeThemeAction(themeId = "")
                },
                ActionItem("random_theme", "Random Theme", "Apply a random theme") {
                    AutomationAction.RandomThemeAction()
                },
                ActionItem("set_intensity", "Set Intensity", "Adjust theme intensity") {
                    AutomationAction.SetThemeIntensityAction(intensity = 1.0f)
                }
            )
        ),
        Category(
            id = "sugarmunch",
            name = "SugarMunch",
            icon = "candy",
            description = "SugarMunch-specific actions",
            actions = listOf(
                ActionItem("claim_reward", "Claim Reward", "Claim daily reward") {
                    AutomationAction.ClaimRewardAction()
                },
                ActionItem("open_screen", "Open Screen", "Navigate to a screen") {
                    AutomationAction.LaunchSugarMunchScreenAction(
                        screen = AutomationAction.LaunchSugarMunchScreenAction.SugarMunchScreen.CATALOG
                    )
                },
                ActionItem("add_points", "Add Points", "Add Sugar Points") {
                    AutomationAction.AddSugarPointsAction(points = 10, reason = "automation")
                }
            )
        ),
        Category(
            id = "system",
            name = "System",
            icon = "settings",
            description = "System actions and notifications",
            actions = listOf(
                ActionItem("notification", "Show Notification", "Display a notification") {
                    AutomationAction.ShowNotificationAction(title = "", message = "")
                },
                ActionItem("toast", "Show Toast", "Show a toast message") {
                    AutomationAction.ShowToastAction(message = "")
                },
                ActionItem("vibrate", "Vibrate", "Vibrate the device") {
                    AutomationAction.VibrateAction()
                },
                ActionItem("brightness", "Set Brightness", "Adjust screen brightness") {
                    AutomationAction.SetBrightnessAction(level = 0.5f)
                }
            )
        ),
        Category(
            id = "apps",
            name = "Apps",
            icon = "apps",
            description = "Launch and manage apps",
            actions = listOf(
                ActionItem("open_app", "Open App", "Launch an app") {
                    AutomationAction.OpenAppAction(packageName = "")
                },
                ActionItem("share_app", "Share App", "Share an app") {
                    AutomationAction.ShareAppAction(packageName = "")
                }
            )
        ),
        Category(
            id = "control",
            name = "Control Flow",
            icon = "flow",
            description = "Control execution flow",
            actions = listOf(
                ActionItem("wait", "Wait", "Pause for a duration") {
                    AutomationAction.WaitAction(durationMs = 1000)
                },
                ActionItem("condition", "Condition", "Conditional execution") {
                    AutomationAction.ConditionalAction(
                        condition = AutomationCondition.TimeCondition(0, 0, 23, 59),
                        thenActions = emptyList()
                    )
                },
                ActionItem("run_task", "Run Task", "Execute another task") {
                    AutomationAction.RunTaskAction(taskId = "")
                }
            )
        )
    )
}

// ═════════════════════════════════════════════════════════════
// TRIGGER CATEGORIES FOR UI
// ═════════════════════════════════════════════════════════════

object TriggerCategories {
    
    data class Category(
        val id: String,
        val name: String,
        val icon: String,
        val description: String,
        val triggers: List<TriggerItem>
    )
    
    data class TriggerItem(
        val id: String,
        val name: String,
        val description: String,
        val createTrigger: () -> AutomationTrigger
    )
    
    val categories = listOf(
        Category(
            id = "time",
            name = "Time",
            icon = "clock",
            description = "Schedule based on time",
            triggers = listOf(
                TriggerItem("specific_time", "Specific Time", "Trigger at a specific time") {
                    AutomationTrigger.TimeTrigger(hour = 12, minute = 0)
                },
                TriggerItem("interval", "Interval", "Trigger periodically") {
                    AutomationTrigger.IntervalTrigger(intervalMinutes = 60)
                },
                TriggerItem("sunrise", "Sunrise", "Trigger at sunrise") {
                    AutomationTrigger.SunriseSunsetTrigger(
                        event = AutomationTrigger.SunriseSunsetTrigger.SunEvent.SUNRISE
                    )
                },
                TriggerItem("sunset", "Sunset", "Trigger at sunset") {
                    AutomationTrigger.SunriseSunsetTrigger(
                        event = AutomationTrigger.SunriseSunsetTrigger.SunEvent.SUNSET
                    )
                }
            )
        ),
        Category(
            id = "apps",
            name = "Apps",
            icon = "apps",
            description = "Trigger when apps open or close",
            triggers = listOf(
                TriggerItem("app_opened", "App Opened", "When an app is launched") {
                    AutomationTrigger.AppOpenedTrigger(packageNames = emptyList())
                },
                TriggerItem("app_closed", "App Closed", "When an app is closed") {
                    AutomationTrigger.AppClosedTrigger(packageNames = emptyList())
                }
            )
        ),
        Category(
            id = "sugarmunch",
            name = "SugarMunch",
            icon = "candy",
            description = "Trigger on SugarMunch events",
            triggers = listOf(
                TriggerItem("effect_toggled", "Effect Toggled", "When an effect changes") {
                    AutomationTrigger.EffectToggledTrigger()
                },
                TriggerItem("theme_changed", "Theme Changed", "When theme changes") {
                    AutomationTrigger.ThemeChangedTrigger()
                },
                TriggerItem("reward_claimed", "Reward Claimed", "When a reward is claimed") {
                    AutomationTrigger.RewardClaimedTrigger()
                }
            )
        ),
        Category(
            id = "system",
            name = "System",
            icon = "settings",
            description = "Trigger on system events",
            triggers = listOf(
                TriggerItem("battery_level", "Battery Level", "When battery reaches a level") {
                    AutomationTrigger.BatteryLevelTrigger(level = 20)
                },
                TriggerItem("charging", "Charging", "When charging state changes") {
                    AutomationTrigger.ChargingTrigger(
                        state = AutomationTrigger.ChargingTrigger.ChargingState.PLUGGED_IN
                    )
                },
                TriggerItem("wifi", "WiFi Connected", "When WiFi connects") {
                    AutomationTrigger.WifiConnectedTrigger()
                },
                TriggerItem("screen", "Screen State", "When screen turns on/off") {
                    AutomationTrigger.ScreenStateTrigger(
                        state = AutomationTrigger.ScreenStateTrigger.ScreenState.ON
                    )
                }
            )
        ),
        Category(
            id = "location",
            name = "Location",
            icon = "location",
            description = "Trigger based on location",
            triggers = listOf(
                TriggerItem("geofence_enter", "Enter Area", "When entering a location") {
                    AutomationTrigger.GeofenceTrigger(
                        latitude = 0.0,
                        longitude = 0.0,
                        radiusMeters = 100f,
                        transition = AutomationTrigger.GeofenceTrigger.GeofenceTransition.ENTER
                    )
                },
                TriggerItem("geofence_exit", "Exit Area", "When leaving a location") {
                    AutomationTrigger.GeofenceTrigger(
                        latitude = 0.0,
                        longitude = 0.0,
                        radiusMeters = 100f,
                        transition = AutomationTrigger.GeofenceTrigger.GeofenceTransition.EXIT
                    )
                }
            )
        ),
        Category(
            id = "sensor",
            name = "Sensor",
            icon = "sensors",
            description = "Trigger using device sensors",
            triggers = listOf(
                TriggerItem("shake", "Shake", "When device is shaken") {
                    AutomationTrigger.ShakeTrigger()
                },
                TriggerItem("orientation", "Orientation", "When device orientation changes") {
                    AutomationTrigger.OrientationTrigger(
                        orientation = AutomationTrigger.OrientationTrigger.DeviceOrientation.FACE_UP
                    )
                }
            )
        ),
        Category(
            id = "manual",
            name = "Manual",
            icon = "touch",
            description = "Trigger manually or from shortcut",
            triggers = listOf(
                TriggerItem("manual", "Manual Trigger", "Run on demand") {
                    AutomationTrigger.ManualTrigger()
                }
            )
        )
    )
}
