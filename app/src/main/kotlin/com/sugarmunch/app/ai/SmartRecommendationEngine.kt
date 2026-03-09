package com.sugarmunch.app.ai

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartRecommendationEngine @Inject constructor(
    private val context: Context
) {
    private val engine by lazy { RecommendationEngine.getInstance(context) }

    suspend fun getRecommendations(limit: Int = 10): List<Recommendation> {
        return engine.getRecommendations(limit)
    }

    suspend fun getPersonalizedFeed(): PersonalizedFeed {
        return engine.getPersonalizedFeed()
    }
}
