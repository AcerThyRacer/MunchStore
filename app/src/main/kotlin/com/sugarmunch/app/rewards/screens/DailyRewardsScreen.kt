package com.sugarmunch.app.rewards.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.rewards.*
import com.sugarmunch.app.shop.ShopManager
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyRewardsScreen(
    onBack: () -> Unit,
    onSugarPassClick: () -> Unit = {},
    onAchievementsClick: () -> Unit = {},
    onQuestsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val rewardsManager = remember { DailyRewardsManager.getInstance(context) }
    val shopManager = remember { ShopManager.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    val currentStreak by rewardsManager.currentStreak.collectAsState(initial = 0)
    val longestStreak by rewardsManager.longestStreak.collectAsState(initial = 0)
    val totalClaims by rewardsManager.totalClaims.collectAsState(initial = 0)
    val claimHistory by rewardsManager.claimHistory.collectAsState(initial = emptyList())
    val streakMilestones by rewardsManager.streakMilestones.collectAsState(initial = emptyList())
    val canClaimToday by rewardsManager.canClaimToday.collectAsState(initial = false)
    val streakStatus by rewardsManager.streakStatus.collectAsState(initial = StreakStatus.CLAIM_AVAILABLE(0))
    val nextReward by rewardsManager.nextReward.collectAsState(initial = DailyRewardsManager.REWARD_TIERS.first())
    
    var showClaimAnimation by remember { mutableStateOf(false) }
    var claimResult by remember { mutableStateOf<ClaimResult?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Daily Rewards",
                        color = colors.onSurface,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurface
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onSugarPassClick) {
                        Text("Sugar Pass", color = colors.primary)
                    }
                    TextButton(onClick = onQuestsClick) {
                        Text("Quests", color = colors.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedThemeBackground()
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colors.background.copy(alpha = 0.95f),
                                colors.background.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Streak Counter
                StreakCounter(
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    colors = colors
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Today's Reward
                TodayRewardCard(
                    streak = currentStreak,
                    reward = nextReward,
                    canClaim = canClaimToday,
                    streakStatus = streakStatus,
                    onClaim = {
                        scope.launch {
                            val result = rewardsManager.claimDailyReward()
                            claimResult = result
                            if (result is ClaimResult.SUCCESS) {
                                showClaimAnimation = true
                            }
                            showResultDialog = true
                        }
                    },
                    colors = colors
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Week Preview
                Text(
                    "This Week's Rewards",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                WeekPreviewRow(
                    startDay = currentStreak + 1,
                    currentStreak = currentStreak,
                    rewardsManager = rewardsManager,
                    colors = colors
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Streak Benefits
                StreakBenefitsCard(colors = colors)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Stats
                StreakStatsCard(
                    totalClaims = totalClaims,
                    longestStreak = longestStreak,
                    colors = colors
                )

                Spacer(modifier = Modifier.height(24.dp))

                ProgressionQuickLinksCard(
                    onAchievementsClick = onAchievementsClick,
                    onQuestsClick = onQuestsClick,
                    colors = colors
                )

                if (claimHistory.isNotEmpty() || streakMilestones.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    RecentRewardsCard(
                        claimHistory = claimHistory.takeLast(5).asReversed(),
                        streakMilestones = streakMilestones,
                        colors = colors
                    )
                }
            }
        }
    }

    // Claim Animation
    if (showClaimAnimation) {
        ClaimSuccessAnimation(
            onComplete = { showClaimAnimation = false }
        )
    }

    // Result Dialog
    if (showResultDialog) {
        ClaimResultDialog(
            result = claimResult,
            onDismiss = { showResultDialog = false },
            colors = colors
        )
    }
}

@Composable
private fun StreakCounter(
    currentStreak: Int,
    longestStreak: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val infiniteTransition = rememberInfiniteTransition(label = "streak_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.primary.copy(alpha = 0.15f)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, colors.primary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Current Streak",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🔥",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "$currentStreak",
                    style = MaterialTheme.typography.displayLarge,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Day${if (currentStreak == 1) "" else "s"} in a row!",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface
            )
            
            if (longestStreak > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Best: $longestStreak days",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun TodayRewardCard(
    streak: Int,
    reward: DayReward,
    canClaim: Boolean,
    streakStatus: StreakStatus,
    onClaim: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val rewardColor = when (reward.type) {
        RewardType.COMMON -> Color(0xFFCD7F32)
        RewardType.UNCOMMON -> Color(0xFFC0C0C0)
        RewardType.RARE -> Color(0xFFFFD700)
        RewardType.EPIC -> Color(0xFF9370DB)
        RewardType.LEGENDARY -> Color(0xFFFF1493)
        RewardType.ULTIMATE -> Color(0xFF00CED1)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Day badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = rewardColor.copy(alpha = 0.2f)
            ) {
                Text(
                    "Day ${streak + 1}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = rewardColor,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Reward icon based on type
            Text(
                when (reward.type) {
                    RewardType.COMMON -> "🍬"
                    RewardType.UNCOMMON -> "🍭"
                    RewardType.RARE -> "🍫"
                    RewardType.EPIC -> "🎁"
                    RewardType.LEGENDARY -> "👑"
                    RewardType.ULTIMATE -> "💎"
                },
                style = MaterialTheme.typography.displayLarge
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                reward.title,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Reward details
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RewardPill(
                    icon = "🍬",
                    value = "+${reward.sugarPoints}",
                    colors = colors
                )
                RewardPill(
                    icon = "⭐",
                    value = "+${reward.xp} XP",
                    colors = colors
                )
            }
            
            if (reward.isMilestone) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.2f)
                ) {
                    Text(
                        "⭐ MILESTONE REWARD - 2x MULTIPLIER!",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFB8860B)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Claim button
            when (streakStatus) {
                is StreakStatus.CLAIM_AVAILABLE -> {
                    Button(
                        onClick = onClaim,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary
                        )
                    ) {
                        Text(
                            "Claim Daily Reward!",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                is StreakStatus.ALREADY_CLAIMED -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Claimed! Come back tomorrow",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
                is StreakStatus.AT_RISK -> {
                    Column {
                        Button(
                            onClick = onClaim,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9800)
                            )
                        ) {
                            Text(
                                "Claim Now - Streak at Risk!",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${streakStatus.hoursRemaining} hours left to keep your streak!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF9800),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardPill(
    icon: String,
    value: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WeekPreviewRow(
    startDay: Int,
    currentStreak: Int,
    rewardsManager: DailyRewardsManager,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val weekRewards = remember(startDay) {
        rewardsManager.getWeekPreview(startDay)
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(weekRewards) { reward ->
            val isToday = reward.day == startDay
            val dayColor = when (reward.type) {
                RewardType.COMMON -> Color(0xFFCD7F32)
                RewardType.UNCOMMON -> Color(0xFFC0C0C0)
                RewardType.RARE -> Color(0xFFFFD700)
                RewardType.EPIC -> Color(0xFF9370DB)
                RewardType.LEGENDARY -> Color(0xFFFF1493)
                RewardType.ULTIMATE -> Color(0xFF00CED1)
            }

            Card(
                modifier = Modifier.width(80.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isToday) 
                        colors.primary.copy(alpha = 0.2f) 
                    else 
                        colors.surface.copy(alpha = 0.7f)
                ),
                border = if (isToday) {
                    androidx.compose.foundation.BorderStroke(2.dp, colors.primary)
                } else if (reward.isMilestone) {
                    androidx.compose.foundation.BorderStroke(1.dp, dayColor.copy(alpha = 0.5f))
                } else null
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Day ${reward.day}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        when (reward.type) {
                            RewardType.COMMON -> "🍬"
                            RewardType.UNCOMMON -> "🍭"
                            RewardType.RARE -> "🍫"
                            RewardType.EPIC -> "🎁"
                            RewardType.LEGENDARY -> "👑"
                            RewardType.ULTIMATE -> "💎"
                        },
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${reward.sugarPoints}",
                        style = MaterialTheme.typography.bodySmall,
                        color = dayColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakBenefitsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Streak Benefits",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            BenefitRow(
                days = "7 days",
                benefit = "1.25x multiplier on all rewards",
                icon = "🔥",
                colors = colors
            )
            BenefitRow(
                days = "14 days",
                benefit = "1.5x multiplier + Epic rewards",
                icon = "⭐",
                colors = colors
            )
            BenefitRow(
                days = "30 days",
                benefit = "2x multiplier + Ultimate reward",
                icon = "👑",
                colors = colors
            )
        }
    }
}

@Composable
private fun BenefitRow(
    days: String,
    benefit: String,
    icon: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = colors.primary.copy(alpha = 0.2f)
        ) {
            Text(
                icon,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                days,
                style = MaterialTheme.typography.bodySmall,
                color = colors.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                benefit,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface
            )
        }
    }
}

@Composable
private fun StreakStatsCard(
    totalClaims: Int,
    longestStreak: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = "$totalClaims",
                label = "Total Claims",
                icon = "📅",
                colors = colors
            )
            StatItem(
                value = "$longestStreak",
                label = "Best Streak",
                icon = "🏆",
                colors = colors
            )
        }
    }
}

@Composable
private fun ProgressionQuickLinksCard(
    onAchievementsClick: () -> Unit,
    onQuestsClick: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Keep The Loop Going",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onAchievementsClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Achievements")
                }
                OutlinedButton(
                    onClick = onQuestsClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Checklist, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quests")
                }
            }
        }
    }
}

@Composable
private fun RecentRewardsCard(
    claimHistory: List<ClaimHistoryEntry>,
    streakMilestones: List<Int>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Recent Reward History",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )

            if (streakMilestones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Milestones: ${streakMilestones.joinToString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            claimHistory.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Day ${entry.dayNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${entry.sugarPoints} Sugar Points • ${entry.xp} XP",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.65f)
                        )
                    }
                    Text(
                        Date(entry.date).toString().take(10),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ClaimSuccessAnimation(
    onComplete: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onComplete() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "🎉",
                modifier = Modifier.scale(scale),
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Claimed!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        onComplete()
    }
}

@Composable
private fun ClaimResultDialog(
    result: ClaimResult?,
    onDismiss: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    when (result) {
        is ClaimResult.SUCCESS -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎉", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reward Claimed!")
                    }
                },
                text = {
                    Column {
                        Text(
                            "Day ${result.day} - ${result.reward.title}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🍬", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "+${result.sugarPoints} Sugar Points",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (result.multiplier > 1f) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "(${result.multiplier}x)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "+${result.xp} XP",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Awesome!")
                    }
                },
                containerColor = colors.surface
            )
        }
        ClaimResult.ALREADY_CLAIMED -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Already Claimed") },
                text = { Text("You've already claimed today's reward. Come back tomorrow!") },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("OK")
                    }
                },
                containerColor = colors.surface
            )
        }
        else -> {}
    }
}
