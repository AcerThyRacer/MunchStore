package com.sugarmunch.app.events

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/**
 * SugarMunch Event Challenges System
 * 
 * Challenge Types:
 * - Install X apps during the event
 * - Use Y effect Z times
 * - Earn W event points
 * - Complete daily/weekly tasks
 * - Social challenges (share, invite friends)
 */

enum class ChallengeType {
    INSTALL_APPS,
    USE_EFFECT,
    EARN_POINTS,
    DAILY_LOGIN,
    SHARE_APP,
    USE_THEME,
    INVITE_FRIEND,
    COLLECT_ITEMS,
    STREAK_DAYS,
    SPECIAL_ACTION
}

enum class ChallengeFrequency {
    ONE_TIME,      // Complete once during event
    DAILY,         // Resets every day
    WEEKLY,        // Resets every week
    PROGRESSIVE    // Multi-tier challenge
}

enum class ChallengeDifficulty {
    EASY,
    MEDIUM,
    HARD,
    LEGENDARY
}

data class EventChallenge(
    val id: String,
    val eventId: String,
    val name: String,
    val description: String,
    val type: ChallengeType,
    val frequency: ChallengeFrequency,
    val difficulty: ChallengeDifficulty,
    val targetValue: Int,
    val rewardPoints: Int,
    val rewardBonus: ChallengeReward? = null,
    val icon: String,
    val order: Int = 0,
    val isSecret: Boolean = false,
    val prerequisites: List<String> = emptyList()
)

data class ChallengeReward(
    val gems: Int = 0,
    val xp: Int = 0,
    val itemId: String? = null,
    val itemName: String? = null
)

data class UserChallengeProgress(
    val challengeId: String,
    val currentValue: Int = 0,
    val targetValue: Int,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val claimedAt: Long? = null,
    val lastResetAt: Long = System.currentTimeMillis(),
    val streakCount: Int = 0
) {
    val progressPercent: Float
        get() = (currentValue.toFloat() / targetValue).coerceIn(0f, 1f)
    
    val isClaimed: Boolean
        get() = claimedAt != null
    
    val canClaim: Boolean
        get() = isCompleted && !isClaimed
}

object EventChallenges {
    
    // Halloween Challenges
    val HALLOWEEN_INSTALL_5 = EventChallenge(
        id = "halloween_install_5",
        eventId = "halloween_spooktacular",
        name = "Candy Collector",
        description = "Install 5 apps during the Spooktacular",
        type = ChallengeType.INSTALL_APPS,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.EASY,
        targetValue = 5,
        rewardPoints = 50,
        icon = "🎃"
    )
    
    val HALLOWEEN_SPOOKY_THEME = EventChallenge(
        id = "halloween_spooky_theme",
        eventId = "halloween_spooktacular",
        name = "Spooky Style",
        description = "Use a dark or spooky theme for 24 hours",
        type = ChallengeType.USE_THEME,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.EASY,
        targetValue = 1,
        rewardPoints = 30,
        icon = "🌙"
    )
    
    val HALLOWEEN_NIGHT_INSTALL = EventChallenge(
        id = "halloween_night_install",
        eventId = "halloween_spooktacular",
        name = "Night Owl",
        description = "Install an app between midnight and 3 AM",
        type = ChallengeType.SPECIAL_ACTION,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.MEDIUM,
        targetValue = 1,
        rewardPoints = 75,
        icon = "🦉",
        isSecret = true
    )
    
    val HALLOWEEN_CANDY_SHARE = EventChallenge(
        id = "halloween_candy_share",
        eventId = "halloween_spooktacular",
        name = "Trick or Treat",
        description = "Share 3 apps with friends",
        type = ChallengeType.SHARE_APP,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.EASY,
        targetValue = 3,
        rewardPoints = 40,
        icon = "🍬"
    )
    
    val HALLOWEEN_GHOST_HUNT = EventChallenge(
        id = "halloween_ghost_hunt",
        eventId = "halloween_spooktacular",
        name = "Ghost Hunter",
        description = "Find all 5 hidden ghosts in the app (tap secret spots!)",
        type = ChallengeType.COLLECT_ITEMS,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.HARD,
        targetValue = 5,
        rewardPoints = 150,
        icon = "👻",
        isSecret = true
    )
    
