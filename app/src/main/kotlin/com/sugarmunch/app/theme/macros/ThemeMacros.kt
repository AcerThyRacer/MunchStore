package com.sugarmunch.app.theme.macros

import com.sugarmunch.app.theme.model.CandyTheme
import com.sugarmunch.app.theme.model.GranularThemeConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Programmable Theme Macros System
 * 
 * Allows users to create conditional and animated theme behaviors:
 * - Time-based transitions (circadian rhythm)
 * - App-launch triggers (gaming mode)
 * - Reactive to music/weather/battery
 * - Animate intensity over time
 * - Custom scripts with conditions and actions
 */

/**
 * A theme macro - conditional or animated theme behavior
 */
@Serializable
data class ThemeMacro(
    val id: String = "macro_${System.currentTimeMillis()}",
    val name: String,
    val description: String,
    val isEnabled: Boolean = true,
    val trigger: MacroTrigger,
    val action: MacroAction,
    val conditions: List<MacroCondition> = emptyList()
) {
    /**
     * Check if macro should execute
     */
    fun shouldExecute(context: MacroContext): Boolean {
        if (!isEnabled) return false
        
        // Check all conditions
        if (conditions.any { !it.evaluate(context) }) return false
        
        // Check trigger
        return trigger.shouldFire(context)
    }
}

/**
 * Context for macro evaluation
 */
data class MacroContext(
    val currentTime: Long = System.currentTimeMillis(),
    val currentApp: String? = null,
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val isPlayingMusic: Boolean = false,
    val weatherCondition: WeatherCondition? = null,
    val currentTheme: CandyTheme? = null,
    val granularConfig: GranularThemeConfig? = null
)

/**
 * Weather conditions for weather-based triggers
 */
@Serializable
enum class WeatherCondition(val displayName: String) {
    SUNNY("Sunny"),
    CLOUDY("Cloudy"),
    RAINY("Rainy"),
    SNOWY("Snowy"),
    STORMY("Stormy"),
    FOGGY("Foggy")
}

// ═════════════════════════════════════════════════════════════
// TRIGGERS
// ═════════════════════════════════════════════════════════════

/**
 * Macro trigger - what causes the macro to fire
 */
@Serializable
sealed class MacroTrigger {
    abstract fun shouldFire(context: MacroContext): Boolean

    /**
     * Time-based trigger
     */
    @Serializable
    data class TimeTrigger(
        val hour: Int,
        val minute: Int,
        val daysOfWeek: Set<Int> = emptySet() // 0=Sunday, 6=Saturday
    ) : MacroTrigger() {
        override fun shouldFire(context: MacroContext): Boolean {
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = context.currentTime
            }
            val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(java.util.Calendar.MINUTE)
            val currentDay = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1
            
            if (currentHour != hour || currentMinute != minute) return false
            
            return daysOfWeek.isEmpty() || currentDay in daysOfWeek
        }
    }

    /**
     * App launch trigger
     */
    @Serializable
    data class AppTrigger(
        val packageNames: Set<String>,
        val triggerOnOpen: Boolean = true,
        val triggerOnClose: Boolean = false
    ) : MacroTrigger() {
        override fun shouldFire(context: MacroContext): Boolean {
            return context.currentApp in packageNames
        }
    }

    /**
     * Battery level trigger
     */
    @Serializable
    data class BatteryTrigger(
        val threshold: Int,
        val condition: BatteryCondition
    ) : MacroTrigger() {
        override fun shouldFire(context: MacroContext): Boolean {
            return when (condition) {
                BatteryCondition.BELOW -> context.batteryLevel < threshold
                BatteryCondition.ABOVE -> context.batteryLevel > threshold
                BatteryCondition.EXACTLY -> context.batteryLevel == threshold
                BatteryCondition.CHARGING -> context.isCharging
                BatteryCondition.NOT_CHARGING -> !context.isCharging
            }
        }
    }

    @Serializable
    enum class BatteryCondition(val displayName: String) {
        BELOW("Below"),
        ABOVE("Above"),
        EXACTLY("Exactly"),
        CHARGING("Charging"),
        NOT_CHARGING("Not Charging")
    }

    /**
     * Music playback trigger
     */
    @Serializable
    data class MusicTrigger(
        val onPlay: Boolean = true,
        val onPause: Boolean = false
    ) : MacroTrigger() {
        override fun shouldFire(context: MacroContext): Boolean {
            return context.isPlayingMusic && onPlay
        }
    }

    /**
     * Weather trigger
     */
    @Serializable
    data class WeatherTrigger(
        val conditions: Set<WeatherCondition>
    ) : MacroTrigger() {
        override fun shouldFire(context: MacroContext): Boolean {
            return context.weatherCondition in conditions
        }
    }

    /**
     * Interval trigger - fires repeatedly
     */
    @Serializable
    data class IntervalTrigger(
        val intervalMinutes: Int,
        val startTime: Long? = null,
        val endTime: Long? = null
    ) : MacroTrigger() {
        private var lastFireTime = 0L
        
        override fun shouldFire(context: MacroContext): Boolean {
            val now = context.currentTime
            
            // Check time window
            if (startTime != null && now < startTime) return false
            if (endTime != null && now > endTime) return false
            
            // Check interval
            if (now - lastFireTime >= intervalMinutes * 60_000) {
                lastFireTime = now
                return true
            }
            
            return false
        }
    }
}

