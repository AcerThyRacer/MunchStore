package com.sugarmunch.app.p2p

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.candyJarDataStore: DataStore<Preferences> by preferencesDataStore(name = "candy_jar")

/**
 * Manages "Candy Jar" opt-in: user can host pieces of the app catalog for P2P distribution.
 */
class CandyJarManager private constructor(private val context: Context) {
    private val dataStore = context.candyJarDataStore
    private val isHostEnabledKey = booleanPreferencesKey("is_host_enabled")
    private val hostedAppIdsKey = stringSetPreferencesKey("hosted_app_ids")

    val isHostEnabled: Flow<Boolean> = dataStore.data.map { it[isHostEnabledKey] ?: false }
    val hostedAppIds: Flow<Set<String>> = dataStore.data.map { it[hostedAppIdsKey] ?: emptySet() }

    suspend fun setHostEnabled(enabled: Boolean) {
        dataStore.edit { it[isHostEnabledKey] = enabled }
    }

    suspend fun addHostedApp(appId: String) {
        dataStore.edit { prefs ->
            val current = prefs[hostedAppIdsKey] ?: emptySet()
            prefs[hostedAppIdsKey] = current + appId
        }
    }

    suspend fun removeHostedApp(appId: String) {
        dataStore.edit { prefs ->
            val current = prefs[hostedAppIdsKey] ?: emptySet()
            prefs[hostedAppIdsKey] = current - appId
        }
    }

    companion object {
        @Volatile
        private var instance: CandyJarManager? = null
        fun getInstance(context: Context): CandyJarManager =
            instance ?: synchronized(this) {
                instance ?: CandyJarManager(context.applicationContext).also { instance = it }
            }
    }
}
