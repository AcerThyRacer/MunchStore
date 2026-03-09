package com.sugarmunch.app.events

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * SugarMunch LiveOps Manager
 * 
 * Remote configuration and A/B testing for events:
 * - Feature flags for event features
 * - Dynamic event content updates
 * - A/B testing groups
 * - Remote configuration
 */

enum class FeatureFlag {
    EVENTS_ENABLED,
    HALLOWEEN_EVENT,
    WINTER_EVENT,
    VALENTINES_EVENT,
    SPRING_EVENT,
    SUMMER_EVENT,
    ANNIVERSARY_EVENT,
    CHALLENGE_NOTIFICATIONS,
    REWARD_NOTIFICATIONS,
    EVENT_LEADERBOARD,
    EVENT_SHOP,
    EVENT_SHARING,
    PREMIUM_REWARDS,
    DYNAMIC_DIFFICULTY,
    COMMUNITY_CHALLENGES
}

enum class ABTestGroup {
    CONTROL,
    VARIANT_A,
    VARIANT_B,
    VARIANT_C
}

data class ABTest(
    val id: String,
    val name: String,
    val description: String,
    val group: ABTestGroup,
    val startTime: Long,
    val endTime: Long?
)

data class EventConfig(
    val eventId: String,
    val isEnabled: Boolean,
    val startDateOverride: Long?,
    val endDateOverride: Long?,
    val challengeModifiers: Map<String, Double>,
    val rewardMultipliers: Map<String, Double>,
    val extraRewards: List<String>,
    val customProperties: Map<String, String>
)

data class LiveOpsConfig(
    val version: Int,
    val lastUpdated: Long,
    val featureFlags: Map<FeatureFlag, Boolean>,
    val abTests: List<ABTest>,
    val eventConfigs: Map<String, EventConfig>,
    val globalMessage: String?,
    val maintenanceMode: Boolean,
    val minimumAppVersion: String?
)

private val Context.liveOpsDataStore: DataStore<Preferences> by preferencesDataStore(name = "live_ops")

class LiveOpsManager(private val context: Context) {
    private val dataStore = context.liveOpsDataStore
    
