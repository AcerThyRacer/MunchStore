package com.sugarmunch.app.ui.customization

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.onboarding.CandyFactoryOnboarding

/**
 * Onboarding Replay Screen
 * Allow users to replay the Candy Factory onboarding
 */

@Composable
fun OnboardingReplayScreen(
    onNavigateBack: () -> Unit,
    onRestartOnboarding: () -> Unit
) {
    var showOnboarding by remember { mutableStateOf(false) }
    
    if (showOnboarding) {
        CandyFactoryOnboarding(
            onComplete = {
                showOnboarding = false
                onRestartOnboarding()
            },
            onRequestOverlayPermission = {
                // Handle overlay permission request
                val intent = android.content.Intent(
                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:${androidx.compose.ui.platform.LocalContext.current.packageName}")
                )
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                androidx.compose.ui.platform.LocalContext.current.startActivity(intent)
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Candy Factory Onboarding") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon
                Text(
                    text = "🏭",
                    style = MaterialTheme.typography.displayLarge
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Title
                Text(
                    text = "Candy Factory Onboarding",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                Text(
                    text = "Relive the sweetest onboarding experience! Go through the 7-screen Candy Factory tour again.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // What's included
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "What's Included:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        listOf(
                            "🍬 Welcome to the Candy Factory",
                            "🎨 Choose Your Candy themes",
                            "📊 Set Your Sweetness levels",
                            "🃏 Pick Your Style cards",
                            "✨ Enable the Magic FAB",
                            "⚡ Sugar Rush Mode",
                            "🎉 Ready to Explore!"
                        ).forEach { item ->
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { showOnboarding = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Start Tour")
                    }
                }
            }
        }
    }
}
