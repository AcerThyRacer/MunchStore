package com.sugarmunch.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import com.sugarmunch.app.features.model.AchievementRarity
import com.sugarmunch.app.pass.*
import com.sugarmunch.app.shop.ShopManager
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SugarPassScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val passManager = remember { SugarPassManager.getInstance(context) }
    val xpManager = remember { XpManager.getInstance(context) }
    val shopManager = remember { ShopManager.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    // State
    val progress by passManager.seasonProgressData.collectAsState(initial = null)
    val hasPremium by passManager.hasPremium.collectAsState(initial = false)
    val claimableRewards by passManager.claimableRewards.collectAsState(initial = emptyList())
    val sugarPoints by shopManager.sugarPoints.collectAsState(initial = 0)
    
    var showRewardDialog by remember { mutableStateOf<PassReward?>(null) }
    var showPremiumDialog by remember { mutableStateOf(false) }
    var showTierSkipDialog by remember { mutableStateOf(false) }
    var showClaimAllResult by remember { mutableStateOf<ClaimAllResult?>(null) }
    var showTierUpAnimation by remember { mutableStateOf(false) }
    
    // Track tier changes for animation
    var lastTier by remember { mutableStateOf(0) }
    val currentTier = progress?.currentTier ?: 1
    
    LaunchedEffect(currentTier) {
        if (lastTier > 0 && currentTier > lastTier) {
            showTierUpAnimation = true
        }
        lastTier = currentTier
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "🍭 Sugar Pass",
                            color = colors.onSurface,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        progress?.let {
                            Text(
                                it.currentSeason?.name ?: "Current Season",
                                color = colors.onSurface.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
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
                    // Sugar Points
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.primary.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🍬", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "$sugarPoints",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Premium badge
                    if (hasPremium) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFD700).copy(alpha = 0.3f)
                        ) {
                            Text(
                                "⭐ PREMIUM",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB8860B),
                                fontWeight = FontWeight.Bold
                            )
                        }
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

            progress?.let { prog ->
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Progress Section
                    PassProgressHeader(
                        progress = prog,
                        onClaimAll = {
                            scope.launch {
                                val result = passManager.claimAllRewards()
                                showClaimAllResult = result
                            }
                        },
                        hasClaimable = claimableRewards.isNotEmpty(),
                        colors = colors
                    )
                    
                    // Indie Discovery Section
                    IndieDiscoverySection(
                        colors = colors,
                        onInstallClick = { /* Handle install */ }
                    )
                    
                    // Premium CTA (if not premium)
                    if (!hasPremium && !prog.isMaxTier) {
                        PremiumCtaCard(
                            onUpgrade = { showPremiumDialog = true },
                            colors = colors
                        )
                    }
                    
                    // Tier Track
                    TierTrack(
                        currentTier = prog.currentTier,
                        maxTier = prog.maxTier,
                        hasPremium = hasPremium,
                        nextLegendaryTier = prog.nextLegendaryTier,
                        onTierClick = { tier ->
                            val rewards = passManager.getRewardsForTier(tier)
                            if (rewards.isNotEmpty()) {
                                // Show first unclaimed or first reward
                                val reward = rewards.find { r ->
                                    !prog.claimedRewards.contains("${r.tier}_${r.track.name}")
                                } ?: rewards.first()
                                showRewardDialog = reward
                            }
                        },
                        colors = colors
                    )
                }
            }
        }
    }

    // Dialogs
    showRewardDialog?.let { reward ->
        RewardDetailDialog(
            reward = reward,
            canClaim = currentTier >= reward.tier && (reward.isFree || hasPremium),
            isClaimed = progress?.let { it.claimedRewards.contains("${reward.tier}_${reward.track.name}") } ?: false,
            onClaim = {
                scope.launch {
                    when (val result = passManager.claimReward(reward.tier, reward.track)) {
                        is RewardClaimResult.SUCCESS -> {
                            showRewardDialog = null
                            // Show success animation or toast
                        }
                        else -> {
                            // Handle error
                        }
                    }
                }
            },
            onDismiss = { showRewardDialog = null },
            colors = colors
        )
    }

    if (showPremiumDialog) {
        PremiumPurchaseDialog(
            sugarPoints = sugarPoints,
            onPurchaseWithPoints = {
                scope.launch {
                    when (val result = passManager.buyPremium(useSugarPoints = true)) {
                        PurchaseResult.SUCCESS -> {
                            showPremiumDialog = false
                        }
                        is PurchaseResult.INSUFFICIENT_FUNDS -> {
                            // Show insufficient funds message
                        }
                        else -> {}
                    }
                }
            },
            onDismiss = { showPremiumDialog = false },
            colors = colors
        )
    }

    if (showTierSkipDialog) {
        TierSkipDialog(
            sugarPoints = sugarPoints,
            currentTier = currentTier,
            onSkipTiers = { count ->
                scope.launch {
                    passManager.buyTierSkips(count)
                    showTierSkipDialog = false
                }
            },
            onDismiss = { showTierSkipDialog = false },
            colors = colors
        )
    }

    showClaimAllResult?.let { result ->
        ClaimAllResultDialog(
            result = result,
            onDismiss = { showClaimAllResult = null },
            colors = colors
        )
    }

    if (showTierUpAnimation) {
        TierUpAnimation(
            newTier = currentTier,
            onComplete = { showTierUpAnimation = false }
        )
    }
}