    companion object {
        @Volatile
        private var instance: LiveOpsManager? = null
        
        private const val CONFIG_VERSION = 1
        private const val CACHE_DURATION_HOURS = 6
        private const val DEFAULT_REMOTE_URL = "https://config.sugarmunch.app/liveops.json"
        
        fun getInstance(context: Context): LiveOpsManager {
            return instance ?: synchronized(this) {
                instance ?: LiveOpsManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // Feature Flags
    // ═════════════════════════════════════════════════════════════════
    
    fun isFeatureEnabled(flag: FeatureFlag): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[booleanPreferencesKey("flag_${flag.name}")] ?: getDefaultFlagValue(flag)
        }
    }
    
    suspend fun isFeatureEnabledSync(flag: FeatureFlag): Boolean {
        return isFeatureEnabled(flag).first()
    }
    
    private fun getDefaultFlagValue(flag: FeatureFlag): Boolean {
        return when (flag) {
            FeatureFlag.EVENTS_ENABLED -> true
            FeatureFlag.HALLOWEEN_EVENT -> true
            FeatureFlag.WINTER_EVENT -> true
            FeatureFlag.VALENTINES_EVENT -> true
            FeatureFlag.SPRING_EVENT -> true
            FeatureFlag.SUMMER_EVENT -> true
            FeatureFlag.ANNIVERSARY_EVENT -> true
            FeatureFlag.CHALLENGE_NOTIFICATIONS -> true
            FeatureFlag.REWARD_NOTIFICATIONS -> true
            FeatureFlag.EVENT_LEADERBOARD -> false
            FeatureFlag.EVENT_SHOP -> false
            FeatureFlag.EVENT_SHARING -> true
            FeatureFlag.PREMIUM_REWARDS -> true
            FeatureFlag.DYNAMIC_DIFFICULTY -> false
            FeatureFlag.COMMUNITY_CHALLENGES -> false
        }
    }
    
    suspend fun setFeatureFlag(flag: FeatureFlag, enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("flag_${flag.name}")] = enabled
        }
    }
    
    suspend fun enableAllEventFeatures() {
        FeatureFlag.values().forEach { flag ->
            if (flag.name.contains("EVENT") || flag.name.contains("CHALLENGE") || flag.name.contains("REWARD")) {
                setFeatureFlag(flag, true)
            }
        }
    }
    
    suspend fun disableAllEventFeatures() {
        FeatureFlag.values().forEach { flag ->
            if (flag.name.contains("EVENT") || flag.name.contains("CHALLENGE") || flag.name.contains("REWARD")) {
                setFeatureFlag(flag, false)
            }
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // A/B Testing
    // ═════════════════════════════════════════════════════════════════
    
    fun getABTestGroup(testId: String): Flow<ABTestGroup> {
        return dataStore.data.map { prefs ->
            val groupName = prefs[stringPreferencesKey("abtest_${testId}_group")]
            groupName?.let { ABTestGroup.valueOf(it) } ?: ABTestGroup.CONTROL
        }
    }
    
    suspend fun getABTestGroupSync(testId: String): ABTestGroup {
        return getABTestGroup(testId).first()
    }
    
    suspend fun assignABTestGroup(testId: String, group: ABTestGroup) {
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("abtest_${testId}_group")] = group.name
            prefs[longPreferencesKey("abtest_${testId}_assigned")] = System.currentTimeMillis()
        }
    }
    
    suspend fun getActiveABTests(): List<ABTest> {
        return dataStore.data.map { prefs ->
            val testIds = prefs[stringPreferencesKey("abtest_ids")]?.split(",") ?: emptyList()
            testIds.mapNotNull { id ->
                val group = prefs[stringPreferencesKey("abtest_${id}_group")]
                val startTime = prefs[longPreferencesKey("abtest_${id}_start")]
                if (group != null && startTime != null) {
                    ABTest(
                        id = id,
                        name = "Test $id",
                        description = "",
                        group = ABTestGroup.valueOf(group),
                        startTime = startTime,
                        endTime = prefs[longPreferencesKey("abtest_${id}_end")]
                    )
                } else null
            }.filter { test ->
                val now = System.currentTimeMillis()
                test.endTime == null || now < test.endTime
            }
        }.first()
    }
    
    // ═════════════════════════════════════════════════════════════════
    // Event Configuration
    // ═════════════════════════════════════════════════════════════════
    
    fun getEventConfig(eventId: String): Flow<EventConfig> {
        return dataStore.data.map { prefs ->
            EventConfig(
                eventId = eventId,
                isEnabled = prefs[booleanPreferencesKey("config_${eventId}_enabled")] ?: true,
                startDateOverride = prefs[longPreferencesKey("config_${eventId}_start")],
                endDateOverride = prefs[longPreferencesKey("config_${eventId}_end")],
                challengeModifiers = emptyMap(),
                rewardMultipliers = emptyMap(),
                extraRewards = emptyList(),
                customProperties = emptyMap()
            )
        }
    }
    
    suspend fun getEventConfigSync(eventId: String): EventConfig {
        return getEventConfig(eventId).first()
    }
    
    suspend fun setEventEnabled(eventId: String, enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("config_${eventId}_enabled")] = enabled
        }
    }
    
    suspend fun setEventDateOverride(eventId: String, startDate: Long?, endDate: Long?) {
        dataStore.edit { prefs ->
            startDate?.let { prefs[longPreferencesKey("config_${eventId}_start")] = it }
                ?: prefs.remove(longPreferencesKey("config_${eventId}_start"))
            endDate?.let { prefs[longPreferencesKey("config_${eventId}_end")] = it }
                ?: prefs.remove(longPreferencesKey("config_${eventId}_end"))
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // Remote Configuration
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun refreshConfig(): Boolean {
        return try {
            withTimeout(10000) {
                val configJson = fetchRemoteConfig()
                if (configJson != null) {
                    parseAndApplyConfig(configJson)
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun fetchRemoteConfig(): String? {
        return try {
            URL(DEFAULT_REMOTE_URL).readText()
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun parseAndApplyConfig(jsonString: String) {
        val json = JSONObject(jsonString)
        
        // Parse feature flags
        val flagsJson = json.optJSONObject("feature_flags")
        flagsJson?.let { flags ->
            FeatureFlag.values().forEach { flag ->
                val value = flags.optBoolean(flag.name, getDefaultFlagValue(flag))
                setFeatureFlag(flag, value)
            }
        }
        
        // Parse A/B tests
        val testsJson = json.optJSONArray("ab_tests")
        testsJson?.let { tests ->
            val testIds = mutableListOf<String>()
            for (i in 0 until tests.length()) {
                val test = tests.getJSONObject(i)
                val testId = test.getString("id")
                testIds.add(testId)
                
                // Only assign if not already assigned
                val existingGroup = getABTestGroupSync(testId)
                if (existingGroup == ABTestGroup.CONTROL) {
                    val groups = test.getJSONArray("groups")
                    val randomGroup = (0 until groups.length()).random()
                    assignABTestGroup(testId, ABTestGroup.valueOf(groups.getString(randomGroup)))
                }
            }
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey("abtest_ids")] = testIds.joinToString(",")
            }
        }
        
        // Parse event configs
        val eventsJson = json.optJSONObject("event_configs")
        eventsJson?.let { events ->
            events.keys().forEach { eventId ->
                val eventConfig = events.getJSONObject(eventId)
                setEventEnabled(eventId, eventConfig.optBoolean("enabled", true))
            }
        }
        
        // Store update timestamp
        dataStore.edit { prefs ->
            prefs[intPreferencesKey("config_version")] = json.optInt("version", CONFIG_VERSION)
            prefs[longPreferencesKey("config_updated")] = System.currentTimeMillis()
        }
    }
    
    suspend fun shouldRefreshConfig(): Boolean {
        val lastUpdated = dataStore.data.map { 
            it[longPreferencesKey("config_updated")] ?: 0 
        }.first()
        
        val cacheDurationMs = TimeUnit.HOURS.toMillis(CACHE_DURATION_HOURS.toLong())
        return System.currentTimeMillis() - lastUpdated > cacheDurationMs
    }
    
    // ═════════════════════════════════════════════════════════════════
    // Configuration Helpers
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun getGlobalMessage(): String? {
        return dataStore.data.map { 
            it[stringPreferencesKey("global_message")]
        }.first()
    }
    
    suspend fun isInMaintenanceMode(): Boolean {
        return dataStore.data.map { 
            it[booleanPreferencesKey("maintenance_mode")] ?: false 
        }.first()
    }
    
    suspend fun getMinimumAppVersion(): String? {
        return dataStore.data.map { 
            it[stringPreferencesKey("min_app_version")]
        }.first()
    }
    
    suspend fun setMaintenanceMode(enabled: Boolean, message: String? = null) {
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("maintenance_mode")] = enabled
            message?.let { prefs[stringPreferencesKey("maintenance_message")] = it }
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // Analytics Integration
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun logFeatureFlagExposure(flag: FeatureFlag, value: Boolean) {
        // Would integrate with AnalyticsManager
        // AnalyticsManager.logEvent("feature_flag_exposed", mapOf(
        //     "flag" to flag.name,
        //     "value" to value
        // ))
    }
    
    suspend fun logABTestExposure(testId: String, group: ABTestGroup) {
        // Would integrate with AnalyticsManager
        // AnalyticsManager.logEvent("ab_test_exposed", mapOf(
        //     "test_id" to testId,
        //     "group" to group.name
        // ))
    }
    
    // ═════════════════════════════════════════════════════════════════
    // Local Configuration (for testing/debugging)
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun applyLocalConfig(config: LiveOpsConfig) {
        config.featureFlags.forEach { (flag, enabled) ->
            setFeatureFlag(flag, enabled)
        }
        
        config.eventConfigs.forEach { (eventId, eventConfig) ->
            setEventEnabled(eventId, eventConfig.isEnabled)
        }
        
        dataStore.edit { prefs ->
            prefs[intPreferencesKey("config_version")] = config.version
            prefs[longPreferencesKey("config_updated")] = config.lastUpdated
            config.globalMessage?.let { prefs[stringPreferencesKey("global_message")] = it }
            prefs[booleanPreferencesKey("maintenance_mode")] = config.maintenanceMode
            config.minimumAppVersion?.let { prefs[stringPreferencesKey("min_app_version")] = it }
        }
    }
    
    suspend fun resetToDefaults() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
