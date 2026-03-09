package com.sugarmunch.app.ui.screens.trading

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.features.model.AchievementRarity
import com.sugarmunch.app.shop.ShopItemType
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.trading.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * ═══════════════════════════════════════════════════════════════════
 * MARKETPLACE SCREEN
 * 
 * Community marketplace for trading items!
 * 
 * Features:
 * • Browse trade listings
 * • Filter by Have/Want/Category/Rarity
 * • Listing cards with details
 * • Make offer functionality
 * • Create your own listings
 * • My listings management
 * ═══════════════════════════════════════════════════════════════════
 */

enum class MarketplaceTab {
    BROWSE,
    MY_LISTINGS,
    MY_OFFERS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    onBack: () -> Unit,
    onCreateListing: () -> Unit = {}
) {
    val context = LocalContext.current
    val marketManager = remember { MarketManager.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    var selectedTab by remember { mutableStateOf(MarketplaceTab.BROWSE) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val listings by marketManager.allListings.collectAsState()
    val myListings by marketManager.myListings.collectAsState()
    val filteredListings by marketManager.filteredListings.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Marketplace",
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
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateListing,
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            ) {
                Icon(Icons.Default.Add, "Create Listing")
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
                // Search bar
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { 
                        searchQuery = it
                        // Trigger search
                    },
                    colors = colors
                )
                
                // Tab selector
                MarketplaceTabSelector(
                    selectedTab = selectedTab,
                    onTabSelect = { selectedTab = it },
                    myListingsCount = myListings.count { it.isActive && !it.isExpired },
                    colors = colors
                )
                
                // Content
                when (selectedTab) {
                    MarketplaceTab.BROWSE -> BrowseListingsContent(
                        listings = if (searchQuery.isBlank()) listings else filteredListings,
                        searchQuery = searchQuery,
                        marketManager = marketManager,
                        colors = colors,
                        scope = scope
                    )
                    MarketplaceTab.MY_LISTINGS -> MyListingsContent(
                        listings = myListings,
                        marketManager = marketManager,
                        colors = colors
                    )
                    MarketplaceTab.MY_OFFERS -> MyOffersContent(
                        colors = colors
                    )
                }
            }
        }
    }
    
    // Filter bottom sheet
    if (showFilterSheet) {
        FilterBottomSheet(
            onDismiss = { showFilterSheet = false },
            colors = colors
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search listings...") },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, null)
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary,
            focusedLabelColor = colors.primary,
            unfocusedContainerColor = colors.surface.copy(alpha = 0.8f)
        )
    )
}

