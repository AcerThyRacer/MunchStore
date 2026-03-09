package com.sugarmunch.app.ui.screens.automation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.sugarmunch.app.automation.*
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationScreen(
    onBack: () -> Unit,
    onCreateTask: () -> Unit,
    onEditTask: (String) -> Unit,
    onTemplatesClick: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    val engine = remember { AutomationEngine.getInstance(context) }
    val tasks by engine.getAllTasks().collectAsState(initial = emptyList())
    val history by engine.getExecutionHistory(10).collectAsState(initial = emptyList())
    
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Automation",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
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
                    IconButton(onClick = onTemplatesClick) {
                        Icon(
                            Icons.Default.Collections,
                            contentDescription = "Templates",
                            tint = colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateTask,
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New Automation") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedThemeBackground()
            
            if (tasks.isEmpty()) {
                EmptyAutomationState(
                    colors = colors,
                    onCreateClick = onCreateTask,
                    onTemplatesClick = onTemplatesClick
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Stats section
                    item {
                        AutomationStatsCard(
                            activeCount = tasks.count { it.enabled },
                            totalRuns = tasks.sumOf { it.runCount.toLong() },
                            lastRun = history.firstOrNull(),
                            colors = colors
                        )
                    }
                    
                    // Section title
                    item {
                        Text(
                            text = "Your Automations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    // Task list
                    items(
                        items = tasks,
                        key = { it.id }
                    ) { task ->
                        AutomationCard(
                            task = task,
                            colors = colors,
                            onToggle = { enabled ->
                                scope.launch {
                                    engine.setTaskEnabled(task.id, enabled)
                                }
                            },
                            onEdit = { onEditTask(task.id) },
                            onDelete = { showDeleteDialog = task.id },
                            onRun = {
                                scope.launch {
                                    engine.runTask(task.id, "manual")
                                }
                            }
                        )
                    }
                    
                    // Spacer for FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    showDeleteDialog?.let { taskId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Automation?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            engine.deleteTask(taskId)
                            showDeleteDialog = null
                        }
                    }
                ) {
                    Text("Delete", color = colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            },
            containerColor = colors.surface
        )
    }
}

@Composable
private fun EmptyAutomationState(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onCreateClick: () -> Unit,
    onTemplatesClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated icon
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors.primary.copy(alpha = 0.3f),
                            colors.primary.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoMode,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = colors.primary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No Automations Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Create custom workflows to automate SugarMunch effects, themes, and actions based on time, events, or conditions.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onTemplatesClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            ),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(Icons.Default.Collections, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Browse Templates")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onCreateClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.primary
            ),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create From Scratch")
        }
    }
}

