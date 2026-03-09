package com.sugarmunch.app.shop.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sugarmunch.app.features.model.Rarity
import com.sugarmunch.app.shop.*
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    onBack: () -> Unit,
    onSugarPassClick: () -> Unit = {},
    onMarketplaceClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val shopManager = remember { ShopManager.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    val sugarPoints by shopManager.sugarPoints.collectAsState(initial = 0)
    val userLevel by shopManager.userLevel.collectAsState(initial = 1)
    val userXP by shopManager.userXP.collectAsState(initial = 0)
    val inventory by shopManager.inventory.collectAsState(initial = emptyList())
    
    var selectedCategory by remember { mutableStateOf<ShopItemType?>(null) }
    var showPurchaseDialog by remember { mutableStateOf<ShopItem?>(null) }
    var purchaseResult by remember { mutableStateOf<PurchaseResult?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }
    
    val dailyDeal = remember { shopManager.getDailyDeal() }
    val featuredItems = remember { ShopCatalog.getFeaturedItems() }
    val newItems = remember { ShopCatalog.getNewItems() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Sugar Shop",
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
                    // Sugar Points Display
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.primary.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🍬", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "$sugarPoints",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.primary,
                                fontWeight = FontWeight.Bold
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

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Level & XP Bar
                ShopLevelBar(
                    level = userLevel,
                    xp = userXP,
                    xpForNextLevel = shopManager.getXPForNextLevel(userLevel),
                    colors = colors
                )

                // Sugar Pass entry
                Card(
                        onClick = onSugarPassClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.primary.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🍭", style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Sugar Pass",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.onSurface
                                )
                                Text(
                                    "Earn Sugar Crystals, claim rewards",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurface.copy(alpha = 0.8f)
                                )
                            }
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = colors.onSurface)
                        }
                    }
                }
                Card(
                    onClick = onMarketplaceClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.secondary.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("\uD83D\uDCE6", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Trading",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.onSurface
                            )
                            Text(
                                "Trade themes, badges, and more",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface.copy(alpha = 0.8f)
                            )
                        }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = colors.onSurface)
                    }
                }

                // Category Chips
                ShopCategoryChips(
                    selectedCategory = selectedCategory,
                    onCategorySelect = { selectedCategory = it },
                    colors = colors
                )

                // Content
                when (selectedCategory) {
                    null -> {
                        // Show featured/daily deals
                        ShopFeaturedContent(
                            dailyDeal = dailyDeal,
                            featuredItems = featuredItems,
                            newItems = newItems,
                            sugarPoints = sugarPoints,
                            onItemClick = { showPurchaseDialog = it },
                            colors = colors
                        )
                    }
                    else -> {
                        // Show category items
                        ShopCategoryGrid(
                            items = ShopCatalog.getItemsByType(selectedCategory!!),
                            sugarPoints = sugarPoints,
                            inventory = inventory,
                            onItemClick = { showPurchaseDialog = it },
                            colors = colors
                        )
                    }
                }

                // Purchase Dialog
                if (showPurchaseDialog != null) {
                    val item = showPurchaseDialog!!
                    PurchaseConfirmationDialog(
                        item = item,
                        sugarPoints = sugarPoints,
                        onConfirm = {
                            scope.launch {
                                val result = shopManager.purchaseItem(item)
                                purchaseResult = result
                                showPurchaseDialog = null
                                showResultDialog = true
                            }
                        },
                        onDismiss = { showPurchaseDialog = null },
                        colors = colors
                    )
                }

                // Result Dialog
                if (showResultDialog) {
                    PurchaseResultDialog(
                        result = purchaseResult,
                        onDismiss = {
                            showResultDialog = false
                            purchaseResult = null
                        },
                        colors = colors
                    )
                }
            }
        }
    }
@Composable
private fun ShopLevelBar(
    level: Int,
    xp: Int,
    xpForNextLevel: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val xpProgress = (xp % 1000) / 1000f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = colors.primary
                    ) {
                        Text(
                            "Lv. $level",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Shop Level",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            "$xp / $xpForNextLevel XP",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { xpProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = colors.primary,
                trackColor = colors.surfaceVariant
            )
        }
    }
}

@Composable
private fun ShopCategoryChips(
    selectedCategory: ShopItemType?,
    onCategorySelect: (ShopItemType?) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val categories = listOf(
        null to "Home",
        ShopItemType.THEME to "Themes",
        ShopItemType.EFFECT to "Effects",
        ShopItemType.BADGE to "Badges",
        ShopItemType.ICON to "Icons",
        ShopItemType.BOOST to "Boosts",
        ShopItemType.BUNDLE to "Bundles"
    )

    ScrollableTabRow(
        selectedTabIndex = categories.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0),
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.Transparent,
        contentColor = colors.primary,
        edgePadding = 16.dp
    ) {
        categories.forEach { (category, label) ->
            Tab(
                selected = selectedCategory == category,
                onClick = { onCategorySelect(category) },
                text = {
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            )
        }
    }
}

