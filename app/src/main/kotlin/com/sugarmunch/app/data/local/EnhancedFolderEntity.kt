package com.sugarmunch.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

/**
 * Enhanced folder entity with smart rules, animations, and badges.
 * Supports nested folders, auto-categorization, and visual effects.
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

    // Enhanced fields for Phase 1
    val smartRule: SmartFolderRule? = null,
    val badgeCount: Int = 0,
    val badgeType: BadgeType = BadgeType.NONE,
    val animationType: FolderAnimationType = FolderAnimationType.EXPAND,
    val customIconUrl: String? = null,
    val accentGradient: GradientSpec? = null,
    val depthLevel: Int = 0, // 0 = root, max 3
    val autoSort: Boolean = false,
    val sortMode: FolderSortMode = FolderSortMode.MANUAL,
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val accessCount: Int = 0,
    val isPinned: Boolean = false,
    val tags: List<String> = emptyList()
)

/**
 * Smart folder rules for auto-categorization
 */
enum class SmartFolderRule {
    NONE,                   // Manual folder
    BY_CATEGORY,            // Auto-group by app category
    BY_USAGE_FREQUENCY,     // Most used apps
    BY_RECENT,              // Recently used apps
    BY_INSTALL_TIME,        // Recently installed
    BY_SIZE,                // App size
    BY_RATING,              // App ratings
    BY_FEATURED,            // Featured apps only
    BY_GAMES,               // Game apps only
    BY_SOCIAL,              // Social media apps
    BY_PRODUCTIVITY,        // Productivity apps
    BY_ENTERTAINMENT,       // Entertainment apps
    BY_UTILITIES,           // Utility apps
    BY_CUSTOM_TAG,          // Custom user tags
    BY_TIME_OF_DAY,         // Time-based (morning/afternoon/evening)
    BY_LOCATION             // Location-aware
}

/**
 * Badge types for folder indicators
 */
enum class BadgeType {
    NONE,
    NOTIFICATION_COUNT,     // Show notification count
    APP_COUNT,              // Show app count
    NEW_APPS,               // "New" badge for new apps
    UPDATED,                // "Updated" badge
    CUSTOM_TEXT,            // Custom text badge
    DOT,                    // Simple dot indicator
    PROGRESS,               // Progress indicator
    UNREAD                  // Unread count
}

/**
 * Animation types for folder interactions
 */
enum class FolderAnimationType {
    EXPAND,                 // Expand in place
    EXPLODE,                // Explode outward
    MORPH,                  // Morph into apps
    SLIDE_UP,               // Slide up sheet
    FADE_IN,                // Simple fade
    BOUNCE,                 // Bouncy expansion
    FLIP_3D,                // 3D flip animation
    RIPPLE,                 // Ripple effect
    SPREAD,                 // Spread from center
    CASCADE                 // Cascade animation
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
 * Gradient specification for folder backgrounds
 */
data class GradientSpec(
    val startColor: String,
    val endColor: String,
    val direction: GradientDirection = GradientDirection.TOP_BOTTOM,
    val isAnimated: Boolean = false,
    val animationDurationMs: Long = 3000
)

enum class GradientDirection {
    TOP_BOTTOM,
    LEFT_RIGHT,
    DIAGONAL_TL_BR,
    DIAGONAL_BL_TR,
    RADIAL,
    SWEEP
}

/**
 * Visual styles for folders
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
    ANIMATED
}

/**
 * Folder preview configuration
 */
data class FolderPreviewConfig(
    val showAppIcons: Boolean = true,
    val maxPreviewIcons: Int = 4,
    val previewLayout: PreviewLayout = PreviewLayout.GRID_2X2,
    val showAppName: Boolean = false,
    val iconCornerRadius: Float = 8f,
    val iconSpacing: Float = 4f
)

enum class PreviewLayout {
    GRID_2X2,
    GRID_3X3,
    STACK,
    LIST,
    CIRCULAR
}

/**
 * Smart folder configuration
 */
data class SmartFolderConfig(
    val rule: SmartFolderRule,
    val autoUpdate: Boolean = true,
    val updateIntervalMs: Long = 60000, // 1 minute
    val maxApps: Int = 20,
    val minApps: Int = 1,
    val excludeSystemApps: Boolean = true,
    val customFilter: String? = null // Custom query filter
)
