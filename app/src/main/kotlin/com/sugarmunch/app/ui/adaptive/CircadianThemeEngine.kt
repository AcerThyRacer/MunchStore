package com.sugarmunch.app.ui.adaptive

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// ---------------------------------------------------------------------------
// TimePeriod — six circadian segments across 24 hours
// ---------------------------------------------------------------------------

enum class TimePeriod(
    val label: String,
    val emoji: String,
    val startHour: Int,
    val endHour: Int,
) {
    DAWN("Dawn", "🌅", 6, 8),
    MORNING("Morning", "☀️", 8, 12),
    AFTERNOON("Afternoon", "🌤️", 12, 17),
    SUNSET("Sunset", "🌇", 17, 19),
    EVENING("Evening", "🌙", 19, 22),
    NIGHT("Night", "🌑", 22, 6);

    fun containsHour(hour: Int): Boolean = when (this) {
        NIGHT -> hour >= startHour || hour < endHour
        else -> hour in startHour until endHour
    }

    fun durationHours(): Int = when (this) {
        NIGHT -> 8
        else -> endHour - startHour
    }

    fun next(): TimePeriod = entries[(ordinal + 1) % entries.size]
}

// ---------------------------------------------------------------------------
// CircadianPalette — eight candy-appropriate colours per period
// ---------------------------------------------------------------------------

data class CircadianPalette(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val surface: Color,
    val onBackground: Color,
    val onSurface: Color,
    val accent: Color,
)

// ---------------------------------------------------------------------------
// CircadianThemeEngine — singleton that drives all time-of-day calculations
// ---------------------------------------------------------------------------

object CircadianThemeEngine {

    // -- palette table (candy-themed) ------------------------------------

    private val palettes: Map<TimePeriod, CircadianPalette> = mapOf(
        TimePeriod.DAWN to CircadianPalette(
            primary = Color(0xFFFFB7C5),     // cotton-candy pink
            secondary = Color(0xFFFFDAB9),   // peach puff
            tertiary = Color(0xFFFFF0DB),    // warm cream
            background = Color(0xFFFFF5EE),  // seashell
            surface = Color(0xFFFFFFFF),
            onBackground = Color(0xFF5D4037),
            onSurface = Color(0xFF4E342E),
            accent = Color(0xFFFF8A80),      // soft coral
        ),
        TimePeriod.MORNING to CircadianPalette(
            primary = Color(0xFFFF6F91),     // bubblegum pink
            secondary = Color(0xFFFF9671),   // tangerine
            tertiary = Color(0xFFFFC75F),    // lemon drop
            background = Color(0xFFFFF8E1),  // warm white
            surface = Color(0xFFFFFFFF),
            onBackground = Color(0xFF3E2723),
            onSurface = Color(0xFF4E342E),
            accent = Color(0xFFFF5252),      // cherry red
        ),
        TimePeriod.AFTERNOON to CircadianPalette(
            primary = Color(0xFFE91E63),     // hot pink lollipop
            secondary = Color(0xFFFF5722),   // orange candy
            tertiary = Color(0xFFFFEB3B),    // lemon zest
            background = Color(0xFFFFFDE7),  // bright cream
            surface = Color(0xFFFFFFFF),
            onBackground = Color(0xFF212121),
            onSurface = Color(0xFF37474F),
            accent = Color(0xFFD500F9),      // grape candy
        ),
        TimePeriod.SUNSET to CircadianPalette(
            primary = Color(0xFFFF7043),     // burnt orange taffy
            secondary = Color(0xFFFF80AB),   // pink sherbet
            tertiary = Color(0xFFFFAB40),    // butterscotch
            background = Color(0xFFFFF3E0),  // warm amber cream
            surface = Color(0xFFFFECB3),
            onBackground = Color(0xFF4E342E),
            onSurface = Color(0xFF5D4037),
            accent = Color(0xFFFF6D00),      // caramel
        ),
        TimePeriod.EVENING to CircadianPalette(
            primary = Color(0xFF7E57C2),     // lavender candy
            secondary = Color(0xFF5C6BC0),   // blueberry
            tertiary = Color(0xFFAB47BC),    // plum
            background = Color(0xFFEDE7F6),  // soft lavender bg
            surface = Color(0xFFE8EAF6),
            onBackground = Color(0xFF311B92),
            onSurface = Color(0xFF283593),
            accent = Color(0xFFE040FB),      // neon grape
        ),
        TimePeriod.NIGHT to CircadianPalette(
            primary = Color(0xFF3949AB),     // midnight blueberry
            secondary = Color(0xFF1A237E),   // deep indigo
            tertiary = Color(0xFF283593),    // dark plum
            background = Color(0xFF0D1B2A),  // deep night
            surface = Color(0xFF1B2838),
            onBackground = Color(0xFFBBDEFB),
            onSurface = Color(0xFF90CAF9),
            accent = Color(0xFF448AFF),      // blue glow
        ),
    )

