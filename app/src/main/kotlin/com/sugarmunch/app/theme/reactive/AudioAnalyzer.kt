package com.sugarmunch.app.theme.reactive

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.sugarmunch.app.util.SecureLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.abs

/**
 * Audio Analyzer for Music-Reactive Themes
 * 
 * Features:
 * - Real-time audio frequency analysis
 * - Beat detection
 * - Volume/amplitude tracking
 * - Frequency band separation (bass, mid, treble)
 */
class AudioAnalyzer(private val context: Context) {
    private val logger = SecureLogger.create("AudioAnalyzer")
    private val scope = CoroutineScope(Dispatchers.IO)
    
    // Audio recording parameters
    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    
    // Audio state flows
    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()
    
    private val _frequencyBands = MutableStateFlow(FrequencyBands(0f, 0f, 0f))
    val frequencyBands: StateFlow<FrequencyBands> = _frequencyBands.asStateFlow()
    
    private val _beatDetected = MutableStateFlow(false)
    val beatDetected: StateFlow<Boolean> = _beatDetected.asStateFlow()
    
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    // Beat detection
    private var recentAmplitudes = ArrayDeque<Float>(100)
    private var beatThreshold = 1.3f
    private var lastBeatTime = 0L
    private val beatCooldown = 150L // ms
    
    /**
     * Start audio analysis
     */
    fun startListening() {
        if (_isListening.value) return
        
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                logger.e("AudioRecord initialization failed")
                return
            }
            
            audioRecord?.startRecording()
            isRecording = true
            _isListening.value = true
            
            scope.launch {
                analyzeAudio()
            }
            
            logger.d("Audio analyzer started")
        } catch (e: SecurityException) {
            logger.e("Microphone permission not granted", e)
        } catch (e: Exception) {
            logger.e("Failed to start audio analyzer", e)
        }
    }
    
    /**
     * Stop audio analysis
     */
    fun stopListening() {
        isRecording = false
        _isListening.value = false
        
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            logger.d("Audio analyzer stopped")
        } catch (e: Exception) {
            logger.e("Error stopping audio analyzer", e)
        }
    }
    
    /**
     * Analyze audio data in real-time
     */
    private suspend fun analyzeAudio() {
        val buffer = ShortArray(bufferSize / 2)
        
        while (isRecording && scope.isActive) {
            val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            
            if (readSize > 0) {
                // Calculate amplitude
                val amplitude = calculateAmplitude(buffer, readSize)
                _amplitude.value = amplitude
                
                // Calculate frequency bands
                val bands = calculateFrequencyBands(buffer, readSize)
                _frequencyBands.value = bands
                
                // Detect beats
                if (detectBeat(amplitude)) {
                    _beatDetected.value = true
                    delay(100)
                    _beatDetected.value = false
                }
            }
            
            delay(16) // ~60fps
        }
    }
    
    /**
     * Calculate overall amplitude from audio buffer
     */
    private fun calculateAmplitude(buffer: ShortArray, size: Int): Float {
        var sum = 0.0
        for (i in 0 until size) {
            sum += abs(buffer[i].toDouble())
        }
        return (sum / size).toFloat() / Short.MAX_VALUE
    }
    
    /**
     * Calculate frequency bands (simplified FFT approximation)
     */
    private fun calculateFrequencyBands(buffer: ShortArray, size: Int): FrequencyBands {
        // Simplified frequency band calculation
        // In production, use proper FFT
        
        val third = size / 3
        var bass = 0.0
        var mid = 0.0
        var treble = 0.0
        
        for (i in 0 until size) {
            val amplitude = abs(buffer[i].toDouble())
            when {
                i < third -> bass += amplitude
                i < 2 * third -> mid += amplitude
                else -> treble += amplitude
            }
        }
        
        return FrequencyBands(
            bass = (bass / third).toFloat() / Short.MAX_VALUE,
            mid = (mid / third).toFloat() / Short.MAX_VALUE,
            treble = (treble / third).toFloat() / Short.MAX_VALUE
        )
    }
    
    /**
     * Detect beat based on amplitude spike
     */
    private fun detectBeat(currentAmplitude: Float): Boolean {
        val now = System.currentTimeMillis()
        
        // Check cooldown
        if (now - lastBeatTime < beatCooldown) return false
        
        // Add to recent amplitudes
        recentAmplitudes.addLast(currentAmplitude)
        if (recentAmplitudes.size > 100) {
            recentAmplitudes.removeFirst()
        }
        
        // Calculate average
        if (recentAmplitudes.size < 10) return false
        
        val average = recentAmplitudes.average().toFloat()
        
        // Detect spike
        if (currentAmplitude > average * beatThreshold && currentAmplitude > 0.1f) {
            lastBeatTime = now
            return true
        }
        
        return false
    }
    
    /**
     * Check if microphone permission is granted
     */
    fun hasPermission(): Boolean {
        return try {
            val status = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
            status == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Frequency bands data
 */
data class FrequencyBands(
    val bass: Float,    // Low frequencies (20-250 Hz)
    val mid: Float,     // Mid frequencies (250-4000 Hz)
    val treble: Float   // High frequencies (4000-20000 Hz)
) {
    /**
     * Get dominant band
     */
    fun getDominantBand(): FrequencyBand {
        return when {
            bass >= mid && bass >= treble -> FrequencyBand.BASS
            mid >= bass && mid >= treble -> FrequencyBand.MID
            else -> FrequencyBand.TREBLE
        }
    }
}

enum class FrequencyBand { BASS, MID, TREBLE }

/**
 * Beat Detector for music visualization
 */
class BeatDetector {
    private var energyHistory = ArrayDeque<Float>(43)
    private var beatCount = 0
    private var lastBeatTime = 0L
    
    data class BeatDetectionResult(
        val isBeat: Boolean,
        val confidence: Float,
        val bpm: Float
    )
    
    /**
     * Process audio frame and detect beats
     */
    fun processFrame(amplitude: Float): BeatDetectionResult {
        val now = System.currentTimeMillis()
        
        // Add to history
        energyHistory.addLast(amplitude)
        if (energyHistory.size > 43) {
            energyHistory.removeFirst()
        }
        
        // Need enough history
        if (energyHistory.size < 43) {
            return BeatDetectionResult(false, 0f, 0f)
        }
        
        // Calculate local energy average
        val localAverage = energyHistory.takeLast(10).average().toFloat()
        val globalAverage = energyHistory.average().toFloat()
        
        // Beat detection threshold
        val threshold = globalAverage * 1.5f
        
        // Detect beat
        val isBeat = amplitude > threshold && amplitude > localAverage * 1.3f
        
        if (isBeat && now - lastBeatTime > 150) {
            beatCount++
            lastBeatTime = now
        }
        
        // Calculate BPM (beats per minute)
        val bpm = if (beatCount > 1) {
            beatCount.toFloat() / ((now - lastBeatTime) / 60000f)
        } else 0f
        
        val confidence = if (isBeat) {
            (amplitude / threshold).coerceIn(1f, 3f)
        } else 0f
        
        return BeatDetectionResult(isBeat, confidence, bpm)
    }
    
    /**
     * Reset detector state
     */
    fun reset() {
        energyHistory.clear()
        beatCount = 0
        lastBeatTime = 0
    }
}
