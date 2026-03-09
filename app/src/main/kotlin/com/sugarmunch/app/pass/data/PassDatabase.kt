package com.sugarmunch.app.pass.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sugarmunch.app.pass.PassRewardType
import com.sugarmunch.app.pass.RewardTrack
import kotlinx.coroutines.flow.Flow

/**
 * Room Database for Sugar Pass data
 * 
 * Tables:
 * - PassProgressEntity: User's current progress
 * - ClaimedRewardEntity: Track which rewards have been claimed
 * - SeasonHistoryEntity: Past season completions
 * - XpHistoryEntity: XP earning history
 * - SeasonEntity: Active/previous season data
 */

// ═════════════════════════════════════════════════════════════
// ENTITIES
// ═════════════════════════════════════════════════════════════

@Entity(tableName = "pass_progress")
data class PassProgressEntity(
    @PrimaryKey val id: Int = 1, // Single row for current progress
    val currentTier: Int = 1,
    val totalXp: Int = 0,
    val hasPremium: Boolean = false,
    val premiumPurchaseDate: Long? = null,
    val currentSeasonId: String = "",
    val seasonStartDate: Long = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "claimed_rewards",
    indices = [Index(value = ["tier", "track"], unique = true)]
)
data class ClaimedRewardEntity(
    @PrimaryKey val id: String, // "{tier}_{track}"
    val tier: Int,
    val track: String, // FREE, PREMIUM, LEGENDARY
    val claimedAt: Long = System.currentTimeMillis(),
    val rewardType: String,
    val rewardName: String,
    val rewardValue: Int = 0
)

@Entity(tableName = "season_history")
data class SeasonHistoryEntity(
    @PrimaryKey val seasonId: String,
    val seasonName: String,
    val seasonEmoji: String,
    val startDate: Long,
    val endDate: Long,
    val finalTier: Int,
    val maxTier: Int = 100,
    val hadPremium: Boolean,
    val totalXpEarned: Int,
    val rewardsClaimed: Int,
    val exclusiveRewards: String, // Comma-separated list
    val completed: Boolean = true
)

@Entity(tableName = "xp_history")
data class XpHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val amount: Int,
    val source: String, // DAILY_LOGIN, APP_INSTALL, etc.
    val description: String = "",
    val multiplier: Float = 1.0f,
    val finalAmount: Int = amount,
    val tierAtTime: Int = 1
)

@Entity(tableName = "seasons")
data class SeasonEntity(
    @PrimaryKey val seasonId: String,
    val name: String,
    val description: String,
    val emoji: String,
    val month: Int,
    val themePrimaryColor: Long,
    val themeSecondaryColor: Long,
    val themeAccentColor: Long,
    val backgroundStyle: String,
    val exclusiveRewards: String, // JSON array of reward IDs
    val isActive: Boolean = false,
    val startTime: Long? = null,
    val endTime: Long? = null
)

@Entity(
    tableName = "user_boosts",
    indices = [Index(value = ["boostType", "isActive"])]
)
data class UserBoostEntity(
    @PrimaryKey val id: String,
    val boostType: String, // XP_BOOST, CURRENCY_BOOST, etc.
    val multiplier: Float,
    val activatedAt: Long,
    val expiresAt: Long,
    val isActive: Boolean = true,
    val source: String = "" // How it was obtained
)

