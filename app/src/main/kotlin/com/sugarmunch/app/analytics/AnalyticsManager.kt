package com.sugarmunch.app.analytics

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*
import java.util.*

private val Context.analyticsDataStore: DataStore<Preferences> by preferencesDataStore(name = "analytics")

/**
 * Analytics Manager - Tracks user app usage, installs, and engagement
 * Provides insights into user behavior and app statistics
 */
class AnalyticsManager private constructor(private val context: Context) {

    private val dataStore = context.analyticsDataStore

    // Preferences keys
    private object Keys {
        // Session tracking
        val TOTAL_SESSIONS = intPreferencesKey("total_sessions")
        val TOTAL_TIME_SPENT = longPreferencesKey("total_time_spent")
        val LAST_SESSION_START = longPreferencesKey("last_session_start")
        
        // App installs
        val TOTAL_INSTALLS = intPreferencesKey("total_installs")
        val INSTALL_HISTORY = stringPreferencesKey("install_history")
        val CATEGORY_BREAKDOWN = stringPreferencesKey("category_breakdown")
        
        // Feature usage
        val THEMES_TRIED = stringSetPreferencesKey("themes_tried")
        val EFFECTS_USED = stringSetPreferencesKey("effects_used")
        val SHOP_PURCHASES = intPreferencesKey("shop_purchases")
        
        // Engagement
        val ACHIEVEMENTS_UNLOCKED = intPreferencesKey("achievements_unlocked")
        val FAVORITES_ADDED = intPreferencesKey("favorites_added")
        val COLLECTIONS_CREATED = intPreferencesKey("collections_created")
        
        // Time patterns
        val HOURLY_ACTIVITY = stringPreferencesKey("hourly_activity")
        val DAILY_ACTIVITY = stringPreferencesKey("daily_activity")
        
        // Peak usage
        val PEAK_SESSION_TIME = longPreferencesKey("peak_session_time")
        val MOST_ACTIVE_DAY = stringPreferencesKey("most_active_day")
        val MOST_ACTIVE_HOUR = intPreferencesKey("most_active_hour")
    }

    // ═════════════════════════════════════════════════════════════
    // SESSION TRACKING
    // ═════════════════════════════════════════════════════════════

    fun startSession() {
        scope.launch {
            dataStore.edit { prefs ->
                prefs[Keys.LAST_SESSION_START] = System.currentTimeMillis()
                prefs[Keys.TOTAL_SESSIONS] = (prefs[Keys.TOTAL_SESSIONS] ?: 0) + 1
            }
            
            // Track start time for hourly activity
            trackHourlyActivity()
        }
    }

    fun endSession() {
        scope.launch {
            val startTime = dataStore.data.map { it[Keys.LAST_SESSION_START] ?: 0 }.first()
            if (startTime > 0) {
                val duration = System.currentTimeMillis() - startTime
                
                dataStore.edit { prefs ->
                    prefs[Keys.TOTAL_TIME_SPENT] = (prefs[Keys.TOTAL_TIME_SPENT] ?: 0) + duration
                    
                    // Update peak session time
                    val currentPeak = prefs[Keys.PEAK_SESSION_TIME] ?: 0
                    if (duration > currentPeak) {
                        prefs[Keys.PEAK_SESSION_TIME] = duration
                    }
                }
                
                trackDailyActivity(duration)
            }
        }
    }

    val totalSessions: Flow<Int> = dataStore.data.map { it[Keys.TOTAL_SESSIONS] ?: 0 }
    val totalTimeSpent: Flow<Long> = dataStore.data.map { it[Keys.TOTAL_TIME_SPENT] ?: 0 }
    val averageSessionTime: Flow<Long> = combine(totalSessions, totalTimeSpent) { sessions, time ->
        if (sessions > 0) time / sessions else 0
    }

    // ═════════════════════════════════════════════════════════════
    // APP INSTALL TRACKING
    // ═════════════════════════════════════════════════════════════

    suspend fun trackAppInstall(appId: String, appName: String, category: String, sizeBytes: Long) {
        dataStore.edit { prefs ->
            // Update total
            prefs[Keys.TOTAL_INSTALLS] = (prefs[Keys.TOTAL_INSTALLS] ?: 0) + 1
            
            // Add to history
            val history = parseInstallHistory(prefs[Keys.INSTALL_HISTORY] ?: "")
            val newEntry = InstallEntry(
                date = System.currentTimeMillis(),
                appId = appId,
                appName = appName,
                category = category,
                sizeBytes = sizeBytes
            )
            prefs[Keys.INSTALL_HISTORY] = serializeInstallHistory(history + newEntry)
            
            // Update category breakdown
            val breakdown = parseCategoryBreakdown(prefs[Keys.CATEGORY_BREAKDOWN] ?: "")
            val updated = breakdown.toMutableMap()
            updated[category] = (updated[category] ?: 0) + 1
            prefs[Keys.CATEGORY_BREAKDOWN] = serializeCategoryBreakdown(updated)
        }
    }

