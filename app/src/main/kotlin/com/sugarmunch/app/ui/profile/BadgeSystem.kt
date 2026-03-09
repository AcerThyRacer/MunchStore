package com.sugarmunch.app.ui.profile

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ─── Badge Data Model ────────────────────────────────────────────────────────

enum class BadgeRarity(val displayName: String, val color: Color) {
    COMMON("Common", Color(0xFF9E9E9E)),
    UNCOMMON("Uncommon", Color(0xFF4CAF50)),
    RARE("Rare", Color(0xFF2196F3)),
    EPIC("Epic", Color(0xFF9C27B0)),
    LEGENDARY("Legendary", Color(0xFFFF9800))
}

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val rarity: BadgeRarity,
    val iconEmoji: String,
    val isUnlocked: Boolean = false,
    val progress: Float = 0f,
    val unlockedAt: Long? = null
)

// ─── Badge Renderer ──────────────────────────────────────────────────────────

@Composable
fun BadgeRenderer(
    badge: Badge,
    size: Dp = 64.dp,
    showAnimation: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_${badge.id}")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    // Scale-in animation on first appear
    val scaleAnim = remember { Animatable(if (showAnimation && badge.isUnlocked) 0.5f else 1f) }
    val glowAnim = remember { Animatable(if (showAnimation && badge.isUnlocked) 1.5f else 0f) }

    LaunchedEffect(badge.id, showAnimation) {
        if (showAnimation && badge.isUnlocked) {
            scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            glowAnim.animateTo(0f, tween(600))
        }
    }

    val rarityColor = badge.rarity.color
    val dimAlpha = if (badge.isUnlocked) 1f else 0.4f

    Box(
        modifier = modifier
            .size(size)
            .scale(scaleAnim.value)
            .alpha(dimAlpha),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = this.size.minDimension * 0.08f
            val radius = (this.size.minDimension - strokeWidth) / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // Glow burst on appear
            if (glowAnim.value > 0f) {
                drawCircle(
                    color = rarityColor.copy(alpha = 0.3f * glowAnim.value),
                    radius = radius * (1f + glowAnim.value * 0.3f),
                    center = center
                )
            }

            // Inner circle background
            drawCircle(
                color = if (badge.isUnlocked) rarityColor.copy(alpha = 0.15f)
                else Color.Gray.copy(alpha = 0.1f),
                radius = radius - strokeWidth / 2f,
                center = center
            )

            when {
                !badge.isUnlocked -> {
                    // Locked: static gray ring + progress arc
                    drawCircle(
                        color = Color.Gray.copy(alpha = 0.3f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )
                    if (badge.progress > 0f) {
                        drawArc(
                            color = rarityColor.copy(alpha = 0.6f),
                            startAngle = -90f,
                            sweepAngle = 360f * badge.progress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                badge.rarity == BadgeRarity.COMMON -> {
                    drawCircle(
                        color = rarityColor,
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )
                }

                badge.rarity == BadgeRarity.UNCOMMON -> {
                    drawCircle(
                        color = rarityColor.copy(alpha = 0.6f + 0.4f * pulse),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )
                }

                badge.rarity == BadgeRarity.RARE -> {
                    rotate(rotation) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    rarityColor, rarityColor.copy(alpha = 0.3f),
                                    rarityColor, rarityColor.copy(alpha = 0.3f)
                                )
                            ),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                badge.rarity == BadgeRarity.EPIC -> {
                    val epicShift = rarityColor.copy(
                        red = (rarityColor.red + 0.15f * pulse).coerceAtMost(1f),
                        blue = (rarityColor.blue - 0.1f * pulse).coerceAtLeast(0f)
                    )
                    rotate(rotation * 0.7f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    rarityColor, epicShift, rarityColor,
                                    epicShift.copy(alpha = 0.5f), rarityColor
                                )
                            ),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth * pulse, cap = StrokeCap.Round)
                        )
                    }
                }

                badge.rarity == BadgeRarity.LEGENDARY -> {
                    // Holographic shimmer ring
                    rotate(rotation * 0.5f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFFFFD700), Color(0xFFFFA500),
                                    Color(0xFFFF8C00), Color(0xFFFFD700),
                                    Color(0xFFFFE4B5), Color(0xFFFFD700)
                                )
                            ),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    // Golden particle emission
                    val particleCount = 6
                    for (i in 0 until particleCount) {
                        val angle = Math.toRadians(((rotation * 1.2f) + i * (360f / particleCount)).toDouble())
                        val dist = radius + strokeWidth * 0.5f + (pulse - 0.85f) * 40f
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = pulse * 0.7f),
                            radius = 2.5f,
                            center = Offset(
                                center.x + (dist * cos(angle)).toFloat(),
                                center.y + (dist * sin(angle)).toFloat()
                            )
                        )
                    }
                }
            }
        }

        // Emoji icon or lock overlay
        if (badge.isUnlocked) {
            Text(
                text = badge.iconEmoji,
                fontSize = (size.value * 0.38f).sp,
                textAlign = TextAlign.Center
            )
        } else {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                modifier = Modifier.size(size * 0.35f),
                tint = Color.Gray.copy(alpha = 0.6f)
            )
        }
    }
}

