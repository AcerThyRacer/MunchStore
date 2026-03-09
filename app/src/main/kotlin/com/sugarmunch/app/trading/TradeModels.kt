package com.sugarmunch.app.trading

import androidx.room.*
import com.sugarmunch.app.features.model.AchievementRarity
import com.sugarmunch.app.shop.ShopItemType
import kotlinx.serialization.Serializable
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════
 * SUGARMUNCH TRADING SYSTEM - DATA MODELS
 * 
 * Pokemon-card style trading for shop items with friends!
 * ═══════════════════════════════════════════════════════════════════
 */

/**
 * Trade Status - Tracks the lifecycle of a trade offer
 */
enum class TradeStatus {
    PENDING,      // ⏳ Waiting for response
    ACCEPTED,     // ✅ Trade completed successfully
    DECLINED,     // ❌ Trade rejected by recipient
    CANCELLED,    // 🚫 Cancelled by sender
    EXPIRED,      // ⏰ Trade timed out (24h default)
    INVALID       // ⚠️ Items no longer available/valid
}

/**
 * Trade Type - Direct trade or Gift
 */
enum class TradeType {
    TRADE,        // 🤝 Item-for-item exchange
    GIFT          // 🎁 One-way gift (no return items)
}

/**
 * Gift Wrapping Style - Visual presentation for gifts
 */
enum class GiftWrapping(
    val displayName: String,
    val emoji: String,
    val colorHex: String,
    val animation: String
) {
    CLASSIC("Classic", "🎁", "#FF6B6B", "bounce"),
    ELEGANT("Elegant", "🎀", "#FFD700", "sparkle"),
    CUTE("Cute", "🧸", "#FFB6C1", "wiggle"),
    MYSTERIOUS("Mystery", "🎭", "#9370DB", "shake"),
    FESTIVE("Festive", "🎄", "#228B22", "pulse"),
    CANDY("Candy", "🍬", "#FF69B4", "spin"),
    GOLDEN("Golden", "👑", "#FFD700", "glow"),
    NONE("No Wrap", "📦", "#808080", "none")
}

/**
 * Trade Item - Wrapper for shop items in a trade
 * Includes metadata about the item being traded
 */
@Serializable
@Entity(tableName = "trade_items")
data class TradeItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val itemId: String,                    // Reference to ShopItem.id
    val itemType: ShopItemType,            // Category for filtering
    val itemName: String,                  // Display name
    val itemIcon: String,                  // Emoji/icon
    val itemRarity: AchievementRarity,     // For visual flair
    val quantity: Int = 1,                 // Stack size
    val previewColor: String = "#FFB6C1",  // Item color
    val isTradable: Boolean = true,        // Can this item be traded?
    val tradeCooldownUntil: Long? = null   // Cooldown after trade
)

/**
 * Trade Offer - Core trading entity
 * Represents a trade proposal between two users
 */
@Serializable
@Entity(tableName = "trade_offers")
data class TradeOffer(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    
    // Participants
    val senderId: String,                  // Who sent the offer
    val senderName: String,                // Display name
    val recipientId: String,               // Who receives the offer
    val recipientName: String,             // Display name
    
    // Trade Details
    val tradeType: TradeType = TradeType.TRADE,
    val status: TradeStatus = TradeStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // 24h default
    val respondedAt: Long? = null,         // When action was taken
    
    // Items (JSON serialized lists)
    val senderItemsJson: String,           // Items sender is offering
    val recipientItemsJson: String? = null, // Items requested in return (null for gifts)
    
    // Sugar Points (can be added to trades)
    val senderSugarPoints: Int = 0,
    val recipientSugarPoints: Int = 0,
    
    // Gift-specific fields
    val giftMessage: String? = null,
    val giftWrapping: GiftWrapping? = null,
    val isAnonymous: Boolean = false,
    
    // Metadata
    val message: String? = null,           // Trade message
    val declinedReason: String? = null,    // Why was it declined
    val serverTradeId: String? = null      // For cloud sync
) {
    val isGift: Boolean get() = tradeType == TradeType.GIFT
    val isExpired: Boolean get() = System.currentTimeMillis() > expiresAt && status == TradeStatus.PENDING
    val isPending: Boolean get() = status == TradeStatus.PENDING
}

