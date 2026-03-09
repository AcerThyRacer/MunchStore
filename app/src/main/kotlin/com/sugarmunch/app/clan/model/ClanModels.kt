package com.sugarmunch.app.clan.model

import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

// ═════════════════════════════════════════════════════════════════
// ENUMS
// ═════════════════════════════════════════════════════════════════

enum class ClanRole(val rank: Int, val canKick: Boolean, val canPromote: Boolean, val canInvite: Boolean) {
    LEADER(4, true, true, true),
    OFFICER(3, true, true, true),
    MEMBER(2, false, false, true),
    RECRUIT(1, false, false, false)
}

enum class WarStatus {
    PENDING,      // War scheduled but not started
    ACTIVE,       // War in progress
    ENDED,        // War ended, calculating results
    COMPLETED     // Results distributed
}

enum class ClanWarResult {
    VICTORY,
    DEFEAT,
    DRAW
}

enum class ClanMessageType {
    TEXT,
    SYSTEM_JOIN,
    SYSTEM_LEAVE,
    SYSTEM_PROMOTE,
    SYSTEM_DEMOTE,
    SYSTEM_KICK,
    APP_SHARE,
    THEME_SHARE,
    EFFECT_SHARE
}

enum class ClanRewardType {
    WAR_VICTORY,
    PARTICIPATION,
    CLAN_ACHIEVEMENT,
    MILESTONE,
    SEASONAL
}

enum class ClanJoinPolicy {
    OPEN,         // Anyone can join
    REQUEST,      // Requires approval
    INVITE_ONLY   // Only by invitation
}

// ═════════════════════════════════════════════════════════════════
// CLAN ENTITY
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "clans")
data class Clan(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val tag: String,                    // Short tag like [GLD]
    val description: String = "",
    val emblem: String = "🛡️",           // Emoji emblem
    val primaryColor: String = "#FFB6C1",
    val secondaryColor: String = "#98FF98",
    val level: Int = 1,
    val xp: Int = 0,
    val maxMembers: Int = 50,
    val joinPolicy: ClanJoinPolicy = ClanJoinPolicy.REQUEST,
    val minLevelToJoin: Int = 1,
    val trophies: Int = 0,              // Clan ranking points
    val warWins: Int = 0,
    val warLosses: Int = 0,
    val warDraws: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val isPublic: Boolean = true,
    val language: String = "en",
    val requirements: String = ""       // Free text requirements
)

// ═════════════════════════════════════════════════════════════════
// CLAN MEMBER ENTITY
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "clan_members")
data class ClanMember(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val clanId: String,
    val userId: String,
    val username: String,
    val displayName: String,
    val avatarEmoji: String = "🍭",
    val role: ClanRole = ClanRole.RECRUIT,
    val joinedAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    
    // Weekly contribution stats
    val weeklyInstalls: Int = 0,
    val weeklyAchievements: Int = 0,
    val weeklyPoints: Int = 0,
    val totalContributions: Int = 0,
    
    // Lifetime stats
    val totalWarParticipations: Int = 0,
    val warWinsContributed: Int = 0,
    val donatedThemes: Int = 0,
    val donatedEffects: Int = 0,
    
    val isOnline: Boolean = false,
    val statusMessage: String = ""
)

// ═════════════════════════════════════════════════════════════════
// CLAN WAR ENTITY
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "clan_wars")
data class ClanWar(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val seasonId: String,
    
    // Clan info
    val homeClanId: String,
    val homeClanName: String,
    val homeClanTag: String,
    val homeClanEmblem: String,
    
    // Enemy info
    val enemyClanId: String,
    val enemyClanName: String,
    val enemyClanTag: String,
    val enemyClanEmblem: String,
    
    // War stats
    val homeScore: Int = 0,
    val enemyScore: Int = 0,
    val status: WarStatus = WarStatus.PENDING,
    val result: ClanWarResult? = null,
    
    // Timing
    val startedAt: Long = System.currentTimeMillis(),
    val endsAt: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000), // 7 days
    val endedAt: Long? = null,
    
    // Rewards distributed
    val rewardsDistributed: Boolean = false,
    
    // Member contributions (stored as JSON)
    val contributionsJson: String = "[]"
)