// ─── Badge Unlock Animation ──────────────────────────────────────────────────

@Composable
fun BadgeUnlockAnimation(
    badge: Badge,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val overlayAlpha = remember { Animatable(0f) }
    val badgeScale = remember { Animatable(0.1f) }
    val crackAlpha = remember { Animatable(0f) }
    val revealAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val particleProgress = remember { Animatable(0f) }
    val fadeOut = remember { Animatable(1f) }

    val particleSeeds = remember {
        List(24) {
            Triple(
                Random.nextFloat() * 360f,
                Random.nextFloat() * 0.5f + 0.5f,
                Random.nextFloat() * 0.6f + 0.4f
            )
        }
    }

    LaunchedEffect(Unit) {
        // 1. Dark overlay fades in
        overlayAlpha.animateTo(0.7f, tween(300))
        // 2. Sealed badge scales up with spring overshoot
        badgeScale.animateTo(1.2f, spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ))
        // 3. Crack effect
        crackAlpha.animateTo(1f, tween(200))
        delay(100)
        // 4. Badge reveal with burst
        badgeScale.animateTo(1f, tween(150))
        crackAlpha.animateTo(0f, tween(200))
        revealAlpha.animateTo(1f, tween(300))
        particleProgress.animateTo(1f, tween(500))
        // 5. Text fades in
        textAlpha.animateTo(1f, tween(400))
        // 6. Particle shower continues
        delay(1000)
        // 7. Everything fades out
        fadeOut.animateTo(0f, tween(300))
        onComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(fadeOut.value),
        contentAlignment = Alignment.Center
    ) {
        // Dark overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color.Black.copy(alpha = overlayAlpha.value))
        }

        // Crack lines radiating from center
        if (crackAlpha.value > 0f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val lineCount = 8
                for (i in 0 until lineCount) {
                    val angle = Math.toRadians((i * 360.0 / lineCount))
                    val len = 80f * crackAlpha.value
                    drawLine(
                        color = badge.rarity.color.copy(alpha = crackAlpha.value),
                        start = center,
                        end = Offset(
                            center.x + (len * cos(angle)).toFloat(),
                            center.y + (len * sin(angle)).toFloat()
                        ),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Particle burst
        if (particleProgress.value > 0f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                particleSeeds.forEach { (angleDeg, speedFactor, sizeFactor) ->
                    val angle = Math.toRadians(angleDeg.toDouble())
                    val dist = 120f * particleProgress.value * speedFactor
                    val pAlpha = (1f - particleProgress.value).coerceAtLeast(0f) * 0.8f
                    drawCircle(
                        color = badge.rarity.color.copy(alpha = pAlpha),
                        radius = 4f * sizeFactor,
                        center = Offset(
                            center.x + (dist * cos(angle)).toFloat(),
                            center.y + (dist * sin(angle)).toFloat()
                        )
                    )
                }
            }
        }

        // Badge + text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.scale(badgeScale.value)) {
                if (revealAlpha.value > 0f) {
                    Box(modifier = Modifier.alpha(revealAlpha.value)) {
                        BadgeRenderer(
                            badge = badge.copy(isUnlocked = true),
                            size = 96.dp,
                            showAnimation = false
                        )
                    }
                } else {
                    // Sealed/wrapped placeholder
                    Surface(
                        modifier = Modifier.size(96.dp),
                        shape = RoundedCornerShape(SugarDimens.Radius.lg),
                        color = badge.rarity.color.copy(alpha = 0.3f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("?", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

            Box(modifier = Modifier.alpha(textAlpha.value)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = badge.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))
                    Text(
                        text = "Unlocked!",
                        style = MaterialTheme.typography.titleMedium,
                        color = badge.rarity.color
                    )
                }
            }
        }
    }
}

