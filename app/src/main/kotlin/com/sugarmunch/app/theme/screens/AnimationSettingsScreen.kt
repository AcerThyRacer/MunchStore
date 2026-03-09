package com.sugarmunch.app.theme.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.AnimationSettingsManager
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.model.*
import com.sugarmunch.app.ui.components.Haptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimationSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val animManager = remember { AnimationSettingsManager.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    val settings by animManager.settings.collectAsState()
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    var selectedSection by remember { mutableStateOf<AnimSection?>(null) }
    var previewKey by remember { mutableIntStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Animation Studio")
                        Text(
                            "Customize every movement",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        animManager.resetToDefault()
                        Haptics.performTick(context)
                        previewKey++
                    }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset")
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
                // Quick Presets
                item {
                    QuickPresetSelector(
                        currentSettings = settings,
                        onPresetSelected = { preset, name ->
                            animManager.applyPreset(preset)
                            Haptics.performSuccess(context)
                            previewKey++
                        },
                        colors = colors
                    )
                }
                
                // Master Toggle
                item {
                    MasterAnimationCard(
                        enabled = settings.animationsEnabled && !settings.reduceMotion,
                        onToggle = { 
                            animManager.setAnimationsEnabled(it)
                            Haptics.performClick(context)
                        },
                        colors = colors
                    )
                }
                
                // Live Preview
                item {
                    AnimationPreviewCard(
                        settings = settings,
                        previewKey = previewKey,
                        onTriggerPreview = { previewKey++ },
                        colors = colors
                    )
                }
                
                // Transition Settings
                item {
                    SectionCard(
                        title = "Screen Transitions",
                        icon = Icons.Default.Animation,
                        colors = colors,
                        onClick = { selectedSection = AnimSection.TRANSITIONS }
                    )
                }
                
                // List Animations
                item {
                    SectionCard(
                        title = "List Animations",
                        icon = Icons.Default.ViewList,
                        colors = colors,
                        onClick = { selectedSection = AnimSection.LIST }
                    )
                }
                
                // Particle Settings
                item {
                    SectionCard(
                        title = "Particle Effects",
                        icon = Icons.Default.Star,
                        colors = colors,
                        onClick = { selectedSection = AnimSection.PARTICLES }
                    )
                }
                
                // Button & Touch
                item {
                    SectionCard(
                        title = "Touch Interactions",
                        icon = Icons.Default.TouchApp,
                        colors = colors,
                        onClick = { selectedSection = AnimSection.TOUCH }
                    )
                }
                
                // FAB & Overlay
                item {
                    SectionCard(
                        title = "FAB & Overlay",
                        icon = Icons.Default.RadioButtonChecked,
                        colors = colors,
                        onClick = { selectedSection = AnimSection.FAB }
                    )
                }
                
                // Effects & Feedback
                item {
                    SectionCard(
                        title = "Effects & Feedback",
                        icon = Icons.Default.Celebration,
                        colors = colors,
                        onClick = { selectedSection = AnimSection.EFFECTS }
                    )
                }
                
                // Physics Settings
                item {
                    SectionCard(
                        title = "Physics Engine",
                        icon = Icons.Default.Speed,
                        colors = colors,
                        onClick = { selectedSection = AnimSection.PHYSICS }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            
            // Bottom Sheet for Sections
            AnimatedVisibility(
                visible = selectedSection != null,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                selectedSection?.let { section ->
                    SectionDetailSheet(
                        section = section,
                        settings = settings,
                        onSettingsChange = { animManager.updateSettings(it) },
                        onDismiss = { selectedSection = null },
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickPresetSelector(
    currentSettings: AnimationSettings,
    onPresetSelected: (AnimationSettings, String) -> Unit,
    colors: AdjustedColors
) {
    Column {
        Text(
            text = "Quick Presets",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AnimationPresets.ALL_PRESETS) { (name, preset) ->
                val isSelected = currentSettings == preset
                
                val displayName = name.replaceFirstChar { it.uppercase() }
                val icon = when (name) {
                    "smooth" -> Icons.Default.Waves
                    "chill" -> Icons.Default.AcUnit
                    "energetic" -> Icons.Default.Bolt
                    "gamer" -> Icons.Default.SportsEsports
                    "minimal" -> Icons.Default.Minimize
                    "cinematic" -> Icons.Default.Movie
                    "off" -> Icons.Default.DoNotDisturb
                    else -> Icons.Default.Animation
                }
                
                Card(
                    onClick = { onPresetSelected(preset, name) },
                    modifier = Modifier.width(100.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) colors.primary else colors.surface.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 8.dp else 2.dp
                    )
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
                            tint = if (isSelected) colors.onPrimary else colors.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) colors.onPrimary else colors.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MasterAnimationCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    colors: AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) 
                colors.primary.copy(alpha = 0.15f) 
            else 
                colors.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.linearGradient(
                            if (enabled) 
                                listOf(colors.primary, colors.secondary)
                            else 
                                listOf(Color.Gray, Color.DarkGray)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (enabled) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (enabled) "Animations On" else "Animations Off",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = if (enabled) "Full candy experience" else "Reduced motion mode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.primary,
                    checkedTrackColor = colors.primary.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun AnimationPreviewCard(
    settings: AnimationSettings,
    previewKey: Int,
    onTriggerPreview: () -> Unit,
    colors: AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                
                Button(
                    onClick = onTriggerPreview,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Replay")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Demo animations
            DemoButtonAnimation(settings, colors, previewKey)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DemoCardAnimation(settings, colors, previewKey)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DemoListAnimation(settings, colors, previewKey)
        }
    }
}

@Composable
private fun DemoButtonAnimation(
    settings: AnimationSettings,
    colors: AdjustedColors,
    key: Int
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) settings.buttonPressScale else 1f,
        animationSpec = if (settings.animationsEnabled && !settings.reduceMotion) {
            spring(stiffness = settings.springStiffness, dampingRatio = settings.springDamping)
        } else {
            snap()
        },
        label = "button_scale"
    )
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { 
                isPressed = true
                kotlinx.coroutines.GlobalScope.launch {
                    delay(150)
                    isPressed = false
                }
            },
            modifier = Modifier
                .scale(scale)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.secondary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Button Press Demo")
        }
    }
}

@Composable
private fun DemoCardAnimation(
    settings: AnimationSettings,
    colors: AdjustedColors,
    key: Int
) {
    var visible by remember(key) { mutableStateOf(false) }
    
    LaunchedEffect(key) {
        visible = false
        delay(100)
        visible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = if (settings.animationsEnabled && !settings.reduceMotion) {
            spring(stiffness = settings.springStiffness, dampingRatio = settings.springDamping)
        } else snap(),
        label = "card_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = if (settings.animationsEnabled && !settings.reduceMotion) {
            tween(300)
        } else snap(),
        label = "card_alpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .graphicsLayer { this.alpha = alpha },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(colors.tertiary.copy(alpha = 0.3f), CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(12.dp)
                        .background(colors.onSurface.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(10.dp)
                        .background(colors.onSurface.copy(alpha = 0.1f), RoundedCornerShape(5.dp))
                )
            }
        }
    }
}

@Composable
private fun DemoListAnimation(
    settings: AnimationSettings,
    colors: AdjustedColors,
    key: Int
) {
    val items = remember { List(3) { it } }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { index, item ->
            var visible by remember(key, index) { mutableStateOf(false) }
            
            LaunchedEffect(key) {
                visible = false
                delay(200 + (index * settings.listStaggerDelay.toLong()))
                visible = true
            }
            
            val offsetY by animateFloatAsState(
                targetValue = if (visible) 0f else 30f,
                animationSpec = if (settings.animationsEnabled && !settings.reduceMotion) {
                    spring(stiffness = settings.springStiffness, dampingRatio = settings.springDamping)
                } else snap(),
                label = "item_offset"
            )
            
            val itemAlpha by animateFloatAsState(
                targetValue = if (visible) 1f else 0f,
                animationSpec = tween(200),
                label = "item_alpha"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .graphicsLayer {
                        translationY = offsetY
                        alpha = itemAlpha
                    }
                    .background(
                        colors.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        colors.primary.copy(alpha = 0.15f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun SectionDetailSheet(
    section: AnimSection,
    settings: AnimationSettings,
    onSettingsChange: (AnimationSettings) -> Unit,
    onDismiss: () -> Unit,
    colors: AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.98f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            colors.onSurface.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp)
                        )
                        .clickable { onDismiss() }
                )
            }
            
            // Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = section.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Section-specific controls
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                when (section) {
                    AnimSection.TRANSITIONS -> TransitionControls(settings, onSettingsChange, colors)
                    AnimSection.LIST -> ListControls(settings, onSettingsChange, colors)
                    AnimSection.PARTICLES -> ParticleControls(settings, onSettingsChange, colors)
                    AnimSection.TOUCH -> TouchControls(settings, onSettingsChange, colors)
                    AnimSection.FAB -> FabControls(settings, onSettingsChange, colors)
                    AnimSection.EFFECTS -> EffectsControls(settings, onSettingsChange, colors)
                    AnimSection.PHYSICS -> PhysicsControls(settings, onSettingsChange, colors)
                }
            }
        }
    }
}

