package com.sugarmunch.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sugarmunch.app.clan.ClanDao
import com.sugarmunch.app.clan.model.*
import com.sugarmunch.app.automation.AutomationTaskEntity
import com.sugarmunch.app.automation.ExecutionHistoryEntity
import com.sugarmunch.app.automation.AutomationConverters
import com.sugarmunch.app.data.local.dao.AchievementDao
import com.sugarmunch.app.data.local.dao.SugarEffectDao
import com.sugarmunch.app.data.local.dao.FabConfigDao
import com.sugarmunch.app.data.local.entity.AchievementEntity
import com.sugarmunch.app.data.local.entity.SugarEffectEntity
import com.sugarmunch.app.data.local.entity.FabConfigEntity

@Database(
    entities = [
        AppEntity::class,
        PredictionEntity::class,
        AppUsageEntity::class,
        CachedAppEntity::class,
        InstallHistoryEntity::class,
        SyncStateEntity::class,
        // Clan entities
        Clan::class,
        ClanMember::class,
        ClanWar::class,
        ClanMessage::class,
        ClanInvitation::class,
        ClanJoinRequest::class,
        ClanReward::class,
        ClanShopItem::class,
        ClanSeason::class,
        ClanLeaderboardEntry::class,
        ClanAchievement::class,
        ClanAchievementProgress::class,
        UserClanData::class,
        // Automation entities
        AutomationTaskEntity::class,
        ExecutionHistoryEntity::class,
        // Folder entities
        FolderEntity::class,
        // SugarMunch Extreme entities (v9)
        AchievementEntity::class,
        SugarEffectEntity::class,
        FabConfigEntity::class
    ],
    version = 9,
    exportSchema = true
)
@TypeConverters(ClanConverters::class, AutomationConverters::class, FolderConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun predictionDao(): PredictionDao
    abstract fun appUsageDao(): AppUsageDao
    abstract fun cachedAppDao(): CachedAppDao
    abstract fun installHistoryDao(): InstallHistoryDao
    abstract fun syncStateDao(): SyncStateDao
    abstract fun clanDao(): ClanDao
    abstract fun automationDao(): com.sugarmunch.app.automation.AutomationDao
    abstract fun folderDao(): FolderDao
    // SugarMunch Extreme DAOs (v9)
    abstract fun achievementDao(): AchievementDao
    abstract fun sugarEffectDao(): SugarEffectDao
    abstract fun fabConfigDao(): FabConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Migration 8→9: Add SugarMunch Extreme tables (achievements, sugar_effects, fab_config) */
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Achievements table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `achievements` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `iconResId` INTEGER NOT NULL,
                        `isUnlocked` INTEGER NOT NULL DEFAULT 0,
                        `unlockedAt` INTEGER,
                        `progress` INTEGER NOT NULL DEFAULT 0,
                        `maxProgress` INTEGER NOT NULL DEFAULT 100,
                        `rarity` TEXT NOT NULL DEFAULT 'COMMON',
                        `requirementType` TEXT NOT NULL DEFAULT '',
                        `requirementValue` INTEGER NOT NULL DEFAULT 0,
                        `hidden` INTEGER NOT NULL DEFAULT 0,
                        `order` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // Sugar effects table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sugar_effects` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `isEnabled` INTEGER NOT NULL DEFAULT 0,
                        `intensity` REAL NOT NULL DEFAULT 1.0,
                        `config` TEXT NOT NULL DEFAULT '{}',
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // FAB config table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `fab_config` (
                        `id` TEXT NOT NULL,
                        `style` TEXT NOT NULL DEFAULT 'GUMBALL_MACHINE',
                        `candyType` TEXT NOT NULL DEFAULT 'GUMBALL',
                        `trailEffect` TEXT NOT NULL DEFAULT 'SPARKLE',
                        `size` REAL NOT NULL DEFAULT 1.0,
                        `opacity` REAL NOT NULL DEFAULT 1.0,
                        `shortcutEffects` TEXT NOT NULL DEFAULT '[]',
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // Insert default FAB config
                db.execSQL("""
                    INSERT INTO `fab_config` (`id`, `style`, `candyType`, `trailEffect`, `size`, `opacity`, `shortcutEffects`)
                    VALUES ('default', 'GUMBALL_MACHINE', 'GUMBALL', 'SPARKLE', 1.0, 1.0, '[]')
                """.trimIndent())
            }
        }

        /** Migration 7→8: Add folders table */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `folders` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `parentId` TEXT,
                        `iconResId` INTEGER,
                        `iconColor` TEXT,
                        `folderStyle` TEXT NOT NULL DEFAULT 'DEFAULT',
                        `backgroundColor` TEXT,
                        `sortOrder` INTEGER NOT NULL DEFAULT 0,
                        `isSystemFolder` INTEGER NOT NULL DEFAULT 0,
                        `isExpanded` INTEGER NOT NULL DEFAULT 1,
                        `appIds` TEXT NOT NULL DEFAULT '[]',
                        `subFolderIds` TEXT NOT NULL DEFAULT '[]',
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        /** No-op migration 6→7. Use proper migrations for future schema changes. */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Schema unchanged; placeholder for migration path.
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sugarmunch_db"
                ).addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
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
