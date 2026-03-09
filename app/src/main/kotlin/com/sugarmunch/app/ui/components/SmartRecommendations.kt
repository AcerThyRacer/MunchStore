package com.sugarmunch.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sugarmunch.app.ai.*
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.launch

// ═════════════════════════════════════════════════════════════
// SMART RECOMMENDATIONS UI COMPONENTS
// ═════════════════════════════════════════════════════════════

/**
 * Complete Recommendations Feed
 * Combines all recommendation sections into a scrollable feed
 */
@Composable
fun SmartRecommendationsFeed(
    onAppClick: (String) -> Unit,
    onInstallClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recommendationEngine = remember { RecommendationEngine.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    var feed by remember { mutableStateOf<PersonalizedFeed?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        feed = recommendationEngine.getPersonalizedFeed()
        isLoading = false
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Context Header
        feed?.let { personalizedFeed ->
            ContextHeader(
                header = personalizedFeed.contextHeader,
                colors = colors
            )
        }
        
        if (isLoading) {
            RecommendationsLoadingPlaceholder()
        } else {
            feed?.sections?.forEach { section ->
                when (section.type) {
                    FeedSectionType.RECOMMENDED_FOR_YOU -> {
                        RecommendedForYouSection(
                            recommendations = section.apps,
                            title = section.title,
                            subtitle = section.subtitle,
                            onAppClick = onAppClick,
                            onInstallClick = onInstallClick,
                            colors = colors
                        )
                    }
                    FeedSectionType.BECAUSE_YOU_INSTALLED -> {
                        BecauseYouInstalledSection(
                            recommendations = section.apps,
                            title = section.title,
                            subtitle = section.subtitle,
                            onAppClick = onAppClick,
                            onInstallClick = onInstallClick,
                            colors = colors
                        )
                    }
                    FeedSectionType.TRENDING_NOW -> {
                        TrendingNowSection(
                            recommendations = section.apps,
                            title = section.title,
                            subtitle = section.subtitle,
                            onAppClick = onAppClick,
                            onInstallClick = onInstallClick,
                            colors = colors
                        )
                    }
                    FeedSectionType.CONTEXTUAL -> {
                        ContextualSection(
                            recommendations = section.apps,
                            title = section.title,
                            subtitle = section.subtitle,
                            onAppClick = onAppClick,
                            onInstallClick = onInstallClick,
                            colors = colors
                        )
                    }
                    FeedSectionType.DISCOVER -> {
                        DiscoverSection(
                            recommendations = section.apps,
                            title = section.title,
                            subtitle = section.subtitle,
                            onAppClick = onAppClick,
                            onInstallClick = onInstallClick,
                            colors = colors
                        )
                    }
                }
            }
        }
    }
}

/**
 * Context-aware header showing time-appropriate greeting
 */
@Composable
private fun ContextHeader(
    header: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = colors.primary.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = header,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
        }
    }
}

/**
 * "Recommended for You" section with confidence indicators
 */
@Composable
private fun RecommendedForYouSection(
    recommendations: List<Recommendation>,
    title: String,
    subtitle: String,
    onAppClick: (String) -> Unit,
    onInstallClick: (String) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    RecommendationSection(
        title = title,
        subtitle = subtitle,
        colors = colors
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(recommendations, key = { it.app.id }) { recommendation ->
                SmartRecommendationCard(
                    recommendation = recommendation,
                    onAppClick = onAppClick,
                    onInstallClick = onInstallClick,
                    colors = colors,
                    showConfidence = true
                )
            }
        }
    }
}

/**
 * "Because You Installed X" section with similarity indicators
 */
@Composable
private fun BecauseYouInstalledSection(
    recommendations: List<Recommendation>,
    title: String,
    subtitle: String,
    onAppClick: (String) -> Unit,
    onInstallClick: (String) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    if (recommendations.isEmpty()) return
    
    RecommendationSection(
        title = title,
        subtitle = subtitle,
        colors = colors,
        accentColor = colors.secondary
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(recommendations, key = { it.app.id }) { recommendation ->
                SimilarityRecommendationCard(
                    recommendation = recommendation,
                    onAppClick = onAppClick,
                    onInstallClick = onInstallClick,
                    colors = colors
                )
            }
        }
    }
}

/**
 * "Trending Now" section with fire indicators and momentum
 */
