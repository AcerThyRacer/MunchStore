package com.sugarmunch.sugarbeats

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import org.fossify.commons.extensions.checkUseEnglish
import com.sugarmunch.sugarbeats.helpers.SimpleMediaController

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        checkUseEnglish()
        initController()
    }

    private fun initController() {
        SimpleMediaController.getInstance(applicationContext).createControllerAsync()
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStop(owner: LifecycleOwner) {
                    SimpleMediaController.destroyInstance()
                }
            }
        )
    }
}
