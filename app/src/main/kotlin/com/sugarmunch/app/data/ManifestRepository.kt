package com.sugarmunch.app.data

import com.sugarmunch.app.data.local.AppDao
import com.sugarmunch.app.data.local.toAppEntry
import com.sugarmunch.app.data.local.toEntity
import com.sugarmunch.app.phaseone.PhaseOneUtilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for fetching and caching app manifests.
 * Uses only injected AppDao - no manual DB creation.
 */
@Singleton
class ManifestRepository @Inject constructor(
    private val appDao: AppDao,
    private val client: OkHttpClient
) {
    private val _trails = MutableStateFlow<List<CandyTrailEntry>>(emptyList())
    val trails: Flow<List<CandyTrailEntry>> = _trails.asStateFlow()

    suspend fun fetchApps(): Result<List<AppEntry>> = withContext(Dispatchers.IO) {
        runCatching {
            // Try to fetch from network
            val networkApps = fetchFromNetwork()
            
            // Cache to database
            appDao.deleteAllApps()
            appDao.insertApps(networkApps.map { it.toEntity() })
            
            networkApps
        }.recoverCatching {
            // Fallback to cached data on network failure
            val cached = appDao.getAppCount()
            if (cached > 0) {
                fetchFromCache()
            } else {
                throw it
            }
        }
    }

    private suspend fun fetchFromNetwork(): List<AppEntry> {
        val request = Request.Builder().url(DEFAULT_MANIFEST_URL).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: ${response.message}")
        }
        val body = response.body?.string() ?: throw Exception("Empty response")
        val manifest = com.google.gson.Gson().fromJson(body, AppsManifest::class.java)
        _trails.value = PhaseOneUtilities.mergeTrails(manifest.trails ?: emptyList())
        return PhaseOneUtilities.mergeApps(manifest.apps)
    }

    private suspend fun fetchFromCache(): List<AppEntry> {
        val flow = appDao.getAllApps().map { entities ->
            entities.map { it.toAppEntry() }
        }
        return PhaseOneUtilities.mergeApps(flow.firstOrNull() ?: emptyList())
    }

    fun getCachedApps(): Flow<List<AppEntry>> {
        return appDao.getAllApps().map { entities ->
            entities.map { it.toAppEntry() }
        }
    }

    /** Current trails (updated after fetchFromNetwork). */
    fun getTrails(): List<CandyTrailEntry> = _trails.value

    companion object {
        const val DEFAULT_MANIFEST_URL = "https://raw.githubusercontent.com/sugarmunch/SugarMunch/main/docs/sugarmunch-apps.json"
    }
}
