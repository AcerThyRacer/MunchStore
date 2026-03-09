package com.sugarmunch.app.ui.screens.events

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.events.*
import com.sugarmunch.app.ui.theme.currentThemeColors
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onEventClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val eventManager = remember { EventManager.getInstance(context) }
    val challengeManager = remember { ChallengeProgressManager.getInstance(context) }
    val rewardsManager = remember { EventRewardsManager.getInstance(context) }
    
    val activeEvents by eventManager.getActiveEvents().collectAsState(initial = emptyList())
    val upcomingEvents by eventManager.getUpcomingEvents().collectAsState(initial = emptyList())
    val colors = currentThemeColors()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Active", "Upcoming", "Past")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "🎉 Seasonal Events",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(colors.background)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = colors.surface,
                contentColor = colors.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            when (selectedTab) {
                0 -> ActiveEventsTab(
                    events = activeEvents,
                    onEventClick = onEventClick,
                    challengeManager = challengeManager,
                    rewardsManager = rewardsManager
                )
                1 -> UpcomingEventsTab(
                    events = upcomingEvents
                )
                2 -> PastEventsTab()
            }
        }
    }
}

@Composable
private fun ActiveEventsTab(
    events: List<SeasonalEvent>,
    onEventClick: (String) -> Unit,
    challengeManager: ChallengeProgressManager,
    rewardsManager: EventRewardsManager
) {
    if (events.isEmpty()) {
        EmptyEventsState(
            icon = "🌟",
            title = "No Active Events",
            message = "Check back soon for exciting seasonal events!"
        )
        return
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "🔥 Currently Active",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(events) { event ->
            ActiveEventCard(
                event = event,
                onClick = { onEventClick(event.id) },
                challengeManager = challengeManager,
                rewardsManager = rewardsManager
            )
        }
    }
}

@Composable
private fun ActiveEventCard(
    event: SeasonalEvent,
    onClick: () -> Unit,
    challengeManager: ChallengeProgressManager,
    rewardsManager: EventRewardsManager
) {
    val context = LocalContext.current
    val daysRemaining = event.daysRemaining()
    val hoursRemaining = (daysRemaining * 24).toInt()
    
    val challenges = remember { EventChallenges.getChallengesForEvent(event.id) }
    var completedCount by remember { mutableStateOf(0) }
    
    LaunchedEffect(event.id) {
        completedCount = challengeManager.getCompletedChallengesCount(event.id)
    }
    
    val totalPoints = remember { challenges.sumOf { it.rewardPoints } }
    var currentPoints by remember { mutableStateOf(0) }
    
    LaunchedEffect(event.id) {
        currentPoints = challengeManager.getTotalEventPoints(event.id)
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = event.theme.backgroundGradient
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Event icon and name
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.theme.particleType.name.first().toString(),
                        fontSize = 48.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        
                        // Countdown badge
                        Surface(
                            color = if (daysRemaining <= 2) Color(0xFFFF6B6B) else Color(0xFF4CAF50),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (daysRemaining > 1) "⏰ $daysRemaining days left" else "⏰ Ending soon!",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "View",
                        tint = Color.Black.copy(alpha = 0.5f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress section
                Text(
                    text = "Event Progress",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Black.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { if (totalPoints > 0) currentPoints.toFloat() / totalPoints else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = event.accentColor,
                    trackColor = Color.Black.copy(alpha = 0.1f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$currentPoints / $totalPoints points",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "$completedCount / ${challenges.size} challenges",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Quick actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = event.accentColor
                        )
                    ) {
                        Text("View Event")
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingEventsTab(events: List<SeasonalEvent>) {
    if (events.isEmpty()) {
        EmptyEventsState(
            icon = "📅",
            title = "No Upcoming Events",
            message = "No events scheduled in the next 30 days"
        )
        return
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "📅 Coming Soon",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(events) { event ->
            UpcomingEventCard(event = event)
        }
    }
}

@Composable
private fun UpcomingEventCard(event: SeasonalEvent) {
    val daysUntil = event.daysUntilStart()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.8f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(event.theme.backgroundGradient.take(2))
                    ),
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
                    fontSize = 32.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = event.shortDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalContentColor.current.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = event.accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Starts in $daysUntil days",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = event.accentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun PastEventsTab() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = LocalContentColor.current.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Event history coming soon",
            style = MaterialTheme.typography.bodyLarge,
            color = LocalContentColor.current.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun EmptyEventsState(
    icon: String,
    title: String,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = icon,
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = LocalContentColor.current.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EventCountdown(
    endDate: Long,
    accentColor: Color
) {
    var timeRemaining by remember { mutableStateOf(calculateTimeRemaining(endDate)) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            timeRemaining = calculateTimeRemaining(endDate)
        }
    }
    
    Surface(
        color = accentColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeUnitDisplay(timeRemaining.days, "DAYS")
            Text(":", fontWeight = FontWeight.Bold, color = accentColor)
            TimeUnitDisplay(timeRemaining.hours, "HRS")
            Text(":", fontWeight = FontWeight.Bold, color = accentColor)
            TimeUnitDisplay(timeRemaining.minutes, "MIN")
            Text(":", fontWeight = FontWeight.Bold, color = accentColor)
            TimeUnitDisplay(timeRemaining.seconds, "SEC")
        }
    }
}

@Composable
private fun TimeUnitDisplay(value: Long, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString().padStart(2, '0'),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = label,
            fontSize = 8.sp
        )
    }
}

private data class TimeRemaining(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long
)

private fun calculateTimeRemaining(endDateMillis: Long): TimeRemaining {
    val now = System.currentTimeMillis()
    val diff = max(0, endDateMillis - now)
    
    val seconds = (diff / 1000) % 60
    val minutes = (diff / (1000 * 60)) % 60
    val hours = (diff / (1000 * 60 * 60)) % 24
    val days = diff / (1000 * 60 * 60 * 24)
    
    return TimeRemaining(days, hours, minutes, seconds)
}
