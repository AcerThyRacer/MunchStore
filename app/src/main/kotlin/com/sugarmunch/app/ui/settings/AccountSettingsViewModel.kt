package com.sugarmunch.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sugarmunch.app.auth.AuthManager
import com.sugarmunch.app.backup.CloudBackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Account statistics data class
 */
data class AccountStats(
    val themesCreated: Int = 0,
    val effectsCreated: Int = 0,
    val totalDownloads: Int = 0,
    val level: Int = 1,
    val xp: Int = 0,
    val sugarPoints: Int = 0
)

/**
 * UI state for Account Settings screen
 */
data class AccountUiState(
    val user: com.sugarmunch.app.auth.UserSession? = null,
    val stats: AccountStats? = null,
    val isLoading: Boolean = false,
    val isExporting: Boolean = false
)

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authManager: AuthManager,
    private val backupManager: CloudBackupManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()
    
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()
    
    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadUserData()
    }
    
    /**
     * Load user data and stats
     */
    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Get current user
                val user = authManager.currentUser.value
                
                // TODO: Load actual stats from repositories
                val stats = AccountStats(
                    themesCreated = 0, // Load from ThemeProfileRepository
                    effectsCreated = 0, // Load from EffectEngine
                    totalDownloads = 0, // Load from AppUsageRepository
                    level = 1, // Load from SugarPassManager
                    xp = 0, // Load from XpManager
                    sugarPoints = 0 // Load from ShopManager
                )
                
                _uiState.value = AccountUiState(
                    user = user,
                    stats = stats,
                    isLoading = false
                )
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load account data: ${e.message}"
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * Link account with Google
     */
    fun linkWithGoogle() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // TODO: Implement Google Sign-In flow
                // This would require launching Google Sign-In intent
                // For now, show a message
                _errorMessage.value = "Google Sign-In flow not yet implemented"
                
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to link Google account: ${e.message}"
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * Show change email dialog
     */
    fun showChangeEmailDialog() {
        // TODO: Implement change email dialog
        _errorMessage.value = "Change email feature coming soon"
    }
    
    /**
     * Show change password dialog
     */
    fun showChangePasswordDialog() {
        // TODO: Implement change password dialog
        _errorMessage.value = "Change password feature coming soon"
    }
    
    /**
     * Export personal data
     */
    fun exportPersonalData() {
        _showExportDialog.value = true
    }
    
    /**
     * Export data as JSON
     */
    fun exportDataAsJSON() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isExporting = true)
                
                // TODO: Implement JSON export
                backupManager.exportDataToJson()
                
                _showExportDialog.value = false
                _uiState.value = _uiState.value.copy(isExporting = false)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to export data: ${e.message}"
                _uiState.value = _uiState.value.copy(isExporting = false)
            }
        }
    }
    
    /**
     * Export data as ZIP
     */
    fun exportDataAsZIP() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isExporting = true)
                
                // TODO: Implement ZIP export
                backupManager.exportDataToZip()
                
                _showExportDialog.value = false
                _uiState.value = _uiState.value.copy(isExporting = false)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to export data: ${e.message}"
                _uiState.value = _uiState.value.copy(isExporting = false)
            }
        }
    }
    
    /**
     * Dismiss export dialog
     */
    fun dismissExportDialog() {
        _showExportDialog.value = false
    }
    
    /**
     * Show delete account confirmation
     */
    fun showDeleteConfirmation() {
        _showDeleteDialog.value = true
    }
    
    /**
     * Dismiss delete dialog
     */
    fun dismissDeleteDialog() {
        _showDeleteDialog.value = false
    }
    
    /**
     * Delete user account
     */
    fun deleteAccount() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val result = authManager.deleteAccount()
                
                if (result.isSuccess) {
                    _showDeleteDialog.value = false
                    _uiState.value = AccountUiState()
                    // Navigate back or to onboarding
                } else {
                    _errorMessage.value = "Failed to delete account: ${result.exceptionOrNull()?.message}"
                }
                
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete account: ${e.message}"
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * Sign out user
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val result = authManager.signOut()
                
                if (result.isSuccess) {
                    _uiState.value = AccountUiState()
                    // Navigate back or to onboarding
                } else {
                    _errorMessage.value = "Failed to sign out: ${result.exceptionOrNull()?.message}"
                }
                
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to sign out: ${e.message}"
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * Dismiss error message
     */
    fun dismissError() {
        _errorMessage.value = null
    }
}
