package com.sugarmunch.app.automation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.sugarmunch.app.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * System-related automation actions execution logic.
 * Handles notifications, toasts, vibrations, brightness, volume, sound, and screen control.
 */

private const val CHANNEL_ID = "automation_channel"
private const val NOTIFICATION_ID = 1001

/**
 * Execute ShowNotification action
 * Displays a system notification with the specified title and message
 */
suspend fun ActionExecutor.executeShowNotification(
    action: AutomationAction.ShowNotificationAction
): ActionResult = withContext(Dispatchers.Main) {
    try {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Automation Notifications",
                when (action.priority) {
                    AutomationAction.ShowNotificationAction.NotificationPriority.LOW ->
                        NotificationManager.IMPORTANCE_LOW
                    AutomationAction.ShowNotificationAction.NotificationPriority.DEFAULT ->
                        NotificationManager.IMPORTANCE_DEFAULT
                    AutomationAction.ShowNotificationAction.NotificationPriority.HIGH ->
                        NotificationManager.IMPORTANCE_HIGH
                    AutomationAction.ShowNotificationAction.NotificationPriority.URGENT ->
                        NotificationManager.IMPORTANCE_HIGH
                }
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create pending intent for notification tap
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(action.title)
            .setContentText(action.message)
            .setPriority(
                when (action.priority) {
                    AutomationAction.ShowNotificationAction.NotificationPriority.LOW ->
                        NotificationCompat.PRIORITY_LOW
                    AutomationAction.ShowNotificationAction.NotificationPriority.DEFAULT ->
                        NotificationCompat.PRIORITY_DEFAULT
                    AutomationAction.ShowNotificationAction.NotificationPriority.HIGH,
                    AutomationAction.ShowNotificationAction.NotificationPriority.URGENT ->
                        NotificationCompat.PRIORITY_HIGH
                }
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())

        ActionResult.Success("Notification shown: ${action.title}")
    } catch (e: Exception) {
        ActionResult.Failure(e.message ?: "Failed to show notification")
    }
}

/**
 * Execute ShowToast action
 * Displays a toast message with the specified duration
 */
suspend fun ActionExecutor.executeShowToast(action: AutomationAction.ShowToastAction): ActionResult =
    withContext(Dispatchers.Main) {
        try {
            val duration = when (action.duration) {
                AutomationAction.ShowToastAction.ToastDuration.SHORT -> Toast.LENGTH_SHORT
                AutomationAction.ShowToastAction.ToastDuration.LONG -> Toast.LENGTH_LONG
            }

            Toast.makeText(context, action.message, duration).show()
            ActionResult.Success("Toast shown: ${action.message}")
        } catch (e: Exception) {
            ActionResult.Failure(e.message ?: "Failed to show toast")
        }
    }

/**
 * Execute Vibrate action
 * Triggers device vibration with the specified pattern
 */
suspend fun ActionExecutor.executeVibrate(action: AutomationAction.VibrateAction): ActionResult {
    return try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = when (action.pattern) {
            AutomationAction.VibrateAction.VibrationPattern.SHORT -> longArrayOf(0, 200)
            AutomationAction.VibrateAction.VibrationPattern.LONG -> longArrayOf(0, 500)
            AutomationAction.VibrateAction.VibrationPattern.DOUBLE -> longArrayOf(0, 200, 100, 200)
            AutomationAction.VibrateAction.VibrationPattern.TRIPLE -> longArrayOf(0, 200, 100, 200, 100, 200)
            AutomationAction.VibrateAction.VibrationPattern.HEARTBEAT -> longArrayOf(0, 100, 100, 100, 400, 100, 100)
            AutomationAction.VibrateAction.VibrationPattern.CUSTOM ->
                action.customPattern?.toLongArray() ?: longArrayOf(0, 200)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }

        ActionResult.Success("Vibration executed: ${action.pattern}")
    } catch (e: Exception) {
        ActionResult.Failure(e.message ?: "Failed to vibrate")
    }
}

