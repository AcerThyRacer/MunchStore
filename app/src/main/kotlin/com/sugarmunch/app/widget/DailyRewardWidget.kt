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
import com.sugarmunch.app.rewards.DailyRewardsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Daily Rewards Widget - Shows streak count and claim status
 * Updates automatically and opens the app when clicked
 */
class DailyRewardWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Widget added to home screen
    }

    override fun onDisabled(context: Context) {
        // Widget removed from home screen
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_daily_reward)
            
            // Get rewards manager
            val rewardsManager = DailyRewardsManager.getInstance(context)
            
            // Get current data
            CoroutineScope(Dispatchers.IO).launch {
                val streak = rewardsManager.currentStreak.first()
                val canClaim = rewardsManager.canClaimToday.first()
                
                // Update widget UI
                views.setTextViewText(R.id.widget_streak_count, streak.toString())
                views.setTextViewText(
                    R.id.widget_status_text,
                    if (canClaim) "Claim now!" else "Come back tomorrow"
                )
                
                // Set click action
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("open_rewards", true)
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                
                // Update widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, DailyRewardWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            
            appWidgetIds.forEach { appWidgetId ->
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}
