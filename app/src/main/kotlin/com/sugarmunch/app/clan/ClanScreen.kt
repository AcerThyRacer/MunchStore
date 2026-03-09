package com.sugarmunch.app.clan

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

/**
 * Clan/Guild System
 * 
 * Features:
 * - Create and join clans
 * - Clan chat
 * - Clan wars/competitions
 * - Shared rewards
 * - Member management
 */
@Composable
fun ClanScreen(
    onClanClick: (Clan) -> Unit,
    onCreateClan: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val myClan = remember { getMockMyClan() }
    val recommendedClans = remember { getMockRecommendedClans() }
    val topClans = remember { getMockTopClans() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clans") },
                actions = {
                    IconButton(onClick = onCreateClan) {
                        Icon(Icons.Default.Add, contentDescription = "Create Clan")
                    }
                }
            )
        },
        floatingActionButton = {
            if (myClan != null) {
                FloatingActionButton(
                    onClick = { /* Open clan chat */ },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Clan Chat")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("My Clan") },
                    icon = { 
                        Badge { Text(if (myClan != null) "1" else "0") }
                        Icon(Icons.Default.People, contentDescription = null)
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Discover") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Top Clans") }
                )
            }
            
            // Content
            when (selectedTab) {
                0 -> MyClanContent(
                    clan = myClan,
                    onClanClick = { onClanClick(it) }
                )
                1 -> DiscoverContent(
                    clans = recommendedClans,
                    onClanClick = { onClanClick(it) }
                )
                2 -> TopClansContent(
                    clans = topClans,
                    onClanClick = { onClanClick(it) }
                )
            }
        }
    }
}

@Composable
private fun MyClanContent(
    clan: Clan?,
    onClanClick: (Clan) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (clan == null) {
            item {
                EmptyClanState()
            }
        } else {
            item {
                ClanSummaryCard(clan = clan, onClick = { onClanClick(clan) })
            }
            
            item {
                ClanStatsRow(clan = clan)
            }
            
            item {
                Text(
                    text = "Members Online",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            items(clan.onlineMembers.take(5)) { member ->
                MemberListItem(member = member)
            }
            
            item {
                Text(
                    text = "Active Challenges",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            items(clan.activeChallenges) { challenge ->
                ChallengeListItem(challenge = challenge)
            }
        }
    }
}

@Composable
private fun ClanSummaryCard(clan: Clan, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clan emblem
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = clan.colors.map { Color(it) }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = clan.tag,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = clan.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${clan.memberCount} members • Level ${clan.level}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ClanStatsRow(clan: Clan) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ClanStatColumn(
            value = clan.totalPoints.toString(),
            label = "Clan Points",
            icon = Icons.Default.Star
        )
        ClanStatColumn(
            value = clan.wins.toString(),
            label = "War Wins",
            icon = Icons.Default.EmojiEvents
        )
        ClanStatColumn(
            value = clan.rank.toString(),
            label = "Global Rank",
            icon = Icons.Default.Leaderboard
        )
    }
}

@Composable
private fun ClanStatColumn(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MemberListItem(member: ClanMember) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFF69B4), Color(0xFF9370DB))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.name.take(2).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = member.role,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Online indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (member.isOnline) Color(0xFF4CAF50) else Color.Gray
                    )
            )
        }
    }
}

@Composable
private fun ChallengeListItem(challenge: ClanChallenge) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = challenge.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "${challenge.currentProgress}/${challenge.targetProgress} progress",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                LinearProgressIndicator(
                    progress = challenge.currentProgress.toFloat() / challenge.targetProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "+${challenge.rewardPoints}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EmptyClanState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.PeopleOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Clan Yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Join a clan to compete in wars and earn rewards together!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = { /* Browse clans */ }) {
                    Text("Browse Clans")
                }
                Button(onClick = { /* Create clan */ }) {
                    Text("Create Clan")
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// DATA MODELS
// ═════════════════════════════════════════════════════════════

data class Clan(
    val id: String,
    val name: String,
    val tag: String,
    val description: String,
    val colors: List<Long>,
    val memberCount: Int,
    val level: Int,
    val totalPoints: Int,
    val wins: Int,
    val losses: Int,
    val rank: Int,
    val onlineMembers: List<ClanMember>,
    val activeChallenges: List<ClanChallenge>
)

data class ClanMember(
    val id: String,
    val name: String,
    val role: String,
    val isOnline: Boolean,
    val contributionPoints: Int
)

data class ClanChallenge(
    val id: String,
    val name: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val rewardPoints: Int,
    val endsAt: Long
)

private fun getMockMyClan(): Clan? {
    return null // User not in a clan
}

private fun getMockRecommendedClans(): List<Clan> {
    return listOf(
        Clan(
            id = "clan_1",
            name = "Sugar Rush",
            tag = "RUSH",
            description = "Competitive clan looking for active players!",
            colors = listOf(0xFFFF69B4, 0xFF9370DB),
            memberCount = 42,
            level = 15,
            totalPoints = 125000,
            wins = 87,
            losses = 23,
            rank = 156,
            onlineMembers = emptyList(),
            activeChallenges = emptyList()
        ),
        Clan(
            id = "clan_2",
            name = "Candy Kings",
            tag = "KING",
            description = "Casual fun and friendship!",
            colors = listOf(0xFFFFD700, 0xFFFFA500),
            memberCount = 28,
            level = 10,
            totalPoints = 78000,
            wins = 45,
            losses = 32,
            rank = 342,
            onlineMembers = emptyList(),
            activeChallenges = emptyList()
        )
    )
}

private fun getMockTopClans(): List<Clan> {
    return listOf(
        Clan(
            id = "clan_top1",
            name = "Elite Squad",
            tag = "ELIT",
            description = "Top ranked clan worldwide",
            colors = listOf(0xFF00FF00, 0xFF008000),
            memberCount = 50,
            level = 50,
            totalPoints = 500000,
            wins = 450,
            losses = 12,
            rank = 1,
            onlineMembers = emptyList(),
            activeChallenges = emptyList()
        )
    )
}
