package com.sugarmunch.tv

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * TV Application class for SugarMunch
 * Initializes Hilt dependency injection
 */
@HiltAndroidApp
class SugarMunchTvApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize TV-specific components
        initializeTvComponents()
    }
    
    private fun initializeTvComponents() {
        // Initialize TV recommendations
        // Setup background sync
        // Configure image loading for TV
    }
}
