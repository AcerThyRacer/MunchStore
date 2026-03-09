package com.sugarmunch.app.ui.screens.trading

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.features.model.AchievementRarity
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.trading.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * ═══════════════════════════════════════════════════════════════════
 * TRADING HUB SCREEN
 * 
 * Main trading interface with tabs:
 * • Active Trades - Pending offers sent/received
 * • History - Past trades and completed deals
 * • Marketplace - Community trade listings
 * ═══════════════════════════════════════════════════════════════════
 */

enum class TradeTab {
    ACTIVE_TRADES,
    HISTORY,
    MARKETPLACE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeScreen(
    onBack: () -> Unit,
    onCreateTrade: () -> Unit,
    onSendGift: () -> Unit,
    onBrowseMarket: () -> Unit
) {
    val context = LocalContext.current
    val tradeManager = remember { TradeManager.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    var selectedTab by remember { mutableStateOf(TradeTab.ACTIVE_TRADES) }
    val activeTrades by tradeManager.activeTrades.collectAsState()
    val tradeHistory by tradeManager.tradeHistory.collectAsState()
    val unreadCount by remember { 
        derivedStateOf { 
            activeTrades.count { 
                it.recipientId == "current_user_id" && it.status == TradeStatus.PENDING 
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Trading Hub",
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
                    // New trade button
                    FilledIconButton(
                        onClick = onCreateTrade,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = colors.primary
                        )
                    ) {
                        Icon(Icons.Default.Add, "New Trade")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == TradeTab.ACTIVE_TRADES) {
                FloatingActionButton(
                    onClick = onSendGift,
                    containerColor = colors.secondary,
                    contentColor = colors.onSecondary
                ) {
                    Icon(Icons.Default.CardGiftcard, "Send Gift")
                }
            }
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
            
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Tab selector
                TradeTabSelector(
                    selectedTab = selectedTab,
                    onTabSelect = { selectedTab = it },
                    unreadCount = unreadCount,
                    colors = colors
                )
                
                // Content
                when (selectedTab) {
                    TradeTab.ACTIVE_TRADES -> ActiveTradesContent(
                        activeTrades = activeTrades,
                        tradeManager = tradeManager,
                        colors = colors,
                        scope = scope
                    )
                    TradeTab.HISTORY -> TradeHistoryContent(
                        history = tradeHistory,
                        colors = colors
                    )
                    TradeTab.MARKETPLACE -> MarketplacePreviewContent(
                        onBrowseMarket = onBrowseMarket,
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
private fun TradeTabSelector(
    selectedTab: TradeTab,
    onTabSelect: (TradeTab) -> Unit,
    unreadCount: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(4.dp)
        ) {
            TradeTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                val animatedScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.02f else 1f,
                    label = "tab_scale"
                )
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .scale(animatedScale)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) colors.primary.copy(alpha = 0.2f)
                            else Color.Transparent
                        )
                        .clickable { onTabSelect(tab) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            when (tab) {
                                TradeTab.ACTIVE_TRADES -> "Active"
                                TradeTab.HISTORY -> "History"
                                TradeTab.MARKETPLACE -> "Market"
                            },
                            color = if (isSelected) colors.primary else colors.onSurface.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        
                        // Unread badge
                        if (tab == TradeTab.ACTIVE_TRADES && unreadCount > 0) {
                            Badge(
                                containerColor = colors.error
                            ) {
                                Text(
                                    unreadCount.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveTradesContent(
    activeTrades: List<TradeOffer>,
    tradeManager: TradeManager,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    scope: CoroutineScope
) {
    val json = remember { Json { ignoreUnknownKeys = true } }
    
    // Separate sent and received
    val received = activeTrades.filter { 
        it.recipientId == "current_user_id" && it.status == TradeStatus.PENDING 
    }
    val sent = activeTrades.filter { 
        it.senderId == "current_user_id" && it.status == TradeStatus.PENDING 
    }
    
    if (activeTrades.isEmpty()) {
        EmptyTradesState(colors = colors)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Received offers section
            if (received.isNotEmpty()) {
                item {
                    Text(
                        "📥 Received Offers",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(received) { trade ->
                    TradeCard(
                        trade = trade,
                        isReceived = true,
                        colors = colors,
                        onAccept = {
                            scope.launch {
                                tradeManager.acceptOffer(trade.id)
                            }
                        },
                        onDecline = {
                            scope.launch {
                                tradeManager.declineOffer(trade.id)
                            }
                        },
                        onCancel = null
                    )
                }
            }
            
            // Sent offers section
            if (sent.isNotEmpty()) {
                item {
                    Text(
                        "📤 Sent Offers",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                
                items(sent) { trade ->
                    TradeCard(
                        trade = trade,
                        isReceived = false,
                        colors = colors,
                        onAccept = null,
                        onDecline = null,
                        onCancel = {
                            scope.launch {
                                tradeManager.cancelOffer(trade.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TradeCard(
    trade: TradeOffer,
    isReceived: Boolean,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onAccept: (() -> Unit)?,
    onDecline: (() -> Unit)?,
    onCancel: (() -> Unit)?
) {
    val json = remember { Json { ignoreUnknownKeys = true } }
    val senderItems = remember { 
        json.decodeFromString<List<TradeItem>>(trade.senderItemsJson)
    }
    val recipientItems = remember {
        trade.recipientItemsJson?.let {
            json.decodeFromString<List<TradeItem>>(it)
        } ?: emptyList()
    }
    
    // Countdown timer for expiration
    var timeRemaining by remember { mutableStateOf(trade.expiresAt - System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (timeRemaining > 0) {
            delay(1000)
            timeRemaining = trade.expiresAt - System.currentTimeMillis()
        }
    }
    
    val hours = TimeUnit.MILLISECONDS.toHours(timeRemaining)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeRemaining) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeRemaining) % 60
    val timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    
    val isUrgent = timeRemaining < TimeUnit.HOURS.toMillis(1)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        border = if (trade.isGift) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                Brush.linearGradient(
                    listOf(colors.secondary, colors.tertiary)
                )
            )
        } else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Avatar
                    Surface(
                        shape = CircleShape,
                        color = colors.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                if (isReceived) trade.senderName.first().toString() else trade.recipientName.first().toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.primary
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            if (isReceived) "From: ${trade.senderName}" else "To: ${trade.recipientName}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            if (trade.isGift) "🎁 Gift" else "🤝 Trade",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (trade.isGift) colors.secondary else colors.primary
                        )
                    }
                }
                
                // Expiration timer
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isUrgent) colors.error.copy(alpha = 0.2f) 
                            else colors.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isUrgent) colors.error else colors.onSurfaceVariant
                        )
                        Text(
                            timeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isUrgent) colors.error else colors.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Items section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sender offers
                TradeItemsPreview(
                    items = senderItems,
                    sugarPoints = trade.senderSugarPoints,
                    label = if (isReceived) "They offer:" else "You offer:",
                    colors = colors
                )
                
                if (!trade.isGift && recipientItems.isNotEmpty()) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = colors.onSurface.copy(alpha = 0.5f)
                    )
                    
                    // Recipient offers
                    TradeItemsPreview(
                        items = recipientItems,
                        sugarPoints = trade.recipientSugarPoints,
                        label = if (isReceived) "You give:" else "They give:",
                        colors = colors
                    )
                }
            }
            
            // Message
            trade.message?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "\"$msg\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.7f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
            
            // Action buttons
            if (isReceived && onAccept != null && onDecline != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDecline,
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
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary
                        )
                    ) {
                        Text("Accept")
                    }
                }
            } else if (onCancel != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.error
                    )
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancel Trade")
                }
            }
        }
    }
}

@Composable
private fun TradeItemsPreview(
    items: List<TradeItem>,
    sugarPoints: Int,
    label: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Item icons
        Row(
            horizontalArrangement = Arrangement.spacedBy((-8).dp)
        ) {
            items.take(3).forEach { item ->
                Surface(
                    shape = CircleShape,
                    color = Color(android.graphics.Color.parseColor(item.previewColor)).copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        when (item.itemRarity) {
                            AchievementRarity.COMMON -> Color(0xFFCD7F32)
                            AchievementRarity.UNCOMMON -> Color(0xFFC0C0C0)
                            AchievementRarity.RARE -> Color(0xFFFFD700)
                            AchievementRarity.EPIC -> Color(0xFF9370DB)
                            AchievementRarity.LEGENDARY -> Color(0xFFFF1493)
                        }
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            item.itemIcon,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            if (items.size > 3) {
                Surface(
                    shape = CircleShape,
                    color = colors.surfaceVariant,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "+${items.size - 3}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurface
                        )
                    }
                }
            }
        }
        
        if (sugarPoints > 0) {
            Text(
                "+ $sugarPoints 🍬",
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary
            )
        }
    }
}

@Composable
private fun TradeHistoryContent(
    history: List<TradeHistoryEntry>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    if (history.isEmpty()) {
        EmptyHistoryState(colors = colors)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history) { entry ->
                HistoryCard(entry = entry, colors = colors)
            }
        }
    }
}

