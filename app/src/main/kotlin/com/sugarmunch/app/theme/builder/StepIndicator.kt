package com.sugarmunch.app.theme.builder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Step indicator for the theme builder wizard.
 * Displays a numbered circle with completion status and title.
 *
 * @param step The step number (1-based)
 * @param title The title for this step
 * @param isActive Whether this step is currently active
 * @param isCompleted Whether this step has been completed
 */
@Composable
fun StepIndicator(
    step: Int,
    title: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> Brush.linearGradient(
                            colors = listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
                        )
                        isActive -> Brush.linearGradient(
                            colors = listOf(Color(0xFF2196F3), Color(0xFF03A9F4))
                        )
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "$step",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
