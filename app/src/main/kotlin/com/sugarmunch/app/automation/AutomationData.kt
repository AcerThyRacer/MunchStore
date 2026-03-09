package com.sugarmunch.app.automation

import android.content.Context
import androidx.room.*
import com.sugarmunch.app.data.local.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// ═════════════════════════════════════════════════════════════
// AUTOMATION DATA MODELS
// ═════════════════════════════════════════════════════════════

/**
 * Represents a complete automation task with trigger, conditions, and actions
 */
@Serializable
data class AutomationTask(
    val id: String,
    val name: String,
    val description: String,
    val enabled: Boolean = true,
    val trigger: AutomationTrigger,
    val conditions: List<AutomationCondition> = emptyList(),
    val actions: List<AutomationAction>,
    val createdAt: Long = System.currentTimeMillis(),
    val lastRunAt: Long? = null,
    val runCount: Int = 0,
    val isTemplate: Boolean = false,
    val templateId: String? = null,
    val variables: Map<String, String> = emptyMap()
)

/**
 * All possible trigger types for automation
 */
@Serializable
sealed class AutomationTrigger {
    abstract val triggerId: String
    
    @Serializable
    data class TimeTrigger(
        override val triggerId: String = "time_specific",
        val hour: Int,
        val minute: Int,
        val repeatDays: List<Int> = emptyList(),
        val timezone: String = "local"
    ) : AutomationTrigger()
    
    @Serializable
    data class IntervalTrigger(
        override val triggerId: String = "time_interval",
        val intervalMinutes: Long,
        val startTime: Long? = null,
        val endTime: Long? = null
    ) : AutomationTrigger()
    
    @Serializable
    data class SunriseSunsetTrigger(
        override val triggerId: String = "sunrise_sunset",
        val event: SunEvent,
        val offsetMinutes: Int = 0
    ) : AutomationTrigger() {
        enum class SunEvent { SUNRISE, SUNSET }
    }
    
    @Serializable
    data class AppOpenedTrigger(
        override val triggerId: String = "app_opened",
        val packageNames: List<String>,
        val matchAll: Boolean = false
    ) : AutomationTrigger()
    
    @Serializable
    data class AppClosedTrigger(
        override val triggerId: String = "app_closed",
        val packageNames: List<String>,
        val matchAll: Boolean = false
    ) : AutomationTrigger()
    
    @Serializable
    data class AppInstalledTrigger(
        override val triggerId: String = "app_installed",
        val packageNamePattern: String? = null
    ) : AutomationTrigger()
    
    @Serializable
    data class EffectToggledTrigger(
        override val triggerId: String = "effect_toggled",
        val effectId: String? = null,
        val state: EffectState = EffectState.EITHER
    ) : AutomationTrigger() {
        enum class EffectState { ENABLED, DISABLED, EITHER }
    }
    
    @Serializable
    data class ThemeChangedTrigger(
        override val triggerId: String = "theme_changed",
        val themeId: String? = null
    ) : AutomationTrigger()
    
    @Serializable
    data class RewardClaimedTrigger(
        override val triggerId: String = "reward_claimed",
        val rewardType: String? = null
    ) : AutomationTrigger()
    
    @Serializable
    data class BatteryLevelTrigger(
        override val triggerId: String = "battery_level",
        val level: Int,
        val operator: ComparisonOperator = ComparisonOperator.LESS_THAN_OR_EQUAL
    ) : AutomationTrigger() {
        enum class ComparisonOperator {
            LESS_THAN, LESS_THAN_OR_EQUAL, EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL
        }
    }
    
    @Serializable
    data class ChargingTrigger(
        override val triggerId: String = "charging",
        val state: ChargingState
    ) : AutomationTrigger() {
        enum class ChargingState { PLUGGED_IN, UNPLUGGED, FAST_CHARGING, WIRELESS_CHARGING }
    }
    
