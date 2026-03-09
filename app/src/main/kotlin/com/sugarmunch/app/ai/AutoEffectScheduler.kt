package com.sugarmunch.app.ai

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Restored compatibility shell for the legacy auto effect scheduler.
 */
class AutoEffectScheduler private constructor(
    private val context: Context
) {
    private val _activeSchedules = MutableStateFlow<List<EffectSchedule>>(emptyList())
    val activeSchedules: StateFlow<List<EffectSchedule>> = _activeSchedules

    private val _currentContext = MutableStateFlow<EffectContext?>(null)
    val currentContext: StateFlow<EffectContext?> = _currentContext

    suspend fun scheduleEffect(schedule: EffectSchedule): String = schedule.id.ifBlank {
        "schedule_${System.currentTimeMillis()}"
    }

    suspend fun cancelSchedule(scheduleId: String) = Unit

    suspend fun createPresetSchedule(preset: SchedulePreset): String {
        return "preset_${preset.name.lowercase()}"
    }

    suspend fun setAppTrigger(trigger: AppEffectTrigger): String = trigger.id.ifBlank {
        "trigger_${System.currentTimeMillis()}"
    }

    suspend fun removeAppTrigger(triggerId: String) = Unit

    fun createPresetAppTrigger(preset: AppTriggerPreset): AppEffectTrigger {
        return AppEffectTrigger(id = "", name = preset.name, targetPackages = emptyList())
    }

    fun createPresetLocationTrigger(preset: LocationTriggerPreset): LocationEffectTrigger {
        return LocationEffectTrigger(id = "", name = preset.name)
    }

    suspend fun activateContext(context: EffectContext) {
        _currentContext.value = context
    }

    suspend fun deactivateCurrentContext() {
        _currentContext.value = null
    }

    suspend fun detectAndApplyContext() = Unit

    companion object {
        @Volatile
        private var instance: AutoEffectScheduler? = null

        fun getInstance(context: Context): AutoEffectScheduler {
            return instance ?: synchronized(this) {
                instance ?: AutoEffectScheduler(context.applicationContext).also { instance = it }
            }
        }
    }
}

data class EffectSchedule(
    val id: String,
    val name: String,
    val description: String = "",
    val triggerTime: Long = 0L,
    val effects: List<String> = emptyList(),
    val themeId: String? = null,
    val themeIntensity: Float = 1f,
    val autoDisable: Long? = null,
    val daysOfWeek: List<Int> = emptyList()
)

data class AppEffectTrigger(
    val id: String,
    val name: String,
    val targetPackages: List<String>,
    val effects: List<String> = emptyList(),
    val themeId: String? = null,
    val themeIntensity: Float = 1f,
    val durationMinutes: Int? = null,
    val enterDelayMs: Long = 0L
)

data class LocationEffectTrigger(
    val id: String,
    val name: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radiusMeters: Float = 100f,
    val effects: List<String> = emptyList(),
    val themeId: String? = null,
    val themeIntensity: Float = 1f
)

sealed class EffectContext {
    data class ScheduleContext(val scheduleId: String) : EffectContext()
    data class AppContext(val packageName: String) : EffectContext()
    data class LocationContext(val triggerId: String) : EffectContext()
    data class ActivityContext(val activityType: ActivityType) : EffectContext()
}

enum class ActivityType {
    GENERAL,
    WORK,
    GAMING,
    RELAXING
}

enum class SchedulePreset {
    MORNING_BOOST,
    FOCUS_MODE,
    EVENING_CHILL,
    GAMING_MODE,
    NIGHT_OWL,
    WEEKEND_VIBES
}

enum class AppTriggerPreset {
    GAMING_APPS,
    VIDEO_APPS,
    SOCIAL_APPS,
    PRODUCTIVITY_APPS
}

enum class LocationTriggerPreset {
    HOME,
    WORK,
    GYM
}
