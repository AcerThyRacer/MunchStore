package com.sugarmunch.app.ai

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class MoodDetector @Inject constructor(
    private val context: Context
) {
    private val _currentMood = MutableStateFlow(MoodState.Neutral)
    val currentMood: StateFlow<MoodState> = _currentMood

    suspend fun detectMood(): MoodState = _currentMood.value

    fun setMood(mood: MoodState) {
        _currentMood.value = mood
    }

    fun getSuggestedThemeId(): String? {
        return when (_currentMood.value) {
            MoodState.Happy -> "chill_mint"
            MoodState.Focused -> "focus_minimal"
            MoodState.Energetic -> "sugarrush_classic"
            MoodState.Calm -> "dark_cocoa"
            MoodState.Neutral -> null
        }
    }
}

enum class MoodState {
    Happy,
    Focused,
    Energetic,
    Calm,
    Neutral
}