@Composable
private fun MarketplaceTabSelector(
    selectedTab: MarketplaceTab,
    onTabSelect: (MarketplaceTab) -> Unit,
    myListingsCount: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(4.dp)
        ) {
            MarketplaceTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                val count = when (tab) {
                    MarketplaceTab.MY_LISTINGS -> myListingsCount
                    else -> null
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            when (tab) {
                                MarketplaceTab.BROWSE -> "Browse"
                                MarketplaceTab.MY_LISTINGS -> "My Listings"
                                MarketplaceTab.MY_OFFERS -> "My Offers"
                            },
                            color = if (isSelected) colors.primary else colors.onSurface.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        
                        count?.let {
                            Badge(
                                containerColor = if (isSelected) colors.primary else colors.surfaceVariant
                            ) {
                                Text(it.toString(), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrowseListingsContent(
    listings: List<TradeListing>,
    searchQuery: String,
    marketManager: MarketManager,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    scope: CoroutineScope
) {
    if (listings.isEmpty()) {
        EmptyMarketplaceState(
            message = if (searchQuery.isBlank()) 
                "No listings yet. Be the first to create one!" 
            else 
                "No results found for \"$searchQuery\"",
            colors = colors
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Featured/Rare listings header
            val rareListings = listings.filter { 
                it.rarityFilter == AchievementRarity.EPIC || it.rarityFilter == AchievementRarity.LEGENDARY 
            }.take(3)
            
            if (rareListings.isNotEmpty() && searchQuery.isBlank()) {
                item {
                    Text(
                        "🔥 Hot Listings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(rareListings) { listing ->
                    FeaturedListingCard(
                        listing = listing,
                        marketManager = marketManager,
                        colors = colors,
                        scope = scope
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "📦 All Listings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            
            items(listings.filter { it !in rareListings }) { listing ->
                ListingCard(
                    listing = listing,
                    marketManager = marketManager,
                    colors = colors,
                    scope = scope
                )
            }
        }
    }
}

@Composable
private fun ListingCard(
    listing: TradeListing,
    marketManager: MarketManager,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    scope: CoroutineScope
) {
    val json = remember { Json { ignoreUnknownKeys = true } }
    val haveItems = remember { 
        json.decodeFromString<List<TradeItem>>(listing.haveItemsJson)
    }
    val wantItems = listing.wantItemsJson?.let {
        json.decodeFromString<List<TradeItem>>(it)
    }
    
    // Time remaining
    val timeRemaining = listing.expiresAt - System.currentTimeMillis()
    val daysRemaining = TimeUnit.MILLISECONDS.toDays(timeRemaining)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        border = when (listing.rarityFilter) {
            AchievementRarity.LEGENDARY -> androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF1493))
            AchievementRarity.EPIC -> androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF9370DB))
            AchievementRarity.RARE -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
            else -> null
        },
        onClick = { /* View details */ }
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
                    Surface(
                        shape = CircleShape,
                        color = colors.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                listing.creatorAvatar,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            listing.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "by ${listing.creatorName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Listing type badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (listing.listingType) {
                        ListingType.HAVE_WANT -> colors.primary.copy(alpha = 0.2f)
                        ListingType.HAVE_OFFER -> colors.secondary.copy(alpha = 0.2f)
                        ListingType.WANT_BUY -> colors.tertiary.copy(alpha = 0.2f)
                        ListingType.TRADE_ONLY -> colors.surfaceVariant
                    }
                ) {
                    Text(
                        when (listing.listingType) {
                            ListingType.HAVE_WANT -> "🔄"
                            ListingType.HAVE_OFFER -> "🎁"
                            ListingType.WANT_BUY -> "💰"
                            ListingType.TRADE_ONLY -> "🤝"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Items section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Have items
                ListingItemsPreview(
                    items = haveItems,
                    label = "Has:",
                    colors = colors
                )
                
                // Arrow
                if (listing.listingType != ListingType.HAVE_OFFER) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = colors.onSurface.copy(alpha = 0.3f)
                    )
                    
                    // Want items
                    if (wantItems != null) {
                        ListingItemsPreview(
                            items = wantItems,
                            label = "Wants:",
                            colors = colors
                        )
                    } else if (listing.wantSugarPoints != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${listing.wantSugarPoints} 🍬",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.primary
                            )
                            Text("or best offer", style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        Text(
                            "Offers welcome",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            // Footer
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stats
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "👁 ${listing.viewCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        "💬 ${listing.offerCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        "⏰ ${daysRemaining}d left",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (daysRemaining < 2) colors.error else colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                // Make offer button
                Button(
                    onClick = { /* Make offer */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Make Offer", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun FeaturedListingCard(
    listing: TradeListing,
    marketManager: MarketManager,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    scope: CoroutineScope
) {
    val json = remember { Json { ignoreUnknownKeys = true } }
    val haveItems = remember { 
        json.decodeFromString<List<TradeItem>>(listing.haveItemsJson)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (listing.rarityFilter) {
                AchievementRarity.LEGENDARY -> Color(0xFFFF1493).copy(alpha = 0.1f)
                AchievementRarity.EPIC -> Color(0xFF9370DB).copy(alpha = 0.1f)
                else -> colors.primary.copy(alpha = 0.1f)
            }
        ),
        border = when (listing.rarityFilter) {
            AchievementRarity.LEGENDARY -> androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF1493))
            AchievementRarity.EPIC -> androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF9370DB))
            else -> null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Featured item showcase
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(android.graphics.Color.parseColor(haveItems.firstOrNull()?.previewColor ?: "#FFB6C1")).copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            haveItems.firstOrNull()?.itemIcon ?: "🎁",
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                }
            }
            
            // Info
            Column(
                modifier = Modifier.weight(0.6f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Rarity badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (listing.rarityFilter) {
                            AchievementRarity.LEGENDARY -> Color(0xFFFF1493)
                            AchievementRarity.EPIC -> Color(0xFF9370DB)
                            AchievementRarity.RARE -> Color(0xFFFFD700)
                            else -> colors.surfaceVariant
                        }
                    ) {
                        Text(
                            listing.rarityFilter?.name ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        listing.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    
                    Text(
                        "by ${listing.creatorName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary
                    )
                ) {
                    Text("View Details")
                }
            }
        }
    }
}

@Composable
private fun ListingItemsPreview(
    items: List<TradeItem>,
    label: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
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
                            AchievementRarity.LEGENDARY -> Color(0xFFFF1493)
                            AchievementRarity.EPIC -> Color(0xFF9370DB)
                            AchievementRarity.RARE -> Color(0xFFFFD700)
                            AchievementRarity.UNCOMMON -> Color(0xFFC0C0C0)
                            else -> Color(0xFFCD7F32)
                        }
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(item.itemIcon, style = MaterialTheme.typography.bodyMedium)
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
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyListingsContent(
    listings: List<TradeListing>,
    marketManager: MarketManager,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val active = listings.filter { it.isActive && !it.isExpired }
    val expired = listings.filter { it.isExpired || !it.isActive }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (active.isNotEmpty()) {
            item {
                Text(
                    "Active (${active.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(active) { listing ->
                MyListingCard(
                    listing = listing,
                    isActive = true,
                    marketManager = marketManager,
                    colors = colors
                )
            }
        }
        
        if (expired.isNotEmpty()) {
            item {
                Text(
                    "Expired (${expired.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            
            items(expired) { listing ->
                MyListingCard(
                    listing = listing,
                    isActive = false,
                    marketManager = marketManager,
                    colors = colors
                )
            }
        }
        
        if (listings.isEmpty()) {
            item {
                EmptyMyListingsState(colors = colors)
            }
        }
    }
}

@Composable
private fun MyListingCard(
    listing: TradeListing,
    isActive: Boolean,
    marketManager: MarketManager,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val json = remember { Json { ignoreUnknownKeys = true } }
    val haveItems = remember { 
        json.decodeFromString<List<TradeItem>>(listing.haveItemsJson)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                colors.surface.copy(alpha = 0.9f) 
            else 
                colors.surface.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item icons
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(android.graphics.Color.parseColor(haveItems.firstOrNull()?.previewColor ?: "#FFB6C1")).copy(alpha = 0.3f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        haveItems.firstOrNull()?.itemIcon ?: "📦",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    listing.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) colors.onSurface else colors.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    "${listing.viewCount} views • ${listing.offerCount} offers",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
            }
            
            if (isActive) {
                Row {
                    IconButton(onClick = { marketManager.renewListing(listing.id) }) {
                        Icon(Icons.Default.Refresh, "Renew")
                    }
                    IconButton(onClick = { marketManager.deleteListing(listing.id) }) {
                        Icon(Icons.Default.Delete, "Delete", tint = colors.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun MyOffersContent(
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Handshake,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = colors.onSurface.copy(alpha = 0.3f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "No Active Offers",
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Offers you've made on listings will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyMarketplaceState(
    message: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Store,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = colors.onSurface.copy(alpha = 0.3f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyMyListingsState(
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.PostAdd,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = colors.onSurface.copy(alpha = 0.3f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "No Listings Yet",
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Create a listing to trade with the community!",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    onDismiss: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                "Filter Listings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Category filter
            Text(
                "Category",
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(MarketManager.CATEGORIES) { (category, name) ->
                    FilterChip(
                        selected = false,
                        onClick = { },
                        label = { Text(name) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sort options
            Text(
                "Sort By",
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            MarketManager.SORT_OPTIONS.forEach { sort ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = false,
                        onClick = { }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when (sort) {
                            MarketSort.NEWEST -> "Newest First"
                            MarketSort.OLDEST -> "Oldest First"
                            MarketSort.HIGHEST_VALUE -> "Highest Value"
                            MarketSort.LOWEST_VALUE -> "Lowest Value"
                            MarketSort.MOST_VIEWED -> "Most Viewed"
                            MarketSort.EXPIRING_SOON -> "Expiring Soon"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary
                )
            ) {
                Text("Apply Filters")
            }
        }
    }
}
