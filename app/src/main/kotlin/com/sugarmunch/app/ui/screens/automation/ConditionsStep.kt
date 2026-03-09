package com.sugarmunch.app.ui.screens.automation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.default.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.automation.AutomationCondition
import com.sugarmunch.app.theme.model.AdjustedColors

/**
 * The second step in the task builder wizard - conditions configuration.
 *
 * Allows users to add optional conditions that must be met for the
 * automation to run. Includes quick condition chips and a list of
 * selected conditions.
 *
 * @param builderState The state holder for the task builder
 * @param colors The theme colors to use for styling
 */
@Composable
fun ConditionsStep(
    builderState: VisualTaskBuilderState,
    colors: AdjustedColors
) {
    val conditions by builderState.selectedConditions.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Add Conditions (Optional)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )

            Text(
                text = "Only run when these conditions are met",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
        }

        // Quick condition buttons
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickConditionChip(
                    label = "Weekdays only",
                    onClick = {
                        builderState.addCondition(
                            AutomationCondition.TimeCondition(
                                0, 0, 23, 59,
                                daysOfWeek = listOf(1, 2, 3, 4, 5)
                            )
                        )
                    },
                    colors = colors
                )
                QuickConditionChip(
                    label = "Battery above 20%",
                    onClick = {
                        builderState.addCondition(
                            AutomationCondition.BatteryCondition(minLevel = 20)
                        )
                    },
                    colors = colors
                )
                QuickConditionChip(
                    label = "Charging",
                    onClick = {
                        builderState.addCondition(
                            AutomationCondition.BatteryCondition(mustBeCharging = true)
                        )
                    },
                    colors = colors
                )
                QuickConditionChip(
                    label = "WiFi connected",
                    onClick = {
                        builderState.addCondition(
                            AutomationCondition.WifiCondition(connected = true)
                        )
                    },
                    colors = colors
                )
            }
        }

        // Selected conditions list
        if (conditions.isNotEmpty()) {
            item {
                Text(
                    text = "Active Conditions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(conditions) { condition ->
                ConditionCard(
                    condition = condition,
                    colors = colors,
                    onRemove = { builderState.removeCondition(condition) }
                )
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Conditions are optional. If none are set, the automation will always run when triggered.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

/**
 * A quick action chip for adding common conditions.
 *
 * @param label The text to display on the chip
 * @param onClick Callback when the chip is clicked
 * @param colors The theme colors to use for styling
 */
@Composable
private fun QuickConditionChip(
    label: String,
    onClick: () -> Unit,
    colors: AdjustedColors
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = colors.primary.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.primary
            )
        }
    }
}

/**
 * Displays a selected condition with remove functionality.
 *
 * @param condition The condition to display
 * @param colors The theme colors to use for styling
 * @param onRemove Callback when the remove button is clicked
 */
@Composable
private fun ConditionCard(
    condition: AutomationCondition,
    colors: AdjustedColors,
    onRemove: () -> Unit
) {
    val (icon, title, description) = when (condition) {
        is AutomationCondition.TimeCondition -> Triple(
            Icons.Default.Schedule,
            "Time Window",
            "${condition.startHour}:${condition.startMinute.toString().padStart(2, '0')} - " +
            "${condition.endHour}:${condition.endMinute.toString().padStart(2, '0')}"
        )
        is AutomationCondition.BatteryCondition -> Triple(
            Icons.Default.BatteryFull,
            "Battery Level",
            listOfNotNull(
                condition.minLevel?.let { "Above $it%" },
                condition.maxLevel?.let { "Below $it%" },
                condition.mustBeCharging?.let { if (it) "Charging" else "Not charging" }
            ).joinToString(", ")
        )
        is AutomationCondition.WifiCondition -> Triple(
            Icons.Default.Wifi,
            "WiFi",
            if (condition.connected) "Connected${condition.ssid?.let { " to $it" } ?: ""}"
            else "Disconnected"
        )
        else -> Triple(Icons.Default.Help, "Condition", "Custom condition")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = colors.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = colors.primary)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = colors.error
                )
            }
        }
    }
}
