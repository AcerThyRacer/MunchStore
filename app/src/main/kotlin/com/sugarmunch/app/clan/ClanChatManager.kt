package com.sugarmunch.app.clan

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.*
import com.sugarmunch.app.clan.model.*
import com.sugarmunch.app.clan.work.ClanChatSyncWorker
import com.sugarmunch.app.data.local.AppDatabase
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.theme.model.CandyTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.TimeUnit

private val Context.chatDataStore: DataStore<Preferences> by preferencesDataStore(name = "sugarmunch_clan_chat")

/**
 * ClanChatManager - In-app clan chat system
 * 
 * Features:
 * - Real-time clan messaging
 * - Message persistence
 * - Rich messages (share apps, effects, themes)
 * - System messages (joins, leaves, promotions)
 * - Message deletion for moderators
 */
class ClanChatManager private constructor(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private val dataStore = context.chatDataStore
    private val database = AppDatabase.getDatabase(context)
    private val clanDao = database.clanDao()
    private val clanManager = ClanManager.getInstance(context)
    
    // Current user ID
    private val currentUserId: String
        get() = "current_user" // Would come from auth
    
    // State flows
    private val _messages = MutableStateFlow<List<ClanMessage>>(emptyList())
    val messages: StateFlow<List<ClanMessage>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _hasMoreMessages = MutableStateFlow(true)
    val hasMoreMessages: StateFlow<Boolean> = _hasMoreMessages.asStateFlow()
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    private val _isTyping = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val isTyping: StateFlow<Map<String, Boolean>> = _isTyping.asStateFlow()
    
    // Message paging
    private var lastLoadedTimestamp: Long = Long.MAX_VALUE
    private val pageSize = 50
    
    // Send status
    data class SendResult(
        val success: Boolean,
        val message: ClanMessage? = null,
        val error: String? = null
    )
    
    companion object {
        const val MAX_MESSAGE_LENGTH = 500
        const val MESSAGE_RETENTION_DAYS = 30
        
        @Volatile
        private var instance: ClanChatManager? = null
        
        fun getInstance(context: Context): ClanChatManager {
            return instance ?: synchronized(this) {
                instance ?: ClanChatManager(context.applicationContext).also { 
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
            // Clean up old messages periodically
            cleanupOldMessages()
            
            // Start sync worker
            scheduleChatSync()
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // MESSAGE OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun sendMessage(
        content: String,
        replyToMessageId: String? = null
    ): SendResult {
        val clan = clanManager.currentClan.value ?: return SendResult(
            success = false,
            error = "You must be in a clan to chat"
        )
        
        val member = clanManager.currentMember.value ?: return SendResult(
            success = false,
            error = "You must be a clan member to chat"
        )
        
        // Validation
        val trimmedContent = content.trim()
        when {
            trimmedContent.isEmpty() -> return SendResult(
                success = false,
                error = "Message cannot be empty"
            )
            trimmedContent.length > MAX_MESSAGE_LENGTH -> return SendResult(
                success = false,
                error = "Message too long (max $MAX_MESSAGE_LENGTH characters)"
            )
        }
        
        return try {
            val message = ClanMessage(
                clanId = clan.id,
                userId = currentUserId,
                username = member.username,
                avatarEmoji = member.avatarEmoji,
                role = member.role,
                type = ClanMessageType.TEXT,
                content = trimmedContent,
                replyToMessageId = replyToMessageId
            )
            
            clanDao.insertMessage(message)
            
            // Add to local state immediately
            _messages.value = (_messages.value + message).sortedBy { it.timestamp }
            
            // Trigger sync
            syncMessage(message)
            
            SendResult(success = true, message = message)
        } catch (e: Exception) {
            SendResult(success = false, error = e.message)
        }
    }
    
    suspend fun sendAppShare(
        app: AppEntry,
        message: String = ""
    ): SendResult {
        val clan = clanManager.currentClan.value ?: return SendResult(
            success = false,
            error = "You must be in a clan to share"
        )
        
        val member = clanManager.currentMember.value ?: return SendResult(
            success = false,
            error = "You must be a clan member to share"
        )
        
        return try {
            val clanMessage = ClanMessage(
                clanId = clan.id,
                userId = currentUserId,
                username = member.username,
                avatarEmoji = member.avatarEmoji,
                role = member.role,
                type = ClanMessageType.APP_SHARE,
                content = message.ifEmpty { "Check out ${app.name}!" },
                attachmentId = app.id,
                attachmentName = app.name,
                attachmentPreview = app.iconUrl
            )
            
            clanDao.insertMessage(clanMessage)
            _messages.value = (_messages.value + clanMessage).sortedBy { it.timestamp }
            
            SendResult(success = true, message = clanMessage)
        } catch (e: Exception) {
            SendResult(success = false, error = e.message)
        }
    }
    
    suspend fun sendThemeShare(
        theme: CandyTheme,
        message: String = ""
    ): SendResult {
        val clan = clanManager.currentClan.value ?: return SendResult(
            success = false,
            error = "You must be in a clan to share"
        )
        
        val member = clanManager.currentMember.value ?: return SendResult(
            success = false,
            error = "You must be a clan member to share"
        )
        
        return try {
            val clanMessage = ClanMessage(
                clanId = clan.id,
                userId = currentUserId,
                username = member.username,
                avatarEmoji = member.avatarEmoji,
                role = member.role,
                type = ClanMessageType.THEME_SHARE,
                content = message.ifEmpty { "Check out the ${theme.name} theme!" },
                attachmentId = theme.id,
                attachmentName = theme.name,
                attachmentPreview = theme.previewColor
            )
            
            clanDao.insertMessage(clanMessage)
            _messages.value = (_messages.value + clanMessage).sortedBy { it.timestamp }
            
            SendResult(success = true, message = clanMessage)
        } catch (e: Exception) {
            SendResult(success = false, error = e.message)
        }
    }
    
    suspend fun sendEffectShare(
        effectName: String,
        effectId: String,
        message: String = ""
    ): SendResult {
        val clan = clanManager.currentClan.value ?: return SendResult(
            success = false,
            error = "You must be in a clan to share"
        )
        
        val member = clanManager.currentMember.value ?: return SendResult(
            success = false,
            error = "You must be a clan member to share"
        )
        
        return try {
            val clanMessage = ClanMessage(
                clanId = clan.id,
                userId = currentUserId,
                username = member.username,
                avatarEmoji = member.avatarEmoji,
                role = member.role,
                type = ClanMessageType.EFFECT_SHARE,
                content = message.ifEmpty { "Check out this effect: $effectName!" },
                attachmentId = effectId,
                attachmentName = effectName
            )
            
            clanDao.insertMessage(clanMessage)
            _messages.value = (_messages.value + clanMessage).sortedBy { it.timestamp }
            
            SendResult(success = true, message = clanMessage)
        } catch (e: Exception) {
            SendResult(success = false, error = e.message)
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // MESSAGE RETRIEVAL
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun loadMessages(clanId: String, initialLoad: Boolean = true) {
        if (initialLoad) {
            _isLoading.value = true
            lastLoadedTimestamp = Long.MAX_VALUE
        }
        
        try {
            val messages = if (initialLoad) {
                clanDao.getRecentMessages(clanId, pageSize)
            } else {
                clanDao.getMessagesBefore(clanId, lastLoadedTimestamp, pageSize)
            }
            
            if (messages.isNotEmpty()) {
                lastLoadedTimestamp = messages.minOf { it.timestamp }
            }
            
            _hasMoreMessages.value = messages.size >= pageSize
            
            if (initialLoad) {
                _messages.value = messages.sortedBy { it.timestamp }
            } else {
                val currentIds = _messages.value.map { it.id }.toSet()
                val newMessages = messages.filter { it.id !in currentIds }
                _messages.value = (_messages.value + newMessages).sortedBy { it.timestamp }
            }
            
            // Mark as read
            markAsRead()
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun loadMoreMessages(clanId: String) {
        if (_isLoading.value || !_hasMoreMessages.value) return
        loadMessages(clanId, initialLoad = false)
    }
    
    suspend fun refreshMessages(clanId: String) {
        val latestMessage = _messages.value.maxByOrNull { it.timestamp }
        val afterTimestamp = latestMessage?.timestamp ?: 0
        
        val newMessages = clanDao.getMessagesAfter(clanId, afterTimestamp)
        
        if (newMessages.isNotEmpty()) {
            val currentIds = _messages.value.map { it.id }.toSet()
            val uniqueNewMessages = newMessages.filter { it.id !in currentIds }
            
            if (uniqueNewMessages.isNotEmpty()) {
                _messages.value = (_messages.value + uniqueNewMessages).sortedBy { it.timestamp }
                
                // Update unread count if not currently viewing
                val lastReadTimestamp = dataStore.data.map { it[LAST_READ_KEY] ?: 0L }
                    .first()
                
                val unreadMessages = uniqueNewMessages.filter { 
                    it.userId != currentUserId && it.timestamp > lastReadTimestamp 
                }
                
                if (unreadMessages.isNotEmpty()) {
                    _unreadCount.value += unreadMessages.size
                }
            }
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // MESSAGE MODERATION
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun deleteMessage(messageId: String): Boolean {
        val message = _messages.value.find { it.id == messageId } ?: return false
        
        // Check permissions
        val canDelete = when {
            // Users can delete their own messages
            message.userId == currentUserId -> true
            // Officers/Leaders can delete any message
            clanManager.canKickMembers() -> true
            else -> false
        }
        
        if (!canDelete) return false
        
        return try {
            clanDao.markMessageDeleted(messageId)
            
            // Update local state
            _messages.value = _messages.value.map { 
                if (it.id == messageId) it.copy(isDeleted = true) else it 
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun editMessage(messageId: String, newContent: String): Boolean {
        val message = _messages.value.find { it.id == messageId } ?: return false
        
        // Can only edit own messages
        if (message.userId != currentUserId) return false
        
        // Can only edit text messages
        if (message.type != ClanMessageType.TEXT) return false
        
        // Can't edit messages older than 5 minutes
        if (System.currentTimeMillis() - message.timestamp > 5 * 60 * 1000) return false
        
        return try {
            val updatedMessage = message.copy(
                content = newContent.trim(),
                editedAt = System.currentTimeMillis()
            )
            
            clanDao.updateMessage(updatedMessage)
            
            _messages.value = _messages.value.map {
                if (it.id == messageId) updatedMessage else it
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // TYPING INDICATORS
    // ═════════════════════════════════════════════════════════════════
    
    fun setTyping(isTyping: Boolean) {
        val clanId = clanManager.currentClan.value?.id ?: return
        
        scope.launch {
            // In real implementation, would send to server
            // For now, just local state
            _isTyping.value = if (isTyping) {
                _isTyping.value + (currentUserId to true)
            } else {
                _isTyping.value - currentUserId
            }
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // SYSTEM MESSAGES
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun sendSystemMessage(clanId: String, content: String, type: ClanMessageType) {
        val message = ClanMessage(
            clanId = clanId,
            type = type,
            content = content
        )
        
        clanDao.insertMessage(message)
        _messages.value = (_messages.value + message).sortedBy { it.timestamp }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═════════════════════════════════════════════════════════════════
    
    fun formatMessageTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}d ago"
            else -> {
                val date = Date(timestamp)
                java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault()).format(date)
            }
        }
    }
    
    fun formatMessageTimeFull(timestamp: Long): String {
        val date = Date(timestamp)
        return java.text.SimpleDateFormat(
            "MMM d, yyyy 'at' h:mm a",
            java.util.Locale.getDefault()
        ).format(date)
    }
    
    fun getRoleColor(role: ClanRole?): androidx.compose.ui.graphics.Color {
        return when (role) {
            ClanRole.LEADER -> androidx.compose.ui.graphics.Color(0xFFFFD700) // Gold
            ClanRole.OFFICER -> androidx.compose.ui.graphics.Color(0xFFC0C0C0) // Silver
            ClanRole.MEMBER -> androidx.compose.ui.graphics.Color(0xFFCD7F32) // Bronze
            ClanRole.RECRUIT -> androidx.compose.ui.graphics.Color(0xFF808080) // Gray
            null -> androidx.compose.ui.graphics.Color(0xFF808080)
        }
    }
    
    fun getRoleIcon(role: ClanRole?): String {
        return when (role) {
            ClanRole.LEADER -> "👑"
            ClanRole.OFFICER -> "⭐"
            ClanRole.MEMBER -> "🎖️"
            ClanRole.RECRUIT -> "🌱"
            null -> "👤"
        }
    }
    
    suspend fun markAsRead() {
        _unreadCount.value = 0
        dataStore.edit { prefs ->
            prefs[LAST_READ_KEY] = System.currentTimeMillis()
        }
    }
    
    private suspend fun cleanupOldMessages() {
        val cutoffTime = System.currentTimeMillis() - (MESSAGE_RETENTION_DAYS * 24 * 60 * 60 * 1000)
        
        // Get all clan IDs
        val clanIds = _messages.value.map { it.clanId }.distinct()
        
        clanIds.forEach { clanId ->
            clanDao.deleteOldMessages(clanId, cutoffTime)
        }
    }
    
    private fun scheduleChatSync() {
        ClanChatSyncWorker.schedule(context)
    }
    
    private fun syncMessage(message: ClanMessage) {
        // In real implementation, would send to server
        // For now, just local storage
    }
    
    // ═════════════════════════════════════════════════════════════════
    // REACTIONS (Future feature)
    // ═════════════════════════════════════════════════════════════════
    
    data class MessageReaction(
        val emoji: String,
        val userId: String,
        val username: String
    )
    
    suspend fun addReaction(messageId: String, emoji: String): Boolean {
        // Future feature: Add emoji reactions to messages
        return true
    }
    
    suspend fun removeReaction(messageId: String, emoji: String): Boolean {
        // Future feature: Remove emoji reactions
        return true
    }
    
    private val LAST_READ_KEY = longPreferencesKey("last_read_timestamp")
}
