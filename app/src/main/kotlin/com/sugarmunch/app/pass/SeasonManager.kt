package com.sugarmunch.app.pass

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private val Context.seasonDataStore: DataStore<Preferences> by preferencesDataStore(name = "sugar_pass_seasons")

/**
 * Season Manager - Handles Sugar Pass season lifecycle
 * 
 * Seasons rotate monthly with unique themes:
 * - January: Winter Wonderland
 * - February: Sweethearts
 * - March: Lucky Charms (St. Patrick's)
 * - April: Spring Bloom
 * - May: Candy Carnival
 * - June: Summer Splash
 * - July: Firecracker
 * - August: Beach Treats
 * - September: Back to School
 * - October: Spooky Sugar (Halloween)
 * - November: Harvest Gold
 * - December: Frostbite Festival (Winter)
 * 
 * Each season has:
 * - Exclusive themes
 * - Exclusive effects
 * - Exclusive badges
 * - Season-specific challenges
 * - Limited-time rewards
 */
class SeasonManager private constructor(private val context: Context) {

    private val dataStore = context.seasonDataStore

    // ═════════════════════════════════════════════════════════════
    // SEASON DEFINITIONS
    // ═════════════════════════════════════════════════════════════
    
    companion object {
        const val SEASON_DURATION_DAYS = 30
        const val SEASON_OVERLAP_HOURS = 24 // Previous season rewards can be claimed for 24h
        
        val ALL_SEASONS = listOf(
            SeasonInfo(
                id = "winter_wonderland",
                name = "Winter Wonderland",
                month = Calendar.JANUARY,
                theme = SeasonTheme(
                    primaryColor = 0xFF87CEEB, // Sky blue
                    secondaryColor = 0xFFFFFFFF, // White
                    accentColor = 0xFFB0E0E6, // Powder blue
                    backgroundStyle = SeasonBackgroundStyle.SNOW
                ),
                description = "Chill out with frosty rewards and icy cool themes!",
                exclusiveRewards = listOf("theme_arctic", "effect_snowfall", "badge_penguin"),
                emoji = "❄️"
            ),
            SeasonInfo(
                id = "sweethearts",
                name = "Sweethearts",
                month = Calendar.FEBRUARY,
                theme = SeasonTheme(
                    primaryColor = 0xFFFF69B4, // Hot pink
                    secondaryColor = 0xFFFF1493, // Deep pink
                    accentColor = 0xFFFFC0CB, // Pink
                    backgroundStyle = SeasonBackgroundStyle.HEARTS
                ),
                description = "Share the love with heart-themed rewards!",
                exclusiveRewards = listOf("theme_valentine", "effect_heartburst", "badge_cupid"),
                emoji = "💝"
            ),
            SeasonInfo(
                id = "lucky_charms",
                name = "Lucky Charms",
                month = Calendar.MARCH,
                theme = SeasonTheme(
                    primaryColor = 0xFF32CD32, // Lime green
                    secondaryColor = 0xFF228B22, // Forest green
                    accentColor = 0xFFFFD700, // Gold
                    backgroundStyle = SeasonBackgroundStyle.CLOVERS
                ),
                description = "Get lucky with shamrock surprises and gold rewards!",
                exclusiveRewards = listOf("theme_shamrock", "effect_rainbow", "badge_leprechaun"),
                emoji = "🍀"
            ),
            SeasonInfo(
                id = "spring_bloom",
                name = "Spring Bloom",
                month = Calendar.APRIL,
                theme = SeasonTheme(
                    primaryColor = 0xFFFFB6C1, // Light pink
                    secondaryColor = 0xFF98FB98, // Pale green
                    accentColor = 0xFFFFD700, // Gold
                    backgroundStyle = SeasonBackgroundStyle.FLOWERS
                ),
                description = "Watch rewards bloom with spring freshness!",
                exclusiveRewards = listOf("theme_spring", "effect_pollen", "badge_butterfly"),
                emoji = "🌸"
            ),
            SeasonInfo(
                id = "candy_carnival",
                name = "Candy Carnival",
                month = Calendar.MAY,
                theme = SeasonTheme(
                    primaryColor = 0xFFFF1493, // Deep pink
                    secondaryColor = 0xFF00CED1, // Dark turquoise
                    accentColor = 0xFFFFD700, // Gold
                    backgroundStyle = SeasonBackgroundStyle.CARNIVAL
                ),
                description = "Step right up for circus-themed fun and prizes!",
                exclusiveRewards = listOf("theme_circus", "effect_confetti", "badge_ringmaster"),
                emoji = "🎪"
            ),
            SeasonInfo(
                id = "summer_splash",
                name = "Summer Splash",
                month = Calendar.JUNE,
                theme = SeasonTheme(
                    primaryColor = 0xFF00CED1, // Dark turquoise
                    secondaryColor = 0xFF87CEEB, // Sky blue
                    accentColor = 0xFFFFD700, // Gold
                    backgroundStyle = SeasonBackgroundStyle.BEACH
                ),
                description = "Dive into cool summer rewards!",
                exclusiveRewards = listOf("theme_tropical", "effect_waves", "badge_surfer"),
                emoji = "🏖️"
            ),
            SeasonInfo(
                id = "firecracker",
                name = "Firecracker",
                month = Calendar.JULY,
                theme = SeasonTheme(
                    primaryColor = 0xFFFF4500, // Orange red
                    secondaryColor = 0xFFDC143C, // Crimson
                    accentColor = 0xFFFFD700, // Gold
                    backgroundStyle = SeasonBackgroundStyle.FIREWORKS
                ),
                description = "Explosive rewards that light up the sky!",
                exclusiveRewards = listOf("theme_fireworks", "effect_sparklers", "badge_patriot"),
                emoji = "🎆"
            ),
            SeasonInfo(
                id = "beach_treats",
                name = "Beach Treats",
                month = Calendar.AUGUST,
                theme = SeasonTheme(
                    primaryColor = 0xFFF4A460, // Sandy brown
                    secondaryColor = 0xFF00CED1, // Dark turquoise
                    accentColor = 0xFFFFD700, // Gold
                    backgroundStyle = SeasonBackgroundStyle.OCEAN
                ),
                description = "Soak up the sun with beachy rewards!",
                exclusiveRewards = listOf("theme_beach", "effect_sandstorm", "badge_sunbather"),
                emoji = "🏝️"
            ),
            SeasonInfo(
                id = "back_to_school",
                name = "Back to School",
                month = Calendar.SEPTEMBER,
                theme = SeasonTheme(
                    primaryColor = 0xFF4169E1, // Royal blue
                    secondaryColor = 0xFFFFD700, // Gold
                    accentColor = 0xFFDC143C, // Crimson
                    backgroundStyle = SeasonBackgroundStyle.CLASSROOM
                ),
                description = "Learn and earn with academic achievements!",
                exclusiveRewards = listOf("theme_scholar", "effect_graduation", "badge_valedictorian"),
                emoji = "📚"
            ),
            SeasonInfo(
                id = "spooky_sugar",
                name = "Spooky Sugar",
                month = Calendar.OCTOBER,
                theme = SeasonTheme(
                    primaryColor = 0xFF800080, // Purple
                    secondaryColor = 0xFFFF8C00, // Dark orange
                    accentColor = 0xFF000000, // Black
                    backgroundStyle = SeasonBackgroundStyle.SPOOKY
                ),
                description = "Trick or treat yourself to haunting rewards!",
                exclusiveRewards = listOf("theme_halloween", "effect_ghosts", "badge_vampire"),
                emoji = "🎃"
            ),
            SeasonInfo(
                id = "harvest_gold",
                name = "Harvest Gold",
                month = Calendar.NOVEMBER,
                theme = SeasonTheme(
                    primaryColor = 0xFFDAA520, // Goldenrod
                    secondaryColor = 0xFF8B4513, // Saddle brown
                    accentColor = 0xFFFF8C00, // Dark orange
                    backgroundStyle = SeasonBackgroundStyle.AUTUMN
                ),
                description = "Give thanks for bountiful harvest rewards!",
                exclusiveRewards = listOf("theme_harvest", "effect_leaves", "badge_pilgrim"),
                emoji = "🦃"
            ),
            SeasonInfo(
                id = "frostbite_festival",
                name = "Frostbite Festival",
                month = Calendar.DECEMBER,
                theme = SeasonTheme(
                    primaryColor = 0xFF4682B4, // Steel blue
                    secondaryColor = 0xFFFFFFFF, // White
                    accentColor = 0xFF32CD32, // Lime green
                    backgroundStyle = SeasonBackgroundStyle.HOLIDAY
                ),
                description = "Unwrap festive rewards all month long!",
                exclusiveRewards = listOf("theme_winter", "effect_snowflakes", "badge_elf"),
                emoji = "🎄"
            )
        )
    }