@Composable
private fun ShopFeaturedContent(
    dailyDeal: DailyDeal?,
    featuredItems: List<ShopItem>,
    newItems: List<ShopItem>,
    sugarPoints: Int,
    onItemClick: (ShopItem) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Daily Deal
        dailyDeal?.let { deal ->
            ShopCatalog.getItemById(deal.itemId)?.let { item ->
                Text(
                    "Daily Deal - ${((deal.expiresAt - System.currentTimeMillis()) / (1000 * 60 * 60)).toInt()}h left",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                DailyDealCard(
                    item = item,
                    deal = deal,
                    canAfford = sugarPoints >= deal.salePrice,
                    onClick = { onItemClick(item) },
                    colors = colors
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Featured Items
        if (featuredItems.isNotEmpty()) {
            Text(
                "Featured Items",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(featuredItems) { item ->
                    ShopItemCard(
                        item = item,
                        canAfford = sugarPoints >= item.cost,
                        isOwned = false,
                        onClick = { onItemClick(item) },
                        colors = colors,
                        compact = true
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // New Items
        if (newItems.isNotEmpty()) {
            Text(
                "New Arrivals",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(newItems) { item ->
                    ShopItemCard(
                        item = item,
                        canAfford = sugarPoints >= item.cost,
                        isOwned = false,
                        onClick = { onItemClick(item) },
                        colors = colors,
                        compact = true
                    )
                }
            }
        }
    }
}

@Composable
private fun ShopCategoryGrid(
    items: List<ShopItem>,
    sugarPoints: Int,
    inventory: List<InventoryItem>,
    onItemClick: (ShopItem) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            val isOwned = inventory.any { it.itemId == item.id }
            ShopItemCard(
                item = item,
                canAfford = sugarPoints >= item.cost,
                isOwned = isOwned,
                onClick = { onItemClick(item) },
                colors = colors
            )
        }
    }
}

@Composable
private fun ShopItemCard(
    item: ShopItem,
    canAfford: Boolean,
    isOwned: Boolean,
    onClick: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    compact: Boolean = false
) {
    val rarityColor = when (item.rarity) {
        Rarity.COMMON -> Color(0xFFCD7F32)
        Rarity.UNCOMMON -> Color(0xFFC0C0C0)
        Rarity.RARE -> Color(0xFFFFD700)
        Rarity.EPIC -> Color(0xFF9370DB)
        Rarity.LEGENDARY -> Color(0xFFFF1493)
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (compact) Modifier.height(140.dp) else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOwned) 
                colors.surface.copy(alpha = 0.5f) 
            else 
                colors.surface.copy(alpha = 0.95f)
        ),
        border = if (item.rarity.ordinal >= Rarity.RARE.ordinal) {
            androidx.compose.foundation.BorderStroke(2.dp, rarityColor.copy(alpha = 0.5f))
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(android.graphics.Color.parseColor(item.previewColor)).copy(alpha = 0.3f)
                ) {
                    Text(
                        item.icon,
                        modifier = Modifier.padding(12.dp),
                        style = if (compact) 
                            MaterialTheme.typography.titleMedium 
                        else 
                            MaterialTheme.typography.headlineSmall
                    )
                }
                
                // Owned indicator or Rarity
                if (isOwned) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                    ) {
                        Text(
                            "OWNED",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                } else if (item.rarity.ordinal >= Rarity.RARE.ordinal) {
                    Text(
                        when (item.rarity) {
                            Rarity.RARE -> "⭐"
                            Rarity.EPIC -> "💎"
                            Rarity.LEGENDARY -> "👑"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Column {
                Text(
                    item.name,
                    style = if (compact) 
                        MaterialTheme.typography.bodyMedium 
                    else 
                        MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (!compact) {
                    Text(
                        item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
                
                if (!isOwned) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🍬", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${item.cost}",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (canAfford) colors.primary else colors.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyDealCard(
    item: ShopItem,
    deal: DailyDeal,
    canAfford: Boolean,
    onClick: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.primary.copy(alpha = 0.15f)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, colors.primary.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(android.graphics.Color.parseColor(item.previewColor)).copy(alpha = 0.3f)
            ) {
                Text(
                    item.icon,
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${deal.originalPrice} 🍬",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface.copy(alpha = 0.5f),
                        textDecoration = TextDecoration.LineThrough
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${deal.salePrice} 🍬",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (canAfford) Color(0xFF4CAF50) else colors.error,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4CAF50)
                    ) {
                        Text(
                            "-${deal.discountPercent}%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchaseConfirmationDialog(
    item: ShopItem,
    sugarPoints: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.icon, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Purchase ${item.name}?")
            }
        },
        text = {
            Column {
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Your Sugar Points:", style = MaterialTheme.typography.bodyMedium)
                    Text("$sugarPoints 🍬", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Cost:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${item.cost} 🍬",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (sugarPoints >= item.cost) colors.primary else colors.error
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("After purchase:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${(sugarPoints - item.cost).coerceAtLeast(0)} 🍬",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = sugarPoints >= item.cost,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary
                )
            ) {
                Text("Buy Now")
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

@Composable
private fun PurchaseResultDialog(
    result: PurchaseResult?,
    onDismiss: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val (title, message, icon) = when (result) {
        PurchaseResult.SUCCESS -> Triple(
            "Purchase Successful!",
            "Item added to your inventory!",
            "✅"
        )
        PurchaseResult.FAILURE_INSUFFICIENT_FUNDS -> Triple(
            "Not Enough Sugar Points",
            "Complete achievements or check in daily to earn more!",
            "❌"
        )
        PurchaseResult.FAILURE_REQUIREMENTS -> Triple(
            "Requirements Not Met",
            "You don't meet the requirements for this item yet.",
            "🔒"
        )
        PurchaseResult.FAILURE_ALREADY_OWNED -> Triple(
            "Already Owned",
            "You already have this item in your inventory!",
            "✓"
        )
        PurchaseResult.FAILURE_OUT_OF_STOCK -> Triple(
            "Out of Stock",
            "This limited item is no longer available.",
            "📦"
        )
        else -> Triple("", "", "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title)
            }
        },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary
                )
            ) {
                Text("OK")
            }
        },
        containerColor = colors.surface
    )
}
