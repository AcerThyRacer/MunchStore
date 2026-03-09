package com.sugarmunch.app.ui.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sugarmunch.app.auth.AuthManager
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    onBack: () -> Unit,
    viewModel: AccountSettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    val uiState by viewModel.uiState.collectAsState()
    val showDeleteConfirmation = viewModel.showDeleteDialog.collectAsState(initial = false)
    val showExportOptions = viewModel.showExportDialog.collectAsState(initial = false)
    val errorMessage = viewModel.errorMessage.collectAsState(initial = null)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Account",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colors.onSurface
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
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colors.background.copy(alpha = 0.95f),
                                colors.background.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Account Info Card
                AccountInfoCard(
                    user = uiState.user,
                    colors = colors
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Account Actions Card
                AccountActionsCard(
                    isAnonymous = uiState.user?.isAnonymous == true,
                    isGoogleLinked = uiState.user?.isAnonymous == false,
                    colors = colors,
                    onLinkGoogle = { viewModel.linkWithGoogle() },
                    onChangeEmail = { viewModel.showChangeEmailDialog() },
                    onChangePassword = { viewModel.showChangePasswordDialog() }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Privacy & Data Card
                PrivacyDataCard(
                    colors = colors,
                    onExportData = { viewModel.exportPersonalData() },
                    onPrivacySettings = { /* Navigate to privacy settings */ }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Danger Zone Card
                DangerZoneCard(
                    colors = colors,
                    onSignOut = { viewModel.signOut() },
                    onDeleteAccount = { viewModel.showDeleteConfirmation() }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Account Stats Card
                AccountStatsCard(
                    stats = uiState.stats,
                    colors = colors
                )
            }
        }
        
        // Delete Confirmation Dialog
        if (showDeleteConfirmation.value) {
            DeleteAccountDialog(
                onDismiss = { viewModel.dismissDeleteDialog() },
                onConfirm = { viewModel.deleteAccount() },
                colors = colors
            )
        }
        
        // Export Options Dialog
        if (showExportOptions.value) {
            ExportDataDialog(
                onDismiss = { viewModel.dismissExportDialog() },
                onExportJSON = { viewModel.exportDataAsJSON() },
                onExportZIP = { viewModel.exportDataAsZIP() },
                colors = colors
            )
        }
        
        // Error Snackbar
        errorMessage.value?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = colors.error,
                contentColor = colors.onError
            ) {
                Text(error)
            }
            LaunchedEffect(error) {
                viewModel.dismissError()
            }
        }
    }
}

@Composable
private fun AccountInfoCard(
    user: com.sugarmunch.app.auth.UserSession?,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Surface(
                    shape = RoundedCornerShape(50),
                    color = colors.primary.copy(alpha = 0.2f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (!user?.photoUrl.isNullOrEmpty()) {
                            // TODO: Load image with Coil
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = user?.displayName ?: "Anonymous User",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    
                    Text(
                        text = user?.email ?: "Anonymous Account",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                    
                    if (user?.isAnonymous == true) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Anonymous", style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = colors.tertiary.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Account Details
            AccountDetailRow(
                icon = Icons.Default.Fingerprint,
                label = "User ID",
                value = user?.uid?.take(8) + "..." ?: "N/A",
                colors = colors
            )
            
            AccountDetailRow(
                icon = Icons.Default.CalendarToday,
                label = "Created",
                value = formatDate(user?.createdAt),
                colors = colors
            )
            
            AccountDetailRow(
                icon = Icons.Default.Login,
                label = "Last Sign In",
                value = formatDate(user?.lastSignInAt),
                colors = colors
            )
        }
    }
}

@Composable
private fun AccountDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface
            )
        }
    }
}

