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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.sugarmunch.app.features.model.AchievementRarity
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.trading.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════
 * GIFT SCREEN
 * 
 * Send beautiful gifts to friends!
 * 
 * Features:
 * • Friend selector with search
 * • Your items list with rarity filters
 * • Gift wrapping selection with preview
 * • Custom message input
 * • Anonymous toggle
 * ═══════════════════════════════════════════════════════════════════
 */

enum class GiftStep {
    SELECT_FRIEND,
    SELECT_ITEMS,
    CUSTOMIZE,
    PREVIEW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftScreen(
    onBack: () -> Unit,
    onGiftSent: () -> Unit
) {
    val context = LocalContext.current
    val giftingManager = remember { GiftingManager.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    // State
    var currentStep by remember { mutableStateOf(GiftStep.SELECT_FRIEND) }
    var selectedFriend by remember { mutableStateOf<GiftFriend?>(null) }
    var selectedItems by remember { mutableStateOf<List<TradeItem>>(emptyList()) }
    var sugarPoints by remember { mutableStateOf(0) }
    var selectedWrapping by remember { mutableStateOf(GiftWrapping.CLASSIC) }
    var giftMessage by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }
    
    val remainingGifts by giftingManager.remainingGiftsToday.collectAsState()
    
    // Gift animation
    var showGiftAnimation by remember { mutableStateOf(false) }
    var animationProgress by remember { mutableStateOf(0f) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Send a Gift",
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
                    // Remaining gifts badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (remainingGifts > 0) colors.primary.copy(alpha = 0.2f) else colors.error.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎁", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "$remainingGifts left today",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (remainingGifts > 0) colors.primary else colors.error
                            )
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
            
            // Gift animation overlay
            if (showGiftAnimation) {
                GiftSendAnimation(
                    progress = animationProgress,
                    wrapping = selectedWrapping,
                    colors = colors
                )
            }
            
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Step indicator
                GiftStepIndicator(
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
                        label = "gift_step"
                    ) { step ->
                        when (step) {
                            GiftStep.SELECT_FRIEND -> GiftFriendSelector(
                                selectedFriend = selectedFriend,
                                onFriendSelect = { selectedFriend = it },
                                colors = colors
                            )
                            GiftStep.SELECT_ITEMS -> GiftItemSelector(
                                selectedItems = selectedItems,
                                onItemsChange = { selectedItems = it },
                                sugarPoints = sugarPoints,
                                onSugarPointsChange = { sugarPoints = it },
                                colors = colors
                            )
                            GiftStep.CUSTOMIZE -> GiftCustomization(
                                selectedWrapping = selectedWrapping,
                                onWrappingSelect = { selectedWrapping = it },
                                message = giftMessage,
                                onMessageChange = { giftMessage = it },
                                isAnonymous = isAnonymous,
                                onAnonymousChange = { isAnonymous = it },
                                colors = colors
                            )
                            GiftStep.PREVIEW -> GiftPreviewStep(
                                friend = selectedFriend,
                                items = selectedItems,
                                sugarPoints = sugarPoints,
                                wrapping = selectedWrapping,
                                message = giftMessage,
                                isAnonymous = isAnonymous,
                                colors = colors
                            )
                        }
                    }
                }
                
                // Navigation
                GiftNavigation(
                    currentStep = currentStep,
                    canProceed = when (currentStep) {
                        GiftStep.SELECT_FRIEND -> selectedFriend != null
                        GiftStep.SELECT_ITEMS -> selectedItems.isNotEmpty() || sugarPoints > 0
                        GiftStep.CUSTOMIZE -> true
                        GiftStep.PREVIEW -> true
                    },
                    onBack = {
                        if (currentStep == GiftStep.SELECT_FRIEND) onBack()
                        else currentStep = GiftStep.values()[currentStep.ordinal - 1]
                    },
                    onNext = {
                        if (currentStep == GiftStep.PREVIEW) {
                            // Send gift with animation
                            showGiftAnimation = true
                            scope.launch {
                                // Animate
                                animate(
                                    initialValue = 0f,
                                    targetValue = 1f,
                                    animationSpec = tween(1500)
                                ) { value, _ ->
                                    animationProgress = value
                                }
                                
                                // Send gift
                                selectedFriend?.let { friend ->
                                    giftingManager.sendGift(
                                        recipientId = friend.id,
                                        recipientName = friend.name,
                                        items = selectedItems,
                                        sugarPoints = sugarPoints,
                                        message = giftMessage.takeIf { it.isNotBlank() },
                                        wrapping = selectedWrapping,
                                        isAnonymous = isAnonymous
                                    )
                                }
                                
                                delay(500)
                                onGiftSent()
                            }
                        } else {
                            currentStep = GiftStep.values()[currentStep.ordinal + 1]
                        }
                    },
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun GiftStepIndicator(
    currentStep: GiftStep,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val steps = GiftStep.values()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        steps.forEachIndexed { index, step ->
            val isCompleted = index < currentStep.ordinal
            val isCurrent = index == currentStep.ordinal
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> colors.primary
                                isCurrent -> colors.primary.copy(alpha = 0.3f)
                                else -> colors.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        when {
                            isCompleted -> "✓"
                            else -> (index + 1).toString()
                        },
                        color = when {
                            isCompleted || isCurrent -> colors.onPrimary
                            else -> colors.onSurface.copy(alpha = 0.5f)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .height(2.dp)
                        .align(Alignment.CenterVertically)
                        .background(
                            if (isCompleted) colors.primary else colors.surfaceVariant
                        )
                )
            }
        }
    }
}

