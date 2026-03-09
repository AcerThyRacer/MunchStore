package com.sugarmunch.app.backup.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sugarmunch.app.backup.proton.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Backup & Restore screen
 */
sealed class BackupRestoreUiState {
    object Unauthenticated : BackupRestoreUiState()
    object Loading : BackupRestoreUiState()
    data class Authenticated(
        val email: String,
        val userId: String,
        val backups: List<BackupMetadata>,
        val storageUsage: StorageUsage
    ) : BackupRestoreUiState()
    data class BackingUp(
        val progress: Float,
        val status: String,
        val backups: List<BackupMetadata>,
        val storageUsage: StorageUsage
    ) : BackupRestoreUiState()
    data class Restoring(
        val progress: Float,
        val status: String,
        val backups: List<BackupMetadata>,
        val storageUsage: StorageUsage
    ) : BackupRestoreUiState()
    data class Error(val message: String) : BackupRestoreUiState()
}

/**
 * ViewModel for Backup & Restore functionality
 */
@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    application: Application,
    private val protonDriveManager: ProtonDriveManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<BackupRestoreUiState>(BackupRestoreUiState.Loading)
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadInitialState()
    }

    /**
     * Load initial state from Proton Drive Manager
     */
    private fun loadInitialState() {
        viewModelScope.launch {
            protonDriveManager.authState.collect { authState ->
                when (authState) {
                    is ProtonAuthState.Unauthenticated -> {
                        _uiState.value = BackupRestoreUiState.Unauthenticated
                    }
                    is ProtonAuthState.Authenticating -> {
                        _uiState.value = BackupRestoreUiState.Loading
                    }
                    is ProtonAuthState.Authenticated -> {
                        loadBackups()
                    }
                    is ProtonAuthState.Error -> {
                        _uiState.value = BackupRestoreUiState.Error(authState.message)
                    }
                }
            }
        }

        viewModelScope.launch {
            protonDriveManager.backupState.collect { backupState ->
                when (backupState) {
                    is BackupState.Idle -> {
                        // Refresh backups list
                        if (_uiState.value is BackupRestoreUiState.BackingUp ||
                            _uiState.value is BackupRestoreUiState.Restoring) {
                            loadBackups()
                        }
                    }
                    is BackupState.BackingUp -> {
                        val currentState = _uiState.value
                        if (currentState is BackupRestoreUiState.Authenticated) {
                            _uiState.value = BackupRestoreUiState.BackingUp(
                                progress = backupState.progress,
                                status = backupState.status,
                                backups = currentState.backups,
                                storageUsage = currentState.storageUsage
                            )
                        }
                    }
                    is BackupState.Restoring -> {
                        val currentState = _uiState.value
                        if (currentState is BackupRestoreUiState.Authenticated ||
                            currentState is BackupRestoreUiState.BackingUp) {
                            val backups = when (currentState) {
                                is BackupRestoreUiState.BackingUp -> currentState.backups
                                is BackupRestoreUiState.Authenticated -> currentState.backups
                                else -> emptyList()
                            }
                            val storageUsage = when (currentState) {
                                is BackupRestoreUiState.BackingUp -> currentState.storageUsage
                                is BackupRestoreUiState.Authenticated -> currentState.storageUsage
                                else -> StorageUsage(0, 0, null, null)
                            }
                            _uiState.value = BackupRestoreUiState.Restoring(
                                progress = backupState.progress,
                                status = backupState.status,
                                backups = backups,
                                storageUsage = storageUsage
                            )
                        }
                    }
                    is BackupState.Error -> {
                        _error.value = backupState.message
                    }
                }
            }
        }
    }

    /**
     * Login to Proton account
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = BackupRestoreUiState.Loading
            val result = protonDriveManager.login(email, password)
            result.onFailure { error ->
                _error.value = error.message ?: "Login failed"
                _uiState.value = BackupRestoreUiState.Unauthenticated
            }
        }
    }

    /**
     * Logout from Proton account
     */
    fun logout() {
        viewModelScope.launch {
            protonDriveManager.logout()
            _uiState.value = BackupRestoreUiState.Unauthenticated
        }
    }

    /**
     * Load backups list
     */
    fun loadBackups() {
        viewModelScope.launch {
            val backupsResult = protonDriveManager.listBackups()
            val storageResult = protonDriveManager.getStorageUsage()

            val authState = protonDriveManager.authState.value
            if (authState is ProtonAuthState.Authenticated) {
                _uiState.value = BackupRestoreUiState.Authenticated(
                    email = authState.email,
                    userId = authState.userId,
                    backups = backupsResult.getOrNull() ?: emptyList(),
                    storageUsage = storageResult.getOrNull() ?: StorageUsage(0, 0, null, null)
                )
            }
        }
    }

    /**
     * Create a new backup
     */
    fun createBackup(type: BackupType) {
        viewModelScope.launch {
            protonDriveManager.createBackup(type)
        }
    }

    /**
     * Restore from backup
     */
    fun restoreBackup(backup: BackupMetadata) {
        viewModelScope.launch {
            protonDriveManager.restoreBackup(backup.backupId)
        }
    }

    /**
     * Delete backup
     */
    fun deleteBackup(backup: BackupMetadata) {
        viewModelScope.launch {
            protonDriveManager.deleteBackup(backup.backupId)
            loadBackups()
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Retry last operation
     */
    fun retry() {
        loadInitialState()
    }
}