@Composable
private fun AutomationStatsCard(
    activeCount: Int,
    totalRuns: Long,
    lastRun: ExecutionRecord?,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = activeCount.toString(),
                label = "Active",
                icon = Icons.Default.PlayCircle,
                color = colors.primary,
                colors = colors
            )
            
            VerticalDivider(
                modifier = Modifier.height(40.dp),
                color = colors.onSurface.copy(alpha = 0.1f)
            )
            
            StatItem(
                value = totalRuns.toString(),
                label = "Total Runs",
                icon = Icons.Default.Repeat,
                color = colors.secondary,
                colors = colors
            )
            
            VerticalDivider(
                modifier = Modifier.height(40.dp),
                color = colors.onSurface.copy(alpha = 0.1f)
            )
            
            StatItem(
                value = lastRun?.let { formatTimeAgo(it.triggerTime) } ?: "Never",
                label = "Last Run",
                icon = Icons.Default.Schedule,
                color = colors.tertiary,
                colors = colors,
                isTextValue = true
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    isTextValue: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = color.copy(alpha = 0.15f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            style = if (isTextValue) MaterialTheme.typography.bodyMedium 
                   else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun AutomationCard(
    task: AutomationTask,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRun: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    val triggerIcon = getTriggerIcon(task.trigger)
    val triggerDescription = getTriggerDescription(task.trigger)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Trigger icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(colors.primary.copy(alpha = 0.3f), 
                                       colors.secondary.copy(alpha = 0.3f))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = triggerIcon,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Task info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = triggerDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Last run info
                    task.lastRunAt?.let { lastRun ->
                        Text(
                            text = "Last run: ${formatTimeAgo(lastRun)} • ${task.runCount} runs",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } ?: run {
                        Text(
                            text = "Never run",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // Toggle switch
                Switch(
                    checked = task.enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.primary,
                        checkedTrackColor = colors.primary.copy(alpha = 0.5f)
                    )
                )
            }
            
            // Actions row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                // Run button
                if (task.enabled) {
                    IconButton(
                        onClick = onRun,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Run now",
                            tint = colors.primary
                        )
                    }
                }
                
                // Edit button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                // More menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            leadingIcon = {
                                Icon(Icons.Default.ContentCopy, null)
                            },
                            onClick = {
                                showMenu = false
                                // Duplicate logic would go here
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, null, tint = colors.error)
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun getTriggerIcon(trigger: AutomationTrigger): ImageVector {
    return when (trigger) {
        is AutomationTrigger.TimeTrigger -> Icons.Default.Schedule
        is AutomationTrigger.IntervalTrigger -> Icons.Default.Repeat
        is AutomationTrigger.SunriseSunsetTrigger -> Icons.Default.WbSunny
        is AutomationTrigger.AppOpenedTrigger -> Icons.Default.Apps
        is AutomationTrigger.AppClosedTrigger -> Icons.Default.AppShortcut
        is AutomationTrigger.EffectToggledTrigger -> Icons.Default.AutoAwesome
        is AutomationTrigger.ThemeChangedTrigger -> Icons.Default.Palette
        is AutomationTrigger.BatteryLevelTrigger -> Icons.Default.BatteryAlert
        is AutomationTrigger.ChargingTrigger -> Icons.Default.BatteryChargingFull
        is AutomationTrigger.WifiConnectedTrigger -> Icons.Default.Wifi
        is AutomationTrigger.BluetoothConnectedTrigger -> Icons.Default.Bluetooth
        is AutomationTrigger.ScreenStateTrigger -> Icons.Default.Smartphone
        is AutomationTrigger.GeofenceTrigger -> Icons.Default.LocationOn
        is AutomationTrigger.ShakeTrigger -> Icons.Default.Vibration
        is AutomationTrigger.OrientationTrigger -> Icons.Default.ScreenRotation
        is AutomationTrigger.ProximityTrigger -> Icons.Default.Sensors
        is AutomationTrigger.ManualTrigger -> Icons.Default.TouchApp
        else -> Icons.Default.AutoMode
    }
}

private fun getTriggerDescription(trigger: AutomationTrigger): String {
    return when (trigger) {
        is AutomationTrigger.TimeTrigger -> {
            val days = if (trigger.repeatDays.isEmpty()) "once"
            else if (trigger.repeatDays.size == 7) "daily"
            else "on ${trigger.repeatDays.size} days"
            "At ${trigger.hour}:${trigger.minute.toString().padStart(2, '0')} $days"
        }
        is AutomationTrigger.IntervalTrigger -> "Every ${trigger.intervalMinutes} minutes"
        is AutomationTrigger.SunriseSunsetTrigger -> "At ${trigger.event.name.lowercase()}"
        is AutomationTrigger.AppOpenedTrigger -> "When ${trigger.packageNames.size} apps open"
        is AutomationTrigger.AppClosedTrigger -> "When ${trigger.packageNames.size} apps close"
        is AutomationTrigger.EffectToggledTrigger -> "When effect toggled"
        is AutomationTrigger.ThemeChangedTrigger -> "When theme changes"
        is AutomationTrigger.BatteryLevelTrigger -> "When battery ${trigger.operator.name.lowercase().replace("_", " ")} ${trigger.level}%"
        is AutomationTrigger.ChargingTrigger -> "When ${trigger.state.name.lowercase().replace("_", " ")}"
        is AutomationTrigger.WifiConnectedTrigger -> if (trigger.anyWifi) "On WiFi connect" else "On specific WiFi"
        is AutomationTrigger.ScreenStateTrigger -> "When screen ${trigger.state.name.lowercase()}"
        is AutomationTrigger.GeofenceTrigger -> "${trigger.transition.name.lowercase().replaceFirstChar { it.uppercase() }} location"
        is AutomationTrigger.ShakeTrigger -> "On shake (${trigger.sensitivity.name.lowercase()})"
        is AutomationTrigger.OrientationTrigger -> "On ${trigger.orientation.name.lowercase().replace("_", " ")}"
        is AutomationTrigger.ProximityTrigger -> "On proximity ${trigger.state.name.lowercase()}"
        is AutomationTrigger.ManualTrigger -> "Manual trigger"
        else -> "Custom trigger"
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hr ago"
        days == 1L -> "Yesterday"
        days < 7 -> "$days days ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
