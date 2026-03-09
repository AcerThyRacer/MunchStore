package com.sugarmunch.app.ui.screens.clan

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.clan.ClanManager
import com.sugarmunch.app.clan.ClanWarsManager
import com.sugarmunch.app.clan.model.*
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClanWarScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val clanManager = remember { ClanManager.getInstance(context) }
    val warManager = remember { ClanWarsManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    val activeWar by warManager.activeWar.collectAsState()
    val warHistory by warManager.warHistory.collectAsState()
    val currentClan by clanManager.currentClan.collectAsState()
    val clanMembers by clanManager.clanMembers.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    var showStartWarDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Clan War",
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
            
            if (currentClan == null) {
                // Not in a clan
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Join a clan to participate in wars!",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Active War or Start War Banner
                    if (activeWar != null && activeWar?.status == WarStatus.ACTIVE) {
                        ActiveWarHeader(
                            war = activeWar!!,
                            colors = colors,
                            timeRemaining = warManager.getWarTimeRemaining(activeWar!!)
                        )
                    } else {
                        NoActiveWarBanner(
                            colors = colors,
                            canStart = clanManager.isLeader() || clanManager.isOfficer(),
                            onStartWar = { showStartWarDialog = true }
                        )
                    }
                    
                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = colors.surface.copy(alpha = 0.9f),
                        contentColor = colors.primary
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Current War") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Your Stats") }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("History") }
                        )
                    }
                    
                    // Tab Content
                    when (selectedTab) {
                        0 -> CurrentWarTab(
                            war = activeWar,
                            clanMembers = clanMembers,
                            colors = colors,
                            warManager = warManager
                        )
                        1 -> YourStatsTab(
                            clanMembers = clanMembers,
                            warHistory = warHistory,
                            colors = colors
                        )
                        2 -> WarHistoryTab(
                            history = warHistory,
                            colors = colors
                        )
                    }
                }
            }
        }
    }
    
    // Start War Dialog
    if (showStartWarDialog) {
        StartWarDialog(
            colors = colors,
            onDismiss = { showStartWarDialog = false },
            onStart = {
                scope.launch {
                    val opponent = warManager.findWarOpponent()
                    opponent?.let { enemyClan ->
                        warManager.startWar(enemyClan.id)
                    }
                    showStartWarDialog = false
                }
            }
        )
    }
}

@Composable
private fun ActiveWarHeader(
    war: ClanWar,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    timeRemaining: Long
) {
    val formattedTime = remember(timeRemaining) {
        val days = timeRemaining / (24 * 60 * 60 * 1000)
        val hours = (timeRemaining % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
        val minutes = (timeRemaining % (60 * 60 * 1000)) / (60 * 1000)
        when {
            days > 0 -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.primary.copy(alpha = 0.2f)
        ),
        border = BorderStroke(2.dp, colors.primary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // VS Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Clan
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = colors.primary.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = war.homeClanEmblem,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = war.homeClanTag,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // VS Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.error.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.error,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                // Enemy Clan
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = colors.error.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = war.enemyClanEmblem,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = war.enemyClanTag,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = war.homeScore.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = if (war.homeScore >= war.enemyScore) colors.primary else colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⏰ $formattedTime",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Text(
                    text = war.enemyScore.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = if (war.enemyScore >= war.homeScore) colors.error else colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NoActiveWarBanner(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    canStart: Boolean,
    onStartWar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚔️",
                style = MaterialTheme.typography.displayLarge
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "No Active War",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (canStart) {
                    "Start a war against another clan to earn rewards!"
                } else {
                    "Wait for your clan leaders to start a war."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            if (canStart) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onStartWar,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Clan War")
                }
            }
        }
    }
}

@Composable
private fun CurrentWarTab(
    war: ClanWar?,
    clanMembers: List<ClanMember>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    warManager: ClanWarsManager
) {
    if (war == null || war.status != WarStatus.ACTIVE) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No active war. Start one to see contributions!",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }
    
    // Sort members by contribution
    val sortedMembers = clanMembers.sortedByDescending { 
        warManager.calculateMemberScore(it)
    }
    
    val totalInstalls = clanMembers.sumOf { it.weeklyInstalls }
    val totalAchievements = clanMembers.sumOf { it.weeklyAchievements }
    val totalPoints = clanMembers.sumOf { it.weeklyPoints }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Stats Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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
                    WarStatItem(
                        icon = "📱",
                        value = totalInstalls.toString(),
                        label = "Installs",
                        colors = colors
                    )
                    WarStatItem(
                        icon = "🏆",
                        value = totalAchievements.toString(),
                        label = "Achievements",
                        colors = colors
                    )
                    WarStatItem(
                        icon = "⭐",
                        value = totalPoints.toString(),
                        label = "Points",
                        colors = colors
                    )
                }
            }
        }
        
        // Scoring Info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "💡 How to Score",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Install apps: +${ClanWarsManager.POINTS_PER_INSTALL} points each",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "• Unlock achievements: +${ClanWarsManager.POINTS_PER_ACHIEVEMENT} points each",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "• Daily activity: +${ClanWarsManager.POINTS_PER_ACTIVITY} point each",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        // Top Contributors Header
        item {
            Text(
                text = "Top Contributors",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        // Contributors List
        items(sortedMembers.take(10)) { member, index ->
            ContributorRow(
                rank = index + 1,
                member = member,
                score = warManager.calculateMemberScore(member),
                colors = colors
            )
        }
    }
}

