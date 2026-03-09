package com.sugarmunch.app.data.repository

import android.content.Context
import com.sugarmunch.app.data.local.AppDao
import com.sugarmunch.app.data.local.FolderDao
import com.sugarmunch.app.data.local.FolderEntity
import com.sugarmunch.app.data.local.FolderStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Smart folder suggestion data class
 */
data class FolderSuggestion(
    val folder: FolderEntity?,
    val reason: String,
    val shouldCreate: Boolean,
    val suggestedName: String? = null,
    val confidence: Float = 0.5f
)

/**
 * Repository for managing app folders in the SugarMunch launcher.
 * Handles folder CRUD operations, smart categorization, and folder-app relationships.
 */
class FolderRepository(
    private val folderDao: FolderDao,
    private val appDao: AppDao,
    private val context: Context
) {
    private val mutex = Mutex()

    /**
     * Get all folders as a Flow
     */
    fun getAllFolders(): Flow<List<FolderEntity>> = folderDao.getAllFolders()

    /**
     * Get root-level folders (no parent)
     */
    fun getRootFolders(): Flow<List<FolderEntity>> = folderDao.getRootFolders()

    /**
     * Get child folders of a specific parent
     */
    fun getChildFolders(parentId: String): Flow<List<FolderEntity>> =
        folderDao.getChildFolders(parentId)

    /**
     * Get custom (user-created) folders only
     */
    fun getCustomFolders(): Flow<List<FolderEntity>> = folderDao.getCustomFolders()

    /**
     * Get a specific folder by ID
     */
    suspend fun getFolderById(id: String): FolderEntity? = folderDao.getFolderById(id)

    /**
     * Get folders containing a specific app
     */
    fun getFoldersContainingApp(appId: String): Flow<List<FolderEntity>> =
        folderDao.getFoldersContainingApp(appId)

    /**
     * Create a new folder
     */
    suspend fun createFolder(
        name: String,
        parentId: String? = null,
        style: FolderStyle = FolderStyle.DEFAULT,
        iconColor: String? = null,
        backgroundColor: String? = null,
        isSystemFolder: Boolean = false
    ): Long = mutex.withLock {
        val folder = FolderEntity(
            id = generateFolderId(),
            name = name,
            parentId = parentId,
            folderStyle = style.name,
            iconColor = iconColor,
            backgroundColor = backgroundColor,
            isSystemFolder = isSystemFolder,
            appIds = emptyList(),
            subFolderIds = emptyList()
        )
        folderDao.insertFolder(folder)
    }

    /**
     * Update an existing folder
     */
    suspend fun updateFolder(folder: FolderEntity) = mutex.withLock {
        folderDao.updateFolder(folder.copy(updatedAt = System.currentTimeMillis()))
    }

    /**
     * Delete a folder
     */
    suspend fun deleteFolder(folder: FolderEntity) = mutex.withLock {
        // Remove folder from parent's subFolderIds if it has a parent
        folder.parentId?.let { parentId ->
            val parent = folderDao.getFolderById(parentId)
            parent?.let {
                val updatedSubFolders = parent.subFolderIds - folder.id
                folderDao.updateFolderSubFolders(parentId, updatedSubFolders, System.currentTimeMillis())
            }
        }
        folderDao.deleteFolder(folder)
    }

    /**
     * Add an app to a folder
     */
    suspend fun addAppToFolder(folderId: String, appId: String) = mutex.withLock {
        val folder = folderDao.getFolderById(folderId) ?: return@withLock
        val updatedAppIds = folder.appIds + appId
        folderDao.updateFolderApps(folderId, updatedAppIds, System.currentTimeMillis())
    }

    /**
     * Remove an app from a folder
     */
    suspend fun removeAppFromFolder(folderId: String, appId: String) = mutex.withLock {
        val folder = folderDao.getFolderById(folderId) ?: return@withLock
        val updatedAppIds = folder.appIds - appId
        folderDao.updateFolderApps(folderId, updatedAppIds, System.currentTimeMillis())
    }

    /**
     * Add a subfolder to a parent folder
     */
    suspend fun addSubFolder(parentId: String, subFolderId: String) = mutex.withLock {
        val parent = folderDao.getFolderById(parentId) ?: return@withLock
        val updatedSubFolders = parent.subFolderIds + subFolderId
        folderDao.updateFolderSubFolders(parentId, updatedSubFolders, System.currentTimeMillis())
    }

    /**
     * Remove a subfolder from a parent folder
     */
    suspend fun removeSubFolder(parentId: String, subFolderId: String) = mutex.withLock {
        val parent = folderDao.getFolderById(parentId) ?: return@withLock
        val updatedSubFolders = parent.subFolderIds - subFolderId
        folderDao.updateFolderSubFolders(parentId, updatedSubFolders, System.currentTimeMillis())
    }

    /**
     * Update folder sort order
     */
    suspend fun updateSortOrder(folderId: String, sortOrder: Int) = mutex.withLock {
        folderDao.updateFolderSortOrder(folderId, sortOrder, System.currentTimeMillis())
    }

    /**
     * Initialize default system folders
     */
    suspend fun initializeSystemFolders() = mutex.withLock {
        val existingFolders = folderDao.getAllFolders().first()
        if (existingFolders.isNotEmpty()) return@withLock

        val timestamp = System.currentTimeMillis()

        // Master SugarMunch folder (root)
        val masterFolderId = "sugarmunch_master"
        val masterFolder = FolderEntity(
            id = masterFolderId,
            name = "SugarMunch",
            parentId = null,
            folderStyle = FolderStyle.GLASSMORPHIC.name,
            iconColor = "#FF69B4", // Hot pink
            backgroundColor = "#1A1A2E",
            isSystemFolder = true,
            sortOrder = 0,
            createdAt = timestamp,
            updatedAt = timestamp
        )
        folderDao.insertFolder(masterFolder)

        // Smart sub-folders
        val categories = listOf(
            "Games" to FolderStyle.NEON,
            "Productivity" to FolderStyle.CRYSTAL,
            "Social" to FolderStyle.LIQUID,
            "Utilities" to FolderStyle.DEFAULT,
            "Video & Music" to FolderStyle.HOLOGRAPHIC
        )

        categories.forEachIndexed { index, (category, style) ->
            val subFolder = FolderEntity(
                id = "sugarmunch_${category.lowercase().replace(" ", "_")}",
                name = category,
                parentId = masterFolderId,
                folderStyle = style.name,
                isSystemFolder = true,
                sortOrder = index + 1,
                createdAt = timestamp,
                updatedAt = timestamp
            )
            folderDao.insertFolder(subFolder)
        }
    }

    /**
     * Smart categorization: Automatically sort apps into appropriate folders
     */
    suspend fun autoCategorizeApps() = mutex.withLock {
        val allApps = appDao.getAllApps().first()
        val folders = folderDao.getAllFolders().first()

        allApps.forEach { app ->
            app.category?.let { category ->
                // Find matching folder
                val matchingFolder = folders.find { folder ->
                    folder.name.equals(category, ignoreCase = true) &&
                    folder.parentId == "sugarmunch_master"
                }

                if (matchingFolder != null && app.id !in matchingFolder.appIds) {
                    addAppToFolder(matchingFolder.id, app.id)
                }
            }
        }
    }

    /**
     * Smart folder suggestions for an app
     * Returns a list of suggested folders based on:
     * - Category matching
     * - Usage patterns (frequently used apps)
     * - Existing folder memberships
     * - Time-based patterns (morning, evening apps)
     */
    suspend fun suggestFoldersForApp(app: com.sugarmunch.app.hub.UnifiedAppInfo): List<FolderSuggestion> =
        withContext(Dispatchers.Default) {
            val suggestions = mutableListOf<FolderSuggestion>()
            val folders = folderDao.getAllFolders().first()

            // Suggest based on category
            app.category?.let { category ->
                val matchingFolder = folders.find { folder ->
                    folder.name.equals(category, ignoreCase = true)
                }
                if (matchingFolder != null && app.id !in matchingFolder.appIds) {
                    suggestions.add(
                        FolderSuggestion(
                            folder = matchingFolder,
                            reason = "Matches category: $category",
                            shouldCreate = false,
                            confidence = 0.9f
                        )
                    )
                }
            }

            // Suggest based on usage patterns
            if (app.launchCount > 10) {
                val frequentFolder = folders.find { it.name.equals("Frequent", ignoreCase = true) }
                if (frequentFolder != null && app.id !in frequentFolder.appIds) {
                    suggestions.add(
                        FolderSuggestion(
                            folder = frequentFolder,
                            reason = "Frequently used app (${app.launchCount} launches)",
                            shouldCreate = false,
                            confidence = 0.8f
                        )
                    )
                }
            }

            // Suggest based on existing folder memberships
            if (app.folderIds.isNotEmpty()) {
                app.folderIds.forEach { folderId ->
                    folders.find { it.id == folderId }?.let { folder ->
                        // Find sibling folders
                        folder.parentId?.let { parentId ->
                            folders.find { it.id == parentId }?.let { parent ->
                                parent.subFolderIds.forEach { siblingId ->
                                    if (siblingId != folderId) {
                                        folders.find { it.id == siblingId }?.let { sibling ->
                                            if (app.id !in sibling.appIds) {
                                                suggestions.add(
                                                    FolderSuggestion(
                                                        folder = sibling,
                                                        reason = "Related to ${folder.name}",
                                                        shouldCreate = false,
                                                        confidence = 0.6f
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Suggest based on app type (system vs SugarMunch)
            if (app.isSugarMunchApp) {
                val sugarmunchFolder = folders.find { it.name.equals("SugarMunch", ignoreCase = true) }
                if (sugarmunchFolder != null && app.id !in sugarmunchFolder.appIds) {
                    suggestions.add(
                        FolderSuggestion(
                            folder = sugarmunchFolder,
                            reason = "SugarMunch app",
                            shouldCreate = false,
                            confidence = 0.7f
                        )
                    )
                }
            }

            // Suggest creating new folder if no good match
            if (suggestions.isEmpty()) {
                val suggestedName = app.category ?: "Apps"
                suggestions.add(
                    FolderSuggestion(
                        folder = null,
                        reason = "No matching folder found",
                        shouldCreate = true,
                        suggestedName = suggestedName,
                        confidence = 0.5f
                    )
                )
            }

            // Sort by confidence
            suggestions.sortedByDescending { it.confidence }
        }

    /**
     * Get folder with all apps included
     */
    suspend fun getFolderWithApps(folderId: String): FolderEntity? {
        val folder = folderDao.getFolderById(folderId) ?: return null
        return folder
    }

    /**
     * Search folders by name
     */
    fun searchFolders(query: String): Flow<List<FolderEntity>> {
        return folderDao.getAllFolders().map { folders ->
            folders.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    /**
     * Export folder configuration as JSON
     */
    suspend fun exportFolderConfig(): String {
        val folders = folderDao.getAllFolders().first()
        return com.google.gson.Gson().toJson(folders)
    }

    /**
     * Import folder configuration from JSON
     */
    suspend fun importFolderConfig(json: String) = mutex.withLock {
        val folders = com.google.gson.Gson().fromJson(
            json,
            Array<FolderEntity>::class.java
        ).toList()
        folderDao.insertFolders(folders)
    }

    private fun generateFolderId(): String {
        return "folder_${System.currentTimeMillis()}_${(0..10000).random()}"
    }
}
