package com.sugarmunch.app.ui.seasonal

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.withFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.ui.design.SugarDimens
import java.time.LocalDate
import java.time.MonthDay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ─── Season Enum ────────────────────────────────────────────────────────────────

enum class Season(
    val displayName: String,
    val emoji: String,
    val startMonth: Int,
    val startDay: Int,
    val endMonth: Int,
    val endDay: Int
) {
    HALLOWEEN("Halloween", "🎃", 10, 15, 11, 2),
    CHRISTMAS("Christmas", "🎄", 12, 1, 12, 31),
    SPRING("Spring Bloom", "🌸", 3, 1, 4, 30),
    NEW_YEAR("New Year", "🎆", 12, 28, 1, 5),
    SUGAR_FESTIVAL("Sugar Festival", "🍬", 6, 1, 6, 15),
    VALENTINE("Valentine's", "💕", 2, 7, 2, 21),
    SUMMER("Summer Vibes", "🌊", 7, 1, 8, 31),
    AUTUMN("Autumn Harvest", "🍂", 9, 15, 10, 14)
}

// ─── Seasonal Theme Data ────────────────────────────────────────────────────────

data class SeasonalTheme(
    val season: Season,
    val primaryColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val accentColor: Color,
    val particleTypes: List<String>,
    val backgroundScene: String?,
    val specialEffects: List<String>
)

data class SeasonalReward(
    val name: String,
    val description: String,
    val emoji: String,
    val rarity: String,
    val isUnlocked: Boolean = false
)

// ─── Theme Definitions ──────────────────────────────────────────────────────────

