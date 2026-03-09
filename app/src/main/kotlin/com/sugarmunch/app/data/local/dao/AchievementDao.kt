package com.sugarmunch.app.data.local.dao

import androidx.room.*
import com.sugarmunch.app.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Achievements
 */
@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievements ORDER BY category, `order`")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievementById(id: String): AchievementEntity?

    @Query("SELECT * FROM achievements WHERE category = :category ORDER BY `order`")
    fun getAchievementsByCategory(category: String): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 1 ORDER BY unlockedAt DESC")
    fun getUnlockedAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 0 ORDER BY category, `order`")
    fun getLockedAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    @Query("UPDATE achievements SET isUnlocked = 1, unlockedAt = :unlockedAt, progress = maxProgress WHERE id = :id")
    suspend fun unlockAchievement(id: String, unlockedAt: Long = System.currentTimeMillis())

    @Query("UPDATE achievements SET progress = :progress WHERE id = :id")
    suspend fun updateAchievementProgress(id: String, progress: Int)

    @Query("SELECT COUNT(*) FROM achievements WHERE isUnlocked = 1")
    fun getUnlockedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM achievements")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT * FROM achievements WHERE progress < maxProgress AND isUnlocked = 0 LIMIT 1")
    suspend fun getNextAchievement(): AchievementEntity?
}
