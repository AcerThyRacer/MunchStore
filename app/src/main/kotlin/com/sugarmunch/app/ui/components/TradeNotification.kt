package com.sugarmunch.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.trading.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════
 * TRADE NOTIFICATION COMPONENTS
 * 
 * In-app notifications for trading events:
 * • Popup when receiving trade offers
 * • Quick accept/decline actions
 * • Badge on trade icon
 * • Notification queue management
 * ═══════════════════════════════════════════════════════════════════
 */

/**
 * Trade notification badge for bottom nav
 */
@Composable
fun TradeNotificationBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_scale"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopEnd
    ) {
        // Main badge
        Box(
            modifier = Modifier
                .size(20.dp)
                .scale(if (count > 0) scale else 1f)
                .clip(CircleShape)
                .background(colors.error)
                .border(2.dp, colors.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (count > 9) "9+" else count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = if (count > 9) 8.dp.value.sp else 10.dp.value.sp
            )
        }
    }
}

// Extension to convert dp to sp (approximate)
private val Float.sp get() = this

/**
 * Trade notification popup - slides in from top
 */
@Composable
fun TradeNotificationPopup(
    notification: TradeNotification?,
    onAccept: (() -> Unit)? = null,
    onDecline: (() -> Unit)? = null,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
    ) {
        notification?.let { notif ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surface.copy(alpha = 0.98f)
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
                        // Icon based on type
                        Surface(
                            shape = CircleShape,
                            color = getNotificationColor(notif.type, colors).copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    getNotificationEmoji(notif.type),
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                notif.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Text(
                                notif.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface.copy(alpha = 0.8f),
                                maxLines = 2
                            )
                        }
                        
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Dismiss")
                        }
                    }
                    
                    // Quick actions for offers
                    if (notif.type == TradeNotificationType.OFFER_RECEIVED) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onDecline?.invoke()
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colors.error
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(colors.error.copy(alpha = 0.5f))
                                )
                            ) {
                                Text("Decline")
                            }
                            
                            Button(
                                onClick = {
                                    onAccept?.invoke()
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primary
                                )
                            ) {
                                Text("Accept")
                            }
                        }
                    }
                }
            }
            
            // Auto-dismiss after delay for non-actionable notifications
            LaunchedEffect(notif.id) {
                if (notif.type != TradeNotificationType.OFFER_RECEIVED) {
                    delay(5000)
                    onDismiss()
                }
            }
        }
    }
}

/**
 * Trade notification queue manager - handles multiple notifications
 */
@Composable
fun TradeNotificationManager(
    tradeManager: TradeManager,
    onNavigateToTrades: () -> Unit
) {
    val notifications by tradeManager.notifications.collectAsState()
    val unreadNotifications = notifications.filter { !it.isRead }
    
    // Show only the first unread notification
    val currentNotification = unreadNotifications.firstOrNull()
    
    TradeNotificationPopup(
        notification = currentNotification,
        onAccept = {
            currentNotification?.let { notif ->
                // Accept the trade
            }
        },
        onDecline = {
            currentNotification?.let { notif ->
                // Decline the trade
            }
        },
        onDismiss = {
            currentNotification?.let { notif ->
                tradeManager.markNotificationAsRead(notif.id)
            }
        }
    )
}

/**
 * Floating trade indicator - shows when new trades are available
 */
@Composable
fun FloatingTradeIndicator(
    pendingCount: Int,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    val scope = rememberCoroutineScope()
    
    // Bounce animation
    val infiniteTransition = rememberInfiniteTransition(label = "float_bounce")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    
    AnimatedVisibility(
        visible = pendingCount > 0,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .graphicsLayer { translationY = offsetY }
                .clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
            color = colors.primary,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Animated gift icon
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = -10f,
                        targetValue = 10f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "wiggle"
                    )
                    
                    Text(
                        "🎁",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.graphicsLayer { rotationZ = rotation }
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column {
                    Text(
                        "$pendingCount New Trade${if (pendingCount > 1) "s" else ""}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onPrimary
                    )
                    Text(
                        "Tap to view",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onPrimary.copy(alpha = 0.8f)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.onPrimary
                )
            }
        }
    }
}

/**
 * Gift received celebration overlay
 */