    @Serializable
    data class WifiConnectedTrigger(
        override val triggerId: String = "wifi_connected",
        val ssid: String? = null,
        val anyWifi: Boolean = true
    ) : AutomationTrigger()
    
    @Serializable
    data class BluetoothConnectedTrigger(
        override val triggerId: String = "bluetooth_connected",
        val deviceName: String? = null,
        val deviceType: DeviceType? = null
    ) : AutomationTrigger() {
        enum class DeviceType { HEADPHONES, SPEAKER, WATCH, CAR, COMPUTER, ANY }
    }
    
    @Serializable
    data class ScreenStateTrigger(
        override val triggerId: String = "screen_state",
        val state: ScreenState
    ) : AutomationTrigger() {
        enum class ScreenState { ON, OFF, UNLOCKED }
    }
    
    @Serializable
    data class GeofenceTrigger(
        override val triggerId: String = "geofence",
        val latitude: Double,
        val longitude: Double,
        val radiusMeters: Float,
        val transition: GeofenceTransition
    ) : AutomationTrigger() {
        enum class GeofenceTransition { ENTER, EXIT, DWELL }
    }
    
    @Serializable
    data class ShakeTrigger(
        override val triggerId: String = "shake",
        val sensitivity: ShakeSensitivity = ShakeSensitivity.MEDIUM,
        val minShakes: Int = 1
    ) : AutomationTrigger() {
        enum class ShakeSensitivity { LOW, MEDIUM, HIGH }
    }
    
    @Serializable
    data class OrientationTrigger(
        override val triggerId: String = "orientation",
        val orientation: DeviceOrientation
    ) : AutomationTrigger() {
        enum class DeviceOrientation { PORTRAIT, LANDSCAPE, FACE_UP, FACE_DOWN }
    }
    
    @Serializable
    data class ProximityTrigger(
        override val triggerId: String = "proximity",
        val state: ProximityState
    ) : AutomationTrigger() {
        enum class ProximityState { NEAR, FAR }
    }
    
    @Serializable
    data class ManualTrigger(
        override val triggerId: String = "manual",
        val shortcutName: String? = null
    ) : AutomationTrigger()
}

/**
 * Conditions that can be combined with AND/OR logic
 */
@Serializable
sealed class AutomationCondition {
    @Serializable
    data class TimeCondition(
        val startHour: Int,
        val startMinute: Int,
        val endHour: Int,
        val endMinute: Int,
        val daysOfWeek: List<Int> = (0..6).toList()
    ) : AutomationCondition()
    
    @Serializable
    data class BatteryCondition(
        val minLevel: Int? = null,
        val maxLevel: Int? = null,
        val mustBeCharging: Boolean? = null
    ) : AutomationCondition()
    
    @Serializable
    data class LocationCondition(
        val latitude: Double,
        val longitude: Double,
        val radiusMeters: Float,
        val mustBeInside: Boolean = true
    ) : AutomationCondition()
    
    @Serializable
    data class WifiCondition(
        val connected: Boolean,
        val ssid: String? = null
    ) : AutomationCondition()
    
    @Serializable
    data class AppRunningCondition(
        val packageNames: List<String>,
        val mustBeRunning: Boolean = true
    ) : AutomationCondition()
    
    @Serializable
    data class EffectActiveCondition(
        val effectId: String,
        val mustBeActive: Boolean = true
    ) : AutomationCondition()
    
    @Serializable
    data class CompositeCondition(
        val operator: LogicalOperator,
        val conditions: List<AutomationCondition>
    ) : AutomationCondition() {
        enum class LogicalOperator { AND, OR, NOT }
    }
}

/**
 * All possible action types
 */
@Serializable
sealed class AutomationAction {
    abstract val actionId: String
    abstract val delayMs: Long
    
    @Serializable
    data class EnableEffectAction(
        override val actionId: String = "effect_enable",
        val effectId: String,
        val intensity: Float? = null,
        override val delayMs: Long = 0
    ) : AutomationAction()
    