private data class GiftFriend(
    val id: String,
    val name: String,
    val avatar: String,
    val level: Int,
    val lastActive: String
)

@Composable
private fun GiftFriendSelector(
    selectedFriend: GiftFriend?,
    onFriendSelect: (GiftFriend) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val friends = remember {
        listOf(
            GiftFriend("1", "CandyQueen", "🍭", 25, "Online"),
            GiftFriend("2", "SugarRush", "🚀", 18, "2m ago"),
            GiftFriend("3", "GummyBear", "🧸", 12, "1h ago"),
            GiftFriend("4", "LollipopKid", "🍭", 8, "Online"),
            GiftFriend("5", "ChocoLover", "🍫", 30, "5m ago")
        )
    }
    
    val filteredFriends = friends.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search friends") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    focusedLabelColor = colors.primary
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        items(filteredFriends) { friend ->
            val isSelected = selectedFriend?.id == friend.id
            
            Card(
                onClick = { onFriendSelect(friend) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) 
                        colors.secondary.copy(alpha = 0.2f) 
                    else 
                        colors.surface.copy(alpha = 0.9f)
                ),
                border = if (isSelected) {
                    androidx.compose.foundation.BorderStroke(2.dp, colors.secondary)
                } else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colors.secondary.copy(alpha = 0.2f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                friend.avatar,
                                style = MaterialTheme.typography.headlineSmall
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
                            "Level ${friend.level} • ${friend.lastActive}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    if (isSelected) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colors.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GiftItemSelector(
    selectedItems: List<TradeItem>,
    onItemsChange: (List<TradeItem>) -> Unit,
    sugarPoints: Int,
    onSugarPointsChange: (Int) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val mockItems = remember {
        listOf(
            TradeItem("1", com.sugarmunch.app.shop.ShopItemType.THEME, 
                     "Cotton Candy", "🍭", AchievementRarity.RARE, previewColor = "#FFB6C1"),
            TradeItem("2", com.sugarmunch.app.shop.ShopItemType.EFFECT, 
                     "Rainbow Sparkle", "✨", AchievementRarity.EPIC, previewColor = "#FFD700"),
            TradeItem("3", com.sugarmunch.app.shop.ShopItemType.BADGE, 
                     "Sweet Tooth", "🦷", AchievementRarity.UNCOMMON, previewColor = "#98FF98"),
            TradeItem("4", com.sugarmunch.app.shop.ShopItemType.ICON, 
                     "Candy Jar", "🍬", AchievementRarity.COMMON, previewColor = "#FF69B4"),
            TradeItem("5", com.sugarmunch.app.shop.ShopItemType.THEME, 
                     "Dark Berry", "🫐", AchievementRarity.RARE, previewColor = "#9370DB"),
            TradeItem("6", com.sugarmunch.app.shop.ShopItemType.BADGE, 
                     "Legendary Collector", "👑", AchievementRarity.LEGENDARY, previewColor = "#FFD700")
        )
    }
    
    val yourBalance = 2500
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                "What would you like to gift?",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            // Sugar points slider
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
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
                        Text("Sugar Points", style = MaterialTheme.typography.bodyLarge)
                        Text("$sugarPoints 🍬", style = MaterialTheme.typography.titleMedium, color = colors.primary)
                    }
                    
                    Slider(
                        value = sugarPoints.toFloat(),
                        onValueChange = { onSugarPointsChange(it.toInt()) },
                        valueRange = 0f..yourBalance.toFloat().coerceAtMost(1000f),
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primary,
                            activeTrackColor = colors.primary
                        )
                    )
                    
                    Text("Balance: $yourBalance 🍬", style = MaterialTheme.typography.labelSmall, color = colors.onSurface.copy(alpha = 0.6f))
                }
            }
            
            Text(
                "Your Items",
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Items grid in lazy column using custom layout
        val rows = mockItems.chunked(3)
        items(rows) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    val isSelected = selectedItems.any { it.itemId == item.itemId }
                    
                    Card(
                        onClick = {
                            if (isSelected) {
                                onItemsChange(selectedItems.filter { it.itemId != item.itemId })
                            } else {
                                onItemsChange(selectedItems + item)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(item.itemIcon, style = MaterialTheme.typography.headlineMedium)
                                Text(item.itemName, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, maxLines = 1)
                            }
                            
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(colors.primary.copy(alpha = 0.3f))
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
                // Fill empty slots
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun GiftCustomization(
    selectedWrapping: GiftWrapping,
    onWrappingSelect: (GiftWrapping) -> Unit,
    message: String,
    onMessageChange: (String) -> Unit,
    isAnonymous: Boolean,
    onAnonymousChange: (Boolean) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val suggestions = remember { GiftingManager.GIFT_MESSAGE_SUGGESTIONS }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Wrapping selection
        item {
            Text(
                "Choose Wrapping",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(GiftWrapping.values().size) { index ->
                    val wrapping = GiftWrapping.values()[index]
                    val isSelected = selectedWrapping == wrapping
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onWrappingSelect(wrapping) }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) 
                                Color(android.graphics.Color.parseColor(wrapping.colorHex)).copy(alpha = 0.3f)
                            else 
                                colors.surface.copy(alpha = 0.9f),
                            border = if (isSelected) {
                                androidx.compose.foundation.BorderStroke(2.dp, 
                                    Color(android.graphics.Color.parseColor(wrapping.colorHex)))
                            } else null,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    wrapping.emoji,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            wrapping.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) colors.primary else colors.onSurface
                        )
                    }
                }
            }
        }
        
        // Message
        item {
            Text(
                "Add a Message",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Suggestion chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.take(5).forEach { suggestion ->
                    SuggestionChip(
                        onClick = { onMessageChange(suggestion) },
                        label = { Text(suggestion, maxLines = 1) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                label = { Text("Your message") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    focusedLabelColor = colors.primary
                )
            )
        }
        
        // Anonymous toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surface.copy(alpha = 0.9f)
                ),
                onClick = { onAnonymousChange(!isAnonymous) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isAnonymous) colors.tertiary.copy(alpha = 0.2f) else colors.surfaceVariant,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                if (isAnonymous) "🎭" else "👤",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Anonymous Gift",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Your friend won't know who sent it",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Switch(
                        checked = isAnonymous,
                        onCheckedChange = onAnonymousChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.tertiary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun GiftPreviewStep(
    friend: GiftFriend?,
    items: List<TradeItem>,
    sugarPoints: Int,
    wrapping: GiftWrapping,
    message: String,
    isAnonymous: Boolean,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Gift box preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                GiftBoxPreview(
                    wrapping = wrapping,
                    size = 150.dp,
                    isAnimated = true
                )
            }
        }
        
        item {
            // Recipient
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
                        color = colors.secondary.copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                if (isAnonymous) "🎭" else (friend?.avatar ?: "?"),
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            if (isAnonymous) "Mystery Recipient" else (friend?.name ?: "Unknown"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isAnonymous) "They won't know it's from you" else "Level ${friend?.level ?: 0}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Items summary
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
                    Text(
                        "Contains:",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    items.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(item.itemIcon, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(item.itemName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    
                    if (sugarPoints > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text("🍬", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$sugarPoints Sugar Points", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Message preview
        if (message.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(android.graphics.Color.parseColor(wrapping.colorHex)).copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Message:",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "\"$message\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GiftBoxPreview(
    wrapping: GiftWrapping,
    size: androidx.compose.ui.unit.Dp,
    isAnimated: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gift_float")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gift_scale"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gift_rotation"
    )
    
    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                if (isAnimated) {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(android.graphics.Color.parseColor(wrapping.colorHex)).copy(alpha = 0.3f),
            border = androidx.compose.foundation.BorderStroke(3.dp, 
                Color(android.graphics.Color.parseColor(wrapping.colorHex))),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    wrapping.emoji,
                    style = MaterialTheme.typography.displayLarge
                )
            }
        }
    }
}

@Composable
private fun GiftSendAnimation(
    progress: Float,
    wrapping: GiftWrapping,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val scale = 1f + progress * 2f
    val alpha = 1f - progress
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background.copy(alpha = 0.9f * alpha)),
        contentAlignment = Alignment.Center
    ) {
        // Gift box flying up
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                    translationY = -progress * 500f
                }
        ) {
            GiftBoxPreview(
                wrapping = wrapping,
                size = 120.dp,
                isAnimated = false
            )
        }
        
        // Sparkles
        if (progress > 0.3f) {
            repeat(8) { index ->
                val angle = (index * 45f) * (Math.PI / 180f)
                val distance = (progress - 0.3f) * 300f
                val x = kotlin.math.cos(angle).toFloat() * distance
                val y = kotlin.math.sin(angle).toFloat() * distance
                
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer {
                            translationX = x
                            translationY = y
                            this.alpha = 1f - (progress - 0.3f) * 2f
                        }
                        .clip(CircleShape)
                        .background(wrapping.colorHex.let { Color(android.graphics.Color.parseColor(it)) })
                )
            }
        }
    }
}

@Composable
private fun GiftNavigation(
    currentStep: GiftStep,
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
                Text(if (currentStep == GiftStep.SELECT_FRIEND) "Cancel" else "Back")
            }
            
            Button(
                onClick = onNext,
                enabled = canProceed,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.secondary
                )
            ) {
                Text(
                    when (currentStep) {
                        GiftStep.PREVIEW -> "🎁 Send Gift"
                        else -> "Next"
                    }
                )
            }
        }
    }
}

// Simplified FlowRow
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement
    ) {
        content()
    }
}