    // -- public API -------------------------------------------------------

    fun getCurrentPeriod(overrideHour: Int? = null): TimePeriod {
        val hour = overrideHour ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return TimePeriod.entries.first { it.containsHour(hour) }
    }

    /**
     * Returns 0f..1f indicating progress *within* the current period, with the
     * last 30 minutes blending into the next period for a smooth cross-fade.
     */
    fun getTransitionProgress(overrideHour: Int? = null, overrideMinute: Int? = null): Float {
        val cal = Calendar.getInstance()
        val hour = overrideHour ?: cal.get(Calendar.HOUR_OF_DAY)
        val minute = overrideMinute ?: cal.get(Calendar.MINUTE)
        val period = getCurrentPeriod(hour)

        val minutesSinceStart = when (period) {
            TimePeriod.NIGHT -> {
                if (hour >= period.startHour) (hour - period.startHour) * 60 + minute
                else (hour + 24 - period.startHour) * 60 + minute
            }
            else -> (hour - period.startHour) * 60 + minute
        }
        val totalMinutes = period.durationHours() * 60
        val crossfadeWindow = 30

        val minutesUntilEnd = totalMinutes - minutesSinceStart
        return if (minutesUntilEnd <= crossfadeWindow) {
            1f - (minutesUntilEnd.toFloat() / crossfadeWindow)
        } else {
            0f
        }
    }

    fun getCurrentPalette(overrideHour: Int? = null, overrideMinute: Int? = null): CircadianPalette {
        val period = getCurrentPeriod(overrideHour)
        val progress = getTransitionProgress(overrideHour, overrideMinute)
        val current = palettes.getValue(period)
        val next = palettes.getValue(period.next())
        return blendPalettes(current, next, progress)
    }

    fun getPaletteForPeriod(period: TimePeriod): CircadianPalette = palettes.getValue(period)

    fun getAnimationIntensity(overrideHour: Int? = null): Float {
        val hour = overrideHour ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (getCurrentPeriod(hour)) {
            TimePeriod.DAWN -> 0.4f
            TimePeriod.MORNING -> 0.7f
            TimePeriod.AFTERNOON -> 1.0f
            TimePeriod.SUNSET -> 0.6f
            TimePeriod.EVENING -> 0.35f
            TimePeriod.NIGHT -> 0.15f
        }
    }

    fun getParticleMultiplier(overrideHour: Int? = null): Float {
        val hour = overrideHour ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (getCurrentPeriod(hour)) {
            TimePeriod.DAWN -> 0.5f
            TimePeriod.MORNING -> 0.8f
            TimePeriod.AFTERNOON -> 1.0f
            TimePeriod.SUNSET -> 0.7f
            TimePeriod.EVENING -> 0.4f
            TimePeriod.NIGHT -> 0.2f
        }
    }

    // -- helpers ----------------------------------------------------------

    private fun lerpColor(a: Color, b: Color, t: Float): Color = Color(
        red = a.red + (b.red - a.red) * t,
        green = a.green + (b.green - a.green) * t,
        blue = a.blue + (b.blue - a.blue) * t,
        alpha = a.alpha + (b.alpha - a.alpha) * t,
    )

    private fun blendPalettes(a: CircadianPalette, b: CircadianPalette, t: Float): CircadianPalette =
        CircadianPalette(
            primary = lerpColor(a.primary, b.primary, t),
            secondary = lerpColor(a.secondary, b.secondary, t),
            tertiary = lerpColor(a.tertiary, b.tertiary, t),
            background = lerpColor(a.background, b.background, t),
            surface = lerpColor(a.surface, b.surface, t),
            onBackground = lerpColor(a.onBackground, b.onBackground, t),
            onSurface = lerpColor(a.onSurface, b.onSurface, t),
            accent = lerpColor(a.accent, b.accent, t),
        )
}

// ---------------------------------------------------------------------------
// CircadianThemeOverlay — Canvas-based translucent colour wash
// ---------------------------------------------------------------------------

