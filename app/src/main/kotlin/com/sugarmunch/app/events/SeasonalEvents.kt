package com.sugarmunch.app.events

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.util.CoroutineUtils.safeCollect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.eventsDataStore: DataStore<Preferences> by preferencesDataStore(name = "seasonal_events")

/**
 * Seasonal Events System
 * Limited-time events with exclusive rewards
 */
@Singleton
class SeasonalEventManager @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.eventsDataStore

    companion object {
        val ACTIVE_EVENT_KEY = stringPreferencesKey("active_event")
        val EVENT_PROGRESS_KEY = intPreferencesKey("event_progress")
        val EVENT_CLAIMED_REWARDS_KEY = stringPreferencesKey("event_claimed_rewards")
        val LAST_EVENT_CHECK_KEY = longPreferencesKey("last_event_check")
    }

    /**
     * Get current active seasonal event
     */
    fun getCurrentEvent(): Flow<SeasonalEvent?> {
        return dataStore.data.map { prefs ->
            val eventId = prefs[ACTIVE_EVENT_KEY]
            eventId?.let { getEventById(it) }
        }.safeCollect()
    }

    /**
     * Get event by ID
     */
    private fun getEventById(eventId: String): SeasonalEvent? {
        return ALL_SEASONAL_EVENTS.find { it.id == eventId }
    }

    /**
     * Get event progress
     */
    fun getEventProgress(): Flow<Int> {
        return dataStore.data.map { prefs ->
            prefs[EVENT_PROGRESS_KEY] ?: 0
        }.safeCollect()
    }

    /**
     * Update event progress
     */
    suspend fun updateEventProgress(progress: Int) {
        dataStore.edit { prefs ->
            prefs[EVENT_PROGRESS_KEY] = progress
        }
    }

    /**
     * Check if reward has been claimed
     */
    suspend fun isRewardClaimed(rewardId: String): Boolean {
        val claimed = getClaimedRewards()
        return claimed.contains(rewardId)
    }

    /**
     * Get claimed rewards
     */
    private suspend fun getClaimedRewards(): List<String> {
        return dataStore.data.map { prefs ->
            val jsonStr = prefs[EVENT_CLAIMED_REWARDS_KEY] ?: "[]"
            // Simple parsing for demo - would use JSON in production
            jsonStr.split(",").filter { it.isNotBlank() }
        }.first()
    }

    /**
     * Claim event reward
     */
    suspend fun claimReward(rewardId: String) {
        val claimed = getClaimedRewards().toMutableList()
        claimed.add(rewardId)
        dataStore.edit { prefs ->
            prefs[EVENT_CLAIMED_REWARDS_KEY] = claimed.joinToString(",")
        }
    }

    /**
     * Check and update active event based on date
     */
    suspend fun updateActiveEvent() {
        val calendar = java.util.Calendar.getInstance()
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        val activeEvent = ALL_SEASONAL_EVENTS.find { event ->
            val startCal = java.util.Calendar.getInstance().apply {
                time = java.util.Date(event.startDate)
            }
            val endCal = java.util.Calendar.getInstance().apply {
                time = java.util.Date(event.endDate)
            }
            val now = System.currentTimeMillis()
            now in event.startDate..event.endDate
        }

        dataStore.edit { prefs ->
            prefs[ACTIVE_EVENT_KEY] = activeEvent?.id
            prefs[LAST_EVENT_CHECK_KEY] = System.currentTimeMillis()
        }
    }

    /**
     * Get all upcoming events
     */
    fun getUpcomingEvents(): List<SeasonalEvent> {
        val now = System.currentTimeMillis()
        return ALL_SEASONAL_EVENTS.filter { it.startDate > now }
            .sortedBy { it.startDate }
    }

    /**
     * Get event by season
     */
    fun getEventsBySeason(season: Season): List<SeasonalEvent> {
        return ALL_SEASONAL_EVENTS.filter { it.season == season }
    }
}

/**
 * Seasonal Event data class
 */
data class SeasonalEvent(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val season: Season,
    val startDate: Long,
    val endDate: Long,
    val tasks: List<EventTask>,
    val rewards: List<EventReward>,
    val isRecurring: Boolean = true
)