    // Winter Wonderland Challenges
    val WINTER_DAILY_LOGIN = EventChallenge(
        id = "winter_daily_login",
        eventId = "winter_wonderland",
        name = "Daily Wonderland",
        description = "Visit the app every day during the event",
        type = ChallengeType.DAILY_LOGIN,
        frequency = ChallengeFrequency.DAILY,
        difficulty = ChallengeDifficulty.EASY,
        targetValue = 1,
        rewardPoints = 20,
        icon = "📅"
    )
    
    val WINTER_FROZEN_THEME = EventChallenge(
        id = "winter_frozen_theme",
        eventId = "winter_wonderland",
        name = "Ice Palace",
        description = "Use a winter or ice-themed theme",
        type = ChallengeType.USE_THEME,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.EASY,
        targetValue = 1,
        rewardPoints = 30,
        icon = "🏰"
    )
    
    val WINTER_SHARE_WARMTH = EventChallenge(
        id = "winter_share_warmth",
        eventId = "winter_wonderland",
        name = "Share the Warmth",
        description = "Gift an app recommendation to a friend",
        type = ChallengeType.SHARE_APP,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.MEDIUM,
        targetValue = 1,
        rewardPoints = 50,
        icon = "🎁"
    )
    
    val WINTER_SNOWFLAKE_COLLECT = EventChallenge(
        id = "winter_snowflake_collect",
        eventId = "winter_wonderland",
        name = "Snowflake Collector",
        description = "Collect 50 snowflakes by browsing apps",
        type = ChallengeType.COLLECT_ITEMS,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.MEDIUM,
        targetValue = 50,
        rewardPoints = 100,
        icon = "❄️"
    )
    
    val WINTER_GIFT_GIVER = EventChallenge(
        id = "winter_gift_giver",
        eventId = "winter_wonderland",
        name = "Secret Santa",
        description = "Help 5 friends discover new apps",
        type = ChallengeType.INVITE_FRIEND,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.HARD,
        targetValue = 5,
        rewardPoints = 150,
        icon = "🎅"
    )
    
    // Valentine's Challenges
    val VALENTINE_SHARE_LOVE = EventChallenge(
        id = "valentine_share_love",
        eventId = "valentines_sweetheart",
        name = "Spread the Love",
        description = "Share your favorite app with someone special",
        type = ChallengeType.SHARE_APP,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.EASY,
        targetValue = 1,
        rewardPoints = 30,
        icon = "💌"
    )
    
    val VALENTINE_ROMANTIC_THEME = EventChallenge(
        id = "valentine_romantic_theme",
        eventId = "valentines_sweetheart",
        name = "Romantic Glow",
        description = "Use a pink or red theme for the day",
        type = ChallengeType.USE_THEME,
        frequency = ChallengeFrequency.DAILY,
        difficulty = ChallengeDifficulty.EASY,
        targetValue = 1,
        rewardPoints = 20,
        icon = "💕"
    )
    
    val VALENTINE_CHOCOLATE_COLLECT = EventChallenge(
        id = "valentine_chocolate_collect",
        eventId = "valentines_sweetheart",
        name = "Chocolate Lover",
        description = "Collect 25 chocolate hearts",
        type = ChallengeType.COLLECT_ITEMS,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.MEDIUM,
        targetValue = 25,
        rewardPoints = 75,
        icon = "🍫"
    )
    
    val VALENTINE_FRIEND_BONUS = EventChallenge(
        id = "valentine_friend_bonus",
        eventId = "valentines_sweetheart",
        name = "Love Multiplier",
        description = "Install apps with 3 friends on the same day",
        type = ChallengeType.SPECIAL_ACTION,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.HARD,
        targetValue = 1,
        rewardPoints = 150,
        icon = "💝"
    )
    
    val VALENTINE_DAILY_GIFT = EventChallenge(
        id = "valentine_daily_gift",
        eventId = "valentines_sweetheart",
        name = "Daily Sweetheart",
        description = "Send a daily gift to a friend",
        type = ChallengeType.DAILY_LOGIN,
        frequency = ChallengeFrequency.DAILY,
        difficulty = ChallengeDifficulty.EASY,
        targetValue = 1,
        rewardPoints = 25,
        icon = "🌹"
    )
    
    // Spring Blossom Challenges
    val SPRING_DAILY_BLOOM = EventChallenge(
        id = "spring_daily_bloom",
        eventId = "spring_blossom",
        name = "Daily Bloom",
        description = "Visit the Candy Garden daily",
        type = ChallengeType.DAILY_LOGIN,
        frequency = ChallengeFrequency.DAILY,
        difficulty = ChallengeDifficulty.EASY,
        targetValue = 1,
        rewardPoints = 20,
        icon = "🌸"
    )
    
