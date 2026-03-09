package com.sugarmunch.app.clan

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * CoopDiscoveryManager - Handles the "Multiplayer App Store" feature.
 * Clan members can create shared wishlists, collectively review apps,
 * and discover apps together.
 */
class CoopDiscoveryManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)

    // Data models for Co-op Discovery
    data class SharedAppWishlist(
        val id: String = UUID.randomUUID().toString(),
        val clanId: String,
        val appPackageName: String,
        val appName: String,
        val addedByUserId: String,
        val addedAt: Long = System.currentTimeMillis(),
        val upvotes: Int = 0,
        val downvotes: Int = 0,
        val notes: String = ""
    )

    data class ClanAppReview(
        val id: String = UUID.randomUUID().toString(),
        val clanId: String,
        val appPackageName: String,
        val appName: String,
        val reviewerUserId: String,
        val reviewerName: String,
        val rating: Float, // 1 to 5
        val reviewText: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _sharedWishlists = MutableStateFlow<List<SharedAppWishlist>>(emptyList())
    val sharedWishlists: StateFlow<List<SharedAppWishlist>> = _sharedWishlists.asStateFlow()

    private val _clanReviews = MutableStateFlow<List<ClanAppReview>>(emptyList())
    val clanReviews: StateFlow<List<ClanAppReview>> = _clanReviews.asStateFlow()

    init {
        // Initialization logic, load from DB
        scope.launch {
            // Simulated loading for now
        }
    }

    suspend fun addToSharedWishlist(
        clanId: String,
        appPackageName: String,
        appName: String,
        userId: String,
        notes: String = ""
    ) {
        val newItem = SharedAppWishlist(
            clanId = clanId,
            appPackageName = appPackageName,
            appName = appName,
            addedByUserId = userId,
            notes = notes
        )
        val currentList = _sharedWishlists.value.toMutableList()
        currentList.add(newItem)
        _sharedWishlists.value = currentList
        // TODO: Persist to DB
    }

    suspend fun voteOnWishlistApp(wishlistId: String, isUpvote: Boolean) {
        val currentList = _sharedWishlists.value.map { item ->
            if (item.id == wishlistId) {
                if (isUpvote) item.copy(upvotes = item.upvotes + 1)
                else item.copy(downvotes = item.downvotes + 1)
            } else {
                item
            }
        }
        _sharedWishlists.value = currentList
        // TODO: Persist to DB
    }

    suspend fun addClanReview(
        clanId: String,
        appPackageName: String,
        appName: String,
        userId: String,
        reviewerName: String,
        rating: Float,
        reviewText: String
    ) {
        val review = ClanAppReview(
            clanId = clanId,
            appPackageName = appPackageName,
            appName = appName,
            reviewerUserId = userId,
            reviewerName = reviewerName,
            rating = rating,
            reviewText = reviewText
        )
        val currentReviews = _clanReviews.value.toMutableList()
        currentReviews.add(review)
        _clanReviews.value = currentReviews
        // TODO: Persist to DB
    }

    suspend fun getWishlistForClan(clanId: String): List<SharedAppWishlist> {
        return _sharedWishlists.value.filter { it.clanId == clanId }.sortedByDescending { it.upvotes }
    }

    suspend fun getReviewsForClan(clanId: String): List<ClanAppReview> {
        return _clanReviews.value.filter { it.clanId == clanId }.sortedByDescending { it.timestamp }
    }

    companion object {
        @Volatile
        private var instance: CoopDiscoveryManager? = null

        fun getInstance(context: Context): CoopDiscoveryManager {
            return instance ?: synchronized(this) {
                instance ?: CoopDiscoveryManager(context.applicationContext).also {
                    instance = it
                }
            }
        }

        fun destroy() {
            instance = null
        }
    }
}
