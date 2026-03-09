package com.sugarmunch.app.ui.screens.trading

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.features.model.AchievementRarity
import com.sugarmunch.app.shop.ShopManager
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.trading.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * ═══════════════════════════════════════════════════════════════════
 * CREATE TRADE SCREEN
 * 
 * Create a new trade offer with:
 * • Friend selector
 * • Your items (what you offer)
 * • Request items (what you want)
 * • Sugar Points addition
 * • Preview and confirm
 * ═══════════════════════════════════════════════════════════════════
 */

enum class CreateTradeStep {
    SELECT_FRIEND,
    SELECT_YOUR_ITEMS,
    SELECT_THEIR_ITEMS,
    ADD_SUGAR_POINTS,
    REVIEW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTradeScreen(
    onBack: () -> Unit,
    onTradeCreated: () -> Unit
) {
    val context = LocalContext.current
    val tradeManager = remember { TradeManager.getInstance(context) }
    val shopManager = remember { ShopManager.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    // State
    var currentStep by remember { mutableStateOf(CreateTradeStep.SELECT_FRIEND) }
    var selectedFriend by remember { mutableStateOf<Friend?>(null) }
    var yourItems by remember { mutableStateOf<List<TradeItem>>(emptyList()) }
    var theirItems by remember { mutableStateOf<List<TradeItem>>(emptyList()) }
    var yourSugarPoints by remember { mutableStateOf(0) }
    var theirSugarPoints by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("") }
    
    // Validation
    val canProceed = when (currentStep) {
        CreateTradeStep.SELECT_FRIEND -> selectedFriend != null
        CreateTradeStep.SELECT_YOUR_ITEMS -> yourItems.isNotEmpty() || yourSugarPoints > 0
        CreateTradeStep.SELECT_THEIR_ITEMS -> true // Optional
        CreateTradeStep.ADD_SUGAR_POINTS -> true // Optional
        CreateTradeStep.REVIEW -> yourItems.isNotEmpty() || yourSugarPoints > 0
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "New Trade",
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
                // Progress indicator
                TradeStepProgress(
                    currentStep = currentStep,
                    colors = colors
                )
                
                // Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            (fadeIn() + slideInHorizontally { it / 4 })
                                .togetherWith(fadeOut() + slideOutHorizontally { -it / 4 })
                        },
                        label = "step_content"
                    ) { step ->
                        when (step) {
                            CreateTradeStep.SELECT_FRIEND -> FriendSelectorStep(
                                selectedFriend = selectedFriend,
                                onFriendSelect = { selectedFriend = it },
                                colors = colors
                            )
                            CreateTradeStep.SELECT_YOUR_ITEMS -> SelectItemsStep(
                                title = "What you're offering",
                                subtitle = "Select items from your inventory",
                                selectedItems = yourItems,
                                onItemsChange = { yourItems = it },
                                colors = colors
                            )
                            CreateTradeStep.SELECT_THEIR_ITEMS -> SelectItemsStep(
                                title = "What you want",
                                subtitle = "Request items (optional)",
                                selectedItems = theirItems,
                                onItemsChange = { theirItems = it },
                                colors = colors,
                                isRequestMode = true
                            )
                            CreateTradeStep.ADD_SUGAR_POINTS -> SugarPointsStep(
                                yourPoints = yourSugarPoints,
                                theirPoints = theirSugarPoints,
                                onYourPointsChange = { yourSugarPoints = it },
                                onTheirPointsChange = { theirSugarPoints = it },
                                message = message,
                                onMessageChange = { message = it },
                                colors = colors
                            )
                            CreateTradeStep.REVIEW -> ReviewStep(
                                friend = selectedFriend,
                                yourItems = yourItems,
                                theirItems = theirItems,
                                yourSugarPoints = yourSugarPoints,
                                theirSugarPoints = theirSugarPoints,
                                message = message,
                                colors = colors
                            )
                        }
                    }
                }
                
                // Navigation buttons
                TradeNavigationButtons(
                    currentStep = currentStep,
                    canProceed = canProceed,
                    onBack = {
                        if (currentStep == CreateTradeStep.SELECT_FRIEND) {
                            onBack()
                        } else {
                            currentStep = CreateTradeStep.values()[currentStep.ordinal - 1]
                        }
                    },
                    onNext = {
                        if (currentStep == CreateTradeStep.REVIEW) {
                            // Submit trade
                            scope.launch {
                                selectedFriend?.let { friend ->
                                    val result = tradeManager.createOffer(
                                        recipientId = friend.id,
                                        recipientName = friend.name,
                                        senderItems = yourItems,
                                        recipientItems = theirItems,
                                        senderSugarPoints = yourSugarPoints,
                                        message = message.takeIf { it.isNotBlank() }
                                    )
                                    if (result is TradeResult.Success) {
                                        onTradeCreated()
                                    }
                                }
                            }
                        } else {
                            currentStep = CreateTradeStep.values()[currentStep.ordinal + 1]
                        }
                    },
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun TradeStepProgress(
    currentStep: CreateTradeStep,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val steps = CreateTradeStep.values()
    val progress = (currentStep.ordinal + 1).toFloat() / steps.size
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = colors.primary,
            trackColor = colors.surfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Step ${currentStep.ordinal + 1} of ${steps.size}: ${getStepName(currentStep)}",
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurface.copy(alpha = 0.7f)
        )
    }
}

