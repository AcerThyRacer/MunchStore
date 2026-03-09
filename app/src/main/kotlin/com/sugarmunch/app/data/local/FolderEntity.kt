package com.sugarmunch.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

/**
 * Represents a folder for organizing apps in the SugarMunch launcher.
 * Supports nested folders, custom styling, smart categorization, and visual effects.
 * Enhanced in Phase 1 with smart rules, animations, and badges.
 */
@Entity(tableName = "folders")
@TypeConverters(FolderConverters::class)
data class FolderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val parentId: String? = null,
    val iconResId: Int? = null,
    val iconColor: String? = null,
    val folderStyle: String = FolderStyle.DEFAULT.name,
    val backgroundColor: String? = null,
    val sortOrder: Int = 0,
    val isSystemFolder: Boolean = false,
    val isExpanded: Boolean = true,
    val appIds: List<String> = emptyList(),
    val subFolderIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // === Phase 1 Enhanced Fields ===

    // Smart folder rules
    val smartRule: String? = null,
    val smartConfigJson: String? = null,

    // Badge system
    val badgeCount: Int = 0,
    val badgeType: String = BadgeType.NONE.name,
    val badgeCustomText: String? = null,  // Renamed from badgeText to avoid conflict with getBadgeText()

    // Animation configuration
    val animationType: String = FolderAnimationType.EXPAND.name,
    val animationDurationMs: Long = 300,

    // Custom icon support
    val customIconUrl: String? = null,
    val iconEmoji: String? = null,

    // Gradient backgrounds
    val gradientStartColor: String? = null,
    val gradientEndColor: String? = null,
    val gradientDirection: String? = null,
    val isGradientAnimated: Boolean = false,

    // Nesting support (max 3 levels)
    val depthLevel: Int = 0,

    // Auto-sorting
    val autoSort: Boolean = false,
    val sortMode: String = FolderSortMode.MANUAL.name,

    // Usage tracking
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val accessCount: Int = 0,

    // Quick access
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,

    // Tags for smart organization
    val tags: List<String> = emptyList(),

    // Preview configuration
    val maxPreviewApps: Int = 4,
    val showNamesInPreview: Boolean = false,

    // Folder size limits
    val maxApps: Int = 100
) {
    /**
     * Get the effective folder style enum
     */
    fun getFolderStyle(): FolderStyle {
        return try {
            FolderStyle.valueOf(folderStyle)
        } catch (e: IllegalArgumentException) {
            FolderStyle.DEFAULT
        }
    }

    /**
     * Get the effective badge type enum
     */
    fun getBadgeType(): BadgeType {
        return try {
            BadgeType.valueOf(badgeType)
        } catch (e: IllegalArgumentException) {
            BadgeType.NONE
        }
    }

    /**
     * Get the effective animation type enum
     */
    fun getAnimationType(): FolderAnimationType {
        return try {
            FolderAnimationType.valueOf(animationType)
        } catch (e: IllegalArgumentException) {
            FolderAnimationType.EXPAND
        }
    }

    /**
     * Get the effective sort mode enum
     */
    fun getSortMode(): FolderSortMode {
        return try {
            FolderSortMode.valueOf(sortMode)
        } catch (e: IllegalArgumentException) {
            FolderSortMode.MANUAL
        }
    }

    /**
     * Check if this folder has a gradient background
     */
    fun hasGradient(): Boolean {
        return gradientStartColor != null && gradientEndColor != null
    }

    /**
     * Check if this is a root folder (no parent)
     */
    fun isRootFolder(): Boolean = parentId == null

    /**
     * Check if this folder can have subfolders
     */
    fun canNest(): Boolean = depthLevel < 3

    /**
     * Check if this folder is at max nesting depth
     */
    fun isAtMaxDepth(): Boolean = depthLevel >= 3

    /**
     * Get the app count badge text
     */
    fun getBadgeText(): String {
        return when (getBadgeType()) {
            BadgeType.APP_COUNT -> appIds.size.toString()
            BadgeType.NOTIFICATION_COUNT -> badgeCount.toString()
            BadgeType.NEW_APPS -> "NEW"
            BadgeType.UPDATED -> "UPD"
            BadgeType.CUSTOM_TEXT -> badgeCustomText ?: ""
            BadgeType.DOT -> ""
            BadgeType.PROGRESS -> "$badgeCount%"
            BadgeType.UNREAD -> if (badgeCount > 99) "99+" else badgeCount.toString()
            BadgeType.NONE -> ""
        }
    }

    /**
     * Create a copy with updated access time
     */
    fun withAccess(): FolderEntity = copy(
        lastAccessedAt = System.currentTimeMillis(),
        accessCount = accessCount + 1
    )

    /**
     * Create a copy with app added
     */
    fun withAppAdded(appId: String): FolderEntity = copy(
        appIds = (appIds + appId).distinct().take(maxApps),
        updatedAt = System.currentTimeMillis()
    )

    /**
     * Create a copy with app removed
     */
    fun withAppRemoved(appId: String): FolderEntity = copy(
        appIds = appIds.filter { it != appId },
        updatedAt = System.currentTimeMillis()
    )

    /**
     * Create a copy with subfolder added
     */
    fun withSubfolderAdded(folderId: String): FolderEntity? {
        return if (canNest()) {
            copy(subFolderIds = (subFolderIds + folderId).distinct())
        } else null
    }
}

/**
 * Visual styles for folders - expanded for Phase 1
 */
enum class FolderStyle {
    DEFAULT,
    GLASSMORPHIC,
    NEON,
    HOLOGRAPHIC,
    LIQUID,
    CRYSTAL,
    SUGAR_RUSH,
    MINIMAL,
    NEUMORPHIC,
    GRADIENT,
    ANIMATED,
    BLUR,
    VIGNETTE
}

/**
 * Smart folder rules for auto-categorization
 */
enum class SmartFolderRule {
    NONE,
    BY_CATEGORY,
    BY_USAGE_FREQUENCY,
    BY_RECENT,
    BY_INSTALL_TIME,
    BY_SIZE,
    BY_RATING,
    BY_FEATURED,
    BY_GAMES,
    BY_SOCIAL,
    BY_PRODUCTIVITY,
    BY_ENTERTAINMENT,
    BY_UTILITIES,
    BY_CUSTOM_TAG,
    BY_TIME_OF_DAY,
    BY_LOCATION,
    BY_PACKAGE_PREFIX
}

/**
 * Badge types for folder indicators
 */
enum class BadgeType {
    NONE,
    NOTIFICATION_COUNT,
    APP_COUNT,
    NEW_APPS,
    UPDATED,
    CUSTOM_TEXT,
    DOT,
    PROGRESS,
    UNREAD
}

/**
 * Animation types for folder interactions
 */
enum class FolderAnimationType {
    EXPAND,
    EXPLODE,
    MORPH,
    SLIDE_UP,
    FADE_IN,
    BOUNCE,
    FLIP_3D,
    RIPPLE,
    SPREAD,
    CASCADE
}

/**
 * Sort modes for folder contents
 */
enum class FolderSortMode {
    MANUAL,
    ALPHABETICAL,
    ALPHABETICAL_DESC,
    USAGE_FREQUENCY,
    RECENTLY_USED,
    INSTALL_DATE,
    SIZE,
    RATING,
    CUSTOM
}

/**
 * Gradient direction for folder backgrounds
 */
enum class GradientDirection {
    TOP_BOTTOM,
    LEFT_RIGHT,
    DIAGONAL_TL_BR,
    DIAGONAL_BL_TR,
    RADIAL,
    SWEEP
}
