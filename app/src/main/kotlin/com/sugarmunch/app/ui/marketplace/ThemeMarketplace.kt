package com.sugarmunch.app.ui.marketplace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

// region Data Models

data class MarketplaceTheme(
    val id: String,
    val name: String,
    val author: String,
    val description: String,
    val previewColors: List<Color>,
    val downloadCount: Int = 0,
    val rating: Float = 0f,
    val ratingCount: Int = 0,
    val tags: List<String> = emptyList(),
    val category: String = "General",
    val createdAt: Long = System.currentTimeMillis(),
    val isInstalled: Boolean = false,
    val isFeatured: Boolean = false
)

enum class MarketplaceSortOrder(val label: String) {
    FEATURED("Featured"),
    TRENDING("Trending"),
    NEWEST("Newest"),
    TOP_RATED("Top Rated"),
    MOST_DOWNLOADED("Most Downloaded")
}

// endregion

// region Main Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeMarketplaceScreen(
    onBack: () -> Unit,
    onInstallTheme: (MarketplaceTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf(MarketplaceSortOrder.FEATURED) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var previewTheme by remember { mutableStateOf<MarketplaceTheme?>(null) }

    val categories = remember {
        listOf("All", "Candy", "Dark", "Neon", "Pastel", "Nature", "Retro")
    }

    val filteredThemes = remember(searchQuery, selectedSort, selectedCategory) {
        SampleMarketplaceData.sampleThemes
            .filter { theme ->
                val matchesQuery = searchQuery.isBlank() ||
                    theme.name.contains(searchQuery, ignoreCase = true) ||
                    theme.author.contains(searchQuery, ignoreCase = true) ||
                    theme.tags.any { it.contains(searchQuery, ignoreCase = true) }
                val matchesCategory = selectedCategory == null ||
                    selectedCategory == "All" ||
                    theme.category == selectedCategory
                matchesQuery && matchesCategory
            }
            .let { list ->
                when (selectedSort) {
                    MarketplaceSortOrder.FEATURED -> list.sortedByDescending { it.isFeatured }
                    MarketplaceSortOrder.TRENDING -> list.sortedByDescending {
                        it.downloadCount * it.rating
                    }
                    MarketplaceSortOrder.NEWEST -> list.sortedByDescending { it.createdAt }
                    MarketplaceSortOrder.TOP_RATED -> list.sortedByDescending { it.rating }
                    MarketplaceSortOrder.MOST_DOWNLOADED -> list.sortedByDescending {
                        it.downloadCount
                    }
                }
            }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Theme Marketplace") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                        Icon(
                            if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(SugarDimens.Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
        ) {
            // Search bar
            item(span = { GridItemSpan(2) }) {
                AnimatedVisibility(
                    visible = isSearchExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search themes…") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(SugarDimens.Radius.pill),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs)
                        ) {
                            categories.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat ||
                                        (selectedCategory == null && cat == "All"),
                                    onClick = {
                                        selectedCategory = if (cat == "All") null else cat
                                    },
                                    label = { Text(cat) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
                    }
                }
            }

            // Sort tabs
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs)
                ) {
                    MarketplaceSortOrder.entries.forEach { sort ->
                        FilterChip(
                            selected = selectedSort == sort,
                            onClick = { selectedSort = sort },
                            label = { Text(sort.label) }
                        )
                    }
                }
            }

            // Featured carousel
            if (selectedSort == MarketplaceSortOrder.FEATURED) {
                val featured = filteredThemes.filter { it.isFeatured }
                if (featured.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Column {
                            Text(
                                "Featured",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = SugarDimens.Spacing.xs)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(
                                    SugarDimens.Spacing.sm
                                )
                            ) {
                                items(featured, key = { it.id }) { theme ->
                                    FeaturedThemeCard(
                                        theme = theme,
                                        onClick = { previewTheme = theme },
                                        modifier = Modifier.width(280.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
                        }
                    }
                }
            }

            // Theme grid
            items(filteredThemes, key = { it.id }) { theme ->
                MarketplaceThemeCard(
                    theme = theme,
                    onInstall = { onInstallTheme(theme) },
                    onPreview = { previewTheme = theme }
                )
            }
        }
    }

    // Preview bottom sheet
    previewTheme?.let { theme ->
        ThemePreviewSheet(
            theme = theme,
            onInstall = {
                onInstallTheme(theme)
                previewTheme = null
            },
            onDismiss = { previewTheme = null }
        )
    }
}

// endregion

// region Featured Card

