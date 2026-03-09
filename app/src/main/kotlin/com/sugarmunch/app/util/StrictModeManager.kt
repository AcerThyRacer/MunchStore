package com.sugarmunch.app.util

import android.os.Build
import android.os.StrictMode
import com.sugarmunch.app.BuildConfig

/**
 * StrictMode configuration for detecting accidental disk/network access on main thread
 * Only enabled in debug builds
 */
object StrictModeManager {

    fun enable() {
        if (!BuildConfig.DEBUG) return

        enableStrictMode()
        enableVmPolicy()
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll() // Detect all disk and network access
                .penaltyLog()
                .penaltyFlashScreen() // Visual indicator
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        penaltyListener({ it }, { violation ->
                            // Log violations for debugging
                            android.util.Log.w("StrictMode", "Thread policy violation", violation)
                        })
                    }
                }
                .build()
        )
    }

    private fun enableVmPolicy() {
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll() // Detect all memory leaks and VM issues
                .penaltyLog()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectLeakedRegistrationObjects()
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        detectCredentialProtectedWhileLocked()
                        detectImplicitDirectBoot()
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        detectIncorrectContextUse()
                        detectUnsafeIntentLaunch()
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        detectNonSdkApiUsage()
                    }
                }
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        penaltyListener({ it }, { violation ->
                            android.util.Log.w("StrictMode", "VM policy violation", violation)
                        })
                    }
                }
                .build()
        )
    }

    fun disable() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)
    }

    /**
     * Temporarily allow disk reads (use sparingly!)
     */
    inline fun <T> allowDiskReads(block: () -> T): T {
        val oldPolicy = StrictMode.getThreadPolicy()
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder(oldPolicy)
                .permitDiskReads()
                .build()
        )
        return try {
            block()
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
    }

    /**
     * Temporarily allow disk writes (use sparingly!)
     */
    inline fun <T> allowDiskWrites(block: () -> T): T {
        val oldPolicy = StrictMode.getThreadPolicy()
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder(oldPolicy)
                .permitDiskWrites()
                .build()
        )
        return try {
            block()
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
    }

    /**
     * Temporarily allow network access (use sparingly!)
     */
    inline fun <T> allowNetwork(block: () -> T): T {
        val oldPolicy = StrictMode.getThreadPolicy()
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder(oldPolicy)
                .permitNetwork()
                .build()
        )
        return try {
            block()
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
    }
}
