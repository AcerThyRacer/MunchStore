package com.sugarmunch.app.ui.screens

import android.view.WindowManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.sugarmunch.app.effects.EffectEngine
import com.sugarmunch.app.ui.theme.CandyPink
import com.sugarmunch.app.ui.theme.CottonCandyBlue
import com.sugarmunch.app.ui.theme.CandyMint
import com.sugarmunch.app.ui.theme.CandyPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectsScreen(
    onBack: () -> Unit,
    onBuildCustomEffect: () -> Unit = {},
    onScheduleEffect: () -> Unit = {}
) {
    val context = LocalContext.current
    val windowManager = remember { context.getSystemService<WindowManager>()!! }
    val activeEffects by EffectEngine.activeEffects.collectAsState()
    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(activeEffects.size) {
        if (activeEffects.isNotEmpty()) {
            showConfetti = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Effects")
                        Text(
                            "${activeEffects.size} active",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onBuildCustomEffect) {
                        Icon(Icons.Filled.Build, contentDescription = "Build Custom Effect")
                    }
                    IconButton(onClick = onScheduleEffect) {
                        Icon(Icons.Filled.Schedule, contentDescription = "Schedule Effects")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CandyPurple.copy(alpha = 0.15f),
                            CandyPink.copy(alpha = 0.1f),
                            CottonCandyBlue.copy(alpha = 0.08f)
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quick actions
                item {
                    QuickActionsRow(
                        onBuildCustom = onBuildCustomEffect,
                        onSchedule = onScheduleEffect
                    )
                }
                
                // Active effects summary
                if (activeEffects.isNotEmpty()) {
                    item {
                        ActiveEffectsSummary(
                            activeCount = activeEffects.size,
                            onDisableAll = {
                                EffectEngine.allEffects().forEach { effect ->
                                    if (EffectEngine.isEnabled(effect.id)) {
                                        EffectEngine.toggle(context, windowManager, effect)
                                    }
                                }
                            }
                        )
                    }
                }
                
                // Effects list
                items(EffectEngine.allEffects()) { effect ->
                    EffectCard(
                        effect = effect,
                        isActive = effect.id in activeEffects,
                        onToggle = {
                            EffectEngine.toggle(context, windowManager, effect)
                        }
                    )
                }
            }
        }
        
        // Confetti celebration when effects are active
        CandyConfettiOverlay(
            show = showConfetti,
            onComplete = { showConfetti = false }
        )
    }
}

@Composable
private fun QuickActionsRow(
    onBuildCustom: () -> Unit,
    onSchedule: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Build,
            label = "Custom Effect",
            color = CandyPink,
            onClick = onBuildCustom
        )
        QuickActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Schedule,
            label = "Schedule",
            color = CottonCandyBlue,
            onClick = onSchedule
        )
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.15f),
            contentColor = color
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ActiveEffectsSummary(
    activeCount: Int,
    onDisableAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CandyMint.copy(alpha = 0.2f)
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
                    .clip(CircleShape)
                    .background(CandyMint),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$activeCount",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Effects Active",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Tap to disable all",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onDisableAll) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = "Disable all"
                )
            }
        }
    }
}

@Composable
private fun EffectCard(
    effect: com.sugarmunch.app.effects.Effect,
    isActive: Boolean,
    onToggle: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.02f else 1f,
        animationSpec = tween(300),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                getEffectColor(effect.id).copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 8.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Effect icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = if (isActive) {
                                listOf(getEffectColor(effect.id), getEffectColor(effect.id).copy(alpha = 0.7f))
                            } else {
                                listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.2f))
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    getEffectEmoji(effect.id),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    effect.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Effect capabilities
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (effect.hasVisual) {
                        CapabilityChip("Visual", CandyPink)
                    }
                    if (effect.hasHaptic) {
                        CapabilityChip("Haptic", Color(0xFFFFFACD))
                    }
                    if (effect.hasSound) {
                        CapabilityChip("Sound", CottonCandyBlue)
                    }
                }
            }
            
            Switch(
                checked = isActive,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = getEffectColor(effect.id),
                    checkedTrackColor = getEffectColor(effect.id).copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun CapabilityChip(
    label: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .background(
                color.copy(alpha = 0.15f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

private fun getEffectColor(effectId: String): Color {
    return when (effectId) {
        "sugarrush" -> CandyPink
        "rainbow_tint" -> CandyPurple
        "mint_wash" -> CandyMint
        "caramel_dim" -> Color(0xFFDEB887)
        "candy_confetti" -> CottonCandyBlue
        "heartbeat_haptic" -> Color(0xFFFF6B6B)
        else -> CandyPink
    }
}

private fun getEffectEmoji(effectId: String): String {
    return when (effectId) {
        "sugarrush" -> "\uD83C\uDF6C"
        "rainbow_tint" -> "\uD83C\uDF08"
        "mint_wash" -> "\uD83C\uDF3F"
        "caramel_dim" -> "\uD83C\uDF6A"
        "candy_confetti" -> "\uD83C\uDF8A"
        "heartbeat_haptic" -> "\uD83D\uDC93"
        else -> "\u2728"
    }
}