private fun getStepName(step: CreateTradeStep): String = when (step) {
    CreateTradeStep.SELECT_FRIEND -> "Select Friend"
    CreateTradeStep.SELECT_YOUR_ITEMS -> "Your Items"
    CreateTradeStep.SELECT_THEIR_ITEMS -> "Request Items"
    CreateTradeStep.ADD_SUGAR_POINTS -> "Sugar Points"
    CreateTradeStep.REVIEW -> "Review"
}

// Mock friend data
private data class Friend(
    val id: String,
    val name: String,
    val avatar: String,
    val level: Int,
    val isOnline: Boolean
)

@Composable
private fun FriendSelectorStep(
    selectedFriend: Friend?,
    onFriendSelect: (Friend) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    // Mock friends list
    val friends = remember {
        listOf(
            Friend("1", "CandyQueen", "🍭", 25, true),
            Friend("2", "SugarRush", "🚀", 18, false),
            Friend("3", "GummyBear", "🧸", 12, true),
            Friend("4", "LollipopKid", "🍭", 8, true),
            Friend("5", "ChocoLover", "🍫", 30, false),
            Friend("6", "MintyFresh", "🌿", 15, true)
        )
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Who do you want to trade with?",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(friends) { friend ->
            val isSelected = selectedFriend?.id == friend.id
            
            Card(
                onClick = { onFriendSelect(friend) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) 
                        colors.primary.copy(alpha = 0.2f) 
                    else 
                        colors.surface.copy(alpha = 0.9f)
                ),
                border = if (isSelected) {
                    androidx.compose.foundation.BorderStroke(2.dp, colors.primary)
                } else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            shape = CircleShape,
                            color = colors.primary.copy(alpha = 0.2f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    friend.avatar,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }
                        
                        // Online indicator
                        if (friend.isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                                    .border(2.dp, colors.surface, CircleShape)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            friend.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            "Level ${friend.level}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    if (isSelected) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colors.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectItemsStep(
    title: String,
    subtitle: String,
    selectedItems: List<TradeItem>,
    onItemsChange: (List<TradeItem>) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    isRequestMode: Boolean = false
) {
    // Mock inventory
    val mockItems = remember {
        listOf(
            TradeItem(itemId = "1", itemType = com.sugarmunch.app.shop.ShopItemType.THEME, 
                     itemName = "Cotton Candy", itemIcon = "🍭", itemRarity = AchievementRarity.RARE,
                     previewColor = "#FFB6C1"),
            TradeItem(itemId = "2", itemType = com.sugarmunch.app.shop.ShopItemType.EFFECT, 
                     itemName = "Rainbow Sparkle", itemIcon = "✨", itemRarity = AchievementRarity.EPIC,
                     previewColor = "#FFD700"),
            TradeItem(itemId = "3", itemType = com.sugarmunch.app.shop.ShopItemType.BADGE, 
                     itemName = "Sweet Tooth", itemIcon = "🦷", itemRarity = AchievementRarity.UNCOMMON,
                     previewColor = "#98FF98"),
            TradeItem(itemId = "4", itemType = com.sugarmunch.app.shop.ShopItemType.ICON, 
                     itemName = "Candy Jar", itemIcon = "🍬", itemRarity = AchievementRarity.COMMON,
                     previewColor = "#FF69B4"),
            TradeItem(itemId = "5", itemType = com.sugarmunch.app.shop.ShopItemType.THEME, 
                     itemName = "Dark Berry", itemIcon = "🫐", itemRarity = AchievementRarity.RARE,
                     previewColor = "#9370DB")
        )
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.7f)
            )
            
            // Selected items chips
            if (selectedItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedItems.forEach { item ->
                        InputChip(
                            selected = true,
                            onClick = { onItemsChange(selectedItems - item) },
                            label = { Text(item.itemName) },
                            avatar = { Text(item.itemIcon) },
                            trailingIcon = {
                                Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                            }
                        )
                    }
                }
            }
        }
        
        // Items grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mockItems) { item ->
                val isSelected = selectedItems.any { it.itemId == item.itemId }
                
                Card(
                    onClick = {
                        if (isSelected) {
                            onItemsChange(selectedItems.filter { it.itemId != item.itemId })
                        } else {
                            onItemsChange(selectedItems + item)
                        }
                    },
                    modifier = Modifier.aspectRatio(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) 
                            colors.primary.copy(alpha = 0.3f)
                        else 
                            colors.surface.copy(alpha = 0.9f)
                    ),
                    border = when (item.itemRarity) {
                        AchievementRarity.LEGENDARY -> androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF1493))
                        AchievementRarity.EPIC -> androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF9370DB))
                        AchievementRarity.RARE -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                        else -> null
                    }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                item.itemIcon,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                item.itemName,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                        
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(colors.primary.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.align(Alignment.Center)
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
private fun SugarPointsStep(
    yourPoints: Int,
    theirPoints: Int,
    onYourPointsChange: (Int) -> Unit,
    onTheirPointsChange: (Int) -> Unit,
    message: String,
    onMessageChange: (String) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val yourSugarPoints = 2500 // Mock current balance
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Add Sugar Points",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Sweeten the deal with Sugar Points",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.7f)
            )
        }
        
        // Your points offer
        item {
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "You offer:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurface
                        )
                        Text(
                            "$yourPoints 🍬",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Slider(
                        value = yourPoints.toFloat(),
                        onValueChange = { onYourPointsChange(it.toInt()) },
                        valueRange = 0f..yourSugarPoints.toFloat().coerceAtMost(1000f),
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primary,
                            activeTrackColor = colors.primary
                        )
                    )
                    
                    Text(
                        "Balance: $yourSugarPoints 🍬",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        // Their points request
        item {
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "You request:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurface
                        )
                        Text(
                            "$theirPoints 🍬",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Slider(
                        value = theirPoints.toFloat(),
                        onValueChange = { onTheirPointsChange(it.toInt()) },
                        valueRange = 0f..500f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.secondary,
                            activeTrackColor = colors.secondary
                        )
                    )
                }
            }
        }
        
        // Message
        item {
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                label = { Text("Add a message (optional)") },
                placeholder = { Text("Hey! Want to trade?") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    focusedLabelColor = colors.primary
                )
            )
        }
    }
}