/**
 * Trade History Entry - Record of completed/cancelled trades
 */
@Serializable
@Entity(tableName = "trade_history")
data class TradeHistoryEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val tradeOfferId: String,              // Reference to original offer
    
    // Participants (snapshot at time of trade)
    val senderId: String,
    val senderName: String,
    val recipientId: String,
    val recipientName: String,
    
    // Final State
    val tradeType: TradeType,
    val finalStatus: TradeStatus,
    val completedAt: Long = System.currentTimeMillis(),
    
    // Items Traded
    val itemsSentJson: String,             // What sender gave
    val itemsReceivedJson: String,         // What recipient gave (or empty for gifts)
    val sugarPointsTransferred: Int = 0,   // Net sugar points moved
    
    // Statistics
    val totalItemValue: Int = 0,           // Estimated value for analytics
    val wasGift: Boolean = false,
    val wasAnonymous: Boolean = false
)

/**
 * Trade Listing - Community marketplace listing
 * Users can post what they have/want
 */
@Serializable
@Entity(tableName = "trade_listings")
data class TradeListing(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    
    // Creator
    val creatorId: String,
    val creatorName: String,
    val creatorAvatar: String = "🍬",
    
    // Listing Details
    val listingType: ListingType,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000), // 7 days
    val isActive: Boolean = true,
    
    // Items
    val haveItemsJson: String,             // What they have to trade
    val wantItemsJson: String? = null,     // What they want (null for "offers welcome")
    
    // Sugar Points
    val wantSugarPoints: Int? = null,      // Specific price or null for negotiable
    
    // Filters
    val categoryFilter: ShopItemType? = null,
    val rarityFilter: AchievementRarity? = null,
    
    // Stats
    val viewCount: Int = 0,
    val offerCount: Int = 0,
    val serverListingId: String? = null
) {
    val isExpired: Boolean get() = System.currentTimeMillis() > expiresAt
}

/**
 * Listing Type - What kind of listing
 */
enum class ListingType {
    HAVE_WANT,      // 🔄 Have X, want Y
    HAVE_OFFER,     // 🎁 Have X, accepting offers
    WANT_BUY,       // 💰 Looking to buy with Sugar Points
    TRADE_ONLY      // 🤝 Item-for-item only
}

/**
 * Trade Request - Response to a listing
 */
@Serializable
@Entity(tableName = "trade_requests")
data class TradeRequest(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val listingId: String,                 // Which listing
    val requesterId: String,               // Who's making the offer
    val requesterName: String,
    val offeredItemsJson: String,          // What they're offering
    val offeredSugarPoints: Int = 0,
    val message: String? = null,
    val status: RequestStatus = RequestStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val respondedAt: Long? = null
)

enum class RequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    CANCELLED,
    EXPIRED
}

/**
 * Trade Statistics - User's trading stats
 */
@Serializable
@Entity(tableName = "trade_stats")
data class TradeStats(
    @PrimaryKey val userId: String,
    
    // Trade Counts
    val totalTradesCompleted: Int = 0,
    val totalTradesSent: Int = 0,
    val totalTradesReceived: Int = 0,
    val totalGiftsSent: Int = 0,
    val totalGiftsReceived: Int = 0,
    
    // Success Rates
    val acceptedCount: Int = 0,
    val declinedCount: Int = 0,
    val cancelledCount: Int = 0,
    
    // Values
    val totalItemsTraded: Int = 0,
    val totalSugarPointsTraded: Int = 0,
    
    // Ratings
    val traderRating: Float = 5.0f,        // 1-5 star rating
    val ratingCount: Int = 0,
    
    // Streaks
    val currentDailyTradeStreak: Int = 0,
    val longestTradeStreak: Int = 0,
    val lastTradeDate: Long = 0,
    
    // Limits
    val giftsSentToday: Int = 0,
    val lastGiftResetDate: Long = System.currentTimeMillis()
) {
    val successRate: Float
        get() = if (totalTradesSent > 0) {
            acceptedCount.toFloat() / totalTradesSent
        } else 0f
    
    val canSendGiftToday: Boolean
        get() = giftsSentToday < DAILY_GIFT_LIMIT
}

