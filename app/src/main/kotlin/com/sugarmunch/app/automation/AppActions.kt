package com.sugarmunch.app.automation

import android.content.Context
import android.content.Intent
import com.sugarmunch.app.MainActivity

/**
 * App-related automation actions execution logic.
 * Handles opening apps, launching SugarMunch screens, and sharing the app.
 */

/**
 * Execute OpenApp action
 * Opens the specified application by package name
 */
suspend fun ActionExecutor.executeOpenApp(action: AutomationAction.OpenAppAction): ActionResult {
    return try {
        val intent = context.packageManager.getLaunchIntentForPackage(action.packageName)
            ?: return ActionResult.Failure("App not found: ${action.packageName}")

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (action.bringToFront) {
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }

        context.startActivity(intent)
        ActionResult.Success("Opened app: ${action.packageName}")
    } catch (e: Exception) {
        ActionResult.Failure(e.message ?: "Failed to open app")
    }
}

/**
 * Execute LaunchSugarMunchScreen action
 * Opens a specific screen within the SugarMunch app
 */
suspend fun ActionExecutor.executeLaunchSugarMunchScreen(
    action: AutomationAction.LaunchSugarMunchScreenAction
): ActionResult {
    return try {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("destination", action.screen.name)
        }

        context.startActivity(intent)
        ActionResult.Success("Opened SugarMunch screen: ${action.screen}")
    } catch (e: Exception) {
        ActionResult.Failure(e.message ?: "Failed to open SugarMunch screen")
    }
}

/**
 * Execute ShareApp action
 * Opens the system share dialog for the specified app
 */
suspend fun ActionExecutor.executeShareApp(action: AutomationAction.ShareAppAction): ActionResult {
    return try {
        val appInfo = context.packageManager.getApplicationInfo(action.packageName, 0)
        val appName = context.packageManager.getApplicationLabel(appInfo).toString()

        val message = action.customMessage ?: "Check out $appName!"

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_SUBJECT, "Share $appName")
        }

        val chooser = Intent.createChooser(shareIntent, "Share via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)

        ActionResult.Success("Share dialog opened for: $appName")
    } catch (e: Exception) {
        ActionResult.Failure(e.message ?: "Failed to share app")
    }
}