    val SPRING_GARDEN_THEME = EventChallenge(
        id = "spring_garden_theme",
        eventId = "spring_blossom",
        name = "Garden Keeper",
        description = "Use a nature or spring theme",
        type = ChallengeType.USE_THEME,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.EASY,
        targetValue = 1,
        rewardPoints = 30,
        icon = "🌿"
    )
    
    val SPRING_BUNNY_HUNT = EventChallenge(
        id = "spring_bunny_hunt",
        eventId = "spring_blossom",
        name = "Bunny Hunt",
        description = "Find the hidden bunny 7 times",
        type = ChallengeType.COLLECT_ITEMS,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.MEDIUM,
        targetValue = 7,
        rewardPoints = 100,
        icon = "🐰"
    )
    
    val SPRING_POLLEN_COLLECT = EventChallenge(
        id = "spring_pollen_collect",
        eventId = "spring_blossom",
        name = "Pollen Collector",
        description = "Gather 100 pollen points by browsing",
        type = ChallengeType.COLLECT_ITEMS,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.MEDIUM,
        targetValue = 100,
        rewardPoints = 80,
        icon = "🐝"
    )
    
    val SPRING_FRIENDSHIP_GROW = EventChallenge(
        id = "spring_friendship_grow",
        eventId = "spring_blossom",
        name = "Growing Together",
        description = "Make 3 new friends during the event",
        type = ChallengeType.INVITE_FRIEND,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.HARD,
        targetValue = 3,
        rewardPoints = 120,
        icon = "🌱"
    )
    
    // Summer Splash Challenges
    val SUMMER_BEACH_THEME = EventChallenge(
        id = "summer_beach_theme",
        eventId = "summer_splash",
        name = "Beach Vibes",
        description = "Use a tropical or beach theme",
        type = ChallengeType.USE_THEME,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.EASY,
        targetValue = 1,
        rewardPoints = 30,
        icon = "🏖️"
    )
    
    val SUMMER_SPLASH_PARTY = EventChallenge(
        id = "summer_splash_party",
        eventId = "summer_splash",
        name = "Splash Party",
        description = "Install 10 apps during the summer splash",
        type = ChallengeType.INSTALL_APPS,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.MEDIUM,
        targetValue = 10,
        rewardPoints = 100,
        icon = "🎉"
    )
    
    val SUMMER_ICE_CREAM_COLLECT = EventChallenge(
        id = "summer_ice_cream_collect",
        eventId = "summer_splash",
        name = "Ice Cream Dream",
        description = "Collect 30 ice cream scoops",
        type = ChallengeType.COLLECT_ITEMS,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.MEDIUM,
        targetValue = 30,
        rewardPoints = 90,
        icon = "🍦"
    )
    
    val SUMMER_TROPICAL_DISCOVER = EventChallenge(
        id = "summer_tropical_discover",
        eventId = "summer_splash",
        name = "Tropical Explorer",
        description = "Discover 5 new tropical-themed apps",
        type = ChallengeType.SPECIAL_ACTION,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.MEDIUM,
        targetValue = 5,
        rewardPoints = 75,
        icon = "🌺"
    )
    
    val SUMMER_STREAK_HEAT = EventChallenge(
        id = "summer_streak_heat",
        eventId = "summer_splash",
        name = "Heat Wave",
        description = "Maintain a 7-day streak during summer",
        type = ChallengeType.STREAK_DAYS,
        frequency = ChallengeFrequency.ONE_TIME,
        difficulty = ChallengeDifficulty.HARD,
        targetValue = 7,
        rewardPoints = 150,
        icon = "🔥"
    )
    
    val ALL_CHALLENGES: List<EventChallenge> = listOf(
        HALLOWEEN_INSTALL_5, HALLOWEEN_SPOOKY_THEME, HALLOWEEN_NIGHT_INSTALL,
        HALLOWEEN_CANDY_SHARE, HALLOWEEN_GHOST_HUNT,
        WINTER_DAILY_LOGIN, WINTER_FROZEN_THEME, WINTER_SHARE_WARMTH,
        WINTER_SNOWFLAKE_COLLECT, WINTER_GIFT_GIVER,
        VALENTINE_SHARE_LOVE, VALENTINE_ROMANTIC_THEME, VALENTINE_CHOCOLATE_COLLECT,
        VALENTINE_FRIEND_BONUS, VALENTINE_DAILY_GIFT,
        SPRING_DAILY_BLOOM, SPRING_GARDEN_THEME, SPRING_BUNNY_HUNT,
        SPRING_POLLEN_COLLECT, SPRING_FRIENDSHIP_GROW,
        SUMMER_BEACH_THEME, SUMMER_SPLASH_PARTY, SUMMER_ICE_CREAM_COLLECT,
        SUMMER_TROPICAL_DISCOVER, SUMMER_STREAK_HEAT
    )
    
