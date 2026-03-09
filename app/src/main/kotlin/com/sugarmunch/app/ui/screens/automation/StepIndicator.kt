package com.sugarmunch.app.ui.screens.automation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.model.AdjustedColors

/**
 * Displays a horizontal step indicator for the task builder wizard.
 *
 * Shows the four steps (Trigger, Conditions, Actions, Review) with visual
 * indicators for completed, active, and pending states.
 *
 * @param currentStep The currently active step in the wizard
 * @param colors The theme colors to use for styling
 */
@Composable
fun StepIndicator(
    currentStep: BuilderStep,
    colors: AdjustedColors
) {
    val steps = listOf("Trigger", "Conditions", "Actions", "Review")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, stepName ->
            val isActive = index <= currentStep.ordinal
            val isCurrent = index == currentStep.ordinal

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = when {
                                isCurrent -> colors.primary
                                isActive -> colors.primary.copy(alpha = 0.5f)
                                else -> colors.onSurface.copy(alpha = 0.1f)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive && !isCurrent) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = colors.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = (index + 1).toString(),
                            color = if (isActive) colors.onPrimary else colors.onSurface.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = stepName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrent) colors.primary
                           else if (isActive) colors.onSurface
                           else colors.onSurface.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .height(2.dp)
                        .padding(top = 16.dp)
                        .background(
                            color = if (index < currentStep.ordinal) colors.primary
                                   else colors.onSurface.copy(alpha = 0.1f)
                        )
                )
            }
        }
    }
}
