package com.sugarmunch.app.automation

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Pre-built automation templates for common use cases
 * Think "IFTTT recipes" for SugarMunch
 */
object AutomationTemplates {
    
    data class Template(
        val id: String,
        val name: String,
        val description: String,
        val icon: String,
        val category: TemplateCategory,
        val difficulty: Difficulty,
        val createTask: () -> AutomationTask
    )
    
    enum class TemplateCategory {
        PRODUCTIVITY, GAMING, WELLNESS, BATTERY, SOCIAL, CUSTOMIZATION, SYSTEM
    }
    
    enum class Difficulty {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
    
    // ═════════════════════════════════════════════════════════════
    // FOCUS MODE - Work hours = focus theme + dim effects
    // ═════════════════════════════════════════════════════════════
    
    private val focusModeTemplate = Template(
        id = "template_focus_mode",
        name = "Focus Mode",
        description = "Automatically switch to a calm theme and disable distracting effects during work hours",
        icon = "work",
        category = TemplateCategory.PRODUCTIVITY,
        difficulty = Difficulty.BEGINNER
    ) {
        AutomationTask(
            id = UUID.randomUUID().toString(),
            name = "Focus Mode",
            description = "Work hours automation - keeps you focused",
            enabled = true,
            isTemplate = false,
            templateId = "template_focus_mode",
            trigger = AutomationTrigger.TimeTrigger(
                hour = 9,
                minute = 0,
                repeatDays = listOf(1, 2, 3, 4, 5) // Weekdays
            ),
            conditions = emptyList(),
            actions = listOf(
                AutomationAction.ChangeThemeAction(
                    themeId = "chill_mint",
                    intensity = 0.5f,
                    animate = true
                ),
                AutomationAction.DisableEffectAction(effectId = "sugarrush"),
                AutomationAction.DisableEffectAction(effectId = "confetti"),
                AutomationAction.ShowNotificationAction(
                    title = "Focus Mode Activated",
                    message = "Stay productive! Distractions minimized.",
                    priority = AutomationAction.ShowNotificationAction.NotificationPriority.LOW
                )
            )
        )
    }
    
    // ═════════════════════════════════════════════════════════════
    // GAMING MODE - Gaming apps = gaming preset
    // ═════════════════════════════════════════════════════════════
    
    private val gamingModeTemplate = Template(
        id = "template_gaming_mode",
        name = "Gaming Mode",
        description = "Enable high-energy effects and themes when launching games",
        icon = "games",
        category = TemplateCategory.GAMING,
        difficulty = Difficulty.BEGINNER
    ) {
        AutomationTask(
            id = UUID.randomUUID().toString(),
            name = "Gaming Mode",
            description = "Gaming preset with maximum effects",
            enabled = true,
            isTemplate = false,
            templateId = "template_gaming_mode",
            trigger = AutomationTrigger.AppOpenedTrigger(
                packageNames = listOf(
                    "com.android.game",
                    "com.tencent.game",
                    "com.activision.callofduty",
                    "com.epicgames.fortnite",
                    "com.supercell.clashofclans",
                    "com.roblox.client",
                    "com.mojang.minecraftpe"
                ),
                matchAll = false
            ),
            conditions = listOf(
                AutomationCondition.BatteryCondition(minLevel = 20)
            ),
            actions = listOf(
                AutomationAction.ChangeThemeAction(
                    themeId = "sugarrush_nuclear",
                    intensity = 1.5f,
                    animate = true
                ),
                AutomationAction.EnableEffectAction(
                    effectId = "sugarrush",
                    intensity = 1.5f
                ),
                AutomationAction.SetThemeIntensityAction(
                    intensity = 1.8f,
                    component = AutomationAction.SetThemeIntensityAction.ThemeComponent.ALL
                ),
                AutomationAction.ShowToastAction(
                    message = "GAMING MODE ACTIVATED!",
                    duration = AutomationAction.ShowToastAction.ToastDuration.SHORT
                )
            )
        )
    }
    
    // ═════════════════════════════════════════════════════════════
    // BEDTIME - 10pm = night theme + all effects off
    // ═════════════════════════════════════════════════════════════
    