    fun getChallengesForEvent(eventId: String): List<EventChallenge> {
        return ALL_CHALLENGES.filter { it.eventId == eventId }
    }
    
    fun getChallengeById(challengeId: String): EventChallenge? {
        return ALL_CHALLENGES.find { it.id == challengeId }
    }
    
    fun getDifficultyMultiplier(difficulty: ChallengeDifficulty): Float {
        return when (difficulty) {
            ChallengeDifficulty.EASY -> 1.0f
            ChallengeDifficulty.MEDIUM -> 1.5f
            ChallengeDifficulty.HARD -> 2.0f
            ChallengeDifficulty.LEGENDARY -> 3.0f
        }
    }
}

private val Context.challengeDataStore: DataStore<Preferences> by preferencesDataStore(name = "event_challenges")

class ChallengeProgressManager(private val context: Context) {
    private val dataStore = context.challengeDataStore
    
    companion object {
        @Volatile
        private var instance: ChallengeProgressManager? = null
        
        fun getInstance(context: Context): ChallengeProgressManager {
            return instance ?: synchronized(this) {
                instance ?: ChallengeProgressManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    private fun getChallengeKey(challengeId: String, suffix: String): Preferences.Key<String> {
        return stringPreferencesKey("challenge_${challengeId}_$suffix")
    }
    
    suspend fun getProgress(challengeId: String): UserChallengeProgress? {
        val challenge = EventChallenges.getChallengeById(challengeId) ?: return null
        
        return dataStore.data.map { prefs ->
            UserChallengeProgress(
                challengeId = challengeId,
                currentValue = prefs[intPreferencesKey("challenge_${challengeId}_current")] ?: 0,
                targetValue = challenge.targetValue,
                isCompleted = prefs[booleanPreferencesKey("challenge_${challengeId}_completed")] ?: false,
                completedAt = prefs[longPreferencesKey("challenge_${challengeId}_completed_at")],
                claimedAt = prefs[longPreferencesKey("challenge_${challengeId}_claimed_at")],
                lastResetAt = prefs[longPreferencesKey("challenge_${challengeId}_reset_at")] ?: System.currentTimeMillis(),
                streakCount = prefs[intPreferencesKey("challenge_${challengeId}_streak")] ?: 0
            )
        }.first()
    }
    
    fun getProgressFlow(challengeId: String): Flow<UserChallengeProgress?> {
        val challenge = EventChallenges.getChallengeById(challengeId) ?: return flowOf(null)
        
        return dataStore.data.map { prefs ->
            UserChallengeProgress(
                challengeId = challengeId,
                currentValue = prefs[intPreferencesKey("challenge_${challengeId}_current")] ?: 0,
                targetValue = challenge.targetValue,
                isCompleted = prefs[booleanPreferencesKey("challenge_${challengeId}_completed")] ?: false,
                completedAt = prefs[longPreferencesKey("challenge_${challengeId}_completed_at")],
                claimedAt = prefs[longPreferencesKey("challenge_${challengeId}_claimed_at")],
                lastResetAt = prefs[longPreferencesKey("challenge_${challengeId}_reset_at")] ?: System.currentTimeMillis(),
                streakCount = prefs[intPreferencesKey("challenge_${challengeId}_streak")] ?: 0
            )
        }
    }
    
    suspend fun updateProgress(challengeId: String, increment: Int = 1): UserChallengeProgress? {
        val challenge = EventChallenges.getChallengeById(challengeId) ?: return null
        val currentProgress = getProgress(challengeId)
        
        val newValue = (currentProgress?.currentValue ?: 0) + increment
        val shouldComplete = newValue >= challenge.targetValue && !(currentProgress?.isCompleted ?: false)
        
        dataStore.edit { prefs ->
            prefs[intPreferencesKey("challenge_${challengeId}_current")] = newValue.coerceAtMost(challenge.targetValue)
            
            if (shouldComplete) {
                prefs[booleanPreferencesKey("challenge_${challengeId}_completed")] = true
                prefs[longPreferencesKey("challenge_${challengeId}_completed_at")] = System.currentTimeMillis()
            }
        }
        
        return getProgress(challengeId)
    }
    
    suspend fun setProgress(challengeId: String, value: Int): UserChallengeProgress? {
        val challenge = EventChallenges.getChallengeById(challengeId) ?: return null
        val currentProgress = getProgress(challengeId)
        
        val newValue = value.coerceIn(0, challenge.targetValue)
        val shouldComplete = newValue >= challenge.targetValue && !(currentProgress?.isCompleted ?: false)
        
        dataStore.edit { prefs ->
            prefs[intPreferencesKey("challenge_${challengeId}_current")] = newValue
            
            if (shouldComplete) {
                prefs[booleanPreferencesKey("challenge_${challengeId}_completed")] = true
                prefs[longPreferencesKey("challenge_${challengeId}_completed_at")] = System.currentTimeMillis()
            }
        }
        
        return getProgress(challengeId)
    }
    
    suspend fun claimChallengeReward(challengeId: String): Boolean {
        val progress = getProgress(challengeId) ?: return false
        
        if (!progress.canClaim) return false
        
        dataStore.edit { prefs ->
            prefs[longPreferencesKey("challenge_${challengeId}_claimed_at")] = System.currentTimeMillis()
        }
        
        return true
    }
    
    suspend fun resetDailyChallenges() {
        val dailyChallenges = EventChallenges.ALL_CHALLENGES.filter { it.frequency == ChallengeFrequency.DAILY }
        
        dataStore.edit { prefs ->
            dailyChallenges.forEach { challenge ->
                prefs[intPreferencesKey("challenge_${challenge.id}_current")] = 0
                prefs[booleanPreferencesKey("challenge_${challenge.id}_completed")] = false
                prefs.remove(longPreferencesKey("challenge_${challenge.id}_completed_at"))
                prefs.remove(longPreferencesKey("challenge_${challenge.id}_claimed_at"))
                prefs[longPreferencesKey("challenge_${challenge.id}_reset_at")] = System.currentTimeMillis()
            }
        }
    }
    
    suspend fun resetEventChallenges(eventId: String) {
        val eventChallenges = EventChallenges.getChallengesForEvent(eventId)
        
        dataStore.edit { prefs ->
            eventChallenges.forEach { challenge ->
                prefs[intPreferencesKey("challenge_${challenge.id}_current")] = 0
                prefs[booleanPreferencesKey("challenge_${challenge.id}_completed")] = false
                prefs.remove(longPreferencesKey("challenge_${challenge.id}_completed_at"))
                prefs.remove(longPreferencesKey("challenge_${challenge.id}_claimed_at"))
                prefs[longPreferencesKey("challenge_${challenge.id}_reset_at")] = System.currentTimeMillis()
                prefs[intPreferencesKey("challenge_${challenge.id}_streak")] = 0
            }
        }
    }
    
    suspend fun getAllProgressForEvent(eventId: String): List<UserChallengeProgress> {
        val challenges = EventChallenges.getChallengesForEvent(eventId)
        return challenges.mapNotNull { getProgress(it.id) }
    }
    
    suspend fun getCompletedChallengesCount(eventId: String): Int {
        return getAllProgressForEvent(eventId).count { it.isCompleted }
    }
    
    suspend fun getTotalEventPoints(eventId: String): Int {
        val challenges = EventChallenges.getChallengesForEvent(eventId)
        val progress = getAllProgressForEvent(eventId)
        
        return progress.filter { it.isClaimed }.sumOf { p ->
            challenges.find { it.id == p.challengeId }?.rewardPoints ?: 0
        }
    }
    
    suspend fun shouldResetDaily(): Boolean {
        val lastReset = dataStore.data.map { 
            it[longPreferencesKey("last_daily_reset")] ?: 0 
        }.first()
        
        val lastResetDate = LocalDate.ofEpochDay(lastReset / (24 * 60 * 60 * 1000))
        return lastResetDate.isBefore(LocalDate.now())
    }
    
    suspend fun recordDailyReset() {
        dataStore.edit { prefs ->
            prefs[longPreferencesKey("last_daily_reset")] = System.currentTimeMillis()
        }
    }
}