@Composable
private fun ReviewStep(
    friend: Friend?,
    yourItems: List<TradeItem>,
    theirItems: List<TradeItem>,
    yourSugarPoints: Int,
    theirSugarPoints: Int,
    message: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Review Trade",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Trading with
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surface.copy(alpha = 0.9f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colors.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                friend?.avatar ?: "?",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            "Trading with",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            friend?.name ?: "Unknown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    }
                }
            }
        }
        
        // You give
        item {
            TradeSummaryCard(
                title = "You give:",
                items = yourItems,
                sugarPoints = yourSugarPoints,
                colors = colors,
                isOutgoing = true
            )
        }
        
        // Arrow
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = colors.primary.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.SwapVert,
                            contentDescription = null,
                            tint = colors.primary
                        )
                    }
                }
            }
        }
        
        // You receive
        item {
            if (theirItems.isNotEmpty() || theirSugarPoints > 0) {
                TradeSummaryCard(
                    title = "You receive:",
                    items = theirItems,
                    sugarPoints = theirSugarPoints,
                    colors = colors,
                    isOutgoing = false
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surface.copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "🎁 Gift - Nothing requested in return",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        // Message
        if (message.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        "\"$message\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = colors.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TradeSummaryCard(
    title: String,
    items: List<TradeItem>,
    sugarPoints: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    isOutgoing: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOutgoing) 
                colors.surface.copy(alpha = 0.9f)
            else 
                colors.primary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Items
            items.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(android.graphics.Color.parseColor(item.previewColor)).copy(alpha = 0.3f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(item.itemIcon, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            item.itemName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            item.itemRarity.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (item.itemRarity) {
                                AchievementRarity.LEGENDARY -> Color(0xFFFF1493)
                                AchievementRarity.EPIC -> Color(0xFF9370DB)
                                AchievementRarity.RARE -> Color(0xFFFFD700)
                                else -> colors.onSurface.copy(alpha = 0.6f)
                            }
                        )
                    }
                }
            }
            
            // Sugar points
            if (sugarPoints > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isOutgoing) colors.primary.copy(alpha = 0.2f) else colors.secondary.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🍬", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        "$sugarPoints Sugar Points",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TradeNavigationButtons(
    currentStep: CreateTradeStep,
    canProceed: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surface.copy(alpha = 0.95f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (currentStep == CreateTradeStep.SELECT_FRIEND) "Cancel" else "Back")
            }
            
            Button(
                onClick = onNext,
                enabled = canProceed,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary
                )
            ) {
                Text(
                    if (currentStep == CreateTradeStep.REVIEW) "Send Trade" else "Next"
                )
            }
        }
    }
}

// FlowRow implementation for selected items
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Simplified - in production use proper FlowRow from accompanist
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement
    ) {
        content()
    }
}