    // ═════════════════════════════════════════════════════════════
    // PREFERENCES KEYS
    // ═════════════════════════════════════════════════════════════
    
    private object Keys {
        val CURRENT_SEASON_ID = stringPreferencesKey("current_season_id")
        val SEASON_START_TIME = longPreferencesKey("season_start_time")
        val SEASON_END_TIME = longPreferencesKey("season_end_time")
        val LAST_SEASON_ID = stringPreferencesKey("last_season_id")
        val SEASON_HISTORY = stringPreferencesKey("season_history")
        
        // User progress for current season
        val USER_SEASON_TIER = intPreferencesKey("user_season_tier")
        val USER_SEASON_XP = intPreferencesKey("user_season_xp")
        val PREMIUM_PURCHASED = booleanPreferencesKey("premium_purchased")
        val CLAIMED_REWARDS = stringPreferencesKey("claimed_rewards")
        
        // Stats
        val TOTAL_SEASONS_COMPLETED = intPreferencesKey("total_seasons_completed")
        val HIGHEST_TIER_REACHED = intPreferencesKey("highest_tier_reached")
    }

    // ═════════════════════════════════════════════════════════════
    // STATE FLOWS
    // ═════════════════════════════════════════════════════════════
    
    val currentSeason: Flow<SeasonInfo?> = dataStore.data.map { prefs ->
        val seasonId = prefs[Keys.CURRENT_SEASON_ID]
        seasonId?.let { getSeasonById(it) }
    }
    
