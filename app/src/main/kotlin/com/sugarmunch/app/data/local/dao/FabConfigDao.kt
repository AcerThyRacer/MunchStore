package com.sugarmunch.app.data.local.dao

import androidx.room.*
import com.sugarmunch.app.data.local.entity.FabConfigEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for FAB Configuration
 */
@Dao
interface FabConfigDao {

    @Query("SELECT * FROM fab_config WHERE id = :id")
    suspend fun getFabConfigById(id: String): FabConfigEntity?

    @Query("SELECT * FROM fab_config ORDER BY id LIMIT 1")
    fun getDefaultFabConfig(): Flow<FabConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFabConfig(config: FabConfigEntity)

    @Update
    suspend fun updateFabConfig(config: FabConfigEntity)

    @Query("UPDATE fab_config SET style = :style, candyType = :candyType, trailEffect = :trailEffect, size = :size, opacity = :opacity WHERE id = :id")
    suspend fun updateFabConfig(
        id: String,
        style: String,
        candyType: String,
        trailEffect: String,
        size: Float,
        opacity: Float
    )

    @Query("UPDATE fab_config SET shortcutEffects = :effects WHERE id = :id")
    suspend fun updateShortcutEffects(id: String, effects: String)

    @Query("DELETE FROM fab_config WHERE id = :id")
    suspend fun deleteFabConfig(id: String)
}
