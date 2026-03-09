package com.sugarmunch.app.events

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*
import java.time.LocalDate

/**
 * SugarMunch Event Rewards System
 * 
 * Manages event-exclusive rewards:
 * - Limited-time themes and effects
 * - Event badges
 * - Event currency
 * - Special titles
 */

data class EventRewardItem(
    val id: String,
    val eventId: String,
    val name: String,
    val description: String,
    val type: RewardType,
    val rarity: RewardRarity,
    val icon: String,
    val requiredPoints: Int,
    val unlockRequirement: UnlockRequirement,
    val previewData: RewardPreview? = null,
    val isPremium: Boolean = false,
    val isSecret: Boolean = false
)

enum class RewardType {
    THEME,
    EFFECT,
    BADGE,
    TITLE,
    CURRENCY,
    SHOP_ITEM,
    EXCLUSIVE_APP
}

enum class RewardRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY,
    EVENT_EXCLUSIVE
}

sealed class UnlockRequirement {
    data class PointsRequirement(val points: Int) : UnlockRequirement()
    data class ChallengeRequirement(val challengeIds: List<String>) : UnlockRequirement()
    data class StreakRequirement(val days: Int) : UnlockRequirement()
    data class CombinedRequirement(
        val points: Int,
        val challenges: List<String>,
        val daysActive: Int
    ) : UnlockRequirement()
    object SecretRequirement : UnlockRequirement()
}

data class RewardPreview(
    val themeColors: List<androidx.compose.ui.graphics.Color>? = null,
    val effectAnimationUrl: String? = null,
    val badgeFrame: String? = null
)

data class UserRewardState(
    val rewardId: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val isClaimed: Boolean = false,
    val claimedAt: Long? = null,
    val progress: Int = 0
)

object EventRewardCatalog {
    
    // Halloween Rewards
    val SPOOKY_CANDY_THEME = EventRewardItem(
        id = "spooky_candy_theme",
        eventId = "halloween_spooktacular",
        name = "Spooky Candy",
        description = "A hauntingly beautiful dark theme with ghostly purples and pumpkin oranges",
        type = RewardType.THEME,
        rarity = RewardRarity.EVENT_EXCLUSIVE,
        icon = "🎃",
        requiredPoints = 100,
        unlockRequirement = UnlockRequirement.PointsRequirement(100)
    )
    
    val GHOSTLY_PRESENCE_EFFECT = EventRewardItem(
        id = "ghostly_presence_effect",
        eventId = "halloween_spooktacular",
        name = "Ghostly Presence",
        description = "Ethereal ghosts float across your screen with every tap",
        type = RewardType.EFFECT,
        rarity = RewardRarity.EVENT_EXCLUSIVE,
        icon = "👻",
        requiredPoints = 250,
        unlockRequirement = UnlockRequirement.PointsRequirement(250)
    )
    
    val SPIRIT_WALKER_BADGE = EventRewardItem(
        id = "spirit_walker_badge",
        eventId = "halloween_spooktacular",
        name = "Spirit Walker",
        description = "Proof you survived the Candy Spooktacular",
        type = RewardType.BADGE,
        rarity = RewardRarity.EPIC,
        icon = "🎖️",
        requiredPoints = 500,
        unlockRequirement = UnlockRequirement.ChallengeRequirement(
            listOf("halloween_install_5", "halloween_ghost_hunt")
        )
    )
    
    val CANDY_GHOUL_TITLE = EventRewardItem(
        id = "candy_ghoul_title",
        eventId = "halloween_spooktacular",
        name = "Candy Ghoul",
        description = "A legendary title for true Halloween masters",
        type = RewardType.TITLE,
        rarity = RewardRarity.LEGENDARY,
        icon = "🏷️",
        requiredPoints = 750,
        unlockRequirement = UnlockRequirement.CombinedRequirement(
            points = 750,
            challenges = listOf("halloween_install_5", "halloween_ghost_hunt", "halloween_night_install"),
            daysActive = 5
        ),
        isSecret = true
    )
    
    // Winter Wonderland Rewards
    val FROSTED_CANDY_THEME = EventRewardItem(
        id = "frosted_candy_theme",
        eventId = "winter_wonderland",
        name = "Frosted Candy",
        description = "Cool ice blues and snowy whites create a winter paradise",
        type = RewardType.THEME,
        rarity = RewardRarity.EVENT_EXCLUSIVE,
        icon = "❄️",
        requiredPoints = 100,
        unlockRequirement = UnlockRequirement.PointsRequirement(100)
    )
    
    val BLIZZARD_RUSH_EFFECT = EventRewardItem(
        id = "blizzard_rush_effect",
        eventId = "winter_wonderland",
        name = "Blizzard Rush",
        description = "Gentle snowfall drifts across your apps",
        type = RewardType.EFFECT,
        rarity = RewardRarity.EVENT_EXCLUSIVE,
        icon = "🌨️",
        requiredPoints = 250,
        unlockRequirement = UnlockRequirement.PointsRequirement(250)
    )
    