// ─── Badge Showcase (Trophy Wall) ────────────────────────────────────────────

private enum class BadgeFilter(val label: String) {
    ALL("All"), UNLOCKED("Unlocked"), LOCKED("Locked"),
    COMMON("Common"), UNCOMMON("Uncommon"), RARE("Rare"),
    EPIC("Epic"), LEGENDARY("Legendary")
}

private enum class BadgeSort(val label: String) {
    RECENT("Recently Unlocked"), RARITY("Rarity"), NAME("Name")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeShowcase(
    badges: List<Badge>,
    onBadgeTapped: (Badge) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeFilter by remember { mutableStateOf(BadgeFilter.ALL) }
    var activeSort by remember { mutableStateOf(BadgeSort.RECENT) }
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }

    val filteredBadges = remember(badges, activeFilter, activeSort) {
        val filtered = when (activeFilter) {
            BadgeFilter.ALL -> badges
            BadgeFilter.UNLOCKED -> badges.filter { it.isUnlocked }
            BadgeFilter.LOCKED -> badges.filter { !it.isUnlocked }
            BadgeFilter.COMMON -> badges.filter { it.rarity == BadgeRarity.COMMON }
            BadgeFilter.UNCOMMON -> badges.filter { it.rarity == BadgeRarity.UNCOMMON }
            BadgeFilter.RARE -> badges.filter { it.rarity == BadgeRarity.RARE }
            BadgeFilter.EPIC -> badges.filter { it.rarity == BadgeRarity.EPIC }
            BadgeFilter.LEGENDARY -> badges.filter { it.rarity == BadgeRarity.LEGENDARY }
        }
        when (activeSort) {
            BadgeSort.RECENT -> filtered.sortedByDescending { it.unlockedAt ?: 0L }
            BadgeSort.RARITY -> filtered.sortedByDescending { it.rarity.ordinal }
            BadgeSort.NAME -> filtered.sortedBy { it.name }
        }
    }

    val unlockedCount = badges.count { it.isUnlocked }

    Column(modifier = modifier.fillMaxSize()) {
        // Count display
        Text(
            text = "$unlockedCount/${badges.size} Unlocked",
            modifier = Modifier.padding(
                horizontal = SugarDimens.Spacing.md,
                vertical = SugarDimens.Spacing.xs
            ),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Filter chips
        LazyRow(
            modifier = Modifier.padding(horizontal = SugarDimens.Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs)
        ) {
            items(BadgeFilter.entries.toList()) { filter ->
                FilterChip(
                    selected = activeFilter == filter,
                    onClick = { activeFilter = filter },
                    label = { Text(filter.label, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }

        // Sort chips
        LazyRow(
            modifier = Modifier.padding(
                horizontal = SugarDimens.Spacing.md,
                vertical = SugarDimens.Spacing.xxs
            ),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs)
        ) {
            items(BadgeSort.entries.toList()) { sort ->
                FilterChip(
                    selected = activeSort == sort,
                    onClick = { activeSort = sort },
                    label = { Text(sort.label, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))

        // Badge grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SugarDimens.Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
        ) {
            items(filteredBadges, key = { it.id }) { badge ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(SugarDimens.Radius.sm))
                        .clickable {
                            selectedBadge = badge
                            onBadgeTapped(badge)
                        }
                        .padding(SugarDimens.Spacing.xxs),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BadgeRenderer(
                        badge = badge,
                        size = 56.dp,
                        showAnimation = false
                    )
                    Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxxs))
                    Text(
                        text = badge.name,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }

    // Detail bottom sheet
    selectedBadge?.let { badge ->
        ModalBottomSheet(
            onDismissRequest = { selectedBadge = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            BadgeDetailSheet(
                badge = badge,
                onDismiss = { selectedBadge = null }
            )
        }
    }
}

// ─── Badge Detail Sheet ──────────────────────────────────────────────────────

@Composable
fun BadgeDetailSheet(
    badge: Badge,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SugarDimens.Spacing.lg)
            .padding(bottom = SugarDimens.Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large badge render
        BadgeRenderer(
            badge = badge,
            size = 96.dp,
            showAnimation = true
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

        // Name
        Text(
            text = badge.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))

        // Rarity chip
        Surface(
            shape = RoundedCornerShape(SugarDimens.Radius.pill),
            color = badge.rarity.color.copy(alpha = 0.15f)
        ) {
            Text(
                text = badge.rarity.displayName,
                modifier = Modifier.padding(
                    horizontal = SugarDimens.Spacing.sm,
                    vertical = SugarDimens.Spacing.xxs
                ),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = badge.rarity.color
            )
        }

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))

        // Description
        Text(
            text = badge.description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = SugarDimens.Spacing.md)
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.lg))

        if (badge.isUnlocked) {
            // Unlock date
            badge.unlockedAt?.let { timestamp ->
                Text(
                    text = "Unlocked on ${formatTimestamp(timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
            }

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = badge.rarity.color
                )
            ) {
                Text("Showcase this badge")
            }
        } else {
            // Progress bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(badge.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = badge.rarity.color
                    )
                }
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))
                LinearProgressIndicator(
                    progress = { badge.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(SugarDimens.Radius.pill)),
                    color = badge.rarity.color,
                    trackColor = badge.rarity.color.copy(alpha = 0.15f)
                )
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
                Text(
                    text = "Keep collecting to unlock this badge!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun formatTimestamp(epochMillis: Long): String {
    val date = java.util.Date(epochMillis)
    val format = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
    return format.format(date)
}

