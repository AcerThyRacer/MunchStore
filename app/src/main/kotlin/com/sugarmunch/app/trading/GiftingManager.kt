package com.sugarmunch.app.trading

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════
 * SUGARMUNCH GIFTING MANAGER
 * 
 * Send gifts to friends with style! Features:
 * • Beautiful gift wrapping options
 * • Custom messages
 * • Anonymous gifting
 * • Daily gift limits
 * • Gift opening animations
 * ═══════════════════════════════════════════════════════════════════
 */

private val Context.giftingDataStore by preferencesDataStore(name = "gifting_prefs")

class GiftingManager private constructor(private val context: Context) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }
    private val dataStore = context.giftingDataStore
    
    // Dependencies
    private val tradeManager = TradeManager.getInstance(context)
    
    // State flows
    private val _receivedGifts = MutableStateFlow<List<TradeOffer>>(emptyList())
    private val _sentGifts = MutableStateFlow<List<TradeOffer>>(emptyList())
    private val _unopenedGiftCount = MutableStateFlow(0)
    private val _todaysGiftCount = MutableStateFlow(0)
    private val _remainingGiftsToday = MutableStateFlow(DAILY_GIFT_LIMIT)
    
    val receivedGifts: StateFlow<List<TradeOffer>> = _receivedGifts.asStateFlow()
    val sentGifts: StateFlow<List<TradeOffer>> = _sentGifts.asStateFlow()
    val unopenedGiftCount: StateFlow<Int> = _unopenedGiftCount.asStateFlow()
    val remainingGiftsToday: StateFlow<Int> = _remainingGiftsToday.asStateFlow()
    
    // Gift opening animation state
    private val _currentOpeningGift = MutableStateFlow<TradeOffer?>(null)
    private val _openingProgress = MutableStateFlow(0f)
    val currentOpeningGift: StateFlow<TradeOffer?> = _currentOpeningGift.asStateFlow()
    val openingProgress: StateFlow<Float> = _openingProgress.asStateFlow()
    
    companion object {
        @Volatile
        private var INSTANCE: GiftingManager? = null
        
        fun getInstance(context: Context): GiftingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GiftingManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
        
        // Gift messages suggestions
        val GIFT_MESSAGE_SUGGESTIONS = listOf(
            "Thought you'd like this! 🎁",
            "Thanks for being awesome! 💕",
            "A little something for you! ✨",
            "Surprise! Hope you like it 🎉",
            "Just because you're sweet 🍬",
            "Happy gaming! 🎮",
            "You're the best! 🌟",
            "Sharing the sugar! 🍭",
            "This made me think of you 💭",
            "From my collection to yours 🎨"
        )
        
        // Special occasion messages
        val OCCASION_MESSAGES = mapOf(
            "birthday" to listOf(
                "Happy Birthday! 🎂🎈",
                "Make a wish! 🕯️",
                "Another year sweeter! 🎉"
            ),
            "friendship" to listOf(
                "Best friends forever! 👯",
                "Thanks for being you! 💝",
                "Friendship is sweet! 🍯"
            ),
            "achievement" to listOf(
                "Congrats on your achievement! 🏆",
                "You earned this! 🌟",
                "Celebrating your success! 🎊"
            ),
            "random" to listOf(
                "Random act of kindness! 💫",
                "Just spreading joy! 🌈",
                "You deserve this! 💎"
            )
        )
    }
    
    init {
        loadGiftData()
        resetDailyGiftCountIfNeeded()
    }
    
    // ═════════════════════════════════════════════════════════════════
    // SEND GIFT
    // ═════════════════════════════════════════════════════════════════
    
    /**
     * Send a gift to a friend
     */
    suspend fun sendGift(
        recipientId: String,
        recipientName: String,
        items: List<TradeItem>,
        sugarPoints: Int = 0,
        message: String? = null,
        wrapping: GiftWrapping = GiftWrapping.CLASSIC,
        isAnonymous: Boolean = false
    ): GiftResult {
        // Check daily limit
        if (_todaysGiftCount.value >= DAILY_GIFT_LIMIT) {
            return GiftResult.DailyLimitReached
        }
        
        // Validate
        if (items.isEmpty() && sugarPoints == 0) {
            return GiftResult.Invalid("Gift must contain at least one item or Sugar Points")
        }
        
        // Create gift via TradeManager
        val result = tradeManager.createGiftOffer(
            recipientId = recipientId,
            recipientName = recipientName,
            items = items,
            sugarPoints = sugarPoints,
            message = message,
            wrapping = wrapping,
            isAnonymous = isAnonymous
        )
        
        return when (result) {
            is TradeResult.Success -> {
                incrementDailyGiftCount()
                GiftResult.Success
            }
            is TradeResult.Failure -> GiftResult.Error(result.error)
        }
    }
    
    /**
     * Quick send a gift with default wrapping
     */
    suspend fun quickSendGift(
        recipientId: String,
        recipientName: String,
        item: TradeItem,
        message: String = GIFT_MESSAGE_SUGGESTIONS.random()
    ): GiftResult {
        return sendGift(
            recipientId = recipientId,
            recipientName = recipientName,
            items = listOf(item),
            message = message,
            wrapping = GiftWrapping.CLASSIC,
            isAnonymous = false
        )
    }
    
    /**
     * Send an anonymous mystery gift
     */
    suspend fun sendMysteryGift(
        recipientId: String,
        recipientName: String,
        items: List<TradeItem>,
        message: String = "Someone thinks you're special... 🎭"
    ): GiftResult {
        return sendGift(
            recipientId = recipientId,
            recipientName = recipientName,
            items = items,
            message = message,
            wrapping = GiftWrapping.MYSTERIOUS,
            isAnonymous = true
        )
    }
    
    // ═════════════════════════════════════════════════════════════════
    // OPEN GIFT
    // ═════════════════════════════════════════════════════════════════
    
    /**
     * Start opening a gift (triggers animation)
     */
    fun startOpeningGift(giftId: String) {
        val gift = _receivedGifts.value.find { it.id == giftId }
        gift?.let {
            _currentOpeningGift.value = it
            _openingProgress.value = 0f
        }
    }
    
    /**
     * Update opening progress (0.0 - 1.0)
     */
    fun updateOpeningProgress(progress: Float) {
        _openingProgress.value = progress.coerceIn(0f, 1f)
    }
    
    /**
     * Complete opening a gift
     */
    suspend fun openGift(giftId: String): GiftOpenResult {
        val result = tradeManager.openGift(giftId)
        
        _currentOpeningGift.value = null
        _openingProgress.value = 0f
        
        return when (result) {
            is TradeResult.Success -> {
                refreshGiftData()
                val gift = getGiftById(giftId)
                val items = gift?.senderItemsJson?.let {
                    json.decodeFromString<List<TradeItem>>(it)
                } ?: emptyList()
                
                GiftOpenResult.Success(
                    items = items,
                    sugarPoints = gift?.senderSugarPoints ?: 0,
                    wrapping = gift?.giftWrapping ?: GiftWrapping.CLASSIC,
                    message = gift?.giftMessage,
                    fromAnonymous = gift?.isAnonymous ?: false,
                    fromUser = if (gift?.isAnonymous == false) gift.senderName else null
                )
            }
            is TradeResult.Failure -> GiftOpenResult.Error(result.error)
        }
    }
    
    /**
     * Cancel opening animation
     */
    fun cancelOpening() {
        _currentOpeningGift.value = null
        _openingProgress.value = 0f
    }
    
    // ═════════════════════════════════════════════════════════════════
    // QUERIES
    // ═════════════════════════════════════════════════════════════════
    
    fun getReceivedGifts(includeOpened: Boolean = false): List<TradeOffer> {
        return _receivedGifts.value.filter { 
            if (includeOpened) true else it.status == TradeStatus.PENDING 
        }.sortedByDescending { it.createdAt }
    }
    
    fun getSentGifts(): List<TradeOffer> {
        return _sentGifts.value.sortedByDescending { it.createdAt }
    }
    
    fun getGiftById(giftId: String): TradeOffer? {
        return (_receivedGifts.value + _sentGifts.value).find { it.id == giftId }
    }
    
    fun getUnopenedGifts(): List<TradeOffer> {
        return _receivedGifts.value.filter { it.status == TradeStatus.PENDING }
    }
    
    fun hasUnopenedGifts(): Boolean {
        return _unopenedGiftCount.value > 0
    }
    
    fun canSendMoreGiftsToday(): Boolean {
        return _remainingGiftsToday.value > 0
    }
    
    // ═════════════════════════════════════════════════════════════════
    // GIFT WRAPPING UTILITIES
    // ═════════════════════════════════════════════════════════════════
    
    /**
     * Get wrapping recommendation based on item rarity
     */
    fun getWrappingRecommendation(items: List<TradeItem>): GiftWrapping {
        val hasLegendary = items.any { it.itemRarity == AchievementRarity.LEGENDARY }
        val hasEpic = items.any { it.itemRarity == AchievementRarity.EPIC }
        
        return when {
            hasLegendary -> GiftWrapping.GOLDEN
            hasEpic -> GiftWrapping.ELEGANT
            items.size > 3 -> GiftWrapping.FESTIVE
            else -> GiftWrapping.CLASSIC
        }
    }
    
    /**
     * Get wrapping color for UI
     */
    fun getWrappingColor(wrapping: GiftWrapping): String {
        return wrapping.colorHex
    }
    
    /**
     * Get message suggestions based on context
     */
    fun getMessageSuggestions(context: String = "general"): List<String> {
        return OCCASION_MESSAGES[context] ?: GIFT_MESSAGE_SUGGESTIONS
    }
    
    /**
     * Preview what a gift will look like
     */
    fun previewGift(
        items: List<TradeItem>,
        wrapping: GiftWrapping,
        message: String?,
        isAnonymous: Boolean
    ): GiftPreview {
        return GiftPreview(
            items = items,
            wrapping = wrapping,
            message = message,
            isAnonymous = isAnonymous,
            totalValue = items.sumOf { it.itemRarity.ordinal * 100 },
            estimatedRarity = if (items.isNotEmpty()) {
                items.maxByOrNull { it.itemRarity.ordinal }?.itemRarity ?: AchievementRarity.COMMON
            } else AchievementRarity.COMMON
        )
    }
    
    // ═════════════════════════════════════════════════════════════════
    // DAILY LIMITS
    // ═════════════════════════════════════════════════════════════════
    
    private suspend fun incrementDailyGiftCount() {
        dataStore.edit { prefs ->
            val current = prefs[GIFTS_SENT_TODAY] ?: 0
            val today = prefs[LAST_GIFT_DATE] ?: ""
            
            if (today != getTodayString()) {
                // New day, reset
                prefs[GIFTS_SENT_TODAY] = 1
                prefs[LAST_GIFT_DATE] = getTodayString()
            } else {
                prefs[GIFTS_SENT_TODAY] = current + 1
            }
        }
        updateGiftCounts()
    }
    
    private fun resetDailyGiftCountIfNeeded() {
        scope.launch {
            dataStore.edit { prefs ->
                val lastDate = prefs[LAST_GIFT_DATE] ?: ""
                if (lastDate != getTodayString()) {
                    prefs[GIFTS_SENT_TODAY] = 0
                    prefs[LAST_GIFT_DATE] = getTodayString()
                }
            }
            updateGiftCounts()
        }
    }
    
    private suspend fun updateGiftCounts() {
        dataStore.data.collect { prefs ->
            val sent = prefs[GIFTS_SENT_TODAY] ?: 0
            _todaysGiftCount.value = sent
            _remainingGiftsToday.value = (DAILY_GIFT_LIMIT - sent).coerceAtLeast(0)
        }
    }
    
    private fun loadGiftData() {
        scope.launch {
            // Load from TradeManager
            val allTrades = tradeManager.activeTrades.value
            _receivedGifts.value = allTrades.filter { 
                it.recipientId == getCurrentUserId() && it.tradeType == TradeType.GIFT 
            }
            _sentGifts.value = allTrades.filter { 
                it.senderId == getCurrentUserId() && it.tradeType == TradeType.GIFT 
            }
            _unopenedGiftCount.value = _receivedGifts.value.count { it.status == TradeStatus.PENDING }
        }
    }
    
    private fun refreshGiftData() {
        loadGiftData()
    }
    
    private fun getCurrentUserId(): String {
        return "current_user_id"
    }
    
    private fun getTodayString(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
    }
    
    // DataStore keys
    private val GIFTS_SENT_TODAY = intPreferencesKey("gifts_sent_today")
    private val LAST_GIFT_DATE = stringPreferencesKey("last_gift_date")
}

// ═══════════════════════════════════════════════════════════════════
// RESULT TYPES
// ═══════════════════════════════════════════════════════════════════

sealed class GiftResult {
    data object Success : GiftResult()
    data class DailyLimitReached : GiftResult()
    data class Invalid(val reason: String) : GiftResult()
    data class Error(val error: TradeError) : GiftResult()
}

sealed class GiftOpenResult {
    data class Success(
        val items: List<TradeItem>,
        val sugarPoints: Int,
        val wrapping: GiftWrapping,
        val message: String?,
        val fromAnonymous: Boolean,
        val fromUser: String?
    ) : GiftOpenResult()
    
    data class Error(val error: TradeError) : GiftOpenResult()
}

/**
 * Gift preview for UI
 */
data class GiftPreview(
    val items: List<TradeItem>,
    val wrapping: GiftWrapping,
    val message: String?,
    val isAnonymous: Boolean,
    val totalValue: Int,
    val estimatedRarity: AchievementRarity
)
