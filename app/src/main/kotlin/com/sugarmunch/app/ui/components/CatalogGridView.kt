package com.sugarmunch.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
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
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.phaseone.PhaseOneUtilities
import com.sugarmunch.app.theme.components.rememberReduceMotion
import com.sugarmunch.app.theme.engine.ThemeManager

@Composable
fun CatalogGridView(
    apps: List<AppEntry>,
    onAppClick: (String) -> Unit,
    gridColumns: Int = 2,
    cardStyle: String = "default",
    accentOverrides: Map<String, String> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val animIntensity by themeManager.animationIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(gridColumns.coerceIn(2, 4)),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(apps, key = { it.id }) { app ->
            AppGridCard(
                app = app,
                colors = colors,
                cardStyle = cardStyle,
                accentOverride = accentOverrides[app.id],
                animIntensity = animIntensity,
                onClick = { onAppClick(app.id) }
            )
        }
    }
}

@Composable
private fun AppGridCard(
    app: AppEntry,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    cardStyle: String,
    accentOverride: String? = null,
    animIntensity: Float,
    onClick: () -> Unit
) {
    val reduceMotion = rememberReduceMotion()
    val utilitySpec = remember(app.id) { PhaseOneUtilities.specFor(app.id) }
    val infiniteTransition = rememberInfiniteTransition(label = "card_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (reduceMotion) 1f else 1f + (0.02f * animIntensity),
        animationSpec = infiniteRepeatable(
            animation = tween(if (reduceMotion) Int.MAX_VALUE else (2000 / animIntensity).toInt().coerceAtLeast(500)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val accentColor = (accentOverride ?: app.accentColor)?.let { hex ->
        try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (_: Exception) { null }
    } ?: colors.primary
    val showAccentBorder = cardStyle == "accent" || cardStyle == "max"
    val surfaceAlpha = when (cardStyle) {
        "glass" -> 0.6f
        "max" -> 1f
        else -> if (utilitySpec != null) 0.98f else 0.95f
    }
    val heroColors = utilitySpec?.heroColors ?: listOf(
        accentColor,
        colors.secondary,
        colors.tertiary
    )
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .scale(scale)
            .then(
                if (showAccentBorder) Modifier.border(2.dp, accentColor, RoundedCornerShape(20.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = surfaceAlpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = (4f * animIntensity).dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        Brush.linearGradient(
                            colors = heroColors.mapIndexed { index, color ->
                                color.copy(alpha = 0.22f + (index * 0.08f))
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (app.iconUrl != null && utilitySpec == null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(app.iconUrl)
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
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    heroColors
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            utilitySpec?.emojiIcon ?: "\uD83C\uDF6C",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                utilitySpec?.let { spec ->
                    Text(
                        text = spec.shortTagline,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.72f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = app.category ?: "App",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
                app.badge?.let { badge ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accentColor.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = badge,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                FilledTonalButton(
                    onClick = onClick,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = accentColor.copy(alpha = 0.2f)
                    )
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (utilitySpec != null) "Dive in" else "Get",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun ViewToggleButton(
    isGridView: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    IconButton(
        onClick = onToggle,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isGridView) {
                Icons.Default.ViewList
            } else {
                Icons.Default.GridView
            },
            contentDescription = if (isGridView) "Switch to list" else "Switch to grid",
            tint = colors.onSurface
        )
    }
}
