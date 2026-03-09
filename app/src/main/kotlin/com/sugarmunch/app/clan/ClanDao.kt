package com.sugarmunch.app.clan

import androidx.room.*
import com.sugarmunch.app.clan.model.Clan as ClanEntity
import com.sugarmunch.app.clan.model.ClanAchievement
import com.sugarmunch.app.clan.model.ClanAchievementProgress
import com.sugarmunch.app.clan.model.ClanInvitation
import com.sugarmunch.app.clan.model.ClanJoinPolicy
import com.sugarmunch.app.clan.model.ClanJoinRequest
import com.sugarmunch.app.clan.model.ClanLeaderboardEntry
import com.sugarmunch.app.clan.model.ClanMember as ClanMemberEntity
import com.sugarmunch.app.clan.model.ClanMessage
import com.sugarmunch.app.clan.model.ClanRole
import com.sugarmunch.app.clan.model.ClanReward
import com.sugarmunch.app.clan.model.ClanSeason
import com.sugarmunch.app.clan.model.ClanShopItem
import com.sugarmunch.app.clan.model.ClanWar
import com.sugarmunch.app.clan.model.InvitationStatus
import com.sugarmunch.app.clan.model.JoinRequestStatus
import com.sugarmunch.app.clan.model.UserClanData
import com.sugarmunch.app.clan.model.WarStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ClanDao {
    
    // ═════════════════════════════════════════════════════════════════
    // CLAN OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM clans WHERE id = :clanId")
    suspend fun getClanById(clanId: String): ClanEntity?
    
    @Query("SELECT * FROM clans WHERE id = :clanId")
    fun getClanByIdFlow(clanId: String): Flow<ClanEntity?>
    
    @Query("SELECT * FROM clans ORDER BY trophies DESC LIMIT :limit")
    suspend fun getTopClans(limit: Int = 100): List<ClanEntity>
    
    @Query("SELECT * FROM clans ORDER BY trophies DESC LIMIT :limit")
    fun getTopClansFlow(limit: Int = 100): Flow<List<ClanEntity>>
    
    @Query("SELECT * FROM clans WHERE name LIKE :query OR tag LIKE :query ORDER BY trophies DESC")
    suspend fun searchClans(query: String): List<ClanEntity>
    
    @Query("SELECT * FROM clans WHERE joinPolicy = :policy ORDER BY trophies DESC")
    suspend fun getClansByJoinPolicy(policy: ClanJoinPolicy): List<ClanEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClan(clan: ClanEntity)
    
    @Update
    suspend fun updateClan(clan: ClanEntity)
    
    @Delete
    suspend fun deleteClan(clan: ClanEntity)
    
    @Query("DELETE FROM clans WHERE id = :clanId")
    suspend fun deleteClanById(clanId: String)
    
    // ═════════════════════════════════════════════════════════════════
    // CLAN MEMBER OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM clan_members WHERE clanId = :clanId ORDER BY role DESC, joinedAt ASC")
    suspend fun getClanMembers(clanId: String): List<ClanMemberEntity>
    
    @Query("SELECT * FROM clan_members WHERE clanId = :clanId ORDER BY role DESC, joinedAt ASC")
    fun getClanMembersFlow(clanId: String): Flow<List<ClanMemberEntity>>
    
    @Query("SELECT * FROM clan_members WHERE clanId = :clanId AND role IN (:roles)")
    suspend fun getClanMembersByRoles(clanId: String, roles: List<ClanRole>): List<ClanMemberEntity>
    
    @Query("SELECT * FROM clan_members WHERE userId = :userId LIMIT 1")
    suspend fun getMemberByUserId(userId: String): ClanMemberEntity?
    
    @Query("SELECT * FROM clan_members WHERE userId = :userId LIMIT 1")
    fun getMemberByUserIdFlow(userId: String): Flow<ClanMemberEntity?>
    
    @Query("SELECT COUNT(*) FROM clan_members WHERE clanId = :clanId")
    suspend fun getMemberCount(clanId: String): Int
    
    @Query("SELECT COUNT(*) FROM clan_members WHERE clanId = :clanId AND isOnline = 1")
    suspend fun getOnlineMemberCount(clanId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: ClanMemberEntity)
    
    @Update
    suspend fun updateMember(member: ClanMemberEntity)
    
    @Delete
    suspend fun deleteMember(member: ClanMemberEntity)
    
    @Query("DELETE FROM clan_members WHERE clanId = :clanId AND userId = :userId")
    suspend fun removeMember(clanId: String, userId: String)
    
    @Query("DELETE FROM clan_members WHERE clanId = :clanId")
    suspend fun removeAllMembers(clanId: String)
    
    @Query("SELECT * FROM clan_members WHERE clanId = :clanId ORDER BY weeklyPoints DESC LIMIT :limit")
    suspend fun getTopContributors(clanId: String, limit: Int = 10): List<ClanMemberEntity>
    
    @Query("UPDATE clan_members SET weeklyInstalls = 0, weeklyAchievements = 0, weeklyPoints = 0 WHERE clanId = :clanId")
    suspend fun resetWeeklyStats(clanId: String)
    
    // ═════════════════════════════════════════════════════════════════
    // CLAN WAR OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM clan_wars WHERE id = :warId")
    suspend fun getWarById(warId: String): ClanWar?
    
    @Query("SELECT * FROM clan_wars WHERE homeClanId = :clanId OR enemyClanId = :clanId ORDER BY startedAt DESC")
    suspend fun getClanWars(clanId: String): List<ClanWar>
    
    @Query("SELECT * FROM clan_wars WHERE homeClanId = :clanId OR enemyClanId = :clanId ORDER BY startedAt DESC")
    fun getClanWarsFlow(clanId: String): Flow<List<ClanWar>>
    
    @Query("SELECT * FROM clan_wars WHERE (homeClanId = :clanId OR enemyClanId = :clanId) AND status = :status")
    suspend fun getWarsByStatus(clanId: String, status: WarStatus): List<ClanWar>
    
    @Query("SELECT * FROM clan_wars WHERE (homeClanId = :clanId OR enemyClanId = :clanId) AND status = :status LIMIT 1")
    suspend fun getActiveWar(clanId: String, status: WarStatus = WarStatus.ACTIVE): ClanWar?
    
    @Query("SELECT * FROM clan_wars WHERE (homeClanId = :clanId OR enemyClanId = :clanId) AND status = :status")
    fun getActiveWarFlow(clanId: String, status: WarStatus = WarStatus.ACTIVE): Flow<ClanWar?>
    
    @Query("SELECT * FROM clan_wars WHERE status = :status")
    suspend fun getAllWarsByStatus(status: WarStatus): List<ClanWar>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWar(war: ClanWar)
    
    @Update
    suspend fun updateWar(war: ClanWar)
    
    @Delete
    suspend fun deleteWar(war: ClanWar)
    
    // ═════════════════════════════════════════════════════════════════
    // CLAN MESSAGE OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM clan_messages WHERE clanId = :clanId AND isDeleted = 0 ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(clanId: String, limit: Int = 100): List<ClanMessage>
    
    @Query("SELECT * FROM clan_messages WHERE clanId = :clanId AND isDeleted = 0 ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMessagesFlow(clanId: String, limit: Int = 100): Flow<List<ClanMessage>>
    
    @Query("SELECT * FROM clan_messages WHERE clanId = :clanId AND timestamp > :afterTimestamp AND isDeleted = 0 ORDER BY timestamp ASC")
    suspend fun getMessagesAfter(clanId: String, afterTimestamp: Long): List<ClanMessage>
    
    @Query("SELECT * FROM clan_messages WHERE clanId = :clanId AND timestamp < :beforeTimestamp AND isDeleted = 0 ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getMessagesBefore(clanId: String, beforeTimestamp: Long, limit: Int = 50): List<ClanMessage>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ClanMessage)
    
    @Update
    suspend fun updateMessage(message: ClanMessage)
    
    @Query("UPDATE clan_messages SET isDeleted = 1 WHERE id = :messageId")
    suspend fun markMessageDeleted(messageId: String)
    
    @Query("DELETE FROM clan_messages WHERE clanId = :clanId AND timestamp < :beforeTimestamp")
    suspend fun deleteOldMessages(clanId: String, beforeTimestamp: Long)
    
    // ═════════════════════════════════════════════════════════════════
    // INVITATION OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM clan_invitations WHERE invitedUserId = :userId AND status = :status")
    suspend fun getPendingInvitationsForUser(userId: String, status: InvitationStatus = InvitationStatus.PENDING): List<ClanInvitation>
    
    @Query("SELECT * FROM clan_invitations WHERE invitedUserId = :userId AND status = :status")
    fun getPendingInvitationsForUserFlow(userId: String, status: InvitationStatus = InvitationStatus.PENDING): Flow<List<ClanInvitation>>
    
    @Query("SELECT * FROM clan_invitations WHERE clanId = :clanId AND status = :status")
    suspend fun getPendingInvitationsForClan(clanId: String, status: InvitationStatus = InvitationStatus.PENDING): List<ClanInvitation>
    
    @Query("SELECT COUNT(*) FROM clan_invitations WHERE clanId = :clanId AND invitedUserId = :userId AND status = :status")
    suspend fun hasPendingInvitation(clanId: String, userId: String, status: InvitationStatus = InvitationStatus.PENDING): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvitation(invitation: ClanInvitation)
    
    @Update
    suspend fun updateInvitation(invitation: ClanInvitation)
    
    @Query("UPDATE clan_invitations SET status = :status, respondedAt = :respondedAt WHERE id = :invitationId")
    suspend fun respondToInvitation(invitationId: String, status: InvitationStatus, respondedAt: Long)
    
    @Query("DELETE FROM clan_invitations WHERE expiresAt < :currentTime AND status = :status")
    suspend fun deleteExpiredInvitations(currentTime: Long, status: InvitationStatus = InvitationStatus.PENDING)
    
    // ═════════════════════════════════════════════════════════════════
    // JOIN REQUEST OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM clan_join_requests WHERE clanId = :clanId AND status = :status")
    suspend fun getPendingJoinRequests(clanId: String, status: JoinRequestStatus = JoinRequestStatus.PENDING): List<ClanJoinRequest>
    
    @Query("SELECT * FROM clan_join_requests WHERE clanId = :clanId AND status = :status")
    fun getPendingJoinRequestsFlow(clanId: String, status: JoinRequestStatus = JoinRequestStatus.PENDING): Flow<List<ClanJoinRequest>>
    
    @Query("SELECT * FROM clan_join_requests WHERE userId = :userId AND clanId = :clanId LIMIT 1")
    suspend fun getJoinRequest(userId: String, clanId: String): ClanJoinRequest?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJoinRequest(request: ClanJoinRequest)
    
    @Update
    suspend fun updateJoinRequest(request: ClanJoinRequest)
    
    @Query("UPDATE clan_join_requests SET status = :status, respondedAt = :respondedAt, respondedByUserId = :respondedBy WHERE id = :requestId")
    suspend fun respondToJoinRequest(requestId: String, status: JoinRequestStatus, respondedAt: Long, respondedBy: String)
    
    // ═════════════════════════════════════════════════════════════════
    // REWARD OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM clan_rewards WHERE clanId = :clanId ORDER BY createdAt DESC")
    suspend fun getClanRewards(clanId: String): List<ClanReward>
    
    @Query("SELECT * FROM clan_rewards WHERE clanId = :clanId AND isClaimed = 0")
    suspend fun getUnclaimedRewards(clanId: String): List<ClanReward>
    
    @Query("SELECT * FROM clan_rewards WHERE clanId = :clanId AND isClaimed = 0")
    fun getUnclaimedRewardsFlow(clanId: String): Flow<List<ClanReward>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: ClanReward)
    
    @Query("UPDATE clan_rewards SET isClaimed = 1, claimedAt = :claimedAt, claimedByUserId = :userId WHERE id = :rewardId")
    suspend fun claimReward(rewardId: String, userId: String, claimedAt: Long)
    
    // ═════════════════════════════════════════════════════════════════
    // CLAN SHOP OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM clan_shop_items WHERE isActive = 1 ORDER BY minClanLevel ASC")
    suspend fun getActiveShopItems(): List<ClanShopItem>
    
    @Query("SELECT * FROM clan_shop_items WHERE isActive = 1 AND minClanLevel <= :clanLevel ORDER BY minClanLevel ASC")
    suspend fun getAvailableShopItems(clanLevel: Int): List<ClanShopItem>
    
    @Query("SELECT * FROM clan_shop_items WHERE id = :itemId")
    suspend fun getShopItem(itemId: String): ClanShopItem?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShopItem(item: ClanShopItem)
    
    @Query("UPDATE clan_shop_items SET purchasedCount = purchasedCount + 1 WHERE id = :itemId")
    suspend fun incrementPurchaseCount(itemId: String)
    
    // ═════════════════════════════════════════════════════════════════
    // SEASON OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM clan_seasons WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSeason(): ClanSeason?
    
    @Query("SELECT * FROM clan_seasons WHERE isActive = 1 LIMIT 1")
    fun getActiveSeasonFlow(): Flow<ClanSeason?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeason(season: ClanSeason)
    
    @Update
    suspend fun updateSeason(season: ClanSeason)
    
    // ═════════════════════════════════════════════════════════════════
    // LEADERBOARD OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM clan_leaderboard WHERE seasonId = :seasonId ORDER BY rank ASC LIMIT :limit")
    suspend fun getLeaderboard(seasonId: String, limit: Int = 100): List<ClanLeaderboardEntry>
    
    @Query("SELECT * FROM clan_leaderboard WHERE seasonId = :seasonId ORDER BY rank ASC LIMIT :limit")
    fun getLeaderboardFlow(seasonId: String, limit: Int = 100): Flow<List<ClanLeaderboardEntry>>
    
    @Query("SELECT * FROM clan_leaderboard WHERE clanId = :clanId AND seasonId = :seasonId LIMIT 1")
    suspend fun getClanLeaderboardEntry(clanId: String, seasonId: String): ClanLeaderboardEntry?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntry(entry: ClanLeaderboardEntry)
    
    @Query("DELETE FROM clan_leaderboard WHERE seasonId = :seasonId")
    suspend fun clearLeaderboard(seasonId: String)
    
    // ═════════════════════════════════════════════════════════════════
    // ACHIEVEMENT OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM clan_achievements ORDER BY `order` ASC")
    suspend fun getAllAchievements(): List<ClanAchievement>
    
    @Query("SELECT * FROM clan_achievement_progress WHERE clanId = :clanId")
    suspend fun getClanAchievementProgress(clanId: String): List<ClanAchievementProgress>
    
    @Query("SELECT * FROM clan_achievement_progress WHERE clanId = :clanId AND achievementId = :achievementId LIMIT 1")
    suspend fun getAchievementProgress(clanId: String, achievementId: String): ClanAchievementProgress?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: ClanAchievement)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievementProgress(progress: ClanAchievementProgress)
    
    @Update
    suspend fun updateAchievementProgress(progress: ClanAchievementProgress)
    
    // ═════════════════════════════════════════════════════════════════
    // USER CLAN DATA OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM user_clan_data WHERE userId = :userId LIMIT 1")
    suspend fun getUserClanData(userId: String): UserClanData?
    
    @Query("SELECT * FROM user_clan_data WHERE userId = :userId LIMIT 1")
    fun getUserClanDataFlow(userId: String): Flow<UserClanData?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserClanData(data: UserClanData)
    
    @Update
    suspend fun updateUserClanData(data: UserClanData)
    
    @Query("UPDATE user_clan_data SET clanCurrency = clanCurrency + :amount WHERE userId = :userId")
    suspend fun addClanCurrency(userId: String, amount: Int)
    
    @Query("UPDATE user_clan_data SET clanCurrency = clanCurrency - :amount WHERE userId = :userId")
    suspend fun spendClanCurrency(userId: String, amount: Int)
    
    // ═════════════════════════════════════════════════════════════════
    // TRANSACTION OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    @Transaction
    suspend fun leaveClanTransaction(userId: String, clanId: String) {
        removeMember(clanId, userId)
        val remainingMembers = getMemberCount(clanId)
        if (remainingMembers == 0) {
            deleteClanById(clanId)
        }
    }
    
    @Transaction
    suspend fun disbandClanTransaction(clanId: String) {
        removeAllMembers(clanId)
        deleteClanById(clanId)
    }
}
