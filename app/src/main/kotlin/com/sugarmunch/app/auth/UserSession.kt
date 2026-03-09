package com.sugarmunch.app.auth

import kotlinx.serialization.Serializable

/**
 * UserSession - Represents an authenticated user session
 *
 * This data class holds all relevant information about a logged-in user,
 * including their profile information, authentication state, and metadata.
 *
 * @property uid Unique user ID from Firebase Auth
 * @property email User's email address (null for anonymous users)
 * @property displayName User's display name
 * @property photoUrl URL to user's profile photo
 * @property isAnonymous Whether this is an anonymous account
 * @property createdAt Account creation timestamp (milliseconds since epoch)
 * @property lastSignInAt Last sign-in timestamp (milliseconds since epoch)
 * @property wasUpgradedFromAnonymous Whether account was upgraded from anonymous
 */
@Serializable
data class UserSession(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSignInAt: Long = System.currentTimeMillis(),
    val wasUpgradedFromAnonymous: Boolean = false
) {
    /**
     * Check if user has a verified email
     */
    fun hasVerifiedEmail(): Boolean = !email.isNullOrBlank()

    /**
     * Get display name or fallback to email or "Anonymous User"
     */
    fun getDisplayName(): String {
        return displayName?.takeIf { it.isNotBlank() }
            ?: email?.takeIf { it.isNotBlank() }
            ?: "Anonymous User"
    }

    /**
     * Get initials for avatar (e.g., "JD" for John Doe)
     */
    fun getInitials(): String {
        val name = getDisplayName()
        val parts = name.split(" ", ".", "@").filter { it.isNotBlank() }
        return when {
            parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercaseChar()}${parts[1].firstOrNull()?.uppercaseChar()}"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "AU" // Anonymous User
        }
    }

    /**
     * Get account age in days
     */
    fun getAccountAgeDays(): Long {
        return (System.currentTimeMillis() - createdAt) / (24 * 60 * 60 * 1000)
    }

    /**
     * Check if account is a legacy account (created more than 30 days ago)
     */
    fun isLegacyAccount(): Boolean = getAccountAgeDays() > 30

    /**
     * Create a copy with updated email (for account upgrades)
     */
    fun withEmail(newEmail: String): UserSession {
        return copy(email = newEmail, isAnonymous = false)
    }

    /**
     * Create a copy with updated display name
     */
    fun withDisplayName(newDisplayName: String): UserSession {
        return copy(displayName = newDisplayName)
    }

    /**
     * Create a copy with updated photo URL
     */
    fun withPhotoUrl(newPhotoUrl: String): UserSession {
        return copy(photoUrl = newPhotoUrl)
    }

    companion object {
        /**
         * Create an empty/invalid user session
         * Used as a default value when no user is signed in
         */
        fun empty(): UserSession {
            return UserSession(uid = "")
        }

        /**
         * Check if a user session is valid (has non-empty UID)
         */
        fun isValid(session: UserSession?): Boolean {
            return session?.uid?.isNotBlank() == true
        }
    }
}

/**
 * User profile update request
 */
data class ProfileUpdateRequest(
    val displayName: String? = null,
    val photoUrl: String? = null
) {
    /**
     * Check if request has any updates
     */
    fun hasUpdates(): Boolean = displayName != null || photoUrl != null
}

/**
 * Account statistics for a user
 */
data class UserAccountStats(
    val totalInstalls: Int = 0,
    val totalDownloads: Int = 0,
    val sugarPoints: Int = 0,
    val level: Int = 1,
    val xp: Int = 0,
    val xpToNextLevel: Int = 1000,
    val achievementsUnlocked: Int = 0,
    val totalAchievements: Int = 50,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val themesCreated: Int = 0,
    val effectsCreated: Int = 0,
    val collectionsCreated: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0
) {
    /**
     * Get progress to next level (0.0 - 1.0)
     */
    fun getLevelProgress(): Float {
        return if (xpToNextLevel > 0) {
            xp.coerceAtMost(xpToNextLevel).toFloat() / xpToNextLevel
        } else 0f
    }

    /**
     * Get achievement progress (0.0 - 1.0)
     */
    fun getAchievementProgress(): Float {
        return achievementsUnlocked.toFloat() / totalAchievements
    }

    /**
     * Check if user is max level (placeholder: level 100)
     */
    fun isMaxLevel(): Boolean = level >= 100
}
