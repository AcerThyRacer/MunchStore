package com.sugarmunch.app.ui.components.search

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.data.repository.*

/**
 * Advanced Search Bar with suggestions and voice search
 */
@Composable
fun AdvancedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onVoiceSearch: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onSortClick: () -> Unit = {},
    suggestions: List<String> = emptyList(),
    recentSearches: List<RecentSearch> = emptyList(),
    onSuggestionClick: (String) -> Unit = {},
    onRecentSearchClick: (String) -> Unit = {},
    onClearRecentSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showSuggestions by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Search input field
        OutlinedTextField(
            value = query,
            onValueChange = { 
                onQueryChange(it)
                showSuggestions = it.isNotBlank() && isFocused
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { 
                    isFocused = it.isFocused
                    showSuggestions = it.isFocused && query.isNotBlank()
                },
            placeholder = { Text("Search apps, games, themes...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                    IconButton(onClick = onVoiceSearch) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice search")
                    }
                }
            },
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Filter and sort chips
        AnimatedVisibility(
            visible = query.isNotBlank() || isFocused,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = onFilterClick,
                    label = { Text("Filters") },
                    leadingIcon = if (true) {
                        { Icon(Icons.Default.FilterList, contentDescription = null, Modifier.size(18.dp)) }
                    } else null
                )

                FilterChip(
                    onClick = onSortClick,
                    label = { Text("Sort") },
                    leadingIcon = { 
                        Icon(Icons.Default.Sort, contentDescription = null, Modifier.size(18.dp)) 
                    }
                )
            }
        }

        // Suggestions dropdown
        AnimatedVisibility(
            visible = showSuggestions && suggestions.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            SuggestionsDropdown(
                suggestions = suggestions,
                onSuggestionClick = {
                    onSuggestionClick(it)
                    onSearch(it)
                    showSuggestions = false
                }
            )
        }

        // Recent searches
        AnimatedVisibility(
            visible = !showSuggestions && recentSearches.isNotEmpty() && query.isBlank(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            RecentSearchesList(
                recentSearches = recentSearches,
                onRecentSearchClick = {
                    onRecentSearchClick(it)
                    onSearch(it)
                },
                onClearRecentSearch = onClearRecentSearch
            )
        }
    }
}

@Composable
private fun SuggestionsDropdown(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            suggestions.forEach { suggestion ->
                ListItem(
                    headlineContent = { Text(suggestion) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .clickable { onSuggestionClick(suggestion) }
                        .padding(horizontal = 8.dp)
                )
                if (suggestion != suggestions.last()) {
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                }
            }
        }
    }
}

@Composable
private fun RecentSearchesList(
    recentSearches: List<RecentSearch>,
    onRecentSearchClick: (String) -> Unit,
    onClearRecentSearch: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                    text = "Recent Searches",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { /* Clear all */ },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Clear all")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            recentSearches.forEach { search ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRecentSearchClick(search.query) }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Outlined.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = search.query,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { onClearRecentSearch(search.query) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// FILTER BOTTOM SHEET
// ═════════════════════════════════════════════════════════════

@Composable
fun FilterBottomSheet(
    filters: SearchFilters,
    onFiltersChange: (SearchFilters) -> Unit,
    onDismiss: () -> Unit,
    availableCategories: List<String> = emptyList()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.headlineSmall
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = { 
                    onFiltersChange(SearchFilters())
                }) {
                    Text("Reset")
                }
                Button(onClick = onDismiss) {
                    Text("Apply")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Categories filter
            if (availableCategories.isNotEmpty()) {
                item {
                    FilterSection(title = "Categories") {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(availableCategories) { category ->
                                FilterChip(
                                    selected = filters.categories.contains(category),
                                    onClick = {
                                        val newCategories = if (filters.categories.contains(category)) {
                                            filters.categories - category
                                        } else {
                                            filters.categories + category
                                        }
                                        onFiltersChange(filters.copy(categories = newCategories))
                                    },
                                    label = { Text(category) }
                                )
                            }
                        }
                    }
                }
            }

            // Rating filter
            item {
                FilterSection(title = "Minimum Rating") {
                    RatingFilterSlider(
                        value = filters.minRating,
                        onValueChange = { 
                            onFiltersChange(filters.copy(minRating = it))
                        }
                    )
                }
            }

            // Size filter
            item {
                FilterSection(title = "Maximum Size") {
                    SizeFilterSlider(
                        value = filters.maxSizeMB,
                        onValueChange = {
                            onFiltersChange(filters.copy(maxSizeMB = it))
                        }
                    )
                }
            }

            // Toggle filters
            item {
                FilterSection(title = "More Options") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterToggle(
                            label = "Featured only",
                            checked = filters.featuredOnly,
                            onCheckedChange = {
                                onFiltersChange(filters.copy(featuredOnly = it))
                            }
                        )
                        FilterToggle(
                            label = "Free apps only",
                            checked = filters.freeOnly,
                            onCheckedChange = {
                                onFiltersChange(filters.copy(freeOnly = it))
                            }
                        )
                    }
                }
            }
        }

        // Active filters summary
        if (filters.hasActiveFilters()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filters.getActiveFilterCount()} active filter${if (filters.getActiveFilterCount() > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    TextButton(onClick = { onFiltersChange(SearchFilters()) }) {
                        Text("Clear all")
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun RatingFilterSlider(
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Any",
                style = MaterialTheme.typography.bodySmall,
                color = if (value == 0f) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "%.1f".format(value),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..5f,
            steps = 9
        )
    }
}

@Composable
private fun SizeFilterSlider(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Any size",
                style = MaterialTheme.typography.bodySmall,
                color = if (value == 0) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (value > 0) "$value MB" else "No limit",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..500f,
            steps = 9
        )
    }
}

@Composable
private fun FilterToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

// ═════════════════════════════════════════════════════════════
// SORT BOTTOM SHEET
// ═════════════════════════════════════════════════════════════

@Composable
fun SortBottomSheet(
    currentSort: SortOption,
    onSortChange: (SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Sort By",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(SortOption.values()) { option ->
                SortOptionItem(
                    option = option,
                    selected = currentSort == option,
                    onSelected = {
                        onSortChange(option)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun SortOptionItem(
    option: SortOption,
    selected: Boolean,
    onSelected: () -> Unit
) {
    ListItem(
        headlineContent = { Text(option.displayName) },
        leadingIcon = {
            RadioButton(
                selected = selected,
                onClick = onSelected
            )
        },
        modifier = Modifier
            .clickable(onClick = onSelected)
            .fillMaxWidth()
    )
}
