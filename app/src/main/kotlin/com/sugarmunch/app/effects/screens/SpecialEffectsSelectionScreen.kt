package com.sugarmunch.app.effects.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.effects.fab.*
import com.sugarmunch.app.effects.special.SpecialEffectsCatalog
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialEffectsSelectionScreen(
    onBack: () -> Unit,
    onFabConfigClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val fabManager = remember { FabConfigurationManager.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    
    val selectedIds by fabManager.selectedEffectIds.collectAsState()
    val primaryId by fabManager.primaryEffectId.collectAsState()
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showPresetDialog by remember { mutableStateOf(false) }
    
    val categories = remember {
        FabEffectDisplayCatalog.ALL_DISPLAY_INFO.groupBy { it.category }.keys.toList()
    }
    
    val filteredEffects = remember(selectedCategory) {
        if (selectedCategory == null) {
            FabEffectDisplayCatalog.ALL_DISPLAY_INFO
        } else {
            FabEffectDisplayCatalog.ALL_DISPLAY_INFO.filter { it.category == selectedCategory }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "✨ Special Effects",
                            color = colors.onSurface
                        )
                        Text(
                            "${selectedIds.size} selected • Tap to choose",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurface
                        )
                    }
                },
                actions = {
                    // FAB Config button
                    IconButton(onClick = onFabConfigClick) {
                        Icon(
                            Icons.Default.TouchApp,
                            contentDescription = "FAB Configuration",
                            tint = colors.primary
                        )
                    }
                    // Presets
                    IconButton(onClick = { showPresetDialog = true }) {
                        Icon(
                            Icons.Default.Style,
                            contentDescription = "Presets",
                            tint = colors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            if (selectedIds.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showPresetDialog = true },
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${selectedIds.size} Selected")
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Animated candy background
            AnimatedThemeBackground()
            
            // Floating candy decorations
            FloatingCandyDecorations()
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selected effects preview
                if (selectedIds.isNotEmpty()) {
                    item {
                        SelectedEffectsCarousel(
                            selectedIds = selectedIds,
                            primaryId = primaryId,
                            colors = colors,
                            onSetPrimary = { fabManager.setPrimaryEffect(it) },
                            onRemove = { fabManager.deselectEffect(it) }
                        )
                    }
                }
                
                // Category filter chips
                item {
                    CategoryFilterRow(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        colors = colors,
                        onCategorySelect = { selectedCategory = it }
                    )
                }
                
                // Effect cards grid
                items(filteredEffects.chunked(2)) { rowEffects ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowEffects.forEach { effect ->
                            CandyEffectCard(
                                effect = effect,
                                isSelected = effect.effectId in selectedIds,
                                isPrimary = effect.effectId == primaryId,
                                colors = colors,
                                modifier = Modifier.weight(1f),
                                onToggle = {
                                    fabManager.toggleEffectSelection(effect.effectId)
                                },
                                onMakePrimary = {
                                    fabManager.setPrimaryEffect(effect.effectId)
                                }
                            )
                        }
                        // Fill empty space if odd number
                        if (rowEffects.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
    
    // Preset selection dialog
    if (showPresetDialog) {
        PresetSelectionDialog(
            onDismiss = { showPresetDialog = false },
            onPresetSelected = { presetId ->
                fabManager.applyPresetById(presetId)
                showPresetDialog = false
            },
            colors = colors
        )
    }
}

@Composable
private fun SelectedEffectsCarousel(
    selectedIds: Set<String>,
    primaryId: String?,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onSetPrimary: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Selected Effects",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Tap ⭐ to set primary",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(selectedIds.toList()) { effectId ->
                    val effect = FabEffectDisplayCatalog.getById(effectId)
                    effect?.let {
                        SelectedEffectChip(
                            effect = it,
                            isPrimary = effectId == primaryId,
                            colors = colors,
                            onSetPrimary = { onSetPrimary(effectId) },
                            onRemove = { onRemove(effectId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedEffectChip(
    effect: FabEffectDisplayInfo,
    isPrimary: Boolean,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onSetPrimary: () -> Unit,
    onRemove: () -> Unit
) {
    val gradientColors = effect.gradientColors.map { Color(it) }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) 
                colors.primary.copy(alpha = 0.2f) 
            else 
                colors.surfaceVariant
        ),
        border = if (isPrimary) {
            BorderStroke(2.dp, colors.primary)
        } else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gradient indicator
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        brush = Brush.linearGradient(gradientColors),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    effect.emoji,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column {
                Text(
                    effect.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface
                )
                if (isPrimary) {
                    Text(
                        "PRIMARY",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Star button
            IconButton(
                onClick = onSetPrimary,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    if (isPrimary) Icons.Default.Star else Icons.Outlined.Star,
                    contentDescription = "Set primary",
                    tint = if (isPrimary) colors.primary else colors.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
            
            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = colors.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String?,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onCategorySelect: (String?) -> Unit
) {
    val categoryEmojis = mapOf(
        "Particles" to "✨",
        "Ambient" to "🌸",
        "Trails" to "🌈",
        "Overlay" to "🎨",
        "Burst" to "🎆",
        "Reactive" to "🎵",
        "Seasonal" to "❄️"
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelect(null) },
                label = { Text("🍭 All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary,
                    selectedLabelColor = colors.onPrimary
                )
            )
        }
        
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelect(category) },
                label = { Text("${categoryEmojis[category] ?: "🍬"} $category") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary,
                    selectedLabelColor = colors.onPrimary
                )
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CandyEffectCard(
    effect: FabEffectDisplayInfo,
    isSelected: Boolean,
    isPrimary: Boolean,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit,
    onMakePrimary: () -> Unit
) {
    val gradientColors = effect.gradientColors.map { Color(it) }
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Card(
        onClick = onToggle,
        modifier = modifier
            .scale(scale)
            .aspectRatio(0.85f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                colors.surface.copy(alpha = 0.95f)
            else
                colors.surfaceVariant.copy(alpha = 0.7f)
        ),
        border = if (isSelected) {
            BorderStroke(
                3.dp,
                Brush.linearGradient(gradientColors)
            )
        } else if (isPrimary) {
            BorderStroke(2.dp, colors.primary)
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 12.dp else 4.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Candy gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                gradientColors[0].copy(alpha = 0.3f),
                                gradientColors.getOrElse(1) { gradientColors[0] }.copy(alpha = 0.1f)
                            )
                        )
                    )
            )
            
            // Animated candy swirls for selected state
            if (isSelected) {
                AnimatedCandySwirls(gradientColors)
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Effect emoji with animated container
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = gradientColors.map { it.copy(alpha = 0.6f) }
                            ),
                            shape = CircleShape
                        )
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(gradientColors),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = effect.emoji,
                        fontSize = 28.sp
                    )
                    
                    // Sparkle animation for selected
                    if (isSelected) {
                        SparkleOverlay()
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = effect.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = effect.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
                
                if (isSelected) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Primary toggle
                        if (isSelected) {
                            SmallFloatingActionButton(
                                onClick = onMakePrimary,
                                containerColor = if (isPrimary) colors.primary else colors.surface,
                                contentColor = if (isPrimary) colors.onPrimary else colors.primary,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Primary",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        // Checkmark
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                if (effect.isPremium) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "⭐ PREMIUM",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedCandySwirls(colors: List<Color>) {
    val infiniteTransition = rememberInfiniteTransition(label = "swirls")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCandySwirls(colors, rotation)
    }
}

private fun DrawScope.drawCandySwirls(colors: List<Color>, rotation: Float) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    
    colors.forEachIndexed { index, color ->
        val angle = rotation + (index * 120f)
        val radian = Math.toRadians(angle.toDouble())
        val offsetX = kotlin.math.cos(radian).toFloat() * 30f
        val offsetY = kotlin.math.sin(radian).toFloat() * 30f
        
        drawCircle(
            color = color.copy(alpha = 0.1f),
            radius = 20f,
            center = Offset(centerX + offsetX, centerY + offsetY)
        )
    }
}

@Composable
private fun SparkleOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkleScale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkleAlpha"
    )
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        Text(
            text = "✨",
            fontSize = 16.sp,
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
                .offset((-4).dp, 4.dp),
            color = Color.White
        )
    }
}

@Composable
private fun FloatingCandyDecorations() {
    val candies = listOf("🍬", "🍭", "🍪", "🧁", "🎂")
    
    Box(modifier = Modifier.fillMaxSize()) {
        candies.forEachIndexed { index, candy ->
            FloatingCandy(
                emoji = candy,
                initialDelay = index * 500,
                startX = (index * 20).dp,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun FloatingCandy(
    emoji: String,
    initialDelay: Int,
    startX: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -50f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000 + initialDelay, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )
    
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500 + initialDelay, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatX"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000 + initialDelay, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotate"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = modifier
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp,
            modifier = Modifier
                .offset(startX + offsetX.dp, 100.dp + offsetY.dp)
                .rotate(rotation)
                .alpha(alpha)
        )
    }
}

@Composable
private fun PresetSelectionDialog(
    onDismiss: () -> Unit,
    onPresetSelected: (String) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "🎨 Choose a Preset",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FabConfigurationPresets.ALL_PRESETS.forEach { (id, preset) ->
                    PresetCard(
                        id = id,
                        preset = preset,
                        colors = colors,
                        onClick = { onPresetSelected(id) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PresetCard(
    id: String,
    preset: FabConfiguration,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    val presetEmojis = mapOf(
        "sugar_rush" to "🚀",
        "chill_mode" to "☁️",
        "party_mode" to "🎉",
        "romantic" to "💕",
        "gaming" to "🎮",
        "minimal" to "✨"
    )
    
    val presetDescriptions = mapOf(
        "sugar_rush" to "High energy effects for maximum sweetness",
        "chill_mode" to "Calm and soothing ambient effects",
        "party_mode" to "Explosive celebration effects",
        "romantic" to "Lovely hearts and gentle sparkles",
        "gaming" to "Neon trails and reactive effects",
        "minimal" to "Just the essentials"
    )
    
    val colorScheme = preset.customColorScheme ?: FabColorScheme()
    val primaryColor = Color(colorScheme.primaryColor)
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = primaryColor.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .border(2.dp, primaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    presetEmojis[id] ?: "🍭",
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    id.replace("_", " ").replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    presetDescriptions[id] ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    "${preset.selectedEffectIds.size} effects • ${preset.layoutMode.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