// ═════════════════════════════════════════════════════════════
// ACTIONS
// ═════════════════════════════════════════════════════════════

/**
 * Macro action - what happens when the macro fires
 */
@Serializable
sealed class MacroAction {
    abstract suspend fun execute(context: MacroContext): MacroActionResult

    /**
     * Switch to a specific theme
     */
    @Serializable
    data class SwitchTheme(
        val themeId: String,
        val transitionDuration: Int = 1000
    ) : MacroAction() {
        override suspend fun execute(context: MacroContext): MacroActionResult {
            // Theme switching would be handled by ThemeManager
            return MacroActionResult.Success("Switched to theme: $themeId")
        }
    }

    /**
     * Animate intensity over time
     */
    @Serializable
    data class AnimateIntensity(
        val targetThemeIntensity: Float? = null,
        val targetBackgroundIntensity: Float? = null,
        val targetParticleIntensity: Float? = null,
        val targetAnimationIntensity: Float? = null,
        val durationMs: Long = 5000,
        val easing: EasingType = EasingType.EASE_IN_OUT
    ) : MacroAction() {
        override suspend fun execute(context: MacroContext): MacroActionResult {
            // Animation would be handled by ThemeManager
            delay(durationMs)
            return MacroActionResult.Success("Animation complete")
        }
    }

    /**
     * Apply granular theme adjustments
     */
    @Serializable
    data class ApplyGranularConfig(
        val config: GranularThemeConfig
    ) : MacroAction() {
        override suspend fun execute(context: MacroContext): MacroActionResult {
            return MacroActionResult.Success("Applied granular config")
        }
    }

    /**
     * Cycle through themes
     */
    @Serializable
    data class CycleThemes(
        val themeIds: List<String>,
        val intervalMinutes: Int = 5
    ) : MacroAction() {
        override suspend fun execute(context: MacroContext): MacroActionResult {
            return MacroActionResult.Success("Cycling through ${themeIds.size} themes")
        }
    }

    /**
     * Random theme from category
     */
    @Serializable
    data class RandomTheme(
        val category: String? = null,
        val excludeCurrent: Boolean = true
    ) : MacroAction() {
        override suspend fun execute(context: MacroContext): MacroActionResult {
            return MacroActionResult.Success("Applied random theme")
        }
    }
}

@Serializable
enum class EasingType(val displayName: String) {
    LINEAR("Linear"),
    EASE_IN("Ease In"),
    EASE_OUT("Ease Out"),
    EASE_IN_OUT("Ease In Out"),
    BOUNCE("Bounce"),
    ELASTIC("Elastic")
}

sealed class MacroActionResult {
    data class Success(val message: String) : MacroActionResult()
    data class Failure(val error: String) : MacroActionResult()
}

// ═════════════════════════════════════════════════════════════
// CONDITIONS
// ═════════════════════════════════════════════════════════════

/**
 * Macro condition - must be true for macro to execute
 */
@Serializable
sealed class MacroCondition {
    abstract fun evaluate(context: MacroContext): Boolean