// ─── Sample Data ─────────────────────────────────────────────────────────────

object SampleBadges {
    val allBadges: List<Badge> = listOf(
        Badge("first_candy", "First Candy", "Collect your first candy", BadgeRarity.COMMON, "🍬", true, 1f, 1700000000000L),
        Badge("sweet_tooth", "Sweet Tooth", "Eat 10 candies in one session", BadgeRarity.COMMON, "🦷", true, 1f, 1700100000000L),
        Badge("sugar_rush", "Sugar Rush", "Score a 50-combo streak", BadgeRarity.COMMON, "⚡", true, 1f, 1700200000000L),
        Badge("candy_fan", "Candy Fan", "Play 25 rounds", BadgeRarity.COMMON, "🎮", true, 1f, 1700300000000L),
        Badge("lollipop_lover", "Lollipop Lover", "Collect 100 lollipops", BadgeRarity.UNCOMMON, "🍭", true, 1f, 1700400000000L),
        Badge("gummy_guru", "Gummy Guru", "Master all gummy levels", BadgeRarity.UNCOMMON, "🐻", true, 1f, 1700500000000L),
        Badge("choco_champ", "Choco Champ", "Complete the chocolate world", BadgeRarity.UNCOMMON, "🍫", true, 1f, 1700600000000L),
        Badge("mint_master", "Mint Master", "Achieve mint perfection", BadgeRarity.UNCOMMON, "🌿", false, 0.7f),
        Badge("jelly_juggler", "Jelly Juggler", "Juggle 5 jellybeans at once", BadgeRarity.RARE, "🫘", true, 1f, 1700700000000L),
        Badge("caramel_king", "Caramel King", "Reach the caramel kingdom", BadgeRarity.RARE, "👑", true, 1f, 1700800000000L),
        Badge("bubble_burst", "Bubble Burst", "Pop 1000 bubblegum bubbles", BadgeRarity.RARE, "🫧", false, 0.45f),
        Badge("taffy_titan", "Taffy Titan", "Stretch taffy to the max", BadgeRarity.RARE, "💪", false, 0.3f),
        Badge("sugar_wizard", "Sugar Wizard", "Cast 50 sugar spells", BadgeRarity.EPIC, "🧙", true, 1f, 1700900000000L),
        Badge("candy_crusher", "Candy Crusher", "Crush 500 candies in a row", BadgeRarity.EPIC, "💥", false, 0.6f),
        Badge("rainbow_rider", "Rainbow Rider", "Ride the rainbow bridge", BadgeRarity.EPIC, "🌈", false, 0.2f),
        Badge("cotton_cloud", "Cotton Cloud", "Float on cotton candy clouds", BadgeRarity.EPIC, "☁️", false, 0.1f),
        Badge("golden_ticket", "Golden Ticket", "Find the golden candy ticket", BadgeRarity.LEGENDARY, "🎫", true, 1f, 1701000000000L),
        Badge("sugar_supreme", "Sugar Supreme", "Reach the highest sugar level", BadgeRarity.LEGENDARY, "🏆", false, 0.35f),
        Badge("candy_cosmos", "Candy Cosmos", "Discover the candy universe", BadgeRarity.LEGENDARY, "🌌", false, 0.05f),
        Badge("eternal_sweet", "Eternal Sweet", "Complete every challenge", BadgeRarity.LEGENDARY, "✨", false, 0.01f)
    )
}