@Composable
private fun FeaturedThemeCard(
    theme: MarketplaceTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(180.dp)
            .clip(RoundedCornerShape(SugarDimens.Radius.lg))
            .background(
                Brush.linearGradient(
                    colors = theme.previewColors.ifEmpty {
                        listOf(Color.Gray, Color.DarkGray)
                    }
                )
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(SugarDimens.Spacing.md),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                theme.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "by ${theme.author}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RatingBar(
                    rating = theme.rating,
                    starColor = Color(0xFFFFD700),
                    modifier = Modifier.height(16.dp)
                )
                Spacer(modifier = Modifier.width(SugarDimens.Spacing.xs))
                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(SugarDimens.Spacing.xxxs))
                Text(
                    formatCount(theme.downloadCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// endregion

// region Theme Card

@Composable
fun MarketplaceThemeCard(
    theme: MarketplaceTheme,
    onInstall: () -> Unit,
    onPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(SugarDimens.Radius.md))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onPreview)
            .animateContentSize()
    ) {
        // Color preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(topStart = SugarDimens.Radius.md, topEnd = SugarDimens.Radius.md))
                .background(
                    Brush.linearGradient(
                        colors = theme.previewColors.ifEmpty {
                            listOf(Color.Gray, Color.DarkGray)
                        }
                    )
                )
        ) {
            // Color swatches
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(SugarDimens.Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xxxs)
            ) {
                theme.previewColors.take(5).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }

        // Info section
        Column(modifier = Modifier.padding(SugarDimens.Spacing.sm)) {
            Text(
                theme.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                theme.author,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            if (theme.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxs))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xxxs),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    theme.tags.take(3).forEach { tag ->
                        Text(
                            tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(SugarDimens.Radius.xs))
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                                .padding(
                                    horizontal = SugarDimens.Spacing.xxs,
                                    vertical = SugarDimens.Spacing.xxxs
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))

            // Bottom row: rating | downloads | install
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    String.format("%.1f", theme.rating),
                    style = MaterialTheme.typography.labelSmall
                )

                Spacer(modifier = Modifier.width(SugarDimens.Spacing.xs))

                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    formatCount(theme.downloadCount),
                    style = MaterialTheme.typography.labelSmall
                )

                Spacer(modifier = Modifier.weight(1f))

                if (theme.isInstalled) {
                    Text(
                        "Installed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        "Install",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(SugarDimens.Radius.xs))
                            .clickable(onClick = onInstall)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(
                                horizontal = SugarDimens.Spacing.xs,
                                vertical = SugarDimens.Spacing.xxxs
                            )
                    )
                }
            }
        }
    }
}

// endregion

