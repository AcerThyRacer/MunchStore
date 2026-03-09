package com.sugarmunch.app.ui.profile

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlin.math.cos
import kotlin.math.sin

// ─── Avatar Frames ───────────────────────────────────────────────────────────

enum class AvatarFrame(val displayName: String, val primaryColor: Color, val secondaryColor: Color) {
    NONE("None", Color.Transparent, Color.Transparent),
    FIRE("Fire", Color(0xFFFF6B35), Color(0xFFFF0000)),
    ICE("Ice", Color(0xFF00BFFF), Color(0xFFE0FFFF)),
    NEON_PINK("Neon Pink", Color(0xFFFF1493), Color(0xFFFF69B4)),
    NEON_GREEN("Neon Green", Color(0xFF39FF14), Color(0xFF00FF41)),
    RAINBOW("Rainbow", Color(0xFFFF0000), Color(0xFF0000FF)),
    CANDY("Candy", Color(0xFFFFB6C1), Color(0xFFFF69B4)),
    GALAXY("Galaxy", Color(0xFF4B0082), Color(0xFF00CED1)),
    GOLD("Gold", Color(0xFFFFD700), Color(0xFFFFA500)),
    SILVER("Silver", Color(0xFFC0C0C0), Color(0xFFE8E8E8)),
    DIAMOND("Diamond", Color(0xFFB9F2FF), Color(0xFF87CEEB)),
    CHERRY_BLOSSOM("Cherry Blossom", Color(0xFFFFB7C5), Color(0xFFFF69B4)),
    LIGHTNING("Lightning", Color(0xFFFFFF00), Color(0xFFFFD700)),
    OCEAN("Ocean", Color(0xFF006994), Color(0xFF00CED1)),
    FOREST("Forest", Color(0xFF228B22), Color(0xFF90EE90)),
    SUNSET("Sunset", Color(0xFFFF4500), Color(0xFFFF8C00)),
    MIDNIGHT("Midnight", Color(0xFF191970), Color(0xFF483D8B)),
    HOLOGRAPHIC("Holographic", Color(0xFFE0C3FC), Color(0xFF8EC5FC)),
    CRYSTAL("Crystal", Color(0xFFE8E8E8), Color(0xFFFFFFFF)),
    TOXIC("Toxic", Color(0xFF00FF00), Color(0xFF32CD32))
}

// ─── Profile Configuration ───────────────────────────────────────────────────

enum class ProfileBackground { GRADIENT, PATTERN, SOLID, ANIMATED }

data class ProfileCardConfig(
    val displayName: String = "Sugar Lover",
    val bio: String = "",
    val avatarFrame: AvatarFrame = AvatarFrame.NONE,
    val backgroundStyle: ProfileBackground = ProfileBackground.GRADIENT,
    val backgroundColor1: Color = Color(0xFFFFB6C1),
    val backgroundColor2: Color = Color(0xFFFF69B4),
    val showcaseBadgeIds: List<String> = emptyList(),
    val level: Int = 1,
    val sugarPoints: Int = 0,
    val joinDate: String = ""
)

// ─── Animated Avatar Frame ───────────────────────────────────────────────────

