package com.sugarmunch.app.config

import com.sugarmunch.app.BuildConfig

/**
 * Feature flags for gating new or risky features.
 * Can be backed by Firebase Remote Config or BuildConfig for compile-time flags.
 *
 * Usage: if (FeatureFlags.isPluginSystemEnabled) { ... }
 */
object FeatureFlags {

    /** Plugin install/update flows. Disable remotely if needed. */
    val isPluginSystemEnabled: Boolean
        get() = true

    /** P2P sharing. Can be disabled for compliance or rollout. */
    val isP2PEnabled: Boolean
        get() = true

    /** Clan/social features. */
    val isClanEnabled: Boolean
        get() = true

    /** Trading / marketplace. */
    val isTradingEnabled: Boolean
        get() = true

    /** Use Remote Config in release for remote toggles. */
    private val useRemoteConfig: Boolean
        get() = !BuildConfig.DEBUG
}
