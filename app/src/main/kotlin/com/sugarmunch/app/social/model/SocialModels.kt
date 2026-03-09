package com.sugarmunch.app.social.model

import kotlinx.serialization.Serializable

/**
 * Social Features Data Models
 * User profiles, activities, comments, and more
 */

/**
 * User Profile
 */
@Serializable
data class UserProfile(
    val id: String,
    val username: String,
    val displayName: String,
    val bio: String = "",
    val avatarEmoji: String = "🍬",
    val avatarColor: Int = 0xFFFF69B4,
    val isVerified: Boolean = false,
    val isPremium: Boolean = false,
    val joinDate: Long = System.currentTimeMillis(),
    val lastActive: Long = System.currentTimeMillis(),
    val stats: UserStats = UserStats(),
    val privacy: PrivacySettings = PrivacySettings()
)

/**
 * User Statistics
 */
@Serializable
data class UserStats(
    val totalInstalls: Int = 0,
    val themesCreated: Int = 0,
    val effectsCreated: Int = 0,
    val collectionsCreated: Int = 0,
    val achievementsUnlocked: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val sugarPoints: Int = 0,
    val level: Int = 1,
    val xp: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0
) {
    val xpToNextLevel: Int
        get() = level * 1000

    val xpProgress: Float
        get() = (xp % xpToNextLevel).toFloat() / xpToNextLevel
}

/**
 * Privacy Settings
 */
@Serializable
data class PrivacySettings(
    val isProfilePublic: Boolean = true,
    val showActivity: Boolean = true,
    val showCollections: Boolean = true,
    val allowDirectMessages: Boolean = false,
    val showOnlineStatus: Boolean = false
)

/**
 * Social Activity
 */
@Serializable
data class SocialActivity(
    val id: String,
    val userId: String,
    val user: UserProfile,
    val type: ActivityType,
    val targetType: TargetType,
    val targetId: String,
    val targetName: String,
    val targetImageUrl: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false
)

/**
 * Activity Types
 */
enum class ActivityType {
    INSTALLED_APP,
    FAVORITED_APP,
    RATED_APP,
    CREATED_THEME,
    CREATED_EFFECT,
    CREATED_COLLECTION,
    FOLLOWED_USER,
    ACHIEVEMENT_UNLOCKED,
    SHARED_APP,
    PURCHASED_ITEM,
    CLAIMED_REWARD,
    LEVEL_UP,
    JOINED
}

/**
 * Target Types
 */
enum class TargetType {
    APP,
    THEME,
    EFFECT,
    COLLECTION,
    USER,
    ACHIEVEMENT,
    SHOP_ITEM
}

/**
 * Comment
 */
@Serializable
data class Comment(
    val id: String,
    val userId: String,
    val user: UserProfile,
    val targetType: TargetType,
    val targetId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val repliesCount: Int = 0,
    val parentCommentId: String? = null,
    val isEdited: Boolean = false
)

/**
 * Follow Relationship
 */
@Serializable
data class FollowRelationship(
    val followerId: String,
    val followingId: String,
    val status: FollowStatus,
    val createdAt: Long = System.currentTimeMillis()
)

enum class FollowStatus {
    PENDING,
    ACCEPTED,
    BLOCKED
}

/**
 * Community Content (Themes/Effects)
 */
@Serializable
data class CommunityContent(
    val id: String,
    val creatorId: String,
    val creator: UserProfile,
    val contentType: ContentType,
    val name: String,
    val description: String,
    val data: String, // JSON serialized theme/effect data
    val previewImageUrl: String? = null,
    val downloadCount: Int = 0,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val rating: Float = 0f,
    val tags: List<String> = emptyList(),
    val isFeatured: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ContentType {
    THEME,
    EFFECT,
    PRESET
}

/**
 * Leaderboard Entry
 */
@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val user: UserProfile,
    val score: Long,
    val category: LeaderboardCategory,
    val period: LeaderboardPeriod,
    val trend: TrendDirection = TrendDirection.STABLE
)

enum class LeaderboardCategory {
    MOST_INSTALLS,
    MOST_SUGAR_POINTS,
    LONGEST_STREAK,
    MOST_ACHIEVEMENTS,
    MOST_FOLLOWERS,
    MOST_COLLECTIONS,
    TOP_CREATOR
}

enum class LeaderboardPeriod {
    ALL_TIME,
    THIS_WEEK,
    THIS_MONTH
}

enum class TrendDirection {
    RISING,
    STABLE,
    FALLING
}

/**
 * Notification
 */
@Serializable
data class SocialNotification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val fromUserId: String,
    val fromUser: UserProfile? = null,
    val targetType: TargetType? = null,
    val targetId: String? = null,
    val message: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

enum class NotificationType {
    NEW_FOLLOWER,
    FOLLOW_REQUEST,
    ACTIVITY_LIKE,
    ACTIVITY_COMMENT,
    CONTENT_FEATURED,
    LEVEL_UP,
    ACHIEVEMENT_UNLOCKED,
    MENTION
}

/**
 * Report Content
 */
@Serializable
data class ContentReport(
    val id: String,
    val reporterId: String,
    val contentType: TargetType,
    val contentId: String,
    val reason: ReportReason,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: ReportStatus = ReportStatus.PENDING
)

enum class ReportReason {
    SPAM,
    INAPPROPRIATE_CONTENT,
    HARASSMENT,
    COPYRIGHT_INFRINGEMENT,
    FAKE_CONTENT,
    OTHER
}

enum class ReportStatus {
    PENDING,
    UNDER_REVIEW,
    RESOLVED,
    REJECTED
}

/**
 * Search Results
 */
@Serializable
data class SocialSearchResults(
    val users: List<UserProfile> = emptyList(),
    val themes: List<CommunityContent> = emptyList(),
    val effects: List<CommunityContent> = emptyList(),
    val collections: List<AppCollection> = emptyList(),
    val totalResults: Int = 0
)

/**
 * App Collection (Social)
 */
@Serializable
data class AppCollection(
    val id: String,
    val creatorId: String,
    val creator: UserProfile,
    val name: String,
    val description: String,
    val iconEmoji: String = "📦",
    val appIds: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val followersCount: Int = 0,
    val likesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
