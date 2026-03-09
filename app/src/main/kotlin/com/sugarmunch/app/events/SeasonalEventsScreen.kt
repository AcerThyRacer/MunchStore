package com.sugarmunch.app.events

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
 * Seasonal Events System
 * 
 * Features:
 * - Limited-time events
 * - Special challenges
 * - Exclusive rewards
 * - Event countdown timers
 * - Progress tracking
 */
@Composable
fun SeasonalEventsScreen(
    onEventClick: (SeasonalEvent) -> Unit,
    onClaimReward: (EventReward) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeEvents = remember { getMockActiveEvents() }
    val upcomingEvents = remember { getMockUpcomingEvents() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seasonal Events") },
                actions = {
                    IconButton(onClick = { /* Calendar */ }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Calendar")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Active events
            if (activeEvents.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Active Events",
                        icon = Icons.Default.Celebration
                    )
                }
                
                items(activeEvents) { event ->
                    ActiveEventCard(
                        event = event,
                        onClick = { onEventClick(event) }
                    )
                }
            }
            
            // Upcoming events
            if (upcomingEvents.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Coming Soon",
                        icon = Icons.Default.AccessTime
                    )
                }
                
                items(upcomingEvents) { event ->
                    UpcomingEventCard(event = event)
                }
            }
            
            // Event tips
            item {
                EventTipsCard()
            }
        }
    }
}

@Composable
private fun ActiveEventCard(
    event: SeasonalEvent,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Event banner gradient
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = event.colors.map { Color(it) }
                    )
                )
            }
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Event badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = event.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress & countdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Progress
                    Column {
                        Text(
                            text = "Progress: ${event.currentProgress}/${event.targetProgress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                        LinearProgressIndicator(
                            progress = event.currentProgress.toFloat() / event.targetProgress,
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(6.dp),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                    
                    // Countdown
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
                                text = "${event.daysRemaining}d left",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            // Exclusive badge
            if (event.isExclusive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "EXCLUSIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingEventCard(event: SeasonalEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = event.colors.map { Color(it) }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = event.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Starts in ${event.daysUntilStart} days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            OutlinedButton(
                onClick = { /* Set reminder */ },
                enabled = !event.reminderSet
            ) {
                if (event.reminderSet) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text("Remind")
                }
            }
        }
    }
}

@Composable
private fun EventTipsCard() {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Event Tips",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            EventTip("Complete daily challenges for bonus event points")
            EventTip("Team up with clan members for group rewards")
            EventTip("Exclusive rewards are only available during the event")
            EventTip("Check back daily for new limited-time challenges")
        }
    }
}

@Composable
private fun EventTip(tip: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = tip,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

// ═════════════════════════════════════════════════════════════
// DATA MODELS
// ═════════════════════════════════════════════════════════════

data class SeasonalEvent(
    val id: String,
    val name: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val colors: List<Long>,
    val startDate: Long,
    val endDate: Long,
    val currentProgress: Int,
    val targetProgress: Int,
    val isExclusive: Boolean,
    val reminderSet: Boolean,
    val rewards: List<EventReward>
) {
    val daysRemaining: Int
        get() = ((endDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    
    val daysUntilStart: Int
        get() = ((startDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
}

data class EventReward(
    val id: String,
    val name: String,
    val requirement: Int,
    val isClaimed: Boolean
)

private fun getMockActiveEvents(): List<SeasonalEvent> {
    return listOf(
        SeasonalEvent(
            id = "event_1",
            name = "Holiday Spectacular",
            description = "Celebrate the holidays with exclusive themes and rewards!",
            icon = Icons.Default.Celebration,
            colors = listOf(0xFFD42426, 0xFF165B33, 0xFFF8B229),
            startDate = System.currentTimeMillis() - 86400000 * 5,
            endDate = System.currentTimeMillis() + 86400000 * 25,
            currentProgress = 350,
            targetProgress = 1000,
            isExclusive = true,
            reminderSet = false,
            rewards = listOf(
                EventReward("r1", "Holiday Theme", 200, false),
                EventReward("r2", "Festive Effect", 500, false),
                EventReward("r3", "Exclusive Badge", 1000, false)
            )
        ),
        SeasonalEvent(
            id = "event_2",
            name = "New Year Challenge",
            description = "Start the new year with special challenges!",
            icon = Icons.Default.Festival,
            colors = listOf(0xFFFFD700, 0xFF000000, 0xFF800080),
            startDate = System.currentTimeMillis() - 86400000 * 2,
            endDate = System.currentTimeMillis() + 86400000 * 10,
            currentProgress = 120,
            targetProgress = 500,
            isExclusive = false,
            reminderSet = true,
            rewards = listOf(
                EventReward("r1", "NY Theme", 250, false),
                EventReward("r2", "2026 Badge", 500, false)
            )
        )
    )
}

private fun getMockUpcomingEvents(): List<SeasonalEvent> {
    return listOf(
        SeasonalEvent(
            id = "event_3",
            name = "Valentine's Special",
            description = "Share the love with sweet themes!",
            icon = Icons.Default.Favorite,
            colors = listOf(0xFFFF69B4, 0xFFFFB6C1, 0xFFFF1493),
            startDate = System.currentTimeMillis() + 86400000 * 30,
            endDate = System.currentTimeMillis() + 86400000 * 45,
            currentProgress = 0,
            targetProgress = 1000,
            isExclusive = true,
            reminderSet = false,
            rewards = emptyList()
        )
    )
}
