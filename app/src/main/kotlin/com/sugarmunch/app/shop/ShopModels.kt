package com.sugarmunch.app.shop

import com.sugarmunch.app.features.model.Rarity

/**
 * Sugar Shop - Virtual Economy Models
 * Users spend Sugar Points on exclusive items
 */

enum class ShopItemType {
    THEME,          // Exclusive themes
    EFFECT,         // Premium effects
    PARTICLE,       // Particle styles
    ICON,           // App icons
    BADGE,          // Profile badges
    BOOST,          // Temporary boosts
    FEATURE,        // Unlock features
    BUNDLE          // Item bundles
}

enum class ShopItemAvailability {
    PERMANENT,      // Always available
    LIMITED_TIME,   // Limited time offer
    SEASONAL,       // Seasonal (holidays)
    EXCLUSIVE,      // One-time availability
    RARE_DROP       // Random availability
}

data class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val type: ShopItemType,
    val cost: Int,                      // Sugar Points cost
    val rarity: Rarity,
    val availability: ShopItemAvailability = ShopItemAvailability.PERMANENT,
    val icon: String,                   // Emoji or icon reference
    val previewColor: String,           // Hex color for preview
    val requirements: ShopRequirements? = null,
    val limitedQuantity: Int? = null,   // Limited stock
    val availableFrom: Long? = null,    // Timestamp for limited items
    val availableUntil: Long? = null,   // Timestamp for limited items
    val discount: Int = 0,              // Percentage discount
    val isNew: Boolean = false,
    val bundleItems: List<String>? = null // For bundles, list of item IDs
)

data class ShopRequirements(
    val minLevel: Int = 0,
    val achievementIds: List<String> = emptyList(),
    val achievementCount: Int = 0,
    val totalInstalls: Int = 0,
    val streakDays: Int = 0
)

data class UserInventory(
    val userId: String,
    val items: List<InventoryItem>,
    val equippedTheme: String? = null,
    val equippedEffect: String? = null,
    val equippedBadge: String? = null,
    val equippedIcon: String? = null,
    val activeBoosts: List<ActiveBoost> = emptyList()
)

data class InventoryItem(
    val itemId: String,
    val acquiredAt: Long,
    val quantity: Int = 1
)

data class ActiveBoost(
    val boostType: BoostType,
    val multiplier: Float,
    val expiresAt: Long
)

enum class BoostType {
    SUGAR_POINTS,       // Earn points faster
    XP_GAIN,            // Level up faster
    DISCOUNT,           // Shop discounts
    EXCLUSIVE_ACCESS    // Access to exclusive items
}

data class ShopBundle(
    val id: String,
    val name: String,
    val description: String,
    val items: List<String>,
    val totalValue: Int,
    val bundlePrice: Int,
    val savingsPercent: Int
)

// Daily Deals
data class DailyDeal(
    val itemId: String,
    val originalPrice: Int,
    val salePrice: Int,
    val discountPercent: Int,
    val expiresAt: Long
)

// Shop Stats for analytics
 data class ShopStats(
    val totalPurchases: Int,
    val totalSpent: Int,
    val favoriteCategory: ShopItemType,
    val rarestItemOwned: String?
)
