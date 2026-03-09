package com.sugarmunch.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ScrollView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.sugarmunch.app.effects.Effect
import com.sugarmunch.app.effects.EffectEngine
import com.sugarmunch.app.MainActivity
import com.sugarmunch.app.R

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var touchDownTime = 0L
    private var isDragging = false

    /** Use application context for long-lived prefs to avoid leaking service context. */
    private fun overlayPrefs() = applicationContext.getSharedPreferences(PREFS_OVERLAY, MODE_PRIVATE)
    private fun getFabX() = overlayPrefs().getInt(KEY_FAB_X, 0)
    private fun getFabY() = overlayPrefs().getInt(KEY_FAB_Y, 200)
    private fun saveFabPosition(x: Int, y: Int) = overlayPrefs().edit().putInt(KEY_FAB_X, x).putInt(KEY_FAB_Y, y).apply()

    override fun onCreate() {
        super.onCreate()
        if (!android.provider.Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showOverlay()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.overlay_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply { setShowBadge(false) }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
        val pending = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.overlay_notification_title))
            .setContentText(getString(R.string.overlay_notification_text))
            .setSmallIcon(R.drawable.ic_fab_candy)
            .setContentIntent(pending)
            .setOngoing(true)
            .build()
    }

    private fun showOverlay() {
        val wm = windowManager ?: return
        val inflater = LayoutInflater.from(applicationContext)
        overlayView = inflater.inflate(R.layout.overlay_fab, null)
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = getFabX()
            y = getFabY()
        }
        val fab = overlayView?.findViewById<View>(R.id.fab)
        val panelScroll = overlayView?.findViewById<ScrollView>(R.id.effects_panel_scroll)
        val effectsPanel = overlayView?.findViewById<LinearLayout>(R.id.effects_panel)
        panelScroll?.let { scroll ->
            val metrics = DisplayMetrics()
            wm.defaultDisplay.getRealMetrics(metrics)
            (scroll.layoutParams as? LinearLayout.LayoutParams)?.let { params ->
                params.height = (metrics.heightPixels * 0.5f).toInt()
                scroll.layoutParams = params
            }
        }
        fab?.setOnTouchListener { _, event -> fabTouchListener(panelScroll, event); true }
        effectsPanel?.let { panel ->
            val openAppRow = TextView(this).apply {
                text = getString(R.string.overlay_open_sugarmunch)
                setPadding(16, 12, 16, 12)
                setTextColor(0xFF000000.toInt())
                setOnClickListener {
                    startActivity(Intent(this@OverlayService, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    panelScroll?.visibility = View.GONE
                }
            }
            panel.addView(openAppRow)
            EffectEngine.allEffects().forEach { effect ->
                val label = TextView(this).apply {
                    text = effect.name
                    setPadding(16, 12, 8, 12)
                    setTextColor(0xFF000000.toInt())
                }
                val switch = Switch(this).apply {
                    isChecked = effect.isActive()
                    setOnCheckedChangeListener { _, _ ->
                        EffectEngine.toggle(this@OverlayService, wm, effect)
                    }
                }
                val effectRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    addView(label, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                    addView(switch)
                }
                effectRow.setOnClickListener { switch.isChecked = !switch.isChecked; switch.callOnClick() }
                panel.addView(effectRow)
            }
        }
        wm.addView(overlayView, params)
    }

    private fun fabTouchListener(panelScroll: ScrollView?, event: MotionEvent) {
        val p = params ?: return
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchDownTime = System.currentTimeMillis()
                isDragging = false
                initialX = p.x
                initialY = p.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = (event.rawX - initialTouchX).toInt()
                val dy = (event.rawY - initialTouchY).toInt()
                if (dx * dx + dy * dy > 64) isDragging = true
                if (isDragging) {
                    val wm = windowManager ?: return
                    val metrics = DisplayMetrics()
                    wm.defaultDisplay.getRealMetrics(metrics)
                    val maxX = metrics.widthPixels - 80
                    val maxY = metrics.heightPixels - 80
                    p.x = (initialX + dx).coerceIn(0, maxX)
                    p.y = (initialY + dy).coerceIn(0, maxY)
                    wm.updateViewLayout(overlayView, p)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    saveFabPosition(p.x, p.y)
                } else {
                    val elapsed = System.currentTimeMillis() - touchDownTime
                    if (elapsed >= LONG_PRESS_MS) {
                        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    } else {
                        panelScroll?.visibility = if (panelScroll?.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    }
                }
            }
        }
    }

    private fun removeOverlay() {
        overlayView?.let { windowManager?.removeView(it) }
        overlayView = null
        params = null
    }

    companion object {
        private const val CHANNEL_ID = "sugarmunch_overlay"
        private const val NOTIFICATION_ID = 1001
        private const val PREFS_OVERLAY = "sugarmunch_overlay"
        private const val KEY_FAB_X = "fab_x"
        private const val KEY_FAB_Y = "fab_y"
        private const val LONG_PRESS_MS = 500L
    }
}
