package com.sugarmunch.app.theme.marketplace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.theme.components.*

/**
 * Theme Component Marketplace
 *
 * Browse, purchase, and install theme components created by the community.
 * Features:
 * - Component browsing by category
 * - Search and filtering
 * - Premium component purchases
 * - Community ratings and reviews
 * - Creator profiles
 * - Installation management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentMarketplace(
    onNavigateBack: () -> Unit,
    onComponentInstalled: (ThemeComponent) -> Unit,
    onPurchaseClick: (ThemeComponent) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ComponentCategory?>(null) }
    var showPremiumOnly by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf(MarketplaceSort.POPULAR) }

    val allComponents = remember { ComponentLibrary.components.values.toList() }
    
    val filteredComponents = remember(searchQuery, selectedCategory, showPremiumOnly) {
        allComponents.filter { component ->
            val matchesSearch = searchQuery.isBlank() ||
                component.name.contains(searchQuery, ignoreCase = true) ||
                component.description.contains(searchQuery, ignoreCase = true)
            
            val matchesCategory = selectedCategory == null || component.category == selectedCategory
            
            val matchesPremium = !showPremiumOnly || component.isPremium
            
            matchesSearch && matchesCategory && matchesPremium
        }.sortedWith(
            when (sortBy) {
                MarketplaceSort.POPULAR -> compareByDescending { it.name.length } // Placeholder
                MarketplaceSort.NEWEST -> compareByDescending { it.version }
                MarketplaceSort.NAME -> compareBy { it.name }
                MarketplaceSort.PRICE -> compareBy { it.isPremium }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Component Marketplace") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Show cart */ }) {
                        Icon(Icons.Outlined.ShoppingCart, contentDescription = "Cart")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search and Filters
            MarketplaceFilters(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                showPremiumOnly = showPremiumOnly,
                onPremiumOnlyChange = { showPremiumOnly = it },
                sortBy = sortBy,
                onSortByChange = { sortBy = it }
            )

            // Results Count
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredComponents.size} components",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Component Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredComponents, key = { it.id }) { component ->
                    ComponentCard(
                        component = component,
                        onInstallClick = { onComponentInstalled(component) },
                        onPurchaseClick = { onPurchaseClick(component) }
                    )
                }

                if (filteredComponents.isEmpty()) {
                    item {
                        EmptyMarketplaceState(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .padding(32.dp)
                        )
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// MARKETPLACE FILTERS
// ═════════════════════════════════════════════════════════════

@Composable
private fun MarketplaceFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: ComponentCategory?,
    onCategorySelected: (ComponentCategory?) -> Unit,
    showPremiumOnly: Boolean,
    onPremiumOnlyChange: (Boolean) -> Unit,
    sortBy: MarketplaceSort,
    onSortByChange: (MarketplaceSort) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search components...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { })
        )

        // Category Filter
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text("All") },
                    leadingIcon = if (selectedCategory == null) {
                        { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                    } else null
                )
            }
            
            items(ComponentCategory.entries) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.displayName) },
                    leadingIcon = if (selectedCategory == category) {
                        { Text(category.icon) }
                    } else null
                )
            }
        }

        // Sort and Premium Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sort Dropdown
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { }
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text("Sort: ${sortBy.displayName}") },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                )
            }

            // Premium Filter Toggle
            FilterChip(
                selected = showPremiumOnly,
                onClick = { onPremiumOnlyChange(!showPremiumOnly) },
                label = { Text("Premium Only") },
                leadingIcon = if (showPremiumOnly) {
                    { Icon(Icons.Default.Star, contentDescription = null, Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════
// COMPONENT CARD
// ═════════════════════════════════════════════════════════════

@Composable
private fun ComponentCard(
    component: ThemeComponent,
    onInstallClick: () -> Unit,
    onPurchaseClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (component.isPremium) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(getCategoryColor(component.category).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = component.category.icon,
                            fontSize = 20.sp
                        )
                    }
                    
                    Column {
                        Text(
                            text = component.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = component.category.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (component.isPremium) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Premium",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Description
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = component.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Divider()
                        
                        InfoRow("Version", component.version)
                        InfoRow("Author", component.author)
                        InfoRow("Category", component.category.displayName)
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (component.isPremium) {
                    Button(
                        onClick = onPurchaseClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700)
                        )
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Purchase")
                    }
                } else {
                    Button(
                        onClick = onInstallClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Install")
                    }
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Show less" else "Show more"
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// COMPONENT DETAIL SCREEN
// ═════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentDetailScreen(
    component: ThemeComponent,
    onNavigateBack: () -> Unit,
    onInstallClick: (ThemeComponent) -> Unit,
    onPurchaseClick: (ThemeComponent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(component.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview Section
            item {
                ComponentPreview(component)
            }

            // Info Section
            item {
                ComponentInfoSection(component)
            }

            // Reviews Section
            item {
                ComponentReviews(component)
            }

            // Creator Section
            item {
                CreatorInfo(component.author)
            }

            // Bottom Action Bar
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (component.isPremium) {
                            Button(
                                onClick = { onPurchaseClick(component) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Purchase - $2.99")
                            }
                        } else {
                            Button(
                                onClick = { onInstallClick(component) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Install Free")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComponentPreview(component: ThemeComponent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(getCategoryColor(component.category).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = component.category.icon,
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = component.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = getCategoryColor(component.category)
                )
            }
        }
    }
}

@Composable
private fun ComponentInfoSection(component: ThemeComponent) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = component.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Divider()

            Text(
                text = "Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            InfoRow("Version", component.version)
            InfoRow("Author", component.author)
            InfoRow("Category", component.category.displayName)
            InfoRow("Type", if (component.isPremium) "Premium ⭐" else "Free")
        }
    }
}

@Composable
private fun ComponentReviews(component: ThemeComponent) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                    text = "Reviews",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "4.8 (124 reviews)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sample Reviews
            repeat(3) { index ->
                ReviewItem(
                    reviewer = "User${index + 1}",
                    rating = 5 - index,
                    comment = "Great component! Works perfectly with my themes.",
                    date = "${index + 1} days ago"
                )
                if (index < 2) Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun ReviewItem(
    reviewer: String,
    rating: Int,
    comment: String,
    date: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = reviewer,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(5) { starIndex ->
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = if (starIndex < rating) Color(0xFFFFD700) else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = comment,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CreatorInfo(author: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = author.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Created by $author",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "View Profile",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedButton(onClick = { }) {
                Text("Follow")
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// HELPER COMPOSABLES
// ═════════════════════════════════════════════════════════════

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyMarketplaceState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Components Found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Try adjusting your search or filters",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun getCategoryColor(category: ComponentCategory): Color {
    return when (category) {
        ComponentCategory.GRADIENT -> Color(0xFFFF6B6B)
        ComponentCategory.PARTICLES -> Color(0xFF4ECDC4)
        ComponentCategory.EFFECTS -> Color(0xFFFFE66D)
        ComponentCategory.ANIMATIONS -> Color(0xFF95E1D3)
        ComponentCategory.COLORS -> Color(0xFFF38181)
        ComponentCategory.TEXTURES -> Color(0xFFAA96DA)
        ComponentCategory.LIGHTING -> Color(0xFFFCBAD3)
        ComponentCategory.UI -> Color(0xFFA8D8EA)
    }
}

enum class MarketplaceSort(val displayName: String) {
    POPULAR("Most Popular"),
    NEWEST("Newest"),
    NAME("Name"),
    PRICE("Price")
}