@Entity(tableName = "pass_stats")
data class PassStatsEntity(
    @PrimaryKey val id: Int = 1,
    val lifetimeXp: Int = 0,
    val lifetimeTiersCompleted: Int = 0,
    val totalRewardsClaimed: Int = 0,
    val exclusiveItemsOwned: Int = 0,
    val premiumPurchasesCount: Int = 0,
    val seasonsCompleted: Int = 0,
    val highestTierReached: Int = 1,
    val fastestTier100TimeHours: Int? = null,
    val totalSugarPointsFromPass: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

// ═════════════════════════════════════════════════════════════
// DAO
// ═════════════════════════════════════════════════════════════

@Dao
interface PassProgressDao {
    @Query("SELECT * FROM pass_progress WHERE id = 1")
    fun getProgress(): Flow<PassProgressEntity?>
    
    @Query("SELECT * FROM pass_progress WHERE id = 1")
    suspend fun getProgressSync(): PassProgressEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateProgress(progress: PassProgressEntity)
    
    @Query("UPDATE pass_progress SET currentTier = :tier, totalXp = :xp, lastUpdated = :timestamp WHERE id = 1")
    suspend fun updateTierAndXp(tier: Int, xp: Int, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE pass_progress SET hasPremium = :premium, premiumPurchaseDate = :date WHERE id = 1")
    suspend fun updatePremiumStatus(premium: Boolean, date: Long? = System.currentTimeMillis())
    
    @Query("DELETE FROM pass_progress")
    suspend fun clearProgress()
}

@Dao
interface ClaimedRewardDao {
    @Query("SELECT * FROM claimed_rewards ORDER BY tier ASC")
    fun getAllClaimed(): Flow<List<ClaimedRewardEntity>>
    
    @Query("SELECT * FROM claimed_rewards WHERE tier = :tier")
    suspend fun getClaimedForTier(tier: Int): List<ClaimedRewardEntity>
    
    @Query("SELECT COUNT(*) FROM claimed_rewards")
    fun getClaimedCount(): Flow<Int>
    
    @Query("SELECT * FROM claimed_rewards WHERE id = :rewardId")
    suspend fun isClaimed(rewardId: String): ClaimedRewardEntity?
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertClaimed(reward: ClaimedRewardEntity)
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllClaimed(rewards: List<ClaimedRewardEntity>)
    
    @Query("DELETE FROM claimed_rewards")
    suspend fun clearAll()
    
    @Query("SELECT COUNT(*) FROM claimed_rewards WHERE track = 'FREE'")
    fun getFreeRewardsCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM claimed_rewards WHERE track != 'FREE'")
    fun getPremiumRewardsCount(): Flow<Int>
}

@Dao
interface SeasonHistoryDao {
    @Query("SELECT * FROM season_history ORDER BY endDate DESC")
    fun getAllHistory(): Flow<List<SeasonHistoryEntity>>
    
    @Query("SELECT * FROM season_history WHERE seasonId = :seasonId")
    suspend fun getSeason(seasonId: String): SeasonHistoryEntity?
    
    @Query("SELECT * FROM season_history ORDER BY finalTier DESC LIMIT 1")
    suspend fun getBestSeason(): SeasonHistoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeasonHistory(season: SeasonHistoryEntity)
    
    @Query("SELECT COUNT(*) FROM season_history WHERE completed = 1")
    fun getCompletedSeasonsCount(): Flow<Int>
    
    @Query("SELECT AVG(finalTier) FROM season_history")
    fun getAverageTier(): Flow<Float?>
    
    @Query("SELECT * FROM season_history WHERE hadPremium = 1")
    fun getPremiumSeasons(): Flow<List<SeasonHistoryEntity>>
}

@Dao
interface XpHistoryDao {
    @Query("SELECT * FROM xp_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 100): Flow<List<XpHistoryEntity>>
    
    @Query("SELECT * FROM xp_history WHERE source = :source ORDER BY timestamp DESC")
    fun getHistoryBySource(source: String): Flow<List<XpHistoryEntity>>
    
    @Query("SELECT SUM(finalAmount) FROM xp_history WHERE timestamp >= :since")
    suspend fun getXpSince(since: Long): Int?
    
    @Query("SELECT SUM(finalAmount) FROM xp_history WHERE source = :source")
    suspend fun getTotalXpFromSource(source: String): Int?
    
    @Insert
    suspend fun insertXpEntry(entry: XpHistoryEntity): Long
    
    @Insert
    suspend fun insertAllXpEntries(entries: List<XpHistoryEntity>)
    
    @Query("SELECT * FROM xp_history WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getTodayHistory(startOfDay: Long): Flow<List<XpHistoryEntity>>
    
    @Query("SELECT COUNT(*) FROM xp_history WHERE timestamp >= :startOfDay")
    suspend fun getTodayEntryCount(startOfDay: Long): Int
    
    @Query("DELETE FROM xp_history WHERE timestamp < :olderThan")
    suspend fun deleteOldEntries(olderThan: Long)
}

@Dao
interface SeasonDao {
    @Query("SELECT * FROM seasons WHERE isActive = 1 LIMIT 1")
    fun getActiveSeason(): Flow<SeasonEntity?>
    
    @Query("SELECT * FROM seasons WHERE seasonId = :seasonId")
    suspend fun getSeason(seasonId: String): SeasonEntity?
    
    @Query("SELECT * FROM seasons ORDER BY month ASC")
    fun getAllSeasons(): Flow<List<SeasonEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeason(season: SeasonEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSeasons(seasons: List<SeasonEntity>)
    
    @Query("UPDATE seasons SET isActive = 0")
    suspend fun deactivateAllSeasons()
    
    @Query("UPDATE seasons SET isActive = 1, startTime = :startTime, endTime = :endTime WHERE seasonId = :seasonId")
    suspend fun activateSeason(seasonId: String, startTime: Long, endTime: Long)
}

@Dao
interface UserBoostDao {
    @Query("SELECT * FROM user_boosts WHERE isActive = 1 AND expiresAt > :currentTime")
    fun getActiveBoosts(currentTime: Long = System.currentTimeMillis()): Flow<List<UserBoostEntity>>
    
    @Query("SELECT * FROM user_boosts WHERE boostType = :type AND isActive = 1 AND expiresAt > :currentTime LIMIT 1")
    suspend fun getActiveBoostOfType(type: String, currentTime: Long = System.currentTimeMillis()): UserBoostEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoost(boost: UserBoostEntity)
    
    @Query("UPDATE user_boosts SET isActive = 0 WHERE id = :boostId")
    suspend fun deactivateBoost(boostId: String)
    
    @Query("UPDATE user_boosts SET isActive = 0 WHERE expiresAt <= :currentTime")
    suspend fun deactivateExpiredBoosts(currentTime: Long = System.currentTimeMillis())
    
    @Query("SELECT SUM(multiplier) FROM user_boosts WHERE isActive = 1 AND boostType = :type")
    suspend fun getTotalMultiplierForType(type: String): Float?
}

@Dao
interface PassStatsDao {
    @Query("SELECT * FROM pass_stats WHERE id = 1")
    fun getStats(): Flow<PassStatsEntity?>
    
    @Query("SELECT * FROM pass_stats WHERE id = 1")
    suspend fun getStatsSync(): PassStatsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStats(stats: PassStatsEntity)
    
    @Query("""
        UPDATE pass_stats SET 
            lifetimeXp = lifetimeXp + :xp,
            totalRewardsClaimed = totalRewardsClaimed + :rewards,
            exclusiveItemsOwned = exclusiveItemsOwned + :exclusive,
            lastUpdated = :timestamp
        WHERE id = 1
    """)
    suspend fun incrementStats(xp: Int = 0, rewards: Int = 0, exclusive: Int = 0, timestamp: Long = System.currentTimeMillis())
    
    @Query("""
        UPDATE pass_stats SET 
            premiumPurchasesCount = premiumPurchasesCount + 1,
            lastUpdated = :timestamp
        WHERE id = 1
    """)
    suspend fun incrementPremiumPurchases(timestamp: Long = System.currentTimeMillis())
    
    @Query("""
        UPDATE pass_stats SET 
            seasonsCompleted = seasonsCompleted + 1,
            lifetimeTiersCompleted = lifetimeTiersCompleted + :tiers,
            lastUpdated = :timestamp
        WHERE id = 1
    """)
    suspend fun incrementSeasonCompleted(tiers: Int, timestamp: Long = System.currentTimeMillis())
}

// ═════════════════════════════════════════════════════════════
// DATABASE
// ═════════════════════════════════════════════════════════════

@Database(
    entities = [
        PassProgressEntity::class,
        ClaimedRewardEntity::class,
        SeasonHistoryEntity::class,
        XpHistoryEntity::class,
        SeasonEntity::class,
        UserBoostEntity::class,
        PassStatsEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class PassDatabase : RoomDatabase() {
    abstract fun passProgressDao(): PassProgressDao
    abstract fun claimedRewardDao(): ClaimedRewardDao
    abstract fun seasonHistoryDao(): SeasonHistoryDao
    abstract fun xpHistoryDao(): XpHistoryDao
    abstract fun seasonDao(): SeasonDao
    abstract fun userBoostDao(): UserBoostDao
    abstract fun passStatsDao(): PassStatsDao

    companion object {
        @Volatile
        private var INSTANCE: PassDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {}
        }

        fun getDatabase(context: Context): PassDatabase {
            return INSTANCE ?: synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    PassDatabase::class.java,
                    "sugar_pass_db"
                ).addMigrations(MIGRATION_1_2)
                if (com.sugarmunch.app.BuildConfig.DEBUG) {
                    builder.fallbackToDestructiveMigration()
                }
                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// REPOSITORY
// ═════════════════════════════════════════════════════════════

class PassRepository(context: Context) {
    private val database = PassDatabase.getDatabase(context)
    private val progressDao = database.passProgressDao()
    private val claimedDao = database.claimedRewardDao()
    private val historyDao = database.seasonHistoryDao()
    private val xpDao = database.xpHistoryDao()
    private val seasonDao = database.seasonDao()
    private val boostDao = database.userBoostDao()
    private val statsDao = database.passStatsDao()

    // Progress operations
    fun getProgress() = progressDao.getProgress()
    suspend fun updateProgress(progress: PassProgressEntity) = progressDao.updateProgress(progress)
    suspend fun updateTierAndXp(tier: Int, xp: Int) = progressDao.updateTierAndXp(tier, xp)
    suspend fun updatePremiumStatus(premium: Boolean) = progressDao.updatePremiumStatus(premium)

    // Claimed rewards operations
    fun getClaimedRewards() = claimedDao.getAllClaimed()
    suspend fun isRewardClaimed(rewardId: String) = claimedDao.isClaimed(rewardId) != null
    suspend fun claimReward(reward: ClaimedRewardEntity) = claimedDao.insertClaimed(reward)
    fun getClaimedCount() = claimedDao.getClaimedCount()

    // Season history operations
    fun getSeasonHistory() = historyDao.getAllHistory()
    suspend fun addSeasonToHistory(season: SeasonHistoryEntity) = historyDao.insertSeasonHistory(season)
    fun getCompletedSeasonsCount() = historyDao.getCompletedSeasonsCount()
    fun getAverageTier() = historyDao.getAverageTier()

    // XP history operations
    fun getRecentXpHistory(limit: Int = 100) = xpDao.getRecentHistory(limit)
    fun getXpHistoryBySource(source: String) = xpDao.getHistoryBySource(source)
    suspend fun addXpEntry(entry: XpHistoryEntity) = xpDao.insertXpEntry(entry)
    suspend fun getXpSince(timestamp: Long) = xpDao.getXpSince(timestamp) ?: 0

    // Season operations
    fun getActiveSeason() = seasonDao.getActiveSeason()
    suspend fun activateSeason(seasonId: String, startTime: Long, endTime: Long) {
        seasonDao.deactivateAllSeasons()
        seasonDao.activateSeason(seasonId, startTime, endTime)
    }
    suspend fun insertSeasons(seasons: List<SeasonEntity>) = seasonDao.insertAllSeasons(seasons)

    // Boost operations
    fun getActiveBoosts() = boostDao.getActiveBoosts()
    suspend fun addBoost(boost: UserBoostEntity) = boostDao.insertBoost(boost)
    suspend fun deactivateExpiredBoosts() = boostDao.deactivateExpiredBoosts()

    // Stats operations
    fun getStats() = statsDao.getStats()
    suspend fun updateStats(stats: PassStatsEntity) = statsDao.updateStats(stats)
    suspend fun incrementStats(xp: Int = 0, rewards: Int = 0, exclusive: Int = 0) {
        statsDao.incrementStats(xp, rewards, exclusive)
    }

    // Combined operations
    suspend fun clearAllData() {
        progressDao.clearProgress()
        claimedDao.clearAll()
    }
}

// ═════════════════════════════════════════════════════════════
// CONVERTERS
// ═════════════════════════════════════════════════════════════

class PassConverters {
    @TypeConverter
    fun fromRewardTrack(track: RewardTrack): String = track.name
    
    @TypeConverter
    fun toRewardTrack(track: String): RewardTrack = RewardTrack.valueOf(track)
    
    @TypeConverter
    fun fromPassRewardType(type: PassRewardType): String = type.name
    
    @TypeConverter
    fun toPassRewardType(type: String): PassRewardType = PassRewardType.valueOf(type)
}

// ═════════════════════════════════════════════════════════════
// EXTENSION FUNCTIONS
// ═════════════════════════════════════════════════════════════

fun ClaimedRewardEntity.toRewardId(): String = "${tier}_${track}"

fun String.toClaimedRewardId(): Pair<Int, String> {
    val parts = split("_")
    return Pair(parts[0].toInt(), parts[1])
}

fun createClaimedRewardEntity(tier: Int, track: RewardTrack, type: PassRewardType, name: String, value: Int = 0): ClaimedRewardEntity {
    return ClaimedRewardEntity(
        id = "${tier}_${track.name}",
        tier = tier,
        track = track.name,
        rewardType = type.name,
        rewardName = name,
        rewardValue = value
    )
}