    val ICE_MASTER_BADGE = EventRewardItem(
        id = "ice_master_badge",
        eventId = "winter_wonderland",
        name = "Ice Master",
        description = "Conquered the frozen wonderland",
        type = RewardType.BADGE,
        rarity = RewardRarity.EPIC,
        icon = "🧊",
        requiredPoints = 500,
        unlockRequirement = UnlockRequirement.ChallengeRequirement(
            listOf("winter_snowflake_collect", "winter_gift_giver")
        )
    )
    
    val WINTER_CRYSTALS = EventRewardItem(
        id = "winter_crystals",
        eventId = "winter_wonderland",
        name = "Winter Crystals",
        description = "500 bonus Sugar Gems",
        type = RewardType.CURRENCY,
        rarity = RewardRarity.RARE,
        icon = "💎",
        requiredPoints = 300,
        unlockRequirement = UnlockRequirement.PointsRequirement(300)
    )
    
    // Valentine's Rewards
    val SWEETHEART_THEME = EventRewardItem(
        id = "sweetheart_theme",
        eventId = "valentines_sweetheart",
        name = "Sweetheart",
        description = "Romantic pinks and reds with heart motifs",
        type = RewardType.THEME,
        rarity = RewardRarity.EVENT_EXCLUSIVE,
        icon = "💝",
        requiredPoints = 100,
        unlockRequirement = UnlockRequirement.PointsRequirement(100)
    )
    
    val LOVE_BURST_EFFECT = EventRewardItem(
        id = "love_burst_effect",
        eventId = "valentines_sweetheart",
        name = "Love Burst",
        description = "Hearts burst and float with every interaction",
        type = RewardType.EFFECT,
        rarity = RewardRarity.EVENT_EXCLUSIVE,
        icon = "💖",
        requiredPoints = 250,
        unlockRequirement = UnlockRequirement.PointsRequirement(250)
    )
    
    val SWEETHEART_BADGE = EventRewardItem(
        id = "sweetheart_badge_reward",
        eventId = "valentines_sweetheart",
        name = "True Sweetheart",
        description = "A true romantic at heart",
        type = RewardType.BADGE,
        rarity = RewardRarity.EPIC,
        icon = "💕",
        requiredPoints = 400,
        unlockRequirement = UnlockRequirement.ChallengeRequirement(
            listOf("valentine_share_love", "valentine_friend_bonus")
        )
    )
    
    // Spring Blossom Rewards
    val BLOSSOM_THEME = EventRewardItem(
        id = "blossom_theme",
        eventId = "spring_blossom",
        name = "Blossom",
        description = "Fresh spring greens with cherry blossom pinks",
        type = RewardType.THEME,
        rarity = RewardRarity.EVENT_EXCLUSIVE,
        icon = "🌸",
        requiredPoints = 100,
        unlockRequirement = UnlockRequirement.PointsRequirement(100)
    )
    
    val PETAL_DANCE_EFFECT = EventRewardItem(
        id = "petal_dance_effect",
        eventId = "spring_blossom",
        name = "Petal Dance",
        description = "Flower petals drift gently across your screen",
        type = RewardType.EFFECT,
        rarity = RewardRarity.EVENT_EXCLUSIVE,
        icon = "🌺",
        requiredPoints = 250,
        unlockRequirement = UnlockRequirement.PointsRequirement(250)
    )
    
    val GARDEN_KEEPER_BADGE = EventRewardItem(
        id = "garden_keeper_badge",
        eventId = "spring_blossom",
        name = "Garden Keeper",
        description = "Nurtured the Candy Garden to full bloom",
        type = RewardType.BADGE,
        rarity = RewardRarity.EPIC,
        icon = "🌷",
        requiredPoints = 500,
        unlockRequirement = UnlockRequirement.ChallengeRequirement(
            listOf("spring_bunny_hunt", "spring_friendship_grow")
        )
    )
    
    // Summer Splash Rewards
    val TROPICAL_CANDY_THEME = EventRewardItem(
        id = "tropical_candy_theme",
        eventId = "summer_splash",
        name = "Tropical Candy",
        description = "Bright tropical colors with ocean blues and sunset oranges",
        type = RewardType.THEME,
        rarity = RewardRarity.EVENT_EXCLUSIVE,
        icon = "🏖️",
        requiredPoints = 100,
        unlockRequirement = UnlockRequirement.PointsRequirement(100)
    )
    
