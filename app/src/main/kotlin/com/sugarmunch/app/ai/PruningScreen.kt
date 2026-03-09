package com.sugarmunch.app.ai

import android.app.Activity
import android.app.usage.UsageStats
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.rememberCoroutineScope

/**
 * Smart App Pruning: suggests offloading apps not used in months.
 * Backup to P2P can be wired when that flow is ready.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PruningScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    val analytics = remember { AppUsageAnalytics.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    var unusedSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasPermission by remember { mutableStateOf(false) }
    var lastUsedInfo by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(Unit) {
        // Check if we have usage stats permission
        hasPermission = analytics.hasUsageStatsPermission()
        
        if (hasPermission) {
            // Load unused apps (not used in 3 months)
            unusedSuggestions = analytics.getUnusedAppIds(months = 3)
            
            // Get last used info for each unused app
            val infoMap = mutableMapOf<String, String>()
            unusedSuggestions.forEach { packageName ->
                val stats = analytics.getAppUsageStats(packageName, days = 90)
                infoMap[packageName] = if (stats != null && stats.lastTimeUsed > 0) {
                    val daysSinceUse = (System.currentTimeMillis() - stats.lastTimeUsed) / (24 * 60 * 60 * 1000)
                    "Last used ${daysSinceUse} days ago"
                } else {
                    "Never used"
                }
            }
            lastUsedInfo = infoMap
        }
        
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage & pruning", color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            AnimatedThemeBackground()
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.primary
                )
            } else if (!hasPermission) {
                // Show permission request UI
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = colors.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Usage Access Required",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "To suggest unused apps, SugarMunch needs access to your app usage statistics.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            analytics.openUsageStatsSettings()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Open Settings", color = colors.onPrimary)
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                    item {
                        Text(
                            "Apps you haven't used",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.primary
                        )
                        Text(
                            "Offload to free space. Data can be backed up to P2P.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    if (unusedSuggestions.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "No suggestions right now.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.onSurface
                                    )
                                    Text(
                                        "All your apps have been used recently!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    } else {
                        items(unusedSuggestions) { packageName ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            packageName.substringAfterLast("."),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.onSurface
                                        )
                                        Text(
                                            lastUsedInfo[packageName] ?: "Unknown",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                    IconButton(onClick = {
                                        // TODO: Implement offload functionality
                                    }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Offload",
                                            tint = colors.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
