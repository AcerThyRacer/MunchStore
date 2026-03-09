package com.sugarmunch.app.features.model

import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

// ═════════════════════════════════════════════════════════════════
// FAVORITES / WISHLIST
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "favorites")
data class FavoriteApp(
    @PrimaryKey val appId: String,
    val name: String,
    val packageName: String,
    val description: String,
    val iconUrl: String?,
    val version: String,
    val category: String?,
    val addedAt: Long = System.currentTimeMillis(),
    val notifyOnUpdate: Boolean = true,
    val priority: Int = 0 // 0 = normal, 1 = high, 2 = urgent
)

@Entity(tableName = "wishlist")
data class WishlistItem(
    @PrimaryKey val appId: String,
    val name: String,
    val notes: String = "",
    val addedAt: Long = System.currentTimeMillis(),
    val targetPrice: Float? = null,
    val notified: Boolean = false
)

// ═════════════════════════════════════════════════════════════════
// ACHIEVEMENTS / GAMIFICATION
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val icon: String, // Emoji or resource
    val category: AchievementCategory,
    val rarity: AchievementRarity,
    val requirementType: String,
    val requirementValue: Int,
    val hidden: Boolean = false,
    val order: Int = 0
)

@Entity(tableName = "user_achievements")
data class UserAchievement(
    @PrimaryKey val achievementId: String,
    val unlockedAt: Long? = null,
    val progress: Int = 0,
    val maxProgress: Int = 100
) {
    val isUnlocked: Boolean get() = unlockedAt != null
    val progressPercent: Float get() = progress.toFloat() / maxProgress
}

enum class AchievementCategory {
    INSTALLER,      // Installing apps
    EXPLORER,       // Browsing/discovering
    CUSTOMIZER,     // Themes/effects
    SOCIAL,         // Social interactions
    COLLECTOR,      // Favorites/wishlist
    MASTER          // Completionist
}

enum class AchievementRarity {
    COMMON,      // Bronze
    UNCOMMON,    // Silver
    RARE,        // Gold
    EPIC,        // Platinum
    LEGENDARY    // Rainbow/Diamond
}

// ═════════════════════════════════════════════════════════════════
// USER PROFILE
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val username: String,
    val displayName: String,
    val bio: String = "",
    val avatarEmoji: String = "🍭",
    val avatarColor: String = "#FFB6C1",
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val isPublic: Boolean = true
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val userId: String,
    val totalInstalls: Int = 0,
    val totalEffectsUsed: Int = 0,
    val totalThemesTried: Int = 0,
    val favoriteAppsCount: Int = 0,
    val achievementsUnlocked: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val timeSpentMinutes: Long = 0,
    val sugarPoints: Int = 0
)

// ═════════════════════════════════════════════════════════════════
// RATINGS & REVIEWS
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "app_ratings")
data class AppRating(
    @PrimaryKey val appId: String,
    val averageRating: Float = 0f,
    val totalRatings: Int = 0,
    val fiveStars: Int = 0,
    val fourStars: Int = 0,
    val threeStars: Int = 0,
    val twoStars: Int = 0,
    val oneStars: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_ratings")
data class UserRating(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val appId: String,
    val userId: String,
    val rating: Int, // 1-5
    val review: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ═════════════════════════════════════════════════════════════════
// APP HISTORY
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "app_history")
data class AppHistory(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val appId: String,
    val appName: String,
    val action: String, // VIEWED, INSTALLED, FAVORITED, etc
    val timestamp: Long = System.currentTimeMillis(),
    val details: String? = null
)

@Entity(tableName = "update_notifications")
data class UpdateNotification(
    @PrimaryKey val appId: String,
    val appName: String,
    val currentVersion: String,
    val newVersion: String,
    val changelog: String = "",
    val notifiedAt: Long = System.currentTimeMillis(),
    val dismissed: Boolean = false
)

// ═════════════════════════════════════════════════════════════════
// COLLECTIONS
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "app_collections")
data class AppCollection(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val icon: String = "📁",
    val createdBy: String,
    val isPublic: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val followersCount: Int = 0
)

@Entity(tableName = "collection_apps")
data class CollectionApp(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val collectionId: String,
    val appId: String,
    val addedAt: Long = System.currentTimeMillis()
)

// ═════════════════════════════════════════════════════════════════
// SOCIAL
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "friendships")
data class Friendship(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId1: String,
    val userId2: String,
    val status: String, // PENDING, ACCEPTED, BLOCKED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "social_activities")
data class SocialActivity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val type: String, // INSTALLED_APP, FAVORITED, etc
    val targetId: String? = null,
    val targetName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

// Type converters
class Converters {
    private val gson = Gson()
}