@Composable
private fun PassProgressHeader(
    progress: SeasonProgressData,
    onClaimAll: () -> Unit,
    hasClaimable: Boolean,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Top row: Tier and Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current Tier Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = getTierColor(progress.currentTier)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "TIER",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${progress.currentTier}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (progress.isMaxTier) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("👑", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                
                // Time remaining
                if (!progress.isMaxTier) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (progress.timeRemainingMs < 86400000) // Less than 24h
                            Color(0xFFFF6B6B).copy(alpha = 0.2f)
                        else
                            colors.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (progress.timeRemainingMs < 86400000)
                                    Color(0xFFFF6B6B)
                                else
                                    colors.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                progress.formatTimeRemaining(),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (progress.timeRemainingMs < 86400000)
                                    Color(0xFFFF6B6B)
                                else
                                    colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar
            if (!progress.isMaxTier) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${progress.xpInCurrentTier} / ${progress.xpNeededForNext} XP",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            "${(progress.progressPercent * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress.progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = getTierColor(progress.currentTier),
                        trackColor = colors.surfaceVariant
                    )
                }
            } else {
                // Max tier celebration
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👑", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "MAX TIER REACHED!",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFB8860B),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("👑", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Next rewards preview
            if (progress.nextLegendaryTier != null && !progress.isMaxTier) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Next Legendary: Tier ${progress.nextLegendaryTier}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Claim all button
            if (hasClaimable) {
                Button(
                    onClick = onClaimAll,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary
                    )
                ) {
                    Text(
                        "🎁 Claim All Rewards (${progress.unclaimedRewardsCount})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumCtaCard(
    onUpgrade: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val infiniteTransition = rememberInfiniteTransition(label = "premium_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFD700).copy(alpha = 0.15f)
        ),
        border = BorderStroke(2.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
        onClick = onUpgrade
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFD700).copy(alpha = 0.3f)
            ) {
                Text(
                    "⭐",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Upgrade to Premium",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Unlock 100+ premium rewards!",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    "🍬 ${SugarPassManager.PREMIUM_PASS_PRICE} or ${SugarPassManager.PREMIUM_PASS_REAL_MONEY}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFB8860B),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.primary
            )
        }
    }
}

@Composable
private fun TierTrack(
    currentTier: Int,
    maxTier: Int,
    hasPremium: Boolean,
    nextLegendaryTier: Int?,
    onTierClick: (Int) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val listState = rememberLazyListState()
    
    // Scroll to current tier
    LaunchedEffect(currentTier) {
        val targetIndex = (currentTier - 1).coerceAtLeast(0)
        listState.animateScrollToItem(targetIndex)
    }
    
    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(maxTier) { index ->
            val tier = index + 1
            val isCurrent = tier == currentTier
            val isPast = tier < currentTier
            val isLegendary = tier % 10 == 0
            val isNextLegendary = tier == nextLegendaryTier
            
            TierNode(
                tier = tier,
                isCurrent = isCurrent,
                isPast = isPast,
                isLegendary = isLegendary,
                isNextLegendary = isNextLegendary,
                hasPremium = hasPremium,
                onClick = { onTierClick(tier) },
                colors = colors
            )
        }
    }
}

