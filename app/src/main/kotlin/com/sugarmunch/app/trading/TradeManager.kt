package com.sugarmunch.app.trading

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.auth.AuthManager
import com.sugarmunch.app.features.model.AchievementRarity
import com.sugarmunch.app.shop.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * ═══════════════════════════════════════════════════════════════════
 * SUGARMUNCH TRADE MANAGER
 *
 * Core trading system - like Pokemon card trading but for SugarMunch!
 *
 * Features:
 * • Send/receive trade offers between friends
 * • Accept/decline/cancel trades
 * • Trade history tracking
 * • Validation (ownership, tradable status)
 * • Real-time notifications
 * ═══════════════════════════════════════════════════════════════════
 * 
 * Note for Hilt migration: Use @ApplicationContext for the Context parameter
 * and inject AuthManager as a dependency
 */

private val Context.tradeDataStore by preferencesDataStore(name = "trading_prefs")

class TradeManager constructor(
    private val context: Context,
    private val authManager: AuthManager
) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }
    
    // In-memory caches
    private val _activeTrades = MutableStateFlow<List<TradeOffer>>(emptyList())
    private val _tradeHistory = MutableStateFlow<List<TradeHistoryEntry>>(emptyList())
    private val _notifications = MutableStateFlow<List<TradeNotification>>(emptyList())
    
    // Public flows
    val activeTrades: StateFlow<List<TradeOffer>> = _activeTrades.asStateFlow()
    val tradeHistory: StateFlow<List<TradeHistoryEntry>> = _tradeHistory.asStateFlow()
    val notifications: StateFlow<List<TradeNotification>> = _notifications.asStateFlow()
    
    // Trade locks to prevent concurrent modifications
    private val tradeLocks = ConcurrentHashMap<String, Any>()
    
    // Dependencies (would be injected in real app)
    private val shopManager = ShopManager.getInstance(context)
    
    init {
        // Start background cleanup
        startCleanupWorker()
        // Load cached data
        loadCachedData()
    }

    companion object {
        /**
         * Maximum number of parallel trades allowed
         */
        const val MAX_PARALLEL_TRADES = 5
        
        /**
         * Trade cooldown period in hours
         */
        const val TRADE_COOLDOWN_HOURS = 24
        
        /**
         * Daily gift limit per user
         */
        const val DAILY_GIFT_LIMIT = 10
    }

    // ═════════════════════════════════════════════════════════════════
    // TRADE CREATION
    // ═════════════════════════════════════════════════════════════════

    /**
     * Create a new trade offer
     * @param recipientId User to trade with
     * @param senderItems Items you're offering
     * @param recipientItems Items you want in return
     * @param senderSugarPoints Sugar points to include
     * @param message Optional message
     * @return TradeResult with the created offer or error
     */
    suspend fun createOffer(
        recipientId: String,
        recipientName: String,
        senderItems: List<TradeItem>,
        recipientItems: List<TradeItem> = emptyList(),
        senderSugarPoints: Int = 0,
        message: String? = null
    ): TradeResult {
        // Validation
        val validation = validateTrade(
            senderItems = senderItems,
            recipientItems = recipientItems,
            senderSugarPoints = senderSugarPoints,
            recipientId = recipientId
        )
        
        if (validation is TradeValidationResult.Invalid) {
            return TradeResult.Failure(TradeError.UNKNOWN) // Map reason to error
        }
        
        // Check for self-trade
        if (recipientId == getCurrentUserId()) {
            return TradeResult.Failure(TradeError.SELF_TRADE_NOT_ALLOWED)
        }
        
        // Check level requirement
        if (!meetsTradeRequirements()) {
            return TradeResult.Failure(TradeError.TRADE_LIMIT_REACHED)
        }
        
        // Lock items during trade creation
        val lockKey = "create_${System.currentTimeMillis()}"
        synchronized(tradeLocks.computeIfAbsent(lockKey) { Any() }) {
            // Create trade offer
            val offer = TradeOffer(
                senderId = getCurrentUserId(),
                senderName = getCurrentUserName(),
                recipientId = recipientId,
                recipientName = recipientName,
                tradeType = TradeType.TRADE,
                senderItemsJson = json.encodeToString(senderItems),
                recipientItemsJson = json.encodeToString(recipientItems),
                senderSugarPoints = senderSugarPoints,
                message = message
            )
            
            // Reserve items (remove from available inventory)
            reserveItems(senderItems)
            
            // Save trade
            saveTradeOffer(offer)
            
            // Send notification to recipient
            sendNotification(
                userId = recipientId,
                type = TradeNotificationType.OFFER_RECEIVED,
                title = "New Trade Offer!",
                message = "${getCurrentUserName()} wants to trade with you!",
                tradeId = offer.id
            )
            
            // Update stats
            updateTradeStats { it.copy(totalTradesSent = it.totalTradesSent + 1) }
            
            return TradeResult.Success("Trade offer sent!")
        }
    }
    
    /**
     * Create a gift offer (one-way trade)
     */
    suspend fun createGiftOffer(
        recipientId: String,
        recipientName: String,
        items: List<TradeItem>,
        sugarPoints: Int = 0,
        message: String? = null,
        wrapping: GiftWrapping = GiftWrapping.CLASSIC,
        isAnonymous: Boolean = false
    ): TradeResult {
        // Check gift limits
        if (!canSendGiftToday()) {
            return TradeResult.Failure(TradeError.GIFT_LIMIT_REACHED)
        }
        
        // Validate
        val validation = validateTrade(
            senderItems = items,
            senderSugarPoints = sugarPoints,
            recipientId = recipientId
        )
        
        if (validation is TradeValidationResult.Invalid) {
            return TradeResult.Failure(TradeError.UNKNOWN)
        }
        
        val offer = TradeOffer(
            senderId = getCurrentUserId(),
            senderName = if (isAnonymous) "Anonymous" else getCurrentUserName(),
            recipientId = recipientId,
            recipientName = recipientName,
            tradeType = TradeType.GIFT,
            senderItemsJson = json.encodeToString(items),
            recipientItemsJson = null,
            senderSugarPoints = sugarPoints,
            giftMessage = message,
            giftWrapping = wrapping,
            isAnonymous = isAnonymous
        )
        
        reserveItems(items)
        saveTradeOffer(offer)
        
        sendNotification(
            userId = recipientId,
            type = TradeNotificationType.GIFT_RECEIVED,
            title = "🎁 You received a gift!",
            message = if (isAnonymous) "Someone sent you a mysterious gift!" 
                     else "${getCurrentUserName()} sent you a gift!",
            tradeId = offer.id
        )
        
        incrementGiftsSent()
        
        return TradeResult.Success("Gift sent!")
    }
    
    // ═════════════════════════════════════════════════════════════════
    // TRADE ACTIONS
    // ═════════════════════════════════════════════════════════════════
    
    /**
     * Accept a pending trade offer
     */
    suspend fun acceptOffer(tradeId: String): TradeResult {
        val trade = getTradeById(tradeId) 
            ?: return TradeResult.Failure(TradeError.UNKNOWN)
        
        if (trade.status != TradeStatus.PENDING) {
            return TradeResult.Failure(TradeError.ALREADY_TRADING)
        }
        
        if (trade.recipientId != getCurrentUserId()) {
            return TradeResult.Failure(TradeError.INVALID_RECIPIENT)
        }
        
        synchronized(tradeLocks.computeIfAbsent(tradeId) { Any() }) {
            // Validate items are still available
            val senderItems = json.decodeFromString<List<TradeItem>>(trade.senderItemsJson)
            val recipientItems = trade.recipientItemsJson?.let {
                json.decodeFromString<List<TradeItem>>(it)
            } ?: emptyList()
            
            // Execute trade
            executeTrade(trade, senderItems, recipientItems)
            
            // Update trade status
            val updatedTrade = trade.copy(
                status = TradeStatus.ACCEPTED,
                respondedAt = System.currentTimeMillis()
            )
            updateTradeOffer(updatedTrade)
            
            // Add to history
            addToHistory(updatedTrade, senderItems, recipientItems)
            
            // Notify sender
            sendNotification(
                userId = trade.senderId,
                type = TradeNotificationType.OFFER_ACCEPTED,
                title = "Trade Accepted!",
                message = "${getCurrentUserName()} accepted your trade offer!",
                tradeId = tradeId
            )
            
            // Update stats
            updateTradeStats { 
                it.copy(
                    acceptedCount = it.acceptedCount + 1,
                    totalTradesCompleted = it.totalTradesCompleted + 1
                )
            }
            
            return TradeResult.Success("Trade completed!")
        }
    }
    
    /**
     * Decline a trade offer
     */
    suspend fun declineOffer(tradeId: String, reason: String? = null): TradeResult {
        val trade = getTradeById(tradeId)
            ?: return TradeResult.Failure(TradeError.UNKNOWN)
        
        if (trade.status != TradeStatus.PENDING) {
            return TradeResult.Failure(TradeError.ALREADY_TRADING)
        }
        
        synchronized(tradeLocks.computeIfAbsent(tradeId) { Any() }) {
            // Return reserved items to sender
            val senderItems = json.decodeFromString<List<TradeItem>>(trade.senderItemsJson)
            returnReservedItems(senderItems)
            
            val updatedTrade = trade.copy(
                status = TradeStatus.DECLINED,
                respondedAt = System.currentTimeMillis(),
                declinedReason = reason
            )
            updateTradeOffer(updatedTrade)
            
            // Notify sender
            sendNotification(
                userId = trade.senderId,
                type = TradeNotificationType.OFFER_DECLINED,
                title = "Trade Declined",
                message = "${getCurrentUserName()} declined your trade offer",
                tradeId = tradeId
            )
            
            updateTradeStats { it.copy(declinedCount = it.declinedCount + 1) }
            
            return TradeResult.Success("Trade declined")
        }
    }
    
    /**
     * Cancel a trade you sent
     */
    suspend fun cancelOffer(tradeId: String): TradeResult {
        val trade = getTradeById(tradeId)
            ?: return TradeResult.Failure(TradeError.UNKNOWN)
        
        if (trade.senderId != getCurrentUserId()) {
            return TradeResult.Failure(TradeError.INVALID_RECIPIENT)
        }
        
        if (trade.status != TradeStatus.PENDING) {
            return TradeResult.Failure(TradeError.ALREADY_TRADING)
        }
        
        synchronized(tradeLocks.computeIfAbsent(tradeId) { Any() }) {
            // Return reserved items
            val senderItems = json.decodeFromString<List<TradeItem>>(trade.senderItemsJson)
            returnReservedItems(senderItems)
            
            val updatedTrade = trade.copy(
                status = TradeStatus.CANCELLED,
                respondedAt = System.currentTimeMillis()
            )
            updateTradeOffer(updatedTrade)
            
            // Notify recipient
            sendNotification(
                userId = trade.recipientId,
                type = TradeNotificationType.OFFER_CANCELLED,
                title = "Trade Cancelled",
                message = "${getCurrentUserName()} cancelled the trade offer",
                tradeId = tradeId
            )
            
            updateTradeStats { it.copy(cancelledCount = it.cancelledCount + 1) }
            
            return TradeResult.Success("Trade cancelled")
        }
    }
    
    /**
     * Open a gift
     */
    suspend fun openGift(tradeId: String): TradeResult {
        val trade = getTradeById(tradeId)
            ?: return TradeResult.Failure(TradeError.UNKNOWN)
        
        if (trade.tradeType != TradeType.GIFT) {
            return TradeResult.Failure(TradeError.UNKNOWN)
        }
        
        if (trade.recipientId != getCurrentUserId()) {
            return TradeResult.Failure(TradeError.INVALID_RECIPIENT)
        }
        
        synchronized(tradeLocks.computeIfAbsent(tradeId) { Any() }) {
            val items = json.decodeFromString<List<TradeItem>>(trade.senderItemsJson)
            
            // Add items to inventory
            items.forEach { item ->
                addItemToInventory(item)
            }
            
            // Add sugar points
            if (trade.senderSugarPoints > 0) {
                addSugarPoints(trade.senderSugarPoints)
            }
            
            // Update trade
            val updatedTrade = trade.copy(
                status = TradeStatus.ACCEPTED,
                respondedAt = System.currentTimeMillis()
            )
            updateTradeOffer(updatedTrade)
            
            // Add to history
            addToHistory(updatedTrade, items, emptyList())
            
            // Notify sender that gift was opened
            if (!trade.isAnonymous) {
                sendNotification(
                    userId = trade.senderId,
                    type = TradeNotificationType.GIFT_OPENED,
                    title = "Gift Opened!",
                    message = "${getCurrentUserName()} opened your gift!",
                    tradeId = tradeId
                )
            }
            
            updateTradeStats { 
                it.copy(
                    totalGiftsReceived = it.totalGiftsReceived + 1,
                    totalTradesCompleted = it.totalTradesCompleted + 1
                )
            }
            
            return TradeResult.Success("Gift opened!")
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // VALIDATION
    // ═════════════════════════════════════════════════════════════════
    
    fun validateTrade(
        senderItems: List<TradeItem> = emptyList(),
        recipientItems: List<TradeItem> = emptyList(),
        senderSugarPoints: Int = 0,
        recipientSugarPoints: Int = 0,
        recipientId: String? = null
    ): TradeValidationResult {
        // Check ownership of sender items
        senderItems.forEach { item ->
            if (!ownsItem(item)) {
                return TradeValidationResult.Invalid("You don't own: ${item.itemName}")
            }
            if (!item.isTradable) {
                return TradeValidationResult.Invalid("${item.itemName} is not tradable")
            }
            if (item.tradeCooldownUntil != null && System.currentTimeMillis() < item.tradeCooldownUntil) {
                return TradeValidationResult.Invalid("${item.itemName} is on trade cooldown")
            }
        }
        
        // Check sugar points
        if (senderSugarPoints > 0 && getCurrentSugarPoints() < senderSugarPoints) {
            return TradeValidationResult.Invalid("Insufficient Sugar Points")
        }
        
        // At least one item or sugar points must be offered
        if (senderItems.isEmpty() && senderSugarPoints == 0) {
            return TradeValidationResult.Invalid("Trade must include at least one item or Sugar Points")
        }
        
        return TradeValidationResult.Valid
    }
    
    // ═════════════════════════════════════════════════════════════════
    // QUERIES
    // ═════════════════════════════════════════════════════════════════
    
    fun getTradeById(tradeId: String): TradeOffer? {
        return _activeTrades.value.find { it.id == tradeId }
    }
    
    fun getPendingTradesForUser(userId: String): List<TradeOffer> {
        return _activeTrades.value.filter { 
            (it.senderId == userId || it.recipientId == userId) && 
            it.status == TradeStatus.PENDING 
        }
    }
    
    fun getReceivedOffers(): List<TradeOffer> {
        return _activeTrades.value.filter { 
            it.recipientId == getCurrentUserId() && it.status == TradeStatus.PENDING 
        }
    }
    
    fun getSentOffers(): List<TradeOffer> {
        return _activeTrades.value.filter { 
            it.senderId == getCurrentUserId() && it.status == TradeStatus.PENDING 
        }
    }
    
    fun getTradeHistory(limit: Int = 50): List<TradeHistoryEntry> {
        return _tradeHistory.value.take(limit)
    }
    
    fun getUnreadNotificationCount(): Int {
        return _notifications.value.count { !it.isRead && it.userId == getCurrentUserId() }
    }
    
    fun markNotificationAsRead(notificationId: String) {
        _notifications.value = _notifications.value.map { 
            if (it.id == notificationId) it.copy(isRead = true) else it 
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═════════════════════════════════════════════════════════════════
    
    private fun executeTrade(
        trade: TradeOffer,
        senderItems: List<TradeItem>,
        recipientItems: List<TradeItem>
    ) {
        // Remove items from sender
        senderItems.forEach { removeItemFromInventory(it) }
        
        // Remove items from recipient (if not a gift)
        if (trade.tradeType == TradeType.TRADE) {
            recipientItems.forEach { removeItemFromInventory(it, trade.recipientId) }
        }
        
        // Add items to recipient
        senderItems.forEach { 
            val tradedItem = it.copy(
                tradeCooldownUntil = System.currentTimeMillis() + (TRADE_COOLDOWN_HOURS * 60 * 60 * 1000)
            )
            addItemToInventory(tradedItem, trade.recipientId) 
        }
        
        // Add items to sender (if not a gift)
        if (trade.tradeType == TradeType.TRADE) {
            recipientItems.forEach { 
                val tradedItem = it.copy(
                    tradeCooldownUntil = System.currentTimeMillis() + (TRADE_COOLDOWN_HOURS * 60 * 60 * 1000)
                )
                addItemToInventory(tradedItem, trade.senderId) 
            }
        }
        
        // Transfer sugar points
        if (trade.senderSugarPoints > 0) {
            removeSugarPoints(trade.senderSugarPoints, trade.senderId)
            addSugarPoints(trade.senderSugarPoints, trade.recipientId)
        }
        if (trade.recipientSugarPoints > 0) {
            removeSugarPoints(trade.recipientSugarPoints, trade.recipientId)
            addSugarPoints(trade.recipientSugarPoints, trade.senderId)
        }
    }
    
    private fun reserveItems(items: List<TradeItem>) {
        // Mark items as reserved in inventory
        // Implementation depends on inventory system
    }
    
    private fun returnReservedItems(items: List<TradeItem>) {
        // Unmark reserved items
    }
    
    private fun addItemToInventory(item: TradeItem, userId: String = getCurrentUserId()) {
        // Add to user's inventory
    }
    
    private fun removeItemFromInventory(item: TradeItem, userId: String = getCurrentUserId()) {
        // Remove from user's inventory
    }
    
    private fun addSugarPoints(points: Int, userId: String = getCurrentUserId()) {
        // Add sugar points to user
    }
    
    private fun removeSugarPoints(points: Int, userId: String = getCurrentUserId()): Boolean {
        // In production, this would call ShopManager to deduct points
        return true
    }

    private fun ownsItem(item: TradeItem): Boolean {
        // Check if user owns item by querying ShopManager inventory
        val currentUserId = getCurrentUserId()
        return when (item) {
            is TradeItem.Theme -> {
                // Check if user owns this theme
                true // Would check ShopManager.inventory
            }
            is TradeItem.Effect -> {
                // Check if user owns this effect
                true // Would check ShopManager.inventory
            }
            is TradeItem.Badge -> {
                // Check if user owns this badge
                true // Would check ShopManager.inventory
            }
            is TradeItem.Icon -> {
                // Check if user owns this icon
                true // Would check ShopManager.inventory
            }
            is TradeItem.Boost -> false // Boosts are consumed, not tradable
            is TradeItem.SugarPoints -> false // Sugar points handled separately
        }
    }

    private fun getCurrentUserId(): String {
        return authManager.getCurrentUserId().takeIf { it.isNotEmpty() } ?: "anonymous_user"
    }

    private fun getCurrentUserName(): String {
        return authManager.currentUser.value?.getDisplayName() ?: "Anonymous User"
    }

    private fun getCurrentSugarPoints(): Int {
        // Would get from ShopManager
        return ShopManager.getInstance(context).sugarPoints.value
    }
    
    private fun meetsTradeRequirements(): Boolean {
        // Check level, etc.
        return true
    }
    
    private fun canSendGiftToday(): Boolean {
        val stats = getTradeStats()
        return stats.giftsSentToday < DAILY_GIFT_LIMIT
    }
    
    private fun incrementGiftsSent() {
        updateTradeStats { 
            it.copy(
                giftsSentToday = it.giftsSentToday + 1,
                totalGiftsSent = it.totalGiftsSent + 1
            )
        }
    }
    
    private fun saveTradeOffer(offer: TradeOffer) {
        _activeTrades.value += offer
        persistTrades()
    }
    
    private fun updateTradeOffer(offer: TradeOffer) {
        _activeTrades.value = _activeTrades.value.map { 
            if (it.id == offer.id) offer else it 
        }
        persistTrades()
    }
    
    private fun addToHistory(trade: TradeOffer, sent: List<TradeItem>, received: List<TradeItem>) {
        val entry = TradeHistoryEntry(
            tradeOfferId = trade.id,
            senderId = trade.senderId,
            senderName = trade.senderName,
            recipientId = trade.recipientId,
            recipientName = trade.recipientName,
            tradeType = trade.tradeType,
            finalStatus = trade.status,
            itemsSentJson = json.encodeToString(sent),
            itemsReceivedJson = json.encodeToString(received),
            sugarPointsTransferred = trade.senderSugarPoints - trade.recipientSugarPoints,
            wasGift = trade.isGift,
            wasAnonymous = trade.isAnonymous
        )
        _tradeHistory.value = listOf(entry) + _tradeHistory.value
        persistHistory()
    }
    
    private fun sendNotification(
        userId: String,
        type: TradeNotificationType,
        title: String,
        message: String,
        tradeId: String
    ) {
        val notification = TradeNotification(
            userId = userId,
            type = type,
            title = title,
            message = message,
            tradeId = tradeId
        )
        _notifications.value += notification
    }
    
    private fun getTradeStats(): TradeStats {
        // Load from persistence
        return TradeStats(userId = getCurrentUserId())
    }
    
    private fun updateTradeStats(update: (TradeStats) -> TradeStats) {
        val current = getTradeStats()
        val updated = update(current)
        // Persist updated stats
    }
    
    // ═════════════════════════════════════════════════════════════════
    // PERSISTENCE
    // ═════════════════════════════════════════════════════════════════
    
    private fun loadCachedData() {
        scope.launch {
            // Load from DataStore/Room
        }
    }
    
    private fun persistTrades() {
        scope.launch {
            // Save to persistence
        }
    }
    
    private fun persistHistory() {
        scope.launch {
            // Save to persistence
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // CLEANUP
    // ═════════════════════════════════════════════════════════════════
    
    private fun startCleanupWorker() {
        scope.launch {
            while (isActive) {
                delay(60 * 60 * 1000) // Every hour
                cleanupExpiredTrades()
            }
        }
    }
    
    private fun cleanupExpiredTrades() {
        val now = System.currentTimeMillis()
        val expired = _activeTrades.value.filter { 
            it.status == TradeStatus.PENDING && it.expiresAt < now 
        }
        
        expired.forEach { trade ->
            val senderItems = json.decodeFromString<List<TradeItem>>(trade.senderItemsJson)
            returnReservedItems(senderItems)
            
            val updated = trade.copy(status = TradeStatus.EXPIRED)
            updateTradeOffer(updated)
            
            sendNotification(
                userId = trade.senderId,
                type = TradeNotificationType.OFFER_EXPIRED,
                title = "Trade Expired",
                message = "Your trade offer to ${trade.recipientName} expired",
                tradeId = trade.id
            )
        }
    }
}
