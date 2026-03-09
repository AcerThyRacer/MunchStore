package com.sugarmunch.app.pass

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Sugar Pass - Battle Pass System
 * 
 * Features:
 * - Free and Premium tracks
 * - 50 tiers of rewards
 * - Seasonal progression
 * - XP earning system
 * - Exclusive rewards
 */
@Composable
fun SugarPassScreen(
    onClaimReward: (PassReward) -> Unit,
    onPurchasePass: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    // Mock pass data
    val currentSeason = remember { getCurrentSeason() }
    val userProgress = remember { getUserPassProgress() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sugar Pass") },
                subtitle = { Text("Season ${currentSeason.number}: ${currentSeason.name}") },
                actions = {
                    if (!userProgress.hasPremium) {
                        FilledTonalIconButton(
                            onClick = onPurchasePass
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Season header
            item {
                SeasonHeader(
                    season = currentSeason,
                    progress = userProgress
                )
            }
            
            // Progress bar
            item {
                PassProgress(
                    currentTier = userProgress.currentTier,
                    maxTier = currentSeason.maxTier,
                    currentXP = userProgress.currentXP,
                    xpToNext = userProgress.xpToNextTier
                )
            }
            
            // Free vs Premium toggle
            item {
                PremiumBanner(hasPremium = userProgress.hasPremium)
            }
            
            // Rewards track
            item {
                Text(
                    text = "Rewards Track",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Rewards list
            items(currentSeason.rewards) { reward ->
                RewardCard(
                    reward = reward,
                    isUnlocked = userProgress.currentTier >= reward.tier,
                    isClaimed = userProgress.claimedRewards.contains(reward.id),
                    onClaim = { onClaimReward(reward) }
                )
            }
            
            // XP earning tips
            item {
                XPTipsCard()
            }
        }
    }
}

@Composable
private fun SeasonHeader(
    season: SugarPassSeason,
    progress: UserPassProgress
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // Background gradient
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF69B4),
                            Color(0xFF9370DB),
                            Color(0xFF20B2AA)
                        )
                    )
                )
            }
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = season.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = season.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Days remaining
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${season.daysRemaining} days remaining",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Season badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "S${season.number}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PassProgress(
    currentTier: Int,
    maxTier: Int,
    currentXP: Int,
    xpToNext: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tier $currentTier / $maxTier",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$currentXP / $xpToNext XP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = currentTier.toFloat() / maxTier,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PremiumBanner(hasPremium: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasPremium) {
                Brush.linearGradient(
                    colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                )
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (hasPremium) Icons.Default.Star else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (hasPremium) Color.Black else MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasPremium) "Premium Pass Active" else "Unlock Premium Rewards",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (hasPremium) Color.Black else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            if (!hasPremium) {
                Button(onClick = { /* Purchase */ }) {
                    Text("Upgrade")
                }
            }
        }
    }
}

@Composable
private fun RewardCard(
    reward: PassReward,
    isUnlocked: Boolean,
    isClaimed: Boolean,
    onClaim: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !isUnlocked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                isClaimed -> MaterialTheme.colorScheme.secondaryContainer
                reward.isPremium -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (reward.isPremium && !isClaimed) {
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                )
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Reward icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isUnlocked) {
                                if (reward.isPremium) {
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF20B2AA), Color(0xFF008B8B))
                                    )
                                }
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (reward.type) {
                            RewardType.SUGAR_POINTS -> Icons.Default.MonetizationOn
                            RewardType.THEME -> Icons.Default.Palette
                            RewardType.EFFECT -> Icons.Default.BlurOn
                            RewardType.BADGE -> Icons.Default.EmojiEvents
                            RewardType.BOOST -> Icons.Default.TrendingUp
                        },
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (isUnlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Reward info
                Column {
                    Text(
                        text = reward.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tier ${reward.tier}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (reward.isPremium) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color(0xFFFFD700)
                            )
                        }
                    }
                }
            }
            
            // Claim button
            when {
                !isUnlocked -> {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                isClaimed -> {
                    AssistChip(
                        onClick = { },
                        label = { Text("Claimed") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                else -> {
                    Button(onClick = onClaim) {
                        Text("Claim")
                    }
                }
            }
        }
    }
}

@Composable
private fun XPTipsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "How to Earn XP",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            XPTipItem("Complete daily challenges", 50)
            XPTipItem("Download new apps", 25)
            XPTipItem("Create custom themes", 100)
            XPTipItem("Share with friends", 75)
            XPTipItem("Daily login streak", 20)
        }
    }
}

@Composable
private fun XPTipItem(action: String, xp: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = action,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
        Text(
            text = "+$xp XP",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ═════════════════════════════════════════════════════════════
// DATA MODELS
// ═════════════════════════════════════════════════════════════

data class SugarPassSeason(
    val number: Int,
    val name: String,
    val description: String,
    val maxTier: Int,
    val daysRemaining: Int,
    val rewards: List<PassReward>
)

data class PassReward(
    val id: String,
    val name: String,
    val tier: Int,
    val type: RewardType,
    val isPremium: Boolean,
    val value: Int
)

enum class RewardType {
    SUGAR_POINTS, THEME, EFFECT, BADGE, BOOST
}

data class UserPassProgress(
    val currentTier: Int,
    val currentXP: Int,
    val xpToNextTier: Int,
    val hasPremium: Boolean,
    val claimedRewards: Set<String>
)

private fun getCurrentSeason(): SugarPassSeason {
    return SugarPassSeason(
        number = 3,
        name = "Candy Wonderland",
        description = "Explore a sweet world of exclusive rewards!",
        maxTier = 50,
        daysRemaining = 45,
        rewards = buildList {
            for (tier in 1..10) {
                add(
                    PassReward(
                        id = "reward_$tier",
                        name = when (tier % 5) {
                            0 -> "Legendary Theme"
                            1 -> "Sugar Points"
                            2 -> "Rare Effect"
                            3 -> "Exclusive Badge"
                            else -> "XP Boost"
                        },
                        tier = tier,
                        type = when (tier % 5) {
                            0 -> RewardType.THEME
                            1 -> RewardType.SUGAR_POINTS
                            2 -> RewardType.EFFECT
                            3 -> RewardType.BADGE
                            else -> RewardType.BOOST
                        },
                        isPremium = tier % 3 == 0,
                        value = tier * 100
                    )
                )
            }
        }
    )
}

private fun getUserPassProgress(): UserPassProgress {
    return UserPassProgress(
        currentTier = 7,
        currentXP = 3500,
        xpToNextTier = 5000,
        hasPremium = false,
        claimedRewards = setOf("reward_1", "reward_2", "reward_3")
    )
}
