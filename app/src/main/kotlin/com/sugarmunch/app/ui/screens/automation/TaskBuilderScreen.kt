package com.sugarmunch.app.ui.screens.automation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.automation.AutomationRepository
import com.sugarmunch.app.automation.AutomationEngine
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.launch

/**
 * Main screen for building and editing automation tasks.
 *
 * Provides a step-by-step wizard for creating automations:
 * 1. Select a trigger that starts the automation
 * 2. Configure optional conditions
 * 3. Add actions to execute
 * 4. Review and save the automation
 *
 * Supports both creating new automations and editing existing ones.
 * Can also load from templates.
 *
 * @param taskId Optional ID of an existing task to edit
 * @param templateId Optional ID of a template to load
 * @param onBack Callback when user wants to exit without saving
 * @param onTaskCreated Callback when task is successfully created or updated
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBuilderScreen(
    taskId: String? = null,
    templateId: String? = null,
    onBack: () -> Unit,
    onTaskCreated: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)

    val builderState = remember { VisualTaskBuilderState() }
    val currentStep by builderState.currentStep.collectAsState()

    val repository = remember { AutomationRepository.getInstance(context) }
    val engine = remember { AutomationEngine.getInstance(context) }
    val scope = rememberCoroutineScope()

    var showExitDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Load existing task if editing
    LaunchedEffect(taskId) {
        taskId?.let { id ->
            isLoading = true
            val task = repository.getTaskById(id)
            task?.let { builderState.loadTask(it) }
            isLoading = false
        }
    }

    // Load template if provided
    LaunchedEffect(templateId) {
        templateId?.let { id ->
            AutomationTemplates.getTemplateById(id)?.let { template ->
                val task = template.createTask()
                builderState.loadTask(task)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (taskId != null) "Edit Automation" else "New Automation",
                        color = colors.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.Default.Close, "Close", tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
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

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.primary
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Step indicator
                    StepIndicator(
                        currentStep = currentStep,
                        colors = colors
                    )

                    // Step content
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState.ordinal > initialState.ordinal) {
                                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                            } else {
                                slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                            }
                        },
                        label = "step_content"
                    ) { step ->
                        when (step) {
                            BuilderStep.TRIGGER -> TriggerStep(
                                builderState = builderState,
                                colors = colors
                            )
                            BuilderStep.CONDITIONS -> ConditionsStep(
                                builderState = builderState,
                                colors = colors
                            )
                            BuilderStep.ACTIONS -> ActionsStep(
                                builderState = builderState,
                                colors = colors
                            )
                            BuilderStep.REVIEW -> ReviewStep(
                                builderState = builderState,
                                colors = colors
                            )
                        }
                    }
                }
            }
        }
    }

    // Bottom navigation buttons
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = colors.surface.copy(alpha = 0.95f),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back button (except on first step)
                if (currentStep != BuilderStep.TRIGGER) {
                    OutlinedButton(
                        onClick = { builderState.previousStep() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.onSurface
                        )
                    ) {
                        Icon(Icons.Default.ArrowBack, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Next/Create button
                Button(
                    onClick = {
                        if (currentStep == BuilderStep.REVIEW) {
                            // Save task
                            scope.launch {
                                builderState.buildTask()?.let { task ->
                                    repository.saveTask(task)
                                    onTaskCreated()
                                }
                            }
                        } else {
                            builderState.nextStep()
                        }
                    },
                    enabled = when (currentStep) {
                        BuilderStep.TRIGGER -> builderState.selectedTrigger.value != null
                        BuilderStep.ACTIONS -> builderState.selectedActions.value.isNotEmpty()
                        else -> true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Text(
                        if (currentStep == BuilderStep.REVIEW) {
                            if (taskId != null) "Save Changes" else "Create Automation"
                        } else "Next"
                    )
                    if (currentStep != BuilderStep.REVIEW) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, null)
                    }
                }
            }
        }
    }

    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Discard Changes?") },
            text = { Text("Your progress will be lost if you go back now.") },
            confirmButton = {
                TextButton(onClick = onBack) {
                    Text("Discard", color = colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Keep Editing")
                }
            },
            containerColor = colors.surface
        )
    }
}
