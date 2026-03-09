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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.tv.presentation.TvMainViewModel
import com.sugarmunch.tv.ui.components.TvAppCard

/**
 * TV Detail Screen
 * Large hero image layout with:
 * - Hero image (16:9)
 * - App info and description
 * - Prominent install button
 * - Screenshots carousel
 * - Related apps row
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvDetailScreen(
    appId: String,
    onBack: () -> Unit,
    viewModel: TvMainViewModel,
    modifier: Modifier = Modifier
) {
    val app = viewModel.getAppById(appId)
    val relatedApps = app?.category?.let { 
        viewModel.getRelatedApps(appId, it) 
    } ?: emptyList()

    if (app == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("App not found", style = MaterialTheme.typography.headlineMedium)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Section
        HeroSection(
            app = app,
            onBack = onBack,
            onInstall = { /* TODO: Trigger install */ },
            onShare = { /* TODO: Share app */ }
        )

        // Content Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp)
        ) {
            // Description
            Text(
                text = "About",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = app.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3f
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Screenshots (placeholder)
            ScreenshotsSection()

            Spacer(modifier = Modifier.height(48.dp))

            // Related Apps
            if (relatedApps.isNotEmpty()) {
                RelatedAppsSection(
                    apps = relatedApps,
                    onAppSelected = { /* TODO: Navigate to other app */ }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Additional Info
            AppInfoSection(app = app)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HeroSection(
    app: AppEntry,
    onBack: () -> Unit,
    onInstall: () -> Unit,
    onShare: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
    ) {
        // Hero Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            app.accentColor?.let { colorStr ->
                val color = try {
                    Color(android.graphics.Color.parseColor(colorStr))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.6f),
                                    Color.Black
                                )
                            )
                        )
                )
            }

            // Back Button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        // App Info Card (overlapping bottom of hero)
        Card(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            colors = CardDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Icon
                Card(
                    modifier = Modifier.size(120.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    app.iconUrl?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = app.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                // App Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    app.badge?.let { badge ->
                        Text(
                            text = badge.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        app.category?.let { category ->
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "Version ${app.version}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Rating placeholder
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "4.5",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(32.dp))

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share")
                    }

                    Button(
                        onClick = onInstall,
                        colors = ButtonDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Install"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Install",
                            style = MaterialTheme.typography.titleMedium,
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
fun ScreenshotsSection() {
    Text(
        text = "Screenshots",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    // Placeholder screenshots
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(end = 48.dp)
    ) {
        items(5) { index ->
            Card(
                onClick = { },
                modifier = Modifier
                    .width(400.dp)
                    .height(225.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Screenshot ${index + 1}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun RelatedAppsSection(
    apps: List<AppEntry>,
    onAppSelected: (String) -> Unit
) {
    Text(
        text = "Related Apps",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(end = 48.dp)
    ) {
        items(apps) { app ->
            TvAppCard(
                app = app,
                onClick = { onAppSelected(app.id) },
                modifier = Modifier.width(240.dp)
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppInfoSection(app: AppEntry) {
    Column {
        Text(
            text = "App Info",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        InfoRow("Package", app.packageName)
        InfoRow("Version", app.version)
        InfoRow("Source", app.source ?: "Unknown")
        app.category?.let { InfoRow("Category", it) }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