@Composable
fun AnimatedAvatarFrame(
    frame: AvatarFrame,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (frame == AvatarFrame.NONE) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) { content() }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "frame_${frame.name}")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (frame) {
                    AvatarFrame.FIRE -> 1200
                    AvatarFrame.ICE -> 4000
                    AvatarFrame.RAINBOW -> 2000
                    AvatarFrame.HOLOGRAPHIC -> 3000
                    AvatarFrame.CANDY -> 2500
                    else -> 3000
                },
                easing = LinearEasing
            )
        ),
        label = "rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val primary = frame.primaryColor
    val secondary = frame.secondaryColor

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = size.minDimension * 0.06f
            val radius = (size.minDimension - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            when (frame) {
                AvatarFrame.FIRE -> {
                    val flicker = pulse * 0.3f + 0.7f
                    for (i in 0..2) {
                        val r = radius + (i * 2f)
                        drawCircle(
                            color = primary.copy(alpha = (0.6f - i * 0.15f) * flicker),
                            radius = r,
                            center = center,
                            style = Stroke(width = strokeWidth - i)
                        )
                    }
                    rotate(rotation * 0.5f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(primary, secondary, primary.copy(alpha = 0.2f), primary)
                            ),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                AvatarFrame.ICE -> {
                    rotate(rotation * 0.3f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(primary, secondary, Color.White, primary)
                            ),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    // Sparkle dots
                    for (i in 0..5) {
                        val angle = Math.toRadians((rotation + i * 60.0))
                        val sparkleAlpha = ((sin(angle * 3).toFloat() + 1f) / 2f) * pulse
                        drawCircle(
                            color = Color.White.copy(alpha = sparkleAlpha.coerceIn(0f, 1f)),
                            radius = 3f,
                            center = Offset(
                                center.x + (radius * cos(angle)).toFloat(),
                                center.y + (radius * sin(angle)).toFloat()
                            )
                        )
                    }
                }

                AvatarFrame.RAINBOW -> {
                    rotate(rotation) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color.Red, Color(0xFFFF8000), Color.Yellow,
                                    Color.Green, Color.Cyan, Color.Blue,
                                    Color.Magenta, Color.Red
                                )
                            ),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                AvatarFrame.CANDY -> {
                    rotate(rotation * 0.6f) {
                        val stripeColors = listOf(
                            Color(0xFFFFB6C1), Color.White,
                            Color(0xFFFF69B4), Color.White,
                            Color(0xFFFFB6C1), Color.White,
                            Color(0xFFFF69B4), Color.White
                        )
                        drawArc(
                            brush = Brush.sweepGradient(colors = stripeColors),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                AvatarFrame.GOLD, AvatarFrame.SILVER, AvatarFrame.DIAMOND -> {
                    val shimmerOffset = rotation / 360f
                    val shimmerLight = if (frame == AvatarFrame.DIAMOND) Color.White
                    else secondary
                    rotate(rotation * 0.8f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    primary, shimmerLight, primary,
                                    primary.copy(alpha = 0.8f), shimmerLight, primary
                                )
                            ),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    // Metallic shimmer highlight
                    val highlightAngle = Math.toRadians((shimmerOffset * 360.0))
                    drawCircle(
                        color = shimmerLight.copy(alpha = 0.5f * pulse),
                        radius = strokeWidth * 0.8f,
                        center = Offset(
                            center.x + (radius * cos(highlightAngle)).toFloat(),
                            center.y + (radius * sin(highlightAngle)).toFloat()
                        )
                    )
                }

                AvatarFrame.HOLOGRAPHIC -> {
                    rotate(rotation * 0.4f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFFE0C3FC), Color(0xFF8EC5FC),
                                    Color(0xFFA8EDEA), Color(0xFFFED6E3),
                                    Color(0xFFD4FC79), Color(0xFFE0C3FC)
                                )
                            ),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    // Iridescent shimmer sweep
                    val sweepAngle = Math.toRadians((rotation * 1.5).toDouble())
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f * pulse),
                        radius = strokeWidth,
                        center = Offset(
                            center.x + (radius * cos(sweepAngle)).toFloat(),
                            center.y + (radius * sin(sweepAngle)).toFloat()
                        )
                    )
                }

                else -> {
                    // Default animated gradient ring for all other frames
                    rotate(rotation * 0.5f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    primary, secondary,
                                    primary.copy(alpha = 0.4f), secondary, primary
                                )
                            ),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    drawCircle(
                        color = primary.copy(alpha = 0.15f * pulse),
                        radius = radius + strokeWidth,
                        center = center
                    )
                }
            }
        }
        content()
    }
}

// ─── Profile Card ────────────────────────────────────────────────────────────