    /**
     * Time range condition
     */
    @Serializable
    data class TimeRange(
        val startHour: Int,
        val startMinute: Int,
        val endHour: Int,
        val endMinute: Int
    ) : MacroCondition() {
        override fun evaluate(context: MacroContext): Boolean {
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = context.currentTime
            }
            val currentMinutes = calendar.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
                calendar.get(java.util.Calendar.MINUTE)
            val startMinutes = startHour * 60 + startMinute
            val endMinutes = endHour * 60 + endMinute
            
            return if (startMinutes <= endMinutes) {
                currentMinutes in startMinutes..endMinutes
            } else {
                currentMinutes >= startMinutes || currentMinutes <= endMinutes
            }
        }
    }

    /**
     * Battery level condition
     */
    @Serializable
    data class BatteryLevel(
        val minLevel: Int = 0,
        val maxLevel: Int = 100
    ) : MacroCondition() {
        override fun evaluate(context: MacroContext): Boolean {
            return context.batteryLevel in minLevel..maxLevel
        }
    }

    /**
     * Charging state condition
     */
    @Serializable
    data class ChargingState(
        val mustBeCharging: Boolean
    ) : MacroCondition() {
        override fun evaluate(context: MacroContext): Boolean {
            return context.isCharging == mustBeCharging
        }
    }

    /**
     * App running condition
     */
    @Serializable
    data class AppRunning(
        val packageNames: Set<String>
    ) : MacroCondition() {
        override fun evaluate(context: MacroContext): Boolean {
            return context.currentApp in packageNames
        }
    }

    /**
     * Weather condition
     */
    @Serializable
    data class Weather(
        val allowedConditions: Set<WeatherCondition>
    ) : MacroCondition() {
        override fun evaluate(context: MacroContext): Boolean {
            return context.weatherCondition in allowedConditions
        }
    }

    /**
     * Music playing condition
     */
    @Serializable
    data class MusicPlaying(
        val mustBePlaying: Boolean = true
    ) : MacroCondition() {
        override fun evaluate(context: MacroContext): Boolean {
            return context.isPlayingMusic == mustBePlaying
        }
    }
}

// ═════════════════════════════════════════════════════════════
// MACRO MANAGER
// ═════════════════════════════════════════════════════════════

/**
 * Manages theme macros and their execution
 */
class ThemeMacroManager {
    
    private val _macros = MutableStateFlow<List<ThemeMacro>>(emptyList())
    val macros: StateFlow<List<ThemeMacro>> = _macros.asStateFlow()
    
    private val _activeMacros = MutableStateFlow<List<String>>(emptyList())
    val activeMacros: StateFlow<List<String>> = _activeMacros.asStateFlow()
    
    /**
     * Add a macro
     */
    fun addMacro(macro: ThemeMacro) {
        _macros.value = _macros.value + macro
    }
    
    /**
     * Remove a macro
     */
    fun removeMacro(macroId: String) {
        _macros.value = _macros.value.filter { it.id != macroId }
    }
    
    /**
     * Update a macro
     */
    fun updateMacro(macroId: String, update: ThemeMacro.() -> ThemeMacro) {
        _macros.value = _macros.value.map { macro ->
            if (macro.id == macroId) macro.update() else macro
        }
    }
    
    /**
     * Enable/disable a macro
     */
    fun setMacroEnabled(macroId: String, enabled: Boolean) {
        updateMacro(macroId) { copy(isEnabled = enabled) }
    }
    
    /**
     * Check and execute macros
     */
    suspend fun checkAndExecute(context: MacroContext): List<MacroActionResult> {
        val results = mutableListOf<MacroActionResult>()
        
        for (macro in _macros.value) {
            if (macro.shouldExecute(context)) {
                _activeMacros.value = _activeMacros.value + macro.id
                val result = macro.action.execute(context)
                results.add(result)
                
                // Remove from active after short delay
                kotlinx.coroutines.delay(1000)
                _activeMacros.value = _activeMacros.value.filter { it != macro.id }
            }
        }
        
        return results
    }
    
    /**
     * Get macros by trigger type
     */
    fun getMacrosByTrigger(triggerClass: Class<out MacroTrigger>): List<ThemeMacro> {
        return _macros.value.filter { triggerClass.isInstance(it.trigger) }
    }
    
    /**
     * Export macros to JSON
     */
    fun exportToJson(): String {
        val json = kotlinx.serialization.json.Json { prettyPrint = true }
        return json.encodeToString(_macros.value)
    }
    
    /**
     * Import macros from JSON
     */
    fun importFromJson(jsonString: String): Result<Unit> {
        return runCatching {
            val json = kotlinx.serialization.json.Json
            val imported = json.decodeFromString<List<ThemeMacro>>(jsonString)
            _macros.value = _macros.value + imported
        }
    }
}