/**
 * Event Task
 */
data class EventTask(
    val id: String,
    val title: String,
    val description: String,
    val requirement: TaskRequirement,
    val progress: Int = 0,
    val isCompleted: Boolean = false
)

/**
 * Task Requirement
 */
sealed class TaskRequirement {
    data class InstallApps(val count: Int) : TaskRequirement()
    data class EnableEffects(val count: Int) : TaskRequirement()
    data class TryThemes(val count: Int) : TaskRequirement()
    data class DailyLogins(val count: Int) : TaskRequirement()
    data class ShareApps(val count: Int) : TaskRequirement()
    data class EarnSugarPoints(val count: Int) : TaskRequirement()
}

/**
 * Event Reward
 */
data class EventReward(
    val id: String,
    val title: String,
    val description: String,
    val rewardType: RewardType,
    val rewardValue: Int,
    val requiredProgress: Int,
    val isExclusive: Boolean = false,
    val icon: String = "🎁"
)

/**
 * Reward Types
 */
enum class RewardType {
    SUGAR_POINTS,
    XP,
    EXCLUSIVE_THEME,
    EXCLUSIVE_EFFECT,
    EXCLUSIVE_BADGE,
    BOOST,
    BUNDLE
}

/**
 * Seasons
 */
enum class Season {
    WINTER,
    SPRING,
    SUMMER,
    FALL,
    HOLIDAY
}

/**
 * All Seasonal Events
 */
