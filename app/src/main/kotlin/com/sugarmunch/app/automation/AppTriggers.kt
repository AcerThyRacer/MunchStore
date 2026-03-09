package com.sugarmunch.app.automation

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

/**
 * App-based trigger evaluation functions.
 * Handles AppOpenedTrigger, AppClosedTrigger, and AppInstalledTrigger.
 */
internal class AppTriggerEvaluator(private val context: Context) {

    /**
     * Evaluates app opened triggers that fire when specified apps are launched.
     */
    fun evaluateAppOpenedTrigger(trigger: AutomationTrigger.AppOpenedTrigger): Flow<TriggerEvent> =
        callbackFlow {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == Intent.ACTION_MAIN &&
                        intent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
                        val packageName = intent.component?.packageName
                        if (packageName != null &&
                            (trigger.matchAll || packageName in trigger.packageNames)) {
                            trySend(TriggerEvent.AppOpenedEvent(packageName))
                        }
                    }
                }
            }

            val filter = IntentFilter(Intent.ACTION_MAIN)
            filter.addCategory(Intent.CATEGORY_LAUNCHER)
            context.registerReceiver(receiver, filter)

            awaitClose { context.unregisterReceiver(receiver) }
        }

    /**
     * Evaluates app closed triggers that fire when specified apps are closed.
     */
    fun evaluateAppClosedTrigger(trigger: AutomationTrigger.AppClosedTrigger): Flow<TriggerEvent> =
        flow {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            var previousApps = emptySet<String>()

            while (currentCoroutineContext().isActive) {
                val currentApps = activityManager.runningAppProcesses
                    ?.mapNotNull { it.processName }
                    ?.toSet() ?: emptySet()

                val closedApps = previousApps - currentApps
                closedApps.forEach { packageName ->
                    if (packageName in trigger.packageNames) {
                        emit(TriggerEvent.AppClosedEvent(packageName))
                    }
                }

                previousApps = currentApps
                delay(5000) // Check every 5 seconds
            }
        }

    /**
     * Evaluates app installed triggers that fire when new apps are installed.
     */
    fun evaluateAppInstalledTrigger(trigger: AutomationTrigger.AppInstalledTrigger): Flow<TriggerEvent> =
        callbackFlow {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == Intent.ACTION_PACKAGE_ADDED) {
                        val packageUri = intent.data
                        val packageName = packageUri?.schemeSpecificPart

                        if (packageName != null) {
                            val pattern = trigger.packageNamePattern
                            if (pattern == null || packageName.contains(pattern)) {
                                trySend(TriggerEvent.AppInstalledEvent(packageName))
                            }
                        }
                    }
                }
            }

            val filter = IntentFilter(Intent.ACTION_PACKAGE_ADDED)
            filter.addDataScheme("package")
            context.registerReceiver(receiver, filter)

            awaitClose { context.unregisterReceiver(receiver) }
        }

    companion object {
        private const val TAG = "AppTriggerEvaluator"
    }
}