    val seasonTimeRemaining: Flow<Long> = dataStore.data.map { prefs ->
        val endTime = prefs[Keys.SEASON_END_TIME] ?: 0
        (endTime - System.currentTimeMillis()).coerceAtLeast(0)
    }
    
    val isPremium: Flow<Boolean> = dataStore.data.map { it[Keys.PREMIUM_PURCHASED] ?: false }
    
    val userSeasonTier: Flow<Int> = dataStore.data.map { it[Keys.USER_SEASON_TIER] ?: 1 }
    val userSeasonXp: Flow<Int> = dataStore.data.map { it[Keys.USER_SEASON_XP] ?: 0 }
    
    val claimedRewards: Flow<List<String>> = dataStore.data.map { prefs ->
        (prefs[Keys.CLAIMED_REWARDS] ?: "").split(",").filter { it.isNotEmpty() }
    }

    // ═════════════════════════════════════════════════════════════
    // SEASON MANAGEMENT
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Get current season info
     */
    fun getCurrentSeasonInfo(): SeasonInfo? {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        return ALL_SEASONS.find { it.month == currentMonth }
    }
    
    /**
     * Get season by ID
     */
    fun getSeasonById(id: String): SeasonInfo? {
        return ALL_SEASONS.find { it.id == id }
    }
    
    /**
     * Get next season
     */
    fun getNextSeason(): SeasonInfo? {
        val calendar = Calendar.getInstance()
        val nextMonth = (calendar.get(Calendar.MONTH) + 1) % 12
        return ALL_SEASONS.find { it.month == nextMonth }
    }
    
    /**
     * Get time remaining in current season
     */
    fun getTimeRemaining(): Long {
        val calendar = Calendar.getInstance()
        val endOfMonth = calendar.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
        return (endOfMonth - System.currentTimeMillis()).coerceAtLeast(0)
    }
    
    /**
     * Format time remaining as string
     */
    fun formatTimeRemaining(millis: Long): String {
        val days = TimeUnit.MILLISECONDS.toDays(millis)
        val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        
        return when {
            days > 0 -> "$days" + "d ${hours}h"
            hours > 0 -> "$hours" + "h ${minutes}m"
            else -> "$minutes" + "m"
        }
    }

