package com.sugarmunch.app.ui.screens.automation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.default.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sugarmunch.app.automation.AutomationAction
import com.sugarmunch.app.automation.SugarMunchScreen
import com.sugarmunch.app.automation.VibrationPattern
import com.sugarmunch.app.theme.model.AdjustedColors

/**
 * The third step in the task builder wizard - actions configuration.
 *
 * Allows users to add, remove, and reorder actions that will be
 * executed when the automation is triggered.
 *
 * @param builderState The state holder for the task builder
 * @param colors The theme colors to use for styling
 */
@Composable
fun ActionsStep(
    builderState: VisualTaskBuilderState,
    colors: AdjustedColors
) {
    val actions by builderState.selectedActions.collectAsState()
    var showActionPicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Add Actions",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )

                Text(
                    text = "What should happen when triggered?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )
            }

            if (actions.isEmpty()) {
                item {
                    EmptyActionsState(colors = colors)
                }
            } else {
                itemsIndexed(actions) { index, action ->
                    ActionCard(
                        action = action,
                        index = index,
                        colors = colors,
                        onRemove = { builderState.removeAction(action) },
                        onMoveUp = { builderState.moveAction(index, index - 1) },
                        onMoveDown = { builderState.moveAction(index, index + 1) },
                        canMoveUp = index > 0,
                        canMoveDown = index < actions.size - 1
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // Add action button
        Surface(
            color = colors.surface.copy(alpha = 0.95f),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { showActionPicker = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Action")
            }
        }
    }

    // Action picker dialog
    if (showActionPicker) {
        ActionPickerDialog(
            colors = colors,
            onDismiss = { showActionPicker = false },
            onActionSelected = { action ->
                builderState.addAction(action)
                showActionPicker = false
            }
        )
    }
}

/**
 * Displays an empty state when no actions have been added.
 *
 * @param colors The theme colors to use for styling
 */
@Composable
private fun EmptyActionsState(colors: AdjustedColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = colors.onSurface.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No actions added yet",
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface.copy(alpha = 0.5f)
        )

        Text(
            text = "Tap the button below to add your first action",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Displays an action card with move and remove controls.
 *
 * This component is defined in BuilderComponents.kt - this is just
 * a reference to the shared component.
 *
 * @param action The action to display
 * @param index The position of this action in the list
 * @param colors The theme colors to use for styling
 * @param onRemove Callback when the remove button is clicked
 * @param onMoveUp Callback when the move up button is clicked
 * @param onMoveDown Callback when the move down button is clicked
 * @param canMoveUp Whether the action can be moved up
 * @param canMoveDown Whether the action can be moved down
 */
@Composable
fun ActionCard(
    action: AutomationAction,
    index: Int,
    colors: AdjustedColors,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean
) {
    val (icon, title, description) = getActionDisplayInfo(action)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step number
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = colors.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (index + 1).toString(),
                        color = colors.onPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = colors.secondary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = colors.secondary)
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
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = colors.error.copy(alpha = 0.7f)
                    )
                }
            }

            // Reorder buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = canMoveUp,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = "Move up",
                        tint = if (canMoveUp) colors.onSurface.copy(alpha = 0.6f)
                               else colors.onSurface.copy(alpha = 0.2f)
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = canMoveDown,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowDownward,
                        contentDescription = "Move down",
                        tint = if (canMoveDown) colors.onSurface.copy(alpha = 0.6f)
                               else colors.onSurface.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

/**
 * Gets the display information for an action.
 *
 * @param action The action to get info for
 * @return A triple of (icon, title, description)
 */
private fun getActionDisplayInfo(action: AutomationAction): Triple<ImageVector, String, String> {
    return when (action) {
        is AutomationAction.EnableEffectAction -> Triple(
            Icons.Default.AutoAwesome, "Enable Effect", action.effectId
        )
        is AutomationAction.DisableEffectAction -> Triple(
            Icons.Default.AutoAwesome, "Disable Effect", action.effectId
        )
        is AutomationAction.ToggleEffectAction -> Triple(
            Icons.Default.AutoAwesome, "Toggle Effect", action.effectId
        )
        is AutomationAction.ChangeThemeAction -> Triple(
            Icons.Default.Palette, "Change Theme", action.themeId
        )
        is AutomationAction.RandomThemeAction -> Triple(
            Icons.Default.Shuffle, "Random Theme", action.category ?: "Any category"
        )
        is AutomationAction.SetThemeIntensityAction -> Triple(
            Icons.Default.Tune, "Set Intensity", "${(action.intensity * 100).toInt()}%"
        )
        is AutomationAction.OpenAppAction -> Triple(
            Icons.Default.OpenInNew, "Open App", action.packageName
        )
        is AutomationAction.LaunchSugarMunchScreenAction -> Triple(
            Icons.Default.AppShortcut, "Open Screen", action.screen.name
        )
        is AutomationAction.ClaimRewardAction -> Triple(
            Icons.Default.CardGiftcard, "Claim Reward", "Daily reward"
        )
        is AutomationAction.AddSugarPointsAction -> Triple(
            Icons.Default.Star, "Add Points", "${action.points} points"
        )
        is AutomationAction.ShowNotificationAction -> Triple(
            Icons.Default.Notifications, "Show Notification", action.title
        )
        is AutomationAction.ShowToastAction -> Triple(
            Icons.Default.ChatBubble, "Show Toast", action.message.take(30)
        )
        is AutomationAction.VibrateAction -> Triple(
            Icons.Default.Vibration, "Vibrate", action.pattern.name.lowercase()
        )
        is AutomationAction.SetBrightnessAction -> Triple(
            Icons.Default.BrightnessMedium, "Set Brightness", "${(action.level * 100).toInt()}%"
        )
        is AutomationAction.WaitAction -> Triple(
            Icons.Default.Timer, "Wait", "${action.durationMs}ms"
        )
        else -> Triple(Icons.Default.Help, "Action", "Unknown action")
    }
}

/**
 * Dialog for selecting an action to add to the automation.
 *
 * @param colors The theme colors to use for styling
 * @param onDismiss Callback when the dialog is dismissed
 * @param onActionSelected Callback when an action is selected
 */
@Composable
private fun ActionPickerDialog(
    colors: AdjustedColors,
    onDismiss: () -> Unit,
    onActionSelected: (AutomationAction) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Action",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null)
                    }
                }

                // Categories
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCategories.categories.forEach { category ->
                        item {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }

                        items(category.actions) { actionItem ->
                            ActionPickerCard(
                                name = actionItem.name,
                                description = actionItem.description,
                                colors = colors,
                                onClick = { onActionSelected(actionItem.createAction()) }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

/**
 * A card in the action picker dialog.
 *
 * @param name The action name to display
 * @param description The action description to display
 * @param colors The theme colors to use for styling
 * @param onClick Callback when the card is clicked
 */
@Composable
private fun ActionPickerCard(
    name: String,
    description: String,
    colors: AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
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

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = colors.primary
            )
        }
    }
}
