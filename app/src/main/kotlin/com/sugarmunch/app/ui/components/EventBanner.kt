package com.sugarmunch.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.events.EventManager
import com.sugarmunch.app.events.SeasonalEvent
import com.sugarmunch.app.events.SeasonalEvents
import kotlinx.coroutines.delay
import java.time.LocalDate

/**
 * Event Banner Component
 * 
 * Displays on the home screen during active events.
 * Shows event info, countdown, and tap-to-navigate.
 */

@Composable
fun EventBanner(
    onEventClick: (String) -> Unit,
    onDismiss: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val eventManager = remember { EventManager.getInstance(context) }
    
    var activeEvent by remember { mutableStateOf<SeasonalEvent?>(null) }
    var isVisible by remember { mutableStateOf(true) }
    var isDismissed by remember { mutableStateOf(false) }
    
    // Check for active events
    LaunchedEffect(Unit) {
        val events = eventManager.getActiveEvents(LocalDate.now()).first()
        activeEvent = events.firstOrNull()
    }
    
    if (activeEvent == null || isDismissed) return
    
    val event = activeEvent!!
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        EventBannerContent(
            event = event,
            onClick = { onEventClick(event.id) },
            onDismiss = {
                isVisible = false
                onDismiss?.invoke()
            }
        )
    }
}

@Composable
private fun EventBannerContent(
    event: SeasonalEvent,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val daysRemaining = event.daysRemaining()
    val isEndingSoon = daysRemaining <= 2
    
    // Pulsing animation for urgency
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isEndingSoon) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = event.accentColor.copy(alpha = 0.5f)
            )
            .scale(pulseScale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box {
            // Animated gradient background
            AnimatedGradientBackground(
                colors = event.theme.backgroundGradient,
                modifier = Modifier.fillMaxWidth()
            )
            
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Top row: Event name and dismiss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Live indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PulsingDot(color = if (isEndingSoon) Color.Red else Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEndingSoon) "ENDING SOON" else "LIVE EVENT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isEndingSoon) Color.Red else Color(0xFF4CAF50)
                        )
                    }
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Event info row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Event icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.3f)),
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
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Event details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = event.shortDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Countdown
                    EventCountdownCompact(
                        daysRemaining = daysRemaining,
                        accentColor = event.accentColor,
                        isUrgent = isEndingSoon
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Action bar
                Surface(
                    color = if (isEndingSoon) Color.Red.copy(alpha = 0.2f) else event.accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEndingSoon) "⏰ Claim rewards before it's too late!" else "🎁 Complete challenges for exclusive rewards",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isEndingSoon) Color.Red else event.accentColor,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "View",
                            tint = if (isEndingSoon) Color.Red else event.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedGradientBackground(
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    
    // Animate between different gradient positions
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )
    
    val brush = Brush.verticalGradient(
        colors = colors.map { it.copy(alpha = 0.9f) },
        startY = 0f,
        endY = 200f * (1 + offset * 0.2f)
    )
    
    Box(
        modifier = modifier.background(brush)
    )
}

@Composable
private fun PulsingDot(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )
    
    Box(
        modifier = Modifier
            .size(8.dp)
            .scale(scale)
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = alpha))
    )
}

@Composable
private fun EventCountdownCompact(
    daysRemaining: Long,
    accentColor: Color,
    isUrgent: Boolean
) {
    Column(horizontalAlignment = Alignment.End) {
        Surface(
            color = if (isUrgent) Color.Red.copy(alpha = 0.2f) else accentColor.copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$daysRemaining",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isUrgent) Color.Red else accentColor,
                    fontSize = 20.sp
                )
                Text(
                    text = if (daysRemaining == 1L) "DAY" else "DAYS",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUrgent) Color.Red.copy(alpha = 0.8f) else accentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * Compact event banner for smaller spaces
 */
@Composable
fun EventBannerCompact(
    onEventClick: (String) -> Unit
) {
    val context = LocalContext.current
    val eventManager = remember { EventManager.getInstance(context) }
    
    var activeEvent by remember { mutableStateOf<SeasonalEvent?>(null) }
    
    LaunchedEffect(Unit) {
        val events = eventManager.getActiveEvents(LocalDate.now()).first()
        activeEvent = events.firstOrNull()
    }
    
    if (activeEvent == null) return
    
    val event = activeEvent!!
    val daysRemaining = event.daysRemaining()
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onEventClick(event.id) },
        color = event.accentColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
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
                fontSize = 20.sp
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = event.accentColor
                )
                Text(
                    text = "$daysRemaining days left",
                    style = MaterialTheme.typography.labelSmall,
                    color = event.accentColor.copy(alpha = 0.8f)
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View",
                tint = event.accentColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Mini event chip for app bars or headers
 */
@Composable
fun EventChip(
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val eventManager = remember { EventManager.getInstance(context) }
    
    var activeEvent by remember { mutableStateOf<SeasonalEvent?>(null) }
    
    LaunchedEffect(Unit) {
        val events = eventManager.getActiveEvents(LocalDate.now()).first()
        activeEvent = events.firstOrNull()
    }
    
    if (activeEvent == null) return
    
    val event = activeEvent!!
    
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = event.accentColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pulsing dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White)
            )
            
            Spacer(modifier = Modifier.width(6.dp))
            
            Text(
                text = event.name,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Event floating action button
 */
@Composable
fun EventFloatingButton(
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val eventManager = remember { EventManager.getInstance(context) }
    
    var activeEvent by remember { mutableStateOf<SeasonalEvent?>(null) }
    
    LaunchedEffect(Unit) {
        val events = eventManager.getActiveEvents(LocalDate.now()).first()
        activeEvent = events.firstOrNull()
    }
    
    if (activeEvent == null) return
    
    val event = activeEvent!!
    
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_scale"
    )
    
    FloatingActionButton(
        onClick = onClick,
        containerColor = event.accentColor,
        contentColor = Color.White,
        modifier = Modifier.scale(scale)
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
            fontSize = 24.sp
        )
    }
}
