package com.sugarmunch.app.rewards

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.progression.ProgressionTracker
import com.sugarmunch.app.shop.ShopManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.TimeUnit

private val Context.rewardsDataStore: DataStore<Preferences> by preferencesDataStore(name = "daily_rewards")

/**
 * Daily Rewards System - Login streaks with escalating rewards
 * Day 7 rewards are LEGENDARY
 * 
 * Note for Hilt migration: Use @ApplicationContext for the Context parameter
 */
class DailyRewardsManager constructor(private val context: Context) {

    private val dataStore = context.rewardsDataStore
    private val shopManager = ShopManager.getInstance(context)
    private val progressionTracker = ProgressionTracker.getInstance(context)

    // Preferences keys
    private object Keys {
        val LAST_CLAIM_DATE = longPreferencesKey("last_claim_date")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LONGEST_STREAK = intPreferencesKey("longest_streak")
        val TOTAL_CLAIMS = intPreferencesKey("total_claims")
        val CLAIM_HISTORY = stringPreferencesKey("claim_history")
        val STREAK_MILESTONES = stringPreferencesKey("streak_milestones")
        val BONUS_MULTIPLIER = floatPreferencesKey("bonus_multiplier")
    }

    // ═════════════════════════════════════════════════════════════
    // REWARD TIERS
    // ═════════════════════════════════════════════════════════════
    companion object {
        val REWARD_TIERS = listOf(
            DayReward(1, 50, 10, RewardType.COMMON, "Welcome Back"),
            DayReward(2, 75, 15, RewardType.COMMON, "Building Momentum"),
            DayReward(3, 100, 20, RewardType.UNCOMMON, "Getting Sweet"),
            DayReward(4, 125, 25, RewardType.UNCOMMON, "Sugar Rush"),
            DayReward(5, 150, 30, RewardType.RARE, "Candy Collector"),
            DayReward(6, 200, 40, RewardType.RARE, "Almost Legendary"),
            DayReward(7, 500, 100, RewardType.LEGENDARY, "WEEKLY BONUS", isMilestone = true),
            DayReward(8, 100, 20, RewardType.UNCOMMON, "New Week"),
            DayReward(9, 125, 25, RewardType.UNCOMMON, "Keep Going"),
            DayReward(10, 150, 30, RewardType.RARE, "Dedicated"),
            DayReward(11, 175, 35, RewardType.RARE, "Sweet Tooth"),
            DayReward(12, 200, 40, RewardType.EPIC, "Candy King"),
            DayReward(13, 250, 50, RewardType.EPIC, "Almost There"),
            DayReward(14, 750, 150, RewardType.LEGENDARY, "2-WEEK STREAK", isMilestone = true),
            DayReward(15, 200, 40, RewardType.EPIC, "Half Month Hero"),
            DayReward(16, 225, 45, RewardType.EPIC, "Unstoppable"),
            DayReward(17, 250, 50, RewardType.EPIC, "Sugar Master"),
            DayReward(18, 275, 55, RewardType.EPIC, "Candy Champion"),
            DayReward(19, 300, 60, RewardType.EPIC, "Almost Monthly"),
            DayReward(20, 350, 70, RewardType.EPIC, "20 Days Strong"),
            DayReward(21, 400, 80, RewardType.LEGENDARY, "3-Week Warrior"),
            DayReward(22, 350, 70, RewardType.EPIC, "Going Strong"),
            DayReward(23, 375, 75, RewardType.EPIC, "Dedicated User"),
            DayReward(24, 400, 80, RewardType.EPIC, "Sweet Legend"),
            DayReward(25, 450, 90, RewardType.EPIC, "Candy Master"),
            DayReward(26, 500, 100, RewardType.LEGENDARY, "Elite Status"),
            DayReward(27, 550, 110, RewardType.LEGENDARY, "Super Sweet"),
            DayReward(28, 600, 120, RewardType.LEGENDARY, "Monthly Marvel"),
            DayReward(29, 700, 140, RewardType.LEGENDARY, "Almost Perfect"),
            DayReward(30, 1000, 250, RewardType.ULTIMATE, "MONTHLY MASTER", isMilestone = true)
        )

        const val STREAK_RESET_HOURS = 48 // Hours before streak resets
        const val BONUS_DAY_INTERVAL = 7 // Every 7 days is a bonus
    }

    // ═════════════════════════════════════════════════════════════
    // STATE FLOWS
    // ═════════════════════════════════════════════════════════════

    val currentStreak: Flow<Int> = dataStore.data.map { it[Keys.CURRENT_STREAK] ?: 0 }
    val longestStreak: Flow<Int> = dataStore.data.map { it[Keys.LONGEST_STREAK] ?: 0 }
    val totalClaims: Flow<Int> = dataStore.data.map { it[Keys.TOTAL_CLAIMS] ?: 0 }
    val lastClaimDate: Flow<Long> = dataStore.data.map { it[Keys.LAST_CLAIM_DATE] ?: 0 }

