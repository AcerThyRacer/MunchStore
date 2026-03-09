package com.sugarmunch.app.backup.proton

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Proton Drive API Service
 * 
 * Retrofit interface for Proton Drive API operations
 */
interface ProtonDriveApiService {

    // ═════════════════════════════════════════════════════════════
    // AUTHENTICATION
    // ═════════════════════════════════════════════════════════════

    /**
     * Get Proton authentication info (SRP salt, etc.)
     */
    @GET("/core/v4/auth/info")
    suspend fun getAuthInfo(@Query("Email") email: String): Response<ProtonAuthResponse>

    /**
     * Create Proton session (login)
     */
    @POST("/core/v4/auth")
    suspend fun createSession(@Body loginRequest: ProtonLoginRequest): Response<ProtonAuthResponse>

    /**
     * Refresh access token
     */
    @POST("/core/v4/auth/refresh")
    suspend fun refreshToken(@Body refreshToken: Map<String, String>): Response<ProtonAuthResponse>

    /**
     * Logout and invalidate session
     */
    @DELETE("/core/v4/auth")
    suspend fun logout(): Response<ProtonAuthResponse>

    // ═════════════════════════════════════════════════════════════
    // DRIVE OPERATIONS
    // ═════════════════════════════════════════════════════════════

    /**
     * List files in a folder
     */
    @GET("/drive/v4/files")
    suspend fun listFiles(
        @Header("Authorization") accessToken: String,
        @Query("ParentLinkID") parentLinkId: String? = null,
        @Query("Page") page: Int = 0,
        @Query("PageSize") pageSize: Int = 50
    ): Response<ProtonDriveResponse>

    /**
     * List folders
     */
    @GET("/drive/v4/folders")
    suspend fun listFolders(
        @Header("Authorization") accessToken: String,
        @Query("ParentLinkID") parentLinkId: String? = null
    ): Response<ProtonDriveResponse>

    /**
     * Create folder
     */
    @POST("/drive/v4/folders")
    suspend fun createFolder(
        @Header("Authorization") accessToken: String,
        @Body folderData: Map<String, Any>
    ): Response<ProtonDriveResponse>

    /**
     * Get file metadata
     */
    @GET("/drive/v4/files/{fileId}")
    suspend fun getFileMetadata(
        @Header("Authorization") accessToken: String,
        @Path("fileId") fileId: String
    ): Response<ProtonDriveResponse>

    /**
     * Delete file
     */
    @DELETE("/drive/v4/files/{fileId}")
    suspend fun deleteFile(
        @Header("Authorization") accessToken: String,
        @Path("fileId") fileId: String
    ): Response<ProtonAuthResponse>

    /**
     * Delete folder
     */
    @DELETE("/drive/v4/folders/{folderId}")
    suspend fun deleteFolder(
        @Header("Authorization") accessToken: String,
        @Path("folderId") folderId: String
    ): Response<ProtonAuthResponse>

    // ═════════════════════════════════════════════════════════════
    // FILE UPLOAD/DOWNLOAD
    // ═════════════════════════════════════════════════════════════

    /**
     * Create file upload session
     */
    @POST("/drive/v4/files/upload")
    suspend fun createUploadSession(
        @Header("Authorization") accessToken: String,
        @Body uploadRequest: ProtonUploadRequest
    ): Response<ProtonUploadResponse>

    /**
     * Upload file content (multipart)
     */
    @Multipart
    @POST("/upload/{uploadId}")
    suspend fun uploadFileContent(
        @Header("Authorization") accessToken: String,
        @Path("uploadId") uploadId: String,
        @Part fileData: MultipartBody.Part
    ): Response<ProtonUploadResponse>

    /**
     * Get download URL for file
     */
    @GET("/drive/v4/files/{fileId}/download")
    suspend fun getDownloadUrl(
        @Header("Authorization") accessToken: String,
        @Path("fileId") fileId: String
    ): Response<ProtonDownloadResponse>

    /**
     * Download file content
     */
    @GET
    suspend fun downloadFile(
        @Url url: String,
        @Header("Authorization") accessToken: String
    ): Response<ResponseBody>

    // ═════════════════════════════════════════════════════════════
    // SUGARMUNCH BACKUP ENDPOINTS
    // ═════════════════════════════════════════════════════════════

    /**
     * Create SugarMunch backup folder if it doesn't exist
     */
    @POST("/drive/v4/folders")
    suspend fun createBackupFolder(
        @Header("Authorization") accessToken: String,
        @Body folderData: Map<String, Any>
    ): Response<ProtonDriveResponse>

    /**
     * Get backup folder by name
     */
    @GET("/drive/v4/folders")
    suspend fun getBackupFolder(
        @Header("Authorization") accessToken: String,
        @Query("Name") folderName: String
    ): Response<ProtonDriveResponse>

    /**
     * Upload backup manifest
     */
    @POST("/drive/v4/files")
    suspend fun uploadManifest(
        @Header("Authorization") accessToken: String,
        @Body manifestData: Map<String, Any>
    ): Response<ProtonUploadResponse>

    /**
     * List all SugarMunch backups
     */
    @GET("/drive/v4/files")
    suspend fun listBackups(
        @Header("Authorization") accessToken: String,
        @Query("ParentLinkID") parentFolderId: String
    ): Response<ProtonDriveResponse>
}
