package com.sugarmunch.app.ui.widgetcustom

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.widget.RemoteViews
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.sugarmunch.app.widget.DailyRewardWidget
import com.sugarmunch.app.widget.QuickInstallWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class WidgetStyle(val displayName: String) {
    MATCH_APP("Match App Theme"),
    TRANSPARENT("Transparent"),
    FROSTED_GLASS("Frosted Glass"),
    SOLID_COLOR("Solid Color"),
    GRADIENT("Gradient"),
    OUTLINED("Outlined")
}

enum class WidgetCornerRadius(val radiusDp: Int, val displayName: String) {
    NONE(0, "Square"),
    SMALL(8, "Subtle"),
    MEDIUM(16, "Rounded"),
    LARGE(24, "Very Rounded"),
    PILL(32, "Pill")
}

enum class WidgetSize(val displayName: String) {
    COMPACT("Compact"),
    STANDARD("Standard"),
    EXPANDED("Expanded")
}

data class WidgetAppearance(
    val style: WidgetStyle = WidgetStyle.MATCH_APP,
    val cornerRadius: WidgetCornerRadius = WidgetCornerRadius.MEDIUM,
    val size: WidgetSize = WidgetSize.STANDARD,
    val backgroundColor: Color? = null,
    val gradientColors: List<Color>? = null,
    val borderColor: Color? = null,
    val borderWidth: Float = 1f,
    val opacity: Float = 0.95f,
    val showShadow: Boolean = true,
    val textColor: Color? = null,
    val accentColor: Color? = null
)

/**
 * Engine that manages per-widget appearance settings and applies them to
 * Android RemoteViews. Persists configuration via SharedPreferences.
 */
