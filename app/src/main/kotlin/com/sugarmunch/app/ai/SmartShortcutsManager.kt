package com.sugarmunch.app.ai

import android.content.Context
import com.sugarmunch.app.data.AppEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Restored legacy shortcut manager entrypoint.
 */
class SmartShortcutsManager private constructor(
    private val context: Context
) {
    private val recommendationEngine = RecommendationEngine.getInstance(context)

    suspend fun getRecommendedApps(limit: Int = 3): List<AppEntry> = withContext(Dispatchers.IO) {
        recommendationEngine.getRecommendations(limit).map { it.app }
    }

    suspend fun createRecommendationsSlice(): AppSlice = withContext(Dispatchers.IO) {
        AppSlice(
            title = "Recommended",
            apps = getRecommendedApps()
        )
    }

    companion object {
        @Volatile
        private var instance: SmartShortcutsManager? = null

        fun getInstance(context: Context): SmartShortcutsManager {
            return instance ?: synchronized(this) {
                instance ?: SmartShortcutsManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

data class AppSlice(
    val title: String,
    val apps: List<AppEntry>
)
