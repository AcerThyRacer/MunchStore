package com.sugarmunch.app.trails

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.trailProgressDataStore: DataStore<Preferences> by preferencesDataStore(name = "trail_progress")

/**
 * Tracks which Candy Trails the user has completed (e.g. installed all apps on the trail).
 * Used to unlock cosmetic rewards in the store.
 */
class TrailProgressManager private constructor(private val context: Context) {

    private val dataStore = context.trailProgressDataStore
    private val completedTrailsKey = stringSetPreferencesKey("completed_trail_ids")
    private val unlockedRewardsKey = stringSetPreferencesKey("unlocked_reward_ids")

    val completedTrails: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[completedTrailsKey] ?: emptySet()
    }

    val unlockedRewards: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[unlockedRewardsKey] ?: emptySet()
    }

    suspend fun markTrailCompleted(trailId: String) {
        dataStore.edit { prefs ->
            val current = prefs[completedTrailsKey] ?: emptySet()
            prefs[completedTrailsKey] = current + trailId
            // Unlock cosmetic reward for completing any trail
            val rewards = prefs[unlockedRewardsKey] ?: emptySet()
            prefs[unlockedRewardsKey] = rewards + "trail_explorer_badge"
        }
    }

    suspend fun markAppInstalledFromTrail(trailId: String, appId: String, allAppIdsInTrail: List<String>) {
        // Caller should check: if (installedCount + 1 >= allAppIdsInTrail.size) markTrailCompleted(trailId)
        // This method is for future use when we track per-app installs per trail
        dataStore.edit { prefs ->
            val current = prefs[completedTrailsKey] ?: emptySet()
            if (appId in allAppIdsInTrail) {
                // For now we don't track partial progress; caller can call markTrailCompleted when all installed
                prefs[completedTrailsKey] = current
            }
        }
    }

    companion object {
        @Volatile
        private var instance: TrailProgressManager? = null
        fun getInstance(context: Context): TrailProgressManager =
            instance ?: synchronized(this) {
                instance ?: TrailProgressManager(context.applicationContext).also { instance = it }
            }
    }
}
