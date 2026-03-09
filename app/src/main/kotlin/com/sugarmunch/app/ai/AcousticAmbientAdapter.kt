package com.sugarmunch.app.ai

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import com.sugarmunch.app.theme.engine.ThemeManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AiAdaptersEntryPoint {
    fun acousticAmbientAdapter(): AcousticAmbientAdapter
}

@Singleton
class AcousticAmbientAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themeManager: ThemeManager
) : SensorEventListener {

    private val TAG = "AcousticAmbientAdapter"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    
    // Exposed state for UI components (like mesh gradients) to throb
    private val _bpmThrob = MutableStateFlow(1f)
    val bpmThrob: StateFlow<Float> = _bpmThrob

    fun start() {
        // Start listening to light sensor
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        
        // Start listening to microphone if permission is granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startAudioRecording()
        } else {
            Log.w(TAG, "Microphone permission not granted for acoustic adaptation.")
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        stopAudioRecording()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lightLevel = event.values[0]
            
            // Adjust background intensity based on light level
            val intensity = when {
                lightLevel < 10 -> 0.3f // Very dark -> dim
                lightLevel > 10000 -> 1.5f // Very bright -> intense
                else -> 1.0f // Normal
            }
            themeManager.setBackgroundIntensity(intensity)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun startAudioRecording() {
        if (isRecording) return
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile("/dev/null")
                prepare()
                start()
            }
            isRecording = true
            
            scope.launch {
                var lastAmplitude = 0
                while (isRecording) {
                    val maxAmplitude = mediaRecorder?.maxAmplitude ?: 0
                    val delta = abs(maxAmplitude - lastAmplitude)
                    lastAmplitude = maxAmplitude
                    
                    // Simple BPM/Throb approximation based on sudden amplitude changes
                    if (delta > 5000) {
                        _bpmThrob.value = 1.5f
                    } else {
                        // Decay
                        _bpmThrob.value = (_bpmThrob.value - 0.1f).coerceAtLeast(1f)
                    }
                    delay(100) // Sample rate
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio recording", e)
        }
    }

    private fun stopAudioRecording() {
        if (!isRecording) return
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio recording", e)
        } finally {
            mediaRecorder = null
            isRecording = false
            _bpmThrob.value = 1f
        }
    }
}
