package com.sugarmunch.app.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.effects.EffectEngine
import com.sugarmunch.app.ui.theme.CandyPink
import com.sugarmunch.app.ui.theme.CottonCandyBlue
import com.sugarmunch.app.ui.theme.CandyMint
import com.sugarmunch.app.ui.theme.CandyPurple
import java.util.Calendar

data class ScheduledEffect(
    val id: String,
    val effectId: String,
    val effectName: String,
    val hour: Int,
    val minute: Int,
    val days: Set<Int>, // 1-7 for Sunday-Saturday
    val enabled: Boolean = true
) {
    val timeString: String
        get() = String.format("%02d:%02d", hour, minute)
    
    val daysString: String
        get() = days.map { 
            when (it) {
                Calendar.SUNDAY -> "Sun"
                Calendar.MONDAY -> "Mon"
                Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"
                Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"
                Calendar.SATURDAY -> "Sat"
                else -> ""
            }
        }.joinToString(", ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectScheduleScreen(
    onBack: () -> Unit
) {
    // Sample scheduled effects
    val scheduledEffects = remember {
        mutableStateListOf(
            ScheduledEffect(
                id = "1",
                effectId = "sugarrush",
                effectName = "Sugarrush",
                hour = 9,
                minute = 0,
                days = setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY),
                enabled = true
            ),
            ScheduledEffect(
                id = "2",
                effectId = "candy_confetti",
                effectName = "Candy Confetti",
                hour = 17,
                minute = 0,
                days = setOf(Calendar.FRIDAY),
                enabled = false
            )
        )
    }
    
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Schedule Effects")
                        Text(
                            "${scheduledEffects.count { it.enabled }} active schedules",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CandyPink,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Schedule")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CandyPurple.copy(alpha = 0.12f),
                            CottonCandyBlue.copy(alpha = 0.08f)
                        )
                    )
                )
        ) {
            if (scheduledEffects.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "\uD83D\uDD52",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Scheduled Effects",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Tap + to schedule an effect",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(scheduledEffects, key = { it.id }) { schedule ->
                        ScheduledEffectCard(
                            schedule = schedule,
                            onToggle = { enabled ->
                                val index = scheduledEffects.indexOf(schedule)
                                if (index >= 0) {
                                    scheduledEffects[index] = schedule.copy(enabled = enabled)
                                }
                            },
                            onDelete = {
                                scheduledEffects.remove(schedule)
                            }
                        )
                    }
                    
                    // Add hint card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = CandyMint.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = CandyMint,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Scheduled effects will automatically activate at the specified time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Add schedule dialog
        if (showAddDialog) {
            AddScheduleDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { newSchedule ->
                    scheduledEffects.add(newSchedule)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun ScheduledEffectCard(
    schedule: ScheduledEffect,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val effectColor = getEffectColor(schedule.effectId)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (schedule.enabled) {
                effectColor.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (schedule.enabled) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time display
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = if (schedule.enabled) {
                                listOf(effectColor, effectColor.copy(alpha = 0.7f))
                            } else {
                                listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.2f))
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        schedule.timeString,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    schedule.effectName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    schedule.daysString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Switch(
                checked = schedule.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = effectColor,
                    checkedTrackColor = effectColor.copy(alpha = 0.5f)
                )
            )
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddScheduleDialog(
    onDismiss: () -> Unit,
    onAdd: (ScheduledEffect) -> Unit
) {
    var selectedEffect by remember { mutableStateOf(EffectEngine.allEffects().first()) }
    var selectedHour by remember { mutableStateOf(12) }
    var selectedMinute by remember { mutableStateOf(0) }
    val selectedDays = remember { mutableStateListOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY) }
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Effect") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Effect selection
                Text(
                    "Select Effect",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EffectEngine.allEffects().take(4).forEach { effect ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (effect.id == selectedEffect.id) {
                                        getEffectColor(effect.id).copy(alpha = 0.2f)
                                    } else {
                                        Color.Transparent
                                    }
                                )
                                .padding(8.dp)
                        ) {
                            Text(
                                getEffectEmoji(effect.id),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                effect.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (effect.id == selectedEffect.id) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = getEffectColor(effect.id),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                
                // Time selection
                Text(
                    "Time: ${String.format("%02d:%02d", selectedHour, selectedMinute)}",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedHour = (selectedHour - 1).coerceIn(0, 23) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("-")
                    }
                    Text(
                        "${String.format("%02d", selectedHour)}:${String.format("%02d", selectedMinute)}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(2f)
                    )
                    Button(
                        onClick = { selectedHour = (selectedHour + 1).coerceIn(0, 23) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        ScheduledEffect(
                            id = System.currentTimeMillis().toString(),
                            effectId = selectedEffect.id,
                            effectName = selectedEffect.name,
                            hour = selectedHour,
                            minute = selectedMinute,
                            days = selectedDays.toSet()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CandyPink
                )
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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