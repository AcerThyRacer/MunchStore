package com.sugarmunch.app.ui.seasonal

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sin

// ─── Reaction Data ──────────────────────────────────────────────────────────────

data class ThemeReaction(
    val emoji: String,
    val label: String,
    val color: Color
)

val availableReactions = listOf(
    ThemeReaction("🔥", "Fire", Color(0xFFFF6B35)),
    ThemeReaction("💯", "Perfect", Color(0xFFFF1493)),
    ThemeReaction("👀", "Nice", Color(0xFF2196F3)),
    ThemeReaction("✨", "Sparkle", Color(0xFFFFD700)),
    ThemeReaction("❤️", "Love", Color(0xFFFF0000)),
    ThemeReaction("🤩", "Wow", Color(0xFFFF69B4)),
    ThemeReaction("💎", "Premium", Color(0xFF00BCD4)),
    ThemeReaction("🌈", "Rainbow", Color(0xFF9C27B0))
)

// ─── Theme Reaction Bar ─────────────────────────────────────────────────────────

@Composable
fun ThemeReactionBar(
    onReact: (ThemeReaction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = SugarDimens.Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs)
    ) {
        items(availableReactions) { reaction ->
            ReactionButton(reaction = reaction, onTap = { onReact(reaction) })
        }
    }
}

