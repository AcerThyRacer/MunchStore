@file:UnstableApi

package com.sugarmunch.sugarbeats.playback.player

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import com.sugarmunch.sugarbeats.activities.MainActivity
import com.sugarmunch.sugarbeats.extensions.broadcastUpdateWidgetState
import com.sugarmunch.sugarbeats.extensions.config
import com.sugarmunch.sugarbeats.extensions.currentMediaItems
import com.sugarmunch.sugarbeats.extensions.setRepeatMode
import com.sugarmunch.sugarbeats.helpers.EXTRA_OPEN_PLAYER
import com.sugarmunch.sugarbeats.helpers.SEEK_INTERVAL_MS
import com.sugarmunch.sugarbeats.playback.PlaybackService
import com.sugarmunch.sugarbeats.playback.PlaybackService.Companion.updatePlaybackInfo
import com.sugarmunch.sugarbeats.playback.SimpleEqualizer
import com.sugarmunch.sugarbeats.playback.getCustomLayout
import com.sugarmunch.sugarbeats.playback.getMediaSessionCallback

/**
 * Initializes player and media session.
 */
internal fun PlaybackService.initializeSessionAndPlayer(
    handleAudioFocus: Boolean,
    handleAudioBecomingNoisy: Boolean
) {
    player = initializePlayer(handleAudioFocus, handleAudioBecomingNoisy)
    playerListener = getPlayerListener()
    mediaSession =
        MediaLibraryService.MediaLibrarySession.Builder(this, player, getMediaSessionCallback())
            .setSessionActivity(getSessionActivityIntent())
            .build()

    withPlayer {
        addListener(playerListener)
        setRepeatMode(config.playbackSetting)
        setPlaybackSpeed(config.playbackSpeed)
        shuffleModeEnabled = config.isShuffleEnabled
        mediaSession.setCustomLayout(getCustomLayout())
        SimpleEqualizer.setupEqualizer(this@initializeSessionAndPlayer, player)
    }
}

private fun PlaybackService.initializePlayer(
    handleAudioFocus: Boolean,
    handleAudioBecomingNoisy: Boolean
): SimpleMusicPlayer {
    val renderersFactory = AudioOnlyRenderersFactory(context = this)
    return SimpleMusicPlayer(
        ExoPlayer.Builder(this, renderersFactory)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setHandleAudioBecomingNoisy(handleAudioBecomingNoisy)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                handleAudioFocus
            )
            .setSeekBackIncrementMs(SEEK_INTERVAL_MS)
            .setSeekForwardIncrementMs(SEEK_INTERVAL_MS)
            .build()
    )
}

private fun Context.getSessionActivityIntent(): PendingIntent {
    val intent = Intent(this, MainActivity::class.java).apply {
        putExtra(EXTRA_OPEN_PLAYER, true)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }

    return PendingIntent.getActivity(
        this,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
}

internal fun PlaybackService.updatePlaybackState() {
    withPlayer {
        updatePlaybackInfo(player)
        broadcastUpdateWidgetState()
        val currentMediaItem = currentMediaItem
        if (currentMediaItem != null) {
            mediaItemProvider.saveRecentItemsWithStartPosition(
                mediaItems = currentMediaItems,
                current = currentMediaItem,
                startPosition = currentPosition
            )
        }
    }
}