@Composable
fun CircadianThemeOverlay(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    overrideHour: Int? = null,
    overrideMinute: Int? = null,
    content: @Composable () -> Unit,
) {
    val palette = remember(overrideHour, overrideMinute) {
        CircadianThemeEngine.getCurrentPalette(overrideHour, overrideMinute)
    }

    val animatedPrimary by animateColorAsState(
        targetValue = palette.primary.copy(alpha = 0.08f),
        animationSpec = tween(durationMillis = 2000),
        label = "circadianPrimary",
    )
    val animatedAccent by animateColorAsState(
        targetValue = palette.accent.copy(alpha = 0.06f),
        animationSpec = tween(durationMillis = 2000),
        label = "circadianAccent",
    )

    Box(modifier = modifier) {
        content()

        if (enabled) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(animatedPrimary, Color.Transparent, animatedAccent),
                        startY = 0f,
                        endY = size.height,
                    ),
                    size = size,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// CircadianSettingsPanel — full-featured settings UI
// ---------------------------------------------------------------------------

@Composable
fun CircadianSettingsPanel(
    modifier: Modifier = Modifier,
    adaptiveEnabled: Boolean = true,
    onAdaptiveToggle: (Boolean) -> Unit = {},
    pinnedPeriod: TimePeriod? = null,
    onPinnedPeriodChange: (TimePeriod?) -> Unit = {},
) {
    var previewHour by remember { mutableFloatStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toFloat()) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val currentPeriod = CircadianThemeEngine.getCurrentPeriod()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // -- header -------------------------------------------------------
        Text(
            text = "🍬 Circadian Theme",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        // -- adaptive toggle ----------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("Adaptive Mode", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Shift colours with the time of day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = adaptiveEnabled,
                    onCheckedChange = onAdaptiveToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }
        }

        // -- period swatches (horizontal row) -----------------------------
        Text("Time Periods", fontWeight = FontWeight.SemiBold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TimePeriod.entries.forEach { period ->
                val palette = CircadianThemeEngine.getPaletteForPeriod(period)
                val isCurrent = period == currentPeriod

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(palette.primary)
                            .then(
                                if (isCurrent) Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.onBackground,
                                    CircleShape,
                                ) else Modifier
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(period.emoji, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        period.label,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                    )
                    if (isCurrent) {
                        Text(
                            "Now",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }

        // -- pin to period dropdown ---------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Pin to Period", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Lock to a specific time period for testing",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))

                Box {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dropdownExpanded = true },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Text(
                            text = pinnedPeriod?.let { "${it.emoji} ${it.label}" } ?: "Auto (follow clock)",
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Auto (follow clock)") },
                            onClick = {
                                onPinnedPeriodChange(null)
                                dropdownExpanded = false
                            },
                        )
                        TimePeriod.entries.forEach { period ->
                            DropdownMenuItem(
                                text = { Text("${period.emoji} ${period.label}") },
                                onClick = {
                                    onPinnedPeriodChange(period)
                                    dropdownExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        }

        // -- 24-hour preview slider ---------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("24-Hour Preview", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Scrub through the day to preview theme changes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))

                val hourInt = previewHour.toInt()
                val minuteInt = ((previewHour - hourInt) * 60).toInt()
                val previewPalette = CircadianThemeEngine.getCurrentPalette(hourInt, minuteInt)
                val previewPeriod = CircadianThemeEngine.getCurrentPeriod(hourInt)

                Text(
                    text = "${previewPeriod.emoji} ${previewPeriod.label} — %02d:%02d".format(hourInt, minuteInt),
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(8.dp))

                Slider(
                    value = previewHour,
                    onValueChange = { previewHour = it },
                    valueRange = 0f..23.99f,
                    steps = 143,
                    colors = SliderDefaults.colors(
                        thumbColor = previewPalette.primary,
                        activeTrackColor = previewPalette.accent,
                    ),
                )
                Spacer(Modifier.height(8.dp))

                // colour swatch preview for the selected hour
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    listOf(
                        "Pri" to previewPalette.primary,
                        "Sec" to previewPalette.secondary,
                        "Ter" to previewPalette.tertiary,
                        "Bg" to previewPalette.background,
                        "Srf" to previewPalette.surface,
                        "Acc" to previewPalette.accent,
                    ).forEach { (name, color) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(color)
                                    .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                            )
                            Text(name, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // -- animation intensity per period -------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Animation Intensity", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))

                TimePeriod.entries.forEach { period ->
                    val intensity = CircadianThemeEngine.getAnimationIntensity(period.startHour)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${period.emoji} ${period.label}",
                            modifier = Modifier.width(110.dp),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = intensity)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        CircadianThemeEngine.getPaletteForPeriod(period).accent,
                                    ),
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${(intensity * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.End,
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// CircadianTransitionDemo — 24-hour auto-cycling visual demo
// ---------------------------------------------------------------------------

private data class DemoParticle(
    var x: Float,
    var y: Float,
    var radius: Float,
    var speed: Float,
    var alpha: Float,
    var phase: Float,
)

@Composable
fun CircadianTransitionDemo(modifier: Modifier = Modifier) {
    // The demo cycles through 24 hours in ~24 seconds (1 s per hour).
    var simulatedHour by remember { mutableFloatStateOf(0f) }
    var particles by remember { mutableStateOf(emptyList<DemoParticle>()) }
    var canvasWidth by remember { mutableFloatStateOf(400f) }
    var canvasHeight by remember { mutableFloatStateOf(300f) }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos == 0L) lastNanos = nanos
                val dt = (nanos - lastNanos) / 1_000_000_000f
                lastNanos = nanos

                simulatedHour = (simulatedHour + dt) % 24f

                // lazily initialise particles when canvas size is known
                if (particles.isEmpty() && canvasWidth > 0f) {
                    particles = List(30) {
                        DemoParticle(
                            x = (Math.random() * canvasWidth).toFloat(),
                            y = (Math.random() * canvasHeight).toFloat(),
                            radius = 2f + (Math.random() * 4f).toFloat(),
                            speed = 10f + (Math.random() * 30f).toFloat(),
                            alpha = 0.3f + (Math.random() * 0.5f).toFloat(),
                            phase = (Math.random() * 2 * PI).toFloat(),
                        )
                    }
                }

                // animate particles
                val intensity = CircadianThemeEngine.getAnimationIntensity(simulatedHour.toInt())
                particles = particles.map { p ->
                    val newY = p.y - p.speed * dt * intensity
                    val wrapped = if (newY < -p.radius) canvasHeight + p.radius else newY
                    p.copy(
                        y = wrapped,
                        x = p.x + sin(p.phase + simulatedHour) * 0.5f,
                    )
                }
            }
        }
    }

    val hourInt = simulatedHour.toInt()
    val minuteInt = ((simulatedHour - hourInt) * 60).toInt()
    val palette = CircadianThemeEngine.getCurrentPalette(hourInt, minuteInt)
    val period = CircadianThemeEngine.getCurrentPeriod(hourInt)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.background),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    canvasWidth = size.width
                    canvasHeight = size.height

                    // background gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(palette.primary.copy(alpha = 0.3f), palette.background),
                        ),
                        size = size,
                    )

                    // particles
                    val multiplier = CircadianThemeEngine.getParticleMultiplier(hourInt)
                    val visibleCount = (particles.size * multiplier).toInt().coerceAtLeast(1)
                    particles.take(visibleCount).forEach { p ->
                        drawCircle(
                            color = palette.accent.copy(alpha = p.alpha * multiplier),
                            radius = p.radius,
                            center = Offset(p.x, p.y),
                        )
                    }

                    // horizon line
                    drawLine(
                        color = palette.secondary.copy(alpha = 0.4f),
                        start = Offset(0f, size.height * 0.75f),
                        end = Offset(size.width, size.height * 0.75f),
                        strokeWidth = 2f,
                    )

                    // "sun/moon" indicator
                    val dayProgress = simulatedHour / 24f
                    val arcX = size.width * dayProgress
                    val arcY = size.height * 0.75f - sin(dayProgress * PI.toFloat()) * size.height * 0.55f
                    val orbColor = if (hourInt in 6..18) palette.accent else palette.tertiary
                    drawCircle(
                        color = orbColor,
                        radius = 14f,
                        center = Offset(arcX, arcY),
                    )
                    drawCircle(
                        color = orbColor.copy(alpha = 0.25f),
                        radius = 22f,
                        center = Offset(arcX, arcY),
                    )
                }

                // period badge overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(
                            palette.surface.copy(alpha = 0.85f),
                            RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "${period.emoji} ${period.label}",
                        color = palette.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                }

                // time readout overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(
                            palette.surface.copy(alpha = 0.85f),
                            RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "%02d:%02d".format(hourInt, minuteInt),
                        color = palette.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                }
            }

            // colour strip at the bottom
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf(
                    palette.primary,
                    palette.secondary,
                    palette.tertiary,
                    palette.accent,
                ).forEach { color ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .background(color),
                    )
                }
            }
        }
    }
}
