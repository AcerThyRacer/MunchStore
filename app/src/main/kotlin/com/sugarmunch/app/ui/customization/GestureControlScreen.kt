package com.sugarmunch.app.ui.customization

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens

/**
 * EXTREME Gesture Control Center
 * Custom gesture mapping and sensitivity controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureControlScreen(
    onNavigateBack: () -> Unit,
    gestureMapping: GestureMapping,
    onGestureMappingChange: (GestureMapping) -> Unit
) {
    var selectedGesture by remember { mutableStateOf<GestureAction?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gesture Control") },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.lg)
        ) {
            // Tap Gestures
            item {
                TapGesturesSection(
                    gestureMapping = gestureMapping,
                    onGestureMappingChange = onGestureMappingChange,
                    onActionSelected = { selectedGesture = it }
                )
            }

            // Swipe Gestures
            item {
                SwipeGesturesSection(
                    gestureMapping = gestureMapping,
                    onGestureMappingChange = onGestureMappingChange,
                    onActionSelected = { selectedGesture = it }
                )
            }

            // Multi-Touch Gestures
            item {
                MultiTouchGesturesSection(
                    gestureMapping = gestureMapping,
                    onGestureMappingChange = onGestureMappingChange,
                    onActionSelected = { selectedGesture = it }
                )
            }

            // Gesture Sensitivity
            item {
                GestureSensitivitySection(
                    gestureMapping = gestureMapping,
                    onGestureMappingChange = onGestureMappingChange
                )
            }

            // Haptic Feedback per Gesture
            item {
                GestureHapticsSection(
                    gestureMapping = gestureMapping,
                    onGestureMappingChange = onGestureMappingChange
                )
            }
        }
    }
}

@Composable
private fun TapGesturesSection(
    gestureMapping: GestureMapping,
    onGestureMappingChange: (GestureMapping) -> Unit,
    onActionSelected: (GestureAction) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Tap Gestures",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.hotPink
            )

            // Single Tap
            GestureActionRow(
                label = "Single Tap",
                currentAction = gestureMapping.singleTap,
                onClick = { onActionSelected(gestureMapping.singleTap) }
            )

            // Double Tap
            GestureActionRow(
                label = "Double Tap",
                currentAction = gestureMapping.doubleTap,
                onClick = { onActionSelected(gestureMapping.doubleTap) }
            )

            // Long Press
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Long Press Duration",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "${gestureMapping.longPressDuration}ms",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Slider(
                    value = gestureMapping.longPressDuration.toFloat(),
                    onValueChange = {
                        onGestureMappingChange(gestureMapping.copy(longPressDuration = it.toInt()))
                    },
                    valueRange = 500f..3000f,
                    steps = 24
                )
            }

            // Long Press Action
            GestureActionRow(
                label = "Long Press Action",
                currentAction = gestureMapping.longPress,
                onClick = { onActionSelected(gestureMapping.longPress) }
            )
        }
    }
}

@Composable
private fun SwipeGesturesSection(
    gestureMapping: GestureMapping,
    onGestureMappingChange: (GestureMapping) -> Unit,
    onActionSelected: (GestureAction) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Swipe Gestures",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.mint
            )

            // Swipe Directions
            GestureActionRow(
                label = "Swipe Up",
                currentAction = gestureMapping.swipeUp,
                onClick = { onActionSelected(gestureMapping.swipeUp) }
            )

            GestureActionRow(
                label = "Swipe Down",
                currentAction = gestureMapping.swipeDown,
                onClick = { onActionSelected(gestureMapping.swipeDown) }
            )

            GestureActionRow(
                label = "Swipe Left",
                currentAction = gestureMapping.swipeLeft,
                onClick = { onActionSelected(gestureMapping.swipeLeft) }
            )

            GestureActionRow(
                label = "Swipe Right",
                currentAction = gestureMapping.swipeRight,
                onClick = { onActionSelected(gestureMapping.swipeRight) }
            )
        }
    }
}

@Composable
private fun MultiTouchGesturesSection(
    gestureMapping: GestureMapping,
    onGestureMappingChange: (GestureMapping) -> Unit,
    onActionSelected: (GestureAction) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Multi-Touch Gestures",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.candyOrange
            )

            // Pinch Gestures
            GestureActionRow(
                label = "Pinch In",
                currentAction = gestureMapping.pinchIn,
                onClick = { onActionSelected(gestureMapping.pinchIn) }
            )

            GestureActionRow(
                label = "Pinch Out",
                currentAction = gestureMapping.pinchOut,
                onClick = { onActionSelected(gestureMapping.pinchOut) }
            )

            // Two-Finger Swipe
            GestureActionRow(
                label = "Two-Finger Swipe",
                currentAction = gestureMapping.twoFingerSwipe,
                onClick = { onActionSelected(gestureMapping.twoFingerSwipe) }
            )

            // Circle Gesture
            GestureActionRow(
                label = "Circle Gesture",
                currentAction = gestureMapping.circleGesture,
                onClick = { onActionSelected(gestureMapping.circleGesture) }
            )

            // Figure-8 Gesture
            GestureActionRow(
                label = "Figure-8 Gesture",
                currentAction = gestureMapping.figure8Gesture,
                onClick = { onActionSelected(gestureMapping.figure8Gesture) }
            )
        }
    }
}

@Composable
private fun GestureActionRow(
    label: String,
    currentAction: GestureAction,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(SugarDimens.Radius.md),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SugarDimens.Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = onClick,
                    label = { Text(currentAction.name.replace("_", " ")) },
                    leadingIcon = {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Gesture,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.ChevronRight,
                    contentDescription = "Change"
                )
            }
        }
    }
}

@Composable
private fun GestureSensitivitySection(
    gestureMapping: GestureMapping,
    onGestureMappingChange: (GestureMapping) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Gesture Sensitivity",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.bubblegumBlue
            )

            // Swipe Distance Threshold
            SliderWithLabel(
                label = "Swipe Distance Threshold (${gestureMapping.swipeDistanceThreshold.toInt()}dp)",
                value = gestureMapping.swipeDistanceThreshold,
                onValueChange = {
                    onGestureMappingChange(gestureMapping.copy(swipeDistanceThreshold = it))
                },
                valueRange = 10f..200f,
                steps = 18
            )

            // Velocity Threshold
            Text("Velocity Threshold", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                VelocityThreshold.entries.forEach { threshold ->
                    FilterChip(
                        selected = gestureMapping.velocityThreshold == threshold,
                        onClick = {
                            onGestureMappingChange(gestureMapping.copy(velocityThreshold = threshold))
                        },
                        label = { Text(threshold.name) }
                    )
                }
            }

            // Angle Tolerance
            Text("Angle Tolerance", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                AngleTolerance.entries.forEach { tolerance ->
                    FilterChip(
                        selected = gestureMapping.angleTolerance == tolerance,
                        onClick = {
                            onGestureMappingChange(gestureMapping.copy(angleTolerance = tolerance))
                        },
                        label = { Text(tolerance.name.replace("_", "° ")) }
                    )
                }
            }

            // Multi-Touch Timeout
            SliderWithLabel(
                label = "Multi-Touch Timeout (${gestureMapping.multiTouchTimeout}ms)",
                value = gestureMapping.multiTouchTimeout.toFloat(),
                onValueChange = {
                    onGestureMappingChange(gestureMapping.copy(multiTouchTimeout = it.toInt()))
                },
                valueRange = 100f..500f,
                steps = 7
            )
        }
    }
}

@Composable
private fun GestureHapticsSection(
    gestureMapping: GestureMapping,
    onGestureMappingChange: (GestureMapping) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Haptic Feedback per Gesture",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.yellow
            )

            Text(
                text = "Configure vibration patterns for each gesture type",
                style = MaterialTheme.typography.bodySmall
            )

            // Gesture-specific haptic toggles
            SwitchWithLabel(
                label = "Haptics for tap gestures",
                checked = true,
                onCheckedChange = { }
            )

            SwitchWithLabel(
                label = "Haptics for swipe gestures",
                checked = true,
                onCheckedChange = { }
            )

            SwitchWithLabel(
                label = "Haptics for multi-touch gestures",
                checked = true,
                onCheckedChange = { }
            )

            // Intensity slider
            SliderWithLabel(
                label = "Gesture Haptic Intensity (50%)",
                value = 0.5f,
                onValueChange = { },
                valueRange = 0f..1f,
                steps = 19
            )
        }
    }
}

@Composable
private fun SwitchWithLabel(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SliderWithLabel(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}