private val seasonalThemes = mapOf(
    Season.HALLOWEEN to SeasonalTheme(
        season = Season.HALLOWEEN,
        primaryColor = Color(0xFFFF6F00),
        secondaryColor = Color(0xFF7B1FA2),
        tertiaryColor = Color(0xFF1B1B1B),
        backgroundColor = Color(0xFF121212),
        surfaceColor = Color(0xFF1E1E2A),
        accentColor = Color(0xFF00E676),
        particleTypes = listOf("bats", "pumpkins", "spiders"),
        backgroundScene = "haunted_night",
        specialEffects = listOf("fog", "flicker", "eerie_glow")
    ),
    Season.CHRISTMAS to SeasonalTheme(
        season = Season.CHRISTMAS,
        primaryColor = Color(0xFFD32F2F),
        secondaryColor = Color(0xFF2E7D32),
        tertiaryColor = Color(0xFFFFD700),
        backgroundColor = Color(0xFFFFF8E1),
        surfaceColor = Color(0xFFFFFDE7),
        accentColor = Color(0xFFFFFFFF),
        particleTypes = listOf("snowflakes", "stars", "gifts"),
        backgroundScene = "cozy_winter",
        specialEffects = listOf("snow_fall", "twinkle", "warm_glow")
    ),
    Season.SPRING to SeasonalTheme(
        season = Season.SPRING,
        primaryColor = Color(0xFFF48FB1),
        secondaryColor = Color(0xFF81C784),
        tertiaryColor = Color(0xFFCE93D8),
        backgroundColor = Color(0xFFF1F8E9),
        surfaceColor = Color(0xFFFFF3E0),
        accentColor = Color(0xFFFFF176),
        particleTypes = listOf("cherry_blossoms", "butterflies"),
        backgroundScene = "garden_bloom",
        specialEffects = listOf("breeze", "petal_drift", "sunshine")
    ),
    Season.NEW_YEAR to SeasonalTheme(
        season = Season.NEW_YEAR,
        primaryColor = Color(0xFFFFD700),
        secondaryColor = Color(0xFFC0C0C0),
        tertiaryColor = Color(0xFF1A1A2E),
        backgroundColor = Color(0xFF0D0D1A),
        surfaceColor = Color(0xFF16213E),
        accentColor = Color(0xFFFFFFFF),
        particleTypes = listOf("fireworks", "confetti", "stars"),
        backgroundScene = "midnight_sky",
        specialEffects = listOf("burst", "shimmer", "countdown")
    ),
    Season.SUGAR_FESTIVAL to SeasonalTheme(
        season = Season.SUGAR_FESTIVAL,
        primaryColor = Color(0xFFFF69B4),
        secondaryColor = Color(0xFF98FB98),
        tertiaryColor = Color(0xFF87CEEB),
        backgroundColor = Color(0xFFFFF0F5),
        surfaceColor = Color(0xFFFFF8DC),
        accentColor = Color(0xFFDDA0DD),
        particleTypes = listOf("candy_canes", "lollipops", "gummy_bears"),
        backgroundScene = "candy_land",
        specialEffects = listOf("rainbow_swirl", "sugar_sparkle", "sweet_bounce")
    ),
    Season.VALENTINE to SeasonalTheme(
        season = Season.VALENTINE,
        primaryColor = Color(0xFFE91E63),
        secondaryColor = Color(0xFFFF5252),
        tertiaryColor = Color(0xFFFFFFFF),
        backgroundColor = Color(0xFFFCE4EC),
        surfaceColor = Color(0xFFFFF0F3),
        accentColor = Color(0xFFB76E79),
        particleTypes = listOf("hearts", "roses"),
        backgroundScene = "romantic_gradient",
        specialEffects = listOf("heartbeat", "rose_petals", "love_glow")
    ),
    Season.SUMMER to SeasonalTheme(
        season = Season.SUMMER,
        primaryColor = Color(0xFF00BCD4),
        secondaryColor = Color(0xFFFF9800),
        tertiaryColor = Color(0xFFFFEB3B),
        backgroundColor = Color(0xFFE0F7FA),
        surfaceColor = Color(0xFFFFF8E1),
        accentColor = Color(0xFFFF7043),
        particleTypes = listOf("bubbles", "waves"),
        backgroundScene = "tropical_beach",
        specialEffects = listOf("wave_motion", "sun_rays", "shimmer")
    ),
    Season.AUTUMN to SeasonalTheme(
        season = Season.AUTUMN,
        primaryColor = Color(0xFFE65100),
        secondaryColor = Color(0xFF5D4037),
        tertiaryColor = Color(0xFFB71C1C),
        backgroundColor = Color(0xFFFBE9E7),
        surfaceColor = Color(0xFFEFEBE9),
        accentColor = Color(0xFFFFB300),
        particleTypes = listOf("leaves", "acorns"),
        backgroundScene = "forest_path",
        specialEffects = listOf("wind_gust", "golden_light", "rustling")
    )
)

// ─── Engine ─────────────────────────────────────────────────────────────────────

class SeasonalThemeEngine(private val context: Context) {

    fun getCurrentSeason(): Season? {
        val today = LocalDate.now()
        return Season.entries.firstOrNull { isSeasonActive(it, today) }
    }

    fun getCurrentSeasonalTheme(): SeasonalTheme? {
        return getCurrentSeason()?.let { seasonalThemes[it] }
    }

    fun isSeasonActive(season: Season): Boolean = isSeasonActive(season, LocalDate.now())

    fun getTransitionProgress(season: Season): Float {
        val today = LocalDate.now()
        if (!isSeasonActive(season, today)) return 0f

        val totalDays = seasonDurationDays(season)
        val elapsed = daysIntoSeason(season, today)

        val fadeInDays = (totalDays * 0.15f).coerceAtLeast(1f)
        val fadeOutStart = totalDays - fadeInDays

        return when {
            elapsed < fadeInDays -> elapsed / fadeInDays
            elapsed > fadeOutStart -> (totalDays - elapsed) / fadeInDays
            else -> 1f
        }.coerceIn(0f, 1f)
    }

    fun getAllSeasons(): List<Season> = Season.entries.toList()