    private val bedtimeTemplate = Template(
        id = "template_bedtime",
        name = "Bedtime",
        description = "Prepare for sleep with a dark theme and all effects disabled",
        icon = "bedtime",
        category = TemplateCategory.WELLNESS,
        difficulty = Difficulty.BEGINNER
    ) {
        AutomationTask(
            id = UUID.randomUUID().toString(),
            name = "Bedtime",
            description = "Wind down for better sleep",
            enabled = true,
            isTemplate = false,
            templateId = "template_bedtime",
            trigger = AutomationTrigger.TimeTrigger(
                hour = 22,
                minute = 0,
                repeatDays = listOf(0, 1, 2, 3, 4, 5, 6) // Every day
            ),
            conditions = emptyList(),
            actions = listOf(
                AutomationAction.ChangeThemeAction(
                    themeId = "dark_berry",
                    intensity = 0.3f,
                    animate = true
                ),
                AutomationAction.DisableEffectAction(effectId = "sugarrush"),
                AutomationAction.DisableEffectAction(effectId = "rainbow_tint"),
                AutomationAction.DisableEffectAction(effectId = "confetti"),
                AutomationAction.DisableEffectAction(effectId = "heartbeat"),
                AutomationAction.SetBrightnessAction(
                    level = 0.2f,
                    temporary = true
                ),
                AutomationAction.ShowNotificationAction(
                    title = "Bedtime Mode",
                    message = "Sweet dreams! Effects disabled for better sleep.",
                    priority = AutomationAction.ShowNotificationAction.NotificationPriority.LOW
                )
            )
        )
    }
    
    // ═════════════════════════════════════════════════════════════
    // MORNING BOOST - 7am = sugar rush + daily reward
    // ═════════════════════════════════════════════════════════════
    
    private val morningBoostTemplate = Template(
        id = "template_morning_boost",
        name = "Morning Boost",
        description = "Start your day with energy - claim rewards and activate sugar rush",
        icon = "sunrise",
        category = TemplateCategory.WELLNESS,
        difficulty = Difficulty.BEGINNER
    ) {
        AutomationTask(
            id = UUID.randomUUID().toString(),
            name = "Morning Boost",
            description = "Energize your morning routine",
            enabled = true,
            isTemplate = false,
            templateId = "template_morning_boost",
            trigger = AutomationTrigger.TimeTrigger(
                hour = 7,
                minute = 0,
                repeatDays = listOf(1, 2, 3, 4, 5, 6, 0)
            ),
            conditions = emptyList(),
            actions = listOf(
                AutomationAction.ChangeThemeAction(
                    themeId = "sugarrush_classic",
                    intensity = 1.2f,
                    animate = true
                ),
                AutomationAction.EnableEffectAction(
                    effectId = "sugarrush",
                    intensity = 1.0f
                ),
                AutomationAction.ClaimRewardAction(),
                AutomationAction.ShowToastAction(
                    message = "Good morning! Time to crush your goals!",
                    duration = AutomationAction.ShowToastAction.ToastDuration.LONG
                ),
                AutomationAction.VibrateAction(
                    pattern = AutomationAction.VibrateAction.VibrationPattern.DOUBLE
                )
            )
        )
    }
    
    // ═════════════════════════════════════════════════════════════
    // BATTERY SAVER - <20% = minimal effects
    // ═════════════════════════════════════════════════════════════
    
    private val batterySaverTemplate = Template(
        id = "template_battery_saver",
        name = "Battery Saver",
        description = "Automatically reduce effects when battery is low to save power",
        icon = "battery",
        category = TemplateCategory.BATTERY,
        difficulty = Difficulty.BEGINNER
    ) {
        AutomationTask(
            id = UUID.randomUUID().toString(),
            name = "Battery Saver",
            description = "Preserve battery when running low",
            enabled = true,
            isTemplate = false,
            templateId = "template_battery_saver",
            trigger = AutomationTrigger.BatteryLevelTrigger(
                level = 20,
                operator = AutomationTrigger.BatteryLevelTrigger.ComparisonOperator.LESS_THAN_OR_EQUAL
            ),
            conditions = emptyList(),
            actions = listOf(
                AutomationAction.ChangeThemeAction(
                    themeId = "chill_mint",
                    intensity = 0.3f
                ),
                AutomationAction.DisableEffectAction(effectId = "sugarrush"),
                AutomationAction.DisableEffectAction(effectId = "rainbow_tint"),
                AutomationAction.DisableEffectAction(effectId = "confetti"),
                AutomationAction.SetThemeIntensityAction(
                    intensity = 0.3f,
                    component = AutomationAction.SetThemeIntensityAction.ThemeComponent.PARTICLES
                ),
                AutomationAction.ShowNotificationAction(
                    title = "Battery Saver Activated",
                    message = "Effects reduced to save power. Charge soon!",
                    priority = AutomationAction.ShowNotificationAction.NotificationPriority.HIGH
                )
            )
        )
    }
    
