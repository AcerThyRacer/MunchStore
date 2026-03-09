package com.sugarmunch.app.effects

import android.content.Context
import android.view.WindowManager

interface Effect {
    val id: String
    val name: String
    val hasVisual: Boolean
    val hasSound: Boolean
    val hasHaptic: Boolean

    fun enable(context: Context, windowManager: WindowManager)
    fun disable()
    fun isActive(): Boolean
}