class WidgetThemeEngine private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val dataStore: SharedPreferences =
        context.getSharedPreferences("widget_theme", Context.MODE_PRIVATE)

    private val _dailyRewardAppearance = MutableStateFlow(WidgetAppearance())
    val dailyRewardAppearance: StateFlow<WidgetAppearance> = _dailyRewardAppearance.asStateFlow()

    private val _quickInstallAppearance = MutableStateFlow(WidgetAppearance())
    val quickInstallAppearance: StateFlow<WidgetAppearance> = _quickInstallAppearance.asStateFlow()

    init {
        load()
    }

    fun updateDailyRewardWidget(appearance: WidgetAppearance) {
        _dailyRewardAppearance.value = appearance
        save()
        refreshDailyRewardWidgets()
    }

    fun updateQuickInstallWidget(appearance: WidgetAppearance) {
        _quickInstallAppearance.value = appearance
        save()
        refreshQuickInstallWidgets()
    }

    fun save() {
        scope.launch {
            dataStore.edit {
                putAppearance(it, PREFIX_DAILY, _dailyRewardAppearance.value)
                putAppearance(it, PREFIX_QUICK, _quickInstallAppearance.value)
            }
        }
    }

    fun load() {
        _dailyRewardAppearance.value = getAppearance(PREFIX_DAILY)
        _quickInstallAppearance.value = getAppearance(PREFIX_QUICK)
    }

    // ── RemoteViews integration ──────────────────────────────────────────

    fun applyToRemoteViews(
        remoteViews: RemoteViews,
        appearance: WidgetAppearance,
        widgetId: Int
    ) {
        val bgColor = resolveBackgroundColor(appearance)
        remoteViews.setInt(widgetId, "setBackgroundColor", bgColor)

        appearance.textColor?.let { color ->
            remoteViews.setInt(widgetId, "setTextColor", color.toArgb())
        }
    }

    // ── Helpers: colour resolution ───────────────────────────────────────

    private fun resolveBackgroundColor(appearance: WidgetAppearance): Int {
        val baseColor = when (appearance.style) {
            WidgetStyle.MATCH_APP -> Color(0xFF1A1A2E)
            WidgetStyle.TRANSPARENT -> Color.Transparent
            WidgetStyle.FROSTED_GLASS -> Color.White.copy(alpha = 0.25f)
            WidgetStyle.SOLID_COLOR -> appearance.backgroundColor ?: Color(0xFF1A1A2E)
            WidgetStyle.GRADIENT -> appearance.gradientColors?.firstOrNull() ?: Color(0xFF1A1A2E)
            WidgetStyle.OUTLINED -> Color.Transparent
        }
        return baseColor.copy(alpha = appearance.opacity.coerceIn(0f, 1f)).toArgb()
    }

    // ── Helpers: widget refresh ──────────────────────────────────────────

    private fun refreshDailyRewardWidgets() {
        scope.launch {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, DailyRewardWidget::class.java)
            manager.getAppWidgetIds(component).forEach { id ->
                DailyRewardWidget.updateAppWidget(context, manager, id)
            }
        }
    }

    private fun refreshQuickInstallWidgets() {
        scope.launch {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, QuickInstallWidget::class.java)
            manager.getAppWidgetIds(component).forEach { id ->
                QuickInstallWidget.updateAppWidget(context, manager, id)
            }
        }
    }

    // ── Persistence helpers ──────────────────────────────────────────────

    private fun putAppearance(
        editor: SharedPreferences.Editor,
        prefix: String,
        a: WidgetAppearance
    ) {
        editor.putString("${prefix}_style", a.style.name)
        editor.putString("${prefix}_corner", a.cornerRadius.name)
        editor.putString("${prefix}_size", a.size.name)
        editor.putFloat("${prefix}_border_width", a.borderWidth)
        editor.putFloat("${prefix}_opacity", a.opacity)
        editor.putBoolean("${prefix}_shadow", a.showShadow)
        a.backgroundColor?.let { editor.putInt("${prefix}_bg_color", it.toArgb()) }
        a.borderColor?.let { editor.putInt("${prefix}_border_color", it.toArgb()) }
        a.textColor?.let { editor.putInt("${prefix}_text_color", it.toArgb()) }
        a.accentColor?.let { editor.putInt("${prefix}_accent_color", it.toArgb()) }
        a.gradientColors?.let { colors ->
            editor.putString(
                "${prefix}_gradient",
                colors.joinToString(",") { it.toArgb().toString() }
            )
        }
    }

    private fun getAppearance(prefix: String): WidgetAppearance {
        val styleName = dataStore.getString("${prefix}_style", null)
        val cornerName = dataStore.getString("${prefix}_corner", null)
        val sizeName = dataStore.getString("${prefix}_size", null)

        return WidgetAppearance(
            style = styleName?.let { runCatching { WidgetStyle.valueOf(it) }.getOrNull() }
                ?: WidgetStyle.MATCH_APP,
            cornerRadius = cornerName?.let {
                runCatching { WidgetCornerRadius.valueOf(it) }.getOrNull()
            } ?: WidgetCornerRadius.MEDIUM,
            size = sizeName?.let { runCatching { WidgetSize.valueOf(it) }.getOrNull() }
                ?: WidgetSize.STANDARD,
            backgroundColor = dataStore.getColorOrNull("${prefix}_bg_color"),
            gradientColors = dataStore.getString("${prefix}_gradient", null)
                ?.split(",")
                ?.mapNotNull { it.toIntOrNull()?.let { argb -> Color(argb) } },
            borderColor = dataStore.getColorOrNull("${prefix}_border_color"),
            borderWidth = dataStore.getFloat("${prefix}_border_width", 1f),
            opacity = dataStore.getFloat("${prefix}_opacity", 0.95f),
            showShadow = dataStore.getBoolean("${prefix}_shadow", true),
            textColor = dataStore.getColorOrNull("${prefix}_text_color"),
            accentColor = dataStore.getColorOrNull("${prefix}_accent_color")
        )
    }

    companion object {
        private const val PREFIX_DAILY = "daily_reward"
        private const val PREFIX_QUICK = "quick_install"

        @Volatile
        private var instance: WidgetThemeEngine? = null

        fun getInstance(context: Context): WidgetThemeEngine {
            return instance ?: synchronized(this) {
                instance ?: WidgetThemeEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}

// ── Extension helpers ────────────────────────────────────────────────────

private fun SharedPreferences.getColorOrNull(key: String): Color? {
    return if (contains(key)) Color(getInt(key, 0)) else null
}

private inline fun SharedPreferences.edit(block: (SharedPreferences.Editor) -> Unit) {
    val editor = edit()
    block(editor)
    editor.apply()
}