    // ═════════════════════════════════════════════════════════════
    // SEASON LIFECYCLE
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Check and handle season transition
     */
    suspend fun checkSeasonTransition(): SeasonTransitionResult {
        val currentStored = dataStore.data.first()[Keys.CURRENT_SEASON_ID]
        val actualCurrent = getCurrentSeasonInfo()?.id
        
        return when {
            currentStored == null && actualCurrent != null -> {
                // First time - start new season
                startNewSeason(actualCurrent)
                SeasonTransitionResult.NEW_SEASON(actualCurrent)
            }
            currentStored != actualCurrent && actualCurrent != null -> {
                // Season changed - end old, start new
                endCurrentSeason()
                startNewSeason(actualCurrent)
                SeasonTransitionResult.SEASON_CHANGE(currentStored ?: "", actualCurrent)
            }
            else -> SeasonTransitionResult.NO_CHANGE
        }
    }
    
    /**
     * Start a new season
     */
    suspend fun startNewSeason(seasonId: String) {
        val season = getSeasonById(seasonId) ?: return
        val now = System.currentTimeMillis()
        val endTime = getEndOfMonthMillis()
        
        dataStore.edit { prefs ->
            // Save previous season to history
            val lastSeason = prefs[Keys.CURRENT_SEASON_ID]
            if (lastSeason != null) {
                prefs[Keys.LAST_SEASON_ID] = lastSeason
                addToHistory(prefs, lastSeason, prefs[Keys.USER_SEASON_TIER] ?: 1)
            }
            
            // Start new season
            prefs[Keys.CURRENT_SEASON_ID] = seasonId
            prefs[Keys.SEASON_START_TIME] = now
            prefs[Keys.SEASON_END_TIME] = endTime
            
            // Reset progress (but keep premium if auto-renew)
            prefs[Keys.USER_SEASON_TIER] = 1
            prefs[Keys.USER_SEASON_XP] = 0
            prefs[Keys.CLAIMED_REWARDS] = ""
            
            // Update stats
            if (lastSeason != null) {
                prefs[Keys.TOTAL_SEASONS_COMPLETED] = (prefs[Keys.TOTAL_SEASONS_COMPLETED] ?: 0) + 1
            }
        }
    }
    
    /**
     * End current season
     */
    suspend fun endCurrentSeason() {
        dataStore.edit { prefs ->
            val seasonId = prefs[Keys.CURRENT_SEASON_ID]
            val finalTier = prefs[Keys.USER_SEASON_TIER] ?: 1
            
            if (seasonId != null) {
                addToHistory(prefs, seasonId, finalTier)
                
                // Update highest tier
                val currentHighest = prefs[Keys.HIGHEST_TIER_REACHED] ?: 0
                if (finalTier > currentHighest) {
                    prefs[Keys.HIGHEST_TIER_REACHED] = finalTier
                }
            }
        }
    }
    
    /**
     * Force start a new season (for testing)
     */
    suspend fun forceStartSeason(seasonId: String) {
        startNewSeason(seasonId)
    }

    // ═════════════════════════════════════════════════════════════
    // PREMIUM MANAGEMENT
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Purchase premium pass
     */
    suspend fun purchasePremium(): Boolean {
        // In real implementation, this would handle payment
        dataStore.edit { prefs ->
            prefs[Keys.PREMIUM_PURCHASED] = true
        }
        return true
    }
    
    /**
     * Check if user has premium
     */
    suspend fun hasPremium(): Boolean {
        return dataStore.data.first()[Keys.PREMIUM_PURCHASED] ?: false
    }

    // ═════════════════════════════════════════════════════════════
    // REWARD CLAIMING
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Check if a reward has been claimed
     */
    suspend fun isRewardClaimed(tier: Int, track: RewardTrack): Boolean {
        val claimed = claimedRewards.first()
        val rewardId = "${tier}_${track.name}"
        return rewardId in claimed
    }
    
    /**
     * Mark a reward as claimed
     */
    suspend fun claimReward(tier: Int, track: RewardTrack): Boolean {
        val isPremiumUser = hasPremium()
        if (track != RewardTrack.FREE && !isPremiumUser) {
            return false // Can't claim premium without pass
        }
        
        val currentTier = userSeasonTier.first()
        if (tier > currentTier) {
            return false // Haven't reached this tier
        }
        
        dataStore.edit { prefs ->
            val claimed = (prefs[Keys.CLAIMED_REWARDS] ?: "").split(",").toMutableList()
            val rewardId = "${tier}_${track.name}"
            if (rewardId !in claimed) {
                claimed.add(rewardId)
                prefs[Keys.CLAIMED_REWARDS] = claimed.joinToString(",")
            }
        }
        return true
    }