@Composable
fun ProfileCard(
    config: ProfileCardConfig,
    modifier: Modifier = Modifier
) {
    val backgroundBrush = when (config.backgroundStyle) {
        ProfileBackground.GRADIENT -> Brush.linearGradient(
            colors = listOf(config.backgroundColor1, config.backgroundColor2)
        )
        ProfileBackground.PATTERN -> Brush.linearGradient(
            colors = listOf(
                config.backgroundColor1,
                config.backgroundColor2.copy(alpha = 0.7f),
                config.backgroundColor1,
                config.backgroundColor2.copy(alpha = 0.5f)
            )
        )
        ProfileBackground.SOLID -> Brush.linearGradient(
            colors = listOf(config.backgroundColor1, config.backgroundColor1)
        )
        ProfileBackground.ANIMATED -> Brush.linearGradient(
            colors = listOf(config.backgroundColor1, config.backgroundColor2)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg),
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.medium)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush)
                .padding(SugarDimens.Spacing.md)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with animated frame
                AnimatedAvatarFrame(
                    frame = config.avatarFrame,
                    modifier = Modifier.size(76.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                        color = Color.White.copy(alpha = 0.3f),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(SugarDimens.Spacing.xs),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))

                // Name + Level
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = config.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(SugarDimens.Spacing.xs))
                    Surface(
                        shape = RoundedCornerShape(SugarDimens.Radius.pill),
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "Lv.${config.level}",
                            modifier = Modifier.padding(
                                horizontal = SugarDimens.Spacing.xs,
                                vertical = SugarDimens.Spacing.xxxs
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Sugar Points
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = SugarDimens.Spacing.xxs)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(SugarDimens.IconSize.sm)
                    )
                    Spacer(modifier = Modifier.width(SugarDimens.Spacing.xxs))
                    Text(
                        text = "${config.sugarPoints} SP",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                // Bio
                if (config.bio.isNotBlank()) {
                    Text(
                        text = config.bio,
                        modifier = Modifier.padding(
                            top = SugarDimens.Spacing.xs,
                            start = SugarDimens.Spacing.md,
                            end = SugarDimens.Spacing.md
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Badge showcase row
                if (config.showcaseBadgeIds.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            SugarDimens.Spacing.xs,
                            Alignment.CenterHorizontally
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        config.showcaseBadgeIds.take(5).forEach { badgeId ->
                            val badge = SampleBadges.allBadges.find { it.id == badgeId }
                            if (badge != null) {
                                BadgeRenderer(
                                    badge = badge,
                                    size = 36.dp,
                                    showAnimation = false
                                )
                            }
                        }
                    }
                }

                // Join date
                if (config.joinDate.isNotBlank()) {
                    Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
                    Text(
                        text = "Joined ${config.joinDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// ─── Profile Card Designer ───────────────────────────────────────────────────

private val profileColorPalette = listOf(
    Color(0xFFFFB6C1), Color(0xFFFF69B4), Color(0xFFFF1493),
    Color(0xFFFF6B35), Color(0xFFFF4500), Color(0xFFFF0000),
    Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFFF00),
    Color(0xFF39FF14), Color(0xFF4CAF50), Color(0xFF228B22),
    Color(0xFF00BFFF), Color(0xFF2196F3), Color(0xFF0000FF),
    Color(0xFF9C27B0), Color(0xFF4B0082), Color(0xFF191970),
    Color(0xFF000000), Color(0xFFFFFFFF)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileCardDesigner(
    config: ProfileCardConfig,
    onConfigChanged: (ProfileCardConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBadgeEditor by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(SugarDimens.Spacing.md)
    ) {
        // Live preview
        Text(
            text = "Preview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
        ProfileCard(config = config)

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.lg))

        // Name & Bio section
        Text(
            text = "Name & Bio",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
        OutlinedTextField(
            value = config.displayName,
            onValueChange = { onConfigChanged(config.copy(displayName = it.take(24))) },
            label = { Text("Display Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
        OutlinedTextField(
            value = config.bio,
            onValueChange = { onConfigChanged(config.copy(bio = it.take(120))) },
            label = { Text("Bio") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.lg))

        // Avatar frame grid
        Text(
            text = "Avatar Frame",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs)
        ) {
            items(AvatarFrame.entries.toList()) { frame ->
                val isSelected = config.avatarFrame == frame
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(SugarDimens.Radius.sm))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .then(
                            if (isSelected) Modifier.border(
                                2.dp,
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(SugarDimens.Radius.sm)
                            ) else Modifier
                        )
                        .clickable { onConfigChanged(config.copy(avatarFrame = frame)) },
                    contentAlignment = Alignment.Center
                ) {
                    if (frame == AvatarFrame.NONE) {
                        Text(
                            text = "✕",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        AnimatedAvatarFrame(
                            frame = frame,
                            modifier = Modifier
                                .size(36.dp)
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(frame.primaryColor, frame.secondaryColor)
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.lg))

        // Background style selector
        Text(
            text = "Background Style",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs)
        ) {
            ProfileBackground.entries.forEach { style ->
                FilterChip(
                    selected = config.backgroundStyle == style,
                    onClick = { onConfigChanged(config.copy(backgroundStyle = style)) },
                    label = { Text(style.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.lg))

        // Color pickers
        Text(
            text = "Background Colors",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))

        Text(
            text = "Primary Color",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))
        ColorPickerRow(
            selectedColor = config.backgroundColor1,
            onColorSelected = { onConfigChanged(config.copy(backgroundColor1 = it)) }
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))

        Text(
            text = "Secondary Color",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))
        ColorPickerRow(
            selectedColor = config.backgroundColor2,
            onColorSelected = { onConfigChanged(config.copy(backgroundColor2 = it)) }
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.lg))

        // Badge showcase editor
        Text(
            text = "Badge Showcase",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tap badges to add/remove from showcase (max 5)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs)
        ) {
            SampleBadges.allBadges.filter { it.isUnlocked }.forEach { badge ->
                val isShowcased = config.showcaseBadgeIds.contains(badge.id)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(SugarDimens.Radius.sm))
                        .background(
                            if (isShowcased) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        )
                        .then(
                            if (isShowcased) Modifier.border(
                                2.dp,
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(SugarDimens.Radius.sm)
                            ) else Modifier
                        )
                        .clickable {
                            val updated = if (isShowcased) {
                                config.showcaseBadgeIds - badge.id
                            } else if (config.showcaseBadgeIds.size < 5) {
                                config.showcaseBadgeIds + badge.id
                            } else {
                                config.showcaseBadgeIds
                            }
                            onConfigChanged(config.copy(showcaseBadgeIds = updated))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    BadgeRenderer(
                        badge = badge,
                        size = 40.dp,
                        showAnimation = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxl))
    }
}

// ─── Color Picker Row ────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPickerRow(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xxs),
        verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xxs)
    ) {
        profileColorPalette.forEach { color ->
            val isSelected = color == selectedColor
            val borderAlpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0f,
                label = "border"
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                        else Modifier
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}
