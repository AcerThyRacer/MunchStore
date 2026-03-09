package com.sugarmunch.app.ui.screens.automation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.automation.AutomationTrigger
import com.sugarmunch.app.theme.model.AdjustedColors

/**
 * The fourth and final step in the task builder wizard - review and save.
 *
 * Displays a summary of the automation configuration and allows users
 * to name and save their automation.
 *
 * @param builderState The state holder for the task builder
 * @param colors The theme colors to use for styling
 */
@Composable
fun ReviewStep(
    builderState: VisualTaskBuilderState,
    colors: AdjustedColors
) {
    val name by builderState.taskName.collectAsState()
    val description by builderState.taskDescription.collectAsState()
    val trigger by builderState.selectedTrigger.collectAsState()
    val conditions by builderState.selectedConditions.collectAsState()
    val actions by builderState.selectedActions.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Review",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )

            Text(
                text = "Review and name your automation",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
        }

        // Name input
        item {
            OutlinedTextField(
                value = name,
                onValueChange = { builderState.setTaskName(it) },
                label = { Text("Automation Name") },
                placeholder = { Text("e.g., Morning Routine") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    focusedLabelColor = colors.primary
                )
            )
        }

        // Description input
        item {
            OutlinedTextField(
                value = description,
                onValueChange = { builderState.setTaskDescription(it) },
                label = { Text("Description (Optional)") },
                placeholder = { Text("What does this automation do?") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    focusedLabelColor = colors.primary
                )
            )
        }

        // Summary card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surface.copy(alpha = 0.95f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Trigger summary
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = colors.primary.copy(alpha = 0.15f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "When",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = trigger?.let { getTriggerDescription(it) } ?: "Not set",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurface
                            )
                        }
                    }

                    // Arrow
                    Box(
                        modifier = Modifier
                            .padding(start = 18.dp, vertical = 8.dp)
                            .width(2.dp)
                            .height(24.dp)
                            .background(colors.onSurface.copy(alpha = 0.2f))
                    )

                    // Conditions summary
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = colors.secondary.copy(alpha = 0.15f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                null,
                                tint = colors.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "If",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = if (conditions.isEmpty()) "Always (no conditions)"
                                       else "${conditions.size} condition(s) met",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurface
                            )
                        }
                    }

                    // Arrow
                    Box(
                        modifier = Modifier
                            .padding(start = 18.dp, vertical = 8.dp)
                            .width(2.dp)
                            .height(24.dp)
                            .background(colors.onSurface.copy(alpha = 0.2f))
                    )

                    // Actions summary
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = colors.tertiary.copy(alpha = 0.15f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                null,
                                tint = colors.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Then",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "${actions.size} action(s) will run",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Validation message
        if (name.isBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colors.error.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            null,
                            tint = colors.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Please enter a name for your automation",
                            color = colors.error
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

/**
 * Gets a human-readable description for a trigger.
 *
 * @param trigger The trigger to describe
 * @return A description string
 */
private fun getTriggerDescription(trigger: AutomationTrigger): String {
    return when (trigger) {
        is AutomationTrigger.TimeTrigger -> {
            "${trigger.hour}:${trigger.minute.toString().padStart(2, '0')}"
        }
        is AutomationTrigger.SunriseTrigger -> "Sunrise"
        is AutomationTrigger.SunsetTrigger -> "Sunset"
        is AutomationTrigger.AppLaunchTrigger -> "App launch"
        is AutomationTrigger.DailyRewardTrigger -> "Daily reward claimed"
        is AutomationTrigger.EffectActivatedTrigger -> "Effect activated"
        is AutomationTrigger.ChargingTrigger -> {
            if (trigger.isCharging) "Charging started" else "Charging stopped"
        }
        is AutomationTrigger.BatteryTrigger -> "Battery at ${trigger.level}%"
        is AutomationTrigger.WifiTrigger -> {
            if (trigger.connected) "WiFi connected" else "WiFi disconnected"
        }
        else -> "Unknown trigger"
    }
}
