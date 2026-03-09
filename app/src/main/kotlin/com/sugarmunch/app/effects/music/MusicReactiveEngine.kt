package com.sugarmunch.app.effects.music

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Phase 2: Music-Reactive Engine
 * 
 * Real-time audio analysis for driving visual effects.
 * Supports microphone input and system audio (where available).
 */

// ─────────────────────────────────────────────────────────────────────────────────
// AUDIO ANALYSIS DATA
// ─────────────────────────────────────────────────────────────────────────────────

/**
 * Comprehensive audio analysis results
 */
data class AudioAnalysisResult(
    // Overall amplitude (0-1)
    val amplitude: Float = 0f,
    
    // Frequency bands (0-1 each)
    val bass: Float = 0f,        // 20-250 Hz
    val lowMid: Float = 0f,      // 250-500 Hz
    val mid: Float = 0f,         // 500-2000 Hz
    val highMid: Float = 0f,     // 2000-4000 Hz
    val treble: Float = 0f,      // 4000-20000 Hz
    
    // Detailed frequency spectrum (32 bands)
    val frequencyBands: FloatArray = FloatArray(32),
    
    // Beat detection
    val beatDetected: Boolean = false,
    val beatStrength: Float = 0f,
    
    // BPM estimation
    val bpm: Float = 0f,
    val bpmConfidence: Float = 0f,
    
    // Energy levels
    val energy: Float = 0f,
    val energyHistory: FloatArray = FloatArray(43),
    
    // Spectral features
    val spectralCentroid: Float = 0f,
    val spectralFlatness: Float = 0f,
    val spectralRolloff: Float = 0f,
    
    // Derived values for effects
    val intensity: Float = 0f,
    val warmth: Float = 0f,
    val brightness: Float = 0f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioAnalysisResult) return false
        return frequencyBands.contentEquals(other.frequencyBands) &&
               energyHistory.contentEquals(other.energyHistory)
    }
    
    override fun hashCode(): Int {
        var result = frequencyBands.contentHashCode()
        result = 31 * result + energyHistory.contentHashCode()
        return result
    }
}

/**
 * Configuration for audio analysis
 */
data class AudioAnalysisConfig(
    val sampleRate: Int = 44100,
    val fftSize: Int = 2048,
    val hopSize: Int = 512,
    val smoothingFactor: Float = 0.3f,
    val beatThreshold: Float = 1.4f,
    val beatTimeoutMs: Long = 100,
    val historySize: Int = 43,
    val useAWeighting: Boolean = true
)

// ─────────────────────────────────────────────────────────────────────────────────
// FFT IMPLEMENTATION
// ─────────────────────────────────────────────────────────────────────────────────

/**
 * Fast Fourier Transform implementation
 */
class FFT(private val size: Int) {
    private val cos = FloatArray(size)
    private val sin = FloatArray(size)
    
    init {
        for (i in 0 until size) {
            cos[i] = kotlin.math.cos(2.0 * kotlin.math.PI * i / size).toFloat()
            sin[i] = kotlin.math.sin(2.0 * kotlin.math.PI * i / size).toFloat()
        }
    }
    
    /**
     * Perform FFT on real and imaginary arrays
     */
    fun fft(real: FloatArray, imag: FloatArray) {
        var n = size
        var m = 0
        while (n > 1) {
            n = n shr 1
            m++
        }
        
        // Bit reversal
        var j = 0
        for (i in 0 until size - 1) {
            if (i < j) {
                val tr = real[j]
                val ti = imag[j]
                real[j] = real[i]
                imag[j] = imag[i]
                real[i] = tr
                imag[i] = ti
            }
            var k = size shr 1
            while (k <= j) {
                j -= k
                k = k shr 1
            }
            j += k
        }
        
        // FFT
        var length = 2
        while (length <= size) {
            val step = size / length
            for (i in 0 until size step length) {
                for (k in 0 until length / 2) {
                    val idx1 = i + k
                    val idx2 = i + k + length / 2
                    val cosIdx = k * step
                    val sinIdx = k * step
                    
                    val tr = real[idx2] * cos[cosIdx] - imag[idx2] * sin[sinIdx]
                    val ti = real[idx2] * sin[sinIdx] + imag[idx2] * cos[cosIdx]
                    
                    real[idx2] = real[idx1] - tr
                    imag[idx2] = imag[idx1] - ti
                    real[idx1] += tr
                    imag[idx1] += ti
                }
            }
            length = length shl 1
        }
    }
    
