package com.sugarmunch.app.ui.screentheme

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.ui.visual.GradientSpec
import com.sugarmunch.app.ui.visual.GradientStop
import com.sugarmunch.app.ui.visual.GradientType
import com.sugarmunch.app.ui.visual.PatternSpec
import com.sugarmunch.app.ui.visual.PatternType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private val Context.screenThemeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sugarmunch_screen_themes"
)

/**
 * Per-screen color, gradient, and pattern overrides that layer on top of the active theme.
 */
data class ScreenThemeOverride(
    val screenRoute: String,
    val primaryColor: Color? = null,
    val secondaryColor: Color? = null,
    val tertiaryColor: Color? = null,
    val surfaceColor: Color? = null,
    val backgroundColor: Color? = null,
    val gradient: GradientSpec? = null,
    val pattern: PatternSpec? = null,
    val intensity: Float? = null
)

class ScreenThemeManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _overrides = MutableStateFlow<Map<String, ScreenThemeOverride>>(emptyMap())
    val overrides: StateFlow<Map<String, ScreenThemeOverride>> = _overrides.asStateFlow()

    init {
        scope.launch { load() }
    }

    fun setOverride(override: ScreenThemeOverride) {
        _overrides.value = _overrides.value.toMutableMap().apply {
            put(override.screenRoute, override)
        }
        scope.launch { save() }
    }

    fun removeOverride(screenRoute: String) {
        _overrides.value = _overrides.value.toMutableMap().apply { remove(screenRoute) }
        scope.launch { save() }
    }

    fun getOverride(screenRoute: String): ScreenThemeOverride? = _overrides.value[screenRoute]

    fun clearAll() {
        _overrides.value = emptyMap()
        scope.launch { save() }
    }

    // ── Persistence ────────────────────────────────────────────────────

    private val OVERRIDES_KEY = stringPreferencesKey("screen_theme_overrides")

    suspend fun save() {
        val json = JSONArray().apply {
            _overrides.value.values.forEach { put(it.toJson()) }
        }
        context.screenThemeDataStore.edit { prefs ->
            prefs[OVERRIDES_KEY] = json.toString()
        }
    }

    suspend fun load() {
        val prefs = context.screenThemeDataStore.data.first()
        val raw = prefs[OVERRIDES_KEY] ?: return
        try {
            val arr = JSONArray(raw)
            val map = mutableMapOf<String, ScreenThemeOverride>()
            for (i in 0 until arr.length()) {
                val override = arr.getJSONObject(i).toScreenThemeOverride()
                map[override.screenRoute] = override
            }
            _overrides.value = map
        } catch (_: Exception) {
            // Corrupted data – start fresh
        }
    }

    companion object {
        @Volatile
        private var instance: ScreenThemeManager? = null

        fun getInstance(context: Context): ScreenThemeManager {
            return instance ?: synchronized(this) {
                instance ?: ScreenThemeManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

// ── JSON serialization helpers ─────────────────────────────────────────

private fun Color.toHexString(): String {
    val a = (alpha * 255).toInt()
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()
    return String.format("#%02X%02X%02X%02X", a, r, g, b)
}

private fun String.hexToColor(): Color {
    val hex = removePrefix("#")
    return when (hex.length) {
        6 -> Color(android.graphics.Color.parseColor("#$hex"))
        8 -> {
            val a = hex.substring(0, 2).toInt(16)
            val r = hex.substring(2, 4).toInt(16)
            val g = hex.substring(4, 6).toInt(16)
            val b = hex.substring(6, 8).toInt(16)
            Color(r, g, b, a)
        }
        else -> Color.Unspecified
    }
}

private fun ScreenThemeOverride.toJson(): JSONObject = JSONObject().apply {
    put("route", screenRoute)
    primaryColor?.let { put("primary", it.toHexString()) }
    secondaryColor?.let { put("secondary", it.toHexString()) }
    tertiaryColor?.let { put("tertiary", it.toHexString()) }
    surfaceColor?.let { put("surface", it.toHexString()) }
    backgroundColor?.let { put("background", it.toHexString()) }
    gradient?.let { put("gradient", it.toJson()) }
    pattern?.let { put("pattern", it.toJson()) }
    intensity?.let { put("intensity", it.toDouble()) }
}

private fun GradientSpec.toJson(): JSONObject = JSONObject().apply {
    put("type", type.name)
    put("angle", angleDegrees.toDouble())
    put("centerX", centerX.toDouble())
    put("centerY", centerY.toDouble())
    put("radiusScale", radiusScale.toDouble())
    put("stops", JSONArray().apply {
        stops.forEach { stop ->
            put(JSONObject().apply {
                put("color", stop.color.toHexString())
                put("position", stop.position.toDouble())
            })
        }
    })
}

private fun PatternSpec.toJson(): JSONObject = JSONObject().apply {
    put("type", type.name)
    put("primary", primaryColor.toHexString())
    put("secondary", secondaryColor.toHexString())
    put("scale", scale.toDouble())
    put("opacity", opacity.toDouble())
    put("rotation", rotation.toDouble())
}

private fun JSONObject.toScreenThemeOverride(): ScreenThemeOverride = ScreenThemeOverride(
    screenRoute = getString("route"),
    primaryColor = optString("primary", null)?.hexToColor(),
    secondaryColor = optString("secondary", null)?.hexToColor(),
    tertiaryColor = optString("tertiary", null)?.hexToColor(),
    surfaceColor = optString("surface", null)?.hexToColor(),
    backgroundColor = optString("background", null)?.hexToColor(),
    gradient = optJSONObject("gradient")?.toGradientSpec(),
    pattern = optJSONObject("pattern")?.toPatternSpec(),
    intensity = if (has("intensity")) getDouble("intensity").toFloat() else null
)

private fun JSONObject.toGradientSpec(): GradientSpec {
    val stopsArr = getJSONArray("stops")
    val stops = (0 until stopsArr.length()).map { i ->
        val obj = stopsArr.getJSONObject(i)
        GradientStop(
            color = obj.getString("color").hexToColor(),
            position = obj.getDouble("position").toFloat()
        )
    }
    return GradientSpec(
        type = GradientType.valueOf(getString("type")),
        stops = stops,
        angleDegrees = optDouble("angle", 0.0).toFloat(),
        centerX = optDouble("centerX", 0.5).toFloat(),
        centerY = optDouble("centerY", 0.5).toFloat(),
        radiusScale = optDouble("radiusScale", 1.0).toFloat()
    )
}

private fun JSONObject.toPatternSpec(): PatternSpec = PatternSpec(
    type = PatternType.valueOf(getString("type")),
    primaryColor = getString("primary").hexToColor(),
    secondaryColor = optString("secondary", "#00000000").hexToColor(),
    scale = optDouble("scale", 1.0).toFloat(),
    opacity = optDouble("opacity", 0.15).toFloat(),
    rotation = optDouble("rotation", 0.0).toFloat()
)

// ── Compose integration ────────────────────────────────────────────────

val LocalScreenTheme = staticCompositionLocalOf<ScreenThemeOverride?> { null }

@Composable
fun ScreenThemedContent(
    screenRoute: String,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val manager = remember { ScreenThemeManager.getInstance(context) }
    val allOverrides by manager.overrides.collectAsState()
    val override = allOverrides[screenRoute]

    if (override != null) {
        val scheme = MaterialTheme.colorScheme
        val animatedScheme = animateOverrideColors(scheme, override)

        CompositionLocalProvider(LocalScreenTheme provides override) {
            MaterialTheme(colorScheme = animatedScheme) {
                content()
            }
        }
    } else {
        content()
    }
}

@Composable
private fun animateOverrideColors(
    base: ColorScheme,
    override: ScreenThemeOverride
): ColorScheme {
    val spec = tween<Color>(durationMillis = 350)
    val primary by animateColorAsState(override.primaryColor ?: base.primary, spec, label = "primary")
    val secondary by animateColorAsState(override.secondaryColor ?: base.secondary, spec, label = "secondary")
    val tertiary by animateColorAsState(override.tertiaryColor ?: base.tertiary, spec, label = "tertiary")
    val surface by animateColorAsState(override.surfaceColor ?: base.surface, spec, label = "surface")
    val background by animateColorAsState(override.backgroundColor ?: base.background, spec, label = "background")

    return base.copy(
        primary = primary,
        secondary = secondary,
        tertiary = tertiary,
        surface = surface,
        background = background
    )
}

@Composable
fun ScreenThemeTransition(
    fromRoute: String,
    toRoute: String,
    progress: Float,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val manager = remember { ScreenThemeManager.getInstance(context) }
    val allOverrides by manager.overrides.collectAsState()

    val fromOverride = allOverrides[fromRoute]
    val toOverride = allOverrides[toRoute]

    val blended = remember(fromOverride, toOverride, progress) {
        blendOverrides(fromRoute, toRoute, fromOverride, toOverride, progress)
    }

    if (blended != null) {
        val scheme = MaterialTheme.colorScheme
        val animatedScheme = animateOverrideColors(scheme, blended)

        CompositionLocalProvider(LocalScreenTheme provides blended) {
            MaterialTheme(colorScheme = animatedScheme) {
                content()
            }
        }
    } else {
        content()
    }
}

private fun blendOverrides(
    fromRoute: String,
    toRoute: String,
    from: ScreenThemeOverride?,
    to: ScreenThemeOverride?,
    progress: Float
): ScreenThemeOverride? {
    if (from == null && to == null) return null

    val clampedProgress = progress.coerceIn(0f, 1f)
    val targetRoute = if (clampedProgress >= 0.5f) toRoute else fromRoute

    fun blendColor(a: Color?, b: Color?): Color? {
        if (a == null && b == null) return null
        val colorA = a ?: return b
        val colorB = b ?: return a
        return lerp(colorA, colorB, clampedProgress)
    }

    fun blendFloat(a: Float?, b: Float?): Float? {
        if (a == null && b == null) return null
        val valA = a ?: b ?: return null
        val valB = b ?: a ?: return null
        return valA + (valB - valA) * clampedProgress
    }

    return ScreenThemeOverride(
        screenRoute = targetRoute,
        primaryColor = blendColor(from?.primaryColor, to?.primaryColor),
        secondaryColor = blendColor(from?.secondaryColor, to?.secondaryColor),
        tertiaryColor = blendColor(from?.tertiaryColor, to?.tertiaryColor),
        surfaceColor = blendColor(from?.surfaceColor, to?.surfaceColor),
        backgroundColor = blendColor(from?.backgroundColor, to?.backgroundColor),
        gradient = if (clampedProgress < 0.5f) from?.gradient else to?.gradient,
        pattern = if (clampedProgress < 0.5f) from?.pattern else to?.pattern,
        intensity = blendFloat(from?.intensity, to?.intensity)
    )
}