    val canClaimToday: Flow<Boolean> = dataStore.data.map { prefs ->
        val lastClaim = prefs[Keys.LAST_CLAIM_DATE] ?: 0
        val today = getTodayStart()
        lastClaim < today
    }

    val nextReward: Flow<DayReward> = currentStreak.map { streak ->
        val day = (streak % 30) + 1
        REWARD_TIERS.find { it.day == day } ?: REWARD_TIERS.first()
    }

    val streakStatus: Flow<StreakStatus> = combine(
        currentStreak,
        lastClaimDate,
        canClaimToday
    ) { streak, lastClaim, canClaim ->
        val hoursSinceClaim = (System.currentTimeMillis() - lastClaim) / (1000 * 60 * 60)
        when {
            canClaim -> {
                if (streak > 0 && hoursSinceClaim in 24 until STREAK_RESET_HOURS) {
                    StreakStatus.AT_RISK(
                        streak,
                        (STREAK_RESET_HOURS - hoursSinceClaim).toInt().coerceAtLeast(0)
                    )
                } else {
                    StreakStatus.CLAIM_AVAILABLE(streak)
                }
            }
            else -> StreakStatus.ALREADY_CLAIMED(streak)
        }
    }

    // ═════════════════════════════════════════════════════════════
    // CLAIM REWARDS
    // ═════════════════════════════════════════════════════════════

    suspend fun claimDailyReward(): ClaimResult {
        val today = getTodayStart()
        val lastClaim = lastClaimDate.first()
        val currentStreakValue = currentStreak.first()

        // Check if already claimed today
        if (lastClaim >= today) {
            return ClaimResult.ALREADY_CLAIMED
        }

        // Calculate streak
        val yesterday = today - TimeUnit.DAYS.toMillis(1)
        val newStreak = if (lastClaim >= yesterday) {
            currentStreakValue + 1 // Continue streak
        } else if ((today - lastClaim) > TimeUnit.HOURS.toMillis(STREAK_RESET_HOURS.toLong())) {
            1 // Reset streak
        } else {
            currentStreakValue + 1 // Continue streak (within grace period)
        }

        // Get reward for this day
        val dayOfCycle = ((newStreak - 1) % 30) + 1
        val reward = REWARD_TIERS.find { it.day == dayOfCycle } ?: REWARD_TIERS.first()

        // Apply bonus multiplier
        val multiplier = calculateMultiplier(newStreak)
        val finalSugarPoints = (reward.sugarPoints * multiplier).toInt()
        val finalXP = (reward.xp * multiplier).toInt()

        // Award rewards
        shopManager.addSugarPoints(finalSugarPoints, "daily_reward_day_$newStreak")
        shopManager.addXP(finalXP)

        // Update streak data
        dataStore.edit { prefs ->
            prefs[Keys.LAST_CLAIM_DATE] = System.currentTimeMillis()
            prefs[Keys.CURRENT_STREAK] = newStreak
            prefs[Keys.TOTAL_CLAIMS] = (prefs[Keys.TOTAL_CLAIMS] ?: 0) + 1
            
            if (newStreak > (prefs[Keys.LONGEST_STREAK] ?: 0)) {
                prefs[Keys.LONGEST_STREAK] = newStreak
            }

            // Save claim to history
            val history = parseClaimHistory(prefs[Keys.CLAIM_HISTORY] ?: "")
            val newEntry = ClaimHistoryEntry(
                date = System.currentTimeMillis(),
                dayNumber = newStreak,
                sugarPoints = finalSugarPoints,
                xp = finalXP
            )
            prefs[Keys.CLAIM_HISTORY] = serializeClaimHistory(history + newEntry)

            // Check for milestones
            if (reward.isMilestone) {
                val milestones = parseMilestones(prefs[Keys.STREAK_MILESTONES] ?: "")
                prefs[Keys.STREAK_MILESTONES] = serializeMilestones(milestones + newStreak)
            }
        }

        progressionTracker.onDailyRewardClaimed(newStreak)

        return ClaimResult.SUCCESS(
            day = newStreak,
            reward = reward,
            sugarPoints = finalSugarPoints,
            xp = finalXP,
            multiplier = multiplier
        )
    }

    // ═════════════════════════════════════════════════════════════
    // STREAK MANAGEMENT
    // ═════════════════════════════════════════════════════════════

    suspend fun resetStreak() {
        dataStore.edit { prefs ->
            prefs[Keys.CURRENT_STREAK] = 0
        }
    }

    suspend fun freezeStreak(): Boolean {
        // Use a streak freeze item (if available in inventory)
        // For now, just extends the claim window
        val lastClaim = lastClaimDate.first()
        dataStore.edit { prefs ->
            prefs[Keys.LAST_CLAIM_DATE] = lastClaim + TimeUnit.HOURS.toMillis(24)
        }
        return true
    }