@Composable
private fun TrendingNowSection(
    recommendations: List<Recommendation>,
    title: String,
    subtitle: String,
    onAppClick: (String) -> Unit,
    onInstallClick: (String) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    RecommendationSection(
        title = title,
        subtitle = subtitle,
        colors = colors,
        accentColor = Color(0xFFFF5722),
        icon = Icons.Default.LocalFireDepartment
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(recommendations, key = { it.app.id }) { recommendation ->
                TrendingRecommendationCard(
                    recommendation = recommendation,
                    onAppClick = onAppClick,
                    onInstallClick = onInstallClick,
                    colors = colors
                )
            }
        }
    }
}

/**
 * Context-aware section with time/activity-based recommendations
 */
@Composable
private fun ContextualSection(
    recommendations: List<Recommendation>,
    title: String,
    subtitle: String,
    onAppClick: (String) -> Unit,
    onInstallClick: (String) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    if (recommendations.isEmpty()) return
    
    RecommendationSection(
        title = title,
        subtitle = subtitle,
        colors = colors,
        accentColor = colors.tertiary,
        icon = Icons.Default.Schedule
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(recommendations, key = { it.app.id }) { recommendation ->
                ContextualRecommendationCard(
                    recommendation = recommendation,
                    onAppClick = onAppClick,
                    onInstallClick = onInstallClick,
                    colors = colors
                )
            }
        }
    }
}

/**
 * Discovery section for exploring new categories
 */
@Composable
private fun DiscoverSection(
    recommendations: List<Recommendation>,
    title: String,
    subtitle: String,
    onAppClick: (String) -> Unit,
    onInstallClick: (String) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    RecommendationSection(
        title = title,
        subtitle = subtitle,
        colors = colors,
        accentColor = colors.secondary,
        icon = Icons.Default.Explore
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(recommendations, key = { it.app.id }) { recommendation ->
                DiscoveryRecommendationCard(
                    recommendation = recommendation,
                    onAppClick = onAppClick,
                    onInstallClick = onInstallClick,
                    colors = colors
                )
            }
        }
    }
}

/**
 * Base recommendation section layout
 */
@Composable
private fun RecommendationSection(
    title: String,
    subtitle: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    accentColor: Color = colors.primary,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Recommend,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            TextButton(onClick = { /* View all */ }) {
                Text("See All", color = accentColor)
            }
        }
        
        // Content
        content()
    }
}

/**
 * Smart recommendation card with ML confidence visualization
 */
