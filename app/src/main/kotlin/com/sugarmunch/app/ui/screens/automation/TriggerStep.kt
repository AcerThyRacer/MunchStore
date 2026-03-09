package com.sugarmunch.app.ui.screens.automation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.model.AdjustedColors

/**
 * The first step in the task builder wizard - trigger selection.
 *
 * Displays a list of available triggers organized by category.
 * Users can select one trigger to start their automation.
 *
 * @param builderState The state holder for the task builder
 * @param colors The theme colors to use for styling
 */
@Composable
fun TriggerStep(
    builderState: VisualTaskBuilderState,
    colors: AdjustedColors
) {
    val selectedTrigger by builderState.selectedTrigger.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Choose a Trigger",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )

            Text(
                text = "What will start this automation?",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
        }

        TriggerCategories.categories.forEach { category ->
            item {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            items(category.triggers) { triggerItem ->
                TriggerCard(
                    name = triggerItem.name,
                    description = triggerItem.description,
                    isSelected = selectedTrigger?.let {
                        it::class == triggerItem.createTrigger()::class
                    } ?: false,
                    colors = colors,
                    onClick = { builderState.setTrigger(triggerItem.createTrigger()) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