    // ═════════════════════════════════════════════════════════════
    // WORKOUT MODE - Shake = high energy
    // ═════════════════════════════════════════════════════════════
    
    private val workoutModeTemplate = Template(
        id = "template_workout",
        name = "Workout Mode",
        description = "Shake your device to activate high-energy effects for workouts",
        icon = "fitness",
        category = TemplateCategory.WELLNESS,
        difficulty = Difficulty.INTERMEDIATE
    ) {
        AutomationTask(
            id = UUID.randomUUID().toString(),
            name = "Workout Mode",
            description = "High-energy mode for exercise",
            enabled = true,
            isTemplate = false,
            templateId = "template_workout",
            trigger = AutomationTrigger.ShakeTrigger(
                sensitivity = AutomationTrigger.ShakeTrigger.ShakeSensitivity.HIGH,
                minShakes = 2
            ),
            conditions = listOf(
                AutomationCondition.TimeCondition(
                    startHour = 6,
                    startMinute = 0,
                    endHour = 22,
                    endMinute = 0
                )
            ),
            actions = listOf(
                AutomationAction.ChangeThemeAction(
                    themeId = "sugarrush_volcano",
                    intensity = 2.0f
                ),
                AutomationAction.EnableEffectAction(effectId = "sugarrush"),
                AutomationAction.EnableEffectAction(effectId = "heartbeat"),
                AutomationAction.SetThemeIntensityAction(intensity = 2.0f),
                AutomationAction.ShowToastAction(
                    message = "WORKOUT MODE! Let's go!",
                    duration = AutomationAction.ShowToastAction.ToastDuration.SHORT
                ),
                AutomationAction.VibrateAction(
                    pattern = AutomationAction.VibrateAction.VibrationPattern.HEARTBEAT
                )
            )
        )
    }
    
    // ═════════════════════════════════════════════════════════════
    // NIGHT OWL - Sunset = darker theme
    // ═════════════════════════════════════════════════════════════
    
    private val nightOwlTemplate = Template(
        id = "template_night_owl",
        name = "Night Owl",
        description = "Automatically switch to a darker theme at sunset for easier viewing",
        icon = "moon",
        category = TemplateCategory.CUSTOMIZATION,
        difficulty = Difficulty.BEGINNER
    ) {
        AutomationTask(
            id = UUID.randomUUID().toString(),
            name = "Night Owl",
            description = "Dark theme at sunset",
            enabled = true,
            isTemplate = false,
            templateId = "template_night_owl",
            trigger = AutomationTrigger.SunriseSunsetTrigger(
                event = AutomationTrigger.SunriseSunsetTrigger.SunEvent.SUNSET,
                offsetMinutes = -30 // 30 minutes before sunset
            ),
            conditions = emptyList(),
            actions = listOf(
                AutomationAction.ChangeThemeAction(
                    themeId = "dark_berry",
                    intensity = 0.7f,
                    animate = true
                ),
                AutomationAction.SetThemeIntensityAction(
                    intensity = 0.7f,
                    component = AutomationAction.SetThemeIntensityAction.ThemeComponent.COLORS
                ),
                AutomationAction.ShowNotificationAction(
                    title = "Night Mode Activated",
                    message = "Easier on the eyes for evening use",
                    priority = AutomationAction.ShowNotificationAction.NotificationPriority.LOW
                )
            )
        )
    }
    
    // ═════════════════════════════════════════════════════════════
    // PARTY MODE - Weekend evenings = maximum everything
    // ═════════════════════════════════════════════════════════════
    
    private val partyModeTemplate = Template(
        id = "template_party",
        name = "Party Mode",
        description = "Weekend evenings with maximum effects and random themes",
        icon = "party",
        category = TemplateCategory.SOCIAL,
        difficulty = Difficulty.INTERMEDIATE
    ) {
        AutomationTask(
            id = UUID.randomUUID().toString(),
            name = "Party Mode",
            description = "Maximum fun for weekend nights",
            enabled = true,
            isTemplate = false,
            templateId = "template_party",
            trigger = AutomationTrigger.TimeTrigger(
                hour = 20,
                minute = 0,
                repeatDays = listOf(5, 6) // Friday and Saturday
            ),
            conditions = listOf(
                AutomationCondition.BatteryCondition(minLevel = 30)
            ),
            actions = listOf(
                AutomationAction.RandomThemeAction(category = "trippy"),
                AutomationAction.SetThemeIntensityAction(intensity = 2.0f),
                AutomationAction.EnableEffectAction(effectId = "sugarrush"),
                AutomationAction.EnableEffectAction(effectId = "rainbow_tint"),
                AutomationAction.EnableEffectAction(effectId = "confetti"),
                AutomationAction.ShowToastAction(
                    message = "PARTY TIME! Let's celebrate!",
                    duration = AutomationAction.ShowToastAction.ToastDuration.LONG
                ),
                AutomationAction.VibrateAction(
                    pattern = AutomationAction.VibrateAction.VibrationPattern.TRIPLE
                )
            )
        )
    }
    
