package com.sugarmunch.app.ai.voice

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class VoiceCommandManager @Inject constructor(
    private val context: Context
) {
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    fun startListening() {
        _isListening.value = true
    }

    fun stopListening() {
        _isListening.value = false
    }

    suspend fun processCommand(command: String): VoiceCommandResult {
        return VoiceCommandResult(
            originalText = command,
            handled = false,
            action = null
        )
    }
}

data class VoiceCommandResult(
    val originalText: String,
    val handled: Boolean,
    val action: String?
)
