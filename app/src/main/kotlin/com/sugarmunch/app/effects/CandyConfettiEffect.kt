package com.sugarmunch.app.effects

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import kotlin.random.Random

object CandyConfettiEffect : Effect {
    override val id = "candy_confetti"
    override val name = "Candy Confetti"
    override val hasVisual = true
    override val hasSound = false
    override val hasHaptic = false

    private var overlayView: View? = null
    private var windowManager: WindowManager? = null

    private val colors = intArrayOf(
        0xFFFFB6C1.toInt(), 0xFF98FF98.toInt(), 0xFFFFFACD.toInt(),
        0xFFDEB887.toInt(), 0xFFB5DEFF.toInt(), 0xFFE6B3FF.toInt()
    )

    override fun enable(context: Context, windowManager: WindowManager) {
        this.windowManager = windowManager
        val view = object : View(context) {
            private var dots: List<Dot> = emptyList()
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
                super.onSizeChanged(w, h, oldw, oldh)
                if (w > 0 && h > 0) {
                    dots = (1..40).map {
                        Dot(
                            x = Random.nextFloat() * w,
                            y = Random.nextFloat() * h,
                            color = colors[it % colors.size],
                            radius = 2f + Random.nextFloat() * 4f
                        )
                    }
                }
            }
            override fun onDraw(canvas: Canvas) {
                if (width == 0 || height == 0) return
                dots.forEach { dot ->
                    paint.color = dot.color
                    paint.alpha = 120
                    canvas.drawCircle(dot.x, dot.y, dot.radius, paint)
                }
                postInvalidateDelayed(200)
            }
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

    private data class Dot(val x: Float, val y: Float, val color: Int, val radius: Float)
}
