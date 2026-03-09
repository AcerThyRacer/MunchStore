package com.sugarmunch.app.ai.sentient

import android.content.Context
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

/**
 * Voice Customizer - Natural language voice commands
 * 
 * Features:
 * - Voice command recognition
 * - Custom wake words
 * - Voice profiles
 * - Text-to-speech feedback
 */
class VoiceCustomizer(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Voice state
    private val _voiceState = MutableStateFlow(VoiceState())
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    // Voice commands
    private val _commands = MutableStateFlow<List<VoiceCommand>>(emptyList())
    val commands: StateFlow<List<VoiceCommand>> = _commands.asStateFlow()

    // Speech recognizer
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    // Voice configuration
    var voiceConfig = VoiceConfig(
        enabled = true,
        wakeWordEnabled = false,
        wakeWord = "Hey SugarMunch",
        language = Locale.getDefault(),
        continuousListening = false,
        hapticFeedback = true,
        audioFeedback = true
    )

    // Voice profiles
    private val voiceProfiles = mutableListOf<VoiceProfile>()

    init {
        initializeSpeechRecognizer()
        loadDefaultCommands()
    }

    private fun initializeSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _voiceState.value = _voiceState.value.copy(
                isAvailable = false,
                error = "Speech recognition not available"
            )
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(createRecognitionListener())

        _voiceState.value = _voiceState.value.copy(isAvailable = true)
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _voiceState.value = _voiceState.value.copy(
                    isListening = true,
                    isProcessing = false
                )
            }

            override fun onBeginningOfSpeech() {
                _voiceState.value = _voiceState.value.copy(isSpeaking = true)
            }

            override fun onRmsChanged(rmsdB: Float) {
                _voiceState.value = _voiceState.value.copy(audioLevel = rmsdB)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _voiceState.value = _voiceState.value.copy(
                    isSpeaking = false,
                    isProcessing = true
                )
            }

            override fun onError(error: Int) {
                _voiceState.value = _voiceState.value.copy(
                    isListening = false,
                    isProcessing = false,
                    error = getErrorMessage(error)
                )
                isListening = false
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    processVoiceCommand(matches.first())
                }
                _voiceState.value = _voiceState.value.copy(
                    isListening = false,
                    isProcessing = false
                )
                isListening = false
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _voiceState.value = _voiceState.value.copy(
                        currentTranscript = matches.first()
                    )
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun getErrorMessage(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
    }

    // ========== VOICE COMMAND PROCESSING ==========

    fun startListening() {
        if (!voiceConfig.enabled || isListening) return

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, voiceConfig.language.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        try {
            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            _voiceState.value = _voiceState.value.copy(error = e.message ?: "Failed to start listening")
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
        _voiceState.value = _voiceState.value.copy(isListening = false)
    }

    fun cancelListening() {
        speechRecognizer?.cancel()
        isListening = false
        _voiceState.value = _voiceState.value.copy(isListening = false)
    }

    private fun processVoiceCommand(transcript: String) {
        val normalizedTranscript = transcript.lowercase().trim()

        // Find matching command
        val matchedCommand = _commands.value.find { command ->
            command.triggers.any { trigger ->
                normalizedTranscript.contains(trigger.lowercase())
            }
        }

        if (matchedCommand != null) {
            executeCommand(matchedCommand, normalizedTranscript)
        } else {
            // Try to parse as natural language
            parseNaturalLanguageCommand(normalizedTranscript)
        }
    }

    private fun executeCommand(command: VoiceCommand, transcript: String) {
        scope.launch {
            _voiceState.value = _voiceState.value.copy(
                lastCommand = command,
                lastTranscript = transcript,
                isExecuting = true
            )

            try {
                command.action(transcript)
                _voiceState.value = _voiceState.value.copy(
                    lastResult = CommandResult(
                        success = true,
                        message = "Command executed: ${command.name}"
                    )
                )
            } catch (e: Exception) {
                _voiceState.value = _voiceState.value.copy(
                    lastResult = CommandResult(
                        success = false,
                        message = e.message ?: "Command failed"
                    )
                )
            } finally {
                _voiceState.value = _voiceState.value.copy(isExecuting = false)
            }
        }
    }

    private fun parseNaturalLanguageCommand(transcript: String) {
        // Parse common natural language patterns
        val result = when {
            transcript.startsWith("open ") -> {
                val appName = transcript.removePrefix("open ").trim()
                CommandResult(
                    success = true,
                    message = "Opening $appName",
                    action = "open_app",
                    parameters = mapOf("app" to appName)
                )
            }
            transcript.startsWith("search for ") -> {
                val query = transcript.removePrefix("search for ").trim()
                CommandResult(
                    success = true,
                    message = "Searching for $query",
                    action = "search",
                    parameters = mapOf("query" to query)
                )
            }
            transcript.startsWith("set theme to ") -> {
                val themeName = transcript.removePrefix("set theme to ").trim()
                CommandResult(
                    success = true,
                    message = "Setting theme to $themeName",
                    action = "set_theme",
                    parameters = mapOf("theme" to themeName)
                )
            }
            transcript.startsWith("create folder") -> {
                CommandResult(
                    success = true,
                    message = "Creating new folder",
                    action = "create_folder"
                )
            }
            else -> CommandResult(
                success = false,
                message = "I didn't understand that command"
            )
        }

        _voiceState.value = _voiceState.value.copy(lastResult = result)
    }

    // ========== COMMAND MANAGEMENT ==========

    private fun loadDefaultCommands() {
        val defaultCommands = listOf(
            VoiceCommand(
                id = "open_app",
                name = "Open App",
                triggers = listOf("open", "launch", "start"),
                action = { transcript ->
                    // Would open the specified app
                }
            ),
            VoiceCommand(
                id = "search",
                name = "Search",
                triggers = listOf("search for", "find", "look for"),
                action = { transcript ->
                    // Would perform search
                }
            ),
            VoiceCommand(
                id = "go_home",
                name = "Go Home",
                triggers = listOf("go home", "home screen", "main screen"),
                action = { _ ->
                    // Would go to home screen
                }
            ),
            VoiceCommand(
                id = "show_recent",
                name = "Show Recent",
                triggers = listOf("recent apps", "recent", "last apps"),
                action = { _ ->
                    // Would show recent apps
                }
            ),
            VoiceCommand(
                id = "take_screenshot",
                name = "Take Screenshot",
                triggers = listOf("screenshot", "take screenshot", "capture screen"),
                action = { _ ->
                    // Would take screenshot
                }
            )
        )

        _commands.value = defaultCommands
    }

    fun addCommand(command: VoiceCommand) {
        _commands.value = _commands.value + command
    }

    fun removeCommand(id: String) {
        _commands.value = _commands.value.filter { it.id != id }
    }

    // ========== VOICE PROFILES ==========

    fun createVoiceProfile(name: String): VoiceProfile {
        val profile = VoiceProfile(
            id = "voice_${System.currentTimeMillis()}",
            name = name,
            createdAt = System.currentTimeMillis()
        )

        voiceProfiles.add(profile)
        return profile
    }

    fun switchVoiceProfile(profileId: String) {
        val profile = voiceProfiles.find { it.id == profileId }
        if (profile != null) {
            _voiceState.value = _voiceState.value.copy(currentProfile = profile)
        }
    }

    // ========== TEXT TO SPEECH ==========

    fun speak(text: String) {
        if (!voiceConfig.audioFeedback) return

        // Would use Android TextToSpeech
        // TTS.speak(text)
    }

    companion object {
        @Volatile
        private var instance: VoiceCustomizer? = null

        fun getInstance(context: Context): VoiceCustomizer {
            return instance ?: synchronized(this) {
                instance ?: VoiceCustomizer(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * Voice state
 */
data class VoiceState(
    val isAvailable: Boolean = false,
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val isProcessing: Boolean = false,
    val isExecuting: Boolean = false,
    val audioLevel: Float = 0f,
    val currentTranscript: String = "",
    val lastCommand: VoiceCommand? = null,
    val lastTranscript: String = "",
    val lastResult: CommandResult? = null,
    val currentProfile: VoiceProfile? = null,
    val error: String? = null
)

/**
 * Voice command
 */
data class VoiceCommand(
    val id: String,
    val name: String,
    val triggers: List<String>,
    val action: suspend (String) -> Unit
)

/**
 * Command result
 */
data class CommandResult(
    val success: Boolean,
    val message: String,
    val action: String? = null,
    val parameters: Map<String, String> = emptyMap()
)

/**
 * Voice profile
 */
data class VoiceProfile(
    val id: String,
    val name: String,
    val createdAt: Long,
    val voiceSamples: List<String> = emptyList(),
    val preferences: Map<String, Any> = emptyMap()
)

/**
 * Voice configuration
 */
data class VoiceConfig(
    val enabled: Boolean = true,
    val wakeWordEnabled: Boolean = false,
    val wakeWord: String = "Hey SugarMunch",
    val language: Locale = Locale.getDefault(),
    val continuousListening: Boolean = false,
    val hapticFeedback: Boolean = true,
    val audioFeedback: Boolean = true
)