@Composable
private fun AccountActionsCard(
    isAnonymous: Boolean,
    isGoogleLinked: Boolean,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onLinkGoogle: () -> Unit,
    onChangeEmail: () -> Unit,
    onChangePassword: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Account Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isAnonymous) {
                Button(
                    onClick = onLinkGoogle,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4285F4) // Google Blue
                    )
                ) {
                    Icon(
                        Icons.Default.Login,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Link Google Account")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                AssistChip(
                    onClick = { },
                    label = { Text("Upgrade to keep your data", style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = colors.tertiary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                if (!isGoogleLinked) {
                    Button(
                        onClick = onLinkGoogle,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4285F4)
                        )
                    ) {
                        Icon(
                            Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Link Google Account")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                OutlinedButton(
                    onClick = onChangeEmail,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.secondary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(colors.secondary, colors.secondary))
                    )
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Email")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = onChangePassword,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.secondary
                    )
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Password")
                }
            }
        }
    }
}

@Composable
private fun PrivacyDataCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onExportData: () -> Unit,
    onPrivacySettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Privacy & Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Export Data Button
            Button(
                onClick = onExportData,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary
                )
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export My Data")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Download all your themes, effects, settings, and personal data",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Privacy Settings (placeholder)
            OutlinedButton(
                onClick = onPrivacySettings,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = false,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.onSurface.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Privacy Settings (Coming Soon)")
            }
        }
    }
}

@Composable
private fun DangerZoneCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.error.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Danger Zone",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.error
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Sign Out Button
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.error
                ),
                border = androidx.compose.ui.Modifier.drawBehind {
                    drawRoundRect(
                        color = colors.error,
                        size = size,
                        style = androidx.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                }
            ) {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Delete Account Button
            Button(
                onClick = onDeleteAccount,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.error
                )
            ) {
                Icon(
                    Icons.Default.DeleteForever,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Account")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "⚠️ Deleting your account is permanent and cannot be undone",
                style = MaterialTheme.typography.bodySmall,
                color = colors.error.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun AccountStatsCard(
    stats: com.sugarmunch.app.ui.settings.AccountStats?,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    if (stats == null) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Your Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.Palette,
                    value = stats.themesCreated.toString(),
                    label = "Themes",
                    colors = colors
                )
                
                StatItem(
                    icon = Icons.Default.AutoAwesome,
                    value = stats.effectsCreated.toString(),
                    label = "Effects",
                    colors = colors
                )
                
                StatItem(
                    icon = Icons.Default.Downloading,
                    value = stats.totalDownloads.toString(),
                    label = "Downloads",
                    colors = colors
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.Star,
                    value = stats.level.toString(),
                    label = "Level",
                    colors = colors
                )
                
                StatItem(
                    icon = Icons.Default.TrendingUp,
                    value = stats.xp.toString(),
                    label = "XP",
                    colors = colors
                )
                
                StatItem(
                    icon = Icons.Default.ShoppingCart,
                    value = stats.sugarPoints.toString(),
                    label = "Sugar Points",
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = colors.error
            )
        },
        title = {
            Text("Delete Account?")
        },
        text = {
            Column {
                Text("This action cannot be undone.")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "All your data including:",
                    fontWeight = FontWeight.Bold
                )
                Text("• Custom themes and effects")
                Text("• Automation tasks")
                Text("• Sugar Points and progress")
                Text("• Clan membership")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Will be permanently deleted.")
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.error
                )
            ) {
                Text("Delete Forever")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = colors.surface
    )
}

@Composable
private fun ExportDataDialog(
    onDismiss: () -> Unit,
    onExportJSON: () -> Unit,
    onExportZIP: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Download,
                contentDescription = null,
                tint = colors.primary
            )
        },
        title = {
            Text("Export Data")
        },
        text = {
            Text("Choose export format:")
        },
        confirmButton = {
            Button(
                onClick = onExportZIP,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary
                )
            ) {
                Text("Export as ZIP")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onExportJSON) {
                    Text("JSON")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
        containerColor = colors.surface
    )
}

private fun formatDate(timestamp: Long?): String {
    return if (timestamp != null) {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(timestamp))
    } else {
        "N/A"
    }
}