    private fun isSeasonActive(season: Season, date: LocalDate): Boolean {
        val month = date.monthValue
        val day = date.dayOfMonth

        return if (season.startMonth <= season.endMonth) {
            (month > season.startMonth || (month == season.startMonth && day >= season.startDay)) &&
                (month < season.endMonth || (month == season.endMonth && day <= season.endDay))
        } else {
            // Wraps around year boundary (e.g., NEW_YEAR: Dec 28 – Jan 5)
            (month > season.startMonth || (month == season.startMonth && day >= season.startDay)) ||
                (month < season.endMonth || (month == season.endMonth && day <= season.endDay))
        }
    }

    private fun seasonDurationDays(season: Season): Float {
        val start = MonthDay.of(season.startMonth, season.startDay)
        val end = MonthDay.of(season.endMonth, season.endDay)
        val startOrd = start.month.value * 31 + start.dayOfMonth
        val endOrd = end.month.value * 31 + end.dayOfMonth
        return if (endOrd >= startOrd) (endOrd - startOrd + 1).toFloat()
        else (365 - startOrd + endOrd + 1).toFloat()
    }

    private fun daysIntoSeason(season: Season, date: LocalDate): Float {
        val todayOrd = date.monthValue * 31 + date.dayOfMonth
        val startOrd = season.startMonth * 31 + season.startDay
        return if (todayOrd >= startOrd) (todayOrd - startOrd).toFloat()
        else (365 - startOrd + todayOrd).toFloat()
    }

