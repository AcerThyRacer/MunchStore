package com.sugarmunch.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY sortOrder ASC, name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderById(id: String): FolderEntity?

    @Query("SELECT * FROM folders WHERE id = :id")
    fun getFolderByIdFlow(id: String): Flow<FolderEntity?>

    @Query("SELECT * FROM folders WHERE parentId IS NULL ORDER BY sortOrder ASC, name ASC")
    fun getRootFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE parentId = :parentId ORDER BY sortOrder ASC, name ASC")
    fun getChildFolders(parentId: String): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE isSystemFolder = 0 ORDER BY name ASC")
    fun getCustomFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE folderStyle = :style ORDER BY name ASC")
    fun getFoldersByStyle(style: String): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>)

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteFolderById(id: String)

    @Query("SELECT * FROM folders WHERE parentId = :parentId")
    suspend fun getChildFoldersSync(parentId: String): List<FolderEntity>

    @Query("SELECT COUNT(*) FROM folders")
    suspend fun getFolderCount(): Int

    @Query("SELECT * FROM folders WHERE appIds LIKE '%' || :appId || '%'")
    fun getFoldersContainingApp(appId: String): Flow<List<FolderEntity>>

    @Query("UPDATE folders SET appIds = :appIds, updatedAt = :timestamp WHERE id = :folderId")
    suspend fun updateFolderApps(folderId: String, appIds: List<String>, timestamp: Long)

    @Query("UPDATE folders SET subFolderIds = :subFolderIds, updatedAt = :timestamp WHERE id = :folderId")
    suspend fun updateFolderSubFolders(folderId: String, subFolderIds: List<String>, timestamp: Long)

    @Query("UPDATE folders SET sortOrder = :sortOrder, updatedAt = :timestamp WHERE id = :folderId")
    suspend fun updateFolderSortOrder(folderId: String, sortOrder: Int, timestamp: Long)
}
