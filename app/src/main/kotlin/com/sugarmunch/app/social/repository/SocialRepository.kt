package com.sugarmunch.app.social.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.social.model.*
import com.sugarmunch.app.util.CoroutineUtils.safeCollect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.socialDataStore: DataStore<Preferences> by preferencesDataStore(name = "social")

/**
 * Social Repository - Manages all social features data
 */
@Singleton
class SocialRepository @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.socialDataStore
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        val CURRENT_USER_KEY = stringPreferencesKey("current_user")
        val FOLLOWING_KEY = stringPreferencesKey("following")
        val FOLLOWERS_KEY = stringPreferencesKey("followers")
        val ACTIVITIES_KEY = stringPreferencesKey("activities")
        val NOTIFICATIONS_KEY = stringPreferencesKey("notifications")
        val SHARED_THEMES_KEY = stringPreferencesKey("shared_themes")
        val SHARED_EFFECTS_KEY = stringPreferencesKey("shared_effects")
        val COLLECTIONS_KEY = stringPreferencesKey("collections")
    }

    // ═════════════════════════════════════════════════════════════
    // USER PROFILE
    // ═════════════════════════════════════════════════════════════

    fun getCurrentUser(): Flow<UserProfile?> {
        return dataStore.data.map { prefs ->
            val jsonStr = prefs[CURRENT_USER_KEY]
            jsonStr?.let { json.decodeFromString<UserProfile>(it) }
        }.safeCollect()
    }

    suspend fun setCurrentUser(user: UserProfile) {
        dataStore.edit { prefs ->
            prefs[CURRENT_USER_KEY] = json.encodeToString(user)
        }
    }

    suspend fun updateUserStats(stats: UserStats) {
        val user = getCurrentUser().first() ?: return
        setCurrentUser(user.copy(stats = stats))
    }

    suspend fun addSugarPoints(points: Int, reason: String) {
        val user = getCurrentUser().first() ?: return
        val newStats = user.stats.copy(
            sugarPoints = user.stats.sugarPoints + points
        )
        setCurrentUser(user.copy(stats = newStats))
    }

    suspend fun addXP(xp: Int) {
        val user = getCurrentUser().first() ?: return
        val currentXP = user.stats.xp + xp
        val newXpToNextLevel = user.stats.level * 1000
        val newLevel = if (currentXP >= newXpToNextLevel) {
            user.stats.level + 1
        } else user.stats.level

        val newStats = user.stats.copy(
            xp = currentXP % newXpToNextLevel,
            level = newLevel
        )
        setCurrentUser(user.copy(stats = newStats))
    }

    // ═════════════════════════════════════════════════════════════
    // FOLLOW SYSTEM
    // ═════════════════════════════════════════════════════════════

    fun getFollowing(): Flow<List<FollowRelationship>> {
        return dataStore.data.map { prefs ->
            val jsonStr = prefs[FOLLOWING_KEY] ?: "[]"
            json.decodeFromString(jsonStr)
        }.safeCollect()
    }

    fun getFollowers(): Flow<List<FollowRelationship>> {
        return dataStore.data.map { prefs ->
            val jsonStr = prefs[FOLLOWERS_KEY] ?: "[]"
            json.decodeFromString(jsonStr)
        }.safeCollect()
    }

    suspend fun followUser(userId: String) {
        val following = getFollowing().first().toMutableList()
        following.add(
            FollowRelationship(
                followerId = getCurrentUser().first()?.id ?: "",
                followingId = userId,
                status = FollowStatus.ACCEPTED
            )
        )
        dataStore.edit { prefs ->
            prefs[FOLLOWING_KEY] = json.encodeToString(following)
        }
    }

    suspend fun unfollowUser(userId: String) {
        val following = getFollowing().first().filter { it.followingId != userId }
        dataStore.edit { prefs ->
            prefs[FOLLOWING_KEY] = json.encodeToString(following)
        }
    }

    suspend fun isFollowing(userId: String): Boolean {
        return getFollowing().first().any { it.followingId == userId }
    }

    // ═════════════════════════════════════════════════════════════
    // ACTIVITY FEED
    // ═════════════════════════════════════════════════════════════

    fun getActivityFeed(): Flow<List<SocialActivity>> {
        return dataStore.data.map { prefs ->
            val jsonStr = prefs[ACTIVITIES_KEY] ?: "[]"
            json.decodeFromString(jsonStr)
        }.safeCollect()
    }

    suspend fun logActivity(
        type: ActivityType,
        targetType: TargetType,
        targetId: String,
        targetName: String,
        metadata: Map<String, String> = emptyMap()
    ) {
        val user = getCurrentUser().first() ?: return
        val activities = getActivityFeed().first().toMutableList()
        
        val newActivity = SocialActivity(
            id = "activity_${System.currentTimeMillis()}",
            userId = user.id,
            user = user,
            type = type,
            targetType = targetType,
            targetId = targetId,
            targetName = targetName,
            metadata = metadata
        )
        
        activities.add(0, newActivity)
        
        // Keep only last 100 activities
        if (activities.size > 100) {
            activities.removeAll { activities.size > 100 }
        }
        
        dataStore.edit { prefs ->
            prefs[ACTIVITIES_KEY] = json.encodeToString(activities)
        }
    }

    suspend fun likeActivity(activityId: String) {
        val activities = getActivityFeed().first().toMutableList()
        val index = activities.indexOfFirst { it.id == activityId }
        if (index >= 0) {
            activities[index] = activities[index].copy(
                likesCount = activities[index].likesCount + 1,
                isLiked = true
            )
            dataStore.edit { prefs ->
                prefs[ACTIVITIES_KEY] = json.encodeToString(activities)
            }
        }
    }

    // ═════════════════════════════════════════════════════════════
    // NOTIFICATIONS
    // ═════════════════════════════════════════════════════════════

    fun getNotifications(): Flow<List<SocialNotification>> {
        return dataStore.data.map { prefs ->
            val jsonStr = prefs[NOTIFICATIONS_KEY] ?: "[]"
            json.decodeFromString(jsonStr)
        }.safeCollect()
    }

    suspend fun addNotification(notification: SocialNotification) {
        val notifications = getNotifications().first().toMutableList()
        notifications.add(0, notification)
        
        // Keep only last 50 notifications
        if (notifications.size > 50) {
            notifications.removeAll { notifications.size > 50 }
        }
        
        dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_KEY] = json.encodeToString(notifications)
        }
    }

    suspend fun markNotificationRead(notificationId: String) {
        val notifications = getNotifications().first().map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
        dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_KEY] = json.encodeToString(notifications)
        }
    }

    suspend fun markAllNotificationsRead() {
        val notifications = getNotifications().first().map {
            it.copy(isRead = true)
        }
        dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_KEY] = json.encodeToString(notifications)
        }
    }

    suspend fun clearNotifications() {
        dataStore.edit { prefs ->
            prefs.remove(NOTIFICATIONS_KEY)
        }
    }

    // ═════════════════════════════════════════════════════════════
    // COMMUNITY CONTENT
    // ═════════════════════════════════════════════════════════════

    fun getSharedThemes(): Flow<List<CommunityContent>> {
        return dataStore.data.map { prefs ->
            val jsonStr = prefs[SHARED_THEMES_KEY] ?: "[]"
            json.decodeFromString(jsonStr)
        }.safeCollect()
    }

    fun getSharedEffects(): Flow<List<CommunityContent>> {
        return dataStore.data.map { prefs ->
            val jsonStr = prefs[SHARED_EFFECTS_KEY] ?: "[]"
            json.decodeFromString(jsonStr)
        }.safeCollect()
    }

    suspend fun shareTheme(content: CommunityContent) {
        val themes = getSharedThemes().first().toMutableList()
        themes.add(0, content)
        dataStore.edit { prefs ->
            prefs[SHARED_THEMES_KEY] = json.encodeToString(themes)
        }
    }

    suspend fun shareEffect(content: CommunityContent) {
        val effects = getSharedEffects().first().toMutableList()
        effects.add(0, content)
        dataStore.edit { prefs ->
            prefs[SHARED_EFFECTS_KEY] = json.encodeToString(effects)
        }
    }

    suspend fun likeContent(contentId: String, contentType: ContentType) {
        when (contentType) {
            ContentType.THEME -> {
                val themes = getSharedThemes().first().map {
                    if (it.id == contentId) it.copy(likesCount = it.likesCount + 1) else it
                }
                dataStore.edit { prefs ->
                    prefs[SHARED_THEMES_KEY] = json.encodeToString(themes)
                }
            }
            ContentType.EFFECT -> {
                val effects = getSharedEffects().first().map {
                    if (it.id == contentId) it.copy(likesCount = it.likesCount + 1) else it
                }
                dataStore.edit { prefs ->
                    prefs[SHARED_EFFECTS_KEY] = json.encodeToString(effects)
                }
            }
            else -> {}
        }
    }

    // ═════════════════════════════════════════════════════════════
    // COLLECTIONS
    // ═════════════════════════════════════════════════════════════

    fun getCollections(): Flow<List<AppCollection>> {
        return dataStore.data.map { prefs ->
            val jsonStr = prefs[COLLECTIONS_KEY] ?: "[]"
            json.decodeFromString(jsonStr)
        }.safeCollect()
    }

    suspend fun saveCollection(collection: AppCollection) {
        val collections = getCollections().first().toMutableList()
        val index = collections.indexOfFirst { it.id == collection.id }
        if (index >= 0) {
            collections[index] = collection
        } else {
            collections.add(collection)
        }
        dataStore.edit { prefs ->
            prefs[COLLECTIONS_KEY] = json.encodeToString(collections)
        }
    }

    suspend fun deleteCollection(collectionId: String) {
        val collections = getCollections().first().filter { it.id != collectionId }
        dataStore.edit { prefs ->
            prefs[COLLECTIONS_KEY] = json.encodeToString(collections)
        }
    }

    // ═════════════════════════════════════════════════════════════
    // LEADERBOARDS
    // ═════════════════════════════════════════════════════════════

    suspend fun getLeaderboard(
        category: LeaderboardCategory,
        period: LeaderboardPeriod
    ): List<LeaderboardEntry> {
        // This would fetch from backend in production
        // For now, return empty list
        return emptyList()
    }

    // ═════════════════════════════════════════════════════════════
    // SEARCH
    // ═════════════════════════════════════════════════════════════

    suspend fun search(query: String): SocialSearchResults {
        val users = emptyList<UserProfile>()
        val themes = getSharedThemes().first().filter {
            it.name.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true)
        }
        val effects = getSharedEffects().first().filter {
            it.name.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true)
        }

        return SocialSearchResults(
            users = users,
            themes = themes,
            effects = effects,
            totalResults = themes.size + effects.size
        )
    }
}