@Composable
private fun YourStatsTab(
    clanMembers: List<ClanMember>,
    warHistory: List<ClanWar>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val currentUserMember = clanMembers.find { it.userId == "current_user" }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Personal Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface.copy(alpha = 0.95f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "📊 Your War Stats",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (currentUserMember != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatBox(
                            value = currentUserMember.weeklyInstalls.toString(),
                            label = "Installs",
                            colors = colors
                        )
                        StatBox(
                            value = currentUserMember.weeklyAchievements.toString(),
                            label = "Achievements",
                            colors = colors
                        )
                        StatBox(
                            value = currentUserMember.weeklyPoints.toString(),
                            label = "Points",
                            colors = colors
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val rank = clanMembers.sortedByDescending { 
                        it.weeklyInstalls * 10 + it.weeklyAchievements * 25 + it.weeklyPoints 
                    }.indexOfFirst { it.userId == "current_user" } + 1
                    
                    Text(
                        text = "🏆 Clan Rank: #${if (rank > 0) rank else "-"}",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "Join the clan to start contributing!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // Lifetime Stats
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface.copy(alpha = 0.95f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "⭐ Lifetime Stats",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val warsWon = warHistory.count { it.result == ClanWarResult.VICTORY }
                val totalWars = warHistory.size
                
                StatRow(label = "Wars Participated", value = totalWars.toString(), colors)
                StatRow(label = "Wars Won", value = warsWon.toString(), colors)
                StatRow(label = "Total Contributions", value = currentUserMember?.totalContributions?.toString() ?: "0", colors)
            }
        }
        
        // Recent War Results
        if (warHistory.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surface.copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "🗓️ Recent Wars",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    warHistory.take(3).forEach { war ->
                        val (icon, color) = when (war.result) {
                            ClanWarResult.VICTORY -> "🏆" to Color(0xFF4CAF50)
                            ClanWarResult.DEFEAT -> "💔" to Color(0xFFE53935)
                            ClanWarResult.DRAW -> "🤝" to Color(0xFFFFA726)
                            else -> "⏳" to colors.onSurface.copy(alpha = 0.5f)
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(icon, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "vs [${war.enemyClanTag}]",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurface
                                )
                            }
                            Text(
                                text = "${war.homeScore} - ${war.enemyScore}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WarHistoryTab(
    history: List<ClanWar>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "📜",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No war history yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "Your completed wars will appear here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        return
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(history) { war ->
            WarHistoryCard(war = war, colors = colors)
        }
    }
}

@Composable
private fun WarHistoryCard(
    war: ClanWar,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val (resultIcon, resultColor, resultText) = when (war.result) {
        ClanWarResult.VICTORY -> Triple("🏆", Color(0xFF4CAF50), "Victory")
        ClanWarResult.DEFEAT -> Triple("💔", Color(0xFFE53935), "Defeat")
        ClanWarResult.DRAW -> Triple("🤝", Color(0xFFFFA726), "Draw")
        else -> Triple("⏳", colors.onSurface.copy(alpha = 0.5f), "Unknown")
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Result Icon
            Surface(
                shape = CircleShape,
                color = resultColor.copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = resultIcon,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = resultText,
                        style = MaterialTheme.typography.titleMedium,
                        color = resultColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.surfaceVariant
                    ) {
                        Text(
                            text = "${war.homeScore} - ${war.enemyScore}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "vs [${war.enemyClanTag}] ${war.enemyClanName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ContributorRow(
    rank: Int,
    member: ClanMember,
    score: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (rank) {
                1 -> Color(0xFFFFD700).copy(alpha = 0.15f) // Gold
                2 -> Color(0xFFC0C0C0).copy(alpha = 0.15f) // Silver
                3 -> Color(0xFFCD7F32).copy(alpha = 0.15f) // Bronze
                else -> colors.surface.copy(alpha = 0.8f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Text(
                text = when (rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> "#$rank"
                },
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(40.dp)
            )
            
            // Avatar
            Text(
                text = member.avatarEmoji,
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.username,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${member.weeklyInstalls} installs • ${member.weeklyAchievements} achievements",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
            }
            
            // Score
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = colors.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WarStatItem(
    icon: String,
    value: String,
    label: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, style = MaterialTheme.typography.headlineSmall)
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun StatBox(
    value: String,
    label: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            color = colors.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StartWarDialog(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onDismiss: () -> Unit,
    onStart: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚔️", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Clan War?")
            }
        },
        text = {
            Column {
                Text(
                    "You are about to start a clan war!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "• War lasts for 7 days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    "• Compete against a matched clan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    "• Earn points through installs and achievements",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    "• Win exclusive rewards for your clan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.8f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text("Start War!")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.onSurface)
            }
        },
        containerColor = colors.surface
    )
}