    // ═════════════════════════════════════════════════════════════
    // ARRIVAL HOME - WiFi home = chill mode
    // ═════════════════════════════════════════════════════════════
    
    private val arrivalHomeTemplate = Template(
        id = "template_arrival_home",
        name = "Arrival Home",
        description = "Switch to a relaxing theme when connecting to home WiFi",
        icon = "home",
        category = TemplateCategory.CUSTOMIZATION,
        difficulty = Difficulty.INTERMEDIATE
    ) {
        AutomationTask(
            id = UUID.randomUUID().toString(),
            name = "Arrival Home",
            description = "Relaxing mode when home",
            enabled = true,
            isTemplate = false,
            templateId = "template_arrival_home",
            trigger = AutomationTrigger.WifiConnectedTrigger(
                ssid = "Home", // User should customize this
                anyWifi = false
            ),
            conditions = emptyList(),
            actions = listOf(
                AutomationAction.ChangeThemeAction(
                    themeId = "chill_chocolate",
                    intensity = 0.6f
                ),
                AutomationAction.DisableEffectAction(effectId = "sugarrush"),
                AutomationAction.SetThemeIntensityAction(
                    intensity = 0.6f,
                    component = AutomationAction.SetThemeIntensityAction.ThemeComponent.ALL
                ),
                AutomationAction.ShowToastAction(
                    message = "Welcome home! Time to relax.",
                    duration = AutomationAction.ShowToastAction.ToastDuration.SHORT
                )
            )
        )
    }
    
    // ═════════════════════════════════════════════════════════════
    // SCREEN TIME REWARD - Use limits = rewards
    // ═════════════════════════════════════════════════════════════
    
    private val screenTimeRewardTemplate = Template(
        id = "template_screen_time",
        name = "Screen Time Reward",
        description = "Claim rewards after extended app usage sessions",
        icon = "timer",
        category = TemplateCategory.PRODUCTIVITY,
        difficulty = Difficulty.ADVANCED
    ) {
        AutomationTask(
            id = UUID.randomUUID().toString(),
            name = "Screen Time Reward",
            description = "Reward for extended usage",
            enabled = true,
            isTemplate = false,
            templateId = "template_screen_time",
            trigger = AutomationTrigger.IntervalTrigger(
                intervalMinutes = 120 // Every 2 hours
            ),
            conditions = listOf(
                AutomationCondition.AppRunningCondition(
                    packageNames = listOf("com.sugarmunch.app"),
                    mustBeRunning = true
                )
            ),
            actions = listOf(
                AutomationAction.AddSugarPointsAction(
                    points = 25,
                    reason = "screen_time_reward"
                ),
                AutomationAction.ShowToastAction(
                    message = "Thanks for using SugarMunch! +25 points!",
                    duration = AutomationAction.ShowToastAction.ToastDuration.SHORT
                ),
                AutomationAction.EnableEffectAction(effectId = "confetti")
            )
        )
    }
    
    // ═════════════════════════════════════════════════════════════
    // ALL TEMPLATES
    // ═════════════════════════════════════════════════════════════
    
    val allTemplates = listOf(
        focusModeTemplate,
        gamingModeTemplate,
        bedtimeTemplate,
        morningBoostTemplate,
        batterySaverTemplate,
        workoutModeTemplate,
        nightOwlTemplate,
        partyModeTemplate,
        arrivalHomeTemplate,
        screenTimeRewardTemplate
    )
    
    fun getTemplatesByCategory(category: TemplateCategory): List<Template> {
        return allTemplates.filter { it.category == category }
    }
    
    fun getTemplatesByDifficulty(difficulty: Difficulty): List<Template> {
        return allTemplates.filter { it.difficulty == difficulty }
    }
    
    fun getTemplateById(id: String): Template? {
        return allTemplates.find { it.id == id }
    }
}

// ═════════════════════════════════════════════════════════════
// TEMPLATE MANAGER
// ═════════════════════════════════════════════════════════════

class TemplateManager private constructor(private val context: Context) {
    