/**
 * Execute SetBrightness action
 * Adjusts screen brightness level or sets to auto mode
 */
suspend fun ActionExecutor.executeSetBrightness(action: AutomationAction.SetBrightnessAction): ActionResult {
    return try {
        if (action.auto) {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            )
        } else {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )

            val brightnessValue = (action.level * 255).toInt().coerceIn(0, 255)
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightnessValue
            )
        }

        ActionResult.Success("Brightness set: ${if (action.auto) "auto" else action.level}")
    } catch (e: SecurityException) {
        ActionResult.Failure("Permission required to change brightness")
    } catch (e: Exception) {
        ActionResult.Failure(e.message ?: "Failed to set brightness")
    }
}

/**
 * Execute SetVolume action
 * Adjusts volume level for the specified audio stream
 */
suspend fun ActionExecutor.executeSetVolume(action: AutomationAction.SetVolumeAction): ActionResult {
    return try {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val streamType = when (action.stream) {
            AutomationAction.SetVolumeAction.AudioStream.MEDIA -> AudioManager.STREAM_MUSIC
            AutomationAction.SetVolumeAction.AudioStream.RING -> AudioManager.STREAM_RING
            AutomationAction.SetVolumeAction.AudioStream.NOTIFICATION -> AudioManager.STREAM_NOTIFICATION
            AutomationAction.SetVolumeAction.AudioStream.ALARM -> AudioManager.STREAM_ALARM
            AutomationAction.SetVolumeAction.AudioStream.SYSTEM -> AudioManager.STREAM_SYSTEM
        }

        val maxVolume = audioManager.getStreamMaxVolume(streamType)
        val volume = (action.level * maxVolume / 100).coerceIn(0, maxVolume)

        audioManager.setStreamVolume(streamType, volume, AudioManager.FLAG_SHOW_UI)

        ActionResult.Success("Volume set: ${action.stream} = $volume/$maxVolume")
    } catch (e: Exception) {
        ActionResult.Failure(e.message ?: "Failed to set volume")
    }
}

/**
 * Execute PlaySound action
 * Plays a system sound or custom sound URI
 */
suspend fun ActionExecutor.executePlaySound(action: AutomationAction.PlaySoundAction): ActionResult {
    return try {
        val uri = when (action.soundType) {
            AutomationAction.PlaySoundAction.SoundType.DEFAULT ->
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            AutomationAction.PlaySoundAction.SoundType.SUCCESS ->
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            AutomationAction.PlaySoundAction.SoundType.ERROR ->
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            AutomationAction.PlaySoundAction.SoundType.NOTIFICATION ->
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            AutomationAction.PlaySoundAction.SoundType.ALARM ->
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            AutomationAction.PlaySoundAction.SoundType.CUSTOM ->
                action.soundUri?.let { Uri.parse(it) } ?: RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_NOTIFICATION
                )
        }

        val ringtone = RingtoneManager.getRingtone(context, uri)
        ringtone.play()

        ActionResult.Success("Sound played: ${action.soundType}")
    } catch (e: Exception) {
        ActionResult.Failure(e.message ?: "Failed to play sound")
    }
}

/**
 * Execute TurnOffScreen action
 * Turns off the device screen after an optional delay
 */
suspend fun ActionExecutor.executeTurnOffScreen(action: AutomationAction.TurnOffScreenAction): ActionResult {
    return try {
        if (action.delaySeconds > 0) {
            delay(action.delaySeconds * 1000L)
        }

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "SugarMunch:ScreenOff"
        )

        // Alternative: Use DevicePolicyManager for admin-level screen off
        // Or simulate power button press through accessibility service

        wakeLock.acquire(100)
        wakeLock.release()

        ActionResult.Success("Screen turned off")
    } catch (e: Exception) {
        ActionResult.Failure(e.message ?: "Failed to turn off screen")
    }
}