// ═════════════════════════════════════════════════════════════════
// WAR CONTRIBUTION DATA
// ═════════════════════════════════════════════════════════════════

data class WarContribution(
    val userId: String,
    val username: String,
    val installs: Int = 0,
    val achievements: Int = 0,
    val points: Int = 0,
    val totalScore: Int = 0
)

// ═════════════════════════════════════════════════════════════════
// CLAN MESSAGE ENTITY
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "clan_messages")
data class ClanMessage(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val clanId: String,
    val userId: String? = null,         // Null for system messages
    val username: String? = null,
    val avatarEmoji: String? = null,
    val role: ClanRole? = null,
    
    val type: ClanMessageType = ClanMessageType.TEXT,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    
    // For rich messages
    val attachmentId: String? = null,   // App ID, Theme ID, etc.
    val attachmentName: String? = null,
    val attachmentPreview: String? = null,
    
    // Metadata
    val isDeleted: Boolean = false,
    val editedAt: Long? = null,
    val replyToMessageId: String? = null
)

// ═════════════════════════════════════════════════════════════════
// CLAN INVITATION ENTITY
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "clan_invitations")
data class ClanInvitation(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val clanId: String,
    val clanName: String,
    val clanTag: String,
    val invitedByUserId: String,
    val invitedByUsername: String,
    val invitedUserId: String,
    val invitedUsername: String,
    val message: String = "",
    val status: InvitationStatus = InvitationStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000), // 7 days
    val respondedAt: Long? = null
)

enum class InvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    EXPIRED
}

// ═════════════════════════════════════════════════════════════════
// JOIN REQUEST ENTITY
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "clan_join_requests")
data class ClanJoinRequest(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val clanId: String,
    val userId: String,
    val username: String,
    val userLevel: Int,
    val message: String = "",
    val status: JoinRequestStatus = JoinRequestStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val respondedAt: Long? = null,
    val respondedByUserId: String? = null
)

enum class JoinRequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}

// ═════════════════════════════════════════════════════════════════
// CLAN REWARD ENTITY
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "clan_rewards")
data class ClanReward(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val clanId: String,
    val type: ClanRewardType,
    val name: String,
    val description: String,
    val icon: String,
    val sugarPoints: Int = 0,
    val xpReward: Int = 0,
    val themeId: String? = null,
    val effectId: String? = null,
    val badgeId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val claimedAt: Long? = null,
    val claimedByUserId: String? = null,
    val isClaimed: Boolean = false
)

// ═════════════════════════════════════════════════════════════════
// CLAN SHOP ITEM
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "clan_shop_items")
data class ClanShopItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val type: ClanShopItemType,
    val icon: String,
    val costClanCurrency: Int,          // Clan-specific currency
    val minClanLevel: Int = 1,
    val stock: Int? = null,             // Null = unlimited
    val purchasedCount: Int = 0,
    val isActive: Boolean = true,
    val availableFrom: Long? = null,
    val availableUntil: Long? = null,
    val themeId: String? = null,
    val effectId: String? = null,
    val badgeId: String? = null
)

enum class ClanShopItemType {
    EXCLUSIVE_THEME,
    EXCLUSIVE_EFFECT,
    CLAN_BADGE,
    BOOST,
    EMBLEM,
    BORDER
}

// ═════════════════════════════════════════════════════════════════
// CLAN SEASON
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "clan_seasons")
data class ClanSeason(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val seasonNumber: Int,
    val name: String,
    val description: String,
    val theme: String,                  // Seasonal theme
    val startedAt: Long,
    val endsAt: Long,
    val isActive: Boolean = true,
    val rewardsJson: String = "[]"
)

// ═════════════════════════════════════════════════════════════════
// CLAN LEADERBOARD ENTRY
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "clan_leaderboard")
data class ClanLeaderboardEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val seasonId: String,
    val clanId: String,
    val clanName: String,
    val clanTag: String,
    val clanEmblem: String,
    val clanLevel: Int,
    val trophies: Int,
    val warWins: Int,
    val warLosses: Int,
    val totalMembers: Int,
    val rank: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

// ═════════════════════════════════════════════════════════════════
// CLAN ACHIEVEMENTS
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "clan_achievements")
data class ClanAchievement(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val requirementType: String,
    val requirementValue: Int,
    val rewardSugarPoints: Int = 0,
    val rewardClanXP: Int = 0,
    val order: Int = 0
)

