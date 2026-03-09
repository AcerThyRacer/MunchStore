package com.sugarmunch.app.theme.reactive

import android.content.Context
import android.media.AudioManager
import android.media.audiofx.Visualizer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Music-Reactive Theme Engine
 * Analyzes audio and creates visual effects that react to music
 */
@Singleton
class MusicReactiveTheme @Inject constructor(
    private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    private var visualizer: Visualizer? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val _bassLevel = MutableStateFlow(0f)
    val bassLevel: StateFlow<Float> = _bassLevel.asStateFlow()
    
    private val _midLevel = MutableStateFlow(0f)
    val midLevel: StateFlow<Float> = _midLevel.asStateFlow()
    
    private val _trebleLevel = MutableStateFlow(0f)
    val trebleLevel: StateFlow<Float> = _trebleLevel.asStateFlow()
    
    private val _beatDetected = MutableStateFlow(false)
    val beatDetected: StateFlow<Boolean> = _beatDetected.asStateFlow()
    
    private val _tempo = MutableStateFlow(0f)
    val tempo: StateFlow<Float> = _tempo.asStateFlow()
    
    private var isListening = false
    private var sessionId = 0
    
    // Color animators
    private val primaryColorAnimator = Animatable(Color.Magenta)
    private val secondaryColorAnimator = Animatable(Color.Cyan)
    private val backgroundColorAnimator = Animatable(Color.Black)
    
    companion object {
        private const val FFT_SIZE = 1024
        private const val BASS_THRESHOLD = 0.7f
        private const val BEAT_THRESHOLD = 0.8f
    }
    
    /**
     * Start listening to audio session
     */
    fun startListening(audioSessionId: Int) {
        if (isListening) return
        
        sessionId = audioSessionId
        isListening = true
        
        try {
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = FFT_SIZE
                captureRate = Visualizer.MAX_CAPTURE_RATE / 2
                
                setCaptureListener { _, waveform, fft ->
                    analyzeAudio(fft)
                }
                
                enabled = true
            }
            
            startBeatDetection()
        } catch (e: Exception) {
            // Visualizer not available
        }
    }
    
    /**
     * Stop listening to audio
     */
    fun stopListening() {
        isListening = false
        visualizer?.enabled = false
        visualizer?.release()
        visualizer = null
        scope.cancel()
    }
    
    /**
     * Analyze audio FFT data
     */
    private fun analyzeAudio(fft: ByteArray?) {
        if (fft == null) return
        
        // Calculate frequency bands
        val bass = calculateBand(fft, 0, 3)
        val mid = calculateBand(fft, 4, 10)
        val treble = calculateBand(fft, 11, 20)
        
        _bassLevel.value = bass
        _midLevel.value = mid
        _trebleLevel.value = treble
        
        // Detect beat
        if (bass > BEAT_THRESHOLD) {
            _beatDetected.value = true
            onBeatDetected(bass)
        } else {
            _beatDetected.value = false
        }
    }
    
    /**
     * Calculate average magnitude for a frequency band
     */
    private fun calculateBand(fft: ByteArray, start: Int, end: Int): Float {
        var sum = 0f
        for (i in start..end) {
            val magnitude = kotlin.math.sqrt((fft[i * 2].toInt() and 0xFF).toDouble() * (fft[i * 2].toInt() and 0xFF) +
                    (fft[i * 2 + 1].toInt() and 0xFF).toDouble() * (fft[i * 2 + 1].toInt() and 0xFF))
            sum += magnitude
        }
        return (sum / (end - start + 1) / 256f).coerceIn(0f, 1f)
    }
    
    /**
     * Start beat detection coroutine
     */
    private fun startBeatDetection() {
        scope.launch {
            var lastBeatTime = 0L
            val beatIntervals = mutableListOf<Long>()
            
            while (isListening) {
                if (_beatDetected.value) {
                    val currentTime = System.currentTimeMillis()
                    if (lastBeatTime > 0) {
                        val interval = currentTime - lastBeatTime
                        beatIntervals.add(interval)
                        
                        // Keep last 10 beats
                        if (beatIntervals.size > 10) {
                            beatIntervals.removeAt(0)
                        }
                        
                        // Calculate tempo (BPM)
                        if (beatIntervals.size >= 4) {
                            val avgInterval = beatIntervals.average()
                            _tempo.value = (60000 / avgInterval).toFloat()
                        }
                    }
                    lastBeatTime = currentTime
                }
                delay(50)
            }
        }
    }
    
    /**
     * Handle beat detection - trigger visual effects
     */
    private fun onBeatDetected(intensity: Float) {
        scope.launch {
            // Animate colors on beat
            primaryColorAnimator.animateTo(
                targetValue = Color.Hsv((primaryColorAnimator.value.hue + 30) % 360, 1f, 1f),
                animationSpec = tween(100)
            )
            
            secondaryColorAnimator.animateTo(
                targetValue = Color.Hsv((secondaryColorAnimator.value.hue + 60) % 360, 1f, 1f),
                animationSpec = tween(100)
            )
        }
    }
    
    /**
     * Get color based on bass level
     */
    fun getBassColor(): Color {
        val hue = (_bassLevel.value * 360) % 360
        return Color.Hsv(hue, 0.8f + (_bassLevel.value * 0.2f), 1f)
    }
    
    /**
     * Get particle intensity based on audio
     */
    fun getParticleIntensity(): Float {
        return (_bassLevel.value + _midLevel.value + _trebleLevel.value) / 3f
    }
    
    /**
     * Draw music-reactive visualizer
     */
    fun DrawScope.drawVisualizer(
        fft: ByteArray?,
        width: Float,
        height: Float
    ) {
        if (fft == null) return
        
        val barWidth = width / (FFT_SIZE / 4)
        
        for (i in 0 until FFT_SIZE / 4) {
            val magnitude = kotlin.math.sqrt((fft[i * 2].toInt() and 0xFF).toDouble() * (fft[i * 2].toInt() and 0xFF) +
                    (fft[i * 2 + 1].toInt() and 0xFF).toDouble() * (fft[i * 2 + 1].toInt() and 0xFF)) / 256f
            
            val barHeight = magnitude * height * 0.8f
            val color = Color.Hsv((i / (FFT_SIZE / 4f)) * 360, 1f, 1f)
            
            drawRect(
                color = color,
                topLeft = Offset(i * barWidth, height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth - 2, barHeight)
            )
        }
    }
    
    /**
     * Get reactive background gradient
     */
    fun getReactiveGradient(): List<Color> {
        val bassHue = _bassLevel.value * 360
        val midHue = _midLevel.value * 360
        val trebleHue = _trebleLevel.value * 360
        
        return listOf(
            Color.Hsv(bassHue, 0.7f, 0.3f + (_bassLevel.value * 0.3f)),
            Color.Hsv(midHue, 0.6f, 0.2f + (_midLevel.value * 0.4f)),
            Color.Hsv(trebleHue, 0.5f, 0.1f + (_trebleLevel.value * 0.5f))
        )
    }
}

/**
 * Audio Session Manager - Helps get audio session ID for visualizer
 */
class AudioSessionManager {
    private var audioSessionId: Int = 0
    
    fun setAudioSessionId(sessionId: Int) {
        audioSessionId = sessionId
    }
    
    fun getAudioSessionId(): Int {
        return audioSessionId
    }
}
