package com.sugarmunch.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.sugarmunch.app.MainActivity
import com.sugarmunch.app.R

/**
 * Quick Install Widget - Shows featured apps for quick access
 * Opens app directly to install screen
 */
class QuickInstallWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_install)
            
            // Featured apps (would be loaded from cache)
            val featuredApps = listOf(
                "sugartube" to "Sugartube",
                "candystore" to "Candy Store",
                "example-app" to "Example App"
            )
            
            // Setup app buttons
            featuredApps.forEachIndexed { index, (appId, appName) ->
                val buttonId = when (index) {
                    0 -> R.id.widget_app_1
                    1 -> R.id.widget_app_2
                    2 -> R.id.widget_app_3
                    else -> null
                }
                
                buttonId?.let { id ->
                    views.setTextViewText(id, appName)
                    
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra("open_app_id", appId)
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        index,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(id, pendingIntent)
                }
            }
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, QuickInstallWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            
            appWidgetIds.forEach { appWidgetId ->
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}
