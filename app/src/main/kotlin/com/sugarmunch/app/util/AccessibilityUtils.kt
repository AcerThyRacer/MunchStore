package com.sugarmunch.app.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.clickable

/**
 * Accessibility Utilities for SugarMunch
 * Provides helpers for making the app accessible to all users
 */

/**
 * Modifier for clickable elements with accessibility support
 */
fun Modifier.accessibleClickable(
    contentDescription: String,
    onClick: () -> Unit
): Modifier {
    return this
        .semantics {
            this.contentDescription = contentDescription
            role = Role.Button
        }
        .clickable(onClick = onClick)
}

/**
 * Modifier for images with accessibility support
 */
fun Modifier.accessibleImage(
    contentDescription: String
): Modifier {
    return this.semantics {
        this.contentDescription = contentDescription
    }
}

/**
 * Modifier for progress indicators with accessibility support
 */
fun Modifier.accessibleProgress(
    progress: Float,
    progressDescription: String = "${(progress * 100).toInt()}%"
): Modifier {
    return this.semantics {
        stateDescription = progressDescription
    }
}

/**
 * Modifier for tabs with accessibility support
 */
fun Modifier.accessibleTab(
    tabName: String,
    isSelected: Boolean
): Modifier {
    return this.semantics {
        contentDescription = if (isSelected) {
            "$tabName, selected"
        } else {
            tabName
        }
        role = Role.Tab
    }
}

/**
 * Modifier for list items with accessibility support
 */
fun Modifier.accessibleListItem(
    itemName: String,
    additionalInfo: String? = null
): Modifier {
    return this.semantics {
        contentDescription = buildString {
            append(itemName)
            additionalInfo?.let { append(". $it") }
        }
        role = Role.Button
    }
}

/**
 * Modifier for headings with accessibility support
 */
fun Modifier.accessibleHeading(
    headingText: String,
    level: Int = 1
): Modifier {
    return this.semantics {
        contentDescription = headingText
    }
}

/**
 * Get accessibility-friendly duration description
 */
fun getDurationDescription(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    val hours = durationMs / (1000 * 60 * 60)

    return buildString {
        if (hours > 0) append("$hours hours ")
        if (minutes > 0) append("$minutes minutes ")
        if (seconds > 0) append("$seconds seconds")
    }.trim()
}

/**
 * Get accessibility-friendly file size description
 */
fun getFileSizeDescription(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> "%.2f gigabytes".format(bytes / 1_000_000_000.0)
        bytes >= 1_000_000 -> "%.2f megabytes".format(bytes / 1_000_000.0)
        bytes >= 1_000 -> "%.2f kilobytes".format(bytes / 1_000.0)
        else -> "$bytes bytes"
    }
}

/**
 * Get accessibility-friendly rating description
 */
fun getRatingDescription(rating: Float, maxRating: Int = 5): String {
    val fullStars = rating.toInt()
    val hasHalfStar = (rating % 1) >= 0.5
    val emptyStars = maxRating - fullStars - (if (hasHalfStar) 1 else 0)

    return buildString {
        append("$rating out of $maxRating stars")
        if (fullStars > 0) append(". $fullStars full stars")
        if (hasHalfStar) append(". 1 half star")
        if (emptyStars > 0) append(". $emptyStars empty stars")
    }
}

/**
 * Get accessibility-friendly percentage description
 */
fun getPercentageDescription(percentage: Float): String {
    return "%.0f percent".format(percentage)
}

/**
 * Get accessibility-friendly count description
 */
fun getCountDescription(count: Int, singular: String, plural: String): String {
    return if (count == 1) {
        "1 $singular"
    } else {
        "$count $plural"
    }
}

/**
 * Check if large text is enabled in system settings
 */
fun isLargeTextEnabled(context: android.content.Context): Boolean {
    return context.resources.configuration.fontScale > 1.0f
}

/**
 * Get recommended minimum touch target size in dp
 */
const val MIN_TOUCH_TARGET_DP = 48

/**
 * Get recommended minimum touch target size in pixels
 */
fun getMinTouchTargetPx(context: android.content.Context): Int {
    return (MIN_TOUCH_TARGET_DP * context.resources.displayMetrics.density).toInt()
}
