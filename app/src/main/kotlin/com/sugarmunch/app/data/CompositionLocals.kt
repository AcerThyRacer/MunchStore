package com.sugarmunch.app.data

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal for PreferencesRepository so composables can access it without
 * manual construction. Set in MainActivity via CompositionLocalProvider.
 */
val LocalPreferencesRepository = staticCompositionLocalOf<PreferencesRepository> {
    error("No PreferencesRepository provided. Ensure CompositionLocalProvider(LocalPreferencesRepository provides ...) is set at the root.")
}