    val totalInstalls: Flow<Int> = dataStore.data.map { it[Keys.TOTAL_INSTALLS] ?: 0 }
    
    val installHistory: Flow<List<InstallEntry>> = dataStore.data.map { prefs ->
        parseInstallHistory(prefs[Keys.INSTALL_HISTORY] ?: "")
    }
    
    val categoryBreakdown: Flow<Map<String, Int>> = dataStore.data.map { prefs ->
        parseCategoryBreakdown(prefs[Keys.CATEGORY_BREAKDOWN] ?: "")
    }

    // ═════════════════════════════════════════════════════════════
    // FEATURE USAGE TRACKING
    // ═════════════════════════════════════════════════════════════

    suspend fun trackThemeTried(themeId: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.THEMES_TRIED] ?: emptySet()
            prefs[Keys.THEMES_TRIED] = current + themeId
        }
    }

    suspend fun trackEffectUsed(effectId: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.EFFECTS_USED] ?: emptySet()
            prefs[Keys.EFFECTS_USED] = current + effectId
        }
    }

    suspend fun trackShopPurchase() {
        dataStore.edit { prefs ->
            prefs[Keys.SHOP_PURCHASES] = (prefs[Keys.SHOP_PURCHASES] ?: 0) + 1
        }
    }

    val themesTriedCount: Flow<Int> = dataStore.data.map { 
        (it[Keys.THEMES_TRIED] ?: emptySet()).size 
    }
    val effectsUsedCount: Flow<Int> = dataStore.data.map { 
        (it[Keys.EFFECTS_USED] ?: emptySet()).size 
    }
    val shopPurchases: Flow<Int> = dataStore.data.map { it[Keys.SHOP_PURCHASES] ?: 0 }

    // ═════════════════════════════════════════════════════════════
    // ENGAGEMENT METRICS
    // ═════════════════════════════════════════════════════════════

    suspend fun trackFavoriteAdded() {
        dataStore.edit { prefs ->
            prefs[Keys.FAVORITES_ADDED] = (prefs[Keys.FAVORITES_ADDED] ?: 0) + 1
        }
    }

    suspend fun trackCollectionCreated() {
        dataStore.edit { prefs ->
            prefs[Keys.COLLECTIONS_CREATED] = (prefs[Keys.COLLECTIONS_CREATED] ?: 0) + 1
        }
    }

    suspend fun trackAchievementUnlocked() {
        dataStore.edit { prefs ->
            prefs[Keys.ACHIEVEMENTS_UNLOCKED] = (prefs[Keys.ACHIEVEMENTS_UNLOCKED] ?: 0) + 1
        }
    }

    val favoritesCount: Flow<Int> = dataStore.data.map { it[Keys.FAVORITES_ADDED] ?: 0 }
    val collectionsCount: Flow<Int> = dataStore.data.map { it[Keys.COLLECTIONS_CREATED] ?: 0 }
    val achievementsCount: Flow<Int> = dataStore.data.map { it[Keys.ACHIEVEMENTS_UNLOCKED] ?: 0 }

    // ═════════════════════════════════════════════════════════════
    // TIME PATTERNS
    // ═════════════════════════════════════════════════════════════

    private suspend fun trackHourlyActivity() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        dataStore.edit { prefs ->
            val activity = parseHourlyActivity(prefs[Keys.HOURLY_ACTIVITY] ?: "")
            val updated = activity.toMutableMap()
            updated[hour] = (updated[hour] ?: 0) + 1
            prefs[Keys.HOURLY_ACTIVITY] = serializeHourlyActivity(updated)
            
            // Update most active hour
            val mostActive = updated.maxByOrNull { it.value }?.key
            if (mostActive != null) {
                prefs[Keys.MOST_ACTIVE_HOUR] = mostActive
            }
        }
    }

    private suspend fun trackDailyActivity(duration: Long) {
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val dayName = when (dayOfWeek) {
            Calendar.SUNDAY -> "Sun"
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> "Unknown"
        }
        
        dataStore.edit { prefs ->
            val activity = parseDailyActivity(prefs[Keys.DAILY_ACTIVITY] ?: "")
            val updated = activity.toMutableMap()
            updated[dayName] = (updated[dayName] ?: 0) + duration
            prefs[Keys.DAILY_ACTIVITY] = serializeDailyActivity(updated)
            
            // Update most active day
            val mostActive = updated.maxByOrNull { it.value }?.key
            if (mostActive != null) {
                prefs[Keys.MOST_ACTIVE_DAY] = mostActive
            }
        }
    }

    val hourlyActivity: Flow<Map<Int, Int>> = dataStore.data.map { 
        parseHourlyActivity(it[Keys.HOURLY_ACTIVITY] ?: "")
    }
    
    val dailyActivity: Flow<Map<String, Long>> = dataStore.data.map { 
        parseDailyActivity(it[Keys.DAILY_ACTIVITY] ?: "")
    }
    
    val mostActiveHour: Flow<Int> = dataStore.data.map { it[Keys.MOST_ACTIVE_HOUR] ?: 12 }
    val mostActiveDay: Flow<String> = dataStore.data.map { it[Keys.MOST_ACTIVE_DAY] ?: "Unknown" }

    // ═════════════════════════════════════════════════════════════
    // STATISTICS SUMMARY
    // ═════════════════════════════════════════════════════════════

    val userStats: Flow<UserStats> = combine(
        totalSessions,
        totalTimeSpent,
        totalInstalls,
        themesTriedCount,
        effectsUsedCount,
        achievementsCount,
        shopPurchases
    ) { sessions, time, installs, themes, effects, achievements, purchases ->
        UserStats(
            totalSessions = sessions,
            totalTimeSpent = time,
            totalInstalls = installs,
            themesTried = themes,
            effectsUsed = effects,
            achievementsUnlocked = achievements,
            shopPurchases = purchases
        )
    }

    // ═════════════════════════════════════════════════════════════
    // WEEKLY INSIGHTS
    // ═════════════════════════════════════════════════════════════

    fun getWeeklyInsights(): WeeklyInsights {
        // Calculate insights based on stored data
        return WeeklyInsights(
            mostInstalledCategory = "Video & Music", // Would calculate from real data
            averageDailyInstalls = 2.5f,
            favoriteFeature = "Themes",
            engagementTrend = EngagementTrend.RISING
        )
    }

    // ═════════════════════════════════════════════════════════════
    // PARSING HELPERS
    // ═════════════════════════════════════════════════════════════

    private fun parseInstallHistory(data: String): List<InstallEntry> {
        if (data.isEmpty()) return emptyList()
        return data.split(";").mapNotNull { entry ->
            val parts = entry.split(",")
            if (parts.size >= 5) {
                InstallEntry(
                    date = parts[0].toLongOrNull() ?: 0,
                    appId = parts[1],
                    appName = parts[2],
                    category = parts[3],
                    sizeBytes = parts[4].toLongOrNull() ?: 0
                )
            } else null
        }
    }

    private fun serializeInstallHistory(history: List<InstallEntry>): String {
        return history.takeLast(100) // Keep last 100
            .joinToString(";") { "${it.date},${it.appId},${it.appName},${it.category},${it.sizeBytes}" }
    }

    private fun parseCategoryBreakdown(data: String): Map<String, Int> {
        if (data.isEmpty()) return emptyMap()
        return data.split(";").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                parts[0] to (parts[1].toIntOrNull() ?: 0)
            } else null
        }.toMap()
    }

    private fun serializeCategoryBreakdown(data: Map<String, Int>): String {
        return data.entries.joinToString(";") { "${it.key}:${it.value}" }
    }

    private fun parseHourlyActivity(data: String): Map<Int, Int> {
        if (data.isEmpty()) return emptyMap()
        return data.split(";").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                (parts[0].toIntOrNull() ?: 0) to (parts[1].toIntOrNull() ?: 0)
            } else null
        }.toMap()
    }

    private fun serializeHourlyActivity(data: Map<Int, Int>): String {
        return data.entries.joinToString(";") { "${it.key}:${it.value}" }
    }

    private fun parseDailyActivity(data: String): Map<String, Long> {
        if (data.isEmpty()) return emptyMap()
        return data.split(";").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                parts[0] to (parts[1].toLongOrNull() ?: 0)
            } else null
        }.toMap()
    }

    private fun serializeDailyActivity(data: Map<String, Long>): String {
        return data.entries.joinToString(";") { "${it.key}:${it.value}" }
    }

    // ═════════════════════════════════════════════════════════════
    // SINGLETON
    // ═════════════════════════════════════════════════════════════

    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)

    companion object {
        @Volatile
        private var instance: AnalyticsManager? = null

        fun getInstance(context: Context): AnalyticsManager {
            return instance ?: synchronized(this) {
                instance ?: AnalyticsManager(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// DATA CLASSES
// ═════════════════════════════════════════════════════════════

data class UserStats(
    val totalSessions: Int,
    val totalTimeSpent: Long,
    val totalInstalls: Int,
    val themesTried: Int,
    val effectsUsed: Int,
    val achievementsUnlocked: Int,
    val shopPurchases: Int
) {
    val averageSessionMinutes: Long
        get() = if (totalSessions > 0) (totalTimeSpent / 1000 / 60) / totalSessions else 0
    
    val totalTimeHours: Long
        get() = totalTimeSpent / 1000 / 60 / 60
}

data class InstallEntry(
    val date: Long,
    val appId: String,
    val appName: String,
    val category: String,
    val sizeBytes: Long
)

data class WeeklyInsights(
    val mostInstalledCategory: String,
    val averageDailyInstalls: Float,
    val favoriteFeature: String,
    val engagementTrend: EngagementTrend
)

enum class EngagementTrend {
    RISING, STABLE, DECLINING
}
