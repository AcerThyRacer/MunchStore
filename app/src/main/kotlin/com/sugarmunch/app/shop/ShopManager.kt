package com.sugarmunch.app.shop

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.features.AchievementManager
import com.sugarmunch.app.features.model.Rarity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import java.util.*

private val Context.shopDataStore: DataStore<Preferences> by preferencesDataStore(name = "sugar_shop")

/**
 * Sugar Shop Manager - Handles purchases, inventory, and currency
 */
class ShopManager private constructor(private val context: Context) {

    private val dataStore = context.shopDataStore
    private val achievementManager = AchievementManager.getInstance(context)

    // Preferences keys
    private object Keys {
        val SUGAR_POINTS = intPreferencesKey("sugar_points")
        val USER_LEVEL = intPreferencesKey("user_level")
        val USER_XP = intPreferencesKey("user_xp")
        val INVENTORY_ITEMS = stringPreferencesKey("inventory_items")
        val EQUIPPED_THEME = stringPreferencesKey("equipped_theme")
        val EQUIPPED_EFFECT = stringPreferencesKey("equipped_effect")
        val EQUIPPED_BADGE = stringPreferencesKey("equipped_badge")
        val EQUIPPED_ICON = stringPreferencesKey("equipped_icon")
        val ACTIVE_BOOSTS = stringPreferencesKey("active_boosts")
        val TOTAL_SPENT = intPreferencesKey("total_spent")
        val DAILY_DEAL_SEEN = longPreferencesKey("daily_deal_seen")
        val LAST_LOGIN = longPreferencesKey("last_shop_login")
    }

    // ═════════════════════════════════════════════════════════════
    // CURRENCY & LEVEL
    // ═════════════════════════════════════════════════════════════

    val sugarPoints: Flow<Int> = dataStore.data.map { it[Keys.SUGAR_POINTS] ?: 0 }
    val userLevel: Flow<Int> = dataStore.data.map { it[Keys.USER_LEVEL] ?: 1 }
    val userXP: Flow<Int> = dataStore.data.map { it[Keys.USER_XP] ?: 0 }
    val totalSpent: Flow<Int> = dataStore.data.map { it[Keys.TOTAL_SPENT] ?: 0 }

    suspend fun addSugarPoints(amount: Int, source: String = "") {
        dataStore.edit { prefs ->
            val current = prefs[Keys.SUGAR_POINTS] ?: 0
            val boostMultiplier = getActiveBoostMultiplier(BoostType.SUGAR_POINTS)
            val finalAmount = (amount * boostMultiplier).toInt()
            prefs[Keys.SUGAR_POINTS] = current + finalAmount
        }
    }

    suspend fun spendSugarPoints(amount: Int): Boolean {
        return spendSugarPoints(amount, "")
    }
    
    suspend fun spendSugarPoints(amount: Int, reason: String): Boolean {
        var success = false
        dataStore.edit { prefs ->
            val current = prefs[Keys.SUGAR_POINTS] ?: 0
            val discount = getActiveDiscount()
            val finalAmount = (amount * (1 - discount)).toInt()
            
            if (current >= finalAmount) {
                prefs[Keys.SUGAR_POINTS] = current - finalAmount
                prefs[Keys.TOTAL_SPENT] = (prefs[Keys.TOTAL_SPENT] ?: 0) + finalAmount
                success = true
            }
        }
        return success
    }

    suspend fun addXP(amount: Int) {
        dataStore.edit { prefs ->
            val boostMultiplier = getActiveBoostMultiplier(BoostType.XP_GAIN)
            val finalAmount = (amount * boostMultiplier).toInt()
            val currentXP = (prefs[Keys.USER_XP] ?: 0) + finalAmount
            val currentLevel = prefs[Keys.USER_LEVEL] ?: 1
            
            // Calculate level up (every 1000 XP)
            val newLevel = (currentXP / 1000) + 1
            if (newLevel > currentLevel) {
                prefs[Keys.USER_LEVEL] = newLevel
                // Award level up bonus
                val bonusPoints = (newLevel - currentLevel) * 100
                prefs[Keys.SUGAR_POINTS] = (prefs[Keys.SUGAR_POINTS] ?: 0) + bonusPoints
            }
            prefs[Keys.USER_XP] = currentXP
        }
    }

    fun getXPForNextLevel(level: Int): Int {
        return level * 1000
    }