@Composable
private fun SmartRecommendationCard(
    recommendation: Recommendation,
    onAppClick: (String) -> Unit,
    onInstallClick: (String) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    showConfidence: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onAppClick(recommendation.app.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // App Icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                colors.primary.copy(alpha = 0.3f),
                                colors.secondary.copy(alpha = 0.2f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (recommendation.app.iconUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(recommendation.app.iconUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    DefaultAppIcon(colors = colors)
                }
                
                // Confidence badge
                if (showConfidence && recommendation.confidence > 0.7f) {
                    ConfidenceBadge(
                        confidence = recommendation.confidence,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
            
            // App Info
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = recommendation.app.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = recommendation.app.category ?: "App",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Reason chip
                RecommendationReasonChip(
                    reason = recommendation.reason,
                    colors = colors
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Install button
                FilledTonalButton(
                    onClick = { onInstallClick(recommendation.app.id) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = colors.primary.copy(alpha = 0.2f)
                    )
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Install", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/**
 * Similarity-based recommendation card
 */
@Composable
private fun SimilarityRecommendationCard(
    recommendation: Recommendation,
    onAppClick: (String) -> Unit,
    onInstallClick: (String) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onAppClick(recommendation.app.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = colors.secondary.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                colors.secondary.copy(alpha = 0.3f),
                                colors.tertiary.copy(alpha = 0.2f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (recommendation.app.iconUrl != null) {
                    AsyncImage(
                        model = recommendation.app.iconUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    DefaultAppIcon(colors = colors)
                }
            }
            
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = recommendation.app.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Matching features
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = colors.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(recommendation.confidence * 100).toInt()}% match",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.secondary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                FilledTonalButton(
                    onClick = { onInstallClick(recommendation.app.id) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = colors.secondary.copy(alpha = 0.2f)
                    )
                ) {
                    Text("Install", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/**
 * Trending recommendation card with fire effect
 */
@Composable
private fun TrendingRecommendationCard(
    recommendation: Recommendation,
    onAppClick: (String) -> Unit,
    onInstallClick: (String) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val fireColor = Color(0xFFFF5722)
    
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onAppClick(recommendation.app.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                fireColor.copy(alpha = 0.2f),
                                colors.primary.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (recommendation.app.iconUrl != null) {
                    AsyncImage(
                        model = recommendation.app.iconUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    DefaultAppIcon(colors = colors)
                }
                
                // Trending badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = fireColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            "HOT",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = recommendation.app.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "Trending now",
                    style = MaterialTheme.typography.labelSmall,
                    color = fireColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                FilledTonalButton(
                    onClick = { onInstallClick(recommendation.app.id) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = fireColor.copy(alpha = 0.2f)
                    )
                ) {
                    Text("Install", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/**
 * Contextual recommendation card with time/activity indicator
 */
@Composable
private fun ContextualRecommendationCard(
    recommendation: Recommendation,
    onAppClick: (String) -> Unit,
    onInstallClick: (String) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onAppClick(recommendation.app.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                colors.tertiary.copy(alpha = 0.3f),
                                colors.primary.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (recommendation.app.iconUrl != null) {
                    AsyncImage(
                        model = recommendation.app.iconUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    DefaultAppIcon(colors = colors)
                }
                
                // Context badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = colors.tertiary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.White
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = recommendation.app.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "Perfect now",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.tertiary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                FilledTonalButton(
                    onClick = { onInstallClick(recommendation.app.id) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = colors.tertiary.copy(alpha = 0.2f)
                    )
                ) {
                    Text("Install", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/**
 * Discovery recommendation card for new categories
 */
@Composable
private fun DiscoveryRecommendationCard(
    recommendation: Recommendation,
    onAppClick: (String) -> Unit,
    onInstallClick: (String) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onAppClick(recommendation.app.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                colors.secondary.copy(alpha = 0.3f),
                                colors.tertiary.copy(alpha = 0.2f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (recommendation.app.iconUrl != null) {
                    AsyncImage(
                        model = recommendation.app.iconUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    DefaultAppIcon(colors = colors)
                }
                
                // New badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = colors.secondary
                ) {
                    Text(
                        "NEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = recommendation.app.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = recommendation.app.category ?: "New Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.secondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                FilledTonalButton(
                    onClick = { onInstallClick(recommendation.app.id) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = colors.secondary.copy(alpha = 0.2f)
                    )
                ) {
                    Text("Explore", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/**
 * Contextual suggestion chips for quick actions
 */
@Composable
fun ContextualSuggestionChips(
    onChipClick: (ContextualSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fabManager = remember { ContextAwareFabManager.getInstance(context) }
    
    val currentContext by fabManager.currentContext.collectAsState()
    val actions = fabManager.getContextualActions()
    
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actions.forEach { action ->
            SuggestionChip(
                onClick = { /* Handle action */ },
                label = { Text(action.label) },
                icon = {
                    Icon(
                        action.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

/**
 * Recommendation reason chip
 */
@Composable
private fun RecommendationReasonChip(
    reason: RecommendationReason,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val (text, chipColor) = when (reason) {
        RecommendationReason.Collaborative -> "For You" to colors.primary
        RecommendationReason.ContentBased -> "Similar" to colors.secondary
        RecommendationReason.Context -> "Now" to colors.tertiary
        RecommendationReason.Trending -> "Trending" to Color(0xFFFF5722)
        RecommendationReason.Similarity -> "Related" to colors.secondary
        RecommendationReason.Discovery -> "New" to colors.secondary
        RecommendationReason.Featured -> "Featured" to colors.primary
    }
    
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = chipColor.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = chipColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * Confidence badge showing ML confidence
 */
@Composable
private fun ConfidenceBadge(
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val color = when {
        confidence > 0.9f -> Color(0xFF4CAF50)
        confidence > 0.7f -> Color(0xFFFFA726)
        else -> Color(0xFF9E9E9E)
    }
    
    Surface(
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        color = color
    ) {
        Text(
            text = "${(confidence * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * Default app icon placeholder
 */
@Composable
private fun DefaultAppIcon(colors: com.sugarmunch.app.theme.model.AdjustedColors) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(colors.primary, colors.secondary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "🍬",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

/**
 * Loading placeholder for recommendations
 */
@Composable
private fun RecommendationsLoadingPlaceholder() {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        repeat(3) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                repeat(3) {
                    Surface(
                        modifier = Modifier
                            .width(160.dp)
                            .height(200.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {}
                }
            }
        }
    }
}

/**
 * Empty state for recommendations
 */
@Composable
fun EmptyRecommendationsState(
    onBrowseClick: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Recommend,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = colors.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Recommendations Yet",
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface
        )
        
        Text(
            text = "Browse and install some apps to get personalized recommendations",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onBrowseClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary
            )
        ) {
            Text("Browse Apps")
        }
    }
}

/**
 * Contextual suggestion data class
 */
data class ContextualSuggestion(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val action: () -> Unit
)
