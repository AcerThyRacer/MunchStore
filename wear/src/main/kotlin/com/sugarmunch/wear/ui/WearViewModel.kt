package com.sugarmunch.wear.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Wear OS ViewModel
 */
@HiltViewModel
class WearViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(WearUiState())
    val uiState: StateFlow<WearUiState> = _uiState.asStateFlow()

    init {
        loadStats()
        checkDailyReward()
    }

    fun loadStats() {
        viewModelScope.launch {
            // Load stats from data layer
            _uiState.value = _uiState.value.copy(
                stats = WearStats(
                    installs = 47,
                    effects = 23,
                    points = 2450
                )
            )
        }
    }

    fun checkDailyReward() {
        viewModelScope.launch {
            // Check if reward can be claimed
            _uiState.value = _uiState.value.copy(
                canClaimReward = true,
                rewardAmount = 150
            )
        }
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            // Claim reward
            _uiState.value = _uiState.value.copy(
                canClaimReward = false,
                stats = _uiState.value.stats.copy(
                    points = _uiState.value.stats.points + _uiState.value.rewardAmount
                )
            )
        }
    }

    fun toggleActiveEffect() {
        viewModelScope.launch {
            // Toggle effect
        }
    }
}

data class WearUiState(
    val stats: WearStats = WearStats(),
    val canClaimReward: Boolean = false,
    val rewardAmount: Int = 0,
    val activeEffect: String = ""
)

data class WearStats(
    val installs: Int = 0,
    val effects: Int = 0,
    val points: Int = 0
)
