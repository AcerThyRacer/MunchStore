package com.sugarmunch.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Achievement Entity for Room Database
 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val iconResId: Int,
    val isUnlocked: Boolean,
    val unlockedAt: Long?,
    val progress: Int,
    val maxProgress: Int,
    val rarity: String = "COMMON",
    val requirementType: String = "",
    val requirementValue: Int = 0,
    val hidden: Boolean = false,
    val order: Int = 0
)
