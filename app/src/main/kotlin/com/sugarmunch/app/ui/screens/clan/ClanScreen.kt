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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.clan.ClanChatManager
import com.sugarmunch.app.clan.ClanManager
import com.sugarmunch.app.clan.ClanWarsManager
import com.sugarmunch.app.clan.model.*
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClanScreen(
    onBack: () -> Unit,
    onNavigateToClanList: () -> Unit,
    onNavigateToClanWar: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToRewards: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val clanManager = remember { ClanManager.getInstance(context) }
    val warManager = remember { ClanWarsManager.getInstance(context) }
    val chatManager = remember { ClanChatManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    val currentClan by clanManager.currentClan.collectAsState()
    val currentMember by clanManager.currentMember.collectAsState()
    val clanMembers by clanManager.clanMembers.collectAsState()
    val activeWar by warManager.activeWar.collectAsState()
    val unreadMessages by chatManager.unreadCount.collectAsState()
    
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showKickDialog by remember { mutableStateOf<ClanMember?>(null) }
    var showPromoteDialog by remember { mutableStateOf<ClanMember?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Clan",
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
                    if (currentClan != null) {
                        // Chat button with unread indicator
                        IconButton(onClick = onNavigateToChat) {
                            BadgedBox(
                                badge = {
                                    if (unreadMessages > 0) {
                                        Badge { Text(unreadMessages.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Chat,
                                    contentDescription = "Chat",
                                    tint = colors.onSurface
                                )
                            }
                        }
                        
                        // Settings (leader/officer only)
                        if (clanManager.canManageSettings()) {
                            IconButton(onClick = { /* Open settings */ }) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = colors.onSurface
                                )
                            }
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
            
            if (currentClan == null) {
                // Not in a clan - show join/create options
                NoClanView(
                    colors = colors,
                    onJoinClan = onNavigateToClanList,
                    onCreateClan = { /* Open create dialog */ }
                )
            } else {
                // In a clan - show clan details
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Clan Info Card
                    ClanInfoCard(
                        clan = currentClan!!,
                        memberCount = clanMembers.size,
                        colors = colors
                    )
                    
                    // Active War Banner (if any)
                    ActiveWarBanner(
                        war = activeWar,
                        colors = colors,
                        onClick = onNavigateToClanWar
                    )
                    
                    // Tab Row
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = colors.surface.copy(alpha = 0.9f),
                        contentColor = colors.primary
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Members") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Stats") }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Rewards") }
                        )
                    }
                    
                    // Tab Content
                    when (selectedTab) {
                        0 -> MembersList(
                            members = clanMembers,
                            currentUserId = "current_user", // Would get from auth
                            colors = colors,
                            canKick = clanManager.canKickMembers(),
                            canPromote = clanManager.canPromoteMembers(),
                            onKick = { showKickDialog = it },
                            onPromote = { showPromoteDialog = it }
                        )
                        1 -> ClanStatsView(
                            clan = currentClan!!,
                            members = clanMembers,
                            colors = colors
                        )
                        2 -> ClanRewardsPreview(
                            colors = colors,
                            onViewAll = onNavigateToRewards
                        )
                    }
                    
                    // Leave Clan Button
                    if (selectedTab == 0) {
                        Button(
                            onClick = { showLeaveDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.error.copy(alpha = 0.2f),
                                contentColor = colors.error
                            )
                        ) {
                            Icon(Icons.Default.ExitToApp, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Leave Clan")
                        }
                    }
                }
            }
        }
    }
    
    // Leave Clan Dialog
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave Clan?") },
            text = { 
                Text("Are you sure you want to leave ${currentClan?.name}? You'll lose your progress in this clan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            clanManager.leaveClan()
                            showLeaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error)
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = colors.surface
        )
    }
    
    // Kick Member Dialog
    showKickDialog?.let { member ->
        AlertDialog(
            onDismissRequest = { showKickDialog = null },
            title = { Text("Kick Member?") },
            text = { Text("Are you sure you want to kick ${member.username} from the clan?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            clanManager.kickMember(member.userId)
                            showKickDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error)
                ) {
                    Text("Kick")
                }
            },
            dismissButton = {
                TextButton(onClick = { showKickDialog = null }) {
                    Text("Cancel")
                }
            },
            containerColor = colors.surface
        )
    }
    
    // Promote Member Dialog
    showPromoteDialog?.let { member ->
        AlertDialog(
            onDismissRequest = { showPromoteDialog = null },
            title = { Text("Promote ${member.username}?") },
            text = { 
                val newRole = when (member.role) {
                    ClanRole.RECRUIT -> "Member"
                    ClanRole.MEMBER -> "Officer"
                    else -> null
                }
                Text("Promote to $newRole?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            clanManager.promoteMember(member.userId)
                            showPromoteDialog = null
                        }
                    }
                ) {
                    Text("Promote")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPromoteDialog = null }) {
                    Text("Cancel")
                }
            },
            containerColor = colors.surface
        )
    }
}

@Composable
private fun NoClanView(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onJoinClan: () -> Unit,
    onCreateClan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🛡️",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "You're not in a clan yet",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Join a clan to compete with others, earn exclusive rewards, and make new friends!",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onJoinClan,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
        ) {
            Icon(Icons.Default.Search, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Find a Clan")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onCreateClan,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.primary
            )
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Your Own Clan")
        }
    }
}