    val OCEAN_BREEZE_EFFECT = EventRewardItem(
        id = "ocean_breeze_effect",
        eventId = "summer_splash",
        name = "Ocean Breeze",
        description = "Gentle waves ripple across your apps",
        type = RewardType.EFFECT,
        rarity = RewardRarity.EVENT_EXCLUSIVE,
        icon = "🌊",
        requiredPoints = 250,
        unlockRequirement = UnlockRequirement.PointsRequirement(250)
    )
    
    val BEACH_BOSS_BADGE = EventRewardItem(
        id = "beach_boss_badge",
        eventId = "summer_splash",
        name = "Beach Boss",
        description = "Ruled the summer waves",
        type = RewardType.BADGE,
        rarity = RewardRarity.EPIC,
        icon = "🏄",
        requiredPoints = 500,
        unlockRequirement = UnlockRequirement.CombinedRequirement(
            points = 500,
            challenges = listOf("summer_splash_party", "summer_streak_heat"),
            daysActive = 7
        )
    )
    
    val ALL_REWARDS: List<EventRewardItem> = listOf(
        SPOOKY_CANDY_THEME, GHOSTLY_PRESENCE_EFFECT, SPIRIT_WALKER_BADGE, CANDY_GHOUL_TITLE,
        FROSTED_CANDY_THEME, BLIZZARD_RUSH_EFFECT, ICE_MASTER_BADGE, WINTER_CRYSTALS,
        SWEETHEART_THEME, LOVE_BURST_EFFECT, SWEETHEART_BADGE,
        BLOSSOM_THEME, PETAL_DANCE_EFFECT, GARDEN_KEEPER_BADGE,
        TROPICAL_CANDY_THEME, OCEAN_BREEZE_EFFECT, BEACH_BOSS_BADGE
    )
    
    fun getRewardsForEvent(eventId: String): List<EventRewardItem> {
        return ALL_REWARDS.filter { it.eventId == eventId }
    }
    
    fun getRewardById(rewardId: String): EventRewardItem? {
        return ALL_REWARDS.find { it.id == rewardId }
    }
    
    fun getRewardsByType(type: RewardType): List<EventRewardItem> {
        return ALL_REWARDS.filter { it.type == type }
    }
    
    fun getRewardsByRarity(rarity: RewardRarity): List<EventRewardItem> {
        return ALL_REWARDS.filter { it.rarity == rarity }
    }
    
    fun getRarityColor(rarity: RewardRarity): androidx.compose.ui.graphics.Color {
        return when (rarity) {
            RewardRarity.COMMON -> androidx.compose.ui.graphics.Color(0xFFB0BEC5)
            RewardRarity.UNCOMMON -> androidx.compose.ui.graphics.Color(0xFF81C784)
            RewardRarity.RARE -> androidx.compose.ui.graphics.Color(0xFF64B5F6)
            RewardRarity.EPIC -> androidx.compose.ui.graphics.Color(0xFFBA68C8)
            RewardRarity.LEGENDARY -> androidx.compose.ui.graphics.Color(0xFFFFD54F)
            RewardRarity.EVENT_EXCLUSIVE -> androidx.compose.ui.graphics.Color(0xFFFF6B6B)
        }
    }
}

private val Context.rewardDataStore: DataStore<Preferences> by preferencesDataStore(name = "event_rewards")

class EventRewardsManager(private val context: Context) {
    private val dataStore = context.rewardDataStore
    private val challengeManager = ChallengeProgressManager.getInstance(context)
    
