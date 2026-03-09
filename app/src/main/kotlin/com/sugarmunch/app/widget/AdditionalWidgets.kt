package com.sugarmunch.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.sugarmunch.app.MainActivity
import com.sugarmunch.app.R
import com.sugarmunch.app.rewards.DailyRewardsManager
import com.sugarmunch.app.shop.ShopManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private fun openAppPendingIntent(context: Context, requestCode: Int): PendingIntent {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    return PendingIntent.getActivity(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

private fun buildStatusWidget(
    context: Context,
    title: String,
    subtitle: String,
    requestCode: Int
): RemoteViews {
    return RemoteViews(context.packageName, R.layout.widget_daily_reward).apply {
        setTextViewText(R.id.widget_streak_count, title)
        setTextViewText(R.id.widget_status_text, subtitle)
        setOnClickPendingIntent(R.id.widget_container, openAppPendingIntent(context, requestCode))
    }
}

class EffectToggleWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(
                id,
                buildStatusWidget(context, "FX", "Open SugarMunch effects", id)
            )
        }
    }
}

class SugarPointsWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val points = runBlocking { ShopManager.getInstance(context).sugarPoints.first() }
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(
                id,
                buildStatusWidget(context, points.toString(), "Sugar Points", id)
            )
        }
    }
}

class DailyRewardQuickWidget : AppWidgetProvider() {
    companion object {
        const val ACTION_CLAIM = "com.sugarmunch.action.CLAIM_REWARD"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val manager = DailyRewardsManager.getInstance(context)
        val streak = runBlocking { manager.currentStreak.first() }
        val canClaim = runBlocking { manager.canClaimToday.first() }
        val subtitle = if (canClaim) "Tap to claim today's reward" else "Come back tomorrow"

        appWidgetIds.forEach { id ->
            val views = buildStatusWidget(context, streak.toString(), subtitle, id).apply {
                val claimIntent = Intent(context, DailyRewardQuickWidget::class.java).apply {
                    action = ACTION_CLAIM
                }
                setOnClickPendingIntent(
                    R.id.widget_container,
                    PendingIntent.getBroadcast(
                        context,
                        id,
                        claimIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_CLAIM) {
            CoroutineScope(Dispatchers.IO).launch {
                DailyRewardsManager.getInstance(context).claimDailyReward()
            }
        }
    }
}

class TrendingAppsWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(
                id,
                buildStatusWidget(context, "Hot", "Trending apps live in the catalog", id)
            )
        }
    }
}

class AchievementProgressWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(
                id,
                buildStatusWidget(context, "🏆", "Track achievement progress in-app", id)
            )
        }
    }
}