@Composable
private fun ClanInfoCard(
    clan: Clan,
    memberCount: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clan Emblem
                Surface(
                    shape = CircleShape,
                    color = Color(android.graphics.Color.parseColor(clan.primaryColor)).copy(alpha = 0.3f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = clan.emblem,
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "[${clan.tag}]",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = clan.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.onSurface,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = clan.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ClanStatItem(
                    value = "Lv.${clan.level}",
                    label = "Level",
                    icon = "⭐",
                    colors = colors
                )
                ClanStatItem(
                    value = memberCount.toString(),
                    label = "Members",
                    icon = "👥",
                    colors = colors
                )
                ClanStatItem(
                    value = clan.trophies.toString(),
                    label = "Trophies",
                    icon = "🏆",
                    colors = colors
                )
                ClanStatItem(
                    value = "${clan.warWins}-${clan.warLosses}",
                    label = "Wars",
                    icon = "⚔️",
                    colors = colors
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // XP Bar
            LinearProgressIndicator(
                progress = { 0.6f }, // Calculate from actual XP
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = colors.primary,
                trackColor = colors.surfaceVariant
            )
            
            Text(
                text = "${clan.xp} / 1000 XP to next level",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ActiveWarBanner(
    war: ClanWar?,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    if (war == null || war.status != WarStatus.ACTIVE) return
    
    val warManager = remember { ClanWarsManager.getInstance(LocalContext.current) }
    val timeRemaining = remember { warManager.getWarTimeRemaining(war) }
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.primary.copy(alpha = 0.2f)
        ),
        border = BorderStroke(2.dp, colors.primary.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚔️",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Active Clan War!",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "vs [${war.enemyClanTag}] ${war.enemyClanName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = "${war.homeScore} - ${war.enemyScore} • ${warManager.formatTimeRemaining(timeRemaining)} left",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.primary
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View War",
                tint = colors.primary
            )
        }
    }
}

@Composable
private fun MembersList(
    members: List<ClanMember>,
    currentUserId: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    canKick: Boolean,
    canPromote: Boolean,
    onKick: (ClanMember) -> Unit,
    onPromote: (ClanMember) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(members) { member ->
            MemberCard(
                member = member,
                isCurrentUser = member.userId == currentUserId,
                colors = colors,
                canKick = canKick && member.role != ClanRole.LEADER && member.userId != currentUserId,
                canPromote = canPromote && member.role.ordinal < ClanRole.OFFICER.ordinal,
                onKick = { onKick(member) },
                onPromote = { onPromote(member) }
            )
        }
    }
}

@Composable
private fun MemberCard(
    member: ClanMember,
    isCurrentUser: Boolean,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    canKick: Boolean,
    canPromote: Boolean,
    onKick: () -> Unit,
    onPromote: () -> Unit
) {
    val chatManager = remember { ClanChatManager.getInstance(LocalContext.current) }
    val roleColor = chatManager.getRoleColor(member.role)
    val roleIcon = chatManager.getRoleIcon(member.role)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) 
                colors.primary.copy(alpha = 0.1f) 
            else 
                colors.surface.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.avatarEmoji,
                    style = MaterialTheme.typography.headlineSmall
                )
                if (member.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                            .align(Alignment.BottomEnd)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.username,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(You)",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.primary
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$roleIcon ${member.role.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = roleColor,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = " • ${member.weeklyPoints} pts this week",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Actions
            if (canPromote) {
                IconButton(onClick = onPromote) {
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = "Promote",
                        tint = colors.primary
                    )
                }
            }
            
            if (canKick) {
                IconButton(onClick = onKick) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Kick",
                        tint = colors.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ClanStatsView(
    clan: Clan,
    members: List<ClanMember>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val totalWeeklyPoints = members.sumOf { it.weeklyPoints }
    val topContributor = members.maxByOrNull { it.weeklyPoints }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Weekly Activity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface.copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📊 Weekly Activity",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                StatRow("Total Points", totalWeeklyPoints.toString(), colors)
                StatRow("Total Installs", members.sumOf { it.weeklyInstalls }.toString(), colors)
                StatRow("Achievements", members.sumOf { it.weeklyAchievements }.toString(), colors)
                StatRow("Active Members", members.count { it.weeklyPoints > 0 }.toString(), colors)
            }
        }
        
        // Top Contributor
        topContributor?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.primary.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👑",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Top Contributor This Week",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${it.username} - ${it.weeklyPoints} points",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // War Record
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface.copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "⚔️ War Record",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WarStat("Wins", clan.warWins.toString(), Color(0xFF4CAF50), colors)
                    WarStat("Losses", clan.warLosses.toString(), Color(0xFFE53935), colors)
                    WarStat("Draws", clan.warDraws.toString(), Color(0xFFFFA726), colors)
                }
            }
        }
    }
}

@Composable
private fun ClanRewardsPreview(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onViewAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface.copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎁 Clan Rewards",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    
                    TextButton(onClick = onViewAll) {
                        Text("View All")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Complete clan activities and win wars to earn exclusive rewards!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Preview items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RewardPreviewItem("🎨", "Themes", colors)
                    RewardPreviewItem("✨", "Effects", colors)
                    RewardPreviewItem("🏆", "Badges", colors)
                    RewardPreviewItem("🎖️", "Emblems", colors)
                }
            }
        }
    }
}

@Composable
private fun ClanStatItem(
    value: String,
    label: String,
    icon: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun StatRow(label: String, value: String, colors: com.sugarmunch.app.theme.model.AdjustedColors) {
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
private fun WarStat(label: String, value: String, color: Color, colors: com.sugarmunch.app.theme.model.AdjustedColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun RewardPreviewItem(icon: String, label: String, colors: com.sugarmunch.app.theme.model.AdjustedColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.primary.copy(alpha = 0.2f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface.copy(alpha = 0.7f)
        )
    }
}
