package com.sugarmunch.app.effects

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.graphics.Color

object MintWashEffect : Effect {
    override val id = "mint_wash"
    override val name = "Mint Wash"
    override val hasVisual = true
    override val hasSound = false
    override val hasHaptic = false

    private var overlayView: View? = null
    private var windowManager: WindowManager? = null

    override fun enable(context: Context, windowManager: WindowManager) {
        this.windowManager = windowManager
        val view = View(context).apply {
            setBackgroundColor(Color.argb(60, 0x98, 0xFF, 0x98))
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.graphics.PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        windowManager.addView(view, params)
        overlayView = view
    }

    override fun disable() {
        overlayView?.let { windowManager?.removeView(it) }
        overlayView = null
        windowManager = null
    }

    override fun isActive(): Boolean = overlayView != null
}
