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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.effects.fab.*
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FabConfigurationScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val fabManager = remember { FabConfigurationManager.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    
    val configuration by fabManager.configuration.collectAsState()
    val selectedEffects by fabManager.selectedEffects.collectAsState()
    val primaryEffect by fabManager.getPrimaryEffect()?.let { rememberUpdatedState(it) }
    val layoutMode by fabManager.layoutMode.collectAsState()
    val animationStyle by fabManager.animationStyle.collectAsState()
    
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    var showPreview by remember { mutableStateOf(true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "🎮 FAB Configuration",
                            color = colors.onSurface
                        )
                        Text(
                            "Customize your quick-access effects",
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
                    // Preview toggle
                    IconButton(onClick = { showPreview = !showPreview }) {
                        Icon(
                            if (showPreview) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Preview",
                            tint = colors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedThemeBackground()
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // FAB Preview
                if (showPreview && selectedEffects.isNotEmpty()) {
                    item {
                        FabPreviewCard(
                            selectedEffects = selectedEffects,
                            primaryEffect = primaryEffect,
                            layoutMode = layoutMode,
                            animationStyle = animationStyle,
                            colors = colors
                        )
                    }
                }
                
                // Layout Mode Selection
                item {
                    LayoutModeCard(
                        currentMode = layoutMode,
                        colors = colors,
                        onModeSelected = { fabManager.setLayoutMode(it) }
                    )
                }
                
                // Animation Style
                item {
                    AnimationStyleCard(
                        currentStyle = animationStyle,
                        colors = colors,
                        onStyleSelected = { fabManager.setAnimationStyle(it) }
                    )
                }
                
                // Display Options
                item {
                    DisplayOptionsCard(
                        showLabels = configuration.showLabels,
                        celebrateOnInstall = configuration.celebrateOnInstall,
                        autoRotate = configuration.autoRotateEffects,
                        colors = colors,
                        onShowLabelsChange = { fabManager.setShowLabels(it) },
                        onCelebrateChange = { fabManager.setCelebrateOnInstall(it) },
                        onAutoRotateChange = { fabManager.setAutoRotate(it) }
                    )
                }
                
                // Selected Effects Order
                if (selectedEffects.size > 1) {
                    item {
                        EffectOrderCard(
                            effects = selectedEffects,
                            primaryId = configuration.primaryEffectId,
                            colors = colors,
                            onSetPrimary = { fabManager.setPrimaryEffect(it) }
                        )
                    }
                }
                
                // Presets
                item {
                    QuickPresetsCard(
                        colors = colors,
                        onPresetSelected = { fabManager.applyPresetById(it) }
                    )
                }
                
                // Clear button
                item {
                    OutlinedButton(
                        onClick = { fabManager.clearAllSelections() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.error
                        ),
                        border = BorderStroke(1.dp, colors.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.ClearAll, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All Selections")
                    }
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun FabPreviewCard(
    selectedEffects: List<FabEffectDisplayInfo>,
    primaryEffect: FabEffectDisplayInfo?,
    layoutMode: FabLayoutMode,
    animationStyle: FabAnimationStyle,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
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
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = null,
                    tint = colors.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Live Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Preview area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colors.surfaceVariant.copy(alpha = 0.5f),
                                colors.background.copy(alpha = 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (layoutMode) {
                    FabLayoutMode.COMPACT -> {
                        CompactFabPreview(
                            effect = primaryEffect ?: selectedEffects.first(),
                            animationStyle = animationStyle,
                            colors = colors
                        )
                    }
                    FabLayoutMode.EXPANDING -> {
                        ExpandingFabPreview(
                            effects = selectedEffects.take(4),
                            animationStyle = animationStyle,
                            colors = colors
                        )
                    }
                    FabLayoutMode.GRID -> {
                        GridFabPreview(
                            effects = selectedEffects.take(4),
                            animationStyle = animationStyle,
                            colors = colors
                        )
                    }
                    FabLayoutMode.CAROUSEL -> {
                        CarouselFabPreview(
                            effects = selectedEffects.take(3),
                            animationStyle = animationStyle,
                            colors = colors
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                "This is how your FAB will appear",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CompactFabPreview(
    effect: FabEffectDisplayInfo,
    animationStyle: FabAnimationStyle,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val gradientColors = effect.gradientColors.map { Color(it) }
    
    AnimatedFabButton(
        animationStyle = animationStyle,
        gradientColors = gradientColors
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    brush = Brush.radialGradient(gradientColors),
                    shape = CircleShape
                )
                .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(effect.emoji, fontSize = 32.sp)
        }
    }
}

@Composable
private fun ExpandingFabPreview(
    effects: List<FabEffectDisplayInfo>,
    animationStyle: FabAnimationStyle,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy((-20).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        effects.forEachIndexed { index, effect ->
            val gradientColors = effect.gradientColors.map { Color(it) }
            val scale = 1f - (index * 0.1f)
            
            AnimatedFabButton(
                animationStyle = animationStyle,
                gradientColors = gradientColors,
                delayMillis = index * 100
            ) {
                Box(
                    modifier = Modifier
                        .size((56 * scale).dp)
                        .scale(scale)
                        .background(
                            brush = Brush.radialGradient(gradientColors),
                            shape = CircleShape
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(effect.emoji, fontSize = 24.sp)
                }
            }
        }
        
        // Main FAB
        val mainGradient = effects.first().gradientColors.map { Color(it) }
        AnimatedFabButton(
            animationStyle = animationStyle,
            gradientColors = mainGradient
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        brush = Brush.radialGradient(mainGradient),
                        shape = CircleShape
                    )
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun GridFabPreview(
    effects: List<FabEffectDisplayInfo>,
    animationStyle: FabAnimationStyle,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        effects.chunked(2).forEach { rowEffects ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowEffects.forEach { effect ->
                    val gradientColors = effect.gradientColors.map { Color(it) }
                    
                    AnimatedFabButton(
                        animationStyle = animationStyle,
                        gradientColors = gradientColors
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    brush = Brush.radialGradient(gradientColors),
                                    shape = CircleShape
                                )
                                .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(effect.emoji, fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CarouselFabPreview(
    effects: List<FabEffectDisplayInfo>,
    animationStyle: FabAnimationStyle,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous (dimmed)
        Box(
            modifier = Modifier
                .size(40.dp)
                .alpha(0.5f)
                .background(
                    colors.surfaceVariant,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = colors.onSurface.copy(alpha = 0.5f)
            )
        }
        
        // Current (main)
        val mainGradient = effects.first().gradientColors.map { Color(it) }
        AnimatedFabButton(
            animationStyle = animationStyle,
            gradientColors = mainGradient
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        brush = Brush.radialGradient(mainGradient),
                        shape = CircleShape
                    )
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(effects.first().emoji, fontSize = 32.sp)
            }
        }
        
        // Next (dimmed)
        Box(
            modifier = Modifier
                .size(40.dp)
                .alpha(0.5f)
                .background(
                    colors.surfaceVariant,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun AnimatedFabButton(
    animationStyle: FabAnimationStyle,
    gradientColors: List<Color>,
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fabAnim")
    
    val animationModifier = when (animationStyle) {
        FabAnimationStyle.BOUNCE -> {
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bounce"
            )
            Modifier.scale(scale)
        }
        FabAnimationStyle.PULSE -> {
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )
            Modifier.scale(scale)
        }
        FabAnimationStyle.FLOAT -> {
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "float"
            )
            Modifier.offset(y = offsetY.dp)
        }
        FabAnimationStyle.SPIN -> {
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "spin"
            )
            Modifier.rotate(rotation)
        }
        FabAnimationStyle.WOBBLE -> {
            val rotation by infiniteTransition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "wobble"
            )
            Modifier.rotate(rotation)
        }
        FabAnimationStyle.JELLY -> {
            val scaleX by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "jellyX"
            )
            val scaleY by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "jellyY"
            )
            Modifier.scale(scaleX, scaleY)
        }
        FabAnimationStyle.GLITTER -> {
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glitter"
            )
            Modifier.alpha(alpha)
        }
    }
    
    Box(
        modifier = animationModifier,
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun LayoutModeCard(
    currentMode: FabLayoutMode,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onModeSelected: (FabLayoutMode) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Layout Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                "How effects are arranged in the FAB",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LayoutModeButton(
                    mode = FabLayoutMode.COMPACT,
                    icon = Icons.Default.RadioButtonUnchecked,
                    label = "Compact",
                    description = "Single effect",
                    isSelected = currentMode == FabLayoutMode.COMPACT,
                    colors = colors,
                    onClick = { onModeSelected(FabLayoutMode.COMPACT) },
                    modifier = Modifier.weight(1f)
                )
                
                LayoutModeButton(
                    mode = FabLayoutMode.EXPANDING,
                    icon = Icons.Default.AddCircle,
                    label = "Expanding",
                    description = "Menu style",
                    isSelected = currentMode == FabLayoutMode.EXPANDING,
                    colors = colors,
                    onClick = { onModeSelected(FabLayoutMode.EXPANDING) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LayoutModeButton(
                    mode = FabLayoutMode.GRID,
                    icon = Icons.Default.Menu,
                    label = "Grid",
                    description = "2x2 layout",
                    isSelected = currentMode == FabLayoutMode.GRID,
                    colors = colors,
                    onClick = { onModeSelected(FabLayoutMode.GRID) },
                    modifier = Modifier.weight(1f)
                )
                
                LayoutModeButton(
                    mode = FabLayoutMode.CAROUSEL,
                    icon = Icons.Default.Swipe,
                    label = "Carousel",
                    description = "Swipeable",
                    isSelected = currentMode == FabLayoutMode.CAROUSEL,
                    colors = colors,
                    onClick = { onModeSelected(FabLayoutMode.CAROUSEL) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LayoutModeButton(
    mode: FabLayoutMode,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    isSelected: Boolean,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                colors.primary.copy(alpha = 0.15f)
            else
                colors.surfaceVariant
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, colors.primary)
        } else null
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) colors.primary else colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) colors.primary else colors.onSurface
            )
            Text(
                description,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun AnimationStyleCard(
    currentStyle: FabAnimationStyle,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onStyleSelected: (FabAnimationStyle) -> Unit
) {
    val animationEmojis = mapOf(
        FabAnimationStyle.BOUNCE to "🦘",
        FabAnimationStyle.WOBBLE to "🍮",
        FabAnimationStyle.SPIN to "🔄",
        FabAnimationStyle.PULSE to "💗",
        FabAnimationStyle.FLOAT to "☁️",
        FabAnimationStyle.JELLY to "🍬",
        FabAnimationStyle.GLITTER to "✨"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Animation Style",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                "How your FAB animates",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(FabAnimationStyle.values().toList()) { style ->
                    val isSelected = currentStyle == style
                    
                    FilterChip(
                        selected = isSelected,
                        onClick = { onStyleSelected(style) },
                        label = {
                            Text("${animationEmojis[style]} ${style.name.lowercase().replaceFirstChar { it.uppercase() }}")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primary,
                            selectedLabelColor = colors.onPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DisplayOptionsCard(
    showLabels: Boolean,
    celebrateOnInstall: Boolean,
    autoRotate: Boolean,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onShowLabelsChange: (Boolean) -> Unit,
    onCelebrateChange: (Boolean) -> Unit,
    onAutoRotateChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Display Options",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Show labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Label,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Show Labels",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurface
                    )
                    Text(
                        "Display effect names in FAB",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = showLabels,
                    onCheckedChange = onShowLabelsChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.primary,
                        checkedTrackColor = colors.primary.copy(alpha = 0.5f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Celebrate on install
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Celebration,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Celebrate on Install",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurface
                    )
                    Text(
                        "Auto-trigger effects when installing apps",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = celebrateOnInstall,
                    onCheckedChange = onCelebrateChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFFD700),
                        checkedTrackColor = Color(0xFFFFD700).copy(alpha = 0.5f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Auto rotate
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Autorenew,
                    contentDescription = null,
                    tint = colors.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Auto-Rotate Effects",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurface
                    )
                    Text(
                        "Cycle through effects automatically",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = autoRotate,
                    onCheckedChange = onAutoRotateChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.secondary,
                        checkedTrackColor = colors.secondary.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

@Composable
private fun EffectOrderCard(
    effects: List<FabEffectDisplayInfo>,
    primaryId: String?,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onSetPrimary: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Effect Priority",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                "Tap ⭐ to set the primary effect",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            effects.forEach { effect ->
                val isPrimary = effect.effectId == primaryId
                val gradientColors = effect.gradientColors.map { Color(it) }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPrimary)
                            colors.primary.copy(alpha = 0.1f)
                        else
                            colors.surfaceVariant
                    ),
                    border = if (isPrimary) {
                        BorderStroke(1.dp, colors.primary)
                    } else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Drag handle
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = null,
                            tint = colors.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        
                        // Effect icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    brush = Brush.radialGradient(gradientColors),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(effect.emoji, fontSize = 20.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                effect.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurface
                            )
                            if (isPrimary) {
                                Text(
                                    "PRIMARY EFFECT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Star button
                        IconButton(
                            onClick = { onSetPrimary(effect.effectId) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                if (isPrimary) Icons.Default.Star else Icons.Default.StarOutline,
                                contentDescription = "Set primary",
                                tint = if (isPrimary) colors.primary else colors.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPresetsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onPresetSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Quick Presets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                "One-tap configuration sets",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val presets = listOf(
                Triple("🚀", "Sugar Rush", "sugar_rush"),
                Triple("☁️", "Chill Mode", "chill_mode"),
                Triple("🎉", "Party Mode", "party_mode"),
                Triple("💕", "Romantic", "romantic"),
                Triple("🎮", "Gaming", "gaming"),
                Triple("✨", "Minimal", "minimal")
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.take(3).forEach { (emoji, label, id) ->
                    PresetButton(
                        emoji = emoji,
                        label = label,
                        colors = colors,
                        onClick = { onPresetSelected(id) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.drop(3).forEach { (emoji, label, id) ->
                    PresetButton(
                        emoji = emoji,
                        label = label,
                        colors = colors,
                        onClick = { onPresetSelected(id) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetButton(
    emoji: String,
    label: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.primary
        ),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.5f))
    ) {
        Text(emoji)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