    // ═════════════════════════════════════════════════════════════
    // INVENTORY MANAGEMENT
    // ═════════════════════════════════════════════════════════════

    val inventory: Flow<List<InventoryItem>> = dataStore.data.map { prefs ->
        parseInventory(prefs[Keys.INVENTORY_ITEMS] ?: "")
    }

    val equippedTheme: Flow<String?> = dataStore.data.map { it[Keys.EQUIPPED_THEME] }
    val equippedEffect: Flow<String?> = dataStore.data.map { it[Keys.EQUIPPED_EFFECT] }
    val equippedBadge: Flow<String?> = dataStore.data.map { it[Keys.EQUIPPED_BADGE] }
    val equippedIcon: Flow<String?> = dataStore.data.map { it[Keys.EQUIPPED_ICON] }

    suspend fun purchaseItem(item: ShopItem): PurchaseResult {
        // Check requirements
        if (!meetsRequirements(item.requirements)) {
            return PurchaseResult.FAILURE_REQUIREMENTS
        }

        // Check if already owned (non-consumable)
        if (isItemOwned(item.id) && item.type != ShopItemType.BOOST) {
            return PurchaseResult.FAILURE_ALREADY_OWNED
        }

        // Check limited quantity
        if (item.limitedQuantity != null) {
            val remaining = getRemainingQuantity(item.id, item.limitedQuantity)
            if (remaining <= 0) {
                return PurchaseResult.FAILURE_OUT_OF_STOCK
            }
        }

        // Calculate price with discount
        val discount = if (item.discount > 0) item.discount / 100f else getActiveDiscount()
        val finalPrice = (item.cost * (1 - discount)).toInt()

        // Check funds
        val currentPoints = sugarPoints.first()
        if (currentPoints < finalPrice) {
            return PurchaseResult.FAILURE_INSUFFICIENT_FUNDS
        }

        // Process purchase
        val success = spendSugarPoints(item.cost)
        if (!success) return PurchaseResult.FAILURE_INSUFFICIENT_FUNDS

        // Add to inventory
        addToInventory(item)

        // Award XP for purchase
        val xpGain = when (item.rarity) {
            Rarity.COMMON -> 10
            Rarity.UNCOMMON -> 25
            Rarity.RARE -> 50
            Rarity.EPIC -> 100
            Rarity.LEGENDARY -> 250
        }
        addXP(xpGain)

        // Handle bundles
        if (item.type == ShopItemType.BUNDLE && item.bundleItems != null) {
            item.bundleItems.forEach { bundleItemId ->
                ShopCatalog.getItemById(bundleItemId)?.let { addToInventory(it) }
            }
        }

        return PurchaseResult.SUCCESS
    }

    suspend fun equipItem(itemId: String, type: ShopItemType) {
        when (type) {
            ShopItemType.THEME -> dataStore.edit { it[Keys.EQUIPPED_THEME] = itemId }
            ShopItemType.EFFECT -> dataStore.edit { it[Keys.EQUIPPED_EFFECT] = itemId }
            ShopItemType.BADGE -> dataStore.edit { it[Keys.EQUIPPED_BADGE] = itemId }
            ShopItemType.ICON -> dataStore.edit { it[Keys.EQUIPPED_ICON] = itemId }
            else -> {}
        }
    }

    suspend fun unequipItem(type: ShopItemType) {
        when (type) {
            ShopItemType.THEME -> dataStore.edit { it.remove(Keys.EQUIPPED_THEME) }
            ShopItemType.EFFECT -> dataStore.edit { it.remove(Keys.EQUIPPED_EFFECT) }
            ShopItemType.BADGE -> dataStore.edit { it.remove(Keys.EQUIPPED_BADGE) }
            ShopItemType.ICON -> dataStore.edit { it.remove(Keys.EQUIPPED_ICON) }
            else -> {}
        }
    }

    private suspend fun addToInventory(item: ShopItem) {
        dataStore.edit { prefs ->
            val current = parseInventory(prefs[Keys.INVENTORY_ITEMS] ?: "")
            val existing = current.find { it.itemId == item.id }
            
            val updated = if (existing != null && item.type == ShopItemType.BOOST) {
                // Stack boosts
                current.map { 
                    if (it.itemId == item.id) it.copy(quantity = it.quantity + 1)
                    else it 
                }
            } else {
                current + InventoryItem(item.id, System.currentTimeMillis(), 1)
            }
            
            prefs[Keys.INVENTORY_ITEMS] = serializeInventory(updated)
        }
    }

