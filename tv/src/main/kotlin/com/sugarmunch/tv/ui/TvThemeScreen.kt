package com.sugarmunch.tv.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Chip
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import com.sugarmunch.app.theme.model.CandyTheme
import com.sugarmunch.app.theme.model.ThemeCategory
import com.sugarmunch.tv.presentation.TvMainViewModel

/**
 * TV Theme Screen
 * Theme selector for TV with:
 * - Large preview cards with live preview
 * - Category filter chips
 * - Current theme indicator
 * - D-pad friendly grid
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvThemeScreen(
    viewModel: TvMainViewModel,
    modifier: Modifier = Modifier
) {
    val themesState by viewModel.themesState.collectAsState()

    val filteredThemes = themesState.selectedCategory?.let { category ->
        themesState.availableThemes.filter { it.category == category }
    } ?: themesState.availableThemes

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Themes",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Choose your candy experience",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Current Theme Section
        item {
            CurrentThemeSection(
                theme = themesState.currentTheme
            )
        }

        // Category Filters
        item {
            CategoryFilterSection(
                categories = themesState.categories,
                selectedCategory = themesState.selectedCategory,
                onCategorySelected = { category ->
                    viewModel.setThemeCategoryFilter(category)
                }
            )
        }

        // Themes Grid
        item {
            ThemesGridSection(
                themes = filteredThemes,
                currentThemeId = themesState.currentTheme?.id,
                onThemeSelected = { themeId ->
                    viewModel.selectTheme(themeId)
                }
            )
        }

        // Theme Preview
        item {
            ThemePreviewSection(theme = themesState.currentTheme)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CurrentThemeSection(theme: CandyTheme?) {
    Column {
        Text(
            text = "Current Theme",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        theme?.let {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    it.baseColors.primary,
                                    it.baseColors.secondary,
                                    it.baseColors.tertiary
                                )
                            )
                        } ?: Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray))
                    )
                    .padding(32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        theme?.let {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = it.baseColors.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = it.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = it.baseColors.onPrimary.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                Chip(
                                    onClick = { },
                                    content = { 
                                        Text(it.category.name.replace("_", " ")) 
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (it.isDark) {
                                    Icon(
                                        imageVector = Icons.Default.DarkMode,
                                        contentDescription = "Dark Theme",
                                        tint = it.baseColors.onPrimary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.LightMode,
                                        contentDescription = "Light Theme",
                                        tint = it.baseColors.onPrimary
                                    )
                                }
                            }
                        } ?: run {
                            Text(
                                text = "No Theme Selected",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                        }
                    }
                }

                // Active indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategoryFilterSection(
    categories: List<ThemeCategory>,
    selectedCategory: ThemeCategory?,
    onCategorySelected: (ThemeCategory?) -> Unit
) {
    Column {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 48.dp)
        ) {
            item {
                FilterChip(
                    label = "All",
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) }
                )
            }

            items(categories) { category ->
                FilterChip(
                    label = category.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = if (selected) {
            androidx.tv.material3.ButtonDefaults.colors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            androidx.tv.material3.ButtonDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    ) {
        Text(label)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ThemesGridSection(
    themes: List<CandyTheme>,
    currentThemeId: String?,
    onThemeSelected: (String) -> Unit
) {
    Column {
        Text(
            text = "Available Themes",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(end = 48.dp)
        ) {
            items(themes) { theme ->
                ThemeCard(
                    theme = theme,
                    isSelected = theme.id == currentThemeId,
                    onClick = { onThemeSelected(theme.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ThemeCard(
    theme: CandyTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.05f else 1f)

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused },
        border = CardDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(
                    width = 3.dp,
                    color = theme.baseColors.primary
                )
            )
        )
    ) {
        Column {
            // Theme Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                theme.baseColors.primary,
                                theme.baseColors.secondary
                            )
                        )
                    )
            ) {
                // Theme category icon
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (theme.category) {
                            ThemeCategory.DARK -> Icons.Default.DarkMode
                            ThemeCategory.CLASSIC -> Icons.Default.Palette
                            else -> Icons.Default.LightMode
                        },
                        contentDescription = null,
                        tint = theme.baseColors.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Selected indicator
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(32.dp)
                            .background(
                                Color.White,
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = theme.baseColors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Theme Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = theme.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Chip(
                        onClick = { },
                        content = {
                            Text(
                                theme.category.name.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ThemePreviewSection(theme: CandyTheme?) {
    Column {
        Text(
            text = "Live Preview",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        theme?.baseColors?.background ?: MaterialTheme.colorScheme.background
                    )
                    .padding(24.dp)
            ) {
                Column {
                    // Sample UI elements showing theme
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    theme?.baseColors?.primary 
                                        ?: MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(12.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Box(
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(16.dp)
                                    .background(
                                        theme?.baseColors?.onBackground?.copy(alpha = 0.7f)
                                            ?: MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(12.dp)
                                    .background(
                                        theme?.baseColors?.onBackground?.copy(alpha = 0.4f)
                                            ?: MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sample buttons
                    Row {
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(36.dp)
                                .background(
                                    theme?.baseColors?.primary 
                                        ?: MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(8.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(36.dp)
                                .background(
                                    theme?.baseColors?.surfaceVariant 
                                        ?: MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sample card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(
                                theme?.baseColors?.surface 
                                    ?: MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(12.dp)
                            )
                    )
                }
            }
        }
    }
}
