package com.sugarmunch.app.backup.proton

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Proton Drive API Data Models
 * 
 * Based on Proton Drive API documentation:
 * https://github.com/ProtonMail/proton-drive-api
 */

// ═════════════════════════════════════════════════════════════
// AUTHENTICATION MODELS
// ═════════════════════════════════════════════════════════════

/**
 * Proton authentication response
 */
@Serializable
data class ProtonAuthResponse(
    val Code: Int,
    val Auth: AuthData? = null,
    val Error: String? = null
)

@Serializable
data class AuthData(
    val AccessToken: String,
    val RefreshToken: String,
    val ExpiresIn: Long,
    val TokenType: String = "Bearer",
    val Scope: String,
    val UID: String,
    val AddressID: String,
    val AddressEmail: String
)

/**
 * Proton login request
 */
@Serializable
data class ProtonLoginRequest(
    val Username: String,
    val Password: String,
    val TwoFactorCode: String? = null,
    val Type: String = "login"
)

// ═════════════════════════════════════════════════════════════
// DRIVE MODELS
// ═════════════════════════════════════════════════════════════

/**
 * Proton Drive file/folder response
 */
@Serializable
data class ProtonDriveResponse(
    val Code: Int,
    val Files: List<ProtonFile>? = null,
    val Folders: List<ProtonFolder>? = null,
    val Error: String? = null
)

/**
 * Proton file metadata
 */
@Serializable
data class ProtonFile(
    val ID: String,
    val Name: String,
    val Size: Long,
    val MIMEType: String,
    val CreateTime: Long,
    val ModifyTime: Long,
    val Hash: String,
    val LinkID: String,
    val ParentLinkID: String,
    val Type: Int, // 1 = file, 2 = folder
    val State: Int // 1 = active, 2 = deleted
)

/**
 * Proton folder metadata
 */
@Serializable
data class ProtonFolder(
    val ID: String,
    val Name: String,
    val CreateTime: Long,
    val ModifyTime: Long,
    val ParentLinkID: String,
    val State: Int
)

/**
 * File upload request
 */
@Serializable
data class ProtonUploadRequest(
    val Filename: String,
    val MIMEType: String,
    val Size: Long,
    val ParentLinkID: String,
    val Hash: String
)

/**
 * File upload response
 */
@Serializable
data class ProtonUploadResponse(
    val Code: Int,
    val File: UploadedFile? = null,
    val Error: String? = null
)

@Serializable
data class UploadedFile(
    val ID: String,
    val Name: String,
    val Hash: String,
    val LinkID: String
)

/**
 * File download response
 */
@Serializable
data class ProtonDownloadResponse(
    val Code: Int,
    val URL: String? = null,
    val Error: String? = null
)

// ═════════════════════════════════════════════════════════════
// BACKUP SPECIFIC MODELS
// ═════════════════════════════════════════════════════════════

/**
 * SugarMunch backup metadata
 */
@Serializable
data class BackupMetadata(
    val backupId: String,
    val userId: String,
    val timestamp: Long,
    val version: String,
    val backupType: BackupType,
    val fileSize: Long,
    val encrypted: Boolean,
    val checksum: String,
    val compressionType: CompressionType,
    val includedData: List<DataType>
)

@Serializable
enum class BackupType {
    FULL,
    INCREMENTAL,
    THEMES_ONLY,
    EFFECTS_ONLY,
    SETTINGS_ONLY
}

@Serializable
enum class CompressionType {
    NONE,
    GZIP,
    ZIP
}

@Serializable
enum class DataType {
    THEMES,
    EFFECTS,
    SETTINGS,
    ACHIEVEMENTS,
    SHOP_INVENTORY,
    DAILY_REWARDS,
    AUTOMATIONS,
    FAVORITES,
    HISTORY
}

/**
 * Backup manifest for tracking all backup files
 */
@Serializable
data class BackupManifest(
    val manifestId: String,
    val userId: String,
    val created: Long,
    val updated: Long,
    val backups: List<BackupMetadata>,
    val totalSize: Long,
    val encryptionVersion: String = "AES-256-GCM"
)

/**
 * Restore request
 */
@Serializable
data class RestoreRequest(
    val backupId: String,
    val restoreTypes: List<DataType>,
    val overwriteExisting: Boolean
)

/**
 * Restore response
 */
@Serializable
data class RestoreResponse(
    val success: Boolean,
    val restoredItems: Int,
    val failedItems: Int,
    val errors: List<String> = emptyList()
)

// ═════════════════════════════════════════════════════════════
// API CONFIG
// ═════════════════════════════════════════════════════════════

object ProtonApiConfig {
    const val BASE_URL = "https://drive.proton.me"
    const val AUTH_URL = "https://account.proton.me"
    const val API_VERSION = "4"
    
    // OAuth scopes
    const val SCOPE_DRIVE = "https://www.protonmail.ch/drive_scope"
    const val SCOPE_MAIL = "https://www.protonmail.ch/mail_scope"
    
    // App-specific identifiers (would be registered with Proton)
    const val CLIENT_ID = "sugarmunch_app"
    const val REDIRECT_URI = "com.sugarmunch.app://proton/callback"
    
    // Backup configuration
    const val BACKUP_FOLDER_NAME = "SugarMunch Backups"
    const val MAX_BACKUP_SIZE_BYTES = 100L * 1024 * 1024 // 100MB
    const val BACKUP_RETENTION_DAYS = 90
}