@Composable
private fun TransitionControls(
    settings: AnimationSettings,
    onChange: (AnimationSettings) -> Unit,
    colors: AdjustedColors
) {
    Column {
        Text(
            "Transition Type",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransitionType.entries.forEach { type ->
                val selected = settings.transitionType == type
                FilterChip(
                    selected = selected,
                    onClick = { onChange(settings.copy(transitionType = type)) },
                    label = { 
                        Text(
                            type.name.replace("_", " ").lowercase()
                                .replaceFirstChar { it.uppercase() }
                        )
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Duration slider
        DurationSlider(
            label = "Transition Duration",
            value = settings.transitionDuration,
            onValueChange = { onChange(settings.copy(transitionDuration = it)) },
            range = 0f..1000f,
            colors = colors
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ToggleRow(
            label = "Screen Transitions",
            checked = settings.screenTransitionEnabled,
            onCheckedChange = { onChange(settings.copy(screenTransitionEnabled = it)) },
            colors = colors
        )
        
        ToggleRow(
            label = "Shared Element Transitions",
            checked = settings.sharedElementTransitions,
            onCheckedChange = { onChange(settings.copy(sharedElementTransitions = it)) },
            colors = colors
        )
    }
}

@Composable
private fun ListControls(
    settings: AnimationSettings,
    onChange: (AnimationSettings) -> Unit,
    colors: AdjustedColors
) {
    Column {
        Text(
            "List Item Animation",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ListAnimationType.entries.forEach { type ->
                val selected = settings.listItemEntrance == type
                FilterChip(
                    selected = selected,
                    onClick = { onChange(settings.copy(listItemEntrance = type)) },
                    label = { 
                        Text(
                            type.name.replace("_", " ").lowercase()
                                .replaceFirstChar { it.uppercase() }
                        )
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        DurationSlider(
            label = "Stagger Delay",
            value = settings.listStaggerDelay,
            onValueChange = { onChange(settings.copy(listStaggerDelay = it)) },
            range = 0f..200f,
            colors = colors
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ToggleRow(
            label = "Pull to Refresh Animation",
            checked = settings.pullToRefreshAnimation,
            onCheckedChange = { onChange(settings.copy(pullToRefreshAnimation = it)) },
            colors = colors
        )
    }
}

@Composable
private fun ParticleControls(
    settings: AnimationSettings,
    onChange: (AnimationSettings) -> Unit,
    colors: AdjustedColors
) {
    Column {
        ToggleRow(
            label = "Enable Particles",
            checked = settings.particlesEnabled,
            onCheckedChange = { onChange(settings.copy(particlesEnabled = it)) },
            colors = colors
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Particle Density",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ParticleDensity.entries.forEach { density ->
                val selected = settings.particleDensity == density
                FilterChip(
                    selected = selected,
                    onClick = { onChange(settings.copy(particleDensity = density)) },
                    label = { 
                        Text(
                            density.name.lowercase()
                                .replaceFirstChar { it.uppercase() }
                        )
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        DurationSlider(
            label = "Particle Speed",
            value = settings.particleSpeed,
            onValueChange = { onChange(settings.copy(particleSpeed = it)) },
            range = 0.1f..3f,
            colors = colors
        )
    }
}

@Composable
private fun TouchControls(
    settings: AnimationSettings,
    onChange: (AnimationSettings) -> Unit,
    colors: AdjustedColors
) {
    Column {
        DurationSlider(
            label = "Button Press Scale",
            value = settings.buttonPressScale,
            onValueChange = { onChange(settings.copy(buttonPressScale = it)) },
            range = 0.8f..1f,
            colors = colors
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ToggleRow(
            label = "Ripple Effects",
            checked = settings.rippleEnabled,
            onCheckedChange = { onChange(settings.copy(rippleEnabled = it)) },
            colors = colors
        )
        
        ToggleRow(
            label = "Card Hover Scale",
            checked = settings.cardHoverScale > 1f,
            onCheckedChange = { 
                onChange(settings.copy(cardHoverScale = if (it) 1.02f else 1f))
            },
            colors = colors
        )
    }
}

@Composable
private fun FabControls(
    settings: AnimationSettings,
    onChange: (AnimationSettings) -> Unit,
    colors: AdjustedColors
) {
    Column {
        Text(
            "FAB Animation",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FabAnimationType.entries.forEach { type ->
                val selected = settings.fabEntranceAnimation == type
                FilterChip(
                    selected = selected,
                    onClick = { onChange(settings.copy(fabEntranceAnimation = type)) },
                    label = { 
                        Text(
                            type.name.lowercase()
                                .replaceFirstChar { it.uppercase() }
                        )
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ToggleRow(
            label = "FAB Drag Physics",
            checked = settings.fabDragPhysics,
            onCheckedChange = { onChange(settings.copy(fabDragPhysics = it)) },
            colors = colors
        )
        
        ToggleRow(
            label = "Snap Animation",
            checked = settings.fabSnapAnimation,
            onCheckedChange = { onChange(settings.copy(fabSnapAnimation = it)) },
            colors = colors
        )
    }
}

@Composable
private fun EffectsControls(
    settings: AnimationSettings,
    onChange: (AnimationSettings) -> Unit,
    colors: AdjustedColors
) {
    Column {
        ToggleRow(
            label = "Shimmer Loading Effect",
            checked = settings.shimmerEnabled,
            onCheckedChange = { onChange(settings.copy(shimmerEnabled = it)) },
            colors = colors
        )
        
        if (settings.shimmerEnabled) {
            DurationSlider(
                label = "Shimmer Speed",
                value = settings.shimmerSpeed,
                onValueChange = { onChange(settings.copy(shimmerSpeed = it)) },
                range = 0.1f..3f,
                colors = colors
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ToggleRow(
            label = "Success Animations",
            checked = settings.successAnimationEnabled,
            onCheckedChange = { onChange(settings.copy(successAnimationEnabled = it)) },
            colors = colors
        )
        
        ToggleRow(
            label = "Confetti on Success",
            checked = settings.confettiOnSuccess,
            onCheckedChange = { onChange(settings.copy(confettiOnSuccess = it)) },
            colors = colors
        )
        
        ToggleRow(
            label = "Error Shake",
            checked = settings.errorShakeEnabled,
            onCheckedChange = { onChange(settings.copy(errorShakeEnabled = it)) },
            colors = colors
        )
        
        ToggleRow(
            label = "Haptic Feedback",
            checked = settings.hapticFeedback,
            onCheckedChange = { onChange(settings.copy(hapticFeedback = it)) },
            colors = colors
        )
    }
}

@Composable
private fun PhysicsControls(
    settings: AnimationSettings,
    onChange: (AnimationSettings) -> Unit,
    colors: AdjustedColors
) {
    Column {
        DurationSlider(
            label = "Spring Stiffness",
            value = settings.springStiffness,
            onValueChange = { onChange(settings.copy(springStiffness = it)) },
            range = 100f..1000f,
            colors = colors
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        DurationSlider(
            label = "Spring Damping",
            value = settings.springDamping,
            onValueChange = { onChange(settings.copy(springDamping = it)) },
            range = 0.1f..1f,
            colors = colors
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ToggleRow(
            label = "Overscroll Stretch",
            checked = settings.overscrollStretch,
            onCheckedChange = { onChange(settings.copy(overscrollStretch = it)) },
            colors = colors
        )
        
        ToggleRow(
            label = "Parallax Scrolling",
            checked = settings.parallaxScrolling,
            onCheckedChange = { onChange(settings.copy(parallaxScrolling = it)) },
            colors = colors
        )
    }
}

@Composable
private fun DurationSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    colors: AdjustedColors
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface
            )
            Text(
                text = "${value.toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.primary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = colors.primary,
                activeTrackColor = colors.primary,
                inactiveTrackColor = colors.surfaceVariant
            )
        )
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: AdjustedColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.primary,
                checkedTrackColor = colors.primary.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val hGapPx = 8.dp.roundToPx()
        val vGapPx = 8.dp.roundToPx()
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        val rowWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()
        
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        var currentRowHeight = 0
        
        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)
            
            if (currentRow.isNotEmpty() && currentRowWidth + hGapPx + placeable.width > constraints.maxWidth) {
                rows.add(currentRow)
                rowWidths.add(currentRowWidth)
                rowHeights.add(currentRowHeight)
                currentRow = mutableListOf()
                currentRowWidth = 0
                currentRowHeight = 0
            }
            
            currentRow.add(placeable)
            currentRowWidth += if (currentRow.size > 1) hGapPx else 0
            currentRowWidth += placeable.width
            currentRowHeight = maxOf(currentRowHeight, placeable.height)
        }
        
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowWidths.add(currentRowWidth)
            rowHeights.add(currentRowHeight)
        }
        
        val width = constraints.maxWidth
        val height = rowHeights.sum() + (rowHeights.size - 1).coerceAtLeast(0) * vGapPx
        
        layout(width, height) {
            var y = 0
            rows.forEachIndexed { rowIndex, row ->
                var x = when (horizontalArrangement) {
                    Arrangement.End -> width - rowWidths[rowIndex]
                    Arrangement.Center -> (width - rowWidths[rowIndex]) / 2
                    else -> 0
                }
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + hGapPx
                }
                y += rowHeights[rowIndex] + vGapPx
            }
        }
    }
}

private fun Modifier.graphicsLayer(block: androidx.compose.ui.graphics.GraphicsLayerScope.() -> Unit): Modifier {
    return this.then(Modifier.drawBehind { })
}

private enum class AnimSection(val title: String, val description: String) {
    TRANSITIONS("Screen Transitions", "Customize how screens move and change"),
    LIST("List Animations", "Control list item entrance and behavior"),
    PARTICLES("Particle Effects", "Fine-tune background particles"),
    TOUCH("Touch Interactions", "Button presses and touch feedback"),
    FAB("FAB & Overlay", "Floating action button animations"),
    EFFECTS("Effects & Feedback", "Success, error, and loading effects"),
    PHYSICS("Physics Engine", "Spring and bounce physics settings")
}