    companion object {
        @Volatile
        private var instance: SeasonalThemeEngine? = null

        fun getInstance(context: Context): SeasonalThemeEngine {
            return instance ?: synchronized(this) {
                instance ?: SeasonalThemeEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}

// ─── Particle Data ──────────────────────────────────────────────────────────────

private data class SeasonParticle(
    var x: Float,
    var y: Float,
    var size: Float,
    var speed: Float,
    var angle: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    var alpha: Float,
    var color: Color,
    var type: Int
)

private fun initParticles(count: Int, width: Float, height: Float, theme: SeasonalTheme): List<SeasonParticle> {
    val colors = listOf(theme.primaryColor, theme.secondaryColor, theme.accentColor, theme.tertiaryColor)
    return List(count) {
        SeasonParticle(
            x = Random.nextFloat() * width,
            y = Random.nextFloat() * height,
            size = Random.nextFloat() * 10f + 4f,
            speed = Random.nextFloat() * 40f + 20f,
            angle = Random.nextFloat() * 360f,
            rotation = Random.nextFloat() * 360f,
            rotationSpeed = Random.nextFloat() * 60f - 30f,
            alpha = Random.nextFloat() * 0.5f + 0.3f,
            color = colors.random(),
            type = Random.nextInt(theme.particleTypes.size)
        )
    }
}

// ─── Seasonal Overlay ───────────────────────────────────────────────────────────

@Composable
fun SeasonalOverlay(modifier: Modifier = Modifier) {
    val today = remember { LocalDate.now() }
    val theme = remember(today) {
        seasonalThemes.values.firstOrNull { theme ->
            val s = theme.season
            val month = today.monthValue
            val day = today.dayOfMonth
            if (s.startMonth <= s.endMonth) {
                (month > s.startMonth || (month == s.startMonth && day >= s.startDay)) &&
                    (month < s.endMonth || (month == s.endMonth && day <= s.endDay))
            } else {
                (month > s.startMonth || (month == s.startMonth && day >= s.startDay)) ||
                    (month < s.endMonth || (month == s.endMonth && day <= s.endDay))
            }
        }
    } ?: return

    SeasonalParticleCanvas(theme = theme, modifier = modifier)
}

@Composable
private fun SeasonalParticleCanvas(
    theme: SeasonalTheme,
    modifier: Modifier = Modifier
) {
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }
    val particles = remember(theme.season) {
        mutableStateOf(emptyList<SeasonParticle>())
    }
    var time by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(theme.season) {
        var lastFrameNanos = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                val dt = if (lastFrameNanos == 0L) 0.016f
                else ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                lastFrameNanos = frameNanos
                time += dt

                if (canvasWidth > 0f && canvasHeight > 0f) {
                    if (particles.value.isEmpty()) {
                        particles.value = initParticles(35, canvasWidth, canvasHeight, theme)
                    }
                    particles.value.forEach { p ->
                        updateParticle(p, dt, canvasWidth, canvasHeight, theme.season, time)
                    }
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        canvasWidth = size.width
        canvasHeight = size.height
        particles.value.forEach { p ->
            drawSeasonParticle(p, theme.season)
        }
        if (theme.season == Season.HALLOWEEN) {
            drawSpiderWeb(this, canvasWidth, canvasHeight)
        }
    }
}

private fun updateParticle(
    p: SeasonParticle, dt: Float, w: Float, h: Float, season: Season, time: Float
) {
    p.rotation += p.rotationSpeed * dt
    when (season) {
        Season.HALLOWEEN -> {
            p.y += p.speed * dt * 0.5f
            p.x += sin(time * 3f + p.angle) * 30f * dt
        }
        Season.CHRISTMAS -> {
            p.y += p.speed * dt
            p.x += sin(time * 1.5f + p.angle) * 20f * dt
        }
        Season.SPRING -> {
            p.y += p.speed * dt * 0.7f
            p.x += sin(time * 2f + p.angle) * 40f * dt
        }
        Season.NEW_YEAR -> {
            val burst = sin(time * 2f + p.angle)
            p.x += cos(p.angle * PI.toFloat() / 180f) * p.speed * dt * burst
            p.y -= p.speed * dt * 0.8f
        }
        Season.SUGAR_FESTIVAL -> {
            p.y += p.speed * dt
            p.x += sin(time * 2.5f + p.angle) * 25f * dt
        }
        Season.VALENTINE -> {
            p.y -= p.speed * dt * 0.6f
            p.x += sin(time * 1.8f + p.angle) * 15f * dt
        }
        Season.SUMMER -> {
            p.y -= p.speed * dt * 0.5f
            p.x += sin(time * 1.2f + p.angle) * 10f * dt
            p.size = p.size + sin(time * 3f + p.x) * 0.05f
        }
        Season.AUTUMN -> {
            p.y += p.speed * dt * 0.8f
            p.x += sin(time * 1.5f + p.angle) * 35f * dt
        }
    }
    // Wrap around
    if (p.y > h + 20f) { p.y = -20f; p.x = Random.nextFloat() * w }
    if (p.y < -20f) { p.y = h + 20f; p.x = Random.nextFloat() * w }
    if (p.x > w + 20f) p.x = -20f
    if (p.x < -20f) p.x = w + 20f
}

private fun DrawScope.drawSeasonParticle(p: SeasonParticle, season: Season) {
    translate(left = p.x, top = p.y) {
        rotate(degrees = p.rotation, pivot = Offset.Zero) {
            when (season) {
                Season.HALLOWEEN -> drawBatShape(p)
                Season.CHRISTMAS -> drawSnowflake(p)
                Season.SPRING -> drawCherryBlossom(p)
                Season.NEW_YEAR -> drawFireworkDot(p)
                Season.SUGAR_FESTIVAL -> drawCandyShape(p)
                Season.VALENTINE -> drawHeart(p)
                Season.SUMMER -> drawBubble(p)
                Season.AUTUMN -> drawLeaf(p)
            }
        }
    }
}

private fun DrawScope.drawBatShape(p: SeasonParticle) {
    val s = p.size
    val path = Path().apply {
        moveTo(0f, 0f)
        lineTo(-s * 1.5f, -s * 0.8f)
        lineTo(-s * 0.8f, -s * 0.2f)
        lineTo(-s * 1.2f, s * 0.5f)
        lineTo(0f, 0f)
        lineTo(s * 1.2f, s * 0.5f)
        lineTo(s * 0.8f, -s * 0.2f)
        lineTo(s * 1.5f, -s * 0.8f)
        close()
    }
    drawPath(path, color = p.color.copy(alpha = p.alpha), style = Fill)
}

private fun DrawScope.drawSnowflake(p: SeasonParticle) {
    val s = p.size
    for (i in 0 until 6) {
        val angle = i * 60f * (PI.toFloat() / 180f)
        val endX = cos(angle) * s
        val endY = sin(angle) * s
        drawLine(
            color = p.color.copy(alpha = p.alpha),
            start = Offset.Zero,
            end = Offset(endX, endY),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )
        // Branch tips
        val branchLen = s * 0.4f
        val midX = endX * 0.6f
        val midY = endY * 0.6f
        drawLine(
            color = p.color.copy(alpha = p.alpha * 0.7f),
            start = Offset(midX, midY),
            end = Offset(midX + cos(angle + 0.8f) * branchLen, midY + sin(angle + 0.8f) * branchLen),
            strokeWidth = 1f
        )
    }
}

private fun DrawScope.drawCherryBlossom(p: SeasonParticle) {
    val s = p.size
    for (i in 0 until 5) {
        val angle = i * 72f * (PI.toFloat() / 180f)
        val petalX = cos(angle) * s * 0.6f
        val petalY = sin(angle) * s * 0.6f
        drawOval(
            color = p.color.copy(alpha = p.alpha),
            topLeft = Offset(petalX - s * 0.25f, petalY - s * 0.4f),
            size = androidx.compose.ui.geometry.Size(s * 0.5f, s * 0.8f)
        )
    }
    drawCircle(color = Color(0xFFFFEB3B).copy(alpha = p.alpha), radius = s * 0.15f)
}

private fun DrawScope.drawFireworkDot(p: SeasonParticle) {
    drawCircle(color = p.color.copy(alpha = p.alpha), radius = p.size * 0.4f)
    drawCircle(color = p.color.copy(alpha = p.alpha * 0.3f), radius = p.size * 0.8f)
}

private fun DrawScope.drawCandyShape(p: SeasonParticle) {
    val s = p.size
    drawCircle(color = p.color.copy(alpha = p.alpha), radius = s * 0.5f)
    drawLine(
        color = Color.White.copy(alpha = p.alpha * 0.5f),
        start = Offset(-s * 0.3f, -s * 0.3f),
        end = Offset(s * 0.3f, s * 0.3f),
        strokeWidth = 2f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawHeart(p: SeasonParticle) {
    val s = p.size
    val path = Path().apply {
        moveTo(0f, s * 0.3f)
        cubicTo(-s * 0.5f, -s * 0.3f, -s, s * 0.1f, 0f, s)
        moveTo(0f, s * 0.3f)
        cubicTo(s * 0.5f, -s * 0.3f, s, s * 0.1f, 0f, s)
    }
    drawPath(path, color = p.color.copy(alpha = p.alpha), style = Fill)
}

private fun DrawScope.drawBubble(p: SeasonParticle) {
    drawCircle(
        color = p.color.copy(alpha = p.alpha * 0.3f),
        radius = p.size * 0.6f,
        style = Fill
    )
    drawCircle(
        color = p.color.copy(alpha = p.alpha * 0.6f),
        radius = p.size * 0.6f,
        style = Stroke(width = 1.5f)
    )
    drawCircle(
        color = Color.White.copy(alpha = p.alpha * 0.5f),
        radius = p.size * 0.15f,
        center = Offset(-p.size * 0.2f, -p.size * 0.2f)
    )
}

private fun DrawScope.drawLeaf(p: SeasonParticle) {
    val s = p.size
    val path = Path().apply {
        moveTo(0f, -s)
        quadraticTo(s * 0.8f, -s * 0.3f, 0f, s)
        quadraticTo(-s * 0.8f, -s * 0.3f, 0f, -s)
        close()
    }
    drawPath(path, color = p.color.copy(alpha = p.alpha), style = Fill)
    // Stem line
    drawLine(
        color = p.color.copy(alpha = p.alpha * 0.8f),
        start = Offset(0f, -s),
        end = Offset(0f, s * 1.3f),
        strokeWidth = 1.2f,
        cap = StrokeCap.Round
    )
}

private fun drawSpiderWeb(scope: DrawScope, w: Float, h: Float) {
    val webColor = Color.White.copy(alpha = 0.08f)
    val cornerX = 0f
    val cornerY = 0f
    val rays = 6
    val rings = 4
    val reach = w.coerceAtMost(h) * 0.25f

    for (i in 0 until rays) {
        val angle = (i.toFloat() / rays) * (PI.toFloat() / 2f)
        scope.drawLine(
            color = webColor,
            start = Offset(cornerX, cornerY),
            end = Offset(cornerX + cos(angle) * reach, cornerY + sin(angle) * reach),
            strokeWidth = 0.8f
        )
    }
    for (ring in 1..rings) {
        val r = reach * ring / rings
        val path = Path().apply {
            for (i in 0..rays) {
                val angle = (i.toFloat() / rays) * (PI.toFloat() / 2f)
                val px = cornerX + cos(angle) * r
                val py = cornerY + sin(angle) * r
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
        }
        scope.drawPath(path, color = webColor, style = Stroke(width = 0.6f))
    }
}

// ─── Seasonal Banner ────────────────────────────────────────────────────────────

@Composable
fun SeasonalBanner(
    season: Season,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = seasonalThemes[season] ?: return
    val infiniteTransition = rememberInfiniteTransition(label = "banner_border")
    val borderPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "border_phase"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SugarDimens.Spacing.md, vertical = SugarDimens.Spacing.sm)
            .drawBehind {
                val borderWidth = 3.dp.toPx()
                val brush = Brush.sweepGradient(
                    colors = listOf(
                        theme.primaryColor,
                        theme.secondaryColor,
                        theme.accentColor,
                        theme.primaryColor
                    ),
                    center = Offset(size.width / 2, size.height / 2)
                )
                rotate(degrees = borderPhase) {
                    drawRoundRect(
                        brush = brush,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                            SugarDimens.Radius.lg.toPx()
                        ),
                        style = Stroke(width = borderWidth)
                    )
                }
            },
        shape = RoundedCornerShape(SugarDimens.Radius.lg),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            theme.primaryColor.copy(alpha = 0.85f),
                            theme.secondaryColor.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(SugarDimens.Spacing.md)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${season.emoji} It's ${season.displayName}!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))
                Text(
                    text = "Special themes and effects are active!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

// ─── Seasonal Rewards Preview ───────────────────────────────────────────────────

@Composable
fun SeasonalRewardsPreview(
    season: Season,
    rewards: List<SeasonalReward>,
    modifier: Modifier = Modifier
) {
    val theme = seasonalThemes[season] ?: return

    Column(modifier = modifier.padding(horizontal = SugarDimens.Spacing.md)) {
        Text(
            text = "${season.emoji} ${season.displayName} Rewards",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(SugarDimens.Spacing.xxs),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
        ) {
            items(rewards) { reward ->
                SeasonalRewardCard(reward = reward, theme = theme)
            }
        }
    }
}

@Composable
private fun SeasonalRewardCard(
    reward: SeasonalReward,
    theme: SeasonalTheme
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "reward_scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.md),
        colors = CardDefaults.cardColors(
            containerColor = if (reward.isUnlocked) theme.surfaceColor
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = reward.emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))
            Text(
                text = reward.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = reward.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = reward.rarity,
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.accentColor,
                    fontWeight = FontWeight.Bold
                )
                if (!reward.isUnlocked) {
                    Spacer(modifier = Modifier.width(SugarDimens.Spacing.xxs))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
