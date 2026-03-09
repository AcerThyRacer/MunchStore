package com.sugarmunch.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * FAB Configuration Entity for Room Database
 */
@Entity(tableName = "fab_config")
data class FabConfigEntity(
    @PrimaryKey
    val id: String,
    val style: String,
    val candyType: String,
    val trailEffect: String,
    val size: Float,
    val opacity: Float,
    val shortcutEffects: String  // JSON list of effect IDs
)
