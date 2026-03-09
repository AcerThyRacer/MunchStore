package com.sugarmunch.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Sugar Effect Entity for Room Database
 */
@Entity(tableName = "sugar_effects")
data class SugarEffectEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val isEnabled: Boolean,
    val intensity: Float,
    val config: String  // JSON configuration
)
