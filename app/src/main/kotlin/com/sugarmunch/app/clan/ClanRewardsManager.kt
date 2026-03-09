package com.sugarmunch.app.clan

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.clan.model.*
import com.sugarmunch.app.data.local.AppDatabase
import com.sugarmunch.app.features.AchievementManager
import com.sugarmunch.app.shop.ShopManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

private val Context.rewardsDataStore: DataStore<Preferences> by preferencesDataStore(name = "sugarmunch_clan_rewards")

/**
 * ClanRewardsManager - Clan-specific rewards and shop
 * 
 * Features:
 * - Clan-specific rewards
 * - Clan shop with exclusive items
 * - War victory rewards
 * - Clan achievement rewards
 */
class ClanRewardsManager private constructor(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private val dataStore = context.rewardsDataStore
    private val database = AppDatabase.getDatabase(context)
    private val clanDao = database.clanDao()
    private val achievementManager = AchievementManager.getInstance(context)
    private val shopManager = ShopManager.getInstance(context)
    private val clanManager = ClanManager.getInstance(context)
    
    // Current user ID
    private val currentUserId: String
        get() = "current_user" // Would come from auth
    
    // State flows
    private val _availableRewards = MutableStateFlow<List<ClanReward>>(emptyList())
    val availableRewards: StateFlow<List<ClanReward>> = _availableRewards.asStateFlow()
    
    private val _unclaimedRewards = MutableStateFlow<List<ClanReward>>(emptyList())
    val unclaimedRewards: StateFlow<List<ClanReward>> = _unclaimedRewards.asStateFlow()
    
    private val _clanCurrency = MutableStateFlow(0)
    val clanCurrency: StateFlow<Int> = _clanCurrency.asStateFlow()
    
    private val _shopItems = MutableStateFlow<List<ClanShopItem>>(emptyList())
    val shopItems: StateFlow<List<ClanShopItem>> = _shopItems.asStateFlow()
    
    private val _purchasedItems = MutableStateFlow<List<String>>(emptyList())
    val purchasedItems: StateFlow<List<String>> = _purchasedItems.asStateFlow()
    
    private val _clanAchievements = MutableStateFlow<List<ClanAchievement>>(emptyList())
    val clanAchievements: StateFlow<List<ClanAchievement>> = _clanAchievements.asStateFlow()
    
    private val _clanAchievementProgress = MutableStateFlow<Map<String, ClanAchievementProgress>>(emptyMap())
    val clanAchievementProgress: StateFlow<Map<String, ClanAchievementProgress>> = _clanAchievementProgress.asStateFlow()
    
    // Purchase result
    data class PurchaseResult(
        val success: Boolean,
        val item: ClanShopItem? = null,
        val error: String? = null
    )
    
    // Reward claim result
    data class ClaimResult(
        val success: Boolean,
        val reward: ClanReward? = null,
        val sugarPointsGained: Int = 0,
        val xpGained: Int = 0,
        val error: String? = null
    )
    
    companion object {
        @Volatile
        private var instance: ClanRewardsManager? = null
        
        fun getInstance(context: Context): ClanRewardsManager {
            return instance ?: synchronized(this) {
                instance ?: ClanRewardsManager(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
        
        fun destroy() {
            instance = null
        }
    }
    
    init {
        scope.launch {
            initializeClanShop()
            initializeClanAchievements()
            loadUserData()
            startPeriodicRefresh()
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═════════════════════════════════════════════════════════════════
    
    private suspend fun initializeClanShop() {
        // Check if shop items already exist
        val existingItems = clanDao.getActiveShopItems()
        if (existingItems.isNotEmpty()) {
            _shopItems.value = existingItems
            return
        }
        
        // Create default shop items
        val defaultItems = listOf(
            // Exclusive Themes
            ClanShopItem(
                name = "Clan Pride Theme",
                description = "Show your clan colors with this exclusive theme",
                type = ClanShopItemType.EXCLUSIVE_THEME,
                icon = "🎨",
                costClanCurrency = 500,
                minClanLevel = 1,
                themeId = "clan_pride_theme"
            ),
            ClanShopItem(
                name = "War Hero Theme",
                description = "Earned by true warriors",
                type = ClanShopItemType.EXCLUSIVE_THEME,
                icon = "⚔️",
                costClanCurrency = 1000,
                minClanLevel = 3,
                themeId = "war_hero_theme"
            ),
            ClanShopItem(
                name = "Legendary Clan Theme",
                description = "Only for the most prestigious clans",
                type = ClanShopItemType.EXCLUSIVE_THEME,
                icon = "👑",
                costClanCurrency = 2500,
                minClanLevel = 5,
                themeId = "legendary_clan_theme"
            ),
            
            // Exclusive Effects
            ClanShopItem(
                name = "Clan Banner Effect",
                description = "Display your clan banner proudly",
                type = ClanShopItemType.EXCLUSIVE_EFFECT,
                icon = "🚩",
                costClanCurrency = 300,
                minClanLevel = 1,
                effectId = "clan_banner_effect"
            ),
            ClanShopItem(
                name = "Victory Fireworks",
                description = "Celebrate your wins with style",
                type = ClanShopItemType.EXCLUSIVE_EFFECT,
                icon = "🎆",
                costClanCurrency = 750,
                minClanLevel = 2,
                effectId = "victory_fireworks_effect"
            ),
            
            // Clan Badges
            ClanShopItem(
                name = "Veteran Badge",
                description = "Show you're a clan veteran",
                type = ClanShopItemType.CLAN_BADGE,
                icon = "🎖️",
                costClanCurrency = 200,
                minClanLevel = 1,
                badgeId = "veteran_badge"
            ),
            ClanShopItem(
                name = "War Champion Badge",
                description = "For those who led their clan to victory",
                type = ClanShopItemType.CLAN_BADGE,
                icon = "🏆",
                costClanCurrency = 1500,
                minClanLevel = 4,
                badgeId = "war_champion_badge"
            ),
            
            // Boosts
            ClanShopItem(
                name = "XP Boost (24h)",
                description = "Double XP for 24 hours",
                type = ClanShopItemType.BOOST,
                icon = "⚡",
                costClanCurrency = 400,
                minClanLevel = 1
            ),
            ClanShopItem(
                name = "Sugar Rush Boost (24h)",
                description = "Double sugar points for 24 hours",
                type = ClanShopItemType.BOOST,
                icon = "🍬",
                costClanCurrency = 600,
                minClanLevel = 2
            ),
            
            // Emblems
            ClanShopItem(
                name = "Golden Dragon Emblem",
                description = "Majestic dragon emblem for your clan",
                type = ClanShopItemType.EMBLEM,
                icon = "🐉",
                costClanCurrency = 2000,
                minClanLevel = 5
            ),
            ClanShopItem(
                name = "Phoenix Emblem",
                description = "Rise from the ashes",
                type = ClanShopItemType.EMBLEM,
                icon = "🔥",
                costClanCurrency = 2000,
                minClanLevel = 5
            ),
            
            // Borders
            ClanShopItem(
                name = "Gold Border",
                description = "Premium gold profile border",
                type = ClanShopItemType.BORDER,
                icon = "✨",
                costClanCurrency = 800,
                minClanLevel = 3
            )
        )
        
        defaultItems.forEach { clanDao.insertShopItem(it) }
        _shopItems.value = defaultItems
    }
    
    private suspend fun initializeClanAchievements() {
        val existingAchievements = clanDao.getAllAchievements()
        if (existingAchievements.isNotEmpty()) {
            _clanAchievements.value = existingAchievements
            return
        }
        
        val defaultAchievements = listOf(
            ClanAchievement(
                id = "first_war_win",
                name = "First Victory",
                description = "Win your first clan war",
                icon = "🏆",
                requirementType = "war_wins",
                requirementValue = 1,
                rewardSugarPoints = 100,
                rewardClanXP = 50,
                order = 1
            ),
            ClanAchievement(
                id = "war_streak_3",
                name = "Winning Streak",
                description = "Win 3 clan wars in a row",
                icon = "🔥",
                requirementType = "war_streak",
                requirementValue = 3,
                rewardSugarPoints = 300,
                rewardClanXP = 150,
                order = 2
            ),
            ClanAchievement(
                id = "active_members_10",
                name = "Growing Clan",
                description = "Have 10 members contribute in a single war",
                icon = "👥",
                requirementType = "active_members",
                requirementValue = 10,
                rewardSugarPoints = 200,
                rewardClanXP = 100,
                order = 3
            ),
            ClanAchievement(
                id = "clan_level_5",
                name = "Rising Power",
                description = "Reach clan level 5",
                icon = "⭐",
                requirementType = "clan_level",
                requirementValue = 5,
                rewardSugarPoints = 500,
                rewardClanXP = 250,
                order = 4
            ),
            ClanAchievement(
                id = "clan_level_10",
                name = "Legendary Clan",
                description = "Reach clan level 10",
                icon = "👑",
                requirementType = "clan_level",
                requirementValue = 10,
                rewardSugarPoints = 1000,
                rewardClanXP = 500,
                order = 5
            ),
            ClanAchievement(
                id = "total_installs_100",
                name = "Installation Nation",
                description = "Clan members install 100 apps total",
                icon = "📱",
                requirementType = "total_installs",
                requirementValue = 100,
                rewardSugarPoints = 250,
                rewardClanXP = 125,
                order = 6
            ),
            ClanAchievement(
                id = "total_achievements_50",
                name = "Achievement Hunters",
                description = "Clan members unlock 50 achievements",
                icon = "🎯",
                requirementType = "total_achievements",
                requirementValue = 50,
                rewardSugarPoints = 300,
                rewardClanXP = 150,
                order = 7
            ),
            ClanAchievement(
                id = "top_10_leaderboard",
                name = "Top Tier",
                description = "Reach top 10 on the clan leaderboard",
                icon = "📊",
                requirementType = "leaderboard_rank",
                requirementValue = 10,
                rewardSugarPoints = 400,
                rewardClanXP = 200,
                order = 8
            ),
            ClanAchievement(
                id = "top_3_leaderboard",
                name = "Elite Three",
                description = "Reach top 3 on the clan leaderboard",
                icon = "🥉",
                requirementType = "leaderboard_rank",
                requirementValue = 3,
                rewardSugarPoints = 800,
                rewardClanXP = 400,
                order = 9
            ),
            ClanAchievement(
                id = "clan_founder_keep",
                name = "Founder's Legacy",
                description = "Maintain a clan for 30 days",
                icon = "🛡️",
                requirementType = "clan_age_days",
                requirementValue = 30,
                rewardSugarPoints = 500,
                rewardClanXP = 250,
                order = 10
            )
        )
        
        defaultAchievements.forEach { clanDao.insertAchievement(it) }
        _clanAchievements.value = defaultAchievements
    }
    
    private suspend fun loadUserData() {
        // Load clan currency
        val userData = clanDao.getUserClanData(currentUserId)
        _clanCurrency.value = userData?.clanCurrency ?: 0
        
        // Load unclaimed rewards
        val clanId = clanManager.currentClan.value?.id
        clanId?.let {
            _unclaimedRewards.value = clanDao.getUnclaimedRewards(it)
        }
        
        // Load purchased items
        dataStore.data.map { prefs ->
            prefs[PURCHASED_ITEMS_KEY]?.toList() ?: emptyList()
        }.collect { items ->
            _purchasedItems.value = items
        }
    }
    
    private fun startPeriodicRefresh() {
        scope.launch {
            while (isActive) {
                delay(60000) // Refresh every minute
                refreshData()
            }
        }
    }
    
    private suspend fun refreshData() {
        val clanId = clanManager.currentClan.value?.id ?: return
        
        _unclaimedRewards.value = clanDao.getUnclaimedRewards(clanId)
        
        val userData = clanDao.getUserClanData(currentUserId)
        _clanCurrency.value = userData?.clanCurrency ?: 0
        
        // Load clan achievement progress
        val progress = clanDao.getClanAchievementProgress(clanId)
        _clanAchievementProgress.value = progress.associateBy { it.achievementId }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // REWARDS
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun getClanRewards(clanId: String): List<ClanReward> {
        return clanDao.getClanRewards(clanId)
    }
    
    suspend fun claimClanReward(rewardId: String): ClaimResult {
        val reward = clanDao.getUnclaimedRewards(clanManager.currentClan.value?.id ?: "")
            .find { it.id == rewardId } ?: return ClaimResult(
                success = false,
                error = "Reward not found or already claimed"
            )
        
        return try {
            // Claim the reward
            clanDao.claimReward(rewardId, currentUserId, System.currentTimeMillis())
            
            // Add sugar points to user
            shopManager.addSugarPoints(reward.sugarPoints)
            
            // Add clan XP if applicable
            if (reward.xpReward > 0) {
                val clan = clanManager.currentClan.value
                clan?.let {
                    clanDao.updateClan(it.copy(xp = it.xp + reward.xpReward))
                }
            }
            
            // Track achievement
            achievementManager.unlockAchievement("first_clan_reward")
            
            // Refresh data
            refreshData()
            
            ClaimResult(
                success = true,
                reward = reward,
                sugarPointsGained = reward.sugarPoints,
                xpGained = reward.xpReward
            )
        } catch (e: Exception) {
            ClaimResult(success = false, error = e.message)
        }
    }
    
    suspend fun claimAllRewards(): List<ClaimResult> {
        val rewards = _unclaimedRewards.value
        return rewards.map { claimClanReward(it.id) }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // CLAN SHOP
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun buyFromClanShop(itemId: String): PurchaseResult {
        val item = clanDao.getShopItem(itemId) ?: return PurchaseResult(
            success = false,
            error = "Item not found"
        )
        
        val clan = clanManager.currentClan.value ?: return PurchaseResult(
            success = false,
            error = "You must be in a clan to purchase"
        )
        
        // Check level requirement
        if (clan.level < item.minClanLevel) {
            return PurchaseResult(
                success = false,
                error = "Clan must be level ${item.minClanLevel} to purchase this item"
            )
        }
        
        // Check if already owned
        if (_purchasedItems.value.contains(itemId)) {
            return PurchaseResult(
                success = false,
                error = "You already own this item"
            )
        }
        
        // Check currency
        if (_clanCurrency.value < item.costClanCurrency) {
            return PurchaseResult(
                success = false,
                error = "Not enough clan currency"
            )
        }
        
        // Check stock
        item.stock?.let { stock ->
            if (item.purchasedCount >= stock) {
                return PurchaseResult(
                    success = false,
                    error = "Item is out of stock"
                )
            }
        }
        
        return try {
            // Deduct currency
            clanDao.spendClanCurrency(currentUserId, item.costClanCurrency)
            
            // Update purchase count
            clanDao.incrementPurchaseCount(itemId)
            
            // Add to purchased items
            val updatedPurchased = _purchasedItems.value + itemId
            dataStore.edit { prefs ->
                prefs[PURCHASED_ITEMS_KEY] = updatedPurchased.toSet()
            }
            _purchasedItems.value = updatedPurchased
            
            // Grant item based on type
            grantShopItem(item)
            
            // Track achievement
            achievementManager.unlockAchievement("clan_shopper")
            
            // Refresh currency
            refreshData()
            
            PurchaseResult(success = true, item = item)
        } catch (e: Exception) {
            PurchaseResult(success = false, error = e.message)
        }
    }
    
    private suspend fun grantShopItem(item: ClanShopItem) {
        when (item.type) {
            ClanShopItemType.EXCLUSIVE_THEME -> {
                item.themeId?.let { themeId ->
                    // Unlock theme in theme manager
                    // themeManager.unlockTheme(themeId)
                }
            }
            ClanShopItemType.EXCLUSIVE_EFFECT -> {
                item.effectId?.let { effectId ->
                    // Unlock effect in effect manager
                    // effectManager.unlockEffect(effectId)
                }
            }
            ClanShopItemType.CLAN_BADGE -> {
                item.badgeId?.let { badgeId ->
                    // Equip badge
                    // profileManager.equipBadge(badgeId)
                }
            }
            ClanShopItemType.BOOST -> {
                // Activate boost
                activateBoost(item)
            }
            ClanShopItemType.EMBLEM -> {
                // Update clan emblem (if officer/leader)
                if (clanManager.canManageSettings()) {
                    clanManager.updateClanSettings(emblem = item.icon)
                }
            }
            ClanShopItemType.BORDER -> {
                // Equip border
                // profileManager.equipBorder(item.icon)
            }
        }
    }
    
    private fun activateBoost(item: ClanShopItem) {
        // Would integrate with boost system
        // boostManager.activateBoost(type, duration)
    }
    
    fun getAvailableShopItems(clanLevel: Int): List<ClanShopItem> {
        return _shopItems.value.filter { it.minClanLevel <= clanLevel }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // CLAN ACHIEVEMENTS
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun trackClanAchievementProgress(achievementId: String, progress: Int) {
        val clanId = clanManager.currentClan.value?.id ?: return
        val achievement = _clanAchievements.value.find { it.id == achievementId } ?: return
        
        val existingProgress = clanDao.getAchievementProgress(clanId, achievementId)
        
        if (existingProgress?.isUnlocked == true) return
        
        val newProgress = (existingProgress?.progress ?: 0) + progress
        val isUnlocked = newProgress >= achievement.requirementValue
        
        val progressRecord = ClanAchievementProgress(
            clanId = clanId,
            achievementId = achievementId,
            progress = newProgress.coerceAtMost(achievement.requirementValue),
            maxProgress = achievement.requirementValue,
            isUnlocked = isUnlocked,
            unlockedAt = if (isUnlocked) System.currentTimeMillis() else null
        )
        
        clanDao.insertAchievementProgress(progressRecord)
        
        // Update state
        _clanAchievementProgress.value = _clanAchievementProgress.value + (achievementId to progressRecord)
        
        // If unlocked, grant rewards
        if (isUnlocked) {
            grantClanAchievementRewards(achievement)
        }
    }
    
    private suspend fun grantClanAchievementRewards(achievement: ClanAchievement) {
        val clanId = clanManager.currentClan.value?.id ?: return
        
        // Create reward
        val reward = ClanReward(
            clanId = clanId,
            type = ClanRewardType.CLAN_ACHIEVEMENT,
            name = "Achievement: ${achievement.name}",
            description = achievement.description,
            icon = achievement.icon,
            sugarPoints = achievement.rewardSugarPoints,
            xpReward = achievement.rewardClanXP
        )
        
        clanDao.insertReward(reward)
        
        // Add XP to clan
        val clan = clanManager.currentClan.value
        clan?.let {
            clanDao.updateClan(it.copy(xp = it.xp + achievement.rewardClanXP))
        }
        
        // Send system message
        val systemMessage = ClanMessage(
            clanId = clanId,
            type = ClanMessageType.SYSTEM_JOIN,
            content = "🎉 Clan unlocked achievement: ${achievement.name}! +${achievement.rewardClanXP} XP"
        )
        clanDao.insertMessage(systemMessage)
        
        refreshData()
    }
    
    fun getAchievementProgress(achievementId: String): Float {
        val progress = _clanAchievementProgress.value[achievementId]
        val achievement = _clanAchievements.value.find { it.id == achievementId }
        
        return if (progress != null && achievement != null) {
            progress.progress.toFloat() / achievement.requirementValue
        } else 0f
    }
    
    // ═════════════════════════════════════════════════════════════════
    // CLAN CURRENCY
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun addClanCurrency(userId: String, amount: Int) {
        clanDao.addClanCurrency(userId, amount)
        if (userId == currentUserId) {
            _clanCurrency.value += amount
        }
    }
    
    suspend fun spendClanCurrency(userId: String, amount: Int): Boolean {
        val userData = clanDao.getUserClanData(userId) ?: return false
        if (userData.clanCurrency < amount) return false
        
        clanDao.spendClanCurrency(userId, amount)
        if (userId == currentUserId) {
            _clanCurrency.value -= amount
        }
        return true
    }
    
    // ═════════════════════════════════════════════════════════════════
    // UTILITY
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun refresh() {
        refreshData()
    }
    
    private val PURCHASED_ITEMS_KEY = stringSetPreferencesKey("purchased_clan_items")
}
