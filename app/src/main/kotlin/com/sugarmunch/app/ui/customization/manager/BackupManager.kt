package com.sugarmunch.app.ui.customization.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.ui.customization.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

/**
 * EXTREME Backup Manager for SugarMunch
 * Comprehensive backup, restore, export, and import functionality
 */
class BackupManager(
    private val context: Context,
    private val customizationRepository: CustomizationRepository
) {
    private val mutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    private val Context.backupDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "sugarmunch_backup"
    )

    private val Keys = object {
        val BACKUP_HISTORY = stringPreferencesKey("backup_history")
        val LAST_BACKUP = stringPreferencesKey("last_backup")
        val SCHEDULED_BACKUP_ENABLED = stringPreferencesKey("scheduled_backup_enabled")
        val SCHEDULED_BACKUP_INTERVAL = stringPreferencesKey("scheduled_backup_interval")
    }

    private val _backupHistory = MutableStateFlow<List<BackupInfo>>(emptyList())
    val backupHistory: StateFlow<List<BackupInfo>> = _backupHistory.asStateFlow()

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring.asStateFlow()

    /**
     * Initialize backup manager
     */
    suspend fun initialize() {
        mutex.withLock {
            loadBackupHistory()
        }
    }

    private suspend fun loadBackupHistory() {
        context.backupDataStore.data.first()[Keys.BACKUP_HISTORY]?.let { jsonStr ->
            _backupHistory.value = try {
                json.decodeFromString<List<BackupInfo>>(jsonStr)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Create a backup
     */
    suspend fun createBackup(
        type: BackupType = BackupType.FULL,
        categories: List<BackupCategory> = BackupCategory.entries
    ): BackupResult {
        return mutex.withLock {
            _isBackingUp.value = true

            try {
                // Gather backup data
                val backupData = gatherBackupData(type, categories)

                // Generate checksum
                val checksum = generateChecksum(backupData)
                val backupWithChecksum = backupData.copy(checksum = checksum)

                // Create backup file
                val backupId = createBackupId()
                val backupJson = json.encodeToString(backupWithChecksum)

                // Save to file
                val backupFile = saveBackupToFile(backupId, backupJson)

                // Update history
                val backupInfo = BackupInfo(
                    id = backupId,
                    type = type,
                    categories = categories,
                    createdAt = System.currentTimeMillis(),
                    sizeBytes = backupJson.length,
                    checksum = checksum,
                    filePath = backupFile
                )
                addBackupToHistory(backupInfo)

                // Update last backup time
                context.backupDataStore.edit { prefs ->
                    prefs[Keys.LAST_BACKUP] = System.currentTimeMillis().toString()
                }

                BackupResult.Success(backupInfo)
            } catch (e: Exception) {
                BackupResult.Failure(e)
            } finally {
                _isBackingUp.value = false
            }
        }
    }

    /**
     * Gather backup data based on type and categories
     */
    private suspend fun gatherBackupData(
        type: BackupType,
        categories: List<BackupCategory>
    ): BackupData {
        return BackupData(
            version = 1,
            createdAt = System.currentTimeMillis(),
            backupType = type,
            includedCategories = categories,
            backgroundConfig = if (BackupCategory.BACKGROUNDS in categories) {
                customizationRepository.backgroundConfigFlow.first()
            } else null,
            colorProfile = if (BackupCategory.COLORS in categories) {
                customizationRepository.colorProfileFlow.first()
            } else null,
            animationProfile = if (BackupCategory.ANIMATIONS in categories) {
                customizationRepository.animationProfileFlow.first()
            } else null,
            gestureMapping = if (BackupCategory.GESTURES in categories) {
                customizationRepository.gestureMappingFlow.first()
            } else null,
            hapticPattern = if (BackupCategory.HAPTICS in categories) {
                customizationRepository.hapticPatternFlow.first()
            } else null,
            layoutConfig = if (BackupCategory.LAYOUTS in categories) {
                customizationRepository.layoutConfigFlow.first()
            } else null,
            navigationConfig = if (BackupCategory.NAVIGATION in categories) {
                customizationRepository.navigationConfigFlow.first()
            } else null,
            cardStyleConfig = if (BackupCategory.CARDS in categories) {
                customizationRepository.cardStyleConfigFlow.first()
            } else null,
            typographyConfig = if (BackupCategory.TYPOGRAPHY in categories) {
                customizationRepository.typographyConfigFlow.first()
            } else null,
            effectConfigs = if (BackupCategory.EFFECTS in categories) {
                customizationRepository.effectConfigsFlow.first()
            } else null,
            particleConfig = if (BackupCategory.PARTICLES in categories) {
                customizationRepository.particleConfigFlow.first()
            } else null,
            profiles = customizationRepository.userProfilesFlow.first(),
            presets = customizationRepository.presetConfigsFlow.first()
        )
    }

    /**
     * Restore from backup
     */
    suspend fun restoreBackup(
        backupId: String,
        merge: Boolean = false
    ): BackupResult {
        return mutex.withLock {
            _isRestoring.value = true

            try {
                // Load backup file
                val backupJson = loadBackupFromFile(backupId)
                val backupData = json.decodeFromString<BackupData>(backupJson)

                // Verify checksum
                if (!verifyChecksum(backupData)) {
                    return@withLock BackupResult.Failure(
                        IllegalStateException("Backup checksum mismatch")
                    )
                }

                // Restore data
                customizationRepository.restoreBackup(backupData, merge)

                BackupResult.Success(
                    BackupInfo(
                        id = backupId,
                        type = backupData.backupType,
                        categories = backupData.includedCategories,
                        createdAt = backupData.createdAt,
                        sizeBytes = backupJson.length,
                        checksum = backupData.checksum,
                        filePath = ""
                    )
                )
            } catch (e: Exception) {
                BackupResult.Failure(e)
            } finally {
                _isRestoring.value = false
            }
        }
    }

    /**
     * Export settings to JSON string
     */
    suspend fun exportSettings(
        categories: List<BackupCategory> = BackupCategory.entries
    ): String {
        val backupData = gatherBackupData(BackupType.PARTIAL, categories)
        val checksum = generateChecksum(backupData)
        return json.encodeToString(backupData.copy(checksum = checksum))
    }

    /**
     * Import settings from JSON string
     */
    suspend fun importSettings(
        jsonString: String,
        merge: Boolean = false
    ): BackupResult {
        return mutex.withLock {
            try {
                val backupData = json.decodeFromString<BackupData>(jsonString)

                // Verify checksum
                if (!verifyChecksum(backupData)) {
                    return@withLock BackupResult.Failure(
                        IllegalStateException("Import checksum mismatch")
                    )
                }

                // Restore data
                customizationRepository.restoreBackup(backupData, merge)

                BackupResult.Success(null)
            } catch (e: Exception) {
                BackupResult.Failure(e)
            }
        }
    }

    /**
     * Delete a backup
     */
    suspend fun deleteBackup(backupId: String) {
        mutex.withLock {
            val history = _backupHistory.value.filter { it.id != backupId }
            _backupHistory.value = history
            saveBackupHistory(history)

            // Delete file
            deleteBackupFile(backupId)
        }
    }

    /**
     * Clear old backups
     */
    suspend fun clearOldBackups(keepCount: Int = 5) {
        mutex.withLock {
            val history = _backupHistory.value
            if (history.size > keepCount) {
                val toDelete = history.sortedByDescending { it.createdAt }
                    .drop(keepCount)

                toDelete.forEach { deleteBackup(it.id) }
            }
        }
    }

    /**
     * Enable scheduled backups
     */
    suspend fun enableScheduledBackups(intervalDays: Int = 7) {
        context.backupDataStore.edit { prefs ->
            prefs[Keys.SCHEDULED_BACKUP_ENABLED] = "true"
            prefs[Keys.SCHEDULED_BACKUP_INTERVAL] = intervalDays.toString()
        }
    }

    /**
     * Disable scheduled backups
     */
    suspend fun disableScheduledBackups() {
        context.backupDataStore.edit { prefs ->
            prefs[Keys.SCHEDULED_BACKUP_ENABLED] = "false"
        }
    }

    /**
     * Check if scheduled backup is due
     */
    suspend fun isScheduledBackupDue(): Boolean {
        val prefs = context.backupDataStore.data.first()
        val isEnabled = prefs[Keys.SCHEDULED_BACKUP_ENABLED] == "true"
        if (!isEnabled) return false

        val intervalDays = prefs[Keys.SCHEDULED_BACKUP_INTERVAL]?.toIntOrNull() ?: 7
        val lastBackup = prefs[Keys.LAST_BACKUP]?.toLongOrNull() ?: 0L

        val daysSinceLastBackup = (System.currentTimeMillis() - lastBackup) / (24 * 60 * 60 * 1000)
        return daysSinceLastBackup >= intervalDays
    }

    /**
     * Perform scheduled backup if due
     */
    suspend fun performScheduledBackupIfDue(): BackupResult? {
        if (isScheduledBackupDue()) {
            return createBackup(BackupType.FULL, BackupCategory.entries)
        }
        return null
    }

    // ═══════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════

    private fun createBackupId(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        return "backup_${dateFormat.format(Date())}"
    }

    private fun generateChecksum(backupData: BackupData): String {
        val dataWithoutChecksum = backupData.copy(checksum = "")
        return json.encodeToString(dataWithoutChecksum).hashCode().toString()
    }

    private fun verifyChecksum(backupData: BackupData): Boolean {
        val calculatedChecksum = generateChecksum(backupData)
        return calculatedChecksum == backupData.checksum
    }

    private fun saveBackupToFile(backupId: String, json: String): String {
        val file = context.getFileStreamPath("$backupId.json")
        file.outputStream().use { it.write(json.toByteArray()) }
        return file.absolutePath
    }

    private fun loadBackupFromFile(backupId: String): String {
        val file = context.getFileStreamPath("$backupId.json")
        return file.readText()
    }

    private fun deleteBackupFile(backupId: String) {
        val file = context.getFileStreamPath("$backupId.json")
        file.delete()
    }

    private suspend fun addBackupToHistory(backupInfo: BackupInfo) {
        val history = _backupHistory.value + backupInfo
        _backupHistory.value = history
        saveBackupHistory(history)
    }

    private suspend fun saveBackupHistory(history: List<BackupInfo>) {
        context.backupDataStore.edit { prefs ->
            prefs[Keys.BACKUP_HISTORY] = json.encodeToString(history)
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // DATA CLASSES
    // ═══════════════════════════════════════════════════════════════

    data class BackupInfo(
        val id: String,
        val type: BackupType,
        val categories: List<BackupCategory>,
        val createdAt: Long,
        val sizeBytes: Int,
        val checksum: String,
        val filePath: String
    )

    sealed class BackupResult {
        data class Success(val backupInfo: BackupInfo?) : BackupResult()
        data class Failure(val exception: Exception) : BackupResult()
    }
}