    @Serializable
    data class DisableEffectAction(
        override val actionId: String = "effect_disable",
        val effectId: String,
        override val delayMs: Long = 0
    ) : AutomationAction()
    
    @Serializable
    data class ToggleEffectAction(
        override val actionId: String = "effect_toggle",
        val effectId: String,
        override val delayMs: Long = 0
    ) : AutomationAction()
    
    @Serializable
    data class SetEffectIntensityAction(
        override val actionId: String = "effect_intensity",
        val effectId: String,
        val intensity: Float,
        override val delayMs: Long = 0
    ) : AutomationAction()
    
    @Serializable
    data class ChangeThemeAction(
        override val actionId: String = "theme_change",
        val themeId: String,
        val intensity: Float? = null,
        val animate: Boolean = true,
        override val delayMs: Long = 0
    ) : AutomationAction()
    
    @Serializable
    data class RandomThemeAction(
        override val actionId: String = "theme_random",
        val category: String? = null,
        override val delayMs: Long = 0
    ) : AutomationAction()
    
    @Serializable
    data class SetThemeIntensityAction(
        override val actionId: String = "theme_intensity",
        val intensity: Float,
        val component: ThemeComponent = ThemeComponent.ALL,
        override val delayMs: Long = 0
    ) : AutomationAction() {
        enum class ThemeComponent { ALL, COLORS, BACKGROUND, PARTICLES, ANIMATIONS }
    }
    
    @Serializable
    data class OpenAppAction(
        override val actionId: String = "app_open",
        val packageName: String,
        val bringToFront: Boolean = true,
        override val delayMs: Long = 0
    ) : AutomationAction()
    
    @Serializable
    data class LaunchSugarMunchScreenAction(
        override val actionId: String = "sm_open_screen",
        val screen: SugarMunchScreen,
        override val delayMs: Long = 0
    ) : AutomationAction() {
        enum class SugarMunchScreen {
            CATALOG, SETTINGS, EFFECTS, THEMES, SHOP, REWARDS, ANALYTICS
        }
    }
    
    @Serializable
    data class ShareAppAction(
        override val actionId: String = "app_share",
        val packageName: String,
        val customMessage: String? = null,
        override val delayMs: Long = 0
    ) : AutomationAction()
    
    @Serializable
    data class ClaimRewardAction(
        override val actionId: String = "sm_claim_reward",
        val rewardType: String? = null,
        override val delayMs: Long = 0
    ) : AutomationAction()
    
    @Serializable
    data class AddSugarPointsAction(
        override val actionId: String = "sm_add_points",
        val points: Int,
        val reason: String,
        override val delayMs: Long = 0
    ) : AutomationAction()
    
    @Serializable
    data class ShowNotificationAction(
        override val actionId: String = "sys_notification",
        val title: String,
        val message: String,
        val priority: NotificationPriority = NotificationPriority.DEFAULT,
        val actions: List<NotificationAction> = emptyList(),
        override val delayMs: Long = 0
    ) : AutomationAction() {
        enum class NotificationPriority { LOW, DEFAULT, HIGH, URGENT }
        @Serializable
        data class NotificationAction(val label: String, val actionId: String)
    }
    
    @Serializable
    data class ShowToastAction(
        override val actionId: String = "sys_toast",
        val message: String,
        val duration: ToastDuration = ToastDuration.SHORT,
        override val delayMs: Long = 0
    ) : AutomationAction() {
        enum class ToastDuration { SHORT, LONG }
    }
    
    @Serializable
    data class VibrateAction(
        override val actionId: String = "sys_vibrate",
        val pattern: VibrationPattern = VibrationPattern.SHORT,
        val customPattern: List<Long>? = null,
        override val delayMs: Long = 0
    ) : AutomationAction() {
        enum class VibrationPattern { SHORT, LONG, DOUBLE, TRIPLE, HEARTBEAT, CUSTOM }
    }
    
