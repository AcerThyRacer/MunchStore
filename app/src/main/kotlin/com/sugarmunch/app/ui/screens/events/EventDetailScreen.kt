package com.sugarmunch.app.ui.screens.events

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.events.*
import com.sugarmunch.app.ui.theme.currentThemeColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    onShare: () -> Unit = {}
) {
    val context = LocalContext.current
    val event = remember { SeasonalEvents.getEventById(eventId) }
    val challengeManager = remember { ChallengeProgressManager.getInstance(context) }
    val rewardsManager = remember { EventRewardsManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    if (event == null) {
        ErrorState(onBack = onBack)
        return
    }
    
    val colors = currentThemeColors()
    val scrollState = rememberScrollState()
    var showLore by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Challenges", "Rewards", "Lore")
    
    val challenges = remember { EventChallenges.getChallengesForEvent(eventId) }
    val rewards = remember { EventRewardCatalog.getRewardsForEvent(eventId) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = event.theme.backgroundGradient.first()
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(padding)
        ) {
            // Hero Section
            EventHeroSection(
                event = event,
                daysRemaining = event.daysRemaining()
            )
            
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = colors.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // Content
            when (selectedTab) {
                0 -> ChallengesTab(
                    challenges = challenges,
                    challengeManager = challengeManager,
                    eventAccent = event.accentColor
                )
                1 -> RewardsTab(
                    rewards = rewards,
                    rewardsManager = rewardsManager,
                    eventAccent = event.accentColor
                )
                2 -> LoreTab(event = event)
            }
        }
    }
}

@Composable
private fun EventHeroSection(
    event: SeasonalEvent,
    daysRemaining: Long
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(event.theme.backgroundGradient)
            )
            .padding(24.dp)
    ) {
        Column {
            // Event icon and countdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Event emoji/icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (event.id) {
                            "halloween_spooktacular" -> "🎃"
                            "winter_wonderland" -> "❄️"
                            "valentines_sweetheart" -> "💕"
                            "spring_blossom" -> "🌸"
                            "summer_splash" -> "🏖️"
                            else -> "🎉"
                        },
                        fontSize = 48.sp
                    )
                }
                
                // Countdown
                Surface(
                    color = if (daysRemaining <= 2) Color(0xFFFF6B6B) else Color(0xFF4CAF50),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$daysRemaining",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (daysRemaining == 1L) "DAY LEFT" else "DAYS LEFT",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Event description
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Characters row
            if (event.lore.characters.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    event.lore.characters.take(3).forEach { character ->
                        CharacterChip(character = character)
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterChip(character: EventCharacter) {
    Surface(
        color = Color.White.copy(alpha = 0.3f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = character.emoji,
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = character.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ChallengesTab(
    challenges: List<EventChallenge>,
    challengeManager: ChallengeProgressManager,
    eventAccent: Color
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Complete challenges to earn points and unlock rewards!",
                style = MaterialTheme.typography.bodyMedium,
                color = LocalContentColor.current.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(challenges) { challenge ->
            ChallengeCard(
                challenge = challenge,
                challengeManager = challengeManager,
                eventAccent = eventAccent
            )
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge: EventChallenge,
    challengeManager: ChallengeProgressManager,
    eventAccent: Color
) {
    val progress by challengeManager.getProgressFlow(challenge.id).collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    
    val isCompleted = progress?.isCompleted ?: false
    val isClaimed = progress?.isClaimed ?: false
    val currentValue = progress?.currentValue ?: 0
    val progressPercent = progress?.progressPercent ?: 0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) eventAccent.copy(alpha = 0.1f) else Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Challenge icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) eventAccent.copy(alpha = 0.2f)
                            else Color.LightGray.copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = challenge.icon,
                        fontSize = 24.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Challenge info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = challenge.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                }
                
                // Points badge
                Surface(
                    color = eventAccent.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "+${challenge.rewardPoints}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = eventAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = eventAccent,
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "$currentValue/${challenge.targetValue}",
                    style = MaterialTheme.typography.labelMedium,
                    color = LocalContentColor.current.copy(alpha = 0.6f)
                )
            }
            
            // Claim button
            if (isCompleted && !isClaimed) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        scope.launch {
                            challengeManager.claimChallengeReward(challenge.id)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = eventAccent
                    )
                ) {
                    Text("Claim ${challenge.rewardPoints} Points")
                }
            }
        }
    }
}

@Composable
private fun RewardsTab(
    rewards: List<EventRewardItem>,
    rewardsManager: EventRewardsManager,
    eventAccent: Color
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Unlock exclusive rewards by completing challenges!",
                style = MaterialTheme.typography.bodyMedium,
                color = LocalContentColor.current.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(rewards) { reward ->
            RewardCard(
                reward = reward,
                rewardsManager = rewardsManager,
                eventAccent = eventAccent
            )
        }
    }
}

@Composable
private fun RewardCard(
    reward: EventRewardItem,
    rewardsManager: EventRewardsManager,
    eventAccent: Color
) {
    val rewardState by rewardsManager.getRewardState(reward.id).collectAsState(
        initial = UserRewardState(reward.id)
    )
    val scope = rememberCoroutineScope()
    
    val isUnlocked = rewardState.isUnlocked
    val isClaimed = rewardState.isClaimed
    val rarityColor = EventRewardCatalog.getRarityColor(reward.rarity)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) rarityColor.copy(alpha = 0.1f) else Color.White
        ),
        border = if (isUnlocked && !isClaimed) {
            BorderStroke(2.dp, rarityColor)
        } else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reward icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isUnlocked) rarityColor.copy(alpha = 0.2f)
                            else Color.LightGray.copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = reward.icon,
                        fontSize = 28.sp,
                        modifier = Modifier.alpha(if (isUnlocked) 1f else 0.5f)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Reward info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = reward.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (reward.isSecret && !isUnlocked) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Secret",
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(start = 4.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                    Text(
                        text = reward.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                    
                    // Rarity badge
                    Surface(
                        color = rarityColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = reward.rarity.name.replace("_", " "),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = rarityColor
                        )
                    }
                }
                
                // Points requirement
                if (!isUnlocked) {
                    Surface(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${reward.requiredPoints} pts",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            // Claim button
            if (isUnlocked && !isClaimed) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        scope.launch {
                            rewardsManager.claimReward(reward.id)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = rarityColor
                    )
                ) {
                    Text("Claim Reward")
                }
            } else if (isClaimed) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Claimed",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
private fun LoreTab(event: SeasonalEvent) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Story card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = event.theme.backgroundGradient.first().copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "📖 The Story",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = event.lore.story,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Intro card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🎬 Welcome",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = event.lore.intro,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp,
                    color = event.accentColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Characters
        if (event.lore.characters.isNotEmpty()) {
            Text(
                text = "👥 Characters",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            event.lore.characters.forEach { character ->
                CharacterDetailCard(character = character)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CharacterDetailCard(character: EventCharacter) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = character.emoji,
                fontSize = 48.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            Column {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = character.role,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = character.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalContentColor.current.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ErrorState(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Red
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Event not found",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Go Back")
        }
    }
}