@Entity(tableName = "clan_achievement_progress")
data class ClanAchievementProgress(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val clanId: String,
    val achievementId: String,
    val progress: Int = 0,
    val maxProgress: Int = 100,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
) {
    val progressPercent: Float get() = progress.toFloat() / maxProgress
}

// ═════════════════════════════════════════════════════════════════
// USER CLAN DATA (for current user's clan membership)
// ═════════════════════════════════════════════════════════════════

@Entity(tableName = "user_clan_data")
data class UserClanData(
    @PrimaryKey val userId: String,
    val clanId: String? = null,
    val role: ClanRole? = null,
    val joinedAt: Long? = null,
    val clanCurrency: Int = 0,          // Personal clan currency balance
    val totalClanContributions: Int = 0
)

// ═════════════════════════════════════════════════════════════════
// TYPE CONVERTERS
// ═════════════════════════════════════════════════════════════════

class ClanConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromClanRole(role: ClanRole): String = role.name

    @TypeConverter
    fun toClanRole(role: String): ClanRole = ClanRole.valueOf(role)

    @TypeConverter
    fun fromWarStatus(status: WarStatus): String = status.name

    @TypeConverter
    fun toWarStatus(status: String): WarStatus = WarStatus.valueOf(status)

    @TypeConverter
    fun fromClanWarResult(result: ClanWarResult?): String? = result?.name

    @TypeConverter
    fun toClanWarResult(result: String?): ClanWarResult? = result?.let { ClanWarResult.valueOf(it) }

    @TypeConverter
    fun fromClanMessageType(type: ClanMessageType): String = type.name

    @TypeConverter
    fun toClanMessageType(type: String): ClanMessageType = ClanMessageType.valueOf(type)

    @TypeConverter
    fun fromClanRewardType(type: ClanRewardType): String = type.name

    @TypeConverter
    fun toClanRewardType(type: String): ClanRewardType = ClanRewardType.valueOf(type)

    @TypeConverter
    fun fromClanJoinPolicy(policy: ClanJoinPolicy): String = policy.name

    @TypeConverter
    fun toClanJoinPolicy(policy: String): ClanJoinPolicy = ClanJoinPolicy.valueOf(policy)

    @TypeConverter
    fun fromInvitationStatus(status: InvitationStatus): String = status.name

    @TypeConverter
    fun toInvitationStatus(status: String): InvitationStatus = InvitationStatus.valueOf(status)

    @TypeConverter
    fun fromJoinRequestStatus(status: JoinRequestStatus): String = status.name

    @TypeConverter
    fun toJoinRequestStatus(status: String): JoinRequestStatus = JoinRequestStatus.valueOf(status)

    @TypeConverter
    fun fromClanShopItemType(type: ClanShopItemType): String = type.name

    @TypeConverter
    fun toClanShopItemType(type: String): ClanShopItemType = ClanShopItemType.valueOf(type)

    @TypeConverter
    fun fromWarContributions(contributions: List<WarContribution>): String {
        return gson.toJson(contributions)
    }

    @TypeConverter
    fun toWarContributions(json: String): List<WarContribution> {
        val type = object : TypeToken<List<WarContribution>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}

// ═════════════════════════════════════════════════════════════════
// UI MODELS (non-entity)
// ═════════════════════════════════════════════════════════════════

data class ClanStats(
    val totalMembers: Int,
    val onlineMembers: Int,
    val weeklyActivity: Int,
    val averageMemberLevel: Float,
    val topContributors: List<ClanMember>,
    val recentWars: List<ClanWar>
)

data class ClanWarSummary(
    val war: ClanWar,
    val homeContributions: List<WarContribution>,
    val enemyContributions: List<WarContribution>,
    val timeRemaining: Long,
    val userContribution: WarContribution?
)

data class ClanPreview(
    val clan: Clan,
    val memberCount: Int,
    val isJoinable: Boolean,
    val distance: Float? = null  // For location-based clans (future)
)

data class ClanActivity(
    val type: String,
    val userId: String,
    val username: String,
    val description: String,
    val timestamp: Long,
    val points: Int = 0
)