@Composable
private fun HistoryCard(
    entry: TradeHistoryEntry,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.8f)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status icon
                Surface(
                    shape = CircleShape,
                    color = when (entry.finalStatus) {
                        TradeStatus.ACCEPTED -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        TradeStatus.DECLINED -> Color(0xFFE57373).copy(alpha = 0.2f)
                        TradeStatus.CANCELLED -> Color(0xFFFFA726).copy(alpha = 0.2f)
                        else -> colors.surfaceVariant
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            when (entry.finalStatus) {
                                TradeStatus.ACCEPTED -> "✅"
                                TradeStatus.DECLINED -> "❌"
                                TradeStatus.CANCELLED -> "🚫"
                                else -> "⏰"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                Column {
                    Text(
                        if (entry.wasGift) "🎁 Gift ${if (entry.wasAnonymous) "(Anonymous)" else ""}" 
                        else "🤝 Trade",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        if (entry.senderId == "current_user_id") 
                            "To: ${entry.recipientName}"
                        else 
                            "From: ${entry.senderName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    SimpleDateFormat("MMM dd", Locale.getDefault())
                        .format(Date(entry.completedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
                if (entry.sugarPointsTransferred != 0) {
                    Text(
                        "${if (entry.sugarPointsTransferred > 0) "+" else ""}${entry.sugarPointsTransferred} 🍬",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (entry.sugarPointsTransferred > 0) Color(0xFF4CAF50) else colors.error
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketplacePreviewContent(
    onBrowseMarket: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Store,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = colors.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Community Marketplace",
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Browse trade listings from the community\nor create your own listings",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onBrowseMarket,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary
            )
        ) {
            Icon(Icons.Default.Store, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Browse Marketplace")
        }
    }
}

@Composable
private fun EmptyTradesState(colors: com.sugarmunch.app.theme.model.AdjustedColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SwapHoriz,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = colors.onSurface.copy(alpha = 0.3f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "No Active Trades",
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Start trading with friends!\nSend gifts or make trade offers.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyHistoryState(colors: com.sugarmunch.app.theme.model.AdjustedColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = colors.onSurface.copy(alpha = 0.3f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "No Trade History",
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Your completed trades will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}