    companion object {
        @Volatile
        private var instance: EventRewardsManager? = null
        
        fun getInstance(context: Context): EventRewardsManager {
            return instance ?: synchronized(this) {
                instance ?: EventRewardsManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    fun getRewardState(rewardId: String): Flow<UserRewardState> {
        return dataStore.data.map { prefs ->
            UserRewardState(
                rewardId = rewardId,
                isUnlocked = prefs[booleanPreferencesKey("reward_${rewardId}_unlocked")] ?: false,
                unlockedAt = prefs[longPreferencesKey("reward_${rewardId}_unlocked_at")],
                isClaimed = prefs[booleanPreferencesKey("reward_${rewardId}_claimed")] ?: false,
                claimedAt = prefs[longPreferencesKey("reward_${rewardId}_claimed_at")],
                progress = prefs[intPreferencesKey("reward_${rewardId}_progress")] ?: 0
            )
        }
    }
    
    suspend fun getRewardStateSync(rewardId: String): UserRewardState {
        return getRewardState(rewardId).first()
    }
    
    suspend fun checkAndUnlockRewards(eventId: String): List<EventRewardItem> {
        val rewards = EventRewardCatalog.getRewardsForEvent(eventId)
        val newlyUnlocked = mutableListOf<EventRewardItem>()
        
        rewards.forEach { reward ->
            if (isRewardAvailable(reward.id) && !isRewardUnlocked(reward.id)) {
                unlockReward(reward.id)
                newlyUnlocked.add(reward)
            }
        }
        
        return newlyUnlocked
    }
    
    suspend fun isRewardAvailable(rewardId: String): Boolean {
        val reward = EventRewardCatalog.getRewardById(rewardId) ?: return false
        val currentPoints = challengeManager.getTotalEventPoints(reward.eventId)
        
        return when (val req = reward.unlockRequirement) {
            is UnlockRequirement.PointsRequirement -> currentPoints >= req.points
            is UnlockRequirement.ChallengeRequirement -> {
                req.challengeIds.all { challengeId ->
                    val progress = challengeManager.getProgress(challengeId)
                    progress?.isCompleted == true
                }
            }
            is UnlockRequirement.StreakRequirement -> {
                // Check streak implementation would go here
                false
            }
            is UnlockRequirement.CombinedRequirement -> {
                val pointsMet = currentPoints >= req.points
                val challengesMet = req.challenges.all { challengeId ->
                    challengeManager.getProgress(challengeId)?.isCompleted == true
                }
                pointsMet && challengesMet
            }
            is UnlockRequirement.SecretRequirement -> {
                // Secret requirements are handled specially
                false
            }
        }
    }
    
    private suspend fun isRewardUnlocked(rewardId: String): Boolean {
        return getRewardStateSync(rewardId).isUnlocked
    }
    
    private suspend fun unlockReward(rewardId: String) {
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("reward_${rewardId}_unlocked")] = true
            prefs[longPreferencesKey("reward_${rewardId}_unlocked_at")] = System.currentTimeMillis()
        }
    }
    
    suspend fun claimReward(rewardId: String): Boolean {
        val state = getRewardStateSync(rewardId)
        if (!state.isUnlocked || state.isClaimed) return false
        
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("reward_${rewardId}_claimed")] = true
            prefs[longPreferencesKey("reward_${rewardId}_claimed_at")] = System.currentTimeMillis()
        }
        
        // Apply reward effect
        applyReward(rewardId)
        
        return true
    }
    
    private suspend fun applyReward(rewardId: String) {
        val reward = EventRewardCatalog.getRewardById(rewardId) ?: return
        
        when (reward.type) {
            RewardType.THEME -> {
                // Apply theme through ThemeManager
                // ThemeManager.getInstance(context).setThemeById(reward.id)
            }
            RewardType.EFFECT -> {
                // Enable effect through EffectEngine
                // EffectEngine.getInstance(context).enableEffect(reward.id)
            }
            RewardType.CURRENCY -> {
                // Add currency to user's balance
                if (reward.id == "winter_crystals") {
                    // Add 500 gems
                }
            }
            else -> { /* Other types handled separately */ }
        }
    }
    
    suspend fun getUnlockedRewardsForEvent(eventId: String): List<EventRewardItem> {
        val rewards = EventRewardCatalog.getRewardsForEvent(eventId)
        return rewards.filter { isRewardUnlocked(it.id) }
    }
    
    suspend fun getClaimedRewardsForEvent(eventId: String): List<EventRewardItem> {
        val rewards = EventRewardCatalog.getRewardsForEvent(eventId)
        return rewards.filter { getRewardStateSync(it.id).isClaimed }
    }
    
    fun getAvailableRewardsForEvent(eventId: String): Flow<List<Pair<EventRewardItem, Boolean>>> {
        val rewards = EventRewardCatalog.getRewardsForEvent(eventId)
        return dataStore.data.map { prefs ->
            rewards.map { reward ->
                val isUnlocked = prefs[booleanPreferencesKey("reward_${reward.id}_unlocked")] ?: false
                reward to isUnlocked
            }
        }
    }
    
    suspend fun getTotalPointsNeeded(eventId: String): Int {
        return EventRewardCatalog.getRewardsForEvent(eventId)
            .maxOfOrNull { it.requiredPoints } ?: 0
    }
    
    suspend fun getProgressToNextReward(eventId: String, currentPoints: Int): Pair<EventRewardItem?, Int> {
        val rewards = EventRewardCatalog.getRewardsForEvent(eventId)
            .sortedBy { it.requiredPoints }
        
        val nextReward = rewards.find { it.requiredPoints > currentPoints && !isRewardUnlocked(it.id) }
        val pointsNeeded = nextReward?.let { it.requiredPoints - currentPoints } ?: 0
        
        return nextReward to pointsNeeded
    }
    
    suspend fun hasClaimedAllRewards(eventId: String): Boolean {
        val rewards = EventRewardCatalog.getRewardsForEvent(eventId)
        return rewards.all { getRewardStateSync(it.id).isClaimed }
    }
    
    suspend fun getEventCompletionPercentage(eventId: String, currentPoints: Int): Float {
        val totalPoints = getTotalPointsNeeded(eventId)
        return if (totalPoints > 0) {
            (currentPoints.toFloat() / totalPoints).coerceIn(0f, 1f)
        } else 0f
    }
}
