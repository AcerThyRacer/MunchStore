package com.sugarmunch.app.pass

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.shop.ShopManager
import kotlinx.coroutines.flow.*
import java.util.*

private val Context.passDataStore: DataStore<Preferences> by preferencesDataStore(name = "sugar_pass")

/**
 * Sugar Pass Manager - Complete Battle Pass System
 * 
 * Features:
 * - 100 tiers with escalating rewards
 * - Free and Premium reward tracks
 * - Season-based progression (monthly)
 * - XP system with multiple sources
 * - Exclusive season rewards
 * - FOMO mechanics with limited-time items
 * 
 * Integration:
 * - XpManager: Handles all XP earning
 * - SeasonManager: Manages season lifecycle
 * - SugarPassRewards: Reward definitions
 * - ShopManager: Currency integration
 */
class SugarPassManager private constructor(private val context: Context) {

    private val dataStore = context.passDataStore
    private val xpManager = XpManager.getInstance(context)
    private val seasonManager = SeasonManager.getInstance(context)
    private val shopManager = ShopManager.getInstance(context)

    // ═════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═════════════════════════════════════════════════════════════
    
    companion object {
        const val PREMIUM_PASS_PRICE = 999 // Sugar Points
        const val PREMIUM_PASS_REAL_MONEY = "$4.99"
        const val TIER_SKIP_PRICE = 150 // Sugar Points per tier
        const val BUNDLE_TIER_SKIP_10 = 1200 // 20% discount
        const val BUNDLE_TIER_SKIP_25 = 2500 // 33% discount
        const val MAX_TIER = 100

        @Volatile
        private var instance: SugarPassManager? = null

        fun getInstance(context: Context): SugarPassManager {
            return instance ?: synchronized(this) {
                instance ?: SugarPassManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    // ═════════════════════════════════════════════════════════════
    // PREFERENCES KEYS
    // ═════════════════════════════════════════════════════════════
    
    private object Keys {
        // User stats
        val LIFETIME_XP = intPreferencesKey("lifetime_xp")
        val LIFETIME_TIERS_COMPLETED = intPreferencesKey("lifetime_tiers_completed")
        val CURRENT_SEASON_TIER = intPreferencesKey("current_season_tier")
        val HAS_PREMIUM = booleanPreferencesKey("has_premium")
        val PREMIUM_PURCHASE_DATE = longPreferencesKey("premium_purchase_date")
        
        // Claimed rewards tracking
        val CLAIMED_FREE_REWARDS = stringPreferencesKey("claimed_free_rewards")
        val CLAIMED_PREMIUM_REWARDS = stringPreferencesKey("claimed_premium_rewards")
        
        // Boosts
        val XP_BOOST_MULTIPLIER = floatPreferencesKey("xp_boost_multiplier")
        val XP_BOOST_EXPIRES = longPreferencesKey("xp_boost_expires")
        
        // Stats
        val TOTAL_REWARDS_CLAIMED = intPreferencesKey("total_rewards_claimed")
        val EXCLUSIVE_ITEMS_OWNED = intPreferencesKey("exclusive_items_owned")
        val FASTEST_TIER_COMPLETION_HOURS = intPreferencesKey("fastest_tier_hours")
    }

    // ═════════════════════════════════════════════════════════════
    // STATE FLOWS
    // ═════════════════════════════════════════════════════════════
    
    val currentTier: Flow<Int> = xpManager.currentTier
    val totalXp: Flow<Int> = xpManager.totalXp
    
    val tierProgress: Flow<TierProgress> = xpManager.getProgressFlow()
    
    val hasPremium: Flow<Boolean> = dataStore.data.map { it[Keys.HAS_PREMIUM] ?: false }
    
    val seasonProgress: Flow<SeasonProgress> = combine(
        currentTier,
        hasPremium,
        seasonManager.currentSeason
    ) { tier, premium, season ->
        SeasonProgress(
            currentTier = tier,
            maxTier = MAX_TIER,
            hasPremium = premium,
            currentSeason = season,
            percentComplete = tier.toFloat() / MAX_TIER
        )
    }
    
    val claimableRewards: Flow<List<PassReward>> = combine(
        currentTier,
        hasPremium,
        getClaimedRewardsFlow()
    ) { tier, premium, claimed ->
        val rewards = mutableListOf<PassReward>()
        
        // Get all rewards up to current tier
        for (t in 1..tier) {
            val tierRewards = SugarPassRewards.getRewardsForTier(t)
            
            tierRewards.forEach { reward ->
                val isClaimed = isRewardClaimed(reward, claimed)
                if (!isClaimed) {
                    // Free rewards always claimable
                    if (reward.track == RewardTrack.FREE) {
                        rewards.add(reward)
                    }
                    // Premium rewards only if user has premium
                    else if (premium) {
                        rewards.add(reward)
                    }
                }
            }
        }
        rewards
    }

    val seasonProgressData: StateFlow<SeasonProgressData?> = combine(
        currentTier,
        totalXp,
        hasPremium,
        seasonManager.currentSeason,
        seasonManager.seasonTimeRemaining,
        getClaimedRewardsFlow()
    ) { tier, xp, premium, season, timeRemaining, claimed ->
        val (xpInTier, xpNeeded) = getTierProgress(xp)
        val nextTierRewards = if (tier < MAX_TIER) {
            SugarPassRewards.getRewardsForTier(tier + 1)
        } else {
            emptyList()
        }

        SeasonProgressData(
            currentTier = tier,
            totalXp = xp,
            xpInCurrentTier = xpInTier,
            xpNeededForNext = xpNeeded,
            progressPercent = if (xpNeeded > 0) xpInTier.toFloat() / xpNeeded else 1f,
            hasPremium = premium,
            currentSeason = season,
            timeRemainingMs = timeRemaining,
            isMaxTier = tier >= MAX_TIER,
            nextTierRewards = nextTierRewards,
            nextLegendaryTier = SugarPassRewards.getNextLegendaryTier(tier),
            unclaimedRewardsCount = getUnclaimedRewardsCount(tier, premium, claimed),
            claimedRewards = claimed
        )
    }.stateIn(
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO),
        SharingStarted.Eagerly,
        null
    )

    // ═════════════════════════════════════════════════════════════
    // TIER & PROGRESS
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Get current tier
     */
    suspend fun getCurrentTier(): Int {
        return currentTier.first()
    }
    
    /**
     * Get detailed progress
     */
    suspend fun getSeasonProgress(): SeasonProgressData {
        return seasonProgressData.filterNotNull().first()
    }
    
    /**
     * Get preview of upcoming rewards
     */
    fun getUpcomingRewardsPreview(startTier: Int, count: Int = 5): List<TierPreview> {
        val previews = mutableListOf<TierPreview>()
        var tier = startTier + 1
        
        while (previews.size < count && tier <= MAX_TIER) {
            val rewards = SugarPassRewards.getRewardsForTier(tier)
            if (rewards.isNotEmpty()) {
                previews.add(TierPreview(
                    tier = tier,
                    freeReward = rewards.find { it.track == RewardTrack.FREE },
                    premiumReward = rewards.find { it.track != RewardTrack.FREE },
                    isLegendary = rewards.any { it.track == RewardTrack.LEGENDARY },
                    isMilestone = tier % 10 == 0
                ))
            }
            tier++
        }
        
        return previews
    }

    // ═════════════════════════════════════════════════════════════
    // REWARD CLAIMING
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Claim a reward
     */
    suspend fun claimReward(tier: Int, track: RewardTrack): RewardClaimResult {
        val currentTierValue = getCurrentTier()
        
        // Check if user has reached this tier
        if (tier > currentTierValue) {
            return RewardClaimResult.TIER_NOT_REACHED
        }
        
        // Check if premium is required
        if (track != RewardTrack.FREE && !hasPremium.first()) {
            return RewardClaimResult.PREMIUM_REQUIRED
        }
        
        // Get reward
        val reward = SugarPassRewards.getRewardForTier(tier, track)
            ?: return RewardClaimResult.REWARD_NOT_FOUND
        
        // Check if already claimed
        if (isRewardClaimed(reward)) {
            return RewardClaimResult.ALREADY_CLAIMED
        }
        
        // Grant the reward
        val grantResult = grantReward(reward)
        if (!grantResult) {
            return RewardClaimResult.GRANT_FAILED
        }
        
        // Mark as claimed
        markRewardClaimed(reward)
        
        // Update stats
        dataStore.edit { prefs ->
            prefs[Keys.TOTAL_REWARDS_CLAIMED] = (prefs[Keys.TOTAL_REWARDS_CLAIMED] ?: 0) + 1
            if (reward.isExclusive) {
                prefs[Keys.EXCLUSIVE_ITEMS_OWNED] = (prefs[Keys.EXCLUSIVE_ITEMS_OWNED] ?: 0) + 1
            }
        }
        
        return RewardClaimResult.SUCCESS(reward)
    }
    
    /**
     * Claim all available rewards up to current tier
     */
    suspend fun claimAllRewards(): ClaimAllResult {
        val currentTierValue = getCurrentTier()
        val hasPremiumValue = hasPremium.first()
        val claimed = getClaimedRewards()
        
        var claimedCount = 0
        var sugarPointsEarned = 0
        val unlockedItems = mutableListOf<String>()
        val errors = mutableListOf<String>()
        
        for (tier in 1..currentTierValue) {
            val rewards = SugarPassRewards.getRewardsForTier(tier)
            
            rewards.forEach { reward ->
                // Skip if not eligible
                if (reward.track != RewardTrack.FREE && !hasPremiumValue) {
                    return@forEach
                }
                
                // Skip if already claimed
                if (isRewardClaimed(reward, claimed)) {
                    return@forEach
                }
                
                // Try to claim
                when (val result = claimReward(tier, reward.track)) {
                    is RewardClaimResult.SUCCESS -> {
                        claimedCount++
                        if (result.reward.type == PassRewardType.SUGAR_POINTS) {
                            sugarPointsEarned += result.reward.value
                        }
                        result.reward.itemId?.let { unlockedItems.add(it) }
                    }
                    else -> errors.add("Tier ${tier}: ${result::class.simpleName}")
                }
            }
        }
        
        return ClaimAllResult(
            claimedCount = claimedCount,
            sugarPointsEarned = sugarPointsEarned,
            unlockedItems = unlockedItems,
            errors = errors
        )
    }

    // ═════════════════════════════════════════════════════════════
    // PREMIUM PURCHASE
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Buy premium pass
     */
    suspend fun buyPremium(useSugarPoints: Boolean = true): PurchaseResult {
        if (hasPremium.first()) {
            return PurchaseResult.ALREADY_OWNED
        }
        
        return if (useSugarPoints) {
            // Try to purchase with Sugar Points
            val currentPoints = shopManager.sugarPoints.first()
            if (currentPoints < PREMIUM_PASS_PRICE) {
                PurchaseResult.INSUFFICIENT_FUNDS(
                    required = PREMIUM_PASS_PRICE,
                    current = currentPoints
                )
            } else {
                // Deduct points
                shopManager.spendSugarPoints(PREMIUM_PASS_PRICE, "sugar_pass_premium")
                activatePremium()
                PurchaseResult.SUCCESS
            }
        } else {
            // Real money purchase - would integrate with billing
            activatePremium()
            PurchaseResult.SUCCESS
        }
    }
    
    /**
     * Buy tier skips
     */
    suspend fun buyTierSkips(count: Int): PurchaseResult {
        val price = when {
            count >= 25 -> BUNDLE_TIER_SKIP_25
            count >= 10 -> BUNDLE_TIER_SKIP_10
            else -> count * TIER_SKIP_PRICE
        }
        
        val currentPoints = shopManager.sugarPoints.first()
        if (currentPoints < price) {
            return PurchaseResult.INSUFFICIENT_FUNDS(price, currentPoints)
        }
        
        // Deduct points
        shopManager.spendSugarPoints(price, "tier_skip_${count}")
        
        // Add XP to skip tiers
        val currentTierValue = getCurrentTier()
        val targetTier = (currentTierValue + count).coerceAtMost(MAX_TIER)
        val xpNeeded = getCumulativeXpForTier(targetTier)
        
        // Award XP to reach target
        repeat(count) {
            xpManager.addXp(
                getXpForTier(currentTierValue + it + 1),
                XpSource.OTHER,
                "tier_skip"
            )
        }
        
        return PurchaseResult.TIER_SKIP_SUCCESS(count, targetTier)
    }
    
    private suspend fun activatePremium() {
        dataStore.edit { prefs ->
            prefs[Keys.HAS_PREMIUM] = true
            prefs[Keys.PREMIUM_PURCHASE_DATE] = System.currentTimeMillis()
        }
        
        // Also update season manager
        seasonManager.purchasePremium()
    }

    // ═════════════════════════════════════════════════════════════
    // REWARD UTILITIES
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Get rewards for a specific tier
     */
    fun getRewardsForTier(tier: Int): List<PassReward> {
        return SugarPassRewards.getRewardsForTier(tier)
    }
    
    /**
     * Get all rewards
     */
    fun getAllRewards(): List<PassReward> {
        return SugarPassRewards.getAllRewards()
    }
    
    /**
     * Get exclusive rewards (creates FOMO)
     */
    fun getExclusiveRewards(): List<PassReward> {
        return SugarPassRewards.getExclusiveRewards()
    }
    
    /**
     * Get value comparison between free and premium
     */
    fun getTrackValues(): TrackValues {
        return TrackValues(
            freeSugarPoints = SugarPassRewards.getFreeTrackTotalValue(),
            premiumSugarPoints = SugarPassRewards.getPremiumTrackTotalValue(),
            freeItems = SugarPassRewards.getFreeRewards().count { it.type != PassRewardType.SUGAR_POINTS && it.type != PassRewardType.XP_BOOST },
            premiumItems = SugarPassRewards.getPremiumRewards().count { it.type != PassRewardType.SUGAR_POINTS && it.type != PassRewardType.XP_BOOST },
            exclusiveCount = SugarPassRewards.getExclusiveRewards().size
        )
    }

    // ═════════════════════════════════════════════════════════════
    // STATS & ANALYTICS
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Get Sugar Pass stats
     */
    suspend fun getStats(): SugarPassStats {
        val prefs = dataStore.data.first()
        return SugarPassStats(
            lifetimeXp = prefs[Keys.LIFETIME_XP] ?: 0,
            lifetimeTiersCompleted = prefs[Keys.LIFETIME_TIERS_COMPLETED] ?: 0,
            totalRewardsClaimed = prefs[Keys.TOTAL_REWARDS_CLAIMED] ?: 0,
            exclusiveItemsOwned = prefs[Keys.EXCLUSIVE_ITEMS_OWNED] ?: 0,
            hasPremium = prefs[Keys.HAS_PREMIUM] ?: false,
            premiumPurchaseDate = prefs[Keys.PREMIUM_PURCHASE_DATE]
        )
    }
    
    /**
     * Get estimated completion time
     */
    suspend fun getEstimatedCompletion(): EstimatedCompletion {
        val currentTierValue = getCurrentTier()
        val daysLeft = seasonManager.getTimeRemaining() / (1000 * 60 * 60 * 24)
        
        if (currentTierValue >= MAX_TIER) {
            return EstimatedCompletion.COMPLETE
        }
        
        val tiersRemaining = MAX_TIER - currentTierValue
        val estimatedDays = xpManager.getEstimatedDaysToTier(MAX_TIER)
        
        return if (estimatedDays != null && estimatedDays <= daysLeft) {
            EstimatedCompletion.ON_TRACK(estimatedDays)
        } else {
            EstimatedCompletion.BEHIND(tiersRemaining, estimatedDays ?: -1)
        }
    }

    // ═════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═════════════════════════════════════════════════════════════
    
    private suspend fun grantReward(reward: PassReward): Boolean {
        return when (reward.type) {
            PassRewardType.SUGAR_POINTS -> {
                shopManager.addSugarPoints(reward.value, "sugar_pass_tier_${reward.tier}")
                true
            }
            PassRewardType.XP_BOOST -> {
                // Apply XP boost
                dataStore.edit { prefs ->
                    prefs[Keys.XP_BOOST_MULTIPLIER] = 1f + (reward.value / 100f)
                    prefs[Keys.XP_BOOST_EXPIRES] = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
                }
                true
            }
            PassRewardType.THEME,
            PassRewardType.EFFECT,
            PassRewardType.BADGE,
            PassRewardType.ICON,
            PassRewardType.PARTICLE_STYLE,
            PassRewardType.TITLE,
            PassRewardType.EMOJI_PACK,
            PassRewardType.FEATURE_UNLOCK,
            PassRewardType.CURRENCY_BOOST -> {
                // Add to inventory via shop manager
                reward.itemId?.let { itemId ->
                    shopManager.unlockPassItem(itemId, reward.type)
                }
                true
            }
        }
    }
    
    private suspend fun isRewardClaimed(reward: PassReward): Boolean {
        return isRewardClaimed(reward, getClaimedRewards())
    }
    
    private fun isRewardClaimed(reward: PassReward, claimed: Set<String>): Boolean {
        val key = "${reward.tier}_${reward.track.name}"
        return key in claimed
    }
    
    private suspend fun markRewardClaimed(reward: PassReward) {
        dataStore.edit { prefs ->
            val key = if (reward.track == RewardTrack.FREE) {
                Keys.CLAIMED_FREE_REWARDS
            } else {
                Keys.CLAIMED_PREMIUM_REWARDS
            }
            val current = (prefs[key] ?: "").split(",").filter { it.isNotEmpty() }.toMutableSet()
            current.add("${reward.tier}_${reward.track.name}")
            prefs[key] = current.joinToString(",")
        }
    }
    
    private suspend fun getClaimedRewards(): Set<String> {
        val prefs = dataStore.data.first()
        val free = (prefs[Keys.CLAIMED_FREE_REWARDS] ?: "").split(",")
        val premium = (prefs[Keys.CLAIMED_PREMIUM_REWARDS] ?: "").split(",")
        return (free + premium).filter { it.isNotEmpty() }.toSet()
    }
    
    private fun getClaimedRewardsFlow(): Flow<Set<String>> = dataStore.data.map { prefs ->
        val free = (prefs[Keys.CLAIMED_FREE_REWARDS] ?: "").split(",")
        val premium = (prefs[Keys.CLAIMED_PREMIUM_REWARDS] ?: "").split(",")
        (free + premium).filter { it.isNotEmpty() }.toSet()
    }
    
    private fun getUnclaimedRewardsCount(
        currentTierValue: Int,
        premium: Boolean,
        claimed: Set<String>
    ): Int {
        var count = 0
        for (tier in 1..currentTierValue) {
            val rewards = SugarPassRewards.getRewardsForTier(tier)
            rewards.forEach { reward ->
                if (reward.track == RewardTrack.FREE || premium) {
                    if (!isRewardClaimed(reward, claimed)) {
                        count++
                    }
                }
            }
        }
        return count
    }
}

// ═════════════════════════════════════════════════════════════
// RESULT CLASSES
// ═════════════════════════════════════════════════════════════

sealed class RewardClaimResult {
    data class SUCCESS(val reward: PassReward) : RewardClaimResult()
    object TIER_NOT_REACHED : RewardClaimResult()
    object PREMIUM_REQUIRED : RewardClaimResult()
    object ALREADY_CLAIMED : RewardClaimResult()
    object REWARD_NOT_FOUND : RewardClaimResult()
    object GRANT_FAILED : RewardClaimResult()
}

data class ClaimAllResult(
    val claimedCount: Int,
    val sugarPointsEarned: Int,
    val unlockedItems: List<String>,
    val errors: List<String>
) {
    val success: Boolean get() = claimedCount > 0 && errors.isEmpty()
}

sealed class PurchaseResult {
    object SUCCESS : PurchaseResult()
    object ALREADY_OWNED : PurchaseResult()
    data class INSUFFICIENT_FUNDS(val required: Int, val current: Int) : PurchaseResult()
    data class TIER_SKIP_SUCCESS(val tiersSkipped: Int, val newTier: Int) : PurchaseResult()
}

// ═════════════════════════════════════════════════════════════
// DATA CLASSES
// ═════════════════════════════════════════════════════════════

data class SeasonProgress(
    val currentTier: Int,
    val maxTier: Int,
    val hasPremium: Boolean,
    val currentSeason: SeasonInfo?,
    val percentComplete: Float
)

data class SeasonProgressData(
    val currentTier: Int,
    val totalXp: Int,
    val xpInCurrentTier: Int,
    val xpNeededForNext: Int,
    val progressPercent: Float,
    val hasPremium: Boolean,
    val currentSeason: SeasonInfo?,
    val timeRemainingMs: Long,
    val isMaxTier: Boolean,
    val nextTierRewards: List<PassReward>,
    val nextLegendaryTier: Int?,
    val unclaimedRewardsCount: Int,
    val claimedRewards: Set<String>
) {
    val canClaimRewards: Boolean get() = unclaimedRewardsCount > 0
    val xpToNextTier: Int get() = (xpNeededForNext - xpInCurrentTier).coerceAtLeast(0)
    
    fun formatTimeRemaining(): String {
        val days = timeRemainingMs / (1000 * 60 * 60 * 24)
        val hours = (timeRemainingMs / (1000 * 60 * 60)) % 24
        return when {
            days > 0 -> "$days" + "d ${hours}h remaining"
            hours > 0 -> "$hours" + "h remaining"
            else -> "< 1h remaining"
        }
    }
}

data class TierPreview(
    val tier: Int,
    val freeReward: PassReward?,
    val premiumReward: PassReward?,
    val isLegendary: Boolean,
    val isMilestone: Boolean
)

data class TrackValues(
    val freeSugarPoints: Int,
    val premiumSugarPoints: Int,
    val freeItems: Int,
    val premiumItems: Int,
    val exclusiveCount: Int
) {
    val totalValue: Int get() = freeSugarPoints + premiumSugarPoints
    val premiumBonus: Int get() = premiumSugarPoints - freeSugarPoints
    val premiumMultiplier: Float get() = if (freeSugarPoints > 0) premiumSugarPoints.toFloat() / freeSugarPoints else 1f
}

data class SugarPassStats(
    val lifetimeXp: Int,
    val lifetimeTiersCompleted: Int,
    val totalRewardsClaimed: Int,
    val exclusiveItemsOwned: Int,
    val hasPremium: Boolean,
    val premiumPurchaseDate: Long?
)

sealed class EstimatedCompletion {
    object COMPLETE : EstimatedCompletion()
    data class ON_TRACK(val estimatedDays: Int) : EstimatedCompletion()
    data class BEHIND(val tiersRemaining: Int, val estimatedDays: Int) : EstimatedCompletion()
}
