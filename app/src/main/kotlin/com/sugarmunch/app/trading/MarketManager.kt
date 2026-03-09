package com.sugarmunch.app.trading

import android.content.Context
import com.sugarmunch.app.auth.AuthManager
import com.sugarmunch.app.features.model.AchievementRarity
import com.sugarmunch.app.shop.ShopItemType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════
 * SUGARMUNCH MARKET MANAGER
 *
 * Community marketplace for trading!
 *
 * Features:
 * • Browse trade listings from the community
 * • Create your own listings (Have/Want)
 * • Search and filter by category, rarity
 * • Make offers on listings
 * • My listings management
 * ═══════════════════════════════════════════════════════════════════
 * 
 * Note for Hilt migration: Use @ApplicationContext for the Context parameter
 * and inject AuthManager as a dependency
 */

class MarketManager constructor(
    private val context: Context,
    private val authManager: AuthManager
) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    // Dependencies
    // Note: For Hilt migration, inject TradeManager instead of creating it here
    private val tradeManager = TradeManager(context, authManager)
    
    // State flows
    private val _allListings = MutableStateFlow<List<TradeListing>>(emptyList())
    private val _myListings = MutableStateFlow<List<TradeListing>>(emptyList())
    private val _filteredListings = MutableStateFlow<List<TradeListing>>(emptyList())
    private val _myRequests = MutableStateFlow<List<TradeRequest>>(emptyList())
    private val _incomingRequests = MutableStateFlow<List<TradeRequest>>(emptyList())
    
    val allListings: StateFlow<List<TradeListing>> = _allListings.asStateFlow()
    val myListings: StateFlow<List<TradeListing>> = _myListings.asStateFlow()
    val filteredListings: StateFlow<List<TradeListing>> = _filteredListings.asStateFlow()
    val myRequests: StateFlow<List<TradeRequest>> = _myRequests.asStateFlow()
    val incomingRequests: StateFlow<List<TradeRequest>> = _incomingRequests.asStateFlow()
    
    // Active filters
    private val _currentFilter = MutableStateFlow(MarketFilter())
    val currentFilter: StateFlow<MarketFilter> = _currentFilter.asStateFlow()
    
    companion object {
        // Maximum active listings per user
        const val MAX_ACTIVE_LISTINGS = 5

        // Listing categories
        val CATEGORIES = listOf(
            null to "All",
            ShopItemType.THEME to "Themes",
            ShopItemType.EFFECT to "Effects",
            ShopItemType.BADGE to "Badges",
            ShopItemType.ICON to "Icons",
            ShopItemType.BOOST to "Boosts"
        )

        // Sort options
        val SORT_OPTIONS = listOf(
            MarketSort.NEWEST,
            MarketSort.OLDEST,
            MarketSort.HIGHEST_VALUE,
            MarketSort.LOWEST_VALUE,
            MarketSort.MOST_VIEWED,
            MarketSort.EXPIRING_SOON
        )
        
        // Listing expiry in days
        const val LISTING_EXPIRY_DAYS = 7
    }
    
    init {
        loadListings()
        startListingCleanup()
    }
    
    // ═════════════════════════════════════════════════════════════════
    // LISTING MANAGEMENT
    // ═════════════════════════════════════════════════════════════════
    
    /**
     * Create a new marketplace listing
     */
    suspend fun createListing(
        title: String,
        description: String,
        listingType: ListingType,
        haveItems: List<TradeItem>,
        wantItems: List<TradeItem>? = null,
        wantSugarPoints: Int? = null,
        category: ShopItemType? = null,
        rarity: AchievementRarity? = null
    ): ListingResult {
        // Check listing limit
        if (_myListings.value.count { it.isActive && !it.isExpired } >= MAX_ACTIVE_LISTINGS) {
            return ListingResult.LimitReached
        }
        
        // Validate items
        if (haveItems.isEmpty()) {
            return ListingResult.Invalid("Must offer at least one item")
        }
        
        // Verify ownership
        haveItems.forEach { item ->
            if (!ownsItem(item)) {
                return ListingResult.Invalid("You don't own: ${item.itemName}")
            }
        }
        
        val listing = TradeListing(
            creatorId = getCurrentUserId(),
            creatorName = getCurrentUserName(),
            listingType = listingType,
            title = title,
            description = description,
            haveItemsJson = json.encodeToString(haveItems),
            wantItemsJson = wantItems?.let { json.encodeToString(it) },
            wantSugarPoints = wantSugarPoints,
            categoryFilter = category,
            rarityFilter = rarity
        )
        
        _allListings.value += listing
        _myListings.value += listing
        applyFilter()
        
        // Simulate server sync
        simulateServerSync()
        
        return ListingResult.Success(listing.id)
    }
    
    /**
     * Quick create a "Have/Want" listing
     */
    suspend fun quickCreateHaveWant(
        haveItem: TradeItem,
        wantItem: TradeItem
    ): ListingResult {
        return createListing(
            title = "Trading ${haveItem.itemName}",
            description = "Looking for ${wantItem.itemName} in exchange",
            listingType = ListingType.HAVE_WANT,
            haveItems = listOf(haveItem),
            wantItems = listOf(wantItem),
            category = haveItem.itemType
        )
    }
    
    /**
     * Create a "looking for" (WTB) listing
     */
    suspend fun createWantToBuy(
        wantItem: TradeItem,
        maxSugarPoints: Int
    ): ListingResult {
        return createListing(
            title = "WTB: ${wantItem.itemName}",
            description = "Looking to buy with Sugar Points",
            listingType = ListingType.WANT_BUY,
            haveItems = emptyList(), // Will pay with Sugar Points
            wantItems = listOf(wantItem),
            wantSugarPoints = maxSugarPoints,
            category = wantItem.itemType,
            rarity = wantItem.itemRarity
        )
    }
    
    /**
     * Deactivate a listing
     */
    fun deactivateListing(listingId: String) {
        updateListing(listingId) { it.copy(isActive = false) }
    }
    
    /**
     * Renew/extend a listing
     */
    fun renewListing(listingId: String): Boolean {
        val listing = _myListings.value.find { it.id == listingId } ?: return false
        
        if (listing.isExpired) {
            updateListing(listingId) { 
                it.copy(
                    expiresAt = System.currentTimeMillis() + (LISTING_EXPIRY_DAYS * 24 * 60 * 60 * 1000),
                    isActive = true
                )
            }
            return true
        }
        return false
    }
    
    /**
     * Delete a listing
     */
    fun deleteListing(listingId: String) {
        _allListings.value = _allListings.value.filter { it.id != listingId }
        _myListings.value = _myListings.value.filter { it.id != listingId }
        applyFilter()
    }
    
    // ═════════════════════════════════════════════════════════════════
    // BROWSING & SEARCH
    // ═════════════════════════════════════════════════════════════════
    
    /**
     * Browse all active listings
     */
    fun browseListings(
        category: ShopItemType? = null,
        listingType: ListingType? = null,
        rarity: AchievementRarity? = null,
        sortBy: MarketSort = MarketSort.NEWEST
    ): List<TradeListing> {
        var listings = _allListings.value.filter { it.isActive && !it.isExpired }
        
        // Apply filters
        category?.let { cat ->
            listings = listings.filter { it.categoryFilter == cat }
        }
        listingType?.let { type ->
            listings = listings.filter { it.listingType == type }
        }
        rarity?.let { r ->
            listings = listings.filter { it.rarityFilter == r }
        }
        
        // Apply sort
        listings = when (sortBy) {
            MarketSort.NEWEST -> listings.sortedByDescending { it.createdAt }
            MarketSort.OLDEST -> listings.sortedBy { it.createdAt }
            MarketSort.HIGHEST_VALUE -> listings.sortedByDescending { estimateListingValue(it) }
            MarketSort.LOWEST_VALUE -> listings.sortedBy { estimateListingValue(it) }
            MarketSort.MOST_VIEWED -> listings.sortedByDescending { it.viewCount }
            MarketSort.EXPIRING_SOON -> listings.sortedBy { it.expiresAt }
        }
        
        return listings
    }
    
    /**
     * Search listings by text
     */
    fun searchListings(query: String): List<TradeListing> {
        val lowerQuery = query.lowercase()
        return _allListings.value.filter { listing ->
            listing.isActive && !listing.isExpired && (
                listing.title.lowercase().contains(lowerQuery) ||
                listing.description.lowercase().contains(lowerQuery) ||
                listing.haveItemsJson.contains(lowerQuery, ignoreCase = true)
            )
        }
    }
    
    /**
     * Apply filter and update filtered listings
     */
    fun applyFilter(filter: MarketFilter = _currentFilter.value) {
        _currentFilter.value = filter
        
        var listings = _allListings.value.filter { it.isActive && !it.isExpired }
        
        filter.category?.let { listings = listings.filter { it.categoryFilter == it } }
        filter.listingType?.let { listings = listings.filter { it.listingType == it } }
        filter.rarity?.let { listings = listings.filter { it.rarityFilter == it } }
        filter.maxSugarPoints?.let { max ->
            listings = listings.filter { 
                it.wantSugarPoints == null || it.wantSugarPoints <= max 
            }
        }
        
        listings = when (filter.sortBy) {
            MarketSort.NEWEST -> listings.sortedByDescending { it.createdAt }
            MarketSort.OLDEST -> listings.sortedBy { it.createdAt }
            MarketSort.HIGHEST_VALUE -> listings.sortedByDescending { estimateListingValue(it) }
            MarketSort.LOWEST_VALUE -> listings.sortedBy { estimateListingValue(it) }
            MarketSort.MOST_VIEWED -> listings.sortedByDescending { it.viewCount }
            MarketSort.EXPIRING_SOON -> listings.sortedBy { it.expiresAt }
        }
        
        _filteredListings.value = listings
    }
    
    /**
     * Get a single listing by ID
     */
    fun getListing(listingId: String): TradeListing? {
        return _allListings.value.find { it.id == listingId }
    }
    
    /**
     * Increment view count for a listing
     */
    fun viewListing(listingId: String) {
        updateListing(listingId) { 
            it.copy(viewCount = it.viewCount + 1) 
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // TRADE REQUESTS
    // ═════════════════════════════════════════════════════════════════
    
    /**
     * Request a trade for a listing
     */
    suspend fun requestTrade(
        listingId: String,
        offeredItems: List<TradeItem>,
        offeredSugarPoints: Int = 0,
        message: String? = null
    ): RequestResult {
        val listing = getListing(listingId) ?: return RequestResult.ListingNotFound
        
        if (!listing.isActive || listing.isExpired) {
            return RequestResult.ListingInactive
        }
        
        if (listing.creatorId == getCurrentUserId()) {
            return RequestResult.CannotTradeOwnListing
        }
        
        // Validate offer against listing requirements
        val wantItems = listing.wantItemsJson?.let {
            json.decodeFromString<List<TradeItem>>(it)
        }
        
        // Check if offer matches requirements
        if (wantItems != null && offeredItems.isNotEmpty()) {
            // Verify offered items match wanted items
            val matches = wantItems.all { wanted ->
                offeredItems.any { it.itemId == wanted.itemId }
            }
            if (!matches) {
                return RequestResult.InvalidOffer("Offer doesn't match requested items")
            }
        }
        
        // Check Sugar Points requirement
        if (listing.wantSugarPoints != null && offeredSugarPoints < listing.wantSugarPoints) {
            return RequestResult.InvalidOffer("Not enough Sugar Points offered")
        }
        
        val request = TradeRequest(
            listingId = listingId,
            requesterId = getCurrentUserId(),
            requesterName = getCurrentUserName(),
            offeredItemsJson = json.encodeToString(offeredItems),
            offeredSugarPoints = offeredSugarPoints,
            message = message
        )
        
        _myRequests.value += request
        
        // Notify listing creator
        sendListingOfferNotification(listing.creatorId, listingId, getCurrentUserName())
        
        // Update listing offer count
        updateListing(listingId) { 
            it.copy(offerCount = it.offerCount + 1) 
        }
        
        return RequestResult.Success(request.id)
    }
    
    /**
     * Accept a trade request for your listing
     */
    suspend fun acceptRequest(requestId: String): RequestResult {
        val request = _incomingRequests.value.find { it.id == requestId }
            ?: return RequestResult.RequestNotFound
        
        val listing = getListing(request.listingId)
            ?: return RequestResult.ListingNotFound
        
        // Execute the trade via TradeManager
        val haveItems = json.decodeFromString<List<TradeItem>>(listing.haveItemsJson)
        val offeredItems = json.decodeFromString<List<TradeItem>>(request.offeredItemsJson)
        
        // Create trade offer
        val result = tradeManager.createOffer(
            recipientId = request.requesterId,
            recipientName = request.requesterName,
            senderItems = haveItems,
            recipientItems = offeredItems,
            senderSugarPoints = listing.wantSugarPoints ?: 0,
            message = request.message ?: "Trade from marketplace listing"
        )
        
        return when (result) {
            is TradeResult.Success -> {
                updateRequest(requestId) { it.copy(status = RequestStatus.ACCEPTED) }
                deactivateListing(listing.id)
                RequestResult.Success(requestId)
            }
            is TradeResult.Failure -> RequestResult.TradeFailed(result.error)
        }
    }
    
    /**
     * Decline a trade request
     */
    fun declineRequest(requestId: String, reason: String? = null) {
        updateRequest(requestId) { 
            it.copy(
                status = RequestStatus.DECLINED,
                respondedAt = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Get requests for a specific listing
     */
    fun getRequestsForListing(listingId: String): List<TradeRequest> {
        return (_myRequests.value + _incomingRequests.value)
            .filter { it.listingId == listingId }
            .sortedByDescending { it.createdAt }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═════════════════════════════════════════════════════════════════
    
    /**
     * Estimate the value of a listing
     */
    fun estimateListingValue(listing: TradeListing): Int {
        val haveItems = json.decodeFromString<List<TradeItem>>(listing.haveItemsJson)
        return haveItems.sumOf { item ->
            item.itemRarity.ordinal * 100 * item.quantity
        }
    }
    
    /**
     * Get recommended listings based on user's inventory
     */
    fun getRecommendedListings(userItems: List<TradeItem>): List<TradeListing> {
        val userItemIds = userItems.map { it.itemId }.toSet()
        
        return _allListings.value.filter { listing ->
            listing.isActive && !listing.isExpired &&
            listing.listingType == ListingType.HAVE_WANT
        }.filter { listing ->
            // Recommend if listing wants something user has
            val wantItems = listing.wantItemsJson?.let {
                json.decodeFromString<List<TradeItem>>(it)
            } ?: emptyList()
            
            wantItems.any { it.itemId in userItemIds }
        }.take(5)
    }
    
    /**
     * Check if user can create more listings
     */
    fun canCreateListing(): Boolean {
        return _myListings.value.count { it.isActive && !it.isExpired } < MAX_ACTIVE_LISTINGS
    }
    
    // ═════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═════════════════════════════════════════════════════════════════
    
    private fun updateListing(listingId: String, update: (TradeListing) -> TradeListing) {
        _allListings.value = _allListings.value.map { 
            if (it.id == listingId) update(it) else it 
        }
        _myListings.value = _myListings.value.map { 
            if (it.id == listingId) update(it) else it 
        }
        applyFilter()
    }
    
    private fun updateRequest(requestId: String, update: (TradeRequest) -> TradeRequest) {
        _myRequests.value = _myRequests.value.map {
            if (it.id == requestId) update(it) else it
        }
        _incomingRequests.value = _incomingRequests.value.map {
            if (it.id == requestId) update(it) else it
        }
    }

    private fun ownsItem(item: TradeItem): Boolean {
        // Check inventory via ShopManager
        val shopManager = ShopManager.getInstance(context)
        return when (item) {
            is TradeItem.Theme -> shopManager.isItemPurchased(item.themeId)
            is TradeItem.Effect -> shopManager.isItemPurchased(item.effectId)
            is TradeItem.Badge -> shopManager.isItemPurchased(item.badgeId)
            is TradeItem.Icon -> shopManager.isItemPurchased(item.iconId)
            is TradeItem.Boost -> false // Boosts are consumed immediately
            is TradeItem.SugarPoints -> false // Sugar points handled separately
        }
    }
    
    private fun sendListingOfferNotification(creatorId: String, listingId: String, requesterName: String) {
        // Send push notification
    }
    
    private fun simulateServerSync() {
        // Simulate network call
    }
    
    private fun loadListings() {
        scope.launch {
            // Load from server/cache
            // For now, start empty
            _allListings.value = emptyList()
            _myListings.value = emptyList()
        }
    }
    
    private fun startListingCleanup() {
        scope.launch {
            while (isActive) {
                delay(60 * 60 * 1000) // Every hour
                cleanupExpiredListings()
            }
        }
    }
    
    private fun cleanupExpiredListings() {
        val now = System.currentTimeMillis()
        _allListings.value = _allListings.value.map { listing ->
            if (listing.expiresAt < now && listing.isActive) {
                listing.copy(isActive = false)
            } else listing
        }
        _myListings.value = _myListings.value.map { listing ->
            if (listing.expiresAt < now && listing.isActive) {
                listing.copy(isActive = false)
            } else listing
        }
    }
    
    private fun getCurrentUserId(): String = "current_user_id"
    private fun getCurrentUserName(): String = "Current User"
}

// ═══════════════════════════════════════════════════════════════════
// FILTER & SORT
// ═══════════════════════════════════════════════════════════════════

data class MarketFilter(
    val category: ShopItemType? = null,
    val listingType: ListingType? = null,
    val rarity: AchievementRarity? = null,
    val maxSugarPoints: Int? = null,
    val sortBy: MarketSort = MarketSort.NEWEST,
    val searchQuery: String? = null
)

enum class MarketSort {
    NEWEST,
    OLDEST,
    HIGHEST_VALUE,
    LOWEST_VALUE,
    MOST_VIEWED,
    EXPIRING_SOON
}

// ═══════════════════════════════════════════════════════════════════
// RESULT TYPES
// ═══════════════════════════════════════════════════════════════════

sealed class ListingResult {
    data class Success(val listingId: String) : ListingResult()
    data object LimitReached : ListingResult()
    data class Invalid(val reason: String) : ListingResult()
}

sealed class RequestResult {
    data class Success(val requestId: String) : RequestResult()
    data object ListingNotFound : RequestResult()
    data object ListingInactive : RequestResult()
    data object CannotTradeOwnListing : RequestResult()
    data object RequestNotFound : RequestResult()
    data class InvalidOffer(val reason: String) : RequestResult()
    data class TradeFailed(val error: TradeError) : RequestResult()
}
