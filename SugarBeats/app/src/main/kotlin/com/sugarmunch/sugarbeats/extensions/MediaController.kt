package com.sugarmunch.sugarbeats.extensions

import android.os.Bundle
import androidx.media3.session.MediaController
import com.sugarmunch.sugarbeats.playback.CustomCommands

fun MediaController.sendCommand(command: CustomCommands, extras: Bundle = Bundle.EMPTY) = sendCustomCommand(command.sessionCommand, extras)
