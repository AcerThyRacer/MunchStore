package com.sugarmunch.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sugarmunch.app.crash.GlobalExceptionHandler
import com.sugarmunch.app.crash.Severity
import kotlinx.coroutines.launch

/**
 * Error Boundary for Compose - Catches and displays errors gracefully
 * Wraps composables to prevent crashes from propagating
 */
@Composable
fun ErrorBoundary(
    content: @Composable () -> Unit,
    onError: ((Throwable) -> Unit)? = null,
    fallback: @Composable (ErrorInfo) -> Unit = { DefaultErrorFallback(it) }
) {
    var error: Throwable? by remember { mutableStateOf(null) }
    var errorInfo: ErrorInfo? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()

    // Set up error handler for this boundary
    LaunchedEffect(Unit) {
        // In production, you'd use a more sophisticated error catching mechanism
        // This is a simplified version
    }

    if (error != null && errorInfo != null) {
        fallback(errorInfo!!)
    } else {
        // Wrap content with error catching
        // Note: Compose doesn't have native error boundaries like React
        // We use a try-catch approach with state management
        try {
            content()
        } catch (e: Exception) {
            error = e
            errorInfo = ErrorInfo(
                throwable = e,
                message = e.message ?: "Unknown error",
                timestamp = System.currentTimeMillis()
            )
            onError?.invoke(e)
            
            // Report to crash handler (non-fatal)
            scope.launch {
                try {
                    GlobalExceptionHandler.getInstance().reportCaughtException(
                        throwable = e,
                        severity = Severity.ERROR
                    )
                } catch (ex: Exception) {
                    // Silently fail
                }
            }
        }
    }
}

/**
 * Default error fallback UI
 */
@Composable
fun DefaultErrorFallback(errorInfo: ErrorInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Oops! Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = errorInfo.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { /* Retry action */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
            
            TextButton(
                onClick = { /* Show details */ }
            ) {
                Text("Show Details")
            }
        }
    }
}

/**
 * Inline error message display
 */
@Composable
fun InlineError(
    message: String,
    onDismiss: () -> Unit = {},
    onRetry: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { -20 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -20 }) + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                Row {
                    onRetry?.let {
                        TextButton(onClick = it) {
                            Text("Retry")
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Error details dialog
 */
@Composable
fun ErrorDetailsDialog(
    errorInfo: ErrorInfo,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Error Details",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                InfoRow("Type", errorInfo.throwable::class.java.simpleName)
                InfoRow("Message", errorInfo.message)
                InfoRow("Time", errorInfo.formattedTime)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Stack Trace",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Text(
                        text = errorInfo.stackTrace,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { /* Copy to clipboard */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy to Clipboard")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Loading error state with retry
 */
@Composable
fun LoadingErrorState(
    message: String = "Failed to load content",
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

/**
 * Error info data class
 */
data class ErrorInfo(
    val throwable: Throwable,
    val message: String,
    val timestamp: Long,
    val stackTrace: String = throwable.stackTraceToString()
) {
    val formattedTime: String
        get() = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
}

/**
 * Network error specific UI
 */
@Composable
fun NetworkErrorState(
    onRetry: () -> Unit
) {
    LoadingErrorState(
        message = "No internet connection. Please check your network and try again.",
        onRetry = onRetry
    )
}

/**
 * Empty state with optional action
 */
@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Inbox,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        actionLabel?.let { label ->
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { onAction?.invoke() }) {
                Text(label)
            }
        }
    }
}