    private fun parseInventory(data: String): List<InventoryItem> {
        if (data.isEmpty()) return emptyList()
        return data.split(";").mapNotNull { item ->
            val parts = item.split(":")
            if (parts.size >= 2) {
                InventoryItem(
                    itemId = parts[0],
                    acquiredAt = parts[1].toLongOrNull() ?: 0,
                    quantity = parts.getOrNull(2)?.toIntOrNull() ?: 1
                )
            } else null
        }
    }

    private fun serializeInventory(items: List<InventoryItem>): String {
        return items.joinToString(";") { "${it.itemId}:${it.acquiredAt}:${it.quantity}" }
    }

    fun isItemOwned(itemId: String): Boolean {
        return runBlocking {
            inventory.first().any { it.itemId == itemId }
        }
    }

    private fun getRemainingQuantity(itemId: String, totalQuantity: Int): Int {
        // In a real implementation, this would be server-side
        // For now, use local storage
        val purchased = runBlocking {
            inventory.first().find { it.itemId == itemId }?.quantity ?: 0
        }
        return totalQuantity - purchased
    }

    // ═════════════════════════════════════════════════════════════
    // BOOSTS
    // ═════════════════════════════════════════════════════════════

    val activeBoosts: Flow<List<ActiveBoost>> = dataStore.data.map { prefs ->
        parseBoosts(prefs[Keys.ACTIVE_BOOSTS] ?: "")
    }

    suspend fun activateBoost(boostType: BoostType, durationHours: Int, multiplier: Float) {
        val boost = ActiveBoost(
            boostType = boostType,
            multiplier = multiplier,
            expiresAt = System.currentTimeMillis() + (durationHours * 60 * 60 * 1000)
        )
        
        dataStore.edit { prefs ->
            val current = parseBoosts(prefs[Keys.ACTIVE_BOOSTS] ?: "")
            val filtered = current.filter { it.boostType != boostType }
            prefs[Keys.ACTIVE_BOOSTS] = serializeBoosts(filtered + boost)
        }
    }

    private fun getActiveBoostMultiplier(type: BoostType): Float {
        val now = System.currentTimeMillis()
        return runBlocking {
            activeBoosts.first()
                .filter { it.boostType == type && it.expiresAt > now }
                .maxOfOrNull { it.multiplier } ?: 1f
        }
    }

    private fun getActiveDiscount(): Float {
        val now = System.currentTimeMillis()
        val discountBoost = runBlocking {
            activeBoosts.first()
                .filter { it.boostType == BoostType.DISCOUNT && it.expiresAt > now }
                .maxOfOrNull { it.multiplier } ?: 0f
        }
        return discountBoost
    }

    private fun parseBoosts(data: String): List<ActiveBoost> {
        if (data.isEmpty()) return emptyList()
        return data.split(";").mapNotNull { boost ->
            val parts = boost.split(":")
            if (parts.size >= 3) {
                ActiveBoost(
                    boostType = BoostType.valueOf(parts[0]),
                    multiplier = parts[1].toFloatOrNull() ?: 1f,
                    expiresAt = parts[2].toLongOrNull() ?: 0
                )
            } else null
        }.filter { it.expiresAt > System.currentTimeMillis() }
    }

    private fun serializeBoosts(boosts: List<ActiveBoost>): String {
        return boosts.joinToString(";") { "${it.boostType}:${it.multiplier}:${it.expiresAt}" }
    }

    // ═════════════════════════════════════════════════════════════
    // REQUIREMENTS CHECK
    // ═════════════════════════════════════════════════════════════

    private fun meetsRequirements(reqs: ShopRequirements?): Boolean {
        if (reqs == null) return true
        
        val level = runBlocking { userLevel.first() }
        if (level < reqs.minLevel) return false
        
        val totalInstalls = 0 // Would come from stats repository
        if (totalInstalls < reqs.totalInstalls) return false
        
        val streakDays = 0 // Would come from streak repository
        if (streakDays < reqs.streakDays) return false
        
        if (reqs.achievementIds.isNotEmpty()) {
            val hasAll = reqs.achievementIds.all { id ->
                achievementManager.isAchievementUnlocked(id)
            }
            if (!hasAll) return false
        }
        
        if (reqs.achievementCount > 0) {
            // Would check total unlocked achievements
        }
        
        return true
    }

