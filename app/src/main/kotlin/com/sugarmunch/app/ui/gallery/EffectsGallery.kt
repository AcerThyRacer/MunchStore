package com.sugarmunch.app.ui.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens
import com.sugarmunch.app.ui.motion.TransitionType

/**
 * EXTREME Visual Effects Gallery
 * Showcase all effects with live previews
 */

data class EffectCategory(
    val name: String,
    val effects: List<EffectItem>
)

data class EffectItem(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val intensity: Float = 1.0f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectsGalleryScreen(
    onNavigateBack: () -> Unit,
    onEffectClick: (EffectItem) -> Unit
) {
    val effectCategories = remember { getEffectCategories() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Effects Gallery") },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(SugarDimens.Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            effectCategories.flatMap { it.effects }.forEach { effect ->
                item {
                    EffectCard(
                        effect = effect,
                        onClick = { onEffectClick(effect) }
                    )
                }
            }
        }
    }
}

@Composable
fun EffectCard(
    effect: EffectItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(SugarDimens.Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = SugarDimens.Brand.hotPink.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SugarDimens.Spacing.md),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = effect.name,
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.hotPink
            )
            Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
            Text(
                text = effect.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

fun getEffectCategories(): List<EffectCategory> {
    return listOf(
        EffectCategory(
            name = "Particle Storm",
            effects = listOf(
                EffectItem("meteor", "Meteor Shower", "Falling meteors with trails", "Particle Storm"),
                EffectItem("snow", "Snow Storm", "Gentle snowfall", "Particle Storm"),
                EffectItem("bubbles", "Bubble Rising", "Floating bubbles", "Particle Storm")
            )
        ),
        EffectCategory(
            name = "Light Shows",
            effects = listOf(
                EffectItem("aurora", "Aurora Borealis", "Northern lights effect", "Light Shows"),
                EffectItem("neon", "Neon Glow", "Pulsing neon lights", "Light Shows"),
                EffectItem("laser", "Laser Show", "Multi-color laser beams", "Light Shows")
            )
        ),
        EffectCategory(
            name = "Physics Sims",
            effects = listOf(
                EffectItem("pendulum", "Pendulum Wave", "Hypnotic pendulum waves", "Physics Sims"),
                EffectItem("fluid", "Fluid Dynamics", "Liquid simulation", "Physics Sims"),
                EffectItem("cloth", "Cloth Simulation", "Flowing fabric", "Physics Sims")
            )
        ),
        EffectCategory(
            name = "Transitions",
            effects = TransitionType.entries.map { 
                EffectItem(it.name.lowercase(), it.name.replace("_", " "), "Page transition effect", "Transitions")
            }
        )
    )
}
