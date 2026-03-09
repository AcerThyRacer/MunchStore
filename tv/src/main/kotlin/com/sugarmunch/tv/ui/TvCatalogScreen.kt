package com.sugarmunch.tv.ui

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Carousel
import androidx.tv.material3.CarouselDefaults
import androidx.tv.material3.CarouselState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.ImmersiveList
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.tv.presentation.TvMainViewModel
import com.sugarmunch.tv.ui.components.TvAppCard

/**
 * TV Catalog Screen
 * BrowseFragment-style layout with:
 * - Featured carousel at top
 * - Horizontal rows by category
 * - D-pad navigation
 * - Focus indicators
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvCatalogScreen(
    onAppSelected: (String) -> Unit,
    viewModel: TvMainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val featuredFocusRequester = remember { FocusRequester() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Featured Apps Carousel
        if (uiState.featuredApps.isNotEmpty()) {
            item {
                FeaturedCarousel(
                    apps = uiState.featuredApps,
                    onAppSelected = onAppSelected,
                    modifier = Modifier.focusRequester(featuredFocusRequester)
                )
            }
        }

        // Category Rows
        uiState.categoryApps.forEach { (category, apps) ->
            item {
                CategoryRow(
                    title = category,
                    apps = apps,
                    onAppSelected = onAppSelected
                )
            }
        }
    }

    // Auto-focus featured section on first load
    LaunchedEffect(uiState.featuredApps) {
        if (uiState.featuredApps.isNotEmpty()) {
            featuredFocusRequester.requestFocus()
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FeaturedCarousel(
    apps: List<AppEntry>,
    onAppSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val carouselState = remember { CarouselState() }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Featured Apps",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 16.dp)
        )

        Carousel(
            itemCount = apps.size,
            carouselState = carouselState,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .padding(horizontal = 48.dp),
            carouselIndicator = {
                CarouselDefaults.IndicatorRow(
                    itemCount = apps.size,
                    activeItemIndex = carouselState.activeItemIndex,
                    modifier = Modifier.padding(16.dp)
                )
            }
        ) { itemIndex ->
            val app = apps[itemIndex]
            FeaturedCarouselItem(
                app = app,
                onClick = { onAppSelected(app.id) }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FeaturedCarouselItem(
    app: AppEntry,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            app.iconUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 0.3f
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(32.dp)
            ) {
                // Badge
                app.badge?.let { badge ->
                    Text(
                        text = badge.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = app.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = app.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onClick) {
                    Text("View Details")
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategoryRow(
    title: String,
    apps: List<AppEntry>,
    onAppSelected: (String) -> Unit
) {
    val listState = rememberLazyListState()
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = if (isFocused) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${apps.size} apps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            itemsIndexed(apps) { index, app ->
                TvAppCard(
                    app = app,
                    onClick = { onAppSelected(app.id) },
                    modifier = Modifier.width(280.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ImmersiveCatalogScreen(
    apps: List<AppEntry>,
    onAppSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentApp by remember { mutableStateOf(apps.firstOrNull()) }

    ImmersiveList(
        background = { _, _ ->
            currentApp?.let { app ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    app.iconUrl?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        Color.Black.copy(alpha = 0.9f)
                                    )
                                )
                            )
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(32.dp)
        ) {
            itemsIndexed(apps) { index, app ->
                ImmersiveListItem(
                    onClick = { onAppSelected(app.id) },
                    selected = currentApp?.id == app.id
                ) {
                    currentApp = app
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        app.iconUrl?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                        Column {
                            Text(
                                text = app.name,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White
                            )
                            Text(
                                text = app.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