    @Serializable
    data class SetBrightnessAction(
        override val actionId: String = "sys_brightness",
        val level: Float,
        val auto: Boolean = false,
        val temporary: Boolean = false,
        override val delayMs: Long = 0
    ) : AutomationAction()
    
    @Serializable
    data class SetVolumeAction(
        override val actionId: String = "sys_volume",
        val stream: AudioStream,
        val level: Int,
        override val delayMs: Long = 0
    ) : AutomationAction() {
        enum class AudioStream { MEDIA, RING, NOTIFICATION, ALARM, SYSTEM }
    }
    
    @Serializable
    data class PlaySoundAction(
        override val actionId: String = "sys_sound",
        val soundUri: String? = null,
        val soundType: SoundType = SoundType.DEFAULT,
        override val delayMs: Long = 0
    ) : AutomationAction() {
        enum class SoundType { DEFAULT, SUCCESS, ERROR, NOTIFICATION, ALARM, CUSTOM }
    }
    
    @Serializable
    data class TurnOffScreenAction(
        override val actionId: String = "sys_screen_off",
        val delaySeconds: Int = 0,
        override val delayMs: Long = 0
    ) : AutomationAction()
    
    @Serializable
    data class WaitAction(
        override val actionId: String = "control_wait",
        val durationMs: Long,
        override val delayMs: Long = 0
    ) : AutomationAction()
    
    @Serializable
    data class ConditionalAction(
        override val actionId: String = "control_if",
        val condition: AutomationCondition,
        val thenActions: List<AutomationAction>,
        val elseActions: List<AutomationAction> = emptyList(),
        override val delayMs: Long = 0
    ) : AutomationAction()
    
    @Serializable
    data class RunTaskAction(
        override val actionId: String = "control_run_task",
        val taskId: String,
        val waitForCompletion: Boolean = false,
        override val delayMs: Long = 0
    ) : AutomationAction()
}

/**
 * Execution history for tracking automation runs
 */
@Serializable
data class ExecutionRecord(
    val id: String,
    val taskId: String,
    val taskName: String,
    val triggerTime: Long,
    val completionTime: Long? = null,
    val status: ExecutionStatus,
    val actionsExecuted: Int = 0,
    val actionsTotal: Int = 0,
    val errorMessage: String? = null,
    val triggeredBy: String
) {
    enum class ExecutionStatus { PENDING, RUNNING, SUCCESS, PARTIAL_SUCCESS, FAILED, CANCELLED }
}

// ═════════════════════════════════════════════════════════════
// ROOM ENTITIES
// ═════════════════════════════════════════════════════════════

@Entity(tableName = "automation_tasks")
data class AutomationTaskEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val enabled: Boolean,
    val triggerJson: String,
    val conditionsJson: String,
    val actionsJson: String,
    val createdAt: Long,
    val lastRunAt: Long?,
    val runCount: Int,
    val isTemplate: Boolean,
    val templateId: String?,
    val variablesJson: String
)

@Entity(tableName = "execution_history")
data class ExecutionHistoryEntity(
    @PrimaryKey
    val id: String,
    val taskId: String,
    val taskName: String,
    val triggerTime: Long,
    val completionTime: Long?,
    val status: String,
    val actionsExecuted: Int,
    val actionsTotal: Int,
    val errorMessage: String?,
    val triggeredBy: String
)

// ═════════════════════════════════════════════════════════════
// ROOM DAO
// ═════════════════════════════════════════════════════════════

