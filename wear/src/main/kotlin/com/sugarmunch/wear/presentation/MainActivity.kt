package com.sugarmunch.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.compose.layout.fadeAway
import com.sugarmunch.wear.data.ConnectionState
import com.sugarmunch.wear.data.WearDataLayer
import com.sugarmunch.wear.presentation.effects.EffectControlScreen
import com.sugarmunch.wear.presentation.quick.QuickActionsScreen
import com.sugarmunch.wear.presentation.theme.ThemeSwitcherScreen
import com.sugarmunch.wear.presentation.theme.WearAppTheme

/**
 * Main Activity for Wear OS SugarMunch companion app
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearAppTheme {
                SugarMunchWearApp()
            }
        }
    }
}

/**
 * Main app navigation and scaffold
 */
@Composable
fun SugarMunchWearApp() {
    val navController = rememberSwipeDismissableNavController()
    val context = LocalContext.current
    val wearDataLayer = remember { WearDataLayer(context) }
    
    // Initialize connection check
    LaunchedEffect(Unit) {
        wearDataLayer.checkPhoneConnection()
        wearDataLayer.requestSync()
    }

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = {
            // Position indicator is handled per-screen
        }
    ) {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = Screen.Main.route
        ) {
            composable(Screen.Main.route) {
                MainScreen(
                    navController = navController,
                    wearDataLayer = wearDataLayer
                )
            }
            composable(Screen.Effects.route) {
                EffectControlScreen(
                    navController = navController,
                    wearDataLayer = wearDataLayer
                )
            }
            composable(Screen.Themes.route) {
                ThemeSwitcherScreen(
                    navController = navController,
                    wearDataLayer = wearDataLayer
                )
            }
            composable(Screen.QuickActions.route) {
                QuickActionsScreen(
                    navController = navController,
                    wearDataLayer = wearDataLayer
                )
            }
        }
    }
}

/**
 * Main screen with menu options
 */
@Composable
fun MainScreen(
    navController: NavHostController,
    wearDataLayer: WearDataLayer
) {
    val connectionState by wearDataLayer.connectionState.collectAsState()
    val activeEffectCount by wearDataLayer.activeEffectCount.collectAsState()
    val listState = rememberScalingLazyListState()

    Scaffold(
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        timeText = { 
            TimeText(
                modifier = Modifier.fadeAway { listState }
            ) 
        }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            anchorType = ScalingLazyListAnchorType.ItemStart
        ) {
            // App Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🍬",
                        style = MaterialTheme.typography.display3,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "SugarMunch",
                        style = MaterialTheme.typography.title3,
                        color = MaterialTheme.colors.primary
                    )
                    ConnectionStatusChip(connectionState)
                }
            }

            // Active Effects Summary
            if (activeEffectCount > 0) {
                item {
                    ActiveEffectsCard(activeEffectCount)
                }
            }

            // Menu Items
            item {
                MenuChip(
                    icon = "✨",
                    label = "Effects",
                    secondaryLabel = "$activeEffectCount active",
                    onClick = { navController.navigate(Screen.Effects.route) }
                )
            }

            item {
                MenuChip(
                    icon = "🎨",
                    label = "Themes",
                    secondaryLabel = "Change look",
                    onClick = { navController.navigate(Screen.Themes.route) }
                )
            }

            item {
                MenuChip(
                    icon = "⚡",
                    label = "Quick Actions",
                    secondaryLabel = "Presets & Boost",
                    onClick = { navController.navigate(Screen.QuickActions.route) }
                )
            }

            // All Off Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { wearDataLayer.sendAllOff() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.secondaryButtonColors(
                        backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.8f)
                    ),
                    enabled = activeEffectCount > 0
                ) {
                    Text("🛑 All Off")
                }
            }
        }
    }
}

/**
 * Connection status chip
 */
@Composable
fun ConnectionStatusChip(connectionState: ConnectionState) {
    val (icon, text, color) = when (connectionState) {
        is ConnectionState.Connected -> Triple("📱", "Connected", MaterialTheme.colors.primary)
        is ConnectionState.Disconnected -> Triple("❌", "Disconnected", MaterialTheme.colors.error)
        is ConnectionState.Error -> Triple("⚠️", "Error", MaterialTheme.colors.error)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.caption2
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.caption3,
            color = color
        )
    }
}

/**
 * Active effects summary card
 */
@Composable
fun ActiveEffectsCard(count: Int) {
    Card(
        onClick = { },
        backgroundPainter = androidx.wear.compose.material.CardDefaults.cardBackgroundPainter(
            startBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.2f),
            endBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "✨",
                style = MaterialTheme.typography.title2
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "$count Effects",
                    style = MaterialTheme.typography.button
                )
                Text(
                    text = "Active on phone",
                    style = MaterialTheme.typography.caption3,
                    color = MaterialTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Menu chip component
 */
@Composable
fun MenuChip(
    icon: String,
    label: String,
    secondaryLabel: String,
    onClick: () -> Unit
) {
    Chip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        secondaryLabel = {
            Text(
                text = secondaryLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        icon = {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon)
            }
        },
        colors = ChipDefaults.primaryChipColors(),
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Screen navigation routes
 */
sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Effects : Screen("effects")
    data object Themes : Screen("themes")
    data object QuickActions : Screen("quick_actions")
}

/**
 * Loading indicator
 */
@Composable
fun LoadingIndicator(message: String = "Loading...") {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.caption2
            )
        }
    }
}

/**
 * Error dialog
 */
@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    Alert(
        title = { Text("Error") },
        message = { Text(message) },
        onDismiss = onDismiss
    ) {
        item {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.primaryButtonColors()
            ) {
                Text("OK")
            }
        }
    }
}
