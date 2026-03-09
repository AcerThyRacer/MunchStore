package com.sugarmunch.app.clan

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.*
import com.sugarmunch.app.clan.model.*
import com.sugarmunch.app.clan.work.ClanSyncWorker
import com.sugarmunch.app.data.local.AppDatabase
import com.sugarmunch.app.features.AchievementManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.TimeUnit

private val Context.clanDataStore: DataStore<Preferences> by preferencesDataStore(name = "sugarmunch_clan")

/**
 * ClanManager - Complete clan management system
 * 
 * Features:
 * - Create, join, leave clans
 * - Clan roles (Leader, Officer, Member, Recruit)
 * - Clan search and discovery
 * - Invitation system
 * - Clan size limits (max 50 members)
 */
class ClanManager private constructor(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private val dataStore = context.clanDataStore
    private val database = AppDatabase.getDatabase(context)
    private val clanDao = database.clanDao()
    private val achievementManager = AchievementManager.getInstance(context)
    
    // Current user ID (would come from auth in real implementation)
    private val currentUserId: String
        get() = dataStore.data.map { it[USER_ID_KEY] ?: "user_${UUID.randomUUID()}" }
            .stateIn(scope, SharingStarted.Eagerly, "")
            .value
    
    // State flows
    private val _currentClan = MutableStateFlow<Clan?>(null)
    val currentClan: StateFlow<Clan?> = _currentClan.asStateFlow()
    
    private val _currentMember = MutableStateFlow<ClanMember?>(null)
    val currentMember: StateFlow<ClanMember?> = _currentMember.asStateFlow()
    
    private val _clanMembers = MutableStateFlow<List<ClanMember>>(emptyList())
    val clanMembers: StateFlow<List<ClanMember>> = _clanMembers.asStateFlow()
    
    private val _pendingInvitations = MutableStateFlow<List<ClanInvitation>>(emptyList())
    val pendingInvitations: StateFlow<List<ClanInvitation>> = _pendingInvitations.asStateFlow()
    
    private val _pendingJoinRequests = MutableStateFlow<List<ClanJoinRequest>>(emptyList())
    val pendingJoinRequests: StateFlow<List<ClanJoinRequest>> = _pendingJoinRequests.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        scope.launch {
            loadCurrentClan()
            scheduleClanSync()
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═════════════════════════════════════════════════════════════════
    
    private suspend fun loadCurrentClan() {
        val userClanData = clanDao.getUserClanData(currentUserId)
        userClanData?.clanId?.let { clanId ->
            refreshClanData(clanId)
        }
    }
    
    private fun refreshClanData(clanId: String) {
        scope.launch {
            _currentClan.value = clanDao.getClanById(clanId)
            _currentMember.value = clanDao.getMemberByUserId(currentUserId)
            _clanMembers.value = clanDao.getClanMembers(clanId)
            
            // Load pending invitations if officer/leader
            if (canManageInvitations()) {
                _pendingJoinRequests.value = clanDao.getPendingJoinRequests(clanId)
            }
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // CLAN CREATION
    // ═════════════════════════════════════════════════════════════════
    
    data class CreateClanResult(
        val success: Boolean,
        val clan: Clan? = null,
        val error: String? = null
    )
    
    suspend fun createClan(
        name: String,
        tag: String,
        description: String = "",
        emblem: String = "🛡️",
        primaryColor: String = "#FFB6C1",
        secondaryColor: String = "#98FF98",
        joinPolicy: ClanJoinPolicy = ClanJoinPolicy.REQUEST,
        minLevelToJoin: Int = 1
    ): CreateClanResult {
        _isLoading.value = true
        _error.value = null
        
        return try {
            // Validation
            when {
                name.length < 3 -> return CreateClanResult(
                    false, error = "Clan name must be at least 3 characters"
                )
                name.length > 30 -> return CreateClanResult(
                    false, error = "Clan name must be under 30 characters"
                )
                tag.length < 2 || tag.length > 5 -> return CreateClanResult(
                    false, error = "Tag must be 2-5 characters"
                )
                !isTagAvailable(tag) -> return CreateClanResult(
                    false, error = "Tag is already taken"
                )
                currentMember.value != null -> return CreateClanResult(
                    false, error = "You must leave your current clan first"
                )
            }
            
            // Create clan
            val clan = Clan(
                name = name.trim(),
                tag = tag.uppercase().trim(),
                description = description.trim(),
                emblem = emblem,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                joinPolicy = joinPolicy,
                minLevelToJoin = minLevelToJoin,
                maxMembers = 50
            )
            
            clanDao.insertClan(clan)
            
            // Add creator as leader
            val member = ClanMember(
                clanId = clan.id,
                userId = currentUserId,
                username = getCurrentUsername(),
                displayName = getCurrentDisplayName(),
                avatarEmoji = getCurrentAvatar(),
                role = ClanRole.LEADER
            )
            
            clanDao.insertMember(member)
            
            // Update user clan data
            clanDao.insertUserClanData(UserClanData(
                userId = currentUserId,
                clanId = clan.id,
                role = ClanRole.LEADER,
                joinedAt = System.currentTimeMillis()
            ))
            
            // Update state
            _currentClan.value = clan
            _currentMember.value = member
            _clanMembers.value = listOf(member)
            
            // Track achievement
            achievementManager.unlockAchievement("clan_founder")
            
            // Schedule sync
            ClanSyncWorker.scheduleImmediate(context)
            
            CreateClanResult(success = true, clan = clan)
            
        } catch (e: Exception) {
            _error.value = e.message
            CreateClanResult(false, error = e.message)
        } finally {
            _isLoading.value = false
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // JOINING CLANS
    // ═════════════════════════════════════════════════════════════════
    
    data class JoinClanResult(
        val success: Boolean,
        val needsApproval: Boolean = false,
        val error: String? = null
    )
    
    suspend fun joinClan(clanId: String, message: String = ""): JoinClanResult {
        _isLoading.value = true
        _error.value = null
        
        return try {
            val clan = clanDao.getClanById(clanId)
                ?: return JoinClanResult(false, error = "Clan not found")
            
            // Check if already in clan
            if (currentMember.value != null) {
                return JoinClanResult(false, error = "You must leave your current clan first")
            }
            
            // Check clan capacity
            val memberCount = clanDao.getMemberCount(clanId)
            if (memberCount >= clan.maxMembers) {
                return JoinClanResult(false, error = "Clan is full")
            }
            
            // Check level requirement
            val userLevel = getCurrentUserLevel()
            if (userLevel < clan.minLevelToJoin) {
                return JoinClanResult(false, error = "You need to be level ${clan.minLevelToJoin} to join")
            }
            
            when (clan.joinPolicy) {
                ClanJoinPolicy.OPEN -> {
                    // Direct join
                    addMemberToClan(clanId, ClanRole.RECRUIT)
                    return JoinClanResult(success = true)
                }
                ClanJoinPolicy.REQUEST -> {
                    // Create join request
                    val request = ClanJoinRequest(
                        clanId = clanId,
                        userId = currentUserId,
                        username = getCurrentUsername(),
                        userLevel = userLevel,
                        message = message
                    )
                    clanDao.insertJoinRequest(request)
                    return JoinClanResult(success = true, needsApproval = true)
                }
                ClanJoinPolicy.INVITE_ONLY -> {
                    // Check if has invitation
                    val hasInvitation = clanDao.hasPendingInvitation(clanId, currentUserId) > 0
                    if (hasInvitation) {
                        addMemberToClan(clanId, ClanRole.RECRUIT)
                        // Accept the invitation
                        val invitations = clanDao.getPendingInvitationsForUser(currentUserId)
                        invitations.find { it.clanId == clanId }?.let {
                            clanDao.respondToInvitation(
                                it.id, 
                                InvitationStatus.ACCEPTED, 
                                System.currentTimeMillis()
                            )
                        }
                        return JoinClanResult(success = true)
                    }
                    return JoinClanResult(false, error = "This clan is invite-only")
                }
            }
            
        } catch (e: Exception) {
            _error.value = e.message
            JoinClanResult(false, error = e.message)
        } finally {
            _isLoading.value = false
        }
    }
    
    private suspend fun addMemberToClan(clanId: String, role: ClanRole) {
        val member = ClanMember(
            clanId = clanId,
            userId = currentUserId,
            username = getCurrentUsername(),
            displayName = getCurrentDisplayName(),
            avatarEmoji = getCurrentAvatar(),
            role = role
        )
        
        clanDao.insertMember(member)
        clanDao.insertUserClanData(UserClanData(
            userId = currentUserId,
            clanId = clanId,
            role = role,
            joinedAt = System.currentTimeMillis()
        ))
        
        // Send system message
        val systemMessage = ClanMessage(
            clanId = clanId,
            type = ClanMessageType.SYSTEM_JOIN,
            content = "${getCurrentUsername()} joined the clan!"
        )
        clanDao.insertMessage(systemMessage)
        
        // Update state
        refreshClanData(clanId)
        
        // Track achievement
        achievementManager.unlockAchievement("clan_member")
    }
    
    // ═════════════════════════════════════════════════════════════════
    // LEAVING CLAN
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun leaveClan(): Boolean {
        val member = currentMember.value ?: return false
        val clan = currentClan.value ?: return false
        
        return try {
            // If leader, check if there are other officers to promote
            if (member.role == ClanRole.LEADER) {
                val officers = clanDao.getClanMembersByRoles(clan.id, listOf(ClanRole.OFFICER))
                if (officers.isEmpty()) {
                    val members = clanDao.getClanMembersByRoles(
                        clan.id, 
                        listOf(ClanRole.MEMBER, ClanRole.RECRUIT)
                    )
                    if (members.isNotEmpty()) {
                        // Promote oldest member to leader
                        val newLeader = members.minByOrNull { it.joinedAt }
                        newLeader?.let {
                            clanDao.updateMember(it.copy(role = ClanRole.LEADER))
                        }
                    } else {
                        // Disband clan (no members left)
                        clanDao.disbandClanTransaction(clan.id)
                    }
                } else {
                    // Promote oldest officer to leader
                    val newLeader = officers.minByOrNull { it.joinedAt }
                    newLeader?.let {
                        clanDao.updateMember(it.copy(role = ClanRole.LEADER))
                    }
                }
            }
            
            // Remove member
            clanDao.leaveClanTransaction(currentUserId, clan.id)
            
            // Send system message
            val systemMessage = ClanMessage(
                clanId = clan.id,
                type = ClanMessageType.SYSTEM_LEAVE,
                content = "${getCurrentUsername()} left the clan"
            )
            clanDao.insertMessage(systemMessage)
            
            // Clear state
            _currentClan.value = null
            _currentMember.value = null
            _clanMembers.value = emptyList()
            
            true
        } catch (e: Exception) {
            _error.value = e.message
            false
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // MEMBER MANAGEMENT
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun kickMember(userId: String): Boolean {
        if (!canKickMembers()) return false
        
        val clan = currentClan.value ?: return false
        val targetMember = clanDao.getMemberByUserId(userId) ?: return false
        
        // Can't kick higher or equal rank
        if (targetMember.role.rank >= (currentMember.value?.role?.rank ?: 0)) {
            return false
        }
        
        return try {
            clanDao.removeMember(clan.id, userId)
            clanDao.insertUserClanData(UserClanData(
                userId = userId,
                clanId = null,
                role = null
            ))
            
            // Send system message
            val systemMessage = ClanMessage(
                clanId = clan.id,
                type = ClanMessageType.SYSTEM_KICK,
                content = "${targetMember.username} was kicked from the clan"
            )
            clanDao.insertMessage(systemMessage)
            
            refreshClanData(clan.id)
            true
        } catch (e: Exception) {
            _error.value = e.message
            false
        }
    }
    
    suspend fun promoteMember(userId: String): Boolean {
        if (!canPromoteMembers()) return false
        
        val clan = currentClan.value ?: return false
        val targetMember = clanDao.getMemberByUserId(userId) ?: return false
        
        val newRole = when (targetMember.role) {
            ClanRole.RECRUIT -> ClanRole.MEMBER
            ClanRole.MEMBER -> ClanRole.OFFICER
            else -> return false
        }
        
        return try {
            clanDao.updateMember(targetMember.copy(role = newRole))
            
            // Send system message
            val systemMessage = ClanMessage(
                clanId = clan.id,
                type = ClanMessageType.SYSTEM_PROMOTE,
                content = "${targetMember.username} was promoted to ${newRole.name}"
            )
            clanDao.insertMessage(systemMessage)
            
            refreshClanData(clan.id)
            true
        } catch (e: Exception) {
            _error.value = e.message
            false
        }
    }
    
    suspend fun demoteMember(userId: String): Boolean {
        if (!canPromoteMembers()) return false
        
        val clan = currentClan.value ?: return false
        val targetMember = clanDao.getMemberByUserId(userId) ?: return false
        
        // Can't demote leader
        if (targetMember.role == ClanRole.LEADER) return false
        
        val newRole = when (targetMember.role) {
            ClanRole.OFFICER -> ClanRole.MEMBER
            ClanRole.MEMBER -> ClanRole.RECRUIT
            else -> return false
        }
        
        return try {
            clanDao.updateMember(targetMember.copy(role = newRole))
            
            // Send system message
            val systemMessage = ClanMessage(
                clanId = clan.id,
                type = ClanMessageType.SYSTEM_DEMOTE,
                content = "${targetMember.username} was demoted to ${newRole.name}"
            )
            clanDao.insertMessage(systemMessage)
            
            refreshClanData(clan.id)
            true
        } catch (e: Exception) {
            _error.value = e.message
            false
        }
    }
    
    suspend fun transferLeadership(newLeaderUserId: String): Boolean {
        if (!isLeader()) return false
        
        val clan = currentClan.value ?: return false
        val currentLeader = currentMember.value ?: return false
        val newLeader = clanDao.getMemberByUserId(newLeaderUserId) ?: return false
        
        return try {
            // Demote current leader to officer
            clanDao.updateMember(currentLeader.copy(role = ClanRole.OFFICER))
            
            // Promote new leader
            clanDao.updateMember(newLeader.copy(role = ClanRole.LEADER))
            
            refreshClanData(clan.id)
            true
        } catch (e: Exception) {
            _error.value = e.message
            false
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // JOIN REQUEST MANAGEMENT
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun acceptJoinRequest(requestId: String): Boolean {
        if (!canManageInvitations()) return false
        
        return try {
            val request = clanDao.getPendingJoinRequests(currentClan.value?.id ?: "")
                .find { it.id == requestId } ?: return false
            
            // Check capacity
            val memberCount = clanDao.getMemberCount(request.clanId)
            val maxMembers = currentClan.value?.maxMembers ?: 50
            if (memberCount >= maxMembers) {
                return false
            }
            
            // Add member
            val member = ClanMember(
                clanId = request.clanId,
                userId = request.userId,
                username = request.username,
                displayName = request.username,
                role = ClanRole.RECRUIT
            )
            clanDao.insertMember(member)
            
            clanDao.insertUserClanData(UserClanData(
                userId = request.userId,
                clanId = request.clanId,
                role = ClanRole.RECRUIT,
                joinedAt = System.currentTimeMillis()
            ))
            
            // Update request
            clanDao.respondToJoinRequest(
                requestId,
                JoinRequestStatus.ACCEPTED,
                System.currentTimeMillis(),
                currentUserId
            )
            
            // Send system message
            val systemMessage = ClanMessage(
                clanId = request.clanId,
                type = ClanMessageType.SYSTEM_JOIN,
                content = "${request.username} joined the clan!"
            )
            clanDao.insertMessage(systemMessage)
            
            refreshClanData(request.clanId)
            true
        } catch (e: Exception) {
            _error.value = e.message
            false
        }
    }
    
    suspend fun declineJoinRequest(requestId: String): Boolean {
        if (!canManageInvitations()) return false
        
        return try {
            clanDao.respondToJoinRequest(
                requestId,
                JoinRequestStatus.DECLINED,
                System.currentTimeMillis(),
                currentUserId
            )
            
            _pendingJoinRequests.value = clanDao.getPendingJoinRequests(
                currentClan.value?.id ?: ""
            )
            true
        } catch (e: Exception) {
            _error.value = e.message
            false
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // INVITATION SYSTEM
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun inviteUser(userId: String, username: String, message: String = ""): Boolean {
        if (!canInvite()) return false
        
        val clan = currentClan.value ?: return false
        
        return try {
            // Check if already in clan
            val existingMember = clanDao.getMemberByUserId(userId)
            if (existingMember != null) {
                return false
            }
            
            // Check if already invited
            val hasInvitation = clanDao.hasPendingInvitation(clan.id, userId) > 0
            if (hasInvitation) {
                return false
            }
            
            val invitation = ClanInvitation(
                clanId = clan.id,
                clanName = clan.name,
                clanTag = clan.tag,
                invitedByUserId = currentUserId,
                invitedByUsername = getCurrentUsername(),
                invitedUserId = userId,
                invitedUsername = username,
                message = message
            )
            
            clanDao.insertInvitation(invitation)
            true
        } catch (e: Exception) {
            _error.value = e.message
            false
        }
    }
    
    suspend fun acceptInvitation(invitationId: String): Boolean {
        return try {
            val invitations = clanDao.getPendingInvitationsForUser(currentUserId)
            val invitation = invitations.find { it.id == invitationId } ?: return false
            
            // Check capacity
            val memberCount = clanDao.getMemberCount(invitation.clanId)
            val clan = clanDao.getClanById(invitation.clanId)
            if (clan != null && memberCount >= clan.maxMembers) {
                return false
            }
            
            // Add to clan
            addMemberToClan(invitation.clanId, ClanRole.RECRUIT)
            
            // Update invitation
            clanDao.respondToInvitation(
                invitationId,
                InvitationStatus.ACCEPTED,
                System.currentTimeMillis()
            )
            
            refreshClanData(invitation.clanId)
            true
        } catch (e: Exception) {
            _error.value = e.message
            false
        }
    }
    
    suspend fun declineInvitation(invitationId: String): Boolean {
        return try {
            clanDao.respondToInvitation(
                invitationId,
                InvitationStatus.DECLINED,
                System.currentTimeMillis()
            )
            
            _pendingInvitations.value = clanDao.getPendingInvitationsForUser(currentUserId)
            true
        } catch (e: Exception) {
            _error.value = e.message
            false
        }
    }
    
    suspend fun loadPendingInvitations() {
        _pendingInvitations.value = clanDao.getPendingInvitationsForUser(currentUserId)
    }
    
    // ═════════════════════════════════════════════════════════════════
    // CLAN SEARCH & DISCOVERY
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun searchClans(query: String): List<Clan> {
        return clanDao.searchClans("%$query%")
    }
    
    suspend fun getRecommendedClans(limit: Int = 10): List<Clan> {
        // Return clans with open join policy that have space
        return clanDao.getClansByJoinPolicy(ClanJoinPolicy.OPEN)
            .filter { clanDao.getMemberCount(it.id) < it.maxMembers }
            .take(limit)
    }
    
    suspend fun getTopClans(limit: Int = 100): List<Clan> {
        return clanDao.getTopClans(limit)
    }
    
    suspend fun getClanDetails(clanId: String): ClanPreview? {
        val clan = clanDao.getClanById(clanId) ?: return null
        val memberCount = clanDao.getMemberCount(clanId)
        val isJoinable = when (clan.joinPolicy) {
            ClanJoinPolicy.OPEN -> memberCount < clan.maxMembers
            else -> false
        }
        
        return ClanPreview(
            clan = clan,
            memberCount = memberCount,
            isJoinable = isJoinable
        )
    }
    
    // ═════════════════════════════════════════════════════════════════
    // CLAN SETTINGS
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun updateClanSettings(
        description: String? = null,
        joinPolicy: ClanJoinPolicy? = null,
        minLevelToJoin: Int? = null,
        emblem: String? = null,
        primaryColor: String? = null,
        secondaryColor: String? = null,
        isPublic: Boolean? = null
    ): Boolean {
        if (!canManageSettings()) return false
        
        val clan = currentClan.value ?: return false
        
        return try {
            val updatedClan = clan.copy(
                description = description ?: clan.description,
                joinPolicy = joinPolicy ?: clan.joinPolicy,
                minLevelToJoin = minLevelToJoin ?: clan.minLevelToJoin,
                emblem = emblem ?: clan.emblem,
                primaryColor = primaryColor ?: clan.primaryColor,
                secondaryColor = secondaryColor ?: clan.secondaryColor,
                isPublic = isPublic ?: clan.isPublic
            )
            
            clanDao.updateClan(updatedClan)
            _currentClan.value = updatedClan
            true
        } catch (e: Exception) {
            _error.value = e.message
            false
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // PERMISSION CHECKS
    // ═════════════════════════════════════════════════════════════════
    
    fun isLeader(): Boolean = currentMember.value?.role == ClanRole.LEADER
    fun isOfficer(): Boolean = currentMember.value?.role == ClanRole.OFFICER
    fun canKickMembers(): Boolean = currentMember.value?.role?.canKick == true
    fun canPromoteMembers(): Boolean = currentMember.value?.role?.canPromote == true
    fun canInvite(): Boolean = currentMember.value?.role?.canInvite == true
    fun canManageInvitations(): Boolean = isLeader() || isOfficer()
    fun canManageSettings(): Boolean = isLeader()
    fun canManageShop(): Boolean = isLeader() || isOfficer()
    
    // ═════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═════════════════════════════════════════════════════════════════
    
    private suspend fun isTagAvailable(tag: String): Boolean {
        // In real implementation, would check server
        return true
    }
    
    private fun getCurrentUsername(): String {
        return dataStore.data.map { it[USERNAME_KEY] ?: "User" }
            .stateIn(scope, SharingStarted.Eagerly, "User")
            .value
    }
    
    private fun getCurrentDisplayName(): String {
        return dataStore.data.map { it[DISPLAY_NAME_KEY] ?: getCurrentUsername() }
            .stateIn(scope, SharingStarted.Eagerly, getCurrentUsername())
            .value
    }
    
    private fun getCurrentAvatar(): String {
        return dataStore.data.map { it[AVATAR_KEY] ?: "🍭" }
            .stateIn(scope, SharingStarted.Eagerly, "🍭")
            .value
    }
    
    private fun getCurrentUserLevel(): Int {
        return dataStore.data.map { it[USER_LEVEL_KEY] ?: 1 }
            .stateIn(scope, SharingStarted.Eagerly, 1)
            .value
    }
    
    fun setUserData(userId: String, username: String, displayName: String, avatar: String, level: Int) {
        scope.launch {
            dataStore.edit { prefs ->
                prefs[USER_ID_KEY] = userId
                prefs[USERNAME_KEY] = username
                prefs[DISPLAY_NAME_KEY] = displayName
                prefs[AVATAR_KEY] = avatar
                prefs[USER_LEVEL_KEY] = level
            }
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // SYNC SCHEDULING
    // ═════════════════════════════════════════════════════════════════
    
    private fun scheduleClanSync() {
        ClanSyncWorker.schedule(context)
    }
    
    fun syncNow() {
        ClanSyncWorker.scheduleImmediate(context)
    }
    
    // ═════════════════════════════════════════════════════════════════
    // COMPANION
    // ═════════════════════════════════════════════════════════════════
    
    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val DISPLAY_NAME_KEY = stringPreferencesKey("display_name")
        private val AVATAR_KEY = stringPreferencesKey("avatar")
        private val USER_LEVEL_KEY = intPreferencesKey("user_level")
        
        @Volatile
        private var instance: ClanManager? = null
        
        fun getInstance(context: Context): ClanManager {
            return instance ?: synchronized(this) {
                instance ?: ClanManager(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
        
        fun destroy() {
            instance = null
        }
    }
}