/**
 * Trade Notification - Real-time trade updates
 */
@Serializable
data class TradeNotification(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val tradeId: String,
    val type: TradeNotificationType,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val actionData: Map<String, String> = emptyMap()
)

enum class TradeNotificationType {
    OFFER_RECEIVED,        // 🎁 New trade offer!
    OFFER_ACCEPTED,        // ✅ Your trade was accepted!
    OFFER_DECLINED,        // ❌ Your trade was declined
    OFFER_CANCELLED,       // 🚫 Trade cancelled
    OFFER_EXPIRED,         // ⏰ Trade expired
    GIFT_RECEIVED,         // 🎁 You got a gift!
    GIFT_OPENED,           // 📦 Gift was opened
    LISTING_OFFER,         // 💰 Someone made an offer on your listing
    TRADE_COMPLETED,       // 🎉 Trade completed successfully
    TRADE_REMINDER         // ⏰ Reminder about pending trade
}

/**
 * Trade Validation Result
 */
sealed class TradeValidationResult {
    data object Valid : TradeValidationResult()
    data class Invalid(val reason: String) : TradeValidationResult()
}

/**
 * Trade Result
 */
sealed class TradeResult {
    data class Success(val message: String) : TradeResult()
    data class Failure(val error: TradeError) : TradeResult()
}

enum class TradeError {
    INSUFFICIENT_ITEMS,
    ITEM_NOT_TRADABLE,
    ITEM_ON_COOLDOWN,
    INVALID_RECIPIENT,
    SELF_TRADE_NOT_ALLOWED,
    TRADE_LIMIT_REACHED,
    GIFT_LIMIT_REACHED,
    ALREADY_TRADING,
    NETWORK_ERROR,
    SERVER_ERROR,
    UNKNOWN
}

// ═══════════════════════════════════════════════════════════════════
// CONSTANTS
// ═══════════════════════════════════════════════════════════════════

const val DAILY_GIFT_LIMIT = 5
const val DEFAULT_TRADE_EXPIRY_HOURS = 24
const val LISTING_EXPIRY_DAYS = 7
const val MIN_TRADE_LEVEL = 3  // Must be level 3+ to trade
const val TRADE_COOLDOWN_HOURS = 24  // Items can't be re-traded for 24h

// ═══════════════════════════════════════════════════════════════════
// HELPER EXTENSIONS
// ═══════════════════════════════════════════════════════════════════

fun TradeStatus.getDisplayText(): String = when (this) {
    TradeStatus.PENDING -> "Pending"
    TradeStatus.ACCEPTED -> "Accepted"
    TradeStatus.DECLINED -> "Declined"
    TradeStatus.CANCELLED -> "Cancelled"
    TradeStatus.EXPIRED -> "Expired"
    TradeStatus.INVALID -> "Invalid"
}

fun TradeStatus.getEmoji(): String = when (this) {
    TradeStatus.PENDING -> "⏳"
    TradeStatus.ACCEPTED -> "✅"
    TradeStatus.DECLINED -> "❌"
    TradeStatus.CANCELLED -> "🚫"
    TradeStatus.EXPIRED -> "⏰"
    TradeStatus.INVALID -> "⚠️"
}

fun GiftWrapping.getAnimationData(): String = animation
fun AchievementRarity.getTradeValueMultiplier(): Float = when (this) {
    AchievementRarity.COMMON -> 1.0f
    AchievementRarity.UNCOMMON -> 1.5f
    AchievementRarity.RARE -> 2.5f
    AchievementRarity.EPIC -> 5.0f
    AchievementRarity.LEGENDARY -> 10.0f
}
