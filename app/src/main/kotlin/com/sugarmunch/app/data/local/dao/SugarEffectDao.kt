package com.sugarmunch.app.data.local.dao

import androidx.room.*
import com.sugarmunch.app.data.local.entity.SugarEffectEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Sugar Effects
 */
@Dao
interface SugarEffectDao {

    @Query("SELECT * FROM sugar_effects ORDER BY name")
    fun getAllEffects(): Flow<List<SugarEffectEntity>>

    @Query("SELECT * FROM sugar_effects WHERE id = :id")
    suspend fun getEffectById(id: String): SugarEffectEntity?

    @Query("SELECT * FROM sugar_effects WHERE isEnabled = 1 ORDER BY name")
    fun getEnabledEffects(): Flow<List<SugarEffectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEffect(effect: SugarEffectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEffects(effects: List<SugarEffectEntity>)

    @Update
    suspend fun updateEffect(effect: SugarEffectEntity)

    @Query("UPDATE sugar_effects SET isEnabled = :enabled, intensity = :intensity WHERE id = :id")
    suspend fun updateEffectConfig(id: String, enabled: Boolean, intensity: Float)

    @Query("UPDATE sugar_effects SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEffectEnabled(id: String, enabled: Boolean)

    @Query("UPDATE sugar_effects SET intensity = :intensity WHERE id = :id")
    suspend fun setEffectIntensity(id: String, intensity: Float)

    @Query("DELETE FROM sugar_effects WHERE id = :id")
    suspend fun deleteEffect(id: String)

    @Query("DELETE FROM sugar_effects")
    suspend fun deleteAllEffects()
}
