package com.sugarmunch.app.theme.builder

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.Gson

/**
 * Dialog for exporting and importing theme configurations.
 *
 * Supports two modes:
 * - [ExportImportMode.EXPORT]: Displays a shareable theme code
 * - [ExportImportMode.IMPORT]: Allows pasting a theme code to import
 *
 * @param mode Whether to export or import
 * @param theme The theme to export (null for import mode)
 * @param onDismiss Callback when dialog is dismissed
 * @param onImport Callback when a theme is successfully imported (import mode only)
 */
@Composable
fun ExportImportDialog(
    mode: ExportImportMode,
    theme: CustomTheme?,
    onDismiss: () -> Unit,
    onImport: (CustomTheme) -> Unit = {}
) {
    var importCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (mode == ExportImportMode.EXPORT) "Export Theme" else "Import Theme") },
        text = {
            if (mode == ExportImportMode.EXPORT) {
                // Generate theme code
                val themeCode = theme?.toExportCode() ?: "Error generating code"
                Text(
                    text = "Share this code with others:\n\n$themeCode",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Column {
                    Text(
                        text = "Paste a theme code to import:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importCode,
                        onValueChange = { importCode = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            if (mode == ExportImportMode.EXPORT) {
                Button(onClick = {
                    // Copy to clipboard
                    onDismiss()
                }) {
                    Text("Copy")
                }
            } else {
                Button(
                    onClick = {
                        // Parse and import
                        val importedTheme = CustomTheme.fromExportCode(importCode)
                        if (importedTheme != null) {
                            onImport(importedTheme)
                            onDismiss()
                        }
                    },
                    enabled = importCode.isNotBlank()
                ) {
                    Text("Import")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Mode for the export/import dialog.
 */
enum class ExportImportMode { EXPORT, IMPORT }