@Dao
interface AutomationDao {
    @Query("SELECT * FROM automation_tasks WHERE isTemplate = 0 ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<AutomationTaskEntity>>
    
    @Query("SELECT * FROM automation_tasks WHERE isTemplate = 1 ORDER BY createdAt DESC")
    fun getAllTemplates(): Flow<List<AutomationTaskEntity>>
    
    @Query("SELECT * FROM automation_tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): AutomationTaskEntity?
    
    @Query("SELECT * FROM automation_tasks WHERE enabled = 1 AND isTemplate = 0")
    suspend fun getEnabledTasks(): List<AutomationTaskEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTask(task: AutomationTaskEntity)
    
    @Delete
    suspend fun deleteTask(task: AutomationTaskEntity)
    
    @Query("DELETE FROM automation_tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String): Int
    
    @Query("UPDATE automation_tasks SET enabled = :enabled WHERE id = :taskId")
    suspend fun setTaskEnabled(taskId: String, enabled: Boolean)
    
    @Query("UPDATE automation_tasks SET lastRunAt = :timestamp, runCount = runCount + 1 WHERE id = :taskId")
    suspend fun updateTaskExecution(taskId: String, timestamp: Long)
    
    @Query("SELECT * FROM execution_history ORDER BY triggerTime DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 100): Flow<List<ExecutionHistoryEntity>>
    
    @Query("SELECT * FROM execution_history WHERE taskId = :taskId ORDER BY triggerTime DESC")
    fun getHistoryForTask(taskId: String): Flow<List<ExecutionHistoryEntity>>
    
    @Insert
    suspend fun addExecutionRecord(record: ExecutionHistoryEntity)
    
    @Query("UPDATE execution_history SET completionTime = :completionTime, status = :status, actionsExecuted = :actionsExecuted, errorMessage = :errorMessage WHERE id = :recordId")
    suspend fun updateExecutionRecord(
        recordId: String,
        completionTime: Long,
        status: String,
        actionsExecuted: Int,
        errorMessage: String?
    )
    
    @Query("DELETE FROM execution_history WHERE triggerTime < :olderThan")
    suspend fun cleanupOldHistory(olderThan: Long): Int
}

// ═════════════════════════════════════════════════════════════
// TYPE CONVERTERS
// ═════════════════════════════════════════════════════════════

class AutomationConverters {
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }
    
    @TypeConverter
    fun triggerToJson(trigger: AutomationTrigger): String = json.encodeToString(trigger)
    
    @TypeConverter
    fun jsonToTrigger(jsonStr: String): AutomationTrigger = json.decodeFromString(jsonStr)
    
    @TypeConverter
    fun conditionsToJson(conditions: List<AutomationCondition>): String = json.encodeToString(conditions)
    
    @TypeConverter
    fun jsonToConditions(jsonStr: String): List<AutomationCondition> = json.decodeFromString(jsonStr)
    
    @TypeConverter
    fun actionsToJson(actions: List<AutomationAction>): String = json.encodeToString(actions)
    
    @TypeConverter
    fun jsonToActions(jsonStr: String): List<AutomationAction> = json.decodeFromString(jsonStr)
    
    @TypeConverter
    fun variablesToJson(variables: Map<String, String>): String = json.encodeToString(variables)
    
    @TypeConverter
    fun jsonToVariables(jsonStr: String): Map<String, String> = json.decodeFromString(jsonStr)
}

// ═════════════════════════════════════════════════════════════
// REPOSITORY
// ═════════════════════════════════════════════════════════════

class AutomationRepository private constructor(
    private val dao: AutomationDao
) {
    fun getAllTasks(): Flow<List<AutomationTask>> = 
        dao.getAllTasks().map { list -> list.map { it.toTask() } }
    
    fun getAllTemplates(): Flow<List<AutomationTask>> = 
        dao.getAllTemplates().map { list -> list.map { it.toTask() } }
    
    suspend fun getTaskById(taskId: String): AutomationTask? = 
        dao.getTaskById(taskId)?.toTask()
    
    suspend fun getEnabledTasks(): List<AutomationTask> = 
        dao.getEnabledTasks().map { it.toTask() }
    
    suspend fun saveTask(task: AutomationTask) {
        dao.saveTask(task.toEntity())
    }
    
    suspend fun deleteTask(taskId: String) {
        dao.deleteTaskById(taskId)
    }
    
    suspend fun setTaskEnabled(taskId: String, enabled: Boolean) {
        dao.setTaskEnabled(taskId, enabled)
    }
    
    suspend fun recordTaskExecution(taskId: String) {
        dao.updateTaskExecution(taskId, System.currentTimeMillis())
    }
    
    fun getRecentHistory(limit: Int = 100): Flow<List<ExecutionRecord>> = 
        dao.getRecentHistory(limit).map { list -> list.map { it.toRecord() } }
    
    fun getHistoryForTask(taskId: String): Flow<List<ExecutionRecord>> = 
        dao.getHistoryForTask(taskId).map { list -> list.map { it.toRecord() } }
    
    suspend fun addExecutionRecord(record: ExecutionRecord) {
        dao.addExecutionRecord(record.toEntity())
    }
    
    suspend fun updateExecutionRecord(record: ExecutionRecord) {
        dao.updateExecutionRecord(
            recordId = record.id,
            completionTime = record.completionTime ?: System.currentTimeMillis(),
            status = record.status.name,
            actionsExecuted = record.actionsExecuted,
            errorMessage = record.errorMessage
        )
    }
    
    suspend fun cleanupOldHistory(daysToKeep: Int = 30) {
        val cutoff = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000)
        dao.cleanupOldHistory(cutoff)
    }
    
    private fun AutomationTaskEntity.toTask(): AutomationTask {
        val json = Json { ignoreUnknownKeys = true }
        return AutomationTask(
            id = id,
            name = name,
            description = description,
            enabled = enabled,
            trigger = json.decodeFromString(triggerJson),
            conditions = json.decodeFromString(conditionsJson),
            actions = json.decodeFromString(actionsJson),
            createdAt = createdAt,
            lastRunAt = lastRunAt,
            runCount = runCount,
            isTemplate = isTemplate,
            templateId = templateId,
            variables = json.decodeFromString(variablesJson)
        )
    }
    
    private fun AutomationTask.toEntity(): AutomationTaskEntity {
        val json = Json { encodeDefaults = true }
        return AutomationTaskEntity(
            id = id,
            name = name,
            description = description,
            enabled = enabled,
            triggerJson = json.encodeToString(trigger),
            conditionsJson = json.encodeToString(conditions),
            actionsJson = json.encodeToString(actions),
            createdAt = createdAt,
            lastRunAt = lastRunAt,
            runCount = runCount,
            isTemplate = isTemplate,
            templateId = templateId,
            variablesJson = json.encodeToString(variables)
        )
    }
    
    private fun ExecutionHistoryEntity.toRecord(): ExecutionRecord =
        ExecutionRecord(
            id = id,
            taskId = taskId,
            taskName = taskName,
            triggerTime = triggerTime,
            completionTime = completionTime,
            status = ExecutionRecord.ExecutionStatus.valueOf(status),
            actionsExecuted = actionsExecuted,
            actionsTotal = actionsTotal,
            errorMessage = errorMessage,
            triggeredBy = triggeredBy
        )
    
    private fun ExecutionRecord.toEntity(): ExecutionHistoryEntity =
        ExecutionHistoryEntity(
            id = id,
            taskId = taskId,
            taskName = taskName,
            triggerTime = triggerTime,
            completionTime = completionTime,
            status = status.name,
            actionsExecuted = actionsExecuted,
            actionsTotal = actionsTotal,
            errorMessage = errorMessage,
            triggeredBy = triggeredBy
        )
    
    companion object {
        @Volatile
        private var instance: AutomationRepository? = null
        
        fun getInstance(context: Context): AutomationRepository {
            return instance ?: synchronized(this) {
                instance ?: AutomationRepository(
                    AppDatabase.getDatabase(context).automationDao()
                ).also { instance = it }
            }
        }
    }
}