@Composable
private fun TierNode(
    tier: Int,
    isCurrent: Boolean,
    isPast: Boolean,
    isLegendary: Boolean,
    isNextLegendary: Boolean,
    hasPremium: Boolean,
    onClick: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val tierColor = when {
        isLegendary -> Color(0xFFFFD700)
        isCurrent -> colors.primary
        isPast -> colors.success
        else -> colors.surfaceVariant
    }
    
    val icon = when {
        isLegendary -> "👑"
        tier % 25 == 0 -> "💎"
        tier % 5 == 0 -> "⭐"
        else -> null
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Legendary indicator
        if (isNextLegendary) {
            Text(
                "LEGENDARY!",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        // Tier node
        Box(
            modifier = Modifier
                .size(if (isLegendary) 72.dp else if (isCurrent) 64.dp else 56.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCurrent -> tierColor
                        isPast -> tierColor.copy(alpha = 0.3f)
                        else -> colors.surfaceVariant.copy(alpha = 0.5f)
                    }
                )
                .border(
                    width = if (isLegendary) 3.dp else if (isCurrent) 2.dp else 1.dp,
                    color = if (isPast && !isCurrent) tierColor else tierColor.copy(alpha = 0.5f),
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Text(
                    icon,
                    style = if (isLegendary) MaterialTheme.typography.headlineSmall 
                           else MaterialTheme.typography.titleMedium
                )
            } else {
                Text(
                    "$tier",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isCurrent) colors.onPrimary else colors.onSurface,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Tier label
        Text(
            if (isLegendary) "T$tier" else "$tier",
            style = MaterialTheme.typography.labelSmall,
            color = when {
                isCurrent -> colors.primary
                isPast -> colors.success
                else -> colors.onSurface.copy(alpha = 0.5f)
            },
            fontWeight = if (isCurrent || isLegendary) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun RewardDetailDialog(
    reward: PassReward,
    canClaim: Boolean,
    isClaimed: Boolean,
    onClaim: () -> Unit,
    onDismiss: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val rarityColor = when (reward.rarity) {
        AchievementRarity.COMMON -> Color(0xFFCD7F32)
        AchievementRarity.UNCOMMON -> Color(0xFFC0C0C0)
        AchievementRarity.RARE -> Color(0xFFFFD700)
        AchievementRarity.EPIC -> Color(0xFF9370DB)
        AchievementRarity.LEGENDARY -> Color(0xFFFF1493)
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Reward icon
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = reward.previewColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            reward.icon,
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Track badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (reward.track) {
                        RewardTrack.FREE -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        RewardTrack.PREMIUM -> Color(0xFFFFD700).copy(alpha = 0.2f)
                        RewardTrack.LEGENDARY -> Color(0xFFFF1493).copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        reward.track.name,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = when (reward.track) {
                            RewardTrack.FREE -> Color(0xFF4CAF50)
                            RewardTrack.PREMIUM -> Color(0xFFB8860B)
                            RewardTrack.LEGENDARY -> Color(0xFFFF1493)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Name
                Text(
                    reward.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                // Description
                Text(
                    reward.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Rarity
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(rarityColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        reward.rarity.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = rarityColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Exclusive badge
                if (reward.isExclusive) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFF1493).copy(alpha = 0.2f)
                    ) {
                        Text(
                            "🔥 SEASON EXCLUSIVE - NEVER RETURNS!",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF1493),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action button
                when {
                    isClaimed -> {
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
                                    "CLAIMED",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    canClaim -> {
                        Button(
                            onClick = onClaim,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary
                            )
                        ) {
                            Text(
                                "🎁 Claim Reward",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                    reward.track != RewardTrack.FREE -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFD700).copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFFB8860B)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "PREMIUM REQUIRED",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFB8860B),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    else -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colors.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                "Reach Tier ${reward.tier} to Claim",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("Close", color = colors.onSurface.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun PremiumPurchaseDialog(
    sugarPoints: Int,
    onPurchaseWithPoints: () -> Unit,
    onDismiss: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val trackValues = remember { SugarPassRewards.getTrackValues() }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFD700).copy(alpha = 0.2f)
                    ) {
                        Text(
                            "⭐",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Upgrade to Premium",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    "Unlock incredible rewards!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Benefits
                PremiumBenefitRow("🍬", "${trackValues.premiumSugarPoints} Sugar Points", "vs ${trackValues.freeSugarPoints} free")
                PremiumBenefitRow("🎨", "${trackValues.premiumItems} Premium Items", "Themes, effects & more")
                PremiumBenefitRow("🔥", "${trackValues.exclusiveCount} Exclusives", "Never available again!")
                PremiumBenefitRow("⚡", "+25% XP Boost", "Level up faster")
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Purchase button
                val canAfford = sugarPoints >= SugarPassManager.PREMIUM_PASS_PRICE
                
                Button(
                    onClick = onPurchaseWithPoints,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        disabledContainerColor = colors.surfaceVariant
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🍬", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (canAfford) "Buy for ${SugarPassManager.PREMIUM_PASS_PRICE}"
                            else "Need ${SugarPassManager.PREMIUM_PASS_PRICE - sugarPoints} more",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (canAfford) Color(0xFF8B6914) else colors.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Maybe Later", color = colors.onSurface.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun PremiumBenefitRow(icon: String, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun TierSkipDialog(
    sugarPoints: Int,
    currentTier: Int,
    onSkipTiers: (Int) -> Unit,
    onDismiss: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "Skip Tiers",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Skip options
                SkipTierOption(
                    count = 1,
                    price = SugarPassManager.TIER_SKIP_PRICE,
                    sugarPoints = sugarPoints,
                    onSelect = { onSkipTiers(1) },
                    colors = colors
                )
                
                SkipTierOption(
                    count = 10,
                    price = SugarPassManager.BUNDLE_TIER_SKIP_10,
                    sugarPoints = sugarPoints,
                    discount = "20% OFF",
                    onSelect = { onSkipTiers(10) },
                    colors = colors
                )
                
                SkipTierOption(
                    count = 25,
                    price = SugarPassManager.BUNDLE_TIER_SKIP_25,
                    sugarPoints = sugarPoints,
                    discount = "33% OFF",
                    onSelect = { onSkipTiers(25) },
                    colors = colors
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun SkipTierOption(
    count: Int,
    price: Int,
    sugarPoints: Int,
    discount: String? = null,
    onSelect: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val canAfford = sugarPoints >= price
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (canAfford) colors.primary.copy(alpha = 0.1f) 
                           else colors.surfaceVariant.copy(alpha = 0.3f)
        ),
        onClick = { if (canAfford) onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🚀", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Skip $count Tiers",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (canAfford) colors.onSurface else colors.onSurface.copy(alpha = 0.5f)
                    )
                    discount?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🍬", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "$price",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (canAfford) colors.primary else colors.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ClaimAllResultDialog(
    result: ClaimAllResult,
    onDismiss: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎉", style = MaterialTheme.typography.displayLarge)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Rewards Claimed!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Summary
                if (result.sugarPointsEarned > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🍬", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "+${result.sugarPointsEarned} Sugar Points",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Text(
                    "${result.claimedCount} rewards unlocked!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
                
                if (result.unlockedItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${result.unlockedItems.size} new items added to inventory",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary
                    )
                ) {
                    Text(
                        "Awesome!",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TierUpAnimation(
    newTier: Int,
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
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onComplete() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "🎊",
                modifier = Modifier.scale(scale),
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "TIER UP!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Welcome to Tier $newTier",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2500)
        onComplete()
    }
}

@Composable
private fun IndieDiscoverySection(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onInstallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🚀", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Indie Discovery",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFF4081).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍭", style = MaterialTheme.typography.titleLarge)
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Lollipop Launcher",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            "Install to earn 50 Sugar Crystals!",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Button(
                        onClick = onInstallClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Install", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// HELPERS
// ═════════════════════════════════════════════════════════════

private fun getTierColor(tier: Int): Color = when {
    tier >= 100 -> Color(0xFFFFD700) // Gold
    tier >= 75 -> Color(0xFFFF1493) // Deep pink
    tier >= 50 -> Color(0xFF9370DB) // Purple
    tier >= 25 -> Color(0xFF00CED1) // Turquoise
    tier >= 10 -> Color(0xFFFFA500) // Orange
    else -> Color(0xFF4CAF50) // Green
}
