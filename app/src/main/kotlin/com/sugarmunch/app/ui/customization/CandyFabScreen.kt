package com.sugarmunch.app.ui.customization

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.components.*

/**
 * Candy FAB Customization Screen
 * Configure the Sugar FAB V2 candy dispenser
 */

@Composable
fun CandyFabScreen(
    onNavigateBack: () -> Unit
) {
    var dispenserStyle by remember { mutableStateOf(CandyDispenserStyle.GUMBALL_MACHINE) }
    var candyType by remember { mutableStateOf(CandyType.GUMBALL) }
    var trailEffect by remember { mutableStateOf(TrailEffectType.SPARKLE) }
    var fabSize by remember { mutableStateOf(1f) }
    var fabOpacity by remember { mutableStateOf(1f) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Candy Dispenser FAB") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Preview section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Live preview of FAB
                        SugarFabV2(
                            config = SugarFabConfig(
                                style = dispenserStyle,
                                candyType = candyType,
                                trailEffect = trailEffect,
                                size = fabSize,
                                opacity = fabOpacity
                            ),
                            modifier = Modifier.size(100.dp)
                        )
                    }
                }
            }
            
            // Dispenser style
            item {
                Text(
                    text = "Dispenser Style",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CandyDispenserStyle.values().forEach { style ->
                    FilterChip(
                        selected = dispenserStyle == style,
                        onClick = { dispenserStyle = style },
                        label = { Text(getStyleDisplayName(style)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                }
            }
            
            // Candy type
            item {
                Text(
                    text = "Candy Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CandyType.values().forEach { type ->
                        FilterChip(
                            selected = candyType == type,
                            onClick = { candyType = type },
                            label = { Text(getCandyDisplayName(type)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Trail effect
            item {
                Text(
                    text = "Trail Effect",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TrailEffectType.values().forEach { trail ->
                        FilterChip(
                            selected = trailEffect == trail,
                            onClick = { trailEffect = trail },
                            label = { Text(getTrailDisplayName(trail)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // FAB size slider
            item {
                Column {
                    Text(
                        text = "FAB Size",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Small", style = MaterialTheme.typography.bodySmall)
                        
                        Slider(
                            value = fabSize,
                            onValueChange = { fabSize = it },
                            valueRange = 0.8f..1.5f,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text("Large", style = MaterialTheme.typography.bodySmall)
                    }
                    
                    Text(
                        text = "${(fabSize * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // FAB opacity slider
            item {
                Column {
                    Text(
                        text = "Opacity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Transparent", style = MaterialTheme.typography.bodySmall)
                        
                        Slider(
                            value = fabOpacity,
                            onValueChange = { fabOpacity = it },
                            valueRange = 0.5f..1f,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text("Opaque", style = MaterialTheme.typography.bodySmall)
                    }
                    
                    Text(
                        text = "${(fabOpacity * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Quick effect shortcuts
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Quick Effect Shortcuts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Configure up to 6 quick access effects for the FAB menu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(6) { i ->
                                IconButton(
                                    onClick = { /* Configure shortcut */ }
                                ) {
                                    Icon(
                                        androidx.compose.material.icons.Icons.Default.Add,
                                        contentDescription = "Add shortcut"
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Reset button
            item {
                OutlinedButton(
                    onClick = {
                        dispenserStyle = CandyDispenserStyle.GUMBALL_MACHINE
                        candyType = CandyType.GUMBALL
                        trailEffect = TrailEffectType.SPARKLE
                        fabSize = 1f
                        fabOpacity = 1f
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset to Defaults")
                }
            }
        }
    }
}

private fun getStyleDisplayName(style: CandyDispenserStyle): String {
    return when (style) {
        CandyDispenserStyle.GUMBALL_MACHINE -> "Gumball"
        CandyDispenserStyle.JAWBREAKER -> "Jawbreaker"
        CandyDispenserStyle.LOLLIPOP_DISPENSER -> "Lollipop"
    }
}

private fun getCandyDisplayName(type: CandyType): String {
    return when (type) {
        CandyType.GUMBALL -> "Gumball"
        CandyType.JELLY_BEAN -> "Jelly Bean"
        CandyType.LOLLIPOP -> "Lollipop"
        CandyType.CANDY_CANE -> "Candy Cane"
        CandyType.SOUR_BALL -> "Sour Ball"
    }
}

private fun getTrailDisplayName(type: TrailEffectType): String {
    return when (type) {
        TrailEffectType.SPARKLE -> "Sparkle"
        TrailEffectType.CANDY_DUST -> "Candy Dust"
        TrailEffectType.RAINBOW -> "Rainbow"
        TrailEffectType.HEARTS -> "Hearts"
        TrailEffectType.STARS -> "Stars"
    }
}