// region Theme Preview Sheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePreviewSheet(
    theme: MarketplaceTheme,
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var userRating by remember { mutableFloatStateOf(0f) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SugarDimens.Spacing.md)
                .padding(bottom = SugarDimens.Spacing.xxl)
        ) {
            // Large gradient preview with mock UI elements
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(SugarDimens.Radius.lg))
                    .background(
                        Brush.verticalGradient(
                            colors = theme.previewColors.ifEmpty {
                                listOf(Color.Gray, Color.DarkGray)
                            }
                        )
                    )
            ) {
                // Mock UI overlay
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(SugarDimens.Spacing.md)
                ) {
                    // Fake app bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(SugarDimens.Radius.sm))
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Text(
                            "Preview",
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = SugarDimens.Spacing.sm),
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))

                    // Fake cards
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .clip(RoundedCornerShape(SugarDimens.Radius.sm))
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Fake button
                    Row(horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(SugarDimens.Radius.pill))
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Button", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(SugarDimens.Radius.pill))
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Action", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

            // Theme info
            Text(
                theme.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "by ${theme.author}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
            Text(theme.description, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

            // Rating section
            Row(verticalAlignment = Alignment.CenterVertically) {
                RatingBar(
                    rating = theme.rating,
                    modifier = Modifier.height(20.dp)
                )
                Spacer(modifier = Modifier.width(SugarDimens.Spacing.xs))
                Text(
                    "${String.format("%.1f", theme.rating)} (${theme.ratingCount})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))

            Text(
                "Rate this theme",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            RatingBar(
                rating = userRating,
                onRatingChanged = { userRating = it },
                modifier = Modifier.height(28.dp)
            )

            // Tags
            if (theme.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    theme.tags.forEach { tag ->
                        AssistChip(
                            onClick = {},
                            label = { Text(tag) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SugarDimens.Spacing.lg))

            // Install button
            if (theme.isInstalled) {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SugarDimens.Height.buttonLarge),
                    shape = RoundedCornerShape(SugarDimens.Radius.md)
                ) {
                    Text("Already Installed")
                }
            } else {
                Button(
                    onClick = onInstall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SugarDimens.Height.buttonLarge),
                    shape = RoundedCornerShape(SugarDimens.Radius.md),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(SugarDimens.IconSize.sm)
                    )
                    Spacer(modifier = Modifier.width(SugarDimens.Spacing.xs))
                    Text("Install Theme", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// endregion

// region Rating Bar

@Composable
fun RatingBar(
    rating: Float,
    maxRating: Int = 5,
    onRatingChanged: ((Float) -> Unit)? = null,
    starColor: Color = Color(0xFFFFD700),
    modifier: Modifier = Modifier
) {
    val animatedRating by animateFloatAsState(
        targetValue = rating,
        animationSpec = tween(300),
        label = "rating_anim"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxRating) {
            val fillFraction = (animatedRating - (i - 1)).coerceIn(0f, 1f)

            Canvas(
                modifier = Modifier
                    .size(if (onRatingChanged != null) 28.dp else 16.dp)
                    .then(
                        if (onRatingChanged != null) {
                            Modifier.clickable {
                                val newRating = if (rating == i.toFloat()) {
                                    (i - 0.5f)
                                } else {
                                    i.toFloat()
                                }
                                onRatingChanged(newRating)
                            }
                        } else {
                            Modifier
                        }
                    )
            ) {
                val starPath = createStarPath(size)

                // Empty star background
                drawPath(
                    path = starPath,
                    color = starColor.copy(alpha = 0.2f),
                    style = Fill
                )

                // Filled portion
                if (fillFraction > 0f) {
                    drawContext.canvas.save()
                    drawContext.canvas.clipRect(
                        0f,
                        0f,
                        size.width * fillFraction,
                        size.height
                    )
                    drawPath(
                        path = starPath,
                        color = starColor,
                        style = Fill
                    )
                    drawContext.canvas.restore()
                }
            }
        }
    }
}

private fun DrawScope.createStarPath(size: Size): Path {
    val path = Path()
    val cx = size.width / 2f
    val cy = size.height / 2f
    val outerRadius = minOf(cx, cy)
    val innerRadius = outerRadius * 0.4f
    val points = 5
    val angleStep = PI.toFloat() / points
    val startAngle = -PI.toFloat() / 2f

    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = startAngle + i * angleStep
        val x = cx + radius * cos(angle)
        val y = cy + radius * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

// endregion

// region Utilities

private fun formatCount(count: Int): String = when {
    count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
    count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
    else -> count.toString()
}

// endregion

// region Sample Data

object SampleMarketplaceData {
    val sampleThemes: List<MarketplaceTheme> = listOf(
        MarketplaceTheme(
            id = "theme_candy_dream",
            name = "Candy Dream",
            author = "SugarTeam",
            description = "A sweet, pastel-colored theme inspired by cotton candy clouds and lollipop forests. Perfect for a whimsical experience.",
            previewColors = listOf(Color(0xFFFFB6C1), Color(0xFFE6B3FF), Color(0xFFB5DEFF)),
            downloadCount = 12500,
            rating = 4.8f,
            ratingCount = 342,
            tags = listOf("Pastel", "Sweet", "Light"),
            category = "Candy",
            isFeatured = true
        ),
        MarketplaceTheme(
            id = "theme_dark_caramel",
            name = "Dark Caramel",
            author = "NightCoder",
            description = "Rich dark theme with warm caramel accents. Easy on the eyes for late-night sessions.",
            previewColors = listOf(Color(0xFF2D1B2E), Color(0xFFDEB887), Color(0xFF8B4513)),
            downloadCount = 8900,
            rating = 4.6f,
            ratingCount = 215,
            tags = listOf("Dark", "Warm", "Elegant"),
            category = "Dark",
            isFeatured = true
        ),
        MarketplaceTheme(
            id = "theme_neon_sugar",
            name = "Neon Sugar Rush",
            author = "GlowArtist",
            description = "Electrifying neon colors that pop against a deep purple backdrop. For those who like it bold.",
            previewColors = listOf(Color(0xFF1A0A2E), Color(0xFFFF00FF), Color(0xFF00FFFF)),
            downloadCount = 6700,
            rating = 4.5f,
            ratingCount = 180,
            tags = listOf("Neon", "Bold", "Vibrant"),
            category = "Neon",
            isFeatured = true
        ),
        MarketplaceTheme(
            id = "theme_mint_fresh",
            name = "Mint Fresh",
            author = "CoolDesign",
            description = "Refreshing mint and white combination that feels clean and modern.",
            previewColors = listOf(Color(0xFF98FF98), Color(0xFFE0FFF0), Color(0xFF40E0D0)),
            downloadCount = 5400,
            rating = 4.3f,
            ratingCount = 128,
            tags = listOf("Fresh", "Clean", "Mint"),
            category = "Pastel"
        ),
        MarketplaceTheme(
            id = "theme_sunset_sherbet",
            name = "Sunset Sherbet",
            author = "ColorWiz",
            description = "Warm orange to pink gradient reminiscent of a beautiful summer sunset.",
            previewColors = listOf(Color(0xFFFF6B35), Color(0xFFFF8C61), Color(0xFFFF69B4)),
            downloadCount = 7200,
            rating = 4.7f,
            ratingCount = 198,
            tags = listOf("Warm", "Gradient", "Sunset"),
            category = "General",
            isFeatured = true
        ),
        MarketplaceTheme(
            id = "theme_ocean_taffy",
            name = "Ocean Taffy",
            author = "WaveRider",
            description = "Deep sea blues and teals that evoke the calm of ocean waves.",
            previewColors = listOf(Color(0xFF006994), Color(0xFF40E0D0), Color(0xFFE0FFFF)),
            downloadCount = 3800,
            rating = 4.2f,
            ratingCount = 95,
            tags = listOf("Blue", "Calm", "Ocean"),
            category = "Nature"
        ),
        MarketplaceTheme(
            id = "theme_retro_gummy",
            name = "Retro Gummy",
            author = "PixelCandy",
            description = "Nostalgic retro color palette inspired by classic candy machines and arcade games.",
            previewColors = listOf(Color(0xFFE74C3C), Color(0xFFF39C12), Color(0xFF27AE60)),
            downloadCount = 4100,
            rating = 4.4f,
            ratingCount = 112,
            tags = listOf("Retro", "Fun", "Colorful"),
            category = "Retro"
        ),
        MarketplaceTheme(
            id = "theme_lavender_fields",
            name = "Lavender Fields",
            author = "PurpleDreamer",
            description = "Soothing lavender and violet hues that create a peaceful, dreamy atmosphere.",
            previewColors = listOf(Color(0xFFE6E6FA), Color(0xFFDDA0DD), Color(0xFF9370DB)),
            downloadCount = 2900,
            rating = 4.1f,
            ratingCount = 78,
            tags = listOf("Purple", "Calm", "Dreamy"),
            category = "Pastel"
        ),
        MarketplaceTheme(
            id = "theme_cherry_bomb",
            name = "Cherry Bomb",
            author = "RedHot",
            description = "Bold cherry reds and deep crimson for a passionate, energetic look.",
            previewColors = listOf(Color(0xFFDC143C), Color(0xFFB22222), Color(0xFFFF6B6B)),
            downloadCount = 3200,
            rating = 4.0f,
            ratingCount = 88,
            tags = listOf("Red", "Bold", "Energetic"),
            category = "General"
        ),
        MarketplaceTheme(
            id = "theme_midnight_licorice",
            name = "Midnight Licorice",
            author = "DarkVoid",
            description = "Ultra-dark AMOLED-friendly theme with subtle purple undertones.",
            previewColors = listOf(Color(0xFF0D0D0D), Color(0xFF1A1A2E), Color(0xFF16213E)),
            downloadCount = 9800,
            rating = 4.6f,
            ratingCount = 267,
            tags = listOf("AMOLED", "Dark", "Minimal"),
            category = "Dark",
            isInstalled = true
        ),
        MarketplaceTheme(
            id = "theme_bubblegum_pop",
            name = "Bubblegum Pop",
            author = "SugarTeam",
            description = "Playful pinks and magentas that bring joy and energy to every screen.",
            previewColors = listOf(Color(0xFFFF69B4), Color(0xFFFF1493), Color(0xFFFFB6C1)),
            downloadCount = 6100,
            rating = 4.5f,
            ratingCount = 165,
            tags = listOf("Pink", "Fun", "Pop"),
            category = "Candy"
        ),
        MarketplaceTheme(
            id = "theme_forest_candy",
            name = "Forest Candy",
            author = "NatureThemes",
            description = "Earth tones meet candy sweetness in this unique nature-inspired palette.",
            previewColors = listOf(Color(0xFF2D5016), Color(0xFF98FF98), Color(0xFFF0E68C)),
            downloadCount = 2100,
            rating = 3.9f,
            ratingCount = 54,
            tags = listOf("Nature", "Green", "Earthy"),
            category = "Nature"
        )
    )
}

// endregion
