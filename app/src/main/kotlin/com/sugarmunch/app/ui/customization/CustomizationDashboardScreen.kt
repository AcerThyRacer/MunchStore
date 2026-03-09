package com.sugarmunch.app.ui.customization

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens

/**
 * EXTREME Customization Dashboard
 * Main entry point for all customization options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationDashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBackground: () -> Unit,
    onNavigateToColorSystem: () -> Unit,
    onNavigateToComponentAnimation: () -> Unit,
    onNavigateToPerformanceProfiles: () -> Unit,
    onNavigateToGestureControl: () -> Unit,
    onNavigateToTouchHaptic: () -> Unit,
    onNavigateToGridSpacing: () -> Unit,
    onNavigateToNavigation: () -> Unit,
    onNavigateToCardStyle: () -> Unit,
    onNavigateToTypography: () -> Unit,
    onNavigateToEffectMatrix: () -> Unit,
    onNavigateToParticleSystem: () -> Unit,
    onNavigateToProfileManagement: () -> Unit,
    onNavigateToSmartPresets: () -> Unit,
    onNavigateToExperimentalLab: () -> Unit,
    onNavigateToBackupMigration: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EXTREME Customization") },
                onNavigateBack = onNavigateBack,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SugarDimens.Brand.hotPink.copy(alpha = 0.1f)
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(SugarDimens.Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            // Visual Customization
            item {
                CustomizationCard(
                    icon = androidx.compose.material.icons.Icons.Filled.Palette,
                    title = "Background",
                    description = "Static, animated, reactive",
                    onClick = onNavigateToBackground,
                    color = SugarDimens.Brand.hotPink
                )
            }

            item {
                CustomizationCard(
                    icon = androidx.compose.material.icons.Icons.Filled.ColorLens,
                    title = "Color System",
                    description = "Advanced color control",
                    onClick = onNavigateToColorSystem,
                    color = SugarDimens.Brand.mint
                )
            }

            // Animation & Motion
            item {
                CustomizationCard(
                    icon = androidx.compose.material.icons.Icons.Filled.Animation,
                    title = "Component Animation",
                    description = "Per-component control",
                    onClick = onNavigateToComponentAnimation,
                    color = SugarDimens.Brand.candyOrange
                )
            }

            item {
                CustomizationCard(
                    icon = androidx.compose.material.icons.Icons.Filled.Speed,
                    title = "Performance",
                    description = "Quality presets",
                    onClick = onNavigateToPerformanceProfiles,
                    color = SugarDimens.Brand.yellow
                )
            }

            // Interaction
            item {
                CustomizationCard(
                    icon = androidx.compose.material.icons.Icons.Filled.TouchApp,
                    title = "Gesture Control",
                    description = "Custom gestures",
                    onClick = onNavigateToGestureControl,
                    color = SugarDimens.Brand.bubblegumBlue
                )
            }

            item {
                CustomizationCard(
                    icon = androidx.compose.material.icons.Icons.Filled.Vibration,
                    title = "Touch & Haptic",
                    description = "Haptic feedback",
                    onClick = onNavigateToTouchHaptic,
                    color = SugarDimens.Brand.deepPurple
                )
            }

            // Layout & Structure
            item {
                CustomizationCard(
                    icon = androidx.compose.material.icons.Icons.Filled.GridView,
                    title = "Grid & Spacing",
                    description = "Layout controls",
                    onClick = onNavigateToGridSpacing,
                    color = SugarDimens.Brand.green
                )
            }

            item {
                CustomizationCard(
                    icon = androidx.compose.material.icons.Icons.Filled.Navigation,
                    title = "Navigation",
                    description = "Nav style & behavior",
                    onClick = onNavigateToNavigation,
                    color = SugarDimens.Brand.blue
                )
            }

            // Content Display
            item {
                CustomizationCard(
                    icon = androidx.compose.material.icons.Icons.Filled.Web,
                    title = "Card Styles",
                    description = "Card architecture",
                    onClick = onNavigateToCardStyle,
                    color = SugarDimens.Brand.pink
                )
            }

            item {
                CustomizationCard(
                    icon = androidx.compose.material.icons.Icons.Filled.Title,
                    title = "Typography",
                    description = "Font & text control",
                    onClick = onNavigateToTypography,
                    color = SugarDimens.Brand.red
                )
            }

            // Effects & Particles
            item {
                CustomizationCard(
                    icon = androidx.compose.material.icons.Icons.Filled.BlurOn,
                    title = "Effect Matrix",
                    description = "Effect controls",
                    onClick = onNavigateToEffectMatrix,
                    color = SugarDimens.Brand.purple
                )
            }

            item {
                CustomizationCard(
                    icon = androidx.compose.material.icons.Icons.Filled.AutoAwesome,
                    title = "Particle System",
                    description = "Particle physics",
                    onClick = onNavigateToParticleSystem,
                    color = SugarDimens.Brand.hotPink
                )
            }

            // Profiles & Presets
            item {
                CustomizationCard(
                    icon = androidx.compose.material.icons.Icons.Filled.AccountCircle,
                    title = "Profiles",
                    description = "User profiles",
                    onClick = onNavigateToProfileManagement,
                    color = SugarDimens.Brand.mint
                )
            }

            item {
                CustomizationCard(
                    icon = androidx.compose.material.icons.Icons.Filled.Star,
                    title = "Smart Presets",
                    description = "AI-powered presets",
                    onClick = onNavigateToSmartPresets,
                    color = SugarDimens.Brand.yellow
                )
            }

            // Advanced
            item {
                CustomizationCard(
                    icon = androidx.compose.material.icons.Icons.Filled.Science,
                    title = "Experimental Lab",
                    description = "Beta features",
                    onClick = onNavigateToExperimentalLab,
                    color = SugarDimens.Brand.candyOrange
                )
            }

            item {
                CustomizationCard(
                    icon = androidx.compose.material.icons.Icons.Filled.CloudUpload,
                    title = "Backup & Migration",
                    description = "Backup & restore",
                    onClick = onNavigateToBackupMigration,
                    color = SugarDimens.Brand.bubblegumBlue
                )
            }
        }
    }
}

@Composable
private fun CustomizationCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(SugarDimens.Radius.lg),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SugarDimens.Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = color
            )

            Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = color
            )

            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2
            )
        }
    }
}