@Composable
fun GiftReceivedCelebration(
    isVisible: Boolean,
    giftWrapping: GiftWrapping = GiftWrapping.CLASSIC,
    onAnimationComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    var animationProgress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(2000)
            ) { value, _ ->
                animationProgress = value
            }
            onAnimationComplete()
        }
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f * (1 - animationProgress))),
            contentAlignment = Alignment.Center
        ) {
            // Central gift box
            val scale = 0.5f + animationProgress * 0.5f
            val alpha = if (animationProgress < 0.8f) 1f else 1f - (animationProgress - 0.8f) * 5f
            
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
            ) {
                // Gift box
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(android.graphics.Color.parseColor(giftWrapping.colorHex)),
                    modifier = Modifier.size(200.dp),
                    shadowElevation = 16.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            giftWrapping.emoji,
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                }
                
                // Sparkles
                if (animationProgress > 0.3f) {
                    val sparkleAlpha = (animationProgress - 0.3f) / 0.7f
                    repeat(12) { index ->
                        val angle = (index * 30f) * (Math.PI / 180f)
                        val distance = 150f + animationProgress * 100f
                        val x = kotlin.math.cos(angle).toFloat() * distance
                        val y = kotlin.math.sin(angle).toFloat() * distance
                        
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer {
                                    translationX = x
                                    translationY = y
                                    this.alpha = sparkleAlpha * (1f - animationProgress)
                                    scaleX = 1f + animationProgress
                                    scaleY = 1f + animationProgress
                                }
                                .clip(CircleShape)
                                .background(Color.White)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
            
            // Text
            if (animationProgress > 0.2f) {
                Text(
                    "🎁 Gift Received!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 100.dp)
                        .graphicsLayer {
                            this.alpha = if (animationProgress < 0.5f) 
                                (animationProgress - 0.2f) / 0.3f 
                            else 
                                1f - (animationProgress - 0.8f) * 5f
                        }
                )
            }
        }
    }
}

/**
 * Trade status toast - brief notification for trade actions
 */
@Composable
fun TradeStatusToast(
    message: String,
    type: ToastType = ToastType.SUCCESS,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(3000)
            onDismiss()
        }
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = when (type) {
                ToastType.SUCCESS -> Color(0xFF4CAF50)
                ToastType.ERROR -> colors.error
                ToastType.INFO -> colors.primary
                ToastType.WARNING -> Color(0xFFFFA726)
            },
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (type) {
                        ToastType.SUCCESS -> Icons.Default.CheckCircle
                        ToastType.ERROR -> Icons.Default.Error
                        ToastType.INFO -> Icons.Default.Info
                        ToastType.WARNING -> Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = Color.White
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

enum class ToastType {
    SUCCESS,
    ERROR,
    INFO,
    WARNING
}

// ═══════════════════════════════════════════════════════════════════
// HELPER FUNCTIONS
// ═══════════════════════════════════════════════════════════════════

private fun getNotificationColor(
    type: TradeNotificationType,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
): Color = when (type) {
    TradeNotificationType.OFFER_RECEIVED,
    TradeNotificationType.GIFT_RECEIVED -> colors.secondary
    TradeNotificationType.OFFER_ACCEPTED,
    TradeNotificationType.TRADE_COMPLETED -> Color(0xFF4CAF50)
    TradeNotificationType.OFFER_DECLINED,
    TradeNotificationType.OFFER_EXPIRED -> colors.error
    else -> colors.primary
}

private fun getNotificationEmoji(type: TradeNotificationType): String = when (type) {
    TradeNotificationType.OFFER_RECEIVED -> "🤝"
    TradeNotificationType.OFFER_ACCEPTED -> "✅"
    TradeNotificationType.OFFER_DECLINED -> "❌"
    TradeNotificationType.OFFER_CANCELLED -> "🚫"
    TradeNotificationType.OFFER_EXPIRED -> "⏰"
    TradeNotificationType.GIFT_RECEIVED -> "🎁"
    TradeNotificationType.GIFT_OPENED -> "📦"
    TradeNotificationType.LISTING_OFFER -> "💰"
    TradeNotificationType.TRADE_COMPLETED -> "🎉"
    TradeNotificationType.TRADE_REMINDER -> "⏳"
}

// ═══════════════════════════════════════════════════════════════════
// PREVIEW HELPERS
// ═══════════════════════════════════════════════════════════════════

@Composable
fun PreviewTradeNotifications() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Badge example
        Box {
            Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(32.dp))
            TradeNotificationBadge(count = 3, modifier = Modifier.align(Alignment.TopEnd))
        }
        
        // Popup examples
        TradeNotificationPopup(
            notification = TradeNotification(
                userId = "1",
                tradeId = "1",
                type = TradeNotificationType.OFFER_RECEIVED,
                title = "New Trade Offer!",
                message = "CandyQueen wants to trade with you"
            ),
            onDismiss = {}
        )
        
        TradeNotificationPopup(
            notification = TradeNotification(
                userId = "1",
                tradeId = "2",
                type = TradeNotificationType.GIFT_RECEIVED,
                title = "🎁 You received a gift!",
                message = "Someone sent you a mysterious gift!"
            ),
            onDismiss = {}
        )
    }
}
