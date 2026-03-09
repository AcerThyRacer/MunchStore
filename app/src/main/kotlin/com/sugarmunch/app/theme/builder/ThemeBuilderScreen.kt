package com.sugarmunch.app.theme.builder

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Custom Theme Builder - Create unique themes for SugarMunch
 *
 * Features:
 * - Visual color picker with palette
 * - Gradient editor (linear, radial)
 * - Particle system editor
 * - Live preview
 * - Export/import theme codes
 * - Community sharing
 *
 * @param onThemeCreated Callback when a new theme is created
 * @param onNavigateBack Callback when user navigates back
 * @param modifier Optional modifier for the root composable
 */
@Composable
fun ThemeBuilderScreen(
    onThemeCreated: (CustomTheme) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(0) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    // Theme building state
    val themeState = remember { mutableStateOf(ThemeBuilderState()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Custom Theme") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Outlined.FileDownload, contentDescription = "Import")
                    }
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Outlined.FileUpload, contentDescription = "Export")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = (currentStep + 1).toFloat() / 4,
                modifier = Modifier.fillMaxWidth()
            )

            // Step indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StepIndicator(step = 1, title = "Colors", isActive = currentStep == 0, isCompleted = currentStep > 0)
                StepIndicator(step = 2, title = "Gradient", isActive = currentStep == 1, isCompleted = currentStep > 1)
                StepIndicator(step = 3, title = "Effects", isActive = currentStep == 2, isCompleted = currentStep > 2)
                StepIndicator(step = 4, title = "Preview", isActive = currentStep == 3, isCompleted = currentStep > 3)
            }

            // Content based on current step
            when (currentStep) {
                0 -> ColorPickerStep(
                    state = themeState.value,
                    onStateChange = { themeState.value = it }
                )
                1 -> GradientEditorStep(
                    state = themeState.value,
                    onStateChange = { themeState.value = it }
                )
                2 -> EffectsEditorStep(
                    state = themeState.value,
                    onStateChange = { themeState.value = it }
                )
                3 -> PreviewStep(
                    state = themeState.value,
                    onThemeCreated = {
                        onThemeCreated(it.toCustomTheme())
                        onNavigateBack()
                    }
                )
            }

            // Navigation buttons
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { if (currentStep > 0) currentStep-- },
                    enabled = currentStep > 0
                ) {
                    Text("Back")
                }

                if (currentStep < 3) {
                    Button(
                        onClick = { currentStep++ }
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                } else {
                    Button(
                        onClick = {
                            onThemeCreated(themeState.value.toCustomTheme())
                            onNavigateBack()
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Theme")
                    }
                }
            }
        }
    }

    // Export/Import dialogs
    if (showExportDialog) {
        ExportImportDialog(
            mode = ExportImportMode.EXPORT,
            theme = themeState.value.toCustomTheme(),
            onDismiss = { showExportDialog = false }
        )
    }

    if (showImportDialog) {
        ExportImportDialog(
            mode = ExportImportMode.IMPORT,
            theme = null,
            onDismiss = { showImportDialog = false },
            onImport = { importedTheme ->
                themeState.value = importedTheme.toBuilderState()
            }
        )
    }
}
