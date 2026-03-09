package com.sugarmunch.app.clan

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.*
import com.sugarmunch.app.clan.model.*
import com.sugarmunch.app.clan.work.ClanWarWorker
import com.sugarmunch.app.data.local.AppDatabase
import com.sugarmunch.app.features.AchievementManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.TimeUnit

private val Context.warDataStore: DataStore<Preferences> by preferencesDataStore(name = "sugarmunch_clan_wars")

/**
 * ClanWarsManager - Weekly clan competitions
 * 
 * Features:
 * - Weekly clan wars/competitions
 * - Compete against other clans
 * - Scoring based on member activity (installs, achievements, points)
 * - War seasons with rewards
 * - Leaderboards for clans
 */
class ClanWarsManager private constructor(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private val dataStore = context.warDataStore
    private val database = AppDatabase.getDatabase(context)
    private val clanDao = database.clanDao()
    private val achievementManager = AchievementManager.getInstance(context)
    private val clanManager = ClanManager.getInstance(context)
    
    // Current user ID
    private val currentUserId: String
        get() = clanManager.let { 
            // Access through reflection or shared preference for simplicity
            "current_user"
        }
    
    // State flows
    private val _activeWar = MutableStateFlow<ClanWar?>(null)
    val activeWar: StateFlow<ClanWar?> = _activeWar.asStateFlow()
    
    private val _warHistory = MutableStateFlow<List<ClanWar>>(emptyList())
    val warHistory: StateFlow<List<ClanWar>> = _warHistory.asStateFlow()
    
    private val _currentSeason = MutableStateFlow<ClanSeason?>(null)
    val currentSeason: StateFlow<ClanSeason?> = _currentSeason.asStateFlow()
    
    private val _leaderboard = MutableStateFlow<List<ClanLeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<ClanLeaderboardEntry>> = _leaderboard.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _userContribution = MutableStateFlow<WarContribution?>(null)
    val userContribution: StateFlow<WarContribution?> = _userContribution.asStateFlow()
    
    // War configuration
    companion object {
        const val WAR_DURATION_DAYS = 7L
        const val WAR_BREAK_HOURS = 24L
        const val PREPARATION_HOURS = 24L
        
        // Scoring weights
        const val POINTS_PER_INSTALL = 10
        const val POINTS_PER_ACHIEVEMENT = 25
        const val POINTS_PER_ACTIVITY = 1
        
        @Volatile
        private var instance: ClanWarsManager? = null
        
        fun getInstance(context: Context): ClanWarsManager {
            return instance ?: synchronized(this) {
                instance ?: ClanWarsManager(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
        
        fun destroy() {
            instance = null
        }
    }
    
    init {
        scope.launch {
            initializeSeason()
            loadActiveWar()
            loadWarHistory()
            loadLeaderboard()
            scheduleWarWorkers()
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // SEASON MANAGEMENT
    // ═════════════════════════════════════════════════════════════════
    
    private suspend fun initializeSeason() {
        var season = clanDao.getActiveSeason()
        
        if (season == null || season.endsAt < System.currentTimeMillis()) {
            // Create new season
            val lastSeason = clanDao.getActiveSeason()
            val seasonNumber = (lastSeason?.seasonNumber ?: 0) + 1
            
            season = ClanSeason(
                seasonNumber = seasonNumber,
                name = generateSeasonName(seasonNumber),
                description = "Season $seasonNumber - Battle for glory!",
                theme = generateSeasonTheme(seasonNumber),
                startedAt = System.currentTimeMillis(),
                endsAt = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000), // 30 days
                isActive = true
            )
            
            // Deactivate old season
            lastSeason?.let {
                clanDao.updateSeason(it.copy(isActive = false))
            }
            
            clanDao.insertSeason(season)
        }
        
        _currentSeason.value = season
    }
    
    private fun generateSeasonName(number: Int): String {
        val themes = listOf(
            "Sugar Rush",
            "Candy Storm",
            "Sweet Victory",
            "Caramel Crusade",
            "Mint Madness",
            "Berry Blitz",
            "Chocolate Chaos",
            "Vanilla Vanguard"
        )
        return themes[(number - 1) % themes.size]
    }
    
    private fun generateSeasonTheme(number: Int): String {
        val colors = listOf(
            "#FFB6C1", // Pink
            "#98FF98", // Mint
            "#FFFACD", // Yellow
            "#DEB887", // Caramel
            "#B5DEFF", // Blue
            "#E6B3FF"  // Purple
        )
        return colors[(number - 1) % colors.size]
    }
    
    // ═════════════════════════════════════════════════════════════════
    // WAR MANAGEMENT
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun startWar(enemyClanId: String): ClanWar? {
        val clan = clanManager.currentClan.value ?: return null
        val currentWar = _activeWar.value
        
        // Check if already in a war
        if (currentWar != null && currentWar.status == WarStatus.ACTIVE) {
            return null
        }
        
        val enemyClan = clanDao.getClanById(enemyClanId) ?: return null
        
        val war = ClanWar(
            seasonId = _currentSeason.value?.id ?: "",
            homeClanId = clan.id,
            homeClanName = clan.name,
            homeClanTag = clan.tag,
            homeClanEmblem = clan.emblem,
            enemyClanId = enemyClanId,
            enemyClanName = enemyClan.name,
            enemyClanTag = enemyClan.tag,
            enemyClanEmblem = enemyClan.emblem,
            status = WarStatus.ACTIVE,
            endsAt = System.currentTimeMillis() + (WAR_DURATION_DAYS * 24 * 60 * 60 * 1000)
        )
        
        clanDao.insertWar(war)
        _activeWar.value = war
        
        // Send system message to clan
        val systemMessage = ClanMessage(
            clanId = clan.id,
            type = ClanMessageType.SYSTEM_JOIN,
            content = "⚔️ Clan War started against [${enemyClan.tag}] ${enemyClan.name}! Fight for victory!"
        )
        clanDao.insertMessage(systemMessage)
        
        return war
    }
    
    suspend fun endWar(warId: String): ClanWar? {
        val war = clanDao.getWarById(warId) ?: return null
        
        val homeScore = war.homeScore
        val enemyScore = war.enemyScore
        
        val result = when {
            homeScore > enemyScore -> ClanWarResult.VICTORY
            homeScore < enemyScore -> ClanWarResult.DEFEAT
            else -> ClanWarResult.DRAW
        }
        
        val endedWar = war.copy(
            status = WarStatus.ENDED,
            result = result,
            endedAt = System.currentTimeMillis()
        )
        
        clanDao.updateWar(endedWar)
        
        // Update clan stats
        val clan = clanDao.getClanById(war.homeClanId)
        clan?.let {
            val updatedClan = when (result) {
                ClanWarResult.VICTORY -> it.copy(
                    warWins = it.warWins + 1,
                    trophies = it.trophies + 30,
                    xp = it.xp + 100
                )
                ClanWarResult.DEFEAT -> it.copy(
                    warLosses = it.warLosses + 1,
                    trophies = (it.trophies - 15).coerceAtLeast(0),
                    xp = it.xp + 25
                )
                ClanWarResult.DRAW -> it.copy(
                    warDraws = it.warDraws + 1,
                    trophies = it.trophies + 5,
                    xp = it.xp + 50
                )
            }
            clanDao.updateClan(updatedClan)
            
            // Check for level up
            checkClanLevelUp(updatedClan)
        }
        
        // Distribute rewards
        distributeWarRewards(endedWar)
        
        // Send system message
        val resultEmoji = when (result) {
            ClanWarResult.VICTORY -> "🏆"
            ClanWarResult.DEFEAT -> "💔"
            ClanWarResult.DRAW -> "🤝"
        }
        val resultMessage = when (result) {
            ClanWarResult.VICTORY -> "Victory! We won the war!"
            ClanWarResult.DEFEAT -> "Defeat... We'll get them next time!"
            ClanWarResult.DRAW -> "It's a draw! Well fought!"
        }
        
        val systemMessage = ClanMessage(
            clanId = war.homeClanId,
            type = ClanMessageType.SYSTEM_JOIN,
            content = "$resultEmoji $resultMessage Final score: $homeScore - $enemyScore"
        )
        clanDao.insertMessage(systemMessage)
        
        _activeWar.value = null
        loadWarHistory()
        
        return endedWar
    }
    
    suspend fun getWarStatus(warId: String): WarStatus? {
        return clanDao.getWarById(warId)?.status
    }
    
    fun getWarTimeRemaining(war: ClanWar): Long {
        return (war.endsAt - System.currentTimeMillis()).coerceAtLeast(0)
    }
    
    fun formatTimeRemaining(millis: Long): String {
        val days = millis / (24 * 60 * 60 * 1000)
        val hours = (millis % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
        val minutes = (millis % (60 * 60 * 1000)) / (60 * 1000)
        
        return when {
            days > 0 -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // SCORING SYSTEM
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun calculateClanScore(clanId: String): Int {
        val members = clanDao.getClanMembers(clanId)
        return members.sumOf { calculateMemberScore(it) }
    }
    
    fun calculateMemberScore(member: ClanMember): Int {
        return (member.weeklyInstalls * POINTS_PER_INSTALL) +
               (member.weeklyAchievements * POINTS_PER_ACHIEVEMENT) +
               member.weeklyPoints
    }
    
    suspend fun trackInstall(userId: String) {
        val member = clanDao.getMemberByUserId(userId) ?: return
        val updatedMember = member.copy(
            weeklyInstalls = member.weeklyInstalls + 1,
            totalContributions = member.totalContributions + POINTS_PER_INSTALL
        )
        clanDao.updateMember(updatedMember)
        
        updateWarScore(updatedMember.clanId)
    }
    
    suspend fun trackAchievement(userId: String) {
        val member = clanDao.getMemberByUserId(userId) ?: return
        val updatedMember = member.copy(
            weeklyAchievements = member.weeklyAchievements + 1,
            totalContributions = member.totalContributions + POINTS_PER_ACHIEVEMENT
        )
        clanDao.updateMember(updatedMember)
        
        updateWarScore(updatedMember.clanId)
    }
    
    suspend fun trackActivity(userId: String, points: Int = POINTS_PER_ACTIVITY) {
        val member = clanDao.getMemberByUserId(userId) ?: return
        val updatedMember = member.copy(
            weeklyPoints = member.weeklyPoints + points,
            totalContributions = member.totalContributions + points
        )
        clanDao.updateMember(updatedMember)
        
        updateWarScore(updatedMember.clanId)
    }
    
    private suspend fun updateWarScore(clanId: String) {
        val activeWar = clanDao.getActiveWar(clanId) ?: return
        val score = calculateClanScore(clanId)
        
        val updatedWar = if (activeWar.homeClanId == clanId) {
            activeWar.copy(homeScore = score)
        } else {
            activeWar.copy(enemyScore = score)
        }
        
        clanDao.updateWar(updatedWar)
        
        if (_activeWar.value?.id == updatedWar.id) {
            _activeWar.value = updatedWar
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // REWARDS DISTRIBUTION
    // ═════════════════════════════════════════════════════════════════
    
    private suspend fun distributeWarRewards(war: ClanWar) {
        val clan = clanDao.getClanById(war.homeClanId) ?: return
        val members = clanDao.getClanMembers(clan.id)
        
        // Calculate rewards based on result
        val (basePoints, baseXP) = when (war.result) {
            ClanWarResult.VICTORY -> 100 to 50
            ClanWarResult.DRAW -> 50 to 25
            ClanWarResult.DEFEAT -> 25 to 10
            else -> 0 to 0
        }
        
        // Get top contributors
        val contributions = members.map { 
            WarContribution(
                userId = it.userId,
                username = it.username,
                installs = it.weeklyInstalls,
                achievements = it.weeklyAchievements,
                points = it.weeklyPoints,
                totalScore = calculateMemberScore(it)
            )
        }.sortedByDescending { it.totalScore }
        
        // Distribute individual rewards
        contributions.forEachIndexed { index, contribution ->
            val multiplier = when (index) {
                0 -> 2.0f // MVP gets double
                1, 2 -> 1.5f // Top 3 get 1.5x
                else -> 1.0f
            }
            
            val points = (basePoints * multiplier).toInt()
            val xp = (baseXP * multiplier).toInt()
            
            // Create reward record
            val reward = ClanReward(
                clanId = clan.id,
                type = ClanRewardType.WAR_VICTORY,
                name = when (war.result) {
                    ClanWarResult.VICTORY -> "War Victory Reward"
                    ClanWarResult.DRAW -> "War Participation Reward"
                    ClanWarResult.DEFEAT -> "War Effort Reward"
                },
                description = "Reward for participating in the war against [${war.enemyClanTag}]",
                icon = when (war.result) {
                    ClanWarResult.VICTORY -> "🏆"
                    ClanWarResult.DRAW -> "🤝"
                    ClanWarResult.DEFEAT -> "🛡️"
                },
                sugarPoints = points,
                xpReward = xp
            )
            
            clanDao.insertReward(reward)
            
            // Add clan currency to user
            clanDao.addClanCurrency(contribution.userId, points)
            
            // Track achievements for top contributors
            if (index == 0) {
                achievementManager.unlockAchievement("war_mvp")
            }
        }
        
        // Mark war as completed
        clanDao.updateWar(war.copy(
            status = WarStatus.COMPLETED,
            rewardsDistributed = true,
            contributionsJson = com.google.gson.Gson().toJson(contributions)
        ))
        
        // Reset weekly stats
        clanDao.resetWeeklyStats(clan.id)
    }
    
    suspend fun distributeSeasonRewards(seasonId: String) {
        val season = _currentSeason.value ?: return
        if (season.id != seasonId) return
        
        val topClans = clanDao.getLeaderboard(seasonId, limit = 10)
        
        topClans.forEachIndexed { index, entry ->
            val members = clanDao.getClanMembers(entry.clanId)
            
            val (points, title) = when (index) {
                0 -> 1000 to "Season Champion"
                1 -> 750 to "Season Runner-up"
                2 -> 500 to "Season Third Place"
                in 3..4 -> 300 to "Season Top 5"
                in 5..9 -> 150 to "Season Top 10"
                else -> 0 to "Season Participant"
            }
            
            members.forEach { member ->
                val reward = ClanReward(
                    clanId = entry.clanId,
                    type = ClanRewardType.SEASONAL,
                    name = title,
                    description = "Season ${season.seasonNumber} reward for achieving rank #${entry.rank}",
                    icon = "👑",
                    sugarPoints = points
                )
                clanDao.insertReward(reward)
                clanDao.addClanCurrency(member.userId, points)
            }
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // LEADERBOARD
    // ═════════════════════════════════════════════════════════════════
    
    private suspend fun loadLeaderboard() {
        val season = _currentSeason.value ?: return
        _leaderboard.value = clanDao.getLeaderboard(season.id, limit = 100)
    }
    
    suspend fun refreshLeaderboard() {
        val season = _currentSeason.value ?: return
        
        // Get all clans and calculate their rankings
        val clans = clanDao.getTopClans(1000)
        
        val entries = clans.map { clan ->
            val memberCount = clanDao.getMemberCount(clan.id)
            ClanLeaderboardEntry(
                seasonId = season.id,
                clanId = clan.id,
                clanName = clan.name,
                clanTag = clan.tag,
                clanEmblem = clan.emblem,
                clanLevel = clan.level,
                trophies = clan.trophies,
                warWins = clan.warWins,
                warLosses = clan.warLosses,
                totalMembers = memberCount,
                rank = 0 // Will be set after sorting
            )
        }.sortedByDescending { it.trophies }
            .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
        
        // Save to database
        entries.forEach { clanDao.insertLeaderboardEntry(it) }
        
        _leaderboard.value = entries
    }
    
    // ═════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═════════════════════════════════════════════════════════════════
    
    private suspend fun checkClanLevelUp(clan: Clan) {
        val xpNeeded = getXPForNextLevel(clan.level)
        
        if (clan.xp >= xpNeeded) {
            val newLevel = clan.level + 1
            val updatedClan = clan.copy(
                level = newLevel,
                maxMembers = (clan.maxMembers + 2).coerceAtMost(100)
            )
            clanDao.updateClan(updatedClan)
            
            // Notify members
            val systemMessage = ClanMessage(
                clanId = clan.id,
                type = ClanMessageType.SYSTEM_JOIN,
                content = "🎉 Clan leveled up to Level $newLevel! Max members increased to ${updatedClan.maxMembers}!"
            )
            clanDao.insertMessage(systemMessage)
        }
    }
    
    fun getXPForNextLevel(currentLevel: Int): Int {
        return currentLevel * 500
    }
    
    private suspend fun loadActiveWar() {
        val clan = clanManager.currentClan.value ?: return
        _activeWar.value = clanDao.getActiveWar(clan.id)
    }
    
    private suspend fun loadWarHistory() {
        val clan = clanManager.currentClan.value ?: return
        _warHistory.value = clanDao.getClanWars(clan.id)
            .filter { it.status == WarStatus.COMPLETED }
    }
    
    private fun scheduleWarWorkers() {
        ClanWarWorker.schedule(context)
    }
    
    // ═════════════════════════════════════════════════════════════════
    // WAR SUMMARY
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun getWarSummary(warId: String): ClanWarSummary? {
        val war = clanDao.getWarById(warId) ?: return null
        val contributions = com.google.gson.Gson().fromJson(
            war.contributionsJson,
            Array<WarContribution>::class.java
        )?.toList() ?: emptyList()
        
        return ClanWarSummary(
            war = war,
            homeContributions = contributions,
            enemyContributions = emptyList(), // Would fetch from server
            timeRemaining = getWarTimeRemaining(war),
            userContribution = contributions.find { it.userId == currentUserId }
        )
    }
    
    // ═════════════════════════════════════════════════════════════════
    // MATCHMAKING (simplified - would connect to server in real implementation)
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun findWarOpponent(): Clan? {
        val clan = clanManager.currentClan.value ?: return null
        
        // Get clans with similar trophy count
        val potentialOpponents = clanDao.getTopClans(100)
            .filter { it.id != clan.id }
            .filter { 
                val trophyDiff = Math.abs(it.trophies - clan.trophies)
                trophyDiff < 100 // Within 100 trophies
            }
        
        return potentialOpponents.randomOrNull()
    }
}