    /**
     * Get magnitude spectrum from FFT result
     */
    fun getMagnitude(real: FloatArray, imag: FloatArray, output: FloatArray) {
        val outputSize = output.size
        for (i in 0 until outputSize) {
            output[i] = sqrt(real[i] * real[i] + imag[i] * imag[i]) / size
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// BEAT DETECTOR
// ─────────────────────────────────────────────────────────────────────────────────

/**
 * Beat detection algorithm
 */
class BeatDetector(
    private val threshold: Float = 1.4f,
    private val timeoutMs: Long = 100
) {
    private val energyHistory = FloatArray(43)
    private var historyIndex = 0
    private var lastBeatTime = 0L
    private var beatCount = 0
    private var beatTimes = mutableListOf<Long>()
    
    /**
     * Detect beat from energy value
     */
    fun detect(energy: Float, currentTime: Long): Pair<Boolean, Float> {
        // Add to history
        energyHistory[historyIndex] = energy
        historyIndex = (historyIndex + 1) % energyHistory.size
        
        // Calculate local average
        val avgEnergy = energyHistory.average().toFloat()
        
        // Beat detection
        val isBeat = energy > avgEnergy * threshold && 
                     currentTime - lastBeatTime > timeoutMs
        
        if (isBeat) {
            lastBeatTime = currentTime
            beatCount++
            beatTimes.add(currentTime)
            
            // Keep only recent beat times for BPM calculation
            val cutoffTime = currentTime - 10000 // 10 seconds
            beatTimes.removeAll { it < cutoffTime }
        }
        
        val strength = if (avgEnergy > 0) (energy / avgEnergy - 1f).coerceIn(0f, 2f) else 0f
        
        return Pair(isBeat, strength)
    }
    
    /**
     * Estimate BPM from beat history
     */
    fun estimateBPM(): Float {
        if (beatTimes.size < 2) return 0f
        
        val intervals = mutableListOf<Long>()
        for (i in 1 until beatTimes.size) {
            intervals.add(beatTimes[i] - beatTimes[i - 1])
        }
        
        if (intervals.isEmpty()) return 0f
        
        val avgInterval = intervals.average()
        return if (avgInterval > 0) (60000f / avgInterval).toFloat() else 0f
    }
    
    fun reset() {
        energyHistory.fill(0f)
        historyIndex = 0
        lastBeatTime = 0L
        beatCount = 0
        beatTimes.clear()
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// AUDIO ANALYZER
// ─────────────────────────────────────────────────────────────────────────────────

/**
 * Main audio analysis engine
 */
class AudioAnalyzer(
    private val context: Context,
    private val config: AudioAnalysisConfig = AudioAnalysisConfig(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _analysisResult = MutableStateFlow(AudioAnalysisResult())
    val analysisResult: StateFlow<AudioAnalysisResult> = _analysisResult.asStateFlow()
    
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    private var audioRecord: AudioRecord? = null
    private var analysisJob: Job? = null
    
    private val fft = FFT(config.fftSize)
    private val beatDetector = BeatDetector(config.beatThreshold, config.beatTimeoutMs)
    
    private val real = FloatArray(config.fftSize)
    private val imag = FloatArray(config.fftSize)
    private val magnitude = FloatArray(config.fftSize / 2)
    
    // Smoothing buffers
    private val smoothedBands = FloatArray(32)
    private var smoothedAmplitude = 0f
    private var smoothedBass = 0f
    private var smoothedMid = 0f
    private var smoothedTreble = 0f
    
    /**
     * Start audio capture and analysis
     */
    fun start(): Boolean {
        if (_isRunning.value) return true
        
        // Check permission
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w("AudioAnalyzer", "RECORD_AUDIO permission not granted")
            return false
        }
        
        val bufferSize = AudioRecord.getMinBufferSize(
            config.sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(config.fftSize * 2)
        
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                config.sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("AudioAnalyzer", "AudioRecord not initialized")
                return false
            }
            
            audioRecord?.startRecording()
            _isRunning.value = true
            
            analysisJob = scope.launch {
                val buffer = ShortArray(config.fftSize)
                
                while (isActive) {
                    val read = audioRecord?.read(buffer, 0, config.fftSize) ?: 0
                    
                    if (read >= config.fftSize) {
                        analyzeBuffer(buffer)
                    }
                    
                    delay(10) // Small delay to prevent tight loop
                }
            }
            
            return true
        } catch (e: SecurityException) {
            Log.e("AudioAnalyzer", "Security exception", e)
            return false
        } catch (e: Exception) {
            Log.e("AudioAnalyzer", "Failed to start", e)
            return false
        }
    }
    
    /**
     * Stop audio capture
     */
    fun stop() {
        _isRunning.value = false
        analysisJob?.cancel()
        analysisJob = null
        
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        
        beatDetector.reset()
    }
    
    /**
     * Analyze audio buffer
     */
    private fun analyzeBuffer(buffer: ShortArray) {
        // Convert to float and apply window
        for (i in buffer.indices) {
            real[i] = buffer[i].toFloat() / Short.MAX_VALUE
            imag[i] = 0f
            
            // Apply Hann window
            val window = 0.5f * (1 - kotlin.math.cos(2 * kotlin.math.PI * i / (buffer.size - 1)))
            real[i] *= window.toFloat()
        }
        
        // Perform FFT
        fft.fft(real, imag)
        fft.getMagnitude(real, imag, magnitude)
        
        // Calculate frequency bands
        val bands = calculateFrequencyBands(magnitude)
        
        // Calculate overall amplitude
        val amplitude = calculateAmplitude(magnitude)
        
        // Calculate energy
        val energy = calculateEnergy(magnitude)
        
        // Beat detection
        val currentTime = System.currentTimeMillis()
        val (beatDetected, beatStrength) = beatDetector.detect(energy, currentTime)
        val bpm = beatDetector.estimateBPM()
        
        // Calculate spectral features
        val spectralCentroid = calculateSpectralCentroid(magnitude)
        val spectralFlatness = calculateSpectralFlatness(magnitude)
        
        // Apply smoothing
        smoothedAmplitude = smooth(smoothedAmplitude, amplitude, config.smoothingFactor)
        smoothedBass = smooth(smoothedBass, bands.bass, config.smoothingFactor)
        smoothedMid = smooth(smoothedMid, bands.mid, config.smoothingFactor)
        smoothedTreble = smooth(smoothedTreble, bands.treble, config.smoothingFactor)
        
        for (i in bands.detailed.indices) {
            smoothedBands[i] = smooth(smoothedBands[i], bands.detailed[i], config.smoothingFactor)
        }
        
        // Calculate derived values
        val intensity = (smoothedAmplitude * 2f).coerceIn(0f, 2f)
        val warmth = smoothedBass.coerceIn(0f, 1f)
        val brightness = smoothedTreble.coerceIn(0f, 1f)
        
        // Update result
        _analysisResult.value = AudioAnalysisResult(
            amplitude = smoothedAmplitude,
            bass = smoothedBass,
            lowMid = bands.lowMid,
            mid = smoothedMid,
            highMid = bands.highMid,
            treble = smoothedTreble,
            frequencyBands = smoothedBands.copyOf(),
            beatDetected = beatDetected,
            beatStrength = beatStrength,
            bpm = bpm,
            bpmConfidence = if (beatDetector.estimateBPM() > 0) 0.8f else 0f,
            energy = energy,
            spectralCentroid = spectralCentroid,
            spectralFlatness = spectralFlatness,
            intensity = intensity,
            warmth = warmth,
            brightness = brightness
        )
    }
    
    /**
     * Calculate frequency bands from magnitude spectrum
     */
    private fun calculateFrequencyBands(magnitude: FloatArray): FrequencyBands {
        val binWidth = config.sampleRate.toFloat() / config.fftSize
        
        // Define frequency ranges
        val bassRange = 20f..250f
        val lowMidRange = 250f..500f
        val midRange = 500f..2000f
        val highMidRange = 2000f..4000f
        val trebleRange = 4000f..20000f
        
        fun bandEnergy(range: ClosedFloatingPointRange<Float>): Float {
            val startBin = (range.start / binWidth).toInt().coerceIn(0, magnitude.size - 1)
            val endBin = (range.endInclusive / binWidth).toInt().coerceIn(0, magnitude.size - 1)
            
            var sum = 0f
            for (i in startBin..endBin) {
                sum += magnitude[i] * magnitude[i]
            }
            return sqrt(sum / (endBin - startBin + 1))
        }
        
        // Calculate 32 detailed bands (logarithmic spacing)
        val detailedBands = FloatArray(32)
        val minFreq = 20f
        val maxFreq = 20000f
        val logMin = log10(minFreq.toDouble())
        val logMax = log10(maxFreq.toDouble())
        
        for (i in 0 until 32) {
            val logFreq = logMin + (logMax - logMin) * i / 31
            val freq = 10.0.pow(logFreq).toFloat()
            val bin = (freq / binWidth).toInt().coerceIn(0, magnitude.size - 1)
            
            // Average a few bins around the target
            val binStart = (bin - 2).coerceIn(0, magnitude.size - 1)
            val binEnd = (bin + 2).coerceIn(0, magnitude.size - 1)
            var sum = 0f
            for (b in binStart..binEnd) {
                sum += magnitude[b]
            }
            detailedBands[i] = sum / (binEnd - binStart + 1)
        }
        
        return FrequencyBands(
            bass = bandEnergy(bassRange),
            lowMid = bandEnergy(lowMidRange),
            mid = bandEnergy(midRange),
            highMid = bandEnergy(highMidRange),
            treble = bandEnergy(trebleRange),
            detailed = detailedBands
        )
    }
    
    private data class FrequencyBands(
        val bass: Float,
        val lowMid: Float,
        val mid: Float,
        val highMid: Float,
        val treble: Float,
        val detailed: FloatArray
    )
    
    /**
     * Calculate overall amplitude
     */
    private fun calculateAmplitude(magnitude: FloatArray): Float {
        var sum = 0f
        for (i in 1 until magnitude.size / 2) { // Skip DC component
            sum += magnitude[i]
        }
        return (sum / (magnitude.size / 2 - 1)).coerceIn(0f, 1f)
    }
    
    /**
     * Calculate energy
     */
    private fun calculateEnergy(magnitude: FloatArray): Float {
        var sum = 0f
        for (i in 1 until magnitude.size / 2) {
            sum += magnitude[i] * magnitude[i]
        }
        return sqrt(sum)
    }
    
    /**
     * Calculate spectral centroid (brightness measure)
     */
    private fun calculateSpectralCentroid(magnitude: FloatArray): Float {
        var weightedSum = 0f
        var sum = 0f
        val binWidth = config.sampleRate.toFloat() / config.fftSize
        
        for (i in 1 until magnitude.size / 2) {
            val freq = i * binWidth
            weightedSum += freq * magnitude[i]
            sum += magnitude[i]
        }
        
        return if (sum > 0) weightedSum / sum else 0f
    }
    
    /**
     * Calculate spectral flatness (tonal vs noisy)
     */
    private fun calculateSpectralFlatness(magnitude: FloatArray): Float {
        var geometricMean = 1.0
        var arithmeticMean = 0.0
        val n = magnitude.size / 2 - 1
        
        for (i in 1 until magnitude.size / 2) {
            val mag = max(magnitude[i].toDouble(), 1e-10)
            geometricMean *= mag.pow(1.0 / n)
            arithmeticMean += mag
        }
        arithmeticMean /= n
        
        return if (arithmeticMean > 0) (geometricMean / arithmeticMean).toFloat() else 0f
    }
    
    /**
     * Apply exponential smoothing
     */
    private fun smooth(current: Float, target: Float, factor: Float): Float {
        return current * factor + target * (1 - factor)
    }
    
    companion object {
        private var instance: AudioAnalyzer? = null
        
        fun getInstance(context: Context, scope: CoroutineScope): AudioAnalyzer {
            return instance ?: synchronized(this) {
                instance ?: AudioAnalyzer(context.applicationContext, scope = scope).also { instance = it }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// MUSIC-REACTIVE THEME CONTROLLER
// ─────────────────────────────────────────────────────────────────────────────────

/**
 * Controls theme parameters based on audio analysis
 */
class MusicReactiveThemeController(
    private val analyzer: AudioAnalyzer
) {
    private val _reactiveState = MutableStateFlow(ReactiveThemeState())
    val reactiveState: StateFlow<ReactiveThemeState> = _reactiveState.asStateFlow()
    
    /**
     * Configuration for reactive behavior
     */
    var config = ReactiveConfig()
        set(value) {
            field = value
            updateState()
        }
    
    data class ReactiveConfig(
        val hueFollowsBass: Boolean = true,
        val saturationFollowsEnergy: Boolean = true,
        val brightnessFollowsTreble: Boolean = true,
        val intensityFollowsAmplitude: Boolean = true,
        val particleIntensityFollowsBeat: Boolean = true,
        val hueSpeed: Float = 60f,         // Degrees per second
        val saturationMin: Float = 0.5f,
        val saturationMax: Float = 1f,
        val brightnessMin: Float = 0.3f,
        val brightnessMax: Float = 1f,
        val intensityMin: Float = 0.5f,
        val intensityMax: Float = 2f,
        val beatFlashIntensity: Float = 0.3f,
        val smoothingFactor: Float = 0.15f
    )
    
    data class ReactiveThemeState(
        val hue: Float = 0f,
        val saturation: Float = 0.7f,
        val brightness: Float = 0.8f,
        val intensity: Float = 1f,
        val particleIntensity: Float = 1f,
        val backgroundColor: Color = Color.Black,
        val accentColor: Color = Color.White,
        val beatFlash: Float = 0f,
        val energy: Float = 0f
    )
    
    private var lastHue = 0f
    private var lastBeatFlash = 0f
    
    /**
     * Update reactive state based on current audio analysis
     */
    fun update() {
        updateState()
    }
    
    private fun updateState() {
        val audio = analyzer.analysisResult.value
        val cfg = config
        
        // Calculate hue (continuous rotation based on time and bass)
        val hueOffset = if (cfg.hueFollowsBass) audio.bass * 30f else 0f
        val targetHue = (System.currentTimeMillis() % 60000) / 60000f * 360f + hueOffset
        lastHue = smooth(lastHue, targetHue, cfg.smoothingFactor)
        
        // Calculate saturation
        val targetSat = if (cfg.saturationFollowsEnergy) {
            cfg.saturationMin + audio.energy * (cfg.saturationMax - cfg.saturationMin)
        } else {
            (cfg.saturationMin + cfg.saturationMax) / 2
        }
        
        // Calculate brightness
        val targetBright = if (cfg.brightnessFollowsTreble) {
            cfg.brightnessMin + audio.treble * (cfg.brightnessMax - cfg.brightnessMin)
        } else {
            (cfg.brightnessMin + cfg.brightnessMax) / 2
        }
        
        // Calculate intensity
        val targetIntensity = if (cfg.intensityFollowsAmplitude) {
            cfg.intensityMin + audio.amplitude * (cfg.intensityMax - cfg.intensityMin)
        } else {
            1f
        }
        
        // Calculate particle intensity
        val targetParticleIntensity = if (cfg.particleIntensityFollowsBeat) {
            1f + audio.beatStrength * 0.5f
        } else {
            1f
        }
        
        // Beat flash decay
        if (audio.beatDetected) {
            lastBeatFlash = cfg.beatFlashIntensity
        } else {
            lastBeatFlash = max(0f, lastBeatFlash - 0.05f)
        }
        
        // Generate colors
        val accentColor = hsvToColor(lastHue, targetSat, targetBright)
        val bgColor = Color(
            accentColor.red * 0.1f,
            accentColor.green * 0.1f,
            accentColor.blue * 0.1f
        )
        
        _reactiveState.value = ReactiveThemeState(
            hue = lastHue,
            saturation = targetSat,
            brightness = targetBright,
            intensity = targetIntensity,
            particleIntensity = targetParticleIntensity,
            backgroundColor = bgColor,
            accentColor = accentColor,
            beatFlash = lastBeatFlash,
            energy = audio.energy
        )
    }
    
    private fun smooth(current: Float, target: Float, factor: Float): Float {
        return current * factor + target * (1 - factor)
    }
    
    private fun hsvToColor(h: Float, s: Float, v: Float): Color {
        val hNorm = (h % 360f) / 60f
        val i = hNorm.toInt()
        val f = hNorm - i
        val p = v * (1 - s)
        val q = v * (1 - s * f)
        val t = v * (1 - s * (1 - f))
        
        return when (i) {
            0 -> Color(v, t, p)
            1 -> Color(q, v, p)
            2 -> Color(p, v, t)
            3 -> Color(p, q, v)
            4 -> Color(t, p, v)
            else -> Color(v, p, q)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// AUDIO VISUALIZER PRESETS
// ─────────────────────────────────────────────────────────────────────────────────

object AudioVisualizerPresets {
    
    /**
     * Preset configurations for different music styles
     */
    data class VisualizerPreset(
        val name: String,
        val description: String,
        val config: MusicReactiveThemeController.ReactiveConfig,
        val colorScheme: List<Color>
    )
    
    val presets = listOf(
        VisualizerPreset(
            name = "Disco Fever",
            description = "High-energy dance visualization",
            config = MusicReactiveThemeController.ReactiveConfig(
                hueFollowsBass = true,
                saturationFollowsEnergy = true,
                brightnessFollowsTreble = true,
                intensityFollowsAmplitude = true,
                particleIntensityFollowsBeat = true,
                hueSpeed = 120f,
                saturationMin = 0.8f,
                saturationMax = 1f,
                brightnessMin = 0.6f,
                brightnessMax = 1f,
                intensityMin = 0.8f,
                intensityMax = 2f,
                beatFlashIntensity = 0.4f
            ),
            colorScheme = listOf(
                Color(0xFFFF1493), // Deep pink
                Color(0xFF00FFFF), // Cyan
                Color(0xFFFFFF00), // Yellow
                Color(0xFFFF00FF)  // Magenta
            )
        ),
        
        VisualizerPreset(
            name = "Chill Vibes",
            description = "Relaxed ambient visualization",
            config = MusicReactiveThemeController.ReactiveConfig(
                hueFollowsBass = true,
                saturationFollowsEnergy = true,
                brightnessFollowsTreble = false,
                intensityFollowsAmplitude = true,
                particleIntensityFollowsBeat = false,
                hueSpeed = 20f,
                saturationMin = 0.3f,
                saturationMax = 0.6f,
                brightnessMin = 0.4f,
                brightnessMax = 0.7f,
                intensityMin = 0.3f,
                intensityMax = 1f,
                beatFlashIntensity = 0.1f
            ),
            colorScheme = listOf(
                Color(0xFF4169E1), // Royal blue
                Color(0xFF48D1CC), // Medium turquoise
                Color(0xFF9370DB), // Medium purple
                Color(0xFF20B2AA)  // Light sea green
            )
        ),
        
        VisualizerPreset(
            name = "Bass Drop",
            description = "Bass-focused visualization",
            config = MusicReactiveThemeController.ReactiveConfig(
                hueFollowsBass = true,
                saturationFollowsEnergy = true,
                brightnessFollowsTreble = false,
                intensityFollowsAmplitude = true,
                particleIntensityFollowsBeat = true,
                hueSpeed = 40f,
                saturationMin = 0.7f,
                saturationMax = 1f,
                brightnessMin = 0.5f,
                brightnessMax = 1f,
                intensityMin = 0.6f,
                intensityMax = 2f,
                beatFlashIntensity = 0.5f
            ),
            colorScheme = listOf(
                Color(0xFFFF4500), // Orange red
                Color(0xFFFF6347), // Tomato
                Color(0xFFFF0000), // Red
                Color(0xFFFFD700)  // Gold
            )
        ),
        
        VisualizerPreset(
            name = "Neon Dreams",
            description = "Cyberpunk neon aesthetic",
            config = MusicReactiveThemeController.ReactiveConfig(
                hueFollowsBass = true,
                saturationFollowsEnergy = true,
                brightnessFollowsTreble = true,
                intensityFollowsAmplitude = true,
                particleIntensityFollowsBeat = true,
                hueSpeed = 60f,
                saturationMin = 0.9f,
                saturationMax = 1f,
                brightnessMin = 0.7f,
                brightnessMax = 1f,
                intensityMin = 0.7f,
                intensityMax = 2f,
                beatFlashIntensity = 0.3f
            ),
            colorScheme = listOf(
                Color(0xFFFF00FF), // Magenta
                Color(0xFF00FFFF), // Cyan
                Color(0xFF00FF00), // Lime
                Color(0xFFFF0080)  // Rose
            )
        ),
        
        VisualizerPreset(
            name = "Ocean Waves",
            description = "Calm oceanic visualization",
            config = MusicReactiveThemeController.ReactiveConfig(
                hueFollowsBass = true,
                saturationFollowsEnergy = true,
                brightnessFollowsTreble = true,
                intensityFollowsAmplitude = true,
                particleIntensityFollowsBeat = false,
                hueSpeed = 15f,
                saturationMin = 0.4f,
                saturationMax = 0.7f,
                brightnessMin = 0.5f,
                brightnessMax = 0.8f,
                intensityMin = 0.4f,
                intensityMax = 1.2f,
                beatFlashIntensity = 0.15f
            ),
            colorScheme = listOf(
                Color(0xFF006994), // Sea blue
                Color(0xFF40E0D0), // Turquoise
                Color(0xFF5F9EA0), // Cadet blue
                Color(0xFF00CED1)  // Dark turquoise
            )
        )
    )
}