@Composable
private fun ReactionButton(
    reaction: ThemeReaction,
    onTap: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    var showBurst by remember { mutableStateOf(false) }

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(SugarDimens.TouchTarget.standard)
                .scale(scale.value)
                .clip(CircleShape)
                .background(reaction.color.copy(alpha = 0.12f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    scope.launch {
                        showBurst = true
                        scale.animateTo(1.4f, tween(80))
                        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        delay(300)
                        showBurst = false
                    }
                    onTap()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(text = reaction.emoji, fontSize = 22.sp)
        }

        // Particle burst
        if (showBurst) {
            Canvas(modifier = Modifier.size(SugarDimens.TouchTarget.large)) {
                val burstRadius = size.minDimension / 2
                for (i in 0 until 8) {
                    val angle = i * 45f * (Math.PI.toFloat() / 180f)
                    val px = center.x + kotlin.math.cos(angle) * burstRadius * 0.7f
                    val py = center.y + kotlin.math.sin(angle) * burstRadius * 0.7f
                    drawCircle(
                        color = reaction.color.copy(alpha = 0.6f),
                        radius = 3f,
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}

// ─── Floating Reaction ──────────────────────────────────────────────────────────

@Composable
fun FloatingReaction(
    reaction: ThemeReaction,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val yOffset = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    val scaleAnim = remember { Animatable(0.3f) }
    val swayPhase = remember { (Math.random() * Math.PI * 2).toFloat() }

    LaunchedEffect(Unit) {
        launch { scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
        launch { yOffset.animateTo(-300f, tween(2000, easing = LinearEasing)) }
        launch { delay(1400); alpha.animateTo(0f, tween(600)) }
        delay(2000)
        onComplete()
    }

    val sway = sin(swayPhase + yOffset.value * 0.02f) * 20f

    Text(
        text = reaction.emoji,
        fontSize = 28.sp,
        modifier = modifier
            .offset {
                IntOffset(sway.roundToInt(), yOffset.value.roundToInt())
            }
            .graphicsLayer {
                this.alpha = alpha.value
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
            }
    )
}

// ─── Theme Battle Card ──────────────────────────────────────────────────────────

@Composable
fun ThemeBattleCard(
    theme1Name: String,
    theme1Colors: List<Color>,
    theme1Votes: Int,
    theme2Name: String,
    theme2Colors: List<Color>,
    theme2Votes: Int,
    onVoteTheme1: () -> Unit,
    onVoteTheme2: () -> Unit,
    hasVoted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val totalVotes = (theme1Votes + theme2Votes).coerceAtLeast(1)
    val pct1 = theme1Votes.toFloat() / totalVotes
    val pct2 = theme2Votes.toFloat() / totalVotes
    val animatedPct1 by animateFloatAsState(
        targetValue = if (hasVoted) pct1 else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "pct1"
    )
    val animatedPct2 by animateFloatAsState(
        targetValue = if (hasVoted) pct2 else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "pct2"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "vs_fire")
    val fireScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fire_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SugarDimens.Spacing.md),
        shape = RoundedCornerShape(SugarDimens.Radius.lg),
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.medium)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Theme 1 side
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            Brush.verticalGradient(
                                theme1Colors.ifEmpty { listOf(Color.Gray, Color.DarkGray) }
                            )
                        )
                        .padding(SugarDimens.Spacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = theme1Name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(SugarDimens.Spacing.lg))

                        if (hasVoted) {
                            VoteBar(
                                percentage = animatedPct1,
                                color = Color.White.copy(alpha = 0.9f),
                                votes = theme1Votes
                            )
                        } else {
                            TextButton(onClick = onVoteTheme1) {
                                Text("VOTE", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Theme 2 side
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            Brush.verticalGradient(
                                theme2Colors.ifEmpty { listOf(Color.Gray, Color.DarkGray) }
                            )
                        )
                        .padding(SugarDimens.Spacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = theme2Name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(SugarDimens.Spacing.lg))

                        if (hasVoted) {
                            VoteBar(
                                percentage = animatedPct2,
                                color = Color.White.copy(alpha = 0.9f),
                                votes = theme2Votes
                            )
                        } else {
                            TextButton(onClick = onVoteTheme2) {
                                Text("VOTE", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // VS badge in center
            Box(
                modifier = Modifier.align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .scale(fireScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF6F00),
                                    Color(0xFFFF1744),
                                    Color(0xFFD50000)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun VoteBar(
    percentage: Float,
    color: Color,
    votes: Int
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${(percentage * 100).roundToInt()}%",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(8.dp)
        ) {
            drawRoundRect(
                color = color.copy(alpha = 0.25f),
                cornerRadius = CornerRadius(4.dp.toPx()),
                size = size
            )
            drawRoundRect(
                color = color,
                cornerRadius = CornerRadius(4.dp.toPx()),
                size = Size(size.width * percentage, size.height)
            )
        }
        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))
        Text(
            text = "$votes votes",
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.8f)
        )
    }
}

// ─── Theme Battle Screen ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeBattleScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var hasVoted by remember { mutableStateOf(false) }
    var votes1 by remember { mutableIntStateOf(234) }
    var votes2 by remember { mutableIntStateOf(189) }
    var timerSeconds by remember { mutableIntStateOf(14400) } // 4 hours

    LaunchedEffect(Unit) {
        while (timerSeconds > 0) {
            delay(1000)
            timerSeconds--
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Theme Battles",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = SugarDimens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.lg)
        ) {
            // Timer
            item {
                val hours = timerSeconds / 3600
                val minutes = (timerSeconds % 3600) / 60
                val seconds = timerSeconds % 60

                Text(
                    text = "⏱ Battle ends in ${"%02d:%02d:%02d".format(hours, minutes, seconds)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SugarDimens.Spacing.md),
                    textAlign = TextAlign.Center
                )
            }

            // Current featured battle
            item {
                Text(
                    text = "🏆 Current Battle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = SugarDimens.Spacing.md)
                )
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
                ThemeBattleCard(
                    theme1Name = "Neon Candy",
                    theme1Colors = listOf(Color(0xFFFF00FF), Color(0xFF7B1FA2)),
                    theme1Votes = votes1,
                    theme2Name = "Ocean Breeze",
                    theme2Colors = listOf(Color(0xFF00BCD4), Color(0xFF006064)),
                    theme2Votes = votes2,
                    onVoteTheme1 = { votes1++; hasVoted = true },
                    onVoteTheme2 = { votes2++; hasVoted = true },
                    hasVoted = hasVoted
                )
            }

            // Past battles
            item {
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
                Text(
                    text = "📊 Past Battles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = SugarDimens.Spacing.md)
                )
            }

            items(pastBattles) { battle ->
                ThemeBattleCard(
                    theme1Name = battle.name1,
                    theme1Colors = battle.colors1,
                    theme1Votes = battle.votes1,
                    theme2Name = battle.name2,
                    theme2Colors = battle.colors2,
                    theme2Votes = battle.votes2,
                    onVoteTheme1 = {},
                    onVoteTheme2 = {},
                    hasVoted = true
                )
            }

            // Submit theme prompt
            item {
                SubmitThemeCard(modifier = Modifier.padding(horizontal = SugarDimens.Spacing.md))
            }
        }
    }
}

private data class PastBattle(
    val name1: String,
    val colors1: List<Color>,
    val votes1: Int,
    val name2: String,
    val colors2: List<Color>,
    val votes2: Int
)

private val pastBattles = listOf(
    PastBattle(
        "Sugar Rush", listOf(Color(0xFFFF69B4), Color(0xFFFF1493)), 412,
        "Dark Cocoa", listOf(Color(0xFF3E2723), Color(0xFF4E342E)), 387
    ),
    PastBattle(
        "Citrus Pop", listOf(Color(0xFFFFEB3B), Color(0xFFFF9800)), 298,
        "Berry Frost", listOf(Color(0xFF9C27B0), Color(0xFF4A148C)), 315
    )
)

@Composable
private fun SubmitThemeCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SugarDimens.Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🎨 Submit Your Theme",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))
                Text(
                    text = "Create a custom theme and enter the next battle!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(SugarDimens.Spacing.sm))
            Box(
                modifier = Modifier
                    .size(SugarDimens.TouchTarget.standard)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Submit theme",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
