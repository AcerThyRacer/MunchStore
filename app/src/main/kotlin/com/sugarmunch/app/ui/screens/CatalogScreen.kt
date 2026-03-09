package com.sugarmunch.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.data.AppEntry

data class CandyTrail(val name: String, val description: String, val icon: String, val appIds: List<String>)

private sealed class CatalogRow {
    data class CandyTrailsRow(val trails: List<CandyTrail>) : CatalogRow()
    data class CategoryHeader(val categoryName: String) : CatalogRow()
    data class AppRow(val app: AppEntry) : CatalogRow()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onAppClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onThemeClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: CatalogViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
        CatalogViewModel(ManifestRepository(context))
    }
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var isSearchExpanded by remember { mutableStateOf(false) }
    
    // Theme integration
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val bgIntensity by themeManager.backgroundIntensity.collectAsState()
    val animIntensity by themeManager.animationIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)

    LaunchedEffect(Unit) {
        viewModel.loadApps()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = !isSearchExpanded,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Column {
                            Text(
                                "SugarMunch", 
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.onSurface
                            )
                            Text(
                                "Live, Life, Love \u2764",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.primary
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = isSearchExpanded,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.search(it) },
                            placeholder = { Text("Search candy...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = colors.primary
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { }),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.search("") }) {
                                        Icon(Icons.Default.Close, "Clear search")
                                    }
                                }
                            }
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                        Icon(
                            if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = colors.onSurface
                        )
                    }
                    IconButton(onClick = onThemeClick) {
                        Icon(
                            Icons.Default.Palette, 
                            contentDescription = "Theme",
                            tint = colors.primary
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.Settings, 
                            contentDescription = "Settings",
                            tint = colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colors.onSurface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Dynamic theme background
            AnimatedThemeBackground()
            
            // Gradient overlay based on current theme
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colors.background.copy(alpha = 0.95f),
                                colors.background.copy(alpha = 0.8f),
                                colors.background.copy(alpha = 0.6f)
                            )
                        )
                    )
            )

            when (val state = uiState) {
                is CatalogUiState.Loading -> {
                    ShimmerLoadingList(colors = colors)
                }
                is CatalogUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface
                        )
                    }
                }
                is CatalogUiState.Success -> {
                    if (state.filteredApps.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No candy found \uD83C\uDF6C",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.onSurface
                            )
                        }
                    } else {
                        val grouped = state.filteredApps.groupBy { it.category?.takeIf { c -> c.isNotBlank() } ?: "Other" }
                        val categoryOrder = listOf("Video & Music", "Other") + grouped.keys.filter { it !in listOf("Video & Music", "Other") }.sorted()
                        
                        val mockTrails = listOf(
                            CandyTrail("Sour Path", "Pucker up for these zesty apps!", "\uD83C\uDF4B", listOf("app1", "app2")),
                            CandyTrail("Chocolate Road", "Rich and decadent experiences.", "\uD83C\uDF6B", listOf("app3")),
                            CandyTrail("Minty Fresh", "Cool new apps to refresh your day.", "\uD83C\uDF3F", emptyList())
                        )
                        
                        val rows: List<CatalogRow> = listOf(CatalogRow.CandyTrailsRow(mockTrails)) + categoryOrder.flatMap { cat ->
                            listOf(CatalogRow.CategoryHeader(cat)) + (grouped[cat]?.map { CatalogRow.AppRow(it) } ?: emptyList())
                        }
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                rows,
                                key = { row -> when (row) { is CatalogRow.CandyTrailsRow -> "trails"; is CatalogRow.CategoryHeader -> "cat-${row.categoryName}"; is CatalogRow.AppRow -> row.app.id } }
                            ) { row ->
                                when (row) {
                                    is CatalogRow.CandyTrailsRow -> {
                                        androidx.compose.foundation.lazy.LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            items(row.trails) { trail ->
                                                CandyTrailCard(trail, colors, onClick = { /* Navigate to trail apps */ })
                                            }
                                        }
                                    }
                                    is CatalogRow.CategoryHeader -> {
                                        Text(
                                            text = row.categoryName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = colors.primary,
                                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                        )
                                    }
                                    is CatalogRow.AppRow -> CandyAppCard(
                                        name = row.app.name,
                                        description = row.app.description,
                                        version = row.app.version,
                                        colors = colors,
                                        intensity = animIntensity,
                                        onClick = { onAppClick(row.app.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CandyTrailCard(
    trail: CandyTrail,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(colors.primary, colors.secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(trail.icon, style = MaterialTheme.typography.headlineMedium)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = trail.name,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = trail.description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            androidx.compose.material3.Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Start Trail", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ShimmerLoadingList(colors: com.sugarmunch.app.theme.model.AdjustedColors) {
    val transition = rememberInfiniteTransition(label = "catalog_loading")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "catalog_loading_alpha"
    )
    
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surfaceVariant.copy(alpha = alpha)
                )
            ) {
                Box {}
            }
        }
    }
}

@Composable
private fun CandyAppCard(
    name: String,
    description: String,
    version: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    intensity: Float,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f + (0.02f * intensity),
        animationSpec = infiniteRepeatable(
            animation = tween((1500 / maxOf(intensity, 0.5f)).toInt()),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = (4f * intensity).dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dynamic candy icon with theme colors
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(colors.primary, colors.secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("\uD83C\uDF6C", style = MaterialTheme.typography.titleLarge)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "v$version",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.primary
                )
            }
        }
    }
}