    // ═════════════════════════════════════════════════════════════
    // HISTORY
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Get season history
     */
    fun getSeasonHistory(): Flow<List<SeasonHistoryEntry>> = dataStore.data.map { prefs ->
        parseSeasonHistory(prefs[Keys.SEASON_HISTORY] ?: "")
    }
    
    /**
     * Get stats
     */
    suspend fun getStats(): SeasonStats {
        val prefs = dataStore.data.first()
        return SeasonStats(
            totalSeasonsCompleted = prefs[Keys.TOTAL_SEASONS_COMPLETED] ?: 0,
            highestTierReached = prefs[Keys.HIGHEST_TIER_REACHED] ?: 0,
            currentSeasonProgress = prefs[Keys.USER_SEASON_TIER] ?: 1
        )
    }

    // ═════════════════════════════════════════════════════════════
    // HELPERS
    // ═════════════════════════════════════════════════════════════
    
    private fun getEndOfMonthMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
    
    private fun addToHistory(prefs: MutablePreferences, seasonId: String, finalTier: Int) {
        val current = parseSeasonHistory(prefs[Keys.SEASON_HISTORY] ?: "")
        val newEntry = SeasonHistoryEntry(
            seasonId = seasonId,
            completedAt = System.currentTimeMillis(),
            finalTier = finalTier,
            hadPremium = prefs[Keys.PREMIUM_PURCHASED] ?: false
        )
        prefs[Keys.SEASON_HISTORY] = serializeSeasonHistory(current + newEntry)
    }
    
    private fun parseSeasonHistory(data: String): List<SeasonHistoryEntry> {
        if (data.isEmpty()) return emptyList()
        return data.split(";").mapNotNull { entry ->
            val parts = entry.split(",")
            if (parts.size >= 4) {
                SeasonHistoryEntry(
                    seasonId = parts[0],
                    completedAt = parts[1].toLongOrNull() ?: 0,
                    finalTier = parts[2].toIntOrNull() ?: 1,
                    hadPremium = parts[3].toBoolean()
                )
            } else null
        }
    }
    
    private fun serializeSeasonHistory(history: List<SeasonHistoryEntry>): String {
        return history.takeLast(24).joinToString(";") { 
            "${it.seasonId},${it.completedAt},${it.finalTier},${it.hadPremium}" 
        }
    }

    // ═════════════════════════════════════════════════════════════
    // SINGLETON
    // ═════════════════════════════════════════════════════════════
    
    companion object {
        @Volatile
        private var instance: SeasonManager? = null

        fun getInstance(context: Context): SeasonManager {
            return instance ?: synchronized(this) {
                instance ?: SeasonManager(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// DATA CLASSES
// ═════════════════════════════════════════════════════════════

data class SeasonInfo(
    val id: String,
    val name: String,
    val month: Int,
    val theme: SeasonTheme,
    val description: String,
    val exclusiveRewards: List<String>,
    val emoji: String
)

data class SeasonTheme(
    val primaryColor: Long,
    val secondaryColor: Long,
    val accentColor: Long,
    val backgroundStyle: SeasonBackgroundStyle
)

enum class SeasonBackgroundStyle {
    SNOW,       // Falling snowflakes
    HEARTS,     // Floating hearts
    CLOVERS,    // Shamrocks
    FLOWERS,    // Spring flowers
    CARNIVAL,   // Circus colors
    BEACH,      // Beach vibes
    FIREWORKS,  // Explosions
    OCEAN,      // Waves
    CLASSROOM,  // School theme
    SPOOKY,     // Halloween
    AUTUMN,     // Falling leaves
    HOLIDAY     // Christmas/winter
}

sealed class SeasonTransitionResult {
    data class NEW_SEASON(val seasonId: String) : SeasonTransitionResult()
    data class SEASON_CHANGE(val fromSeason: String, val toSeason: String) : SeasonTransitionResult()
    object NO_CHANGE : SeasonTransitionResult()
}

data class SeasonHistoryEntry(
    val seasonId: String,
    val completedAt: Long,
    val finalTier: Int,
    val hadPremium: Boolean
)

data class SeasonStats(
    val totalSeasonsCompleted: Int,
    val highestTierReached: Int,
    val currentSeasonProgress: Int
)