val ALL_SEASONAL_EVENTS = listOf(
    // Halloween Event
    SeasonalEvent(
        id = "halloween_2024",
        name = "Spooky Sugar Rush",
        description = "Collect candy and unlock spooky themes!",
        icon = "🎃",
        season = Season.FALL,
        startDate = java.util.GregorianCalendar(2024, 9, 15).timeInMillis,
        endDate = java.util.GregorianCalendar(2024, 10, 1).timeInMillis,
        tasks = listOf(
            EventTask(
                id = "halloween_task_1",
                title = "Spooky Installs",
                description = "Install 5 apps",
                requirement = TaskRequirement.InstallApps(5)
            ),
            EventTask(
                id = "halloween_task_2",
                title = "Haunted Themes",
                description = "Try 3 dark themes",
                requirement = TaskRequirement.TryThemes(3)
            ),
            EventTask(
                id = "halloween_task_3",
                title = "Trick or Treat",
                description = "Login for 7 days",
                requirement = TaskRequirement.DailyLogins(7)
            )
        ),
        rewards = listOf(
            EventReward(
                id = "halloween_reward_1",
                title = "Pumpkin Points",
                description = "500 Sugar Points",
                rewardType = RewardType.SUGAR_POINTS,
                rewardValue = 500,
                requiredProgress = 100,
                icon = "🍬"
            ),
            EventReward(
                id = "halloween_reward_2",
                title = "Ghost Theme",
                description = "Exclusive Halloween theme",
                rewardType = RewardType.EXCLUSIVE_THEME,
                rewardValue = 0,
                requiredProgress = 300,
                isExclusive = true,
                icon = "👻"
            )
        )
    ),

    // Christmas Event
    SeasonalEvent(
        id = "christmas_2024",
        name = "Sweet Christmas",
        description = "Celebrate the holidays with sweet rewards!",
        icon = "🎄",
        season = Season.WINTER,
        startDate = java.util.GregorianCalendar(2024, 11, 15).timeInMillis,
        endDate = java.util.GregorianCalendar(2024, 11, 26).timeInMillis,
        tasks = listOf(
            EventTask(
                id = "xmas_task_1",
                title = "Gift of Apps",
                description = "Share 5 apps with friends",
                requirement = TaskRequirement.ShareApps(5)
            ),
            EventTask(
                id = "xmas_task_2",
                title = "12 Days of SugarMunch",
                description = "Login for 12 days",
                requirement = TaskRequirement.DailyLogins(12)
            )
        ),
        rewards = listOf(
            EventReward(
                id = "xmas_reward_1",
                title = "Holiday Bonus",
                description = "1000 Sugar Points",
                rewardType = RewardType.SUGAR_POINTS,
                rewardValue = 1000,
                requiredProgress = 100,
                icon = "🎁"
            ),
            EventReward(
                id = "xmas_reward_2",
                title = "Peppermint Theme",
                description = "Exclusive Christmas theme",
                rewardType = RewardType.EXCLUSIVE_THEME,
                rewardValue = 0,
                requiredProgress = 200,
                isExclusive = true,
                icon = "🍬"
            )
        )
    ),

    // New Year Event
    SeasonalEvent(
        id = "newyear_2025",
        name = "Fresh Start Festival",
        description = "Start the new year with fresh themes!",
        icon = "🎉",
        season = Season.WINTER,
        startDate = java.util.GregorianCalendar(2024, 11, 27).timeInMillis,
        endDate = java.util.GregorianCalendar(2025, 0, 5).timeInMillis,
        tasks = listOf(
            EventTask(
                id = "newyear_task_1",
                title = "Resolution Keeper",
                description = "Enable effects for 5 days",
                requirement = TaskRequirement.EnableEffects(5)
            )
        ),
        rewards = listOf(
            EventReward(
                id = "newyear_reward_1",
                title = "New Year Bonus",
                description = "2025 Sugar Points",
                rewardType = RewardType.SUGAR_POINTS,
                rewardValue = 2025,
                requiredProgress = 100,
                icon = "🎊"
            )
        )
    ),

    // Valentine's Event
    SeasonalEvent(
        id = "valentines_2025",
        name = "Sweet Love",
        description = "Share the love with sweet themes!",
        icon = "💝",
        season = Season.WINTER,
        startDate = java.util.GregorianCalendar(2025, 1, 10).timeInMillis,
        endDate = java.util.GregorianCalendar(2025, 1, 15).timeInMillis,
        tasks = listOf(
            EventTask(
                id = "valentines_task_1",
                title = "Spread Love",
                description = "Share 10 apps",
                requirement = TaskRequirement.ShareApps(10)
            )
        ),
        rewards = listOf(
            EventReward(
                id = "valentines_reward_1",
                title = "Love Points",
                description = "520 Sugar Points",
                rewardType = RewardType.SUGAR_POINTS,
                rewardValue = 520,
                requiredProgress = 100,
                icon = "💕"
            )
        )
    ),

    // Spring Event
    SeasonalEvent(
        id = "spring_2025",
        name = "Spring Bloom",
        description = "Fresh themes for spring!",
        icon = "🌸",
        season = Season.SPRING,
        startDate = java.util.GregorianCalendar(2025, 2, 20).timeInMillis,
        endDate = java.util.GregorianCalendar(2025, 3, 20).timeInMillis,
        tasks = listOf(
            EventTask(
                id = "spring_task_1",
                title = "Spring Cleaning",
                description = "Try 5 new themes",
                requirement = TaskRequirement.TryThemes(5)
            )
        ),
        rewards = listOf(
            EventReward(
                id = "spring_reward_1",
                title = "Bloom Bonus",
                description = "Spring Theme Pack",
                rewardType = RewardType.EXCLUSIVE_THEME,
                rewardValue = 0,
                requiredProgress = 100,
                isExclusive = true,
                icon = "🌺"
            )
        )
    ),

    // Summer Event
    SeasonalEvent(
        id = "summer_2025",
        name = "Summer Splash",
        description = "Dive into summer fun!",
        icon = "🌊",
        season = Season.SUMMER,
        startDate = java.util.GregorianCalendar(2025, 5, 20).timeInMillis,
        endDate = java.util.GregorianCalendar(2025, 5, 31).timeInMillis,
        tasks = listOf(
            EventTask(
                id = "summer_task_1",
                title = "Beach Vibes",
                description = "Earn 1000 Sugar Points",
                requirement = TaskRequirement.EarnSugarPoints(1000)
            )
        ),
        rewards = listOf(
            EventReward(
                id = "summer_reward_1",
                title = "Summer Pack",
                description = "Exclusive summer themes",
                rewardType = RewardType.EXCLUSIVE_THEME,
                rewardValue = 0,
                requiredProgress = 100,
                isExclusive = true,
                icon = "🏖️"
            )
        )
    )
)