    fun getRewardPreview(day: Int): DayReward? {
        val dayOfCycle = ((day - 1) % 30) + 1
        return REWARD_TIERS.find { it.day == dayOfCycle }
    }

    fun getWeekPreview(startDay: Int): List<DayReward> {
        return (startDay..minOf(startDay + 6, 30)).mapNotNull { getRewardPreview(it) }
    }

    // ═════════════════════════════════════════════════════════════
    // CALENDAR & HISTORY
    // ═════════════════════════════════════════════════════════════

    val claimHistory: Flow<List<ClaimHistoryEntry>> = dataStore.data.map { prefs ->
        parseClaimHistory(prefs[Keys.CLAIM_HISTORY] ?: "")
    }

    val streakMilestones: Flow<List<Int>> = dataStore.data.map { prefs ->
        parseMilestones(prefs[Keys.STREAK_MILESTONES] ?: "")
    }

    fun getMonthlyCalendar(year: Int, month: Int): List<CalendarDay> {
        val calendar = Calendar.getInstance().apply {
            set(year, month, 1)
        }
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        return (1..daysInMonth).map { day ->
            val date = Calendar.getInstance().apply {
                set(year, month, day)
            }.timeInMillis
            
            CalendarDay(
                day = day,
                date = date,
                isToday = isToday(date),
                isClaimed = isDateClaimed(date),
                isMissed = isDateMissed(date)
            )
        }
    }

    // ═════════════════════════════════════════════════════════════
    // HELPERS
    // ═════════════════════════════════════════════════════════════

    private fun calculateMultiplier(streak: Int): Float {
        return when {
            streak >= 30 -> 2.0f // Monthly master
            streak >= 21 -> 1.75f
            streak >= 14 -> 1.5f
            streak >= 7 -> 1.25f
            else -> 1.0f
        }
    }

    private fun getTodayStart(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun isToday(date: Long): Boolean {
        val today = getTodayStart()
        return date in today..(today + TimeUnit.DAYS.toMillis(1))
    }

    private fun isDateClaimed(date: Long): Boolean {
        val dayStart = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val dayEnd = dayStart + TimeUnit.DAYS.toMillis(1)

        return runBlocking {
            parseClaimHistory(dataStore.data.first()[Keys.CLAIM_HISTORY] ?: "")
                .any { it.date in dayStart until dayEnd }
        }
    }

    private fun isDateMissed(date: Long): Boolean {
        val today = getTodayStart()
        return date < today && !isDateClaimed(date)
    }

    private fun parseClaimHistory(data: String): List<ClaimHistoryEntry> {
        if (data.isEmpty()) return emptyList()
        return data.split(";").mapNotNull { entry ->
            val parts = entry.split(",")
            if (parts.size >= 4) {
                ClaimHistoryEntry(
                    date = parts[0].toLongOrNull() ?: 0,
                    dayNumber = parts[1].toIntOrNull() ?: 0,
                    sugarPoints = parts[2].toIntOrNull() ?: 0,
                    xp = parts[3].toIntOrNull() ?: 0
                )
            } else null
        }
    }

    private fun serializeClaimHistory(history: List<ClaimHistoryEntry>): String {
        return history.joinToString(";") { 
            "${it.date},${it.dayNumber},${it.sugarPoints},${it.xp}" 
        }
    }

    private fun parseMilestones(data: String): List<Int> {
        if (data.isEmpty()) return emptyList()
        return data.split(",").mapNotNull { it.toIntOrNull() }
    }

    private fun serializeMilestones(milestones: List<Int>): String {
        return milestones.joinToString(",")
    }

}

// ═════════════════════════════════════════════════════════════
// DATA CLASSES
// ═════════════════════════════════════════════════════════════

data class DayReward(
    val day: Int,
    val sugarPoints: Int,
    val xp: Int,
    val type: RewardType,
    val title: String,
    val isMilestone: Boolean = false,
    val bonusItem: String? = null
)

enum class RewardType {
    COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, ULTIMATE
}

sealed class StreakStatus {
    data class CLAIM_AVAILABLE(val currentStreak: Int) : StreakStatus()
    data class ALREADY_CLAIMED(val currentStreak: Int) : StreakStatus()
    data class AT_RISK(val currentStreak: Int, val hoursRemaining: Int) : StreakStatus()
}

sealed class ClaimResult {
    data class SUCCESS(
        val day: Int,
        val reward: DayReward,
        val sugarPoints: Int,
        val xp: Int,
        val multiplier: Float
    ) : ClaimResult()
    object ALREADY_CLAIMED : ClaimResult()
    object STREAK_BROKEN : ClaimResult()
}

data class ClaimHistoryEntry(
    val date: Long,
    val dayNumber: Int,
    val sugarPoints: Int,
    val xp: Int
)

data class CalendarDay(
    val day: Int,
    val date: Long,
    val isToday: Boolean,
    val isClaimed: Boolean,
    val isMissed: Boolean
)