    private val repository = AutomationRepository.getInstance(context)
    private val engine = AutomationEngine.getInstance(context)
    
    /**
     * Get all available templates
     */
    fun getAllTemplates(): List<AutomationTemplates.Template> {
        return AutomationTemplates.allTemplates
    }
    
    /**
     * Apply a template by creating a new automation task
     */
    suspend fun applyTemplate(templateId: String, customizations: TaskCustomizations? = null): AutomationTask {
        val template = AutomationTemplates.getTemplateById(templateId)
            ?: throw IllegalArgumentException("Template not found: $templateId")
        
        val baseTask = template.createTask()
        
        val customizedTask = customizations?.let { applyCustomizations(baseTask, it) } ?: baseTask
        
        // Save and start monitoring
        repository.saveTask(customizedTask)
        engine.start()
        
        return customizedTask
    }
    
    /**
     * Preview what a template would create without saving
     */
    fun previewTemplate(templateId: String): AutomationTask? {
        val template = AutomationTemplates.getTemplateById(templateId) ?: return null
        return template.createTask()
    }
    
    /**
     * Get user-created templates (saved tasks marked as templates)
     */
    fun getUserTemplates(): Flow<List<AutomationTask>> {
        return repository.getAllTemplates()
    }
    
    /**
     * Save a task as a custom template
     */
    suspend fun saveAsTemplate(task: AutomationTask, templateName: String, description: String) {
        val templateTask = task.copy(
            id = java.util.UUID.randomUUID().toString(),
            name = templateName,
            description = description,
            isTemplate = true
        )
        repository.saveTask(templateTask)
    }
    
    /**
     * Delete a user-created template
     */
    suspend fun deleteTemplate(templateId: String) {
        repository.deleteTask(templateId)
    }
    
    private fun applyCustomizations(task: AutomationTask, customizations: TaskCustomizations): AutomationTask {
        return task.copy(
            name = customizations.name ?: task.name,
            trigger = customizations.trigger ?: task.trigger,
            actions = if (customizations.additionalActions.isNotEmpty()) {
                task.actions + customizations.additionalActions
            } else {
                task.actions
            },
            conditions = if (customizations.additionalConditions.isNotEmpty()) {
                task.conditions + customizations.additionalConditions
            } else {
                task.conditions
            }
        )
    }
    
    companion object {
        @Volatile
        private var instance: TemplateManager? = null
        
        fun getInstance(context: Context): TemplateManager {
            return instance ?: synchronized(this) {
                instance ?: TemplateManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * Customizations to apply when creating a task from a template
 */
data class TaskCustomizations(
    val name: String? = null,
    val trigger: AutomationTrigger? = null,
    val additionalActions: List<AutomationAction> = emptyList(),
    val additionalConditions: List<AutomationCondition> = emptyList()
)

// ═════════════════════════════════════════════════════════════
// TEMPLATE EXTENSIONS
// ═════════════════════════════════════════════════════════════

fun AutomationTemplates.Template.getPreviewDescription(): String {
    val task = createTask()
    val triggerDesc = when (val t = task.trigger) {
        is AutomationTrigger.TimeTrigger -> "Daily at ${t.hour}:${t.minute.toString().padStart(2, '0')}"
        is AutomationTrigger.AppOpenedTrigger -> "When ${t.packageNames.size} apps open"
        is AutomationTrigger.BatteryLevelTrigger -> "When battery ${t.operator.name.lowercase().replace('_', ' ')} ${t.level}%"
        is AutomationTrigger.ShakeTrigger -> "When device shakes"
        is AutomationTrigger.WifiConnectedTrigger -> "When WiFi connects"
        else -> "On trigger"
    }
    
    val actionCount = task.actions.size
    return "$triggerDesc • $actionCount actions"
}

fun AutomationTemplates.Template.getCategoryColor(): String {
    return when (category) {
        AutomationTemplates.TemplateCategory.PRODUCTIVITY -> "#4CAF50"
        AutomationTemplates.TemplateCategory.GAMING -> "#FF5722"
        AutomationTemplates.TemplateCategory.WELLNESS -> "#9C27B0"
        AutomationTemplates.TemplateCategory.BATTERY -> "#FFC107"
        AutomationTemplates.TemplateCategory.SOCIAL -> "#E91E63"
        AutomationTemplates.TemplateCategory.CUSTOMIZATION -> "#2196F3"
        AutomationTemplates.TemplateCategory.SYSTEM -> "#607D8B"
    }
}