// ═════════════════════════════════════════════════════════════
// PRESET MACROS
// ═════════════════════════════════════════════════════════════

/**
 * Collection of preset macros for common use cases
 */
object PresetMacros {
    
    /**
     * Circadian rhythm - changes theme throughout the day
     */
    val CIRCADIAN_RHYTHM = listOf(
        ThemeMacro(
            name = "Morning Energy",
            description = "Bright, energetic theme in the morning",
            trigger = MacroTrigger.TimeTrigger(hour = 7, minute = 0, daysOfWeek = setOf(1, 2, 3, 4, 5)),
            action = MacroAction.SwitchTheme(themeId = "morning_energy"),
            conditions = listOf()
        ),
        ThemeMacro(
            name = "Day Focus",
            description = "Focused, productive theme during work hours",
            trigger = MacroTrigger.TimeTrigger(hour = 9, minute = 0, daysOfWeek = setOf(1, 2, 3, 4, 5)),
            action = MacroAction.SwitchTheme(themeId = "focus_mode"),
            conditions = listOf()
        ),
        ThemeMacro(
            name = "Evening Relax",
            description = "Calm, relaxing theme in the evening",
            trigger = MacroTrigger.TimeTrigger(hour = 18, minute = 0, daysOfWeek = setOf(1, 2, 3, 4, 5)),
            action = MacroAction.SwitchTheme(themeId = "evening_calm"),
            conditions = listOf()
        ),
        ThemeMacro(
            name = "Night Mode",
            description = "Dark, sleep-friendly theme at night",
            trigger = MacroTrigger.TimeTrigger(hour = 22, minute = 0, daysOfWeek = setOf(0, 1, 2, 3, 4, 5, 6)),
            action = MacroAction.SwitchTheme(themeId = "night_owl"),
            conditions = listOf()
        )
    )
    
    /**
     * Gaming mode - activates when gaming apps are launched
     */
    val GAMING_MODE = ThemeMacro(
        name = "Gaming Mode",
        description = "High-energy theme when gaming",
        trigger = MacroTrigger.AppTrigger(
            packageNames = setOf(
                "com.epicgames.fortnite",
                "com.supercell.clashofclans",
                "com.mojang.minecraftpe"
            ),
            triggerOnOpen = true
        ),
        action = MacroAction.AnimateIntensity(
            targetThemeIntensity = 1.5f,
            targetParticleIntensity = 1.5f,
            targetAnimationIntensity = 1.5f,
            durationMs = 2000
        )
    )
    
    /**
     * Battery saver - reduces intensity when battery is low
     */
    val BATTERY_SAVER = ThemeMacro(
        name = "Battery Saver",
        description = "Reduces theme intensity when battery is low",
        trigger = MacroTrigger.BatteryTrigger(
            threshold = 20,
            condition = MacroTrigger.BatteryCondition.BELOW
        ),
        action = MacroAction.AnimateIntensity(
            targetThemeIntensity = 0.5f,
            targetParticleIntensity = 0.3f,
            targetAnimationIntensity = 0.5f,
            durationMs = 3000
        ),
        conditions = listOf(
            MacroCondition.ChargingState(mustBeCharging = false)
        )
    )
    
    /**
     * Music visualizer - enhances theme when music is playing
     */
    val MUSIC_VISUALIZER = ThemeMacro(
        name = "Music Visualizer",
        description = "Enhanced particles and animations when music plays",
        trigger = MacroTrigger.MusicTrigger(onPlay = true),
        action = MacroAction.AnimateIntensity(
            targetParticleIntensity = 1.3f,
            targetAnimationIntensity = 1.2f,
            durationMs = 1000
        )
    )
    
    /**
     * Rainy day - cozy theme when it's raining
     */
    val RAINY_DAY = ThemeMacro(
        name = "Rainy Day",
        description = "Cozy, warm theme when it's raining",
        trigger = MacroTrigger.WeatherTrigger(
            conditions = setOf(WeatherCondition.RAINY, WeatherCondition.STORMY)
        ),
        action = MacroAction.SwitchTheme(themeId = "cozy_rain"),
        conditions = listOf(
            MacroCondition.TimeRange(
                startHour = 6, startMinute = 0,
                endHour = 20, endMinute = 0
            )
        )
    )
}