    // ═════════════════════════════════════════════════════════════
    // DAILY DEALS
    // ═════════════════════════════════════════════════════════════

    fun getDailyDeal(): DailyDeal? {
        val lastSeen = runBlocking { 
            dataStore.data.map { it[Keys.DAILY_DEAL_SEEN] ?: 0 }.first() 
        }
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        // Generate daily deal based on date seed
        val random = Random(today)
        val eligibleItems = ShopCatalog.getAllItems()
            .filter { it.availability == ShopItemAvailability.PERMANENT }
            .filter { it.cost >= 100 }
        
        if (eligibleItems.isEmpty()) return null
        
        val selectedItem = eligibleItems[random.nextInt(eligibleItems.size)]
        val discount = 20 + random.nextInt(30) // 20-50% off
        
        return DailyDeal(
            itemId = selectedItem.id,
            originalPrice = selectedItem.cost,
            salePrice = (selectedItem.cost * (100 - discount) / 100),
            discountPercent = discount,
            expiresAt = today + (24 * 60 * 60 * 1000)
        )
    }

    // ═════════════════════════════════════════════════════════════
    // REWARDS
    // ═════════════════════════════════════════════════════════════

    suspend fun awardAdReward() {
        // Award sugar points for watching ad
        addSugarPoints(50, "ad_reward")
        addXP(5)
    }

    suspend fun awardAchievementReward(rarity: Rarity) {
        val points = when (rarity) {
            Rarity.COMMON -> 10
            Rarity.UNCOMMON -> 25
            Rarity.RARE -> 50
            Rarity.EPIC -> 100
            Rarity.LEGENDARY -> 250
        }
        addSugarPoints(points, "achievement_reward")
    }

    // ═════════════════════════════════════════════════════════════
    // SUGAR PASS INTEGRATION
    // ═════════════════════════════════════════════════════════════

    /**
     * Unlock an item from Sugar Pass rewards
     */
    suspend fun unlockPassItem(itemId: String, type: com.sugarmunch.app.pass.PassRewardType) {
        dataStore.edit { prefs ->
            val current = parseInventory(prefs[Keys.INVENTORY_ITEMS] ?: "")
            
            // Map PassRewardType to ShopItemType
            val shopType = when (type) {
                com.sugarmunch.app.pass.PassRewardType.THEME -> ShopItemType.THEME
                com.sugarmunch.app.pass.PassRewardType.EFFECT -> ShopItemType.EFFECT
                com.sugarmunch.app.pass.PassRewardType.BADGE -> ShopItemType.BADGE
                com.sugarmunch.app.pass.PassRewardType.ICON -> ShopItemType.ICON
                com.sugarmunch.app.pass.PassRewardType.PARTICLE_STYLE -> ShopItemType.PARTICLE
                com.sugarmunch.app.pass.PassRewardType.TITLE,
                com.sugarmunch.app.pass.PassRewardType.EMOJI_PACK,
                com.sugarmunch.app.pass.PassRewardType.FEATURE_UNLOCK,
                com.sugarmunch.app.pass.PassRewardType.SUGAR_POINTS,
                com.sugarmunch.app.pass.PassRewardType.XP_BOOST,
                com.sugarmunch.app.pass.PassRewardType.CURRENCY_BOOST -> null
            }
            
            // Add to inventory if it's an item type
            if (shopType != null) {
                val existing = current.find { it.itemId == itemId }
                if (existing == null) {
                    val updated = current + InventoryItem(
                        itemId = itemId, 
                        acquiredAt = System.currentTimeMillis(), 
                        quantity = 1
                    )
                    prefs[Keys.INVENTORY_ITEMS] = serializeInventory(updated)
                }
            }
        }
    }

    // ═════════════════════════════════════════════════════════════
    // SINGLETON
    // ═════════════════════════════════════════════════════════════

    companion object {
        @Volatile
        private var instance: ShopManager? = null

        fun getInstance(context: Context): ShopManager {
            return instance ?: synchronized(this) {
                instance ?: ShopManager(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
    }
}

enum class PurchaseResult {
    SUCCESS,
    FAILURE_INSUFFICIENT_FUNDS,
    FAILURE_REQUIREMENTS,
    FAILURE_ALREADY_OWNED,
    FAILURE_OUT_OF_STOCK
}
